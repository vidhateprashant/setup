package com.monstarbill.configs.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monstarbill.configs.models.GeneralPreference;
import com.monstarbill.configs.models.GeneralPreferenceHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.service.PreferencesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/preference")
@CrossOrigin(origins= "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false" )
public class PreferenceController {
	
	@Autowired
	private PreferencesService preferencesService;
	
	/**
	 * Save/update General Preference
	 * @param 
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<GeneralPreference> savePreferences(@Valid @RequestBody GeneralPreference preferences) {
		log.info("Saving the Preferences :: " + preferences.toString());
		preferences = preferencesService.save(preferences);
		log.info("Preferences saved successfully");
		return ResponseEntity.ok(preferences);
	}
	
	/**
	 * get all data of a single preferences for view and update
	 * @param 
	 * @return
	 */
	@GetMapping("/get")
	public ResponseEntity<GeneralPreference> findById(@RequestParam Long id) {
		log.info("Get Preferences for ID :: " + id);
		GeneralPreference preferences = preferencesService.getPreferencesById(id);
		if (preferences == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from find by id preferences");
		return new ResponseEntity<>(preferences, HttpStatus.OK);
	}
	
	/**
	 * get all preference for the table with pagination
	 * @param 
	 * @return
	 */
	@PostMapping("/get/all")
	public ResponseEntity<PaginationResponse> findAll(@RequestBody PaginationRequest paginationRequest) {
		log.info("Get All preferencess started");
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse = preferencesService.findAll(paginationRequest);
		log.info("Get All preferencess Finished");
		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}
	
	@GetMapping("/get/history")
	public ResponseEntity<List<GeneralPreferenceHistory>> findHistoryById(@RequestParam Long preferenceId, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get Employee Audit for General Preference ID :: " + preferenceId);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<GeneralPreferenceHistory> generalPreferenceHistories = preferencesService.findAuditById(preferenceId, pageable);
		log.info("Returning from General Preference Audit by id.");
		return new ResponseEntity<>(generalPreferenceHistories, HttpStatus.OK);
	}
	
	@GetMapping("/get-preference-number")
	public ResponseEntity<String> findPreferenceNumberByMaster(@RequestParam Long subsidiaryId, @RequestParam String masterName) {
		log.info("Get Preference Number by master Name - STARTED :: " + masterName);
		String preferenceNumber = this.preferencesService.findPreferenceNumberByMaster(subsidiaryId, masterName);
		log.info("Get Preference Number by master Name - FINISHED :: " + masterName);
		return new ResponseEntity<>(preferenceNumber, HttpStatus.OK);
	}
	
	@GetMapping("/get-approval-routing-by-status")
	public ResponseEntity<List<String>> findRoutingByStatus(@RequestParam Long subsidiaryId, @RequestParam String formType, @RequestParam(defaultValue = "ACTIVE") String status) {
		log.info("Get approval-routing-by-status - STARTED :: " + status);
		List<String> activeFormNames = this.preferencesService.findRoutingByStatus(subsidiaryId, formType, status);
		log.info("Get approval-routing-by-status - FINISHED :: " + status);
		return new ResponseEntity<>(activeFormNames, HttpStatus.OK);
	}

}
