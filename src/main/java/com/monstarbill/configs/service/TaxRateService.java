package com.monstarbill.configs.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.monstarbill.configs.models.TaxRate;
import com.monstarbill.configs.models.TaxRateHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;

public interface TaxRateService {

	public TaxRate save(TaxRate taxRate);

	public TaxRate getTaxRateById(Long id);

	public PaginationResponse findAllTaxRateRules(PaginationRequest paginationRequest);

	public List<TaxRate> findAllTaxRateRulesWithFilters(String taxType, String taxName);

	public boolean deleteById(Long id);

	public List<TaxRateHistory> findHistoryById(Long id, Pageable pageable);

	public List<TaxRate> getTaxRateBySubsidiaryId(Long subsidiaryId);

}
