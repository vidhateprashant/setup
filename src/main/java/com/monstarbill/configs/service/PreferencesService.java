package com.monstarbill.configs.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.monstarbill.configs.models.GeneralPreference;
import com.monstarbill.configs.models.GeneralPreferenceHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;

public interface PreferencesService {
	
	public GeneralPreference save(GeneralPreference preferences);

	public GeneralPreference getPreferencesById(Long id);
	
	public List<GeneralPreferenceHistory> findAuditById(Long id, Pageable pageable);
	
	public PaginationResponse findAll(PaginationRequest paginationRequest);

	public String findPreferenceNumberByMaster(Long subsidiaryId, String masterName);

	public List<String> findRoutingByStatus(Long subsidiaryId, String formType, String status);

}
