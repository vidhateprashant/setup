package com.monstarbill.configs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.configs.models.CompanyData;

public interface CompanyDataRepository extends JpaRepository<CompanyData, Long> {

	public Optional<CompanyData> findByIdAndIsDeleted(Long id, boolean IsDeleted);

}
