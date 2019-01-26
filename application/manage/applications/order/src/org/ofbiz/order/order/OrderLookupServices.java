/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package org.ofbiz.order.order;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.order.shoppingcart.ItemNotFoundException;
import org.ofbiz.party.contact.ContactHelper;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * OrderLookupServices
 */
public class OrderLookupServices {

	public static final String module = OrderLookupServices.class.getName();

	/*
	 * public static Map<String, Object> findOrders(DispatchContext dctx,
	 * Map<String, ? extends Object> context) { LocalDispatcher dispatcher =
	 * dctx.getDispatcher(); Delegator delegator = dctx.getDelegator(); Security
	 * security = dctx.getSecurity();
	 * 
	 * GenericValue userLogin = (GenericValue) context.get("userLogin"); Integer
	 * viewIndex = (Integer) context.get("viewIndex"); Integer viewSize =
	 * (Integer) context.get("viewSize"); String showAll = (String)
	 * context.get("showAll"); String useEntryDate = (String)
	 * context.get("useEntryDate"); Locale locale = (Locale)
	 * context.get("locale"); if (showAll == null) { showAll = "N"; }
	 * 
	 * // list of fields to select (initial list) List<String> fieldsToSelect =
	 * FastList.newInstance(); fieldsToSelect.add("orderId");
	 * fieldsToSelect.add("orderName"); fieldsToSelect.add("statusId");
	 * fieldsToSelect.add("orderTypeId"); fieldsToSelect.add("orderDate");
	 * fieldsToSelect.add("currencyUom"); fieldsToSelect.add("grandTotal");
	 * fieldsToSelect.add("remainingSubTotal");
	 * 
	 * // sorting by order date newest first List<String> orderBy =
	 * UtilMisc.toList("-orderDate", "-orderId");
	 * 
	 * // list to hold the parameters List<String> paramList =
	 * FastList.newInstance();
	 * 
	 * // list of conditions List<EntityCondition> conditions =
	 * FastList.newInstance();
	 * 
	 * // check security flag for purchase orders boolean canViewPo =
	 * security.hasEntityPermission("ORDERMGR", "_PURCHASE_VIEW", userLogin); if
	 * (!canViewPo) {
	 * conditions.add(EntityCondition.makeCondition("orderTypeId",
	 * EntityOperator.NOT_EQUAL, "PURCHASE_ORDER")); }
	 * 
	 * // dynamic view entity DynamicViewEntity dve = new DynamicViewEntity();
	 * dve.addMemberEntity("OH", "OrderHeader"); dve.addAliasAll("OH", "",
	 * null); // no prefix dve.addRelation("one-nofk", "", "OrderType",
	 * UtilMisc.toList(new ModelKeyMap("orderTypeId", "orderTypeId")));
	 * dve.addRelation("one-nofk", "", "StatusItem", UtilMisc.toList(new
	 * ModelKeyMap("statusId", "statusId")));
	 * 
	 * // start the lookup String orderId = (String) context.get("orderId"); if
	 * (UtilValidate.isNotEmpty(orderId)) { paramList.add("orderId=" + orderId);
	 * conditions.add(makeExpr("orderId", orderId)); }
	 * 
	 * // the base order header fields List<String> orderTypeList =
	 * UtilGenerics.checkList(context.get("orderTypeId")); if (orderTypeList !=
	 * null) { List<EntityExpr> orExprs = FastList.newInstance(); for(String
	 * orderTypeId : orderTypeList) { paramList.add("orderTypeId=" +
	 * orderTypeId);
	 * 
	 * if (!"PURCHASE_ORDER".equals(orderTypeId) ||
	 * ("PURCHASE_ORDER".equals(orderTypeId) && canViewPo)) {
	 * orExprs.add(EntityCondition.makeCondition("orderTypeId",
	 * EntityOperator.EQUALS, orderTypeId)); } }
	 * conditions.add(EntityCondition.makeCondition(orExprs,
	 * EntityOperator.OR)); }
	 * 
	 * String orderName = (String) context.get("orderName"); if
	 * (UtilValidate.isNotEmpty(orderName)) { paramList.add("orderName=" +
	 * orderName); conditions.add(makeExpr("orderName", orderName, true)); }
	 * 
	 * List<String> orderStatusList =
	 * UtilGenerics.checkList(context.get("orderStatusId")); if (orderStatusList
	 * != null) { List<EntityCondition> orExprs = FastList.newInstance();
	 * for(String orderStatusId : orderStatusList) {
	 * paramList.add("orderStatusId=" + orderStatusId); if
	 * ("PENDING".equals(orderStatusId)) { List<EntityExpr> pendExprs =
	 * FastList.newInstance();
	 * pendExprs.add(EntityCondition.makeCondition("statusId",
	 * EntityOperator.EQUALS, "ORDER_CREATED"));
	 * pendExprs.add(EntityCondition.makeCondition("statusId",
	 * EntityOperator.EQUALS, "ORDER_PROCESSING"));
	 * pendExprs.add(EntityCondition.makeCondition("statusId",
	 * EntityOperator.EQUALS, "ORDER_APPROVED"));
	 * orExprs.add(EntityCondition.makeCondition(pendExprs, EntityOperator.OR));
	 * } else { orExprs.add(EntityCondition.makeCondition("statusId",
	 * EntityOperator.EQUALS, orderStatusId)); } }
	 * conditions.add(EntityCondition.makeCondition(orExprs,
	 * EntityOperator.OR)); }
	 * 
	 * List<String> productStoreList =
	 * UtilGenerics.checkList(context.get("productStoreId")); if
	 * (productStoreList != null) { List<EntityExpr> orExprs =
	 * FastList.newInstance(); for(String productStoreId : productStoreList) {
	 * paramList.add("productStoreId=" + productStoreId);
	 * orExprs.add(EntityCondition.makeCondition("productStoreId",
	 * EntityOperator.EQUALS, productStoreId)); }
	 * conditions.add(EntityCondition.makeCondition(orExprs,
	 * EntityOperator.OR)); }
	 * 
	 * List<String> webSiteList =
	 * UtilGenerics.checkList(context.get("orderWebSiteId")); if (webSiteList !=
	 * null) { List<EntityExpr> orExprs = FastList.newInstance(); for(String
	 * webSiteId : webSiteList) { paramList.add("webSiteId=" + webSiteId);
	 * orExprs.add(EntityCondition.makeCondition("webSiteId",
	 * EntityOperator.EQUALS, webSiteId)); }
	 * conditions.add(EntityCondition.makeCondition(orExprs,
	 * EntityOperator.OR)); }
	 * 
	 * List<String> saleChannelList =
	 * UtilGenerics.checkList(context.get("salesChannelEnumId")); if
	 * (saleChannelList != null) { List<EntityExpr> orExprs =
	 * FastList.newInstance(); for(String salesChannelEnumId : saleChannelList)
	 * { paramList.add("salesChannelEnumId=" + salesChannelEnumId);
	 * orExprs.add(EntityCondition.makeCondition("salesChannelEnumId",
	 * EntityOperator.EQUALS, salesChannelEnumId)); }
	 * conditions.add(EntityCondition.makeCondition(orExprs,
	 * EntityOperator.OR)); }
	 * 
	 * String createdBy = (String) context.get("createdBy"); if
	 * (UtilValidate.isNotEmpty(createdBy)) { paramList.add("createdBy=" +
	 * createdBy); conditions.add(makeExpr("createdBy", createdBy)); }
	 * 
	 * String terminalId = (String) context.get("terminalId"); if
	 * (UtilValidate.isNotEmpty(terminalId)) { paramList.add("terminalId=" +
	 * terminalId); conditions.add(makeExpr("terminalId", terminalId)); }
	 * 
	 * String transactionId = (String) context.get("transactionId"); if
	 * (UtilValidate.isNotEmpty(transactionId)) { paramList.add("transactionId="
	 * + transactionId); conditions.add(makeExpr("transactionId",
	 * transactionId)); }
	 * 
	 * String externalId = (String) context.get("externalId"); if
	 * (UtilValidate.isNotEmpty(externalId)) { paramList.add("externalId=" +
	 * externalId); conditions.add(makeExpr("externalId", externalId)); }
	 * 
	 * String internalCode = (String) context.get("internalCode"); if
	 * (UtilValidate.isNotEmpty(internalCode)) { paramList.add("internalCode=" +
	 * internalCode); conditions.add(makeExpr("internalCode", internalCode)); }
	 * 
	 * String dateField = "Y".equals(useEntryDate) ? "entryDate" : "orderDate";
	 * String minDate = (String) context.get("minDate"); if
	 * (UtilValidate.isNotEmpty(minDate) && minDate.length() > 8) { minDate =
	 * minDate.trim(); if (minDate.length() < 14) minDate = minDate + " " +
	 * "00:00:00.000"; paramList.add("minDate=" + minDate);
	 * 
	 * try { Object converted = ObjectType.simpleTypeConvert(minDate,
	 * "Timestamp", null, null); if (converted != null) {
	 * conditions.add(EntityCondition.makeCondition(dateField,
	 * EntityOperator.GREATER_THAN_EQUAL_TO, converted)); } } catch
	 * (GeneralException e) { Debug.logWarning(e.getMessage(), module); } }
	 * 
	 * String maxDate = (String) context.get("maxDate"); if
	 * (UtilValidate.isNotEmpty(maxDate) && maxDate.length() > 8) { maxDate =
	 * maxDate.trim(); if (maxDate.length() < 14) maxDate = maxDate + " " +
	 * "23:59:59.999"; paramList.add("maxDate=" + maxDate);
	 * 
	 * try { Object converted = ObjectType.simpleTypeConvert(maxDate,
	 * "Timestamp", null, null); if (converted != null) {
	 * conditions.add(EntityCondition.makeCondition("orderDate",
	 * EntityOperator.LESS_THAN_EQUAL_TO, converted)); } } catch
	 * (GeneralException e) { Debug.logWarning(e.getMessage(), module); } }
	 * 
	 * // party (role) fields String userLoginId = (String)
	 * context.get("userLoginId"); String partyId = (String)
	 * context.get("partyId"); List<String> roleTypeList =
	 * UtilGenerics.checkList(context.get("roleTypeId"));
	 * 
	 * if (UtilValidate.isNotEmpty(userLoginId) &&
	 * UtilValidate.isEmpty(partyId)) { GenericValue ul = null; try { ul =
	 * delegator.findByPrimaryKeyCache("UserLogin",
	 * UtilMisc.toMap("userLoginId", userLoginId)); } catch
	 * (GenericEntityException e) { Debug.logWarning(e.getMessage(), module); }
	 * if (ul != null) { partyId = ul.getString("partyId"); } }
	 * 
	 * String isViewed = (String) context.get("isViewed"); if
	 * (UtilValidate.isNotEmpty(isViewed)) { paramList.add("isViewed=" +
	 * isViewed); conditions.add(makeExpr("isViewed", isViewed)); }
	 * 
	 * // Shipment Method String shipmentMethod = (String)
	 * context.get("shipmentMethod"); if
	 * (UtilValidate.isNotEmpty(shipmentMethod)) { String carrierPartyId =
	 * shipmentMethod.substring(0, shipmentMethod.indexOf("@")); String
	 * ShippingMethodTypeId =
	 * shipmentMethod.substring(shipmentMethod.indexOf("@")+1);
	 * dve.addMemberEntity("OISG", "OrderItemShipGroup"); dve.addAlias("OISG",
	 * "shipmentMethodTypeId"); dve.addAlias("OISG", "carrierPartyId");
	 * dve.addViewLink("OH", "OISG", Boolean.FALSE, UtilMisc.toList(new
	 * ModelKeyMap("orderId", "orderId")));
	 * 
	 * if (UtilValidate.isNotEmpty(carrierPartyId)) {
	 * paramList.add("carrierPartyId=" + carrierPartyId);
	 * conditions.add(makeExpr("carrierPartyId", carrierPartyId)); }
	 * 
	 * if (UtilValidate.isNotEmpty(ShippingMethodTypeId)) {
	 * paramList.add("ShippingMethodTypeId=" + ShippingMethodTypeId);
	 * conditions.add(makeExpr("shipmentMethodTypeId", ShippingMethodTypeId)); }
	 * } // PaymentGatewayResponse String gatewayAvsResult = (String)
	 * context.get("gatewayAvsResult"); String gatewayScoreResult = (String)
	 * context.get("gatewayScoreResult"); if
	 * (UtilValidate.isNotEmpty(gatewayAvsResult) ||
	 * UtilValidate.isNotEmpty(gatewayScoreResult)) { dve.addMemberEntity("OPP",
	 * "OrderPaymentPreference"); dve.addMemberEntity("PGR",
	 * "PaymentGatewayResponse"); dve.addAlias("OPP",
	 * "orderPaymentPreferenceId"); dve.addAlias("PGR", "gatewayAvsResult");
	 * dve.addAlias("PGR", "gatewayScoreResult"); dve.addViewLink("OH", "OPP",
	 * Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
	 * dve.addViewLink("OPP", "PGR", Boolean.FALSE, UtilMisc.toList(new
	 * ModelKeyMap("orderPaymentPreferenceId", "orderPaymentPreferenceId"))); }
	 * 
	 * if (UtilValidate.isNotEmpty(gatewayAvsResult)) {
	 * paramList.add("gatewayAvsResult=" + gatewayAvsResult);
	 * conditions.add(EntityCondition.makeCondition("gatewayAvsResult",
	 * gatewayAvsResult)); }
	 * 
	 * if (UtilValidate.isNotEmpty(gatewayScoreResult)) {
	 * paramList.add("gatewayScoreResult=" + gatewayScoreResult);
	 * conditions.add(EntityCondition.makeCondition("gatewayScoreResult",
	 * gatewayScoreResult)); }
	 * 
	 * // add the role data to the view if (roleTypeList != null || partyId !=
	 * null) { dve.addMemberEntity("OT", "OrderRole"); dve.addAlias("OT",
	 * "partyId"); dve.addAlias("OT", "roleTypeId"); dve.addViewLink("OH", "OT",
	 * Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId"))); }
	 * 
	 * if (UtilValidate.isNotEmpty(partyId)) { paramList.add("partyId=" +
	 * partyId); fieldsToSelect.add("partyId");
	 * conditions.add(makeExpr("partyId", partyId)); }
	 * 
	 * if (roleTypeList != null) { fieldsToSelect.add("roleTypeId");
	 * List<EntityExpr> orExprs = FastList.newInstance(); for(String roleTypeId
	 * : roleTypeList) { paramList.add("roleTypeId=" + roleTypeId);
	 * orExprs.add(makeExpr("roleTypeId", roleTypeId)); }
	 * conditions.add(EntityCondition.makeCondition(orExprs,
	 * EntityOperator.OR)); }
	 * 
	 * // order item fields String correspondingPoId = (String)
	 * context.get("correspondingPoId"); String subscriptionId = (String)
	 * context.get("subscriptionId"); String productId = (String)
	 * context.get("productId"); String budgetId = (String)
	 * context.get("budgetId"); String quoteId = (String)
	 * context.get("quoteId");
	 * 
	 * if (correspondingPoId != null || subscriptionId != null || productId !=
	 * null || budgetId != null || quoteId != null) { dve.addMemberEntity("OI",
	 * "OrderItem"); dve.addAlias("OI", "correspondingPoId"); dve.addAlias("OI",
	 * "subscriptionId"); dve.addAlias("OI", "productId"); dve.addAlias("OI",
	 * "budgetId"); dve.addAlias("OI", "quoteId"); dve.addViewLink("OH", "OI",
	 * Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId"))); }
	 * 
	 * if (UtilValidate.isNotEmpty(correspondingPoId)) {
	 * paramList.add("correspondingPoId=" + correspondingPoId);
	 * conditions.add(makeExpr("correspondingPoId", correspondingPoId)); }
	 * 
	 * if (UtilValidate.isNotEmpty(subscriptionId)) {
	 * paramList.add("subscriptionId=" + subscriptionId);
	 * conditions.add(makeExpr("subscriptionId", subscriptionId)); }
	 * 
	 * if (UtilValidate.isNotEmpty(productId)) { paramList.add("productId=" +
	 * productId); if (productId.startsWith("%") || productId.startsWith("*") ||
	 * productId.endsWith("%") || productId.endsWith("*")) {
	 * conditions.add(makeExpr("productId", productId)); } else { GenericValue
	 * product = null; try { product = delegator.findByPrimaryKey("Product",
	 * UtilMisc.toMap("productId", productId)); } catch (GenericEntityException
	 * e) { Debug.logWarning(e.getMessage(), module); } if (product != null) {
	 * String isVirtual = product.getString("isVirtual"); if (isVirtual != null
	 * && "Y".equals(isVirtual)) { List<EntityExpr> orExprs =
	 * FastList.newInstance();
	 * orExprs.add(EntityCondition.makeCondition("productId",
	 * EntityOperator.EQUALS, productId));
	 * 
	 * Map<String, Object> varLookup = null; try { varLookup =
	 * dispatcher.runSync("getAllProductVariants", UtilMisc.toMap("productId",
	 * productId)); } catch (GenericServiceException e) {
	 * Debug.logWarning(e.getMessage(), module); } List<GenericValue> variants =
	 * UtilGenerics.checkList(varLookup.get("assocProducts")); if (variants !=
	 * null) { for(GenericValue v : variants) {
	 * orExprs.add(EntityCondition.makeCondition("productId",
	 * EntityOperator.EQUALS, v.getString("productIdTo"))); } }
	 * conditions.add(EntityCondition.makeCondition(orExprs,
	 * EntityOperator.OR)); } else {
	 * conditions.add(EntityCondition.makeCondition("productId",
	 * EntityOperator.EQUALS, productId)); } } else { String failMsg =
	 * UtilProperties.getMessage("OrderErrorUiLabels",
	 * "OrderFindOrderProductInvalid", UtilMisc.toMap("productId", productId),
	 * locale); return ServiceUtil.returnFailure(failMsg); } } }
	 * 
	 * if (UtilValidate.isNotEmpty(budgetId)) { paramList.add("budgetId=" +
	 * budgetId); conditions.add(makeExpr("budgetId", budgetId)); }
	 * 
	 * if (UtilValidate.isNotEmpty(quoteId)) { paramList.add("quoteId=" +
	 * quoteId); conditions.add(makeExpr("quoteId", quoteId)); }
	 * 
	 * // payment preference fields String billingAccountId = (String)
	 * context.get("billingAccountId"); String finAccountId = (String)
	 * context.get("finAccountId"); String cardNumber = (String)
	 * context.get("cardNumber"); String accountNumber = (String)
	 * context.get("accountNumber"); String paymentStatusId = (String)
	 * context.get("paymentStatusId");
	 * 
	 * if (UtilValidate.isNotEmpty(paymentStatusId)) {
	 * paramList.add("paymentStatusId=" + paymentStatusId);
	 * conditions.add(makeExpr("paymentStatusId", paymentStatusId)); } if
	 * (finAccountId != null || cardNumber != null || accountNumber != null ||
	 * paymentStatusId != null) { dve.addMemberEntity("OP",
	 * "OrderPaymentPreference"); dve.addAlias("OP", "finAccountId");
	 * dve.addAlias("OP", "paymentMethodId"); dve.addAlias("OP",
	 * "paymentStatusId", "statusId", null, false, false, null);
	 * dve.addViewLink("OH", "OP", Boolean.FALSE, UtilMisc.toList(new
	 * ModelKeyMap("orderId", "orderId"))); }
	 * 
	 * // search by billing account ID if
	 * (UtilValidate.isNotEmpty(billingAccountId)) {
	 * paramList.add("billingAccountId=" + billingAccountId);
	 * conditions.add(makeExpr("billingAccountId", billingAccountId)); }
	 * 
	 * // search by fin account ID if (UtilValidate.isNotEmpty(finAccountId)) {
	 * paramList.add("finAccountId=" + finAccountId);
	 * conditions.add(makeExpr("finAccountId", finAccountId)); }
	 * 
	 * // search by card number if (UtilValidate.isNotEmpty(cardNumber)) {
	 * dve.addMemberEntity("CC", "CreditCard"); dve.addAlias("CC",
	 * "cardNumber"); dve.addViewLink("OP", "CC", Boolean.FALSE,
	 * UtilMisc.toList(new ModelKeyMap("paymentMethodId", "paymentMethodId")));
	 * 
	 * paramList.add("cardNumber=" + cardNumber);
	 * conditions.add(makeExpr("cardNumber", cardNumber)); }
	 * 
	 * // search by eft account number if
	 * (UtilValidate.isNotEmpty(accountNumber)) { dve.addMemberEntity("EF",
	 * "EftAccount"); dve.addAlias("EF", "accountNumber"); dve.addViewLink("OP",
	 * "EF", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("paymentMethodId",
	 * "paymentMethodId")));
	 * 
	 * paramList.add("accountNumber=" + accountNumber);
	 * conditions.add(makeExpr("accountNumber", accountNumber)); }
	 * 
	 * // shipment/inventory item String inventoryItemId = (String)
	 * context.get("inventoryItemId"); String softIdentifier = (String)
	 * context.get("softIdentifier"); String serialNumber = (String)
	 * context.get("serialNumber"); String shipmentId = (String)
	 * context.get("shipmentId");
	 * 
	 * if (shipmentId != null || inventoryItemId != null || softIdentifier !=
	 * null || serialNumber != null) { dve.addMemberEntity("II",
	 * "ItemIssuance"); dve.addAlias("II", "shipmentId"); dve.addAlias("II",
	 * "inventoryItemId"); dve.addViewLink("OH", "II", Boolean.FALSE,
	 * UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
	 * 
	 * if (softIdentifier != null || serialNumber != null) {
	 * dve.addMemberEntity("IV", "InventoryItem"); dve.addAlias("IV",
	 * "softIdentifier"); dve.addAlias("IV", "serialNumber");
	 * dve.addViewLink("II", "IV", Boolean.FALSE, UtilMisc.toList(new
	 * ModelKeyMap("inventoryItemId", "inventoryItemId"))); } }
	 * 
	 * if (UtilValidate.isNotEmpty(inventoryItemId)) {
	 * paramList.add("inventoryItemId=" + inventoryItemId);
	 * conditions.add(makeExpr("inventoryItemId", inventoryItemId)); }
	 * 
	 * if (UtilValidate.isNotEmpty(softIdentifier)) {
	 * paramList.add("softIdentifier=" + softIdentifier);
	 * conditions.add(makeExpr("softIdentifier", softIdentifier, true)); }
	 * 
	 * if (UtilValidate.isNotEmpty(serialNumber)) {
	 * paramList.add("serialNumber=" + serialNumber);
	 * conditions.add(makeExpr("serialNumber", serialNumber, true)); }
	 * 
	 * if (UtilValidate.isNotEmpty(shipmentId)) { paramList.add("shipmentId=" +
	 * shipmentId); conditions.add(makeExpr("shipmentId", shipmentId)); }
	 * 
	 * // back order checking String hasBackOrders = (String)
	 * context.get("hasBackOrders"); if (UtilValidate.isNotEmpty(hasBackOrders))
	 * { dve.addMemberEntity("IR", "OrderItemShipGrpInvRes"); dve.addAlias("IR",
	 * "quantityNotAvailable"); dve.addViewLink("OH", "IR", Boolean.FALSE,
	 * UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
	 * 
	 * paramList.add("hasBackOrders=" + hasBackOrders); if
	 * ("Y".equals(hasBackOrders)) {
	 * conditions.add(EntityCondition.makeCondition("quantityNotAvailable",
	 * EntityOperator.NOT_EQUAL, null));
	 * conditions.add(EntityCondition.makeCondition("quantityNotAvailable",
	 * EntityOperator.GREATER_THAN, BigDecimal.ZERO)); } else if
	 * ("N".equals(hasBackOrders)) { List<EntityExpr> orExpr =
	 * FastList.newInstance();
	 * orExpr.add(EntityCondition.makeCondition("quantityNotAvailable",
	 * EntityOperator.EQUALS, null));
	 * orExpr.add(EntityCondition.makeCondition("quantityNotAvailable",
	 * EntityOperator.EQUALS, BigDecimal.ZERO));
	 * conditions.add(EntityCondition.makeCondition(orExpr, EntityOperator.OR));
	 * } }
	 * 
	 * // Get all orders according to specific ship to country with
	 * "Only Include" or "Do not Include". String countryGeoId = (String)
	 * context.get("countryGeoId"); String includeCountry = (String)
	 * context.get("includeCountry"); if (UtilValidate.isNotEmpty(countryGeoId)
	 * && UtilValidate.isNotEmpty(includeCountry)) {
	 * paramList.add("countryGeoId=" + countryGeoId);
	 * paramList.add("includeCountry=" + includeCountry); // add condition to
	 * dynamic view dve.addMemberEntity("OCM", "OrderContactMech");
	 * dve.addMemberEntity("PA", "PostalAddress"); dve.addAlias("OCM",
	 * "contactMechId"); dve.addAlias("OCM", "contactMechPurposeTypeId");
	 * dve.addAlias("PA", "countryGeoId"); dve.addViewLink("OH", "OCM",
	 * Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
	 * dve.addViewLink("OCM", "PA", Boolean.FALSE,
	 * ModelKeyMap.makeKeyMapList("contactMechId"));
	 * 
	 * EntityConditionList<EntityExpr> exprs = null; if
	 * ("Y".equals(includeCountry)) { exprs =
	 * EntityCondition.makeCondition(UtilMisc.toList(
	 * EntityCondition.makeCondition("contactMechPurposeTypeId",
	 * "SHIPPING_LOCATION"), EntityCondition.makeCondition("countryGeoId",
	 * countryGeoId)), EntityOperator.AND); } else { exprs =
	 * EntityCondition.makeCondition(UtilMisc.toList(
	 * EntityCondition.makeCondition("contactMechPurposeTypeId",
	 * "SHIPPING_LOCATION"), EntityCondition.makeCondition("countryGeoId",
	 * EntityOperator.NOT_EQUAL, countryGeoId)), EntityOperator.AND); }
	 * conditions.add(exprs); }
	 * 
	 * // set distinct on so we only get one row per order EntityFindOptions
	 * findOpts = new EntityFindOptions(true,
	 * EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
	 * EntityFindOptions.CONCUR_READ_ONLY, true);
	 * 
	 * // create the main condition EntityCondition cond = null; if
	 * (conditions.size() > 0 || showAll.equalsIgnoreCase("Y")) { cond =
	 * EntityCondition.makeCondition(conditions, EntityOperator.AND); }
	 * 
	 * if (Debug.verboseOn()) { Debug.logInfo("Find order query: " +
	 * cond.toString(), module); }
	 * 
	 * List<GenericValue> orderList = FastList.newInstance(); int orderCount =
	 * 0;
	 * 
	 * // get the index for the partial list int lowIndex =
	 * (((viewIndex.intValue() - 1) * viewSize.intValue()) + 1); int highIndex =
	 * viewIndex.intValue() * viewSize.intValue();
	 * findOpts.setMaxRows(highIndex);
	 * 
	 * if (cond != null) { EntityListIterator eli = null; try { // do the lookup
	 * eli = delegator.findListIteratorByCondition(dve, cond, null,
	 * fieldsToSelect, orderBy, findOpts);
	 * 
	 * orderCount = eli.getResultsSizeAfterPartialList();
	 * 
	 * // get the partial list for this page eli.beforeFirst(); if (orderCount >
	 * viewSize.intValue()) { orderList = eli.getPartialList(lowIndex,
	 * viewSize.intValue()); } else if (orderCount > 0) { orderList =
	 * eli.getCompleteList(); }
	 * 
	 * if (highIndex > orderCount) { highIndex = orderCount; } } catch
	 * (GenericEntityException e) { Debug.logError(e, module); return
	 * ServiceUtil.returnError(e.getMessage()); } finally { if (eli != null) {
	 * try { eli.close(); } catch (GenericEntityException e) {
	 * Debug.logWarning(e, e.getMessage(), module); } } } }
	 * 
	 * // create the result map Map<String, Object> result =
	 * ServiceUtil.returnSuccess();
	 * 
	 * // filter out requested inventory problems
	 * filterInventoryProblems(context, result, orderList, paramList);
	 * 
	 * // format the param list String paramString = StringUtil.join(paramList,
	 * "&amp;");
	 * 
	 * result.put("highIndex", Integer.valueOf(highIndex));
	 * result.put("lowIndex", Integer.valueOf(lowIndex));
	 * result.put("viewIndex", viewIndex); result.put("viewSize", viewSize);
	 * result.put("showAll", showAll);
	 * 
	 * result.put("paramList", (paramString != null? paramString: ""));
	 * result.put("orderList", orderList); result.put("orderListSize",
	 * Integer.valueOf(orderCount));
	 * 
	 * return result; }
	 */

	public static Map<String, Object> findOrders(DispatchContext dctx,
			Map<String, ? extends Object> context) {
   
		Delegator delegator = dctx.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String lookupFlag = (String) context.get("lookupFlag");
		String orderStatus = (String) context.get("orderStatus");
		lookupFlag = UtilValidate.isNotEmpty(lookupFlag) ? lookupFlag : "Y";
		String sortField = "-orderId";
		// set the page parameters
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		int lowIndex = 0;
		int highIndex = 0;

		// 选择字段List
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("orderId");
		fieldsToSelect.add("orderDate");
		fieldsToSelect.add("grandTotal");
		fieldsToSelect.add("statusId");
		fieldsToSelect.add("productStoreId");
		fieldsToSelect.add("providerId");
		/*fieldsToSelect.add("discountMoney");
		fieldsToSelect.add("shouldPayMoney");
		fieldsToSelect.add("actualPayMoney");
		fieldsToSelect.add("notPayMoney");
		fieldsToSelect.add("distributionMethod");
		fieldsToSelect.add("getIntegral");
		fieldsToSelect.add("balance");*/
		// list to hold the parameters
		List<String> paramList = FastList.newInstance();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// list of conditions
		List<EntityCondition> conditions = FastList.newInstance();

		// dynamic view entity
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("OH", "OrderHeader");
		dve.addAliasAll("OH", "", null); // no prefix
		dve.addRelation("one-nofk", "", "OrderType",
				UtilMisc.toList(new ModelKeyMap("orderTypeId", "orderTypeId")));
		dve.addRelation("one-nofk", "", "StatusItem",
				UtilMisc.toList(new ModelKeyMap("statusId", "statusId")));
		dve.addMemberEntity("OI", "OrderItem");
		dve.addMemberEntity("P", "Product");
		dve.addAlias("P", "productId");
		dve.addAlias("P", "productTypeId");
		dve.addAlias("P", "productName");
		dve.addAlias("P", "businessPartyId");
		dve.addAliasAll("OI", "oi", null);
		dve.addViewLink("OH", "OI", Boolean.FALSE,
				ModelKeyMap.makeKeyMapList("orderId"));
		dve.addViewLink("OI", "P", Boolean.FALSE,
				ModelKeyMap.makeKeyMapList("productId"));

		conditions.add(makeExpr("productTypeId", "FINISHED_GOOD"));



		String userCategory = "";
		try {
			GenericValue userLoginParty = delegator.findByPrimaryKey("Party",
					UtilMisc.toMap("partyId", userLogin.get("partyId")));
			userCategory = UtilValidate.isNotEmpty(userLoginParty
					.getString("partyCategory")) ? userLoginParty
					.getString("partyCategory") : "";
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if ("BUSINESS".equals(userCategory)) { // 登录用户为商家
			try {
				List<GenericValue> partyRelationShipInfos = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.getString("partyId")));
				if(UtilValidate.isNotEmpty(partyRelationShipInfos)){
					conditions.add(makeExpr("businessPartyId",
							partyRelationShipInfos.get(0).getString("partyIdFrom")));
				}

			}catch (GenericEntityException e){
				e.printStackTrace();
			}
		}
		if (UtilValidate.isNotEmpty(orderStatus)) {
			paramList.add("orderStatus=" + orderStatus);
			paramMap.put("orderStatus", orderStatus);
			if ("ORDER_RETURNED".equals(orderStatus)) { // 退单
				dve.addMemberEntity("RI", "ReturnItem");
				dve.addAlias("RI", "returnStatusId", "statusId", null, null,
						null, null);
				dve.addAliasAll("RI", "", UtilMisc.toSet("statusId"));
				dve.addViewLink("OH", "RI", Boolean.FALSE,
						ModelKeyMap.makeKeyMapList("orderId"));
				conditions.add(EntityCondition.makeCondition("returnStatusId",
						EntityOperator.NOT_EQUAL, "RETURN_REJECTAPPLY"));
			} else {
				conditions.add(makeExpr("statusId", orderStatus));
			}
			if ("ORDER_WAITSHIP".equals(orderStatus)) {
				sortField = "orderId";
			}
		}

		// start the lookup
		String orderId = (String) context.get("orderId");
		if (UtilValidate.isNotEmpty(orderId)) {
			paramList.add("orderId=" + orderId);
			paramMap.put("orderId", orderId);
			conditions.add(EntityCondition.makeCondition("orderId",
					EntityOperator.LIKE, "%" + orderId + "%"));
		}

		String receivePartyName = (String) context.get("receivePartyName");
		String partyPhone = (String) context.get("partyPhone");
		String productName = (String) context.get("productName");
		String billToName = (String) context.get("billToName");
		String paymentMethodType = (String) context.get("paymentMethodType");
		String businessName = (String) context.get("businessName");
		String businessId = (String) context.get("businessId");
		String communityId = (String) context.get("communityId");
		String orderPhone = (String) context.get("orderPhone");

		String distributionMethod = (String) context
				.get("distributionMethoddd"); // 配送方式 暂时不加

		if (UtilValidate.isNotEmpty(receivePartyName)
				|| UtilValidate.isNotEmpty(partyPhone)
				|| UtilValidate.isNotEmpty(communityId)) {
			dve.addMemberEntity("OCM", "OrderContactMech");
			dve.addMemberEntity("PA", "PostalAddress");
			dve.addAliasAll("OCM", "", null);
			dve.addAliasAll("PA", "", null);
			dve.addViewLink("OH", "OCM", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			dve.addViewLink("OCM", "PA", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("contactMechId"));

		}

		if (UtilValidate.isNotEmpty(receivePartyName)) {
			paramList.add("receivePartyName=" + receivePartyName);
			paramMap.put("receivePartyName", receivePartyName);
			conditions.add(EntityCondition.makeCondition("toName",
					EntityOperator.LIKE, "%" + receivePartyName + "%"));
		}

		if (UtilValidate.isNotEmpty(communityId)) {
			paramList.add("communityId=" + communityId);
			paramMap.put("communityId", communityId);
			conditions.add(EntityCondition.makeCondition("communityId",
					communityId));
		}

		if (UtilValidate.isNotEmpty(distributionMethod)) {
			paramList.add("distributionMethoddd=" + distributionMethod);
			paramMap.put("distributionMethod", distributionMethod);
			if ("KDPS".equals(distributionMethod)) {
				conditions.add(EntityCondition.makeCondition(
						"distributionMethod", EntityOperator.IN,
						UtilMisc.toList("ZMPS", "GZRPS")));
			} else {
				conditions.add(EntityCondition.makeCondition(
						"distributionMethod", distributionMethod));
			}
		}

		if (UtilValidate.isNotEmpty(partyPhone)) {
			paramList.add("partyPhone=" + partyPhone);
			paramMap.put("partyPhone", partyPhone);
			conditions.add(EntityCondition.makeCondition("mobilePhone",
					EntityOperator.LIKE, "%" + partyPhone + "%"));
		}

		if (UtilValidate.isNotEmpty(productName)) {
			paramList.add("productName=" + productName);
			paramMap.put("productName", productName);
			conditions.add(EntityCondition.makeCondition("productName",
					EntityOperator.LIKE, "%" + productName + "%"));
		}

		if (UtilValidate.isNotEmpty(businessName)) {
			paramList.add("businessName=" + businessName);
			paramMap.put("businessName", businessName);
			dve.addMemberEntity("PB", "PartyBusiness");
			dve.addAlias("PB", "businessName");
			dve.addAlias("PB", "partyId");
			dve.addAlias("P", "businessPartyId");
			dve.addViewLink("P", "PB", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("businessPartyId", "partyId"));
			conditions.add(EntityCondition.makeCondition("businessName",
					EntityOperator.LIKE, "%" + businessName + "%"));
			conditions.add(EntityCondition.makeCondition("businessName",
					EntityOperator.NOT_EQUAL, null));
		}
		if (UtilValidate.isNotEmpty(businessId)) {
			paramList.add("businessId=" + businessId);
			paramMap.put("businessId", businessId);
			conditions.add(EntityCondition.makeCondition("businessPartyId",
					businessId));
		}
		if (UtilValidate.isNotEmpty(billToName)) {
			paramList.add("billToName=" + billToName);
			paramMap.put("billToName", billToName);
			dve.addMemberEntity("OT", "OrderRole");
			dve.addAliasAll("OT", "", null);
			dve.addMemberEntity("PP", "PartyAndPerson");
			dve.addAlias("PP", "partyId");
//			dve.addAlias("PP", "name");
			dve.addAlias("PP", "nickname");
			dve.addViewLink("OH", "OT", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			dve.addViewLink("OT", "PP", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("partyId"));
//			conditions.add(EntityCondition.makeCondition("name",
//					EntityOperator.LIKE, "%" + billToName + "%"));
			conditions.add(EntityCondition.makeCondition("nickname",
					EntityOperator.LIKE, "%" + billToName + "%"));
			conditions.add(EntityCondition.makeCondition("roleTypeId",
					"BILL_TO_CUSTOMER"));
		}

		if (UtilValidate.isNotEmpty(orderPhone)) {
			paramList.add("orderPhone=" + orderPhone);
			paramMap.put("orderPhone", orderPhone);
			dve.addMemberEntity("OT", "OrderRole");
			dve.addAliasAll("OT", "", null);
			dve.addMemberEntity("PP", "PartyAndPerson");
			dve.addAlias("PP", "partyId");
			dve.addAlias("PP", "mobile");
			dve.addViewLink("OH", "OT", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			dve.addViewLink("OT", "PP", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("partyId"));
			conditions.add(EntityCondition.makeCondition("mobile", orderPhone));
			conditions.add(EntityCondition.makeCondition("roleTypeId",
					"BILL_TO_CUSTOMER"));
		}

		if (UtilValidate.isNotEmpty(paymentMethodType)) {
			paramList.add("paymentMethodType=" + paymentMethodType);
			paramMap.put("paymentMethodType", paymentMethodType);
			dve.addMemberEntity("OPP", "OrderPaymentPreference");
			dve.addAlias("OPP", "orderId");
			dve.addAlias("OPP", "paymentMethodTypeId");
			dve.addViewLink("OH", "OPP", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			conditions.add(EntityCondition.makeCondition("paymentMethodTypeId",
					paymentMethodType));
		}

		String startDate = (String) context.get("startDate");
		if (UtilValidate.isNotEmpty(startDate) && startDate.length() > 8) {
			paramList.add("startDate=" + startDate);
			paramMap.put("startDate", startDate);
			startDate = startDate.trim();
			if (startDate.length() < 14) {
                startDate = startDate + " " + "00:00:00.000";
            }
			try {
				Object converted = ObjectType.simpleTypeConvert(startDate,
						"Timestamp", null, null);
				if (converted != null) {
					conditions.add(EntityCondition.makeCondition("orderDate",
							EntityOperator.GREATER_THAN_EQUAL_TO, converted));
				}
			} catch (GeneralException e) {
				Debug.logWarning(e.getMessage(), module);
			}
		}

		String endDate = (String) context.get("endDate");
		if (UtilValidate.isNotEmpty(endDate) && endDate.length() > 8) {
			paramList.add("endDate=" + endDate);
			paramMap.put("endDate", endDate);
			endDate = endDate.trim();
			if (endDate.length() < 14) {
                endDate = endDate + " " + "23:59:59.999";
            }
			try {
				Object converted = ObjectType.simpleTypeConvert(endDate,
						"Timestamp", null, null);
				if (converted != null) {
					conditions.add(EntityCondition.makeCondition("orderDate",
							EntityOperator.LESS_THAN_EQUAL_TO, converted));
				}
			} catch (GeneralException e) {
				Debug.logWarning(e.getMessage(), module);
			}
		}

		List<GenericValue> orderList = FastList.newInstance();
		int orderCount = 0;

		// get the index for the partial list
		lowIndex = viewIndex * viewSize + 1;
		highIndex = (viewIndex + 1) * viewSize;

		// set distinct on so we only get one row per order
		EntityFindOptions findOpts = new EntityFindOptions(true,
				EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
				EntityFindOptions.CONCUR_READ_ONLY, true);
		// create the main condition
		EntityCondition cond = null;
		if (conditions.size() > 0) {
			cond = EntityCondition
					.makeCondition(conditions, EntityOperator.AND);
		}

		if ("Y".equals(lookupFlag)) {
			EntityListIterator eli = null;
			try {
				// do the lookup
    
				eli = delegator.findListIteratorByCondition(dve, cond, null,
						fieldsToSelect, UtilMisc.toList(sortField), findOpts);

				orderCount = eli.getResultsSizeAfterPartialList();

				// get the partial list for this page
				eli.beforeFirst();
				if (orderCount > viewSize && viewSize >= 0) {
					orderList = eli.getPartialList(lowIndex, viewSize);
				} else if (orderCount > 0) {
					orderList = eli.getCompleteList();
				}

				if (highIndex > orderCount) {
					highIndex = orderCount;
				}
			} catch (GenericEntityException e) {
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			} finally {
				if (eli != null) {
					try {
						eli.close();
					} catch (GenericEntityException e) {
						Debug.logWarning(e, e.getMessage(), module);
					}
				}
			}
		}

		// create the result map
		Map<String, Object> result = ServiceUtil.returnSuccess();

		// format the param list
		String paramString = StringUtil.join(paramList, "&");
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("viewIndex", viewIndex);
		result.put("viewSize", viewSize);
		result.put("paramMap", paramMap);
		result.put("paramList", (paramString != null ? paramString : ""));
		result.put("orderList", orderList);
		result.put("orderListSize", Integer.valueOf(orderCount));
		result.put("orderStatus", orderStatus);
		result.put("lookupFlag", lookupFlag);
  
//        Debug.logInfo(sw.prettyPrint(),module);
//        System.out.println("sw.prettyPrint() = " + sw.prettyPrint());
		return result;
	}

	/**
	 * 虚拟订单查找 add by dongxiao 2016.2.1
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findVirtualOrders(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		Security security = dctx.getSecurity();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String lookupFlag = (String) context.get("lookupFlag");
		String orderStatus = (String) context.get("orderStatus");
		lookupFlag = UtilValidate.isNotEmpty(lookupFlag) ? lookupFlag : "Y";

		// set the page parameters
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		int lowIndex = 0;
		int highIndex = 0;

		// 选择字段List
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("orderId");
		fieldsToSelect.add("orderDate");
		fieldsToSelect.add("grandTotal");
		fieldsToSelect.add("statusId");
		/*fieldsToSelect.add("discountMoney");
		fieldsToSelect.add("shouldPayMoney");
		fieldsToSelect.add("actualPayMoney");
		fieldsToSelect.add("notPayMoney");
		fieldsToSelect.add("balance");*/
		// list to hold the parameters
		List<String> paramList = FastList.newInstance();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// list of conditions
		List<EntityCondition> conditions = FastList.newInstance();

		// dynamic view entity
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("OH", "OrderHeader");
		dve.addAliasAll("OH", "", null); // no prefix
		dve.addRelation("one-nofk", "", "OrderType",
				UtilMisc.toList(new ModelKeyMap("orderTypeId", "orderTypeId")));
		dve.addRelation("one-nofk", "", "StatusItem",
				UtilMisc.toList(new ModelKeyMap("statusId", "statusId")));
		dve.addMemberEntity("OI", "OrderItem");
		dve.addMemberEntity("P", "Product");
		dve.addAlias("P", "productId");
		dve.addAlias("P", "productTypeId");
		dve.addAlias("P", "productName");
		dve.addAlias("P", "businessPartyId");
		dve.addAliasAll("OI", "", null);
		dve.addViewLink("OH", "OI", Boolean.FALSE,
				ModelKeyMap.makeKeyMapList("orderId"));
		dve.addViewLink("OI", "P", Boolean.FALSE,
				ModelKeyMap.makeKeyMapList("productId"));
		conditions.add(makeExpr("productTypeId", "VIRTUAL_GOOD"));
		if (UtilValidate.isNotEmpty(orderStatus)) {
			paramList.add("orderStatus=" + orderStatus);
			paramMap.put("orderStatus", orderStatus);
			if ("ORDER_RETURNED".equals(orderStatus)) { // 退单
				dve.addMemberEntity("RI", "ReturnItem");
				dve.addAlias("RI", "returnStatusId", "statusId", null, null,
						null, null);
				dve.addAliasAll("RI", "", UtilMisc.toSet("statusId"));
				dve.addViewLink("OH", "RI", Boolean.FALSE,
						ModelKeyMap.makeKeyMapList("orderId"));
				conditions.add(EntityCondition.makeCondition("returnStatusId",
						EntityOperator.NOT_EQUAL, "RETURN_REJECTAPPLY"));
			} else {
				conditions.add(makeExpr("statusId", orderStatus));
			}
		}

		String userCategory = "";
		try {
			GenericValue userLoginParty = delegator.findByPrimaryKey("Party",
					UtilMisc.toMap("partyId", userLogin.get("partyId")));
			userCategory = UtilValidate.isNotEmpty(userLoginParty
					.getString("partyCategory")) ? userLoginParty
					.getString("partyCategory") : "";
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if ("BUSINESS".equals(userCategory)) { // 登录用户为商家
			conditions.add(makeExpr("businessPartyId",
					userLogin.getString("partyId")));
		}

		// start the lookup
		String orderId = (String) context.get("orderId");
		if (UtilValidate.isNotEmpty(orderId)) {
			paramList.add("orderId=" + orderId);
			paramMap.put("orderId", orderId);
			conditions.add(EntityCondition.makeCondition("orderId",
					EntityOperator.LIKE, "%" + orderId + "%"));
		}

		String receivePartyName = (String) context.get("receivePartyName");
		String partyPhone = (String) context.get("partyPhone");
		String productName = (String) context.get("productName");
		String billToName = (String) context.get("billToName");
		String paymentMethodType = (String) context.get("paymentMethodType");
		String businessName = (String) context.get("businessName");

		/*
		 * if (UtilValidate.isNotEmpty(receivePartyName) ||
		 * UtilValidate.isNotEmpty(partyPhone)){ dve.addMemberEntity("OCM",
		 * "OrderContactMech"); dve.addMemberEntity("PA", "PostalAddress");
		 * dve.addAliasAll("OCM", "", null); dve.addAliasAll("PA", "", null);
		 * dve.addViewLink("OH", "OCM", Boolean.FALSE,
		 * ModelKeyMap.makeKeyMapList("orderId")); dve.addViewLink("OCM", "PA",
		 * Boolean.FALSE, ModelKeyMap.makeKeyMapList("contactMechId"));
		 * 
		 * }
		 * 
		 * if (UtilValidate.isNotEmpty(receivePartyName)){
		 * paramList.add("receivePartyName=" + receivePartyName);
		 * paramMap.put("receivePartyName",receivePartyName);
		 * conditions.add(EntityCondition.makeCondition("toName",
		 * EntityOperator.LIKE,"%" + receivePartyName + "%")); }
		 */

		if (UtilValidate.isNotEmpty(partyPhone)) {
			paramList.add("partyPhone=" + partyPhone);
			paramMap.put("partyPhone", partyPhone);
			dve.addMemberEntity("OA", "OrderAttribute");
			dve.addAliasAll("OA", "", null);
			conditions.add(EntityCondition
					.makeCondition("attrName", "telPhone"));
			conditions.add(EntityCondition.makeCondition("attrValue",
					EntityOperator.LIKE, "%" + partyPhone + "%"));
			dve.addViewLink("OH", "OA", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
		}

		if (UtilValidate.isNotEmpty(productName)) {
			paramList.add("productName=" + productName);
			paramMap.put("productName", productName);

			conditions.add(EntityCondition.makeCondition("productName",
					EntityOperator.LIKE, "%" + receivePartyName + "%"));
		}

		if (UtilValidate.isNotEmpty(businessName)) {
			paramList.add("businessName=" + businessName);
			paramMap.put("businessName", businessName);
			dve.addMemberEntity("SP", "SupplierProduct");
			dve.addMemberEntity("PG", "PartyGroup");
			dve.addAlias("SP", "partyId");
			dve.addAlias("SP", "productId");
			dve.addAlias("PG", "partyId");
			dve.addAlias("PG", "groupName");
			dve.addViewLink("P", "SP", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("productId"));
			dve.addViewLink("SP", "PG", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("partyId"));
			conditions.add(EntityCondition.makeCondition("groupName",
					EntityOperator.LIKE, "%" + businessName + "%"));
		}

		if (UtilValidate.isNotEmpty(billToName)) {
			paramList.add("billToName=" + billToName);
			paramMap.put("billToName", billToName);
			dve.addMemberEntity("OT", "OrderRole");
			dve.addAliasAll("OT", "", null);
			dve.addMemberEntity("PP", "PartyAndPerson");
			dve.addAlias("PP", "partyId");
//			dve.addAlias("PP", "name");
            dve.addAlias("PP", "nickname");
			dve.addViewLink("OH", "OT", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			dve.addViewLink("OT", "PP", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("partyId"));
//			conditions.add(EntityCondition.makeCondition("name",
//					EntityOperator.LIKE, "%" + billToName + "%"));
            conditions.add(EntityCondition.makeCondition("nickname",
                    EntityOperator.LIKE, "%" + billToName + "%"));
			conditions.add(EntityCondition.makeCondition("roleTypeId",
					"BILL_TO_CUSTOMER"));
		}

		if (UtilValidate.isNotEmpty(paymentMethodType)) {
			paramList.add("paymentMethodType=" + paymentMethodType);
			paramMap.put("paymentMethodType", paymentMethodType);
			dve.addMemberEntity("OPP", "OrderPaymentPreference");
			dve.addAlias("OPP", "orderId");
			dve.addAlias("OPP", "paymentMethodTypeId");
			dve.addViewLink("OH", "OPP", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			conditions.add(EntityCondition.makeCondition("paymentMethodTypeId",
					paymentMethodType));
		}

		String startDate = (String) context.get("startDate");
		if (UtilValidate.isNotEmpty(startDate) && startDate.length() > 8) {
			paramList.add("startDate=" + startDate);
			paramMap.put("startDate", startDate);
			startDate = startDate.trim();
			if (startDate.length() < 14) {
                startDate = startDate + " " + "00:00:00.000";
            }
			try {
				Object converted = ObjectType.simpleTypeConvert(startDate,
						"Timestamp", null, null);
				if (converted != null) {
					conditions.add(EntityCondition.makeCondition("orderDate",
							EntityOperator.GREATER_THAN_EQUAL_TO, converted));
				}
			} catch (GeneralException e) {
				Debug.logWarning(e.getMessage(), module);
			}
		}

		String endDate = (String) context.get("endDate");
		if (UtilValidate.isNotEmpty(endDate) && endDate.length() > 8) {
			paramList.add("endDate=" + endDate);
			paramMap.put("endDate", endDate);
			endDate = endDate.trim();
			if (endDate.length() < 14) {
                endDate = endDate + " " + "23:59:59.999";
            }
			try {
				Object converted = ObjectType.simpleTypeConvert(endDate,
						"Timestamp", null, null);
				if (converted != null) {
					conditions.add(EntityCondition.makeCondition("orderDate",
							EntityOperator.LESS_THAN_EQUAL_TO, converted));
				}
			} catch (GeneralException e) {
				Debug.logWarning(e.getMessage(), module);
			}
		}

		List<GenericValue> orderList = FastList.newInstance();
		int orderCount = 0;

		// get the index for the partial list
		lowIndex = viewIndex * viewSize + 1;
		highIndex = (viewIndex + 1) * viewSize;

		// set distinct on so we only get one row per order
		EntityFindOptions findOpts = new EntityFindOptions(true,
				EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
				EntityFindOptions.CONCUR_READ_ONLY, true);
		// create the main condition
		EntityCondition cond = null;
		if (conditions.size() > 0) {
			cond = EntityCondition
					.makeCondition(conditions, EntityOperator.AND);
		}

		if ("Y".equals(lookupFlag)) {
			EntityListIterator eli = null;
			try {
				// do the lookup
				eli = delegator.findListIteratorByCondition(dve, cond, null,
						fieldsToSelect, UtilMisc.toList("-orderId"), findOpts);

				orderCount = eli.getResultsSizeAfterPartialList();

				// get the partial list for this page
				eli.beforeFirst();
				if (orderCount > viewSize) {
					orderList = eli.getPartialList(lowIndex, viewSize);
				} else if (orderCount > 0) {
					orderList = eli.getCompleteList();
				}

				if (highIndex > orderCount) {
					highIndex = orderCount;
				}
			} catch (GenericEntityException e) {
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			} finally {
				if (eli != null) {
					try {
						eli.close();
					} catch (GenericEntityException e) {
						Debug.logWarning(e, e.getMessage(), module);
					}
				}
			}
		}

		// create the result map
		Map<String, Object> result = ServiceUtil.returnSuccess();

		// format the param list
		String paramString = StringUtil.join(paramList, "&");
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("viewIndex", viewIndex);
		result.put("viewSize", viewSize);
		result.put("paramMap", paramMap);
		result.put("paramList", (paramString != null ? paramString : ""));
		result.put("orderList", orderList);
		result.put("orderListSize", Integer.valueOf(orderCount));
		result.put("orderStatus", orderStatus);
		result.put("lookupFlag", lookupFlag);

		return result;
	}

	public static Map<String, Object> findComment(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		String lookupFlag = (String) context.get("lookupFlag");
		lookupFlag = UtilValidate.isNotEmpty(lookupFlag) ? lookupFlag : "Y";

		// set the page parameters
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		int lowIndex = 0;
		int highIndex = 0;

		// 选择字段List
		List<String> fieldsToSelect = FastList.newInstance();
		// list to hold the parameters
		List<String> paramList = FastList.newInstance();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// list of conditions
		List<EntityCondition> conditions = FastList.newInstance();
		// start the lookup
		String orderId = (String) context.get("orderId");
		String productName = (String) context.get("productName");
		String businessName = (String) context.get("businessName");
		String publishUserId = (String) context.get("publishUserId");
		String commentLevel = (String) context.get("commentLevel");
		String isReply = (String) context.get("isReply");
		String comment = (String) context.get("comment");
		// dynamic view entity
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("PR", "ProductReview");
		dve.addMemberEntity("P", "Product");
		dve.addAliasAll("PR", "", null); // no prefix
		dve.addAliasAll("P", "", null); // no prefix
		dve.addViewLink("PR", "P", Boolean.FALSE,
				ModelKeyMap.makeKeyMapList("productId"));

		if (UtilValidate.isNotEmpty(publishUserId)) {
			paramList.add("publishUserId=" + publishUserId);
			paramMap.put("publishUserId", publishUserId);
			conditions.add(EntityCondition.makeCondition("userLoginId",
					EntityOperator.LIKE, "%" + publishUserId + "%"));
		}

		if (UtilValidate.isNotEmpty(commentLevel)) {
			paramList.add("commentLevel=" + commentLevel);
			paramMap.put("commentLevel", commentLevel);
			conditions.add(EntityCondition.makeCondition("productRating",
					EntityOperator.EQUALS, new BigDecimal(commentLevel)));
		}

		if (UtilValidate.isNotEmpty(isReply)) {
			paramList.add("isReply=" + isReply);
			paramMap.put("isReply", isReply);
			if ("0".equals(isReply)) {
				List<EntityCondition> list = new LinkedList<EntityCondition>();
				list.add(EntityCondition.makeCondition("isReply", "0"));
				list.add(EntityCondition.makeCondition("isReply", null));
				conditions.add(EntityCondition.makeCondition(list,
						EntityOperator.OR));
			} else {
				conditions.add(makeExpr("isReply", isReply));
			}
		}

		if (UtilValidate.isNotEmpty(comment)) {
			paramList.add("comment=" + comment);
			paramMap.put("comment", comment);
			conditions.add(EntityCondition.makeCondition("productReview",
					EntityOperator.LIKE, "%" + comment + "%"));
		}

		if (UtilValidate.isNotEmpty(orderId)) {
			paramList.add("orderId=" + orderId);
			paramMap.put("orderId", orderId);
			conditions.add(EntityCondition.makeCondition("orderId",
					EntityOperator.LIKE, "%" + orderId + "%"));
		}
		if (UtilValidate.isNotEmpty(productName)) {
			paramList.add("productName=" + productName);
			paramMap.put("productName", productName);
			conditions.add(EntityCondition.makeCondition("productName",
					EntityOperator.LIKE, "%" + productName + "%"));
		}

		if (UtilValidate.isNotEmpty(businessName)) {
			paramList.add("businessName=" + businessName);
			paramMap.put("businessName", businessName);
			dve.addMemberEntity("PB", "PartyBusiness");
			dve.addAlias("PB", "businessName");
			dve.addAlias("PB", "partyId");
			dve.addAlias("P", "businessPartyId");
			dve.addViewLink("P", "PB", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("businessPartyId", "partyId"));
			conditions.add(EntityCondition.makeCondition("businessName",
					EntityOperator.LIKE, "%" + businessName + "%"));
			conditions.add(EntityCondition.makeCondition("businessName",
					EntityOperator.NOT_EQUAL, null));
		}
		List<GenericValue> commentList = FastList.newInstance();
		int commentCount = 0;
		// get the index for the partial list
		lowIndex = viewIndex * viewSize + 1;
		highIndex = (viewIndex + 1) * viewSize;
		// set distinct on so we only get one row per order
		EntityFindOptions findOpts = new EntityFindOptions(true,
				EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
				EntityFindOptions.CONCUR_READ_ONLY, false);
		// create the main condition
		EntityCondition cond = null;
		if (conditions.size() > 0) {
			cond = EntityCondition
					.makeCondition(conditions, EntityOperator.AND);
		}
		if ("Y".equals(lookupFlag)) {
			EntityListIterator eli = null;
			try {
				// do the lookup
				eli = delegator.findListIteratorByCondition(dve, cond, null,
						fieldsToSelect, null, findOpts);
				commentCount = eli.getResultsSizeAfterPartialList();
				// get the partial list for this page
				eli.beforeFirst();
				if (commentCount > viewSize) {
					commentList = eli.getPartialList(lowIndex, viewSize);
				} else if (commentCount > 0) {
					commentList = eli.getCompleteList();
				}

				if (highIndex > commentCount) {
					highIndex = commentCount;
				}
			} catch (GenericEntityException e) {
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			} finally {
				if (eli != null) {
					try {
						eli.close();
					} catch (GenericEntityException e) {
						Debug.logWarning(e, e.getMessage(), module);
					}
				}
			}
		}

		// create the result map
		Map<String, Object> result = ServiceUtil.returnSuccess();
		// format the param list
		String paramString = StringUtil.join(paramList, "&");
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("viewIndex", viewIndex);
		result.put("viewSize", viewSize);
		result.put("paramMap", paramMap);
		result.put("paramList", (paramString != null ? paramString : ""));
		result.put("commentList", commentList);
		result.put("commentListSize", Integer.valueOf(commentCount));
		result.put("lookupFlag", lookupFlag);
		return result;
	}

	public static Map<String, Object> findReturns(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		Security security = dctx.getSecurity();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String lookupFlag = (String) context.get("lookupFlag");
		String returnStatus = (String) context.get("returnStatus");
		String returnType = (String) context.get("returnType");
		lookupFlag = UtilValidate.isNotEmpty(lookupFlag) ? lookupFlag : "Y";

		// set the page parameters
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		int lowIndex = 0;
		int highIndex = 0;

		// 选择字段List
		List<String> fieldsToSelect = null;
		/*
		 * fieldsToSelect.add("orderId"); fieldsToSelect.add("orderDate");
		 * fieldsToSelect.add("grandTotal"); fieldsToSelect.add("statusId");
		 */
		// list to hold the parameters
		List<String> paramList = FastList.newInstance();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// list of conditions
		List<EntityCondition> conditions = FastList.newInstance();

		// dynamic view entity
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("OH", "OrderHeader");
		dve.addMemberEntity("RI", "ReturnItem");
		dve.addAlias("OH","productStoreId");
		dve.addAlias("OH","providerId");
		dve.addAliasAll("OH", "", UtilMisc.toSet("statusId")); // no prefix
		dve.addAliasAll("RI", "", null); // no prefix
		dve.addAlias("RI", "createdStamp");

		dve.addRelation("one-nofk", "", "StatusItem",
				UtilMisc.toList(new ModelKeyMap("statusId", "statusId")));
		dve.addMemberEntity("OI", "OrderItem");
		dve.addMemberEntity("P", "Product");
		dve.addAlias("P", "productId");
		dve.addAlias("P", "productName");
		dve.addAlias("P", "businessPartyId");
		dve.addAliasAll("OI", "", null);
		dve.addViewLink("RI", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId","orderId","productId","productId"));
		dve.addViewLink("RI", "OH", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
		dve.addViewLink("OI", "P", Boolean.FALSE,ModelKeyMap.makeKeyMapList("productId"));

		if (UtilValidate.isEmpty(returnType)) {
			conditions.add(makeExpr("returnType", "0"));
		} else {
			conditions.add(makeExpr("returnType", returnType));
		}

		if (UtilValidate.isNotEmpty(returnStatus)) {
			paramList.add("returnStatus=" + returnStatus);
			paramMap.put("returnStatus", returnStatus);
			conditions.add(makeExpr("statusId", returnStatus));
		}

		// start the lookup
		String orderId = (String) context.get("orderId");
		if (UtilValidate.isNotEmpty(orderId)) {
			paramList.add("orderId=" + orderId);
			paramMap.put("orderId", orderId);
			conditions.add(EntityCondition.makeCondition("orderId",
					EntityOperator.LIKE, "%" + orderId + "%"));
		}

		String returnId = (String) context.get("returnId");
		if (UtilValidate.isNotEmpty(returnId)) {
			paramList.add("returnId=" + returnId);
			paramMap.put("returnId", returnId);
			conditions.add(EntityCondition.makeCondition("returnId",
					EntityOperator.LIKE, "%" + returnId + "%"));
		}

		String receivePartyName = (String) context.get("receivePartyName");
		String partyPhone = (String) context.get("partyPhone");
		String productName = (String) context.get("productName");
		String billToName = (String) context.get("billToName");
		String paymentMethodType = (String) context.get("paymentMethodType");
		String businessName = (String) context.get("businessName");

		if (UtilValidate.isNotEmpty(receivePartyName)
				|| UtilValidate.isNotEmpty(partyPhone)) {
			dve.addMemberEntity("OCM", "OrderContactMech");
			dve.addMemberEntity("PA", "PostalAddress");
			dve.addAliasAll("OCM", "", null);
			dve.addAliasAll("PA", "", null);
			dve.addViewLink("OH", "OCM", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			dve.addViewLink("OCM", "PA", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("contactMechId"));

		}

		if (UtilValidate.isNotEmpty(receivePartyName)) {
			paramList.add("receivePartyName=" + receivePartyName);
			paramMap.put("receivePartyName", receivePartyName);
			conditions.add(EntityCondition.makeCondition("toName",
					EntityOperator.LIKE, "%" + receivePartyName + "%"));
		}

		if (UtilValidate.isNotEmpty(partyPhone)) {
			paramList.add("partyPhone=" + partyPhone);
			paramMap.put("partyPhone", partyPhone);
			conditions.add(EntityCondition.makeCondition("mobilePhone",
					EntityOperator.LIKE, "%" + partyPhone + "%"));
		}

		if (UtilValidate.isNotEmpty(productName)) {
			paramList.add("productName=" + productName);
			paramMap.put("productName", productName);
			conditions.add(EntityCondition.makeCondition("productName",
					EntityOperator.LIKE, "%" + productName + "%"));
		}

		if (UtilValidate.isNotEmpty(businessName)) {
			paramList.add("businessName=" + businessName);
			paramMap.put("businessName", businessName);
			dve.addMemberEntity("PB", "PartyBusiness");
			dve.addAlias("PB", "businessName");
			dve.addAlias("PB", "partyId");
			dve.addAlias("P", "businessPartyId");
			dve.addViewLink("P", "PB", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("businessPartyId", "partyId"));
			conditions.add(EntityCondition.makeCondition("businessName",
					EntityOperator.LIKE, "%" + businessName + "%"));
			conditions.add(EntityCondition.makeCondition("businessName",
					EntityOperator.NOT_EQUAL, null));
		}

		String userCategory = "";
		try {
			GenericValue userLoginParty = delegator.findByPrimaryKey("Party",
					UtilMisc.toMap("partyId", userLogin.get("partyId")));
			userCategory = UtilValidate.isNotEmpty(userLoginParty
					.getString("partyCategory")) ? userLoginParty
					.getString("partyCategory") : "";
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if ("BUSINESS".equals(userCategory)) { // 登录用户为商家
//			conditions.add(makeExpr("businessPartyId",
//					userLogin.getString("partyId")));
			try {
				List<GenericValue> partyRelationShipInfos = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.getString("partyId")));
				if(UtilValidate.isNotEmpty(partyRelationShipInfos)){
					conditions.add(makeExpr("businessPartyId",
							partyRelationShipInfos.get(0).getString("partyIdFrom")));
				}
			}catch (GenericEntityException e){
				e.printStackTrace();
			}
		}

		if (UtilValidate.isNotEmpty(billToName)) {
			paramList.add("billToName=" + billToName);
			paramMap.put("billToName", billToName);
			dve.addMemberEntity("OT", "OrderRole");
			dve.addAliasAll("OT", "", null);
			dve.addMemberEntity("PP", "PartyAndPerson");
			dve.addAlias("PP", "partyId");
			dve.addAlias("PP", "name");
			dve.addViewLink("OH", "OT", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			dve.addViewLink("OT", "PP", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("partyId"));
			conditions.add(EntityCondition.makeCondition("name",
					EntityOperator.LIKE, "%" + billToName + "%"));
			conditions.add(EntityCondition.makeCondition("roleTypeId",
					"BILL_TO_CUSTOMER"));
		}

		if (UtilValidate.isNotEmpty(paymentMethodType)) {
			paramList.add("paymentMethodType=" + paymentMethodType);
			paramMap.put("paymentMethodType", paymentMethodType);
			dve.addMemberEntity("OPP", "OrderPaymentPreference");
			dve.addAlias("OPP", "orderId");
			dve.addAlias("OPP", "paymentMethodTypeId");
			dve.addViewLink("OH", "OPP", Boolean.FALSE,
					ModelKeyMap.makeKeyMapList("orderId"));
			conditions.add(EntityCondition.makeCondition("paymentMethodTypeId",
					paymentMethodType));
		}

		String startDate = (String) context.get("startDate");
		if (UtilValidate.isNotEmpty(startDate) && startDate.length() > 8) {
			paramList.add("startDate=" + startDate);
			paramMap.put("startDate", startDate);
			startDate = startDate.trim();
			if (startDate.length() < 14) {
                startDate = startDate + " " + "00:00:00.000";
            }
			try {
				Object converted = ObjectType.simpleTypeConvert(startDate,
						"Timestamp", null, null);
				if (converted != null) {
					conditions.add(EntityCondition.makeCondition(
							"createdStamp",
							EntityOperator.GREATER_THAN_EQUAL_TO, converted));
				}
			} catch (GeneralException e) {
				Debug.logWarning(e.getMessage(), module);
			}
		}

		String endDate = (String) context.get("endDate");
		if (UtilValidate.isNotEmpty(endDate) && endDate.length() > 8) {
			paramList.add("endDate=" + endDate);
			paramMap.put("endDate", endDate);
			endDate = endDate.trim();
			if (endDate.length() < 14) {
                endDate = endDate + " " + "23:59:59.999";
            }
			try {
				Object converted = ObjectType.simpleTypeConvert(endDate,
						"Timestamp", null, null);
				if (converted != null) {
					conditions.add(EntityCondition.makeCondition(
							"createdStamp", EntityOperator.LESS_THAN_EQUAL_TO,
							converted));
				}
			} catch (GeneralException e) {
				Debug.logWarning(e.getMessage(), module);
			}
		}
//        //增加商家判断
//        conditions.add(EntityCondition.makeCondition("productStoreId",context.get("productStoreId")));
        List<GenericValue> orderList = FastList.newInstance();
		int orderCount = 0;

		// get the index for the partial list
		lowIndex = viewIndex * viewSize + 1;
		highIndex = (viewIndex + 1) * viewSize;

		// set distinct on so we only get one row per order
		EntityFindOptions findOpts = new EntityFindOptions(true,
				EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
				EntityFindOptions.CONCUR_READ_ONLY, false);
		// create the main condition
		EntityCondition cond = null;
		if (conditions.size() > 0) {
			cond = EntityCondition
					.makeCondition(conditions, EntityOperator.AND);
		}

		
		if ("Y".equals(lookupFlag)) {
			EntityListIterator eli = null;
			try {
				// do the lookup
				eli = delegator.findListIteratorByCondition(dve, cond, null,
						fieldsToSelect, UtilMisc.toList("-returnId"), findOpts);

				orderCount = eli.getResultsSizeAfterPartialList();

				// get the partial list for this page
				eli.beforeFirst();
				if (orderCount > viewSize && viewSize >= 0) {
					orderList = eli.getPartialList(lowIndex, viewSize);
				} else if (orderCount > 0) {
					orderList = eli.getCompleteList();
				}

				if (highIndex > orderCount) {
					highIndex = orderCount;
				}
			} catch (GenericEntityException e) {
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			} finally {
				if (eli != null) {
					try {
						eli.close();
					} catch (GenericEntityException e) {
						Debug.logWarning(e, e.getMessage(), module);
					}
				}
			}
		}

		// create the result map
		Map<String, Object> result = ServiceUtil.returnSuccess();

		// format the param list
		String paramString = StringUtil.join(paramList, "&");
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("viewIndex", viewIndex);
		result.put("viewSize", viewSize);
		result.put("paramMap", paramMap);
		result.put("paramList", (paramString != null ? paramString : ""));
		result.put("returnList", orderList);
		result.put("returnListSize", Integer.valueOf(orderCount));
		result.put("returnStatus", returnStatus);
		result.put("lookupFlag", lookupFlag);

		return result;
	}

	/**
	 * add by dx 2016.01.05 完成订单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static String findOrderProducts(HttpServletRequest request,
			HttpServletResponse response) throws GenericEntityException {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		// set the page parameters
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) request
					.getParameter("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		int viewSize = 3;
		try {
			viewSize = Integer.parseInt((String) request
					.getParameter("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		int lowIndex = 0;
		int highIndex = 0;
		// get the index for the partial list
		lowIndex = viewIndex * viewSize + 1;
		highIndex = (viewIndex + 1) * viewSize;
		int count = 0;
		// 总共页数
		int pageCount = 0;
		List<GenericValue> productPriceViewList = null;
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("ppv", "ProductAndPriceView");
		dve.addAliasAll("ppv", "", null);
		List<EntityCondition> conditions = FastList.newInstance();
		String productName = request.getParameter("productName");
		String productId = request.getParameter("productId");
		BigDecimal productPriceStart = null;
		BigDecimal productPriceEnd = null;
		conditions.add(EntityCondition.makeCondition("productPriceTypeId",
				"DEFAULT_PRICE"));
		conditions.add(EntityCondition.makeCondition("productTypeId",
				"FINISHED_GOOD"));
		conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
		List<EntityCondition> isSkuList = FastList.newInstance();
		isSkuList.add(EntityCondition.makeCondition("isVirtual",
				EntityOperator.NOT_EQUAL, "Y"));
		isSkuList.add(EntityCondition.makeCondition("isVirtual",
				EntityOperator.EQUALS, null));
		List<EntityCondition> list = FastList.newInstance();
		list.add(EntityCondition.makeCondition("isDel",
				EntityOperator.NOT_EQUAL, "Y"));
		list.add(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS,
				null));
		conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
		conditions.add(EntityCondition.makeCondition(isSkuList,
				EntityOperator.OR));
		List<EntityCondition> list2 = FastList.newInstance();
		list2.add(EntityCondition.makeCondition("salesDiscontinuationDate",
				EntityOperator.GREATER_THAN_EQUAL_TO,
				UtilDateTime.nowTimestamp()));
		list2.add(EntityCondition.makeCondition("salesDiscontinuationDate",
				EntityOperator.EQUALS, null));
		conditions.add(EntityCondition.makeCondition(list2, EntityOperator.OR));
		conditions
				.add(EntityCondition.makeCondition("introductionDate",
						EntityOperator.LESS_THAN_EQUAL_TO,
						UtilDateTime.nowTimestamp()));
		if (UtilValidate.isNotEmpty(productName)) {
			conditions.add(EntityCondition.makeCondition("productName",
					EntityOperator.LIKE, "%" + productName + "%"));
			request.setAttribute("productName", productName);
		} else {
			request.setAttribute("productName", "");
		}
		if (UtilValidate.isNotEmpty(productId)) {
			conditions.add(EntityCondition.makeCondition("productId",
					EntityOperator.LIKE, "%" + productId + "%"));
			request.setAttribute("productId", productId);
		} else {
			request.setAttribute("productId", "");
		}
		if (UtilValidate.isNotEmpty(request.getParameter("productPriceStart"))) {
			productPriceStart = new BigDecimal(
					request.getParameter("productPriceStart"));
			conditions.add(EntityCondition.makeCondition("price",
					EntityOperator.GREATER_THAN_EQUAL_TO, productPriceStart));
			request.setAttribute("productPriceStart", productPriceStart);
		} else {
			request.setAttribute("productPriceStart", "");
		}
		if (UtilValidate.isNotEmpty(request.getParameter("productPriceEnd"))) {
			productPriceEnd = new BigDecimal(
					request.getParameter("productPriceEnd"));
			conditions.add(EntityCondition.makeCondition("price",
					EntityOperator.LESS_THAN_EQUAL_TO, productPriceEnd));
			request.setAttribute("productPriceEnd", productPriceEnd);
		} else {
			request.setAttribute("productPriceEnd", "");
		}
		EntityFindOptions findOpts = new EntityFindOptions(true,
				EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
				EntityFindOptions.CONCUR_READ_ONLY, false);
		EntityListIterator eli = null;
		eli = delegator.findListIteratorByCondition(dve,
				EntityCondition.makeCondition(conditions, EntityOperator.AND),
				null, null, UtilMisc.toList("productId"), findOpts);
		count = eli.getResultsSizeAfterPartialList();
		eli.beforeFirst();
		if (count > viewSize) {
			productPriceViewList = eli.getPartialList(lowIndex, viewSize);
			if (count % viewSize == 0) {
				pageCount = count / viewSize;
			} else {
				pageCount = count / viewSize + 1;
			}
		} else if (count > 0) {
			productPriceViewList = eli.getCompleteList();
			pageCount = 1;
		}
		if (highIndex > count) {
			highIndex = count;
		}
		List<Map<String, Object>> productPriceNewViewList = new LinkedList<Map<String, Object>>();
		for (GenericValue g : productPriceViewList) {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("productId", g.getString("productId"));
			tempMap.put("productName", g.get("productName"));
			tempMap.put("price", g.get("price"));
			GenericValue business = delegator.findByPrimaryKey("PartyBusiness",
					UtilMisc.toMap("partyId", g.getString("businessPartyId")));
			String bussinessName = (business != null) ? (String) business
					.get("businessName") : "";
			tempMap.put("bussinessName", bussinessName);
			productPriceNewViewList.add(tempMap);
		}
		request.setAttribute("productPriceViewList", productPriceNewViewList);
		request.setAttribute("VIEW_INDEX", viewIndex);
		request.setAttribute("pageCount", pageCount);
		return "success";
	}

	/**
	 * 订单操作日志 add by dongxiao 2016.1.14
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */
	public static String orderLog(HttpServletRequest request,
			HttpServletResponse response) throws GenericEntityException {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String orderId = request.getParameter("orderId");
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// set the page parameters
		List<GenericValue> logList = delegator.findByAnd("OrderOperateLog",
				UtilMisc.toMap("orderId", orderId));
		List<Map<String, Object>> logListNew = new LinkedList<Map<String, Object>>();
		for (GenericValue log : logList) {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("operateType", log.getString("operateType"));
			tempMap.put("operator", log.getString("operator"));
			tempMap.put("operateTime",
					f.format(log.getTimestamp("operateTime")));
			tempMap.put("operateReason", log.getString("operateReason") == null ? "" : log.getString("operateReason"));
			logListNew.add(tempMap);
		}
		request.setAttribute("logList", logListNew);
		return "success";
	}

	/**
	 * 判断产品是否参加活动
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */
	public static String isHaveActive(HttpServletRequest request,
			HttpServletResponse response) throws GenericEntityException {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		DynamicViewEntity dve = new DynamicViewEntity();
		List<EntityCondition> conditions = FastList.newInstance();
		List<GenericValue> list = null;
		String productIds = request.getParameter("productIds");
		String productId = productIds.split(",")[0];
		int isActive = 0;
		EntityFindOptions findOpts = new EntityFindOptions(true,
				EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
				EntityFindOptions.CONCUR_READ_ONLY, false);
		EntityListIterator eli = null;
		// do the lookup
		GenericValue business = null;
		dve.addMemberEntity("PA", "ProductActivity");
		dve.addMemberEntity("PAG", "ProductActivityGoods");
		dve.addAliasAll("PA", "", null);
		dve.addAliasAll("PAG", "", null);
		dve.addViewLink("PA", "PAG", Boolean.FALSE,
				ModelKeyMap.makeKeyMapList("activityId", "activityId"));
		conditions.add(EntityCondition.makeCondition("productId", productId));
		eli = delegator.findListIteratorByCondition(dve,
				EntityCondition.makeCondition(conditions, EntityOperator.AND),
				null, null, null, findOpts);
		list = eli.getCompleteList();
		List<Map<String, Object>> finalList = new LinkedList<Map<String, Object>>();
		if (UtilValidate.isNotEmpty(list)) {
			isActive = 1;
			for (GenericValue g : list) {
				BigDecimal productPrice = null;
				if ("SEC_KILL".equals(g.getString("activityType"))) { // 秒杀
					productPrice = g.getBigDecimal("productPrice");
				} else { // 团购
					Long groupQuantity = g.getLong("hasBuyQuantity"); //已售数量
					List<GenericValue> productGroupOrderRules = null; 
					try {
		                productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule",UtilMisc.toMap("activityId",g.getString("activityId")),UtilMisc.toList("orderQuantity"));//阶梯价规则表
		            } catch (GenericEntityException e) {
		                e.printStackTrace();
		            }
					productPrice = productGroupOrderRules.get(0).getBigDecimal("orderPrice");
		            for(int i=0;i<productGroupOrderRules.size();i++){
		                if(productGroupOrderRules.get(i).getLong("orderQuantity").compareTo(Long.valueOf(groupQuantity)) <= 0){
		                	productPrice = productGroupOrderRules.get(i).getBigDecimal("orderPrice");
		                }else{
		                    break;
		                }
		            }
//					
				}
				if (productPrice != null) {
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("productId", productId);
					tempMap.put(
							"productName",
							delegator.findByPrimaryKey("Product",
									UtilMisc.toMap("productId", productId))
									.getString("productName"));
					tempMap.put("activityName", g.getString("activityName"));
					tempMap.put("activityId", g.getString("activityId"));
					tempMap.put("productPrice", productPrice);
					finalList.add(tempMap);
				}
			}
		}
		if (eli != null) {
			try {
				eli.close();
			} catch (GenericEntityException e) {
				Debug.logWarning(e, e.getMessage(), module);
			}
		}
		if (UtilValidate.isEmpty(finalList)) { // 没有查询到活动
			isActive = 0;
		}
		request.setAttribute("success", isActive);
		request.setAttribute("table", finalList);
		return "success";
	}

	/**
	 * 得到用户默认地址 add by dongxiao 2016.1.10
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */
	public static String getUserPostalAddress(HttpServletRequest request,
			HttpServletResponse response) throws GenericEntityException,
			ItemNotFoundException, CartItemModifyException {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		HttpSession session = request.getSession();
		String currenPartyId = (String) session.getAttribute("currenPartyId");
		List<Map<String, Object>> addressInfos = new LinkedList<Map<String, Object>>();
		GenericValue party = delegator.findByPrimaryKey("Party",
				UtilMisc.toMap("partyId", currenPartyId));
		List<GenericValue> shippingContactMechList = (List<GenericValue>) ContactHelper
				.getContactMechByType(party, "POSTAL_ADDRESS", false);
		for (GenericValue scm : shippingContactMechList) {
			String contactMechId = scm.getString("contactMechId");
			GenericValue postalAddress = delegator.findByPrimaryKey(
					"PostalAddress",
					UtilMisc.toMap("contactMechId", contactMechId));
			if ((postalAddress.get("isDefault") != null)
					&& "1".equals(postalAddress.get("isDefault"))) {
				Map<String, Object> maptemp = new HashMap<String, Object>();
				maptemp.put("postalAddress", postalAddress);
				addressInfos.add(maptemp);
				break;
			}
		}
		if (UtilValidate.isEmpty(addressInfos)) {
			addressInfos = ContactMechWorker.getPartyPostalAddresses(request,
					currenPartyId, "_NA_");
		}
		request.setAttribute("addressInfos", addressInfos);
		return "success";
	}

	public static void filterInventoryProblems(
			Map<String, ? extends Object> context, Map<String, Object> result,
			List<GenericValue> orderList, List<String> paramList) {
		List<String> filterInventoryProblems = FastList.newInstance();

		String doFilter = (String) context.get("filterInventoryProblems");
		if (doFilter == null) {
			doFilter = "N";
		}

		if ("Y".equals(doFilter) && orderList.size() > 0) {
			paramList.add("filterInventoryProblems=Y");
			for (GenericValue orderHeader : orderList) {
				OrderReadHelper orh = new OrderReadHelper(orderHeader);
				BigDecimal backorderQty = orh.getOrderBackorderQuantity();
				if (backorderQty.compareTo(BigDecimal.ZERO) == 1) {
					filterInventoryProblems.add(orh.getOrderId());
				}
			}
		}

		List<String> filterPOsOpenPastTheirETA = FastList.newInstance();
		List<String> filterPOsWithRejectedItems = FastList.newInstance();
		List<String> filterPartiallyReceivedPOs = FastList.newInstance();

		String filterPOReject = (String) context
				.get("filterPOsWithRejectedItems");
		String filterPOPast = (String) context.get("filterPOsOpenPastTheirETA");
		String filterPartRec = (String) context
				.get("filterPartiallyReceivedPOs");
		if (filterPOReject == null) {
			filterPOReject = "N";
		}
		if (filterPOPast == null) {
			filterPOPast = "N";
		}
		if (filterPartRec == null) {
			filterPartRec = "N";
		}

		boolean doPoFilter = false;
		if ("Y".equals(filterPOReject)) {
			paramList.add("filterPOsWithRejectedItems=Y");
			doPoFilter = true;
		}
		if ("Y".equals(filterPOPast)) {
			paramList.add("filterPOsOpenPastTheirETA=Y");
			doPoFilter = true;
		}
		if ("Y".equals(filterPartRec)) {
			paramList.add("filterPartiallyReceivedPOs=Y");
			doPoFilter = true;
		}

		if (doPoFilter && orderList.size() > 0) {
			for (GenericValue orderHeader : orderList) {
				OrderReadHelper orh = new OrderReadHelper(orderHeader);
				String orderType = orh.getOrderTypeId();
				String orderId = orh.getOrderId();

				if ("PURCHASE_ORDER".equals(orderType)) {
					if ("Y".equals(filterPOReject)
							&& orh.getRejectedOrderItems()) {
						filterPOsWithRejectedItems.add(orderId);
					} else if ("Y".equals(filterPOPast)
							&& orh.getPastEtaOrderItems(orderId)) {
						filterPOsOpenPastTheirETA.add(orderId);
					} else if ("Y".equals(filterPartRec)
							&& orh.getPartiallyReceivedItems()) {
						filterPartiallyReceivedPOs.add(orderId);
					}
				}
			}
		}

		result.put("filterInventoryProblemsList", filterInventoryProblems);
		result.put("filterPOsWithRejectedItemsList", filterPOsWithRejectedItems);
		result.put("filterPOsOpenPastTheirETAList", filterPOsOpenPastTheirETA);
		result.put("filterPartiallyReceivedPOsList", filterPartiallyReceivedPOs);
	}

	protected static EntityExpr makeExpr(String fieldName, String value) {
		return makeExpr(fieldName, value, false);
	}

	protected static EntityExpr makeExpr(String fieldName, String value,
			boolean forceLike) {
		EntityComparisonOperator<?, ?> op = forceLike ? EntityOperator.LIKE
				: EntityOperator.EQUALS;

		if (value.startsWith("*")) {
			op = EntityOperator.LIKE;
			value = "%" + value.substring(1);
		} else if (value.startsWith("%")) {
			op = EntityOperator.LIKE;
		}

		if (value.endsWith("*")) {
			op = EntityOperator.LIKE;
			value = value.substring(0, value.length() - 1) + "%";
		} else if (value.endsWith("%")) {
			op = EntityOperator.LIKE;
		}

		if (forceLike) {
			if (!value.startsWith("%")) {
				value = "%" + value;
			}
			if (!value.endsWith("%")) {
				value = value + "%";
			}
		}

		return EntityCondition.makeCondition(fieldName, op, value);
	}
	
	/**
	 * 订单统计列表查询	add by qianjin 2016.08.17
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getOrderStatisticsList(DispatchContext dctx,Map<String, ? extends Object> context) {
		Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map<String, Object>> recordsList = FastList.newInstance();
        
        //总记录数
        int totalSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));
        
        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));
        
        lowIndex = viewIndex * viewSize;
        highIndex = (viewIndex + 1) * viewSize;
        
        //源生SQL
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        
        String where_sql = "";
        //开始时间
        if (UtilValidate.isNotEmpty(context.get("startTime"))) {
        	where_sql += "and oh.created_stamp >='"+context.get("startTime")+"' ";
        }
        
        //结束时间
        if (UtilValidate.isNotEmpty(context.get("endTime"))) {
        	where_sql += "and oh.created_stamp <='"+context.get("endTime")+"' ";
        }
        
        //按社区精确查询
        if (UtilValidate.isNotEmpty(context.get("communityId"))) {
        	where_sql += "and c.community_id ='"+context.get("communityId")+"' ";
        }
        
        String group_sql = "";
        //如果分组不为空，并且等于商品
        if(UtilValidate.isNotEmpty(context.get("groupBy")) && "product".equals(context.get("groupBy"))){
        	group_sql = "group by PRODUCT_ID ";
        }else if(UtilValidate.isNotEmpty(context.get("groupBy")) && "business".equals(context.get("groupBy"))){
        	group_sql = "group by PARTY_ID ";
        }
        String limit_sql = "limit "+viewIndex * viewSize+","+viewSize+" ";
        String sql = "select "+
        		"pb.PARTY_ID,pb.BUSINESS_NAME,"+
        		"p.PRODUCT_ID,p.PRODUCT_NAME,"+
        		"oh.ORDER_ID,sum(oh.ACTUAL_PAY_MONEY) as sales_money,"+
        		"count(oh.ORDER_ID) as order_num,"+
        		"SUM(oi.QUANTITY) as product_num,"+
        		"group_concat(DISTINCT c.`NAME`) as communityName "+
        		"from party_business pb "+
        		"join product p on pb.PARTY_ID = p.MERCHANT_NAME "+
        		"join order_item oi on p.PRODUCT_ID = oi.PRODUCT_ID "+
        		"join order_header oh on oi.ORDER_ID = oh.ORDER_ID "+
        		"join order_contact_mech ocm on oh.ORDER_ID = ocm.ORDER_ID "+
        		"join postal_address pa on ocm.CONTACT_MECH_ID = pa.CONTACT_MECH_ID "+
        		"join community c on pa.COMMUNITY_ID = c.COMMUNITY_ID "+
        		"where p.PRODUCT_TYPE_ID = 'FINISHED_GOOD' "+
        		"and oh.STATUS_ID in ('ORDER_COMPLETED','ORDER_WAITEVALUATE') ";
        if(UtilValidate.isNotEmpty(where_sql)){
        	sql += where_sql;
        }
        if(UtilValidate.isNotEmpty(group_sql)){
        	sql += group_sql;
        }
        String count_sql = "select count(*) as total_size from ("+sql+") a ";
        sql += limit_sql;		
        
        try {
            sqlP.prepareStatement(count_sql);
            ResultSet rs = sqlP.executeQuery();
            while (rs.next()) {
            	totalSize = rs.getInt("total_size");
            }
            rs.close();
            
            sqlP.prepareStatement(sql);
            ResultSet rs_1 = sqlP.executeQuery();
            recordsList = getListFromResultSet(rs_1);
            rs_1.close();
        }catch (Exception e) {
			e.printStackTrace();
		}
        
        //返回的参数
        result.put("recordsList",recordsList);
        result.put("totalSize", Integer.valueOf(totalSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex+1));
        
        return result;
	}

	/**
	 * 订单统计导出 add by qianjin 2016.08.17
	 * @param request
	 * @param response
	 * @return
	 */
	public static String exportOrderStatistics(HttpServletRequest request, HttpServletResponse response) {
    	request.getSession().removeAttribute("exportOrderListStatus");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        //源生SQL
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        
        String where_sql = "";
        //开始时间
        if (UtilValidate.isNotEmpty(request.getParameter("startTime"))) {
        	where_sql += "and oh.created_stamp >='"+request.getParameter("startTime")+"' ";
        }
        
        //结束时间
        if (UtilValidate.isNotEmpty(request.getParameter("endTime"))) {
        	where_sql += "and oh.created_stamp <='"+request.getParameter("endTime")+"' ";
        }
        
        //按社区精确查询
        if (UtilValidate.isNotEmpty(request.getParameter("communityId"))) {
        	where_sql += "and c.community_id ='"+request.getParameter("communityId")+"' ";
        }
        
        String group_sql = "group by c.COMMUNITY_ID,";
        //如果分组不为空，并且等于商品
        if(UtilValidate.isNotEmpty(request.getParameter("groupBy")) && "product".equals(request.getParameter("groupBy"))){
        	group_sql += "PRODUCT_ID ";
        }else if(UtilValidate.isNotEmpty(request.getParameter("groupBy")) && "business".equals(request.getParameter("groupBy"))){
        	group_sql += "PARTY_ID ";
        }
        String sql = "select "+
        		"c.COMMUNITY_ID,c.`NAME` as communityName,";
        if(UtilValidate.isNotEmpty(request.getParameter("groupBy")) && "product".equals(request.getParameter("groupBy"))){
        	sql += "p.PRODUCT_ID,p.PRODUCT_NAME,"+
        			"SUM(oi.QUANTITY) as product_num,"+
        			"sum(QUANTITY * UNIT_PRICE) as productPrice ";
        }else if(UtilValidate.isNotEmpty(request.getParameter("groupBy")) && "business".equals(request.getParameter("groupBy"))){
        	sql += "pb.PARTY_ID,pb.BUSINESS_NAME,"+
         		   "sum(oh.ACTUAL_PAY_MONEY) as sales_money,"+
         		   "count(oh.ORDER_ID) as order_num,"+
            		"SUM(oi.QUANTITY) as product_num ";
        }
        	sql += "from party_business pb "+
        		"join product p on pb.PARTY_ID = p.MERCHANT_NAME "+
        		"join order_item oi on p.PRODUCT_ID = oi.PRODUCT_ID "+
        		"join order_header oh on oi.ORDER_ID = oh.ORDER_ID "+
        		"join order_contact_mech ocm on oh.ORDER_ID = ocm.ORDER_ID "+
        		"join postal_address pa on ocm.CONTACT_MECH_ID = pa.CONTACT_MECH_ID "+
        		"join community c on pa.COMMUNITY_ID = c.COMMUNITY_ID "+
        		"where p.PRODUCT_TYPE_ID = 'FINISHED_GOOD' "+
        		"and oh.STATUS_ID in ('ORDER_COMPLETED','ORDER_WAITEVALUATE') ";
        if(UtilValidate.isNotEmpty(where_sql)){
        	sql += where_sql;
        }
        if(UtilValidate.isNotEmpty(group_sql)){
        	sql += group_sql;
        }
        
        List<Object[]> recordsList = FastList.newInstance();
        try {
            sqlP.prepareStatement(sql);
            ResultSet rs = sqlP.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
            	Object[] obj = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                	Object o = rs.getObject(i);
                	if("product_num".equals(md.getColumnName(i)) || "productPrice".equals(md.getColumnName(i))){
                		o = UtilMisc.doubleTrans(o);
                	}
                	obj[i-1] = o;
                }
                recordsList.add(obj);
            }
            rs.close();
        }catch (Exception e) {
			e.printStackTrace();
		}
        String fileName = "";
        String title = "";
        String[] rowName = null;
        String startTime = "";
        String endTime = "";
        
        //如果分组不为空，并且等于商品
        if(UtilValidate.isNotEmpty(request.getParameter("groupBy")) && "product".equals(request.getParameter("groupBy"))){
        	fileName = "订单统计报表(按商品统计)_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
        	title = "按商品统计";
        	rowName = new String[]{"","销售数量","销售总金额"};
        }else if(UtilValidate.isNotEmpty(request.getParameter("groupBy")) && "business".equals(request.getParameter("groupBy"))){
        	fileName = "订单统计报表(按商家统计)_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
        	title = "按商家统计";
        	rowName = new String[]{"","销售金额","订单总数","商品总数"};
        }
        
        //开始时间
        if (UtilValidate.isNotEmpty(request.getParameter("startTime"))) {
        	startTime = request.getParameter("startTime");
        }
        
        //结束时间
        if (UtilValidate.isNotEmpty(request.getParameter("endTime"))) {
        	endTime = request.getParameter("endTime");
        }
        
        try {
        	Map paramMap = FastMap.newInstance();
        	paramMap.put("startTime", startTime);
        	paramMap.put("endTime", endTime);
//        	ExcelExport ex = new ExcelExport(response,fileName,title,rowName,recordsList,paramMap);
//        	ex.export();
		} catch (Exception e) {
			e.printStackTrace();
		}
        request.getSession().setAttribute("exportOrderListStatus", "success");
        return "success";
	}
	
	/**
     * 设置session中的某项参数值 add by qianjin 2016.07.12
     * @param request
     * @param response
     * @return
     */
    public static String updateSessionForParam(HttpServletRequest request, HttpServletResponse response) {
    	String attrName = request.getParameter("attrName");
    	String attrVal = request.getParameter("attrVal");
    	request.getSession().setAttribute(attrName,attrVal);
    	return "success";
    }
    
    /**
     * 获取session中的某项参数值 add by qianjin 2016.07.12
     * @param request
     * @param response
     * @return
     */
    public static String getSessionForParam(HttpServletRequest request, HttpServletResponse response) {
    	String attrName = request.getParameter("attrName");
    	request.setAttribute("attrVal", request.getSession().getAttribute(attrName));
    	return "success";
    }
	
	/**
	 * ResultSet转换为List<Map<String, Object>> add by qianjin 2016.08.17
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> getListFromResultSet(ResultSet rs) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> rowData = new HashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData);
        }
        return list;
    }
}
