package com.monstarbill.configs.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.configs.models.TaxGroup;
import com.monstarbill.configs.payload.request.PaginationRequest;

@Component("taxGroupDao")
public interface TaxGroupDao {
	
	public List<TaxGroup> findAll(String whereClause, PaginationRequest paginationRequest);

	public Long getCount(String whereClause);

}
