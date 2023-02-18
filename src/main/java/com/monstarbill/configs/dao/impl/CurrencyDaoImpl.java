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
import com.monstarbill.configs.dao.CurrencyDao;
import com.monstarbill.configs.models.Currency;
import com.monstarbill.configs.payload.request.PaginationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("currencyDaoImpl")
public class CurrencyDaoImpl implements CurrencyDao {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public static final String GET_CURRENCY = "select new com.monstarbill.configs.models.Currency(c.id, c.name, c.code, c.isInactive) "
			+ " FROM Currency c " 
			+ " WHERE 1=1 ";
	
	public static final String GET_CURRENCY_COUNT = "select count(*) from Currency c WHERE 1=1 ";

	@Override
	public List<Currency> findAll(String whereClause, PaginationRequest paginationRequest) {
		List<Currency> currency = new ArrayList<Currency>();
		StringBuilder finalSql = new StringBuilder(GET_CURRENCY);
		if (StringUtils.isNotEmpty(whereClause))
			finalSql.append(whereClause.toString());
		finalSql.append(
				CommonUtils.prepareOrderByClause(paginationRequest.getSortColumn(), paginationRequest.getSortOrder()));
		log.info("Final SQL to get all currency : " + finalSql.toString());
		try {
			TypedQuery<Currency> sql = this.entityManager.createQuery(finalSql.toString(), Currency.class);
			sql.setFirstResult(paginationRequest.getPageNumber() * paginationRequest.getPageSize());
			sql.setMaxResults(paginationRequest.getPageSize());
			currency = sql.getResultList();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the list of Currency :: " + ex.toString());
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null)
				errorExceptionMessage = ex.toString();
			throw new CustomException(errorExceptionMessage);
		}
		return currency;
	}

	@Override
	public Long getCount(String whereClause) {
		Long count = 0L;

		StringBuilder finalSql = new StringBuilder(GET_CURRENCY_COUNT);
		// where clause
		if (StringUtils.isNotEmpty(whereClause))
			finalSql.append(whereClause.toString());

		log.info("Final SQL to get all currency Count w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<Long> sql = this.entityManager.createQuery(finalSql.toString(), Long.class);
			count = sql.getSingleResult();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the count of currency :: " + ex.toString());

			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null)
				errorExceptionMessage = ex.toString();

			throw new CustomException(errorExceptionMessage);
		}
		return count;
	}

}
