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

import com.monstarbill.configs.models.TaxRate;
import com.monstarbill.configs.models.TaxRateHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.service.TaxRateService;

import lombok.extern.slf4j.Slf4j;

/**
 * All WS's of the Tax-rate and it's child components if any
 * @author Prashant
 */
@Slf4j
@RestController
@RequestMapping("/tax-rate")
//@CrossOrigin(origins= "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false" )
public class TaxRateController {

	@Autowired
	private TaxRateService taxRateService;
	
	/**
	 * Save/update the tax-rate	
	 * @param taxRate
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<TaxRate> saveSupplier(@Valid @RequestBody TaxRate taxRate) {
		log.info("Saving the Tax Rate Rule :: " + taxRate.toString());
		taxRate = taxRateService.save(taxRate);
		log.info("Tax Rate saved successfully");
		return ResponseEntity.ok(taxRate);
	}
	
	/**
	 * get tax-rate based on it's id
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public ResponseEntity<TaxRate> findById(@RequestParam Long id) {
		log.info("Get Supplier for ID :: " + id);
		TaxRate taxRate = taxRateService.getTaxRateById(id);
		if (taxRate == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from find by id supplier");
		return new ResponseEntity<>(taxRate, HttpStatus.OK);
	}
	
	/**
	 * get list of tax-rates
	 * @return
	 */
	@PostMapping("/get/all")
	public ResponseEntity<PaginationResponse> findAllTaxRateRules(@RequestBody PaginationRequest paginationRequest) {
		log.info("Get all tax rate rules started.");
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse = taxRateService.findAllTaxRateRules(paginationRequest);
		log.info("Get all tax rate rules completed.");
		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}
	
	/**
	 * get list of tax-rates based on applied filter
	 * @param taxType
	 * @param taxName
	 * @return
	 */
	@GetMapping("/get/all/filters")
	public ResponseEntity<List<TaxRate>> findAllTaxRateRulesWithFilters(@RequestParam String taxType, @RequestParam String taxName) {
		log.info("Get all tax rate rules with filters started.");
		List<TaxRate> taxRateRules = new ArrayList<TaxRate>();
		taxRateRules = taxRateService.findAllTaxRateRulesWithFilters(taxType, taxName);
		log.info("Get all tax rate rules with filters completed.");
		return new ResponseEntity<>(taxRateRules, HttpStatus.OK);
	}

	/**
	 * soft delete the taxrate by it's id
	 * @param id
	 * @return
	 */
	@GetMapping("/delete")
	public ResponseEntity<Boolean> deleteById(@RequestParam Long id) {
		log.info("Delete Taxrate by ID :: " + id);
		boolean isDeleted = false;
		isDeleted = taxRateService.deleteById(id);
		log.info("Delete Taxrate by ID Completed.");
		return new ResponseEntity<>(isDeleted, HttpStatus.OK);
	}
	
	/**
	 * Find history by supplier Id
	 * Supported for server side pagination
	 * @param id
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	@GetMapping("/get/history")
	public ResponseEntity<List<TaxRateHistory>> findHistoryById(@RequestParam Long id, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get TaxRate Audit for Supplier ID :: " + id);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<TaxRateHistory> taxRateHistoris = this.taxRateService.findHistoryById(id, pageable);
		log.info("Returning from TaxRate Audit by id.");
		return new ResponseEntity<>(taxRateHistoris, HttpStatus.OK);
	}
	
	/**
	 * get tax-rate based on subsidiaryId
	 * @param subsidiaryId
	 * @return
	 */
	@GetMapping("/get-tax-rate-by-subsidiary")
	public ResponseEntity<List<TaxRate>> findBySubsidiaryId(@RequestParam Long subsidiaryId) {
		log.info("Get subsidiary id :: " + subsidiaryId);
		List<TaxRate> taxRate = taxRateService.getTaxRateBySubsidiaryId(subsidiaryId);
		if (taxRate == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning tax rate by subsidiary id");
		return new ResponseEntity<>(taxRate, HttpStatus.OK);
	}
}
