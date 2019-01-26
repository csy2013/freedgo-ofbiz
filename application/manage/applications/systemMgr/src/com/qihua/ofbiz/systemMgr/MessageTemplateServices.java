package com.qihua.ofbiz.systemMgr;

import javolution.util.FastList;
import javolution.util.FastSet;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Larry on 2017/3/20.
 * 消息模板服务
 */
public class MessageTemplateServices {
    private final static Logger logger = LoggerFactory.getLogger(MessageTemplateServices.class) ;
    public static final String module = LogisticsServices.class.getName();
    public static final String resource = "SystemMgrUiLabels";
    public static final String resourceError = "SystemMgrErrorUiLabels";


    /**
     * 模板列表数据:查询条件(模板类型，模板名称，发送方式)
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findMessage(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //当前用户登录信息
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<GenericValue> messageTemplateList = FastList.newInstance();

        //总记录数
        int messageTemplateListSize = 0;
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
       // DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        Set<String> fieldsToSelect = FastSet.newInstance();

        //排序字段名称
        String sortField = "messageTemplateId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String)context.get("sortType");
        }
        orderBy.add(sortType+sortField);

        //设置动态View
        /*dynamicView.addMemberEntity("MT", "MessageTemplate");
        dynamicView.addAlias("MT", "messageTemplateId");
        dynamicView.addAlias("MT", "templateType");
        dynamicView.addAlias("MT", "inUse");
        dynamicView.addAlias("MT", "templateName");
        dynamicView.addAlias("MT", "sendMode");
        dynamicView.addAlias("MT", "templateContent");
        dynamicView.addAlias("MT", "messageSignature");*/
        fieldsToSelect.add("messageTemplateId");
        fieldsToSelect.add("templateType");
        fieldsToSelect.add("isUse");
        fieldsToSelect.add("templateName");
        fieldsToSelect.add("sendMode");
        fieldsToSelect.add("templateContent");
        fieldsToSelect.add("messageSignature");


        //根据类型查询
        if (UtilValidate.isNotEmpty(context.get("templateType"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("templateType"), EntityOperator.EQUALS,context.get("templateType")));
        }

        //根据模板名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("templateName"))) {
            andExprs.add(EntityCondition.makeCondition("templateName", EntityOperator.LIKE,"%" + context.get("templateName") + "%"));
        }

        //根据发送类型查询
        if (UtilValidate.isNotEmpty(context.get("sendMode"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("sendMode"), EntityOperator.EQUALS, context.get("sendMode")));
        }
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }


        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            //EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            EntityListIterator pli = delegator.find("MessageTemplate",mainCond,null,fieldsToSelect, UtilMisc.toList(orderBy),findOpts);
            // 获取分页所需的记录集合
            messageTemplateList = pli.getPartialList(lowIndex, viewSize);

            // 获取总记录数
            messageTemplateListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > messageTemplateListSize) {
                highIndex = messageTemplateListSize;
            }

            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }

        //返回的参数
        result.put("recordsList",messageTemplateList);
        result.put("totalSize", Integer.valueOf(messageTemplateListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }

    // 创建消息模板
    public static Map<String, Object> editMessageTemplate(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        // 获取用户登录信息
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        // 取值
        String messageTemplateId = (String) context.get("messageTemplateId");
        String templateType = (String) context.get("templateType");
        String isUse = (String) context.get("isUse");
        String templateName = (String) context.get("templateName");
        String sendMode = (String) context.get("sendMode");
        String templateContent = (String) context.get("templateContent");
        String messageSignature = (String) context.get("messageSignature");
       // String messageChain = (String) context.get("messageChain");
        String operateType = (String) context.get("operateType");
        String linkUrl = (String)context.get("linkUrl");
        String linkId = (String)context.get("linkId");
        String linkName = (String)context.get("linkName");
        String firstLinkType = (String) context.get("firstLinkType");
        String secondLinkType = (String) context.get("secondLinkType");

        GenericValue messageTemplate = delegator.makeValue("MessageTemplate") ;
        messageTemplate.setString("templateType",templateType);
        messageTemplate.setString("isUse",isUse);
        messageTemplate.setString("templateName",templateName);
        messageTemplate.setString("sendMode",sendMode);
        messageTemplate.setString("templateContent",templateContent);
       // messageTemplate.setString("messageChain",messageChain);
        messageTemplate.setString("messageSignature",messageSignature);
        messageTemplate.setString("linkUrl",linkUrl);
        messageTemplate.setString("linkId",linkId);
        messageTemplate.setString("linkName",linkName);
        messageTemplate.setString("firstLinkType",firstLinkType) ;
        messageTemplate.setString("secondLinkType",secondLinkType);
        // 修改操作
        if("update".equals(operateType)){
            try {
                messageTemplate.setString("messageTemplateId",messageTemplateId);
                messageTemplate.store();
            }catch (Exception e){
                e.printStackTrace();
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "编辑修改失败",
                        UtilMisc.toMap("messageTemplateId", messageTemplateId), locale));
            }
        // 新增操作
        }else if ("create".equals(operateType)){
            try {
                messageTemplateId = delegator.getNextSeqId("messageTemplate") ;
                messageTemplate.setString("messageTemplateId",messageTemplateId);
                messageTemplate.create();
            }catch (Exception e){
                e.printStackTrace();
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "新增失败",
                        UtilMisc.toMap("messageTemplateId", messageTemplateId), locale));
            }
        }
        return ServiceUtil.returnSuccess() ;
    }

    // 创建消息模板
    public static Map<String, Object> deleteMessageTemplate(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        String mtIds = (String) context.get("mtIds");
        if(UtilValidate.isEmpty(mtIds)) {
            return ServiceUtil.returnError("非法请求");
        }
        String [] mtIdArr = mtIds.split(",") ;
        List<String> ids = new ArrayList<String>() ;
        for (String mtId : mtIdArr){
            ids.add(mtId) ;
        }
        EntityCondition condition = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("messageTemplateId"),EntityOperator.IN,ids) ;
        try {
            delegator.removeByCondition("MessageTemplate",condition) ;
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("删除失败！");
        }
        return ServiceUtil.returnSuccess() ;
    }

    /**
     * 根据类型ID获取enum数据 add by yangpeng 2017/03/22
     * @param delegator
     * @param enumTypeId
     * @return
     */
    public static List getEnumByTypeId(Delegator delegator, String enumTypeId) {
        //返回的数据List
        List enumList = FastList.newInstance();
        try {
            //根据类型ID获取enum数据
//            Map paramMap = UtilMisc.toMap("enumTypeId", enumTypeId,"enumCode" , "N");
            Map paramMap = UtilMisc.toMap("enumTypeId", enumTypeId);
            List orderBy = UtilMisc.toList("sequenceId");
            enumList = delegator.findList("Enumeration",EntityCondition.makeCondition(paramMap), null, orderBy, null, false);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return enumList;
    }

    /**
     * 设置启用禁用 add by yangpeng 2017/03/23
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String,Object> settingIsUse(DispatchContext dctx, Map<String, ? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        String messageTemplateId = (String) context.get("messageTemplateId");
        String isUse = (String) context.get("isUse");
        try {
            // 查询对象
            GenericValue mt = delegator.findByPrimaryKey("MessageTemplate", UtilMisc.toMap("messageTemplateId", messageTemplateId));
            mt.setString("isUse",isUse);
            mt.store();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage()) ;
        }
        return ServiceUtil.returnSuccess() ;
    }

}
