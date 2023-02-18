package com.monstarbill.configs.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.configs.models.GeneralPreference;
import com.monstarbill.configs.payload.request.PaginationRequest;

@Component("preferencesDao")
public interface PreferencesDao {
	
	public List<GeneralPreference> findAll(String whereClause, PaginationRequest paginationRequest);

	public Long getCount(String whereClause);

}
