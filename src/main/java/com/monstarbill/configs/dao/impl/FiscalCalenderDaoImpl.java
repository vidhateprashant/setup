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
import com.monstarbill.configs.dao.FiscalCalenderDao;
import com.monstarbill.configs.models.FiscalCalender;
import com.monstarbill.configs.payload.request.PaginationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("fiscalCalenderDao")

public class FiscalCalenderDaoImpl implements FiscalCalenderDao {

	@PersistenceContext
	private EntityManager entityManager;

	public static final String GET_FISCAL_CALENDER = "select new com.monstarbill.configs.models.FiscalCalender(f.id, f.name, f.startMonth, f.endMonth, s.name as subsidiaryName, fa.yearName as yearName, f.isActive ) "
			+ " from FiscalCalender f left join Subsidiary s ON s.id = f.subsidiaryId inner join FiscalCalanderAccounting fa ON fa.fiscalId = f.id WHERE  1=1 ";

	public static final String GET_FISCAL_CALENDER_COUNT = "select count(*) from FiscalCalender f WHERE 1=1 ";

	@Override
	public List<FiscalCalender> findAll(String whereClause, PaginationRequest paginationRequest) {
		List<FiscalCalender> fiscalCalander = new ArrayList<FiscalCalender>();
		StringBuilder finalSql = new StringBuilder(GET_FISCAL_CALENDER);
		if (StringUtils.isNotEmpty(whereClause))
			finalSql.append(whereClause.toString());
		finalSql.append(" GROUP BY f.id, f.name, f.startMonth, f.endMonth, s.name, fa.yearName ");
		finalSql.append(
				CommonUtils.prepareOrderByClause(paginationRequest.getSortColumn(), paginationRequest.getSortOrder()));
		log.info("Final SQL to get all Fiscal Calender " + finalSql.toString());
		try {
			TypedQuery<FiscalCalender> sql = this.entityManager.createQuery(finalSql.toString(), FiscalCalender.class);
			sql.setFirstResult(paginationRequest.getPageNumber() * paginationRequest.getPageSize());
			sql.setMaxResults(paginationRequest.getPageSize());
			fiscalCalander = sql.getResultList();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the list of Fiscal calender :: " + ex.toString());
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null)
				errorExceptionMessage = ex.toString();
			throw new CustomException(errorExceptionMessage);
		}
		return fiscalCalander;
	}

	@Override
	public Long getCount(String whereClause) {
		Long count = 0L;

		StringBuilder finalSql = new StringBuilder(GET_FISCAL_CALENDER_COUNT);
		// where clause
		if (StringUtils.isNotEmpty(whereClause))
			finalSql.append(whereClause.toString());

		log.info("Final SQL to get all Fiscal calender Count w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<Long> sql = this.entityManager.createQuery(finalSql.toString(), Long.class);
			count = sql.getSingleResult();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the count of Fiscal calender :: " + ex.toString());

			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null)
				errorExceptionMessage = ex.toString();

			throw new CustomException(errorExceptionMessage);
		}
		return count;
	}
}
