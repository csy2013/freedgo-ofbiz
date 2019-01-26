package com.qihua.ofbiz.home;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.List;
import java.util.Map;

public class IndexTemplateServices {
    public static Map<String, Object> getIndexTemplateListJson(DispatchContext dctx, Map<String, ? extends Object> context) {
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
        int viewSize = 10;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 10;
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
        dynamicView.addMemberEntity("IT", "IndexTemplate");
        //设置需要查询的字段
        dynamicView.addAlias("IT", "templateId");
        dynamicView.addAlias("IT", "templateName");
        dynamicView.addAlias("IT", "isUsed");
        fieldsToSelect.add("templateId");
        fieldsToSelect.add("templateName");
        fieldsToSelect.add("isUsed");

        // 按商家名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("templateName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("templateName"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("templateName") + "%")));
        }


        // 排序字段名称
        String sortField = "templateId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        // 排序类型
        String sortType = "-";
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
            e.printStackTrace();
        }
        // 返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(totalSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    public static Map<String, Object> indexTemplateAdd(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();

        String templateName = (String) context.get("templateName");
        String isUsed = (String) context.get("isUsed");

        if("Y".equals(isUsed)){
            //先将其他的在使用的修改为不使用
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("isUsed", "N");
            EntityCondition UpdateCon = EntityCondition.makeCondition("isUsed", EntityComparisonOperator.EQUALS, "Y");
            delegator.storeByCondition("IndexTemplate", updateFields, UpdateCon);
        }


        String templateId = delegator.getNextSeqId("IndexTemplate");
        GenericValue indexTemplate =delegator.makeValue("IndexTemplate", UtilMisc.toMap("templateName",templateName,"isUsed",isUsed,"templateId",templateId));
        indexTemplate.create();

        //添加明细
        List<GenericValue> contentList = FastList.newInstance();
        String templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        GenericValue indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","1");
        indexTemplateContent.put("widgetType","index_widget_banner");
        indexTemplateContent.put("widgetName","轮播图");
        indexTemplateContent.put("remark","循环播放已添加的广告图，点击广告图进入指定页面");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","2");
        indexTemplateContent.put("widgetType","index_widget_navigation");
        indexTemplateContent.put("widgetName","菜单导航");
        indexTemplateContent.put("remark","集中展示重要内容和服务，点击分类图标进入指定页面");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","3");
        indexTemplateContent.put("widgetType","index_widget_notice");
        indexTemplateContent.put("widgetName","公告");
        indexTemplateContent.put("remark","滚动展示已添加的公告标题，点击进入指定页面");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","4");
        indexTemplateContent.put("widgetType","index_widget_user_dynamic");
        indexTemplateContent.put("widgetName","用户动态");
        indexTemplateContent.put("remark","动态展示用户拼团，购物，拆红包等行为信息，点击引导按钮进入指定页面");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","5");
        indexTemplateContent.put("widgetType","index_widget_group_order");
        indexTemplateContent.put("widgetName","星意拼团");
        indexTemplateContent.put("remark","动态展示拼团商品信息，点击进入拼团商品详情页");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","6");
        indexTemplateContent.put("widgetType","index_widget_activity_show");
        indexTemplateContent.put("widgetName","活动推荐");
        indexTemplateContent.put("remark","滑动展示已添加的活动广告图，点击进入指定页面");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","7");
        indexTemplateContent.put("widgetType","index_widget_acitivity_seckill");
        indexTemplateContent.put("widgetName","秒杀现场");
        indexTemplateContent.put("remark","动态展示秒杀商品信息，点击进入秒杀商品详情页");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","8");
        indexTemplateContent.put("widgetType","index_widget_micro_paper");
        indexTemplateContent.put("widgetName","星意微刊");
        indexTemplateContent.put("remark","展示已添加的营销文章，点击进入文章详情页");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","9");
        indexTemplateContent.put("widgetType","index_widget_promo_area");
        indexTemplateContent.put("widgetName","促销专题");
        indexTemplateContent.put("remark","展示已添加的促销专题封面和商品，点击专题封面进入促销专题页，点击商品进入商品详情页");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","10");
        indexTemplateContent.put("widgetType","index_widget_hot_brand");
        indexTemplateContent.put("widgetName","热门品牌");
        indexTemplateContent.put("remark","滑动展示已添加的品牌广告图，点击进入指定品牌的商品列表页");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","11");
        indexTemplateContent.put("widgetType","index_widget_productstore");
        indexTemplateContent.put("widgetName","店铺推荐");
        indexTemplateContent.put("remark","滑动展示已添加的店铺广告图，点击进入指定店铺首页");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","12");
        indexTemplateContent.put("widgetType","index_widget_product");
        indexTemplateContent.put("widgetName","商品推荐");
        indexTemplateContent.put("remark","展示已添加的商品，点击进入详情页");
        contentList.add(indexTemplateContent);

        templateContentId = delegator.getNextSeqId("IndexTemplateContent");
        indexTemplateContent =delegator.makeValue("IndexTemplateContent",UtilMisc.toMap("isShow","Y","templateContentId",templateContentId,"templateId",templateId));
        indexTemplateContent.put("sequenceNum","13");
        indexTemplateContent.put("widgetType","index_widget_guess");
        indexTemplateContent.put("widgetName","猜你喜欢");
        indexTemplateContent.put("remark","根据用户标签或浏览历史，动态展示推荐商品，点击进入商品详情页");
        contentList.add(indexTemplateContent);

        delegator.storeAll(contentList);

        return result;
    }

    public static Map<String, Object> editTemplateStatus(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();

        String templateId = (String) context.get("templateId");
        String isUsed = (String) context.get("isUsed");
        if("Y".equals(isUsed)){
            //先将其他的在使用的修改为不使用
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("isUsed", "N");
            EntityCondition UpdateCon = EntityCondition.makeCondition("isUsed", EntityComparisonOperator.EQUALS, "Y");
            delegator.storeByCondition("IndexTemplate", updateFields, UpdateCon);
        }

        Map<String, Object> updateFields = FastMap.newInstance();
        updateFields.put("isUsed", isUsed);
        EntityCondition UpdateCon = EntityCondition.makeCondition("templateId", EntityComparisonOperator.EQUALS, templateId);
        delegator.storeByCondition("IndexTemplate", updateFields, UpdateCon);

        return result;
    }



    public static Map<String, Object> deleteTemplate(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        String templateId = (String) context.get("templateId");
        delegator.removeByAnd("IndexTemplate",UtilMisc.toMap("templateId",templateId));
        delegator.removeByAnd("IndexTemplateContent",UtilMisc.toMap("templateId",templateId));
        return result;
    }

    public static Map<String, Object> getTempleteContent(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        String templateId = (String) context.get("templateId");

        List<String> orderBy = FastList.newInstance();
        orderBy.add("sequenceNum");

        List<GenericValue> recordList = delegator.findByAnd("IndexTemplateContent",UtilMisc.toMap("templateId",templateId),orderBy);
        result.put("recordList",recordList);

        return result;
    }
    //保存配置的模块明细
    public static Map<String, Object> saveTemplateContent(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String records= (String) context.get("recordList");

        Gson gson=new Gson();
        List<Map> recordList = gson.fromJson(records,new TypeToken<List<Map>>(){}.getType());
        List<GenericValue> templateContentList = FastList.newInstance();
        for(Map record:recordList){
            record.remove("lastUpdatedStamp");
            record.remove("lastUpdatedTxStamp");
            record.remove("createdStamp");
            record.remove("createdTxStamp");
            record.remove("isInEdit");

            GenericValue content = delegator.makeValue("IndexTemplateContent",record);
            templateContentList.add(content);
        }
        delegator.storeAll(templateContentList);
        return result;
    }


}
