package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.configs.models.TaxGroup;

public interface TaxGroupRepository extends JpaRepository<TaxGroup, String>{
	
	public Optional<TaxGroup> findByIdAndIsDeleted(Long id, boolean isDeleted);

	public List<TaxGroup> findBySubsidiaryId(Long subsidiaryId);

	public Optional<TaxGroup> findByNameAndSubsidiaryIdAndIsDeleted(String name, Long id, boolean isDeleted);
	
	public Optional<TaxGroup> findByNameAndIsDeleted(String name, boolean isDeleted);
}
