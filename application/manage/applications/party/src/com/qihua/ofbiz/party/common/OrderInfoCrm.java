package com.qihua.ofbiz.party.common;

import java.util.List;

public class OrderInfoCrm {

	private String brandId; //
	private String channelId; //
	private String sourceId; //
	private String sellerNick; //
	private String buyerNick; //
	private String buyerSex; //
	private String buyerAvatar; //
	private String custId; //
	private String accountMac; //
	private String tradeType; //
	private String tradeCode; //
	private String tradeAmt; //
	private String tradeNum; //
	private String totalFee; //
	private String deliveryTime; //
	private String payTime; //
	private String endTime; //
	private String orderDate; //
	private String buyerRate; //
	private String isBackOrder; //
	private String tradeStatus; //
	private String integralNum; //
	private String isIntegralOrder; //
	private Receiver receiver; //
	private List<SubTrade> subTrades; // 

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getSellerNick() {
		return sellerNick;
	}

	public void setSellerNick(String sellerNick) {
		this.sellerNick = sellerNick;
	}

	public String getBuyerNick() {
		return buyerNick;
	}

	public void setBuyerNick(String buyerNick) {
		this.buyerNick = buyerNick;
	}

	public String getBuyerSex() {
		return buyerSex;
	}

	public void setBuyerSex(String buyerSex) {
		this.buyerSex = buyerSex;
	}

	public String getBuyerAvatar() {
		return buyerAvatar;
	}

	public void setBuyerAvatar(String buyerAvatar) {
		this.buyerAvatar = buyerAvatar;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getAccountMac() {
		return accountMac;
	}

	public void setAccountMac(String accountMac) {
		this.accountMac = accountMac;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public String getTradeCode() {
		return tradeCode;
	}

	public void setTradeCode(String tradeCode) {
		this.tradeCode = tradeCode;
	}

	public String getTradeAmt() {
		return tradeAmt;
	}

	public void setTradeAmt(String tradeAmt) {
		this.tradeAmt = tradeAmt;
	}

	public String getTradeNum() {
		return tradeNum;
	}

	public void setTradeNum(String tradeNum) {
		this.tradeNum = tradeNum;
	}

	public String getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}

	public String getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(String deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public String getPayTime() {
		return payTime;
	}

	public void setPayTime(String payTime) {
		this.payTime = payTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getBuyerRate() {
		return buyerRate;
	}

	public void setBuyerRate(String buyerRate) {
		this.buyerRate = buyerRate;
	}

	public String getIsBackOrder() {
		return isBackOrder;
	}

	public void setIsBackOrder(String isBackOrder) {
		this.isBackOrder = isBackOrder;
	}

	public String getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(String tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public String getIntegralNum() {
		return integralNum;
	}

	public void setIntegralNum(String integralNum) {
		this.integralNum = integralNum;
	}

	public String getIsIntegralOrder() {
		return isIntegralOrder;
	}

	public void setIsIntegralOrder(String isIntegralOrder) {
		this.isIntegralOrder = isIntegralOrder;
	}

	public Receiver getReceiver() {
		return receiver;
	}

	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

	public List<SubTrade> getSubTrades() {
		return subTrades;
	}

	public void setSubTrades(List<SubTrade> subTrades) {
		this.subTrades = subTrades;
	}

}
