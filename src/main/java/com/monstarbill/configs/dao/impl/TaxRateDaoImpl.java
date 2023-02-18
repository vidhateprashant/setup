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
import com.monstarbill.configs.dao.TaxRateDao;
import com.monstarbill.configs.models.TaxRate;
import com.monstarbill.configs.payload.request.PaginationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("taxRateDaoImpl")
public class TaxRateDaoImpl implements TaxRateDao {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public static final String GET_TAX_RATE = " select t from TaxRate t WHERE 1=1 ";
	public static final String GET_TAX_RATE_COUNT = " select count(t) from TaxRate t WHERE 1=1 ";

	@Override
	public List<TaxRate> findAll(String whereClause, PaginationRequest paginationRequest) {
		List<TaxRate> taxRates = new ArrayList<TaxRate>();
		
		StringBuilder finalSql = new StringBuilder(GET_TAX_RATE);
		// where clause
		if (StringUtils.isNotEmpty(whereClause)) finalSql.append(whereClause.toString());
		// order by clause
		finalSql.append(CommonUtils.prepareOrderByClause(paginationRequest.getSortColumn(), paginationRequest.getSortOrder()));
		
		log.info("Final SQL to get all Tax Rate w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<TaxRate> sql = this.entityManager.createQuery(finalSql.toString(), TaxRate.class);
			// pagination
			sql.setFirstResult(paginationRequest.getPageNumber() * paginationRequest.getPageSize());
			sql.setMaxResults(paginationRequest.getPageSize());
			taxRates = sql.getResultList();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the list of Tax Rates :: " + ex.toString());
			
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
			
			throw new CustomException(errorExceptionMessage);
		}
		return taxRates;
	}

	@Override
	public Long getCount(String whereClause) {
		Long count = 0L;
		
		StringBuilder finalSql = new StringBuilder(GET_TAX_RATE_COUNT);
		// where clause
		if (StringUtils.isNotEmpty(whereClause)) finalSql.append(whereClause.toString());
		
		log.info("Final SQL to get all Tax Rate Count w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<Long> sql = this.entityManager.createQuery(finalSql.toString(), Long.class);
			count = sql.getSingleResult();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the count of Tax Rate :: " + ex.toString());
			
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
			
			throw new CustomException(errorExceptionMessage);
		}
		return count;
	}
}
