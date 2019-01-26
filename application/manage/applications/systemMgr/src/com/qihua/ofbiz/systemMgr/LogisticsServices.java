package com.qihua.ofbiz.systemMgr;

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
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by AlexYao on 2016-4-11 13:46:12.
 */
public class LogisticsServices {

    public static final String module = LogisticsServices.class.getName();
    public static final String resource = "SystemMgrUiLabels";
    public static final String resourceError = "SystemMgrErrorUiLabels";


    /**
     * 分页查询物流公司 Add By AlexYao 2016-4-11
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> logisticsCompany(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Locale locale = (Locale) context.get("locale");

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


        List<Map> companyList = FastList.newInstance();
        int companyListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("LC", "LogisticsCompany");
        dynamicView.addAlias("LC", "id");
        dynamicView.addAlias("LC", "localCompanyId");
        dynamicView.addAlias("LC", "companyId");
        dynamicView.addAlias("LC", "companyName");
        dynamicView.addAlias("LC", "sequenceId");
        dynamicView.addAlias("LC", "isEnabled");
        dynamicView.addAlias("LC", "productStoreId");
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("id");
        fieldsToSelect.add("localCompanyId");
        fieldsToSelect.add("companyId");
        fieldsToSelect.add("companyName");
        fieldsToSelect.add("sequenceId");
        fieldsToSelect.add("isEnabled");

        String sortField = "sequenceId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        //根据物流公司编号模糊查询
        if (UtilValidate.isNotEmpty(context.get("localCompanyId"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("localCompanyId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("localCompanyId") + "%")));
        }

        //根据物流公司名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("companyName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("companyName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("companyName") + "%")));
        }

        //根据咨询内容模糊查询
        if (UtilValidate.isNotEmpty(context.get("consultContent"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("consultContent"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("consultContent") + "%")));
        }

        if(UtilValidate.isNotEmpty(context.get("productStoreId"))){
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, context.get("productStoreId")));
    
        }
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

            int i = 0;
            //遍历查询结果集
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map record = FastMap.newInstance();
                record.put("id", gv.getString("id"));
                record.put("localCompanyId", gv.get("localCompanyId"));
                record.put("companyId", gv.get("companyId"));
                record.put("companyName", gv.get("companyName"));
                record.put("sequenceId", gv.get("sequenceId"));
                record.put("isEnabled", gv.get("isEnabled"));
                companyList.add(record);
            }

            //总记录数
            companyListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > companyListSize) {
                highIndex = companyListSize;
            }

            //关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in logistics company find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        result.put("recordsList", companyList);
        result.put("totalSize", Integer.valueOf(companyListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 新建物流公司 Add By AlexYao 2016-4-11
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> addLogisticsCompany(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String companyName = (String) context.get("companyName");
        String localCompanyId = (String) context.get("localCompanyId");
        String companyId = (String) context.get("companyId");
        String companyWebsite = (String) context.get("companyWebsite");
        String inquireWebsite = (String) context.get("inquireWebsite");
        String sequenceId = (String) context.get("sequenceId");
        String isEnabled = (String) context.get("isEnabled");
        String productStoreId = (String) context.get("productStoreId");
        
        if (UtilValidate.isEmpty(isEnabled)) {
            isEnabled = "N";
        }
        if (UtilValidate.isNotEmpty(companyName) && UtilValidate.isNotEmpty(localCompanyId)) {
            String id = delegator.getNextSeqId("LogisticsCompany");
            GenericValue logisticsCompany = delegator.makeValue("LogisticsCompany");
            logisticsCompany.set("id", id);
            logisticsCompany.set("companyName", companyName);
            logisticsCompany.set("localCompanyId", localCompanyId);
            if (UtilValidate.isNotEmpty(companyId)) {
                logisticsCompany.set("companyId", companyId);
            }
            if (UtilValidate.isNotEmpty(companyWebsite)) {
                logisticsCompany.set("companyWebsite", companyWebsite);
            }
            if (UtilValidate.isNotEmpty(inquireWebsite)) {
                logisticsCompany.set("inquireWebsite", inquireWebsite);
            }
            if (UtilValidate.isNotEmpty(sequenceId)) {
                logisticsCompany.set("sequenceId", Long.valueOf(sequenceId));
            }
            logisticsCompany.set("isEnabled", isEnabled);
            logisticsCompany.set("productStoreId",productStoreId);
            try {
                delegator.create(logisticsCompany);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 修改物流公司 Add By AlexYao 2016-4-11
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editLogisticsCompany(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String id = (String) context.get("id");
        String companyName = (String) context.get("companyName");
        String localCompanyId = (String) context.get("localCompanyId");
        String companyId = (String) context.get("companyId");
        String companyWebsite = (String) context.get("companyWebsite");
        String inquireWebsite = (String) context.get("inquireWebsite");
        String sequenceId = (String) context.get("sequenceId");
        String isEnabled = (String) context.get("isEnabled");
        if (UtilValidate.isNotEmpty(id) && UtilValidate.isNotEmpty(companyName) && UtilValidate.isNotEmpty(localCompanyId)) {
            GenericValue logisticsCompany = null;
            try {
                logisticsCompany = delegator.findByPrimaryKey("LogisticsCompany", UtilMisc.toMap("id", id));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(logisticsCompany)) {
                logisticsCompany.set("companyName", companyName);
                logisticsCompany.set("localCompanyId", localCompanyId);
                logisticsCompany.set("companyId", companyId);
                logisticsCompany.set("companyWebsite", companyWebsite);
                logisticsCompany.set("inquireWebsite", inquireWebsite);
                if(UtilValidate.isNotEmpty(sequenceId)){
                    logisticsCompany.set("sequenceId", Long.valueOf(sequenceId));
                }else{
                    logisticsCompany.set("sequenceId", null);
                }
                if (UtilValidate.isNotEmpty(isEnabled)) {
                    logisticsCompany.set("isEnabled", isEnabled);
                }
                try {
                    delegator.store(logisticsCompany);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 删除物流公司 Add By AlexYao 2016-4-11
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> deleteLogisticsCompany(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        String ids = (String) context.get("ids");
        if (UtilValidate.isNotEmpty(ids)) {
            List<GenericValue> tobeRemove = new ArrayList<GenericValue>();
            try {
                for (String id : StringUtil.split(ids, ",")) {
                    List<GenericValue> logisticsTemples = delegator.findByAnd("LogisticsTemple", UtilMisc.toMap("logisticsCompanyId", id));
                    if(UtilValidate.isNotEmpty(logisticsTemples)) {
                        for (GenericValue logisticsTemple : logisticsTemples) {
                            List<GenericValue> defaultLogisticsTemples = delegator.findByAnd("DefaultLogisticsTemple",UtilMisc.toMap("logisticsTempleId", logisticsTemple.get("logisticsTempleId")));
                            if(UtilValidate.isNotEmpty(defaultLogisticsTemples)){
                                tobeRemove.addAll(defaultLogisticsTemples);
                            }
                            List<GenericValue> logisticsTempleItems = delegator.findByAnd("LogisticsTempleItem",UtilMisc.toMap("logisticsTempleId", logisticsTemple.get("logisticsTempleId")));
                            if(UtilValidate.isNotEmpty(logisticsTempleItems)){
                                tobeRemove.addAll(logisticsTempleItems);
                            }
                        }
                        tobeRemove.addAll(logisticsTemples);
                    }
                    List<GenericValue> logisticsDocuments = delegator.findByAnd("LogisticsDocuments", UtilMisc.toMap("logisticsCompanyId", id));
                    if(UtilValidate.isNotEmpty(logisticsDocuments)){
                        tobeRemove.addAll(logisticsDocuments);
                    }
                    List<GenericValue> orderDeliverys = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("deliveryCompany", id));
                    if(UtilValidate.isNotEmpty(orderDeliverys)){
                        tobeRemove.addAll(orderDeliverys);
                    }
                    List<GenericValue> logisticsCompanys = delegator.findByAnd("LogisticsCompany", UtilMisc.toMap("id", id));
                    tobeRemove.addAll(logisticsCompanys);
                }
                if(UtilValidate.isNotEmpty(tobeRemove)){
                    delegator.removeAll(tobeRemove);
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 物流公司详情 Add By AlexYao 2016-4-11
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> logisticsCompanyDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String id = (String) context.get("id");
        if (UtilValidate.isNotEmpty(id)) {
            GenericValue logisticsCompany = null;
            try {
                logisticsCompany = delegator.findByPrimaryKey("LogisticsCompany", UtilMisc.toMap("id", id));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(logisticsCompany)) {
                result.put("id", logisticsCompany.get("id"));
                result.put("companyName", logisticsCompany.get("companyName"));
                result.put("localCompanyId", logisticsCompany.get("localCompanyId"));
                result.put("companyId", logisticsCompany.get("companyId"));
                result.put("companyWebsite", logisticsCompany.get("companyWebsite"));
                result.put("inquireWebsite", logisticsCompany.get("inquireWebsite"));
                result.put("sequenceId", UtilValidate.isNotEmpty(logisticsCompany.get("sequenceId"))?logisticsCompany.getLong("sequenceId").toString():"");
                result.put("isEnabled", logisticsCompany.get("isEnabled"));
            }
        }
        return result;
    }

    /**
     * 修改是否启用物流公司 Add By AlexYao 2016-4-11
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editLCIsEnable(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String id = (String) context.get("id");
        String isEnabled = (String) context.get("isEnabled");
        if (UtilValidate.isNotEmpty(id) && UtilValidate.isNotEmpty(isEnabled)) {
            GenericValue logisticsCompany = null;
            try {
                logisticsCompany = delegator.findByPrimaryKey("LogisticsCompany", UtilMisc.toMap("id", id));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(logisticsCompany)) {
                logisticsCompany.set("isEnabled", isEnabled);
            }
            try {
                delegator.store(logisticsCompany);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取省市 Add By AlexYao 2016-4-12
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> selectProvinceList(DispatchContext dctx, Map<String, ? extends Object> context) {
        List tree = FastList.newInstance();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        try {
            List<GenericValue> geoAssocs = delegator.findByAnd("GeoAssoc", UtilMisc.toMap("geoId", "CHN", "geoAssocTypeId", "REGIONS"));
            if (UtilValidate.isNotEmpty(geoAssocs)) {
                for (GenericValue geoAssoc : geoAssocs) {
                    String geoId = geoAssoc.getString("geoIdTo");
                    GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", geoId));
                    String name = geo.getString("geoName");
                    Map map = new HashMap();
                    map.put("provinceId", geoId);
                    map.put("provinceName", name);
                    List cityList = new ArrayList();
                    List<GenericValue> geoAssocs1 = delegator.findByAnd("GeoAssoc", UtilMisc.toMap("geoId", geoId, "geoAssocTypeId", "REGIONS"));
                    if (UtilValidate.isNotEmpty(geoAssocs1)) {
                        for (GenericValue geoAssoc1 : geoAssocs1) {
                            String cityId = geoAssoc1.getString("geoIdTo");
                            GenericValue geo1 = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", cityId));
                            String cityName = geo1.getString("geoName");
                            Map<String, String> map1 = new HashMap<String, String>();
                            map1.put("cityId", cityId);
                            map1.put("cityName", cityName);
                            cityList.add(map1);
                        }
                    }
                    map.put("cityList", cityList);
                    tree.add(map);
                }
            }
        } catch (GenericEntityException e) {
            ServiceUtil.returnError(e.getMessage());
        }
        result.put("areaTree", tree);
        return result;
    }

    /**
     * 新增物流模板   Add By AlexYao 2016-4-13
     *
     * @param request
     * @param response
     * @return
     */
    public static String addLogisticsTempleJson(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String productStoreId = (String)request.getAttribute("productStoreId");
      
        
        String freightTemplateName = request.getParameter("freightTemplateName");
        String freightPackageMail = request.getParameter("freightPackageMail");
        String freightMethods = request.getParameter("freightMethods");
        String logComId = request.getParameter("logComId");
        if (UtilValidate.isNotEmpty(freightTemplateName) && UtilValidate.isNotEmpty(freightPackageMail) && UtilValidate.isNotEmpty(freightMethods) && UtilValidate.isNotEmpty(logComId)) {
            List<GenericValue> logisticsTemples = null;
            try {
                logisticsTemples = delegator.findByAnd("LogisticsTemple", UtilMisc.toMap("logisticsMethods",freightMethods,"logisticsCompanyId",logComId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isNotEmpty(logisticsTemples)){
                request.setAttribute("status",false);
                request.setAttribute("info","该模板已存在");
                return "error";
            }
            Map map = request.getParameterMap();
            String[] areas = (String[]) map.get(logComId + "_areas");
            String[] start = (String[]) map.get(logComId + "_start");
            String[] postage = (String[]) map.get(logComId + "_postage");
            String[] plus = (String[]) map.get(logComId + "_plus");
            String[] postageplus = (String[]) map.get(logComId + "_postageplus");
            if (UtilValidate.isNotEmpty(areas) && UtilValidate.isNotEmpty(start) && UtilValidate.isNotEmpty(postage) && UtilValidate.isNotEmpty(plus) && UtilValidate.isNotEmpty(postageplus)) {
                List<GenericValue> tobeStore = new ArrayList<GenericValue>();
                GenericValue logisticsTemple = delegator.makeValue("LogisticsTemple");
                String logisticsTempleId = delegator.getNextSeqId("LogisticsTemple");
                logisticsTemple.set("logisticsTempleId", logisticsTempleId);
                logisticsTemple.set("logisticsTempleName", freightTemplateName);
                logisticsTemple.set("logisticsPackageMail", freightPackageMail);
                logisticsTemple.set("logisticsMethods", freightMethods);
                logisticsTemple.set("logisticsCompanyId", logComId);
                tobeStore.add(logisticsTemple);
                GenericValue defaultLogisticsTemple = delegator.makeValue("DefaultLogisticsTemple");
                String defaultLogisticsTempleId = delegator.getNextSeqId("DefaultLogisticsTemple");
                defaultLogisticsTemple.set("id", defaultLogisticsTempleId);
                defaultLogisticsTemple.set("productStoreId", productStoreId);
                defaultLogisticsTemple.set("logisticsTempleId", logisticsTempleId);
                List<GenericValue> curLogisticsTempleInfos=FastList.newInstance();
                try {
                    curLogisticsTempleInfos=delegator.findByAnd("LogisticsTemple");
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if(UtilValidate.isEmpty(curLogisticsTempleInfos)){
                    // 第一条自动设置为默认模板
                    defaultLogisticsTemple.set("isDefault", "Y");
                }else{
                    defaultLogisticsTemple.set("isDefault", "N");
                }

                tobeStore.add(defaultLogisticsTemple);
                for (int i = 0; i < areas.length; i++) {
                    GenericValue logisticsTempleItem = delegator.makeValue("LogisticsTempleItem");
                    String logisticsTempleItemId = delegator.getNextSeqId("LogisticsTemple");
                    logisticsTempleItem.set("id", logisticsTempleItemId);
                    logisticsTempleItem.set("logisticsTempleId", logisticsTempleId);
                    logisticsTempleItem.set("areas", areas[i]);
                    logisticsTempleItem.set("start", new BigDecimal(start[i]));
                    logisticsTempleItem.set("postage", new BigDecimal(postage[i]));
                    logisticsTempleItem.set("plus", new BigDecimal(plus[i]));
                    logisticsTempleItem.set("postagePlus", new BigDecimal(postageplus[i]));
                    tobeStore.add(logisticsTempleItem);
                }
                if (UtilValidate.isNotEmpty(tobeStore)) {
                    try {
                        delegator.storeAll(tobeStore);
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        request.setAttribute("status",true);
        return "success";
    }

    /**
     * 编辑物流模板   Add By AlexYao 2016-4-13
     *
     * @param request
     * @param response
     * @return
     */
    public static String editLogisticsTempleJson(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String logisticsTempleId = request.getParameter("logisticsTempleId");
        String freightTemplateName = request.getParameter("freightTemplateName");
        String freightPackageMail = request.getParameter("freightPackageMail");
        String freightMethods = request.getParameter("freightMethods");
        String logComId = request.getParameter("logComId");
        if (UtilValidate.isNotEmpty(logisticsTempleId) && UtilValidate.isNotEmpty(freightTemplateName) && UtilValidate.isNotEmpty(freightPackageMail) && UtilValidate.isNotEmpty(freightMethods) && UtilValidate.isNotEmpty(logComId)) {
            List<GenericValue> logisticsTemples = null;
            try {
                logisticsTemples = delegator.findByAnd("LogisticsTemple", UtilMisc.toMap("logisticsMethods",freightMethods,"logisticsCompanyId",logComId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isNotEmpty(logisticsTemples)){
                for(GenericValue temple : logisticsTemples){
                    if(!logisticsTempleId.equals(temple.get("logisticsTempleId"))) {
                        request.setAttribute("status", false);
                        request.setAttribute("info", "该模板已存在");
                        return "error";
                    }
                }
            }
            Map map = request.getParameterMap();
            String[] areas = (String[]) map.get(logComId + "_areas");
            String[] start = (String[]) map.get(logComId + "_start");
            String[] postage = (String[]) map.get(logComId + "_postage");
            String[] plus = (String[]) map.get(logComId + "_plus");
            String[] postageplus = (String[]) map.get(logComId + "_postageplus");
            if (UtilValidate.isNotEmpty(areas) && UtilValidate.isNotEmpty(start) && UtilValidate.isNotEmpty(postage) && UtilValidate.isNotEmpty(plus) && UtilValidate.isNotEmpty(postageplus)) {
                List<GenericValue> tobeStore = new ArrayList<GenericValue>();
                GenericValue logisticsTemple = null;
                try {
                    logisticsTemple = delegator.findByPrimaryKey("LogisticsTemple", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                logisticsTemple.set("logisticsTempleId", logisticsTempleId);
                logisticsTemple.set("logisticsTempleName", freightTemplateName);
                logisticsTemple.set("logisticsPackageMail", freightPackageMail);
                logisticsTemple.set("logisticsMethods", freightMethods);
                logisticsTemple.set("logisticsCompanyId", logComId);
                tobeStore.add(logisticsTemple);
                List<GenericValue> logisticsTempleItems = null;
                try {
                    logisticsTempleItems = delegator.findByAnd("LogisticsTempleItem", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(logisticsTempleItems)) {
                    try {
                        delegator.removeAll(logisticsTempleItems);
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < areas.length; i++) {
                    GenericValue logisticsTempleItem = delegator.makeValue("LogisticsTempleItem");
                    String logisticsTempleItemId = delegator.getNextSeqId("LogisticsTemple");
                    logisticsTempleItem.set("id", logisticsTempleItemId);
                    logisticsTempleItem.set("logisticsTempleId", logisticsTempleId);
                    logisticsTempleItem.set("areas", areas[i]);
                    logisticsTempleItem.set("start", new BigDecimal(start[i]));
                    logisticsTempleItem.set("postage", new BigDecimal(postage[i]));
                    logisticsTempleItem.set("plus", new BigDecimal(plus[i]));
                    logisticsTempleItem.set("postagePlus", new BigDecimal(postageplus[i]));
                    tobeStore.add(logisticsTempleItem);
                }
                if (UtilValidate.isNotEmpty(tobeStore)) {
                    try {
                        delegator.storeAll(tobeStore);
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        request.setAttribute("status", true);
        return "success";
    }

    /**
     * 复制物流模板  Add By AlexYao 2016-4-13
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> copyLogisticsTemple(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String logisticsTempleId = (String) context.get("logisticsTempleId");
        GenericValue logisticsTemple = null;
        List<GenericValue> defaultLogisticsTemple = null;
        List<GenericValue> logisticsTempleItems = null;
        try {
            logisticsTemple = delegator.findByPrimaryKey("LogisticsTemple", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
            defaultLogisticsTemple = delegator.findByAnd("DefaultLogisticsTemple", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
            logisticsTempleItems = delegator.findByAnd("LogisticsTempleItem", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(logisticsTemple)) {
            List<GenericValue> tobeStore = new ArrayList<GenericValue>();
            GenericValue newLogisticsTemple = delegator.makeValue("LogisticsTemple");
            String newLogisticsTempleId = delegator.getNextSeqId("LogisticsTemple");
            newLogisticsTemple.set("logisticsTempleId", newLogisticsTempleId);
            newLogisticsTemple.set("logisticsTempleName", logisticsTemple.get("logisticsTempleName") + "的副本");
            newLogisticsTemple.set("logisticsPackageMail", logisticsTemple.get("logisticsPackageMail"));
            newLogisticsTemple.set("logisticsMethods", logisticsTemple.get("logisticsMethods"));
            newLogisticsTemple.set("logisticsCompanyId", logisticsTemple.get("logisticsCompanyId"));
            tobeStore.add(newLogisticsTemple);
            if (UtilValidate.isNotEmpty(defaultLogisticsTemple)) {
                GenericValue newDefaultLogisticsTemple = delegator.makeValue("DefaultLogisticsTemple");
                String newId = delegator.getNextSeqId("DefaultLogisticsTemple");
                newDefaultLogisticsTemple.set("id", newId);
                newDefaultLogisticsTemple.set("productStoreId", defaultLogisticsTemple.get(0).get("productStoreId"));
                newDefaultLogisticsTemple.set("logisticsTempleId", newLogisticsTempleId);
                newDefaultLogisticsTemple.set("isDefault", "N");
                tobeStore.add(newDefaultLogisticsTemple);
            }
            if (UtilValidate.isNotEmpty(logisticsTempleItems)) {
                for (GenericValue logisticsTempleItem : logisticsTempleItems) {
                    GenericValue newLogisticsTempleItem = delegator.makeValue("LogisticsTempleItem");
                    String newId = delegator.getNextSeqId("LogisticsTemple");
                    newLogisticsTempleItem.set("id", newId);
                    newLogisticsTempleItem.set("logisticsTempleId", newLogisticsTempleId);
                    newLogisticsTempleItem.set("areas", logisticsTempleItem.get("areas"));
                    newLogisticsTempleItem.set("start", logisticsTempleItem.get("start"));
                    newLogisticsTempleItem.set("postage", logisticsTempleItem.get("postage"));
                    newLogisticsTempleItem.set("plus", logisticsTempleItem.get("plus"));
                    newLogisticsTempleItem.set("postagePlus", logisticsTempleItem.get("postagePlus"));
                    tobeStore.add(newLogisticsTempleItem);
                }
            }
            if (UtilValidate.isNotEmpty(tobeStore)) {
                try {
                    delegator.storeAll(tobeStore);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 删除物流模板  Add By AlexYao 2016-4-13
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> deleteLogisticsTemple(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String logisticsTempleId = (String) context.get("logisticsTempleId");
        GenericValue logisticsTemple = null;
        List<GenericValue> defaultLogisticsTemple = null;
        List<GenericValue> logisticsTempleItems = null;
        try {
            logisticsTemple = delegator.findByPrimaryKey("LogisticsTemple", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
            defaultLogisticsTemple = delegator.findByAnd("DefaultLogisticsTemple", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
            logisticsTempleItems = delegator.findByAnd("LogisticsTempleItem", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        List<GenericValue> tobeRemove = new ArrayList<GenericValue>();
        if (UtilValidate.isNotEmpty(logisticsTempleItems)) {
            tobeRemove.addAll(logisticsTempleItems);
        }
        if (UtilValidate.isNotEmpty(defaultLogisticsTemple)) {
            tobeRemove.addAll(defaultLogisticsTemple);
        }
        if (UtilValidate.isNotEmpty(logisticsTemple)) {
            tobeRemove.add(logisticsTemple);
        }
        if (UtilValidate.isNotEmpty(tobeRemove)) {
            try {
                delegator.removeAll(tobeRemove);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 设置默认物流模板  Add By AlexYao 2016-4-13
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> setDefaultLogisticsTemple(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String logisticsTempleId = (String) context.get("logisticsTempleId");
        GenericValue logisticsTemple = null;
        List<GenericValue> newDefaultLogisticsTemple = null;
        List<GenericValue> tobeStore = new ArrayList<GenericValue>();
        try {
            logisticsTemple = delegator.findByPrimaryKey("LogisticsTemple", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
            newDefaultLogisticsTemple = delegator.findByAnd("DefaultLogisticsTemple", UtilMisc.toMap("logisticsTempleId", logisticsTempleId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if(UtilValidate.isNotEmpty(logisticsTemple)){
            String logisticsMethods = null;
            if("0".equals(logisticsTemple.get("logisticsMethods"))){
                logisticsMethods = "1";
            }else{
                logisticsMethods = "0";
            }
            List<GenericValue> oldLogisticsTemple = null;
            try {
                oldLogisticsTemple = delegator.findByAnd("LogisticsTemple", UtilMisc.toMap("logisticsMethods", logisticsMethods,"logisticsCompanyId",logisticsTemple.get("logisticsCompanyId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isNotEmpty(oldLogisticsTemple)){
                List<GenericValue> oldDefaultLogisticsTemples = null;
                try {
                    oldDefaultLogisticsTemples = delegator.findByAnd("DefaultLogisticsTemple", UtilMisc.toMap("logisticsTempleId", oldLogisticsTemple.get(0).get("logisticsTempleId"),"isDefault", "Y"));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(oldDefaultLogisticsTemples)) {
                    for (int i = 0; i < oldDefaultLogisticsTemples.size(); i++) {
                        oldDefaultLogisticsTemples.get(i).set("isDefault", "N");
                    }
                    tobeStore.addAll(oldDefaultLogisticsTemples);
                }
            }
        }
        if (UtilValidate.isNotEmpty(newDefaultLogisticsTemple)) {
            newDefaultLogisticsTemple.get(0).set("isDefault", "Y");
            tobeStore.addAll(newDefaultLogisticsTemple);
        }
        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 新增物流单据  Add By AlexYao 2016-4-15
     * @param request
     * @param response
     * @return
     */
    public static String addDocuments(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String logisticsCompanyId = request.getParameter("billName");
        String width = request.getParameter("billWidth");
        String height = request.getParameter("billHeight");
        String contentId = request.getParameter("express-bg");
        String content = request.getParameter("express-content");
        String productStoreId = (String)request.getAttribute("productStoreId");
        GenericValue logisticsDocuments = delegator.makeValue("LogisticsDocuments");
        String logisticsDocumentsId = delegator.getNextSeqId("LogisticsDocuments");
        logisticsDocuments.set("logisticsDocumentsId",logisticsDocumentsId);
        logisticsDocuments.set("logisticsCompanyId",logisticsCompanyId);
        logisticsDocuments.set("width",Long.valueOf(width));
        logisticsDocuments.set("height",Long.valueOf(height));
        logisticsDocuments.set("contentId",contentId);
        logisticsDocuments.set("productStoreId",productStoreId);
        if(UtilValidate.isNotEmpty(content)){
            logisticsDocuments.set("content",content);
        }
        try {
            delegator.create(logisticsDocuments);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return "success";
    }

    /**
     * 编辑物流单据  Add By AlexYao 2016-4-15
     * @param request
     * @param response
     * @return
     */
    public static String editDocuments(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String logisticsDocumentsId = request.getParameter("logisticsDocumentsId");
        String logisticsCompanyId = request.getParameter("billName");
        String width = request.getParameter("billWidth");
        String height = request.getParameter("billHeight");
        String contentId = request.getParameter("express-bg");
        String content = request.getParameter("express-content");
        String productStoreId = (String) request.getAttribute("productStoreId");

        GenericValue logisticsDocuments = null;
        try {
            logisticsDocuments = delegator.findByPrimaryKey("LogisticsDocuments", UtilMisc.toMap("logisticsDocumentsId",logisticsDocumentsId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if(UtilValidate.isNotEmpty(logisticsDocuments)){
            logisticsDocuments.set("logisticsCompanyId",logisticsCompanyId);
            logisticsDocuments.set("width",Long.valueOf(width));
            logisticsDocuments.set("height",Long.valueOf(height));
            logisticsDocuments.set("contentId",contentId);
            logisticsDocuments.set("content",content);
            logisticsDocuments.set("productStoreId",productStoreId);
            try {
                delegator.store(logisticsDocuments);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }

        return "success";
    }

    /**
     * 删除物流单据  Add By AlexYao 2016-4-15
     * @param request
     * @param response
     * @return
     */
    public static String deleteDocuments(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String logisticsDocumentsId = request.getParameter("logisticsDocumentsId");

        GenericValue logisticsDocuments = null;
        try {
            logisticsDocuments = delegator.findByPrimaryKey("LogisticsDocuments", UtilMisc.toMap("logisticsDocumentsId",logisticsDocumentsId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if(UtilValidate.isNotEmpty(logisticsDocuments)){
            try {
                delegator.removeValue(logisticsDocuments);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }

        return "success";
    }
}
