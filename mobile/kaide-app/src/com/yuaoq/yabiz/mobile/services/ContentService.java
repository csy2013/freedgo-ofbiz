package com.yuaoq.yabiz.mobile.services;

import com.yuaoq.yabiz.app.security.common.TokenUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by changsy on 2017/11/1.
 */
public class ContentService {
    
    /**
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> indexBanner(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        Delegator delegator = dispatchContext.getDelegator();
        LocalDispatcher localDispatcher = dispatchContext.getDispatcher();
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dispatchContext.getDispatcher();
        String baseUrl = UtilProperties.getMessage("application.properties", "image.base.url", locale);
        List<Map> resultData = FastList.newInstance();
        try {
            //选择默认首页模块
            GenericValue indexTemplate = EntityUtil.getFirst(delegator.findByAnd("WebSiteIndexTemplate", UtilMisc.toMap("webSiteId", "app", "isDefault", "Y")));
            if (UtilValidate.isNotEmpty(indexTemplate)) {
                //Banner
                
                List<GenericValue> indexAdvertRels = delegator.findByAnd("WebSiteIndexTemplateContent", UtilMisc.toMap("siteIndexTemplateId", indexTemplate.get("siteIndexTemplateId")), UtilMisc.toList("sequenceNum"));
                if (UtilValidate.isNotEmpty(indexAdvertRels)) {
                    for (int j = 0; j < indexAdvertRels.size(); j++) {
                        GenericValue advertRels = indexAdvertRels.get(j);
                        Long seq = advertRels.getLong("sequenceNum");
                        String advertId = advertRels.getString("advertId");
                        getBannerData(delegator, baseUrl, advertId, resultData,"A");
                        
                    }
                }
            }
            
        } catch (GenericEntityException e)
        
        {
            e.printStackTrace();
            
        }
        result.put("resultData", resultData);
        return result;
    }
    
   
    
    /**
     *获取魔方的数据
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> indexBall(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        Delegator delegator = dispatchContext.getDelegator();
        LocalDispatcher localDispatcher = dispatchContext.getDispatcher();
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dispatchContext.getDispatcher();
        String baseUrl = UtilProperties.getMessage("application.properties", "image.base.url", locale);
        List<Map> resultData = FastList.newInstance();
        try {
            //选择默认首页模块
            GenericValue indexTemplate = EntityUtil.getFirst(delegator.findByAnd("WebSiteIndexTemplate", UtilMisc.toMap("webSiteId", "app", "isDefault", "Y")));
            if (UtilValidate.isNotEmpty(indexTemplate)) {
                //Banner
                
                List<GenericValue> indexAdvertRels = delegator.findByAnd("WebSiteIndexTemplateContent", UtilMisc.toMap("siteIndexTemplateId", indexTemplate.get("siteIndexTemplateId")), UtilMisc.toList("sequenceNum"));
                if (UtilValidate.isNotEmpty(indexAdvertRels)) {
                    for (int j = 0; j < indexAdvertRels.size(); j++) {
                        GenericValue advertRels = indexAdvertRels.get(j);
                        Long seq = advertRels.getLong("sequenceNum");
                        String advertId = advertRels.getString("advertId");
                        getBallData(delegator, baseUrl, advertId, resultData,"A");
                        
                    }
                }
            }
            
        } catch (GenericEntityException e)
        
        {
            e.printStackTrace();
            
        }
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 获取店铺性价比推荐
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> indexAct(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        Delegator delegator = dispatchContext.getDelegator();
        LocalDispatcher localDispatcher = dispatchContext.getDispatcher();
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dispatchContext.getDispatcher();
        String baseUrl = UtilProperties.getMessage("application.properties", "image.base.url", locale);
        List<Map> resultData = FastList.newInstance();
        try {
            //选择默认首页模块
            GenericValue indexTemplate = EntityUtil.getFirst(delegator.findByAnd("WebSiteIndexTemplate", UtilMisc.toMap("webSiteId", "app", "isDefault", "Y")));
            if (UtilValidate.isNotEmpty(indexTemplate)) {
                //Banner
                
                List<GenericValue> indexAdvertRels = delegator.findByAnd("WebSiteIndexTemplateContent", UtilMisc.toMap("siteIndexTemplateId", indexTemplate.get("siteIndexTemplateId")), UtilMisc.toList("sequenceNum"));
                if (UtilValidate.isNotEmpty(indexAdvertRels)) {
                    for (int j = 0; j < indexAdvertRels.size(); j++) {
                        GenericValue advertRels = indexAdvertRels.get(j);
                        Long seq = advertRels.getLong("sequenceNum");
                        String advertId = advertRels.getString("advertId");
                        getActData(delegator, baseUrl, advertId, resultData,"A");
                        
                    }
                }
            }
            
        } catch (GenericEntityException e)
        
        {
            e.printStackTrace();
            
        }
        result.put("resultData", resultData);
        return result;
    }
    public static void getBannerData(Delegator delegator, String baseUrl,String advertId,List resultData,String defineType) {
        List<GenericValue> banners = null;
        try {
            banners = delegator.findByAnd("AdvertAndContent", UtilMisc.toMap("advertTypeId", "banner", "advertId", advertId, "defineType", defineType), UtilMisc.toList("sequenceNum"));
            if (UtilValidate.isNotEmpty(banners)) {
                for (int i = 0; i < banners.size(); i++) {
                    Map banner = FastMap.newInstance();
                    GenericValue advertContent = banners.get(i);
                    String imgUrl = advertContent.getString("imgSrc");
                    imgUrl = baseUrl + imgUrl;
                    String relateVal = (String) advertContent.get("relationId");
                    String imgOrder = advertContent.get("sequenceNum").toString();
                    String relationTypeId = advertContent.getString("relationTypeId");
                    banner.put("imgUrl",imgUrl);
                    banner.put("relateVal",relateVal);
                    banner.put("imgOrder",imgOrder);
                    banner.put("relationTypeId",relationTypeId);
                    resultData.add(banner);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
    }
    
    public static void getBallData(Delegator delegator, String baseUrl,String advertId,List resultData,String defineType)  {
        //获取ball
        List<GenericValue> balls = null;
        try {
            balls = delegator.findByAnd("AdvertAndContent", UtilMisc.toMap("advertTypeId", "ball", "advertId", advertId, "defineType", defineType), UtilMisc.toList("sequenceNum"));
            
            if (UtilValidate.isNotEmpty(balls)) {
    
                for (int i = 0; i < balls.size(); i++) {
                    Map ball = FastMap.newInstance();
                    GenericValue advertContent = balls.get(i);
                    String imgUrl = advertContent.getString("imgSrc");
                    imgUrl = baseUrl + imgUrl;
                    String relateVal = (String) advertContent.get("relationId");
                    String relationTypeId = advertContent.getString("relationTypeId");
                    String imgOrder = advertContent.get("sequenceNum").toString();
                    String title = (String) advertContent.get("advertContentName");
                 
                    ball.put("imgUrl",imgUrl);
                    ball.put("relateVal",relateVal);
                    ball.put("imgOrder",imgOrder);
                    ball.put("relationTypeId",relationTypeId);
                    ball.put("title",title);
                    resultData.add(ball);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
    }
    public static void getActData(Delegator delegator, String baseUrl,String advertId,List resultData,String defineType)  {
        //获取ball
        List<GenericValue> act3 = null;
        try {
            act3 = delegator.findByAnd("AdvertAndContent", UtilMisc.toMap("advertTypeId", "act3", "advertId", advertId, "defineType", defineType), UtilMisc.toList("sequenceNum"));
            
            if (UtilValidate.isNotEmpty(act3)) {
                
                for (int i = 0; i < act3.size(); i++) {
                    Map act = FastMap.newInstance();
                    GenericValue advertContent = act3.get(i);
                    String imgUrl = advertContent.getString("imgSrc");
                    imgUrl = baseUrl + imgUrl;
                    String relateVal = (String) advertContent.get("relationId");
                    String relationTypeId = advertContent.getString("relationTypeId");
                    String imgOrder = advertContent.get("sequenceNum").toString();
                    String title = (String) advertContent.get("advertContentName");
    
                    act.put("imgUrl",imgUrl);
                    act.put("relateVal",relateVal);
                    act.put("imgOrder",imgOrder);
                    act.put("relationTypeId",relationTypeId);
                    act.put("title",title);
                    resultData.add(act);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
    }
    
    public static Map<String, Object> getAppToken(DispatchContext dctx, Map<String, ? extends Object> context){
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String tokenName = UtilProperties.getPropertyValue("application.properties", "app.security.token.header");
        String userName = "";
        String token = (String) context.get("X-Authorization");
        if (UtilValidate.isNotEmpty(tokenName)) {
            TokenUtils tokenUtils = new TokenUtils();
            String secret = UtilProperties.getPropertyValue("application.properties", "app.security.jwt.tokenSigningKey");
            tokenUtils.setSecret(secret);
            userName = tokenUtils.getUsernameFromToken(token);
            Delegator delegator = dctx.getDelegator();
            try {
                GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
                result.put("userLogin", userLogin);
            } catch (GenericEntityException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return result;
    }
}
