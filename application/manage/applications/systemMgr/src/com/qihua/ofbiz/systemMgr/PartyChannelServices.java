package com.qihua.ofbiz.systemMgr;

import javolution.util.FastList;
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
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.List;
import java.util.Map;

public class PartyChannelServices {

    public static Map<String, Object> addPartyChannel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();

        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();

        String channelCode= (String)context.get("channelCode");
        String channelName= (String)context.get("channelName");
        String remark= (String)context.get("remark");
        String status= (String)context.get("status");

        String channelId = delegator.getNextSeqId("PartyChannel");
        GenericValue partyChannel = delegator.makeValue("PartyChannel",UtilMisc.toMap("channelId",channelId, "channelCode",channelCode,"channelName",channelName,"remark",remark,"status",status));
        try {
            partyChannel.create();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Map<String, Object> getChannelListForJson(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();

        String channelCode= (String)context.get("channelCode");
        String channelName= (String)context.get("channelName");

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
        dynamicView.addMemberEntity("PC", "PartyChannel");
        //设置需要查询的字段
        dynamicView.addAlias("PC", "channelId");
        dynamicView.addAlias("PC", "channelCode");
        dynamicView.addAlias("PC", "channelName");
        dynamicView.addAlias("PC", "remark");
        dynamicView.addAlias("PC", "createdStamp");
        dynamicView.addAlias("PC", "status");

        fieldsToSelect.add("channelId");
        fieldsToSelect.add("channelCode");
        fieldsToSelect.add("channelName");
        fieldsToSelect.add("remark");
        fieldsToSelect.add("createdStamp");
        fieldsToSelect.add("status");

        // 按商家名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("channelCode"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("channelCode"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("channelCode") + "%")));
        }

        // 按法人名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("channelName"))) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("channelName"),
                    EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("channelName") + "%")));
        }

        // 排序字段名称
        String sortField = "createdStamp";
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
            e.printStackTrace();
        }

        // 返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(totalSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    public static Map<String, Object> deleteChannel(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        String channelId = (String) context.get("channelId");
        delegator.removeByAnd("PartyChannel",UtilMisc.toMap("channelId",channelId));
        return result;

    }

    public static Map<String, Object>  getChannelById(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        String channelId = (String) context.get("channelId");
        GenericValue partyChannel = delegator.findByAnd("PartyChannel",UtilMisc.toMap("channelId",channelId)).get(0);
        result.put("channelInfo",partyChannel);
        return result;
    }


    public static Map<String, Object>  updatePartyChannel(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        String channelId = (String) context.get("channelId");
        String channelCode= (String)context.get("channelCode");
        String channelName= (String)context.get("channelName");
        String remark= (String)context.get("remark");
        String status= (String)context.get("status");

        GenericValue partyChannel =delegator.makeValue("PartyChannel");
        partyChannel.put("channelId",channelId);
        partyChannel.put("channelCode",channelCode);
        partyChannel.put("channelName",channelName);
        partyChannel.put("remark",remark);
        partyChannel.put("status",status);

        partyChannel.store();

        return result;
    }



}
