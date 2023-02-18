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
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

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
@Table(schema = "setup", name = "subsidiary")
@ToString
@Audited
@AuditTable("subsidiary_aud")
public class Subsidiary implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Name is mandatory")
	@Column(nullable = false, updatable = false, unique = true)
	private String name;

	@Column(name="legal_name")
	private String legalName;

	@Column(name="parent_company", updatable = false)
	private String parentCompany;

	@NotBlank(message = "Country is mandatory")
	@Column(updatable = false)
	private String country;

	@Email(message = "Email is not valid")
	private String email;

	private String website;

	@Column(updatable = false)
	private String language;

	@NotBlank(message = "Currency is mandatory")
	@Column(updatable = false)
	private String currency;

	@NotBlank(message = "Fiscal Calender is mandatory")
	@Column(name="fiscal_calender")
	private String fiscalCalender;

	//@Column(unique = true)
	private String pan;
	
	//@Column(unique = true)
	private String cin;
	
	//@Column(unique = true)
	private String tan;
	
	@Column(name = "bid_mail")
	private String bidMail;

	@Column(name = "invoice_mail")
	private String invoiceMail;
	
	private String invoicePasswd;

	@Column(name = "admin_mail")
	private String adminMail;

	@Column(name="is_active", columnDefinition = "boolean default true")
	private boolean isActive;
	
	@Column(name="active_date")
	private Date activeDate;
	
	@Column(name="is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;

	private String integrated_id;
	
	@Column(name = "logo_metadata")
	private String logoMetadata;
	
	@Lob
	private byte[] logo;

	@CreationTimestamp
	@Column(name="created_date", updatable = false)
	private Date createdDate;

	@Column(name="created_by")
	private String createdBy;

	@UpdateTimestamp
	@Column(name="last_modified_date")
	private Timestamp lastModifiedDate;

	@Column(name="last_modified_by")
	private String lastModifiedBy;
	
	@Transient
	private List<SubsidiaryAddress> subsidiaryAddresses;
	
	public Subsidiary(Long id, String name, String parentCompany, String currency, Date createdDate) {
		this.id = id;
		this.name = name;
		this.parentCompany = parentCompany;
		this.currency = currency;
		this.createdDate = createdDate;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param subsidiary
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<SubsidiaryHistory> compareFields(Subsidiary subsidiary)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<SubsidiaryHistory> subsidiaryHistories = new ArrayList<SubsidiaryHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(subsidiary);

				if (oldValue == null) {
					if (newValue != null) {
						subsidiaryHistories.add(this.prepareSubsidiaryHistory(subsidiary, field));
					}
				} else if (!oldValue.equals(newValue)) {
					subsidiaryHistories.add(this.prepareSubsidiaryHistory(subsidiary, field));
				}
			}
		}
		return subsidiaryHistories;
	}

	private SubsidiaryHistory prepareSubsidiaryHistory(Subsidiary subsidiary, Field field) throws IllegalAccessException {
		SubsidiaryHistory subsidiaryHistory = new SubsidiaryHistory();
		subsidiaryHistory.setSubsidiaryId(subsidiary.getId());
		subsidiaryHistory.setModuleName(AppConstants.SUBSIDIARY);
		subsidiaryHistory.setChangeType(AppConstants.UI);
		subsidiaryHistory.setOperation(Operation.UPDATE.toString());
		subsidiaryHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) {
			subsidiaryHistory.setOldValue(field.get(this).toString());			
		}
		if (field.get(subsidiary) != null) {
			subsidiaryHistory.setNewValue(field.get(subsidiary).toString());
		}
		subsidiaryHistory.setLastModifiedBy(subsidiary.getLastModifiedBy());
		return subsidiaryHistory;
	}
}
