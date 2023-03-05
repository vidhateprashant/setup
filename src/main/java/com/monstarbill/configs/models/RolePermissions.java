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
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

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
@Table(schema = "setup", name = "role_permissions")
@ToString
@Audited
@AuditTable("role_permissions_aud")
public class RolePermissions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "role_id")
	private Long roleId;
	
	@Column(name = "module_name")
	private String moduleName;
	
	@Column(name = "access_point")
	private String accessPoint;
	
	@Column(name="is_view", columnDefinition = "boolean default false")
	private boolean isView;
	
	@Column(name="is_create", columnDefinition = "boolean default false")
	private boolean isCreate;
	
	@Column(name="is_edit", columnDefinition = "boolean default false")
	private boolean isEdit;

	@Column(name="is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;
	
	@CreationTimestamp
	@Column(name="created_date", updatable = false)
	private Date createdDate;

	@CreatedBy
	@Column(name="created_by", updatable = false)
	private String createdBy;

	@UpdateTimestamp
	@Column(name="last_modified_date")
	private Timestamp lastModifiedDate;

	@LastModifiedBy
	@Column(name="last_modified_by")
	private String lastModifiedBy;

	public RolePermissions(String moduleName, String accessPoint, boolean create, boolean edit, boolean view) {
		this.moduleName = moduleName;
		this.accessPoint = accessPoint;
		this.isCreate = create;
		this.isEdit = edit;
		this.isView = view;
	}
	
}