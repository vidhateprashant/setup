package com.monstarbill.configs.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.util.ResourceUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.DirectoryScopes;
import com.google.api.services.directory.model.User;
import com.google.api.services.directory.model.UserName;
import com.monstarbill.configs.common.AppConstants;
import com.monstarbill.configs.common.CommonUtils;
import com.monstarbill.configs.common.CustomException;
import com.monstarbill.configs.common.CustomMessageException;
import com.monstarbill.configs.dao.SubsidiaryDao;
import com.monstarbill.configs.enums.Operation;
import com.monstarbill.configs.models.Subsidiary;
import com.monstarbill.configs.models.SubsidiaryAddress;
import com.monstarbill.configs.models.SubsidiaryHistory;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.repository.SubsidiaryAddressRepository;
import com.monstarbill.configs.repository.SubsidiaryHistoryRepository;
import com.monstarbill.configs.repository.SubsidiaryRepository;
import com.monstarbill.configs.service.SubsidiaryService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class SubsidiaryServiceImpl implements SubsidiaryService {

	@Autowired
	private SubsidiaryRepository subsidiaryRepository;

	@Autowired
	private SubsidiaryAddressRepository subsidiaryAddressRepository;

	@Autowired
	private SubsidiaryHistoryRepository subsidiaryHistoryRepository;

	@Autowired
	private SubsidiaryDao subsidiaryDao;
	
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final List<String> SCOPES =
		      Collections.singletonList(DirectoryScopes.ADMIN_DIRECTORY_USER);

	@Override
	public Subsidiary save(Subsidiary subsidiary) {

		if (CollectionUtils.isEmpty(subsidiary.getSubsidiaryAddresses())) {
			log.error("Subsidiary address should not be empty.");
			throw new CustomMessageException("Subsidiary address should not be empty.");
		}

		Optional<Subsidiary> oldSubsidiary = Optional.empty();

		if (subsidiary.getId() == null) {
			subsidiary.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldSubsidiary = this.subsidiaryRepository.findByIdAndIsDeleted(subsidiary.getId(), false);
			if (oldSubsidiary.isPresent()) {
				try {
					oldSubsidiary = Optional.ofNullable((Subsidiary) oldSubsidiary.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		subsidiary.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		if (subsidiary.isActive() == true) {
			subsidiary.setActiveDate(null);
		}
		Subsidiary subsidiaryUpdated;
		try {
			subsidiaryUpdated = this.subsidiaryRepository.save(subsidiary);
		} catch (DataIntegrityViolationException e) {
			log.error(" Subsidiary unique constrain violetd." + e.getMostSpecificCause());
			throw new CustomException("Subsidiary unique constrain violetd :" + e.getMostSpecificCause());
		}

		if (subsidiaryUpdated == null) {
			log.info("Error while saving the subsidiary.");
			throw new CustomMessageException("Error while saving the subsidiary.");
		}

		// update the history table
		this.updateSubsidiarySaveHistory(subsidiaryUpdated, oldSubsidiary);
		log.info("Subsidiary saved successfully.");

		if (CollectionUtils.isNotEmpty(subsidiary.getSubsidiaryAddresses())) {
			Long subsidiaryId = subsidiaryUpdated.getId();

			log.info("Subsidiary Addresses Started.");
			for (SubsidiaryAddress subsidiaryAddress : subsidiary.getSubsidiaryAddresses()) {
				subsidiaryAddress.setSubsidiaryId(subsidiaryId);
				SubsidiaryAddress subsidiaryAddressSaved = this.saveAddress(subsidiaryAddress);

				if (subsidiaryAddressSaved == null) {
					log.info("Error while saving the subsidiary Address :: " + subsidiaryAddress.toString());
					throw new CustomMessageException("Error while saving the subsidiary Address.");
				}
			}
			log.info("Subsidiary Addresses Finished.");
			subsidiaryUpdated.setSubsidiaryAddresses(subsidiary.getSubsidiaryAddresses());
		}

		return subsidiaryUpdated;
	}

	/**
	 * Update the history table
	 * 
	 * @param subsidiary
	 * @param oldSubsidiary
	 */
	private void updateSubsidiarySaveHistory(Subsidiary subsidiary, Optional<Subsidiary> oldSubsidiary) {
		// add new entry if new record else enter the difference
		if (oldSubsidiary.isPresent()) {
			// insert the updated fields in history table
			List<SubsidiaryHistory> subsidiaryHistories = new ArrayList<SubsidiaryHistory>();
			try {
				subsidiaryHistories = oldSubsidiary.get().compareFields(subsidiary);
				if (CollectionUtils.isNotEmpty(subsidiaryHistories)) {
					this.subsidiaryHistoryRepository.saveAll(subsidiaryHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Supplied History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.subsidiaryHistoryRepository.save(this.prepareSubsidiaryHistory(subsidiary.getId(), null,
					AppConstants.SUBSIDIARY, Operation.CREATE.toString(), subsidiary.getLastModifiedBy(), null,
					String.valueOf(subsidiary.getId())));
		}
	}

	@Override
	public List<String> getParentCompanyNames() {
		return this.subsidiaryRepository.findDistinctSubsidiaryNames(false);
	}

	@Override
	public Subsidiary getSubsidiaryById(Long id) {
		Optional<Subsidiary> subsidiary = Optional.ofNullable(new Subsidiary());
		subsidiary = this.subsidiaryRepository.findByIdAndIsDeleted(id, false);
		if (!subsidiary.isPresent()) {
			log.info("Subsidiary is not found given id : " + id);
			throw new CustomMessageException("Subsidiary is not found given id : " + id);
		}
		subsidiary.get().setSubsidiaryAddresses(this.getAddressBySubsidiaryId(id));
		return subsidiary.get();
	}

	@Override
	public List<Subsidiary> getSubsidiariesByFilter(String startDate, String endDate) {
		return this.subsidiaryRepository.getAllWithFieldsAndDeletedFilters(startDate, endDate, false);
	}

	@Override
	public SubsidiaryAddress getAddressById(Long id) {
		Optional<SubsidiaryAddress> subsidiaryAddress = Optional.ofNullable(new SubsidiaryAddress());
		subsidiaryAddress = this.subsidiaryAddressRepository.findByIdAndIsDeleted(id, false);

		if (!subsidiaryAddress.isPresent()) {
			log.info("Address not found against the subsidiary : " + id);
			throw new CustomMessageException("Address not found against the subsidiary : " + id);
		}

		return subsidiaryAddress.get();
	}

	@Override
	public List<SubsidiaryAddress> getAddressBySubsidiaryId(Long subsidiaryId) {
		if (subsidiaryId == null) {
			throw new CustomMessageException("Subsidiary Id should be null.");
		}
		return this.subsidiaryAddressRepository.findBySubsidiaryIdAndIsDeleted(subsidiaryId, false);
	}

	@Override
	public SubsidiaryAddress saveAddress(SubsidiaryAddress subsidiaryAddress) {
		Optional<SubsidiaryAddress> oldSubsidiaryAddress = Optional.ofNullable(null);

		if (subsidiaryAddress.getId() == null) {
			subsidiaryAddress.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldSubsidiaryAddress = this.subsidiaryAddressRepository.findByIdAndIsDeleted(subsidiaryAddress.getId(),
					false);
			if (oldSubsidiaryAddress.isPresent()) {
				try {
					oldSubsidiaryAddress = Optional.ofNullable((SubsidiaryAddress) oldSubsidiaryAddress.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		subsidiaryAddress.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		subsidiaryAddress = this.subsidiaryAddressRepository.save(subsidiaryAddress);

		if (subsidiaryAddress == null) {
			log.info("Error while saving the subsidiary address.");
			throw new CustomMessageException("Error while saving the subsidiary Address.");
		}

		if (oldSubsidiaryAddress.isPresent()) {
			// insert the updated fields in history table
			List<SubsidiaryHistory> subsidiaryHistories = new ArrayList<SubsidiaryHistory>();
			try {
				subsidiaryHistories = oldSubsidiaryAddress.get().compareFields(subsidiaryAddress);
				if (CollectionUtils.isNotEmpty(subsidiaryHistories)) {
					this.subsidiaryHistoryRepository.saveAll(subsidiaryHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Supplied History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.subsidiaryHistoryRepository.save(this.prepareSubsidiaryHistory(subsidiaryAddress.getSubsidiaryId(),
					subsidiaryAddress.getId(), AppConstants.SUBSIDIARY_ADDRESS, Operation.CREATE.toString(),
					subsidiaryAddress.getLastModifiedBy(), null, String.valueOf(subsidiaryAddress.getId())));
		}

		log.info("Subsidiary Address saved successfully.");
		return subsidiaryAddress;
	}

	/**
	 * Prepares the history for the Subsidiary
	 * 
	 * @param taxRateId
	 * @param moduleName
	 * @param operation
	 * @param lastModifiedBy
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public SubsidiaryHistory prepareSubsidiaryHistory(Long subsidiaryId, Long childId, String moduleName,
			String operation, String lastModifiedBy, String oldValue, String newValue) {
		SubsidiaryHistory subsidiaryHistory = new SubsidiaryHistory();
		subsidiaryHistory.setSubsidiaryId(subsidiaryId);
		subsidiaryHistory.setChildId(childId);
		subsidiaryHistory.setModuleName(moduleName);
		subsidiaryHistory.setChangeType(AppConstants.UI);
		subsidiaryHistory.setOperation(operation);
		subsidiaryHistory.setOldValue(oldValue);
		subsidiaryHistory.setNewValue(newValue);
		subsidiaryHistory.setLastModifiedBy(lastModifiedBy);
		return subsidiaryHistory;
	}

	@Override
	public PaginationResponse getSubsidiaries(Date startDate, Date endDate, int pageNumber, int pageSize,
			String sortColumnName, String sortOrder) {
		List<Subsidiary> subsidiaries = new ArrayList<Subsidiary>();
		String whereClause = prepareWhereClause(startDate, endDate).toString();
		String orderByClause = " ORDER BY " + sortColumnName + " " + sortOrder;
		subsidiaries = this.subsidiaryDao.findAll(whereClause, pageNumber, pageSize, orderByClause);

		Long totalRecords = this.subsidiaryDao.getCount(whereClause);

		return CommonUtils.setPaginationResponse(pageNumber, pageSize, subsidiaries, totalRecords);
	}

	private StringBuilder prepareWhereClause(Date startDate, Date endDate) {
		StringBuilder whereClause = new StringBuilder(" AND s.isDeleted is false ");
		if (startDate != null) {
			whereClause.append(" AND to_char(s.createdDate, 'yyyy-MM-dd') >= '").append(startDate).append("'");
		}
		if (endDate != null) {
			whereClause.append(" AND to_char(s.createdDate, 'yyyy-MM-dd') <= '").append(endDate).append("'");
		}
		return whereClause;
	}

	@Override
	public List<SubsidiaryHistory> findHistoryBySubsidiaryId(Long subsidiaryId, Pageable pageable) {
		List<SubsidiaryHistory> histories = this.subsidiaryHistoryRepository.findBySubsidiaryIdOrderById(subsidiaryId, pageable);
		String createdBy = histories.get(0).getLastModifiedBy();
		histories.forEach(e->{
			e.setCreatedBy(createdBy);
		});
		return histories;
	}

	@Override
	public Subsidiary getSubsidiaryAndActiveAddressById(Long id) {
		Optional<Subsidiary> subsidiary = Optional.ofNullable(new Subsidiary());
		subsidiary = this.subsidiaryRepository.findByIdAndIsDeleted(id, false);
		if (!subsidiary.isPresent()) {
			log.info("Subsidiary is not found given id : " + id);
			throw new CustomMessageException("Subsidiary is not found given id : " + id);
		}
		subsidiary.get()
				.setSubsidiaryAddresses(this.subsidiaryAddressRepository.findAllBySubsidiaryIdAndIsActive(id, true));
		return subsidiary.get();
	}

	/**
	 * To get list(id, name) only to display in the Dropdown
	 */
	@Override
	public Map<Long, String> getSubsidiaries() {
		return this.subsidiaryRepository.findIdAndNameMap(false);
	}

	@Override
	public Boolean getValidateName(String name) {
		// if name is empty then name is not valid
		if (StringUtils.isEmpty(name))
			return false;

		Long countOfRecordsWithSameName = this.subsidiaryRepository.getCountByName(name.trim());
		// if we we found the count greater than 0 then it is not valid. If it is zero
		// then it is valid string
		if (countOfRecordsWithSameName > 0)
			return false;
		else
			return true;
	}

	private Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) {
		Credential credential = null;
		try {
			File file = ResourceUtils.getFile("classpath:credentials.json");
			GoogleClientSecrets clientSecrets =
			        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(file)));
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
			        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
			        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
			        .setAccessType("online")
			        .build();
			log.info("GoogleAuthorizationCodeFlow flow .. ");
			LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8081).build();
			log.info("LocalServerReceiver receiver .. ");
			credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
			log.info("credential created .. ");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return credential;
	}

	@Override
	public User createUser(String userName) {
		log.info("User Creation Started");
		NetHttpTransport HTTP_TRANSPORT = null;
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}
	    Directory service =
	        new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
	            .setApplicationName("My App Name")
	            .build();
	    User user = new User();
        // populate are the required fields only
        UserName name = new UserName();
        name.setFamilyName("Co.");
        name.setGivenName(userName);
        user.setName(name);
        user.setPassword("Welcome123");
        user.setPrimaryEmail(userName+"@monstarbill.com");
        // requires DirectoryScopes.ADMIN_DIRECTORY_USER scope  
        try {
			user = service.users().insert(user).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
        log.info("User Created : " + user);
		return user;
	}
	
	@Override
	public Long getSubsidiaryIdByName(String name) {
		return subsidiaryRepository.findIdByNameAndIsDeleted(name, false);
	}
	
	@Override
	public String findCurrencyBySubsidiaryName(String subsidiaryName) {
		return this.subsidiaryRepository.findCurrencyByNameAndIsDeleted(subsidiaryName, false);
	}
}
