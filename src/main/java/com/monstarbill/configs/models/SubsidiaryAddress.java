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
@Table(	name = "subsidiary_address")
@ToString
@Audited
@AuditTable("subsidiary_address_aud")
public class SubsidiaryAddress implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "subsidiary_id", nullable = false)
	private Long subsidiaryId;

	@NotBlank(message = "Country is mandatory")
	private String country;

	private String attention;

	private String address;

	private String phone;

	@NotBlank(message = "Address1 is mandatory")
	private String address1;

	private String address2;

	private String city;

	private String state;

	private String zipcode;
	
	@Column(name="is_active", columnDefinition = "boolean default false")
	private boolean isActive;
	
	@Column(name="is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;

	@Column(name = "registration_code")
	private String registrationCode;

	@Column(name = "registration_type")
	private String registrationType;
	
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
	 * @param subsidiaryAddress
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<SubsidiaryHistory> compareFields(SubsidiaryAddress subsidiaryAddress)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<SubsidiaryHistory> subsidiaryAddressHistories = new ArrayList<SubsidiaryHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(subsidiaryAddress);

				if (oldValue == null) {
					if (newValue != null) {
						subsidiaryAddressHistories.add(this.prepareSubsidiaryAddressHistory(subsidiaryAddress, field));
					}
				} else if (!oldValue.equals(newValue)) {
					subsidiaryAddressHistories.add(this.prepareSubsidiaryAddressHistory(subsidiaryAddress, field));
				}
			}
		}
		return subsidiaryAddressHistories;
	}

	private SubsidiaryHistory prepareSubsidiaryAddressHistory(SubsidiaryAddress subsidiaryAddress, Field field) throws IllegalAccessException {
		SubsidiaryHistory subsidiaryHistory = new SubsidiaryHistory();
		subsidiaryHistory.setSubsidiaryId(subsidiaryAddress.getSubsidiaryId());
		subsidiaryHistory.setChildId(subsidiaryAddress.getId());
		subsidiaryHistory.setModuleName(AppConstants.SUBSIDIARY_ADDRESS);
		subsidiaryHistory.setChangeType(AppConstants.UI);
		subsidiaryHistory.setOperation(Operation.UPDATE.toString());
		subsidiaryHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) subsidiaryHistory.setOldValue(field.get(this).toString());
		if (field.get(subsidiaryAddress) != null) subsidiaryHistory.setNewValue(field.get(subsidiaryAddress).toString());
		subsidiaryHistory.setLastModifiedBy(subsidiaryAddress.getLastModifiedBy());
		return subsidiaryHistory;
	}
}