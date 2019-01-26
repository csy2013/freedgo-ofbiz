package com.qihua.ofbiz.help;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionFactory;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Larry on 2017/3/27.
 */
public class HelpCategoryServices {

    private final static Logger logger = LoggerFactory.getLogger(HelpCategoryServices.class) ;
    public static final String module = HelpCategoryServices.class.getName();
    public static final String resource = "CustomerMgrUiLabels";
    public static final String resourceError = "CustomerMgrErrorUiLabels";
    
    /**
    * 广告列表查询 （查询条件：分类名称）
    * Created on 2017/3/27 10:12
    * @author:larry
    */
    public static Map<String,Object> findCategory(DispatchContext dctx, Map<String, ? extends Object> context){
        Map<String,Object> result = ServiceUtil.returnSuccess() ;
        // 获取本地delegator对像
        Delegator delegator = dctx.getDelegator();

        String categoryName = (String) context.get("categoryName");

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

        //根据模板名称模糊查询
        EntityExpr entityExprs = null ;
        if (UtilValidate.isNotEmpty(categoryName)) {
            entityExprs = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("categoryName"), EntityOperator.LIKE,EntityFunction.UPPER("%" + categoryName + "%"));
        }
        List<GenericValue> recordList = new ArrayList<GenericValue>() ;
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
           // EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            EntityListIterator pli = delegator.find("HelpCategory",entityExprs,null,null, null,null);
            // 获取分页所需的记录集合
            recordList = pli.getPartialList(lowIndex, viewSize);

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

        //根据分类排序的值做升序排序
        recordList = EntityUtil.orderBy(recordList, UtilMisc.toList("sequenceNum ASC"));

        //返回的参数
        result.put("recordsList",recordList);
        result.put("totalSize", Integer.valueOf(messageTemplateListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
    * 保存帮助类别新增，编辑功能
    * Created on 2017/3/27 10:27
    * @author:larry
     * @throws GenericEntityException 
    */
    public static  Map<String,Object> saveCategory(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException{
        Delegator delegator = dctx.getDelegator();

        Map<String,Object> result = ServiceUtil.returnSuccess() ;
        
        String helpCategoryId = (String) context.get("helpCategoryId");
        String categoryName = (String) context.get("categoryName");
        String imgUrl = (String) context.get("imgUrl");
        String isShow = (String) context.get("isShow");
        String sequenceNum = (String) context.get("sequenceNum");
        String showChannel = (String) context.get("showChannel");
        
        if(UtilValidate.isNotEmpty(showChannel) && showChannel.length() > 1){
            showChannel = showChannel.substring(1,showChannel.length()-1) ;
        }
        GenericValue category = delegator.makeValue("HelpCategory") ;
        if(UtilValidate.isNotEmpty(categoryName)) {
            category.setString("categoryName",categoryName);
        }
        if(UtilValidate.isNotEmpty(imgUrl)) {
            category.setString("imgUrl",imgUrl);
        }
        if(UtilValidate.isNotEmpty(isShow)) {
            category.setString("isShow",isShow);
        }
        if(UtilValidate.isNotEmpty(sequenceNum)) {
            // 判断排序是否重复
            List<GenericValue> helpCategorys = new ArrayList<GenericValue>();
            helpCategorys = delegator.findByAnd("HelpCategory", UtilMisc.toMap("sequenceNum", Long.parseLong(sequenceNum)));
            if(helpCategorys != null && helpCategorys.size() > 1) {
            	result.put("isF", "F");
            	return result;
            }
            category.setString("sequenceNum",sequenceNum);
        }
        if(UtilValidate.isNotEmpty(showChannel)) {
            category.setString("showChannel",showChannel);
        }

        // add
        if(UtilValidate.isEmpty(helpCategoryId)){
            helpCategoryId = delegator.getNextSeqId("helpCategory") ;
            category.setString("helpCategoryId",helpCategoryId);
            try {
                category.create() ;
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(),e);
                return ServiceUtil.returnError(e.getMessage()) ;
            }
        }else{ // edit
            try {
                category.setString("helpCategoryId",helpCategoryId);
                category.store();
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(),e);
                return ServiceUtil.returnError(e.getMessage()) ;
            }
        }
        return ServiceUtil.returnSuccess() ;
    }

    /**
    * 根据id获取helpCategory
    * Created on 2017/3/27 10:53
    * @author:larry
    */
    public static Map<String,Object> getCategoryOne(DispatchContext dcxt,Map<String,Object> context){
        String helpCategoryId = (String) context.get("helpCategoryId");
        Delegator delegator = dcxt.getDelegator();
        Map<String,Object> result = ServiceUtil.returnSuccess() ;
        try {
            GenericValue helpCategory = delegator.findByPrimaryKey("HelpCategory", UtilMisc.toMap("helpCategoryId", helpCategoryId));
            result.put("helpCategory",helpCategory) ;
        } catch (GenericEntityException e) {
            logger.debug(e.getMessage(),e);
            return ServiceUtil.returnError(e.getMessage()) ;
        }
        return result ;
    }

    /**
     * 根据id删除helpCategory
     * Created on 2017/3/27 10:53
     * @author:larry
     */
    public static Map<String,Object> delteCategory(DispatchContext dcxt,Map<String,Object> context){
        String hcIds = (String) context.get("hcIds");
        Delegator delegator = dcxt.getDelegator();
        if (UtilValidate.isEmpty(hcIds)) {
            return ServiceUtil.returnError("非法操作！");
        }
        Set<String> ids = new HashSet<String>() ;
        String [] idArr = hcIds.split(",") ;
        for (int i = 0; i < idArr.length; i++) {
            ids.add(idArr[i]) ;
        }
        EntityExpr codition = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("helpCategoryId"), EntityOperator.IN, ids);
        try {
            Boolean tx = TransactionUtil.begin() ;
            // 删除级联信息
            List<GenericValue> infoList = delegator.findList("HelpInfo", codition, null, null, null, false);
            if(!infoList.isEmpty()){
                List<String> helpIds = new ArrayList<String>() ;
                for (GenericValue gv :infoList){
                    helpIds.add(gv.getString("helpInfoId")) ;
                }
                EntityCondition helpInfoCondition = EntityCondition.makeCondition("helpInfoId",EntityOperator.IN,helpIds) ;
                // 删除级联服务支持
                delegator.removeByCondition("ServiceSupport",helpInfoCondition) ;
                // 删除帮助信息
                delegator.removeByCondition("HelpInfo",helpInfoCondition) ;
             }
            delegator.removeByCondition("HelpCategory",codition) ;
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
}
