package com.yuaoq.yabiz.app.mobile.microservice.party.api.v1;

import com.google.gson.Gson;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.services.kdmall.KdRetData;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
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
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.order.CouponServices;
import org.ofbiz.order.order.OrderReadHelper;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

/**
 * Created by changchen on 2018/4/18.
 */


@RestController
@RequestMapping(path = "/api/party/v1")
public class PartyControllerV1 {
    public static final String module = PartyControllerV1.class.getName();
    
    /**
     * 用户信息
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    @RequestMapping(value = "/partyInfo", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> userLogin(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token, String mall_id) {
        //获取登录用户信息
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        
        Map<String, Object> resultData = FastMap.newInstance();
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
            String partyId = "";
            if (UtilValidate.isNotEmpty(userLogin)) {
                partyId = userLogin.getString("partyId");
            }
            String lastToken = userLogin.getString("lastToken");
            if (UtilValidate.isEmpty(lastToken)) lastToken = "test";
            if (UtilValidate.isNotEmpty(userLogin)) {
                if (UtilValidate.isNotEmpty(partyId)) {
                    GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
                    String member_id = person.getString("member_id");
                    String unionId = person.getString("unionId");
                    String phone = person.getString("mobile");
                    String gender = person.getString("gender");
                    String kaidetoken = "";
                    String sex = "";
                    if (UtilValidate.isNotEmpty(gender) && gender.equals("M")) {
                        sex = "1";
                    } else {
                        sex = "2";
                    }
                    String nickName = person.getString("nickname");
                    String headPhoto = person.getString("headphoto");
                    resultData.put("nickname", nickName);
                    resultData.put("headphoto", headPhoto);
                    
                    Map<String, Object> result03 = dispatcher.runSync("kaide-getMemberIdByUnionId", UtilMisc.toMap("unionid", unionId));
                    if (ServiceUtil.isError(result03)) {
                        resultData.put("retCode", 0);
                        resultData.put("message", "根据unionid获取获取token和memberID信息错误");
                    }
                    KdRetData retData = new Gson().fromJson((String) result03.get("result"), KdRetData.class);
                    if (retData.getResult().equalsIgnoreCase("22001")) {
                        //获取成功
                        // 取得token 和 member_id的值
                        member_id = (String) retData.getData().get("memberID");
                        kaidetoken = (String) retData.getData().get("token");
                    }
                    Map<String, Object> resultData1 = dispatcher.runSync("kaide-userGetScore", UtilMisc.toMap("member_id", member_id, "token", kaidetoken, "mall_id", mall_id));
                    if (ServiceUtil.isError(resultData1)) {
                        Debug.logError(ServiceUtil.getErrorMessage(resultData1), module);
                    } else {
                        String scoreResult = (String) resultData1.get("result");
                        Map scoreMap = new Gson().fromJson(scoreResult, Map.class);
                        if (UtilValidate.isNotEmpty(scoreMap) && UtilValidate.isNotEmpty(scoreMap.get("data"))) {
                            resultData.put("score", scoreMap.get("data"));
                        } else {
                            resultData.put("score", "0");
                        }
                    }
                    
                    resultData.put("retCode", "1");
                    resultData.put("message", "用户信息查询成功");
                    if (UtilValidate.isNotEmpty(member_id)) {
                        resultData.put("member_id", member_id);
                    } else {
                        resultData.put("score", ">0<");
                    }
                    
                    resultData.put("mobile", phone);
                    
                }
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "查询用户信息失败");
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "查询用户信息失败");
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 用户信息
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/partyInfoByMemberId", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> partyInfoByMemberId(HttpServletRequest request, HttpServletResponse response) {
        //获取登录用户信息
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String memberId = request.getParameter("member_id");
        Map<String, Object> resultData = FastMap.newInstance();
        try {
            
            List<GenericValue> persons = delegator.findByAnd("Person", UtilMisc.toMap("member_id", memberId));
            if (UtilValidate.isNotEmpty(persons)) {
                GenericValue person = persons.get(0);
                resultData.put("retCode", "1");
                resultData.put("message", "用户信息查询成功");
                resultData.put("person", person);
            } else {
                resultData.put("retCode", 0);
                resultData.put("message", "查询用户信息失败");
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "查询用户信息失败");
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    @RequestMapping(value = "/coupon/my", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> partyCoupon(HttpServletRequest request, String status, HttpServletResponse response, JwtAuthenticationToken token) throws ParseException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        
        //已领取、已使用、已过期
        boolean beganTransaction = false;
        EntityListIterator pli = null;
        List<Map> couponMapList = FastList.newInstance();
        try {
            
            String loginName = ((UserContext) token.getPrincipal()).getUsername();
            Delegator delegator = (Delegator) request.getAttribute("delegator");
            
            String partyId = CommonUtils.getPartyId(delegator, loginName);
            
            beganTransaction = TransactionUtil.begin();
            
            
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PPCP", "ProductPromoCodeParty");
            dynamicView.addAlias("PPCP", "productPromoCodeId");
            dynamicView.addAlias("PPCP", "partyId");
            dynamicView.addAlias("PPCP", "getDate");
            dynamicView.addAlias("PPCP", "useDate");
            
            dynamicView.addMemberEntity("PPC", "ProductPromoCode");
            dynamicView.addAlias("PPC", "productPromoCodeId");
            dynamicView.addAlias("PPC", "promoCodeStatus");
            dynamicView.addAlias("PPC", "couponCode");
            dynamicView.addAlias("PPC", "fromDate");
            dynamicView.addAlias("PPC", "thruDate");
            dynamicView.addViewLink("PPCP", "PPC", false, ModelKeyMap.makeKeyMapList("productPromoCodeId", "productPromoCodeId"));
            
            dynamicView.addMemberEntity("PPCN", "ProductPromoCoupon");
            dynamicView.addAlias("PPCN", "couponCode");
            dynamicView.addAlias("PPCN", "couponName");
            dynamicView.addAlias("PPCN", "couponType");
            dynamicView.addAlias("PPCN", "payFill");
            dynamicView.addAlias("PPCN", "payReduce");
            dynamicView.addAlias("PPCN", "applyScope");
            dynamicView.addAlias("PPCN", "couponProductType");
            dynamicView.addViewLink("PPC", "PPCN", false, ModelKeyMap.makeKeyMapList("couponCode", "couponCode"));
            
            dynamicView.addMemberEntity("PSCA", "ProductStoreCouponAppl");
            dynamicView.addAlias("PSCA", "couponCode");
            dynamicView.addAlias("PSCA", "productStoreId");
            dynamicView.addViewLink("PPCN", "PSCA", false, ModelKeyMap.makeKeyMapList("couponCode", "couponCode"));
            
            dynamicView.addMemberEntity("PG", "PartyGroup");
            dynamicView.addAlias("PG", "productStoreId");
            dynamicView.addAlias("PG", "isInner");
            dynamicView.addViewLink("PSCA", "PG", false, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));
            
            dynamicView.addMemberEntity("PS", "ProductStore");
            dynamicView.addAlias("PS", "storeName");
            dynamicView.addAlias("PS", "productStoreId");
            dynamicView.addViewLink("PSCA", "PS", false, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));
            
            List<EntityCondition> filedExprs = FastList.newInstance();
            if (UtilValidate.isNotEmpty(status)) {
            
            }
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyId"), EntityOperator.EQUALS, partyId));
            List<String> statusList = FastList.newInstance();
            statusList.add("G");
            statusList.add("D");
            if ("G".equalsIgnoreCase(status)) {
                //领取未使用，未过期的
                filedExprs.add(EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, new Timestamp(System.currentTimeMillis())));
                filedExprs.add(EntityCondition.makeCondition("promoCodeStatus", EntityOperator.IN, statusList));
            } else if ("D".equalsIgnoreCase(status)) {
                //已过期的
                filedExprs.add(EntityCondition.makeCondition("thruDate", EntityOperator.LESS_THAN, new Timestamp(System.currentTimeMillis())));
                filedExprs.add(EntityCondition.makeCondition("promoCodeStatus", EntityOperator.IN, statusList));
            } else if ("U".equalsIgnoreCase(status)) {
                filedExprs.add(EntityCondition.makeCondition("promoCodeStatus", EntityOperator.EQUALS, status));
            }
            EntityCondition mainCond = null;
            if (filedExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
            }
            
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, null, null);
            
            List<GenericValue> couponList = pli.getCompleteList();
            
            
            if (UtilValidate.isNotEmpty(couponList)) {
                for (GenericValue coupon : couponList) {
                    Map couponMap = coupon.toMap();
                    couponMap.put("useBeginDate", CommonUtils.getStringDate((Timestamp) couponMap.get("fromDate")));
                    couponMap.put("useEndDate", CommonUtils.getStringDate(couponMap.get("thruDate") == null ? null : (Timestamp) couponMap.get("thruDate")));
                    couponMap.put("getDate", CommonUtils.getStringDate((Timestamp) couponMap.get("getDate")));
                    couponMap.put("useDate", CommonUtils.getStringDate(couponMap.get("useDate") == null ? null : (Timestamp) couponMap.get("useDate")));
                    couponMap.put("type", "coupon");
                    couponMapList.add(couponMap);
                }
            }
            
            //获取用户的代金劵，ticket信息
            List<GenericValue> tickets = delegator.findByAnd("TicketAndProduct", UtilMisc.toMap("partyId", partyId));
            
            if ("G".equalsIgnoreCase(status)) {
                //领取未使用，未过期的
                tickets = EntityUtil.filterByAnd(tickets, UtilMisc.toMap("ticketStatus", "notUsed"));
                tickets = EntityUtil.filterByDate(tickets, UtilDateTime.nowTimestamp(), "useStartTime", "useEndTime", true);
            } else if ("D".equalsIgnoreCase(status)) {
                //已过期的
                EntityCondition condition = EntityCondition.makeCondition("useEndTime", EntityOperator.LESS_THAN, UtilDateTime.nowTimestamp());
                tickets = EntityUtil.filterByCondition(tickets, condition);
            } else if ("U".equalsIgnoreCase(status)) {
                tickets = EntityUtil.filterByAnd(tickets, UtilMisc.toMap("ticketStatus", "hasUsed"));
            }
            
            
            if (UtilValidate.isNotEmpty(tickets)) {
                for (int i = 0; i < tickets.size(); i++) {
                    GenericValue ticket = tickets.get(i);
                    
                    Map<String, Object> ticketMap = FastMap.newInstance();
                    ticketMap.put("productPromoCodeId", ticket.getString("ticketId"));
                    ticketMap.put("partyId", partyId);
                    ticketMap.put("getDate", CommonUtils.getStringDate((Timestamp) ticket.getTimestamp("createdStamp")));
                    ticketMap.put("useDate", CommonUtils.getStringDate((Timestamp) ticket.getTimestamp("useDate")));
                    ticketMap.put("isInner", "Y");
//                    G领取未过期,"D":过期 "U"已经使用
                    if (ticket.getString("ticketStatus").equals("hasUsed")) {
                        ticketMap.put("promoCodeStatus", "U");
                    } else if (ticket.getString("ticketStatus").equals("notUsed")) {
                        Timestamp endDate = ticket.getTimestamp("useEndTime");
                        if (endDate.getTime() < UtilDateTime.nowTimestamp().getTime()) {
                            ticketMap.put("promoCodeStatus", "D");
                        } else {
                            ticketMap.put("promoCodeStatus", "G");
                        }
                    }
                    
                    ticketMap.put("couponCode", ticket.getString("ticketNo"));
                    ticketMap.put("fromDate", CommonUtils.getStringDate((Timestamp) ticket.getTimestamp("useStartTime")));
                    ticketMap.put("thruDate", CommonUtils.getStringDate((Timestamp) ticket.getTimestamp("useEndTime")));
                    ticketMap.put("couponName", ticket.getString("productName"));
                    ticketMap.put("couponType", "VIRTUAL_GOOD");
                    ticketMap.put("payFill", ticket.getString(""));
                    ticketMap.put("payReduce", ticket.getString("voucherAmount"));
                    ticketMap.put("applyScope", "A");
                    String productId = ticket.getString("productId");
                    List<GenericValue> products = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_CONF"));
                    if (UtilValidate.isNotEmpty(products) && products.size() > 0) {
                        ticketMap.put("couponProductType", "COUPON_PRT_PART_IN");
                    } else {
                        ticketMap.put("couponProductType", "COUPON_PRT_ALL");
                    }
                    
                    ticketMap.put("useBeginDate", CommonUtils.getStringDate((Timestamp) ticket.getTimestamp("useStartTime")));
                    ticketMap.put("useEndDate", CommonUtils.getStringDate((Timestamp) ticket.getTimestamp("useEndTime")));
                    ticketMap.put("type", "ticket");
                    couponMapList.add(ticketMap);
                }
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (pli != null) {
                try {
                    pli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
            
            }
        }
        //排序
        if ("G".equalsIgnoreCase(status)) {
            //领取未使用，未过期的,未使用代金券列表：按照领取代金券的时间倒序排序；
            for (int i = 0; i < couponMapList.size() - 1; i++) {
                for (int j = 1; j < couponMapList.size() - i; j++) {
                    if (UtilValidate.isNotEmpty(couponMapList.get(j - 1).get("getDate")) && UtilValidate.isNotEmpty(couponMapList.get(j).get("getDate"))) {
                        Date date1 = CommonUtils.getDateFromStr((String) couponMapList.get(j - 1).get("getDate"));
                        Date date2 = CommonUtils.getDateFromStr((String) couponMapList.get(j).get("getDate"));
                        if (date1.getTime() < (date2.getTime())) {
                            Map temp = couponMapList.get(j - 1);
                            couponMapList.set((j - 1), couponMapList.get(j));
                            couponMapList.set(j, temp);
                        }
                    }
                }
            }
            
        } else if ("D".equalsIgnoreCase(status)) {
            //已过期的,已过期代金券列表：按照代金券过期时间倒序排序；
            for (int i = 0; i < couponMapList.size() - 1; i++) {
                for (int j = 1; j < couponMapList.size() - i; j++) {
                    if (UtilValidate.isNotEmpty(couponMapList.get(j - 1).get("useEndDate")) && UtilValidate.isNotEmpty(couponMapList.get(j).get("useEndDate"))) {
                        Date date1 = CommonUtils.getDateFromStr((String) couponMapList.get(j - 1).get("useEndDate"));
                        Date date2 = CommonUtils.getDateFromStr((String) couponMapList.get(j).get("useEndDate"));
                        if (date1.getTime() < (date2.getTime())) {
                            Map temp = couponMapList.get(j - 1);
                            couponMapList.set((j - 1), couponMapList.get(j));
                            couponMapList.set(j, temp);
                        }
                    }
                }
            }
        } else if ("U".equalsIgnoreCase(status)) {
            //已经使用的,已使用代金券列表：按照代金券使用时间倒序排序；
            for (int i = 0; i < couponMapList.size() - 1; i++) {
                for (int j = 1; j < couponMapList.size() - i; j++) {
                    if (UtilValidate.isNotEmpty(couponMapList.get(j - 1).get("useDate")) && UtilValidate.isNotEmpty(couponMapList.get(j).get("useDate"))) {
                        Date date1 = CommonUtils.getDateFromStr((String) couponMapList.get(j - 1).get("useDate"));
                        Date date2 = CommonUtils.getDateFromStr((String) couponMapList.get(j).get("useDate"));
                        if (date1.getTime() < (date2.getTime())) {
                            Map temp = couponMapList.get(j - 1);
                            couponMapList.set((j - 1), couponMapList.get(j));
                            couponMapList.set(j, temp);
                        }
                    }
                }
            }
        }
        resultData.put("coupons", couponMapList);
        resultData.put("retCode", 1);
        
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    
    @RequestMapping(value = "/coupon/Detail", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> couponDetail(HttpServletRequest request, HttpServletResponse response, String productPromoCodeId, JwtAuthenticationToken token) throws GenericEntityException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(productPromoCodeId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "productPromoCodeId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productPromoCodeList = delegator.findByAnd("ProductPromoCodeParty", UtilMisc.toMap("productPromoCodeId", productPromoCodeId));
        if (UtilValidate.isEmpty(productPromoCodeList)) {
            resultData.put("retCode", 0);
            resultData.put("message", "找不到该代金券信息");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        GenericValue productPromoCodeParty = productPromoCodeList.get(0);
        GenericValue promoCode = productPromoCodeParty.getRelatedOne("ProductPromoCode");
        String couponCode = promoCode.getString("couponCode");
        GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
        String promoCouponCode = productPromoCoupon.getString("couponCode");
        List<GenericValue> productStroeAppls = delegator.findByAnd("ProductStoreCouponAppl", UtilMisc.toMap("couponCode", promoCouponCode));
        GenericValue productStroeAppl = null;
        if (UtilValidate.isNotEmpty(productStroeAppls)) {
            productStroeAppl = productStroeAppls.get(0);
        }
        
        String productStroeAppId = productStroeAppl.getString("productStoreId");
        List<GenericValue> partyGroups = delegator.findByAnd("PartyGroup", UtilMisc.toMap("productStoreId", productStroeAppId));
        GenericValue partyGroup = null;
        if (UtilValidate.isNotEmpty(partyGroups)) {
            partyGroup = partyGroups.get(0);
        }
        String productStoreId = partyGroup.getString("productStoreId");
        GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
        
        GenericValue coupon = promoCode.getRelatedOne("ProductPromoCoupon");
        
        Map couponInfo = FastMap.newInstance();
        couponInfo.put("useBeginDate", CommonUtils.getStringDate2(promoCode.getTimestamp("fromDate")));
        couponInfo.put("useEndDate", CommonUtils.getStringDate2(promoCode.getTimestamp("thruDate")));
        couponInfo.put("getDate", CommonUtils.getStringDate2(productPromoCodeParty.getTimestamp("getDate")));
        couponInfo.put("useDate", CommonUtils.getStringDate2(productPromoCodeParty.getTimestamp("useDate")));
        couponInfo.put("productPromoCodeId", productPromoCodeParty.getString("productPromoCodeId"));
        couponInfo.put("couponName", coupon.getString("couponName"));
        couponInfo.put("couponType", coupon.getString("couponType"));
        couponInfo.put("storeName", productStore.getString("storeName"));
        couponInfo.put("isInner", partyGroup.getString("isInner"));
        couponInfo.put("productStoreId", productStore.getString("productStoreId"));
        couponInfo.put("payFill", coupon.getString("payFill"));
        couponInfo.put("payReduce", coupon.getString("payReduce"));
        couponInfo.put("applyScope", coupon.getString("applyScope"));
        couponInfo.put("couponProductType", coupon.getString("couponProductType"));
        
        resultData.put("retCode", 1);
        resultData.put("couponInfo", couponInfo);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    @RequestMapping(value = "/coupon/get", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> partyCouponGet(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String couponCode = request.getParameter("couponCode");
        //全场通用的不用传productStoreId
        String productStoreId = request.getParameter("productStoreId");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
            String partyId = userLogin.getString("partyId");
            if (UtilValidate.isNotEmpty(couponCode)) {
                
                Map<String, Object> resultData1 = CouponServices.validateCouponCodeGetStatus(partyId, couponCode, productStoreId, delegator);
                if (ServiceUtil.isError(resultData1)) {
                    resultData.put("retCode", 0);
                    resultData.put("message", ServiceUtil.getErrorMessage(resultData1));
                    return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
                }
                GenericValue coupon = (GenericValue) resultData1.get("coupon");
                //生成优惠劵code
                //创建一条代金券
                //生成用户优惠劵
                //更新优惠劵Coupon信息
                Map serviceIn = FastMap.newInstance();
                serviceIn.put("couponCode", couponCode);
                serviceIn.put("userLogin", userLogin);
                try {
                    Map<String, Object> retMap = dispatcher.runSync("createOneProductPromoCode", serviceIn);
                    String productPromoCodeId = (String) retMap.get("productPromoCodeId");
                    resultData.put("productPromoCodeId", productPromoCodeId);
                    GenericValue partyCoupon = delegator.makeValue("ProductPromoCodeParty", UtilMisc.toMap("partyId", partyId, "productPromoCodeId", productPromoCodeId, "sourceTypeId", "USER_PROD_GET", "getDate", UtilDateTime.nowTimestamp(), "valueId", productStoreId));
                    partyCoupon.create();
                    GenericValue productPromoCode = delegator.findByPrimaryKey("ProductPromoCode", UtilMisc.toMap("productPromoCodeId", productPromoCodeId));
                    productPromoCode.set("promoCodeStatus", "G");
                    productPromoCode.store();
                    
                } catch (Exception e) {
                    resultData.put("retCode", 0);
                    resultData.put("message", e.getMessage());
                    e.printStackTrace();
                }
                
            } else {
                resultData.put("retCode", 0);
                resultData.put("message", "没有传优惠劵信息");
            }
            
            
            resultData.put("retCode", 1);
            
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
            
            }
        }
        
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 我的拼团列表
     *
     * @param request
     * @param status
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/myOrderGroupList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> partyGroupList(HttpServletRequest request, JwtAuthenticationToken token, String status, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> resultData = FastMap.newInstance();
        int limit = pageSize;
        //LocalDispatcher对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        
        String loginName = ((UserContext) token.getPrincipal()).getUsername();

        //记录集合
        List<Map> recordsList = FastList.newInstance();
        
        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        //每页显示记录条数
        int viewSize = 10;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        
        boolean beganTransaction = false;
        EntityListIterator pli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            String partyId = "";
            if (UtilValidate.isNotEmpty(loginName)) {
                GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
                if (UtilValidate.isNotEmpty(userLogin)) {
                    partyId = userLogin.getString("partyId");
                }
            }
            
            //动态view
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            //查询条件集合，用于数据库查询
            List<EntityCondition> andExprs = FastList.newInstance();
            EntityCondition mainCond = null;
            //排序字段集合
            List<String> orderBy = FastList.newInstance();
            //显示字段集合
            List<String> fieldsToSelect = FastList.newInstance();
            
            dynamicView.addMemberEntity("TG", "TogetherGroup");
            dynamicView.addAlias("TG", "togetherId");
            dynamicView.addAlias("TG", "createUserId");
            //拼团状态
            dynamicView.addAlias("TG", "status");
            dynamicView.addAlias("TG", "productId");
            //拼团创建时间
            dynamicView.addAlias("TG", "createDate");
            //多少人成团
            dynamicView.addAlias("TG", "limitUserNum");
            //当前人数
            dynamicView.addAlias("TG", "currentNum");
            dynamicView.addAlias("TG", "activityId");
            
            dynamicView.addMemberEntity("TGR", "TogetherGroupRelOrder");
            dynamicView.addAlias("TGR", "orderId");
            dynamicView.addViewLink("TG", "TGR", true, ModelKeyMap.makeKeyMapList("togetherId", "togetherId"));
            
            dynamicView.addMemberEntity("R", "OrderRole");
            dynamicView.addAlias("R", "partyId");
            dynamicView.addViewLink("TGR", "R", true, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
            
            fieldsToSelect.add("togetherId");
            fieldsToSelect.add("createUserId");
            fieldsToSelect.add("status");
            fieldsToSelect.add("productId");
            fieldsToSelect.add("createDate");
            fieldsToSelect.add("limitUserNum");
            fieldsToSelect.add("currentNum");
            fieldsToSelect.add("activityId");
            fieldsToSelect.add("orderId");
            fieldsToSelect.add("partyId");
            dynamicView.setGroupBy(fieldsToSelect);
            
            orderBy.add("-createDate");
            
            if (UtilValidate.isNotEmpty(status)) {
                andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, status));
            } else {
                //按 拼团订单状态 TOGETHER_RUNING拼团中 TOGETHER_DONE已成团  TOGETHER_FAIL未成团
                andExprs.add(EntityCondition.makeCondition("status", EntityOperator.IN, UtilMisc.toList("TOGETHER_RUNING", "TOGETHER_DONE", "TOGETHER_FAIL")));
            }
            
            List<EntityCondition> defaultExprs2 = FastList.newInstance();
            if (UtilValidate.isNotEmpty(partyId)) {
                defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyId"), EntityOperator.EQUALS, partyId));
                andExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.AND));
            }
            
            //添加where条件
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String productId = gv.getString("productId");
                String togetherId = gv.getString("togetherId");
                String createUserId = gv.getString("createUserId");
                String activityId = gv.getString("activityId");
                String groupStatus = gv.getString("status");
                String createDate = gv.getString("createDate");
                String limitUserNum = gv.getString("limitUserNum");
                String orderId = gv.getString("orderId");

                map.put("togetherId", togetherId);
                map.put("createDate", createDate);
                map.put("groupStatus", groupStatus);
                map.put("orderId", orderId);
                
                //拼团标志 是发起的还是参与的
                if (createUserId.equals(partyId)) {
                    map.put("createUser", "Y");
                } else {
                    map.put("createUser", "N");
                }

                if (UtilValidate.isNotEmpty(togetherId)) {
                    List<GenericValue> groupRelOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId));
                    if (UtilValidate.isNotEmpty(groupRelOrders)) {
                        List<String> personHeadList = FastList.newInstance();
                        List<String> personList = FastList.newInstance();
                        for (int g = 0; g < groupRelOrders.size(); g++) {
                            String headImgUrl = groupRelOrders.get(g).getString("headImgUrl");
                            personHeadList.add(headImgUrl);
                        }
                        if (personHeadList.size() > 5) {
                            personList.add(personHeadList.get(0));
                            personList.add(personHeadList.get(1));
                            personList.add(personHeadList.get(2));
                            personList.add(personHeadList.get(3));
                            personList.add(personHeadList.get(4));
                        } else {
                            for (int p = 0; p < personHeadList.size(); p++) {
                                personList.add(personHeadList.get(p));
                            }
                        }
                        map.put("personList", personList);
                    }
                }
                
                GenericValue good = delegator.findByPrimaryKey("ProductActivityGoods", UtilMisc.toMap("productId", productId, "activityId", activityId));
                
                StringBuffer productIds = new StringBuffer();
                if (UtilValidate.isNotEmpty(good)) {
                    productIds.append(productId);
                    productIds.append(",");
                    BigDecimal activityPrice = good.getBigDecimal("activityPrice").setScale(2, BigDecimal.ROUND_HALF_UP);

                    List productList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(productIds)) {
                        Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", productIds.toString()));
                        List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                        if (UtilValidate.isNotEmpty(products)) {
                            Map<String, Object> productMap = FastMap.newInstance();
                            for (int j = 0; j < products.size(); j++) {
                                Map<String, Object> product = products.get(j);
                                productMap.put("productId", product.get("productId"));
                                productMap.put("price", product.get("price"));
                                productMap.put("productName", product.get("productName"));
                                productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                                productMap.put("activityPrice", activityPrice);
                                //多少人成团
                                productMap.put("limitUserNum", limitUserNum);
                                productMap.put("groupStatus", groupStatus);
                                productList.add(productMap);
                                
                            }
                        }
                        
                        map.put("products", productList);
                        
                    }
                    
                }
                recordsList.add(map);
            }
            
            // 获取总记录数
            size = pli.getResultsSizeAfterPartialList();

            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (pli != null) {
                try {
                    pli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
            
            }
        }
        
        //返回的参数
        resultData.put("recordsList", recordsList);
        resultData.put("totalSize", Integer.valueOf(size));
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 我的拼团详情
     *
     * @param request
     * @param togetherId
     * @return
     */
    @RequestMapping(value = "/myOrderGroupDetail", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> partyGroupDetail(HttpServletRequest request, String togetherId) {
        Map<String, Object> resultData = FastMap.newInstance();
        
        //LocalDispatcher对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        
        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;

        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        //每页显示记录条数
        int viewSize = 10;

        resultData.put("viewSize", Integer.valueOf(viewSize));
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        
        dynamicView.addMemberEntity("TG", "TogetherGroup");
        dynamicView.addAlias("TG", "togetherId");
        dynamicView.addAlias("TG", "createUserId");
        //拼团状态
        dynamicView.addAlias("TG", "status");
        dynamicView.addAlias("TG", "productId");
        //拼团创建时间
        dynamicView.addAlias("TG", "createDate");
        //多少人成团
        dynamicView.addAlias("TG", "limitUserNum");
        //当前人数
        dynamicView.addAlias("TG", "currentNum");
        //活动编号
        dynamicView.addAlias("TG", "activityId");
        
        fieldsToSelect.add("togetherId");
        fieldsToSelect.add("createUserId");
        fieldsToSelect.add("status");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("createDate");
        fieldsToSelect.add("limitUserNum");
        fieldsToSelect.add("currentNum");
        fieldsToSelect.add("activityId");
        
        dynamicView.setGroupBy(fieldsToSelect);
        
        if (UtilValidate.isNotEmpty(togetherId)) {
            //按 拼团订单状态 TOGETHER_RUNING拼团中 TOGETHER_DONE已成团  TOGETHER_FAIL未成团
            andExprs.add(EntityCondition.makeCondition("togetherId", EntityOperator.EQUALS, togetherId));
        }
        
        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        boolean beganTransaction = false;
        EntityListIterator pli = null;
        try {
            
            beganTransaction = TransactionUtil.begin();
            
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), null, findOpts);
            
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String createUserId = gv.getString("createUserId");
                String productId = gv.getString("productId");
                String activityId = gv.getString("activityId");
                Timestamp createDate = gv.getTimestamp("createDate");
                String limitUserNum = gv.getString("limitUserNum");
                String status = gv.getString("status");
                
                String statusId = "";
                Timestamp currentTime = UtilDateTime.nowTimestamp();
                Long differentTime = new Long(currentTime.getTime() - createDate.getTime());
                //时间差值 秒
                resultData.put("differentTime", 86400 - differentTime / 1000);//86400
                resultData.put("status", status);
                //多少人成团
                resultData.put("limitUserNum", limitUserNum);
                resultData.put("productId", productId);
                
                if (UtilValidate.isNotEmpty(togetherId)) {
                    List<GenericValue> groupRelOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId));
                    if (UtilValidate.isNotEmpty(groupRelOrders)) {
                        List<String> personList = FastList.newInstance();
                        for (int i = 0; i < groupRelOrders.size(); i++) {
                            String orderUserId = groupRelOrders.get(i).getString("orderUserId");
                            if (orderUserId.equals(createUserId)) {
                                String headImgUrl = groupRelOrders.get(i).getString("headImgUrl");
                                resultData.put("colonelHeadPhoto", headImgUrl);
                            } else {
                                String headImgUrl = groupRelOrders.get(i).getString("headImgUrl");
                                personList.add(headImgUrl);
                            }
                        }
                        resultData.put("personList", personList);
                    }
                    
                }
                
                GenericValue good = delegator.findByPrimaryKey("ProductActivityGoods", UtilMisc.toMap("activityId", activityId, "productId", productId));
                if (UtilValidate.isNotEmpty(good)) {
                    String activityPrice = good.getBigDecimal("activityPrice").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    List productList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(productId)) {
                        Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", productId.toString()));
                        List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                        if (UtilValidate.isNotEmpty(products)) {
                            Map<String, Object> productMap = FastMap.newInstance();
                            for (int j = 0; j < products.size(); j++) {
                                Map<String, Object> product = products.get(j);
                                productMap.put("productId", product.get("productId"));
                                productMap.put("price", product.get("price"));
                                productMap.put("productName", product.get("productName"));
                                productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                                productMap.put("activityPrice", activityPrice);
                                productList.add(productMap);
                            }
                        }
                        resultData.put("products", productList);
                    }
                    
                }
            }
            
            // 获取总记录数
            size = pli.getResultsSizeAfterPartialList();

            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (pli != null) {
                try {
                    pli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
            
            }
        }
        //返回的参数
        resultData.put("totalSize", Integer.valueOf(size));
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 订单详情
     *
     * @param request
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/order/orderDetail", method = RequestMethod.POST)//@RequestBody Paginate paginate
    public ResponseEntity<Map<String, Object>> partyOrderDetail(HttpServletRequest request, String orderId) {
        Map<String, Object> resultData = FastMap.newInstance();
        
        //LocalDispatcher对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Locale locale = (Locale) request.getAttribute("locale");

        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //查询条件集合，用于数据库查询
        List allOrders = FastList.newInstance();
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
            if (UtilValidate.isNotEmpty(orderId)) {
                GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
                BigDecimal grandTotal = orderHeader.getBigDecimal("grandTotal").setScale(2, BigDecimal.ROUND_HALF_UP);
                OrderReadHelper orderReadHelper = new OrderReadHelper(orderHeader);
                List<GenericValue> orderItems = orderReadHelper.getOrderItems();
                List<GenericValue> orderAdjustments = orderReadHelper.getAdjustments();
                BigDecimal num = OrderReadHelper.getOrderProductsNum(delegator, orderId);
                //优惠多少金额
                BigDecimal activityPrice = BigDecimal.ZERO;

                //运费
                BigDecimal newShippingTotal = BigDecimal.ZERO;
                
                String returnId = "";
                String saleOrderTypeId = orderHeader.getString("saleOrderTypeId");
                String statusId = orderHeader.getString("statusId");
                
                List<GenericValue> adjuestments = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(adjuestments)) {
                    for (int a = 0; a < adjuestments.size(); a++) {
                        String orderAdjustmentTypeId = adjuestments.get(a).getString("orderAdjustmentTypeId");
                        if ("SHIPPING_CHARGES".equals(orderAdjustmentTypeId)) {
                            BigDecimal amount = adjuestments.get(a).getBigDecimal("amount");
                            newShippingTotal = newShippingTotal.add(amount);
                        } else {
                            BigDecimal amount = adjuestments.get(a).getBigDecimal("amount");
                            activityPrice = activityPrice.add(amount);
                        }
                        
                    }
                    
                }
                
                BigDecimal newOrderSubTotal = BigDecimal.ZERO;
                //如果没有活动价格
                if (BigDecimal.ZERO.equals(activityPrice)) {
                    //最后应付价格 就是 应付价格加上邮费
                    newOrderSubTotal = grandTotal.subtract(newShippingTotal);
                } else {
                    BigDecimal afterActivityTotal = grandTotal.subtract(activityPrice);
                    //修改后的原价 应该付的价格减去促销表中的优惠 加上邮费 最后应该付的钱
                    newOrderSubTotal = afterActivityTotal.subtract(newShippingTotal);
                }
                
                List<GenericValue> orderPaymentPreferences = null;
                try {
                    orderPaymentPreferences = EntityUtil.filterByAnd(orderHeader.getRelated("OrderPaymentPreference"), UtilMisc.toList(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_CANCELLED")));
                } catch (GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                    e.printStackTrace();
                }
                
                Map orderMap = FastMap.newInstance();
                Timestamp createdDate = null;
                if (orderPaymentPreferences != null && (!orderPaymentPreferences.isEmpty())) {
                    GenericValue orderPaymentPreference = EntityUtil.getFirst(orderPaymentPreferences);
                    String payType = orderPaymentPreference.getString("paymentMethodTypeId");
                    //支付时间
                    createdDate = orderPaymentPreference.getTimestamp("createdDate");
                    String paymentStyle = "";
                    if (payType.equals("EXT_PING")) {
                        paymentStyle = "微信支付";
                    } else {
                        paymentStyle = "现金支付";
                    }
                    //付款方式
                    orderMap.put("paymentStyle", paymentStyle);
                    orderMap.put("expressWay", "快递到家");
                }
                
                //订单编号
                orderMap.put("orderId", orderId);
                
                //拼团
                List<GenericValue> toGroupOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(toGroupOrders)) {
                    GenericValue toGroupOrder = toGroupOrders.get(0);
                    String togetherId = toGroupOrder.getString("togetherId");
                    orderMap.put("togetherId", togetherId);
                }
                
                List<GenericValue> orderGroupRels = delegator.findByAnd("OrderGroupOrderRel", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(orderGroupRels)) {
                    GenericValue orderGroupRel = orderGroupRels.get(0);
                    String orderGroupId = orderGroupRel.getString("orderGroupId");
                    //赠送
                    List<GenericValue> partyOrderRelPresentList = delegator.findByAnd("PartyOrderRelPresent", UtilMisc.toMap("orderGroupId", orderGroupId));
                    if (UtilValidate.isNotEmpty(partyOrderRelPresentList)) {
                        GenericValue partyOrderRelPresent = partyOrderRelPresentList.get(0);
                        String giftId = partyOrderRelPresent.getString("giftId");
                        orderMap.put("giftId", giftId);
                    }
                    
                    //心愿
                    List<GenericValue> partyOrderWishs = delegator.findByAnd("PartyOrderWish", UtilMisc.toMap("orderGroupId", orderGroupId));
                    if (UtilValidate.isNotEmpty(partyOrderWishs)) {
                        GenericValue partyOrderWish = partyOrderWishs.get(0);
                        String wishId = partyOrderWish.getString("wishId");
                        orderMap.put("wishId", wishId);
                    }
                }
                
                if ("ORDER_WAITRECEIVE".equals(statusId) || "ORDER_WAITEVALUATE".equals(statusId) || "ORDER_COMPLETED".equals(statusId) || "".equals(statusId)) {
                    List<GenericValue> orderDeliverys = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderId));
                    if (UtilValidate.isNotEmpty(orderDeliverys)) {
                        GenericValue orderDelivery = orderDeliverys.get(0);
                        //物流单号
                        String deliverId = orderDelivery.getString("logisticsNumber1");
                        orderMap.put("deliverId", deliverId);
                        String deliveryCompany = orderDelivery.getString("deliveryCompany");
                        orderMap.put("companyId", deliveryCompany);
                    }
                    String deliveryCompanyName="";
                    String deliverLogisticsNo ="";
                    //添加订单物流编号和物流公司
                    GenericValue orderDelivery = EntityUtil.getFirst(delegator.findByAnd("OrderDelivery",UtilMisc.toMap("orderId",orderId)));
                    if(UtilValidate.isNotEmpty(orderDelivery)){
                        String deliveryCompany = orderDelivery.getString("deliveryCompany");
                        deliveryCompanyName = EntityUtil.getFirst(delegator.findByAnd("LogisticsCompany",UtilMisc.toMap("companyId",deliveryCompany))).getString("companyName");
                        //物流单号
                        deliverLogisticsNo=orderDelivery.getString("logisticsNumber1");
                    }
                    orderMap.put("deliveryCompanyName",deliveryCompanyName);//物流公司名称
                    orderMap.put("deliverLogisticsNo",deliverLogisticsNo);//物流运单号

                }
                
                //下单时间 开始时间 结束时间
                orderMap.put("createdStamp", orderHeader.getString("createdStamp"));
                
                orderMap.put("num", num);
                String productName = "";
                StringBuffer pName = new StringBuffer();
                String categoryName = "";
                
                if (orderItems != null) {
                    String orderStatus = orderHeader.getString("statusId");
                    Timestamp createdStamp = orderHeader.getTimestamp("createdStamp");
                    //String statusId = "";
                    if ("ORDER_COMPLETED".equals(orderStatus) || "ORDER_WAITEVALUATE".equals(orderStatus)) {
                        statusId = "交易成功";
                        //订单状态
                        orderMap.put("orderStatus", statusId);
                    } else if ("ORDER_WAITPAY".equals(orderStatus)) {
                        Timestamp currentTime = UtilDateTime.nowTimestamp();
                        Long differentTime = new Long(currentTime.getTime() - createdStamp.getTime());
                        
                        Long setGroupOrderCancelTime = new Long(0);
                        GenericValue orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                        if (UtilValidate.isNotEmpty(orderRule)) {
                            if ("togetherCart".equals(saleOrderTypeId)) {
                                //拼团订单
                                String groupCancelStamp = orderRule.getString("groupCancelStamp");
                                String groupCancelUom = orderRule.getString("groupCancelUom");
                                Integer cancelTime = Integer.parseInt(groupCancelStamp);
                                
                                if ("d".equals(groupCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60 * 60 * 24);
                                } else if ("h".equals(groupCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60 * 60);
                                } else if ("min".equals(groupCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60);
                                }
                            } else if ("seckillCart".equals(saleOrderTypeId)) {
                                //秒杀订单 seckillCancelStamp seckillCancelUom
                                String seckillCancelStamp = orderRule.getString("seckillCancelStamp");
                                String seckillCancelUom = orderRule.getString("seckillCancelUom");
                                Integer cancelTime = Integer.parseInt(seckillCancelStamp);
                                
                                if ("d".equals(seckillCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60 * 60 * 24);
                                } else if ("h".equals(seckillCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60 * 60);
                                } else if ("min".equals(seckillCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60);
                                }
                            } else {
                                //普通订单 ordinaryCancelStamp ordinaryCancelUom
                                String ordinaryCancelStamp = orderRule.getString("ordinaryCancelStamp");
                                String ordinaryCancelUom = orderRule.getString("ordinaryCancelUom");
                                Integer cancelTime = Integer.parseInt(ordinaryCancelStamp);
                                if ("d".equals(ordinaryCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60 * 60 * 24);
                                } else if ("h".equals(ordinaryCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60 * 60);
                                } else if ("min".equals(ordinaryCancelUom)) {
                                    setGroupOrderCancelTime = new Long(cancelTime * 60);
                                }
                            }
                            
                        }
                        
                        statusId = "待付款";
                        Long diffTime = (setGroupOrderCancelTime - differentTime / 1000);
                        //订单状态
                        orderMap.put("orderStatus", statusId);
                        //付款时间 毫秒数
                        orderMap.put("createdDate", createdDate);
                        //剩余时间秒数
                        orderMap.put("diffTime", diffTime);
                        
                    } else if ("ORDER_WAITSHIP".equals(orderStatus) || "ORDER_WAITRECEIVE".equals(orderStatus)) {
                        statusId = "待收货";
                        //订单状态
                        orderMap.put("orderStatus", statusId);
                        if ("ORDER_WAITSHIP".equals(orderStatus)) {
                            String isReallyWaitShip = "Y";
                            orderMap.put("isReallyWaitShip", isReallyWaitShip);
                        }
                    } else if ("ORDER_CANCELLED".equals(orderStatus) || "ORDER_RETURNED".equals(orderStatus)) {
                        statusId = "交易关闭";
                        //订单状态
                        orderMap.put("orderStatus", statusId);
                    }
                    
                    List<GenericValue> orderItemList = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
                    
                    List<Map> items = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(orderItemList)) {
                        Map<String, Map<String, String>> activityMap = FastMap.newInstance();
                        StringBuffer productIds = new StringBuffer();
                        BigDecimal orderItemTotalPrice = BigDecimal.ZERO;
                        for (int i = 0; i < orderItemList.size(); i++) {
                            Map<String, Object> orderItemMap = FastMap.newInstance();
                            String productId = orderItemList.get(i).getString("productId");
                            BigDecimal quantity = orderItemList.get(i).getBigDecimal("quantity");//UNIT_PRICE
                            BigDecimal unitPrice = orderItemList.get(i).getBigDecimal("unitPrice");
                            productIds.append(productId);
                            productIds.append(",");
                            orderItemTotalPrice = orderItemTotalPrice.add(quantity.multiply(unitPrice));
                            String productQuantity = orderItemList.get(i).getBigDecimal("quantity").setScale(0, BigDecimal.ROUND_DOWN).toString();
                            String productUnitPrice = orderItemList.get(i).getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                            if (UtilValidate.isNotEmpty(activityMap.get(productId))) {
                                Map<String, String> productMap = activityMap.get(productId);
                                productMap.put("quantity", productQuantity);
                                productMap.put("unitPrice", productUnitPrice);
                            } else {
                                Map<String, String> productMap = FastMap.newInstance();
                                productMap.put("quantity", productQuantity);
                                productMap.put("unitPrice", productUnitPrice);
                                activityMap.put(productId, productMap);
                            }
                            orderItemMap.put("productId",productId);
                            orderItemMap.put("quantity",productQuantity);
                            orderItemMap.put("unitPrice",productUnitPrice);
                            items.add(orderItemMap);
                        }
                        //商品价格 展示的价格不是支付的价格
                        orderMap.put("orderGrandTotal", orderItemTotalPrice);
                        String prodStr = productIds.toString();
                        if (prodStr.endsWith(",")) {
                            prodStr = prodStr.substring(0, prodStr.length() - 1);
                        }
                        
                        if (UtilValidate.isNotEmpty(prodStr)) {
                            
                            Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", prodStr));
                            List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                            if (UtilValidate.isNotEmpty(products)) {
                                List<Map> productList = FastList.newInstance();
                                for (int k = 0; k < products.size(); k++) {
                                    Map<String, Object> product = products.get(k);
                                    Map<String, Object> productMap = FastMap.newInstance();
                                    String productId = (String) product.get("productId");
                                    productMap.put("productId", productId);
                                    productMap.put("integralDeductionType",product.get("integralDeductionType"));
                                    List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderId, "productId", productId));
                                    if (UtilValidate.isNotEmpty(returnItems)) {
                                        GenericValue returnItem = returnItems.get(0);
                                        returnId = returnItem.getString("returnId");
                                        productMap.put("returnId", returnId);
                                    }
                                    productName = product.get("productName").toString();
                                    pName.append(productName);
                                    pName.append(",");
                                    productMap.put("productName", product.get("productName"));
                                    String isVariant = (String) product.get("isVariant");
                                    if (isVariant.equals("Y")) {

                                        GenericValue productForFeature = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                                        if (UtilValidate.isNotEmpty(productForFeature)) {
                                            String productFeatureId = productForFeature.getString("featureProductId").toString();
                                            StringBuffer featureProductId = new StringBuffer();
                                            featureProductId.append(productFeatureId);
                                            featureProductId.append("|");

                                            String fPorId = featureProductId.toString();
                                            if (fPorId.endsWith("|")) {
                                                String[] proFeids = fPorId.split("\\|");
                                                List<Map> featureList = FastList.newInstance();
                                                for (int f = 0; f < proFeids.length; f++) {
                                                    Map<String, Object> featureMap = FastMap.newInstance();
                                                    String proFeaId = proFeids[f];
                                                    GenericValue feature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", proFeaId));//PRODUCT_FEATURE_ID
                                                    if (UtilValidate.isNotEmpty(feature)) {
                                                        String productFeatureName = feature.getString("productFeatureName");
                                                        featureMap.put("typeValue", productFeatureName);
                                                        String productFeatureTypeId = feature.getString("productFeatureTypeId");
                                                        if (UtilValidate.isNotEmpty(productFeatureTypeId)) {
                                                            GenericValue featureType = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureTypeId));//product_feature_type
                                                            if (UtilValidate.isNotEmpty(featureType)) {
                                                                String productFeatureTypeName = featureType.getString("productFeatureTypeName");
                                                                featureMap.put("featureTypeName", productFeatureTypeName);
                                                            }

                                                        }

                                                    }
                                                    featureList.add(featureMap);

                                                }
                                                productMap.put("features", featureList);
                                            }
                                        }

                                    }
                                    
                                    productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                                    String primaryProductCategoryId = (String) product.get("primaryProductCategoryId");
                                    if (UtilValidate.isNotEmpty(primaryProductCategoryId)) {
                                        GenericValue category = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", primaryProductCategoryId));
                                        if (UtilValidate.isNotEmpty(category)) {
                                            categoryName = category.getString("categoryName");
                                        }
                                    }

                                    //是否使用积分进行判断
                                    if (UtilValidate.areEqual("2", product.get("integralDeductionType")) || UtilValidate.areEqual("3", product.get("integralDeductionType"))) {
                                        //所需要的积分数
                                        productMap.put("scoreValue", product.get("scoreValue"));
                                        BigDecimal diffPrice = (BigDecimal) product.get("diffPrice");
                                        if (UtilValidate.isEmpty(diffPrice)) {
                                            diffPrice = BigDecimal.ZERO;
                                        }
                                        //差价
                                        productMap.put("diffPrice", diffPrice);
                                    }
                                    BigDecimal price = ((BigDecimal) product.get("price")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    if (UtilValidate.isEmpty(price)) {
                                        price = BigDecimal.ZERO;
                                    }
                                    productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                                    Map<String, String> pactiveMap = activityMap.get(product.get("productId"));
                                    String quantity = (String) pactiveMap.get("quantity");
                                    String productUnitPrice = (String) pactiveMap.get("unitPrice");
                                    if ("0.00".equals(productUnitPrice)){
                                        productMap.put("isPromoGift", "Y");
                                    }else {
                                        productMap.put("isPromoGift", "N");
                                    }
                                    productMap.put("quantity", quantity);
                                    BigDecimal productActivityPrice = BigDecimal.ZERO;
                                    BigDecimal amount = BigDecimal.ZERO;
                                    Map<String, Object> orderGroupInfos = (Map<String, Object>) product.get("orderGroupInfo");
                                    Map<String, Object> secKillInfo = (Map<String, Object>) product.get("secKillInfo");
                                    Map<String, Object> priceDownInfo = (Map<String, Object>) product.get("priceDownInfo");
                                    if (UtilValidate.isNotEmpty(orderGroupInfos) && orderGroupInfos.size() > 0 && "togetherCart".equals(saleOrderTypeId)) {
                                        productActivityPrice = ((BigDecimal) orderGroupInfos.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        productMap.put("price", productActivityPrice);
                                    } else if (UtilValidate.isNotEmpty(secKillInfo) && secKillInfo.size() > 0) {
                                        productActivityPrice = ((BigDecimal) secKillInfo.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        productMap.put("price", productActivityPrice);
                                    } else if (UtilValidate.isNotEmpty(priceDownInfo) && priceDownInfo.size() > 0) {
                                        productMap.put("price", price);
                                    } else if (UtilValidate.isEmpty(orderGroupInfos) && UtilValidate.isEmpty(secKillInfo) && UtilValidate.isEmpty(priceDownInfo)) {
                                        productMap.put("price", productUnitPrice);
                                    } else if (!"togetherCart".equals(saleOrderTypeId)) {
                                        productMap.put("price", productUnitPrice);
                                    }
                                    productList.add(productMap);
                                }

                                List<Map> newProductList = FastList.newInstance();
                                        Set<String> itemIds = FastSet.newInstance();
                                        
                                        for (int i=0;i<productList.size();i++) {
                                            Map<String, Object> oldProductMap = productList.get(i);
                                            String productId = oldProductMap.get("productId").toString();
                                            String prodctName = oldProductMap.get("productName").toString();
                                            String mediumImageUrl = oldProductMap.get("mediumImageUrl").toString();
                                            String price = oldProductMap.get("price").toString();
                                            List<GenericValue> orderItems1 = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId, "productId", productId));
                                            for (GenericValue orderItem : orderItems1) {
                                                if (!itemIds.contains(orderItem.getString("orderItemSeqId"))) {
                                                    itemIds.add(orderItem.getString("orderItemSeqId"));
                                                    Map<String, Object> newProductMap = FastMap.newInstance();
                                                    String unitPrice = orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                                    if ("0.00".equals(unitPrice)) {
                                                        newProductMap.put("isPromoGift", "Y");
                                                        newProductMap.put("price", "0.00");
                                                    } else if (!"0.00".equals(unitPrice)) {
                                                        newProductMap.put("isPromoGift", "N");
                                                        newProductMap.put("price", unitPrice);
                                                    } else if (!price.equals(unitPrice) && !"0.00".equals(price)) {
                                                        newProductMap.put("isPromoGift", "N");
                                                        newProductMap.put("price", price);
                                                    }
                                                    String quantity = oldProductMap.get("quantity").toString();
                                                    newProductMap.put("productName", prodctName);
                                                    newProductMap.put("mediumImageUrl", mediumImageUrl);
                                                    newProductMap.put("quantity", quantity);
                                                    newProductMap.put("productId", productId);
                                                    List<Map> oldFeatureList = UtilValidate.isEmpty((List<Map>) oldProductMap.get("features")) ? null : (List<Map>) oldProductMap.get("features");
                                                    List<Map> newFeatureList = FastList.newInstance();
                                                    if (UtilValidate.isNotEmpty(oldFeatureList)) {
                                                        for (int f = 0; f < oldFeatureList.size(); f++) {
                                                            Map<String, Object> oldFeatureMap = oldFeatureList.get(f);
                                                            Map<String, Object> newFeatureMap = FastMap.newInstance();
                                                            newFeatureMap.put("featureTypeName", oldFeatureMap.get("featureTypeName"));
                                                            newFeatureMap.put("typeValue", oldFeatureMap.get("typeValue"));
                                                            newFeatureList.add(newFeatureMap);
                                                        }
                                                        newProductMap.put("features", newFeatureList);
                                                    }
    
                                                    String rId = UtilValidate.isEmpty(oldProductMap.get("returnId")) ? null : oldProductMap.get("returnId").toString();
                                                    if (UtilValidate.isNotEmpty(rId)) {
                                                        newProductMap.put("returnId", rId);
                                                    }
                                                    String integralDeductionType = oldProductMap.get("integralDeductionType").toString();
                                                    if ("2".equals(integralDeductionType)) {
                                                        int scoreValue2 = UtilValidate.isEmpty(oldProductMap.get("scoreValue")) ? null : (Integer) oldProductMap.get("scoreValue");
                                                        BigDecimal diffPrice2 = UtilValidate.isEmpty(oldProductMap.get("diffPrice")) ? null : ((BigDecimal) oldProductMap.get("diffPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
    
                                                        //所需积分数
                                                        if (UtilValidate.isNotEmpty(scoreValue2)) {
                                                            newProductMap.put("scoreValue", scoreValue2);
                                                        }
                                                        if (UtilValidate.isNotEmpty(diffPrice2)) {
                                                            newProductMap.put("diffPrice", diffPrice2);
                                                        }
    
                                                    } else if ("3".equals(integralDeductionType)) {
                                                        BigDecimal scoreValue3 = UtilValidate.isEmpty(oldProductMap.get("scoreValue")) ? null : ((BigDecimal) oldProductMap.get("scoreValue")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                                        BigDecimal diffPrice3 = UtilValidate.isEmpty(oldProductMap.get("diffPrice")) ? null : ((BigDecimal) oldProductMap.get("diffPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
    
                                                        if (UtilValidate.isNotEmpty(scoreValue3)) {
                                                            newProductMap.put("scoreValue", scoreValue3);
                                                        }
                                                        if (UtilValidate.isNotEmpty(diffPrice3)) {
                                                            newProductMap.put("diffPrice", diffPrice3);
                                                        }
                                                    }
                                                    newProductList.add(newProductMap);
                                                }
                                            }
    
                                        }

                                orderMap.put("products", newProductList);
                            }
                            
                        }
                        
                    }
                }

                //收货人
                List<GenericValue> orderContactMechs = delegator.findByAnd("OrderContactMech", UtilMisc.toMap("orderId", orderId, "contactMechPurposeTypeId", "SHIPPING_LOCATION"));
                if (UtilValidate.isNotEmpty(orderContactMechs)) {
                    List<Map> postalAddressList = FastList.newInstance();
                    for (int j = 0; j < orderContactMechs.size(); j++) {
                        String contactMechId = orderContactMechs.get(j).getString("contactMechId");
                        if (UtilValidate.isNotEmpty(contactMechId)) {
                            GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", contactMechId));
                            Map<String, Object> addressMap = FastMap.newInstance();
                            String attnName = postalAddress.getString("attnName");
                            String address1 = postalAddress.getString("address1");
                            String mobilePhone = postalAddress.getString("mobilePhone");
                            String provinedId = postalAddress.getString("stateProvinceGeoId");
                            if (UtilValidate.isNotEmpty(provinedId)) {
                                GenericValue pGeo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", provinedId));
                                if (UtilValidate.isNotEmpty(pGeo)) {
                                    addressMap.put("provinceName", pGeo.getString("geoName"));
                                }
                            }
                            String cityId = postalAddress.getString("city");
                            if (UtilValidate.isNotEmpty(cityId)) {
                                GenericValue cGeo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", cityId));
                                if (UtilValidate.isNotEmpty(cGeo)) {
                                    addressMap.put("cityName", cGeo.getString("geoName"));
                                }
                            }
                            String countyGeoId = postalAddress.getString("countyGeoId");
                            if (UtilValidate.isNotEmpty(countyGeoId)) {
                                GenericValue coGeo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", countyGeoId));
                                if (UtilValidate.isNotEmpty(coGeo)) {
                                    addressMap.put("countyName", coGeo.getString("geoName"));
                                }
                            }
                            addressMap.put("attnName", attnName);
                            addressMap.put("address1", address1);
                            addressMap.put("mobilePhone", mobilePhone);
                            
                            postalAddressList.add(addressMap);
                        }
                        
                    }
                    orderMap.put("postalAddressList", postalAddressList);
                    
                }
                
                //发票
                List<GenericValue> orderAttributes = delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId", orderId));
                
                String prodStrName = pName.toString();
                if (prodStrName.endsWith(",")) {
                    prodStrName = prodStrName.substring(0, prodStrName.length() - 1);
                }
                
                List<Map> orderAttrList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(orderAttributes)) {
                    
                    for (int j = 0; j < orderAttributes.size(); j++) {
                        Map<String, Object> invoiceMap = FastMap.newInstance();
                        GenericValue orderAttribute = orderAttributes.get(j);
                        String attrName = orderAttribute.getString("attrName");
                        String attrVal = orderAttribute.getString("attrValue");
                        if (attrName.equals("needInvoice")) {//是否需要发票 Y N
                            invoiceMap.put("needInvoice", attrVal);
                        }
                        //个人 公司
                        if (attrName.equals("invoiceType")) {
                            invoiceMap.put("invoiceType", attrVal);
                        }
                        //发票抬头
                        if (attrName.equals("invoiceTitle")) {
                            invoiceMap.put("invoiceTitle", attrVal);
                        }
                        
                        if (attrName.equals("invoiceContentTypeId")) {
                            invoiceMap.put("invoiceContentTypeId", attrVal);
                            if (attrVal.equals("product")) {
                                invoiceMap.put("productName", prodStrName);
                            } else {
                                //获取商品分类
                                invoiceMap.put("categoryName", categoryName);
                            }
                        }
                        orderAttrList.add(invoiceMap);
                    }
                    
                    List<Map> newAttrList = FastList.newInstance();
                    for (int l = 0; l < orderAttrList.size(); l++) {
                        Map<String, Object> newArrMap = FastMap.newInstance();
                        //是否需要发票
                        String needInvoice = (String) orderAttrList.get(l).get("needInvoice");
                        if (UtilValidate.isNotEmpty(needInvoice)) {
                            newArrMap.put("needInvoice", needInvoice);
                        }
                        
                        //发票类型
                        String invoiceType = (String) orderAttrList.get(l).get("invoiceType");
                        if (UtilValidate.isNotEmpty(invoiceType)) {
                            newArrMap.put("invoiceType", invoiceType);
                        }
                        //发票抬头
                        String invoiceTitle = (String) orderAttrList.get(l).get("invoiceTitle");
                        if (UtilValidate.isNotEmpty(invoiceTitle)) {
                            newArrMap.put("invoiceTitle", invoiceTitle);
                        }
                        //发票内容
                        String invoiceContentTypeId = (String) orderAttrList.get(l).get("invoiceContentTypeId");
                        if (UtilValidate.isNotEmpty(invoiceContentTypeId)) {
                            if (invoiceContentTypeId.equals("product")) {
                                String pName1 = (String) orderAttrList.get(l).get("productName");
                                newArrMap.put("invoiceContent", pName1);
                            } else {
                                String cName = (String) orderAttrList.get(l).get("categoryName");
                                newArrMap.put("invoiceContent", cName);
                            }
                        }
                        if (UtilValidate.isNotEmpty(newArrMap)) {
                            newAttrList.add(newArrMap);
                        }
                        
                    }
                    
                    orderMap.put("orderAttrList", newAttrList);
                }
                //商品小计 显示的原价
                orderMap.put("orderGrandTotal", newOrderSubTotal);
                //商品总价 最后支付的价格 orderHeader表中
                orderMap.put("orderSubTotal", grandTotal);
                //运费
                orderMap.put("orderShippingTotal", newShippingTotal);
                BigDecimal newActivityPrice = BigDecimal.ZERO.subtract(activityPrice);
                //促销优惠
                orderMap.put("orderHeaderAdjustments", newActivityPrice);
                allOrders.add(orderMap);
            }
            
            //跳转的页数
            int viewIndex = 0;
            resultData.put("viewIndex", Integer.valueOf(viewIndex));
            
            //每页显示记录条数
            int viewSize = 10;
            resultData.put("viewSize", Integer.valueOf(viewSize));
            
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
            resultData.put("totalSize", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));
            resultData.put("allOrders", allOrders);
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
                resultData.put("retCode", 0);
                resultData.put("message", e.getMessage());
            }
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 订单列表
     *
     * @param request
     * @param orderStatusType
     * @param token
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/order/myOrders", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> myOrders(HttpServletRequest request, String orderStatusType, JwtAuthenticationToken token, @RequestParam(defaultValue = "0") Integer page, Integer pageSize) {
        Map<String, Object> resultData = FastMap.newInstance();
        int limit = pageSize;
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        
        //记录集合
        List<Map> itemList = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        
        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        //每页显示记录条数
        int viewSize = 10;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        EntityListIterator pli = null;
        boolean beganTransaction = false;
        try {
            
            beganTransaction = TransactionUtil.begin();
            
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
            String partyId = "";
            if (UtilValidate.isNotEmpty(userLogin)) {
                partyId = userLogin.getString("partyId");
            }
            
            dynamicView.addMemberEntity("OH", "OrderHeader");
            dynamicView.addAlias("OH", "orderId");
            dynamicView.addAlias("OH", "statusId");
            dynamicView.addAlias("OH", "grandTotal");
            dynamicView.addAlias("OH", "createdStamp");//CREATED_STAMP
            dynamicView.addAlias("OH", "productStoreId");
            dynamicView.addAlias("OH", "saleOrderTypeId");
            dynamicView.addAlias("OH", "isShow");
            //订单类型
            dynamicView.addAlias("OH", "saleOrderTypeId");
            
            dynamicView.addMemberEntity("R", "OrderRole");
            dynamicView.addAlias("R", "partyId");
            dynamicView.addViewLink("OH", "R", false, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
            
            dynamicView.addMemberEntity("PS", "ProductStore");
            dynamicView.addAlias("PS", "ownerPartyId");
            dynamicView.addViewLink("OH", "PS", false, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));

            dynamicView.addMemberEntity("PG", "PartyGroup");
            dynamicView.addAlias("PG", "isInner");//isInner partyName
            dynamicView.addAlias("PG", "partyName");
            dynamicView.addViewLink("PS", "PG", false, ModelKeyMap.makeKeyMapList("ownerPartyId", "partyId"));

            fieldsToSelect.add("orderId");
            fieldsToSelect.add("statusId");
            fieldsToSelect.add("statusId");
            fieldsToSelect.add("createdStamp");
            fieldsToSelect.add("partyId");
            fieldsToSelect.add("grandTotal");
            fieldsToSelect.add("productStoreId");
            fieldsToSelect.add("saleOrderTypeId");
            fieldsToSelect.add("ownerPartyId");
            fieldsToSelect.add("isInner");
            fieldsToSelect.add("partyName");
            
            dynamicView.setGroupBy(fieldsToSelect);
            
            orderBy.add("-createdStamp");
            List<EntityCondition> orExp = FastList.newInstance();
            orExp.add(EntityCondition.makeCondition("isShow", EntityOperator.NOT_EQUAL, "N"));
            orExp.add(EntityCondition.makeCondition("isShow", EntityOperator.EQUALS, null));
            andExprs.add(EntityCondition.makeCondition(orExp, EntityOperator.OR));
            
            List<String> orderStatusList = FastList.newInstance();
            List<String> statusList = FastList.newInstance();
            statusList.add("ORDER_COMPLETED");
            statusList.add("ORDER_WAITPAY");
            statusList.add("ORDER_HAVEPAY");
            statusList.add("ORDER_WAITSHIP");
            statusList.add("ORDER_WAITRECEIVE");
            statusList.add("ORDER_WAITEVALUATE");
            statusList.add("ORDER_CANCELLED");
            statusList.add("ORDER_RETURNED");
            
            if (orderStatusType.equals("已完成")) {
                orderStatusList.add("ORDER_COMPLETED");
                orderStatusList.add("ORDER_WAITEVALUATE");
            } else if (orderStatusType.equals("待收货")) {
                orderStatusList.add("ORDER_HAVEPAY");
                orderStatusList.add("ORDER_WAITSHIP");
                orderStatusList.add("ORDER_WAITRECEIVE");
            } else if (orderStatusType.equals("已取消")) {
                orderStatusList.add("ORDER_CANCELLED");
                orderStatusList.add("ORDER_RETURNED");
            } else if (orderStatusType.equals("待付款")) {
                orderStatusList.add("ORDER_WAITPAY");
            }
            
            if (UtilValidate.isNotEmpty(orderStatusType)) {
                andExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, orderStatusList));
            } else {
                andExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, statusList));
            }
            
            List<EntityCondition> defaultExprs2 = FastList.newInstance();
            if (UtilValidate.isNotEmpty(partyId)) {
                defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyId"), EntityOperator.EQUALS, partyId));
                andExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.AND));
            }
            
            //添加where条件
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            
            // 获取总记录数
            size = pli.getResultsSizeAfterPartialList();
            List<GenericValue> orderHeaderLists = pli.getPartialList(lowIndex, viewSize);
            
            // 获取分页所需的记录集合
            for (GenericValue orderHeader : orderHeaderLists) {
                
                Map map = FastMap.newInstance();
                String orderId = orderHeader.getString("orderId");
                String statusId = orderHeader.getString("statusId");
                String saleOrderTypeId = orderHeader.getString("saleOrderTypeId");
                String orderStatus = "";
                BigDecimal grandTotal = orderHeader.getBigDecimal("grandTotal").setScale(2, BigDecimal.ROUND_HALF_UP);
                if (statusId.equals("ORDER_COMPLETED")) {
                    orderStatus = "交易成功";
                } else if (statusId.equals("ORDER_WAITEVALUATE")) {
                    orderStatus = "交易成功";
                } else if (statusId.equals("ORDER_CANCELLED")) {
                    orderStatus = "交易关闭";
                } else if (statusId.equals("ORDER_RETURNED")) {
                    orderStatus = "交易关闭";
                } else if (statusId.equals("ORDER_HAVEPAY")) {
                    orderStatus = "待收货";
                } else if (statusId.equals("ORDER_WAITSHIP")) {
                    orderStatus = "待收货";
                    String isReallyWaitShip = "Y";
                    map.put("isReallyWaitShip", isReallyWaitShip);
                } else if (statusId.equals("ORDER_WAITRECEIVE")) {
                    orderStatus = "待收货";
                } else if (statusId.equals("ORDER_WAITPAY")) {
                    orderStatus = "待付款";
                }
                
                if ("ORDER_WAITRECEIVE".equals(statusId) || "ORDER_WAITEVALUATE".equals(statusId) || "ORDER_COMPLETED".equals(statusId) || "".equals(statusId)) {
                    List<GenericValue> orderDeliverys = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderId));
                    if (UtilValidate.isNotEmpty(orderDeliverys)) {
                        GenericValue orderDelivery = orderDeliverys.get(0);
                        //物流单号
                        String deliverId = orderDelivery.getString("logisticsNumber1");
                        map.put("deliverId", deliverId);
                        String deliveryCompany = orderDelivery.getString("deliveryCompany");
                        map.put("companyId", deliveryCompany);
                    }
                }
                
                map.put("orderId", orderId);
                map.put("orderStatus", orderStatus);
                map.put("grandTotal", grandTotal);
                map.put("saleOrderTypeId", saleOrderTypeId);
                String storeName = "";
                BigDecimal num = OrderReadHelper.getOrderProductsNum(delegator, orderId);
                map.put("num", num);
                
                String isInner = orderHeader.getString("isInner");
                String partyName = orderHeader.getString("partyName");
                if (isInner.equals("Y")) {
                    storeName = "yabiz商城自营";
                } else {
                    storeName = partyName;
                }
                map.put("storeName", storeName);
                
                //拼团
                List<GenericValue> toGroupOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(toGroupOrders)) {
                    GenericValue toGroupOrder = toGroupOrders.get(0);
                    String togetherId = toGroupOrder.getString("togetherId");
                    map.put("togetherId", togetherId);
                }
                
                List<GenericValue> orderGroupRels = delegator.findByAnd("OrderGroupOrderRel", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(orderGroupRels)) {
                    GenericValue orderGroupRel = orderGroupRels.get(0);
                    String orderGroupId = orderGroupRel.getString("orderGroupId");
                    //赠送
                    List<GenericValue> partyOrderRelPresentList = delegator.findByAnd("PartyOrderRelPresent", UtilMisc.toMap("orderGroupId", orderGroupId));
                    if (UtilValidate.isNotEmpty(partyOrderRelPresentList)) {
                        GenericValue partyOrderRelPresent = partyOrderRelPresentList.get(0);
                        String giftId = partyOrderRelPresent.getString("giftId");
                        map.put("giftId", giftId);
                    }
                    
                    //心愿
                    List<GenericValue> partyOrderWishs = delegator.findByAnd("PartyOrderWish", UtilMisc.toMap("orderGroupId", orderGroupId));
                    if (UtilValidate.isNotEmpty(partyOrderWishs)) {
                        GenericValue partyOrderWish = partyOrderWishs.get(0);
                        String wishId = partyOrderWish.getString("wishId");
                        map.put("wishId", wishId);
                    }
                }
                
                List<GenericValue> orderItmes = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
                StringBuffer productIds = new StringBuffer();
                if (UtilValidate.isNotEmpty(orderItmes)) {
                    
                    List productList = FastList.newInstance();
                    Map<String, Map<String, String>> activityMap = FastMap.newInstance();
                    for (int i = 0; i < orderItmes.size(); i++) {
                        String productId = orderItmes.get(i).getString("productId");
                        productIds.append(productId);
                        productIds.append(",");
                        String quantity = orderItmes.get(i).getBigDecimal("quantity").setScale(0, BigDecimal.ROUND_DOWN).toString();
                        if (UtilValidate.isNotEmpty(activityMap.get(productId))) {
                            Map<String, String> productMap = activityMap.get(productId);
                            productMap.put("quantity", quantity);
                        } else {
                            Map<String, String> productMap = FastMap.newInstance();
                            productMap.put("quantity", quantity);
                            activityMap.put(productId, productMap);
                        }
                    }
                    String prodStr = productIds.toString();
                    if (prodStr.endsWith(",")) {
                        prodStr = prodStr.substring(0, prodStr.length() - 1);
                    }
                    
                    if (UtilValidate.isNotEmpty(prodStr)) {
                        Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", prodStr.toString()));
                        List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                        if (UtilValidate.isNotEmpty(products)) {
                            
                            for (int j = 0; j < products.size(); j++) {
                                Map<String, Object> product = products.get(j);
                                Map<String, Object> productMap = FastMap.newInstance();
                                String pId = (String) product.get("productId");
                                productMap.put("productId", pId);
                                BigDecimal price = (BigDecimal) product.get("price");
                                if (UtilValidate.isEmpty(price)) {
                                    price = BigDecimal.ZERO;
                                }
                                productMap.put("price", price);
                                productMap.put("productName", product.get("productName"));
                                productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                                Map<String, String> pactiveMap = activityMap.get(product.get("productId"));
                                String quantity = (String) pactiveMap.get("quantity");
                                
                                if (UtilValidate.isEmpty(quantity)) {
                                    quantity = "0";
                                }
                                
                                productMap.put("quantity", quantity);
                                String isVariant = (String) product.get("isVariant");
                                if (isVariant.equals("Y")) {
                                    GenericValue productForFeature = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", pId));
                                    if (UtilValidate.isNotEmpty(productForFeature)) {
                                        String productFeatureId = productForFeature.getString("featureProductId").toString();
                                        StringBuffer featureProductId = new StringBuffer();
                                        featureProductId.append(productFeatureId);
                                        featureProductId.append("|");
                                        String fPorId = featureProductId.toString();
                                        if (fPorId.endsWith("|")) {
                                            String[] proFeids = fPorId.split("\\|");
                                            List<Map> featureList = FastList.newInstance();
                                            for (int f = 0; f < proFeids.length; f++) {
                                                Map<String, Object> featureMap = FastMap.newInstance();
                                                String proFeaId = proFeids[f];
                                                GenericValue feature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", proFeaId));//PRODUCT_FEATURE_ID
                                                if (UtilValidate.isNotEmpty(feature)) {
                                                    String productFeatureName = feature.getString("productFeatureName");
                                                    featureMap.put("typeValue", productFeatureName);
                                                    String productFeatureTypeId = feature.getString("productFeatureTypeId");
                                                    if (UtilValidate.isNotEmpty(productFeatureTypeId)) {
                                                        GenericValue featureType = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureTypeId));//product_feature_type
                                                        if (UtilValidate.isNotEmpty(featureType)) {
                                                            String productFeatureTypeName = featureType.getString("productFeatureTypeName");
                                                            featureMap.put("featureTypeName", productFeatureTypeName);
                                                        }
                                                        
                                                    }
                                                    
                                                }
                                                featureList.add(featureMap);
                                                
                                            }
                                            productMap.put("features", featureList);
                                        }
                                        
                                    }
                                    
                                }
                                
                                productList.add(productMap);
                            }
                        }
                        map.put("products", productList);
                    }
                    
                }
                
                itemList.add(map);
            }
            
            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 0;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (pli != null) {
                try {
                    pli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
            
            }
        }
        //返回的参数
        resultData.put("itemList", itemList);
        resultData.put("totalSize", Integer.valueOf(size));
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    @RequestMapping(value = "/coupon/forCart", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> partyCoupon(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        
        //已领取、已使用、已过期
        String productIds = request.getParameter("productIds");
        
        if (UtilValidate.isEmpty(productIds)) {
            resultData.put("retCode", 0);
            resultData.put("message", "产品标识为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String[] productIdArray = productIds.split(",");
        List<String> productList = FastList.newInstance();
        List<Integer> quantityList = FastList.newInstance();
        for (int i = 0; i < productIdArray.length; i++) {
            String product = productIdArray[i];
            String[] productInfo = product.split("_");
            productList.add(productInfo[0]);
            quantityList.add(Integer.parseInt(productInfo[1]));
        }
        
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        boolean beganTransaction = false;
        EntityListIterator pli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
            String partyId = userLogin.getString("partyId");
            
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PPCP", "ProductPromoCodeParty");
            dynamicView.addAlias("PPCP", "productPromoCodeId");
            dynamicView.addAlias("PPCP", "partyId");
            dynamicView.addAlias("PPCP", "getDate");
            dynamicView.addAlias("PPCP", "useDate");
            
            dynamicView.addMemberEntity("PPC", "ProductPromoCode");
            dynamicView.addAlias("PPC", "productPromoCodeId");
            dynamicView.addAlias("PPC", "promoCodeStatus");
            dynamicView.addAlias("PPC", "couponCode");
            dynamicView.addAlias("PPC", "fromDate");
            dynamicView.addAlias("PPC", "thruDate");
            dynamicView.addViewLink("PPCP", "PPC", false, ModelKeyMap.makeKeyMapList("productPromoCodeId", "productPromoCodeId"));
            
            dynamicView.addMemberEntity("PPCN", "ProductPromoCoupon");
            dynamicView.addAlias("PPCN", "couponCode");
            dynamicView.addAlias("PPCN", "couponName");
            dynamicView.addAlias("PPCN", "couponType");
            dynamicView.addAlias("PPCN", "payFill");
            dynamicView.addAlias("PPCN", "payReduce");
            dynamicView.addAlias("PPCN", "applyScope");
            dynamicView.addAlias("PPCN", "startDate");
            dynamicView.addAlias("PPCN", "endDate");
            dynamicView.addAlias("PPCN", "useWithScore");
            dynamicView.addAlias("PPCN", "couponProductType");
            dynamicView.addViewLink("PPC", "PPCN", false, ModelKeyMap.makeKeyMapList("couponCode", "couponCode"));
            List<String> groupByList = UtilMisc.toList("productPromoCodeId", "partyId", "getDate", "useDate", "promoCodeStatus", "couponCode");
            groupByList.add("couponName");
            groupByList.add("couponType");
            groupByList.add("payFill");
            groupByList.add("payReduce");
            groupByList.add("applyScope");
            groupByList.add("fromDate");
            groupByList.add("thruDate");
            groupByList.add("startDate");
            groupByList.add("endDate");
            groupByList.add("useWithScore");
            groupByList.add("couponProductType");
            groupByList.add("productStoreId");
            groupByList.add("storeName");
            groupByList.add("productId");
            dynamicView.setGroupBy(groupByList);
            
            dynamicView.addMemberEntity("PSCA", "ProductStoreCouponAppl");
            dynamicView.addAlias("PSCA", "productStoreId");
            dynamicView.addViewLink("PPC", "PSCA", false, ModelKeyMap.makeKeyMapList("couponCode", "couponCode"));
            
            dynamicView.addMemberEntity("PS", "ProductStore");
            dynamicView.addAlias("PS", "storeName");
            dynamicView.addViewLink("PSCA", "PS", false, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));
            
            
            List<EntityCondition> filedExprs = FastList.newInstance();
            //未使用的
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoCodeStatus"), EntityOperator.IN, UtilMisc.toList("C", "G")));
            filedExprs.add(EntityCondition.makeCondition("partyId", partyId));
            
            if (UtilValidate.isNotEmpty(productIds)) {
                String[] pIds = productIds.split(",");
                
                List<GenericValue> productStores = delegator.findList("Product", EntityCondition.makeCondition("productId", EntityOperator.IN, CollectionUtils.arrayToList(pIds)), null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
                    List<String> storeIds = FastList.newInstance();
                    for (int i = 0; i < productStores.size(); i++) {
                        GenericValue store = productStores.get(i);
                        storeIds.add(store.getString("productStoreId"));
                    }
                    
                    List<EntityCondition> storeExps = FastList.newInstance();
                    filedExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.IN, storeIds));
                }
                dynamicView.addMemberEntity("PCPT", "ProductCouponProduct");
                dynamicView.addAlias("PCPT", "productId");
                
                dynamicView.addViewLink("PPC", "PCPT", true, ModelKeyMap.makeKeyMapList("couponCode", "couponCode"));
                List<EntityCondition> productExps = FastList.newInstance();
                productExps.add(EntityCondition.makeCondition("productId", EntityOperator.IN, productList));
                productExps.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_ALL"));
                filedExprs.add(EntityCondition.makeCondition(productExps, EntityOperator.OR));
            }
            
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(filedExprs, EntityOperator.AND), null, null, null, findOpts);
            List<GenericValue> couponList = pli.getCompleteList();
            couponList = EntityUtil.filterByDate(couponList);
            //找到所有的优惠劵
            //过滤优惠劵： 包括商品的满的规则：优惠劵关联产品列表总金额比较
            //适用店铺: 没有商品的情况，该店铺的所有商品满足
            //全渠道店铺：所有传入的商品满赠
            List<Map> coupList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(couponList)) {
                for (int i = 0; i < couponList.size(); i++) {
                    GenericValue coupon = couponList.get(i);
                    String applyScope = coupon.getString("applyScope");
                    String couponProductType = coupon.getString("couponProductType");
                    BigDecimal totalProductPrice = BigDecimal.ZERO;
                    if ("A".equals(applyScope)) {
                        for (int j = 0; j < productList.size(); j++) {
                            String productId = productList.get(j);
                            Integer quantity = quantityList.get(j);
                            BigDecimal price = EntityUtil.getFirst(EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE")))).getBigDecimal("price");
                            totalProductPrice = totalProductPrice.add(price.multiply(new BigDecimal(quantity)));
                        }
                    } else {
                        String productStoreId = coupon.getString("productStoreId");
                        for (int j = 0; j < productList.size(); j++) {
                            String productId = productList.get(j);
                            Integer quantity = quantityList.get(j);
                            List<GenericValue> store = delegator.findByAnd("Product", UtilMisc.toMap("productId", productId, "productStoreId", productStoreId));
                            if (UtilValidate.isNotEmpty(store)) {
                                BigDecimal price = EntityUtil.getFirst(EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE")))).getBigDecimal("price");
                                totalProductPrice = totalProductPrice.add(price.multiply(new BigDecimal(quantity)));
                            }
                        }
                    }
                    if (totalProductPrice.compareTo(new BigDecimal(coupon.getLong("payFill"))) >= 0) {
                        Map<String, Object> couponMap = coupon.toMap();
                        couponMap.put("useBeginDate", couponMap.get("fromDate"));
                        couponMap.put("useEndDate", couponMap.get("thruDate"));
                        couponMap.put("type", "coupon");
                        coupList.add(couponMap);
                    }
                }
            }
            
            //获取代金劵
            List<GenericValue> ticketList = delegator.findByAnd("TicketAndProduct", UtilMisc.toMap("partyId", partyId, "ticketStatus", "notUsed"));
            ticketList = EntityUtil.filterByDate(ticketList, UtilDateTime.nowTimestamp(), "useStartTime", "useEndTime", true);
            if (UtilValidate.isNotEmpty(ticketList)) {
                for (int i = 0; i < ticketList.size(); i++) {
                    boolean hasIn = false;
                    GenericValue ticket = ticketList.get(i);
                    String productId = ticket.getString("productId");
                    String productStoreId = "";
                    
                        productStoreId = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId)).getString("productStoreId");
                    
                    //劵适应的商品
                    List<GenericValue> products = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_CONF"));
                    
                    if (UtilValidate.isNotEmpty(productList) && UtilValidate.isNotEmpty(products)) {
                        
                        if (!hasIn) {
                            for (int j = 0; j < productList.size(); j++) {
                                String toProductId = productList.get(j);
                                if (!hasIn) {
                                    for (int k = 0; k < products.size(); k++) {
                                        GenericValue productAssoc = products.get(k);
                                        if (toProductId.equalsIgnoreCase(productAssoc.getString("productIdTo"))) {
                                            Map<String, Object> ticketMap = FastMap.newInstance();
                                            ticketMap.put("productPromoCodeId", ticket.getString("ticketId"));
                                            ticketMap.put("partyId", partyId);
                                            ticketMap.put("getDate", ticket.getTimestamp("createdStamp"));
                                            ticketMap.put("useDate", ticket.getTimestamp("useDate"));
//                    G领取未过期,"D":过期 "U"已经使用
                                            if (ticket.getString("ticketStatus").equals("hasUsed")) {
                                                ticketMap.put("promoCodeStatus", "U");
                                            } else if (ticket.getString("ticketStatus").equals("notUsed")) {
                                                Timestamp endDate = ticket.getTimestamp("useEndTime");
                                                if (endDate.getTime() < UtilDateTime.nowTimestamp().getTime()) {
                                                    ticketMap.put("promoCodeStatus", "D");
                                                } else {
                                                    ticketMap.put("promoCodeStatus", "G");
                                                }
                                            }
                                            
                                            ticketMap.put("couponCode", ticket.getString("ticketNo"));
                                            ticketMap.put("fromDate", ticket.getTimestamp("useStartTime"));
                                            ticketMap.put("thruDate", ticket.getTimestamp("useEndTime"));
                                            ticketMap.put("couponName", ticket.getString("productName"));
                                            ticketMap.put("couponType", ticket.getString("VIRTUAL_GOOD"));
                                            ticketMap.put("payFill", ticket.getString(""));
                                            ticketMap.put("payReduce", ticket.getBigDecimal("voucherAmount").setScale(2, BigDecimal.ROUND_HALF_UP));
                                            ticketMap.put("applyScope", ticket.getString("A"));
                                            ticketMap.put("couponProductType", "COUPON_PRT_PART_IN");
                                            
                                            ticketMap.put("useBeginDate", ticket.getTimestamp("useStartTime"));
                                            ticketMap.put("useEndDate", ticket.getTimestamp("useEndTime"));
                                            ticketMap.put("type", "ticket");
                                            ticketMap.put("productStoreId", productStoreId);
                                            coupList.add(ticketMap);
                                            hasIn = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (UtilValidate.isEmpty(products)) {
                        
                        //没有商品说明是整个店铺的商品
                        List<GenericValue> pstores = delegator.findList("Product", EntityCondition.makeCondition("productId", EntityOperator.IN, productList), UtilMisc.toSet("productStoreId"), null, null, false);
                        if (UtilValidate.isNotEmpty(pstores)) {
                            List<String> pstoreIds = FastList.newInstance();
                            for (int j = 0; j < pstores.size(); j++) {
                                GenericValue pstore = pstores.get(j);
                                pstoreIds.add(pstore.getString("productStoreId"));
                            }
                            if (UtilValidate.isNotEmpty(pstoreIds)) {
                               
                                    String pstoreId = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId)).getString("productStoreId");
                                    for (int j = 0; j < pstoreIds.size(); j++) {
                                        String id = pstoreIds.get(j);
                                        if (id.equalsIgnoreCase(pstoreId)) {
                                            Map<String, Object> ticketMap = FastMap.newInstance();
                                            String orderId = ticket.getString("orderId");
                                            GenericValue order = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
                                            if (UtilValidate.isNotEmpty(order)) {
                                                ticketMap.put("getDate", order.getTimestamp("orderDate"));
                                            }
                                            ticketMap.put("productPromoCodeId", ticket.getString("ticketId"));
                                            ticketMap.put("partyId", partyId);
                                            
                                            ticketMap.put("useDate", ticket.getTimestamp("useDate"));
                                            //G领取未过期,"D":过期 "U"已经使用
                                            if (ticket.getString("ticketStatus").equals("hasUsed")) {
                                                ticketMap.put("promoCodeStatus", "U");
                                            } else if (ticket.getString("ticketStatus").equals("notUsed")) {
                                                Timestamp endDate = ticket.getTimestamp("useEndTime");
                                                if (endDate.getTime() < UtilDateTime.nowTimestamp().getTime()) {
                                                    ticketMap.put("promoCodeStatus", "D");
                                                } else {
                                                    ticketMap.put("promoCodeStatus", "G");
                                                }
                                            }
                                            
                                            ticketMap.put("couponCode", ticket.getString("ticketNo"));
                                            ticketMap.put("fromDate", ticket.getTimestamp("useStartTime"));
                                            ticketMap.put("thruDate", ticket.getTimestamp("useEndTime"));
                                            ticketMap.put("couponName", ticket.getString("productName") == null ? "" : ticket.getString("productName"));
                                            ticketMap.put("couponType", "VIRTUAL_GOOD");
                                            ticketMap.put("payFill", ticket.getString(""));
                                            ticketMap.put("payReduce", ticket.getBigDecimal("voucherAmount").setScale(2, BigDecimal.ROUND_HALF_UP));
                                            ticketMap.put("applyScope", ticket.getString("A"));
                                            ticketMap.put("couponProductType", "COUPON_PRT_PART_IN");
                                            ticketMap.put("productStoreId", productStoreId);
                                            ticketMap.put("useBeginDate", ticket.getTimestamp("useStartTime"));
                                            ticketMap.put("useEndDate", ticket.getTimestamp("useEndTime"));
                                            ticketMap.put("type", "ticket");
                                            coupList.add(ticketMap);
                                            break;
                                        }
                                    
                                }
                            }
                        }
                        
                    }
                }
            }
            resultData.put("coupons", coupList);
            
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (pli != null) {
                try {
                    pli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
            
            }
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 用户浏览历史
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    @RequestMapping(value = "/browseHistorys", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> browseHistorys(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String webSiteId = request.getHeader("client");
        String viewIndex = request.getParameter("viewIndex");
        String viewSize = request.getParameter("viewSize");
        
        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        List<Map> recordsList = FastList.newInstance();
        
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator pli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            String partyId = "";
            if (UtilValidate.isNotEmpty(loginName)) {
                GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
                if (UtilValidate.isNotEmpty(userLogin)) {
                    partyId = userLogin.getString("partyId");
                }
            }
            
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PBH", "PartyBrowseHistory");
            dynamicView.addAlias("PBH", "partyId");
            dynamicView.addAlias("PBH", "partyBrowseHistoryId");
            dynamicView.addAlias("PBH", "createdStamp");
            dynamicView.addAlias("PBH", "productId");
            
            dynamicView.addMemberEntity("U", "UserLogin");
            dynamicView.addAlias("U", "partyId");
            dynamicView.addViewLink("PBH", "U", false, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
            
            fieldsToSelect.add("partyBrowseHistoryId");
            fieldsToSelect.add("partyId");
            fieldsToSelect.add("createdStamp");
            fieldsToSelect.add("productId");
            dynamicView.setGroupBy(fieldsToSelect);
            orderBy.add("-createdStamp");
            
            andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            lowIndex = Integer.parseInt(viewIndex) * Integer.parseInt(viewSize) + 1;
            highIndex = (Integer.parseInt(viewIndex) + 1) * Integer.parseInt(viewSize);
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            
            for (GenericValue gv : pli.getPartialList(lowIndex, Integer.parseInt(viewSize))) {
                Map map = FastMap.newInstance();
                String productId = gv.getString("productId");
                String createdStamp = gv.getString("createdStamp");
                String partyBrowseHistoryId = gv.getString("partyBrowseHistoryId");
                map.put("productId", productId);
                map.put("createdStamp", createdStamp);
                map.put("browseHistoryId", partyBrowseHistoryId);
                
                if (UtilValidate.isNotEmpty(productId)) {
                    Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", productId.toString()));
                    List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                    if (UtilValidate.isNotEmpty(products)) {
                        List productsList = FastList.newInstance();
                        for (int j = 0; j < products.size(); j++) {
                            Map<String, Object> product = products.get(j);
                            Map<String, Object> productMap = FastMap.newInstance();
                            String isInner = (String) product.get("isInner");
                            if (UtilValidate.isNotEmpty(isInner)) {
                                if (isInner.equals("Y")) {
                                    productMap.put("isInner", isInner);
                                }
                            }
                            productMap.put("productId", product.get("productId"));
                            productMap.put("productName", product.get("productName"));
                            productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                            BigDecimal activityPrice = BigDecimal.ZERO;
                            BigDecimal amount = BigDecimal.ZERO;
                            Map<String, Object> orderGroupInfos = (Map<String, Object>) product.get("orderGroupInfo");
                            Map<String, Object> secKillInfo = (Map<String, Object>) product.get("secKillInfo");
                            Map<String, Object> priceDownInfo = (Map<String, Object>) product.get("priceDownInfo");
                            if (UtilValidate.isNotEmpty(orderGroupInfos)) {
                                if (orderGroupInfos.size() > 0) {
                                    activityPrice = ((BigDecimal) orderGroupInfos.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", activityPrice);
                                }
                            }
                            if (UtilValidate.isNotEmpty(secKillInfo)) {
                                if (secKillInfo.size() > 0) {
                                    activityPrice = ((BigDecimal) secKillInfo.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", activityPrice);
                                }
                            }
                            if (UtilValidate.isNotEmpty(priceDownInfo)) {
                                if (priceDownInfo.size() > 0) {
                                    amount = ((BigDecimal) priceDownInfo.get("amount")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", amount);
                                }
                            }
                            if (UtilValidate.isEmpty(orderGroupInfos) || UtilValidate.isEmpty(secKillInfo) || UtilValidate.isEmpty(priceDownInfo)) {
                                BigDecimal price = ((BigDecimal) product.get("price")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                if (UtilValidate.isEmpty(price)) {
                                    price = BigDecimal.ZERO;
                                }
                                productMap.put("price", price);
                            }
                            
                            productsList.add(productMap);
                        }
                        map.put("products", productsList);
                    }
                    
                }
                
                recordsList.add(map);
            }
            size = pli.getResultsSizeAfterPartialList();
            
            resultData.put("recordsList", recordsList);
            resultData.put("totalSize", size);
            resultData.put("retCode", 1);
            resultData.put("message", "操作成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (pli != null) {
                try {
                    pli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
            
            }
        }

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 用户浏览历史 删除操作
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    
    @RequestMapping(value = "/delBrowseHistorys", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> delBrowseHistorys(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String isAllClear = request.getParameter("isAllClear");
        String browseHistoryId = request.getParameter("browseHistoryId");
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        try {
            resultData = dispatcher.runSync("deletePartyBrowseHistorys", UtilMisc.toMap("userLoginId", loginName, "isAllClear", isAllClear, "browseHistoryId", browseHistoryId));
            if (ServiceUtil.isError(resultData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 用户浏览历史保存
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    
    @RequestMapping(value = "/savePartyBrowse", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> savePartyBrowse(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String productIds = request.getParameter("productIds");
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        try {
            resultData = dispatcher.runSync("savePartyBrowse", UtilMisc.toMap("userLoginId", loginName, "productIds", productIds));
            if (ServiceUtil.isError(resultData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 用户投诉建议保存
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    
    @RequestMapping(value = "/createFeedback", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createFeedback(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String imageIds = request.getParameter("imageIds");
        String feedbackContent = request.getParameter("feedbackContent");
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        try {
            resultData = dispatcher.runSync("createFeedback", UtilMisc.toMap("userLoginId", loginName, "imageIds", imageIds, "feedbackContent", feedbackContent));
            if (ServiceUtil.isError(resultData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 用户投诉建议列表
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    
    @RequestMapping(value = "/getFeedBackList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getFeedBackList(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String viewIndex = request.getParameter("viewIndex");
        String viewSize = request.getParameter("viewSize");
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        try {
            resultData = dispatcher.runSync("getFeedBackList", UtilMisc.toMap("userLoginId", loginName, "viewIndex", viewIndex, "viewSize", viewSize));
            if (ServiceUtil.isError(resultData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    
    /**
     * 用户积分赠送处理
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    
    @RequestMapping(value = "/useScoreCodeSend", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> useScoreCodeSend(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String type = request.getParameter("sendType");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String webSiteId = request.getHeader("client");
        String memberId = request.getParameter("memberId");
        String mallId = request.getParameter("mallId");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        try {
            if (UtilValidate.isNotEmpty(type)) {
                resultData = dispatcher.runSync("userScoreCodeSend", UtilMisc.toMap("sendType", type, "userLoginId", loginName, "memberId", memberId, "mallId", mallId));
                if (ServiceUtil.isError(resultData)) {
                    resultData.put("retCode", 0);
                    resultData.put("message", ServiceUtil.getErrorMessage(resultData));
                } else {
                    resultData.put("retCode", 1);
                    resultData.put("message", "操作成功");
                }
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    
}
