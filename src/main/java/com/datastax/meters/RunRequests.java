package com.datastax.meters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.meters.dao.TransactionDao;
import com.datastax.meters.data.TransactionGenerator;
import com.datastax.meters.model.Transaction;
import com.datastax.meters.service.SearchService;
import com.datastax.meters.service.SearchServiceImpl;
import com.datastax.demo.utils.KillableRunner;
import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.ThreadUtils;
import com.datastax.demo.utils.Timer;

public class RunRequests {

	private static Logger logger = LoggerFactory.getLogger(RunRequests.class);

	public RunRequests() {

		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
                /* Changed. 04-03-16 Alex */
		String noOfDeviceIDsStr = PropertyHelper.getProperty("noOfDeviceIDs", "1000000");
		String noOfRequestsStr = PropertyHelper.getProperty("noOfRequests", "10000");

		TransactionDao dao = new TransactionDao(contactPointsStr.split(","));
		BlockingQueue<Transaction> queue = new ArrayBlockingQueue<Transaction>(1000);
		int noOfThreads = Integer.parseInt(PropertyHelper.getProperty("noOfThreads", "8"));
		ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);

		int noOfDeviceIDs = Integer.parseInt(noOfDeviceIDsStr);
		int noOfRequests = Integer.parseInt(noOfRequestsStr);

		SearchService cqlService = new SearchServiceImpl();
		List<KillableRunner> tasks = new ArrayList<>();

		for (int i = 0; i < noOfThreads; i++) {

			KillableRunner task = new TransactionReader(cqlService, queue);
			executor.execute(task);
			tasks.add(task);
		}

		Timer timer = new Timer();
		for (int i = 0; i < noOfRequests; i++) {
			String search = TransactionGenerator.tagList.get(new Double(Math.random() * TransactionGenerator.tagList.size()).intValue());
			Set<String> tags = new HashSet<String>();
			tags.add(search);

			Transaction t = new Transaction();
                        /* Changed. 04-03-16 Alex */
			t.setDeviceID(new Double(Math.random() * noOfDeviceIDs).intValue() + "");
			
			if (Math.random()<.5){
				t.setTags(null);
			}else{
				t.setTags(tags);
			}
				
			// Send to a queue
			try {
				queue.put(t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		while (!queue.isEmpty()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
		
		ThreadUtils.shutdown(tasks, executor);
		timer.end();
		logger.info("CQL Query took " + timer.getTimeTakenMillis() + " ms for " +noOfRequests+ " requests. Avg : " + cqlService.getTimerAvg() + "ms per lookup");
		System.exit(0);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RunRequests();

		System.exit(0);
	}

}
