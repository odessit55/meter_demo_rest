Energy Meters - IoT
========================

An energy supplier is looking to capture all details of their smart meters for all their customers. The customer is expecting a total amount of 50 million devices but wants to start testing with 10 million devices although they want to know how DSE will scale when new users come online. They are concerned about the number of nodes that they will need to retain data over the long term. Over 90% of all the smart meters will deliver the values in a file to be loaded every morning. Each device will have 1 value for every 15 min interval in a day (96 values per day). The other 10% will deliver their results in real time.

The customer wants

    A REST API to show how real-time values will be delivered and stored.
    An efficient way to store data going forward.
    An indication on how long it will take to load 10 million new rows per day for daily load 
    A REST interface to get all values for a device between 2 dates. 
    A REST interface to get an aggregated view over 1 week, 1 month, 3 months, 6 months or a year. 


To create the schema, run the following

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup" -DcontactPoints=localhost
	

Alex NOTE: this will not work for this demo.  Either remove or update based on Avinash's data generation process
To create some transactions, run the following 
	
	mvn clean compile exec:java -Dexec.mainClass="com.datastax.meters.Main"  -DcontactPoints=localhost

You can use the following parameters to change the default no of transactions and devices 
	
	-DnoOfTransactions=10000000 -DnoOfCreditCards=1000000
	
To create the solr core, run 

	bin/dsetool create_core datastax_banking_iot.latest_transactions generateResources=true reindex=true coreOptions=rt.yaml

An example of cql queries would be

For the latest transaction table we can run the following types of queries
```
use datastax_banking_iot;

select * from latest_transactions where cc_no = '1234123412341234';

select * from latest_transactions where cc_no = '1234123412341234' and transaction_time > '2015-12-31';

select * from latest_transactions where cc_no = '1234123412341234' and transaction_time > '2015-12-31' and transaction_time < '2016-01-27';
```
For the (historic) transaction table we need to add the year into our queries.

```
select * from transactions where cc_no = '1234123412341234' and year = 2016;

select * from transactions where cc_no = '1234123412341234' and year = 2016 and transaction_time > '2015-12-31';

select * from transactions where cc_no = '1234123412341234' and year = 2016 and transaction_time > '2015-12-31' and transaction_time < '2016-01-27';
```
Using the solr_query

Get all the latest transactions from PC World in London (This is accross all credit cards and users)
```
select * from latest_transactions where solr_query = 'merchant:PC+World location:London' limit  100;
```
Get all the latest transactions for credit card '1' that have a tag of Work. 
```
select * from latest_transactions where solr_query = '{"q":"cc_no:1234123412341234", "fq":"tags:Work"}' limit  1000;
```
Gell all the transaction for credit card '1' that have a tag of Work and are within the last month
```
select * from latest_transactions where solr_query = '{"q":"cc_no:1234123412341234", "fq":"tags:Work", "fq":"transaction_time:[NOW-30DAY TO *]"}' limit  1000;
```

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Either remove or update above stuff...
Alex

To use the webservice, start the web server using 
```
mvn jetty:run
```
Open a browser and use a url like 
```
http://{servername}:8080/datastax-meters-iot/rest/gettransactions/{deviceid}/{from}/{to}
```
Note : the from and to are dates in the format yyyyMMdd hh:mm:ss - eg 
```
http://localhost:8080/datastax-meters-iot/rest/gettransactions/1234123412341234/20150101/20160102/
```

To run the requests run the following 
	NOTE Alex: does not work - remove or update accordingly
------------------------------------------
	mvn clean compile exec:java -Dexec.mainClass="com.datastax.meters.RunRequests" -DcontactPoints=localhost

To change the no of requests and no of devices add the following 

	-DnoOfRequests=100000  -DnoOfCreditCards=1000000
	
To remove the tables and the schema, run the following.

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaTeardown"
    
    
