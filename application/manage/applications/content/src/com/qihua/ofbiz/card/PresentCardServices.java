package com.qihua.ofbiz.card;

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
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import java.util.*;

/**
 * Created by zhajh on 2018/04/08.
 */
public class PresentCardServices {

    private final static Logger logger = LoggerFactory.getLogger(PresentCardServices.class) ;
    public static final String module = PresentCardServices.class.getName();
    public static final String resource = "CustomerMgrUiLabels";
    public static final String resourceError = "CustomerMgrErrorUiLabels";

    /**
     * 礼品卡片管理列表查询
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String,Object> findPresentCard(DispatchContext dctx, Map<String, ? extends Object> context){
        Map<String,Object> result = ServiceUtil.returnSuccess() ;
        // 获取本地delegator对像
        Delegator delegator = dctx.getDelegator();
        String cardName = (String) context.get("cardName");

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


        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //根据礼品卡片名称模糊查询
        EntityExpr entityExprs = null ;
        if (UtilValidate.isNotEmpty(cardName)) {
            entityExprs = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("cardName"), EntityOperator.LIKE,EntityFunction.UPPER("%" + cardName + "%"));
            andExprs.add(entityExprs);
        }

        // 状态为有效 "Y"
        andExprs.add(EntityCondition.makeCondition("status", "Y"));

        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        List<GenericValue> recordList = new ArrayList<GenericValue>() ;
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            EntityListIterator pli = delegator.find("PresentCard",mainCond,null,null, null,findOpts);
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

        //根据礼品卡片编码值做升序排序
        recordList = EntityUtil.orderBy(recordList, UtilMisc.toList("presentCardId ASC"));

        //返回的参数
        result.put("recordsList",recordList);
        result.put("totalSize", Integer.valueOf(messageTemplateListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 保存礼品卡片新增，编辑功能
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    public static  Map<String,Object> savePresentCard(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException{
        Delegator delegator = dctx.getDelegator();

        Map<String,Object> result = ServiceUtil.returnSuccess() ;

        String presentCardId = (String) context.get("presentCardId");
        String cardName = (String) context.get("cardName");
        String contentId = (String) context.get("contentId");
        String cardDesc = (String) context.get("cardDesc");
        String status = (String) context.get("status");



            GenericValue presentCard = delegator.makeValue("PresentCard") ;
            if(UtilValidate.isNotEmpty(cardName)) {
                presentCard.setString("cardName",cardName);
            }
            if(UtilValidate.isNotEmpty(contentId)) {
                presentCard.setString("contentId",contentId);
            }
            if(UtilValidate.isNotEmpty(cardDesc)) {
                presentCard.setString("cardDesc",cardDesc);
            }

            if(UtilValidate.isNotEmpty(status)) {
                presentCard.setString("status",status);
            }

        // add
        if(UtilValidate.isEmpty(presentCardId)){
            presentCardId = delegator.getNextSeqId("PresentCard") ;
            presentCard.setString("presentCardId",presentCardId);
            try {
                presentCard.create() ;
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(),e);
                return ServiceUtil.returnError(e.getMessage()) ;
            }
        }else{ // edit
            try {
                presentCard.setString("presentCardId",presentCardId);
                presentCard.store();
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(),e);
                return ServiceUtil.returnError(e.getMessage()) ;
            }
        }
        return result ;
    }

    /**
     * 根据id获取礼品卡片信息
     * @param dcxt
     * @param context
     * @return
     */
    public static Map<String,Object> findPresentCardById(DispatchContext dcxt,Map<String,Object> context){
        String presentCardId = (String) context.get("presentCardId");
        Delegator delegator = dcxt.getDelegator();
        Map<String,Object> result = ServiceUtil.returnSuccess() ;
        try {
            GenericValue presentCard = delegator.findByPrimaryKey("PresentCard", UtilMisc.toMap("presentCardId", presentCardId));
            result.put("presentCard",presentCard) ;
        } catch (GenericEntityException e) {
            logger.debug(e.getMessage(),e);
            return ServiceUtil.returnError(e.getMessage()) ;
        }
        return result ;
    }


    /**
     * 根据id删除PresentCard
     * @param dcxt
     * @param context
     * @return
     */
    public static Map<String,Object> deltePresentCard(DispatchContext dcxt,Map<String,Object> context){
        String pcIds = (String) context.get("pcIds");
        Delegator delegator = dcxt.getDelegator();
        if (UtilValidate.isEmpty(pcIds)) {
            return ServiceUtil.returnError("非法操作！");
        }
        Set<String> ids = new HashSet<String>() ;
        String [] idArr = pcIds.split(",") ;
        for (int i = 0; i < idArr.length; i++) {
            ids.add(idArr[i]) ;
        }
        EntityExpr codition = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("presentCardId"), EntityOperator.IN, ids);
        try {
            Boolean tx = TransactionUtil.begin() ;
            EntityListIterator pli=delegator.find("PresentCard",codition,null,null,null,null);
            List<GenericValue> presentCardList = pli.getCompleteList();
            for(GenericValue presentCardInfo:presentCardList){
                if(UtilValidate.isNotEmpty(presentCardInfo)){
                    presentCardInfo.setString("status","N");
                    presentCardInfo.store();
                }
            }

            //delegator.removeByCondition("PresentCard",codition) ;
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
