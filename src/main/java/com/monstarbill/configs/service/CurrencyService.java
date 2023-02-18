package com.monstarbill.configs.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.monstarbill.configs.models.Currency;
import com.monstarbill.configs.models.CurrencyHistory;

public interface CurrencyService {
	
	public Currency getCurrencyById(Long id);
	
	public List<CurrencyHistory> findHistoryById(Long id, Pageable pageable);
	
	public List<Currency> save(List<Currency> currencies);

	public Boolean getValidateName(String name);

	public List<Currency> getAllCurrencies();

}
