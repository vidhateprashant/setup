package com.monstarbill.configs.service;

import java.util.List;

import com.monstarbill.configs.models.CompanyData;
import com.monstarbill.configs.models.CompanyDataHistory;

public interface CompanyDataService {

	public CompanyData save(CompanyData companyData);

	public CompanyData getById(Long id);

	public List<CompanyData> getAll();

	public List<CompanyDataHistory> getHistoryById(Long id);

}
