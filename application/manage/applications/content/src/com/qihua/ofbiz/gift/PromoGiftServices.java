package com.qihua.ofbiz.gift;

import javolution.util.FastList;
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
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * create by cc on 20180403
 */
public class PromoGiftServices {

    private final static Logger logger = LoggerFactory.getLogger(PromoGiftServices.class);
    public static final String module = PromoGiftServices.class.getName();
    public static final String resource = "CustomerMgrUiLabels";
    public static final String resourceError = "CustomerMgrErrorUiLabels";


    /**
     * 礼品列表展示
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findPromoGiftProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        // 获取本地delegator对像
        Delegator delegator = dctx.getDelegator();

        String giftName = (String) context.get("giftName");

        //总记录数
        int messageTemplateListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        // 查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;

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
        EntityExpr entityExprs = null;
        if (UtilValidate.isNotEmpty(giftName)) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("giftName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + giftName + "%")));
        }

        entityExprs = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("status"), EntityOperator.EQUALS, "1");

        filedExprs.add(entityExprs);

        if (filedExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
        }

        List<GenericValue> recordList = new ArrayList<GenericValue>();
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            // 去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);

            EntityListIterator pli = delegator.find("PromoGift", mainCond, null, null, null, findOpts);
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

        //根据创建时间做降序
        recordList = EntityUtil.orderBy(recordList, UtilMisc.toList("createdStamp DESC"));

        //返回的参数
        result.put("recordsList", recordList);
        result.put("totalSize", Integer.valueOf(messageTemplateListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 新增礼品 无编辑操作
     *
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> addPromoGiftProduct(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Delegator delegator = dctx.getDelegator();

        Map<String, Object> result = ServiceUtil.returnSuccess();

        String promoGiftId = (String) context.get("promoGiftId");
        String productId = (String) context.get("productId");
        String giftName = (String) context.get("productName");
        String giftType = (String) context.get("productTypeName");
        String isTop = (String) context.get("isTop");


        if (UtilValidate.isEmpty(promoGiftId)) {
            String [] productIds =productId.split(",");
            List<GenericValue> giftList = FastList.newInstance();
            for(String p:productIds){
                //查询是否已经被添加过。
                List<GenericValue> promoGiftList = delegator.findByAnd("PromoGift",UtilMisc.toMap("productId",p,"status","1"));
                //查询是否已经被添加过。
                if(promoGiftList!=null && promoGiftList.size()>0){
                   continue;
                }
                GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",p));
                String productName = product.getString("productName");
                String productTypeId = product.getString("productTypeId");
                GenericValue gift = delegator.makeValue("PromoGift");
                gift.setNextSeqId();
                gift.set("isTop", "Y");
                gift.set("productId", p);
                gift.set("giftName", productName);
                gift.set("status", "1");
                gift.set("giftType", "VIRTUAL_GOOD".equals(productTypeId)?"虚拟商品":"实物商品");
                giftList.add(gift);
            }
           if(giftList.size()>0){
                delegator.storeAll(giftList);
           }

        } else if (UtilValidate.isNotEmpty(promoGiftId)) {
            GenericValue promoGift = delegator.makeValue("PromoGift");
            if (UtilValidate.isNotEmpty(productId)) {
                promoGift.set("productId", productId);
            }
            if (UtilValidate.isNotEmpty(giftName)) {
                promoGift.set("giftName", giftName);
            }
            if (UtilValidate.isNotEmpty(giftType)) {
                promoGift.set("giftType", giftType);
            }
            promoGift.set("promoGiftId", promoGiftId);
            if (UtilValidate.isNotEmpty(isTop)) {
                promoGift.set("isTop", isTop);
            }
            try {
                promoGift.store();
            } catch (GenericEntityException e) {
                logger.debug(e.getMessage(), e);
                result.put("retCode",0);
                result.put("message",e.getMessage());
                return result;
            }
        }
        result.put("retCode",1);
        return result;
    }

    /**
     * 编辑页面修改保存
     *
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> savePromoGiftProduct(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Delegator delegator = dctx.getDelegator();

        Map<String, Object> result = ServiceUtil.returnSuccess();

        String promoGiftId = (String) context.get("promoGiftId");
        String giftName = (String) context.get("giftName");
        String giftType = (String) context.get("giftType");
        String isTop = (String) context.get("isTop");

        try {
            GenericValue promoGift = delegator.findByPrimaryKey("PromoGift", UtilMisc.toMap("promoGiftId", promoGiftId));

            if (UtilValidate.isNotEmpty(giftName)) {
                promoGift.set("giftName", giftName);
            }

            if (UtilValidate.isNotEmpty(giftType)) {
                promoGift.set("giftType", giftType);
            }

            if (UtilValidate.isNotEmpty(isTop)) {
                promoGift.set("isTop", isTop);
            }

            promoGift.store();

        } catch (GenericEntityException e) {
            logger.debug(e.getMessage(), e);
            return ServiceUtil.returnError(e.getMessage());
        }


        return ServiceUtil.returnSuccess();
    }


    /**
     * 修改礼品 查找返回数据
     *
     * @param dcxt
     * @param context
     * @return
     */
    public static Map<String, Object> editPromoGiftProduct(DispatchContext dcxt, Map<String, Object> context) {
        String promoGiftId = (String) context.get("promoGiftId");
        Delegator delegator = dcxt.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        try {
            GenericValue promoGift = delegator.findByPrimaryKey("PromoGift", UtilMisc.toMap("promoGiftId", promoGiftId));
            result.put("promoGift", promoGift);
        } catch (GenericEntityException e) {
            logger.debug(e.getMessage(), e);
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /**
     * 根据id来进行删除
     * 状态修改成0
     *
     * @param dcxt
     * @param context
     * @return
     */
    public static Map<String, Object> deletePromoGiftProduct(DispatchContext dcxt, Map<String, Object> context) {
        String promoGiftIds = (String) context.get("promoGiftIds");
        Delegator delegator = dcxt.getDelegator();

        try {

            if (UtilValidate.isNotEmpty(promoGiftIds)) {
                String[] promoGiftIdArrays = promoGiftIds.split(",");
                for (String promoGiftId : promoGiftIdArrays) {
                    GenericValue promoGift = delegator.findByPrimaryKey("PromoGift", UtilMisc.toMap("promoGiftId", promoGiftId));

                    promoGift.set("status", "0");
                    promoGift.store();

                }
            }
        } catch (GenericEntityException e) {
            logger.debug(e.getMessage(), e);
            return ServiceUtil.returnError(e.getMessage());
        }
        return ServiceUtil.returnSuccess();
    }
}
