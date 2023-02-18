package com.monstarbill.configs.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.configs.models.TaxRate;
import com.monstarbill.configs.payload.request.PaginationRequest;

@Component("taxRateDao")
public interface TaxRateDao {
	public List<TaxRate> findAll(String whereClause, PaginationRequest paginationRequest);

	public Long getCount(String whereClause);
}
