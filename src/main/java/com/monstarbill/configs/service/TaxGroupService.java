package com.monstarbill.configs.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.monstarbill.configs.models.TaxGroup;
import com.monstarbill.configs.models.TaxGroupHistory;
import com.monstarbill.configs.models.TaxRateRule;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;

public interface TaxGroupService {
	
	public TaxGroup save(TaxGroup taxGroup);
	
	public TaxGroup getTaxGroupById(Long id);
	
	public PaginationResponse findAll(PaginationRequest paginationRequest);
	
	public boolean deleteById(Long id);
	
	public List<TaxGroupHistory> findHistoryById(Long id, Pageable pageable);
	
	public TaxRateRule save(TaxRateRule taxRateRule);
	
	public boolean deleteTaxRateRuleById(Long id);

	public List<TaxGroup> getTaxGroupBySubsidiaryId(Long subsidiaryId);

	public TaxGroup findByTaxGroupName(String name);

}
