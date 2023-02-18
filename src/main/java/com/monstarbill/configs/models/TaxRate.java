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
@Table(	name = "tax_rate")
@ToString
@Audited
@AuditTable("tax_rate_aud")
public class TaxRate implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Supplier ID is mandatory")
	@Column(name="supplier_id", updatable = false)
	private Long subsidiaryId;

	@NotBlank(message = "Vendor Registration Type is mandatory")
	@Column(name="vendor_registration_type")
	private String vendorRegistrationType;

	@NotBlank(message = "Tax type is mandatory")
	@Column(name="tax_type")
	private String taxType;

	@NotBlank(message = "Tax name is mandatory")
	@Column(name="tax_name", unique = true)
	private String taxName;

	@NotBlank(message = "Tax Rate is mandatory")
	@Column(name="tax_rates")
	private String taxRates;

	@NotBlank(message = "Available on is mandatory")
	@Column(name="available_on")
	private String availableOn;

	@Column(name="is_itc", columnDefinition = "boolean default false")
	private boolean isItc;

	@Column(name="is_rcm", columnDefinition = "boolean default false")
	private boolean isRcm;

	@NotNull(message = "Effective from is mandatory")
	@Column(name="effective_from")
	private Date effectiveFrom;

	@Column(name="effective_to")
	private Date effectiveTo;

	@Column(name="input_tax_account", updatable = false)
	private String inputTaxAccount;

	@Column(name="output_tax_account", updatable = false)
	private String outputTaxAccount;

	@Column(name="tds_account_code", updatable = false)
	private String tdsAccountCode;
	
	@Column(name="is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;
	
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param taxRate
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<TaxRateHistory> compareFields(TaxRate taxRate)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<TaxRateHistory> taxRateHistories = new ArrayList<TaxRateHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(taxRate);

				if (oldValue == null) {
					if (newValue != null) {
						taxRateHistories.add(this.prepareTaxRateHistory(taxRate, field));
					}
				} else if (!oldValue.equals(newValue)) {
					taxRateHistories.add(this.prepareTaxRateHistory(taxRate, field));
				}
			}
		}
		return taxRateHistories;
	}

	/**
	 * Preapre the history object
	 * @param taxRate
	 * @param field
	 * @return
	 * @throws IllegalAccessException
	 */
	private TaxRateHistory prepareTaxRateHistory(TaxRate taxRate, Field field) throws IllegalAccessException {
		TaxRateHistory taxRateHistory = new TaxRateHistory();
		taxRateHistory.setTaxRateId(taxRate.getId());
		taxRateHistory.setModuleName(AppConstants.TAX_RATE);
		taxRateHistory.setChangeType(AppConstants.UI);
		taxRateHistory.setOperation(Operation.UPDATE.toString());
		taxRateHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) taxRateHistory.setOldValue(field.get(this).toString());
		if (field.get(taxRate) != null) taxRateHistory.setNewValue(field.get(taxRate).toString());
		taxRateHistory.setLastModifiedBy(taxRate.getLastModifiedBy());
		return taxRateHistory;
	}
}