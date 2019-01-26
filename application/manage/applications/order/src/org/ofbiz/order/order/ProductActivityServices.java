package org.ofbiz.order.order;

import javolution.util.FastList;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by changsy on 2018/4/22.
 */
public class ProductActivityServices {
    
    /**
     * 检查团购状态
     
     * @param productStoreId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> validateTogetherStatus(String partyId, String activityId,GenericValue  orderItem, String productStoreId, Delegator delegator) throws GenericEntityException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Map serviceIn = UtilMisc.toMap("activityType", "GROUP_ORDER", "activityAuditStatus", "ACTY_AUDIT_PASS", "productStoreId", productStoreId, "productId", orderItem.get("productId"),"activityId",activityId);
        List<GenericValue> productActivities = delegator.findByAnd("ProductActivityInfo", serviceIn);
        productActivities = EntityUtil.filterByDate(productActivities,UtilDateTime.nowTimestamp(),"activityStartDate","activityEndDate",true);
        GenericValue goods = delegator.findByPrimaryKey("ProductActivityGoods",UtilMisc.toMap("activityId",activityId,"productId",orderItem.get("productId")));
        if(UtilValidate.isEmpty(productActivities) || UtilValidate.isEmpty(goods)){
            return ServiceUtil.returnError("活动信息不正确");
        }else{
            GenericValue productActivity = productActivities.get(0);
            Long limitQuantity = productActivity.getLong("limitQuantity");
            //判断用户是不是已经购买过
            if(UtilValidate.isNotEmpty(limitQuantity)){
                List<EntityCondition> exps = FastList.newInstance();
                exps.add(EntityCondition.makeCondition("partyId",partyId));
                exps.add(EntityCondition.makeCondition("activityId",activityId));
                exps.add(EntityCondition.makeCondition("statusId",EntityOperator.NOT_EQUAL,"ORDER_RETURNED"));
                exps.add(EntityCondition.makeCondition("roleTypeId",EntityOperator.EQUALS,"PLACING_CUSTOMER"));
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
                List<GenericValue> orders = delegator.findList("OrderHeaderAndRole",EntityCondition.makeCondition(exps,EntityOperator.AND),null,null,findOpts,false);
                if(UtilValidate.isNotEmpty(orders)){
                    if(orders.size()>=limitQuantity.intValue()){
                        return ServiceUtil.returnError("用户超出该团购活动的用户参与次数");
                    }
                }
            }
            
            BigDecimal activityPrice = goods.getBigDecimal("activityPrice");
            BigDecimal totalPrice = activityPrice.multiply(orderItem.getBigDecimal("quantity"));
            resultData.put("totalPrice",totalPrice);
            resultData.put("activityPrice",activityPrice);
        }
        return resultData;
    }
    
    /**
     * 检查秒杀状态，并返回秒杀价格
     * @param productStoreId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> validateSeckillStatus(String partyId, String activityId,GenericValue orderItem, String productStoreId, Delegator delegator) throws GenericEntityException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Map serviceIn = UtilMisc.toMap("activityType", "SEC_KILL", "activityAuditStatus", "ACTY_AUDIT_PASS", "productStoreId", productStoreId, "productId", orderItem.get("productId"),"activityId",activityId);
        List<GenericValue> productActivities = delegator.findByAnd("ProductActivityInfo", serviceIn);
        productActivities = EntityUtil.filterByDate(productActivities,UtilDateTime.nowTimestamp(),"activityStartDate","activityEndDate",true);
        GenericValue goods = delegator.findByPrimaryKey("ProductActivityGoods",UtilMisc.toMap("activityId",activityId,"productId",orderItem.get("productId")));
        if(UtilValidate.isEmpty(productActivities) || UtilValidate.isEmpty(goods)){
            return ServiceUtil.returnError("秒杀活动信息不正确");
        }else{
            GenericValue productActivity = productActivities.get(0);
            Long limitQuantity = productActivity.getLong("limitQuantity");
            //判断用户是不是已经购买过
            if(UtilValidate.isNotEmpty(limitQuantity)){
                List<EntityCondition> exps = FastList.newInstance();
                exps.add(EntityCondition.makeCondition("partyId",partyId));
                exps.add(EntityCondition.makeCondition("activityId",activityId));
                exps.add(EntityCondition.makeCondition("statusId",EntityOperator.NOT_EQUAL,"ORDER_RETURNED"));
                exps.add(EntityCondition.makeCondition("roleTypeId",EntityOperator.EQUALS,"PLACING_CUSTOMER"));
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
                List<GenericValue> orders = delegator.findList("OrderHeaderAndRole",EntityCondition.makeCondition(exps,EntityOperator.AND),null,null,findOpts,false);
                if(UtilValidate.isNotEmpty(orders)){
                    if(orders.size()>=limitQuantity.intValue()){
                        return ServiceUtil.returnError("用户超出该秒杀活动的用户参与次数");
                    }
                }
            }
            //检查秒杀活动的数量activityQuantity
            Long occupiedQuantityTotal = productActivity.getLong("occupiedQuantityTotal")==null? 0L :productActivity.getLong("occupiedQuantityTotal");
            Long hasBuyQuantity =  productActivity.getLong("hasBuyQuantity")==null?0:productActivity.getLong("hasBuyQuantity");
            Long activityQuantity = productActivity.getLong("activityQuantity")==null?0:productActivity.getLong("activityQuantity");;
            if(activityQuantity <= (occupiedQuantityTotal+hasBuyQuantity)){
                return ServiceUtil.returnError("该秒杀活动的商品数量不足");
            }
            BigDecimal activityPrice = goods.getBigDecimal("activityPrice");
            BigDecimal totalPrice = activityPrice.multiply(orderItem.getBigDecimal("quantity"));
            resultData.put("totalPrice",totalPrice);
            resultData.put("activityPrice",activityPrice);
        }
        return resultData;
    }
}
