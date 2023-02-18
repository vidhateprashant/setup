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
import com.monstarbill.configs.enums.Status;

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
@Table(schema = "setup", name = "tax_group")
@ToString
@Audited
@AuditTable("tax_group_aud")
public class TaxGroup implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Subsidiary Id is mandatory")
	@Column(name = "subsidiary_id", updatable = false)
	private Long subsidiaryId;

	@Column(name = "country", updatable = false)
	private String country;

	@NotBlank(message = "Tax Group name is mandatory")
	@Column(name = "name", updatable = false, unique = true)
	private String name;

	private String description;

	@NotBlank(message = "Available on is mandatory")
	@Column(name = "available_on")
	private String availableOn;

	@Column(name = "is_inclusive", columnDefinition = "boolean default false")
	private boolean isInclusive;

	@Column(name = "is_active", columnDefinition = "boolean default true")
	private boolean isActive;
	
	@Column(name="active_date")
	private Date activeDate;

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;

	@CreationTimestamp
	@Column(name = "created_date", updatable = false)
	private Date createdDate;

	@Column(name = "created_by", updatable = false)
	private String createdBy;

	@UpdateTimestamp
	@Column(name = "last_modified_date")
	private Timestamp lastModifiedDate;

	@Column(name = "last_modified_by")
	private String lastModifiedBy;

	@Transient
	private List<TaxRateRule> taxRateRules;

	@Transient
	private String subsidiaryName;
	
	@Transient
	private String status;

	public TaxGroup(Long id, Long subsidiaryId, String country, String name, String description, String subsidiaryName, boolean isActive) {
		this.id = id;
		this.subsidiaryId = subsidiaryId;
		this.country = country;
		this.name = name;
		this.description = description;
		this.subsidiaryName = subsidiaryName;
		this.isActive = isActive;
		if (isActive) {
			this.status = Status.ACTIVE.toString();
		} else {
			this.status = Status.INACTIVE.toString();
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param Tax Group
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<TaxGroupHistory> compareFields(TaxGroup taxGroup)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<TaxGroupHistory> taxGroupHistories = new ArrayList<TaxGroupHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(taxGroup);

				if (oldValue == null) {
					if (newValue != null) {
						taxGroupHistories.add(this.prepareTaxGroupHistory(taxGroup, field));
					}
				} else if (!oldValue.equals(newValue)) {
					taxGroupHistories.add(this.prepareTaxGroupHistory(taxGroup, field));
				}
			}
		}
		return taxGroupHistories;
	}

	private TaxGroupHistory prepareTaxGroupHistory(TaxGroup taxGroup, Field field) throws IllegalAccessException {
		TaxGroupHistory taxGroupHistory = new TaxGroupHistory();
		taxGroupHistory.setTaxGroupId(taxGroup.getId());
		taxGroupHistory.setModuleName(AppConstants.TAX_GROUP);
		taxGroupHistory.setChangeType(AppConstants.UI);
		taxGroupHistory.setOperation(Operation.UPDATE.toString());
		taxGroupHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) {
			taxGroupHistory.setOldValue(field.get(this).toString());
		}
		if (field.get(taxGroup) != null) {
			taxGroupHistory.setNewValue(field.get(taxGroup).toString());
		}
		taxGroupHistory.setLastModifiedBy(taxGroup.getLastModifiedBy());
		return taxGroupHistory;
	}

}
