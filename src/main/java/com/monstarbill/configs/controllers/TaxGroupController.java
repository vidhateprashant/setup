package com.monstarbill.configs.controllers;

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

import com.monstarbill.configs.models.TaxGroup;
import com.monstarbill.configs.models.TaxGroupHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.service.TaxGroupService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tax-group")
//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false")
public class TaxGroupController {

	@Autowired
	private TaxGroupService taxGroupService;

	/**
	 * Save/update the tax-group
	 * @param taxGroup
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<TaxGroup> saveTaxGroup(@Valid @RequestBody TaxGroup taxGroup) {
		log.info("Saving the Tax Group :: " + taxGroup.toString());
		taxGroup = taxGroupService.save(taxGroup);
		log.info("Tax Group saved successfully");
		return ResponseEntity.ok(taxGroup);
	}

	/**
	 * get Tax Group based on it's id
	 * 
	 * @param id
	 * @return tax Group
	 */
	@GetMapping("/get")
	public ResponseEntity<TaxGroup> findById(@RequestParam Long id) {
		log.info("Get tax group for ID :: " + id);
		TaxGroup taxGroup = taxGroupService.getTaxGroupById(id);
		if (taxGroup == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from find by id Tax Group");
		return new ResponseEntity<>(taxGroup, HttpStatus.OK);
	}

	/**
	 * get list of Tax Group with/without Filter
	 * 
	 * @return
	 */
	@PostMapping("/get/all")
	public ResponseEntity<PaginationResponse> findAll(@RequestBody PaginationRequest paginationRequest) {
		log.info("Get all Tax Group started.");
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse = taxGroupService.findAll(paginationRequest);
		log.info("Get all tax group completed.");
		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}

	/**
	 * soft delete the TaxGroup by it's id
	 * 
	 * @param id
	 * @return
	 */
	@Deprecated
	@GetMapping("/delete")
	public ResponseEntity<Boolean> deleteById(@RequestParam Long id) {
		log.info("Delete TaxGroup by ID :: " + id);
		boolean isDeleted = false;
		isDeleted = taxGroupService.deleteById(id);
		log.info("Delete TaxGroup by ID Completed.");
		return new ResponseEntity<>(isDeleted, HttpStatus.OK);
	}

	/**
	 * Find history by TaxGroup Id Supported for server side pagination
	 * 
	 * @param id
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	@GetMapping("/get/history")
	public ResponseEntity<List<TaxGroupHistory>> findHistoryById(@RequestParam Long id,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get TaxGroup Audit  :: " + id);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<TaxGroupHistory> taxGroupHistoris = this.taxGroupService.findHistoryById(id, pageable);
		log.info("Returning from TaxGroup Audit by id.");
		return new ResponseEntity<>(taxGroupHistoris, HttpStatus.OK);
	}

	@GetMapping("/get-by-subsidiary")
	public ResponseEntity<List<TaxGroup>> findBySubsidiaryId(@RequestParam Long subsidiaryId) {
		log.info("Get tax group for subsidiary ID :: " + subsidiaryId);
		List<TaxGroup> taxGroups = this.taxGroupService.getTaxGroupBySubsidiaryId(subsidiaryId);
		log.info("Returning from find by subsidiary id Tax Group");
		return new ResponseEntity<>(taxGroups, HttpStatus.OK);
	}
	
	@GetMapping("/find-by-name")
	public ResponseEntity<TaxGroup> findByName(@RequestParam String taxGroupName) {
		log.info("Get tax group for taxGroupName :: " + taxGroupName);
		TaxGroup taxGroup = this.taxGroupService.findByTaxGroupName(taxGroupName);
		log.info("Returning from find by taxGroupName Tax Group");
		return new ResponseEntity<>(taxGroup, HttpStatus.OK);
	}
}
