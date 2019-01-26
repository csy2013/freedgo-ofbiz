package com.qihua.ofbiz.help;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
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
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Larry on 2017/3/28.
 * 帮助管理services
 */
public class HelpInfoServices{

    private final static Logger logger = LoggerFactory.getLogger(HelpInfoServices.class) ;
    public static final String module = HelpInfoServices.class.getName();
    public static final String resource = "CustomerMgrUiLabels";
    public static final String resourceError = "CustomerMgrErrorUiLabels";

    /**
    * 帮助信息编辑保存
    * Created on 2017/3/28 9:28
    * @author:larry
    */
    public static Map<String,Object> editSaveHelpInfo(DispatchContext dctx,Map<String,? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        String helpInfoId = (String) context.get("helpInfoId");
        String helpCategoryId = (String) context.get("helpCategoryId");
        String helpTitle = (String) context.get("helpTitle");
        String helpIcon = (String) context.get("helpIcon");
        String helpAuthor = (String) context.get("helpAuthor");
        String isShow = (String) context.get("isShow");
        String sequenceNum = (String) context.get("sequenceNum");
        String showChannel = (String) context.get("showChannel");
        String helpContent = (String) context.get("helpContent");

        if(UtilValidate.isNotEmpty(showChannel) && showChannel.length() > 1) {
            showChannel = showChannel.substring(1, showChannel.length() - 1);
        }
        GenericValue helpInfo = delegator.makeValue("HelpInfo");
        helpInfo.setString("helpCategoryId",helpCategoryId);
        helpInfo.setString("helpTitle",helpTitle);
        helpInfo.setString("helpIcon",helpIcon);
        helpInfo.setString("helpAuthor",helpAuthor);
        helpInfo.setString("isShow",isShow);
        helpInfo.setString("sequenceNum",sequenceNum);
        helpInfo.setString("showChannel",showChannel);
        helpInfo.setString("helpContent",helpContent);
        if(UtilValidate.isEmpty(helpInfoId)){
            helpInfoId = delegator.getNextSeqId("helpInfoId") ;
            helpInfo.setString("helpInfoId",helpInfoId);
            try {
                helpInfo.create();
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(),e);
                return ServiceUtil.returnError(e.getMessage()) ;
            }
        }else {
            helpInfo.setString("helpInfoId",helpInfoId);
            try {
                helpInfo.store();
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(),e);
                return ServiceUtil.returnError(e.getMessage()) ;
            }
        }
        return ServiceUtil.returnSuccess() ;
    }


    /**
    * 根据Id删除帮助信息
    * Created on 2017/3/28 9:41
    * @author:larry
    */
    public static Map<String,Object> deleteHelpInfo(DispatchContext dctx,Map<String,? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        String hIds = (String) context.get("hIds");
        if(UtilValidate.isEmpty(hIds)) {
            return ServiceUtil.returnError("非法请求！");
        }
        String [] idArr = hIds.split(",") ;
        Set<String> ids = new HashSet<String>() ;
        for (int i = 0; i <idArr.length; i++) {
            ids.add(idArr[i]) ;
        }
        EntityCondition condition = EntityCondition.makeCondition("helpInfoId", EntityOperator.IN,ids) ;
        try {
            Boolean tx = TransactionUtil.begin() ;
            // 删除级联关系，服务支持
            delegator.removeByCondition("ServiceSupport",condition) ;
            // 删除帮助信息
            delegator.removeByCondition("HelpInfo",condition);
            TransactionUtil.commit(tx);
        } catch (GenericEntityException e) {
            try {
                TransactionUtil.rollback();
            } catch (GenericTransactionException e1) {
                e = e1 ;
            }
            logger.debug(e.getMessage(),e);
            return ServiceUtil.returnError(e.getMessage()) ;
        }
        return ServiceUtil.returnSuccess() ;
    }

    /**
    * 帮助信息数据查询
    * Created on 2017/3/28 9:48
    * @author:larry
    */
    public static Map<String,Object> findHelpInfoData(DispatchContext dctx,Map<String,? extends Object> context){
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //记录集合
        List<Map> recordList = FastList.newInstance();

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
         DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        Set<String> fieldsToSelect = FastSet.newInstance();

        //排序字段名称
        String sortField = "helpInfoId";
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
        dynamicView.addMemberEntity("HI", "HelpInfo");
        dynamicView.addAlias("HI", "helpInfoId");
        dynamicView.addAlias("HI", "helpCategoryId");
        dynamicView.addAlias("HI", "helpTitle");
        dynamicView.addAlias("HI", "helpIcon");
        dynamicView.addAlias("HI", "helpAuthor");
        dynamicView.addAlias("HI", "isShow");
        dynamicView.addAlias("HI", "sequenceNum");
        dynamicView.addAlias("HI", "showChannel");
        dynamicView.addAlias("HI", "helpContent");
        dynamicView.addAlias("HI", "createdStamp");

        dynamicView.addMemberEntity("HC","HelpCategory");
        dynamicView.addAlias("HC","helpCategoryId");
        dynamicView.addAlias("HC","categoryName");

        dynamicView.addViewLink("HI","HC",Boolean.TRUE, ModelKeyMap.makeKeyMapList("helpCategoryId", "helpCategoryId"));
        fieldsToSelect.add("helpInfoId");
        fieldsToSelect.add("helpCategoryId");
        fieldsToSelect.add("helpTitle");
        fieldsToSelect.add("helpAuthor");
        fieldsToSelect.add("isShow");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("showChannel");
        fieldsToSelect.add("helpContent");
        fieldsToSelect.add("categoryName") ;
        fieldsToSelect.add("createdStamp") ;

        //根据标题查询
        if (UtilValidate.isNotEmpty(context.get("helpTitle"))) {
            andExprs.add(EntityCondition.makeCondition("helpTitle", EntityOperator.LIKE,"%" + context.get("helpTitle") + "%"));
        }

        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            if(pli!=null && pli.hasNext()){
                for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)){
                    Map map = FastMap.newInstance() ;
                    map.put("helpInfoId",gv.get("helpInfoId")) ;
                    map.put("helpTitle",gv.get("helpTitle")) ;
                    map.put("categoryName",gv.get("categoryName")) ;
                    map.put("createdStamp",gv.get("createdStamp")) ;
                    map.put("helpAuthor",gv.get("helpAuthor")) ;
                    map.put("sequenceNum",gv.get("sequenceNum")) ;
                    recordList.add(map) ;
                }
            }

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
        result.put("recordsList",recordList);
        result.put("totalSize", Integer.valueOf(messageTemplateListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }

    /**
    * 根据主键查询帮助信息
    * Created on 2017/3/28 10:23
    * @author:larry
    */
    public static Map<String,Object> findHelpInfoOne(DispatchContext dctx,Map<String,? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        String helpInfoId = (String) context.get("helpInfoId");
        Map result = FastMap.newInstance() ;
        try {
            GenericValue helpInfo = delegator.findByPrimaryKey("HelpInfo", UtilMisc.toMap("helpInfoId", helpInfoId));
            result.put("helpInfoVO",helpInfo) ;
        } catch (GenericEntityException e) {
            logger.debug(e.getMessage(),e);
            return ServiceUtil.returnError(e.getMessage()) ;
        }
        return result ;
    }
}
