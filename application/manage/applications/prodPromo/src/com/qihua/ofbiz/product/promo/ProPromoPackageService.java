package com.qihua.ofbiz.product.promo;

import com.google.common.base.Joiner;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.model.ModelViewEntity.ComplexAlias;
import org.ofbiz.entity.model.ModelViewEntity.ComplexAliasField;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by changsy on 16/1/9.
 */
public class ProPromoPackageService {

    public static final String module = ProPromoPackageService.class.getName();

    /**
     * 根据活动ID活动有效的活动订单数
     *
     * @param dcx
     * @param context
     * @return活动订单数
     */
    public static int orderNum = 0;


    /**
     * 查询促销活动列表，弹出框使用   add by qianjin 2016/01/20
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getPromoListForModal(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> recordsList = FastList.newInstance();

        //总记录数
        int recordsListSize = 0;
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

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PA", "ProductActivity");
        dynamicView.addAlias("PA", "activityId");
        dynamicView.addAlias("PA", "activityName");
        dynamicView.addAlias("PA", "activityStartDate");
        dynamicView.addAlias("PA", "activityEndDate");
        dynamicView.addAlias("PA", "activityAuditStatus");
        dynamicView.addAlias("PA", "activityType");

        dynamicView.addMemberEntity("E", "Enumeration");
        dynamicView.addAlias("E", "enumId");
        dynamicView.addAlias("E", "activityTypeName", "description", null, false, null, null);
        dynamicView.addViewLink("PA", "E", false, ModelKeyMap.makeKeyMapList("activityType", "enumId"));

        dynamicView.addMemberEntity("PAA", "ProductActivityArea");
        dynamicView.addAlias("PAA", "paId", "activityId", null, false, null, null);
        dynamicView.addAlias("PAA", "communityId");
        dynamicView.addAlias("PAA", "communityName");
        dynamicView.addViewLink("PA", "PAA", Boolean.TRUE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));

        fieldsToSelect.add("activityId");
        fieldsToSelect.add("activityName");
        fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("activityType");
        fieldsToSelect.add("activityTypeName");

        //排序字段名称
        String sortField = "activityId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        //按促销类型查询
        if (UtilValidate.isNotEmpty(context.get("activityType"))) {
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, context.get("activityType")));
        }
        //按促销名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("activityName"))) {
            andExprs.add(EntityCondition.makeCondition("activityName", EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("activityName") + "%")));
        }

        //按社区查询
        if (UtilValidate.isNotEmpty(context.get("activityArea"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("communityName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("activityArea") + "%")));
        }

        //默认条件
        List<EntityCondition> defaultExprs = FastList.newInstance();
        List<EntityCondition> defaultExprs1 = FastList.newInstance();
        List<EntityCondition> defaultExprs2 = FastList.newInstance();
        //未开始（auditStatus为审批通过并且系统当前时间小于销售开始时间）
        defaultExprs1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        defaultExprs1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        defaultExprs.add(EntityCondition.makeCondition(defaultExprs1, EntityOperator.AND));
        //进行中（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
        defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.LESS_THAN, UtilDateTime.nowTimestamp()));
        defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        defaultExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.AND));
        andExprs.add(EntityCondition.makeCondition(defaultExprs, EntityOperator.OR));
        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String activityId = gv.getString("activityId");
                String activityName = gv.getString("activityName");
                Timestamp activityStartDate = gv.getTimestamp("activityStartDate");
                Timestamp activityEndDate = gv.getTimestamp("activityEndDate");
                String activityAuditStatus = gv.getString("activityAuditStatus");
                String activityType = gv.getString("activityType");
                String activityTypeName = gv.getString("activityTypeName");

                map.put("activityId", activityId);
                map.put("activityType", activityType);
                map.put("activityTypeName", activityTypeName);
                map.put("activityName", activityName);
                String audityStatus = "";
                if ("ACTY_AUDIT_PASS".equals(activityAuditStatus) && activityStartDate.after(UtilDateTime.nowTimestamp())) {
                    audityStatus = "未开始";
                } else if ("ACTY_AUDIT_PASS".equals(activityAuditStatus) && activityStartDate.before(UtilDateTime.nowTimestamp()) && activityEndDate.after(UtilDateTime.nowTimestamp())) {
                    audityStatus = "进行中";
                }
                map.put("audityStatus", audityStatus);
                //获取社区名称
                String communityName = "";
                List<GenericValue> areaList = delegator.findByAnd("ProductActivityArea", UtilMisc.toMap("activityId", activityId));
                if (UtilValidate.isNotEmpty(areaList)) {
                    for (int i = 0; i < areaList.size(); i++) {
                        String name = areaList.get(i).getString("communityName");
                        if (i == 0) {
                            communityName += name;
                        } else {
                            communityName += "，" + name;
                        }
                    }
                } else {
                    communityName = "全部社区";
                }
                map.put("communityName", communityName);
                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
        }


        //返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    /**
     * 获取详情 add by Wcy 2016.01.26
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getActivityDetail(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数  活动ID */
        String activityId = (String) context.get("activityId");
        String userLoginId = (String) context.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        String partyId = userLogin.getString("partyId");
        GenericValue activityGoods = null;
        GenericValue activity = null;
        String productId = "";
        try {
            activity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
            activityGoods = EntityUtil.getFirst(delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId)));
            productId = activityGoods.getString("productId");
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == activity) {
            result.put("resultData", resultData);
            return result;
        }

        /** 商品 */
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == product) {
            result.put("resultData", resultData);
            return result;
        }
        /** 图片列表 */
        List<String> banner = FastList.newInstance();
       /* String image1 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_1_LARGE", locale, dispatcher);
        String image2 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_2_LARGE", locale, dispatcher);
        String image3 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_3_LARGE", locale, dispatcher);
        String image4 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_4_LARGE", locale, dispatcher);
        String image5 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_5_LARGE", locale, dispatcher);*/
        String imageUrl1 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
        String imageUrl2 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_2", locale, dispatcher);
        String imageUrl3 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_3", locale, dispatcher);
        String imageUrl4 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_4", locale, dispatcher);
        String imageUrl5 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_5", locale, dispatcher);
        if (UtilValidate.isNotEmpty(imageUrl1)) {
            banner.add(imageUrl1);
        }
        if (UtilValidate.isNotEmpty(imageUrl2)) {
            banner.add(imageUrl2);
        }
        if (UtilValidate.isNotEmpty(imageUrl3)) {
            banner.add(imageUrl3);
        }
        if (UtilValidate.isNotEmpty(imageUrl4)) {
            banner.add(imageUrl4);
        }
        if (UtilValidate.isNotEmpty(imageUrl5)) {
            banner.add(imageUrl5);
        }
        /** 活动标签 */
        List<String> marks = FastList.newInstance();
        if (null != activityGoods) {
            if ("Y".equals(activityGoods.getString("isAnyReturn"))) {
                marks.add("随时退");
            }
            if ("Y".equals(activityGoods.getString("isSupportOverTimeReturn"))) {
                marks.add("过期退");
            }
            if ("Y".equals(activityGoods.getString("isSupportScore"))) {
                marks.add("可积分");
            }
            if ("Y".equals(activityGoods.getString("isSupportReturnScore"))) {
                marks.add("退货返回积分");
            }
            if ("Y".equals(activityGoods.getString("isPostageFree"))) {
                marks.add("包邮");
            }
            // if ("Y".equals(activityGoods.getString("isShowIndex"))) marks.add("推荐到首页");
        }

        /** 团购阶梯列表 */
        List<GenericValue> productGroupOrderRule = null;
        try {
            productGroupOrderRule = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        List<Map<String, Object>> priceList = FastList.newInstance();
        for (GenericValue groupRule : productGroupOrderRule) {
            Map<String, Object> map = FastMap.newInstance();
            map.put("people", groupRule.get("orderQuantity"));
            map.put("price", groupRule.get("orderPrice"));
            priceList.add(map);
        }

        /** 评价列表 */
        List<Map<String, Object>> reviewList = null;
        Map<String, Object> parameContext = FastMap.newInstance();
        parameContext.put("productId", productId);
        //parameContext.put("userLoginId",userLoginId);
        parameContext.put("viewSize", "3");
        Map<String, Object> resultData1 = FastMap.newInstance();
        try {
            Map<String, Object> reviewResult = dispatcher.runSync("findProductReview", parameContext);
            //reviewList = (List<Map<String, Object>>) reviewResult.get("resultData");
            resultData1 = (Map<String, Object>) reviewResult.get("resultData");
            reviewList = (List<Map<String, Object>>) resultData1.get("reviewList");
        } catch (GenericServiceException e) {
            Debug.log(e.getMessage());
        }

        /** 活动是否被收藏 */
        List<GenericValue> productActivityCollection = null;
        try {
            productActivityCollection = delegator.findByAnd("ProductActivityCollection", UtilMisc.toMap("activityId", activityId, "partyId", partyId));
            //productActivityCollection = delegator.findByPrimaryKey("ProductActivityCollection",UtilMisc.toMap("activityId",activityId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        Boolean collection;
        if (UtilValidate.isNotEmpty(productActivityCollection)) {
            collection = true;
        } else {
            collection = false;
        }
        //Boolean collection = !(productActivityCollection == null);

        resultData.put("productId", productId);//商品ID
        if ("GROUP_ORDER".equals(activity.getString("activityType"))) {
            resultData.put("type", "group"); //活动类型  团购：group，秒杀：secKill
            resultData.put("priceList", priceList); //团购阶梯列表
        } else if ("SEC_KILL".equals(activity.getString("activityType"))) {
            resultData.put("type", "secKill"); //活动类型  团购：group，秒杀：secKill
            resultData.put("startTimestamp", activity.getTimestamp("activityStartDate").getTime() / 1000); //抢购开始时间戳
            resultData.put("endTimestamp", activity.getTimestamp("activityEndDate").getTime() / 1000); //抢购结束时间戳
            resultData.put("curPrice", activity.getBigDecimal("productPrice")); //当前价格
            try {
                List<GenericValue> productDefaultPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productPriceTypeId", "DEFAULT_PRICE", "productId", productId)));
                if (UtilValidate.isNotEmpty(productDefaultPrices)) {
                    GenericValue productFeaturePrice = productDefaultPrices.get(0);
                    if (UtilValidate.isNotEmpty(productFeaturePrice.get("price"))) {
                        resultData.put("orgPrice", productFeaturePrice.getBigDecimal("price"));
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        //活动状态
        Date now = new Date();
        //根据开始时间和结束时间来判断活动状态  add by gss
        Date activityStartDate = activity.getTimestamp("activityStartDate");
        Date activityEndDate = activity.getTimestamp("activityEndDate");
        //已售数量
        Long hasBuyQuantity = activity.getLong("hasBuyQuantity");
        //单个ID限购数量
        Long limitQuantity = activity.getLong("limitQuantity");
        //活动数量
        Long activityQuantity = activity.getLong("activityQuantity");
        if (hasBuyQuantity == null) {
            hasBuyQuantity = 0L;
        }
        //剩余数量
        Long soldQuantity = activityQuantity - hasBuyQuantity;

        /** 活动商品可用库存 */
        BigDecimal accountingQuantityTotal = BigDecimal.ZERO;
        List<GenericValue> inventory_item = null;
        try {
            inventory_item = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (UtilValidate.isNotEmpty(inventory_item)) {
            GenericValue inventoryItem = EntityUtil.getFirst(inventory_item);
            accountingQuantityTotal = inventoryItem.getBigDecimal("accountingQuantityTotal");
        }
        //查询该活动已下单数量
        Long buyNum = 0L;
        if ("FINISHED_GOOD".equals(product.get("productTypeId"))) {
            DynamicViewEntity dve = new DynamicViewEntity();
            dve.addMemberEntity("OHR", "OrderHeader");
            dve.addMemberEntity("ORE", "OrderRole");
            dve.addMemberEntity("OIM", "OrderItem");
            dve.addViewLink("OHR", "ORE", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
            dve.addViewLink("OHR", "OIM", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
            dve.addAlias("OHR", "orderId", "orderId", null, true, true, null);
            dve.addAlias("OHR", "statusId");
            dve.addAlias("ORE", "partyId");
            dve.addAlias("ORE", "roleTypeId");
            dve.addAlias("OIM", "activityId");
            dve.addAlias("OIM", "quantity");
            List<EntityCondition> entityConditionList = FastList.newInstance();
            entityConditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN, UtilMisc.toList("ORDER_WAITPAY", "ORDER_CANCELLED", "ORDER_RETURNED")));
            entityConditionList.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
            entityConditionList.add(EntityCondition.makeCondition("roleTypeId", "PLACING_CUSTOMER"));
            entityConditionList.add(EntityCondition.makeCondition("activityId", activityId));
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
            EntityListIterator eli = null;
            // do the lookup
            try {
                eli = delegator.findListIteratorByCondition(dve, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, findOpts);
                List<GenericValue> resultList = eli.getCompleteList();
                eli.close();
                if (UtilValidate.isNotEmpty(resultList)) {
                    for (GenericValue results : resultList) {
                        buyNum += results.getBigDecimal("quantity").longValue();
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        } else if ("VIRTUAL_GOOD".equals(product.get("productTypeId"))) {
            List<GenericValue> tickets = new ArrayList<GenericValue>();
            List<EntityCondition> ticketConditiona = FastList.newInstance();
            ticketConditiona.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
            ticketConditiona.add(EntityCondition.makeCondition("activityId", activityId));
            ticketConditiona.add(EntityCondition.makeCondition("ticketStatus", EntityOperator.IN, UtilMisc.toList("notUsed", "hasUsed", "notAudited", "notRefunded", "rejectApplication", "expired")));
            try {
                tickets = delegator.findList("Ticket", EntityCondition.makeCondition(ticketConditiona, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(tickets)) {
                buyNum += Long.valueOf(tickets.size());
            }
        }
        //可购买的数量
        long needbuyNum = limitQuantity - buyNum;
        //活动结束时间大于当前时间
        if (UtilValidate.isNotEmpty(activityEndDate) && now.before(activityEndDate)) {
            //当前时间大于开始时间 活动进行中
            if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && soldQuantity == 0 || accountingQuantityTotal.compareTo(BigDecimal.ZERO) == 0) {
                resultData.put("state", "clearout");
            } else if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && (soldQuantity > 0) && needbuyNum > 0) {
                resultData.put("state", "ongoing");
            } else if (UtilValidate.isNotEmpty(activityStartDate) && now.before(activityStartDate)) {
                resultData.put("state", "preparation");
            } else if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && (needbuyNum <= 0)) {
                resultData.put("state", "purchase");
            }
        } else {
            resultData.put("state", "finish");
        }
        // resultData.put("state",activityType.get(activity.getString("activityAuditStatus")));//活动状态  未开始：preparation，进行中：ongoing，已结束：finish，已售罄：clearout
        resultData.put("collection", collection); //是否被收藏
        resultData.put("title", product.getString("productName"));  //活动标题
        resultData.put("subTitle", product.getString("productSubheadName"));   //活动副标题
        resultData.put("banner", banner); //图片列表
        resultData.put("mark", marks); //活动标签
        //resultData.put("soldNum",10); //已售数量  //TODO 数据从哪里来？
        //add by gss
        resultData.put("soldNum", hasBuyQuantity); //已售数量
        // resultData.put("orgPrice",10); //商品原价 //TODO 数据从哪里来？
        resultData.put("content", product.getString("mobileDetails") == null ? "" : product.getString("mobileDetails"));//商品详情
        resultData.put("inform", activity.getString("activityDesc") == null ? "" : activity.getString("activityDesc")); //购买须知
        BigDecimal score = BigDecimal.ZERO;
        Long num = 0L;
        Map<String, Object> reviewDatas = FastMap.newInstance();
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PR", "ProductReview");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "isShow");
        dynamicView.addAlias("PR", "sumProductRating", "productRating", null, null, null, "sum");
        dynamicView.addAlias("PR", "num", "productReviewId", null, null, null, "count");
        List<EntityCondition> andExprs = FastList.newInstance();
        andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, product.get("productId")));
        andExprs.add(EntityCondition.makeCondition("isShow", EntityOperator.EQUALS, "1"));
        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        EntityFindOptions findOpts1 = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
        // using list iterator
        EntityListIterator productReviewEli;
        try {
            productReviewEli = delegator.findListIteratorByCondition(dynamicView,
                    mainCond
                    , null, null, null, findOpts1);
            List<GenericValue> prods = productReviewEli.getCompleteList();
            Iterator<GenericValue> prodsIt = prods.iterator();
            while (prodsIt.hasNext()) {
                GenericValue oneProd = prodsIt.next();
                score = oneProd.getBigDecimal("sumProductRating");
                num = oneProd.getLong("num");
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (score == null || score.compareTo(BigDecimal.ZERO) == 0) {
            score = new BigDecimal(10);
        } else {
            score = score.divide(new BigDecimal(num), 0, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(2));
        }
        reviewDatas.put("score", score);
        reviewDatas.put("number", num);
        reviewDatas.put("list", reviewList);

        resultData.put("review", reviewDatas); //评价列表
        result.put("resultData", resultData);
        return result;
    }

    /**
     * 查询折扣列表
     *
     * @param dcx
     * @param context
     * @return
     */

    public static Map<String, Object> findDiscount(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String fromDate = (String) context.get("fromDate");
        String thruDate = (String) context.get("thruDate");

        String promoStatus = (String) context.get("promoStatus");
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> promoList = FastList.newInstance();
        int listSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("promoCode");
        fieldsToSelect.add("promoName");
        fieldsToSelect.add("promoType");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("thruDate");
        fieldsToSelect.add("promoStatus");
        fieldsToSelect.add("productPromoId");

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("productPromoId" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(promoCode)) {
            paramList = paramList + "&promoCode=" + promoCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoCode + "%")));
        }

        if (UtilValidate.isNotEmpty(promoName)) {
            paramList = paramList + "&promoName=" + promoName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoName + "%")));
        }

        if (UtilValidate.isNotEmpty(fromDate)) {
            paramList = paramList + "&fromDate=" + fromDate;
            Object beginDate = null;
            try {
                beginDate = ObjectType.simpleTypeConvert(fromDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN_EQUAL_TO, beginDate));
        }
        if (UtilValidate.isNotEmpty(thruDate)) {
            paramList = paramList + "&thruDate=" + thruDate;
            Object endDate = null;
            try {
                endDate = ObjectType.simpleTypeConvert(thruDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.LESS_THAN_EQUAL_TO, endDate));
        }

        if (UtilValidate.isNotEmpty(promoStatus)) {
            andExprs.add(EntityCondition.makeCondition("promoStatus",
                    EntityOperator.EQUALS, promoStatus));
        }
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.EQUALS, "PROMO_DISCOUNT"));
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

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.find("ProductStorePromoAndAppl", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

                // get the partial list for this page
                promoList = pli.getPartialList(lowIndex, viewSize);

                // attempt to get the full size
                listSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > listSize) {
                    highIndex = listSize;
                }

                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg);
                return ServiceUtil.returnError(e.getMessage());
            }
        } else {
            listSize = 0;
        }
        result.put("promoList", promoList);
        result.put("promoCode", promoCode);
        result.put("promoName", promoName);
        result.put("fromDate", fromDate);
        result.put("thruDate", thruDate);
        result.put("promoStatus", promoStatus);

        result.put("promoListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    /**
     * 查询直降列表
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findStraightDown(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String fromDate = (String) context.get("fromDate");
        String thruDate = (String) context.get("thruDate");

        String promoStatus = (String) context.get("promoStatus");
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");

        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> promoList = FastList.newInstance();
        int listSize = 0;
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

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("promoCode");
        fieldsToSelect.add("promoName");
        fieldsToSelect.add("promoType");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("thruDate");
        fieldsToSelect.add("promoStatus");
        fieldsToSelect.add("productPromoId");

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("productPromoId" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(promoCode)) {
            paramList = paramList + "&promoCode=" + promoCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoCode + "%")));
        }

        if (UtilValidate.isNotEmpty(promoName)) {
            paramList = paramList + "&promoName=" + promoName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoName + "%")));
        }

        if (UtilValidate.isNotEmpty(fromDate)) {
            paramList = paramList + "&fromDate=" + fromDate;
            Object beginDate = null;
            try {
                beginDate = ObjectType.simpleTypeConvert(fromDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN_EQUAL_TO, beginDate));
        }
        if (UtilValidate.isNotEmpty(thruDate)) {
            paramList = paramList + "&thruDate=" + thruDate;
            Object endDate = null;
            try {
                endDate = ObjectType.simpleTypeConvert(thruDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.LESS_THAN_EQUAL_TO, endDate));
        }

        if (UtilValidate.isNotEmpty(promoStatus)) {
            andExprs.add(EntityCondition.makeCondition("promoStatus",
                    EntityOperator.EQUALS, promoStatus));
        }
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.EQUALS, "PROMO_SPE_PRICE"));

        //关联店铺
        andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));

        // build the main condition
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            // get the indexes for the partial list
            // get the indexes for the partial list
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // using list iterator
            EntityListIterator pli = delegator.find("ProductStorePromoAndAppl", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

            // get the partial list for this page
            promoList = pli.getPartialList(lowIndex, viewSize);

            // attempt to get the full size
            listSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > listSize) {
                highIndex = listSize;
            }

            // close the list iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg);
            return ServiceUtil.returnError(e.getMessage());
        }

        result.put("promoList", promoList);
        result.put("promoCode", promoCode);
        result.put("promoName", promoName);
        result.put("fromDate", fromDate);
        result.put("thruDate", thruDate);
        result.put("promoStatus", promoStatus);

        result.put("promoListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    /**
     * 查询包邮列表
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findPackage(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String fromDate = (String) context.get("fromDate");
        String thruDate = (String) context.get("thruDate");

        String promoStatus = (String) context.get("promoStatus");
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> promoList = FastList.newInstance();
        int listSize = 0;
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

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("promoCode");
        fieldsToSelect.add("promoName");
        fieldsToSelect.add("promoType");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("thruDate");
        fieldsToSelect.add("promoStatus");
        fieldsToSelect.add("productPromoId");

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("productPromoId" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(promoCode)) {
            paramList = paramList + "&promoCode=" + promoCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoCode + "%")));
        }

        if (UtilValidate.isNotEmpty(promoName)) {
            paramList = paramList + "&promoName=" + promoName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoName + "%")));
        }

        if (UtilValidate.isNotEmpty(fromDate)) {
            paramList = paramList + "&fromDate=" + fromDate;
            Object beginDate = null;
            try {
                beginDate = ObjectType.simpleTypeConvert(fromDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN_EQUAL_TO, beginDate));
        }
        if (UtilValidate.isNotEmpty(thruDate)) {
            paramList = paramList + "&thruDate=" + thruDate;
            Object endDate = null;
            try {
                endDate = ObjectType.simpleTypeConvert(thruDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.LESS_THAN_EQUAL_TO, endDate));
        }
        /*包邮状态*/
        if (UtilValidate.isNotEmpty(promoStatus)) {
            andExprs.add(EntityCondition.makeCondition("promoStatus",
                    EntityOperator.EQUALS, promoStatus));
        }
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.IN, UtilMisc.toList("PROMO_FREE_SHIPPING", "PROMO_PRE_FREE_SHIPPING")));

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

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.find("ProductStorePromoAndAppl", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

                // get the partial list for this page
                promoList = pli.getPartialList(lowIndex, viewSize);

                // attempt to get the full size
                listSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > listSize) {
                    highIndex = listSize;
                }

                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg);
                return ServiceUtil.returnError(e.getMessage());
            }
        } else {
            listSize = 0;
        }
        result.put("promoList", promoList);
        result.put("promoCode", promoCode);
        result.put("promoName", promoName);
        result.put("fromDate", fromDate);
        result.put("thruDate", thruDate);
        result.put("promoStatus", promoStatus);

        result.put("promoListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    /**
     * 新增折扣 addDiscount
     *
     * @param dtx
     * @param context
     * @return
     */

    public static Map<String, Object> addDiscount(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoName = (String) context.get("promoName");
        String promoTypeId = (String) context.get("promoType");
        String paramEnumId = (String) context.get("paramEnumId");
        String promoType = "PROMO_DISCOUNT";
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String productStoreId = (String) context.get("productStoreId");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dtx.getDelegator();
        String promoProductType = "PROMO_PRT_PART_IN";
        List<String> productids = StringUtil.split(productIds, ",");
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();

        String promoCode = delegator.getNextSeqId("ProductPromo");
        Map serviceIn = UtilMisc.toMap("promoName", promoName, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");
        serviceIn.put("promoCode", "CX_" + promoCode);

        try {
            String productPromoId = "";
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoId = (String) ret.get("productPromoId");

            //创建促销规则
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "折扣规则", "userLogin", userLogin);
            String productPromoRuleId = "";
            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoRuleId = (String) ret.get("productPromoRuleId");

            if ("PROMO_AMOUNT_REDUCE".equals(paramEnumId)) {
                List<String> promos = StringUtil.split(promoCondActions, "|");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    if (promos.size() == 2) {
                        for (int i = 0; i < promos.size(); i++) {
                            String condActions = promos.get(i);
                            List<String> condAction = StringUtil.split(condActions, ",");
//                            if (UtilValidate.isEmpty(promos)) {
//                                return ServiceUtil.returnError("配置错误");
//                            }else
                            if (UtilValidate.isNotEmpty(condAction)) {
                                String cond = condAction.get(0);
                                String action = condAction.get(1);
                                // 调用创建cond
                                String inputParamEnumId = "PPIP_ORDER_TOTAL";
                                String operatorEnumId = "PPC_GTE";
                                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                        "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                                ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                                String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                                if (ServiceUtil.isError(ret)) {
                                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                    return ret;
                                }

                                // 调用创建action
                                String productPromoActionEnumId = "PROMO_PROD_DISC";
                                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                        "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "userLogin", userLogin);
                                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                                if (ServiceUtil.isError(ret)) {
                                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                    return ret;
                                }

                            }
                        }
                    } else if (promos.size() > 2) {
                        for (int i = 0; i < promos.size(); i++) {
                            String condActions = promos.get(i);
                            List<String> condAction = StringUtil.split(condActions, ",");
                            if (UtilValidate.isNotEmpty(condAction)) {
                                String cond = condAction.get(0);
                                String action = condAction.get(1);
                                // 调用创建cond
                                String inputParamEnumId = "PPIP_ORDER_TOTAL";
                                String operatorEnumId = "PPC_GTE";
                                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                        "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                                ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                                String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                                if (ServiceUtil.isError(ret)) {
                                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                    return ret;
                                }

                                // 调用创建action
                                String productPromoActionEnumId = "PROMO_PROD_DISC";
                                BigDecimal quantity = BigDecimal.ONE;
                                BigDecimal amount = new BigDecimal(action);
                                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                        "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                                if (ServiceUtil.isError(ret)) {
                                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                    return ret;
                                }

                            }

                        }
                    }

                }

            } else if ("PROMO_NUM_REDUCE".equals(promoTypeId)) {
                List<String> promos = StringUtil.split(promoCondActions, ",");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    if (promos.size() == 2) {
                        String cond = promos.get(0);
                        String action = promos.get(1);
                        // 调用创建cond
                        String inputParamEnumId = "PPIP_PRODUCT_QUANT";
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                        // 调用创建action
                        String productPromoActionEnumId = "PROMO_PROD_DISC";
                        BigDecimal quantity = BigDecimal.ONE;
                        BigDecimal amount = new BigDecimal(action);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    } else if (promos.size() > 2) {
                        for (int i = 0; i < promos.size(); i++) {
                            String condActions = promos.get(i);
                            List<String> condAction = StringUtil.split(condActions, "|");
                            if (UtilValidate.isNotEmpty(condAction)) {
                                String cond = condAction.get(0);
                                String action = condAction.get(1);
                                // 调用创建cond
                                String inputParamEnumId = "PPIP_ORDER_TOTAL";
                                String operatorEnumId = "PPC_GTE";
                                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                        "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                                ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                                String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                                if (ServiceUtil.isError(ret)) {
                                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                    return ret;
                                }
                                // 调用创建action
                                String productPromoActionEnumId = "PROMO_PROD_DISC";
                                BigDecimal quantity = BigDecimal.ONE;
                                BigDecimal amount = new BigDecimal(action);
                                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                        "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                                if (ServiceUtil.isError(ret)) {
                                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                    return ret;
                                }
                            }

                        }
                    }
                }
            }

            //促销对应商品
            if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                String productPromoActionSeqId = "_NA_";
                String productPromoCondSeqId = "_NA_";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            }

            //调用促销对应店铺
//            查找所有的店铺
            try {
                List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
                    for (int i = 0; i < productStores.size(); i++) {
                        GenericValue productStore = productStores.get(i);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                                "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
//
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 新增包邮
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> addPackage(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoName = (String) context.get("promoName");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");

        String promoCondActions = (String) context.get("promoCondActions");
        //全部产品参与
        String promoProductType = "PROMO_PRT_ALL";

        String promoType = "PROMO_FREE_SHIPPING";
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();
        String promoCode = delegator.getNextSeqId("ProductPromo");

        Map serviceIn = UtilMisc.toMap("promoType", promoType, "promoName", promoName, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");
        //自动生成促销编码
        serviceIn.put("promoCode", "CX_" + promoCode);

        try {
            String productPromoId = "";
            //调用创建促销
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoId = (String) ret.get("productPromoId");
//
            //创建促销规则
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "包邮规则", "userLogin", userLogin);
            String productPromoRuleId = "";
            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
            productPromoRuleId = (String) ret.get("productPromoRuleId");
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }

            if ("PROMO_FREE_SHIPPING".equals(promoType)) {
                String[] condStr = promoCondActions.split(",");
                if (condStr == null || condStr.length != 2) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    //获取页面的值
                    String condValue = condStr[0];

                    // 调用创建cond 促销条件
//                    if(enumId.equals("PPIP_ORDER_TOTAL")){//元 按金额包邮
                    String inputParamEnumId = condStr[1];
                    String operatorEnumId = "PPC_GTE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                            "operatorEnumId", operatorEnumId, "condValue", condValue, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }

                    //调用创建action
                    String productPromoActionEnumId = "PROMO_SHIP_CHARGE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(10), "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }

                }
            }
            //调用促销对应店铺
//            查找所有的店铺
            try {
                List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
                    for (int i = 0; i < productStores.size(); i++) {
                        GenericValue productStore = productStores.get(i);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStore.get("productStoreId"),
                                "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            //会员等级关系

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 修改促销
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updatePromoReduce(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        String promoName = (String) context.get("promoName");
        String promoText = (String) context.get("promoText");
        String promoType = (String) context.get("promoType");//每满/满
        String productStoreId = (String) context.get("productStoreId");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        String levelIds = (String) context.get("levelIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Map<String, Object> ret = null;
        //start by nf  2018/03/05
        String promoProductType = "";
        List<String> productids = StringUtil.split(productIds, ",");
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();
        Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoName", promoName, "promoText", promoText, "promoProductType",
                promoProductType, "productPromoId", productPromoId, "promoStatus", "ACTY_AUDIT_INIT");

        try {
            GenericValue updateValue = delegator.makeValue("ProductPromo", serviceIn);
            updateValue.store();

            // 创建促销规则
            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isNotEmpty(productPromoRules)) {
                GenericValue productPromoRule = productPromoRules.get(0);
                String productPromoRuleId = (String) productPromoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
                if ("PROMO_PRE_REDUCE".equals(promoType)) {
                    List<String> promos = StringUtil.split(promoCondActions, ",");
                    if (UtilValidate.isEmpty(promos)) {
                        return ServiceUtil.returnError("配置错误");
                    } else {
                        String condition = promos.get(0);
                        String action = promos.get(1);
                        // 调用创建cond
                        String inputParamEnumId = "PPIP_PRODUCT_AMOUNT";
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", condition, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                        //调用创建action
                        String productPromoActionEnumId = "PROMO_PRO_PER_AMDISC";
                        BigDecimal quantity = BigDecimal.ONE;
                        BigDecimal amount = new BigDecimal(action);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }


                } else if ("PROMO_REDUCE".equals(promoType)) {
                    List<String> promos = StringUtil.split(promoCondActions, "|");
                    if (UtilValidate.isEmpty(promos)) {
                        return ServiceUtil.returnError("配置错误");
                    } else {
                        for (int i = 0; i < promos.size(); i++) {
                            String condActions = promos.get(i);
                            List<String> condAction = StringUtil.split(condActions, ",");
                            if (UtilValidate.isEmpty(condAction)) {
                                return ServiceUtil.returnError("配置错误");
                            } else {
                                String cond = condAction.get(0);
                                String action = condAction.get(1);
                                // 调用创建cond
                                String inputParamEnumId = "PPIP_PRODUCT_AMOUNT";
                                String operatorEnumId = "PPC_GTE";
                                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                        "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                                ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                                String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                                if (ServiceUtil.isError(ret)) {
                                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                    return ret;
                                }
                                // 调用创建action
                                String productPromoActionEnumId = "PROMO_PROD_AMDISC";
                                BigDecimal quantity = BigDecimal.ONE;
                                BigDecimal amount = new BigDecimal(action);
                                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                        "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                                if (ServiceUtil.isError(ret)) {
                                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                    return ret;
                                }
                            }
                        }
                    }

                }

                //促销对应商品
                if ("PROMO_PRT_ALL".equals(promoProductType)) {
                    //所有产品生效，

                } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_INCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }


                } else if ("PROMO_PRT_PART_EX".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_EXCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }
                }

            }
            //根据当前登陆人店铺进行关联
            try {
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                        "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }

            } catch (Exception e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 新增直降促销
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> addStraightDown(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoName = (String) context.get("promoName");
        String promoTypeId = (String) context.get("promoType");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        Delegator delegator = dtx.getDelegator();
        String promoType = "PROMO_SPE_PRICE";
        String productStoreId = (String) context.get("productStoreId");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");


        String promoProductType = "PROMO_PRT_PART_IN";

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();
        String promoCode = delegator.getNextSeqId("ProductPromo");
        Map serviceIn = UtilMisc.toMap("promoName", promoName, "promoType",
                promoType, "promoProductType", promoProductType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");
        serviceIn.put("promoCode", "CX_" + promoCode);

        try {
            String productPromoId = "";
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoId = (String) ret.get("productPromoId");


//            创建促销规则
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "userLogin", userLogin, "ruleName", promoCode + "直降规则");
            String productPromoRuleId = "";
            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoRuleId = (String) ret.get("productPromoRuleId");

            List<String> promos = StringUtil.split(promoCondActions, "|");
            List<String> proIds = StringUtil.split(productIds, ",");
            if (UtilValidate.isEmpty(promos)) {
                return ServiceUtil.returnError("配置错误");
            } else {
                if (proIds.size() > 1) {
                    for (int i = 0; i < promos.size(); i++) {
                        String action = promos.get(i);
                        String product = proIds.get(i);


                        // 调用创建cond
                        String inputParamEnumId = "PPIP_PRODUCT_TOTAL";
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", "1", "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                        //调用创建action
                        String productPromoActionEnumId = "PROMO_PROD_AMDISC";

                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "productId", product, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                } else if (proIds.size() == 1) {
                    String action = promos.get(0);
                    String product = proIds.get(0);
                    // 调用创建cond
                    String inputParamEnumId = "PPIP_PRODUCT_TOTAL";
                    String operatorEnumId = "PPC_GTE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                            "operatorEnumId", operatorEnumId, "condValue", "1", "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                    //调用创建action
                    String productPromoActionEnumId = "PROMO_PROD_AMDISC";

                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "productId", product, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }

                }

            }

            //促销对应商品

            if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                String productPromoActionSeqId = "_NA_";
                String productPromoCondSeqId = "_NA_";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            }

            //调用促销对应店铺
//            查找所有的店铺
            try {
                List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
                    for (int i = 0; i < productStores.size(); i++) {
                        GenericValue productStore = productStores.get(i);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                                "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }

        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 修改满赠
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updatePromoGiftService(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String productPromoId = (String) context.get("productPromoId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String promoText = (String) context.get("promoText");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        String productIds1 = (String) context.get("productIds1");//赠送的商品 ,id的字符串集合
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String promoType = "PROMO_GIFT";
        String promoProductType = "";
        List<String> productids = StringUtil.split(productIds, ",");
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();
        //创建促销主表
        Map serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "promoName", promoName, "promoText", promoText, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", "ACTY_AUDIT_INIT");


        try {
            GenericValue proPromoGV = delegator.makeValue("ProductPromo", serviceIn);
            proPromoGV.store();
            Map<String, Object> ret = null;
//            删除副表
            Map<String, Object> deleteret = dispatcher.runSync("deleteProductPromoWithoutMainPromo", UtilMisc.toMap("productPromoId", productPromoId));
            if (ServiceUtil.isError(deleteret)) {
                Debug.logError(ServiceUtil.getErrorMessage(deleteret), module);
                return deleteret;
            }
            // 创建促销规则
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "促销规则", "userLogin", userLogin);
            String productPromoRuleId = "";
            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            //获得促销规则主表id
            productPromoRuleId = (String) ret.get("productPromoRuleId");
            //创建促销条件
            String[] condStr = promoCondActions.split(",");
            if (condStr == null || condStr.length != 2) {
                return ServiceUtil.returnError("配置错误");
            }
            String operatorEnumId = "PPC_GTE";
            String inputParamEnumId = condStr[0];
            String condition = condStr[1];
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                    "operatorEnumId", operatorEnumId, "condValue", condition, "userLogin", userLogin);

            ret = dispatcher.runSync("createProductPromoCond", serviceIn);
            //获得促销条件表id
            String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }

            String productPromoActionEnumId = "PROMO_GWP";
            String quantity = "1";
            String[] productids1Str = productIds1.split(",");
            for (String curPrudId : productids1Str) {
                //调用创建促销动作
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                        "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(quantity), "amount", new BigDecimal(1), "productId", curPrudId, "userLogin", userLogin);
                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }
            }
            //促销对应商品
            if ("PROMO_PRT_ALL".equals(promoProductType)) {
                //所有产品生效，
            } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                String productPromoActionSeqId = "_NA_";
                String productPromoCondSeqId1 = "_NA_";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId1, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            }


            //根据当前登陆人店铺进行关联
            try {
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                        "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }

            } catch (Exception e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            //会员等级关系，暂时去除

        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 修改包邮 updatePackage
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updatePackage(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoName = (String) context.get("promoName");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String productPromoId = (String) context.get("productPromoId");
        String promoCondActions = (String) context.get("promoCondActions");
        //全部产品参与
        String promoProductType = (String) context.get("promoProductType");

        String promoType = "PROMO_FREE_SHIPPING";
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();
        String promoCode = delegator.getNextSeqId("ProductPromo");


        Map serviceIn = UtilMisc.toMap("promoType", promoType, "promoName", promoName, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");
        //自动生成促销编码
        serviceIn.put("promoCode", "CX_" + promoCode);

        try {

//            先删除
            Map<String, Object> deleteret = dispatcher.runSync("deleteProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
            if (ServiceUtil.isError(deleteret)) {
                Debug.logError(ServiceUtil.getErrorMessage(deleteret), module);
                return deleteret;
            }

            //调用创建促销
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }

            //创建促销规则
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "包邮规则", "userLogin", userLogin);
            String productPromoRuleId = "";
            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
            productPromoRuleId = (String) ret.get("productPromoRuleId");
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }

            if ("PROMO_FREE_SHIPPING".equals(promoType)) {
                String[] condStr = promoCondActions.split(",");
                if (condStr == null || condStr.length != 2) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    //获取页面的值
                    String condValue = condStr[0];

                    String enumId = condStr[1];
                    // 调用创建cond 促销条件
                    if ("PPIP_ORDER_TOTAL".equals(enumId)) {//元 按金额包邮
                        String inputParamEnumId = "PPIP_ORDER_TOTAL";
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", condValue, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }

                        //调用创建action
                        String productPromoActionEnumId = "PROMO_SHIP_CHARGE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(10), "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }

                    } else if ("PPIP_PRODUCT_QUANT".equals(enumId)) {//件 按商品数量

                        String inputParamEnumId = "PPIP_PRODUCT_QUANT";
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", condValue, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }

                        //调用创建action
                        String productPromoActionEnumId = "PROMO_SHIP_CHARGE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(10), "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }
            }
            //调用促销对应店铺
//            查找所有的店铺
            try {
                List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
                    for (int i = 0; i < productStores.size(); i++) {
                        GenericValue productStore = productStores.get(i);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStore.get("productStoreId"),
                                "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            //会员等级关系

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    public static Map<String, Object> updatePackageTest(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoProductType = (String) context.get("promoProductType");
        String promoCondActions = (String) context.get("promoCondActions");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();

        Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoProductType",
                promoProductType, "productPromoId", productPromoId, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);

        try {

            Map<String, Object> ret = dispatcher.runSync("updateProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            Delegator delegator = dtx.getDelegator();

            // 创建促销规则
            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));

            if (UtilValidate.isNotEmpty(productPromoRules)) {
                GenericValue productPromoRule = productPromoRules.get(0);
                String productPromoRuleId = (String) productPromoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));

                List<String> promos = StringUtil.split(promoCondActions, "|");

                String[] condStr = promoCondActions.split(",");
                if (condStr == null || condStr.length != 2) {
                    return ServiceUtil.returnError("配置错误");
                } else {

                    // 调用创建cond
                    String condValue = condStr[0];
                    String inputParamEnumId = condStr[1];

                    String operatorEnumId = "PPC_GTE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                            "operatorEnumId", operatorEnumId, "condValue", condValue, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                    //调用创建action
                    String productPromoActionEnumId = "PROMO_PROD_AMDISC";

                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(10), "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }

                }

                //调用促销对应店铺
//            查找所有的店铺

                try {
                    List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                    if (UtilValidate.isNotEmpty(productStores)) {
                        for (int i = 0; i < productStores.size(); i++) {
                            GenericValue productStore = productStores.get(i);
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStore.get("productStoreId"), "fromDate", fromDate,
                                    "thruDate", thruDate, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }

                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
                //会员等级关系
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 修改直降
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updateStraightDown(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoProductType = "";
        String promoType = (String) context.get("promoType");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        List<String> productids = StringUtil.split(productIds, ",");
        Delegator delegator = dtx.getDelegator();
        String chkFlg="Y";
        result.put("chkFlg", chkFlg);
        GenericValue auditInfo =EntityUtil.getFirst(delegator.findByAnd("ProductPromo",UtilMisc.toMap("productPromoId",productPromoId))) ;
        if(auditInfo!=null&&"ACTY_AUDIT_PASS".equalsIgnoreCase(auditInfo.getString("promoStatus"))){
            //此刻是已完成状态，需要判断新增商品互斥
            List<String> addProductIds  = ProPromoService.getAddProductIds(delegator,productPromoId,productids);
            if(UtilValidate.isNotEmpty(addProductIds)){
                //check新增的商品存不存在其他促销活动中
                List<String> existProductIds = ProPromoService.checkProductPromoExist(delegator,productStoreId,addProductIds,fromDate,thruDate);
                if(UtilValidate.isNotEmpty(existProductIds)){
                    chkFlg="N";
                    result.put("chkFlg", chkFlg);
                    result.put("errorMsg","商品id为：" + Joiner.on(",").join(existProductIds)+"的商品该时间段内已经在其他活动中！");
                    return result;
                }
            }
        }



        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();

        String promoStatus="ACTY_AUDIT_INIT";
        if(auditInfo!=null&&"ACTY_AUDIT_PASS".equalsIgnoreCase(auditInfo.getString("promoStatus"))){
            promoStatus="ACTY_AUDIT_PASS";
        }


        Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoProductType",
                promoProductType, "productPromoId", productPromoId, "promoStatus", promoStatus, "userLogin", userLogin);

        try {

            Map<String, Object> ret = dispatcher.runSync("updateProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }


            // 创建促销规则
            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));

            if (UtilValidate.isNotEmpty(productPromoRules)) {
                GenericValue productPromoRule = productPromoRules.get(0);
                String productPromoRuleId = (String) productPromoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));

                List<String> promos = StringUtil.split(promoCondActions, "|");
                List<String> proIds = StringUtil.split(productIds, ",");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {

                    for (int i = 0; i < promos.size(); i++) {
                        String action = promos.get(i);
                        String product = proIds.get(i);
                        // 调用创建cond
                        String inputParamEnumId = "PPIP_PRODUCT_TOTAL";
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", "1", "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                        //调用创建action
                        String productPromoActionEnumId = "PROMO_PROD_AMDISC";

                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "productId", product, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

                //促销对应商品
                if ("PROMO_PRT_ALL".equals(promoProductType)) {
                    //所有产品生效，

                } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_INCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }


                } else if ("PROMO_PRT_PART_EX".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_EXCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }
                }

                //调用促销对应店铺
//            查找所有的店铺

                try {
                    List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                    if (UtilValidate.isNotEmpty(productStores)) {
                        for (int i = 0; i < productStores.size(); i++) {
                            GenericValue productStore = productStores.get(i);
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStore.get("productStoreId"), "fromDate", fromDate,
                                    "thruDate", thruDate, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }

                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
                //会员等级关系
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 修改折扣
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updateDiscount(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoProductType = (String) context.get("promoProductType");
        String promoTypeId = (String) context.get("promoTypeId");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();

        Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoProductType",
                promoProductType, "productPromoId", productPromoId, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);

        try {

            Map<String, Object> ret = dispatcher.runSync("updateProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            Delegator delegator = dtx.getDelegator();

            // 创建促销规则
            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));

            if (UtilValidate.isNotEmpty(productPromoRules)) {
                GenericValue productPromoRule = productPromoRules.get(0);
                String productPromoRuleId = (String) productPromoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));


                if ("PROMO_AMOUNT_REDUCE".equals(promoTypeId)) {
                    List<String> promos = StringUtil.split(promoCondActions, "|");
                    if (UtilValidate.isEmpty(promos)) {
                        return ServiceUtil.returnError("配置错误");
                    } else {
                        if (promos.size() == 2) {
                            for (int i = 0; i < promos.size(); i++) {
                                String condActions = promos.get(i);
                                List<String> condAction = StringUtil.split(condActions, ",");
                                if (UtilValidate.isNotEmpty(condAction)) {
                                    String cond = condAction.get(0);
                                    String action = condAction.get(1);
                                    // 调用创建cond
                                    String inputParamEnumId = "PPIP_ORDER_TOTAL";
                                    String operatorEnumId = "PPC_GTE";
                                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                            "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                                    if (ServiceUtil.isError(ret)) {
                                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                        return ret;
                                    }

                                    // 调用创建action
                                    String productPromoActionEnumId = "PROMO_PROD_DISC";
                                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "userLogin", userLogin);
                                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                                    if (ServiceUtil.isError(ret)) {
                                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                        return ret;
                                    }

                                }
                            }
                        } else if (promos.size() > 2) {
                            for (int i = 0; i < promos.size(); i++) {
                                String condActions = promos.get(i);
                                List<String> condAction = StringUtil.split(condActions, ",");
                                if (UtilValidate.isNotEmpty(condAction)) {
                                    String cond = condAction.get(0);
                                    String action = condAction.get(1);
                                    // 调用创建cond
                                    String inputParamEnumId = "PPIP_ORDER_TOTAL";
                                    String operatorEnumId = "PPC_GTE";
                                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                            "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                                    if (ServiceUtil.isError(ret)) {
                                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                        return ret;
                                    }

                                    // 调用创建action
                                    String productPromoActionEnumId = "PROMO_PROD_DISC";
                                    BigDecimal quantity = BigDecimal.ONE;
                                    BigDecimal amount = new BigDecimal(action);
                                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                                    if (ServiceUtil.isError(ret)) {
                                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                        return ret;
                                    }

                                }

                            }
                        }

                    }

                } else if ("PROMO_NUM_REDUCE".equals(promoTypeId)) {
                    List<String> promos = StringUtil.split(promoCondActions, ",");
                    if (UtilValidate.isEmpty(promos)) {
                        return ServiceUtil.returnError("配置错误");
                    } else {
                        if (promos.size() == 2) {
                            String cond = promos.get(0);
                            String action = promos.get(1);
                            // 调用创建cond
                            String inputParamEnumId = "PPIP_PRODUCT_QUANT";
                            String operatorEnumId = "PPC_GTE";
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                    "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                            String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                            // 调用创建action
                            String productPromoActionEnumId = "PROMO_PROD_DISC";
                            BigDecimal quantity = BigDecimal.ONE;
                            BigDecimal amount = new BigDecimal(action);
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                    "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        } else if (promos.size() > 2) {
                            for (int i = 0; i < promos.size(); i++) {
                                String condActions = promos.get(i);
                                List<String> condAction = StringUtil.split(condActions, "|");
                                if (UtilValidate.isNotEmpty(condAction)) {
                                    String cond = condAction.get(0);
                                    String action = condAction.get(1);
                                    // 调用创建cond
                                    String inputParamEnumId = "PPIP_ORDER_TOTAL";
                                    String operatorEnumId = "PPC_GTE";
                                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                            "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                                    if (ServiceUtil.isError(ret)) {
                                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                        return ret;
                                    }
                                    // 调用创建action
                                    String productPromoActionEnumId = "PROMO_PROD_DISC";
                                    BigDecimal quantity = BigDecimal.ONE;
                                    BigDecimal amount = new BigDecimal(action);
                                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                                    if (ServiceUtil.isError(ret)) {
                                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                        return ret;
                                    }
                                }

                            }
                        }
                    }
                }

                //促销对应商品
                if ("PROMO_PRT_ALL".equals(promoProductType)) {
                    //所有产品生效，

                } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_INCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }

                } else if ("PROMO_PRT_PART_EX".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_EXCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }
                }

                //调用促销对应店铺
//            查找所有的店铺

                try {
                    List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                    if (UtilValidate.isNotEmpty(productStores)) {
                        for (int i = 0; i < productStores.size(); i++) {
                            GenericValue productStore = productStores.get(i);
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStore.get("productStoreId"), "fromDate", fromDate,
                                    "thruDate", thruDate, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }

                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
                //会员等级关系

            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 包邮回显数据
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> editPackageDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
            result.put("productPromo", productPromo);
            List<GenericValue> productStorePromoAppls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isNotEmpty(productStorePromoAppls)) {
                GenericValue productStore_PromoAppls = EntityUtil.getFirst(productStorePromoAppls);
                result.put("productStorePromoAppls", productStore_PromoAppls);
            }

            if (UtilValidate.isNotEmpty(productPromo)) {
                List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
                if (UtilValidate.isNotEmpty(productPromoRules)) {
                    GenericValue productPromoRule = productPromoRules.get(0);
                    result.put("productPromoRule", productPromoRule);
                    //cond
                    String ruleId = (String) productPromoRule.get("productPromoRuleId");
                    List<EntityCondition> andExprs = FastList.newInstance();
                    andExprs.add(EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId));
                    andExprs.add(EntityCondition.makeCondition("productPromoRuleId", EntityOperator.EQUALS, ruleId));
                    andExprs.add(EntityCondition.makeCondition("inputParamEnumId", EntityOperator.NOT_EQUAL, "PPIP_PARTY_LEVEL"));
                    List<String> orderBy = FastList.newInstance();
                    orderBy.add("productPromoCondSeqId");

                    List<GenericValue> productPromoConds = delegator.findList("ProductPromoCond", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, orderBy, null, false);
                    result.put("productPromoConds", productPromoConds);

                    result.put("productPromoCond", productPromoConds.get(0));

                    //action
                    List<GenericValue> productPromoActions = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoActions", productPromoActions);
                    String productIds1 = "";
                    //赠送商品的id的集合
                    for (GenericValue actionGV : productPromoActions) {
                        String productId = (String) actionGV.get("productId");
                        productIds1 = productIds1 + productId + ",";
                    }
                    productIds1 = productIds1.substring(0, productIds1.length() - 1);
                    result.put("productIds1", productIds1);
                    //product
                    List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoProducts", productPromoProducts);
                    List<Map<String, Object>> productList = FastList.newInstance();
                    String productIds = "";
                    if (UtilValidate.isNotEmpty(productPromoProducts)) {
                        for (GenericValue coupponPrud : productPromoProducts) {
                            String productId = (String) coupponPrud.get("productId");
                            productIds = productIds + productId + ",";

                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                            Map<String, Object> map = FastMap.newInstance();
                            map.put("productName", product.get("productName"));

                            GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE")));
                            GenericValue marketPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "MARKET_PRICE")));
                            map.put("productName", product.get("productName"));
                            map.put("productId", product.get("productId"));
                            if (UtilValidate.isNotEmpty(defaultprice)) {
                                map.put("defaultprice", defaultprice.get("price"));
                            } else {
                                map.put("defaultprice", 0);
                            }
                            if (UtilValidate.isNotEmpty(marketPrice)) {
                                map.put("marketprice", marketPrice.get("price"));
                            } else {
                                map.put("marketprice", 0);
                            }
                            productList.add(map);
                        }
                        productIds = productIds.substring(0, productIds.length() - 1);

                    }
                    result.put("productIds", productIds);
                    result.put("productList", productList);
                }
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /*批量审批
     *
     * add by gss
     * */
    public Map<String, Object> batchAuditPromoReduce(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String obj = (String) context.get("obj");
        String status = (String) context.get("promoStatus");
        // 截取字符串
        String[] m = obj.split(",");
        String opinion = null;
        // 满减促销Id
        String productPromoId = "";
        // 审核意见
        String auditMessage = "";

        for (int i = 0; i < m.length; i++) {
            opinion = m[i];
            String[] n = opinion.split(":");
            if (n.length > 1) {
                productPromoId = n[0];
                auditMessage = n[1];
            } else {
                productPromoId = n[0];
            }

            try {
                Delegator delegator = dcx.getDelegator();
                GenericValue audit = delegator.makeValue("ProductPromoAudit");
                String promoAuditId = delegator.getNextSeqId("ProductPromoAudit");
                audit.set("promoAuditId", promoAuditId);
                audit.set("productPromoId", productPromoId);
                audit.set("auditMessage", auditMessage);
                audit.set("result", status);
                delegator.create(audit);
                GenericValue promo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
                promo.set("promoStatus", status);
                delegator.store(promo);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return result;
    }

    /* 查看满减驳回原因
     * add  2016-5-5
     * add by gss
     *
     * */
    public Map<String, Object> findAuditMessage(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dcx.getDelegator();
        List<GenericValue> ProductPromoAuditList;
        try {
            ProductPromoAuditList = delegator.findByAnd("ProductPromoAudit", UtilMisc.toMap("productPromoId", productPromoId), UtilMisc.toList("createdStamp"));
            GenericValue ProductPromoAudit = EntityUtil.getFirst(ProductPromoAuditList);
            result.put("ProductPromoAudit", ProductPromoAudit);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


}
