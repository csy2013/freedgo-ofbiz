package com.qihua.ofbiz.product.promo;

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
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 活动管理 service
 * @author qianjin 2016.02.15
 *
 */
public class ProActivityMgrServices {
    public static final String module = ProActivityMgrServices.class.getName();
    public static final String resource = "ProdPromoUiLabels";
    
    /**
     * 活动管理列表查询 add by qianjin 2016.02.15
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getActivityMgrList(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //记录集合
        List<GenericValue> recordsList = FastList.newInstance();
        List<Map> pamcList = FastList.newInstance();
        //总记录数
        int totalSize = 0;
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

        String productStoreId = (String) context.get("productStoreId"); // 店铺编码

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("PAM","ProductActivityManager");
        dynamicView.addAlias("PAM","productActivityManagerId");
        dynamicView.addAlias("PAM","templateId");
        dynamicView.addAlias("PAM","activityManagerName");
        dynamicView.addAlias("PAM", "productStoreId");
        dynamicView.addAlias("PAM", "createdStamp");
        dynamicView.addAlias("PAM", "sequenceId");
        dynamicView.addAlias("PAM", "fromDate");
        dynamicView.addAlias("PAM", "thruDate");
        dynamicView.addAlias("PAM", "isUsed");

        fieldsToSelect.add("productActivityManagerId");
        fieldsToSelect.add("templateId");
        fieldsToSelect.add("activityManagerName");
        fieldsToSelect.add("productStoreId");
        fieldsToSelect.add("createdStamp");
        fieldsToSelect.add("sequenceId");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("thruDate");
        fieldsToSelect.add("isUsed");

        
        //按活动管理名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("activityManagerName"))) {
        	filedExprs.add(EntityCondition.makeCondition("activityManagerName", EntityOperator.LIKE, EntityFunction.UPPER("%"+context.get("activityManagerName")+"%")));
        }

        // 店铺信息
        if(UtilValidate.isNotEmpty(productStoreId)){
            filedExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        }

        //排序字段名称
        String sortField = "sequenceId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
        	sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
        	sortType = (String)context.get("sortType");
        }
        orderBy.add(sortType+sortField);
        
        //添加where条件
        if (filedExprs.size() > 0){
        	mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }
        
        try {
        	lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            recordsList = pli.getPartialList(lowIndex, viewSize);

            for(GenericValue gv : recordsList){
                String productActivityManagerId = gv.getString("productActivityManagerId");// 专题编码
                String activityManagerName = gv.getString("activityManagerName");
                String sequenceId = gv.getString("sequenceId");
                String createdStamp = gv.getString("createdStamp");
                String isUsed = gv.getString("isUsed");
                String fromDate = gv.getString("fromDate");
                String thruDate = gv.getString("thruDate");

                Map map = FastMap.newInstance();
                map.put("productActivityManagerId", productActivityManagerId);
                map.put("activityManagerName", activityManagerName);
                map.put("sequenceId", sequenceId);
                map.put("createdStamp", createdStamp);
                map.put("isUsed", isUsed);
                map.put("fromDate", fromDate);
                map.put("thruDate", thruDate);

                List<GenericValue> subjectColumnInfos=FastList.newInstance();
                String curColumnName="";
                //动态view
                DynamicViewEntity subjectColumn_dv = new DynamicViewEntity();

                subjectColumn_dv.addMemberEntity("SCA","SubjectColumnActivityAssoc");
                subjectColumn_dv.addAlias("SCA","subjectColumnId");
                subjectColumn_dv.addAlias("SCA","productActivityManagerId");

                subjectColumn_dv.addMemberEntity("SC","SubjectColumn");
                subjectColumn_dv.addAlias("SC","subjectColumnId");
                subjectColumn_dv.addAlias("SC","columnName");


                subjectColumn_dv.addViewLink("SCA","SC", Boolean.FALSE,ModelKeyMap.makeKeyMapList("subjectColumnId", "subjectColumnId"));

                List<String> subcol_fieldsToSelect = FastList.newInstance();
                subcol_fieldsToSelect.add("productActivityManagerId");
                subcol_fieldsToSelect.add("subjectColumnId");
                subcol_fieldsToSelect.add("columnName");

                //编辑where条件
                EntityCondition subcol_whereCond = EntityCondition.makeCondition("productActivityManagerId", EntityOperator.EQUALS,productActivityManagerId);
                try {
                    //查询的数据Iterator

                    EntityListIterator subcol_pli = delegator.findListIteratorByCondition(subjectColumn_dv, subcol_whereCond, null, subcol_fieldsToSelect, null, findOpts);

                    for(int i=0;i<subcol_pli.getCompleteList().size();i++){
                        String curColName = subcol_pli.getCompleteList().get(i).getString("columnName");
                        if(i == 0){
                            curColumnName += curColName;
                        }else{
                            curColumnName += "，"+curColName;
                        }
                    }
                    //关闭pli
                    subcol_pli.close();
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Cannot lookup State Column: " + e.toString(), module);
                }
                map.put("columnName", curColumnName);
                pamcList.add(map);
            }
            // 获取总记录数
            totalSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > totalSize) {
                highIndex = totalSize;
            }

            //关闭 iterator
            pli.close();
		} catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }
        
        //返回的参数
        result.put("recordsList",pamcList);
        result.put("totalSize", Integer.valueOf(totalSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        
        return result;
    }
    
//    /**
//     * 活动管理新增	add by qianjin 2016.02.16
//     * @param dctx
//     * @param context
//     * @return
//     */
//    public static Map<String, Object> activityMgrAdd(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
//    	Map<String,Object> result = ServiceUtil.returnSuccess();
//    	//delegator对象
//    	Delegator delegator = dctx.getDelegator();
//    	//LocalDispatcher对象
//        LocalDispatcher dispatcher = dctx.getDispatcher();
//        //userLogin对象
//        GenericValue userLogin = (GenericValue) context.get("userLogin");
//        //参数
//        String activityManagerName = (String)context.get("activityManagerName");
//        String templateId = (String)context.get("templateId");
//        String activityManagerText = (String)context.get("activityManagerText");
//        String productStoreId =(String)context.get("productStoreId");
//        String promos = (String)context.get("promos");
//
//        String textContentId="";
//		 if(UtilValidate.isNotEmpty(activityManagerText) && "1".equals(templateId)){
//			 Map<String, Object> passedParams = UtilMisc.toMap( "dataResourceTypeId", "ELECTRONIC_TEXT","dataTemplateTypeId","NONE",  "contentPurposeTypeId","ARTICLE", "textData",activityManagerText
//					,"statusId", "CTNT_INITIAL_DRAFT","userLogin", userLogin,"contentAssocTypeId","SUB_CONTENT");
//			 try {
//				Map<String,Object> content_result = dispatcher.runSync("createTextContent", passedParams);
//				if(UtilValidate.isNotEmpty(content_result)){
//					textContentId = (String)content_result.get("contentId");
//				}
//			 }
//			 catch (GenericServiceException e) {
//				e.printStackTrace();
//			 }
//		}
//
//		String activityMgrId = delegator.getNextSeqId("ProductActivityManager");
//		//新增一条活动管理记录
//		GenericValue activityMgr_gv = delegator.makeValue("ProductActivityManager", UtilMisc.toMap("productActivityManagerId",activityMgrId));
//		activityMgr_gv.setString("templateId", templateId);
//		activityMgr_gv.setString("activityManagerName", activityManagerName);
//		if(UtilValidate.isNotEmpty(textContentId)){
//			activityMgr_gv.setString("activityManagerText", textContentId);
//		}
//
//        if(UtilValidate.isNotEmpty(productStoreId)){
//            activityMgr_gv.setString("productStoreId", productStoreId);
//        }
//		activityMgr_gv.create();
//
//		//创建活动管理的关联记录
//		JSONArray promos_arr = JSONArray.fromObject(promos);
//		if(!promos_arr.isEmpty()){
//			for(Object obj : promos_arr){
//				JSONObject promo = JSONObject.fromObject(obj);
//				//新的ID
//				String activityMgrAssocId = delegator.getNextSeqId("ProductActivityManagerAssoc");
//				GenericValue activityMgrAssoc_gv = delegator.makeValue("ProductActivityManagerAssoc", UtilMisc.toMap("productActivityManagerAssocId",activityMgrAssocId));
//				activityMgrAssoc_gv.setString("productActivityManagerId",activityMgrId);
//				//判断活动ID是否为空
//				if(UtilValidate.isNotEmpty(promo.getString("promoId"))){
//					activityMgrAssoc_gv.setString("productActivityId",promo.getString("promoId"));
//				}
//				//判断图片ID是否为空
//				if(UtilValidate.isNotEmpty(promo.getString("imgContentId"))){
//					activityMgrAssoc_gv.setString("imgContentId",promo.getString("imgContentId"));
//				}
//				//判断排序号是否为空
//				if(UtilValidate.isNotEmpty(promo.getString("sequenceId"))){
//					activityMgrAssoc_gv.setString("sequenceId", promo.getString("sequenceId"));
//				}
//				activityMgrAssoc_gv.create();
//			}
//		}
//        return result;
//    }

    /**
     * 促销专题	add by zhajh 2018.04.27
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> activityMgrAdd(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        //delegator对象
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //userLogin对象
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //参数
        String activityManagerName = (String)context.get("activityManagerName");
        String sequenceId = (String)context.get("sequenceId");
        String productStoreId =(String)context.get("productStoreId");
        String colInfos = (String)context.get("colInfos");
        String contentId = (String)context.get("contentId");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String isUsed = (String)context.get("isUsed");

        List<EntityCondition> andExprs = FastList.newInstance();

        EntityCondition mainCond = null;
        String activityMgrId = delegator.getNextSeqId("ProductActivityManager");
        //新增一条活动管理记录
        GenericValue activityMgr_gv = delegator.makeValue("ProductActivityManager", UtilMisc.toMap("productActivityManagerId",activityMgrId));
        activityMgr_gv.setString("activityManagerName", activityManagerName);
        activityMgr_gv.set("fromDate", fromDate);
        activityMgr_gv.set("thruDate", thruDate);
        activityMgr_gv.set("isUsed", isUsed);
        if(UtilValidate.isNotEmpty(contentId)){
            activityMgr_gv.setString("contentId", contentId);
        }
        if(UtilValidate.isNotEmpty(sequenceId)){
            andExprs.add(EntityCondition.makeCondition("sequenceId", EntityOperator.GREATER_THAN_EQUAL_TO, Long.parseLong(sequenceId)));
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                //去除重复数据
                EntityListIterator pli=delegator.find("ProductActivityManager",mainCond,null,null,null,null);
                List<GenericValue> pamForSeq = pli.getCompleteList();
                if(UtilValidate.isNotEmpty(pamForSeq)){
                    for(GenericValue pamInfo:pamForSeq){
                        String curSequenceId =pamInfo.getString("sequenceId");
                        Long curSequenceNum=Long.valueOf(curSequenceId)+Long.valueOf("1");
                        pamInfo.setString("sequenceId",curSequenceNum.toString());
                        pamInfo.store();
                    }
                }
                pli.close();
            }
            activityMgr_gv.setString("sequenceId", sequenceId);
        }
        if(UtilValidate.isNotEmpty(productStoreId)){
            activityMgr_gv.setString("productStoreId", productStoreId);
        }
        activityMgr_gv.create();
        // 取得栏目信息
        List<Map<String, Object>> colInfoList = FastList.newInstance();//栏目列表
        if(UtilValidate.isNotEmpty(colInfos)){
            String[] tColInfosArray = colInfos.split("\\|");
            Map<String,Object> mapTemp=FastMap.newInstance();
            for (String attrInfo : tColInfosArray) {
                mapTemp=FastMap.newInstance();
                if(UtilValidate.isNotEmpty(attrInfo)) {
                    String[] attrInfos = attrInfo.split("\\^");
                    String colName = attrInfos[0];//栏目名称
                    String colProductIds = attrInfos[1];//关联商品信息
                    if(UtilValidate.isNotEmpty(colProductIds)){
                        String[] productInfos = colProductIds.split(",");
                        if(UtilValidate.isNotEmpty(productInfos)){
                            List<String> productIds=FastList.newInstance();
                            for(String curProductId:productInfos){
                                productIds.add(curProductId);
                            }
                            mapTemp = FastMap.newInstance();
                            mapTemp.put("colName", colName);
                            mapTemp.put("productIds", productIds);
                            colInfoList.add(mapTemp);
                        }
                    }
                }
            }
        }

        // 创建栏目信息
        if(UtilValidate.isNotEmpty(colInfoList)){
            for(Map<String,Object> curColInfo:colInfoList){

                //新的ID
                String subjectColumnId = delegator.getNextSeqId("SubjectColumn");
                GenericValue subjectColumn_gv = delegator.makeValue("SubjectColumn", UtilMisc.toMap("subjectColumnId",subjectColumnId));
                subjectColumn_gv.setString("columnName",(String)curColInfo.get("colName"));
                subjectColumn_gv.create();
                List<String> productIds=FastList.newInstance();
                productIds=(List<String>)curColInfo.get("productIds");
                if(UtilValidate.isNotEmpty(productIds)){
                    for(String id:productIds){
                        // 创建栏目和商品的关系信息
                        String subjectColumnProductAssocId = delegator.getNextSeqId("SubjectColumnProductAssoc");
                        GenericValue subjectColumnProductAssoc_gv = delegator.makeValue("SubjectColumnProductAssoc", UtilMisc.toMap("subjectColumnProductAssocId",subjectColumnProductAssocId));
                        subjectColumnProductAssoc_gv.setString("subjectColumnId",subjectColumnId);
                        subjectColumnProductAssoc_gv.setString("productId",id);
                        subjectColumnProductAssoc_gv.create();
                    }
                }
                // 创建促销专题和栏目的关系
                String subjectColumnActivityAssocId = delegator.getNextSeqId("SubjectColumnActivityAssoc");
                GenericValue subjectColumnActivityAssoc_gv = delegator.makeValue("SubjectColumnActivityAssoc", UtilMisc.toMap("subjectColumnActivityAssocId",subjectColumnActivityAssocId));
                subjectColumnActivityAssoc_gv.setString("subjectColumnId",subjectColumnId);
                subjectColumnActivityAssoc_gv.setString("productActivityManagerId",activityMgrId);
                subjectColumnActivityAssoc_gv.create();
            }
        }

        return result;
    }
    
//    /**
//     * 根据ID查询活动管理	add by qianjin 2016.02.18
//     * @param dctx
//     * @param context
//     * @return
//     */
//    public static Map<String, Object> getActivityMgrById(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
//    	Map<String,Object> result = ServiceUtil.returnSuccess();
//    	//delegator对象
//    	Delegator delegator = dctx.getDelegator();
//    	//LocalDispatcher对象
//        LocalDispatcher dispatcher = dctx.getDispatcher();
//        //userLogin对象
//        GenericValue userLogin = (GenericValue) context.get("userLogin");
//        //参数
//        String activityManagerId = (String)context.get("activityManagerId");
//
//        //获取活动管理数据
//        GenericValue activityMgr_gv = delegator.findByPrimaryKey("ProductActivityManager", UtilMisc.toMap("productActivityManagerId",activityManagerId));
//        String templateId = activityMgr_gv.getString("templateId");					 //模板ID
//        String activityManagerName = activityMgr_gv.getString("activityManagerName");//活动名称
//        String activityManagerText = activityMgr_gv.getString("activityManagerText");//正文ContentId
//        //根据contentId获取正文内容
//        if(UtilValidate.isNotEmpty(activityManagerText)){
//        	GenericValue Content=delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId",activityManagerText));
//	    	 if(UtilValidate.isNotEmpty(Content))
//	    	 {
//	    		 GenericValue Contenttext=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",Content.get("dataResourceId")));
//	    		 if(UtilValidate.isNotEmpty(Contenttext))
//	 			 {
//	    			 activityManagerText = Contenttext.getString("textData");
//	 			 }
//	    	 }
//        }else{
//        	activityManagerText = "";
//        }
//
//        List<Map> promosList = FastList.newInstance();
//        //获取活动管理的促销活动列表
//        List<GenericValue> pama_list = delegator.findByAnd("ProductActivityManagerAssoc", UtilMisc.toMap("productActivityManagerId",activityManagerId));
//        for(GenericValue pama_gv : pama_list){
//        	Map record = FastMap.newInstance();
//        	String productActivityId = UtilValidate.isEmpty(pama_gv.getString("productActivityId")) ? "" : pama_gv.getString("productActivityId");	//活动ID
//        	String imgContentId = pama_gv.getString("imgContentId");			//图片ID
//        	String sequenceId = pama_gv.getString("sequenceId");				//排序号
//        	String activityName = "";		//活动名称
//        	String activityTypeName = "";	//活动类型名称
//        	String communityName = "";		//社区
//        	String activityType = "";
//        	//根据活动ID查询活动详情
//        	if(UtilValidate.isNotEmpty(productActivityId)){
//        		//查询活动详情
//        		GenericValue activity_gv = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId",productActivityId));
//        		activityName = activity_gv.getString("activityName");	//活动名称
//        		activityType = activity_gv.getString("activityType");	//活动类型
//        		//查询活动类型
//        		GenericValue enum_gv = delegator.findByPrimaryKey("Enumeration",  UtilMisc.toMap("enumId",activity_gv.getString("activityType")));
//        		activityTypeName = enum_gv.getString("description");	//活动类型名称
//        		//查询活动的覆盖社区
//        		List<GenericValue> areaList = delegator.findByAnd("ProductActivityArea", UtilMisc.toMap("activityId", productActivityId));
//                if (UtilValidate.isNotEmpty(areaList)) {
//                    for (int i = 0; i < areaList.size(); i++) {
//                        String name = areaList.get(i).getString("communityName");
//                        if (i == 0) {
//                            communityName += name;
//                        } else {
//                            communityName += "，" + name;
//                        }
//                    }
//                } else {
//                    communityName = "全部社区";
//                }
//        	}
//
//        	record.put("productActivityId",productActivityId);
//        	record.put("imgContentId",imgContentId);
//        	record.put("sequenceId",sequenceId);
//        	record.put("activityName",activityName);
//        	record.put("activityTypeName",activityTypeName);
//        	record.put("communityName",communityName);
//        	record.put("activityType",activityType);
//        	promosList.add(record);
//        }
//
//        result.put("activityManagerId",activityManagerId);
//        result.put("templateId",templateId);
//        result.put("activityManagerName",activityManagerName);
//        result.put("activityManagerText",activityManagerText);
//        result.put("promosList",promosList);
//        return result;
//    }

    /**
     * 根据ID查询促销专题	add by zhajh 2018.04.28
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getActivityMgrById(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        //delegator对象
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //userLogin对象
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //参数
        String activityManagerId = (String)context.get("activityManagerId");

        //获取活动管理数据
        GenericValue activityMgr_gv = delegator.findByPrimaryKey("ProductActivityManager", UtilMisc.toMap("productActivityManagerId",activityManagerId));

        // 取得栏目信息
        List<GenericValue> subjectColumnInfos=delegator.findByAnd("GetColumnInfoByProductActivityManagerId", UtilMisc.toMap("productActivityManagerId",activityManagerId));

        List<Map<String,Object>> columnMapList=FastList.newInstance();
        Map<String,Object> curColumnMap=FastMap.newInstance();
        if(UtilValidate.isNotEmpty(subjectColumnInfos)){
            List<Map<String,Object>> productList=FastList.newInstance();
            for(GenericValue scInfo:subjectColumnInfos){
                productList=FastList.newInstance();
                curColumnMap=FastMap.newInstance();
                curColumnMap.put("columnName",scInfo.getString("columnName"));
                if(UtilValidate.isNotEmpty(scInfo)){
                    // 取得栏目管理商品信息
                    // 取得栏目信息
                    List<GenericValue> prodIds=delegator.findByAnd("SubjectColumnProductAssoc", UtilMisc.toMap("subjectColumnId",scInfo.getString("subjectColumnId")));

                    for (GenericValue prodInfo : prodIds) {
                        String productId = prodInfo.getString("productId");
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                        Map<String, Object> map = FastMap.newInstance();
                        map.put("productId", productId);// 商品编码
                        map.put("productName", product.get("productName"));// 商品名称
                        GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE")));
                        if (UtilValidate.isNotEmpty(defaultprice)) {
                            map.put("defaultprice", defaultprice.get("price")); // 销售价格
                        } else {
                            map.put("defaultprice", 0);
                        }
                        // 商品规格的取得
                        String featureInfo = "";// 商品规格
                        if (UtilValidate.isNotEmpty(productId)) {
                            if (UtilValidate.isNotEmpty(product)) {
                                if (UtilValidate.isNotEmpty(product.getString("mainProductId"))) {
                                    if (UtilValidate.isNotEmpty(product.getString("featureaProductId"))) {
                                        GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", product.getString("featureaProductId")));
                                        if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                                            GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
                                            if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                                                String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
                                                String productFeatureName = productFeatureInfo.getString("productFeatureName");
                                                featureInfo = productFeatureTypeName + ":" + productFeatureName;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        map.put("featureInfo", featureInfo);// 特征
                        // 取得商品图片
                        String imgUrl = "";// 商品图片
                        // 根据商品ID获取商品图片url
                        String productAdditionalImage1 = "";
                        List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                                UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                        if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                            imgUrl = "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId");
                        }
                        map.put("imgUrl", imgUrl);
                        productList.add(map);
                    }
                }
                curColumnMap.put("productList", productList);
                columnMapList.add(curColumnMap);
            }
        }

        result.put("activityMgrInfo",activityMgr_gv);
        result.put("columnMapList",columnMapList);
        return result;
    }
    
//    /**
//     * 活动管理修改	add by qianjin 2016.02.18
//     * @param dctx
//     * @param context
//     * @return
//     */
//    public static Map<String, Object> activityMgrEdit(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
//    	Map<String,Object> result = ServiceUtil.returnSuccess();
//    	//delegator对象
//    	Delegator delegator = dctx.getDelegator();
//    	//LocalDispatcher对象
//        LocalDispatcher dispatcher = dctx.getDispatcher();
//        //userLogin对象
//        GenericValue userLogin = (GenericValue) context.get("userLogin");
//        //参数
//        String activityManagerId = (String)context.get("activityManagerId");
//        String activityManagerName = (String)context.get("activityManagerName");
//        String templateId = (String)context.get("templateId");
//        String activityManagerText = (String)context.get("activityManagerText");
//        String promos = (String)context.get("promos");
//
//        String textContentId="";
//		 if(UtilValidate.isNotEmpty(activityManagerText) && "1".equals(templateId)){
//			 Map<String, Object> passedParams = UtilMisc.toMap( "dataResourceTypeId", "ELECTRONIC_TEXT","dataTemplateTypeId","NONE",  "contentPurposeTypeId","ARTICLE", "textData",activityManagerText
//					,"statusId", "CTNT_INITIAL_DRAFT","userLogin", userLogin,"contentAssocTypeId","SUB_CONTENT");
//			 try {
//				Map<String,Object> content_result = dispatcher.runSync("createTextContent", passedParams);
//				if(UtilValidate.isNotEmpty(content_result)){
//					textContentId = (String)content_result.get("contentId");
//				}
//			 }
//			 catch (GenericServiceException e) {
//				e.printStackTrace();
//			 }
//		}
//
//		//根据ID查询活动管理记录
//		GenericValue activityMgr_gv = delegator.findByPrimaryKey("ProductActivityManager", UtilMisc.toMap("productActivityManagerId",activityManagerId));
//		activityMgr_gv.setString("templateId", templateId);
//		activityMgr_gv.setString("activityManagerName", activityManagerName);
//		if(UtilValidate.isEmpty(textContentId)){
//			activityMgr_gv.set("activityManagerText", null);
//		}else{
//			activityMgr_gv.set("activityManagerText", textContentId);
//		}
//		activityMgr_gv.store();
//
//		//删除活动管理的关联记录
//		delegator.removeByAnd("ProductActivityManagerAssoc", UtilMisc.toMap("productActivityManagerId",activityManagerId));
//
//		//创建活动管理的关联记录
//		JSONArray promos_arr = JSONArray.fromObject(promos);
//		if(!promos_arr.isEmpty()){
//			for(Object obj : promos_arr){
//				JSONObject promo = JSONObject.fromObject(obj);
//				//新的ID
//				String activityMgrAssocId = delegator.getNextSeqId("ProductActivityManagerAssoc");
//				GenericValue activityMgrAssoc_gv = delegator.makeValue("ProductActivityManagerAssoc", UtilMisc.toMap("productActivityManagerAssocId",activityMgrAssocId));
//				activityMgrAssoc_gv.setString("productActivityManagerId",activityManagerId);
//				//判断活动ID是否为空
//				if(UtilValidate.isNotEmpty(promo.getString("promoId"))){
//					activityMgrAssoc_gv.setString("productActivityId",promo.getString("promoId"));
//				}
//				//判断图片ID是否为空
//				if(UtilValidate.isNotEmpty(promo.getString("imgContentId"))){
//					activityMgrAssoc_gv.setString("imgContentId",promo.getString("imgContentId"));
//				}
//				//判断排序号是否为空
//				if(UtilValidate.isNotEmpty(promo.getString("sequenceId"))){
//					activityMgrAssoc_gv.setString("sequenceId", promo.getString("sequenceId"));
//				}
//				activityMgrAssoc_gv.create();
//			}
//		}
//        return result;
//    }

    /**
     * 促销专题修改	add by zhajh 2018.04.28
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> activityMgrEdit(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        //delegator对象
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //userLogin对象
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //参数
        String activityManagerId = (String)context.get("activityManagerId");
        String activityManagerName = (String)context.get("activityManagerName");
        String sequenceId = (String)context.get("sequenceId");
        String productStoreId =(String)context.get("productStoreId");
        String colInfos = (String)context.get("colInfos");
        String contentId = (String)context.get("contentId");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String isUsed = (String)context.get("isUsed");
        List<EntityCondition> andExprs = FastList.newInstance();

        EntityCondition mainCond = null;
        //根据ID查询活动管理记录
        GenericValue activityMgr_gv = delegator.findByPrimaryKey("ProductActivityManager", UtilMisc.toMap("productActivityManagerId",activityManagerId));
        activityMgr_gv.set("fromDate", fromDate);
        activityMgr_gv.set("thruDate", thruDate);
        activityMgr_gv.set("isUsed", isUsed);
        if(UtilValidate.isNotEmpty(activityMgr_gv)){
            if(UtilValidate.isNotEmpty(contentId)){
                activityMgr_gv.setString("contentId", contentId);
            }
            if(UtilValidate.isNotEmpty(sequenceId)){
                andExprs.add(EntityCondition.makeCondition("sequenceId", EntityOperator.GREATER_THAN_EQUAL_TO, Long.parseLong(sequenceId)));
                andExprs.add(EntityCondition.makeCondition("productActivityManagerId", EntityOperator.NOT_EQUAL, activityManagerId));
                if (andExprs.size() > 0) {
                    mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                    //去除重复数据
                    EntityListIterator pli=delegator.find("ProductActivityManager",mainCond,null,null,null,null);
                    List<GenericValue> pamForSeq = pli.getCompleteList();
                    if(UtilValidate.isNotEmpty(pamForSeq)){
                        for(GenericValue pamInfo:pamForSeq){
                            String curSequenceId =pamInfo.getString("sequenceId");
                            Long curSequenceNum=Long.valueOf(curSequenceId)+Long.valueOf("1");
                            pamInfo.setString("sequenceId",curSequenceNum.toString());
                            pamInfo.store();
                        }
                    }
                    pli.close();
                }
                activityMgr_gv.setString("sequenceId", sequenceId);
            }
            if(UtilValidate.isNotEmpty(productStoreId)){
                activityMgr_gv.setString("productStoreId", productStoreId);
            }
            if(UtilValidate.isNotEmpty(activityManagerName)){
                activityMgr_gv.setString("activityManagerName", activityManagerName);
            }
            activityMgr_gv.store();
        }

        // 删除专题关联栏目信息
        List<GenericValue> subjectColumnActivityAssocInfos=delegator.findByAnd("SubjectColumnActivityAssoc", UtilMisc.toMap("productActivityManagerId",activityManagerId));
        if(UtilValidate.isNotEmpty(subjectColumnActivityAssocInfos)){
            for(GenericValue scaInfo:subjectColumnActivityAssocInfos){
                String curSubjectColumnId=scaInfo.getString("subjectColumnId");
                delegator.removeByAnd("SubjectColumn",UtilMisc.toMap("subjectColumnId",curSubjectColumnId));
                delegator.removeByAnd("SubjectColumnProductAssoc",UtilMisc.toMap("subjectColumnId",curSubjectColumnId));
            }
            delegator.removeAll(subjectColumnActivityAssocInfos);
        }

        // 取得栏目信息
        List<Map<String, Object>> colInfoList = FastList.newInstance();//栏目列表
        if(UtilValidate.isNotEmpty(colInfos)){
            String[] tColInfosArray = colInfos.split("\\|");
            Map<String,Object> mapTemp=FastMap.newInstance();
            for (String attrInfo : tColInfosArray) {
                mapTemp=FastMap.newInstance();
                if(UtilValidate.isNotEmpty(attrInfo)) {
                    String[] attrInfos = attrInfo.split("\\^");
                    String colName = attrInfos[0];//栏目名称
                    String colProductIds = attrInfos[1];//关联商品信息
                    if(UtilValidate.isNotEmpty(colProductIds)){
                        String[] productInfos = colProductIds.split(",");
                        if(UtilValidate.isNotEmpty(productInfos)){
                            List<String> productIds=FastList.newInstance();
                            for(String curProductId:productInfos){
                                productIds.add(curProductId);
                            }
                            mapTemp = FastMap.newInstance();
                            mapTemp.put("colName", colName);
                            mapTemp.put("productIds", productIds);
                            colInfoList.add(mapTemp);
                        }
                    }
                }
            }
        }

        // 创建栏目信息
        if(UtilValidate.isNotEmpty(colInfoList)){
            for(Map<String,Object> curColInfo:colInfoList){

                //新的ID
                String subjectColumnId = delegator.getNextSeqId("SubjectColumn");
                GenericValue subjectColumn_gv = delegator.makeValue("SubjectColumn", UtilMisc.toMap("subjectColumnId",subjectColumnId));
                subjectColumn_gv.setString("columnName",(String)curColInfo.get("colName"));
                subjectColumn_gv.create();
                List<String> productIds=FastList.newInstance();
                productIds=(List<String>)curColInfo.get("productIds");
                if(UtilValidate.isNotEmpty(productIds)){
                    for(String id:productIds){
                        // 创建栏目和商品的关系信息
                        String subjectColumnProductAssocId = delegator.getNextSeqId("SubjectColumnProductAssoc");
                        GenericValue subjectColumnProductAssoc_gv = delegator.makeValue("SubjectColumnProductAssoc", UtilMisc.toMap("subjectColumnProductAssocId",subjectColumnProductAssocId));
                        subjectColumnProductAssoc_gv.setString("subjectColumnId",subjectColumnId);
                        subjectColumnProductAssoc_gv.setString("productId",id);
                        subjectColumnProductAssoc_gv.create();
                    }
                }
                // 创建促销专题和栏目的关系
                String subjectColumnActivityAssocId = delegator.getNextSeqId("SubjectColumnActivityAssoc");
                GenericValue subjectColumnActivityAssoc_gv = delegator.makeValue("SubjectColumnActivityAssoc", UtilMisc.toMap("subjectColumnActivityAssocId",subjectColumnActivityAssocId));
                subjectColumnActivityAssoc_gv.setString("subjectColumnId",subjectColumnId);
                subjectColumnActivityAssoc_gv.setString("productActivityManagerId",activityManagerId);
                subjectColumnActivityAssoc_gv.create();
            }
        }
        return result;
    }
    
//    /**
//     * 根据ID删除活动管理,可批量删除
//     * @param dctx
//     * @param context
//     * @return
//     */
//    public static Map<String, Object> activityMgrDel(DispatchContext dctx, Map<String, ? extends Object> context) {
//    	Map<String,Object> result = ServiceUtil.returnSuccess();
//        Delegator delegator = dctx.getDelegator();
//        //获取ids参数
//        String ids = (String)context.get("ids");
//        //转换成list
//        List idList = FastList.newInstance();
//        for(String id : ids.split(",")){
//        	idList.add(id);
//        }
//        //编辑where条件
//        EntityCondition mainCond = EntityCondition.makeCondition("productActivityManagerId", EntityOperator.IN,idList);
//
//        try {
//        	delegator.removeByCondition("ProductActivityManagerAssoc", mainCond);
//			delegator.removeByCondition("ProductActivityManager", mainCond);
//		} catch (GenericEntityException e) {
//			e.printStackTrace();
//		}
//
//        return result;
//    }

    /**
     * 根据ID删除专题活动,可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> activityMgrDel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //获取ids参数
        String ids = (String)context.get("ids");
        //转换成list
        List<String> idList = FastList.newInstance();
        for(String id : ids.split(",")){
            idList.add(id);
        }
        try {
            for(String id:idList){
                // 删除专题关联栏目信息
                List<GenericValue> subjectColumnActivityAssocInfos=delegator.findByAnd("SubjectColumnActivityAssoc", UtilMisc.toMap("productActivityManagerId",id));
                if(UtilValidate.isNotEmpty(subjectColumnActivityAssocInfos)){
                    for(GenericValue scaInfo:subjectColumnActivityAssocInfos){
                        String curSubjectColumnId=scaInfo.getString("subjectColumnId");
                        delegator.removeByAnd("SubjectColumn",UtilMisc.toMap("subjectColumnId",curSubjectColumnId));
                        delegator.removeByAnd("SubjectColumnProductAssoc",UtilMisc.toMap("subjectColumnId",curSubjectColumnId));
                    }
                    delegator.removeAll(subjectColumnActivityAssocInfos);
                }
                //编辑where条件
                EntityCondition mainCond = EntityCondition.makeCondition("productActivityManagerId", EntityOperator.IN,idList);
                delegator.removeByCondition("ProductActivityManager", mainCond);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 活动管理新增	add by qianjin 2016.02.16
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> checkActiveCodeExist(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityCode = (String)context.get("activityCode");
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> activities =   delegator.findByAnd("ProductActivity", UtilMisc.toMap("activityCode", activityCode));
        if(UtilValidate.isNotEmpty(activities)){
            result.put("activity",activities.get(0));
        }
        //delegator对象
        return result;
    }
    
 // Add by zhajh at 20160322 秒杀销售时间的验证 Begin
    /**
     * 秒杀销售时间的验证
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> chkActivityDateForKill(DispatchContext dcx, Map<String, ? extends Object> context) {
   	 LocalDispatcher dispatcher = dcx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Timestamp activityStartDate = (Timestamp) context.get("activityStartDate");
        Timestamp activityEndDate = (Timestamp) context.get("activityEndDate");
        
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 时间验证用时间
        Date startDate=null;
        Date endDate=null;
        
        startDate=new Date(activityStartDate.getTime());
        endDate=new Date(activityEndDate.getTime());
        
        String chkFlg="Y"; //验证检查结果
        String activityEndDateChked="";// 验证后的销售结束时间
        try {
       	Calendar ca=Calendar.getInstance();
     		ca.setTime(startDate);
     		ca.add(Calendar.HOUR, 99);
     		ca.add(Calendar.MINUTE, 59);
     		ca.add(Calendar.SECOND,59);
     		//System.out.println(sdf.format(ca.getTime()));
     		String chkEnd=sdf.format(ca.getTime());
     		String end=sdf.format(activityEndDate);
     		
     		java.util.Calendar c1=java.util.Calendar.getInstance();     
     		java.util.Calendar c2=java.util.Calendar.getInstance();
     		
     		c1.setTime(sdf.parse(end));     
			c2.setTime(sdf.parse(chkEnd)); 
			
			int resultChk=c1.compareTo(c2);  
			
			if(resultChk>0){
				// 验证检查结果
				chkFlg="N";
				activityEndDateChked=chkEnd;
			}else{
				activityEndDateChked=end;
			}
			

		} catch (Exception e) {
			 return ServiceUtil.returnError(e.getMessage());
		}
       result.put("activityEndDateChked", activityEndDateChked); 
       result.put("chkFlg", chkFlg); 
       
       return result;
   	 
    }
    // Add by zhajh at 20160322 秒杀销售时间的验证 End



}
