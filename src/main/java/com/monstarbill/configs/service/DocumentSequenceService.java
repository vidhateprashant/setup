package com.monstarbill.configs.service;

import java.util.List;

import com.monstarbill.configs.models.DocumentSequence;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;

public interface DocumentSequenceService {
	
	public DocumentSequence getDocumentSequenceById(Long id);

	public String getDocumentSequenceNumbers(String transactionalDate,Long subsidiaryId, String type, boolean isDeleted);
	
	public boolean deleteById(Long id);
	
	public List<DocumentSequence> save(List<DocumentSequence> documentSequences);
	
	public PaginationResponse findAll(PaginationRequest paginationRequest);
	
}
