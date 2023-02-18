package com.monstarbill.configs.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monstarbill.configs.common.AppConstants;
import com.monstarbill.configs.common.CommonUtils;
import com.monstarbill.configs.common.CustomException;
import com.monstarbill.configs.common.CustomMessageException;
import com.monstarbill.configs.common.FilterNames;
import com.monstarbill.configs.dao.TaxGroupDao;
import com.monstarbill.configs.enums.Operation;
import com.monstarbill.configs.enums.Status;
import com.monstarbill.configs.models.TaxGroup;
import com.monstarbill.configs.models.TaxGroupHistory;
import com.monstarbill.configs.models.TaxRateRule;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.repository.TaxGroupHistoryRepository;
import com.monstarbill.configs.repository.TaxGroupRepository;
import com.monstarbill.configs.repository.TaxRateRuleRepository;
import com.monstarbill.configs.service.TaxGroupService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class TaxGroupServiceImpl implements TaxGroupService {

	@Autowired
	private TaxGroupRepository taxGroupRepository;

	@Autowired
	private TaxRateRuleRepository taxRateRuleRepository;

	@Autowired
	private TaxGroupHistoryRepository taxGroupHistoryRepository;

	@Autowired
	private TaxGroupDao taxGroupDao;

	@Override
	public TaxGroup save(TaxGroup taxGroup) {
		Long taxGroupId = null;
		Optional<TaxGroup> oldTaxGroup = Optional.empty();

		if (taxGroup.getId() == null) {
			taxGroup.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldTaxGroup = this.taxGroupRepository.findByIdAndIsDeleted(taxGroup.getId(), false);
			if (oldTaxGroup.isPresent()) {
				try {
					oldTaxGroup = Optional.ofNullable((TaxGroup) oldTaxGroup.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}

		taxGroup.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		if (taxGroup.isActive() == true) {
			taxGroup.setActiveDate(null);
		}
		TaxGroup savedTaxGroup;
		try {
			savedTaxGroup = this.taxGroupRepository.save(taxGroup);
		} catch (DataIntegrityViolationException e) {
			log.error("Tax group unique constrain violetd." + e.getMostSpecificCause());
			throw new CustomException("Tax group unique constrain violetd :" + e.getMostSpecificCause());
		}
		log.info("Tax group saved successfully.");
				
		if (savedTaxGroup != null) {
			if (oldTaxGroup.isPresent()) {
				// insert the updated fields in history table
				List<TaxGroupHistory> taxGroupHistories = new ArrayList<TaxGroupHistory>();
				try {
					taxGroupHistories = oldTaxGroup.get().compareFields(savedTaxGroup);
					if (CollectionUtils.isNotEmpty(taxGroupHistories)) {
						this.taxGroupHistoryRepository.saveAll(taxGroupHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException(
							"Error while comparing the new and old objects. Please contact administrator.");
				}
				log.info("Tax Group History is updated successfully");
			} else {
				// Insert in history table as Operation - INSERT
				this.taxGroupHistoryRepository.save(this.prepareTaxGroupHistory(savedTaxGroup.getId(), null,
						AppConstants.TAX_GROUP, Operation.CREATE.toString(), taxGroup.getLastModifiedBy(), null, null));
			}
			log.info("Tax Group Saved successfully.");
		} else {
			log.error("Error while saving the Tax Group.");
			throw new CustomMessageException("Error while saving the Tax Group");
		}
		log.info("Tax group History saved successfully.");

		// save for Tax Rate Rule
		if (CollectionUtils.isNotEmpty(taxGroup.getTaxRateRules())) {
			taxGroupId = savedTaxGroup.getId();

			log.info("Tax Rate Rule Started...");
			for (TaxRateRule taxRateRule : taxGroup.getTaxRateRules()) {
				taxRateRule.setTaxGroupId(taxGroupId);
				this.save(taxRateRule);
			}
			log.info("Tax Rate Rule Finished...");
			savedTaxGroup.setTaxRateRules(taxGroup.getTaxRateRules());
		}

		return savedTaxGroup;
	}

	/**
	 * Prepares the history for the Tax Group
	 * 
	 * @param taxGroupId
	 * @param moduleName
	 * @param operation
	 * @param lastModifiedBy
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public TaxGroupHistory prepareTaxGroupHistory(Long taxGroupId, Long childId, String moduleName, String operation,
			String lastModifiedBy, String oldValue, String newValue) {
		TaxGroupHistory taxGroupHistory = new TaxGroupHistory();
		taxGroupHistory.setTaxGroupId(taxGroupId);
		taxGroupHistory.setChildId(childId);
		taxGroupHistory.setModuleName(moduleName);
		taxGroupHistory.setChangeType(AppConstants.UI);
		taxGroupHistory.setOperation(operation);
		taxGroupHistory.setOldValue(oldValue);
		taxGroupHistory.setNewValue(newValue);
		taxGroupHistory.setLastModifiedBy(lastModifiedBy);
		return taxGroupHistory;
	}

	@Override
	public TaxGroup getTaxGroupById(Long id) {
		Optional<TaxGroup> taxGroup = Optional.empty();
		taxGroup = taxGroupRepository.findByIdAndIsDeleted(id, false);
		
		if (taxGroup.isPresent()) {
			Long taxGroupId = taxGroup.get().getId();

			List<TaxRateRule> TaxRateRules = taxRateRuleRepository.findByTaxGroupId(taxGroupId, false);
			if (CollectionUtils.isNotEmpty(TaxRateRules)) {
				taxGroup.get().setTaxRateRules(TaxRateRules);
			}

		} else {
			log.info("Tax Group Not Found against given Tax Group Id");
			throw new CustomMessageException("Tax Group Not Found against given Tax Group Id : " + id);
		}
		return taxGroup.get();
	}

	@Override
	public PaginationResponse findAll(PaginationRequest paginationRequest) {
		List<TaxGroup> taxGroups = new ArrayList<TaxGroup>();
		
		// preparing where clause
		String whereClause = this.prepareWhereClause(paginationRequest).toString();
		
		// get list
		taxGroups = this.taxGroupDao.findAll(whereClause, paginationRequest);
		
		// getting count
		Long totalRecords = this.taxGroupDao.getCount(whereClause);
		
		return CommonUtils.setPaginationResponse(paginationRequest.getPageNumber(), paginationRequest.getPageSize(), taxGroups, totalRecords);
	
	}

	private StringBuilder prepareWhereClause(PaginationRequest paginationRequest) {
		Map<String, ?> filters = paginationRequest.getFilters();
		
		String subsidiaryName = null;
		String name = null;
		String status = null;
		
		if (filters.containsKey(FilterNames.NAME))
			name = (String) filters.get(FilterNames.NAME);
		if (filters.containsKey(FilterNames.SUBSIDIARY_NAME))
			subsidiaryName = (String) filters.get(FilterNames.SUBSIDIARY_NAME);
		if (filters.containsKey(FilterNames.STATUS))
			status = (String) filters.get(FilterNames.STATUS);
		
		StringBuilder whereClause = new StringBuilder(" AND t.isDeleted is false ");
		if (StringUtils.isNotEmpty(subsidiaryName)) {
			whereClause.append(" AND lower(s.name) like lower ('%").append(subsidiaryName).append("%')");
		}
		if (StringUtils.isNotEmpty(name)) {
			whereClause.append(" AND lower(t.name) like lower('%").append(name).append("%')");
		}
		if (StringUtils.isNoneEmpty(status)) {
			if (Status.ACTIVE.toString().equalsIgnoreCase(status)) {
				whereClause.append(" AND t.isActive is true ");
			} else if (Status.INACTIVE.toString().equalsIgnoreCase(status)) {
				whereClause.append(" AND t.isActive is false ");
			}
		}

		return whereClause;
	}

	@Override
	public boolean deleteById(Long id) {
		TaxGroup taxGroup = new TaxGroup();
		taxGroup = this.getTaxGroupById(id);
		taxGroup.setDeleted(true);

		taxGroup = this.taxGroupRepository.save(taxGroup);

		if (taxGroup == null) {
			log.error("Error while deleting the TaxGroup : " + id);
			throw new CustomMessageException("Error while deleting the Taxgroup : " + id);
		}

		List<TaxRateRule> taxRateRule = this.taxRateRuleRepository.findByTaxGroupIdAndIsDeleted(id, false);
		for (TaxRateRule taxRateRules : taxRateRule) {
			taxRateRules.setDeleted(true);
			taxRateRules.setLastModifiedBy(CommonUtils.getLoggedInUsername());
			this.taxRateRuleRepository.save(taxRateRules);

			// update the operation in the history
			this.taxGroupHistoryRepository.save(this.prepareTaxGroupHistory(taxRateRules.getTaxGroupId(),
					taxRateRules.getId(), AppConstants.TAX_RATE_RULE, Operation.DELETE.toString(),
					taxRateRules.getLastModifiedBy(), String.valueOf(taxRateRules.getId()), null));
		}

		// update the operation in the history
		this.taxGroupHistoryRepository.save(this.prepareTaxGroupHistory(taxGroup.getId(), null, AppConstants.TAX_GROUP,
				Operation.DELETE.toString(), taxGroup.getLastModifiedBy(), String.valueOf(taxGroup.getId()), null));

		return true;
	}

	@Override
	public List<TaxGroupHistory> findHistoryById(Long id, Pageable pageable) {
		return this.taxGroupHistoryRepository.findByTaxGroupId(id, pageable);
	}

	// saving the TaxRate Rule

	@Override
	public TaxRateRule save(TaxRateRule taxRateRule) {
		Optional<TaxRateRule> oldTaxRateRule = Optional.empty();

		if (taxRateRule.getId() == null) {
			taxRateRule.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldTaxRateRule = this.taxRateRuleRepository.findByIdAndIsDeleted(taxRateRule.getId(), false);
			if (oldTaxRateRule.isPresent()) {
				try {
					oldTaxRateRule = Optional.ofNullable((TaxRateRule) oldTaxRateRule.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		taxRateRule.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		taxRateRule = this.taxRateRuleRepository.save(taxRateRule);

		if (taxRateRule == null) {
			log.info("Error while saving the Tax Rate Rule.");
			throw new CustomMessageException("Error while saving the Tax Rate Rule.");
		}
		log.info("Tax rate rule is saved :: " + taxRateRule.getId());

		if (oldTaxRateRule.isPresent()) {
			if (taxRateRule.isDeleted()) {
				// update history as delete
				this.taxGroupHistoryRepository.save(this.prepareTaxGroupHistory(taxRateRule.getTaxGroupId(),
						taxRateRule.getId(), AppConstants.TAX_RATE_RULE, Operation.DELETE.toString(),
						taxRateRule.getLastModifiedBy(), String.valueOf(taxRateRule.getId()), null));
			} else {
				// insert the updated fields in history table
				List<TaxGroupHistory> taxGroupHistories = new ArrayList<TaxGroupHistory>();
				try {
					taxGroupHistories = oldTaxRateRule.get().compareFields(taxRateRule);
					if (CollectionUtils.isNotEmpty(taxGroupHistories)) {
						this.taxGroupHistoryRepository.saveAll(taxGroupHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException(
							"Error while comparing the new and old objects. Please contact administrator.");
				}
			}
			log.info("Tax Rate Rule History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.taxGroupHistoryRepository.save(this.prepareTaxGroupHistory(taxRateRule.getTaxGroupId(),
					taxRateRule.getId(), AppConstants.TAX_RATE_RULE, Operation.CREATE.toString(),
					taxRateRule.getLastModifiedBy(), null, String.valueOf(taxRateRule.getId())));
			log.info("Tax Rate Rule History is updated successfully");
		}
		
		return taxRateRule;
	}

	@Override
	public boolean deleteTaxRateRuleById(Long id) {
		Optional<TaxRateRule> taxRateRule = Optional.ofNullable(null);
		taxRateRule = this.taxRateRuleRepository.findByIdAndIsDeleted(id, false);

		if (!taxRateRule.isPresent()) {
			log.error("Tax Rate Rule is not exist for ID - " + id);
			throw new CustomMessageException("Tax Rate Rule is not exist for ID - " + id);
		}

		taxRateRule.get().setDeleted(true);
		TaxRateRule taxRateRules = this.taxRateRuleRepository.save(taxRateRule.get());

		if (taxRateRules == null) {
			log.error("Error while deleting the Tax Rate Rule : " + id);
			throw new CustomMessageException("Error while deleting the Tax Rate Rule : " + id);
		}

		// update the operation in the history
		this.taxGroupHistoryRepository.save(this.prepareTaxGroupHistory(taxRateRules.getTaxGroupId(),
				taxRateRules.getId(), AppConstants.TAX_RATE_RULE, Operation.DELETE.toString(),
				taxRateRules.getLastModifiedBy(), String.valueOf(taxRateRules.getId()), null));

		return true;
	}

	@Override
	public List<TaxGroup> getTaxGroupBySubsidiaryId(Long subsidiaryId) {
		return this.taxGroupRepository.findBySubsidiaryId(subsidiaryId);
	}

	@Override
	public TaxGroup findByTaxGroupName(String name) {
		Optional<TaxGroup> taxGroup = this.taxGroupRepository.findByNameAndIsDeleted(name, false);
		if (taxGroup.isPresent()) return taxGroup.get();
		return null;
	}
}
