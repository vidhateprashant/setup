package com.monstarbill.configs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.configs.models.GeneralPreference;

public interface GeneralPreferencesRepository extends JpaRepository<GeneralPreference, String> {
	
	public Optional<GeneralPreference> findByIdAndIsDeleted(Long id, boolean isDeleted);

}
