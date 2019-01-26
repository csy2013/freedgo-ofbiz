/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.yuaoq.yabiz.mobile.services;

import com.google.gson.Gson;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.CouponUtils;
import com.yuaoq.yabiz.mobile.common.ProductUtils;
import com.yuaoq.yabiz.mobile.services.kdmall.KdRetData;
import com.yuaoq.yabiz.weixin.app.template.Message;
import com.yuaoq.yabiz.weixin.common.util.JsonMapper;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.order.OrderChangeHelper;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.product.product.TicketEvents;
import org.ofbiz.service.*;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Order Processing Services
 */

public class CouponService {

    @Value("${image.base.url}")
    public static String baseImgUrl;

    public static final String module = CouponService.class.getName();


    /**
     * 代金券定时通知，24小时通知用户
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> promoCodeTask(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        List<EntityCondition> exps = FastList.newInstance();

        //查找距离过期还剩24小时的未使用的代金券进行通知
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PPC", "ProductPromoCode");
        dynamicView.addAlias("PPC", "productPromoCodeId");
        dynamicView.addAlias("PPC", "fromDate");
        dynamicView.addAlias("PPC", "thruDate");
        dynamicView.addAlias("PPC", "promoCodeStatus");
        dynamicView.addAlias("PPC", "isNotify");

        dynamicView.addMemberEntity("PPCP", "ProductPromoCodeParty");
        dynamicView.addAlias("PPCP", "productPromoCodeId");
        dynamicView.addAlias("PPCP", "partyId");
        dynamicView.addViewLink("PPC", "PPCP", false, ModelKeyMap.makeKeyMapList("productPromoCodeId", "productPromoCodeId"));

        Timestamp today = new Timestamp(System.currentTimeMillis());
        Timestamp tomorrow = new Timestamp(System.currentTimeMillis()+(long)24*60*60*1000);
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("thruDate",  EntityOperator.LESS_THAN,tomorrow));
        conditions.add(EntityCondition.makeCondition("thruDate",  EntityOperator.GREATER_THAN,today));
        conditions.add(EntityCondition.makeCondition("isNotify",  EntityOperator.EQUALS,null));
        conditions.add(EntityCondition.makeCondition("promoCodeStatus",  EntityOperator.EQUALS,"G"));

        EntityListIterator eli = null;
        try {
            eli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, null);
            List<GenericValue> coupons = eli.getCompleteList();

            GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "COUPON_LOSEDATE_NOTIFY")));
            if(UtilValidate.isEmpty(config)){
                return resultData;
            }
            String template_id = config.getString("wxLiteTemplateId");
            for(GenericValue coupon:coupons){
                String partyId = coupon.getString("partyId");
                String  thruDate =CommonUtils.getStringDate(coupon.getTimestamp("thruDate")) ;
                String productPromoCodeId = coupon.getString("productPromoCodeId");
                Map couponInfo = CouponUtils.getCouponByPromoCodeId(productPromoCodeId,delegator);
                String sendwxAppOpenId = CommonUtils.getWxAppOpenId(delegator, partyId);
                Map<String, Object> daMap = FastMap.newInstance();
                daMap.put("keyword1",new Message.Data("您有一张代金券即将到期，请尽快使用。","") );
                daMap.put("keyword2", new Message.Data("满"+Double.parseDouble((String) couponInfo.get("payFill"))+"元减"+Double.parseDouble((String)couponInfo.get("payReduce"))+"元",""));
                daMap.put("keyword3", new Message.Data(thruDate,""));
                String daJson = JsonMapper.defaultMapper().toJson(daMap);
                try {
                    resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", sendwxAppOpenId, "template_id", template_id, "page", "/pages/webview/index?path=my-cards", "form_id", "", "data",daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", sendwxAppOpenId, "objectValueId", productPromoCodeId));
                } catch (Exception e) {
                    Debug.log(e.getMessage());
                }

                //修改代金券
                Map<String, Object> updateFields = FastMap.newInstance();
                updateFields.put("isNotify", "Y");
                EntityCondition UpdateCon = EntityCondition.makeCondition("productPromoCodeId", EntityComparisonOperator.EQUALS, productPromoCodeId);
                delegator.storeByCondition("ProductPromoCode", updateFields, UpdateCon);
            }

            List<EntityCondition> conditions2 = FastList.newInstance();
            conditions2.add(EntityCondition.makeCondition("useEndTime",  EntityOperator.NOT_EQUAL,null));
            conditions2.add(EntityCondition.makeCondition("useEndTime",  EntityOperator.LESS_THAN,tomorrow));
            conditions2.add(EntityCondition.makeCondition("useEndTime",  EntityOperator.GREATER_THAN,today));
            conditions2.add(EntityCondition.makeCondition("isNotify",  EntityOperator.EQUALS,null));
            List<GenericValue> tickets = delegator.findList("TicketAndProduct",EntityCondition.makeCondition(conditions2, EntityOperator.AND),null,null,null,false);
            if(tickets==null||tickets.size()==0){
                return resultData;
            }
            for(GenericValue ticket:tickets){
                String  thruDate =CommonUtils.getStringDate(ticket.getTimestamp("useEndTime")) ;
                BigDecimal amount = ticket.getBigDecimal("amount");
                String partyId=ticket.getString("partyId");
                String ticketId=ticket.getString("ticketId");
                String sendwxAppOpenId = CommonUtils.getWxAppOpenId(delegator, partyId);
                Map<String, Object> daMap = FastMap.newInstance();
                daMap.put("keyword1",new Message.Data("您有一张代金券即将到期，请尽快使用。","") );
                daMap.put("keyword2", new Message.Data(amount.doubleValue()+"元",""));
                daMap.put("keyword3", new Message.Data(thruDate,""));
                String daJson = JsonMapper.defaultMapper().toJson(daMap);
                try {
                    resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", sendwxAppOpenId, "template_id", template_id, "page", "/pages/webview/index?path=my-cards", "form_id", "", "data",daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", sendwxAppOpenId, "objectValueId", ticketId));
                } catch (Exception e) {
                    Debug.log(e.getMessage());
                }
                //修改代金券
                Map<String, Object> updateFields = FastMap.newInstance();
                updateFields.put("isNotify", "Y");
                EntityCondition UpdateCon = EntityCondition.makeCondition("ticketId", EntityComparisonOperator.EQUALS, ticketId);
                delegator.storeByCondition("Ticket", updateFields, UpdateCon);

            }


        } catch (GenericEntityException e) {
            e.printStackTrace();
        } finally {
            if (eli != null) {
                try {
                    eli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
        }


        return resultData;
    }


}

