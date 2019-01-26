package com.yuaoq.yabiz.mobile.services;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by changsy on 2017/11/1.
 */
public class PartyService {
    
    public static Map<String, Object> personRegister(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String nickName = (String) context.get("nickName");
        String memberId = (String) context.get("memberId");
        String sex = (String) context.get("sex");
        String headImgUrl = (String) context.get("headImgUrl");
        String token = (String) context.get("token");
        String phone = (String) context.get("phone");
        String password = (String) context.get("password");
        String unionId = (String) context.get("unionId");
        String isNewCust = (String) context.get("isNewCust");
        String partyId = (String) context.get("partyId");
        String openId = (String) context.get("wxAppOpenId");
        String mallId = (String) context.get("mallId");
        Delegator delegator = dispatchContext.getDelegator();
        LocalDispatcher localDispatcher = dispatchContext.getDispatcher();
        Map<String, Object> resultData = FastMap.newInstance();
        
        //创建party And userLogin
        try {
            //创建邮件用户登录名
            if (UtilValidate.isEmpty(sex)) {
                sex = "";
            } else if (sex.equals("0")) {
                sex = "";
            } else if (sex.equals("1")) {
                sex = "M";
            } else {
                sex = "F";
            }
            if (UtilValidate.isNotEmpty(isNewCust)) {
                isNewCust = "N";
            }
            if (UtilValidate.isEmpty(partyId)) {
                
                List<GenericValue> userLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("userLoginId", phone));
                List<GenericValue> persons = delegator.findByAnd("Person", UtilMisc.toMap("mobile", phone));
                if (UtilValidate.isEmpty(persons) && UtilValidate.isEmpty(userLogins)) {
                    
                    Map<String, Object> newUserLogin = localDispatcher.runSync("createPersonAndUserLogin", UtilMisc.toMap("userLoginId", phone, "nickname", nickName, "member_id", memberId, "currentPassword", password, "currentPasswordVerify", password, "gender", sex, "headphoto", headImgUrl, "unionId", unionId, "lastToken", token, "mobile", phone, "partyCategory", "MEMBER", "isNewCust", isNewCust, "wxAppOpenId", openId));
                    if (ServiceUtil.isError(newUserLogin)) {
                        result = ServiceUtil.returnError(ServiceUtil.getErrorMessage(newUserLogin));
                        resultData.put("resultData", result);
                        return resultData;
                    }
                    //新会员注册,送积分
                    Map<String, Object> resultData1 = localDispatcher.runSync("userScoreCodeSend", UtilMisc.toMap("memberId", memberId, "mallId", mallId, "sendType", "NEW_CUST_REGISTER", "userLoginId", phone));
                    if (ServiceUtil.isError(resultData1)) {
                        result = ServiceUtil.returnError(ServiceUtil.getErrorMessage(resultData1));
                        resultData.put("resultData", result);
                        
                    }
                    //处理用户收货，需要在凯德提供的数据做处理
                    List<GenericValue> addressTemp = delegator.findByAnd("PartyPostAddressTemp", UtilMisc.toMap("memberId", memberId));
                    if (UtilValidate.isNotEmpty(addressTemp)) {
                        for (int i = 0; i < addressTemp.size(); i++) {
                            try {
                                GenericValue address = addressTemp.get(i);
                                if (UtilValidate.isNotEmpty(address) && UtilValidate.isNotEmpty(address.getString("mobile")) && UtilValidate.isNotEmpty(address.getString("address1")) && UtilValidate.isNotEmpty(address.getString("toName"))) {
                                    String is_def = address.getString("isDefault");
                                    String toName = address.getString("toName");
                                    String address1 = address.getString("address1");
                                    //创建用户的收货地址信息
                                    String city = "_NA_";
                                    String countyGeoId = "_NA_";
                                    String postalCode = "_NA_";
                                    String stateProvinceGeoId = "_NA_";
                                    if (UtilValidate.isNotEmpty(address1)) {
                                        String[] splitArray = address1.split("\\ ");
                                        if (splitArray.length > 3) {
                                            stateProvinceGeoId = splitArray[0];
                                            city = splitArray[1];
                                            countyGeoId = splitArray[2];
                                            
                                            if (stateProvinceGeoId.endsWith("省") || stateProvinceGeoId.endsWith("市")) {
                                                stateProvinceGeoId = stateProvinceGeoId.substring(0, stateProvinceGeoId.length() - 1);
                                                GenericValue startGeo = EntityUtil.getFirst(delegator.findByAnd("Geo", UtilMisc.toMap("geoName", stateProvinceGeoId)));
                                                if (UtilValidate.isNotEmpty(startGeo)) {
                                                    stateProvinceGeoId = startGeo.getString("geoId");
                                                }
                                            }
                                            GenericValue cityGeo = EntityUtil.getFirst(delegator.findByAnd("Geo", UtilMisc.toMap("geoName", city)));
                                            if (UtilValidate.isNotEmpty(cityGeo)) {
                                                city = cityGeo.getString("geoId");
                                            }
                                            
                                            GenericValue countyGeo = EntityUtil.getFirst(delegator.findByAnd("Geo", UtilMisc.toMap("geoName", countyGeoId)));
                                            if (UtilValidate.isNotEmpty(countyGeo)) {
                                                countyGeoId = countyGeo.getString("geoId");
                                            };
    
                                            address1 = splitArray[3];
                                            
                                        }
                                    }
                                    String mobilePhone = address.getString("mobile");
                                    
                                    String attnName = toName;
//                                    GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phone));
                                    GenericValue userLogin = (GenericValue) newUserLogin.get("newUserLogin");
                                    Map<String, Object> serviceIn = UtilMisc.toMap("address1", address1, "city", city, "partyId", userLogin.get("partyId"), "countryGeoId", "CHN", "countyGeoId", countyGeoId, "mobilePhone", mobilePhone, "attnName", attnName, "postalCode", postalCode, "stateProvinceGeoId", stateProvinceGeoId, "userLogin", userLogin, "toName", attnName, "isDefault", is_def);
                                    result = localDispatcher.runSync("createPartyPostalAddress", serviceIn);
                                    String contactMechId = (String) result.get("contactMechId");
                                    String contactMechPurposeTypeId = "SHIPPING_LOCATION";
                                    serviceIn = UtilMisc.toMap("contactMechId", result.get("contactMechId"), "partyId", userLogin.get("partyId"), "contactMechPurposeTypeId", contactMechPurposeTypeId, "userLogin", userLogin);
                                    result = localDispatcher.runSync("createPartyContactMechPurpose", serviceIn);
                                    //设置为默认货物目的地地址
                                    if (is_def.equals("Y")) {
                                        serviceIn = UtilMisc.toMap("productStoreId", null, "defaultShipAddr", contactMechId, "partyId", userLogin.get("partyId"), "userLogin", userLogin);
                                        result = localDispatcher.runSync("setPartyProfileDefaults", serviceIn);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                               return  ServiceUtil.returnError(e.getMessage());
                            }
                        }
                    }
                    
                    resultData.put("resultData", newUserLogin);
                    
                    
                } else {
                    if (UtilValidate.isNotEmpty(userLogins)) {
                        GenericValue userLogin = EntityUtil.getFirst(userLogins);
                        userLogin.setString("lastToken", token);
                        userLogin.store();
                        partyId = userLogin.getString("partyId");
                        resultData.put("resultData", userLogin);
                        GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
                        if (UtilValidate.isNotEmpty(person)) {
                            person.put("nickname", nickName);
                            person.put("unionId", unionId);
                            person.put("headphoto", headImgUrl);
                            person.put("member_id", memberId);
                            person.put("gender", sex);
                            person.put("mobile", phone);
                            person.put("isNewCust", isNewCust);
                            person.put("wxAppOpenId", openId);
                            person.store();
                        }
                        GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));
                        if (UtilValidate.isNotEmpty(party)) {
                            party.put("partyCategory", "MEMBER");
                            party.store();
                        }

                    }else{
                        //创建登录
                        Map<String, Object> newUserLogin = localDispatcher.runSync("createUserLogin", UtilMisc.toMap("userLoginId", phone, "enabled", "Y", "currentPassword", password, "currentPasswordVerify", password, "partyId", partyId, "requirePasswordChange", "N"));
                        if (ServiceUtil.isError(newUserLogin)) {
                            result = ServiceUtil.returnError(ServiceUtil.getErrorMessage(newUserLogin));
                            resultData.put("resultData", result);
                            return resultData;
                        }
                    }
                    if (UtilValidate.isNotEmpty(persons)) {
        
                        GenericValue person = EntityUtil.getFirst(persons);
                        partyId = person.getString("partyId");
                        userLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId));
                        if (UtilValidate.isNotEmpty(userLogins)) {
                            for (int i = 0; i < userLogins.size(); i++) {
                                GenericValue userLogin = userLogins.get(i);
                                userLogin.setString("lastToken", token);
                                userLogin.store();
                                resultData.put("resultData", userLogin);
                            }
                        }
                        person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
                        if (UtilValidate.isNotEmpty(person)) {
                            person.put("nickname", nickName);
                            person.put("unionId", unionId);
                            person.put("headphoto", headImgUrl);
                            person.put("member_id", memberId);
                            person.put("gender", sex);
                            person.put("mobile", phone);
                            person.put("isNewCust", isNewCust);
                            person.put("wxAppOpenId", openId);
                            person.store();
                        }
                        GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));
                        if (UtilValidate.isNotEmpty(party)) {
                            party.put("partyCategory", "MEMBER");
                            party.store();
                        }
                    }else{
                        GenericValue party = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
                        party.set("nickname", nickName);
                        party.set("member_id", memberId);
                        party.set("gender", sex);
                        party.set("headphoto", headImgUrl);
                        party.set("unionId", unionId);
                        party.set("mobile", phone);
                        party.set("wxAppOpenId", openId);
                        party.store();
        
                        GenericValue party1 = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));
                        party1.set("partyCategory", "MEMBER");
                        party1.store();
                    }
                }
            } else {
                GenericValue party = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
                party.set("nickname", nickName);
                party.set("member_id", memberId);
                party.set("gender", sex);
                party.set("headphoto", headImgUrl);
                party.set("unionId", unionId);
                party.set("mobile", phone);
                party.set("wxAppOpenId", openId);
                party.store();
                
                GenericValue party1 = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));
                party1.set("partyCategory", "MEMBER");
                party1.store();
                
                
                List<GenericValue> userLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isEmpty(userLogins)) {
                    
                    //创建登录
                    Map<String, Object> newUserLogin = localDispatcher.runSync("createUserLogin", UtilMisc.toMap("userLoginId", phone, "enabled", "Y", "currentPassword", password, "currentPasswordVerify", password, "partyId", partyId, "requirePasswordChange", "N"));
                    if (ServiceUtil.isError(newUserLogin)) {
                        result = ServiceUtil.returnError(ServiceUtil.getErrorMessage(newUserLogin));
                        resultData.put("resultData", result);
                        return resultData;
                    }
                    resultData.put("resultData", newUserLogin);
                } else {
                    
                    GenericValue userLogin = userLogins.get(0);
                    userLogin.set("lastToken", token);
                    userLogin.store();
                    resultData.put("resultData", userLogin);
                }
                
            }
            
        } catch (GenericServiceException e) {
            e.printStackTrace();
            result = ServiceUtil.returnError(e.getMessage());
            resultData.put("resultData", result);
            return resultData;
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(resultData.get("resultData"))) {
            resultData.put("resultData", FastMap.newInstance());
        }
        
        return resultData;
    }
    
    public static Map<String, Object> querySettleAmount(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            List<GenericValue> settles = delegator.findByAnd("PartySettleAmount", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            result.put("resultData", settles);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    public static Map<String, Object> queryPartyFavoriteProduct(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            List<GenericValue> favorites = EntityUtil.filterByDate(delegator.findByAnd("PartyFavoriteProduct", UtilMisc.toMap("partyId", userLogin.get("partyId"))));
            result.put("resultData", favorites);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    
    /**
     * 【APP】获取用户足迹列表
     *
     * @return
     * @author wcy
     * @date 2017-5-8
     */
    public static Map<String, Object> getPartyBrowseHistorys(DispatchContext dcx, Map<String, ? extends Object> context) {
        Delegator delegator = dcx.getDelegator();
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        //获取本地化
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = TimeZone.getDefault();
        
        //获取登录账号
        String userLoginId = (String) context.get("userLoginId");
        
        GenericValue userLogin;
        //获取会员编号
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            
            
            //分页参数
            Integer viewSize = 10;  //每页显示记录数
            if (UtilValidate.isNotEmpty((String) context.get("viewSize"))) {
                viewSize = Integer.valueOf((String) context.get("viewSize"));
            }
            
            Integer viewIndex = 0; //当前页
            if (UtilValidate.isNotEmpty((String) context.get("viewIndex"))) {
                viewIndex = Integer.valueOf((String) context.get("viewIndex"));
            }
            int lowIndex = viewIndex + 1;
            int highIndex = viewIndex + viewSize;
            List<Map> returnList = FastList.newInstance();
            
            //动态对象创建
            DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
            List<String> fieldsToSelect = FastList.newInstance();
            dynamicViewViewEntity.addMemberEntity("PBH", "PartyBrowseHistory");   //浏览足迹表
            dynamicViewViewEntity.addAlias("PBH", "productId", null, null, null, true, null);
            dynamicViewViewEntity.addAlias("PBH", "partyId");
            dynamicViewViewEntity.addAlias("PBH", "partyBrowseHistoryId");
            dynamicViewViewEntity.addAlias("PBH", "createdStamp");
            
            
            fieldsToSelect.add("productId");
            fieldsToSelect.add("partyId");
            fieldsToSelect.add("partyBrowseHistoryId");
            fieldsToSelect.add("createdStamp");
            
            dynamicViewViewEntity.setGroupBy(fieldsToSelect);
            
            List<EntityCondition> conditions = FastList.newInstance();
            conditions.add(EntityCondition.makeCondition("partyId", userLogin.getString("partyId")));
            
            //浏览足迹记录分页查询,去除重复
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            findOpts.setDistinct(true);
            
            //创建时间降序操作
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity,
                    EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList
                            ("-createdStamp"), findOpts);
            List<GenericValue> historys = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList(); //总记录数
            eli.close();
            
            BigDecimal price = BigDecimal.ZERO;
            String productId = "";
            String imageUrl = "";
            //遍历获取商品价格、商品名称、商品图片
            for (GenericValue collection : historys) {
                Map historyMap = FastMap.newInstance();
                productId = collection.getString("productId");
                Boolean isEnabled = false;
                
                //获取商品信息
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                if (product == null) continue;
                
                //获取商品内容
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    String baseUrl = UtilProperties.getMessage("content.properties", "kaide.images.baseUrl", locale);
                    imageUrl = baseUrl + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId");
                }
                
                //获取商品价格
                List<GenericValue> productPrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));//默认价格，即销售价
                if (productPrices != null && productPrices.size() == 1) {
                    price = productPrices.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                
                
                //商品是否失效
                if (UtilValidate.areEqual("Y", product.get("isOnline")) && UtilValidate.areEqual("Y", product.get("isVerify")) && UtilValidate.areEqual("N", product.get("isDel")) && UtilValidate.isEmpty(product.get("mainProductId"))) {
                    if (product.getTimestamp("introductionDate").compareTo(UtilDateTime.nowTimestamp()) <= 0) {
                        if (UtilValidate.isNotEmpty(product.get("salesDiscontinuationDate"))) {
                            if (product.getTimestamp("salesDiscontinuationDate").compareTo(UtilDateTime.nowTimestamp()) >= 0) {
                                isEnabled = true;
                            }
                        } else {
                            isEnabled = true;
                        }
                    }
                }
                
                historyMap.put("browseHistoryId", collection.getString("partyBrowseHistoryId"));
                historyMap.put("productUrl", UtilValidate.isEmpty(imageUrl) ? "" : imageUrl);
                historyMap.put("productName", UtilValidate.isEmpty(product.getString("productName")) ? "" : product.getString("productName"));
                historyMap.put("productPrice", price);
                historyMap.put("isEnabled", isEnabled);
                historyMap.put("productId", productId);
                historyMap.put("createdStamp", UtilDateTime.timeStampToString(collection.getTimestamp("createdStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                returnList.add(historyMap);
            }
            resultData.put("max", resultSize);
            resultData.put("returnList", returnList);
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            return ServiceUtil.returnError(e.getMessage());
        }
        return resultData;
    }
    
    
    /**
     * 【APP】清空足迹
     *
     * @return
     * @author wcy
     * @date 2017-5-8
     */
    public static Map<String, Object> deleteByPartyBrowseHistoryId(DispatchContext dcx, Map<String, ? extends Object> context) {
        Delegator delegator = dcx.getDelegator();
        //获取本地化
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        //获取登录账号
        String userLoginId = (String) context.get("userLoginId");
        
        GenericValue userLogin;
        //获取会员编号
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            return ServiceUtil.returnError(e.getMessage());
        }

        String browseHistoryId = "";   //足迹编号
        Boolean isAllClear = false;       //是否清空
        if (UtilValidate.isNotEmpty((String) context.get("browseHistoryId"))) {
            browseHistoryId = (String) context.get("browseHistoryId");
        }
        if (UtilValidate.isNotEmpty((String) context.get("isAllClear"))) {
            isAllClear = Boolean.valueOf((String) context.get("isAllClear"));
        }
        
        try {
            if (true == isAllClear) {
                delegator.removeByAnd("PartyBrowseHistory", UtilMisc.toMap("partyId", userLogin.getString("partyId")));
            }
            if (UtilValidate.isNotEmpty(browseHistoryId)) {
                delegator.removeByAnd("PartyBrowseHistory", UtilMisc.toMap("partyBrowseHistoryId", browseHistoryId));
            }
            
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return resultData;
    }
    
    /**
     * 保存浏览历史记录
     */
    public static Map<String, Object> savePartyBrowse(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        Delegator delegator = dcx.getDelegator();
        String productIds = (String) context.get("productIds");
        
        if (UtilValidate.isEmpty(productIds)) {
            return ServiceUtil.returnError("产品ID不能为空");
            
        }
        for (String productId : productIds.split(",")) {
            try {
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                if (UtilValidate.isEmpty(product)) {
                    continue;
                }
                GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
                //删除重复数据
                List<GenericValue> historys = delegator.findByAnd("PartyBrowseHistory", UtilMisc.toMap("partyId", userLogin.get("partyId"), "productId", product.get("productId")));
                if (UtilValidate.isNotEmpty(historys)) {
                    delegator.removeAll(historys);
                }
                GenericValue partyBrowseHistory = delegator.makeValue("PartyBrowseHistory");
                partyBrowseHistory.set("partyBrowseHistoryId", delegator.getNextSeqId("PartyBrowseHistory"));
                partyBrowseHistory.set("partyId", userLogin.get("partyId"));
                partyBrowseHistory.set("productId", productId);
                delegator.create(partyBrowseHistory);
            } catch (GenericEntityException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return resultData;
    }
    
    
    /**
     * 提交意见反馈(base64)
     *
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> createFeedback(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {
        Delegator delegator = dcx.getDelegator();
        String userLoginId = (String) context.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String feedbackContent = (String) context.get("feedbackContent");
        
        if (UtilValidate.isEmpty(feedbackContent)) {
            return ServiceUtil.returnError("反馈内容不能为空");
            
        }
        String imageIds = (String) context.get("imageIds");
        String[] images = null;
        if (UtilValidate.isNotEmpty(imageIds)) {
            images = imageIds.split(",");
        }
        
        List<GenericValue> toBeSaved = new LinkedList<GenericValue>();
        String partyId = userLogin.getString("partyId");
        // 创建反馈信息
        GenericValue feedback = delegator.makeValue("Feedback");
        String feedbackId = delegator.getNextSeqId("Feedback");
        feedback.set("feedbackId", feedbackId);
        feedback.set("createPartyId", partyId);
        feedback.set("createDate", UtilDateTime.nowTimestamp());
        feedback.set("feedbackContent", feedbackContent);
        toBeSaved.add(feedback);
        // 创建图片关联关系
        if (images != null) {
            for (String contentId : images) {
                GenericValue feedback_Content = delegator.makeValue("FeedbackContent");
                feedback_Content.set("feedbackId", feedbackId);
                feedback_Content.set("contentId", contentId);
                toBeSaved.add(feedback_Content);
            }
        }
        delegator.storeAll(toBeSaved);
        
        return resultData;
    }
    
    /**
     * 我的反馈列表 新增 Add By gss
     *
     * @return
     */
    public static Map<String, Object> getFeedBackList(DispatchContext dcx, Map<String, ? extends Object> context) {
        Delegator delegator = dcx.getDelegator();
        String userLoginId = (String) context.get("userLoginId");
        
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty((String) context.get("viewIndex"))) {
            viewIndex = Integer.valueOf((String) context.get("viewIndex"));
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            return ServiceUtil.returnError("当前已查询数量不能为空");
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty((String) context.get("viewSize"))) {
            viewSize = Integer.valueOf((String) context.get("viewSize"));
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("F", "Feedback");
        dynamicViewViewEntity.addAlias("F", "feedbackId");
        dynamicViewViewEntity.addAlias("F", "createPartyId");
        dynamicViewViewEntity.addAlias("F", "createDate");
        dynamicViewViewEntity.addAlias("F", "feedbackContent");
        dynamicViewViewEntity.addAlias("F", "replyDate");
        dynamicViewViewEntity.addAlias("F", "replyContent");
        dynamicViewViewEntity.addAlias("F", "contactMethod");
        List<EntityCondition> conditions = FastList.newInstance();
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            
            GenericValue userLogin = null;
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(userLogin)) {
                conditions.add(EntityCondition.makeCondition("createPartyId", userLogin.getString("partyId")));
                Boolean beganTransaction = TransactionUtil.begin();
                EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-createDate"), findOpts);
                List<GenericValue> feedbackList = eli.getPartialList(lowIndex, viewSize);
                int resultSize = eli.getResultsSizeAfterPartialList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                List<Map> returnList = FastList.newInstance();
                for (GenericValue feedback : feedbackList) {
                    Map map = FastMap.newInstance();
                    //反馈ID
                    map.put("feedbackId", feedback.get("feedbackId"));
                    //反馈内容
                    map.put("feedbackContent", feedback.get("feedbackContent"));
                    //客服回复内容
                    map.put("replyContent", feedback.get("replyContent"));
                    //反馈时间
                    map.put("createDate", feedback.getTimestamp("createDate"));
                    //客服回复时间
                    map.put("replyDate", UtilValidate.isNotEmpty(feedback.get("replyDate")) ? feedback.getTimestamp("replyDate") : "");
                    //反馈图片
                    List<String> contentIds = FastList.newInstance();
                    List<GenericValue> feedbackContents = delegator.findByAnd("FeedbackContent", UtilMisc.toMap("feedbackId", feedback.get("feedbackId")));
                    if (UtilValidate.isNotEmpty(feedbackContents)) {
                        for (GenericValue feedbackContent : feedbackContents) {
                            contentIds.add(UtilProperties.getMessage("content", "kaide.images.baseUrl", Locale.CHINA) + "/content/control/getImage?contentId=" + feedbackContent.get("contentId"));
                        }
                    }
                    map.put("imageUrl", contentIds);
                    returnList.add(map);
                }
                resultData.put("returnList", returnList);
                resultData.put("max", resultSize);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    /**
     * 积分code type：
     * 每日登陆： LOGIN_PER_DAY
     * 新用户注册： NEW_CUST_REGISTER
     * 首次分享赠送：FIRST_SHARE
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> userScoreCodeSend(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String type = (String) context.get("sendType");
        String userLoginId = (String) context.get("userLoginId");
        String memberId = (String) context.get("memberId");
        String mallId = (String) context.get("mallId");
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();
        
        try {
            GenericValue mallInfo = delegator.findByPrimaryKey("KdMallInfo",UtilMisc.toMap("mallId",mallId));
            if (type.equals("LOGIN_PER_DAY")) {
                List<EntityCondition> exps = FastList.newInstance();
                exps.add(EntityCondition.makeCondition("userLoginId", userLoginId));
                exps.add(EntityCondition.makeCondition("scoreType", "LOGIN_PER_DAY"));
                //当天有没有赠送给每日登陆的积分
                Timestamp nowTime = UtilDateTime.nowTimestamp();
                exps.add(EntityCondition.makeCondition("sendDate", EntityOperator.GREATER_THAN, UtilDateTime.getDayStart(nowTime)));
                exps.add(EntityCondition.makeCondition("sendDate", EntityOperator.LESS_THAN, UtilDateTime.getDayEnd(nowTime)));
                Long sendLogs = delegator.findCountByCondition("PartyScoreSendLog", EntityCondition.makeCondition(exps, EntityOperator.AND), null, null);
                if (UtilValidate.isNotEmpty(sendLogs) && sendLogs.intValue() > 0) {
                
                } else {
                    GenericValue code = delegator.findByPrimaryKey("IntegralCode", UtilMisc.toMap("integralCodeId", "10002"));
                    
                    if (UtilValidate.isNotEmpty(code) && UtilValidate.isNotEmpty(code.getString("integralCodeId"))) {
                        Map<String,Object> result = dispatcher.runSync("kaide-userAddScore", UtilMisc.toMap("member_id", memberId, "locationCode", mallInfo.getString("locationId"), "integralCode", code.getString("integralCodeNo")));
                        if(ServiceUtil.isError(result)){
                        
                        }
                        GenericValue scoreSendLog = delegator.makeValue("PartyScoreSendLog", UtilMisc.toMap("userLoginId", userLoginId, "scoreCode", code.getString("integralCodeId"), "scoreType", "LOGIN_PER_DAY", "sendDate", UtilDateTime.nowTimestamp()));
                        scoreSendLog.setNextSeqId();
                        scoreSendLog.create();
                    }
                }
            } else if (type.equals("NEW_CUST_REGISTER")) {
                GenericValue code = delegator.findByPrimaryKey("IntegralCode", UtilMisc.toMap("integralCodeId", "10003"));
                if (UtilValidate.isNotEmpty(code) && UtilValidate.isNotEmpty(code.getString("integralCodeId"))) {
                    dispatcher.runSync("kaide-userAddScore", UtilMisc.toMap("member_id", memberId, "locationCode", mallInfo.getString("locationId"), "integralCode", code.getString("integralCodeNo")));
                    GenericValue scoreSendLog = delegator.makeValue("PartyScoreSendLog", UtilMisc.toMap("userLoginId", userLoginId, "scoreCode", code.getString("integralCodeId"), "scoreType", "NEW_CUST_REGISTER", "sendDate", UtilDateTime.nowTimestamp()));
    
                }
            } else if (type.equals("FIRST_SHARE")) {
                List<EntityCondition> exps = FastList.newInstance();
                exps.add(EntityCondition.makeCondition("userLoginId", userLoginId));
                exps.add(EntityCondition.makeCondition("scoreType", "FIRST_SHARE"));
                Long sendLogs = delegator.findCountByCondition("PartyScoreSendLog", EntityCondition.makeCondition(exps, EntityOperator.AND), null, null);
                if (UtilValidate.isNotEmpty(sendLogs) && sendLogs.intValue() > 0) {
                
                } else {
                    GenericValue code = delegator.findByPrimaryKey("IntegralCode", UtilMisc.toMap("integralCodeId", "10004"));
                    if (UtilValidate.isNotEmpty(code) && UtilValidate.isNotEmpty(code.getString("integralCodeId"))) {
                        dispatcher.runSync("kaide-userAddScore", UtilMisc.toMap("member_id", memberId, "locationCode", mallInfo.getString("locationId"), "integralCode", code.getString("integralCodeNo")));
                        GenericValue scoreSendLog = delegator.makeValue("PartyScoreSendLog", UtilMisc.toMap("userLoginId", userLoginId, "scoreCode", code.getString("integralCodeId"), "scoreType", "FIRST_SHARE", "sendDate", UtilDateTime.nowTimestamp()));
                        scoreSendLog.setNextSeqId();
                        scoreSendLog.create();
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return resultData;
    }
}
