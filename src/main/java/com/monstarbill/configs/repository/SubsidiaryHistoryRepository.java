package com.monstarbill.configs.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.SubsidiaryHistory;

/**
 * Repository for the Subsidiary and it's childs history
 * @author Prashant
 * 02-Jul-2022
 */
@Repository
public interface SubsidiaryHistoryRepository extends JpaRepository<SubsidiaryHistory, String> {

	public List<SubsidiaryHistory> findBySubsidiaryIdOrderById(Long id, Pageable pageable);

}
