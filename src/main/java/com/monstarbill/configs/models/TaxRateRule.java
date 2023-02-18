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
@Table(	name = "tax_rate_rule")
@ToString
@Audited
@AuditTable("tax_rate_rule_aud")
public class TaxRateRule implements Cloneable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tax_group_id", nullable = false)
	private Long taxGroupId;
	
	@NotNull(message = "Tax rate id is mandatory")
	@Column(name = "tax_rate_id")
	private Long taxRateId;

	@Column(name="is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;
	
	@CreationTimestamp
	@Column(name="created_date", updatable = false)
	private Date createdDate;

	@Column(name="created_by", updatable = false)
	private String createdBy;

	@UpdateTimestamp
	@Column(name="last_modified_date")
	private Timestamp lastModifiedDate;

	@Column(name="last_modified_by")
	private String lastModifiedBy;
	
	@Transient
	private String taxRateName;
	
	@Transient
	private String taxRateType;
	
	@Transient
	private String taxRates;
	
	@Transient
	private String operation;
	
	public TaxRateRule(Long id, Long taxGroupId, Long taxRateId, String taxRateName, String taxRateType, String taxRates) {
		this.id = id;
		this.taxGroupId = taxGroupId;
		this.taxRateId = taxRateId;
		this.taxRateName = taxRateName;
		this.taxRateType = taxRateType;
		this.taxRates = taxRates;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param taxRateRule
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<TaxGroupHistory> compareFields(TaxRateRule taxRateRule)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<TaxGroupHistory> TaxGroupHistories = new ArrayList<TaxGroupHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(taxRateRule);

				if (oldValue == null) {
					if (newValue != null) {
						TaxGroupHistories.add(this.prepareTaxGroupHistory(taxRateRule, field));
					}
				} else if (!oldValue.equals(newValue)) {
					TaxGroupHistories.add(this.prepareTaxGroupHistory(taxRateRule, field));
				}
			}
		}
		return TaxGroupHistories;
	}

	private TaxGroupHistory prepareTaxGroupHistory(TaxRateRule taxRateRule, Field field) throws IllegalAccessException {
		TaxGroupHistory taxGroupHistory = new TaxGroupHistory();
		taxGroupHistory.setTaxGroupId(taxRateRule.getTaxGroupId());
		taxGroupHistory.setChildId(taxRateRule.getId());
		taxGroupHistory.setModuleName(AppConstants.TAX_RATE_RULE);
		taxGroupHistory.setChangeType(AppConstants.UI);
		taxGroupHistory.setOperation(Operation.UPDATE.toString());
		taxGroupHistory.setLastModifiedBy(taxRateRule.getLastModifiedBy());
		taxGroupHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) 
			taxGroupHistory.setOldValue(field.get(this).toString());
		if (field.get(taxRateRule) != null) 
			taxGroupHistory.setNewValue(field.get(taxRateRule).toString());
		return taxGroupHistory;
	}
	
}


