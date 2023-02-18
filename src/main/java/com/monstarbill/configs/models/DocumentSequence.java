package com.monstarbill.configs.models;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import com.monstarbill.configs.common.AppConstants;
import com.monstarbill.configs.common.CommonUtils;
import com.monstarbill.configs.enums.Operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_sequence")
@ToString
@Audited
@AuditTable("document_sequence_aud")
public class DocumentSequence implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Subsidiary is mandatory")
	@Column(name = "subsidiary_id", nullable = false, updatable = false)
	private Long subsidiaryId;

	@NotBlank(message = "Type is mandatory")
	@Column(name = "type", updatable = false)
	private String type;

	@NotNull(message = "Initial number is mandetory")
	@Column(name = "initial_number", updatable = false)
	private Long initialNumber;

	@Column(name = "prefix")
	private String prefix;

	@Column(name = "suffix")
	private String suffix;

	@Column(name = "minimum_digit", updatable = false)
	private Long minimumDigit;

	@NotNull(message = "Start date is mandetory")
	@Column(name = "start_date", updatable = false)
	private Date startDate;

	@NotNull(message = "End date is mandetory")
	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;
	
	@Column(name = "is_active", columnDefinition = "boolean default false")
	private boolean isActive;

	@CreationTimestamp
	@Column(name = "created_date", updatable = false)
	private Date createdDate;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "concated_value")
	private String concatedValue;
	
	@UpdateTimestamp
	@Column(name = "last_modified_date")
	private Timestamp lastModifiedDate;

	@Column(name = "last_modified_by")
	private String lastModifiedBy;

	@Column(name = "current_value")
	private Long currentValue;

	@Transient
	private String subsidiaryName;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	

	public DocumentSequence(Long id, String type, Long initialNumber, String prefix, String suffix, Long minimumDigit,
			Date startDate, Date endDate, Long subsidiaryId, String subsidiaryName ) {
		this.id = id;
		this.type = type;
		this.initialNumber = initialNumber;
		this.prefix = prefix;
		this.suffix = suffix;
		this.minimumDigit = minimumDigit;
		this.startDate = startDate;
		this.endDate = endDate;
		this.subsidiaryId = subsidiaryId;
		this.subsidiaryName = subsidiaryName;
		
	}
	
	
	
	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param documentseq
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<DocumentSequenceHistory> compareFields(DocumentSequence documentSequences)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<DocumentSequenceHistory> documentSequenceHistories = new ArrayList<DocumentSequenceHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(documentSequences);

				if (oldValue == null) {
					if (newValue != null) {
						documentSequenceHistories.add(this.prepareDocumentSequenceHistory(documentSequences, field));
					}
				} else if (!oldValue.equals(newValue)) {
					documentSequenceHistories.add(this.prepareDocumentSequenceHistory(documentSequences, field));
				}
			}
		}
		return documentSequenceHistories;
	}

	private DocumentSequenceHistory prepareDocumentSequenceHistory(DocumentSequence documentSequence, Field field) throws IllegalAccessException {
		DocumentSequenceHistory documentSequenceHistory = new DocumentSequenceHistory();
		documentSequenceHistory.setDocumentSequenceId(documentSequence.getId());
		documentSequenceHistory.setModuleName(AppConstants.DOCUMENT_SEQUENCE);
		documentSequenceHistory.setChangeType(AppConstants.UI);
		documentSequenceHistory.setOperation(Operation.UPDATE.toString());
		documentSequenceHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if(field.get(this)!=null) {
			documentSequenceHistory.setOldValue(field.get(this).toString());
		}
		if(field.get(documentSequence)!=null) {
			documentSequenceHistory.setNewValue(field.get(documentSequence).toString());
		}
		documentSequenceHistory.setLastModifiedBy(documentSequence.getLastModifiedBy());
		return documentSequenceHistory;
	}


	public DocumentSequence(Long id,Long subsidiaryId,
			String type, Date startDate, Date endDate) {
		this.id = id;
		this.subsidiaryId = subsidiaryId;
		this.type = type;
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
