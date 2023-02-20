package com.monstarbill.configs.controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
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

import com.monstarbill.configs.models.FiscalCalanderAccounting;
import com.monstarbill.configs.models.FiscalCalender;
import com.monstarbill.configs.models.FiscalCalenderHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.service.FiscalCalanderService;

import lombok.extern.slf4j.Slf4j;

//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false")
@RestController
@RequestMapping("/fiscal-calender")
@Slf4j
public class FiscalCalenderController {

	@Autowired
	private FiscalCalanderService fiscalCalanderService;

	/**
	 * This saves the fiscal calendar
	 * 
	 * @param fiscal
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<FiscalCalender> save(@Valid @RequestBody FiscalCalender fiscalCalender)
			throws ParseException {
		log.info("Saving the Fiscal Calander :: " + fiscalCalender.toString());

		fiscalCalender = fiscalCalanderService.save(fiscalCalender);

		log.info("Fiscal Calander saved successfully");
		return ResponseEntity.ok(fiscalCalender);
	}

	/**
	 * get the fiscal data by id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public ResponseEntity<FiscalCalender> findById(@RequestParam Long id) {
		log.info("Get Fiscal Calender for ID :: " + id);
		FiscalCalender fiscalCalander = fiscalCalanderService.getFiscalCalanderById(id);
		if (fiscalCalander == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from find by id Fiscal Calender");
		return new ResponseEntity<>(fiscalCalander, HttpStatus.OK);
	}

	/**
	 * Get all the accounting against the fiscal calendar
	 * 
	 * @param fiscalId
	 * @return
	 */
	@Deprecated
	@GetMapping("/fiscal-accounting/get")
	public ResponseEntity<List<FiscalCalanderAccounting>> findAddressByFiscalId(@RequestParam Long fiscalId) {
		log.info("Get Address against the fiscal calender ID :: " + fiscalId);
		List<FiscalCalanderAccounting> fiscalCalanderAccounting = fiscalCalanderService
				.findAccountingByFiscalId(fiscalId);
		if (CollectionUtils.isEmpty(fiscalCalanderAccounting)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(fiscalCalanderAccounting, HttpStatus.OK);
	}
	
	/**
	 * get list of fiscal calendar with/without Filter 
	 * @return
	 */
	@PostMapping("/get/all")
	public ResponseEntity<PaginationResponse> findAll(@RequestBody PaginationRequest paginationRequest) {
		log.info("Get all fiscal calendar started.");
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse = fiscalCalanderService.findAll(paginationRequest);
		log.info("Get all fiscal calendar completed.");
		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}

	/**
	 * get list of fiscal calendar accounting with/without Filter
	 * 
	 * @return
	 */
	@Deprecated
	@GetMapping("/get-accounting")
	public ResponseEntity<List<FiscalCalanderAccounting>> findAllAccounting() {
		log.info("Get FIscal calender accounting List");
		List<FiscalCalanderAccounting> fiscalCalanderAccounting = new ArrayList<FiscalCalanderAccounting>();

		fiscalCalanderAccounting = fiscalCalanderService.findAllAccounting();
		if (fiscalCalanderAccounting == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from fiscal calender accountings list");
		return new ResponseEntity<>(fiscalCalanderAccounting, HttpStatus.OK);
	}

	/**
	 * get list of fiscal calendar list
	 * 
	 * @return
	 * @param startYear, endYear, startMonth, endMonth
	 */
	@GetMapping("/get-accounting-period")
	public ResponseEntity<List<FiscalCalanderAccounting>> getFiscalCalanderAccounting(@RequestParam int startYear,
			@RequestParam int endYear, @RequestParam String startMonth, @RequestParam String endMonth)
			throws ParseException {
		log.info("Fiscal Calander filterd .");
		List<FiscalCalanderAccounting> fiscalCalander = fiscalCalanderService.getFiscalCalanderAccounting(startYear,
				endYear, startMonth, endMonth);
		return new ResponseEntity<>(fiscalCalander, HttpStatus.OK);
	}

	/**
	 * delete the id by fiscal calendar
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/delete")
	public ResponseEntity<Boolean> deleteById(@RequestParam Long id) {
		log.info("Delete fiscal calender by ID :: " + id);
		boolean isDeleted = false;
		isDeleted = fiscalCalanderService.deleteById(id);
		log.info("Delete fiscal calender by ID Completed.");
		return new ResponseEntity<>(isDeleted, HttpStatus.OK);
	}


	/**
	 * Find history by fiscal Id Supported for server side pagination
	 * 
	 * @param id
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	@GetMapping("/get/history")
	public ResponseEntity<List<FiscalCalenderHistory>> findHistoryById(@RequestParam Long id,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get fiscal calender Audit  :: " + id);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<FiscalCalenderHistory> fiscalCalenderHistoris = this.fiscalCalanderService.findHistoryById(id, pageable);
		log.info("Returning from fiscal calender Audit by id.");
		return new ResponseEntity<>(fiscalCalenderHistoris, HttpStatus.OK);
	}
}
