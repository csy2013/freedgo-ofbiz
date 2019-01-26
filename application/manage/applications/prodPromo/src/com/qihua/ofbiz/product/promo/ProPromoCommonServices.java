package com.qihua.ofbiz.product.promo;

import javolution.util.FastList;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ProPromoCommonServices {
    /**
     * 校验同一件商品只能参加直降、秒杀、拼团其中一种促销活动，之前如果参加过就不能参加了。
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> checkGoodIsUsed(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();

        String productStoreId = (String) context.get("productStoreId");
        //商品id
        String id = (String) context.get("id");
        //校验在直降，秒杀，团购中是否已经出现。（有效期内）
        //在规定的时间内
        Timestamp nowTime = UtilDateTime.nowTimestamp();

        //直降
        List<EntityCondition> andExprs = FastList.newInstance();
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoType"), EntityOperator.EQUALS, "PROMO_SPE_PRICE"));//类型为直降
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//

        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN, nowTime));
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("thruDate"), EntityOperator.GREATER_THAN, nowTime));
        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);

        EntityListIterator pli = delegator.find("ProductStorePromoAndAppl",mainCond,null,null,null,null);
        List<GenericValue> promoList = pli.getCompleteList();
        pli.close();
        if(promoList!=null && promoList.size()>0){
            for(GenericValue promo:promoList){
                String productPromoId =promo.getString("productPromoId");
                List<GenericValue> actions = delegator.findByAnd("ProductPromoAction",UtilMisc.toMap("productPromoId",productPromoId,"productId",id));
                if(actions!=null && actions.size()>0){
                    result.put("isExisted","Y");
                    return result;
                }
            }
        }
        //检查是否被其他团购或者秒杀使用  ProductStorePromoAndAct
        List<EntityCondition> andExprs2 = FastList.newInstance();
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN, nowTime));
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("thruDate"), EntityOperator.GREATER_THAN, nowTime));
        EntityCondition mainCond2 = EntityCondition.makeCondition(andExprs2, EntityOperator.AND);

        EntityListIterator pli2 = delegator.find("ProductStorePromoAndAct",mainCond2,null,null,null,null);
        List<GenericValue> actList = pli2.getCompleteList();

        for(GenericValue act:actList){
            String activityId = act.getString("activityId");
            List<GenericValue> acts = delegator.findByAnd("ProductActivityGoods",UtilMisc.toMap("activityId",activityId,"productId",id));
            if(acts!=null && acts.size()>0){
                result.put("isExisted","Y");
                return result;
            }
        }
        result.put("isExisted","N");
        return result;
    }

    public Map<String, Object> getAuditLog(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        String businessId = (String) context.get("businessId");

        List<String> orderBy =FastList.newInstance();
        orderBy.add("-createDate");
        List<GenericValue> auditLogs = delegator.findByAnd("PromoAuditLog",UtilMisc.toMap("businessId",businessId),orderBy);
        List<Map> auditLogList = FastList.newInstance();
        if(auditLogs==null || auditLogs.size()==0){
            result.put("auditLogList",auditLogList);
        }else{
            for(GenericValue auditLog:auditLogs){
                Map auditLogMap = auditLog.toMap();
                String auditPerson = (String) auditLogMap.get("auditPerson");
                String auditPersonName = getUserNick(auditPerson,delegator);
                auditLogMap.put("auditPersonName",auditPersonName);
                auditLogMap.put("auditTime",getStringDate((Timestamp) auditLogMap.get("createDate")));
                auditLogMap.put("auditMessage",auditLogMap.get("auditMessage")==null?"":auditLogMap.get("auditMessage"));
                auditLogList.add(auditLogMap);
            }
            result.put("auditLogList",auditLogList);
        }

        return result;
    }
    public static String getStringDate(Timestamp timestamp){
        if(timestamp==null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp);
    }
    public static String getUserNick(String partyId, Delegator delegator){
        try {
            if(UtilValidate.isEmpty(partyId)){
                return "";
            }
            GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
            if(person==null){
                return "";
            }
            return person.getString("nickname");
        } catch (GenericEntityException e) {
            return "";
        }

    }
}
