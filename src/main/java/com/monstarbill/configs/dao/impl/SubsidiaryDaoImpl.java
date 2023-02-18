package com.monstarbill.configs.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.monstarbill.configs.common.CustomException;
import com.monstarbill.configs.dao.SubsidiaryDao;
import com.monstarbill.configs.models.Subsidiary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("subsidiaryDaoImpl")
public class SubsidiaryDaoImpl implements SubsidiaryDao {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public static final String GET_SUBSIDIARIES = "select new com.monstarbill.configs.models.Subsidiary(id, name, parentCompany, currency, createdDate) from Subsidiary s "
			+ " WHERE 1=1 ";
	
	public static final String GET_SUBSIDIARIES_COUNT = "select count(s) from Subsidiary s WHERE 1=1 ";

	@Override
	public List<Subsidiary> findAll(String whereClause, int pageNumber, int pageSize, String orderByClause) {
		List<Subsidiary> subsidiaries = new ArrayList<Subsidiary>();
		
		StringBuilder finalSql = new StringBuilder(GET_SUBSIDIARIES);
		if (StringUtils.isNotEmpty(whereClause)) finalSql.append(whereClause.toString());
		finalSql.append(orderByClause);
		
		log.info("Final SQL to get all Subsidiary w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<Subsidiary> sql = this.entityManager.createQuery(finalSql.toString(), Subsidiary.class);
			sql.setFirstResult(pageNumber * pageSize);
			sql.setMaxResults(pageSize);
			subsidiaries = sql.getResultList();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the list of Subsidiary :: " + ex.toString());
			
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
			
			throw new CustomException(errorExceptionMessage);
		}
		return subsidiaries;
	}

	@Override
	public Long getCount(String whereClause) {
		Long count = 0L;
		
		StringBuilder finalSql = new StringBuilder(GET_SUBSIDIARIES_COUNT);
		if (StringUtils.isNotEmpty(whereClause)) finalSql.append(whereClause.toString());
		
		log.info("Final SQL to get all Subsidiary w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<Long> sql = this.entityManager.createQuery(finalSql.toString(), Long.class);
			count = sql.getSingleResult();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the count of Subsidiary :: " + ex.toString());
			
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
			
			throw new CustomException(errorExceptionMessage);
		}
		return count;
	}

}
