package com.datastax.meters.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.meters.model.Transaction;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class TransactionDao {

	private static Logger logger = LoggerFactory.getLogger(TransactionDao.class);
	private Session session;

        /* change keyspace name and table name.  Do not use latest transactions. 03-24-16 Alex */

	private static String keyspaceName = "metrics";

	private static String transactionTable = keyspaceName + ".raw_metrics";
  
        /* not used 03-24-16 Alex */

	private static final String INSERT_INTO_TRANSACTION = "Insert into "
			+ transactionTable
			+ " (device_id , metric_time , metric_name , metric_value ) values (?,?,?,?);";
    
        /* not used 03-24-16 Alex */

	private static final String GET_TRANSACTIONS_BY_ID = "select * from " + transactionTable
			+ " where device_id = ? ";
	private static final String GET_TRANSACTIONS_BY_CCNO = "select * from " + transactionTable
			+ " where device_id = ? and metric_time >= ? and metric_time < ?";

	
	private PreparedStatement insertTransactionStmt;
	private PreparedStatement getTransactionById;
	private PreparedStatement getTransactionByCCno;

	private AtomicLong count = new AtomicLong(0);

	public TransactionDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();

		this.session = cluster.connect();

		try {
			this.insertTransactionStmt = session.prepare(INSERT_INTO_TRANSACTION);

			this.getTransactionById = session.prepare(GET_TRANSACTIONS_BY_ID);
			this.getTransactionByCCno = session.prepare(GET_TRANSACTIONS_BY_CCNO);

			this.insertTransactionStmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

		} catch (Exception e) {
			e.printStackTrace();
			session.close();
			cluster.close();
		}
	}

	public void saveTransaction(Transaction transaction) {
		insertTransactionAsync(transaction);
	}

	public void insertTransactionAsync(Transaction transaction) {
		
		int year = new DateTime().withMillis(transaction.getMetricTime().getTime()).getYear();
		
                /* original 03-24-16 Alex */
		ResultSetFuture future = session.executeAsync(this.insertTransactionStmt.bind(transaction.getDeviceID(), 
				transaction.getMetricTime(), transaction.getMetricName(), transaction.getMetricValue()));

             /* not needed. 03/24016 Alex */

		future.getUninterruptibly();

		long total = count.incrementAndGet();

		if (total % 10000 == 0) {
			logger.info("Total transactions processed : " + total);
		}

	}

	public Transaction getTransaction(String deviceId) {

                /* changed to device. 03-24-16 Alex */
		ResultSetFuture rs = this.session.executeAsync(this.getTransactionById.bind(deviceId));

		Row row = rs.getUninterruptibly().one();
		if (row == null) {
			throw new RuntimeException("Error - no transaction for id:" + deviceId);
		}

		return rowToTransaction(row);
	}

	private Transaction rowToTransaction(Row row) {

		Transaction t = new Transaction();

                /* original 03-24-16 Alex */

		t.setDeviceID(row.getString("device_id"));
		t.setMetricTime(row.getDate("metric_time"));
		t.setMetricName(row.getString("metric_name"));
		t.setMetricValue(row.getDecimal("metric_value"));

		return t;
	}

        /* not needed. 03-24-16 Alex */
	
        /* use deviceId  03-24-16 Alex */
	public List<Transaction> getTransactionsForCCNoTagsAndDate(String deviceID, Set<String> tags, DateTime from,
			DateTime to) {
		ResultSet resultSet = this.session.execute(getTransactionByCCno.bind(deviceID, from.toDate(), to.toDate()));
		
		return processResultSet(resultSet, tags);
	}

	private List<Transaction> processResultSet(ResultSet resultSet, Set<String> tags) {
		List<Row> rows = resultSet.all();
		List<Transaction> transactions = new ArrayList<Transaction>();

		for (Row row : rows) {

			Transaction transaction = rowToTransaction(row);	
			
			if (tags !=null && tags.size() !=0){
								
				Iterator<String> iter = tags.iterator();
				
				//Check to see if any of the search tags are in the tags of the transaction.
				while (iter.hasNext()) {
					String tag = iter.next();
					
                                        /* commented whole IF statement. 03-27-16 Alex */
				}
			}else{
				transactions.add(transaction);
			}
		}
		return transactions;
	}
}
