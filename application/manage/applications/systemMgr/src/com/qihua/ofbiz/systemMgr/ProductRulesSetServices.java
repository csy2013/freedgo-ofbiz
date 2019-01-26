/*
 * 文件名：ProductRulesSetServices.java
 * 版权：启华
 * 描述：商品审核设置服务类
 * 修改人：hm
 * 修改时间：2018-5-12
 * 修改单号：
 * 修改内容：
 */

package com.qihua.ofbiz.systemMgr;

import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hm on 2018/5/12.
 */
public class ProductRulesSetServices {

    /**
     * 商品审核设置查询
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String,Object> findProductRulesSet(DispatchContext ctx, Map<String,?extends Object> context){
        //定义返回值result
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //获取调度器
        LocalDispatcher dispatcher = ctx.getDispatcher();
        //获取委托对象，可以对数据库进行增删改查
        Delegator delegator = ctx.getDelegator();
        try {
            //查数据库商品审核表ProductRules数据
            List<GenericValue> productRules = delegator.findList("ProductRules",null,null,null,null,false);
             if(UtilValidate.isNotEmpty(productRules)){
                 GenericValue productRule = productRules.get(0);
                 result.put("productRules",productRule);
             } else {
                 //否则,为空,那么要新增一条数据,先创建一个对象
                 GenericValue productRule = delegator.makeValue("ProductRules");
                 //从页面获取自营商品和商家商品
                 String physicalProductStatus =(String) context.get("physicalProductStatus");
                 String virtualProductStatus = (String) context.get("virtualProductStatus");
                 //更新ProductRule对象 Y--开启 N--关闭
                 productRule.set("rulesId","1");
                 productRule.set("physicalProductStatus","Y");
                 productRule.set("virtualProductStatus","Y");
                 result.put("productRules",productRule);
                 //保存增加
                 productRule.create();

             }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
     return result;
    }

    public static Map<String, Object> updateProductRulesSet(DispatchContext ctx, Map<String, ? extends Object> context){
        //通过serviceUtil获取result返回对象
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //获得数据库操作对象
        Delegator delegator = ctx.getDelegator();
        //获得调度器对象
        LocalDispatcher dispatcher = ctx.getDispatcher();
        try {
            //查询商品审核表ProductRules的数据
            List<GenericValue> productRules = delegator.findList("ProductRules",null,null,null,null,false);
           //如果不为空,更新数据
            if(UtilValidate.isNotEmpty(productRules)){
                GenericValue productRule = productRules.get(0);
                //从页面获取自营商品和商家商品
               String physicalProductStatus =(String) context.get("physicalProductStatus");
               String virtualProductStatus = (String) context.get("virtualProductStatus");
               //更新ProductRule对象
                productRule.set("physicalProductStatus",physicalProductStatus);
                productRule.set("virtualProductStatus",virtualProductStatus);
                //保存更改
                productRule.store();
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }




}
