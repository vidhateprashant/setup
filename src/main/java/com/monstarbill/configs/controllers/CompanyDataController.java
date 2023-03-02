package com.monstarbill.configs.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monstarbill.configs.models.CompanyData;
import com.monstarbill.configs.models.CompanyDataHistory;
import com.monstarbill.configs.service.CompanyDataService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/company-data")
@Slf4j
public class CompanyDataController {

	@Autowired
	private CompanyDataService companyDataService;

	/**
	 * This saves the companyData
	 * 
	 * @param companyData
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<CompanyData> savecompanyData(@Valid @RequestBody CompanyData companyData) {
		log.info("Saving the companyData: " + companyData.toString());
		companyData = companyDataService.save(companyData);
		log.info("CompanyData saved successfully");
		return ResponseEntity.ok(companyData);
	}

	/**
	 * get companyData based on it's id
	 * 
	 * @param id
	 * @return companyData
	 */
	@GetMapping("/get")
	public ResponseEntity<CompanyData> getById(@RequestParam Long id) {
		CompanyData companyData = companyDataService.getById(id);
		log.info("Returning the value agains given id " + id);
		return ResponseEntity.ok(companyData);
	}

	/**
	 * get list of companyData without Filter
	 * 
	 * @return list of company data
	 */
	@GetMapping("/get/all")
	public ResponseEntity<List<CompanyData>> getAll() {
		List<CompanyData> companyData = new ArrayList<>();
		companyData = companyDataService.getAll();
		return ResponseEntity.ok(companyData);
	}

	/**
	 * Find history by companyData Id
	 * 
	 * @param id
	 * @return list of company data history
	 */
	@GetMapping("/get/history")
	public ResponseEntity<List<CompanyDataHistory>> getHistoryById(@RequestParam Long id) {
		List<CompanyDataHistory> companyDataHistories = companyDataService.getHistoryById(id);
		log.info("History found against " + id);
		return ResponseEntity.ok(companyDataHistories);
	}

}
