package com.qihua.ofbiz.business;

import com.qihua.ofbiz.common.ExcelUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class BusinessCommonServices {
    public static final String module = BusinessCommonServices.class.getName();
    /**
     * 获取产品目录树结构json
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getCategoryTree(DispatchContext dctx, Map<String, ? extends Object> context) {

        Delegator delegator = dctx.getDelegator();
        List<Map> productCategoryList = new ArrayList<Map>();
        try {
            List<GenericValue> partyGroups = delegator.findByAnd("PartyGroup",UtilMisc.toMap("isInner","Y"));
            String productStoreId = partyGroups.get(0).getString("productStoreId");

            EntityCondition mainCond1 = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                            EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N")
                    )
                    , EntityOperator.OR);
            EntityCondition mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("productCategoryLevel", EntityOperator.EQUALS, 1L),
                            mainCond1,
                            EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId)
                    )
                    , EntityOperator.AND);
            List<GenericValue> categoryList = delegator.findList("ProductCategory",mainCond,null,null,null,false);
//            List<GenericValue> categoryList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId",null,"isDel",null,"productStoreId",productStoreId));
            if(categoryList!=null&&categoryList.size()>0){
                for(GenericValue category01:categoryList){
                    String productCategory01Id = category01.getString("productCategoryId");
                    String categoryName = category01.getString("categoryName");
                    Map category = UtilMisc.toMap("open",true,"categoryId",productCategory01Id,"categoryName",categoryName,"parentCategoryId","","name",categoryName);

                    List<Map> productCategory02List = new ArrayList<Map>();
                    mainCond1 = EntityCondition.makeCondition(
                            UtilMisc.toList(
                                    EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                                    EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N")
                            )
                            , EntityOperator.OR);
                    mainCond = EntityCondition.makeCondition(
                            UtilMisc.toList(
                                    EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.EQUALS, productCategory01Id),
                                    mainCond1,
                                    EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId)
                            )
                            , EntityOperator.AND);
                    List<GenericValue> categoryList02 = delegator.findList("ProductCategory",mainCond,null,null,null,false);
//                    List<GenericValue> categoryList02 = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId",productCategory01Id,"isDel",null,"productStoreId",productStoreId));
                    for(GenericValue category02:categoryList02){
                        String productCategory02Id = category02.getString("productCategoryId");
                        String categoryName02 = category02.getString("categoryName");
                        Map categoryMap02 = UtilMisc.toMap("open",true,"categoryId",productCategory02Id,"categoryName",categoryName02,"parentCategoryId",productCategory01Id,"name",categoryName02);

                        List<Map> productCategory03List = new ArrayList<Map>();
                        mainCond1 = EntityCondition.makeCondition(
                                UtilMisc.toList(
                                        EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                                        EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N")
                                )
                                , EntityOperator.OR);
                        mainCond = EntityCondition.makeCondition(
                                UtilMisc.toList(
                                        EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.EQUALS, productCategory02Id),
                                        mainCond1,
                                        EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId)
                                )
                                , EntityOperator.AND);
                        List<GenericValue> categoryList03 = delegator.findList("ProductCategory",mainCond,null,null,null,false);
//                        List<GenericValue> categoryList03 = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId",productCategory02Id,"isDel",null,"productStoreId",productStoreId));
                        for(GenericValue category03:categoryList03){
                            String productCategory03Id = category03.getString("productCategoryId");
                            String categoryName03 = category03.getString("categoryName");
                            Map categoryMap03 = UtilMisc.toMap("open",true,"categoryId",productCategory03Id,"categoryName",categoryName03,"parentCategoryId",productCategory02Id,"name",categoryName03);
                            productCategory03List.add(categoryMap03);
                        }

                        categoryMap02.put("children",productCategory03List);
                        productCategory02List.add(categoryMap02);
                    }

                    category.put("children",productCategory02List);
                    productCategoryList.add(category);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        Map map = new HashMap();
        map.put("categoryList",productCategoryList);

        return map;

    }

    /**
     * 根据code得出省市区的中文名称
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getGeoNameById(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        String geoId = (String)context.get("geoId");
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = dctx.getDelegator();
        GenericValue geo = delegator.findByAnd("Geo",UtilMisc.toMap("geoId",geoId)).get(0);
        String geoName = geo.getString("geoName");
        result.put("geoName",geoName);
        return result;
    }

    /**
     * 获取所有的品牌列表
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> getAllBusinessBrandList(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
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
        //品牌表
        dynamicView.addMemberEntity("PBR", "ProductBrand");
        dynamicView.addAlias("PBR", "productBrandId");
//        dynamicView.addAlias("PBR", "logoImg");
        dynamicView.addAlias("PBR", "contentId");
        dynamicView.addAlias("PBR", "brandName");
        dynamicView.addAlias("PBR", "brandNameAlias");
        dynamicView.addAlias("PBR", "brandDesc");
        dynamicView.addAlias("PBR", "isDel");
        dynamicView.addAlias("PBR", "createdStamp");

        fieldsToSelect.add("productBrandId");
//        fieldsToSelect.add("logoImg");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("brandDesc");
        fieldsToSelect.add("createdStamp");

        //品牌名称 模糊查询
        if (UtilValidate.isNotEmpty(context.get("brandName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("brandName"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("brandName") + "%")));
        }

        filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isDel"), EntityOperator.EQUALS, "N"));

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


    public static void uploadPartyContentJava(HttpServletRequest request, HttpServletResponse response) {
        //返回信息，json格式
        String returnJson = "";
        //当前用户
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        //Delegator对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //LocalDispatcher对象
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            //调用Excel导入方法
            Map rs = dispatcher.runSync("createContentFromUploadedFile", UtilMisc.toMap());
            System.out.println(rs.toString());
        }catch (Exception e){

        }

    }

}
