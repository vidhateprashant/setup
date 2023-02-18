package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.ApprovalRoutingPreference;

@Repository
public interface ApprovalRoutingPreferenceRepository extends JpaRepository<ApprovalRoutingPreference, String> {
	
	public List<ApprovalRoutingPreference> findByPreferenceIdAndIsDeleted(Long preferenceId, boolean isDeleted);
	
	public Optional<ApprovalRoutingPreference> findByIdAndIsDeleted(Long id, boolean isDeleted);

	@Query(" select arp "
			+ " FROM GeneralPreference gp "
			+ " LEFT JOIN ApprovalRoutingPreference arp ON gp.id = arp.preferenceId "
			+ " WHERE gp.subsidiaryId = :subsidiaryId AND arp.formName = :formName ")
	public Optional<ApprovalRoutingPreference> findIsRoutingActiveBySubsidiaryAndFormName(Long subsidiaryId, String formName);
	
	@Query(" select arp.formName "
			+ " FROM GeneralPreference gp "
			+ " LEFT JOIN ApprovalRoutingPreference arp ON gp.id = arp.preferenceId "
			+ " WHERE gp.subsidiaryId = :subsidiaryId AND arp.formType = :formType AND arp.isRoutingActive = :isRoutingActive ")
	public List<String> findRoutingByStatus(Long subsidiaryId, String formType, boolean isRoutingActive);
}
