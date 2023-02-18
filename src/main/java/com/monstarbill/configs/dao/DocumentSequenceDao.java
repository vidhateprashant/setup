package com.monstarbill.configs.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.configs.models.DocumentSequence;
import com.monstarbill.configs.payload.request.PaginationRequest;

@Component("documentSequenceDao")
public interface DocumentSequenceDao {

	public List<DocumentSequence> findAll(String whereClause, PaginationRequest paginationRequest);

	public Long getCount(String whereClause);

}
