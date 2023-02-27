package com.monstarbill.configs.controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.google.api.services.directory.model.User;
import com.monstarbill.configs.models.Subsidiary;
import com.monstarbill.configs.models.SubsidiaryAddress;
import com.monstarbill.configs.models.SubsidiaryHistory;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.service.SubsidiaryService;

import lombok.extern.slf4j.Slf4j;

/**
 * All WS's of the Subsidiary and it's child components
 * 
 * @author prashant
 *
 */
@Slf4j
@RestController
@RequestMapping("/subsidiary")
//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false")
public class SubsidiaryController {

	@Autowired
	private SubsidiaryService subsidiaryService;

	/**
	 * This saves the subsidiary & it's address
	 * 
	 * @param subsidiary
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<Subsidiary> saveSubsidiary(@Valid @RequestBody Subsidiary subsidiary) {
		log.info("Saving the Subsidiary :: " + subsidiary.toString());
		subsidiary = subsidiaryService.save(subsidiary);
		log.info("Subsidiary saved successfully");
		return ResponseEntity.ok(subsidiary);
	}

	/**
	 * Returns the subsidiary by it's id
	 * 
	 * @return
	 */
	@GetMapping("/get")
	public ResponseEntity<Subsidiary> getSubsidiary(@RequestParam Long id) {
		return new ResponseEntity<>(subsidiaryService.getSubsidiaryById(id), HttpStatus.OK);
	}
	
	/**
	 * same as above but it returns only the active address
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/get-with-active-address")
	public ResponseEntity<Subsidiary> getSubsidiaryActive(@RequestParam Long id) {
		return new ResponseEntity<>(subsidiaryService.getSubsidiaryAndActiveAddressById(id), HttpStatus.OK);
	}

	/**
	 * Returns the all subsidiaries
	 * 
	 * @return
	 */
	@GetMapping("/get/all")
	public ResponseEntity<PaginationResponse> getAllSubsidiaries(@RequestParam(required = false) Date startDate,
			@RequestParam(required = false) Date endDate, @RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "300") int pageSize, @RequestParam(defaultValue = "id") String sortColumnName,
			@RequestParam(defaultValue = "desc") String sortOrder) {
		return new ResponseEntity<>(
				subsidiaryService.getSubsidiaries(startDate, endDate, pageNumber, pageSize, sortColumnName, sortOrder),
				HttpStatus.OK);
	}

	/**
	 * This returns the distinct parent subsidiary names
	 * 
	 * @return
	 */
	@GetMapping("/get-parent-company-names")
	public ResponseEntity<List<String>> getParentCompanyNames() {
		return new ResponseEntity<>(subsidiaryService.getParentCompanyNames(), HttpStatus.OK);
	}

	@GetMapping("/get/all-filter")
	public ResponseEntity<List<Subsidiary>> getAllSubsidiariesFilter(@RequestParam String startDate,
			@RequestParam String endDate) {
		return new ResponseEntity<>(subsidiaryService.getSubsidiariesByFilter(startDate, endDate), HttpStatus.OK);
	}

	/**
	 * This saves the subsidiary address
	 * 
	 * @param subsidiary address
	 * @return
	 */
	@PostMapping("/address/save")
	public ResponseEntity<SubsidiaryAddress> saveSubsidiaryAddress(@RequestBody SubsidiaryAddress subsidiaryAddress) {
		log.info("Saving the Subsidiary Address :: " + subsidiaryAddress.toString());
		subsidiaryAddress = subsidiaryService.saveAddress(subsidiaryAddress);
		log.info("Subsidiary saved successfully");
		return ResponseEntity.ok(subsidiaryAddress);
	}

	/**
	 * Returns the subsidiary Address by it's id
	 * 
	 * @return
	 */
	@GetMapping("/address/get")
	public ResponseEntity<SubsidiaryAddress> getSubsidiaryAddress(@RequestParam Long id) {
		return new ResponseEntity<>(subsidiaryService.getAddressById(id), HttpStatus.OK);
	}

	/**
	 * Returns the subsidiary Address by it's id
	 * 
	 * @return
	 */
	@GetMapping("/address/get-by-subsidiary")
	public ResponseEntity<List<SubsidiaryAddress>> getSubsidiaryAddressBySubsidiary(@RequestParam Long subsidiaryId) {
		List<SubsidiaryAddress> subsidiaryAddress = new ArrayList<SubsidiaryAddress>();
		subsidiaryAddress = subsidiaryService.getAddressBySubsidiaryId(subsidiaryId);
		return ResponseEntity.ok(subsidiaryAddress);
	}

	/**
	 * Find history by Subsidiary Id Supported for server side pagination
	 * 
	 * @param subsidiaryId
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	@GetMapping("/get/history")
	public ResponseEntity<List<SubsidiaryHistory>> findHistoryById(@RequestParam Long subsidiaryId,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get Subsidiary Audit for Subsidiary ID :: " + subsidiaryId);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<SubsidiaryHistory> subsidiaryHistoris = this.subsidiaryService.findHistoryBySubsidiaryId(subsidiaryId,
				pageable);
		log.info("Returning from Subsidiary Audit by Subsidiary-id.");
		return new ResponseEntity<>(subsidiaryHistoris, HttpStatus.OK);
	}

	/**
	 * To get list(id, name only) to display in the Dropdown
	 * 
	 * @return
	 */
	@GetMapping("/get/all/lov")
	public ResponseEntity<Map<Long, String>> getAllSubsidiariesForLov() {
		return new ResponseEntity<>(subsidiaryService.getSubsidiaries(), HttpStatus.OK);
	}

	@GetMapping("/is-valid-name")
	public ResponseEntity<Boolean> validateName(@RequestParam String name) {
		return new ResponseEntity<>(this.subsidiaryService.getValidateName(name), HttpStatus.OK);
	}

	/**
	 * Create Google User
	 * 
	 * @return User Details
	 */
	@PostMapping("/createUser")
	public User createUser(@RequestParam String userName) {
		return subsidiaryService.createUser(userName);
	}
	
	/**
	 * get subsidiary id by subsidiary Name
	 * @param name
	 * @return
	 */
	@GetMapping("/get-subsidiary-id-by-name")
	public ResponseEntity<Long> getSubsidiaryIdByName(@RequestParam String name) {
		return new ResponseEntity<>(this.subsidiaryService.getSubsidiaryIdByName(name), HttpStatus.OK);
	}
	
	@GetMapping("/get-currency-by-subsidiary-name")
	public ResponseEntity<String> findCurrencyBySubsidiaryName(@RequestParam String name) {
		return new ResponseEntity<>(this.subsidiaryService.findCurrencyBySubsidiaryName(name), HttpStatus.OK);
	}
	
	@GetMapping("/get-logged-username")
	public ResponseEntity<String> getLoggedInUsername() {
		return new ResponseEntity<>(this.subsidiaryService.getLoggedInUsername(), HttpStatus.OK);
	}
}
