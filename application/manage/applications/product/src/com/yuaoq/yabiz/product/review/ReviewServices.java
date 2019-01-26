package com.yuaoq.yabiz.product.review;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.ObjectType;
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
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


/**
 * 评价service
 *
 * @author 钱进 2016.06.06
 */
public class ReviewServices {
    /**
     * 查询评价标签列表 add by qianjin 2016.06.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getReviewLabelList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //当前用户登录信息  
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<GenericValue> recordsList = FastList.newInstance();

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

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();

        dynamicView.addMemberEntity("RL", "ReviewLabel");
        dynamicView.addAlias("RL", "reviewLabelId");
        dynamicView.addAlias("RL", "name");
        dynamicView.addAlias("RL", "star");
        dynamicView.addAlias("RL", "contentId");
        dynamicView.addAlias("RL", "isUse");
        dynamicView.addAlias("RL", "description");
        dynamicView.addMemberEntity("PC", "ProductCategory");
        dynamicView.addAlias("PC", "productCategoryId");
        dynamicView.addAlias("PC", "categoryName");
        dynamicView.addViewLink("RL", "PC", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productCategoryId", "productCategoryId"));
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();

        //排序字段名称
        String sortField = "reviewLabelId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        //按标签名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("name"))) {
            andExprs.add(EntityCondition.makeCondition("name", EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("name") + "%")));
        }
        //按标签编号迷糊查询
        if (UtilValidate.isNotEmpty(context.get("reviewLabelId"))) {
            andExprs.add(EntityCondition.makeCondition("reviewLabelId", EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("reviewLabelId") + "%")));
        }
        //按评价星级精确查询
        if (UtilValidate.isNotEmpty(context.get("star"))) {
            andExprs.add(EntityCondition.makeCondition("star", EntityOperator.EQUALS, (BigDecimal) context.get("star")));
        }
        //按启用状态精确查询
        if (UtilValidate.isNotEmpty(context.get("isUse"))) {
            andExprs.add(EntityCondition.makeCondition("isUse", EntityOperator.EQUALS, context.get("isUse")));
        }

        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, orderBy, findOpts);
            // 获取分页所需的记录集合
            recordsList = pli.getPartialList(lowIndex, viewSize);

            // 获取总记录数
            totalSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > totalSize) {
                highIndex = totalSize;
            }

            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
        }

        //返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(totalSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }


    /**
     * 根据ID删除评价标签,可批量删除 add by qianjin 2016.06.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> delReviewLabel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //获取ids参数
        String ids = (String) context.get("ids");
        //转换成list
        List idList = FastList.newInstance();
        for (String id : ids.split(",")) {
            idList.add(id);
        }
        //编辑where条件
        EntityCondition mainCond = EntityCondition.makeCondition("reviewLabelId", EntityOperator.IN, idList);

        try {
            //delegator.removeByCondition("ProductReviewLabel", mainCond);
            delegator.removeByCondition("ReviewLabel", mainCond);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 新增评价标签 add by qianjin 2016.06.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> addReviewLabel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String reviewLabelId = (String) context.get("reviewLabelId");
        String name = (String) context.get("name");
        BigDecimal star = (BigDecimal) context.get("star");
        String contentId = (String) context.get("contentId");
        String isUse = (String) context.get("isUse");
        String description = (String) context.get("description");
        String productCategoryId = (String) context.get("productCategoryId");
        try {
            //新增一条评价标签记录
            GenericValue rl_gv = delegator.makeValue("ReviewLabel", UtilMisc.toMap("reviewLabelId", reviewLabelId));
            rl_gv.set("name", name);
            rl_gv.set("star", star);
            rl_gv.set("contentId", contentId);
            rl_gv.set("isUse", isUse);
            rl_gv.set("description", description);
            rl_gv.set("productCategoryId", productCategoryId);
            rl_gv.create();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 修改评价标签 add by qianjin 2016.06.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editReviewLabel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String reviewLabelId = (String) context.get("reviewLabelId");
        String name = (String) context.get("name");
        BigDecimal star = (BigDecimal) context.get("star");
        String contentId = (String) context.get("contentId");
        String isUse = (String) context.get("isUse");
        String description = (String) context.get("description");
        String productCategoryId = (String) context.get("productCategoryId");

        try {
            //根据ID查询评价标签
            GenericValue rl_gv = delegator.findByPrimaryKey("ReviewLabel", UtilMisc.toMap("reviewLabelId", reviewLabelId));
            rl_gv.set("name", name);
            rl_gv.set("star", star);
            rl_gv.set("contentId", contentId);
            rl_gv.set("isUse", isUse);
            rl_gv.set("description", description);
            rl_gv.set("productCategoryId", productCategoryId);
            rl_gv.store();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 根据ID获取评价标签ID add by qianjin 2016.06.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getReviewLabelById(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Map paramMap = FastMap.newInstance();
        //参数
        String reviewLabelId = (String) context.get("reviewLabelId");

        try {
            //根据ID获取评价标签记录
            GenericValue gv = delegator.findByPrimaryKey("ReviewLabel", UtilMisc.toMap("reviewLabelId", reviewLabelId));
            String treeName = "";
            if (UtilValidate.isNotEmpty(gv) && UtilValidate.isNotEmpty(gv.get("productCategoryId"))) {
                GenericValue catalog = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", gv.get("productCategoryId")));
                if (UtilValidate.isNotEmpty(catalog)) {
                    treeName = (String) catalog.get("categoryName");
                }
            }
            result.put("treeName", treeName);
            result.put("record", gv);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取标签评价表新的主键ID add by qianjin 2016.06.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getReviewLabelNextSeqId(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        //获取标签评价表新的ID
        String nextSeqId = delegator.getNextSeqId("ReviewLabel").toString();
        result.put("nextSeqId", nextSeqId);
        return result;
    }

    /**
     * 修改评价标签启用状态 add by qianjin 2016.06.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editReviewLabelIsUse(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String reviewLabelId = (String) context.get("reviewLabelId");
        String isUse = (String) context.get("isUse");

        try {
            //根据ID查询评价标签
            GenericValue rl_gv = delegator.findByPrimaryKey("ReviewLabel", UtilMisc.toMap("reviewLabelId", reviewLabelId));
            rl_gv.set("isUse", isUse);
            rl_gv.store();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return result;
    }
}
