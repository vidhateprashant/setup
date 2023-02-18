package com.monstarbill.configs.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.monstarbill.configs.common.AppConstants;
import com.monstarbill.configs.common.CommonUtils;
import com.monstarbill.configs.common.CustomException;
import com.monstarbill.configs.common.CustomMessageException;
import com.monstarbill.configs.enums.Operation;
import com.monstarbill.configs.models.Currency;
import com.monstarbill.configs.models.CurrencyHistory;
import com.monstarbill.configs.repository.CurrencyHistoryRepository;
import com.monstarbill.configs.repository.CurrencyRepository;
import com.monstarbill.configs.service.CurrencyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private CurrencyHistoryRepository currencyHistoryRepository;

	@Override
	public List<Currency> save(List<Currency> currencies) {
		Optional<Currency> oldCurrency = Optional.empty();

		for (Currency currency : currencies) {
			oldCurrency = Optional.empty();

			if (currency.getId() == null) {
				currency.setCreatedBy(CommonUtils.getLoggedInUsername());
			} else {
				// Get the existing object using the deep copy
				oldCurrency = this.currencyRepository.findByIdAndIsDeleted(currency.getId(), false);
				if (oldCurrency.isPresent()) {
					try {
						oldCurrency = Optional.ofNullable((Currency) oldCurrency.get().clone());
					} catch (CloneNotSupportedException e) {
						log.error("Error while Cloning the object. Please contact administrator.");
						throw new CustomException("Error while Cloning the object. Please contact administrator.");
					}

				}
			}

			currency.setLastModifiedBy(CommonUtils.getLoggedInUsername());
			if (currency.isInactive() == false) {
				currency.setActiveDate(null);
			}
			try {
				currency = this.currencyRepository.save(currency);
			} catch (DataIntegrityViolationException e) {
				log.error("Currency unique constrain violetd." + e.getMostSpecificCause());
				throw new CustomException("Currency unique constrain violetd :" + e.getMostSpecificCause());
			}

			if (currency == null) {
				log.info("Error while saving the Currency.");
				throw new CustomMessageException("Error while saving the Currency.");
			}

			// update the data in currency history table
			this.updateCurrencyHistory(currency, oldCurrency);
		}
		return currencies;
	}

	/**
	 * This method save the data in history table Add entry as a Insert if Currency
	 * is new Add entry as a Update if Currency is exists
	 * 
	 * @param Currency
	 * @param oldCurrency
	 */
	private void updateCurrencyHistory(Currency currency, Optional<Currency> oldCurrency) {
		if (oldCurrency.isPresent()) {
			// insert the updated fields in history table
			List<CurrencyHistory> currencyHistories = new ArrayList<CurrencyHistory>();
			try {
				currencyHistories = oldCurrency.get().compareFields(currency);
				if (CollectionUtils.isNotEmpty(currencyHistories)) {
					this.currencyHistoryRepository.saveAll(currencyHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Currency History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.currencyHistoryRepository.save(this.prepareCurrencyHistory(currency.getId(),
					AppConstants.CURRENCY, Operation.CREATE.toString(), currency.getLastModifiedBy(), null,
					String.valueOf(currency.getId())));
		}
	}

	/**
	 * Prepares the history for the Currency
	 * 
	 * @param currencyId
	 * @param moduleName
	 * @param operation
	 * @param lastModifiedBy
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public CurrencyHistory prepareCurrencyHistory(Long currencyId, String moduleName, String operation,
			String lastModifiedBy, String oldValue, String newValue) {
		CurrencyHistory currencyHistory = new CurrencyHistory();
		currencyHistory.setCurrencyId(currencyId);
		currencyHistory.setModuleName(moduleName);
		currencyHistory.setChangeType(AppConstants.UI);
		currencyHistory.setOperation(operation);
		currencyHistory.setOldValue(oldValue);
		currencyHistory.setNewValue(newValue);
		currencyHistory.setLastModifiedBy(lastModifiedBy);
		return currencyHistory;
	}

	@Override
	public Currency getCurrencyById(Long id) {
		Optional<Currency> currency = Optional.empty();
		currency = this.currencyRepository.findByIdAndIsDeleted(id, false);
		if (!currency.isPresent()) {
			log.info("Currency is not found given id : " + id);
			throw new CustomMessageException("Currency is not found given id : " + id);
		}
		return currency.get();
	}


	@Override
	public List<CurrencyHistory> findHistoryById(Long id, Pageable pageable) {
		return this.currencyHistoryRepository.findByCurrencyId(id, pageable);
	}
	@Override
	public Boolean getValidateName(String name) {
		// if name is empty then name is not valid
		if (StringUtils.isEmpty(name)) return false;
		
		Long countOfRecordsWithSameName = this.currencyRepository.getCountByName(name.trim());
		// if we we found the count greater than 0 then it is not valid. If it is zero then it is valid string
		if (countOfRecordsWithSameName > 0) return false; else return true;
	}

	@Override
	public List<Currency> getAllCurrencies() {
		return this.currencyRepository.getActiveCurrencies();
	}

}
