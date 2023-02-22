package com.monstarbill.configs.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monstarbill.configs.common.AppConstants;
import com.monstarbill.configs.common.CommonUtils;
import com.monstarbill.configs.common.CustomException;
import com.monstarbill.configs.common.CustomMessageException;
import com.monstarbill.configs.dao.FiscalCalenderDao;
import com.monstarbill.configs.enums.Operation;
import com.monstarbill.configs.models.FiscalCalanderAccounting;
import com.monstarbill.configs.models.FiscalCalender;
import com.monstarbill.configs.models.FiscalCalenderHistory;
import com.monstarbill.configs.models.Subsidiary;
import com.monstarbill.configs.payload.request.PaginationRequest;
import com.monstarbill.configs.payload.response.PaginationResponse;
import com.monstarbill.configs.repository.FiscalCalanderAccountingRepository;
import com.monstarbill.configs.repository.FiscalCalanderRepository;
import com.monstarbill.configs.repository.FiscalCalenderHistoryRepository;
import com.monstarbill.configs.repository.SubsidiaryRepository;
import com.monstarbill.configs.service.FiscalCalanderService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class FiscalCalanderServiceImpl implements FiscalCalanderService {

	@Autowired
	private FiscalCalanderRepository fiscalCalanderRepository;

	@Autowired
	private FiscalCalenderDao fiscalCalenderDao;
	
	@Autowired
	private SubsidiaryRepository subsidiaryRepository;

	@Autowired
	private FiscalCalanderAccountingRepository fiscalCalanderAccountingRepository;

	@Autowired
	private FiscalCalenderHistoryRepository fiscalCalenderHistoryRepository;

	@Override
	public FiscalCalender save(FiscalCalender fiscalCalender) throws ParseException {

		Long fiscalCalanderId = null;
		Optional<FiscalCalender> oldFiscalCalander = Optional.empty();

		if (fiscalCalender.getId() == null) {
			fiscalCalender.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldFiscalCalander = this.fiscalCalanderRepository.findByIdAndIsDeleted(fiscalCalender.getId(), false);
			if (oldFiscalCalander.isPresent()) {
				try {
					oldFiscalCalander = Optional.ofNullable((FiscalCalender) oldFiscalCalander.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		fiscalCalender.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		if (fiscalCalender.isActive() == true) {
			fiscalCalender.setActiveDate(null);
		}
		FiscalCalender savedFiscalCalander;
		try {
			savedFiscalCalander = this.fiscalCalanderRepository.save(fiscalCalender);
		} catch (DataIntegrityViolationException e) {
			log.error("Fiscal calendar unique constrain violetd." + e.getMostSpecificCause());
			throw new CustomException("Fiscal calendar unique constrain violetd " + e.getMostSpecificCause());
		}
		log.info("Fiscal Calender is updated successfully" + savedFiscalCalander);
		fiscalCalanderId = savedFiscalCalander.getId();
		log.info("reatriving the ID of saved Fiscal calender" + fiscalCalanderId);
		updateFiscalCalanderHistory(oldFiscalCalander, savedFiscalCalander);

//--------------------saving the fiscal calender accounting-------------------------------
		List<FiscalCalanderAccounting> fiscalCalanderAccountings = fiscalCalender.getFiscalCalanderAccounting();
		if (CollectionUtils.isNotEmpty(fiscalCalanderAccountings)) {
			for (FiscalCalanderAccounting fiscalCalanderAccounting : fiscalCalanderAccountings) {
				this.saveAccounting(fiscalCalanderId, fiscalCalanderAccounting);
			}
		}
		System.gc();
		savedFiscalCalander = this.getFiscalCalenderById(savedFiscalCalander.getId());
		return fiscalCalender;
	}

	private FiscalCalender getFiscalCalenderById(Long id) {
		Optional<FiscalCalender> fiscalCalender = Optional.empty();
		fiscalCalender = fiscalCalanderRepository.findByIdAndIsDeleted(id, false);

		if (fiscalCalender.isPresent()) {
			Long fiscalId = fiscalCalender.get().getId();
			log.info("Fiscal calender accounting by fiscal calender id is  found " + id);
			// Get accounting
			List<FiscalCalanderAccounting> fiscalCalanderAccounting = fiscalCalanderAccountingRepository
					.findByFiscalId(fiscalId);
			if (CollectionUtils.isNotEmpty(fiscalCalanderAccounting)) {
				fiscalCalender.get().setFiscalCalanderAccounting(fiscalCalanderAccounting);
			}
		} else {
			log.error("Fiscal calender accounting by fiscal calender id is not found " + id);
			throw new CustomMessageException("Fiscal calender accounting by fiscal calender id is not found " + id);
		}
		return fiscalCalender.get();
	}

	// ------- updating the history --------------------------------------------
	private Long updateFiscalCalanderHistory(Optional<FiscalCalender> oldFiscalCalander,
			FiscalCalender fiscalCalender) {
		Long fiscalCalanderId = null;
		if (fiscalCalender != null) {
			fiscalCalanderId = fiscalCalender.getId();

			if (oldFiscalCalander.isPresent()) {
				// insert the updated fields in history table
				List<FiscalCalenderHistory> supplierHistories = new ArrayList<FiscalCalenderHistory>();
				try {
					supplierHistories = oldFiscalCalander.get().compareFields(fiscalCalender);
					if (CollectionUtils.isNotEmpty(supplierHistories)) {
						this.fiscalCalenderHistoryRepository.saveAll(supplierHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException(
							"Error while comparing the new and old objects. Please contact administrator.");
				}
				log.info("Fiscal Calender History is updated successfully");
			} else {
				// Insert in history table as Operation - INSERT
				this.fiscalCalenderHistoryRepository
						.save(this.prepareFiscalCalanderHistory(fiscalCalanderId, null, AppConstants.FISCAL_CALENDER,
								Operation.CREATE.toString(), fiscalCalender.getLastModifiedBy(), null, null));
			}
			log.info("Fiscal Calender Saved successfully.");
		} else {
			log.error("Error while saving the Fiscal Calender.");
			throw new CustomException("Error while saving the Fiscal Calender.");
		}
		return fiscalCalanderId;
	}

	public FiscalCalenderHistory prepareFiscalCalanderHistory(Long fiscalCalanderId, Long childId, String moduleName,
			String operation, String lastModifiedBy, String oldValue, String newValue) {
		FiscalCalenderHistory fiscalCalanderHistory = new FiscalCalenderHistory();
		fiscalCalanderHistory.setFiscalCalenderId(fiscalCalanderId);
		fiscalCalanderHistory.setChildId(childId);
		fiscalCalanderHistory.setModuleName(moduleName);
		fiscalCalanderHistory.setChangeType(AppConstants.UI);
		fiscalCalanderHistory.setOperation(operation);
		fiscalCalanderHistory.setOldValue(oldValue);
		fiscalCalanderHistory.setNewValue(newValue);
		fiscalCalanderHistory.setLastModifiedBy(lastModifiedBy);
		return fiscalCalanderHistory;
	}

	// -------------------------------------------save fiscal calendar
	// accounting---------------------------------------//

	private FiscalCalanderAccounting saveAccounting(Long fiscalCalanderId,
			FiscalCalanderAccounting fiscalCalanderAccountings) throws ParseException {
		Optional<FiscalCalanderAccounting> oldFiscalCalanderAccounting = Optional.empty();
		if (fiscalCalanderAccountings.getId() == null) {
			fiscalCalanderAccountings.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get existing address using deep copy
			oldFiscalCalanderAccounting = this.fiscalCalanderAccountingRepository
					.findById(fiscalCalanderAccountings.getId());
			if (oldFiscalCalanderAccounting.isPresent()) {
				try {
					oldFiscalCalanderAccounting = Optional
							.ofNullable((FiscalCalanderAccounting) oldFiscalCalanderAccounting.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		fiscalCalanderAccountings.setFiscalId(fiscalCalanderId);
		fiscalCalanderAccountings.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		fiscalCalanderAccountings = this.fiscalCalanderAccountingRepository.save(fiscalCalanderAccountings);
		if (fiscalCalanderAccountings == null) {
			log.info("Error while Saving the Accounting list in Fiscal Calender.");
			throw new CustomMessageException("Error while Saving the Accounting list in Fiscal Calender.");
		}
		// find the updated values and save to history table
		if (oldFiscalCalanderAccounting.isPresent()) {
			if (fiscalCalanderAccountings.isDeleted()) {
				this.fiscalCalenderHistoryRepository
						.save(this.prepareFiscalCalanderHistory(fiscalCalanderAccountings.getFiscalId(),
								fiscalCalanderAccountings.getId(), AppConstants.FISCAL_CALENDER_ACCOUNTING,
								Operation.DELETE.toString(), fiscalCalanderAccountings.getLastModifiedBy(),
								String.valueOf(fiscalCalanderAccountings.getId()), null));
			} else {
				List<FiscalCalenderHistory> FiscalCalanderAccountingHistories = new ArrayList<FiscalCalenderHistory>();
				try {
					FiscalCalanderAccountingHistories = oldFiscalCalanderAccounting.get()
							.compareFields(fiscalCalanderAccountings);
					if (CollectionUtils.isNotEmpty(FiscalCalanderAccountingHistories)) {
						this.fiscalCalenderHistoryRepository.saveAll(FiscalCalanderAccountingHistories);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error while comparing the new and old objects. Please contact administrator.");
					throw new CustomException(
							"Error while comparing the new and old objects. Please contact administrator.");
				}
			}
			log.info("Fiscal calender History is updated successfully for ");
		} else {
			this.fiscalCalenderHistoryRepository
					.save(this.prepareFiscalCalanderHistory(fiscalCalanderAccountings.getFiscalId(),
							fiscalCalanderAccountings.getId(), AppConstants.FISCAL_CALENDER_ACCOUNTING,
							Operation.CREATE.toString(), fiscalCalanderAccountings.getLastModifiedBy(), null,
							String.valueOf(fiscalCalanderAccountings.getId())));
		}
		return fiscalCalanderAccountings;
	}

//-----------------get by id----------------
	@Override
	public FiscalCalender getFiscalCalanderById(Long id) {
		Optional<FiscalCalender> fiscalCalander = Optional.empty();
		fiscalCalander = fiscalCalanderRepository.findByIdAndIsDeleted(id, false);
		if (fiscalCalander.isPresent()) {
			Long fiscalId = fiscalCalander.get().getId();
			log.info("Fiscal calender found against given id : " + id);
			// Get accounting
			List<FiscalCalanderAccounting> fiscalCalanderAccounting = fiscalCalanderAccountingRepository
					.findByFiscalId(fiscalId);
			if (CollectionUtils.isNotEmpty(fiscalCalanderAccounting)) {
				fiscalCalander.get().setFiscalCalanderAccounting(fiscalCalanderAccounting);
			}
		} else {
			log.error("Fiscal Calender Not Found against given fiscal id : " + id);
			throw new CustomMessageException("Fiscal Calender Not Found against given fiscal id : " + id);
		}

		return fiscalCalander.get();
	}

	@Override
	public PaginationResponse findAll(PaginationRequest paginationRequest) {
		List<FiscalCalender> FiscalCalander = new ArrayList<FiscalCalender>();

		// preparing where clause
		String whereClause = this.prepareWhereClause();

		// get list
		FiscalCalander = this.fiscalCalenderDao.findAll(whereClause, paginationRequest);

		// getting count
		Long totalRecords = this.fiscalCalenderDao.getCount(whereClause);

		return CommonUtils.setPaginationResponse(paginationRequest.getPageNumber(), paginationRequest.getPageSize(),
				FiscalCalander, totalRecords);

	}

	private String prepareWhereClause() {
		StringBuilder whereClause = new StringBuilder(" AND f.isDeleted is false ");
		return whereClause.toString();
	}

	// ----------------API for generating the list-----------
	@Override
	public List<FiscalCalanderAccounting> getFiscalCalanderAccounting(int startYear, int endYear, String startMonth,
			String endMonth) throws ParseException {
		List<FiscalCalanderAccounting> fiscalCalanderAccountingListWithFilter = new ArrayList<FiscalCalanderAccounting>();

		StringBuilder startDateValue = new StringBuilder();
		startDateValue.append(startMonth).append("-").append(startYear);
		log.info("start date for fiscal calender accounting " + startDateValue.toString());

		StringBuilder endDateValue = new StringBuilder();
		endDateValue.append(endMonth).append("-").append(endYear);
		log.info("end date for fiscal calender accounting" + endDateValue.toString());

		DateFormat formater = new SimpleDateFormat("MMMM-yyyy");
		Calendar beginCalendar = Calendar.getInstance();
		Calendar finishCalendar = Calendar.getInstance();

		try {
			beginCalendar.setTime(formater.parse(startDateValue.toString()));
			finishCalendar.setTime(formater.parse(endDateValue.toString()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int i = 1;

		while (beginCalendar.before(finishCalendar)) {
			// add one month to date per loop

			String month = formater.format(beginCalendar.getTime()).toUpperCase();
			log.info(" Printing the month with year " + month);
			String[] MonthAndYear = month.split("-");
			log.info(" Printing the year " + MonthAndYear[1] + " And Printing the month " + MonthAndYear[0]);

			int MonthNumber = getMonthNumberByName(MonthAndYear[0]);
			int year = Integer.parseInt(MonthAndYear[1]);
			log.info(" Printing the Month Number " + MonthNumber + " And Printing the Year " + year);

			YearMonth yearMonth = YearMonth.of(year, MonthNumber);
			LocalDate fromDate = yearMonth.atDay(1);
			LocalDate toDate = yearMonth.atEndOfMonth();
			log.info("Printing the date range " + fromDate + "---" + toDate);

			beginCalendar.add(Calendar.MONTH, 1);

			if (i == 1) {
				FiscalCalanderAccounting fiscalCalanderAccounting = new FiscalCalanderAccounting(month, fromDate,
						toDate, false, true, startYear, endYear, false);
				fiscalCalanderAccountingListWithFilter.add(fiscalCalanderAccounting);
			} else {
				FiscalCalanderAccounting fiscalCalanderAccounting = new FiscalCalanderAccounting(month, fromDate,
						toDate, true, false, startYear, endYear, false);
				fiscalCalanderAccountingListWithFilter.add(fiscalCalanderAccounting);
			}
			i++;
		}
		log.info("printing the object for the Fiscal Calender Accounting" + fiscalCalanderAccountingListWithFilter);
		return fiscalCalanderAccountingListWithFilter;

	}

	public static int getMonthNumberByName(String monthName) throws ParseException {
		Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(monthName);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int monthNumber = cal.get(Calendar.MONTH) + 1;
		log.info(" Printing the Month Number " + monthNumber);
		return monthNumber;
	}

	@Override
	public List<FiscalCalanderAccounting> findAccountingByFiscalId(Long fiscalId) {
		List<FiscalCalanderAccounting> fiscalCalanderAccountings = new ArrayList<FiscalCalanderAccounting>();
		for (FiscalCalanderAccounting fiscalCalanderAccounting : fiscalCalanderAccountings) {
			if (!fiscalCalanderAccounting.isYearCompleted()) {
				fiscalCalanderAccountings = this.fiscalCalanderAccountingRepository.findByFiscalId(fiscalId);
			} else {
				log.info("Fiscal Calender is not complete");
			}
		}
		return fiscalCalanderAccountings;
	}

	@Override
	public boolean deleteById(Long id) {
		FiscalCalender fiscalCalender = new FiscalCalender();
		fiscalCalender = this.getFiscalCalanderById(id);
		fiscalCalender.setDeleted(true);
		fiscalCalender = this.fiscalCalanderRepository.save(fiscalCalender);
		if (fiscalCalender == null) {
			log.error("Error while deleting the location : " + id);
			throw new CustomMessageException("Error while deleting the location : " + id);
		}
		// update the operation in the history
		this.fiscalCalenderHistoryRepository.save(this.prepareFiscalCalanderHistory(fiscalCalender.getId(), null,
				AppConstants.FISCAL_CALENDER, Operation.DELETE.toString(), fiscalCalender.getLastModifiedBy(),
				String.valueOf(fiscalCalender.getId()), null));
		return true;
	}

	@Override
	public List<FiscalCalanderAccounting> findAllAccounting() {
		return this.fiscalCalanderAccountingRepository.findAllAccounting();
	}

	@Override
	public List<FiscalCalenderHistory> findHistoryById(Long id, Pageable pageable) {
		return this.fiscalCalenderHistoryRepository.findByFiscalCalenderId(id, pageable);
	}

	@Override
	public FiscalCalender getFiscalCalanderBySubsidiaryId(Long subsidiaryId) {
		FiscalCalender fiscalCalender = new FiscalCalender();
		Subsidiary fiscalCalenderBySubsidiary = this.subsidiaryRepository.findFiscalCalenderById(subsidiaryId);
		if(fiscalCalenderBySubsidiary==null){
			log.error(" There is no fiscal calneder by this subsidiary : " );
			throw new CustomMessageException(" There is no fiscal calneder by this subsidiary : " );
		}
			//Long fiscalId = this.fiscalCalanderRepository.findIdByName(fiscalCalenderBySubsidiary.getFiscalCalender());
			fiscalCalender = this.fiscalCalanderRepository.findByName(fiscalCalenderBySubsidiary.getFiscalCalender());
			//List<FiscalCalanderAccounting> fiscalCalanderAccountings = fiscalCalender.getFiscalCalanderAccounting();
			Long fiscalId = fiscalCalender.getId();
			log.info(" fiscal id " + fiscalId);
			List<FiscalCalanderAccounting>fiscalCalanderAccountings = this.fiscalCalanderAccountingRepository.findByFiscalIdAndIsPeriodOpen(fiscalId, true);
			fiscalCalender.setFiscalCalanderAccounting(fiscalCalanderAccountings);
			
		return fiscalCalender;
	}

}
