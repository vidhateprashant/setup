package com.monstarbill.configs.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monstarbill.configs.models.DocumentSequence;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.service.DocumentSequenceService;

import lombok.extern.slf4j.Slf4j;

/**
 * All WS's of the document and it's child components
 * @author Prithwish
 */
@Slf4j
@RestController
@RequestMapping("/document-sequence")
//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false")
public class DocumentSequenceController {

	@Autowired
	private DocumentSequenceService documentSequenceService;

	/**
	 * This saves the document
	 * @param document
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<List<DocumentSequence>> save(@Valid @RequestBody List<DocumentSequence> documentSequences) {
		log.info("Saving the document sequence :: " + documentSequences.toString());
		documentSequences = documentSequenceService.save(documentSequences);
		log.info("Document Sequence saved successfully");
		return ResponseEntity.ok(documentSequences);
	}

	/**
	 * get the all document value
	 * @return
	 */
	@PostMapping("/get/all")
	public ResponseEntity<PaginationResponse> findAll(@RequestBody PaginationRequest paginationRequest) {
		log.info("Get all Make Payment started.");
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse = documentSequenceService.findAll(paginationRequest);
		log.info("Get all Make Payment completed.");
		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}

	/**
	 * get the Document Sequence by id
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public ResponseEntity<DocumentSequence> findById(@RequestParam Long id) {
		log.info("Get Document Sequence for ID :: " + id);
		DocumentSequence documentSequences = documentSequenceService.getDocumentSequenceById(id);
		if (documentSequences == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from find by id document Sequence");
		return new ResponseEntity<>(documentSequences, HttpStatus.OK);
	}

	/*
	 * Generate the number from the document
	 * @return
	 */
	@GetMapping("/get-document-sequence-numbers")
	public ResponseEntity<String> getDocumentSequenceNames(@RequestParam String transactionalDate, @RequestParam Long subsidiaryId, @RequestParam String formName,
			@RequestParam boolean isDeleted) {
		log.info("Document sequence filterd .");
		String documentSequences = documentSequenceService.getDocumentSequenceNumbers(transactionalDate, subsidiaryId, formName, false);
		log.info("Delete Document Sequence filterd Completed.");
		return new ResponseEntity<>(documentSequences, HttpStatus.OK);
	}

	/**
	 * soft delete the Document sequence by it's id
	 * @param id
	 * @return
	 */
	@GetMapping("/delete")
	public ResponseEntity<Boolean> deleteById(@RequestParam Long id) {
		log.info("Document sequence by ID :: " + id);
		boolean isDeleted = false;
		isDeleted = documentSequenceService.deleteById(id);
		log.info("Delete Document sequence by ID Completed.");
		return new ResponseEntity<>(isDeleted, HttpStatus.OK);
	}

}