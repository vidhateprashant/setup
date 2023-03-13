package com.monstarbill.configs.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.monstarbill.configs.common.AppConstants;
import com.monstarbill.configs.common.CommonUtils;
import com.monstarbill.configs.common.CustomException;
import com.monstarbill.configs.enums.Operation;
import com.monstarbill.configs.feingclients.MasterServiceClient;
import com.monstarbill.configs.models.CompanyData;
import com.monstarbill.configs.models.CompanyDataHistory;
import com.monstarbill.configs.models.CustomRoles;
import com.monstarbill.configs.models.DefaultRolePermissions;
import com.monstarbill.configs.models.Employee;
import com.monstarbill.configs.models.EmployeeAccess;
import com.monstarbill.configs.models.EmployeeRole;
import com.monstarbill.configs.models.RolePermissions;
import com.monstarbill.configs.repository.CompanyDataHistoryRepository;
import com.monstarbill.configs.repository.CompanyDataRepository;
import com.monstarbill.configs.service.CompanyDataService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CompanyDataServiceImpl implements CompanyDataService {

	@Autowired
	private CompanyDataRepository companyDataRepository;

	@Autowired
	private CompanyDataHistoryRepository companyDataHistoryRepository;

	@Autowired
	private MasterServiceClient masterServiceClient;

	@Override
	public CompanyData save(CompanyData companyData) {
		Optional<CompanyData> oldCompanyData = Optional.empty();
		String username = CommonUtils.getLoggedInUsername();

		if (companyData.getId() == null) {
			companyData.setCreatedBy(username);
			companyData.setAccountId(UUID.randomUUID().toString().substring(0, 16).replace("-", ""));
		} else {
			oldCompanyData = this.companyDataRepository.findByIdAndIsDeleted(companyData.getId(), false);
			if (oldCompanyData.isPresent()) {
				companyData.setLastModifiedBy(username);
				try {
					oldCompanyData = Optional.ofNullable((CompanyData) oldCompanyData.get().clone());
				} catch (CloneNotSupportedException e) {
					log.info("Error while cloning the object. Please conatct administration");
					throw (new CustomException("Error while cloning the object. Please conatct administration"));
				}
			}
		}
		CompanyData companyDataSaved;
		companyDataSaved = this.companyDataRepository.save(companyData);
		if (companyDataSaved == null) {
			log.info("Error while saving the CompanyData");
			throw new CustomException("Error while saving the CompanyData");
		}

		// ===== Company Data History Save ======
		this.updateCompanyDataHistory(companyDataSaved, oldCompanyData);
		log.info("Company Data History is saved successfully.");
		return companyDataSaved;
	}

	@Override
	public CompanyData getById(Long id) {
		Optional<CompanyData> companyData = Optional.empty();
		companyData = companyDataRepository.findByIdAndIsDeleted(id, false);
		if (companyData.isPresent()) {
			log.info("CompanyData Found");
		}

		return companyData.get();
	}

	@Override
	public List<CompanyData> getAll() {
		return companyDataRepository.findAll();
	}

	@Override
	public List<CompanyDataHistory> getHistoryById(Long id) {
		return companyDataHistoryRepository.findByCompanyDataId(id);
	}

	private void updateCompanyDataHistory(CompanyData companyData, Optional<CompanyData> oldCompanyData) {
		if (oldCompanyData.isPresent()) {
			// insert the updated fields in history table
			List<CompanyDataHistory> companyDataHistories = new ArrayList<CompanyDataHistory>();
			try {
				companyDataHistories = oldCompanyData.get().compareFields(companyData);
				if (CollectionUtils.isNotEmpty(companyDataHistories)) {
					this.companyDataHistoryRepository.saveAll(companyDataHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
		} else {
			// Insert in history table as Operation - INSERT
			this.companyDataHistoryRepository.save(this.prepareCompanyDataHistory(companyData.getId(),
					AppConstants.COMPANY_DATA, Operation.CREATE.toString(), companyData.getLastModifiedBy(), null,
					String.valueOf(companyData.getId())));
		}
		log.info("AppData History is updated successfully");
	}

	public CompanyDataHistory prepareCompanyDataHistory(Long appDataId, String moduleName, String operation,
			String lastModifiedBy, String oldValue, String newValue) {
		CompanyDataHistory companyDataHistory = new CompanyDataHistory();
		companyDataHistory.setCompanyDataId(appDataId);
		companyDataHistory.setModuleName(moduleName);
		companyDataHistory.setChangeType(AppConstants.UI);
		companyDataHistory.setOperation(operation);
		companyDataHistory.setOldValue(oldValue);
		companyDataHistory.setNewValue(newValue);
		companyDataHistory.setLastModifiedBy(lastModifiedBy);
		return companyDataHistory;
	}

	@Override
	public CompanyData createDatabase(CompanyData companyData) {
		String accountId = companyData.getAccountId();
		if (StringUtils.isEmpty(accountId)) {
			throw new CustomException("Account ID Should not be empty. Please generate Account ID.");
		}
		
		CustomRoles role = this.generateDefaultRoleWithAccount(accountId, companyData.getCompanyName());
		Employee employee = this.generateDefaultEmployeeWithAccount(companyData, role);
		companyData.setEmployee(employee);
		
		return companyData;
	}

	private Employee generateDefaultEmployeeWithAccount(CompanyData companyData, CustomRoles role) {
		List<EmployeeRole> employeeRoles = new ArrayList<EmployeeRole>();
		EmployeeRole employeeRole = new EmployeeRole(role.getId(), role.getName());
		employeeRoles.add(employeeRole);
		
		EmployeeAccess employeeAccess = new EmployeeAccess(true, companyData.getEmail(), "123", employeeRoles);
		Employee employee = new Employee("Manager", companyData.getCompanyName(), employeeAccess, companyData.getAccountId());
		
		employee = this.masterServiceClient.saveEmployee(employee);
		
		return employee;
	}

	private CustomRoles generateDefaultRoleWithAccount(String accountId, String companyName) {
		CustomRoles customRole = new CustomRoles(accountId, companyName + " " + accountId.substring(accountId.length() - 4) + " Admin", true, "ADMIN_APPROVER");
		
		// find the all access for admin-approver
		List<DefaultRolePermissions> defaultRolePermissions = new ArrayList<DefaultRolePermissions>();
		List<RolePermissions> rolePermissions = new ArrayList<RolePermissions>();
		defaultRolePermissions = masterServiceClient.findAccessPointBySelectedAccess("ADMIN");
		for (DefaultRolePermissions defaultRolePermission : defaultRolePermissions) {
			RolePermissions rolePermission = new RolePermissions(defaultRolePermission.getModuleName(), defaultRolePermission.getAccessPoint(), defaultRolePermission.isCreate(), defaultRolePermission.isEdit(), defaultRolePermission.isView());
			rolePermissions.add(rolePermission);
		}
		
		customRole.setRolePermissions(rolePermissions);
		customRole = this.masterServiceClient.saveCustomRole(customRole);
		return customRole;
	}

}
