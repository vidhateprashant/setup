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
import com.monstarbill.configs.dao.PreferencesDao;
import com.monstarbill.configs.enums.Operation;
import com.monstarbill.configs.enums.Status;
import com.monstarbill.configs.models.AccountingPreferences;
import com.monstarbill.configs.models.ApprovalRoutingPreference;
import com.monstarbill.configs.models.CostingPreferences;
import com.monstarbill.configs.models.GeneralPreference;
import com.monstarbill.configs.models.GeneralPreferenceHistory;
import com.monstarbill.configs.models.NumberingPreferences;
import com.monstarbill.configs.models.OtherPreference;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.repository.AccountingPreferencesRepository;
import com.monstarbill.configs.repository.ApprovalRoutingPreferenceRepository;
import com.monstarbill.configs.repository.CostingPreferencesRepository;
import com.monstarbill.configs.repository.GeneralPreferencesRepository;
import com.monstarbill.configs.repository.NumberingPreferencesRepository;
import com.monstarbill.configs.repository.OtherPreferenceRepository;
import com.monstarbill.configs.repository.PreferencesHistoryRepository;
import com.monstarbill.configs.service.PreferencesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PreferencesServiceImpl implements PreferencesService {

	@Autowired
	private GeneralPreferencesRepository generalPreferencesRepository;

	@Autowired
	private AccountingPreferencesRepository accountingPreferencesRepository;

	@Autowired
	private CostingPreferencesRepository costingPreferencesRepository;

	@Autowired
	private NumberingPreferencesRepository numberingPreferencesRepository;

	@Autowired
	private PreferencesHistoryRepository preferencesHistoryRepository;
	
	@Autowired
	private ApprovalRoutingPreferenceRepository approvalRoutingPreferenceRepository;
	
	@Autowired
	private OtherPreferenceRepository otherPreferenceRepository;

	@Autowired
	private PreferencesDao preferencesDao;

	@Override
	public GeneralPreference save(GeneralPreference preferences) {
		Long preferencesId = null;
		String username = CommonUtils.getLoggedInUsername();

		Optional<GeneralPreference> oldPreferences = Optional.empty();
		try {
			// 1. save the preferences
			if (preferences.getId() == null) {
				preferences.setCreatedBy(username);
			} else {
				// Get the existing object using the deep copy
				oldPreferences = this.generalPreferencesRepository.findByIdAndIsDeleted(preferences.getId(), false);
				if (oldPreferences.isPresent()) {
					try {
						oldPreferences = Optional.ofNullable((GeneralPreference) oldPreferences.get().clone());
					} catch (CloneNotSupportedException e) {
						log.error("Error while Cloning the object. Please contact administrator.");
						throw new CustomException("Error while Cloning the object. Please contact administrator.");
					}
				}
			}

			preferences.setLastModifiedBy(username);
			if (preferences.isActive() == true) {
				preferences.setActiveDate(null);
			}
			preferences.setActive(true);
			GeneralPreference savedPreferences;
			try {
				savedPreferences = this.generalPreferencesRepository.save(preferences);
			}catch (DataIntegrityViolationException e) {
				log.error(" Subsidiary value of genral prefrence must be unique :" + e.getMostSpecificCause());
				throw new CustomException("Subsidiary value of genral prefrence must be unique :" + e.getMostSpecificCause());
			}
			log.info("Save preferences Finished...");
			
			preferencesId = updatePreferencesHistory(oldPreferences, savedPreferences);
			log.info("Save preferences History Finished...");
			// ----------------------------------- preferences Finished -------------------------------------------------

			// ----------------------------------- 01. Accounting preferences started -------------------------------------------------
			log.info("Save Accounting preferences started...");
			AccountingPreferences accountingPreferences = preferences.getAccountingPreferences();
			if (accountingPreferences != null) {
				this.saveAccountingPreferences(preferencesId, accountingPreferences);
			}
			log.info("Save Accounting preference Finished...");
			// ----------------------------------- 01. Accounting preferences Finished -------------------------------------------------

			// ----------------------------------- 02. Costing preferences Started-------------------------------------------------
			log.info("Save preferences contact started...");
			CostingPreferences costingPreferences = preferences.getCostingPreferences();

			if (costingPreferences != null) {
				this.saveCostingPreferences(preferencesId, costingPreferences);
			}
			log.info("Save preferences contact Finished...");
			// ----------------------------------- 02. Costing preferences Finished -------------------------------------------------

			// ----------------------------------- 03. Numbering preferences Started-------------------------------------------------
			log.info("Save Numbering preferences Started...");
			List<NumberingPreferences> numberingPreferences = preferences.getNumberingPreferences();
			if (CollectionUtils.isNotEmpty(numberingPreferences)) {
				for (NumberingPreferences numberingPreference : numberingPreferences) {
					this.saveNumberingPreferences(preferencesId, numberingPreference);
				}
				
			}
			log.info("Save Numbering preferences Finished...");
			// ----------------------------------- 03. Numbering preferences Finished -------------------------------------------------
			
			// ----------------------------------- 04. Approval Routing preferences Started-------------------------------------------------
			log.info("Save Approval Routing preferences Started...");
			List<ApprovalRoutingPreference> approvalRoutingPreferences = preferences.getApprovalRoutingPreferences();
			if (CollectionUtils.isNotEmpty(approvalRoutingPreferences)) {
				for (ApprovalRoutingPreference approvalRoutingPreference : approvalRoutingPreferences) {
					this.saveApprovalRoutingPreference(preferencesId, approvalRoutingPreference);					
				}
			}
			log.info("Save Approval Routing preferences Finished...");
			// ----------------------------------- 04. Approval Routing preferences Finished -------------------------------------------------
			
			// ----------------------------------- 05. Other preferences Started-------------------------------------------------
			log.info("Save Other preferences Started...");
			List<OtherPreference> otherPreferences = preferences.getOtherPreferences();
			if (CollectionUtils.isNotEmpty(otherPreferences)) {
				for (OtherPreference otherPreference : otherPreferences) {
					this.saveOtherPreference(preferencesId, otherPreference);
				}
			}
			log.info("Save Other preferences Finished...");
			// ----------------------------------- 04. Approval Routing preferences Finished -------------------------------------------------

			System.gc();
			savedPreferences = this.getPreferencesById(savedPreferences.getId());
			return savedPreferences;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(e.toString());
		}
	}

	private void saveOtherPreference(Long preferenceId, OtherPreference otherPreference) {
		Optional<OtherPreference> oldOtherPreference = Optional.empty();

		if (otherPreference.getId() == null) {
			otherPreference.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldOtherPreference = this.otherPreferenceRepository.findByIdAndIsDeleted(otherPreference.getId(), false);
			if (oldOtherPreference.isPresent()) {
				try {
					oldOtherPreference = Optional.ofNullable((OtherPreference) oldOtherPreference.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}

		otherPreference.setPreferenceId(preferenceId);
		otherPreference.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		OtherPreference otherPreferenceSaved = this.otherPreferenceRepository.save(otherPreference);
		log.info("Other Preferences is saved");
		
		this.updateOtherPreferenceHistory(oldOtherPreference, otherPreferenceSaved);
		log.info("Other Preferences History is saved");
	}

	private void updateOtherPreferenceHistory(Optional<OtherPreference> oldOtherPreference, OtherPreference otherPreference) {
		if (otherPreference != null) {
			if (oldOtherPreference.isPresent()) {
				// insert the updated fields in history table
				List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
				try {
					preferencesHistories = oldOtherPreference.get().compareFields(otherPreference);
					if (CollectionUtils.isNotEmpty(preferencesHistories)) {
						this.preferencesHistoryRepository.saveAll(preferencesHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException("Error while comparing the new and old objects. Please contact administrator.");
				}
			} else {
				// Insert in history table as Operation - INSERT
				this.preferencesHistoryRepository.save(this.preparePreferencesHistory(otherPreference.getPreferenceId(),
								otherPreference.getId(), AppConstants.OTHER_PREFERENCE,
								Operation.CREATE.toString(), otherPreference.getLastModifiedBy(), null,
								String.valueOf(otherPreference.getId())));
			}
			log.info("Other Preferences Saved successfully.");
		} else {
			log.error("Error while saving the Other Preferences.");
			throw new CustomException("Error while saving the Other Preferences.");
		}
	}
	
	private void saveApprovalRoutingPreference(Long preferenceId, ApprovalRoutingPreference approvalRoutingPreference) {
		Optional<ApprovalRoutingPreference> oldApprovalRoutingPreference = Optional.empty();

		if (approvalRoutingPreference.getId() == null) {
			approvalRoutingPreference.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldApprovalRoutingPreference = this.approvalRoutingPreferenceRepository.findByIdAndIsDeleted(approvalRoutingPreference.getId(), false);
			if (oldApprovalRoutingPreference.isPresent()) {
				try {
					oldApprovalRoutingPreference = Optional.ofNullable((ApprovalRoutingPreference) oldApprovalRoutingPreference.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}

		approvalRoutingPreference.setPreferenceId(preferenceId);
		approvalRoutingPreference.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		ApprovalRoutingPreference approvalRoutingPreferenceSaved = this.approvalRoutingPreferenceRepository.save(approvalRoutingPreference);
		log.info("Approval Routing Preferences is saved");
		
		this.updateApprovalRoutingPreferenceHistory(oldApprovalRoutingPreference, approvalRoutingPreferenceSaved);
		log.info("Approval Routing Preferences History is saved");
	}
	
	private void updateApprovalRoutingPreferenceHistory(Optional<ApprovalRoutingPreference> oldApprovalRoutingPreference, ApprovalRoutingPreference approvalRoutingPreference) {
		if (approvalRoutingPreference != null) {
			if (oldApprovalRoutingPreference.isPresent()) {
				// insert the updated fields in history table
				List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
				try {
					preferencesHistories = oldApprovalRoutingPreference.get().compareFields(approvalRoutingPreference);
					if (CollectionUtils.isNotEmpty(preferencesHistories)) {
						this.preferencesHistoryRepository.saveAll(preferencesHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException("Error while comparing the new and old objects. Please contact administrator.");
				}
			} else {
				// Insert in history table as Operation - INSERT
				this.preferencesHistoryRepository.save(this.preparePreferencesHistory(approvalRoutingPreference.getPreferenceId(),
								approvalRoutingPreference.getId(), AppConstants.APPROVAL_ROUTING_PREFERENCE,
								Operation.CREATE.toString(), approvalRoutingPreference.getLastModifiedBy(), null,
								String.valueOf(approvalRoutingPreference.getId())));
			}
			log.info("Approval Routing Preferences Saved successfully.");
		} else {
			log.error("Error while saving the Approval Routing Preferences.");
			throw new CustomException("Error while saving the Approval Routing Preferences.");
		}
	}

	// save the Accounting Preferences
	private void saveAccountingPreferences(Long preferenceId, AccountingPreferences accountingPreferences) {
		Optional<AccountingPreferences> oldAccountingPreferences = Optional.empty();

		if (accountingPreferences.getId() == null) {
			accountingPreferences.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldAccountingPreferences = this.accountingPreferencesRepository.findByIdAndIsDeleted(accountingPreferences.getId(), false);
			if (oldAccountingPreferences.isPresent()) {
				try {
					oldAccountingPreferences = Optional.ofNullable((AccountingPreferences) oldAccountingPreferences.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}

		accountingPreferences.setPreferenceId(preferenceId);
		accountingPreferences.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		AccountingPreferences accountingPreferencesSaved = accountingPreferencesRepository.save(accountingPreferences);
		log.info("Accounting Preferences is saved");
		
		this.updateAccountingPreferencesHistory(oldAccountingPreferences, accountingPreferencesSaved);
		log.info("Accounting Preferences History is saved");
	}


	// save the Costing Preferences
	private void saveCostingPreferences(Long preferenceId, CostingPreferences costingPreference) {
		Optional<CostingPreferences> oldCostingPreferences = Optional.empty();

		if (costingPreference.getId() == null) {
			costingPreference.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldCostingPreferences = this.costingPreferencesRepository.findByIdAndIsDeleted(costingPreference.getId(), false);
			if (oldCostingPreferences.isPresent()) {
				try {
					oldCostingPreferences = Optional.ofNullable((CostingPreferences) oldCostingPreferences.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}

		costingPreference.setPreferenceId(preferenceId);
		costingPreference.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		CostingPreferences costingPreferencesSaved = costingPreferencesRepository.save(costingPreference);
		log.info("Costing Preference is saved.");

		this.updateCostingPreferencesHistory(oldCostingPreferences, costingPreferencesSaved);
		log.info("Costing Preference History is saved.");
	}

	// save the Numbering Preferences
	private void saveNumberingPreferences(Long preferenceId, NumberingPreferences numberingPreferences) {
		Optional<NumberingPreferences> oldNumberingPreferences = Optional.empty();

		if (String.valueOf(numberingPreferences.getMasterStartsWith()).length() > numberingPreferences.getMasterMaxDigit()) {
			throw new CustomMessageException("Number in starts with is greater than max length.");
		}
		
		if (numberingPreferences.getId() == null) {
			numberingPreferences.setMasterCurrentNumber(numberingPreferences.getMasterStartsWith());
			numberingPreferences.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldNumberingPreferences = this.numberingPreferencesRepository.findByIdAndIsDeleted(numberingPreferences.getId(), false);
			if (oldNumberingPreferences.isPresent()) {
				try {
					oldNumberingPreferences = Optional.ofNullable((NumberingPreferences) oldNumberingPreferences.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		
		if (numberingPreferences.getMasterCurrentNumber() == null) {
			numberingPreferences.setMasterCurrentNumber(numberingPreferences.getMasterStartsWith());
		}

		numberingPreferences.setPreferenceId(preferenceId);
		numberingPreferences.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		NumberingPreferences numberingPreferencesSaved = numberingPreferencesRepository.save(numberingPreferences);
		log.info("Numbering Preference is saved.");

		this.updateNumberingPreferencesHistory(oldNumberingPreferences, numberingPreferencesSaved);
		log.info("Numbering Preference History is saved.");
	}

	// update history of preferences
	private Long updatePreferencesHistory(Optional<GeneralPreference> oldGeneralPreference, GeneralPreference generalPreference) {
		Long preferencesId = null;
		if (generalPreference != null) {
			preferencesId = generalPreference.getId();

			if (oldGeneralPreference.isPresent()) {
				// insert the updated fields in history table
				List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
				try {
					preferencesHistories = oldGeneralPreference.get().compareFields(generalPreference);
					if (CollectionUtils.isNotEmpty(preferencesHistories)) {
						this.preferencesHistoryRepository.saveAll(preferencesHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException(
							"Error while comparing the new and old objects. Please contact administrator.");
				}
				log.info("Preferences History is updated successfully");
			} else {
				// Insert in history table as Operation - INSERT
				this.preferencesHistoryRepository
						.save(this.preparePreferencesHistory(preferencesId, null, AppConstants.PREFERENCE,
								Operation.CREATE.toString(), generalPreference.getLastModifiedBy(), null, String.valueOf(preferencesId)));
			}
			log.info("Preferences History Saved successfully.");
		} else {
			log.error("Error while saving the preferences.");
			throw new CustomException("Error while saving the preferences.");
		}
		return preferencesId;
	}

	// update the history of Accounting Preferences
	private void updateAccountingPreferencesHistory(Optional<AccountingPreferences> oldAccountingPreference, AccountingPreferences accountingPreference) {
		if (accountingPreference != null) {
			if (oldAccountingPreference.isPresent()) {
				// insert the updated fields in history table
				List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
				try {
					preferencesHistories = oldAccountingPreference.get().compareFields(accountingPreference);
					if (CollectionUtils.isNotEmpty(preferencesHistories)) {
						this.preferencesHistoryRepository.saveAll(preferencesHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException(
							"Error while comparing the new and old objects. Please contact administrator.");
				}
			} else {
				// Insert in history table as Operation - INSERT
				this.preferencesHistoryRepository.save(this.preparePreferencesHistory(accountingPreference.getPreferenceId(),
								accountingPreference.getId(), AppConstants.ACCOUNTING_PREFERENCE,
								Operation.CREATE.toString(), accountingPreference.getLastModifiedBy(), null,
								String.valueOf(accountingPreference.getId())));
			}
			log.info("Accounting Preferences Saved successfully.");
		} else {
			log.error("Error while saving the Accounting Preferences.");
			throw new CustomException("Error while saving the Accounting Preferences.");
		}
	}

// update the history of Numbering Preferences
	private void updateNumberingPreferencesHistory(Optional<NumberingPreferences> oldNumberingPreferences,
			NumberingPreferences numberingPreferences) {
		if (numberingPreferences != null) {
			if (oldNumberingPreferences.isPresent()) {
				// insert the updated fields in history table
				List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
				try {
					preferencesHistories = oldNumberingPreferences.get().compareFields(numberingPreferences);
					if (CollectionUtils.isNotEmpty(preferencesHistories)) {
						this.preferencesHistoryRepository.saveAll(preferencesHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException(
							"Error while comparing the new and old objects. Please contact administrator.");
				}
			} else {
				// Insert in history table as Operation - INSERT
				this.preferencesHistoryRepository.save(this.preparePreferencesHistory(
						numberingPreferences.getPreferenceId(), numberingPreferences.getId(),
						AppConstants.NUMBERING_PREFERENCE, Operation.CREATE.toString(),
						numberingPreferences.getLastModifiedBy(), null, String.valueOf(numberingPreferences.getId())));
			}
			log.info("Numbering Preferences Saved successfully.");
		} else {
			log.error("Error while saving the Numbering Preferences.");
			throw new CustomException("Error while saving the Numbering Preferences.");
		}
	}

	// update the history of Costing Preferences
	private void updateCostingPreferencesHistory(Optional<CostingPreferences> oldCostingPreferences, CostingPreferences costingPreferences) {
		if (costingPreferences != null) {
			if (oldCostingPreferences.isPresent()) {
				// insert the updated fields in history table
				List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
				try {
					preferencesHistories = oldCostingPreferences.get().compareFields(costingPreferences);
					if (CollectionUtils.isNotEmpty(preferencesHistories)) {
						this.preferencesHistoryRepository.saveAll(preferencesHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException(
							"Error while comparing the new and old objects. Please contact administrator.");
				}
			} else {
				// Insert in history table as Operation - INSERT
				this.preferencesHistoryRepository.save(this.preparePreferencesHistory(
						costingPreferences.getPreferenceId(), costingPreferences.getId(),
						AppConstants.COSTING_PREFERENCE, Operation.CREATE.toString(),
						costingPreferences.getLastModifiedBy(), null, String.valueOf(costingPreferences.getId())));
			}
			log.info("Costing Preferences Saved successfully.");
		} else {
			log.error("Error while saving the Costing Preferences.");
			throw new CustomException("Error while saving the Costing Preferences.");
		}
	}

	/**
	 * Prashant : 01-Jul-2022 Prepares the history objects for all forms and their
	 * child. Use this if you need single object only
	 * 
	 * @param moduleName
	 * @param operation
	 * @param lastModifiedBy
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public GeneralPreferenceHistory preparePreferencesHistory(Long preferencesId, Long childId, String moduleName,
			String operation, String lastModifiedBy, String oldValue, String newValue) {
		GeneralPreferenceHistory preferencesHistory = new GeneralPreferenceHistory();
		preferencesHistory.setPreferenceId(preferencesId);
		preferencesHistory.setChildId(childId);
		preferencesHistory.setModuleName(moduleName);
		preferencesHistory.setChangeType(AppConstants.UI);
		preferencesHistory.setOperation(operation);
		preferencesHistory.setOldValue(oldValue);
		preferencesHistory.setNewValue(newValue);
		preferencesHistory.setLastModifiedBy(lastModifiedBy);
		return preferencesHistory;
	}

	@Override
	public GeneralPreference getPreferencesById(Long id) {
		Optional<GeneralPreference> generalPreference = Optional.empty();
		generalPreference = this.generalPreferencesRepository.findByIdAndIsDeleted(id, false);

		if (generalPreference.isPresent()) {
			// get the Accounting Preferences
			Optional<AccountingPreferences> accountingPreference = this.accountingPreferencesRepository.findByPreferenceIdAndIsDeleted(id, false);
			if (accountingPreference.isPresent()) {
				generalPreference.get().setAccountingPreferences(accountingPreference.get());
			}

			// 2. Get Numbering Preferences
			List<NumberingPreferences> numberingPreference = this.numberingPreferencesRepository.findByPreferenceIdAndIsDeleted(id, false);
			if (CollectionUtils.isNotEmpty(numberingPreference)) {
				generalPreference.get().setNumberingPreferences(numberingPreference);
			}

			// 3. Get Costing
			Optional<CostingPreferences> costingPreference = this.costingPreferencesRepository.findByPreferenceIdAndIsDeleted(id, false);
			if (costingPreference.isPresent()) {
				generalPreference.get().setCostingPreferences(costingPreference.get());
			}
			
			// 4. Get Approval Routing Preferences
			List<ApprovalRoutingPreference> approvalRoutingPreference = this.approvalRoutingPreferenceRepository.findByPreferenceIdAndIsDeleted(id, false);
			if (CollectionUtils.isNotEmpty(approvalRoutingPreference)) {
				generalPreference.get().setApprovalRoutingPreferences(approvalRoutingPreference);
			}

			// 3. Get Costing
			List<OtherPreference> otherPreferences = this.otherPreferenceRepository.findByPreferenceIdAndIsDeleted(id, false);
			if (CollectionUtils.isNotEmpty(otherPreferences)) {
				generalPreference.get().setOtherPreferences(otherPreferences);
			}
						
			return generalPreference.get();
		} else {
			log.error("General Preference is Not Found against given preferences id : " + id);
			throw new CustomMessageException("General Preference is Not Found against given preferences id : " + id);
		}
	}

	@Override
	public List<GeneralPreferenceHistory> findAuditById(Long id, Pageable pageable) {
		return preferencesHistoryRepository.findByPreferenceId(id, pageable);
	}

	@Override
	public PaginationResponse findAll(PaginationRequest paginationRequest) {
		List<GeneralPreference> preferences = new ArrayList<GeneralPreference>();

		// preparing where clause
		String whereClause = this.prepareWhereClause(paginationRequest).toString();

		// get list
		preferences = this.preferencesDao.findAll(whereClause, paginationRequest);

		// getting count
		Long totalRecords = this.preferencesDao.getCount(whereClause);

		return CommonUtils.setPaginationResponse(paginationRequest.getPageNumber(), paginationRequest.getPageSize(),
				preferences, totalRecords);
	}

	private StringBuilder prepareWhereClause(PaginationRequest paginationRequest) {
		Map<String, ?> filters = paginationRequest.getFilters();

		Long subsidiaryId = null;

		if (filters.containsKey(FilterNames.SUBSIDIARY_ID))
			subsidiaryId = ((Number) filters.get(FilterNames.SUBSIDIARY_ID)).longValue();

		StringBuilder whereClause = new StringBuilder(" AND gp.isDeleted is false ");
		if (subsidiaryId !=null) {
			whereClause.append(" AND gp.subsidiaryId = ").append(subsidiaryId);
		}
		
		return whereClause;
	}

	@Override
	public String findPreferenceNumberByMaster(Long subsidiaryId, String masterName) {
		Optional<NumberingPreferences> numberingPreference = this.numberingPreferencesRepository.findBySubsidiaryAndMaster(subsidiaryId, masterName);
		if (!numberingPreference.isPresent()) {
			log.error("Preference configuration is not found against subsidiary & Master.");
			throw new CustomException("Preference configuration is not found against subsidiary & Master.");
		}
		StringBuilder preferenceNumber = new StringBuilder();
		
		if (StringUtils.isNotEmpty(numberingPreference.get().getMasterPrefix()) && numberingPreference.get().getMasterCurrentNumber() != null) {
			log.info("Prefix and current value is found. Now generating the preference Number. subsidiaryId : " + subsidiaryId + ", Master : " + masterName);
			preferenceNumber.append(numberingPreference.get().getMasterPrefix() + numberingPreference.get().getMasterCurrentNumber());			
		} else {
			log.error("Preference configuration is not found valid. Please validate the Prefix & other details.");
			throw new CustomException("Preference configuration is not found valid. Please validate the Prefix & other details.");
		}
		
		Optional<NumberingPreferences> existingNumberingPreference = this.numberingPreferencesRepository.findByIdAndIsDeleted(numberingPreference.get().getId(), false);
		existingNumberingPreference.get().setMasterCurrentNumber(existingNumberingPreference.get().getMasterCurrentNumber() + 1);
		this.numberingPreferencesRepository.save(existingNumberingPreference.get());
		
		log.info("Generated NUmber : " + preferenceNumber.toString());
		return preferenceNumber.toString();
	}

	@Override
	public List<String> findRoutingByStatus(Long subsidiaryId, String formType, String status) {
		boolean isRoutingActive = false;
		if (Status.ACTIVE.toString().equalsIgnoreCase(status)) isRoutingActive = true;
		return this.approvalRoutingPreferenceRepository.findRoutingByStatus(subsidiaryId, formType, isRoutingActive);
	}

	@Override
	public Boolean isCrossCurrencyActiveBySubsidiary(Long subsidiaryId) {
		List<OtherPreference> preferences = this.otherPreferenceRepository.findPreferenceActiveForCrossCurrenyBySubsidiary(subsidiaryId);
		if (CollectionUtils.isEmpty(preferences)) {
			return false;
		}
		return preferences.get(0).isPreferenceActive();
	}

}
