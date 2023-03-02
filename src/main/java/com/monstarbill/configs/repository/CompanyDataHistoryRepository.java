package com.monstarbill.configs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.configs.models.CompanyDataHistory;

public interface CompanyDataHistoryRepository extends JpaRepository<CompanyDataHistory, Long> {

	public List<CompanyDataHistory> findByCompanyDataId(Long id);
}
