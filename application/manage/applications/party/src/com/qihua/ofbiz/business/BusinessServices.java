package com.qihua.ofbiz.business;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.*;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

/**
 * banner service
 *
 * @author
 */
public class BusinessServices {
    public static final String module = BusinessServices.class.getName();
    public static final String resource = "PartyUiLabels";

    /**
     * 商家列表查询
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBusinessList(DispatchContext dctx, Map<String, ? extends Object> context) {
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

        dynamicView.addMemberEntity("P", "Party");
        dynamicView.addAlias("P", "statusId");

        dynamicView.addMemberEntity("PB", "PartyBusiness");
        //设置需要查询的字段
        dynamicView.addAlias("PB", "partyId");
        dynamicView.addAlias("PB", "leageName");
        dynamicView.addAlias("PB", "leageTel");
        dynamicView.addAlias("PB", "createdStamp");
        dynamicView.addViewLink("P", "PB", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));

        dynamicView.addMemberEntity("PG", "PartyGroup");
        dynamicView.addAlias("PG", "partyName");
        dynamicView.addViewLink("P", "PG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));

        dynamicView.addMemberEntity("PBA", "PartyBusinessAudit");
        dynamicView.addAlias("PBA", "auditStatus");
        dynamicView.addViewLink("P", "PBA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));


        fieldsToSelect.add("partyId");
        fieldsToSelect.add("statusId");
        fieldsToSelect.add("partyName");
        fieldsToSelect.add("leageName");
        fieldsToSelect.add("leageTel");
        fieldsToSelect.add("auditStatus");
        fieldsToSelect.add("createdStamp");

        // 按商家名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("partyName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("partyName") + "%")));
        }

        // 按法人名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("leageName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("leageName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("leageName") + "%")));
        }

        // 按审核状态精确查询
        if (UtilValidate.isNotEmpty(context.get("auditStatus"))) {
            filedExprs.add(EntityCondition.makeCondition("auditStatus", EntityOperator.EQUALS, context.get("auditStatus")));
        }

        // 排序字段名称
        String sortField = "partyId";
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
     * 商家列表查询 add by qianjin 2016/01/27
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBusinessToAuditList(DispatchContext dctx, Map<String, ? extends Object> context) {
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
        dynamicView.addMemberEntity("PB", "PartyBusiness");
        //设置需要查询的字段
        dynamicView.addAlias("PB", "partyId");
        dynamicView.addAlias("PB", "leageName");
        dynamicView.addAlias("PB", "leageTel");
        dynamicView.addAlias("PB", "createdStamp");


        dynamicView.addMemberEntity("PG", "PartyGroup");
        dynamicView.addAlias("PG", "partyName");
        dynamicView.addViewLink("PG", "PB", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));

        dynamicView.addMemberEntity("PBA", "PartyBusinessAudit");
        dynamicView.addAlias("PBA", "auditStatus");
        dynamicView.addViewLink("PG", "PBA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));


        fieldsToSelect.add("partyId");
        fieldsToSelect.add("partyName");
        fieldsToSelect.add("leageName");
        fieldsToSelect.add("leageTel");
        fieldsToSelect.add("auditStatus");
        fieldsToSelect.add("createdStamp");

        // 按商家名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("partyName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyName"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("partyName") + "%")));
        }

        // 按法人名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("leageName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("leageName"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("leageName") + "%")));
        }

        // 按审核状态精确查询
        filedExprs.add(EntityCondition.makeCondition("auditStatus", EntityOperator.EQUALS, "0"));

        // 排序字段名称
        String sortField = "partyId";
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
     * 商家详情
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBusinessInfoById(DispatchContext dctx, Map<String, ? extends Object> context) {
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 参数
        String partyId = (String) context.get("partyId");
        try {
            GenericValue party = delegator.findByAnd("Party", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("party", party);

            GenericValue partyGroup = delegator.findByAnd("PartyGroup", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("partyGroup", partyGroup);

            GenericValue partyBusiness = delegator.findByAnd("PartyBusiness", UtilMisc.toMap("partyId", partyId)).get(0);
            String contractDoc = partyBusiness.getString("contractDoc");
            if(UtilValidate.isNotEmpty(contractDoc)){
                String baseImg = UtilProperties.getMessage("application.properties", "image.base.url", locale);
                String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
                if(uploadType.equals("FTP")){
                    baseImg = "";
                }
                String contractDocUrl = baseImg + ContentWorker.renderContentAsText(dispatcher, delegator, contractDoc, false);
                result.put("contractDocUrl",contractDocUrl);
            }

            //得出省市区的中文名称
            String province_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("province"))).get("geoName");
            partyBusiness.setString("province", province_name);

            String city_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("city"))).get("geoName");
            partyBusiness.setString("city", city_name);

            String county_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("county"))).get("geoName");
            partyBusiness.setString("county", county_name);

            String busiProvince_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("busiProvince"))).get("geoName");
            partyBusiness.setString("busiProvince", busiProvince_name);

            String busiCity_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("busiCity"))).get("geoName");
            partyBusiness.setString("busiCity", busiCity_name);

            String busiCounty_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("busiCounty"))).get("geoName");
            partyBusiness.setString("busiCounty", busiCounty_name);

            String branchProvince_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("branchProvince"))).get("geoName");
            partyBusiness.setString("branchProvince", branchProvince_name);

            String branchCity_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("branchCity"))).get("geoName");
            partyBusiness.setString("branchCity", branchCity_name);

            String branchCounty_name = (String) dispatcher.runSync("getGeoNameById", UtilMisc.toMap("geoId", partyBusiness.getString("branchCounty"))).get("geoName");
            partyBusiness.setString("branchCounty", branchCounty_name);

            result.put("partyBusiness", partyBusiness);

            GenericValue partyBusinessAudit = delegator.findByAnd("PartyBusinessAudit", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("partyBusinessAudit", partyBusinessAudit);

            List<GenericValue> partyProductCategory = delegator.findByAnd("PartyProductCategory", UtilMisc.toMap("partyId", partyId));
            result.put("partyProductCategory", partyProductCategory);

            GenericValue businessAudit = EntityUtil.getFirst(delegator.findByAnd("PartyBusinessAudit",UtilMisc.toMap("partyId",partyId)));
            String auditStatus=businessAudit.getString("auditStatus");
            List<GenericValue> partyBusinessBrands =null;
            if("0".equalsIgnoreCase(auditStatus)){
                partyBusinessBrands = delegator.findByAnd("PartyBusinessBrand", UtilMisc.toMap("partyId", partyId,"auditStatus","0","isDel","N"));
            }else{
                partyBusinessBrands = delegator.findByAnd("PartyBusinessBrand", UtilMisc.toMap("partyId", partyId,"auditStatus","1","isDel","N"));
            }

            List<Map> partyBusinessBrand = FastList.newInstance();
            for (GenericValue brand : partyBusinessBrands) {
                String productBrandId = brand.getString("productBrandId");
                //获取brandName
                String brandName = (String) delegator.findByAnd("ProductBrand", UtilMisc.toMap("productBrandId", productBrandId)).get(0).get("brandName");
                partyBusinessBrand.add(UtilMisc.toMap("productBrandId", productBrandId, "brandName", brandName));
            }
            result.put("partyBusinessBrand", partyBusinessBrand);

            GenericValue partyRelationShip = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdFrom", partyId)).get(0);
            result.put("partyRelationShip", partyRelationShip);

        } catch (Exception e) {
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
        String partyId = (String) context.get("partyId");
        String auditStatus = (String) context.get("auditStatus");
        String memberId = (String) context.get("memberId");
        String partyName = (String) context.get("partyName");
        String province = (String) context.get("province");
        String city = (String) context.get("city");
        String county = (String) context.get("county");
        String address = (String) context.get("address");
        String statusId = (String) context.get("statusId");
        String description =context.get("description")==null?"":(String)context.get("description");
        String companyName = (String) context.get("companyName");
        Timestamp companyCreateDate =context.get("companyCreateDate")==null?null:(Timestamp) context.get("companyCreateDate");
        Timestamp businessEndDate =  context.get("businessEndDate")==null?null:(Timestamp) context.get("businessEndDate");
        String leageName = (String) context.get("leageName");
        String leageTel = (String) context.get("leageTel");
        String leageEmail = (String) context.get("leageEmail");
        String leageCardNo = (String) context.get("leageCardNo");
        String logoImg = (String) context.get("logoImg");
//        String leageImgContentId = (String) context.get("leageImgContentId");
        String idCardProsImg = (String) context.get("idCardProsImg");
        String idCardConsImg = (String) context.get("idCardConsImg");
        String qualifImg = (String) context.get("qualifImg");
        String contractDoc = (String) context.get("contractDoc");

        String socialCardNo = (String) context.get("socialCardNo");
        String busiImgContentId = (String) context.get("busiImgContentId");
        String busiProvince = (String) context.get("busiProvince");
        String busiCity = (String) context.get("busiCity");
        String busiCounty = (String) context.get("busiCounty");
        String busiAddress = (String) context.get("busiAddress");

        String bankAcountName = (String) context.get("bankAcountName");
        String bankAccount = (String) context.get("bankAccount");
        String bankBranchName = (String) context.get("bankBranchName");
        String bankBranchAcount = (String) context.get("bankBranchAcount");
        String branchProvince = (String) context.get("branchProvince");
        String branchCity = (String) context.get("branchCity");
        String branchCounty = (String) context.get("branchCounty");
        String partyCategoryArrs = (String) context.get("partyCategoryArrs");
        String partyBrandArrs = (String) context.get("partyBrandArrs");

        boolean beginTransaction = false;
        try {
            beginTransaction = TransactionUtil.begin();
            //删除之前保存的
//            delegator.removeByAnd("PartyGroup", UtilMisc.toMap("partyId", partyId));
//            delegator.removeByAnd("PartyBusiness", UtilMisc.toMap("partyId", partyId));
//            delegator.removeByAnd("PartyRelationship", UtilMisc.toMap("partyIdFrom", partyId));
            delegator.removeByAnd("PartyProductCategory", UtilMisc.toMap("partyId", partyId));
            delegator.removeByAnd("PartyBusinessBrand", UtilMisc.toMap("partyId", partyId,"isDel","N","auditStatus","1"));


            //创建party
//            GenericValue party = delegator.makeValue("Party", UtilMisc.toMap("partyId", partyId, "statusId", statusId));
//            party.store();

            //创建PartyGroup
//            GenericValue partyGroup = delegator.makeValue("PartyGroup", UtilMisc.toMap("partyId", partyId, "partyName", partyName, "isInner", "N"));
//            partyGroup.create();

            //创建PartyBusiness
            GenericValue partyBusiness = delegator.makeValue("PartyBusiness");
            partyBusiness.set("partyId", partyId);
            partyBusiness.setString("companyName", companyName);
            partyBusiness.set("companyCreateDate", companyCreateDate);
            partyBusiness.set("businessEndDate", businessEndDate);
            partyBusiness.set("leageName", leageName);
            partyBusiness.set("leageTel", leageTel);
            partyBusiness.set("leageEmail", leageEmail);
            partyBusiness.set("leageCardNo", leageCardNo);
//            partyBusiness.set("leageImgContentId", leageImgContentId);
            partyBusiness.set("idCardProsImg", idCardProsImg);
            partyBusiness.set("idCardConsImg", idCardConsImg);
            partyBusiness.set("qualifImg", qualifImg);
            partyBusiness.set("contractDoc", contractDoc);
            partyBusiness.set("logoImg", logoImg);

            partyBusiness.set("socialCardNo", socialCardNo);
            partyBusiness.set("busiImgContentId", busiImgContentId);
            partyBusiness.set("busiProvince", busiProvince);
            partyBusiness.set("busiCity", busiCity);
            partyBusiness.set("busiCounty", busiCounty);
            partyBusiness.set("busiAddress", busiAddress);

            partyBusiness.set("bankAcountName", bankAcountName);
            partyBusiness.set("bankAccount", bankAccount);
            partyBusiness.set("bankBranchName", bankBranchName);
            partyBusiness.set("bankBranchAcount", bankBranchAcount);
            partyBusiness.set("branchProvince", branchProvince);
            partyBusiness.set("branchCity", branchCity);
            partyBusiness.set("branchCounty", branchCounty);
            partyBusiness.setString("province", province);
            partyBusiness.setString("city", city);
            partyBusiness.setString("county", county);
            partyBusiness.setString("address", address);
            partyBusiness.setString("description", description);
            partyBusiness.store();

            //修改之前的会员为普通会员

            GenericValue relationship = EntityUtil.getFirst(delegator.findByAnd("PartyRelationship",UtilMisc.toMap("partyIdFrom",partyId,"partyRelationshipTypeId","OWNER")));
            String oldPartyId = relationship.getString("partyIdTo");
            if(!oldPartyId.equalsIgnoreCase(memberId)){
                Map updateFields = FastMap.newInstance();
                updateFields.put("partyCategory", "MEMBER");
                EntityCondition UpdateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, oldPartyId);
                delegator.storeByCondition("Party", updateFields, UpdateCon);


                delegator.removeByAnd("PartyRelationship",UtilMisc.toMap("partyIdFrom",partyId,"partyRelationshipTypeId","OWNER"));
                //创建商家和会员的关联
                GenericValue partyRelationShip = delegator.makeValue("PartyRelationship");
                partyRelationShip.set("partyIdFrom", partyId);
                partyRelationShip.set("partyIdTo", memberId);
                partyRelationShip.set("roleTypeIdFrom", "SUPPLIER");
                partyRelationShip.set("roleTypeIdTo", "EMPLOYEE");
                partyRelationShip.set("partyRelationshipTypeId", "OWNER");
                partyRelationShip.set("fromDate", new Timestamp(System.currentTimeMillis()));
                partyRelationShip.create();

                //修改新会员为商家类型
                updateFields = FastMap.newInstance();
                updateFields.put("partyCategory", "BUSINESS");
                UpdateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, memberId);
                delegator.storeByCondition("Party", updateFields, UpdateCon);

                //给会员添加对应的角色权限
                GenericValue userLoginInfo = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", memberId)).get(0);
                String userLoginId = userLoginInfo.getString("userLoginId");

                GenericValue userLoginSecurityGroup = delegator.makeValue("UserLoginSecurityGroup");
                userLoginSecurityGroup.put("userLoginId",userLoginId);
                userLoginSecurityGroup.put("groupId","SUPPLIER");
                userLoginSecurityGroup.put("fromDate", new Timestamp(System.currentTimeMillis()));
                userLoginSecurityGroup.create();

                //删除之前的角色关系
                GenericValue userLoginInfo2 = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", oldPartyId)).get(0);
                String userLoginId2 = userLoginInfo2.getString("userLoginId");
                delegator.removeByAnd("UserLoginSecurityGroup",UtilMisc.toMap("userLoginId",userLoginId2,"groupId","SUPPLIER"));

            }



            //创建商家和签约目录的关联
            if (partyCategoryArrs != null && partyCategoryArrs.length() > 0) {
                List<GenericValue> partyCategoryList = FastList.newInstance();
                List<String> categoryList = StringUtil.split(partyCategoryArrs, "|");
                for (String category : categoryList) {
                    GenericValue c = delegator.makeValue("PartyProductCategory");
                    String[] oneCategoryArrs = category.split(",");
                    c.put("partyId", partyId);
                    c.put("productCategoryId", oneCategoryArrs[0]);
                    c.put("categoryName", oneCategoryArrs[1]);
                    if (oneCategoryArrs.length >= 3) {
                        c.put("parentCategoryId", oneCategoryArrs[2]);
                    }
                    partyCategoryList.add(c);
                }
                delegator.storeAll(partyCategoryList);
            }

            //关联商家和签约品牌的关联
            if (partyBrandArrs != null && partyBrandArrs.length() > 0) {
                List<GenericValue> partyBrandList = FastList.newInstance();
                List<String> partyBrandListArr1 = StringUtil.split(partyBrandArrs, "|");
                for (String partyBrandId : partyBrandListArr1) {
                    GenericValue c = delegator.makeValue("PartyBusinessBrand");
                    c.setNextSeqId();
                    c.put("productBrandId", partyBrandId);
                    c.put("partyId", partyId);
                    c.put("isOwner", "N");
                    c.put("auditStatus", "1");
                    c.put("isDel", "N");
                    partyBrandList.add(c);
                }
                delegator.storeAll(partyBrandList);
            }


        } catch (Exception e) {
            try {
                if(beginTransaction){
                    TransactionUtil.commit(beginTransaction);

                }
            } catch (GenericTransactionException e1) {
                e1.printStackTrace();
            }
            return ServiceUtil.returnError(e.getMessage());
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
        String memberId = (String) context.get("memberId");
        String partyName = (String) context.get("partyName");
        String province = (String) context.get("province");
        String city = (String) context.get("city");
        String county = (String) context.get("county");
        String address = (String) context.get("address");
        String statusId = (String) context.get("statusId");
        String description =context.get("description")==null?"":(String)context.get("description");
        String companyName = (String) context.get("companyName");
        Timestamp companyCreateDate =context.get("companyCreateDate")==null?null:(Timestamp) context.get("companyCreateDate");
        Timestamp businessEndDate =  context.get("businessEndDate")==null?null:(Timestamp) context.get("businessEndDate");
        String leageName = (String) context.get("leageName");
        String leageTel = (String) context.get("leageTel");
        String leageEmail = (String) context.get("leageEmail");
        String leageCardNo = (String) context.get("leageCardNo");
//        String leageImgContentId = (String) context.get("leageImgContentId");
        String idCardProsImg = (String) context.get("idCardProsImg");
        String idCardConsImg = (String) context.get("idCardConsImg");
        String qualifImg = (String) context.get("qualifImg");
        String logoImg = (String) context.get("logoImg");
        String contractDoc = (String) context.get("contractDoc");

        String socialCardNo = (String) context.get("socialCardNo");
        String busiImgContentId = (String) context.get("busiImgContentId");
        String busiProvince = (String) context.get("busiProvince");
        String busiCity = (String) context.get("busiCity");
        String busiCounty = (String) context.get("busiCounty");
        String busiAddress = (String) context.get("busiAddress");
        String bankAcountName = (String) context.get("bankAcountName");
        String bankAccount = (String) context.get("bankAccount");
        String bankBranchName = (String) context.get("bankBranchName");
        String bankBranchAcount = (String) context.get("bankBranchAcount");
        String branchProvince = (String) context.get("branchProvince");
        String branchCity = (String) context.get("branchCity");
        String branchCounty = (String) context.get("branchCounty");
        String partyCategoryArrs = (String) context.get("partyCategoryArrs");
        String partyBrandArrs = (String) context.get("partyBrandArrs");
        boolean beginTransaction = false;
        try {
            beginTransaction = TransactionUtil.begin();
            String partyId = delegator.getNextSeqId("Party");
            //创建party
            GenericValue party = delegator.makeValue("Party", UtilMisc.toMap("partyId", partyId, "partyTypeId", "PARTY_GROUP", "statusId", statusId, "partyCategory", "BUSINESS"));
            party.create();

            //创建PartyGroup
            GenericValue partyGroup = delegator.makeValue("PartyGroup", UtilMisc.toMap("partyId", partyId, "partyName", partyName, "isInner", "N"));
            partyGroup.create();

            //创建PartyBusiness
            GenericValue partyBusiness = delegator.makeValue("PartyBusiness");
            partyBusiness.set("partyId", partyId);
            partyBusiness.setString("companyName", companyName);
            partyBusiness.set("companyCreateDate", companyCreateDate);
            partyBusiness.set("businessEndDate", businessEndDate);
            partyBusiness.set("leageName", leageName);
            partyBusiness.set("leageTel", leageTel);
            partyBusiness.set("leageEmail", leageEmail);
            partyBusiness.set("leageCardNo", leageCardNo);
//            partyBusiness.set("leageImgContentId", leageImgContentId);
            partyBusiness.set("idCardProsImg", idCardProsImg);
            partyBusiness.set("idCardConsImg", idCardConsImg);
            partyBusiness.set("qualifImg", qualifImg);
            partyBusiness.set("contractDoc", contractDoc);
            partyBusiness.set("logoImg", logoImg);

            partyBusiness.set("socialCardNo", socialCardNo);
            partyBusiness.set("busiImgContentId", busiImgContentId);
            partyBusiness.set("busiProvince", busiProvince);
            partyBusiness.set("busiCity", busiCity);
            partyBusiness.set("busiCounty", busiCounty);
            partyBusiness.set("busiAddress", busiAddress);
            partyBusiness.set("bankAcountName", bankAcountName);
            partyBusiness.set("bankAccount", bankAccount);
            partyBusiness.set("bankBranchName", bankBranchName);
            partyBusiness.set("bankBranchAcount", bankBranchAcount);
            partyBusiness.set("branchProvince", branchProvince);
            partyBusiness.set("branchCity", branchCity);
            partyBusiness.set("branchCounty", branchCounty);
            partyBusiness.setString("province", province);
            partyBusiness.setString("city", city);
            partyBusiness.setString("county", county);
            partyBusiness.setString("address", address);
            partyBusiness.setString("description", description);
            partyBusiness.create();

            //创建商家和会员的关联
            GenericValue partyRelationShip = delegator.makeValue("PartyRelationship");
            partyRelationShip.set("partyIdFrom", partyId);
            partyRelationShip.set("partyIdTo", memberId);
            partyRelationShip.set("roleTypeIdFrom", "SUPPLIER");
            partyRelationShip.set("roleTypeIdTo", "EMPLOYEE");
            partyRelationShip.set("partyRelationshipTypeId", "OWNER");
            partyRelationShip.set("fromDate", new Timestamp(System.currentTimeMillis()));
            partyRelationShip.create();


            //创建商家和签约目录的关联
            if (partyCategoryArrs != null && partyCategoryArrs.length() > 0) {
                List<GenericValue> partyCategoryList = FastList.newInstance();
                List<String> categoryList = StringUtil.split(partyCategoryArrs, "|");
                for (String category : categoryList) {
                    GenericValue c = delegator.makeValue("PartyProductCategory");
                    String[] oneCategoryArrs = category.split(",");
                    c.put("partyId", partyId);
                    c.put("productCategoryId", oneCategoryArrs[0]);
                    c.put("categoryName", oneCategoryArrs[1]);
                    if (oneCategoryArrs.length >= 3) {
                        c.put("parentCategoryId", oneCategoryArrs[2]);
                    }
                    partyCategoryList.add(c);
                }
                delegator.storeAll(partyCategoryList);
            }

            //关联商家和签约品牌的关联
            if (partyBrandArrs != null && partyBrandArrs.length() > 0) {
                List<GenericValue> partyBrandList = FastList.newInstance();
                List<String> partyBrandListArr1 = StringUtil.split(partyBrandArrs, "|");
                for (String partyBrandId : partyBrandListArr1) {
                    GenericValue c = delegator.makeValue("PartyBusinessBrand");
                    c.setNextSeqId();
                    c.put("productBrandId", partyBrandId);
                    c.put("partyId", partyId);
                    c.put("auditStatus", "0");
                    c.put("isOwner", "N");
                    c.put("isDel", "N");
                    partyBrandList.add(c);
                }
                delegator.storeAll(partyBrandList);
            }
            //商家审核表
            String partyBusinessAuditId = delegator.getNextSeqId("PartyBusinessAudit");
            GenericValue partyBusinessAudit = delegator.makeValue("PartyBusinessAudit", UtilMisc.toMap("partyId", partyId, "auditStatus", "0", "partyBusinessAuditId", partyBusinessAuditId));
            partyBusinessAudit.create();

        } catch (Exception e) {
            try {
                if(beginTransaction){
                    TransactionUtil.commit(beginTransaction);

                }
            } catch (GenericTransactionException e1) {
                e1.printStackTrace();
            }
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;

		/*
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
		}*/

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
        String partyId = (String) context.get("partyId");
        // 是否启用状态
        String isUse = (String) context.get("isUse");
        try {
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("statusId", isUse);
            EntityCondition updateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyId);
            delegator.storeByCondition("Party", updateFields, updateCon);
        } catch (Exception e) {
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
     * 商家拒绝意见查询
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessRefuseReason(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 参数
        String partyId = (String) context.get("partyId");

        try {
            GenericValue party = delegator.findByAnd("PartyBusinessAudit", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("auditContent", party.get("auditContent"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 商家导入
     *
     * @param request
     * @param response
     * @return
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

    /**
     * 商家审核通过
     *
     * @return
     */
    public static Map<String, Object> businessAuditPass(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();

        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String settingType = (String) context.get("settingType");
        String daysNum = (String) context.get("daysNum");
        String commission = (String) context.get("commission");
        String partyIds = (String) context.get("partyId");

        List<String> partyIdList = StringUtil.split(partyIds, ",");
        for (String partyId : partyIdList) {
            boolean beginTransaction = false;
            try {
                beginTransaction = TransactionUtil.begin();
                GenericValue partyGroup = delegator.findByAnd("PartyGroup", UtilMisc.toMap("partyId", partyId)).get(0);

                GenericValue partyBusiness = delegator.findByAnd("PartyBusiness", UtilMisc.toMap("partyId", partyId)).get(0);


                //设置审批状态为审批通过
                Map<String, Object> updateFields = FastMap.newInstance();
                updateFields.put("auditStatus", "1");
                EntityCondition updateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyId);
                delegator.storeByCondition("PartyBusinessAudit", updateFields, updateCon);

                //创建店铺,并且设置partyGroup表中的店铺id
                Map<String, Object> storeInfo = dispatcher.runSync("createProductStore", UtilMisc.toMap("userLogin", userLogin, "ownerPartyId", partyId, "storeName", partyGroup.getString("partyName") + "店铺"));
                if (ServiceUtil.isError(storeInfo)) {
                    Debug.logError(ServiceUtil.getErrorMessage(storeInfo), module);
                    return storeInfo;
                }
                String productStoreId = (String) storeInfo.get("productStoreId");
                Map<String, Object> partyGroupUpdateFields = FastMap.newInstance();
                partyGroupUpdateFields.put("productStoreId", productStoreId);
                EntityCondition partyGroupUpdateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyId);
                delegator.storeByCondition("PartyGroup", partyGroupUpdateFields, partyGroupUpdateCon);

                //创建仓库
                Map<String, Object> facilityInfo = dispatcher.runSync("createFacility", UtilMisc.toMap("userLogin", userLogin, "facilityName", partyGroup.getString("partyName") + "仓库",
                        "facilityTypeId", "WAREHOUSE", "ownerPartyId", partyId, "defaultInventoryItemTypeId", "NON_SERIAL_INV_ITEM", "defaultDaysToShip", "2"));
                if (ServiceUtil.isError(facilityInfo)) {
                    Debug.logError(ServiceUtil.getErrorMessage(facilityInfo), module);
                    return facilityInfo;
                }
                String facilityId = (String) facilityInfo.get("facilityId");
                //创建仓库和店铺的关联 createProductStoreFacility
                Map<String, Object> facilityStoreInfo = dispatcher.runSync("createProductStoreFacility", UtilMisc.toMap("userLogin", userLogin,
                        "productStoreId", productStoreId, "facilityId", facilityId, "fromDate", new Timestamp(System.currentTimeMillis())));
                if (ServiceUtil.isError(facilityStoreInfo)) {
                    Debug.logError(ServiceUtil.getErrorMessage(facilityStoreInfo), module);
                    return facilityStoreInfo;
                }

                //创建结算信息录入
                String settingId = delegator.getNextSeqId("PartySettleSetting");
                String partyBusinessAuditId = "";//获取审批的id
                GenericValue partyBusinessAudit = delegator.findByAnd("PartyBusinessAudit", UtilMisc.toMap("partyId", partyId)).get(0);
                partyBusinessAuditId = partyBusinessAudit.getString("partyBusinessAuditId");
                GenericValue partySettleSetting = delegator.makeValue("PartySettleSetting", UtilMisc.toMap("partyId", partyId, "partyBusinessAuditId", partyBusinessAuditId, "settingId", settingId,
                        "createDate", new Timestamp(System.currentTimeMillis()), "settingType", settingType, "daysNum", new Long(daysNum), "commission", new BigDecimal(commission), "statusId", "1"));
                partySettleSetting.create();
                //设置商家的品牌审核状态通过
                Map<String, Object> partyBrandUpdateFields = FastMap.newInstance();
                partyBrandUpdateFields.put("auditStatus", "1");
                EntityCondition partyBrandUpdateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyId);
                delegator.storeByCondition("PartyBusinessBrand", partyBrandUpdateFields, partyBrandUpdateCon);

                //给用户的会员添加角色权限
                //根据商家的partyId获取会员的partyId，再获取会员的userLoginId.
                GenericValue partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdFrom", partyId)).get(0);
                String partyIdTo = partyRelationship.getString("partyIdTo");
                GenericValue userLoginInfo = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyIdTo)).get(0);
                String userLoginId = userLoginInfo.getString("userLoginId");

                GenericValue userLoginSecurityGroup = delegator.makeValue("UserLoginSecurityGroup");
                userLoginSecurityGroup.put("userLoginId",userLoginId);
                userLoginSecurityGroup.put("groupId","SUPPLIER");
                userLoginSecurityGroup.put("fromDate", new Timestamp(System.currentTimeMillis()));
                userLoginSecurityGroup.create();

                //修改绑定的会员类型为商家
                updateFields = FastMap.newInstance();
                updateFields.put("partyCategory", "BUSINESS");
                EntityCondition UpdateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyIdTo);
                delegator.storeByCondition("Party", updateFields, UpdateCon);

            } catch (Exception e) {
                try {
                    if(beginTransaction){
                        TransactionUtil.commit(beginTransaction);
                    }
                } catch (GenericTransactionException e1) {
                    e1.printStackTrace();
                }
                return ServiceUtil.returnError(e.getMessage());
            }

        }

        return result;
    }

    /**
     * 商家审核不通过
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessAuditNoPass(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();

        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String refuseReason = (String) context.get("refuseReason");
        String partyIds = (String) context.get("partyId");
        List<String> partyIdList = StringUtil.split(partyIds, ",");
        for (String partyId : partyIdList) {
            try {
                Map<String, Object> updateFields = FastMap.newInstance();
                updateFields.put("auditStatus", "2");
                updateFields.put("auditContent", refuseReason);
                EntityCondition updateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyId);
                delegator.storeByCondition("PartyBusinessAudit", updateFields, updateCon);
            } catch (Exception e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return result;
    }

    /**
     * 回显商家信息
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessEditDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 参数
        String partyId = (String) context.get("partyId");
        try {
            GenericValue party = delegator.findByAnd("Party", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("party", party);

            GenericValue partyGroup = delegator.findByAnd("PartyGroup", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("partyGroup", partyGroup);

            GenericValue partyBusiness = delegator.findByAnd("PartyBusiness", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("partyBusiness", partyBusiness);

            GenericValue partyBusinessAudit = delegator.findByAnd("PartyBusinessAudit", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("partyBusinessAudit", partyBusinessAudit);

            List<GenericValue> partyProductCategory = delegator.findByAnd("PartyProductCategory", UtilMisc.toMap("partyId", partyId));
            result.put("partyProductCategory", partyProductCategory);

            List<GenericValue> partyBusinessBrands = delegator.findByAnd("PartyBusinessBrand", UtilMisc.toMap("partyId", partyId,"auditStatus","1","isDel","N"));
            List<Map> partyBusinessBrand = FastList.newInstance();
            for (GenericValue brand : partyBusinessBrands) {
                String productBrandId = brand.getString("productBrandId");
                //获取brandName
                String brandName = (String) delegator.findByAnd("ProductBrand", UtilMisc.toMap("productBrandId", productBrandId)).get(0).get("brandName");
                partyBusinessBrand.add(UtilMisc.toMap("productBrandId", productBrandId, "brandName", brandName));
            }
            result.put("partyBusinessBrand", partyBusinessBrand);

            GenericValue partyRelationShip = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdFrom", partyId)).get(0);
            result.put("partyRelationShip", partyRelationShip);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    /**
     * 重新提交审核
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> commitAudit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 参数
        String partyId = (String) context.get("partyId");
        try {
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("auditStatus", "0");
            EntityCondition updateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyId);
            delegator.storeByCondition("PartyBusinessAudit", updateFields, updateCon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取商家结算信息
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getSettleSettingInfo(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // 参数
        String partyId = (String) context.get("partyId");
        try {
            GenericValue partySettleSetting = delegator.findByAnd("PartySettleSetting", UtilMisc.toMap("partyId", partyId)).get(0);
            result.put("partySettleSetting", partySettleSetting);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 修改商家结算信息
     *
     * @return
     */
    public static Map<String, Object> updateBusinessSettle(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        String settingType = (String) context.get("settingType");
        String daysNum = (String) context.get("daysNum");
        String commission = (String) context.get("commission");
        String partyId = (String) context.get("partyId");

        try {
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("settingType", settingType);
            updateFields.put( "daysNum", new Long(daysNum));
            updateFields.put( "commission", new BigDecimal(commission));
            EntityCondition updateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyId);
            delegator.storeByCondition("PartySettleSetting", updateFields, updateCon);

        } catch (Exception e) {
            return ServiceUtil.returnError(e.getMessage());
        }


        return result;
    }

}
