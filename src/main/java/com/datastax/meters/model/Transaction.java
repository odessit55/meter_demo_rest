package com.datastax.meters.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.math.*;

public class Transaction {

	private String deviceID;
	private Date metricTime;
	private String metricName;
	private BigDecimal metricValue;

	private String creditCardNo;
	private String userId;
	private Date transactionTime;
	private String transactionId;
	private Map<String, Double> items;
	private String location;
	private String merchant;
	private Double amount;
	private String status;	
	private String notes;
	private Set<String> tags;

	public Transaction() {
		super();
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public Date getMetricTime() {
		return metricTime;
	}

	public void setMetricTime(Date metricTime) {
		this.metricTime = metricTime;
	}

	public BigDecimal getMetricValue() {
		return metricValue;
	}

	public void setMetricValue(BigDecimal metricValue) {
		this.metricValue = metricValue;
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public void setCreditCardNo(String creditCardNo) {
		this.creditCardNo = creditCardNo;
	}

	public void setTransactionTime(Date transactionTime) {
		this.transactionTime = transactionTime;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public void setItems(Map<String, Double> items) {
		this.items = items;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}


	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	


	public void setUserId(String userId) {
		this.userId = userId;
	}


        /* commented out. 03-27-16 Alex */
	
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
        /* changed 03-24-16 Alex */
		return "Transaction [deviceID=" + deviceID + ", metricTime=" + metricTime + ", metricName="
				+ metricName + ", metricValue=" + metricValue + "]";
	}

	public enum Status {
		CHECK, APPROVED, DECLINED, CLIENT_APPROVED, CLIENT_DECLINED, CLIENT_APPROVAL, TIMEOUT
	}
}
