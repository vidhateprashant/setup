package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.Subsidiary;

@Repository
public interface SubsidiaryRepository extends JpaRepository<Subsidiary, String> {

	@Query("select distinct name from Subsidiary where isDeleted = :isDeleted order by name asc ")
	public List<String> findDistinctSubsidiaryNames(@Param("isDeleted") boolean isDeleted);
	
	public Optional<Subsidiary> findByIdAndIsDeleted(Long id, boolean isDeleted);
	
	@Query("select new com.monstarbill.configs.models.Subsidiary(id, name, parentCompany, currency, createdDate) from Subsidiary Where isDeleted = :isDeleted order by name asc ")
	public List<Subsidiary> getAllWithFieldsAndDeleted(@Param("isDeleted") boolean isDeleted);
	
	@Query("select new com.monstarbill.configs.models.Subsidiary(id, name, parentCompany, currency, createdDate) from Subsidiary "
			+ " where CREATED_DATE > to_date(:startDate,'YYYY-MM-DD') AND CREATED_DATE < to_date(:endDate,'YYYY-MM-DD') AND isDeleted = :isDeleted "
			+ " order by name asc ")
	public List<Subsidiary> getAllWithFieldsAndDeletedFilters(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("isDeleted") boolean isDeleted);
	
	default Map<Long, String> findIdAndNameMap(boolean isDeleted) {
        return this.getAllWithFieldsAndDeleted(isDeleted).stream().collect(Collectors.toMap(Subsidiary::getId, Subsidiary::getName));
    }

	@Query("select count(id) from Subsidiary where lower(name) = lower(:name) AND isDeleted = false ")
	public Long getCountByName(@Param("name") String name);

	@Query(" SELECT id FROM Subsidiary where lower(name) = lower(:subsidiaryName) AND isDeleted = :isDeleted ")
	public Long findIdByNameAndIsDeleted(@Param("subsidiaryName") String subsidiaryName, @Param("isDeleted") boolean isDeleted);
	
	@Query(" SELECT currency FROM Subsidiary where lower(name) = lower(:subsidiaryName) AND isDeleted = :isDeleted ")
	public String findCurrencyByNameAndIsDeleted(String subsidiaryName, boolean isDeleted);
}
