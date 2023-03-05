package com.monstarbill.configs.service;

import java.util.List;

import javax.validation.Valid;

import com.monstarbill.configs.models.CompanyData;
import com.monstarbill.configs.models.CompanyDataHistory;

public interface CompanyDataService {

	public CompanyData save(CompanyData companyData);

	public CompanyData getById(Long id);

	public List<CompanyData> getAll();

	public List<CompanyDataHistory> getHistoryById(Long id);

	public CompanyData createDatabase(@Valid CompanyData companyData);

}
