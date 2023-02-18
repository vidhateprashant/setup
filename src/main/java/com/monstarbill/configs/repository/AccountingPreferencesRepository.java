package com.monstarbill.configs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.AccountingPreferences;

@Repository
public interface AccountingPreferencesRepository extends JpaRepository<AccountingPreferences, String> {
	
	public Optional<AccountingPreferences> findByPreferenceIdAndIsDeleted(Long preferencesId, boolean isDeleted);
	
	public Optional<AccountingPreferences> findByIdAndIsDeleted(Long id, boolean isDeleted);
}
