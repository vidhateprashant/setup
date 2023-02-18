package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.TaxRate;

@Repository
public interface TaxRateRepository extends JpaRepository<TaxRate, String> {

	public Optional<TaxRate> findById(Long id);

	public Optional<TaxRate> findByIdAndIsDeleted(Long id, boolean isDeleted);

	@Query("select t from TaxRate t where t.isDeleted = :isDeleted order by t.id desc ")
	public List<TaxRate> findAllByDeleted(@Param("isDeleted") boolean isDeleted);

	@Query("select t from TaxRate t where taxType = :taxType and taxName like :taxName% and t.isDeleted = :isDeleted order by t.id desc ")
	public List<TaxRate> findAllWithFiltersByDeleted(@Param("taxType") String taxType, @Param("taxName") String taxName, @Param("isDeleted") boolean isDeleted);

	public List<TaxRate> findBySubsidiaryIdAndIsDeleted(Long subsidiaryId, boolean isDeleted);

}
