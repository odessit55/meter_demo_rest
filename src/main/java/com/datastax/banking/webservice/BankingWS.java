package com.datastax.banking.webservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.banking.model.Transaction;
import com.datastax.banking.service.SearchService;
import com.datastax.banking.service.SearchServiceImpl;

@WebService
@Path("/")
public class BankingWS {

	private Logger logger = LoggerFactory.getLogger(BankingWS.class);
	private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMdd");

	//Service Layer.
	private SearchService service = new SearchServiceImpl();
	
	@GET
        /* use deiviceid. 03-27-16 Alex */
	/* @Path("/gettransactions/{creditcardno}/{from}/{to}")  */
	@Path("/gettransactions/{deviceid}/{from}/{to}")
	@Produces(MediaType.APPLICATION_JSON)
        /* changed. put PathParam as creditcardno  03-24-16 Alex */
	/* public Response getMovements(@PathParam("creditcardno") String ccNo, @PathParam("from") String fromDate, */
	public Response getMovements(@PathParam("deviceid") String deviceID, @PathParam("from") String fromDate,
			@PathParam("to") String toDate) {
		
		DateTime from = DateTime.now();
		DateTime to = DateTime.now();
		try {
			from = new DateTime(inputDateFormat.parse(fromDate));
			to = new DateTime(inputDateFormat.parse(toDate));
		} catch (ParseException e) {
			String error = "Caught exception parsing dates " + fromDate + "-" + toDate;
			
			logger.error(error);
			return Response.status(Status.BAD_REQUEST).entity(error).build();
		}
				
                /* changed. 03-24-16 Alex */
		/* List<Transaction> result = service.getTransactionsByTagAndDate(ccNo, null, from, to); */
		List<Transaction> result = service.getTransactionsByTagAndDate(deviceID, null, from, to);
                
		
		return Response.status(Status.OK).entity(result).build();
	}
	
}
