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
import com.monstarbill.configs.dao.TaxRateDao;
import com.monstarbill.configs.enums.Operation;
import com.monstarbill.configs.models.TaxRate;
import com.monstarbill.configs.models.TaxRateHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.repository.TaxRateHistoryRepository;
import com.monstarbill.configs.repository.TaxRateRepository;
import com.monstarbill.configs.service.TaxRateService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class TaxRateServiceImpl implements TaxRateService {

	@Autowired
	private TaxRateRepository taxRateRepository;

	@Autowired
	private TaxRateHistoryRepository taxRateHistoryRepository;
	
	@Autowired
	private TaxRateDao taxRateDao;
	
	@Override
	public TaxRate save(TaxRate taxRate) {

		Optional<TaxRate> oldTaxRate = Optional.ofNullable(null);

		if (taxRate.getId() == null) {
			taxRate.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldTaxRate = this.taxRateRepository.findByIdAndIsDeleted(taxRate.getId(), false);
			if (oldTaxRate.isPresent()) {
				try {
					oldTaxRate = Optional.ofNullable((TaxRate) oldTaxRate.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}

		taxRate.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		try {
			taxRate = taxRateRepository.save(taxRate);
		} catch (DataIntegrityViolationException e) {
			log.error("Tax Rate Rule unique constrain violetd." + e.getMostSpecificCause());
			throw new CustomException("Tax Rate Rule unique constrain violetd :" + e.getMostSpecificCause());
		}
		
		if (taxRate == null) {
			log.info("Error while saving the Tax Rate.");
			throw new CustomMessageException("Error while saving the Tax Rate.");
		}
		
		if (oldTaxRate.isPresent()) {
			// insert the updated fields in history table
			List<TaxRateHistory> taxRateHistories = new ArrayList<TaxRateHistory>();
			try {
				taxRateHistories = oldTaxRate.get().compareFields(taxRate);
				if (CollectionUtils.isNotEmpty(taxRateHistories)) {
					this.taxRateHistoryRepository.saveAll(taxRateHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException("Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Supplied History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT 
			this.taxRateHistoryRepository.save(this.prepareTaxRateHistory(taxRate.getId(), AppConstants.TAX_RATE, Operation.CREATE.toString(), taxRate.getLastModifiedBy(), null, String.valueOf(taxRate.getId())));
		}
		
		return taxRate;
	}
	
	/**
	 * Prepares the history for the TaxRate
	 * @param taxRateId
	 * @param moduleName
	 * @param operation
	 * @param lastModifiedBy
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public TaxRateHistory prepareTaxRateHistory(Long taxRateId, String moduleName, String operation, String lastModifiedBy, String oldValue, String newValue) {
		TaxRateHistory taxRateHistory = new TaxRateHistory();
		taxRateHistory.setTaxRateId(taxRateId);
		taxRateHistory.setModuleName(moduleName);
		taxRateHistory.setChangeType(AppConstants.UI);
		taxRateHistory.setOperation(operation);
		taxRateHistory.setOldValue(oldValue);
		taxRateHistory.setNewValue(newValue);
		taxRateHistory.setLastModifiedBy(lastModifiedBy);
		return taxRateHistory;
	}

	@Override
	public TaxRate getTaxRateById(Long id) {
		Optional<TaxRate> taxRate = Optional.ofNullable(new TaxRate());
		taxRate = taxRateRepository.findByIdAndIsDeleted(id, false);
		if (!taxRate.isPresent()) {
			log.info("Tax Rate Rule is not exist for id - " + id);
			throw new CustomMessageException("Tax Rate Rule is not exist for id - " + id);
		}
		return taxRate.get();
	}

	@Override
	public PaginationResponse findAllTaxRateRules(PaginationRequest paginationRequest) {
		List<TaxRate> taxRateRules = new ArrayList<TaxRate>();
		// preparing where clause
		String whereClause = this.prepareWhereClause(paginationRequest).toString();
		
		// get list
		taxRateRules = this.taxRateDao.findAll(whereClause, paginationRequest);
		
		// getting count
		Long totalRecords = this.taxRateDao.getCount(whereClause);
		
		return CommonUtils.setPaginationResponse(paginationRequest.getPageNumber(), paginationRequest.getPageSize(), taxRateRules, totalRecords);
	}

	private StringBuilder prepareWhereClause(PaginationRequest paginationRequest) {
		Map<String, ?> filters = paginationRequest.getFilters();

		String taxName = null;
		String taxType = null;

		if (filters.containsKey(FilterNames.NAME))
			taxName = (String) filters.get(FilterNames.NAME);
		if (filters.containsKey(FilterNames.TYPE))
			taxType = (String) filters.get(FilterNames.TYPE);
		
		StringBuilder whereClause = new StringBuilder(" AND t.isDeleted is false ");
		if (StringUtils.isNotEmpty(taxType)) {
			whereClause.append(" AND lower(t.taxType) like lower ('%").append(taxType).append("%')");
		}
		if (StringUtils.isNotEmpty(taxName)) {
			whereClause.append(" AND lower(t.taxName) like lower('%").append(taxName).append("%')");
		}
		return whereClause;
	}

	@Override
	public List<TaxRate> findAllTaxRateRulesWithFilters(String taxType, String taxName) {
		List<TaxRate> taxRateRules = new ArrayList<TaxRate>();
		taxRateRules = taxRateRepository.findAllWithFiltersByDeleted(taxType, taxName, false);
		return taxRateRules;
	}

	@Override
	public boolean deleteById(Long id) {
		TaxRate taxRate = new TaxRate();
		taxRate = this.getTaxRateById(id);
		taxRate.setDeleted(true);
		
		taxRate = this.taxRateRepository.save(taxRate);
		
		if (taxRate == null) {
			log.error("Error while deleting the Tax rate rules : " + id);
			throw new CustomMessageException("Error while deleting the Tax rate rules : " + id);
		}
		
		this.taxRateHistoryRepository.save(this.prepareTaxRateHistory(taxRate.getId(), AppConstants.TAX_RATE, Operation.DELETE.toString(), taxRate.getLastModifiedBy(), String.valueOf(taxRate.getId()), null));
		
		return true;
	}

	@Override
	public List<TaxRateHistory> findHistoryById(Long id, Pageable pageable) {
		return this.taxRateHistoryRepository.findByTaxRateId(id, pageable);
	}

	@Override
	public List<TaxRate> getTaxRateBySubsidiaryId(Long subsidiaryId) {
		List<TaxRate> taxRates =  new ArrayList<TaxRate>();
		taxRates = taxRateRepository.findBySubsidiaryIdAndIsDeleted(subsidiaryId, false);
		log.info("Get all tax rate by subsidiary id ." + taxRates);
		return taxRates;
	}

}
