package com.datastax.banking.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.banking.model.Transaction;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/** 
 * Inserts into 2 tables 
 * @author patrickcallaghan
 *
 */
public class TransactionDao {

	private static Logger logger = LoggerFactory.getLogger(TransactionDao.class);
	private Session session;

        /* change keyspace name and table name.  Do not use latest transactions. 03-24-16 Alex */
     /*
	private static String keyspaceName = "datastax_banking_iot";

	private static String transactionTable = keyspaceName + ".transactions";
      */

	private static String keyspaceName = "metrics";

	private static String transactionTable = keyspaceName + ".raw_metrics";
  
        /* not used 03-24-16 Alex */
   /*
	private static String latestTransactionTable = keyspaceName + ".latest_transactions";
	private static final String INSERT_INTO_TRANSACTION = "Insert into "
			+ transactionTable
			+ " (cc_no, year, transaction_time, transaction_id, location, merchant, amount, user_id, status, notes, tags) values (?,?,?,?,?,?,?,?,?,?,?);";
    */

	private static final String INSERT_INTO_TRANSACTION = "Insert into "
			+ transactionTable
			+ " (device_id , metric_time , metric_name , metric_value ) values (?,?,?,?);";
    
        /* not used 03-24-16 Alex */
   /*
	private static final String INSERT_INTO_LATEST_TRANSACTION = "Insert into "
			+ latestTransactionTable
			+ " (cc_no, transaction_time, transaction_id, location, merchant, amount, user_id, status, notes, tags) values (?,?,?,?,?,?,?,?,?,?) ";
	private static final String GET_TRANSACTIONS_BY_ID = "select * from " + transactionTable
			+ " where cc_no = ? and year = ?";
	private static final String GET_TRANSACTIONS_BY_CCNO = "select * from " + transactionTable
			+ " where cc_no = ? and year = ? and transaction_time >= ? and transaction_time < ?";
	private static final String GET_LATEST_TRANSACTIONS_BY_CCNO = "select * from " + latestTransactionTable
			+ " where cc_no = ? and transaction_time >= ? and transaction_time < ?";
    */

	private static final String GET_TRANSACTIONS_BY_ID = "select * from " + transactionTable
			+ " where device_id = ? ";
	private static final String GET_TRANSACTIONS_BY_CCNO = "select * from " + transactionTable
			+ " where device_id = ? and metric_time >= ? and metric_time < ?";

	
	private PreparedStatement insertTransactionStmt;
	                /* private PreparedStatement insertLatestTransactionStmt; */
	private PreparedStatement getTransactionById;
	private PreparedStatement getTransactionByCCno;
	                /* private PreparedStatement getLatestTransactionByCCno; */

	private AtomicLong count = new AtomicLong(0);

	public TransactionDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();

		this.session = cluster.connect();

		try {
			this.insertTransactionStmt = session.prepare(INSERT_INTO_TRANSACTION);
			   /* this.insertLatestTransactionStmt = session.prepare(INSERT_INTO_LATEST_TRANSACTION); */

			this.getTransactionById = session.prepare(GET_TRANSACTIONS_BY_ID);
			this.getTransactionByCCno = session.prepare(GET_TRANSACTIONS_BY_CCNO);
			   /* this.getLatestTransactionByCCno = session.prepare(GET_LATEST_TRANSACTIONS_BY_CCNO); */

			   /* this.insertLatestTransactionStmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM); */
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
             /*
		ResultSetFuture future = session.executeAsync(this.insertTransactionStmt.bind(transaction.getCreditCardNo(), year, 
				transaction.getTransactionTime(), transaction.getTransactionId(), transaction.getLocation(),
				transaction.getMerchant(),transaction.getAmount(), transaction.getUserId(), transaction.getStatus(),
				transaction.getNotes(), transaction.getTags()));
             */
		ResultSetFuture future = session.executeAsync(this.insertTransactionStmt.bind(transaction.getDeviceID(), 
				transaction.getMetricTime(), transaction.getMetricName(), transaction.getMetricValue()));

             /* not needed. 03/24016 Alex */
             /*
		ResultSetFuture future1 = session.executeAsync(this.insertLatestTransactionStmt.bind(
				transaction.getCreditCardNo(), transaction.getTransactionTime(), transaction.getTransactionId(),
				transaction.getLocation(), transaction.getMerchant(), transaction.getAmount(), transaction.getUserId(),
				transaction.getStatus(), transaction.getNotes(), transaction.getTags()));

		future1.getUninterruptibly();
             */

		future.getUninterruptibly();

		long total = count.incrementAndGet();

		if (total % 10000 == 0) {
			logger.info("Total transactions processed : " + total);
		}

	}

	/* public Transaction getTransaction(String transactionId) { */
	public Transaction getTransaction(String deviceId) {

                /* changed to device. 03-24-16 Alex */
		   /* ResultSetFuture rs = this.session.executeAsync(this.getTransactionById.bind(transactionId)); */
		ResultSetFuture rs = this.session.executeAsync(this.getTransactionById.bind(deviceId));

		Row row = rs.getUninterruptibly().one();
		if (row == null) {
			        /* throw new RuntimeException("Error - no transaction for id:" + transactionId); */
			throw new RuntimeException("Error - no transaction for id:" + deviceId);
		}

		return rowToTransaction(row);
	}

	private Transaction rowToTransaction(Row row) {

		Transaction t = new Transaction();

                /* original 03-24-16 Alex */
            /*
		t.setAmount(row.getDouble("amount"));
		t.setCreditCardNo(row.getString("cc_no"));
		t.setMerchant(row.getString("merchant"));
		t.setLocation(row.getString("location"));
		t.setTransactionId(row.getString("transaction_id"));
		t.setTransactionTime(row.getDate("transaction_time"));
		t.setUserId(row.getString("user_id"));
		t.setNotes(row.getString("notes"));
		t.setStatus(row.getString("status"));
		t.setTags(row.getSet("tags", String.class));
             */

		t.setDeviceID(row.getString("device_id"));
		t.setMetricTime(row.getDate("metric_time"));
		t.setMetricName(row.getString("metric_name"));
		t.setMetricValue(row.getDecimal("metric_value"));

		return t;
	}

        /* not needed. 03-24-16 Alex */
/*
	public List<Transaction> getLatestTransactionsForCCNoTagsAndDate(String ccNo, Set<String> tags, DateTime from,
			DateTime to) {
		ResultSet resultSet = this.session.execute(getLatestTransactionByCCno.bind(ccNo, from.toDate(), to.toDate()));
		return processResultSet(resultSet, tags);
	}
*/
	
        /* use deviceId  03-24-16 Alex */
	/* public List<Transaction> getTransactionsForCCNoTagsAndDate(String ccNo, Set<String> tags, DateTime from, */
	public List<Transaction> getTransactionsForCCNoTagsAndDate(String deviceID, Set<String> tags, DateTime from,
			DateTime to) {
		/* ResultSet resultSet = this.session.execute(getTransactionByCCno.bind(ccNo, from.toDate(), to.toDate())); */
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
                                     /*
					if (transaction.getTags().contains(tag)) {
						transactions.add(transaction);
						break;
					}
                                      */
				}
			}else{
				transactions.add(transaction);
			}
		}
		return transactions;
	}
}
