package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.SubsidiaryAddress;

@Repository
public interface SubsidiaryAddressRepository extends JpaRepository<SubsidiaryAddress, String> {

	public List<SubsidiaryAddress> findAll();
	
	public Optional<SubsidiaryAddress> findByIdAndIsDeleted(Long id, boolean isDeleted);

	public List<SubsidiaryAddress> findBySubsidiaryIdAndIsDeleted(Long subsidiaryId, boolean isDeleted);

	public List<SubsidiaryAddress> findAllBySubsidiaryIdAndIsActive(Long id, boolean isInactive);
}
