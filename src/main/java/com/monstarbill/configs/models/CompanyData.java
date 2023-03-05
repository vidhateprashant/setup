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
@Table(name = "company_data")
@ToString
@Audited
@AuditTable("company_data_aud")
public class CompanyData implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Company Name is mandatory")
	@Column(name = "company_name", nullable = false)
	private String companyName;

	@Column(name = "account_id")
	private String accountId;

	@Column(name = "data_center")
	private String dataCenter;

	@Column(name = "activation_date")
	private Date activationDate;

	@Column(name = "de_activation_date")
	private Date deActivationDate;

	@Email
	private String email;

	private String fullName;

	private String vat;

	private String website;

	private String currency;

	@NotBlank(message = "Country Should not be blank")
	@Column(name = "country")
	private String country;

	private String attention;

	private String addressee;

	private String phone;

	private String address1;

	private String address2;

	private String city;

	private String state;

	private String zipCode;

	@Column(name = "company_logo_metadata")
	private String companyLogoMetadata;

	@Lob
	private byte[] companyLogo;

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;

	@CreationTimestamp
	@Column(name = "created_date", updatable = false)
	private Date createdDate;

	@Column(name = "created_by")
	private String createdBy;

	@UpdateTimestamp
	@Column(name = "last_modified_date")
	private Timestamp lastModifiedDate;

	@Column(name = "last_modified_by")
	private String lastModifiedBy;
	
	@Transient
	private Employee employee;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public List<CompanyDataHistory> compareFields(CompanyData companyData)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<CompanyDataHistory> comapnyDataHistories = new ArrayList<CompanyDataHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(companyData);

				if (oldValue == null) {
					if (newValue != null) {
						comapnyDataHistories.add(this.prepareComapnyDataHistory(companyData, field));
					}
				} else if (!oldValue.equals(newValue)) {
					comapnyDataHistories.add(this.prepareComapnyDataHistory(companyData, field));
				}
			}
		}
		return comapnyDataHistories;
	}

	private CompanyDataHistory prepareComapnyDataHistory(CompanyData companyData, Field field)
			throws IllegalAccessException {
		CompanyDataHistory comapnyDataHistory = new CompanyDataHistory();
		comapnyDataHistory.setCompanyDataId(companyData.getId());
		comapnyDataHistory.setModuleName(AppConstants.COMPANY_DATA);
		comapnyDataHistory.setChangeType(AppConstants.UI);
		comapnyDataHistory.setOperation(Operation.UPDATE.toString());
		comapnyDataHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) {
			comapnyDataHistory.setOldValue(field.get(this).toString());
		}
		if (field.get(companyData) != null) {
			comapnyDataHistory.setNewValue(field.get(companyData).toString());
		}
		comapnyDataHistory.setLastModifiedBy(companyData.getLastModifiedBy());
		return comapnyDataHistory;
	}

}
