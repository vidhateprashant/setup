package com.monstarbill.configs.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.configs.models.Currency;
import com.monstarbill.configs.payload.request.PaginationRequest;

@Component("currencyDao")
public interface CurrencyDao {
	
	public List<Currency> findAll(String whereClause, PaginationRequest paginationRequest);
	public Long getCount(String whereClause);

}




