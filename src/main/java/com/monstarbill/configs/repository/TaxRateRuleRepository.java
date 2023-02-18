package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.TaxRateRule;

@Repository
public interface TaxRateRuleRepository  extends JpaRepository<TaxRateRule, String> {	

	public Optional<TaxRateRule> findByIdAndIsDeleted(Long id, boolean isDeleted);
	
	public List<TaxRateRule> findByTaxGroupIdAndIsDeleted(Long taxGroupId, boolean isDeleted);
	
	@Query("select new com.monstarbill.configs.models.TaxRateRule(tr.id, tr.taxGroupId, tr.taxRateId, t.taxName as taxRateName, t.taxType as taxRateType, t.taxRates as taxRate) from TaxRateRule tr INNER JOIN TaxRate t ON t.id = tr.taxRateId WHERE tr.isDeleted = :isDeleted AND tr.taxGroupId = :taxGroupId ")
	public List<TaxRateRule> findByTaxGroupId(@Param("taxGroupId") Long taxGroupId, @Param("isDeleted") boolean isDeleted);

}
