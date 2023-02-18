package com.monstarbill.configs.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.configs.models.FiscalCalender;
import com.monstarbill.configs.payload.request.PaginationRequest;

@Component("fiscalCalenderDao")
public interface FiscalCalenderDao {
	
	public List<FiscalCalender> findAll(String whereClause,  PaginationRequest paginationRequest);

	public Long getCount(String whereClause);

}




