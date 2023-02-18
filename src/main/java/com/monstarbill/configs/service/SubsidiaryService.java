package com.monstarbill.configs.service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import com.google.api.services.directory.model.User;

import com.monstarbill.configs.models.Subsidiary;
import com.monstarbill.configs.models.SubsidiaryAddress;
import com.monstarbill.configs.models.SubsidiaryHistory;
import com.monstarbill.configs.payload.response.PaginationResponse;

public interface SubsidiaryService {

	public Subsidiary save(Subsidiary subsidiary);

	public List<String> getParentCompanyNames();

	public Subsidiary getSubsidiaryById(Long id);

	public PaginationResponse getSubsidiaries(Date startDate, Date endDate, int pageNumber, int pageSize,
			String sortColumnName, String sortOrder);

	public List<Subsidiary> getSubsidiariesByFilter(String startDate, String endDate);

	public SubsidiaryAddress saveAddress(SubsidiaryAddress subsidiaryAddress);

	public SubsidiaryAddress getAddressById(Long id);

	public List<SubsidiaryAddress> getAddressBySubsidiaryId(Long subsidiaryId);

	public List<SubsidiaryHistory> findHistoryBySubsidiaryId(Long subsidiaryId, Pageable pageable);

	public Subsidiary getSubsidiaryAndActiveAddressById(Long id);

	public Map<Long, String> getSubsidiaries();

	public Boolean getValidateName(String name);

	public User createUser(String userName);

	public Long getSubsidiaryIdByName(String name);

	public String findCurrencyBySubsidiaryName(String subsidiaryName);

}
