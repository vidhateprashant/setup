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
@Table(	name = "costing_preferences")
@ToString
@Audited
@AuditTable("costing_preferences_aud")
public class CostingPreferences implements Cloneable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="preference_id")
	private Long preferenceId;
	
	@Column(name="costing_method")
	private String costingMethod;
	
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
	public List<GeneralPreferenceHistory> compareFields(CostingPreferences costingPreferences)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(costingPreferences);

				if (oldValue == null) {
					if (newValue != null) {
						preferencesHistories.add(this.preparePreferencesHistory(costingPreferences, field));
					}
				} else if (!oldValue.equals(newValue)) {
					preferencesHistories.add(this.preparePreferencesHistory(costingPreferences, field));
				}
			}
		}
		return preferencesHistories;
	}
	
	private GeneralPreferenceHistory preparePreferencesHistory(CostingPreferences costingPreferences, Field field) throws IllegalAccessException {
		GeneralPreferenceHistory preferencesHistory = new GeneralPreferenceHistory();
		preferencesHistory.setPreferenceId(costingPreferences.getPreferenceId());
		preferencesHistory.setChildId(costingPreferences.getId());
		preferencesHistory.setModuleName(AppConstants.COSTING_PREFERENCE);
		preferencesHistory.setChangeType(AppConstants.UI);
		preferencesHistory.setLastModifiedBy(costingPreferences.getLastModifiedBy());
		preferencesHistory.setOperation(Operation.UPDATE.toString());
		preferencesHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) preferencesHistory.setOldValue(field.get(this).toString());
		if (field.get(costingPreferences) != null) preferencesHistory.setNewValue(field.get(costingPreferences).toString());
		return preferencesHistory;
	}
}
