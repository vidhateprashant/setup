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
@Table(	name = "currency")
@ToString
@Audited
@AuditTable("currency_aud")
public class Currency implements Cloneable{
    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "name  is mandatory")
	@Column(name="name", nullable = false)
	private String name;
	
	@Column(name="code")
	private String code;

	@Column(name="is_inactive", columnDefinition = "boolean default false")
	private boolean isInactive;
	
	@Column(name="active_date")
	private Date activeDate;
	
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
	
	public Currency(Long id, String name, String code, Date createdDate, boolean isInactive ) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.createdDate = createdDate;
		this.isInactive = isInactive;
	}
	
	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param currency
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<CurrencyHistory> compareFields(Currency currency)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<CurrencyHistory> currencyHistories = new ArrayList<CurrencyHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(currency);

				if (oldValue == null) {
					if (newValue != null) {
						currencyHistories.add(this.prepareCurrencyHistory(currency, field));
					}
				} else if (!oldValue.equals(newValue)) {
					currencyHistories.add(this.prepareCurrencyHistory(currency, field));
				}
			}
		}
		return currencyHistories;
	}

	private CurrencyHistory prepareCurrencyHistory(Currency currency, Field field) throws IllegalAccessException {
		CurrencyHistory currencyHistory = new CurrencyHistory();
		currencyHistory.setCurrencyId(currency.getId());
		currencyHistory.setModuleName(AppConstants.CURRENCY);
		currencyHistory.setChangeType(AppConstants.UI);
		currencyHistory.setOperation(Operation.UPDATE.toString());
		currencyHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		currencyHistory.setOldValue(field.get(this).toString());
		currencyHistory.setNewValue(field.get(currency).toString());
		currencyHistory.setLastModifiedBy(currency.getLastModifiedBy());
		return currencyHistory;
	}

	public Currency(Long id, String name, String code, boolean isInactive) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.isInactive = isInactive;
	}
}
