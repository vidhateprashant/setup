package com.monstarbill.configs.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.TaxGroupHistory;

/**
 * Repository for the TaxGroup and it's childs history
 * @author Ajay
 * 20-Jul-2022
 */
@Repository
public interface TaxGroupHistoryRepository extends JpaRepository<TaxGroupHistory, String> {

	public List<TaxGroupHistory> findByTaxGroupId(Long id, Pageable pageable);



}
