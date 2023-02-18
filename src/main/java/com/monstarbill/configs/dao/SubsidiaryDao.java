package com.monstarbill.configs.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.configs.models.Subsidiary;

@Component("subsidiaryDao")
public interface SubsidiaryDao {
	
	public List<Subsidiary> findAll(String whereClause, int pageNumber, int pageSize, String orderByClause);

	public Long getCount(String whereClause);
}
