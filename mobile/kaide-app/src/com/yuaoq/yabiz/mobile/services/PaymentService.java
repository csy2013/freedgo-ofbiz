package com.yuaoq.yabiz.mobile.services;

import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.ProductUtils;
import com.yuaoq.yabiz.weixin.app.template.Message;
import com.yuaoq.yabiz.weixin.common.util.JsonMapper;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ofbiz.service.LocalDispatcher;

public class PaymentService {

    public static Map<String, Object> completePay(DispatchContext dispatchContext, Map<String, ? extends Object> context) throws GenericEntityException, GenericServiceException {
        Map<String, Object> resultData = FastMap.newInstance();

        String orderGroupId = (String) context.get("orderGroupId");
        if (UtilValidate.isEmpty(orderGroupId)) {
            resultData.put("retCode", "0");
            resultData.put("message", "orderGroupId不能为空");
            return resultData;
        }

        Delegator delegator = dispatchContext.getDelegator();
        LocalDispatcher dispatcher = dispatchContext.getDispatcher();

        //查询orderGroup状态
        List<GenericValue> orderGroupList = delegator.findByAnd("OrderGroup", UtilMisc.toMap("orderGroupId", orderGroupId));
        if (orderGroupList == null || orderGroupList.size() == 0) {
            resultData.put("retCode", "0");
            resultData.put("message", "查询不到该订单");
            return resultData;
        }

        String orderGroupType = orderGroupList.get(0).getString("orderGroupType");
        switch (orderGroupType) {
            case "ORDER_GIFT":
                //赠送商品类型订单
                resultData = handleGiftOrder(orderGroupId, delegator);
                break;
            case "ORDER_WISH":
                //心愿单订单
                resultData = handleWishOrder(orderGroupId, delegator, dispatcher);
                break;
            case "ORDER_GROUP":
                //拼团订单
                resultData = handleGroupOrder(orderGroupId, delegator, dispatcher);
                break;
            default:
                //处理其他类型订单
                resultData.put("retCode", "1");
                resultData.put("message", "success");
                break;
        }

        return resultData;
    }

    /**
     * 心愿订单
     *
     * @param orderGroupId
     * @param delegator
     * @param dispatcher
     * @return
     * @throws GenericEntityException
     */
    private static Map<String, Object> handleWishOrder(String orderGroupId, Delegator delegator, LocalDispatcher dispatcher) throws GenericEntityException, GenericServiceException {
        Map<String, Object> resultData = FastMap.newInstance();
        List<GenericValue> wishList = delegator.findByAnd("PartyOrderWish", UtilMisc.toMap("orderGroupId", orderGroupId));
        GenericValue wish = wishList.get(0);
        if (!"WISH_WAIT_PAY".equals(wish.getString("status"))) {
            resultData.put("retCode", "0");
            resultData.put("message", "该订单不是未支付状态！");
            return resultData;
        }
        //删除用户心愿单
        String sendPartyId = wish.getString("sendPartyId");
        String achievePartyId = wish.getString("achievePartyId");
        String wishId = wish.getString("wishId");
        List<GenericValue> shoppingLists = delegator.findByAnd("ShoppingList", UtilMisc.toMap("partyId", sendPartyId, "shoppingListTypeId", "SLT_WISH_LIST"));
        if (shoppingLists != null && shoppingLists.size() > 0) {
            GenericValue shoppingList = shoppingLists.get(0);
            String shoppingListId = shoppingList.getString("shoppingListId");
            //查找心愿单产品id的集合

            List<GenericValue> wishDetails = delegator.findByAnd("PartyOrderWishDetail", UtilMisc.toMap("wishId", wishId));
            List<String> productIds = FastList.newInstance();
            for (GenericValue wishDetail : wishDetails) {
                String productId = wishDetail.getString("productId");
                productIds.add(productId);
            }
            EntityCondition deleCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("shoppingListId", EntityOperator.EQUALS, shoppingListId),
                            EntityCondition.makeCondition("productId", EntityOperator.IN, productIds)
                    )
                    , EntityOperator.AND);
            delegator.removeByCondition("ShoppingListItem", deleCond);

        }

        Map<String, Object> updateFields = FastMap.newInstance();
        updateFields.put("status", "WISH_HAS_ACHIEVE");
        updateFields.put("achieveDate", new Timestamp(System.currentTimeMillis()));
        EntityCondition UpdateCon = EntityCondition.makeCondition("wishId", EntityComparisonOperator.EQUALS, wishId);
        delegator.storeByCondition("PartyOrderWish", updateFields, UpdateCon);


        String wishDate = CommonUtils.getStringDate(wish.getTimestamp("sendDate"));
        //发送模板消息
        String sendwxAppOpenId = CommonUtils.getWxAppOpenId(delegator, sendPartyId);
        String achievewxAppOpenId = CommonUtils.getWxAppOpenId(delegator, achievePartyId);

        String sendNickName = CommonUtils.getUserNick(sendPartyId, delegator);
        String achieveNickName = CommonUtils.getUserNick(achievePartyId, delegator);

        GenericValue fromconfig = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "WISH_FROM_NOTIFY")));
        GenericValue toconfig = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "WISH_TO_NOTIFY")));

        if (UtilValidate.isNotEmpty(fromconfig) && UtilValidate.isNotEmpty(sendwxAppOpenId)) {
            String template_id = fromconfig.getString("wxLiteTemplateId");
            //发送给发起人
            String remark = "您的好友 " + achieveNickName + " 帮您实现了一条心愿，我们将尽快为您发货";
            Map<String, Object> daMap = FastMap.newInstance();
            daMap.put("keyword1", new Message.Data(remark, ""));
            daMap.put("keyword2", new Message.Data(wishDate, ""));
            String daJson = JsonMapper.defaultMapper().toJson(daMap);
            try {
                resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", sendwxAppOpenId, "template_id", template_id, "page", "/pages/webview/index?path=alreadyrealized&csi=share", "form_id", "", "data", daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", sendwxAppOpenId, "objectValueId", wishId));
            } catch (Exception e) {
                Debug.log(e.getMessage());
            }

        }

        if (UtilValidate.isNotEmpty(toconfig) && UtilValidate.isNotEmpty(achievewxAppOpenId)) {
            String template_id = toconfig.getString("wxLiteTemplateId");
            //发送给发起人
            String remark = "恭喜您成功实现了好友 " + sendNickName + " 的心愿单，我们将尽快为您的好友发货。";
            Map<String, Object> daMap = FastMap.newInstance();
            daMap.put("keyword1", new Message.Data(remark, ""));
            daMap.put("keyword2", new Message.Data(wishDate, ""));
            String daJson = JsonMapper.defaultMapper().toJson(daMap);
            try {
                resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", achievewxAppOpenId, "template_id", template_id, "page", "/pages/webview/index?path=alreadyrealizedf&csi=share", "form_id", "", "data", daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", achievewxAppOpenId, "objectValueId", wishId));
            } catch (Exception e) {
                Debug.log(e.getMessage());
            }
        }


        resultData.put("retCode", "1");
        resultData.put("message", "success");
        return resultData;
    }


    /**
     * 礼品订单完成后，需要修改礼品单状态为待赠送
     *
     * @param orderGroupId
     * @param delegator
     * @return
     */
    private static Map<String, Object> handleGiftOrder(String orderGroupId, Delegator delegator) throws GenericEntityException {
        Map<String, Object> resultData = FastMap.newInstance();
        List<GenericValue> partyOrderRelPresentList = delegator.findByAnd("PartyOrderRelPresent", UtilMisc.toMap("orderGroupId", orderGroupId));
        GenericValue partyOrderRelPresent = partyOrderRelPresentList.get(0);
        String status = partyOrderRelPresent.getString("status");
        if (!"GIFT_WAIT_PAY".equals(status)) {
            resultData.put("retCode", "0");
            resultData.put("message", "该订单不是未支付状态！");
            return resultData;
        }
        //修改赠送表状态
        partyOrderRelPresent.put("status", "GIFT_WAIT_SEND");
        partyOrderRelPresent.store();

        resultData.put("retCode", "1");
        resultData.put("message", "success");
        return resultData;
    }

    /**
     * 创建拼团订单 最后一个人支付成功后修改拼团状态
     *
     * @param orderGroupId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    private static Map<String, Object> handleGroupOrder(String orderGroupId, Delegator delegator, LocalDispatcher dispatcher) throws GenericEntityException {
        Map<String, Object> resultData = FastMap.newInstance();
        GenericValue orderGroup = delegator.findByPrimaryKey("OrderGroup", UtilMisc.toMap("orderGroupId", orderGroupId));
        if (UtilValidate.isNotEmpty(orderGroup)) {
            List<GenericValue> orderRels = delegator.findByAnd("OrderGroupOrderRel", UtilMisc.toMap("orderGroupId", orderGroupId));
            //所有的orders
            if (UtilValidate.isNotEmpty(orderRels)) {
                BigDecimal count = BigDecimal.ZERO;
                GenericValue orderRel = orderRels.get(0);
                String orderId = orderRel.getString("orderId");
                if (UtilValidate.isNotEmpty(orderId)) {
                    List<GenericValue> orderPayments = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
                    if (UtilValidate.isNotEmpty(orderPayments)) {
                        GenericValue orderPayment = orderPayments.get(0);
                        String statusId = orderPayment.getString("statusId");
                        if ("PAYMENT_RECEIVED".equals(statusId)) {
                            List<GenericValue> togetherGroupRels = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("orderId", orderId));
                            if (UtilValidate.isNotEmpty(togetherGroupRels)) {
                                GenericValue togetherGroupRel = togetherGroupRels.get(0);
                                String togetherId = togetherGroupRel.getString("togetherId");
                                if (UtilValidate.isNotEmpty(togetherId)) {
                                    GenericValue togetherGroup = delegator.findByPrimaryKey("TogetherGroup", UtilMisc.toMap("togetherId", togetherId));
                                    if (UtilValidate.isNotEmpty(togetherGroup)) {
                                        BigDecimal currentNum = new BigDecimal(togetherGroup.getString("currentNum"));
                                        BigDecimal limitUserNum = new BigDecimal(togetherGroup.getString("limitUserNum"));
                                        int personNum = currentNum.compareTo(limitUserNum);
                                        String togetherStatus = "";
                                        if (personNum < 0) {
                                            togetherStatus = "TOGETHER_RUNING";
                                            togetherGroup.set("togetherId", togetherId);
                                            togetherGroup.set("status", togetherStatus);
                                            togetherGroup.store();
                                        } else if (personNum == 0) {
                                            togetherStatus = "TOGETHER_DONE";
                                            togetherGroup.set("togetherId", togetherId);
                                            togetherGroup.set("status", togetherStatus);
                                            togetherGroup.store();

                                        }

                                    }
                                }

                                if (UtilValidate.isNotEmpty(togetherId)) {
                                    GenericValue togetherGroup = delegator.findByPrimaryKey("TogetherGroup", UtilMisc.toMap("togetherId", togetherId));
                                    Long limitUserNum = togetherGroup.getLong("limitUserNum");
                                    Long currentNum = togetherGroup.getLong("currentNum");
                                    int leavePersonNums = (int) (limitUserNum - currentNum);//剩余团购数量
                                    if(leavePersonNums>0){
                                        resultData.put("retCode", "1");
                                        resultData.put("message", "success");
                                        return resultData;
                                    }
                                    //真实参与的组团用户
                                    List<GenericValue> realTogetherGroupRels = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId));

                                    //通知微信通知模版消息
                                    GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "TOGETHER_SUCCESS_NOTIFY")));
                                    if (UtilValidate.isEmpty(config)) {
                                        resultData.put("retCode", 0);
                                        resultData.put("message", "消息类型对应的template为空");
                                        return resultData;
                                    }
                                    String template_id = config.getString("wxLiteTemplateId");
                                    String activityId = togetherGroup.getString("activityId");
                                    String productId = togetherGroup.getString("productId");
                                    for (GenericValue togetherGroupRelTemp : realTogetherGroupRels) {
                                        String partyId = togetherGroupRelTemp.getString("orderUserId");
                                        String orderId1 = togetherGroupRelTemp.getString("orderId");
                                        String sendwxAppOpenId = CommonUtils.getWxAppOpenId(delegator, partyId);

                                        double price = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId, "productId", productId)).get(0).getBigDecimal("activityPrice").doubleValue();
                                        Map<String, Object> daMap = FastMap.newInstance();
                                        daMap.put("keyword1", new Message.Data(orderId1 + "", ""));
                                        daMap.put("keyword2", new Message.Data(ProductUtils.getProductName(productId, delegator), ""));
                                        daMap.put("keyword3", new Message.Data(price + "", ""));
                                        daMap.put("keyword4", new Message.Data(limitUserNum + "", ""));
                                        String daJson = JsonMapper.defaultMapper().toJson(daMap);
                                        try {
                                            resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", sendwxAppOpenId, "template_id", template_id, "page", "/pages/webview/index?path=orderDetail&orderId=" + orderId, "form_id", "", "data", daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", sendwxAppOpenId, "objectValueId", togetherId));
                                        } catch (Exception e) {
                                            Debug.log(e.getMessage());
                                        }

                                    }

                                }


                            }
                        }
                    }


                }


            }

        }

        resultData.put("retCode", "1");
        resultData.put("message", "success");
        return resultData;

    }


}
