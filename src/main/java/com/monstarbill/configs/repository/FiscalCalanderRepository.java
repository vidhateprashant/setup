package com.monstarbill.configs.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.configs.models.FiscalCalender;

public interface FiscalCalanderRepository extends JpaRepository<FiscalCalender, String>{
	
	public Optional<FiscalCalender> findByIdAndIsDeleted(Long id, boolean isDeleted);

}
