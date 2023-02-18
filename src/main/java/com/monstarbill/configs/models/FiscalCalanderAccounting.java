package com.monstarbill.configs.models;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(	name = "fiscal_calender_accounting")
@ToString
@Audited
@AuditTable("fiscal_calender_accounting_aud")
public class FiscalCalanderAccounting implements Cloneable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="year_name")
	private String yearName;
	
	@Column(name="fiscal_id", updatable = false)
	private Long fiscalId;
	
	@Column(name="start_year", updatable = false)
	private int startYear;
	
	@Column(name="end_year", updatable = false)
	private int endYear;
	
	@Column(name="month")
	private String month;
	
	@Column(name = "from_date")
	private LocalDate fromDate;
	
	@Column(name = "to_date")
	private LocalDate toDate;
	
	@Column(name="is_lock", columnDefinition = "boolean default true")
	private boolean isLock;
	
	@Column(name="is_period_open", columnDefinition = "boolean default true")
	private boolean isPeriodOpen;
	
	@Column(name="is_period_close", columnDefinition = "boolean default true")
	private boolean isPeriodClose;
	
	@Column(name="is_year_completed", columnDefinition = "boolean default true")
	private boolean isYearCompleted;

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
	
	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public FiscalCalanderAccounting(String month, LocalDate fromDate, LocalDate toDate, boolean isLock, boolean isPeriodOpen, int startYear, int endYear, boolean isYearCompleted) {
		this.month = month;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.isLock = isLock;
		this.isPeriodOpen= isPeriodOpen;
		this.startYear= startYear;
		this.endYear= endYear;
		this.isYearCompleted= isYearCompleted;
		
	}
	
	
	
	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param fiscal calender
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<FiscalCalenderHistory> compareFields(FiscalCalanderAccounting fiscalCalanderAccounting)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<FiscalCalenderHistory> fiscalCalanderHistories = new ArrayList<FiscalCalenderHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(fiscalCalanderAccounting);

				if (oldValue == null) {
					if (newValue != null) {
						fiscalCalanderHistories.add(this.prepareFiscalCalanderHistory(fiscalCalanderAccounting, field));
					}
				} else if (!oldValue.equals(newValue)) {
					fiscalCalanderHistories.add(this.prepareFiscalCalanderHistory(fiscalCalanderAccounting, field));
				}
			}
		}
		return fiscalCalanderHistories;
	}

	private FiscalCalenderHistory prepareFiscalCalanderHistory(FiscalCalanderAccounting fiscalCalanderAccounting, Field field) throws IllegalAccessException {
		FiscalCalenderHistory fiscalCalenderHistory = new FiscalCalenderHistory();
		fiscalCalenderHistory.setFiscalCalenderId(fiscalCalanderAccounting.getId());
		fiscalCalenderHistory.setModuleName(AppConstants.FISCAL_CALENDER_ACCOUNTING);
		fiscalCalenderHistory.setChangeType(AppConstants.UI);
		fiscalCalenderHistory.setOperation(Operation.UPDATE.toString());
		fiscalCalenderHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null)
			fiscalCalenderHistory.setOldValue(field.get(this).toString());
		if (field.get(fiscalCalanderAccounting) != null)
			fiscalCalenderHistory.setNewValue(field.get(fiscalCalanderAccounting).toString());
		fiscalCalenderHistory.setLastModifiedBy(fiscalCalanderAccounting.getLastModifiedBy());
		return fiscalCalenderHistory;
	}

	public FiscalCalanderAccounting(Long id, String yearName, Long fiscalId, int startYear, int endYear, String month,
			LocalDate fromDate, LocalDate toDate, boolean isLock, boolean isPeriodOpen, boolean isPeriodClose,
			boolean isYearCompleted) {
		this.id = id;
		this.yearName = yearName;
		this.fiscalId = fiscalId;
		this.startYear = startYear;
		this.endYear = endYear;
		this.month = month;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.isLock = isLock;
		this.isPeriodOpen = isPeriodOpen;
		this.isPeriodClose = isPeriodClose;
		this.isYearCompleted = isYearCompleted;
	}
	

}