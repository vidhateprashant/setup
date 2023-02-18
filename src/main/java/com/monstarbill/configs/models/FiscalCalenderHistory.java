package com.monstarbill.configs.models;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(	name = "fiscal_calender_history")
@ToString
public class FiscalCalenderHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long fiscalCalenderId;
	
	private Long childId;
	
	@Column(name = "module_name")
	private String moduleName;
	
	private String operation;
	
	@Column(name = "field_name")
	private String fieldName;
	
	@Column(name = "change_type")
	private String changeType;

	@Column(name = "new_value")
	private String newValue;

	@Column(name = "old_value")
	private String oldValue;

	@Column(name = "last_modified_by")
	private String lastModifiedBy;

	@UpdateTimestamp
	@Column(name = "last_modified_date", updatable = false)
	private Timestamp lastModifiedDate;
	
	@CreationTimestamp
	@Column(name="created_date", updatable = false)
	private Date createdDate;

}