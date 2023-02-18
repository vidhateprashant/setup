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
@Table(name = "fiscal")
@ToString
@Audited
@AuditTable("fiscal_aud")
public class FiscalCalender implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "name is mandatory")
	@Column(name = "name", nullable = false, updatable = false, unique = true)
	private String name;

	@NotBlank(message = "Start month is mandatory")
	@Column(name = "start_month", updatable = false)
	private String startMonth;

	@Column(name = "end_month", updatable = false)
	private String endMonth;

	@Column(name = "is_fiscal_calender", columnDefinition = "boolean default true")
	private boolean isFiscalCalender;

	@Column(name = "is_tax_calender", columnDefinition = "boolean default true")
	private boolean isTaxCalender;

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;

	@Column(name = "subsidiary_id")
	private Long subsidiaryId;
	
	@Column(name="is_active", columnDefinition = "boolean default true")
	private boolean isActive;

	@Column(name="active_date")
	private Date activeDate;
	
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
	private String startMonthName;

	@Transient
	private String endMonthName;

	@Transient
	private String subsidiaryName;
	
	@Transient
	private String yearName;
	
	@Transient
	private String status;

	@Transient
	private List<FiscalCalanderAccounting> fiscalCalanderAccounting;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public FiscalCalender(Long id, String name, String startMonth, String endMonth,
			 String subsidiaryName, String yearName, boolean isActive ) {
		this.id = id;
		this.name = name;
		this.startMonth = startMonth;
		this.endMonth = endMonth;
		this.subsidiaryName = subsidiaryName;
		this.yearName = yearName;
		if (isActive) {
			this.status = Status.ACTIVE.toString();			
		} else {
			this.status = Status.INACTIVE.toString();
		}
	}

	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param fiscal Calender
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<FiscalCalenderHistory> compareFields(FiscalCalender fiscalCalander)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<FiscalCalenderHistory> fiscalCalanderHistories = new ArrayList<FiscalCalenderHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(fiscalCalander);

				if (oldValue == null) {
					if (newValue != null) {
						fiscalCalanderHistories.add(this.prepareFiscalCalanderHistory(fiscalCalander, field));
					}
				} else if (!oldValue.equals(newValue)) {
					fiscalCalanderHistories.add(this.prepareFiscalCalanderHistory(fiscalCalander, field));
				}
			}
		}
		return fiscalCalanderHistories;
	}

	private FiscalCalenderHistory prepareFiscalCalanderHistory(FiscalCalender fiscalCalander, Field field)
			throws IllegalAccessException {
		FiscalCalenderHistory fiscalCalenderHistory = new FiscalCalenderHistory();
		fiscalCalenderHistory.setFiscalCalenderId(fiscalCalander.getId());
		fiscalCalenderHistory.setModuleName(AppConstants.FISCAL_CALENDER);
		fiscalCalenderHistory.setChangeType(AppConstants.UI);
		fiscalCalenderHistory.setOperation(Operation.UPDATE.toString());
		fiscalCalenderHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null)
			fiscalCalenderHistory.setOldValue(field.get(this).toString());
		if (field.get(fiscalCalander) != null)
			fiscalCalenderHistory.setNewValue(field.get(fiscalCalander).toString());
		fiscalCalenderHistory.setLastModifiedBy(fiscalCalander.getLastModifiedBy());
		return fiscalCalenderHistory;
	}

}
