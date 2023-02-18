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
@Table(	name = "accounting_preferences")
@ToString
@Audited
@AuditTable("accounting_preferences_aud")
public class AccountingPreferences implements Cloneable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="preference_id")
	private Long preferenceId;
	
	@Column(name="purchase_discount_account")
	private Long purchaseDiscountAccount;

	@Column(name="default_expense_account")
	private Long defaultExpenseAccount;

	@Column(name="default_inventory_account")
	private Long defaultInventoryAccount;

	@Column(name="default_prepayment_account")
	private Long defaultPrepaymentAccount;

	@Column(name="default_payable_account")
	private Long defaultPayableAccount;

	@Column(name="grn_accrual_account")
	private Long grnAccrualAccount;

	@Column(name="default_vendor_return_account")
	private Long defaultVendorReturnAccount;

	@Column(name="default_exchange_rate_varience_account")
	private Long defaultExchangeRateVarienceAccount;

	@Column(name="default_cost_of_goods_sold_account")
	private Long defaultCostofGoodsSoldAccount;

	@Column(name="default_income_account")
	private Long defaultIncomeAccount;
	
	@Column(name="is_active")
	private boolean isActive;
	
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
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param supplier
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<GeneralPreferenceHistory> compareFields(AccountingPreferences accountingPreferences)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(accountingPreferences);

				if (oldValue == null) {
					if (newValue != null) {
						preferencesHistories.add(this.preparePreferencesHistory(accountingPreferences, field));
					}
				} else if (!oldValue.equals(newValue)) {
					preferencesHistories.add(this.preparePreferencesHistory(accountingPreferences, field));
				}
			}
		}
		return preferencesHistories;
	}
	
	private GeneralPreferenceHistory preparePreferencesHistory(AccountingPreferences accountingPreferences, Field field) throws IllegalAccessException {
		GeneralPreferenceHistory preferencesHistory = new GeneralPreferenceHistory();
		preferencesHistory.setPreferenceId(accountingPreferences.getPreferenceId());
		preferencesHistory.setChildId(accountingPreferences.getId());
		preferencesHistory.setModuleName(AppConstants.ACCOUNTING_PREFERENCE);
		preferencesHistory.setChangeType(AppConstants.UI);
		preferencesHistory.setLastModifiedBy(accountingPreferences.getLastModifiedBy());
		preferencesHistory.setOperation(Operation.UPDATE.toString());
		preferencesHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) preferencesHistory.setOldValue(field.get(this).toString());
		if (field.get(accountingPreferences) != null) preferencesHistory.setNewValue(field.get(accountingPreferences).toString());
		return preferencesHistory;
	}

}
