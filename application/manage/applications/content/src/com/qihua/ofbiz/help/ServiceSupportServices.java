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
public class ServiceSupportServices {

    private final static Logger logger = LoggerFactory.getLogger(ServiceSupportServices.class) ;
    public static final String module = ServiceSupportServices.class.getName();
    public static final String resource = "CustomerMgrUiLabels";
    public static final String resourceError = "CustomerMgrErrorUiLabels";

    /**
    * 服务支持编辑保存
    * Created on 2017/3/28 9:28
    * @author:larry
    */
    public static Map<String,Object> editSaveServiceSupport(DispatchContext dctx,Map<String,? extends Object> context){
        Delegator delegator = dctx.getDelegator();

        String serviceSupportId = (String) context.get("serviceSupportId");
        String serviceSupportName = (String) context.get("serviceSupportName");
        String imgUrl = (String) context.get("imgUrl");
        String helpInfoId = (String) context.get("helpInfoId");


        GenericValue serviceSupport = delegator.makeValue("ServiceSupport");
        serviceSupport.setString("serviceSupportId",serviceSupportId);
        serviceSupport.setString("serviceSupportName",serviceSupportName);
        serviceSupport.setString("imgUrl",imgUrl);
        serviceSupport.setString("helpInfoId",helpInfoId);

        if(UtilValidate.isEmpty(serviceSupportId)){
            serviceSupportId = delegator.getNextSeqId("serviceSupport") ;
            serviceSupport.setString("serviceSupportId",serviceSupportId);
            try {
                serviceSupport.create();
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(),e);
                return ServiceUtil.returnError(e.getMessage()) ;
            }
        }else {
            serviceSupport.setString("serviceSupportId",serviceSupportId);
            try {
                serviceSupport.store();
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(),e);
                return ServiceUtil.returnError(e.getMessage()) ;
            }
        }
        return ServiceUtil.returnSuccess() ;
    }


    /**
    * 根据Id删除服务支持
    * Created on 2017/3/28 9:41
    * @author:larry
    */
    public static Map<String,Object> deleteServiceSupport(DispatchContext dctx,Map<String,? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        String ssIds = (String) context.get("ssIds");
        if(UtilValidate.isEmpty(ssIds)) {
            return ServiceUtil.returnError("非法请求！");
        }
        String [] idArr = ssIds.split(",") ;
        Set<String> ids = new HashSet<String>() ;
        for (int i = 0; i <idArr.length; i++) {
            ids.add(idArr[i]) ;
        }
        EntityCondition condition = EntityCondition.makeCondition("serviceSupportId", EntityOperator.IN,ids) ;
        try {
            delegator.removeByCondition("ServiceSupport",condition);
        } catch (GenericEntityException e) {
            logger.debug(e.getMessage(),e);
            return ServiceUtil.returnError(e.getMessage()) ;
        }
        return ServiceUtil.returnSuccess() ;
    }

    /**
    * 服务支持数据查询
    * Created on 2017/3/28 9:48
    * @author:larry
    */
    public static Map<String,Object> findServiceSupportData(DispatchContext dctx,Map<String,? extends Object> context){
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
        String sortField = "serviceSupportId";
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
        dynamicView.addMemberEntity("SS", "ServiceSupport");
        dynamicView.addAlias("SS", "serviceSupportId");
        dynamicView.addAlias("SS", "serviceSupportName");
        dynamicView.addAlias("SS", "imgUrl");
        dynamicView.addAlias("SS", "helpInfoId");
        dynamicView.addAlias("SS", "createdStamp");


        dynamicView.addMemberEntity("HI","HelpInfo");
        dynamicView.addAlias("HI","helpInfoId");
        dynamicView.addAlias("HI","helpTitle");

        dynamicView.addViewLink("SS","HI",Boolean.TRUE, ModelKeyMap.makeKeyMapList("helpInfoId", "helpInfoId"));
        fieldsToSelect.add("helpInfoId");
        fieldsToSelect.add("serviceSupportId");
        fieldsToSelect.add("serviceSupportName");
        fieldsToSelect.add("imgUrl");
        fieldsToSelect.add("createdStamp");
        fieldsToSelect.add("helpTitle");

        //根据标题查询
       /* if (UtilValidate.isNotEmpty(context.get("helpTitle"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("helpTitle"), EntityOperator.EQUALS,context.get("helpTitle")));
        }*/

        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, null, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            if(pli!=null){
                for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)){
                    Map map = FastMap.newInstance() ;
                    map.put("helpInfoId",gv.get("helpInfoId")) ;
                    map.put("serviceSupportId",gv.get("serviceSupportId")) ;
                    map.put("serviceSupportName",gv.get("serviceSupportName")) ;
                    map.put("createdStamp",gv.get("createdStamp")) ;
                    map.put("imgUrl",gv.get("imgUrl")) ;
                    map.put("helpTitle",gv.get("helpTitle")) ;
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
    * 根据主键查询服务支持
    * Created on 2017/3/28 10:23
    * @author:larry
    */
    public static Map<String,Object> findServiceSupportOne(DispatchContext dctx,Map<String,? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        String serviceSupportId = (String) context.get("serviceSupportId");
        Map result = FastMap.newInstance() ;
        try {
            GenericValue serviceSupport = delegator.findByPrimaryKey("ServiceSupport", UtilMisc.toMap("serviceSupportId", serviceSupportId));
            result.put("serviceSupportVO",serviceSupport) ;
        } catch (GenericEntityException e) {
            logger.debug(e.getMessage(),e);
            return ServiceUtil.returnError(e.getMessage()) ;
        }
        return result ;
    }
}
