package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.OtherPreference;

@Repository
public interface OtherPreferenceRepository extends JpaRepository<OtherPreference, String> {
	
	public List<OtherPreference> findByPreferenceIdAndIsDeleted(Long preferenceId, boolean isDeleted);
	
	public Optional<OtherPreference> findByIdAndIsDeleted(Long id, boolean isDeleted);
	
	@Query(" select op FROM OtherPreference op INNER JOIN GeneralPreference gp ON gp.id = op.preferenceId AND op.formName = 'Cross Currency Bank Payment' WHERE gp.subsidiaryId = :subsidiaryId ")
	public List<OtherPreference> findPreferenceActiveForCrossCurrenyBySubsidiary(Long subsidiaryId);

}
