package com.monstarbill.configs.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monstarbill.configs.models.Currency;
import com.monstarbill.configs.models.CurrencyHistory;
import com.monstarbill.configs.service.CurrencyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/currency")
//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false")
public class CurrencyController {

	@Autowired
	private CurrencyService currencyService;

	/**
	 * This saves the currency
	 * 
	 * @param currency
	 * @return
	 * 
	 */
	@PostMapping("/save")
	public ResponseEntity<List<Currency>> save(@Valid @RequestBody List<Currency> currencies) {
		log.info("Saving the Currency :: " + currencies.toString());
		currencies = currencyService.save(currencies);
		log.info("Currency saved successfully");
		return ResponseEntity.ok(currencies);
	}

	/**
	 * get Currency based on it's id
	 * 
	 * @param id
	 * @return Currency
	 */
	@GetMapping("/get")
	public ResponseEntity<Currency> getCurrency(@RequestParam Long id) {
		return new ResponseEntity<>(currencyService.getCurrencyById(id), HttpStatus.OK);
	}

	/**
	 * get list of all Currency
	 * 
	 * @return
	 */
	@GetMapping("/get/all")
	public ResponseEntity<List<Currency>> getAllCurrency() {
		List<Currency> currencies = new ArrayList<Currency>();
		currencies = currencyService.getAllCurrencies();
		return ResponseEntity.ok(currencies);
	}

	/**
	 * Find history by Currency Id Supported for server side pagination
	 * 
	 * @param id
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	@GetMapping("/get/history")
	public ResponseEntity<List<CurrencyHistory>> findHistoryById(@RequestParam Long id,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get Currency Audit  :: " + id);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<CurrencyHistory> currencyHistories = this.currencyService.findHistoryById(id, pageable);
		log.info("Returning from Currency Audit by id.");
		return new ResponseEntity<>(currencyHistories, HttpStatus.OK);
	}
	
	@GetMapping("/is-valid-name")
	public ResponseEntity<Boolean> validateName(@RequestParam String name) {
		return new ResponseEntity<>(this.currencyService.getValidateName(name), HttpStatus.OK);
	}
}
