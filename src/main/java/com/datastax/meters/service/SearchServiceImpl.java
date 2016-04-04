package com.datastax.meters.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;

import com.datastax.meters.dao.TransactionDao;
import com.datastax.meters.model.Transaction;
import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.Timer;

public class SearchServiceImpl implements SearchService {

	private TransactionDao dao;
	private long timerSum = 0;
	private AtomicLong timerCount= new AtomicLong();

	public SearchServiceImpl() {		
		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
		this.dao = new TransactionDao(contactPointsStr.split(","));
	}	

	@Override
	public double getTimerAvg(){
		return timerSum/timerCount.get();
	}

	@Override
        /* Changed. 03-24-16 Alex */

	public List<Transaction> getTransactionsByTagAndDate(String deviceID, Set<String> search, DateTime from, DateTime to) {
		
		Timer timer = new Timer();
		List<Transaction> transactions;

		//If the to and from dates are within the 3 months we can use the latest transactions table as it should be faster.		
                /* only one set of transactions for now. 03-24-16 Alex */
			transactions = dao.getTransactionsForDeviceIDTagsAndDate(deviceID, search, from, to);
			
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}
}
