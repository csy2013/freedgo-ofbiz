package com.qihua.ofbiz.integral;

import com.alibaba.druid.sql.visitor.functions.Char;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericPK;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/5/7.
 */
public class partyIntegralGiftService {
    /**
     * 积分赠送查询
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> findIntegralGiftes(DispatchContext ctx, Map<String, ? extends Object> context) {
        //1.定义返回值result(Map类型)
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //获取调度器
        LocalDispatcher dispatcher = ctx.getDispatcher();
        //获取委托对象，可以对数据库进行增删改查
        Delegator delegator = ctx.getDelegator();
        //定义积分赠送产品的List集合
        List<GenericValue> orderIntegralRuleProds = FastList.newInstance();
        //定义积分赠送产品对象
        GenericValue PartyGiftProd = null;
        try {
            //查询积分赠送表partyIntegralStatus的数据
            List<GenericValue> orderIntegralRules = delegator.findList("OrderIntegralRule", null, null, null, null, false);
            //查询积分赠送产品表partyIntegralGifts的数据
            orderIntegralRuleProds = delegator.findList("OrderIntegralRuleProd", null, null, null, null, false);
            //获取积分赠送的第一个对象
            if (UtilValidate.isNotEmpty(orderIntegralRules) && UtilValidate.isNotEmpty(orderIntegralRuleProds)) {
                GenericValue orderIntegralRule = orderIntegralRules.get(0);
                //将积分赠送对象和积分赠送产品对象放进Map中返回
                result.put("orderIntegralRule", orderIntegralRule);
                result.put("orderIntegralRuleProd", orderIntegralRuleProds);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    //查找首次积分赠送信息回显到页面findIntegralNewService
    public static Map<String, Object> findIntegralNewService(DispatchContext ctx, Map<String, ? extends Object> context) {
        //1.定义返回值result(Map类型)
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //获取调度器
        LocalDispatcher dispatcher = ctx.getDispatcher();
        //获取委托对象，可以对数据库进行增删改查
        Delegator delegator = ctx.getDelegator();
        //定义积分赠送产品的List集合
        List<GenericValue> orderIntegralRuleProds = FastList.newInstance();
        //定义积分赠送产品对象
        GenericValue PartyGiftProd = null;
        try {
            //查询积分赠送表partyIntegralGifts的数据
            EntityCondition mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("status", EntityOperator.EQUALS, "1")
                    )
                    , EntityOperator.AND);
            List<GenericValue> orderIntegralRules = delegator.findList("OrderIntegralRule", mainCond, null, null, null, false);
            if (UtilValidate.isNotEmpty(orderIntegralRules)) {
                GenericValue orderIntegralRule = orderIntegralRules.get(0);
                String partyIntegralGiftId= orderIntegralRule.getString("partyIntegralGiftId");
                orderIntegralRuleProds=delegator.findByAnd("OrderIntegralRuleProd",UtilMisc.toMap("partyIntegralGiftId",partyIntegralGiftId));
                //将积分赠送对象和积分赠送产品对象放进Map中返回
                result.put("orderIntegralRule", orderIntegralRule);
                result.put("orderIntegralRuleProd", orderIntegralRuleProds);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 积分赠送SaveOrUpdate
     */
    public static Map<String, Object> saveOrUpdateGiftes(DispatchContext ctx, Map<String, ? extends Object> context) {
        //1.通过serviceUtil获取result返回对象
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //2.获得数据库操作对象
        Delegator delegator = ctx.getDelegator();
        //获得调度器对象
        LocalDispatcher dispatcher = ctx.getDispatcher();
        //定义积分赠送产品集合
        List<GenericValue> orderIntegralRuleProds = FastList.newInstance();
        //定义积分赠送产品对象
        GenericValue PartyGiftProd = null;
        try {
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("status", "0");
            updateFields.put("endTime",  new Timestamp(System.currentTimeMillis()));
            EntityCondition UpdateCon = EntityCondition.makeCondition("status", EntityComparisonOperator.EQUALS, "1");
            delegator.storeByCondition("OrderIntegralRule", updateFields, UpdateCon);

            //这是如果partyIntegralGifts为空并且partyGiftProds为空就进行增加
            GenericValue orderIntegralRule = delegator.makeValue("OrderIntegralRule");
            String isFullOpen = (String) context.get("isFullOpen");
            String isAssignOpen = (String) context.get("isAssignOpen");
            BigDecimal orderMoney = (BigDecimal) context.get("orderMoney");

            String productLineList = (String) context.get("productLineList");
            orderIntegralRule.setNextSeqId();
            orderIntegralRule.set("isFullOpen", isFullOpen);
            orderIntegralRule.set("isAssignOpen", isAssignOpen);
            orderIntegralRule.set("orderMoney", orderMoney);
            orderIntegralRule.set("startTime", new Timestamp(System.currentTimeMillis()));
            orderIntegralRule.set("status", "1");
            orderIntegralRule.create();
            String partyIntegralGiftId = (String) orderIntegralRule.get("partyIntegralGiftId");
            //遍历商品信息
            List<String> productLines = StringUtil.split(productLineList, "|");
            int i = 0;
                //遍历商品信息，获取每个商品赋给PartyGiftProd,然后把PartyGiftProd放进PartyGiftProds中
                if (UtilValidate.isNotEmpty(productLines)) {
                    for (String prodLine : productLines) {
                    GenericValue orderIntegralRuleProd = delegator.makeValue("OrderIntegralRuleProd");
                    String[] lineStrs = prodLine.split(",");
                    String productId = lineStrs[0];
                    String integralCodeNo = lineStrs[1];
                    orderIntegralRuleProd.setNextSeqId();
                    orderIntegralRuleProd.set("partyGiftProdId", productId);
                    orderIntegralRuleProd.set("partyIntegralGiftId", partyIntegralGiftId);
                    orderIntegralRuleProd.set("integralCodeNo", integralCodeNo);
                    orderIntegralRuleProd.create();
                }
//                orderIntegralRuleProds = delegator.findList("OrderIntegralRuleProd", null, null, null, null, false);
            }

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }
}
