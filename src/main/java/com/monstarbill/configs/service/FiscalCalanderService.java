package com.monstarbill.configs.service;

import java.text.ParseException;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.monstarbill.configs.models.FiscalCalanderAccounting;
import com.monstarbill.configs.models.FiscalCalender;
import com.monstarbill.configs.models.FiscalCalenderHistory;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;

public interface FiscalCalanderService {
	
	public FiscalCalender save(FiscalCalender fiscalCalender) throws ParseException;

	public FiscalCalender getFiscalCalanderById(Long id);
	
	public PaginationResponse findAll(PaginationRequest paginationRequest);

	public  List<FiscalCalanderAccounting> getFiscalCalanderAccounting(int startYear, int endYear, String startMonth, String endMonth)throws ParseException;

	public List<FiscalCalanderAccounting> findAccountingByFiscalId(Long fiscalId);

	public boolean deleteById(Long id);

	public List<FiscalCalanderAccounting> findAllAccounting();

	public List<FiscalCalenderHistory> findHistoryById(Long id, Pageable pageable);

	public FiscalCalender getFiscalCalanderBySubsidiaryId(Long subsidiaryId);


}
