package com.datastax.meters.service;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.datastax.meters.model.Transaction;

public interface SearchService {

	public double getTimerAvg();

	List<Transaction> getTransactionsByTagAndDate(String DeviceID, Set<String> search, DateTime from, DateTime to);
}
