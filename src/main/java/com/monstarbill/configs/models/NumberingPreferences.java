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
@Table(	name = "numbering_preferences")
@ToString
@Audited
@AuditTable("numbering_preferences_aud")
public class NumberingPreferences implements Cloneable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="preference_id")
	private Long preferenceId;
	
	@Column(name="master_name")
	private String masterName;
	
	@Column(name="master_prefix")
	private String masterPrefix;
	
	@Column(name="master_max_digit")
	private Long masterMaxDigit;
	
	@Column(name="master_starts_with")
	private Long masterStartsWith;
	
	@Column(name="master_current_number")
	private Long masterCurrentNumber;
	
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
	private Long subsidiaryId;
	
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
	public List<GeneralPreferenceHistory> compareFields(NumberingPreferences numberingPreferences)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(numberingPreferences);

				if (oldValue == null) {
					if (newValue != null) {
						preferencesHistories.add(this.preparePreferencesHistory(numberingPreferences, field));
					}
				} else if (!oldValue.equals(newValue)) {
					preferencesHistories.add(this.preparePreferencesHistory(numberingPreferences, field));
				}
			}
		}
		return preferencesHistories;
	}
	
	private GeneralPreferenceHistory preparePreferencesHistory(NumberingPreferences numberingPreferences, Field field) throws IllegalAccessException {
		GeneralPreferenceHistory preferencesHistory = new GeneralPreferenceHistory();
		preferencesHistory.setPreferenceId(numberingPreferences.getPreferenceId());
		preferencesHistory.setChildId(numberingPreferences.getId());
		preferencesHistory.setModuleName(AppConstants.NUMBERING_PREFERENCE);
		preferencesHistory.setChangeType(AppConstants.UI);
		preferencesHistory.setLastModifiedBy(numberingPreferences.getLastModifiedBy());
		preferencesHistory.setOperation(Operation.UPDATE.toString());
		preferencesHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) preferencesHistory.setOldValue(field.get(this).toString());
		if (field.get(numberingPreferences) != null) preferencesHistory.setNewValue(field.get(numberingPreferences).toString());
		return preferencesHistory;
	}

	public NumberingPreferences(Long id, Long preferenceId, Long subsidiaryId, String masterName, String masterPrefix, Long masterMaxDigit, Long masterStartsWith, Long masterCurrentNumber) {
		this.id = id;
		this.preferenceId = preferenceId;
		this.subsidiaryId = subsidiaryId;
		this.masterName = masterName;
		this.masterPrefix = masterPrefix;
		this.masterMaxDigit = masterMaxDigit;
		this.masterStartsWith = masterStartsWith;
		this.masterCurrentNumber = masterCurrentNumber;
	}
}
