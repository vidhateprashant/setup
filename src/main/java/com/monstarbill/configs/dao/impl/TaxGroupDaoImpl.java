package com.monstarbill.configs.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.monstarbill.configs.common.CommonUtils;
import com.monstarbill.configs.common.CustomException;
import com.monstarbill.configs.dao.TaxGroupDao;
import com.monstarbill.configs.models.TaxGroup;
import com.monstarbill.configs.payload.request.PaginationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("taxGroupDaoImpl")
public class TaxGroupDaoImpl implements TaxGroupDao{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public static final String GET_TAX_GROUP = "select new com.monstarbill.configs.models.TaxGroup(t.id, t.subsidiaryId, t.country, t.name, t.description, s.name as subsidiaryName, t.isActive) "
			+ " from TaxGroup t INNER JOIN Subsidiary s ON s.id = t.subsidiaryId WHERE 1=1 ";
	
	public static final String GET_TAX_GROUP_COUNT = "select count(1) "
			+ " from TaxGroup t inner join Subsidiary s ON s.id = t.subsidiaryId WHERE 1=1 ";

	@Override
	public List<TaxGroup> findAll(String whereClause, PaginationRequest paginationRequest) {
		List<TaxGroup> taxGroup = new ArrayList<TaxGroup>();
		
		StringBuilder finalSql = new StringBuilder(GET_TAX_GROUP);
		// where clause
		if (StringUtils.isNotEmpty(whereClause)) finalSql.append(whereClause.toString());
		// order by clause
		finalSql.append(CommonUtils.prepareOrderByClause(paginationRequest.getSortColumn(), paginationRequest.getSortOrder()));
		
		log.info("Final SQL to get all Tax Group w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<TaxGroup> sql = this.entityManager.createQuery(finalSql.toString(), TaxGroup.class);
			// pagination
			sql.setFirstResult(paginationRequest.getPageNumber() * paginationRequest.getPageSize());
			sql.setMaxResults(paginationRequest.getPageSize());
			taxGroup = sql.getResultList();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the list of Tax Group :: " + ex.toString());
			
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
			
			throw new CustomException(errorExceptionMessage);
		}
		return taxGroup;
	}
	
	@Override
	public Long getCount(String whereClause) {
		Long count = 0L;
		
		StringBuilder finalSql = new StringBuilder(GET_TAX_GROUP_COUNT);
		// where clause
		if (StringUtils.isNotEmpty(whereClause)) finalSql.append(whereClause.toString());
		
		log.info("Final SQL to get all Tax group Count w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<Long> sql = this.entityManager.createQuery(finalSql.toString(), Long.class);
			count = sql.getSingleResult();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the count of Tax group :: " + ex.toString());
			
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
			
			throw new CustomException(errorExceptionMessage);
		}
		return count;
	}

}
