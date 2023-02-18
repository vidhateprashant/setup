package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.monstarbill.configs.models.FiscalCalanderAccounting;

public interface FiscalCalanderAccountingRepository extends JpaRepository<FiscalCalanderAccounting, String> {

	public Optional<FiscalCalanderAccounting> findById(Long id);

	public List<FiscalCalanderAccounting> findByFiscalId(Long id);
	
	@Query(" select new com.monstarbill.configs.models.FiscalCalanderAccounting(id, yearName, fiscalId, startYear ,endYear ,month , fromDate, toDate, isLock, isPeriodOpen, isPeriodClose, isYearCompleted) from FiscalCalanderAccounting ORDER BY id asc ")
	public List<FiscalCalanderAccounting> findAllAccounting();

}
