package com.qihua.ofbiz.business;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * 商品品牌管理 新增 查询
 * author cc
 */
public class BusinessBrandServices {
    public static final String module = BusinessBrandServices.class.getName();
    public static final String resource = "PartyUiLabels";

    /**
     * 修改成商品品牌查询 需要修改
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBusinessBrandList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 记录集合
        List<GenericValue> recordsList = FastList.newInstance();

        // 总记录数
        int totalSize = 0;
        // 查询开始条数
        int lowIndex = 0;
        // 查询结束条数
        int highIndex = 0;

        // 跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        // 每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // 查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        // 排序字段集合
        List<String> orderBy = FastList.newInstance();
        // 显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        //品牌表 中 img 品牌名称 别名
        //中间表
        dynamicView.addMemberEntity("PB", "PartyBusinessBrand");
        dynamicView.addAlias("PB", "partyId");
        dynamicView.addAlias("PB", "productBrandId");

        //商家表
        dynamicView.addMemberEntity("P", "PartyBusiness");
        dynamicView.addAlias("P", "partyId");
        dynamicView.addAlias("P", "auditStatus");
        dynamicView.addAlias("P", "logoImg");
        dynamicView.addViewLink("PB", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        //品牌表
        dynamicView.addMemberEntity("B", "ProductBrand");
        dynamicView.addAlias("B", "productBrandId");
        dynamicView.addAlias("B", "brandName");
        dynamicView.addAlias("B", "brandNameAlias");
        dynamicView.addAlias("B", "brandDesc");
        dynamicView.addAlias("B", "createdStamp");
        dynamicView.addViewLink("PB", "B", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        fieldsToSelect.add("partyId");
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("auditStatus");
        fieldsToSelect.add("logoImg");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("brandDesc");
        fieldsToSelect.add("createdStamp");

        //品牌名称 模糊查询
        if (UtilValidate.isNotEmpty(context.get("brandName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("brandName"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("brandName") + "%")));
        }

        // 按法人名称模糊查询
//		if (UtilValidate.isNotEmpty(context.get("legalPersonName"))) {
//			filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("legalPersonName"),
//					EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("legalPersonName") + "%")));
//		}

        // 按审核状态精确查询
        if (UtilValidate.isNotEmpty(context.get("auditStatus"))) {
            filedExprs.add(
                    EntityCondition.makeCondition("auditStatus", EntityOperator.EQUALS, context.get("auditStatus")));
        }

        // 排序字段名称 可能要加
        String sortField = "productBrandId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        // 排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        // 添加where条件
        if (filedExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            // 去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // 查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect,
                    orderBy, findOpts);
            // 获取分页所需的记录集合
            recordsList = pli.getPartialList(lowIndex, viewSize);

            // 获取总记录数
            totalSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > totalSize) {
                highIndex = totalSize;
            }

            // 关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }

        // 返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(totalSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    //查询所有商家提交审核的品牌
    public static Map<String, Object> getPartyReviewBrandList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 记录集合
        List<GenericValue> recordsList = FastList.newInstance();

        // 总记录数
        int totalSize = 0;
        // 查询开始条数
        int lowIndex = 0;
        // 查询结束条数
        int highIndex = 0;

        // 跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        // 每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // 查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        // 排序字段集合
        List<String> orderBy = FastList.newInstance();
        // 显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        //品牌表 中 img 品牌名称 别名
        //中间表
        dynamicView.addMemberEntity("PB", "PartyBusinessBrand");
        dynamicView.addAlias("PB", "partyId");
        dynamicView.addAlias("PB", "productBrandId");

        //商家表
        dynamicView.addMemberEntity("P", "PartyBusiness");
        dynamicView.addAlias("P", "partyId");
        dynamicView.addAlias("P", "auditStatus");
        dynamicView.addAlias("P", "logoImg");
        dynamicView.addViewLink("PB", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        //品牌表
        dynamicView.addMemberEntity("B", "ProductBrand");
        dynamicView.addAlias("B", "productBrandId");
        dynamicView.addAlias("B", "brandName");
        dynamicView.addAlias("B", "brandNameAlias");
        dynamicView.addAlias("B", "brandDesc");
        dynamicView.addAlias("B", "createdStamp");
        dynamicView.addViewLink("PB", "B", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        fieldsToSelect.add("partyId");
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("auditStatus");
        fieldsToSelect.add("logoImg");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("brandDesc");
        fieldsToSelect.add("createdStamp");

        //品牌名称 模糊查询
        if (UtilValidate.isNotEmpty(context.get("brandName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("brandName"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("brandName") + "%")));
        }

        //店铺名 模糊查询
        if (UtilValidate.isNotEmpty(context.get("storeName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("storeName"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("storeName") + "%")));
        }


        // 按审核状态精确查询
        if (UtilValidate.isNotEmpty(context.get("auditStatus"))) {
            filedExprs.add(
                    EntityCondition.makeCondition("auditStatus", EntityOperator.EQUALS, context.get("auditStatus")));
        }

        // 排序字段名称 可能要加
        String sortField = "productBrandId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        // 排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        // 添加where条件
        if (filedExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            // 去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // 查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect,
                    orderBy, findOpts);
            // 获取分页所需的记录集合
            recordsList = pli.getPartialList(lowIndex, viewSize);

            // 获取总记录数
            totalSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > totalSize) {
                highIndex = totalSize;
            }

            // 关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }

        // 返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(totalSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 新增商家时 商家的品牌分类
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getPartyProductCategoryList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> productCategoryList = FastList.newInstance();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String partyId = "";
        if (UtilValidate.isNotEmpty(userLogin)) {
            String userPartyId = userLogin.getString("partyId");
            if (UtilValidate.isNotEmpty(userPartyId)) {
                try {
                    List<GenericValue> partyRelationships = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userPartyId));
                    if (UtilValidate.isNotEmpty(partyRelationships)) {
                        GenericValue partyRelationship = partyRelationships.get(0);
                        partyId = partyRelationship.getString("partyIdFrom");
                    }
                } catch (GenericEntityException e) {
                    String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                    Debug.logError(e, errMsg, module);
                }
            }
        }
        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        dynamicView.addMemberEntity("PC", "PartyProductCategory");//CATEGORY_NAME
        dynamicView.addAlias("PC", "id", "productCategoryId", null, false, false, null);
        dynamicView.addAlias("PC", "name", "categoryName", null, false, false, null);
        dynamicView.addAlias("PC", "partyId", "partyId", null, false, false, null);
        dynamicView.addAlias("PC", "pId", "parentCategoryId", null, false, false, null);
//		dynamicView.addAlias("PC", "isDel");//PARENT_CATEGORY_ID

        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        fieldsToSelect.add("id");
        fieldsToSelect.add("name");
        fieldsToSelect.add("pId");
        fieldsToSelect.add("partyId");
//		fieldsToSelect.add("isDel");

        dynamicView.setGroupBy(fieldsToSelect);
        orderBy.add("pId");

        dynamicView.setGroupBy(fieldsToSelect);
        orderBy.add("partyId");

        List<EntityCondition> entityConditionList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(partyId)) {
            entityConditionList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        }

        try {
            // 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView,
                    EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, fieldsToSelect,
                    orderBy, findOpts);
            productCategoryList = pli.getCompleteList();
            // 关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
        }

        result.put("productCategoryList", productCategoryList);
        return result;
    }

    //

    /**
     * 平台根据 partyId 查看商品分类
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getPartyProductCategoryListByParyId(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> productCategoryList = FastList.newInstance();
        String partyId = (String) context.get("partyId");

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        dynamicView.addMemberEntity("PC", "PartyProductCategory");//CATEGORY_NAME
        dynamicView.addAlias("PC", "id", "productCategoryId", null, false, false, null);
        dynamicView.addAlias("PC", "name", "categoryName", null, false, false, null);
        dynamicView.addAlias("PC", "partyId", "partyId", null, false, false, null);
        dynamicView.addAlias("PC", "pId", "parentCategoryId", null, false, false, null);
//		dynamicView.addAlias("PC", "isDel");//PARENT_CATEGORY_ID

        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        fieldsToSelect.add("id");
        fieldsToSelect.add("name");
        fieldsToSelect.add("pId");
        fieldsToSelect.add("partyId");
//		fieldsToSelect.add("isDel");

        dynamicView.setGroupBy(fieldsToSelect);
        orderBy.add("pId");

        dynamicView.setGroupBy(fieldsToSelect);
        orderBy.add("partyId");

        List<EntityCondition> entityConditionList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(partyId)) {
            entityConditionList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        }

        try {
            // 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView,
                    EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, fieldsToSelect,
                    orderBy, findOpts);
            productCategoryList = pli.getCompleteList();
            // 关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
        }

        result.put("productCategoryList", productCategoryList);
        return result;
    }


    /**
     * 平台
     * 品牌审核页面 列表展示商家提交的待审核
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findStoreBrandForWaiting(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String brandName = (String) context.get("brandName");
        String partyName = (String) context.get("partyName");
        Delegator delegator = dcx.getDelegator();


        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> activityList = FastList.newInstance();
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

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        dynamicView.addMemberEntity("PB", "ProductBrand");
        dynamicView.addAlias("PB", "productBrandId");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addAlias("PB", "brandNameAlias");
        dynamicView.addAlias("PBB", "isDel1", "isDel", null, false, null, null);


        dynamicView.addMemberEntity("PBB", "PartyBusinessBrand");
        dynamicView.addAlias("PBB", "partyBusinessBrandId");
        dynamicView.addAlias("PBB", "partyId");
        dynamicView.addAlias("PBB", "auditStatus");
        dynamicView.addAlias("PBB", "createdStamp");
        dynamicView.addAlias("PBB", "isDel2", "isDel", null, false, null, null);


        dynamicView.addViewLink("PB", "PBB", false, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        dynamicView.addMemberEntity("PSE", "ProductStore");
        dynamicView.addAlias("PSE", "storeName");
        dynamicView.addAlias("PSE", "ownerPartyId");
        dynamicView.addViewLink("PBB", "PSE", false, ModelKeyMap.makeKeyMapList("partyId", "ownerPartyId"));



        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("createdStamp");

        fieldsToSelect.add("partyId");
        //
        fieldsToSelect.add("auditStatus");

        fieldsToSelect.add("storeName");
        fieldsToSelect.add("partyBusinessBrandId");

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("createdStamp" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";

        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("auditStatus"), EntityOperator.EQUALS, "0"));
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isDel2"), EntityOperator.EQUALS, "N"));
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isDel1"), EntityOperator.EQUALS, "N"));

        if (UtilValidate.isNotEmpty(partyName)) {
            paramList = paramList + "&partyName=" + partyName;
            andExprs.add(EntityCondition.makeCondition("storeName", EntityOperator.LIKE, EntityFunction.UPPER("%" + partyName + "%")));
        }

        if (UtilValidate.isNotEmpty(brandName)) {
            paramList = paramList + "&brandName=" + brandName;
            andExprs.add(EntityCondition.makeCondition("brandName", EntityOperator.LIKE, EntityFunction.UPPER("%" + brandName + "%")));
        }

        List<GenericValue> activities = null;
        // build the main condition
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {

            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            activityList = pli.getPartialList(lowIndex, viewSize);

            listSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > listSize) {
                highIndex = listSize;
            }

            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg);
            return ServiceUtil.returnError(e.getMessage());
        }finally{

        }

        result.put("groupList", activityList);
        result.put("groupListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    /**
     * 查看品牌 模糊查询
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findProductBrands(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String brandName = (String) context.get("brandName");
        String partyName = (String) context.get("partyName");
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

        List<GenericValue> activityList = FastList.newInstance();
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

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        dynamicView.addMemberEntity("PB", "ProductBrand");
        dynamicView.addAlias("PB", "productBrandId");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addAlias("PB", "brandNameAlias");
        dynamicView.addAlias("PB", "isDel");//Y N B R
//		dynamicView.addAlias("PB", "isUsed");//Y
        dynamicView.addAlias("PB", "createdStamp");

        dynamicView.addMemberEntity("PBB", "PartyBusinessBrand");
        dynamicView.addAlias("PBB", "partyId");
        dynamicView.addAlias("PBB", "logoContentId");
        dynamicView.addAlias("PBB", "certificateContentId");
        dynamicView.addViewLink("PB", "PBB", false, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        dynamicView.addMemberEntity("PG", "PartyGroup");
        dynamicView.addAlias("PG", "partyName");
        dynamicView.addViewLink("PBB", "PG", false, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("isDel");
        fieldsToSelect.add("createdStamp");

        fieldsToSelect.add("partyId");
        //自定义申请品牌时 logo和证书
        fieldsToSelect.add("logoContentId");
        fieldsToSelect.add("certificateContentId");

        fieldsToSelect.add("partyName");

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("createdStamp" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";

        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isDel"), EntityOperator.EQUALS, "B"));

        if (UtilValidate.isNotEmpty(partyName)) {
            paramList = paramList + "&partyName=" + partyName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + partyName + "%")));
        }

        if (UtilValidate.isNotEmpty(brandName)) {
            paramList = paramList + "&brandName=" + brandName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("brandName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + brandName + "%")));
        }

        List<GenericValue> activities = null;
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
//                EntityListIterator pli = delegator.find("ProductActivity", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
                // get the partial list for this page
                activityList = pli.getPartialList(lowIndex, viewSize);

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
        result.put("groupList", activityList);
//		result.put("activityCode", activityCode);
        result.put("brandName", brandName);
        result.put("partyName", partyName);
//		result.put("activityEndDate", activityEndDate);
//		result.put("activityAuditStatus", activityAuditStatus);

        result.put("groupListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 商家端
     * 查看品牌列表
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findPartyProductBrand(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String brandName = (String) context.get("brandName");
//        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String auditStatus = (String) context.get("auditStatus");
        Delegator delegator = dcx.getDelegator();
//        String partyId = userLogin.getString("partyId");

        String productStoreId = (String) context.get("productStoreId");
        //根据productStoreId查询partyId
        String partyId =EntityUtil.getFirst(delegator.findByAnd("PartyGroup",UtilMisc.toMap("productStoreId",productStoreId))).getString("partyId");
        Locale locale = (Locale) context.get("locale");

        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> activityList = FastList.newInstance();
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

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        dynamicView.addMemberEntity("PB", "ProductBrand");
        dynamicView.addAlias("PB", "productBrandId");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addAlias("PB", "brandNameAlias");
        dynamicView.addAlias("PB", "isDel1", "isDel", null, false, null, null);
        dynamicView.addAlias("PB", "createdStamp");

        //关联表
        dynamicView.addMemberEntity("PBB", "PartyBusinessBrand");
        dynamicView.addAlias("PBB", "partyBusinessBrandId");
        dynamicView.addAlias("PBB", "productBrandId");
        dynamicView.addAlias("PBB", "auditStatus");
        dynamicView.addAlias("PBB", "isDel2", "isDel", null, false, null, null);
        dynamicView.addAlias("PBB", "partyId");

        dynamicView.addViewLink("PB", "PBB", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("partyBusinessBrandId");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("auditStatus");
        fieldsToSelect.add("createdStamp");


        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("createdStamp" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();

        String paramList = "";

        andExprs.add(EntityCondition.makeCondition("isDel1", EntityOperator.EQUALS, "N"));
        andExprs.add(EntityCondition.makeCondition("isDel2", EntityOperator.EQUALS, "N"));
        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        if (UtilValidate.isNotEmpty(brandName)) {
            paramList = paramList + "&brandName=" + brandName;
            andExprs.add(EntityCondition.makeCondition("brandName", EntityOperator.LIKE, "%" + brandName + "%"));
        }
        if (UtilValidate.isNotEmpty(auditStatus)) {
            paramList = paramList + "&auditStatus=" + auditStatus;
            andExprs.add(EntityCondition.makeCondition("auditStatus", EntityOperator.EQUALS, auditStatus));
        }
        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        EntityListIterator pli = null;
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            activityList = pli.getPartialList(lowIndex, viewSize);
            listSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > listSize) {
                highIndex = listSize;
            }


        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg);
            return ServiceUtil.returnError(e.getMessage());
        } finally {
            pli.close();
        }

        result.put("auditStatus", auditStatus == null ? "" : auditStatus);
        result.put("brandList", activityList);

        result.put("brandListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 根据productBrandId查看品牌 商家端
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findPartyProductBrandById(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productBrandId = (String) context.get("productBrandId");
        Delegator delegator = dcx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String partyId = "";
        if (UtilValidate.isNotEmpty(userLogin)) {
            partyId = userLogin.getString("partyId");
        }

        List<GenericValue> activityList = FastList.newInstance();

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        dynamicView.addMemberEntity("PB", "ProductBrand");
//		dynamicView.addAlias("PB", "productBrandId");
        dynamicView.addAliasAll("PB", "", null);
        dynamicView.addAlias("PB", "productBrandId");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addAlias("PB", "brandNameAlias");
//        dynamicView.addAlias("PB", "isUsed");
        dynamicView.addAlias("PB", "isDel");
//		dynamicView.addAlias("PB", "brandDesc");
        dynamicView.addAlias("PB", "createdStamp");

        dynamicView.addViewLink("PB", "BC", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        //关联表
        dynamicView.addMemberEntity("PBB", "PartyBusinessBrand");
//		dynamicView.addAlias("PBB", "partyId");
//		dynamicView.addAlias("PBB", "productBrandId");
        dynamicView.addAlias("PBB", "logoContentId");
        dynamicView.addAlias("PBB", "certificateContentId");
        dynamicView.addAlias("PBB", "auditStatus");

        dynamicView.addViewLink("PB", "PBB", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        dynamicView.addMemberEntity("BC", "ProductBrandCategory");//PRODUCT_BRAND_CATEGORY
        dynamicView.addAlias("BC", "productBrandId");
        dynamicView.addViewLink("BC", "PBC", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productCategoryId", "productCategoryId"));

        dynamicView.addMemberEntity("PBC", "PartyProductCategory");//PARTY_PRODUCT_CATEGORY
        dynamicView.addAlias("PBC", "productCategoryId");
        dynamicView.addViewLink("PBC", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyIdFrom"));

        dynamicView.addMemberEntity("PR", "PartyRelationship");
        dynamicView.addAlias("PR", "partyIdFrom");
        dynamicView.addAlias("PR", "partyIdTo");

        dynamicView.addMemberEntity("PG", "PartyGroup");
        dynamicView.addAlias("PG", "partyName");
        dynamicView.addViewLink("PR", "PG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyIdFrom", "partyId"));

        dynamicView.addMemberEntity("U", "UserLogin");
        dynamicView.addAlias("U", "partyId");

        dynamicView.addViewLink("PR", "U", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyIdTo", "partyId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("isDel");
        fieldsToSelect.add("createdStamp");

        fieldsToSelect.add("partyName");

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;

        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isDel"), EntityOperator.EQUALS, "B"));

        if (UtilValidate.isNotEmpty(productBrandId)) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productBrandId"), EntityOperator.EQUALS, productBrandId));
        }

        if (UtilValidate.isNotEmpty(partyId)) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyId"), EntityOperator.EQUALS, partyId));
        }

        // build the main condition
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {

            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
            // using list iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), null, findOpts);
            // get the partial list for this page
            activityList = pli.getCompleteList();

            // close the list iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg);
            return ServiceUtil.returnError(e.getMessage());
        }

        result.put("groupList", activityList);
        result.put("productBrandId", productBrandId);

        return result;
    }

    /**
     * 商家端 根据productBrandId删掉品牌
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> deletePartyProductBrand(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String partyBusinessBrandId = (String) context.get("partyBusinessBrandId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
//        String partyId = userLogin.getString("partyId");
        Delegator delegator = dcx.getDelegator();
        String productStoreId = (String) context.get("productStoreId");
        //根据productStoreId查询partyId
        String partyId =EntityUtil.getFirst(delegator.findByAnd("PartyGroup",UtilMisc.toMap("productStoreId",productStoreId))).getString("partyId");

        try {

            GenericValue productBrand = delegator.findByPrimaryKey("PartyBusinessBrand", UtilMisc.toMap("partyBusinessBrandId", partyBusinessBrandId));
            if (UtilValidate.isNotEmpty(productBrand)) {
                productBrand.set("isDel", "Y");
                productBrand.store();
            }

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 商家端 根据productBrandId查看品牌分类
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getPartyProductBrandCategoryList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Map<String, List> paramMap = FastMap.newInstance();
        //品牌productBrandId
        String productBrandId = (String) context.get("productBrandId");

        try {
            //根据品牌ID获取该品牌的分类信息
            List<GenericValue> productBrandCategoryList = delegator.findByAnd("ProductBrandCategory", UtilMisc.toMap("productBrandId", productBrandId), null);
            paramMap.put("productBrandCategoryList", productBrandCategoryList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("paramMap", paramMap);
        return result;
    }


    /**
     * 根据id查看品牌
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findProductBrandById(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productBrandId = (String) context.get("productBrandId");
        Delegator delegator = dcx.getDelegator();

        List<GenericValue> activityList = FastList.newInstance();

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        dynamicView.addMemberEntity("PB", "ProductBrand");
        dynamicView.addAlias("PB", "productBrandId");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addAlias("PB", "brandNameAlias");
        dynamicView.addAlias("PB", "isDel");//Y N B R
//		dynamicView.addAlias("PB", "isUsed");//Y
        dynamicView.addAlias("PB", "createdStamp");

        dynamicView.addMemberEntity("PBB", "PartyBusinessBrand");
        dynamicView.addAlias("PBB", "partyId");
        dynamicView.addViewLink("PB", "PBB", false, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        dynamicView.addMemberEntity("PG", "PartyGroup");
        dynamicView.addAlias("PG", "partyName");
        dynamicView.addViewLink("PBB", "PG", false, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("isDel");
        fieldsToSelect.add("createdStamp");

        fieldsToSelect.add("partyId");
        fieldsToSelect.add("partyName");

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;

        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isDel"), EntityOperator.EQUALS, "B"));

        if (UtilValidate.isNotEmpty(productBrandId)) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productBrandId"), EntityOperator.EQUALS, productBrandId));
        }

        // build the main condition
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {

            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
            // using list iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), null, findOpts);
            // get the partial list for this page
            activityList = pli.getCompleteList();

            // close the list iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg);
            return ServiceUtil.returnError(e.getMessage());
        }

        result.put("groupList", activityList);
        result.put("productBrandId", productBrandId);

        return result;
    }


    /**
     * 审核品牌
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> auditProductBrand(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String partyBusinessBrandId = (String) context.get("partyBusinessBrandId");
        String productBrandId = (String) context.get("productBrandId");
        String partyId = (String) context.get("partyId");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
        String message = (String) context.get("auditMessage");
        Delegator delegator = dcx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String userLoginId = userLogin.getString("userLoginId");
        try {
            GenericValue auditLog = delegator.makeValue("PromoAuditLog");
            auditLog.setNextSeqId();
            String businessId = "brand_" + partyBusinessBrandId;
            auditLog.put("businessId", businessId);
            auditLog.put("auditType", "ACTY_AUDIT_PASS".equals(activityAuditStatus) ? "通过" : "驳回");
            auditLog.put("auditPerson", userLoginId);
            auditLog.put("auditMessage", message);
            auditLog.put("createDate", new Timestamp(System.currentTimeMillis()));
            auditLog.create();

            List<GenericValue> partyBusinessBrands = delegator.findByAnd("PartyBusinessBrand", UtilMisc.toMap("partyBusinessBrandId", partyBusinessBrandId));
            if (UtilValidate.isNotEmpty(partyBusinessBrands)) {
                GenericValue partyBusinessBrand = partyBusinessBrands.get(0);
                //查看该品牌是否商家自定义品牌
                String isOwner = partyBusinessBrand.getString("isOwner");
                if ("ACTY_AUDIT_PASS".equals(activityAuditStatus)) {
                    partyBusinessBrand.set("auditStatus", "1");
                } else {
                    partyBusinessBrand.set("auditStatus", "2");
                }
                partyBusinessBrand.set("auditMessage", message);
                partyBusinessBrand.store();
                if("Y".equalsIgnoreCase(isOwner)){
                    //自定义品牌，需要设置品牌的审批状态
                    String auditStatus="";
                    if ("ACTY_AUDIT_PASS".equals(activityAuditStatus)) {
                        auditStatus="1";
                    } else {
                        auditStatus="2";
                    }
                    Map<String, Object> updateFields = FastMap.newInstance();
                    updateFields.put("auditStatus",auditStatus);
                    EntityCondition UpdateCon = EntityCondition.makeCondition("productBrandId", EntityComparisonOperator.EQUALS, productBrandId);
                    delegator.storeByCondition("ProductBrand", updateFields, UpdateCon);
                }

            }

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查看审核品牌 拒绝原因
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> findBrandAuditMessage(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String partyBusinessBrandId = (String) context.get("partyBusinessBrandId");

        Delegator delegator = dcx.getDelegator();

        try {

            GenericValue brand = delegator.findByPrimaryKey("PartyBusinessBrand",UtilMisc.toMap("partyBusinessBrandId",partyBusinessBrandId));
            String curAuditMessage = "没有输入内容";
            if (UtilValidate.isNotEmpty(brand)) {
                curAuditMessage = brand.getString("auditMessage");
            }
            result.put("curAuditMessage", curAuditMessage);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据id删除品牌
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> deleteBrand(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productBrandId = (String) context.get("productBrandId");
        Delegator delegator = dcx.getDelegator();

        try {

            GenericValue productBrand = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", productBrandId));
            if (UtilValidate.isNotEmpty(productBrandId)) {
                productBrand.set("isDel", "Y");
                productBrand.set("productBrandId", productBrandId);
                productBrand.store();
            }

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 商家详情 add by qianjin 2016.01.27
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBusinessInfoById(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 参数
        String businessId = (String) context.get("businessId");
        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // 显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("PB", "PartyBusiness");
        dynamicView.addAlias("PB", "partyId");
        dynamicView.addAlias("PB", "businessName");
        dynamicView.addAlias("PB", "legalPersonName");
        dynamicView.addAlias("PB", "tel");
        dynamicView.addAlias("PB", "logoImg");
        dynamicView.addAlias("PB", "address");
        dynamicView.addAlias("PB", "idCard");
        dynamicView.addAlias("PB", "businessLicense");
        dynamicView.addAlias("PB", "idCardProsImg");
        dynamicView.addAlias("PB", "idCardConsImg");
        dynamicView.addAlias("PB", "businessLicenseImg");
        dynamicView.addAlias("PB", "auditStatus");
        dynamicView.addAlias("PB", "description");
        dynamicView.addAlias("PB", "createdStamp");
        dynamicView.addAlias("PB", "businessType");

        dynamicView.addMemberEntity("P", "Party");
        dynamicView.addAlias("P", "statusId");
        dynamicView.addViewLink("PB", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        dynamicView.addMemberEntity("PR", "PartyRelationship");
        dynamicView.addAlias("PR", "partyIdTo");
        dynamicView.addViewLink("PB", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyIdFrom"));

        dynamicView.addMemberEntity("G1", "Geo");
        dynamicView.addAlias("G1", "provinceId", "geoId", null, false, false, null);
        dynamicView.addAlias("G1", "provinceName", "geoName", null, false, false, null);
        dynamicView.addViewLink("PB", "G1", Boolean.FALSE, ModelKeyMap.makeKeyMapList("province", "geoId"));

        dynamicView.addMemberEntity("G2", "Geo");
        dynamicView.addAlias("G2", "cityId", "geoId", null, false, false, null);
        dynamicView.addAlias("G2", "cityName", "geoName", null, false, false, null);
        dynamicView.addViewLink("PB", "G2", Boolean.FALSE, ModelKeyMap.makeKeyMapList("city", "geoId"));

        dynamicView.addMemberEntity("G3", "Geo");
        dynamicView.addAlias("G3", "countyId", "geoId", null, false, false, null);
        dynamicView.addAlias("G3", "countyName", "geoName", null, false, false, null);
        dynamicView.addViewLink("PB", "G3", Boolean.FALSE, ModelKeyMap.makeKeyMapList("county", "geoId"));

        dynamicView.addMemberEntity("EN", "Enumeration");
        dynamicView.addAlias("EN", "businessTypeId", "enumId", null, false, false, null);
        dynamicView.addAlias("EN", "businessTypeName", "description", null, false, false, null);
        dynamicView.addViewLink("PB", "EN", Boolean.FALSE, ModelKeyMap.makeKeyMapList("businessType", "enumId"));

        fieldsToSelect.add("businessName");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("partyIdTo");
        fieldsToSelect.add("provinceId");
        fieldsToSelect.add("provinceName");
        fieldsToSelect.add("cityId");
        fieldsToSelect.add("cityName");
        fieldsToSelect.add("countyId");
        fieldsToSelect.add("countyName");
        fieldsToSelect.add("address");
        fieldsToSelect.add("tel");
        fieldsToSelect.add("legalPersonName");
        fieldsToSelect.add("idCard");
        fieldsToSelect.add("businessLicense");
        fieldsToSelect.add("auditStatus");
        fieldsToSelect.add("statusId");
        fieldsToSelect.add("idCardProsImg");
        fieldsToSelect.add("idCardConsImg");
        fieldsToSelect.add("businessLicenseImg");
        fieldsToSelect.add("logoImg");
        fieldsToSelect.add("createdStamp");
        fieldsToSelect.add("description");
        fieldsToSelect.add("businessTypeId");
        fieldsToSelect.add("businessTypeName");
        // 条件
        EntityCondition whereCond = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, businessId);

        try {
            // 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, whereCond, null, fieldsToSelect,
                    null, null);
            Map record_map = FastMap.newInstance();
            for (GenericValue gv : pli.getCompleteList()) {
                record_map.put("businessName", gv.getString("businessName"));
                record_map.put("partyId", gv.getString("partyId"));
                record_map.put("partyIdTo", gv.getString("partyIdTo"));
                record_map.put("provinceId", gv.getString("provinceId"));
                record_map.put("provinceName", gv.getString("provinceName"));
                record_map.put("cityId", gv.getString("cityId"));
                record_map.put("cityName", gv.getString("cityName"));
                record_map.put("countyId", gv.getString("countyId"));
                record_map.put("countyName", gv.getString("countyName"));
                record_map.put("address", gv.getString("address"));
                record_map.put("tel", gv.getString("tel"));
                record_map.put("legalPersonName", gv.getString("legalPersonName"));
                record_map.put("idCard", gv.getString("idCard"));
                record_map.put("businessLicense", gv.getString("businessLicense"));
                record_map.put("businessTypeId", gv.getString("businessTypeId"));
                record_map.put("businessTypeName", gv.getString("businessTypeName"));
                // 替换审核状态
                String auditStatus = "";
                if ("0".equals(gv.getString("auditStatus"))) {
                    auditStatus = "待审核";
                } else if ("1".equals(gv.getString("auditStatus"))) {
                    auditStatus = "已通过";
                } else if ("2".equals(gv.getString("auditStatus"))) {
                    auditStatus = "已拒绝";
                }
                record_map.put("auditStatus", auditStatus);
                // 替换启用状态
                String statusId = "";
                if ("PARTY_ENABLED".equals(gv.getString("statusId"))) {
                    statusId = "是";
                } else {
                    statusId = "否";
                }
                record_map.put("isUse", gv.getString("statusId"));
                record_map.put("statusId", statusId);
                record_map.put("idCardProsImg", gv.getString("idCardProsImg"));
                record_map.put("idCardConsImg", gv.getString("idCardConsImg"));
                record_map.put("businessLicenseImg", gv.getString("businessLicenseImg"));
                record_map.put("logoImg", gv.getString("logoImg"));
                record_map.put("createdStamp", gv.getString("createdStamp"));
                record_map.put("description", gv.getString("description"));

                // 获取商家的品牌，并编辑成字符串格式
                // 动态view
                DynamicViewEntity pbb_dv = new DynamicViewEntity();
                pbb_dv.addMemberEntity("PBB", "PartyBusinessBrand");
                pbb_dv.addAlias("PBB", "partyId");
                pbb_dv.addAlias("PBB", "productBrandId");
                pbb_dv.addMemberEntity("PB", "ProductBrand");
                pbb_dv.addAlias("PB", "brandName");
                pbb_dv.addViewLink("PBB", "PB", false, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));
                // 条件
                EntityCondition cond = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
                        gv.getString("partyId"));

                EntityListIterator brand_list = delegator.findListIteratorByCondition(pbb_dv, cond, null, null, null,
                        findOpts);
                String brands_str = "";
                String brandIds_str = "";
                for (int i = 0; i < brand_list.getCompleteList().size(); i++) {
                    brands_str += brand_list.getCompleteList().get(i).getString("brandName");
                    brandIds_str += brand_list.getCompleteList().get(i).getString("productBrandId");
                    if (i != brand_list.getCompleteList().size() - 1) {
                        brands_str += "、";
                        brandIds_str += ",";
                    }
                }
                record_map.put("brands", brands_str);
                record_map.put("brandIds", brandIds_str);
                brand_list.close();
            }
            result.put("record", record_map);
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 商家修改 add by qianjin 2016.01.28
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 参数
        String businessName = (String) context.get("businessName");
        String businessId = (String) context.get("businessId");
        List<String> brands = UtilValidate.isEmpty(context.get("brands[]")) ? FastList.newInstance()
                : (List) context.get("brands[]");
        String logoImg = (String) context.get("logoImg");
        String province = (String) context.get("province");
        String city = (String) context.get("city");
        String county = (String) context.get("county");
        String address = (String) context.get("address");
        String tel = (String) context.get("tel");
        String isUse = (String) context.get("isUse");
        String legalPersonName = (String) context.get("legalPersonName");
        String idCard = (String) context.get("idCard");
        String businessLicense = (String) context.get("businessLicense");
        String idCardProsImg = (String) context.get("idCardProsImg");
        String idCardConsImg = (String) context.get("idCardConsImg");
        String businessLicenseImg = (String) context.get("businessLicenseImg");
        String description = (String) context.get("description");
        // 商家类型
        String businessType = (String) context.get("businessType");

        try {
            // 调用账号启用服务
            dispatcher.runSync("editBusinessIsUse", UtilMisc.toMap("businessId", businessId, "isUse", isUse));

            // 根据ID获取商家记录并修改
            GenericValue pb_gv = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", businessId));
            pb_gv.setString("businessName", businessName);
            pb_gv.setString("logoImg", logoImg);
            pb_gv.setString("province", province);
            pb_gv.setString("city", city);
            pb_gv.setString("county", county);
            pb_gv.setString("address", address);
            pb_gv.setString("tel", tel);
            pb_gv.setString("legalPersonName", legalPersonName);
            pb_gv.setString("idCard", idCard);
            pb_gv.setString("businessLicense", businessLicense);
            pb_gv.setString("idCardProsImg", idCardProsImg);
            pb_gv.setString("idCardConsImg", idCardConsImg);
            pb_gv.setString("businessLicenseImg", businessLicenseImg);
            pb_gv.setString("description", description);
            pb_gv.setString("businessType", businessType);

            pb_gv.store();

            // 删除该商家的品牌数据
            delegator.removeByAnd("PartyBusinessBrand", UtilMisc.toMap("partyId", businessId));
            // 新增商家和品牌关系
            for (String brandId : brands) {
                GenericValue brand_gv = delegator.makeValue("PartyBusinessBrand",
                        UtilMisc.toMap("partyId", businessId, "productBrandId", brandId));
                brand_gv.create();
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 商家新增 add by qianjin 2016.01.26
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 参数
        String businessName = (String) context.get("businessName");
        String partyId = (String) context.get("partyId");
        List<String> brands = UtilValidate.isEmpty(context.get("brands[]")) ? FastList.newInstance()
                : (List) context.get("brands[]");
        String logoImg = (String) context.get("logoImg");
        String province = (String) context.get("province");
        String city = (String) context.get("city");
        String county = (String) context.get("county");
        String address = (String) context.get("address");
        String tel = (String) context.get("tel");
        String isUse = UtilValidate.isEmpty(context.get("isUse")) ? "PARTY_DISABLED" : (String) context.get("isUse");
        String legalPersonName = (String) context.get("legalPersonName");
        String idCard = (String) context.get("idCard");
        String businessLicense = (String) context.get("businessLicense");
        String idCardProsImg = (String) context.get("idCardProsImg");
        String idCardConsImg = (String) context.get("idCardConsImg");
        String businessLicenseImg = (String) context.get("businessLicenseImg");
        String auditStatus = (String) context.get("auditStatus");
        String description = (String) context.get("description");
        String businessType = (String) context.get("businessType");

        try {
            // 调用检查人员是否已申请商家服务
            Map cpb_map = dispatcher.runSync("checkPartyIsBusiness", UtilMisc.toMap("partyId", partyId, "equals", "1"));
            int isHas = (Integer) cpb_map.get("status");
            // 0:有申请记录 1：没有申请记录
            if (isHas == 0) {
                // 修改申请记录的状态，并开通账号启用状态
                List<GenericValue> b_list = (List) cpb_map.get("recordsList");
                for (GenericValue gv : b_list) {
                    String b_id = gv.getString("partyId");
                    dispatcher.runSync("editBusinessIsUse", UtilMisc.toMap("businessId", b_id, "isUse", isUse));
                    // 根据ID获取商家记录并修改
                    GenericValue pb_gv = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", b_id));
                    pb_gv.setString("businessName", businessName);
                    pb_gv.setString("logoImg", logoImg);
                    pb_gv.setString("province", province);
                    pb_gv.setString("city", city);
                    pb_gv.setString("county", county);
                    pb_gv.setString("address", address);
                    pb_gv.setString("tel", tel);
                    pb_gv.setString("legalPersonName", legalPersonName);
                    pb_gv.setString("idCard", idCard);
                    pb_gv.setString("businessLicense", businessLicense);
                    pb_gv.setString("idCardProsImg", idCardProsImg);
                    pb_gv.setString("idCardConsImg", idCardConsImg);
                    pb_gv.setString("businessLicenseImg", businessLicenseImg);
                    pb_gv.setString("auditStatus", "1");
                    pb_gv.setString("description", description);
                    pb_gv.setString("businessType", businessType);
                    pb_gv.store();
                }
            } else {
                String business_id = getBusinessPartyId(delegator);
                // 调用创建party服务
                Map pg_map = dispatcher.runSync("createPartyGroup", UtilMisc.toMap("groupName", businessName, "partyId",
                        business_id, "statusId", isUse, "partyCategory", "BUSINESS"));

                // 新增一条商家记录
                GenericValue business_gv = delegator.makeValue("PartyBusiness", UtilMisc.toMap("partyId", business_id));
                business_gv.setString("businessName", businessName);
                business_gv.setString("logoImg", logoImg);
                business_gv.setString("province", province);
                business_gv.setString("city", city);
                business_gv.setString("county", county);
                business_gv.setString("address", address);
                business_gv.setString("tel", tel);
                business_gv.setString("legalPersonName", legalPersonName);
                business_gv.setString("idCard", idCard);
                business_gv.setString("businessLicense", businessLicense);
                business_gv.setString("idCardProsImg", idCardProsImg);
                business_gv.setString("idCardConsImg", idCardConsImg);
                business_gv.setString("businessLicenseImg", businessLicenseImg);
                business_gv.setString("businessType", businessType);
                if (UtilValidate.isEmpty(auditStatus)) {
                    auditStatus = "1";
                    // 调用创建userLogin服务
                    dispatcher.runSync("createUserLogin",
                            UtilMisc.toMap("userLogin", (GenericValue) context.get("userLogin"), "userLoginId",
                                    business_id, "enabled", "Y", "currentPassword", "123456", "currentPasswordVerify",
                                    "123456", "partyId", business_id));

                    // 调用创建登录用户权限服务
                    dispatcher.runSync("addUserLoginToSecurityGroup",
                            UtilMisc.toMap("userLogin", (GenericValue) context.get("userLogin"), "userLoginId",
                                    business_id, "groupId", "SUPPLIER"));
                }
                business_gv.setString("auditStatus", auditStatus);
                business_gv.setString("description", description);
                business_gv.create();

                // 调用创建party角色服务，创建用户角色
                dispatcher.runSync("ensurePartyRole", UtilMisc.toMap("userLogin",
                        (GenericValue) context.get("userLogin"), "partyId", partyId, "roleTypeId", "EMPLOYEE"));
                // 调用创建party角色服务，创建商家角色
                dispatcher.runSync("ensurePartyRole", UtilMisc.toMap("userLogin",
                        (GenericValue) context.get("userLogin"), "partyId", business_id, "roleTypeId", "SUPPLIER"));

                // 调用创建商家和会员的关联关系服务
                dispatcher.runSync("createPartyRelationship",
                        UtilMisc.toMap("userLogin", (GenericValue) context.get("userLogin"), "partyIdFrom", business_id,
                                "partyIdTo", partyId, "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE",
                                "partyRelationshipTypeId", "OWNER"));
                if (UtilValidate.isNotEmpty(brands)) {
                    // 新增商家和品牌关系
                    for (String brandId : brands) {
                        GenericValue brand_gv = delegator.makeValue("PartyBusinessBrand",
                                UtilMisc.toMap("partyId", business_id, "productBrandId", brandId));
                        brand_gv.create();
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据下一个商家partyID，确保该partyID创建的登录账号唯一 add by qianjin 2016.01.26
     *
     * @param delegator
     * @return
     */
    private static String getBusinessPartyId(Delegator delegator) {
        String business_id = "SJ_" + delegator.getNextSeqId("Party");
        try {
            GenericValue ul_gv = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", business_id));
            if (UtilValidate.isNotEmpty(ul_gv)) {
                business_id = getBusinessPartyId(delegator);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return business_id;
    }

    /**
     * 检查该用户是否已是商家 add by qianjin 2016.01.26
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> checkPartyIsBusiness(DispatchContext dctx,
                                                           Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 参数
        String partyId = (String) context.get("partyId");

        // 动态view
        DynamicViewEntity pr_dv = new DynamicViewEntity();
        pr_dv.addMemberEntity("PB", "PartyBusiness");
        pr_dv.addAlias("PB", "partyId");
        pr_dv.addAlias("PB", "auditStatus");

        pr_dv.addMemberEntity("PR", "PartyRelationship");
        pr_dv.addAlias("PR", "partyIdFrom");
        pr_dv.addAlias("PR", "partyIdTo");
        pr_dv.addAlias("PR", "roleTypeIdFrom");
        pr_dv.addViewLink("PB", "PR", false, ModelKeyMap.makeKeyMapList("partyId", "partyIdFrom"));
        // 查询条件集合，用于数据库查询
        List<EntityCondition> exprs = FastList.newInstance();
        EntityCondition cond = null;
        exprs.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, partyId));
        exprs.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "SUPPLIER"));

        // 添加where条件
        if (exprs.size() > 0) {
            cond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
        }
        // 显示字段集合
        List<String> fields = FastList.newInstance();
        List<String> order = FastList.newInstance();

        try {
            // 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, true);
            EntityListIterator pr_li = delegator.findListIteratorByCondition(pr_dv, cond, null, fields, order, null);
            // 判断该人员是否已经是商家，0:是 1：否
            if (pr_li.getResultsSizeAfterPartialList() == 0) {
                result.put("status", 1);
            } else {
                result.put("status", 0);
            }
            result.put("recordsList", pr_li.getCompleteList());
            pr_li.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 商家修改是否启用状态 add by qianjin 2016/01/27
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editBusinessIsUse(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 商家ID
        String businessId = (String) context.get("businessId");
        // 是否启用状态
        String isUse = (String) context.get("isUse");

        try {
            // 根据ID获取商家记录并修改
            GenericValue party_gv = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", businessId));
            party_gv.setString("statusId", isUse);
            party_gv.store();

            // 判断启用状态，更改账号的启用状态
            String enabled = "Y";
            if ("PARTY_DISABLED".equals(isUse)) {
                enabled = "N";
            }
            GenericValue ul_gv = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", businessId));
            ul_gv.setString("enabled", enabled);
            ul_gv.store();

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 商家审核 add by qianjin 2016.01.28
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessAudit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 参数
        String businessId = (String) context.get("businessId");
        String auditStatus = (String) context.get("auditStatus");
        String auditContent = (String) context.get("auditContent");

        try {
            GenericValue pb_gv = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", businessId));
            pb_gv.setString("auditStatus", auditStatus);
            pb_gv.store();

            String pba_id = delegator.getNextSeqId("PartyBusinessAudit");
            // 新增一条商家审核记录
            GenericValue pba_gv = delegator.makeValue("PartyBusinessAudit",
                    UtilMisc.toMap("partyBusinessAuditId", pba_id));
            pba_gv.setString("partyId", businessId);
            pba_gv.setString("auditContent", auditContent);
            pba_gv.setString("auditStatus", auditStatus);
            pba_gv.create();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 商家拒绝意见查询 add by qianjin 2016.01.28
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessRefuseReason(DispatchContext dctx,
                                                           Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 参数
        String businessId = (String) context.get("businessId");

        try {
            List<GenericValue> pba_list = delegator.findByAnd("PartyBusinessAudit",
                    UtilMisc.toMap("partyId", businessId, "auditStatus", "2"), UtilMisc.toList("-createdStamp"));
            if (UtilValidate.isNotEmpty(pba_list)) {
                GenericValue pba_gv = EntityUtil.getFirst(pba_list);
                result.put("auditContent", pba_gv.getString("auditContent"));
            } else {
                result.put("auditContent", "无内容");
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 商家导入
     *
     * @param request
     * @param response
     */
    public static void businessImport(HttpServletRequest request, HttpServletResponse response) {
        // 返回信息，json格式
        String returnJson = "";
        // 当前用户
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        // Delegator对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        // LocalDispatcher对象
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            // 调用Excel导入方法
            Map rs = dispatcher.runSync("excelImport", UtilMisc.toMap("request", request, "xmlUrl",
                    "src/com/qihua/ofbiz/business/BusinessValidate.xml", "validateCellData", "businessValidateCell"));
            // 获取导入的信息
            returnJson = rs.get("msg").toString();
            // 获取导入的数据list
            List<Map> listDatas = (List<Map>) rs.get("listDatas");

            // 遍历list，进行新增或修改操作
            for (Map record : listDatas) {
                String businessName = (String) record.get("business_name");
                String partyId = (String) record.get("party_id");
                String province = (String) record.get("province");
                String city = (String) record.get("city");
                String county = (String) record.get("county");
                String address = (String) record.get("address");
                String tel = (String) record.get("tel");
                String isUse = (String) record.get("is_use");
                String legalPersonName = (String) record.get("legal_person_name");
                String businessTypeName = (String) record.get("business_type");
                try {
                    // 设置商家参数map
                    Map business_map = FastMap.newInstance();
                    business_map.put("businessName", businessName);
                    business_map.put("partyId", partyId);
                    // 获取省ID
                    List<GenericValue> province_list = delegator.findByAnd("Geo",
                            UtilMisc.toMap("geoName", province, "geoTypeId", "PROVINCE"));
                    business_map.put("province", EntityUtil.getFirst(province_list).getString("geoId"));
                    // 获取市ID
                    List<GenericValue> city_list = delegator.findByAnd("Geo",
                            UtilMisc.toMap("geoName", city, "geoTypeId", "CITY"));
                    business_map.put("city", EntityUtil.getFirst(city_list).getString("geoId"));
                    // 获取区ID
                    List<GenericValue> county_list = delegator.findByAnd("Geo",
                            UtilMisc.toMap("geoName", county, "geoTypeId", "COUNTY"));
                    business_map.put("county", EntityUtil.getFirst(county_list).getString("geoId"));
                    business_map.put("address", address);
                    business_map.put("tel", tel);
                    business_map.put("legalPersonName", legalPersonName);

                    // 获取商品类型id
                    List<GenericValue> businessTypeList = delegator.findByAnd("Enumeration",
                            UtilMisc.toMap("description", businessTypeName, "enumTypeId", "BUSINESS_TYPE"));
                    business_map.put("businessType", EntityUtil.getFirst(businessTypeList).getString("enumId"));

                    if ("是".equals(isUse)) {
                        business_map.put("isUse", "PARTY_ENABLED");
                    } else {
                        business_map.put("isUse", "PARTY_DISABLED");
                    }
                    // 存放userLogin
                    business_map.put("userLogin", userLogin);

                    dispatcher.runSync("businessAdd", business_map);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        } finally {
            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.write(returnJson);
            out.flush();
            out.close();
        }
    }


    public static Map<String, Object> brandAuth(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 参数
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productStoreId = (String) context.get("productStoreId");
        String partyId =EntityUtil.getFirst(delegator.findByAnd("PartyGroup",UtilMisc.toMap("productStoreId",productStoreId))).getString("partyId");


        String productBrandIds = (String) context.get("ids");
        try {
            List<GenericValue> partyBrandList = FastList.newInstance();
            String[] productBrandArrs = productBrandIds.split(",");
            for (String productBrandId : productBrandArrs) {
                GenericValue partyBusinessBrand = delegator.makeValue("PartyBusinessBrand");
				partyBusinessBrand.setNextSeqId();
                partyBusinessBrand.set("partyId", partyId);
                partyBusinessBrand.set("auditStatus", "0");
                partyBusinessBrand.set("isDel", "N");
                partyBusinessBrand.set("isOwner", "N");
                partyBusinessBrand.set("productBrandId", productBrandId);
                partyBrandList.add(partyBusinessBrand);
            }
            delegator.storeAll(partyBrandList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
