package com.monstarbill.configs.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monstarbill.configs.common.AppConstants;
import com.monstarbill.configs.common.CommonUtils;
import com.monstarbill.configs.common.CustomException;
import com.monstarbill.configs.common.CustomMessageException;
import com.monstarbill.configs.dao.DocumentSequenceDao;
import com.monstarbill.configs.enums.Operation;
import com.monstarbill.configs.models.DocumentSequence;
import com.monstarbill.configs.models.DocumentSequenceHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.repository.DocumentSequenceHistoryRepository;
import com.monstarbill.configs.repository.DocumentSequenceRepository;
import com.monstarbill.configs.service.DocumentSequenceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class DocumentSequenceServiceImpl implements DocumentSequenceService {

	@Autowired
	private DocumentSequenceDao documentSequenceDao;

	@Autowired
	private DocumentSequenceRepository documentSequenceRepository;

	@Autowired
	private DocumentSequenceHistoryRepository documentSequenceHistoryRepository;

	@Override
	public List<DocumentSequence> save(List<DocumentSequence> documentSequences) {
		Optional<DocumentSequence> oldDocumentSequence = Optional.empty();
		for (DocumentSequence documentSequence : documentSequences) {

			oldDocumentSequence = Optional.empty();
			Date existingEndDate = null;
			Date existingStartDate = null;
			Date currentEndDate = documentSequence.getEndDate();
			Date curentStartDate = documentSequence.getStartDate();
			List<DocumentSequence> documentSequenceExistValue = new ArrayList<>();
			documentSequenceExistValue = this.documentSequenceRepository.findBySubsidiaryIdAndTypeAndIsDeleted(
					documentSequence.getSubsidiaryId(), documentSequence.getType(), false);
			if (documentSequence.getId() == null) {
				if (documentSequence.getEndDate() != null) {
					for (DocumentSequence existingDocumentSequence : documentSequenceExistValue) {
//						if (documentSequence.getEndDate() == null) {
//							existingEndDate = systemDate;
						existingEndDate = existingDocumentSequence.getEndDate();
						existingStartDate = existingDocumentSequence.getStartDate();
						if ((existingEndDate.compareTo(curentStartDate) > 0)
										&& (currentEndDate.compareTo(existingStartDate) > 0)) {
							log.error("Document Sequence for same type and subsidiary already exsist for the selected time range ");
							throw new CustomException("Document Sequence for same type and subsidiary already exsist for the selected time range ");
						}
					}

					if (curentStartDate.compareTo(currentEndDate) > 0) {
						log.error("End date is less than Start date");
						throw new CustomException("End date is less than Start date");
					}
				} else {
					for (DocumentSequence existingDocumentSequence : documentSequenceExistValue) {
							if (existingDocumentSequence.getEndDate() == null) {
									log.error(" Enter End date for previous sequence subsidiary :"
											+ documentSequence.getSubsidiaryId() + " type : "
											+ documentSequence.getType() +" and start date : "+documentSequence.getStartDate().toString());
									throw new CustomException( "Enter End date for previous sequence subsidiary :"
											+ documentSequence.getSubsidiaryId() + " type : "
											+ documentSequence.getType() +" and start date : "+documentSequence.getStartDate().toString());
								
							}
							if (existingDocumentSequence.getEndDate() != null) {
								existingEndDate = existingDocumentSequence.getEndDate();
								existingStartDate = existingDocumentSequence.getStartDate();
								if ((existingEndDate.compareTo(curentStartDate) > 0) || (existingStartDate.compareTo(curentStartDate) < 0)) {
									log.error(" Check data for correct entry ");
									throw new CustomException("Check data for correct entry ");
								}
							}
						
					}
				}
			}
			if (documentSequence.getId() == null) {
				documentSequence.setCreatedBy(CommonUtils.getLoggedInUsername());
				documentSequence.setCurrentValue(documentSequence.getInitialNumber());
			} else {
				// Get the existing object using the deep copy
				oldDocumentSequence = this.documentSequenceRepository.findByIdAndIsDeleted(documentSequence.getId(),
						false);
				if (oldDocumentSequence.isPresent()) {
					try {
						oldDocumentSequence = Optional.ofNullable((DocumentSequence) oldDocumentSequence.get().clone());
					} catch (CloneNotSupportedException e) {
						log.error("Error while Cloning the object. Please contact administrator.");
						throw new CustomException("Error while Cloning the object. Please contact administrator.");
					}
				}
			}
			
			Long minimumDigit = documentSequence.getMinimumDigit();
			Long initialNumber = documentSequence.getInitialNumber();
			if (String.valueOf(initialNumber).length() < minimumDigit) {
				throw new CustomMessageException("Length of Initial Number should be greater than minimum length.");
			}
			documentSequence.setLastModifiedBy(CommonUtils.getLoggedInUsername());
			try {
				documentSequence = this.documentSequenceRepository.save(documentSequence);
			} catch (DataIntegrityViolationException e) {
				log.error("Document Sequence unique constrain violetd." + e.getMostSpecificCause());
				throw new CustomException(" Document Sequence unique constrain violetd :" + e.getMostSpecificCause());
			}

			if (documentSequence == null) {
				log.info("Document Sequence is not exsist for this selected tranasaction data.");
				throw new CustomMessageException(
						"Document Sequence is not exsist for this selected tranasaction data. ");
			}
			// update the data in document history table
			this.updateDocumentSequenceHistory(documentSequence, oldDocumentSequence);
		}
		return documentSequences;

	}

	private void updateDocumentSequenceHistory(DocumentSequence documentSequence,
			Optional<DocumentSequence> oldDocumentSequence) {
		if (oldDocumentSequence.isPresent()) {
			// insert the updated fields in history table
			List<DocumentSequenceHistory> documentSequenceHistories = new ArrayList<DocumentSequenceHistory>();
			try {
				documentSequenceHistories = oldDocumentSequence.get().compareFields(documentSequence);
				if (CollectionUtils.isNotEmpty(documentSequenceHistories)) {
					this.documentSequenceHistoryRepository.saveAll(documentSequenceHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("DocumentSequence Histories is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.documentSequenceHistoryRepository.save(this.preparedocumentSequenceHistory(documentSequence.getId(),
					null, AppConstants.DOCUMENT_SEQUENCE, Operation.CREATE.toString(),
					documentSequence.getLastModifiedBy(), null, String.valueOf(documentSequence.getId())));
		}
	}

	/**
	 * Prepares the history for the document Sequence
	 * 
	 * @param documentSequenceId
	 * @param moduleName
	 * @param operation
	 * @param lastModifiedBy
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public DocumentSequenceHistory preparedocumentSequenceHistory(Long documentSequenceId, Long childId,
			String moduleName, String operation, String lastModifiedBy, String oldValue, String newValue) {
		DocumentSequenceHistory documentSequenceHistory = new DocumentSequenceHistory();
		documentSequenceHistory.setDocumentSequenceId(documentSequenceId);
		documentSequenceHistory.setChildId(childId);
		documentSequenceHistory.setModuleName(moduleName);
		documentSequenceHistory.setChangeType(AppConstants.UI);
		documentSequenceHistory.setOperation(operation);
		documentSequenceHistory.setOldValue(oldValue);
		documentSequenceHistory.setNewValue(newValue);
		documentSequenceHistory.setLastModifiedBy(lastModifiedBy);
		return documentSequenceHistory;
	}

	@Override
	public DocumentSequence getDocumentSequenceById(Long id) {
		Optional<DocumentSequence> documentSequences = Optional.ofNullable(new DocumentSequence());
		documentSequences = this.documentSequenceRepository.findByIdAndIsDeleted(id, false);
		if (!documentSequences.isPresent()) {
			log.info("DocumentSequence is not found given id : " + id);
			throw new CustomMessageException("documentSequence is not found given id : " + id);

		}

		return documentSequences.get();
	}

	@Override
	public String getDocumentSequenceNumbers(String transactionalDate, Long subsidiaryId, String type,
			boolean isDeleted) {
		Optional<DocumentSequence> documentSequences = Optional.empty();
		documentSequences = documentSequenceRepository.findBySubsidiaryIdAndTypeAndisDeleted(transactionalDate,
				subsidiaryId, type, false);
		if (!documentSequences.isPresent()) {
			log.error("Document Sequence the correct value is not present");
			throw new CustomException("Document Sequence the correct value is not present");
		}
		StringBuilder documentSequenceTotalValue = new StringBuilder();
		if (StringUtils.isNotEmpty(documentSequences.get().getPrefix())) {
			documentSequenceTotalValue.append(documentSequences.get().getPrefix());
		} 
			documentSequenceTotalValue.append(documentSequences.get().getCurrentValue());
			
		if (StringUtils.isNotEmpty(documentSequences.get().getSuffix())) {
			documentSequenceTotalValue.append(documentSequences.get().getSuffix());
		}
		log.info(" Creating a Document Sequence string bt taking the concated value");
		String documentNumber = documentSequenceTotalValue.toString();
		documentSequences.get().setCurrentValue(documentSequences.get().getCurrentValue() + 1);
		log.info("Document Sequence the current value is increasing by 1");
		this.documentSequenceRepository.save(documentSequences.get());
		return documentNumber;
	}

	@Override
	public boolean deleteById(Long id) {
		DocumentSequence documentSequences = new DocumentSequence();
		documentSequences = this.getDocumentSequenceById(id);
		documentSequences.setDeleted(true);
		documentSequences = this.documentSequenceRepository.save(documentSequences);
		log.info("Deleting the Document Sequence ");
		if (documentSequences == null) {
			log.error("Error while deleting  : " + id);
			throw new CustomMessageException("Error while deleting  : " + id);
		}
		this.documentSequenceHistoryRepository.save(this.preparedocumentSequenceHistory(documentSequences.getId(), null,
				AppConstants.DOCUMENT_SEQUENCE, Operation.DELETE.toString(), documentSequences.getLastModifiedBy(),
				String.valueOf(documentSequences.getId()), null));
		return true;
	}

	@Override
	public PaginationResponse findAll(PaginationRequest paginationRequest) {
		List<DocumentSequence> documentSequence = new ArrayList<DocumentSequence>();

		// preparing where clause
		String whereClause = this.prepareWhereClause(paginationRequest).toString();

		// get list
		documentSequence = this.documentSequenceDao.findAll(whereClause, paginationRequest);

		// getting count
		Long totalRecords = this.documentSequenceDao.getCount(whereClause);

		return CommonUtils.setPaginationResponse(paginationRequest.getPageNumber(), paginationRequest.getPageSize(),
				documentSequence, totalRecords);
	}

	private StringBuilder prepareWhereClause(PaginationRequest paginationRequest) {
		StringBuilder whereClause = new StringBuilder(" AND ds.isDeleted is false");
		return whereClause;
	}

}
