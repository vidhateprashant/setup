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
@Table(	name = "general_preference")
@ToString
@Audited
@AuditTable("general_preference_aud")
public class GeneralPreference implements Cloneable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message = "Subsidiary Id is mandatory")
	@Column(name="subsidiary_id", unique = true)
	private Long subsidiaryId;
	
	@Column(name="is_active")
	private boolean isActive;
	
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
	
	@Transient
	private AccountingPreferences accountingPreferences;
	
	@Transient
	private CostingPreferences costingPreferences;
	
	@Transient
	private List<NumberingPreferences> numberingPreferences;
	
	@Transient
	private List<ApprovalRoutingPreference> approvalRoutingPreferences;
	
	@Transient
	private List<OtherPreference> otherPreferences;
	
	@Transient
	private String subsidiaryName;
	
	@Transient
	private String costingMethod;
	
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
	public List<GeneralPreferenceHistory> compareFields(GeneralPreference preferences)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<GeneralPreferenceHistory> preferencesHistories = new ArrayList<GeneralPreferenceHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(preferences);

				if (oldValue == null) {
					if (newValue != null) {
						preferencesHistories.add(this.preparePreferencesHistory(preferences, field));
					}
				} else if (!oldValue.equals(newValue)) {
					preferencesHistories.add(this.preparePreferencesHistory(preferences, field));
				}
			}
		}
		return preferencesHistories;
	}
	
	private GeneralPreferenceHistory preparePreferencesHistory(GeneralPreference generalPreference, Field field) throws IllegalAccessException {
		GeneralPreferenceHistory generalPreferenceHistory = new GeneralPreferenceHistory();
		generalPreferenceHistory.setPreferenceId(generalPreference.getId());
		generalPreferenceHistory.setModuleName(AppConstants.PREFERENCE);
		generalPreferenceHistory.setChangeType(AppConstants.UI);
		generalPreferenceHistory.setLastModifiedBy(generalPreference.getLastModifiedBy());
		generalPreferenceHistory.setOperation(Operation.UPDATE.toString());
		generalPreferenceHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) generalPreferenceHistory.setOldValue(field.get(this).toString());
		if (field.get(generalPreference) != null) generalPreferenceHistory.setNewValue(field.get(generalPreference).toString());
		return generalPreferenceHistory;
	}
	
	public GeneralPreference(Long id, Long subsidiaryId, String name, String costingMethod) {
		this.id = id;
		this.subsidiaryId = subsidiaryId;
		this.subsidiaryName = name;
		this.costingMethod = costingMethod;
	}
}
