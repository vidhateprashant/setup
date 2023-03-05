package com.monstarbill.configs.models;

import java.sql.Timestamp;
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
@Table(schema = "setup", name = "custom_roles")
@ToString
@Audited
@AuditTable("custom_roles_aud")
public class CustomRoles implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Role name is Mandatory")
	@Column(unique = true)
	private String name;
	
	@Column(name = "subsidiary_id")
	private Long subsidiaryId;
	
	@Column(name = "account_id")
	private String accountId;
	
	@Column(name = "selected_role")
	private String selectedAccess;

	@Column(name="is_active")
	private boolean isActive;
	
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
	
	@Transient
	private String subsidiaryName;

	@Transient
	private List<RolePermissions> rolePermissions;

	public CustomRoles(String accountId, String name, boolean active, String selectedAccess) {
		this.accountId = accountId;
		this.name = name;
		this.isActive = active;
		this.selectedAccess = selectedAccess;
	}
}
