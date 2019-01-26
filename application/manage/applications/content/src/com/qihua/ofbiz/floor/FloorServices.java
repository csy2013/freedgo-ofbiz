package com.qihua.ofbiz.floor;


import javolution.util.FastList;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The type Floor services.
 */
public class FloorServices {
    public static final String module = FloorServices.class.getName();
    public static final String resource = "SystemMgrUiLabels";
    public static final String resourceError = "SystemMgrErrorUiLabels";

    /**
     * 楼层列表
     *
     * @param dcx     the dcx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 15:09:24
     */
    public static Map<String, Object> findFloors(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
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

        List<Map> floorList = FastList.newInstance();
        int floorListSize = 0;
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

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("F", "Floor");
        dynamicView.addAlias("F", "floorId");
        dynamicView.addAlias("F", "productCategoryId");
        dynamicView.addAlias("F", "floorName");
        dynamicView.addAlias("F", "imgUrl");
        dynamicView.addAlias("F", "isEnabled");
        dynamicView.addAlias("F", "sequenceNum");
        dynamicView.addMemberEntity("PC", "ProductCategory");
        dynamicView.addAlias("PC", "categoryName");
        dynamicView.addViewLink("F", "PC", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productCategoryId", "productCategoryId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("floorId");
        fieldsToSelect.add("productCategoryId");
        fieldsToSelect.add("floorName");
        fieldsToSelect.add("imgUrl");
        fieldsToSelect.add("isEnabled");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("categoryName");

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, null, null, fieldsToSelect, orderBy, findOpts);

                // attempt to get the full size
                floorListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > floorListSize) {
                    highIndex = floorListSize;
                }
                for (GenericValue floor : pli.getPartialList(lowIndex, viewSize)) {
                    Map resultMap = floor.getAllFields();
                    String siteNames = null;
                    List<GenericValue> floorWebSites = delegator.findByAnd("FloorWebSite", UtilMisc.toMap("floorId", floor.get("floorId")));
                    if (UtilValidate.isNotEmpty(floorWebSites)) {
                        for (GenericValue floorWebSite : floorWebSites) {
                            GenericValue webSite = delegator.findByPrimaryKey("WebSite", UtilMisc.toMap("webSiteId", floorWebSite.get("webSiteId")));
                            if (UtilValidate.isNotEmpty(webSite)) {
                                if (UtilValidate.isEmpty(siteNames)) {
                                    siteNames = webSite.getString("siteName");
                                } else {
                                    siteNames += "、" + webSite.getString("siteName");
                                }
                            }
                        }
                    }
                    resultMap.put("siteNames", siteNames);
                    floorList.add(resultMap);
                }
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            floorListSize = 0;
        }
        result.put("floorList", floorList);
        result.put("floorListSize", floorListSize);
        result.put("highIndex", highIndex);
        result.put("lowIndex", lowIndex);

        return result;

    }

    /**
     * 获取商品一级分类
     *
     * @param delegator the delegator
     * @return the first category
     * @author AlexYao
     * @date 2017 -03-22 15:10:30
     */
    public static List<GenericValue> getFirstCategory(Delegator delegator) {
        List<GenericValue> productCategorys = null;
        try {
            productCategorys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", null, "productCategoryLevel", 1L));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return productCategorys;
    }


    /**
     * 新增楼层
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 15:06:46
     */
    public static Map<String, Object> addFloor(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String productCategoryId = (String) context.get("productCategoryId");
        String floorName = (String) context.get("floorName");
        String imgUrl = (String) context.get("imgUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String webSiteIds = (String) context.get("webSiteIds");
        String isAllWebSite = (String) context.get("isAllWebSite");
        List<GenericValue> floors = null;
        List<EntityCondition> conditionList = FastList.newInstance();
        conditionList.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
        try {
            floors = delegator.findList("Floor", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floors)) {
            result.put("error", "该楼层已存在");
            return result;
        }
        List<GenericValue> tobeStore = FastList.newInstance();
        GenericValue floor = delegator.makeValue("Floor");
        String floorId = delegator.getNextSeqId("Floor");
        floor.set("floorId", floorId);
        floor.set("productCategoryId", productCategoryId);
        floor.set("floorName", floorName);
        floor.set("imgUrl", imgUrl);
        floor.set("isEnabled", isEnabled);
        floor.set("sequenceNum", sequenceNum);
        tobeStore.add(floor);
        if (UtilValidate.areEqual(isAllWebSite, "Y")) {
            List<GenericValue> webSiteList = null;
            try {
                webSiteList = delegator.findByAnd("WebSite", UtilMisc.toMap("isEnabled", "Y"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            for (GenericValue webSite : webSiteList) {
                GenericValue floorWebSite = delegator.makeValue("FloorWebSite");
                String floorWebSiteId = delegator.getNextSeqId("FloorWebSite");
                floorWebSite.set("floorWebSiteId", floorWebSiteId);
                floorWebSite.set("floorId", floorId);
                floorWebSite.set("webSiteId", webSite.get("webSiteId"));
                tobeStore.add(floorWebSite);
            }
        } else {
            String[] webSiteIdList = webSiteIds.split(",");
            for (String webSiteId : webSiteIdList) {
                GenericValue floorWebSite = delegator.makeValue("FloorWebSite");
                String floorWebSiteId = delegator.getNextSeqId("FloorWebSite");
                floorWebSite.set("floorWebSiteId", floorWebSiteId);
                floorWebSite.set("floorId", floorId);
                floorWebSite.set("webSiteId", webSiteId);
                tobeStore.add(floorWebSite);
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorId", floorId);
        return result;
    }

    /**
     * 编辑楼层
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 15:11:25
     */
    public static Map<String, Object> editFloor(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorId = (String) context.get("floorId");
        String productCategoryId = (String) context.get("productCategoryId");
        String floorName = (String) context.get("floorName");
        String imgUrl = (String) context.get("imgUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String webSiteIds = (String) context.get("webSiteIds");
        String isAllWebSite = (String) context.get("isAllWebSite");
        result.put("floorId", floorId);
        if (UtilValidate.isNotEmpty(sequenceNum)) {
            List<GenericValue> floors = null;
            List<EntityCondition> conditionList = FastList.newInstance();
            conditionList.add(EntityCondition.makeCondition("floorId", EntityOperator.NOT_EQUAL, floorId));
            conditionList.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
            try {
                floors = delegator.findList("Floor", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floors)) {
                result.put("error", "该楼层已存在");
                return result;
            }
        }
        GenericValue floor = null;
        try {
            floor = delegator.findByPrimaryKey("Floor", UtilMisc.toMap("floorId", floorId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(floor)) {
            return result;
        }
        List<GenericValue> tobeStore = FastList.newInstance();
        if (UtilValidate.isNotEmpty(productCategoryId)) {
            floor.set("productCategoryId", productCategoryId);
        }
        if (UtilValidate.isNotEmpty(floorName)) {
            floor.set("floorName", floorName);
        }
        if (UtilValidate.isNotEmpty(imgUrl)) {
            floor.set("imgUrl", imgUrl);
        }
        if (UtilValidate.isNotEmpty(isEnabled)) {
            floor.set("isEnabled", isEnabled);
        }
        if (UtilValidate.isNotEmpty(sequenceNum)) {
            floor.set("sequenceNum", sequenceNum);
        }
        tobeStore.add(floor);
        if (UtilValidate.areEqual(isAllWebSite, "Y")) {
            List<GenericValue> webSiteList = null;
            try {
                delegator.removeByAnd("FloorWebSite", UtilMisc.toMap("floorId", floorId));
                webSiteList = delegator.findByAnd("WebSite", UtilMisc.toMap("isEnabled", "Y"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            for (GenericValue webSite : webSiteList) {
                GenericValue floorWebSite = delegator.makeValue("FloorWebSite");
                String floorWebSiteId = delegator.getNextSeqId("FloorWebSite");
                floorWebSite.set("floorWebSiteId", floorWebSiteId);
                floorWebSite.set("floorId", floorId);
                floorWebSite.set("webSiteId", webSite.get("webSiteId"));
                tobeStore.add(floorWebSite);
            }
        } else {
            if (UtilValidate.isNotEmpty(webSiteIds)) {
                try {
                    delegator.removeByAnd("FloorWebSite", UtilMisc.toMap("floorId", floorId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                String[] webSiteIdList = webSiteIds.split(",");
                for (String webSiteId : webSiteIdList) {
                    GenericValue floorWebSite = delegator.makeValue("FloorWebSite");
                    String floorWebSiteId = delegator.getNextSeqId("FloorWebSite");
                    floorWebSite.set("floorWebSiteId", floorWebSiteId);
                    floorWebSite.set("floorId", floorId);
                    floorWebSite.set("webSiteId", webSiteId);
                    tobeStore.add(floorWebSite);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除楼层
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 15:13:53
     */
    public static Map<String, Object> removeFloor(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorIds = (String) context.get("floorIds");
        String[] floorIdList = floorIds.split(",");
        for (String floorId : floorIdList) {
            try {
                delegator.removeByAnd("FloorLabelBanner", UtilMisc.toMap("floorId", floorId));//删除楼层标签广告
                delegator.removeByAnd("FloorLabelBrand", UtilMisc.toMap("floorId", floorId));//删除楼层标签品牌
                delegator.removeByAnd("FloorLabelProduct", UtilMisc.toMap("floorId", floorId));//删除楼层标签商品
                delegator.removeByAnd("FloorLabel", UtilMisc.toMap("floorId", floorId));//删除楼层标签
                delegator.removeByAnd("FloorBanner", UtilMisc.toMap("floorId", floorId));//删除楼层广告
                delegator.removeByAnd("FloorBrand", UtilMisc.toMap("floorId", floorId));//删除楼层品牌
                delegator.removeByAnd("FloorProduct", UtilMisc.toMap("floorId", floorId));//删除楼层商品
                delegator.removeByAnd("FloorWebSite", UtilMisc.toMap("floorId", floorId));//删除楼层站点
                delegator.removeByAnd("Floor", UtilMisc.toMap("floorId", floorId));//删除楼层
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }

        }
        result.put("result", floorIdList.length);
        return result;
    }

    /**
     * 楼层详情
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 15:14:06
     */
    public static Map<String, Object> floorDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorId = (String) context.get("floorId");
        GenericValue floor = null;
        List<GenericValue> floorWebSites = null;
        try {
            floor = delegator.findByPrimaryKey("Floor", UtilMisc.toMap("floorId", floorId));
            floorWebSites = delegator.findByAnd("FloorWebSite", UtilMisc.toMap("floorId", floorId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorId", floor.get("floorId"));
        result.put("productCategoryId", floor.get("productCategoryId"));
        result.put("floorName", floor.get("floorName"));
        result.put("imgUrl", floor.get("imgUrl"));
        result.put("isEnabled", floor.get("isEnabled"));
        result.put("sequenceNum", floor.get("sequenceNum"));
        String webSiteIds = null;
        for (GenericValue floorWebSite : floorWebSites) {
            if (webSiteIds == null) {
                webSiteIds = floorWebSite.getString("webSiteId");
            } else {
                webSiteIds += "," + floorWebSite.getString("webSiteId");
            }
        }
        result.put("webSiteIds", webSiteIds);
        return result;
    }

    /**
     * 楼层标签列表
     *
     * @param dcx     the dcx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 18:28:23
     */
    public static Map<String, Object> findFloorLabels(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String floorId = (String) context.get("floorId");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> floorLabelList = FastList.newInstance();
        int floorLabelListSize = 0;
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

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("FL", "FloorLabel");
        dynamicView.addAlias("FL", "floorLabelId");
        dynamicView.addAlias("FL", "floorLabelName");
        dynamicView.addAlias("FL", "showBanner");
        dynamicView.addAlias("FL", "showBrand");
        dynamicView.addAlias("FL", "isEnabled");
        dynamicView.addAlias("FL", "sequenceNum");
        dynamicView.addMemberEntity("F", "Floor");
        dynamicView.addAlias("F", "floorId");
        dynamicView.addAlias("F", "floorName");
        dynamicView.addViewLink("FL", "F", Boolean.TRUE, ModelKeyMap.makeKeyMapList("floorId", "floorId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("floorLabelId");
        fieldsToSelect.add("floorId");
        fieldsToSelect.add("floorName");
        fieldsToSelect.add("floorLabelName");
        fieldsToSelect.add("showBanner");
        fieldsToSelect.add("showBrand");
        fieldsToSelect.add("isEnabled");
        fieldsToSelect.add("sequenceNum");

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition("floorId", floorId), null, fieldsToSelect, orderBy, findOpts);

                floorLabelList = pli.getPartialList(lowIndex, viewSize);
                // attempt to get the full size
                floorLabelListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > floorLabelListSize) {
                    highIndex = floorLabelListSize;
                }
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            floorLabelListSize = 0;
        }
        result.put("floorLabelList", floorLabelList);
        result.put("floorLabelListSize", floorLabelListSize);
        result.put("highIndex", highIndex);
        result.put("lowIndex", lowIndex);

        return result;

    }

    /**
     * 新增楼层标签
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 18:35:13
     */
    public static Map<String, Object> addFloorLabel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorId = (String) context.get("floorId");
        String floorLabelName = (String) context.get("floorLabelName");
        String showBanner = (String) context.get("showBanner");
        String showBrand = (String) context.get("showBrand");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        GenericValue floorLabel = delegator.makeValue("FloorLabel");
        String floorLabelId = delegator.getNextSeqId("FloorLabel");
        floorLabel.set("floorLabelId", floorLabelId);
        floorLabel.set("floorId", floorId);
        floorLabel.set("floorLabelName", floorLabelName);
        floorLabel.set("showBanner", showBanner);
        floorLabel.set("showBrand", showBrand);
        floorLabel.set("isEnabled", isEnabled);
        floorLabel.set("sequenceNum", sequenceNum);
        tobeStore.add(floorLabel);
        List<GenericValue> labels = null;
        try {
            labels = delegator.findByAnd("FloorLabel", UtilMisc.toMap("floorId", floorId, "sequenceNum", sequenceNum));//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labels)) {
            List<GenericValue> floorLabels = null;
            List<EntityCondition> conditions = FastList.newInstance();
            conditions.add(EntityCondition.makeCondition("floorId", floorId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorLabels = delegator.findList("FloorLabel", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorLabels)) {
                for (GenericValue label : floorLabels) {
                    label.set("sequenceNum", label.getLong("sequenceNum") + 1L);
                    tobeStore.add(label);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelId", floorLabelId);
        return result;
    }

    /**
     * 编辑楼层标签
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 18:38:01
     */
    public static Map<String, Object> editFloorLabel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorId = (String) context.get("floorId");
        String floorLabelId = (String) context.get("floorLabelId");
        String floorLabelName = (String) context.get("floorLabelName");
        String showBanner = (String) context.get("showBanner");
        String showBrand = (String) context.get("showBrand");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        GenericValue floorLabel = null;
        try {
            floorLabel = delegator.findByPrimaryKey("FloorLabel", UtilMisc.toMap("floorLabelId", floorLabelId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(floorLabel)) {
            return result;
        }
        if (UtilValidate.isNotEmpty(floorLabelName)) {
            floorLabel.set("floorLabelName", floorLabelName);
        }
        if (UtilValidate.isNotEmpty(showBanner)) {
            floorLabel.set("showBanner", showBanner);
        }
        if (UtilValidate.isNotEmpty(showBrand)) {
            floorLabel.set("showBrand", showBrand);
        }
        if (UtilValidate.isNotEmpty(isEnabled)) {
            floorLabel.set("isEnabled", isEnabled);
        }
        if (UtilValidate.isNotEmpty(sequenceNum)) {
            floorLabel.set("sequenceNum", sequenceNum);
        }
        tobeStore.add(floorLabel);
        List<GenericValue> labels = null;
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("floorId", floorId));
        conditions.add(EntityCondition.makeCondition("floorLabelId", EntityOperator.NOT_EQUAL, floorLabelId));
        conditions.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
        try {
            labels = delegator.findList("FloorLabel", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labels)) {
            List<GenericValue> floorLabels = null;
            conditions.clear();
            conditions.add(EntityCondition.makeCondition("floorId", floorId));
            conditions.add(EntityCondition.makeCondition("floorLabelId", EntityOperator.NOT_EQUAL, floorLabelId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorLabels = delegator.findList("FloorLabel", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorLabels)) {
                for (GenericValue label : floorLabels) {
                    label.set("sequenceNum", label.getLong("sequenceNum") + 1L);
                    tobeStore.add(label);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelId", floorLabelId);
        return result;
    }

    /**
     * 删除楼层标签
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-22 18:40:48
     */
    public static Map<String, Object> removeFloorLabel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorLabelIds = (String) context.get("floorLabelIds");
        String[] floorLabelIdList = floorLabelIds.split(",");
        for (String floorLabelId : floorLabelIdList) {
            try {
                delegator.removeByAnd("FloorLabelBanner", UtilMisc.toMap("floorLabelId", floorLabelId));//删除楼层标签广告
                delegator.removeByAnd("FloorLabelBrand", UtilMisc.toMap("floorLabelId", floorLabelId));//删除楼层标签品牌
                delegator.removeByAnd("FloorLabelProduct", UtilMisc.toMap("floorLabelId", floorLabelId));//删除楼层标签商品
                delegator.removeByAnd("FloorLabel", UtilMisc.toMap("floorLabelId", floorLabelId));//删除楼层标签
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }

        }
        result.put("result", floorLabelIdList.length);
        return result;
    }

    /**
     * 楼层标签详情
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the generic value
     * @author AlexYao
     * @date 2017 -03-22 18:43:32
     */
    public static Map<String, Object> floorLabelDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorLabelId = (String) context.get("floorLabelId");
        GenericValue floorLabel = null;
        try {
            floorLabel = delegator.findByPrimaryKey("FloorLabel", UtilMisc.toMap("floorLabelId", floorLabelId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelId", floorLabel.get("floorLabelId"));
        result.put("floorId", floorLabel.get("floorId"));
        result.put("floorLabelName", floorLabel.get("floorLabelName"));
        result.put("showBanner", floorLabel.get("showBanner"));
        result.put("showBrand", floorLabel.get("showBrand"));
        result.put("isEnabled", floorLabel.get("isEnabled"));
        result.put("sequenceNum", floorLabel.get("sequenceNum"));
        return result;
    }

    /**
     * 楼层标签商品列表
     *
     * @param dcx     the dcx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:59:38
     */
    public static Map<String, Object> findFloorLabelProducts(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String floorLabelId = (String) context.get("floorLabelId");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<Map> floorLabelProductList = FastList.newInstance();
        int floorLabelProductListSize = 0;
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

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("FLP", "FloorLabelProduct");
        dynamicView.addAlias("FLP", "floorLabelProductId");
        dynamicView.addAlias("FLP", "floorId");
        dynamicView.addAlias("FLP", "floorLabelId");
        dynamicView.addAlias("FLP", "productId");
        dynamicView.addAlias("FLP", "sequenceNum");
        dynamicView.addMemberEntity("P", "Product");
        dynamicView.addAlias("P", "productName");
        dynamicView.addViewLink("FLP", "P", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productId", "productId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("floorLabelProductId");
        fieldsToSelect.add("floorId");
        fieldsToSelect.add("floorLabelId");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("productName");

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition("floorLabelId", floorLabelId), null, fieldsToSelect, orderBy, findOpts);

                // attempt to get the full size
                floorLabelProductListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > floorLabelProductListSize) {
                    highIndex = floorLabelProductListSize;
                }
                for (GenericValue floor : pli.getPartialList(lowIndex, viewSize)) {
                    Map resultMap = floor.getAllFields();
                    String imgUrl = null;
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", floor.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        GenericValue content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", productContents.get(0).get("contentId")));
                        if (UtilValidate.isNotEmpty(content)) {
                            GenericValue dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                            if (UtilValidate.isNotEmpty(dataResource)) {
                                imgUrl = dataResource.getString("objectInfo");
                            }
                        }
                    }
                    resultMap.put("imgUrl", imgUrl);
                    floorLabelProductList.add(resultMap);
                }
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            floorLabelProductListSize = 0;
        }
        result.put("floorLabelProductList", floorLabelProductList);
        result.put("floorLabelProductListSize", floorLabelProductListSize);
        result.put("highIndex", highIndex);
        result.put("lowIndex", lowIndex);

        return result;

    }

    /**
     * 新增楼层标签商品
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:10:35
     */
    public static Map<String, Object> addFloorLabelProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorId = (String) context.get("floorId");
        String floorLabelId = (String) context.get("floorLabelId");
        String productId = (String) context.get("productId");
        Long sequenceNum = (Long) context.get("sequenceNum");
        List<GenericValue> floorLabelProducts = null;
        try {
            floorLabelProducts = delegator.findByAnd("FloorLabelProduct", UtilMisc.toMap("floorLabelId", floorLabelId, "productId", productId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floorLabelProducts)) {
            result.put("error", "该商品已添加");
            return result;
        }
        GenericValue floorLabelProduct = delegator.makeValue("FloorLabelProduct");
        String floorLabelProductId = delegator.getNextSeqId("FloorLabelProduct");
        floorLabelProduct.set("floorLabelProductId", floorLabelProductId);
        floorLabelProduct.set("floorId", floorId);
        floorLabelProduct.set("floorLabelId", floorLabelId);
        floorLabelProduct.set("productId", productId);
        floorLabelProduct.set("sequenceNum", sequenceNum);
        tobeStore.add(floorLabelProduct);
        List<GenericValue> labelProducts = null;
        try {
            labelProducts = delegator.findByAnd("FloorLabelProduct", UtilMisc.toMap("floorLabelId", floorLabelId, "sequenceNum", sequenceNum));//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelProducts)) {
            floorLabelProducts = null;
            List<EntityCondition> conditions = FastList.newInstance();
            conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorLabelProducts = delegator.findList("FloorLabelProduct", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorLabelProducts)) {
                for (GenericValue labelProduct : floorLabelProducts) {
                    labelProduct.set("sequenceNum", labelProduct.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelProduct);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelProductId", floorLabelProductId);
        return result;
    }

    /**
     * 编辑楼层标签商品
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:21
     */
    public static Map<String, Object> editFloorLabelProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorLabelProductId = (String) context.get("floorLabelProductId");
        String floorLabelId = (String) context.get("floorLabelId");
        String productId = (String) context.get("productId");
        Long sequenceNum = (Long) context.get("sequenceNum");
        result.put("floorLabelProductId", floorLabelProductId);
        List<GenericValue> floorLabelProducts = null;
        List<EntityCondition> conditionList = FastList.newInstance();
        conditionList.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
        conditionList.add(EntityCondition.makeCondition("floorLabelProductId", EntityOperator.NOT_EQUAL, floorLabelProductId));
        conditionList.add(EntityCondition.makeCondition("productId", productId));
        try {
            floorLabelProducts = delegator.findList("FloorLabelProduct", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floorLabelProducts)) {
            result.put("error", "该商品已添加");
            return result;
        }
        GenericValue floorLabelProduct = null;
        try {
            floorLabelProduct = delegator.findByPrimaryKey("FloorLabelProduct", UtilMisc.toMap("floorLabelProductId", floorLabelProductId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(floorLabelProduct)) {
            return result;
        }
        floorLabelProduct.set("productId", productId);
        floorLabelProduct.set("sequenceNum", sequenceNum);
        tobeStore.add(floorLabelProduct);
        List<GenericValue> labelProducts = null;
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
        conditions.add(EntityCondition.makeCondition("floorLabelProductId", EntityOperator.NOT_EQUAL, floorLabelProductId));
        conditions.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
        try {
            labelProducts = delegator.findList("FloorLabelProduct", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelProducts)) {
            floorLabelProducts = null;
            conditions.clear();
            conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
            conditions.add(EntityCondition.makeCondition("floorLabelProductId", EntityOperator.NOT_EQUAL, floorLabelProductId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorLabelProducts = delegator.findList("FloorLabelProduct", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorLabelProducts)) {
                for (GenericValue labelProduct : floorLabelProducts) {
                    labelProduct.set("sequenceNum", labelProduct.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelProduct);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除楼层标签商品
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:46
     */
    public static Map<String, Object> removeFloorLabelProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorLabelProductIds = (String) context.get("floorLabelProductIds");
        String[] floorLabelProductIdList = floorLabelProductIds.split(",");
        for (String floorLabelProductId : floorLabelProductIdList) {
            try {
                delegator.removeByAnd("FloorLabelProduct", UtilMisc.toMap("floorLabelProductId", floorLabelProductId));//删除楼层标签商品
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        result.put("result", floorLabelProductIdList.length);
        return result;
    }

    /**
     * 楼层标签商品详情
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:59
     */
    public static Map<String, Object> floorLabelProductDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorLabelProductId = (String) context.get("floorLabelProductId");
        GenericValue floorLabelProduct = null;
        try {
            floorLabelProduct = delegator.findByPrimaryKey("FloorLabelProduct", UtilMisc.toMap("floorLabelProductId", floorLabelProductId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelProductId", floorLabelProduct.get("floorLabelProductId"));
        result.put("productId", floorLabelProduct.get("productId"));
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", floorLabelProduct.get("productId")));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("productName", product.get("productName"));
        List<GenericValue> productContents = null;
        try {
            productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", floorLabelProduct.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(productContents)) {
            GenericValue content = null;
            try {
                content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", productContents.get(0).get("contentId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(content)) {
                GenericValue dataResource = null;
                try {
                    dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(dataResource)) {
                    result.put("imgUrl", dataResource.getString("objectInfo"));
                }
            }
        }
        result.put("sequenceNum", floorLabelProduct.get("sequenceNum"));
        return result;
    }

    /**
     * 楼层标签品牌列表
     *
     * @param dcx     the dcx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:59:38
     */
    public static Map<String, Object> findFloorLabelBrands(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String floorLabelId = (String) context.get("floorLabelId");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<Map> floorLabelBrandList = FastList.newInstance();
        int floorLabelBrandListSize = 0;
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

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("FLB", "FloorLabelBrand");
        dynamicView.addAlias("FLB", "floorLabelBrandId");
        dynamicView.addAlias("FLB", "floorId");
        dynamicView.addAlias("FLB", "floorLabelId");
        dynamicView.addAlias("FLB", "productBrandId");
        dynamicView.addAlias("FLB", "linkUrl");
        dynamicView.addAlias("FLB", "isEnabled");
        dynamicView.addAlias("FLB", "sequenceNum");
        dynamicView.addMemberEntity("PB", "ProductBrand");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addViewLink("FLB", "PB", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("floorLabelBrandId");
        fieldsToSelect.add("floorId");
        fieldsToSelect.add("floorLabelId");
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("linkUrl");
        fieldsToSelect.add("isEnabled");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition("floorLabelId", floorLabelId), null, fieldsToSelect, orderBy, findOpts);

                // attempt to get the full size
                floorLabelBrandListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > floorLabelBrandListSize) {
                    highIndex = floorLabelBrandListSize;
                }
                for (GenericValue floor : pli.getPartialList(lowIndex, viewSize)) {
                    Map resultMap = floor.getAllFields();
                    resultMap.put("imgUrl", "/content/control/getImage?contentId=" + floor.get("contentId"));
                    floorLabelBrandList.add(resultMap);
                }
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            floorLabelBrandListSize = 0;
        }
        result.put("floorLabelBrandList", floorLabelBrandList);
        result.put("floorLabelBrandListSize", floorLabelBrandListSize);
        result.put("highIndex", highIndex);
        result.put("lowIndex", lowIndex);

        return result;

    }

    /**
     * 新增楼层标签品牌
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:10:35
     */
    public static Map<String, Object> addFloorLabelBrand(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorId = (String) context.get("floorId");
        String floorLabelId = (String) context.get("floorLabelId");
        String productBrandId = (String) context.get("productBrandId");
        String linkUrl = (String) context.get("linkUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        List<GenericValue> floorLabelBrands = null;
        try {
            floorLabelBrands = delegator.findByAnd("FloorLabelBrand", UtilMisc.toMap("floorLabelId", floorLabelId, "productBrandId", productBrandId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floorLabelBrands)) {
            result.put("error", "该品牌已添加");
            return result;
        }
        GenericValue floorLabelBrand = delegator.makeValue("FloorLabelBrand");
        String floorLabelBrandId = delegator.getNextSeqId("FloorLabelBrand");
        floorLabelBrand.set("floorLabelBrandId", floorLabelBrandId);
        floorLabelBrand.set("floorId", floorId);
        floorLabelBrand.set("floorLabelId", floorLabelId);
        floorLabelBrand.set("productBrandId", productBrandId);
        floorLabelBrand.set("linkUrl", linkUrl);
        floorLabelBrand.set("isEnabled", isEnabled);
        floorLabelBrand.set("sequenceNum", sequenceNum);
        tobeStore.add(floorLabelBrand);
        List<GenericValue> labelBrands = null;
        try {
            labelBrands = delegator.findByAnd("FloorLabelBrand", UtilMisc.toMap("floorLabelId", floorLabelId, "sequenceNum", sequenceNum));//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelBrands)) {
            floorLabelBrands = null;
            List<EntityCondition> conditions = FastList.newInstance();
            conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorLabelBrands = delegator.findList("FloorLabelBrand", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorLabelBrands)) {
                for (GenericValue labelBrand : floorLabelBrands) {
                    labelBrand.set("sequenceNum", labelBrand.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelBrand);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelBrandId", floorLabelBrandId);
        return result;
    }

    /**
     * 编辑楼层标签品牌
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:21
     */
    public static Map<String, Object> editFloorLabelBrand(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorLabelBrandId = (String) context.get("floorLabelBrandId");
        String floorLabelId = (String) context.get("floorLabelId");
        String productBrandId = (String) context.get("productBrandId");
        String linkUrl = (String) context.get("linkUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        result.put("floorLabelBrandId", floorLabelBrandId);
        List<GenericValue> floorLabelBrands = null;
        List<EntityCondition> conditionList = FastList.newInstance();
        conditionList.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
        conditionList.add(EntityCondition.makeCondition("floorLabelBrandId", EntityOperator.NOT_EQUAL, floorLabelBrandId));
        conditionList.add(EntityCondition.makeCondition("productBrandId", productBrandId));
        try {
            floorLabelBrands = delegator.findList("FloorLabelBrand", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floorLabelBrands)) {
            result.put("error", "该品牌已添加");
            return result;
        }
        GenericValue floorLabelBrand = null;
        try {
            floorLabelBrand = delegator.findByPrimaryKey("FloorLabelBrand", UtilMisc.toMap("floorLabelBrandId", floorLabelBrandId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(floorLabelBrand)) {
            return result;
        }
        if (UtilValidate.isNotEmpty(productBrandId)) {
            floorLabelBrand.set("productBrandId", productBrandId);
        }
        if (UtilValidate.isNotEmpty(linkUrl)) {
            floorLabelBrand.set("linkUrl", linkUrl);
        }
        if (UtilValidate.isNotEmpty(isEnabled)) {
            floorLabelBrand.set("isEnabled", isEnabled);
        }
        if (UtilValidate.isNotEmpty(sequenceNum)) {
            floorLabelBrand.set("sequenceNum", sequenceNum);
        }
        tobeStore.add(floorLabelBrand);
        List<GenericValue> labelBrands = null;
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
        conditions.add(EntityCondition.makeCondition("floorLabelBrandId", EntityOperator.NOT_EQUAL, floorLabelBrandId));
        conditions.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
        try {
            labelBrands = delegator.findList("FloorLabelBrand", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelBrands)) {
            floorLabelBrands = null;
            conditions.clear();
            conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
            conditions.add(EntityCondition.makeCondition("floorLabelBrandId", EntityOperator.NOT_EQUAL, floorLabelBrandId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorLabelBrands = delegator.findList("FloorLabelBrand", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorLabelBrands)) {
                for (GenericValue labelBrand : floorLabelBrands) {
                    labelBrand.set("sequenceNum", labelBrand.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelBrand);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除楼层标签品牌
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:46
     */
    public static Map<String, Object> removeFloorLabelBrand(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorLabelBrandIds = (String) context.get("floorLabelBrandIds");
        String[] floorLabelBrandIdList = floorLabelBrandIds.split(",");
        for (String floorLabelBrandId : floorLabelBrandIdList) {
            try {
                delegator.removeByAnd("FloorLabelBrand", UtilMisc.toMap("floorLabelBrandId", floorLabelBrandId));//删除楼层标签商品
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        result.put("result", floorLabelBrandIdList.length);
        return result;
    }

    /**
     * 楼层标签品牌详情
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:59
     */
    public static Map<String, Object> floorLabelBrandDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorLabelBrandId = (String) context.get("floorLabelBrandId");
        GenericValue floorLabelBrand = null;
        try {
            floorLabelBrand = delegator.findByPrimaryKey("FloorLabelBrand", UtilMisc.toMap("floorLabelBrandId", floorLabelBrandId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelBrandId", floorLabelBrand.get("floorLabelBrandId"));
        result.put("productBrandId", floorLabelBrand.get("productBrandId"));
        result.put("linkUrl", floorLabelBrand.get("linkUrl"));
        result.put("isEnabled", floorLabelBrand.get("isEnabled"));
        result.put("sequenceNum", floorLabelBrand.get("sequenceNum"));
        return result;
    }

    /**
     * 楼层标签广告列表
     *
     * @param dcx     the dcx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:44:43
     */
    public static Map<String, Object> findFloorLabelBanners(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String floorLabelId = (String) context.get("floorLabelId");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> floorLabelBannerList = FastList.newInstance();
        int floorLabelBannerListSize = 0;
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

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("FLB", "FloorLabelBanner");
        dynamicView.addAlias("FLB", "floorLabelBannerId");
        dynamicView.addAlias("FLB", "floorId");
        dynamicView.addAlias("FLB", "floorLabelId");
        dynamicView.addAlias("FLB", "bannerName");
        dynamicView.addAlias("FLB", "imgUrl");
        dynamicView.addAlias("FLB", "linkUrl");
        dynamicView.addAlias("FLB", "isEnabled");
        dynamicView.addAlias("FLB", "sequenceNum");
        dynamicView.addAlias("FLB", "description");

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("floorLabelBannerId");
        fieldsToSelect.add("floorId");
        fieldsToSelect.add("floorLabelId");
        fieldsToSelect.add("bannerName");
        fieldsToSelect.add("imgUrl");
        fieldsToSelect.add("linkUrl");
        fieldsToSelect.add("isEnabled");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("description");

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition("floorLabelId", floorLabelId), null, fieldsToSelect, orderBy, findOpts);

                floorLabelBannerList = pli.getPartialList(lowIndex, viewSize);
                // attempt to get the full size
                floorLabelBannerListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > floorLabelBannerListSize) {
                    highIndex = floorLabelBannerListSize;
                }
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            floorLabelBannerListSize = 0;
        }
        result.put("floorLabelBannerList", floorLabelBannerList);
        result.put("floorLabelBannerListSize", floorLabelBannerListSize);
        result.put("highIndex", highIndex);
        result.put("lowIndex", lowIndex);

        return result;

    }

    /**
     * 新增楼层标签广告
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:45:12
     */
    public static Map<String, Object> addFloorLabelBanner(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorId = (String) context.get("floorId");
        String floorLabelId = (String) context.get("floorLabelId");
        String bannerName = (String) context.get("bannerName");
        String imgUrl = (String) context.get("imgUrl");
        String linkUrl = (String) context.get("linkUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String description = (String) context.get("description");
        
        String firstLinkType = (String) context.get("firstLinkType");
        String linkId = (String) context.get("linkId");
        String linkName = (String) context.get("linkName");
        
        GenericValue floorLabelBanner = delegator.makeValue("FloorLabelBanner");
        String floorLabelBannerId = delegator.getNextSeqId("FloorLabelBanner");
        floorLabelBanner.set("floorLabelBannerId", floorLabelBannerId);
        floorLabelBanner.set("floorId", floorId);
        floorLabelBanner.set("floorLabelId", floorLabelId);
        floorLabelBanner.set("bannerName", bannerName);
        floorLabelBanner.set("imgUrl", imgUrl);
        floorLabelBanner.set("linkUrl", linkUrl);
        floorLabelBanner.set("isEnabled", isEnabled);
        floorLabelBanner.set("sequenceNum", sequenceNum);
        
        if (UtilValidate.isNotEmpty(firstLinkType)) {
        	floorLabelBanner.set("firstLinkType", firstLinkType);
        }
        if (UtilValidate.isNotEmpty(linkId)) {
        	floorLabelBanner.set("linkId", linkId);
        }
        if (UtilValidate.isNotEmpty(linkName)) {
        	floorLabelBanner.set("linkName", linkName);
        }
        
        if (UtilValidate.isNotEmpty(description)) {
            floorLabelBanner.set("description", description);
        }
        tobeStore.add(floorLabelBanner);
        List<GenericValue> labelBanners = null;
        try {
            labelBanners = delegator.findByAnd("FloorLabelBanner", UtilMisc.toMap("floorLabelId", floorLabelId, "sequenceNum", sequenceNum));//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelBanners)) {
            List<GenericValue> floorLabelBanners = null;
            List<EntityCondition> conditions = FastList.newInstance();
            conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorLabelBanners = delegator.findList("FloorLabelBanner", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorLabelBanners)) {
                for (GenericValue labelBanner : floorLabelBanners) {
                    labelBanner.set("sequenceNum", labelBanner.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelBanner);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelBannerId", floorLabelBannerId);
        return result;
    }

    /**
     * 编辑楼层标签广告
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:46:29
     */
    public static Map<String, Object> editFloorLabelBanner(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorLabelBannerId = (String) context.get("floorLabelBannerId");
        String floorLabelId = (String) context.get("floorLabelId");
        String bannerName = (String) context.get("bannerName");
        String imgUrl = (String) context.get("imgUrl");
        String linkUrl = (String) context.get("linkUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String description = (String) context.get("description");
        
        String firstLinkType = (String) context.get("firstLinkType");
        String linkId = (String) context.get("linkId");
        String linkName = (String) context.get("linkName");
        
        result.put("floorLabelBannerId", floorLabelBannerId);
        GenericValue floorLabelBanner = null;
        try {
            floorLabelBanner = delegator.findByPrimaryKey("FloorLabelBanner", UtilMisc.toMap("floorLabelBannerId", floorLabelBannerId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(floorLabelBanner)) {
            return result;
        }
        if (UtilValidate.isNotEmpty(bannerName)) {
            floorLabelBanner.set("bannerName", bannerName);
        }
        if (UtilValidate.isNotEmpty(imgUrl)) {
            floorLabelBanner.set("imgUrl", imgUrl);
        }
        if (UtilValidate.isNotEmpty(linkUrl)) {
            floorLabelBanner.set("linkUrl", linkUrl);
        }
        if (UtilValidate.isNotEmpty(isEnabled)) {
            floorLabelBanner.set("isEnabled", isEnabled);
        }
        if (UtilValidate.isNotEmpty(sequenceNum)) {
            floorLabelBanner.set("sequenceNum", sequenceNum);
        }
        if (UtilValidate.isNotEmpty(description)) {
            floorLabelBanner.set("description", description);
        }
        
        if (UtilValidate.isNotEmpty(firstLinkType)) {
        	floorLabelBanner.set("firstLinkType", firstLinkType);
        }
        if (UtilValidate.isNotEmpty(linkId)) {
        	floorLabelBanner.set("linkId", linkId);
        }
        if (UtilValidate.isNotEmpty(linkName)) {
        	floorLabelBanner.set("linkName", linkName);
        }
        
        tobeStore.add(floorLabelBanner);
        List<GenericValue> labelBanners = null;
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
        conditions.add(EntityCondition.makeCondition("floorLabelBannerId", EntityOperator.NOT_EQUAL, floorLabelBannerId));
        conditions.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
        try {
            labelBanners = delegator.findList("FloorLabelBanner", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelBanners)) {
            List<GenericValue> floorLabelBanners = null;
            conditions.clear();
            conditions.add(EntityCondition.makeCondition("floorLabelId", floorLabelId));
            conditions.add(EntityCondition.makeCondition("floorLabelBannerId", EntityOperator.NOT_EQUAL, floorLabelBannerId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorLabelBanners = delegator.findList("FloorLabelBanner", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorLabelBanners)) {
                for (GenericValue labelBanner : floorLabelBanners) {
                    labelBanner.set("sequenceNum", labelBanner.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelBanner);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除楼层标签品牌
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:46:50
     */
    public static Map<String, Object> removeFloorLabelBanner(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorLabelBannerIds = (String) context.get("floorLabelBannerIds");
        String[] floorLabelBannerIdList = floorLabelBannerIds.split(",");
        for (String floorLabelBannerId : floorLabelBannerIdList) {
            try {
                delegator.removeByAnd("FloorLabelBanner", UtilMisc.toMap("floorLabelBannerId", floorLabelBannerId));//删除楼层标签商品
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        result.put("result", floorLabelBannerIdList.length);
        return result;
    }

    /**
     * 楼层标签广告详情
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:47:05
     */
    public static Map<String, Object> floorLabelBannerDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorLabelBannerId = (String) context.get("floorLabelBannerId");
        GenericValue floorLabelBanner = null;
        try {
            floorLabelBanner = delegator.findByPrimaryKey("FloorLabelBanner", UtilMisc.toMap("floorLabelBannerId", floorLabelBannerId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorLabelBannerId", floorLabelBanner.get("floorLabelBannerId"));
        result.put("bannerName", floorLabelBanner.get("bannerName"));
        result.put("imgUrl", floorLabelBanner.get("imgUrl"));
        result.put("linkUrl", floorLabelBanner.get("linkUrl"));
        result.put("isEnabled", floorLabelBanner.get("isEnabled"));
        result.put("sequenceNum", floorLabelBanner.get("sequenceNum"));
        result.put("description", floorLabelBanner.get("description"));
        
        result.put("firstLinkType", floorLabelBanner.get("firstLinkType"));
        result.put("linkId", floorLabelBanner.get("linkId"));
        result.put("linkName", floorLabelBanner.get("linkName"));
        
        return result;
    }

    /**
     * 楼层商品列表
     *
     * @param dcx     the dcx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:59:38
     */
    public static Map<String, Object> findFloorProducts(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String floorId = (String) context.get("floorId");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<Map> floorProductList = FastList.newInstance();
        int floorProductListSize = 0;
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

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("FLP", "FloorProduct");
        dynamicView.addAlias("FLP", "floorProductId");
        dynamicView.addAlias("FLP", "floorId");
        dynamicView.addAlias("FLP", "productId");
        dynamicView.addAlias("FLP", "sequenceNum");
        dynamicView.addMemberEntity("P", "Product");
        dynamicView.addAlias("P", "productName");
        dynamicView.addViewLink("FLP", "P", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productId", "productId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("floorProductId");
        fieldsToSelect.add("floorId");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("productName");

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition("floorId", floorId), null, fieldsToSelect, orderBy, findOpts);

                // attempt to get the full size
                floorProductListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > floorProductListSize) {
                    highIndex = floorProductListSize;
                }
                for (GenericValue floor : pli.getPartialList(lowIndex, viewSize)) {
                    Map resultMap = floor.getAllFields();
                    String imgUrl = null;
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", floor.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        GenericValue content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", productContents.get(0).get("contentId")));
                        if (UtilValidate.isNotEmpty(content)) {
                            GenericValue dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                            if (UtilValidate.isNotEmpty(dataResource)) {
                                imgUrl = dataResource.getString("objectInfo");
                            }
                        }
                    }
                    resultMap.put("imgUrl", imgUrl);
                    floorProductList.add(resultMap);
                }
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            floorProductListSize = 0;
        }
        result.put("floorProductList", floorProductList);
        result.put("floorProductListSize", floorProductListSize);
        result.put("highIndex", highIndex);
        result.put("lowIndex", lowIndex);

        return result;

    }

    /**
     * 新增楼层商品
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:10:35
     */
    public static Map<String, Object> addFloorProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorId = (String) context.get("floorId");
        String productId = (String) context.get("productId");
        Long sequenceNum = (Long) context.get("sequenceNum");
        List<GenericValue> floorProducts = null;
        try {
            floorProducts = delegator.findByAnd("FloorProduct", UtilMisc.toMap("floorId", floorId, "productId", productId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floorProducts)) {
            result.put("error", "该商品已添加");
            return result;
        }
        GenericValue floorProduct = delegator.makeValue("FloorProduct");
        String floorProductId = delegator.getNextSeqId("FloorProduct");
        floorProduct.set("floorProductId", floorProductId);
        floorProduct.set("floorId", floorId);
        floorProduct.set("productId", productId);
        floorProduct.set("sequenceNum", sequenceNum);
        tobeStore.add(floorProduct);
        List<GenericValue> labelProducts = null;
        try {
            labelProducts = delegator.findByAnd("FloorProduct", UtilMisc.toMap("floorId", floorId, "sequenceNum", sequenceNum));//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelProducts)) {
            floorProducts = null;
            List<EntityCondition> conditions = FastList.newInstance();
            conditions.add(EntityCondition.makeCondition("floorId", floorId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorProducts = delegator.findList("FloorProduct", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorProducts)) {
                for (GenericValue labelProduct : floorProducts) {
                    labelProduct.set("sequenceNum", labelProduct.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelProduct);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorProductId", floorProductId);
        return result;
    }

    /**
     * 编辑楼层商品
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:21
     */
    public static Map<String, Object> editFloorProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorProductId = (String) context.get("floorProductId");
        String floorId = (String) context.get("floorId");
        String productId = (String) context.get("productId");
        Long sequenceNum = (Long) context.get("sequenceNum");
        result.put("floorProductId", floorProductId);
        List<GenericValue> floorProducts = null;
        List<EntityCondition> conditionList = FastList.newInstance();
        conditionList.add(EntityCondition.makeCondition("floorId", floorId));
        conditionList.add(EntityCondition.makeCondition("floorProductId", EntityOperator.NOT_EQUAL, floorProductId));
        conditionList.add(EntityCondition.makeCondition("productId", productId));
        try {
            floorProducts = delegator.findList("FloorProduct", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floorProducts)) {
            result.put("error", "该商品已添加");
            return result;
        }
        GenericValue floorProduct = null;
        try {
            floorProduct = delegator.findByPrimaryKey("FloorProduct", UtilMisc.toMap("floorProductId", floorProductId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(floorProduct)) {
            return result;
        }
        floorProduct.set("productId", productId);
        floorProduct.set("sequenceNum", sequenceNum);
        tobeStore.add(floorProduct);
        List<GenericValue> labelProducts = null;
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("floorId", floorId));
        conditions.add(EntityCondition.makeCondition("floorProductId", EntityOperator.NOT_EQUAL, floorProductId));
        conditions.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
        try {
            labelProducts = delegator.findList("FloorProduct", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelProducts)) {
            floorProducts = null;
            conditions.clear();
            conditions.add(EntityCondition.makeCondition("floorId", floorId));
            conditions.add(EntityCondition.makeCondition("floorProductId", EntityOperator.NOT_EQUAL, floorProductId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorProducts = delegator.findList("FloorProduct", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorProducts)) {
                for (GenericValue labelProduct : floorProducts) {
                    labelProduct.set("sequenceNum", labelProduct.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelProduct);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除楼层商品
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:46
     */
    public static Map<String, Object> removeFloorProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorProductIds = (String) context.get("floorProductIds");
        String[] floorProductIdList = floorProductIds.split(",");
        for (String floorProductId : floorProductIdList) {
            try {
                delegator.removeByAnd("FloorProduct", UtilMisc.toMap("floorProductId", floorProductId));//删除楼层商品
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        result.put("result", floorProductIdList.length);
        return result;
    }

    /**
     * 楼层商品详情
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:59
     */
    public static Map<String, Object> floorProductDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorProductId = (String) context.get("floorProductId");
        GenericValue floorProduct = null;
        try {
            floorProduct = delegator.findByPrimaryKey("FloorProduct", UtilMisc.toMap("floorProductId", floorProductId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorProductId", floorProduct.get("floorProductId"));
        result.put("productId", floorProduct.get("productId"));
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", floorProduct.get("productId")));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("productName", product.get("productName"));
        List<GenericValue> productContents = null;
        try {
            productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", floorProduct.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(productContents)) {
            GenericValue content = null;
            try {
                content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", productContents.get(0).get("contentId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(content)) {
                GenericValue dataResource = null;
                try {
                    dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(dataResource)) {
                    result.put("imgUrl", dataResource.getString("objectInfo"));
                }
            }
        }
        result.put("sequenceNum", floorProduct.get("sequenceNum"));
        return result;
    }

    /**
     * 楼层品牌列表
     *
     * @param dcx     the dcx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:59:38
     */
    public static Map<String, Object> findFloorBrands(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String floorId = (String) context.get("floorId");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<Map> floorBrandList = FastList.newInstance();
        int floorBrandListSize = 0;
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

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("FLB", "FloorBrand");
        dynamicView.addAlias("FLB", "floorBrandId");
        dynamicView.addAlias("FLB", "floorId");
        dynamicView.addAlias("FLB", "productBrandId");
        dynamicView.addAlias("FLB", "linkUrl");
        dynamicView.addAlias("FLB", "isEnabled");
        dynamicView.addAlias("FLB", "sequenceNum");
        dynamicView.addMemberEntity("PB", "ProductBrand");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addViewLink("FLB", "PB", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("floorBrandId");
        fieldsToSelect.add("floorId");
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("linkUrl");
        fieldsToSelect.add("isEnabled");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition("floorId", floorId), null, fieldsToSelect, orderBy, findOpts);

                // attempt to get the full size
                floorBrandListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > floorBrandListSize) {
                    highIndex = floorBrandListSize;
                }
                for (GenericValue floor : pli.getPartialList(lowIndex, viewSize)) {
                    Map resultMap = floor.getAllFields();
                    resultMap.put("imgUrl", "/content/control/getImage?contentId=" + floor.get("contentId"));
                    floorBrandList.add(resultMap);
                }
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            floorBrandListSize = 0;
        }
        result.put("floorBrandList", floorBrandList);
        result.put("floorBrandListSize", floorBrandListSize);
        result.put("highIndex", highIndex);
        result.put("lowIndex", lowIndex);

        return result;

    }

    /**
     * 新增楼层品牌
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:10:35
     */
    public static Map<String, Object> addFloorBrand(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorId = (String) context.get("floorId");
        String productBrandId = (String) context.get("productBrandId");
        String linkUrl = (String) context.get("linkUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        List<GenericValue> floorBrands = null;
        try {
            floorBrands = delegator.findByAnd("FloorBrand", UtilMisc.toMap("floorId", floorId, "productBrandId", productBrandId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floorBrands)) {
            result.put("error", "该品牌已添加");
            return result;
        }
        GenericValue floorBrand = delegator.makeValue("FloorBrand");
        String floorBrandId = delegator.getNextSeqId("FloorBrand");
        floorBrand.set("floorBrandId", floorBrandId);
        floorBrand.set("floorId", floorId);
        floorBrand.set("productBrandId", productBrandId);
        floorBrand.set("linkUrl", linkUrl);
        floorBrand.set("isEnabled", isEnabled);
        floorBrand.set("sequenceNum", sequenceNum);
        tobeStore.add(floorBrand);
        List<GenericValue> labelBrands = null;
        try {
            labelBrands = delegator.findByAnd("FloorBrand", UtilMisc.toMap("floorId", floorId, "sequenceNum", sequenceNum));//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelBrands)) {
            floorBrands = null;
            List<EntityCondition> conditions = FastList.newInstance();
            conditions.add(EntityCondition.makeCondition("floorId", floorId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorBrands = delegator.findList("FloorBrand", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorBrands)) {
                for (GenericValue labelBrand : floorBrands) {
                    labelBrand.set("sequenceNum", labelBrand.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelBrand);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorBrandId", floorBrandId);
        return result;
    }

    /**
     * 编辑楼层品牌
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:21
     */
    public static Map<String, Object> editFloorBrand(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorBrandId = (String) context.get("floorBrandId");
        String floorId = (String) context.get("floorId");
        String productBrandId = (String) context.get("productBrandId");
        String linkUrl = (String) context.get("linkUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        result.put("floorBrandId", floorBrandId);
        List<GenericValue> floorBrands = null;
        List<EntityCondition> conditionList = FastList.newInstance();
        conditionList.add(EntityCondition.makeCondition("floorId", floorId));
        conditionList.add(EntityCondition.makeCondition("floorBrandId", EntityOperator.NOT_EQUAL, floorBrandId));
        conditionList.add(EntityCondition.makeCondition("productBrandId", productBrandId));
        try {
            floorBrands = delegator.findList("FloorBrand", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(floorBrands)) {
            result.put("error", "该品牌已添加");
            return result;
        }
        GenericValue floorBrand = null;
        try {
            floorBrand = delegator.findByPrimaryKey("FloorBrand", UtilMisc.toMap("floorBrandId", floorBrandId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(floorBrand)) {
            return result;
        }
        if (UtilValidate.isNotEmpty(productBrandId)) {
            floorBrand.set("productBrandId", productBrandId);
        }
        if (UtilValidate.isNotEmpty(linkUrl)) {
            floorBrand.set("linkUrl", linkUrl);
        }
        if (UtilValidate.isNotEmpty(isEnabled)) {
            floorBrand.set("isEnabled", isEnabled);
        }
        if (UtilValidate.isNotEmpty(sequenceNum)) {
            floorBrand.set("sequenceNum", sequenceNum);
        }
        tobeStore.add(floorBrand);
        List<GenericValue> labelBrands = null;
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("floorId", floorId));
        conditions.add(EntityCondition.makeCondition("floorBrandId", EntityOperator.NOT_EQUAL, floorBrandId));
        conditions.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
        try {
            labelBrands = delegator.findList("FloorBrand", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelBrands)) {
            floorBrands = null;
            conditions.clear();
            conditions.add(EntityCondition.makeCondition("floorId", floorId));
            conditions.add(EntityCondition.makeCondition("floorBrandId", EntityOperator.NOT_EQUAL, floorBrandId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorBrands = delegator.findList("FloorBrand", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorBrands)) {
                for (GenericValue labelBrand : floorBrands) {
                    labelBrand.set("sequenceNum", labelBrand.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelBrand);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除楼层品牌
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:46
     */
    public static Map<String, Object> removeFloorBrand(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorBrandIds = (String) context.get("floorBrandIds");
        String[] floorBrandIdList = floorBrandIds.split(",");
        for (String floorBrandId : floorBrandIdList) {
            try {
                delegator.removeByAnd("FloorBrand", UtilMisc.toMap("floorBrandId", floorBrandId));//删除楼层商品
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        result.put("result", floorBrandIdList.length);
        return result;
    }

    /**
     * 楼层品牌详情
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-23 14:11:59
     */
    public static Map<String, Object> floorBrandDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorBrandId = (String) context.get("floorBrandId");
        GenericValue floorBrand = null;
        try {
            floorBrand = delegator.findByPrimaryKey("FloorBrand", UtilMisc.toMap("floorBrandId", floorBrandId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorBrandId", floorBrand.get("floorBrandId"));
        result.put("productBrandId", floorBrand.get("productBrandId"));
        result.put("linkUrl", floorBrand.get("linkUrl"));
        result.put("isEnabled", floorBrand.get("isEnabled"));
        result.put("sequenceNum", floorBrand.get("sequenceNum"));
        return result;
    }

    /**
     * 楼层广告列表
     *
     * @param dcx     the dcx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:44:43
     */
    public static Map<String, Object> findFloorBanners(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String floorId = (String) context.get("floorId");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> floorBannerList = FastList.newInstance();
        int floorBannerListSize = 0;
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

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        }

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("FLB", "FloorBanner");
        dynamicView.addAlias("FLB", "floorBannerId");
        dynamicView.addAlias("FLB", "floorId");
        dynamicView.addAlias("FLB", "bannerName");
        dynamicView.addAlias("FLB", "imgUrl");
        dynamicView.addAlias("FLB", "linkUrl");
        dynamicView.addAlias("FLB", "isEnabled");
        dynamicView.addAlias("FLB", "sequenceNum");
        dynamicView.addAlias("FLB", "description");

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("floorBannerId");
        fieldsToSelect.add("floorId");
        fieldsToSelect.add("bannerName");
        fieldsToSelect.add("imgUrl");
        fieldsToSelect.add("linkUrl");
        fieldsToSelect.add("isEnabled");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("description");

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition("floorId", floorId), null, fieldsToSelect, orderBy, findOpts);

                floorBannerList = pli.getPartialList(lowIndex, viewSize);
                // attempt to get the full size
                floorBannerListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > floorBannerListSize) {
                    highIndex = floorBannerListSize;
                }
                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            floorBannerListSize = 0;
        }
        result.put("floorBannerList", floorBannerList);
        result.put("floorBannerListSize", floorBannerListSize);
        result.put("highIndex", highIndex);
        result.put("lowIndex", lowIndex);

        return result;

    }

    /**
     * 新增楼层广告
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:45:12
     */
    public static Map<String, Object> addFloorBanner(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorId = (String) context.get("floorId");
        String bannerName = (String) context.get("bannerName");
        String imgUrl = (String) context.get("imgUrl");
        String linkUrl = (String) context.get("linkUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String description = (String) context.get("description");
        

        String firstLinkType = (String) context.get("firstLinkType");
        String linkId = (String) context.get("linkId");
        String linkName = (String) context.get("linkName");
        
        GenericValue floorBanner = delegator.makeValue("FloorBanner");
        String floorBannerId = delegator.getNextSeqId("FloorBanner");
        floorBanner.set("floorBannerId", floorBannerId);
        floorBanner.set("floorId", floorId);
        floorBanner.set("bannerName", bannerName);
        floorBanner.set("imgUrl", imgUrl);
        floorBanner.set("linkUrl", linkUrl);
        floorBanner.set("isEnabled", isEnabled);
        floorBanner.set("sequenceNum", sequenceNum);
        
        if (UtilValidate.isNotEmpty(firstLinkType)) {
            floorBanner.set("firstLinkType", firstLinkType);
        }
        if (UtilValidate.isNotEmpty(linkId)) {
            floorBanner.set("linkId", linkId);
        }
        if (UtilValidate.isNotEmpty(linkName)) {
            floorBanner.set("linkName", linkName);
        }
        
        if (UtilValidate.isNotEmpty(description)) {
            floorBanner.set("description", description);
        }
        tobeStore.add(floorBanner);
        List<GenericValue> labelBanners = null;
        try {
            labelBanners = delegator.findByAnd("FloorBanner", UtilMisc.toMap("floorId", floorId, "sequenceNum", sequenceNum));//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelBanners)) {
            List<GenericValue> floorBanners = null;
            List<EntityCondition> conditions = FastList.newInstance();
            conditions.add(EntityCondition.makeCondition("floorId", floorId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorBanners = delegator.findList("FloorBanner", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorBanners)) {
                for (GenericValue labelBanner : floorBanners) {
                    labelBanner.set("sequenceNum", labelBanner.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelBanner);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorBannerId", floorBannerId);
        return result;
    }

    /**
     * 编辑楼层广告
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:46:29
     */
    public static Map<String, Object> editFloorBanner(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> tobeStore = FastList.newInstance();
        //参数
        String floorBannerId = (String) context.get("floorBannerId");
        String floorId = (String) context.get("floorId");
        String bannerName = (String) context.get("bannerName");
        String imgUrl = (String) context.get("imgUrl");
        String linkUrl = (String) context.get("linkUrl");
        String isEnabled = (String) context.get("isEnabled");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String description = (String) context.get("description");
        
        
        String firstLinkType = (String) context.get("firstLinkType");
        String linkId = (String) context.get("linkId");
        String linkName = (String) context.get("linkName");
        
        result.put("floorBannerId", floorBannerId);
        GenericValue floorBanner = null;
        try {
            floorBanner = delegator.findByPrimaryKey("FloorBanner", UtilMisc.toMap("floorBannerId", floorBannerId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(floorBanner)) {
            return result;
        }
        if (UtilValidate.isNotEmpty(bannerName)) {
            floorBanner.set("bannerName", bannerName);
        }
        if (UtilValidate.isNotEmpty(imgUrl)) {
            floorBanner.set("imgUrl", imgUrl);
        }
        if (UtilValidate.isNotEmpty(linkUrl)) {
            floorBanner.set("linkUrl", linkUrl);
        }
        if (UtilValidate.isNotEmpty(isEnabled)) {
            floorBanner.set("isEnabled", isEnabled);
        }
        if (UtilValidate.isNotEmpty(sequenceNum)) {
            floorBanner.set("sequenceNum", sequenceNum);
        }
        if (UtilValidate.isNotEmpty(description)) {
            floorBanner.set("description", description);
        }
        
        if (UtilValidate.isNotEmpty(firstLinkType)) {
            floorBanner.set("firstLinkType", firstLinkType);
        }
        if (UtilValidate.isNotEmpty(linkId)) {
            floorBanner.set("linkId", linkId);
        }
        if (UtilValidate.isNotEmpty(linkName)) {
            floorBanner.set("linkName", linkName);
        }
        tobeStore.add(floorBanner);
        List<GenericValue> labelBanners = null;
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("floorId", floorId));
        conditions.add(EntityCondition.makeCondition("floorBannerId", EntityOperator.NOT_EQUAL, floorBannerId));
        conditions.add(EntityCondition.makeCondition("sequenceNum", sequenceNum));
        try {
            labelBanners = delegator.findList("FloorBanner", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);//相同序号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(labelBanners)) {
            List<GenericValue> floorBanners = null;
            conditions.clear();
            conditions.add(EntityCondition.makeCondition("floorId", floorId));
            conditions.add(EntityCondition.makeCondition("floorBannerId", EntityOperator.NOT_EQUAL, floorBannerId));
            conditions.add(EntityCondition.makeCondition("sequenceNum", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceNum));
            try {
                floorBanners = delegator.findList("FloorBanner", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(floorBanners)) {
                for (GenericValue labelBanner : floorBanners) {
                    labelBanner.set("sequenceNum", labelBanner.getLong("sequenceNum") + 1L);
                    tobeStore.add(labelBanner);
                }
            }
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除楼层品牌
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:46:50
     */
    public static Map<String, Object> removeFloorBanner(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorBannerIds = (String) context.get("floorBannerIds");
        String[] floorBannerIdList = floorBannerIds.split(",");
        for (String floorBannerId : floorBannerIdList) {
            try {
                delegator.removeByAnd("FloorBanner", UtilMisc.toMap("floorBannerId", floorBannerId));//删除楼层商品
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        result.put("result", floorBannerIdList.length);
        return result;
    }

    /**
     * 楼层广告详情
     *
     * @param dctx    the dctx
     * @param context the context
     * @return the map
     * @author AlexYao
     * @date 2017 -03-24 10:47:05
     */
    public static Map<String, Object> floorBannerDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String floorBannerId = (String) context.get("floorBannerId");
        GenericValue floorBanner = null;
        try {
            floorBanner = delegator.findByPrimaryKey("FloorBanner", UtilMisc.toMap("floorBannerId", floorBannerId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("floorBannerId", floorBanner.get("floorBannerId"));
        result.put("bannerName", floorBanner.get("bannerName"));
        result.put("imgUrl", floorBanner.get("imgUrl"));
        result.put("linkUrl", floorBanner.get("linkUrl"));
        result.put("isEnabled", floorBanner.get("isEnabled"));
        result.put("sequenceNum", floorBanner.get("sequenceNum"));
        result.put("description", floorBanner.get("description"));
        
        result.put("firstLinkType", floorBanner.get("firstLinkType"));
        result.put("linkId", floorBanner.get("linkId"));
        result.put("linkName", floorBanner.get("linkName"));
        
        return result;
    }
}
