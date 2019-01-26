package org.ofbiz.order.ticket;

import javolution.util.FastList;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by changsy on 16/2/1.
 */
public class TicketService {
    public static final String module = TicketService.class.getName();

    /**
     * 核销验证码
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> dealTicketValidate(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String ticketNo = (String) context.get("ticketNo");
        String ticketNos = (String) context.get("ticketNos");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();    //dispatcher对象
        GenericValue ticket = null;
        String orderId = "";

        String partyBusinessId = null;
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        if (userLogin != null) {
            String partyId = (String) userLogin.get("partyId");
            GenericValue partyBusiness = null;
            try {
                partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", partyId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
                ServiceUtil.returnError(e.getMessage());
            }
            if (partyBusiness != null) {
                partyBusinessId = (String) partyBusiness.get("partyId");
            }
        }
        try {
            if (UtilValidate.isNotEmpty(ticketNo)) {
                List<GenericValue> tickets = null;
                if (UtilValidate.isEmpty(partyBusinessId)) {
                    tickets = delegator.findByAnd("Ticket", UtilMisc.toMap("ticketNo", ticketNo));
                } else {
                    DynamicViewEntity dynamicView = new DynamicViewEntity();

                    // define the main condition & expression list
                    EntityCondition mainCond = null;
                    List<String> fieldsToSelect = FastList.newInstance();

                    // default view settings
                    dynamicView.addMemberEntity("TK", "Ticket");
                    dynamicView.addAlias("TK", "ticketStatus");
                    dynamicView.addAlias("TK", "ticketId");
                    dynamicView.addAlias("TK", "partyId");
                    dynamicView.addAlias("TK", "productId");
                    dynamicView.addAlias("TK", "orderId");
                    dynamicView.addAlias("TK", "ticketNo");
                    fieldsToSelect.add("ticketId");
                    fieldsToSelect.add("ticketStatus");
                    fieldsToSelect.add("ticketNo");
                    fieldsToSelect.add("orderId");


                    //商品名称
                    dynamicView.addMemberEntity("PT", "Product");
                    dynamicView.addAlias("PT", "productName");
                    dynamicView.addAlias("PT", "productId");
                    dynamicView.addAlias("PT", "businessPartyId");
                    dynamicView.addViewLink("TK", "PT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));
                    fieldsToSelect.add("businessPartyId");

                    fieldsToSelect.add("productName");
                    fieldsToSelect.add("productId");

                    //商家名称
                    dynamicView.addMemberEntity("PB", "PartyBusiness");
                    dynamicView.addAlias("PB", "businessName");
                    dynamicView.addAlias("PB", "partyId");
                    dynamicView.addViewLink("PT", "PB", Boolean.FALSE, ModelKeyMap.makeKeyMapList("businessPartyId", "partyId"));

                    // filter on Name
                    if (UtilValidate.isNotEmpty(partyBusinessId)) {
                        List<EntityCondition> listcond = FastList.newInstance();
                        listcond.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("businessPartyId"), EntityOperator.EQUALS, partyBusinessId));
                        listcond.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("ticketNo"), EntityOperator.EQUALS, ticketNo));
                        mainCond = EntityCondition.makeCondition(listcond, EntityOperator.AND);
                    }else{
                        mainCond = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("ticketNo"), EntityOperator.EQUALS, ticketNo);
                    }

                    //去除重复数据
                    EntityFindOptions findOpts = new EntityFindOptions();
                    findOpts.setResultSetType(EntityFindOptions.TYPE_SCROLL_INSENSITIVE);
                    findOpts.setDistinct(true);
                    findOpts.setSpecifyTypeAndConcur(true);
                    
                    EntityListIterator ticketIter = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, findOpts);
                    tickets = ticketIter.getCompleteList();
                    ticketIter.close();
                }

                if (UtilValidate.isNotEmpty(tickets)) {
                    ticket = tickets.get(0);
                    String ticketStatus = (String) ticket.get("ticketStatus");
                    if (UtilValidate.isEmpty(ticketStatus)) {
                        result.put("dealResult", "notFind");
                    } else if ("hasUsed".equals(ticketStatus)) {
                        result.put("dealResult", "hasUsed");
                    } else if ("notAudited".equals(ticketStatus)) {
                        result.put("dealResult", "notAudited");
                    } else if ("notRefunded".equals(ticketStatus)) {
                        result.put("dealResult", "notRefunded");
                    } else if ("hasRefuned".equals(ticketStatus)) {
                        result.put("dealResult", "hasRefuned");
                    } else if ("expired".equals(ticketStatus)) {
                        result.put("dealResult", "expired");
                    } else if ("notUsed".equals(ticketStatus) || "rejectApplication".equals(ticketStatus)) {
                        String ticketId = (String) ticket.get("ticketId");
                        GenericValue ticket1 = delegator.findByPrimaryKey("Ticket", UtilMisc.toMap("ticketId", ticketId));
                        ticket1.set("ticketStatus", "hasUsed");
                        ticket1.set("useDate", UtilDateTime.nowTimestamp());
                        delegator.store(ticket1);
                        result.put("dealResult", "success");
                        try {
                            //调用虚拟订单的积分保存方法
                            Map map = dispatcher.runSync("saveVirtualOrderIntegral", UtilMisc.toMap("orderId", ticket1.get("orderId"),
                                    "ticketList", UtilMisc.toList(ticketId)
                            ));
                        } catch (GenericServiceException e) {
                            e.printStackTrace();
                        }
                    }
                    result.put("ticket", delegator.findByPrimaryKey("Ticket",UtilMisc.toMap("ticketId",ticket.get("ticketId"))));
                    orderId = (String) ticket.get("orderId");
                    List<EntityCondition> mainCond = FastList.newInstance();
                    EntityCondition whereCond = EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId);
                    mainCond.add(whereCond);
                    EntityCondition ticketCond = EntityCondition.makeCondition("ticketNo", EntityOperator.NOT_EQUAL, ticketNo);
                    mainCond.add(ticketCond);

                    List<EntityCondition> statusCond = FastList.newInstance();
                    EntityCondition notUsedCond = EntityCondition.makeCondition("ticketStatus", EntityOperator.EQUALS, "notUsed");
                    EntityCondition rejectApplicationCond = EntityCondition.makeCondition("ticketStatus", EntityOperator.EQUALS, "rejectApplication");
                    statusCond.add(notUsedCond);
                    statusCond.add(rejectApplicationCond);
                    EntityCondition statusConditions = EntityCondition.makeCondition(statusCond, EntityOperator.OR);

                    mainCond.add(statusConditions);

                    EntityListIterator ticketIter = delegator.find("Ticket", EntityCondition.makeCondition(mainCond, EntityOperator.AND), null, null, null, null);
                    result.put("ticketList", ticketIter.getCompleteList());
                    ticketIter.close();

                } else {
                    ticket = delegator.makeValue("Ticket");
                    ticket.set("ticketNo", ticketNo);
                    result.put("ticket", ticket);
                    result.put("dealResult", "notFind");
                    return result;
                }
            }

            //处理多个ticket验证

            if (UtilValidate.isNotEmpty(ticketNos)) {
                List<String> ticketNoList = StringUtil.split(ticketNos, ",");
                List<GenericValue> dealTickets = FastList.newInstance();
                if (UtilValidate.isNotEmpty(ticketNoList)) {
                    for (int i = 0; i < ticketNoList.size(); i++) {
                        String ticketNo1 = ticketNoList.get(i);
                        List<GenericValue> tickets = delegator.findByAnd("Ticket", UtilMisc.toMap("ticketNo", ticketNo1));
                        if (UtilValidate.isNotEmpty(tickets)) {
                            GenericValue ticket1 = tickets.get(0);
                            orderId = (String) ticket1.get("orderId");
                            ticket1.set("ticketStatus", "hasUsed");
                            ticket1.set("useDate", UtilDateTime.nowTimestamp());
                            dealTickets.add(ticket1);
                        }
                    }
                    delegator.storeAll(dealTickets);
                    try {
                        //调用虚拟订单的积分保存方法
                        Map map = dispatcher.runSync("saveVirtualOrderIntegral", UtilMisc.toMap("orderId", orderId, "ticketList", ticketNoList));
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                    }
                }

                result.put("dealAllResult", "success");
            }

            //  判断当前订单的券是否全部“已使用”或“已退款”，全部“已使用”或“已退款”的情况下判断订单是否已评价；同时符合这两个条件时
            if (UtilValidate.isNotEmpty(orderId)) {
                List<EntityCondition> AndCondList = FastList.newInstance();
                AndCondList.add(EntityCondition.makeCondition("ticketStatus", EntityOperator.NOT_EQUAL, "hasUsed"));
                AndCondList.add(EntityCondition.makeCondition("ticketStatus", EntityOperator.NOT_EQUAL, "hasRefuned"));
                AndCondList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
                EntityCondition andCond = EntityCondition.makeCondition(AndCondList, EntityOperator.AND);
                long count = delegator.findCountByCondition("Ticket", andCond, null, null);
                boolean canDeal = true;
                if (count > 0) {
                    canDeal = false;
                }
                long reviewCount = delegator.findCountByCondition("ProductReview", EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId), null, null);
                if (reviewCount == 0) {
                    canDeal = false;
//                        canDeal = true;
                }
                if (canDeal) {
                    //修改订单状态已完成
                    LocalDispatcher localDispatcher = dcx.getDispatcher();
                    Map serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_COMPLETED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
                    try {
                        localDispatcher.runSync("changeOrderStatus", serviceContext);
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                        ServiceUtil.returnError(e.getMessage());
                    }
                }
            }

        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
}

    /**
     * 核销验查询
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> queryTicket(DispatchContext dct, Map<String, ? extends Object> context) {

        Delegator delegator = dct.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        String userName = (String) context.get("userName");
        String partyName = (String) context.get("partyName");
        String mobile = (String) context.get("mobile");
        String orderId = (String) context.get("orderId");
        String productName = (String) context.get("productName");
        String ticketNo = (String) context.get("ticketNo");
        String status = (String) context.get("status");
        String partyBusinessId = (String) context.get("partyBusinessId");
        String isPartyBusiness = (String) context.get("isPartyBusiness");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        if (userLogin != null) {
            String partyId = (String) userLogin.get("partyId");
            GenericValue partyBusiness = null;
            try {
                partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", partyId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
                ServiceUtil.returnError(e.getMessage());
            }
            if (partyBusiness != null) {
                isPartyBusiness = "Y";
                partyBusinessId = partyId;
            } else {
                isPartyBusiness = "N";
            }
        }

        List<GenericValue> ticketList = FastList.newInstance();
        int ticketListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // blank param list
        String paramList = "";


        DynamicViewEntity dynamicView = new DynamicViewEntity();

        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        List<String> fieldsToSelect = FastList.newInstance();

        // default view settings
        dynamicView.addMemberEntity("TK", "Ticket");
        dynamicView.addAlias("TK", "ticketStatus");
        dynamicView.addAlias("TK", "ticketId");
        dynamicView.addAlias("TK", "partyId");
        dynamicView.addAlias("TK", "productId");
        dynamicView.addAlias("TK", "orderId");
        dynamicView.addAlias("TK", "ticketNo");
        fieldsToSelect.add("ticketId");
        fieldsToSelect.add("ticketStatus");
        fieldsToSelect.add("ticketNo");
        fieldsToSelect.add("orderId");


        // ----
        // Person Fields
        // ----

        // modify the dynamic view
        //用户名,手机号码
        dynamicView.addMemberEntity("PE", "Person");
        dynamicView.addViewLink("TK", "PE", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));
        dynamicView.addAlias("PE", "name");
        dynamicView.addAlias("PE", "mobile");
        fieldsToSelect.add("name");
        fieldsToSelect.add("mobile");

        //商品名称
        dynamicView.addMemberEntity("PT", "Product");
        dynamicView.addAlias("PT", "productName");
        dynamicView.addAlias("PT", "businessPartyId");
        dynamicView.addAlias("PT", "productId");
        dynamicView.addViewLink("TK", "PT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));


        fieldsToSelect.add("productName");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("businessPartyId");
        //商家名称
        dynamicView.addMemberEntity("PB", "PartyBusiness");
        dynamicView.addAlias("PB", "businessName");
        dynamicView.addAlias("PB", "partyId");
        dynamicView.addViewLink("PT", "PB", Boolean.FALSE, ModelKeyMap.makeKeyMapList("businessPartyId", "partyId"));
        fieldsToSelect.add("businessName");
        fieldsToSelect.add("businessPartyId");
//        退货申请
       /* dynamicView.addMemberEntity("RI", "ReturnItem");
        dynamicView.addAlias("RI", "examinePassTime");
        dynamicView.addAlias("RI", "applyTime");
        dynamicView.addAlias("RI", "completeTime");
        dynamicView.addViewLink("TK", "RI", Boolean.TRUE, ModelKeyMap.makeKeyMapList("orderId"));
        fieldsToSelect.add("examinePassTime");
        fieldsToSelect.add("applyTime");
        fieldsToSelect.add("completeTime");*/
        //虚拟商品有效期
        dynamicView.addMemberEntity("PAG", "ProductActivityGoods");
        dynamicView.addAlias("PAG", "virtualProductEndDate");
        dynamicView.addAlias("PAG", "virtualProductStartDate");
        dynamicView.addViewLink("TK", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId"));
        fieldsToSelect.add("virtualProductEndDate");
        fieldsToSelect.add("virtualProductEndDate");

        // filter on Name
        if (UtilValidate.isNotEmpty(isPartyBusiness) && "Y".equals(isPartyBusiness)) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("businessPartyId"), EntityOperator.EQUALS, partyBusinessId));

        } else {

            if (UtilValidate.isNotEmpty(partyName)) {
                paramList = paramList + "&partyName=" + partyName;
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("businessName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + partyName + "%")));
            }
        }

        // filter on Name
        if (UtilValidate.isNotEmpty(userName)) {
            paramList = paramList + "&userName=" + userName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("name"), EntityOperator.LIKE, EntityFunction.UPPER("%" + userName + "%")));
        }
        if (UtilValidate.isNotEmpty(mobile)) {
            paramList = paramList + "&mobile=" + mobile;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("mobile"), EntityOperator.LIKE, EntityFunction.UPPER("%" + mobile + "%")));
        }

        if (UtilValidate.isNotEmpty(orderId)) {
            paramList = paramList + "&orderId=" + orderId;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("orderId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + orderId + "%")));
        }
        if (UtilValidate.isNotEmpty(productName)) {
            paramList = paramList + "&productName=" + productName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + productName + "%")));
        }
        if (UtilValidate.isNotEmpty(ticketNo)) {
            paramList = paramList + "&ticketNo=" + ticketNo;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("ticketNo"), EntityOperator.LIKE, EntityFunction.UPPER("%" + ticketNo + "%")));
        }
        if (UtilValidate.isNotEmpty(status)) {
            paramList = paramList + "&status=" + status;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("ticketStatus"), EntityOperator.EQUALS, status));
        }
        // build the main condition
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }


        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                //去除重复数据
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                //查询的数据Iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
                // 获取分页所需的记录集合
                ticketList = pli.getPartialList(lowIndex, viewSize);

                // 获取总记录数
                ticketListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > ticketListSize) {
                    highIndex = ticketListSize;
                }

                // attempt to get the full size
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(e.getMessage());
            }
        } else {
            ticketListSize = 0;
        }

        result.put("ticketList", ticketList);
        result.put("partyName", partyName);
        result.put("userName", userName);
        result.put("mobile", mobile);
        result.put("productName", productName);
        result.put("status", status);
        result.put("ticketNo", ticketNo);
        result.put("ticketListSize", Integer.valueOf(ticketListSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    /**
     * 核销验查询
     *
     * @param delegator
     * @param ids
     * @return
     */
    public static EntityListIterator getEntityListIterator(Delegator delegator, String ids) throws GenericEntityException {
        List listIds = StringUtil.split(ids, ",");
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        // define the main condition & expression list
        EntityCondition mainCond = null;
        List<String> fieldsToSelect = FastList.newInstance();

        // default view settings
        dynamicView.addMemberEntity("TK", "Ticket");
        dynamicView.addAlias("TK", "ticketStatus");
        dynamicView.addAlias("TK", "ticketId");
        dynamicView.addAlias("TK", "partyId");
        dynamicView.addAlias("TK", "productId");
        dynamicView.addAlias("TK", "orderId");
        dynamicView.addAlias("TK", "ticketNo");
        fieldsToSelect.add("ticketId");
        fieldsToSelect.add("ticketStatus");
        fieldsToSelect.add("ticketNo");
        fieldsToSelect.add("orderId");


        // ----
        // Person Fields
        // ----

        // modify the dynamic view
        //用户名,手机号码
        dynamicView.addMemberEntity("PE", "Person");
        dynamicView.addViewLink("TK", "PE", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));
        dynamicView.addAlias("PE", "name");
        dynamicView.addAlias("PE", "mobile");
        fieldsToSelect.add("name");
        fieldsToSelect.add("mobile");

        //商品名称
        dynamicView.addMemberEntity("PT", "Product");
        dynamicView.addAlias("PT", "productName");
        dynamicView.addAlias("PT", "productId");
        dynamicView.addViewLink("TK", "PT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));


        fieldsToSelect.add("productName");
        fieldsToSelect.add("productId");

        //商家名称
        dynamicView.addMemberEntity("PB", "PartyBusiness");
        dynamicView.addAlias("PB", "businessName");
        dynamicView.addAlias("PB", "partyId");
        dynamicView.addViewLink("TK", "PB", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
        fieldsToSelect.add("businessName");

//        退货申请
       /* dynamicView.addMemberEntity("RI", "ReturnItem");
        dynamicView.addAlias("RI", "examinePassTime");
        dynamicView.addAlias("RI", "applyTime");
        dynamicView.addAlias("RI", "completeTime");
        dynamicView.addViewLink("TK", "RI", Boolean.TRUE, ModelKeyMap.makeKeyMapList("orderId"));
        fieldsToSelect.add("examinePassTime");
        fieldsToSelect.add("applyTime");
        fieldsToSelect.add("completeTime");*/
        //虚拟商品有效期
        dynamicView.addMemberEntity("PAG", "ProductActivityGoods");
        dynamicView.addAlias("PAG", "virtualProductEndDate");
        dynamicView.addAlias("PAG", "virtualProductStartDate");
        dynamicView.addViewLink("TK", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId"));
        fieldsToSelect.add("virtualProductEndDate");
        fieldsToSelect.add("virtualProductStartDate");

        // filter on Name
        if (UtilValidate.isNotEmpty(ids)) {
            mainCond = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("ticketId"), EntityOperator.IN, listIds);
        }

        //去除重复数据
        EntityFindOptions findOpts = new EntityFindOptions();
        findOpts.setResultSetType(EntityFindOptions.TYPE_SCROLL_INSENSITIVE);
        findOpts.setDistinct(true);
        findOpts.setSpecifyTypeAndConcur(true);
        //查询的数据Iterator
        return delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, findOpts);


    }

    public static List<GenericValue> listTickets(final Delegator delegator, final String ids) throws GenericEntityException {
        return TransactionUtil.doTransaction(new Callable<List<GenericValue>>() {
            public List<GenericValue> call() throws Exception {
                EntityListIterator it = null;
                try {
                    it = getEntityListIterator(delegator, ids);
                    return it.getCompleteList();
                } finally {
                    if (it != null) {
                        it.close();
                    }
                }
            }
        }, "sql select", 0, true);
    }
}
