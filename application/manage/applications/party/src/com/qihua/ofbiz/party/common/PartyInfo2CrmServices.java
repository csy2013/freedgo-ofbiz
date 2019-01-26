/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.qihua.ofbiz.party.common;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONObject;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;

public class PartyInfo2CrmServices {

	public static final String module = PartyInfo2CrmServices.class.getName();
	public static final String resource = "PartyUiLabels";
	public static final String resourceError = "PartyErrorUiLabels";
	public static final String rootIp = "http://116.62.159.142:8365"; // 服务器地址及端口号
	public static final String rootIp_fan = "http://116.62.159.142:8368";// 服务器地址及端口号
//	public static final String rootIp = "http://192.168.1.220:8090";// 服务器地址及端口号
//	public static final String rootIp = "http://10.9.16.128:8090";// 服务器地址及端口号
//	public static final String rootIp_fan = "http://10.9.16.128:8092";// 服务器地址及端口号
	// public static final String rootIp = "http://192.168.1.68:8181";//
	// 服务器地址及端口号
	public static final String source = "/middle/invoker/ico/crm";

	public static final DateFormat sdf_datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 时间格式
	public static final DateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd"); // 日期格式

	/**
	 * 会员信息新增（基本信息）To Crm端(CRM_01)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createPartyInfoCrm01(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance(); // 结果集  
        Map<String, Object> params = FastMap.newInstance(); // 请求参数
        Map<String, String> headers = FastMap.newInstance();// 请求头
        // 获取参数
        // HTTP HEAD 参数
//        String headCode     = (String) context.get("headCode");   
//        String headKey      = (String) context.get("headKey");  
        // 输入参数
        String brandId      = (String) context.get("productBrandId");            // 商户id
        String custCode     = (String) context.get("custCode");           // 客户编号       
        String vipCode      = (String) context.get("vipCode");            // 会员编号       
        String channelId    = (String) context.get("channelId");          // 渠道id         
        String realName     = (String) context.get("realName");           // 真实名称       
        String nick         = (String) context.get("nick");               // 昵称           
        String picUrl       = (String) context.get("picUrl");             // 头像           
        String custType     = (String) context.get("custType");           // 客户类型       
        String gender       = (String) context.get("gender");             // 性别           
        String mobile       = (String) context.get("mobile");             // 手机号码       
        String mail         = (String) context.get("mail");               // 电子邮箱       
        String wechat       = (String) context.get("wechat");             // 微信openid     
        String sourceId     = (String) context.get("sourceId");           // 来源           
        String sourceSubId  = (String) context.get("sourceSubId");        // 来源子渠道     
        String postCode     = (String) context.get("postCode");           // 邮编           
        String address      = (String) context.get("address");            // 地址           
        String storeId      = (String) context.get("storeId");            // 门店（实体）   
        Timestamp birthday     = (Timestamp) context.get("birthday");     // 生日           
        String occuption    = (String) context.get("occuption");          // 职业           
        String qq           = (String) context.get("qq");                 // QQ             
        String blog         = (String) context.get("blog");               // 微博           
        String passportType = (String) context.get("passportType");       // 证件类型       
        String passportNo   = (String) context.get("passportNo");         // 证件号         
        String workplace    = (String) context.get("workplace");          // 工作单位地址   
        String hobby        = (String) context.get("hobby");              // 爱好           
        String income       = (String) context.get("income");             // 收入           
        String province     = (String) context.get("province");           // 省份           
        String city         = (String) context.get("city");               // 城市           
        String area         = (String) context.get("area");               // 区域           
        String createId     = (String) context.get("createId");           // 创建人id       
        String modifyId     = (String) context.get("modifyId");           // 修改人id       
        Timestamp modifyTime   = (Timestamp) context.get("modifyTime");   // 修改时间       
        String status       = (String) context.get("status");             // 状态           
        Timestamp createTime   = (Timestamp) context.get("createTime");   // 创建时间       
        String isFollow     = (String) context.get("isFollow");           // 是否关注       
        String accountMac   = (String) context.get("accountMac");         // 第三方会员编码 
        
        // 中间件调用错误信息
        String iserr="0"; // 0:没有 1：有

        // 输出参数
        Map<String,Object> mainJsonMap=FastMap.newInstance();
        // HTTP请求
        // 使用httpclient请求
        // 设置header头
//        headers.put("code", headCode);//品牌code
//        headers.put("key",  headKey); //品牌key
        // 设置参数
        // header头
//        if(UtilValidate.isNotEmpty(headCode)){
//        	params.put("code", headCode);      // 品牌code
//        }
//        if(UtilValidate.isNotEmpty(headKey)){
//        	params.put("key",  headKey);       // 品牌key
//             
//        }
        
        // 输入参数
        if(UtilValidate.isNotEmpty(brandId)){
   		 	params.put("productBrandId",brandId);              // 商户id
       	}
		if(UtilValidate.isNotEmpty(custCode)){
			params.put("custCode",custCode);            // 客户编号       
		}
		if(UtilValidate.isNotEmpty(vipCode)){
			 params.put("vipCode",vipCode);              // 会员编号       
		}
		if(UtilValidate.isNotEmpty(channelId)){ 
			params.put("channelId",channelId);           // 渠道id         
		}
		if(UtilValidate.isNotEmpty(realName)){   
			 params.put("realName",realName);            // 真实名称       
		}
		if(UtilValidate.isNotEmpty(nick)){     
			params.put("nick",nick);                     // 昵称           
		}
		if(UtilValidate.isNotEmpty(picUrl)){ 
			 params.put("picUrl",picUrl);                // 头像           
		}
		if(UtilValidate.isNotEmpty(custType)){ 
			 params.put("custType",custType);            // 客户类型       
		}
		if(UtilValidate.isNotEmpty(gender)){  
			 params.put("gender",gender);                // 性别           
		}
		if(UtilValidate.isNotEmpty(mobile)){
			 params.put("mobile",mobile);                // 手机号码       
		}
		if(UtilValidate.isNotEmpty(mail)){   
			params.put("mail",mail);                     // 电子邮箱       
		}
		if(UtilValidate.isNotEmpty(wechat)){   
			 params.put("wechat",wechat);                // 微信openid     
		}
		if(UtilValidate.isNotEmpty(sourceId)){
			 params.put("sourceId",sourceId);            // 来源           
		}
		if(UtilValidate.isNotEmpty(sourceSubId)){    
			params.put("sourceSubId",sourceSubId);       // 来源子渠道     
		}
		if(UtilValidate.isNotEmpty(postCode)){  
			params.put("postCode",postCode);             // 邮编           
		}
		if(UtilValidate.isNotEmpty(address)){  
			params.put("address",address);               // 地址           
		}
		if(UtilValidate.isNotEmpty(storeId)){  
			 params.put("storeId",storeId);              // 门店（实体）   
		}

		if(UtilValidate.isNotEmpty(birthday)){
	        params.put("birthday",sdf_datetime.format(birthday));             // 生日           
	       
		}
		if(UtilValidate.isNotEmpty(occuption)){ 
			 params.put("occuption",occuption);          // 职业           
		}
		if(UtilValidate.isNotEmpty(qq)){     
			  params.put("qq",qq);                       // QQ             
		}
		if(UtilValidate.isNotEmpty(blog)){ 
			params.put("blog",blog);                     // 微博           
		}
		if(UtilValidate.isNotEmpty(passportType)){ 
			params.put("passportType",passportType);     // 证件类型       
		}
		if(UtilValidate.isNotEmpty(passportNo)){ 
			params.put("passportNo",passportNo);         // 证件号         
		}
		if(UtilValidate.isNotEmpty(workplace)){   
			 params.put("workplace",workplace);          // 工作单位地址   
		}
		if(UtilValidate.isNotEmpty(hobby)){  
			 params.put("hobby",hobby);                  // 爱好           
		}
		if(UtilValidate.isNotEmpty(income)){  
			 params.put("income",income);                // 收入           
		}
		if(UtilValidate.isNotEmpty(province)){  
			params.put("province",province);             // 省份           
		}
		if(UtilValidate.isNotEmpty(city)){
			  params.put("city",city);                   // 城市           
		}                           
		if(UtilValidate.isNotEmpty(area)){  
			params.put("area",area);                     // 区域           
		}
		if(UtilValidate.isNotEmpty(createId)){   
			 params.put("createId",createId);            // 创建人id       
		}
		if(UtilValidate.isNotEmpty(modifyId)){ 
			 params.put("modifyId",modifyId);            // 修改人id       
		}
		if(UtilValidate.isNotEmpty(modifyTime)){  
			 params.put("modifyTime",sdf_datetime.format(modifyTime));        // 修改时间       
		}
		if(UtilValidate.isNotEmpty(status)){  
			 params.put("status",status);                // 状态           
		}
		if(UtilValidate.isNotEmpty(createTime)){   
			 params.put("createTime",sdf_datetime.format(createTime));        // 创建时间       
		}
		if(UtilValidate.isNotEmpty(isFollow)){   
			params.put("isFollow",isFollow);             // 是否关注       
		}
		if(UtilValidate.isNotEmpty(accountMac)){   	
			 params.put("accountMac",accountMac);        // 第三方会员编码
		}
		
        // URL的设定
//        String httpUrl = rootIp+"/CRM/cust/addCusttomerAndMerge.do";
        String httpUrl = rootIp + source + "/cust/addCusttomerAndMerge";
        
        
        // 返回值
        String response = null;
        try {
			response= HttpUtil.post(httpUrl, headers, params);
//			response= mockData.mocJsonDataCrm1();
			if (!UtilValidate.isEmpty(response)) {
	            mainJsonMap=FastMap.newInstance();
				//** 响应数据转换成json格式 *//
	            mainJsonMap= analysisData.analysisDataForMainJson(response);
	        }
		} catch (Exception e) {
			 Debug.logError(e, "Problem connecting to Fedex server", module);
		}
        //返回的参数
        result.put("expStatus",mainJsonMap.get("status"));           // 标识     
        result.put("code", mainJsonMap.get("code"));                 // 编码     
        result.put("msg", mainJsonMap.get("msg"));                   // 信息     
        result.put("data", mainJsonMap.get("data"));                 // 结果集   
        result.put("receivetime", mainJsonMap.get("receivetime"));   // 请求时间 
        result.put("backtime", mainJsonMap.get("backtime"));         // 返回时间 
        result.put("syscode", mainJsonMap.get("syscode"));           // 系统code 
        result.put("itfcode", mainJsonMap.get("itfcode"));           // 接口code 
        
        // 中间件调用错误信息
        if(UtilValidate.isNotEmpty(mainJsonMap.get("err"))){
        	iserr="1";// 1：有错误 0：无错误
        	Map<String,Object> errInfoMap=(Map<String,Object>)mainJsonMap.get("errInfo");
        	result.put("errInfoMap", errInfoMap);           // 错误信息
        }
        result.put("iserr", iserr);           // 是否有错
        
        
        return result;
    }
    
    
    
    /**
     * 会员信息修改（基本信息）2Crm To Crm端(CRM_02)
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> updateCustInfoCrm02(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance(); // 结果集  
        Map<String, Object> params = FastMap.newInstance(); // 请求参数
        Map<String, String> headers = FastMap.newInstance();// 请求头
        // 获取参数
        // HTTP HEAD 参数
        String headCode     = (String) context.get("headCode");   
        String headKey      = (String) context.get("headKey");  
        // 输入参数
        String brandId      = (String) context.get("productBrandId");            // 商户id
        String custCode     = (String) context.get("custCode");           // 客户编号    
        String custId       = (String) context.get("custId");           // 客户编号    
        String vipCode      = (String) context.get("vipCode");            // 会员编号       
        String channelId    = (String) context.get("channelId");          // 渠道id         
        String realName     = (String) context.get("realName");           // 真实名称       
        String nick         = (String) context.get("nick");               // 昵称           
        String picUrl       = (String) context.get("picUrl");             // 头像           
        String custType     = (String) context.get("custType");           // 客户类型       
        String gender       = (String) context.get("gender");             // 性别           
        String mobile       = (String) context.get("mobile");             // 手机号码       
        String mail         = (String) context.get("mail");               // 电子邮箱       
        String wechat       = (String) context.get("wechat");             // 微信openid     
        String sourceId     = (String) context.get("sourceId");           // 来源           
        String sourceSubId  = (String) context.get("sourceSubId");        // 来源子渠道     
        String postCode     = (String) context.get("postCode");           // 邮编           
        String address      = (String) context.get("address");            // 地址           
        String storeId      = (String) context.get("storeId");            // 门店（实体）   
        Timestamp birthday     = (Timestamp) context.get("birthday");     // 生日           
        String occuption    = (String) context.get("occuption");          // 职业           
        String qq           = (String) context.get("qq");                 // QQ             
        String blog         = (String) context.get("blog");               // 微博           
        String passportType = (String) context.get("passportType");       // 证件类型       
        String passportNo   = (String) context.get("passportNo");         // 证件号         
        String workplace    = (String) context.get("workplace");          // 工作单位地址   
        String hobby        = (String) context.get("hobby");              // 爱好           
        String income       = (String) context.get("income");             // 收入           
        String province     = (String) context.get("province");           // 省份           
        String city         = (String) context.get("city");               // 城市           
        String area         = (String) context.get("area");               // 区域           
        String createId     = (String) context.get("createId");           // 创建人id       
        String modifyId     = (String) context.get("modifyId");           // 修改人id       
        Timestamp modifyTime   = (Timestamp) context.get("modifyTime");   // 修改时间       
        String status       = (String) context.get("status");             // 状态           
        Timestamp createTime   = (Timestamp) context.get("createTime");   // 创建时间       
        String isFollow     = (String) context.get("isFollow");           // 是否关注       
        String accountMac   = (String) context.get("accountMac");         // 第三方会员编码 
        String drivingLicence   = (String) context.get("drivingLicence"); // 驾驶证

        // 输出参数
        Map<String,Object> mainJsonMap=FastMap.newInstance();
        
        // 中间件调用错误信息
        String iserr="0"; // 0:没有 1：有
        // HTTP请求
        // 使用httpclient请求
        // 设置header头
//        headers.put("code", headCode);//品牌code
//        headers.put("key",  headKey); //品牌key
        // 设置参数
        // header头
        if(UtilValidate.isNotEmpty(headCode)){
        	params.put("code", headCode);      // 品牌code
        }
        if(UtilValidate.isNotEmpty(headKey)){
        	params.put("key",  headKey);       // 品牌key
             
        }
        
        // 输入参数
        if(UtilValidate.isNotEmpty(brandId)){
        	 params.put("productBrandId",brandId);              // 商户id
        }
    	if(UtilValidate.isNotEmpty(custCode)){
    		 params.put("custCode",custCode);            // 客户编号       
    	}
    	if(UtilValidate.isNotEmpty(custId)){
    		params.put("rowId",custId);            // 客户编号       
    	}
    	if(UtilValidate.isNotEmpty(vipCode)){
    		 params.put("vipCode",vipCode);              // 会员编号       
    	}
    	if(UtilValidate.isNotEmpty(channelId)){
    		params.put("channelId",channelId);          // 渠道id         
    	}
    	if(UtilValidate.isNotEmpty(realName)){
    		params.put("realName",realName);            // 真实名称       
    	}
    	if(UtilValidate.isNotEmpty(nick)){
    		 params.put("nick",nick);                   // 昵称           
    	}
    	if(UtilValidate.isNotEmpty(picUrl)){
    		params.put("picUrl",picUrl);                // 头像           
    	}
    	if(UtilValidate.isNotEmpty(custType)){
    		params.put("custType",custType);            // 客户类型       
    	}
    	if(UtilValidate.isNotEmpty(gender)){
    		params.put("gender",gender);                // 性别           
    	}
    	if(UtilValidate.isNotEmpty(mobile)){
    		 params.put("mobile",mobile);               // 手机号码       
    	}
    	if(UtilValidate.isNotEmpty(mail)){
    		params.put("mail",mail);                    // 电子邮箱       
    	}
    	if(UtilValidate.isNotEmpty(wechat)){
    		params.put("wechat",wechat);                // 微信openid     
    	}
    	if(UtilValidate.isNotEmpty(sourceId)){
    		params.put("sourceId",sourceId);            // 来源           
    	}
    	if(UtilValidate.isNotEmpty(sourceSubId)){
    		params.put("sourceSubId",sourceSubId);      // 来源子渠道     
    	}
    	if(UtilValidate.isNotEmpty(postCode)){
    		params.put("postCode",postCode);            // 邮编           
    	}
    	if(UtilValidate.isNotEmpty(address)){
    		 params.put("address",address);             // 地址           
    	}
    	if(UtilValidate.isNotEmpty(storeId)){
    		params.put("storeId",storeId);              // 门店（实体）   
    	}
    	if(UtilValidate.isNotEmpty(birthday)){
    		params.put("birthday",sdf_datetime.format(birthday));            // 生日           
    	}
    	if(UtilValidate.isNotEmpty(occuption)){
    		params.put("occuption",occuption);          // 职业           
    	}
    	if(UtilValidate.isNotEmpty(qq)){
    		 params.put("qq",qq);                        // QQ             
    	}
    	if(UtilValidate.isNotEmpty(blog)){
    		 params.put("blog",blog);                    // 微博           
    	}
    	if(UtilValidate.isNotEmpty(passportType)){
    		 params.put("passportType",passportType);    // 证件类型       
    	}
    	if(UtilValidate.isNotEmpty(passportNo)){
    		 params.put("passportNo",passportNo);        // 证件号         
    	}
    	if(UtilValidate.isNotEmpty(workplace)){
    		 params.put("workplace",workplace);          // 工作单位地址   
    	}
    	if(UtilValidate.isNotEmpty(hobby)){
    		 params.put("hobby",hobby);                  // 爱好           
    	}
    	if(UtilValidate.isNotEmpty(income)){
    		 params.put("income",income);                // 收入           
    	}
    	if(UtilValidate.isNotEmpty(province)){
    		params.put("province",province);             // 省份           
    	}
    	if(UtilValidate.isNotEmpty(city)){
    		 params.put("city",city);                    // 城市           
    	}
    	if(UtilValidate.isNotEmpty(area)){
    		params.put("area",area);                     // 区域           
    	}
    	if(UtilValidate.isNotEmpty(createId)){
    		 params.put("createId",createId);            // 创建人id       
    	}
    	if(UtilValidate.isNotEmpty(modifyId)){
    		params.put("modifyId",modifyId);             // 修改人id       
    	}
    	if(UtilValidate.isNotEmpty(modifyTime)){
    		params.put("modifyTime",sdf_datetime.format(modifyTime));         // 修改时间       
    	}
    	if(UtilValidate.isNotEmpty(status)){
    		 params.put("status",status);                // 状态           
    	}
    	if(UtilValidate.isNotEmpty(createTime)){
    		 params.put("createTime",sdf_datetime.format(createTime));        // 创建时间       
    	}
    	if(UtilValidate.isNotEmpty(isFollow)){
    		 params.put("isFollow",isFollow);            // 是否关注       
    	}
    	if(UtilValidate.isNotEmpty(accountMac)){
    		params.put("accountMac",accountMac);         // 第三方会员编码
    	}
    	if(UtilValidate.isNotEmpty(drivingLicence)){
    		params.put("drivingLicence",drivingLicence);         // 驾驶证
    	}
        // URL的设定
        String httpUrl = rootIp + source + "/cust/modifyCustInfo";
        // 返回值
        String response = null;
        try {
			response= HttpUtil.post(httpUrl, headers, params);
//			response= mockData.mocJsonDataCrm2();
			if (!UtilValidate.isEmpty(response)) {
	            mainJsonMap=FastMap.newInstance();
				//** 响应数据转换成json格式 *//
	            mainJsonMap= analysisData.analysisDataForMainJson(response);
	        }
		} catch (Exception e) {
			 Debug.logError(e, "Problem connecting to Fedex server", module);
		}
        //返回的参数
        result.put("expStatus",mainJsonMap.get("status"));           // 标识     
        result.put("code", mainJsonMap.get("code"));                 // 编码     
        result.put("msg", mainJsonMap.get("msg"));                   // 信息     
        result.put("data", mainJsonMap.get("data"));                 // 结果集   
        result.put("receivetime", mainJsonMap.get("receivetime"));   // 请求时间 
        result.put("backtime", mainJsonMap.get("backtime"));         // 返回时间 
        result.put("syscode", mainJsonMap.get("syscode"));           // 系统code 
        result.put("itfcode", mainJsonMap.get("itfcode"));           // 接口code 
        
        // 中间件调用错误信息
        if(UtilValidate.isNotEmpty(mainJsonMap.get("err"))){
        	iserr="1";// 1：有错误 0：无错误
        	Map<String,Object> errInfoMap=(Map<String,Object>)mainJsonMap.get("errInfo");
        	result.put("errInfoMap", errInfoMap);           // 错误信息
        }
        result.put("iserr", iserr);           // 是否有错
        return result;
    }

	/**
	 * 会员信息查询（基本信息） To Crm端(CRM_03)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> searchPageCustInfoCrm0301(DispatchContext ctx, Map<String, ? extends Object> context) {

		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// 输入参数
		String custCode = (String) context.get("custCode"); // 会员编号
		String brandId = (String) context.get("productBrandId"); // 商户编号
		String mobile = (String) context.get("mobile"); // 手机号码
		String mail = (String) context.get("mail"); // 电子邮箱
		String currentPage = (String) context.get("currentPage"); // 当前页数
		String pageSize = (String) context.get("pageSize"); // 每页大小

		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		Map<String, Object> custInfo = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(custCode)) {
			params.put("custCode", custCode); // 会员编号
		}
		
		if (UtilValidate.isNotEmpty(brandId)) {
			params.put("productBrandId", brandId); // 会员编号
		}

		if (UtilValidate.isNotEmpty(mobile)) {
			params.put("mobile", mobile); // 手机号码
		}

		if (UtilValidate.isNotEmpty(mail)) {
			params.put("mail", mail); // 电子邮箱
		}

		if (UtilValidate.isNotEmpty(currentPage)) {
			params.put("currentPage", currentPage); // 当前页数
		}

		if (UtilValidate.isNotEmpty(pageSize)) {
			params.put("pageSize", pageSize); // 每页大小
		}

		// URL的设定
		String httpUrl = rootIp + source + "/cust/searchPageCustInfo";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response= mockData.mocJsonDataCrm3();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				if (data != "null") {
					custInfo = analysisData.analysisDataCrm03(data);
				}
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
//		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
//		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		// 解析后得到
		result.put("custInfo", custInfo); // 客户信息

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}
	
	/**
	 * 会员基本信息查询（包含扩展属性、权益、优惠券）
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> searchPageCustInfoCrm04(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 输入参数
		String memberCode = (String) context.get("memberCode"); // 会员编号
		String mobile = (String) context.get("mobile"); // 手机号码
		String mail = (String) context.get("mail"); // 电子邮箱

		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		Map<String, Object> custInfo = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(memberCode)) {
			params.put("memberCode", memberCode); // 会员编号
		}

		if (UtilValidate.isNotEmpty(mobile)) {
			params.put("mobile", mobile); // 手机号码
		}

		if (UtilValidate.isNotEmpty(mail)) {
			params.put("mail", mail); // 电子邮箱
		}

		// URL的设定
		String httpUrl = rootIp + source + "/cust/getChannelMembersDetailed";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *
				mainJsonMap = analysisData.analysisDataForMainJson(res);
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				if (data != "null") {
					custInfo = analysisData.analysisDataCrm04(data);
					// 判断资产信息列表
					if(UtilValidate.isNotEmpty(custInfo.get("channelMemberAssets"))) {
						
					}
				}
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		// 解析后得到
		result.put("custInfo", custInfo); // 客户信息

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}
	
	
	

	/**
	 * 会员权益查询 To Crm端(CRM_06)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> searchCustRightsCrm06(
			DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		// String headCode= (String) context.get("headCode"); // 品牌code
		// String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String rightsCode = (String) context.get("rightsCode"); // 权益编号
		String brandId = (String) context.get("productBrandId"); // 店铺id
		String custId = (String) context.get("custId"); // 客户id
		String rightsName = (String) context.get("rightsName"); // 权益名称
		String status = (String) context.get("status"); // 状态
		String currentPage = (String) context.get("currentPage"); // 当前页数
		String pageSize = (String) context.get("pageSize"); // 每页大小
		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		Map<String, Object> rightInfo = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(rightsCode)) {
			params.put("rightsCode", rightsCode); // 权益编号
		}

		if (UtilValidate.isNotEmpty(brandId)) {
			params.put("productBrandId", brandId); // 店铺id
		}

		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId); // 客户id
		}

		if (UtilValidate.isNotEmpty(rightsName)) {
			params.put("rightsName", rightsName); // 权益名称
		}

		if (UtilValidate.isNotEmpty(status)) {
			params.put("status", status.toString()); // 状态
		}

		if (UtilValidate.isNotEmpty(currentPage)) {
			params.put("current", currentPage.toString()); // 当前页数
		}

		if (UtilValidate.isNotEmpty(pageSize)) {
			params.put("pageSize", pageSize.toString()); // 每页大小
		}

		// URL的设定
		String httpUrl = rootIp + source + "/cust/searchCustRights";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response= mockData.mocJsonDataCrm6();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				if (data != "null") {
					rightInfo = analysisData.analysisDataCrm06(data);
				}
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		// 解析后得到
		result.put("rightInfo", rightInfo); // 权益信息

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 * 会员优惠券查询 To Crm端(CRM_08)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> searchCouponAccountCrm08(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		// String headCode= (String) context.get("headCode"); // 品牌code
		// String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String brandId = (String) request.getParameter("productBrandId"); // 权益编号
		String custId = (String) request.getParameter("custId"); // 权益名称
		String couponCode = (String) request.getParameter("couponCode"); // 权益编号
		String couponName = (String) request.getParameter("couponName"); // 权益名称
		String currentPage = (String) request.getParameter("currentPage"); // 当前页数
		String pageSize = (String) request.getParameter("pageSize"); // 每页大小
		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		Map<String, Object> couponInfo = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(brandId)) {
			params.put("productBrandId", brandId); // 商户ID
		}

		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId); // 客户编号
		}

		if (UtilValidate.isNotEmpty(couponCode)) {
			params.put("couponCode", couponCode); // 权益编号
		}

		if (UtilValidate.isNotEmpty(couponName)) {
			params.put("couponName", couponName); // 权益名称
		}

		if (UtilValidate.isNotEmpty(currentPage)) {
			params.put("currentPage", currentPage.toString()); // 当前页数
		}

		if (UtilValidate.isNotEmpty(pageSize)) {
			params.put("pageSize", pageSize.toString()); // 每页大小
		}

		// URL的设定
		String httpUrl = rootIp + source + "/cust/searchCouponAccount";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response= mockData.mocJsonDataCrm8();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				if (data != "null") {
					couponInfo = analysisData.analysisDataCrm08(data);
				}
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		// 解析后得到
		result.put("couponInfo", couponInfo); // 优惠券信息

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;
	}

	/**
	 * 优惠券消耗 To Crm端(CRM_11)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> consumeCardCrm11(DispatchContext dct, Map<String,? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		// String headCode= (String) context.get("headCode"); // 品牌code
		// String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String memberCode = (String) context.get("memberCode"); // 会员号
		String couponNo = (String) context.get("couponNo"); // 优惠券编号
		String isFlag = (String) context.get("isFlag"); // 核销标识
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(memberCode)) {
			params.put("memberCode", memberCode); // 会员号
		}

		if (UtilValidate.isNotEmpty(couponNo)) {
			params.put("couponNo", couponNo); // 优惠券编号
		}

		if (UtilValidate.isNotEmpty(isFlag)) {
			params.put("isFlag", isFlag); // 核销标识
		}

		// URL的设定
		String httpUrl = rootIp + source + "/coupon/consumeCard";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response= mockData.mocJsonDataCrm11();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;
	}
	
	/**
	 * 会员资产（爱车）信息查询
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static Map<String, Object> queryCustomerVehiCrm13(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有
		
		String custId = request.getParameter("custId"); // 会员编号
		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId);
		}
		
		String currentPage = request.getParameter("currentPage"); // 当前页数
		if (UtilValidate.isNotEmpty(currentPage)) {
			params.put("currentPage", currentPage);
		}
		
		String pageSize = request.getParameter("pageSize"); // 每页大小
		if (UtilValidate.isNotEmpty(pageSize)) {
			params.put("pageSize", pageSize);
		}
		
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// URL的设定
		String httpUrl = rootIp + source + "/cust/queryCustomerVehi";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response= mockData.mocJsonDataCrm11();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;
	}

	/**
	 * 会员资产（爱车）同步
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> syncCustVehiCrm14(DispatchContext ctx, Map<String, ? extends Object> context) throws GenericEntityException {

		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有
		
		String rowId = (String) context.get("rowId"); // 主键ID
		String custId = (String) context.get("custId"); // 客户编号
		String vehiModelId = (String) context.get("vehiModelId"); // 车型编号
		String vehiModelName = (String) context.get("vehiModelName"); // 车型名称
		String vehiBrand = (String) context.get("vehiBrand"); // 爱车品牌
		Timestamp roadTime = (Timestamp) context.get("roadTime"); // 上路时间
		Integer mileage = (Integer) context.get("mileage"); // 行驶里程
		Integer isTransfer = (Integer) context.get("isTransfer"); // 是否过户
		String vehiNum = (String) context.get("vehiNum"); // 车牌号
		String engineNum = (String) context.get("engineNum"); // 发动机号
		Timestamp insuranceEndDate = (Timestamp) context.get("insuranceEndDate"); // 保险到期日期
		Timestamp maintenanceDate = (Timestamp) context.get("maintenanceDate"); // 上次保养时间
		String purchaseChannel = (String) context.get("purchaseChannel"); // 购买渠道
		String status = (String) context.get("status"); // 状态
		String vehiTypeName = (String) context.get("vehiTypeName"); // 车系品牌字段
		
		if (UtilValidate.isNotEmpty(rowId)) {
			params.put("rowId", rowId);
		}
		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId);
		}
		if (UtilValidate.isNotEmpty(vehiModelId)) {
			params.put("vehiModelId", vehiModelId);
		}
		if (UtilValidate.isNotEmpty(vehiModelName)) {
			params.put("vehiModelName", vehiModelName);
		}
		if (UtilValidate.isNotEmpty(vehiBrand)) {
			params.put("vehiBrand", vehiBrand);
		}
		if (UtilValidate.isNotEmpty(roadTime)) {
			params.put("roadTime", roadTime);
		}
		if (UtilValidate.isNotEmpty(mileage)) {
			params.put("mileage", mileage);
		}
		if (UtilValidate.isNotEmpty(isTransfer)) {
			params.put("isTransfer", isTransfer);
		}
		if (UtilValidate.isNotEmpty(vehiNum)) {
			params.put("vehiNum", vehiNum);
		}
		if (UtilValidate.isNotEmpty(engineNum)) {
			params.put("engineNum", engineNum);
		}
		if (UtilValidate.isNotEmpty(insuranceEndDate)) {
			params.put("insuranceEndDate", insuranceEndDate);
		}
		if (UtilValidate.isNotEmpty(maintenanceDate)) {
			params.put("maintenanceDate", maintenanceDate);
		}
		if (UtilValidate.isNotEmpty(purchaseChannel)) {
			params.put("purchaseChannel", purchaseChannel);
		}
		if (UtilValidate.isNotEmpty(status)) {
			params.put("status", status);
		}
		if (UtilValidate.isNotEmpty(vehiTypeName)) {
			params.put("vehiTypeName", vehiTypeName);
		}
		
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// URL的设定
		String httpUrl = rootIp + source + "/cust/syncCustVehi";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response= mockData.mocJsonDataCrm11();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;

	}

	/**
	 * 新增订单信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> addOrderInfoCrm19(DispatchContext ctx, Map<String, ? extends Object> context) throws GenericEntityException {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取订单号
		String orderId = String.valueOf(context.get("orderId"));
		// 获取订单详情
		Delegator delegator = ctx.getDelegator();
		
		// get the order type
        String partyId = (String) context.get("partyId");// 会员Id
    	// 获取订单信息
    	GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
    	String orderType = orderHeader.getString("salesOrderType"); // 订单类型
    	// 根据订单类型获取订单状态
    	String statusId = orderHeader.getString("statusId");
        Map<String, Object> paramsCRM = FastMap.newInstance(); // 保存订单信息，同步到CRM   spj
        paramsCRM.put("productBrandId", "1"); // 商户id spj
        paramsCRM.put("channelId", orderHeader.getString("productStoreId")); // 渠道id spj
        paramsCRM.put("sourceId", "1"); // 来源id spj
        paramsCRM.put("custId", context.get("custId")); // 客户id（卖家在CRM的ID）
        paramsCRM.put("tradeType", orderType); // 订单类型
        
        // 根据partyId获取客户在crm的rowId
        GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
        paramsCRM.put("buyerNick", person.getString("nickname")); // 买家昵称
        paramsCRM.put("mobile", person.getString("mobile")); // 买家手机号码
        String buyerSex = null;
        if ("M".equals(person.getString("gender"))) {
        	buyerSex = "1";
        } else if ("F".equals(person.getString("gender"))) {
        	buyerSex = "0";
        } else {
        	buyerSex = "2";
        }
        paramsCRM.put("buyerSex", buyerSex); // 买家性别
        paramsCRM.put("sellerNick", "365车展"); // 卖家昵称
        paramsCRM.put("orderType", "fixed"); // 
        paramsCRM.put("tradeCode", orderId); // 订单编号
        paramsCRM.put("tradeAmt", orderHeader.getBigDecimal("actualPayMoney")); // 交易总额
        paramsCRM.put("orderDate", sdf_datetime.format(orderHeader.getTimestamp("orderDate"))); // 下单时间
        // 判断订单的订单类型
        if ("INTEGRAL_ORDER".equals(orderType)) {
        	paramsCRM.put("isIntegralOrder", true);
        } else {
        	paramsCRM.put("isIntegralOrder", false);
        }
    	paramsCRM.put("integralNum", orderHeader.getBigDecimal("useIntegral")); //使用积分

        Map<String, Object> receiver = new HashMap<String, Object>(); // 收货人信息
        receiver.put("userName", orderHeader.getString("buyerName"));
        receiver.put("mobile", orderHeader.getString("buyerTelphone"));
        receiver.put("phone", orderHeader.getString("buyerTelphone"));
        paramsCRM.put("receiver", receiver); // 收货信息
        String tradeStatus = returnOrderStatus(orderType, statusId);
        paramsCRM.put("tradeStatus", tradeStatus); // 订单状态
        
        List<Map<String, Object>> subTrades = FastList.newInstance(); // 保存子订单的信息
        Map<String, Object> subTrade = FastMap.newInstance(); // 子订单
        List<GenericValue> orderItems =  new ArrayList<GenericValue>();
        BigDecimal number = BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.ZERO; // 应付总额
        orderItems = delegator.findByAnd("OrderItem",UtilMisc.toMap("orderId",orderId));
    	// 判断是否是退款
        String isOrder = (String) context.get("isOrder");
        for (GenericValue oi : orderItems){
        	subTrade = new HashMap<String, Object>();
        	subTrade.put("sourceId", "1"); // 来源id spj
        	subTrade.put("channelId", orderHeader.getString("productStoreId")); // 渠道id spj
        	subTrade.put("tradeCode", orderId); // 订单编号
        	GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", oi.getString("productId"))); // 获取商品信息
        	subTrade.put("channelProductId", product.getString("productId")); // 商品id
        	subTrade.put("productId", product.get("rowId"));
        	subTrade.put("productIdIco", product.get("productId"));
        	subTrade.put("productName", product.getString("productName")); // 商品名称
        	List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", product.getString("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
        	if (productContents != null && productContents.size() > 0) {
        		subTrade.put("picUrl", "/content/control/getImage?contentId="+productContents.get(0).getString("contentId")); // 商品图片
        	}
        	subTrade.put("num", oi.getBigDecimal("quantity")); // 购买数量
        	// 判断订单是否贷款
        	if("LOAN_GOOD".equals(product.getString("productTypeId"))) {
        		// 贷款商品
	        	subTrade.put("price", oi.getBigDecimal("unitPrice")); // 商品单价
	        	subTrade.put("discount", 0.0); // 折扣金额
        	} else {
            	// 获取商品原始价格
            	List<GenericValue> productPrices = delegator.findByAnd("ProductAndPriceView", UtilMisc.toMap("productId", product.getString("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
            	if (productPrices != null && productContents.size() > 0) {
    	        	subTrade.put("price", productPrices.get(0).getBigDecimal("price")); // 商品单价
    	        	subTrade.put("discount", productPrices.get(0).getBigDecimal("price").subtract(oi.getBigDecimal("unitPrice"))); // 折扣金额
            	}
        	}
        	subTrade.put("totalFee", oi.getBigDecimal("unitPrice").multiply(oi.getBigDecimal("quantity"))); // 商品总价
        	subTrade.put("payment", oi.getBigDecimal("unitPrice").multiply(oi.getBigDecimal("quantity"))); // 应付金额
        	totalFee.add(oi.getBigDecimal("unitPrice").multiply(oi.getBigDecimal("quantity"))); //
        	subTrade.put("tradeStatus", tradeStatus); // 商品状态
        	if (UtilValidate.isNotEmpty(isOrder) && "Y".equals(isOrder)) {
        		subTrade.put("isBackOrder", "1"); // 是退款单
        	} else {
        		subTrade.put("isBackOrder", "0"); // 非退款单
        	}
           number = number.add(oi.getBigDecimal("quantity"));
           subTrades.add(subTrade);
        }
    	if (UtilValidate.isNotEmpty(isOrder) && "Y".equals(isOrder)) {
    		paramsCRM.put("isBackOrder", "1"); // 是退款单
    	} else {
    		paramsCRM.put("isBackOrder", "0"); // 非退款单
    	}
        paramsCRM.put("tradeNum", number); // 商品数量
        paramsCRM.put("totalFee", totalFee); // 应付总额
        paramsCRM.put("subTrades", subTrades); // 订单子订单
        // 获取支付时间
        GenericValue orderPaymentPreference = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId)).get(0);
        paramsCRM.put("payTime", sdf_datetime.format(orderPaymentPreference.getTimestamp("createdDate"))); // 支付时间
		
		// 将订单信息转成JSON字符串
		JSONObject object = JSONObject.fromObject(paramsCRM);
		params.put("tradeJson", object.toString());
		
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有
		// URL的设定
		String httpUrl = rootIp + source + "/order/addOrderInfoForIco";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;
	}
	
	 /**
     * 判断订单状态
     * 
     * @param orderType
     * @param statusId
     * @return
     */
    private static String returnOrderStatus(String orderType, String statusId) {
    	String orderStatus = null;
    	if ("BUY_CAR_ORDER".equals(orderType)) { // 买车订单
    		if("ORDER_WAITPAY".equals(statusId) || "ORDER_WAITPAYFINAL".equals(statusId)) {
    			orderStatus = "1"; // 待付款和待尾款
    		} else if ("ORDER_WAITEVALUATE".equals(statusId)) {
    			orderStatus = "9"; // 待评价
    		} else if ("ORDER_COMPLETED".equals(statusId)) {
    			orderStatus = "4"; // 已完成
    		} else if ("ORDER_CANCELLED".equals(statusId)) {
    			orderStatus = "5"; // 已取消
    		} else if ("ORDER_RETURNED".equals(statusId)) {
    			orderStatus = "6"; // 退单
    		}
    	} else if ("SELL_CAR_ORDER".equals(orderType)) { // 卖车评估订单
    		if("ORDER_WAITPAY".equals(statusId)) {
    			orderStatus = "1"; // 待付款
    		} else if ("ORDER_WAITEVALUATE".equals(statusId)) {
    			orderStatus = "9"; // 待评价
    		} else if ("ORDER_COMPLETED".equals(statusId)) {
    			orderStatus = "4"; // 已完成
    		}
    	} else if ("MAINTAIN_CAR_ORDER".equals(orderType)) { // 保养订单
    		if("ORDER_WAITPAY".equals(statusId)) {
    			orderStatus = "1"; // 待付款
    		} else if ("ORDER_WAITPRODUCE".equals(statusId)) {
    			orderStatus = "3"; // 待保养
    		} else if ("ORDER_WAITEVALUATE".equals(statusId)) {
    			orderStatus = "9"; // 待评价
    		} else if ("ORDER_COMPLETED".equals(statusId)) {
    			orderStatus = "4"; // 已完成
    		} else if ("ORDER_CANCELLED".equals(statusId)) {
    			orderStatus = "5"; // 已取消
    		} else if ("ORDER_RETURNED".equals(statusId)) {
    			orderStatus = "6"; // 退单
    		}
    	} else if ("REPAIR_ORDER".equals(orderType)) { // 维修订单
    		if ("ORDER_WAITPAY".equals(statusId)) {
    			orderStatus = "1"; // 待付款
    		} else if ("ORDER_WAITEVALUATE".equals(statusId)) {
    			orderStatus = "9"; // 待评价
    		} else if ("ORDER_COMPLETED".equals(statusId)) {
    			orderStatus = "4"; // 已完成
    		}
    	} else if ("INSURANCE_ORDER".equals(orderType)) { // 保险订单
    		if ("ORDER_WAITPAY".equals(statusId)) {
    			orderStatus = "1"; // 待付款
    		} else if ("ORDER_COMPLETED".equals(statusId)) {
    			orderStatus = "4"; // 已完成
    		} else if ("ORDER_CANCELLED".equals(statusId)) {
    			orderStatus = "5"; // 已取消
    		} 
    	} else if ("PARETS_ORDER".equals(orderType)) { // 配件订单
    		if("ORDER_WAITPAY".equals(statusId)) {
    			orderStatus = "1"; // 待付款
    		} else if ("ORDER_WAITPRODUCE".equals(statusId)) {
    			orderStatus = "3"; // 待验证
    		} else if ("ORDER_WAITEVALUATE".equals(statusId)) {
    			orderStatus = "9"; // 待评价
    		} else if ("ORDER_COMPLETED".equals(statusId)) {
    			orderStatus = "4"; // 已完成
    		} else if ("ORDER_CANCELLED".equals(statusId)) {
    			orderStatus = "5"; // 已取消
    		} else if ("ORDER_RETURNED".equals(statusId)) {
    			orderStatus = "6"; // 退单
    		}
    	} else if ("PAY_ORDER".equals(orderType)) { // 买单订单
    		if ("ORDER_WAITEVALUATE".equals(statusId)) {
     			orderStatus = "9"; // 待评价
     		} else if ("ORDER_COMPLETED".equals(statusId)) {
     			orderStatus = "4"; // 已完成
     		} 
    	} else if ("RENEW_ORDER".equals(orderType)) { // 续费订单
    		if ("ORDER_COMPLETED".equals(statusId)) {
     			orderStatus = "4"; // 已完成
     		} 
    	} else if ("INTEGRAL_ORDER".equals(orderType)) { // 积分订单
    		if ("ORDER_WAITPRODUCE".equals(statusId)) {
    			orderStatus = "3"; // 待验证
    		} else if ("ORDER_WAITEVALUATE".equals(statusId)) {
     			orderStatus = "9"; // 待评价
     		} else if ("ORDER_COMPLETED".equals(statusId)) {
     			orderStatus = "4"; // 已完成
     		}
    	} else if ("HOTEL_ORDER".equals(orderType)) { // 酒店订单
    		if("ORDER_WAITPAY".equals(statusId)) {
    			orderStatus = "1"; // 待付款
    		} else if ("ORDER_WAITPRODUCE".equals(statusId)) {
    			orderStatus = "3"; // 待验证
    		} else if ("ORDER_WAITEVALUATE".equals(statusId)) {
     			orderStatus = "9"; // 待评价
    		} else if ("ORDER_COMPLETED".equals(statusId)) {
     			orderStatus = "4"; // 已完成
     		} else if ("ORDER_RETURNED".equals(statusId)) {
    			orderStatus = "6"; // 退单
    		} else if ("ORDER_CANCELLED".equals(statusId)) {
    			orderStatus = "5"; // 已取消
    		} 
    	}
    	return orderStatus;
    }

	
	/**
	 * 用户在进行注册、签到、登录、访问某些重要页面等行为时调用行为接口将行为数据传递到CRM系统中，
	 * CRM接受到行为信息需将行为信息存储到行为信息表中，
	 * 并需根据行为类型至互动中心的积分规则中判断此行为类型是否存在，
	 * 如存在需根据规则给予会员积分，计算完积分后需更新会员总积分及会员积分账户、积分变动日志
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static Map<String, Object> addActivityInfoCrm21(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		
		String custId = (String) context.get("custId"); //会员编号  -- CRM 系统编号 
		if(UtilValidate.isNotEmpty(custId)){
			params.put("custId", custId);
		}
		String brandId = (String) context.get("productBrandId"); //商户ID
		if(UtilValidate.isNotEmpty(brandId)){
			params.put("productBrandId", brandId);
		}
		String activityType = (String) context.get("activityType"); //行为类型  1 注册  2签到  3登录  4收藏  5评价 6 完善资料
		if(UtilValidate.isNotEmpty(activityType)){
			params.put("activityType", activityType);
		}
		Timestamp createTime = (Timestamp) context.get("createTime"); //发生时间
		if(UtilValidate.isNotEmpty(createTime)){
			params.put("createTime", createTime);
		}
		String sourceId = (String) context.get("sourceId"); //发生渠道   1 BOMS\2 ECM\3 微商城
		if(UtilValidate.isNotEmpty(sourceId)){
			params.put("sourceId", sourceId);
		}
		String ipAddress = (String) context.get("ipAddress"); //ip地址
		if(UtilValidate.isNotEmpty(ipAddress)){
			params.put("ipAddress", ipAddress);
		}
		String urlAddress = (String) context.get("urlAddress"); //URL
		if(UtilValidate.isNotEmpty(urlAddress)){
			params.put("urlAddress", urlAddress);
		}
		String desp = (String) context.get("desp"); //描述
		if(UtilValidate.isNotEmpty(desp)){
			params.put("desp", desp);
		}

		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有
		// URL的设定
		String httpUrl = rootIp + source + "/cust/addActivityInfo";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response= mockData.mocJsonDataCrm27();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;
	}
	
	/**
	 * 积分查询接口
	 * 
	 * 用户在微商城、BOMS、ECM查询会员积分时调用此接口，查询时需查询会员当前积分总额及积分明细
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static Map<String, Object> queryCustomerIntegralCrm22(DispatchContext ctx, Map<String, ? extends Object> context){
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		
		String custId = (String) context.get("custId"); //会员编号
		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId);
		}
		String currentPage = (String) context.get("currentPage"); //当前页数
		if (UtilValidate.isNotEmpty(currentPage)) {
			params.put("currentPage", currentPage);
		}
		String pageSize = (String) context.get("pageSize"); //每页大小
		if (UtilValidate.isNotEmpty(pageSize)) {
			params.put("pageSize", pageSize);
		}
		String getWay = (String) context.get("getWay"); //积分流水的查询条件
		if (UtilValidate.isNotEmpty(getWay)) {
			params.put("getWay", getWay);
		}
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有
		// URL的设定
		String httpUrl = rootIp + source + "/cust/queryCustomerIntegral";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response= mockData.mocJsonDataCrm27();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;
	}
	
	/**
	 * 积分变动接口（CRM23）
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> modifyCustomerIntegralCrm23(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		String custId = (String) context.get("custId"); // 用户id
		String integralNum = (String) context.get("integralNum"); // 变动积分总数
		String getWay = (String) context.get("getWay"); // 积分变动类型 0：增加 1：扣减
		String changeType = (String) context.get("changeType"); // CRM积分变动CODE  179注册 180完善资料 181赠送积分 182领取积分 69消费送积分 74消费扣积分 78退货扣积分  178营销活动
		String serialType = (String) context.get("serialType"); // 积分变动描述 0：注册 1登陆 2完善资料 3赠送 4领取 5订单 6营销活动 7CRM后台
		String integralType = (String) context.get("integralType"); // 积分类型 0：普通积分 1：赠送积分
		String brandId = (String) context.get("productBrandId"); // 商铺id
		String channelId = (String) context.get("channelId"); // 渠道id
		String isCreateOrder = (String) context.get("isCreateOrder"); // 是否创建订单 Y:创建订单，N：非创建订单
		String operCode = (String) context.get("operCode"); // CRM积分记录标识
		String orderId = (String) context.get("orderId"); // 积分订单编号
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId); // 用户id
		}

		if (UtilValidate.isNotEmpty(integralNum)) {
			params.put("integralNum", integralNum); // 用户id
		}

		if (UtilValidate.isNotEmpty(getWay)) {
			params.put("getWay", getWay); // 积分变动类型 0：增加 1：扣减
		}

		if (UtilValidate.isNotEmpty(changeType)) {
			params.put("changeType", changeType); // CRM积分变动CODE  179注册 180完善资料 181赠送积分 182领取积分 69消费送积分 74消费扣积分 78退货扣积分  178营销活动
		}
		
		if (UtilValidate.isNotEmpty(serialType)) {
			params.put("serialType", serialType); // 积分变动描述 0：注册 1登陆 2完善资料 3赠送 4领取 5订单 6营销活动 7CRM后台
		}

		if (UtilValidate.isNotEmpty(integralType)) {
			params.put("integralType", integralType); // 积分类型 0：普通积分 1：赠送积分
		}

		if (UtilValidate.isNotEmpty(brandId)) {
			params.put("productBrandId", brandId); // 商铺id
		}

		if (UtilValidate.isNotEmpty(channelId)) {
			params.put("channelId", channelId); // 渠道id
		}
		
		if (UtilValidate.isNotEmpty(isCreateOrder)) {
			params.put("isCreateOrder", isCreateOrder); //  是否创建订单 Y:创建订单，N：非创建订单
		}
		
		if (UtilValidate.isNotEmpty(operCode)) {
			params.put("operCode", operCode); // CRM积分记录标识
		}

		if (UtilValidate.isNotEmpty(orderId)) {
			params.put("orderId", orderId); // 积分订单编号
		}
		
		// URL的设定
		String httpUrl = rootIp + source + "/cust/modifyCustomerIntegral";
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}
	/**
	 * 新增管理用户信息 To Crm端(CRM_27)
	 *
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> addUserCrm27(DispatchContext ctx,
												   Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		// String headCode= (String) request.getParameter("headCode"); // 品牌code
		// String headKey = (String) request.getParameter("headKey"); // 品牌key
		// 输入参数
		String brandId = (String) context.get("productBrandId"); // 商户ID
		String loginName = (String) context.get("loginName"); // 登录名
		String loginPwd = (String) context.get("loginPwd"); // 登录密码
		String realName = (String) context.get("realName"); // 真实名称
		String mobile = (String) context.get("mobile"); // 手机号码
		String email = (String) context.get("email"); // 邮箱
		String roleId = (String) context.get("roleId"); // 角色id
		String descp = (String) context.get("descp"); // 角色描述
		String status = (String) context.get("status"); // 状态
		String brandCode = (String) context.get("brandCode"); // 商户编码
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(brandId)) {
			params.put("productBrandId", brandId); // 商户ID
		}

		if (UtilValidate.isNotEmpty(loginName)) {
			params.put("loginName", loginName); // 登录名
		}

		if (UtilValidate.isNotEmpty(loginPwd)) {
			params.put("loginPwd", loginPwd); // 登录密码
		}

		if (UtilValidate.isNotEmpty(realName)) {
			params.put("realName", realName); // 真实名称
		}

		if (UtilValidate.isNotEmpty(mobile)) {
			params.put("mobile", mobile); // 手机号码
		}

		if (UtilValidate.isNotEmpty(email)) {
			params.put("email", email); // 邮箱
		}

		if (UtilValidate.isNotEmpty(roleId)) {
			params.put("roleId", roleId); // 角色id
		}

		if (UtilValidate.isNotEmpty(descp)) {
			params.put("descp", descp); // 角色描述
		}

		if (UtilValidate.isNotEmpty(status)) {
			params.put("status", status); // 状态
		}

		if (UtilValidate.isNotEmpty(brandCode)) {
			params.put("brandCode", brandCode); // 商户编
		}

		// URL的设定
		String httpUrl = rootIp_fan + source + "/privilege/addUser";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;
	}


	/**
	 * 修改管理用户信息 To Crm端(CRM_28)
	 *
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> modifyUserCrm28(DispatchContext ctx,
													  Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		// String headCode= (String) context.get("headCode"); // 品牌code
		// String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String rowId = (String) context.get("rowId"); // crm主键Id
		String brandId = (String) context.get("productBrandId"); // 商户ID
		String loginName = (String) context.get("loginName"); // 登录名
		String loginPwd = (String) context.get("loginPwd"); // 登录密码
		String realName = (String) context.get("realName"); // 真实名称
		String mobile = (String) context.get("mobile"); // 手机号码
		String email = (String) context.get("email"); // 邮箱
		String descp = (String) context.get("descp"); // 角色描述
		String status = (String) context.get("status"); // 状态 // 角色描述
		String roleId = (String) context.get("roleId"); //角色Id
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(rowId)) {
			params.put("rowId", rowId); // 角色Id
		}
		if (UtilValidate.isNotEmpty(brandId)) {
			params.put("productBrandId", brandId); // 商户ID
		}

		if (UtilValidate.isNotEmpty(loginName)) {
			params.put("loginName", loginName); // 登录名
		}

		if (UtilValidate.isNotEmpty(loginPwd)) {
			params.put("loginPwd", loginPwd); // 登录密码
		}

		if (UtilValidate.isNotEmpty(realName)) {
			params.put("realName", realName); // 真实名称
		}

		if (UtilValidate.isNotEmpty(mobile)) {
			params.put("mobile", mobile); // 手机号码
		}

		if (UtilValidate.isNotEmpty(email)) {
			params.put("email", email); // 邮箱
		}

		if (UtilValidate.isNotEmpty(descp)) {
			params.put("descp", descp); // 角色描述
		}

		if (UtilValidate.isNotEmpty(status)) {
			params.put("status", status); // 状态
		}

		if (UtilValidate.isNotEmpty(roleId)) {
			params.put("roleId", roleId); // 角色id
		}

		// URL的设定
		String httpUrl = rootIp_fan + source + "/privilege/modifyUser";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response = mockData.mocJsonDataCrm28();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错

		return result;
	}

	/**
	 * 会员角色新增 To Crm端(CRM_29)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> addCustRoleCrm29(
			DispatchContext ctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		// String headCode = (String) context.get("headCode"); // 品牌code
		// String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String roleName = (String) context.get("roleName"); // 角色名称
		String brandId = (String) context.get("productBrandId"); // 商户ID
		String descp = (String) context.get("descp"); // 角色描述
		String brandCode = (String) context.get("brandCode"); // 商户编码
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(roleName)) {
			params.put("roleName", roleName); // 角色名称
		}
		if (UtilValidate.isNotEmpty(brandId)) {
			params.put("productBrandId", brandId); // 商户ID
		}
		if (UtilValidate.isNotEmpty(descp)) {
			params.put("descp", descp); // 角色描述
		}
		if (UtilValidate.isNotEmpty(brandCode)) {
			params.put("brandCode", brandCode); // 商户编
		}
		// URL的设定
		String httpUrl = rootIp_fan + source + "/privilege/addRole";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response = mockData.ocJsonDataCrm29();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 * 修改角色信息 To Crm端(CRM_30)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> modifyRoleCrm30(
			DispatchContext ctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		// String headCode = (String) context.get("headCode"); // 品牌code
		// String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String rowId = (String)  context.get("rowId"); // 角色Id
		String brandId = (String)  context.get("productBrandId"); // 商户ID
		String roleName = (String)  context.get("roleName"); // 角色名称
		String status = (String)  context.get("status"); // 角色状态
		String descp = (String)  context.get("descp"); // 角色描述
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(rowId)) {
			params.put("rowId", rowId); // 角色Id
		}
		if (UtilValidate.isNotEmpty(brandId)) {
			params.put("productBrandId", brandId); // 商户ID
		}
		if (UtilValidate.isNotEmpty(roleName)) {
			params.put("roleName", roleName); // 角色名称
		}
		if (UtilValidate.isNotEmpty(status)) {
			params.put("status", status.toString()); // 角色状态
		}
		if (UtilValidate.isNotEmpty(descp)) {
			params.put("descp", descp); // 角色描述
		}
		params.put("brandCode", "10001");

		// URL的设定
		String httpUrl = rootIp_fan + source + "/privilege/modifyRole";
		// 返回值
		String res = null;
		try {
			res = HttpUtil.post(httpUrl, headers, params);
			// response = mockData.mocJsonDataCrm30();
			if (!UtilValidate.isEmpty(res)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(res);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 * 获取商户菜单列表(全部菜单) To Crm端(CRM_31)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getBrandMenuLisCrm31(DispatchContext ctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		String headCode = (String) context.get("headCode"); // 品牌code
		String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String brandCode = (String) context.get("brandCode"); // 商户编码
		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有
		// HTTP请求
		// 使用httpclient请求
		// 设置header头
		// headers.put("code", headCode); // 品牌code
		// headers.put("key", headKey); // 品牌key
		// 设置参数
		// header头
		if (UtilValidate.isNotEmpty(headCode)) {
			params.put("code", headCode); // 品牌code
		}
		if (UtilValidate.isNotEmpty(headKey)) {
			params.put("key", headKey); // 品牌key

		}

		// 输入参数
		if (UtilValidate.isNotEmpty(brandCode)) {
			params.put("brandCode", brandCode); // 商户编码
		}

		// URL的设定
		// String httpUrl = rootIp+"/CRM/privilege/getBrandMenuList.do";
		String httpUrl = rootIp_fan + source
				+ "/privilege/getBrandMenuList";
		// 返回值
		String response = null;
		Map<String, Object> tmpMap = FastMap.newInstance();// 节点临时用map

		try {
			 response= HttpUtil.post(httpUrl, headers, params);
//			response = mockData.mocJsonDataCrm31();
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				tmpMap = FastMap.newInstance();
				if (data != "null") {
					tmpMap = analysisData.analysisDataCrm31(data);
				}
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		// 解析后得到
		result.put("parentMap", tmpMap.get("parentMap")); // 父节点菜单项
		result.put("childrenMap", tmpMap.get("childrenMap")); // 子节点菜单项

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 * 保存角色权限（菜单） To Crm端(CRM_3233)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> saveRolePermissionCrm3233(
			DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
//		String headCode = (String) context.get("headCode"); // 品牌code
//		String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String roleId = (String) context.get("roleId"); // 角色Id
		String menuIds = (String) context.get("menuIds"); // 菜单id
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// HTTP请求
		// 使用httpclient请求
		// 设置header头
		// headers.put("code", headCode); // 品牌code
		// headers.put("key", headKey); // 品牌key
		// 设置参数
		// header头
//		if (UtilValidate.isNotEmpty(headCode)) {
//			params.put("code", headCode); // 品牌code
//		}
//		if (UtilValidate.isNotEmpty(headKey)) {
//			params.put("key", headKey); // 品牌key
//
//		}

		// 输入参数

		if (UtilValidate.isNotEmpty(roleId)) {
			params.put("roleId", roleId); // 角色Id
		}
		if (UtilValidate.isNotEmpty(menuIds)) {
			params.put("menuIds", menuIds); // 菜单id
		}

		// URL的设定 TODO
		String httpUrl = rootIp_fan + source +"/privilege/modifyRoleMenuRef";
		// String httpUrl =
		// rootIp+"/invoker/ico/crm/privilege/saveRolePermission";
		// String httpUrl =
		// rootIp+"/middle/invoker/ico/crm/cust/addCusttomerAndMerge";
		// 返回值
		String response = null;
		try {
			response= HttpUtil.post(httpUrl, headers, params);
//			response = mockData.mocJsonDataCrm3233();
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
			}
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 * 获取商户菜单列表(全部菜单) To Crm端(CRM_34)
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getMenuIdsByRoleIdCrm34(
			DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头
		// 获取参数
		// HTTP HEAD 参数
		String headCode = (String) context.get("headCode"); // 品牌code
		String headKey = (String) context.get("headKey"); // 品牌key
		// 输入参数
		String roleId = (String) context.get("roleId"); // 角色Id
		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有
		// HTTP请求
		// 使用httpclient请求
		// 设置header头
		// headers.put("code", headCode); // 品牌code
		// headers.put("key", headKey); // 品牌key
		// 设置参数
		// header头
		if (UtilValidate.isNotEmpty(headCode)) {
			params.put("code", headCode); // 品牌code
		}
		if (UtilValidate.isNotEmpty(headKey)) {
			params.put("key", headKey); // 品牌key

		}

		// 输入参数
		if (UtilValidate.isNotEmpty(roleId)) {
			params.put("roleId", roleId); // 角色Id
		}

		// URL的设定
		String httpUrl = rootIp_fan + source +"/privilege/getMenusByRoleId";
		// 返回值
		String response = null;

		Map<String, Object> tmpMap = new HashMap<String, Object>();// 节点临时用map

		try {
			 response= HttpUtil.post(httpUrl, headers, params);
//			response = mockData.mocJsonDataCrm34();
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
				// 输出参数
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				tmpMap = FastMap.newInstance();
				if (data != "null") {
					tmpMap = analysisData.analysisDataCrm31(data);
				}
			}

		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		// 解析后得到
		result.put("parentMap", tmpMap.get("parentMap")); // 父节点菜单项
		result.put("childrenMap", tmpMap.get("childrenMap")); // 子节点菜单项

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}


	/**
	 * 会员权益发放 To Crm端(CRM_05)
	 *
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> addRightsToMemberCrm05(
			DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		// 输入参数
		String memberCode = (String) context.get("memberCode"); // 会员号
		String packageCode = (String) context.get("packageCode"); // 权益包编号
		String startDate = (String) context.get("startDate"); // 起始时间
		String endDate = (String) context.get("endDate"); // 结束时间
		String packageType = (String) context.get("packageType"); // 权益类型

		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(memberCode)) {
			params.put("memberCode", memberCode); // 会员号
		}
		if (UtilValidate.isNotEmpty(packageCode)) {
			params.put("packageCode", packageCode); // 会员号
		}
		if (UtilValidate.isNotEmpty(startDate)) {
			try {
				params.put("startDate", UtilDateTime.stringToTimeStamp(startDate, "yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), Locale.getDefault()));

			} catch (ParseException e) {
				Debug.log(e.getMessage());
			}
		}
		if (UtilValidate.isNotEmpty(endDate)) {
			try {
				params.put("endDate", UtilDateTime.stringToTimeStamp(endDate, "yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), Locale.getDefault()));
			} catch (ParseException e) {
				Debug.log(e.getMessage());
			}
		}
		if (UtilValidate.isNotEmpty(packageType)) {
			params.put("packageType", packageType); // 会员号
		}


		// URL的设定
		String httpUrl = rootIp + source + "/cust/addRightsToMember";
		// 返回值
		String response = null;

		Map<String, Object> tmpMap = new HashMap<String, Object>();// 节点临时用map

		try {
			response= HttpUtil.post(httpUrl, headers, params);
//			response = mockData.mocJsonDataCrm34();
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
				// 输出参数
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				tmpMap = FastMap.newInstance();
				if (data != "null") {
					tmpMap = analysisData.analysisDataCrm31(data);
				}
			}

		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 * 会员权益消费 To Crm端(CRM_07)
	 *
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> expendRightsCrm07(
			DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		// 输入参数
		String memberCode = (String) context.get("memberCode"); // 会员号
		String rightsAccountId = (String) context.get("rightsAccountId"); // 权益id
		Integer costCount = (Integer) context.get("costCount"); // 数值

		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(memberCode)) {
			params.put("memberCode", memberCode); // 会员号
		}
		if (UtilValidate.isNotEmpty(rightsAccountId)) {
			params.put("rightsAccountId", rightsAccountId); // 权益id
		}
		if (UtilValidate.isNotEmpty(costCount)) {
			params.put("costCount", costCount); // 数值
		}

		// URL的设定
		String httpUrl = rootIp + source + "/cust/expendRights";
		// 返回值
		String response = null;

		Map<String, Object> tmpMap = new HashMap<String, Object>();// 节点临时用map

		try {
			response= HttpUtil.post(httpUrl, headers, params);
//			response = mockData.mocJsonDataCrm34();
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
				// 输出参数
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				tmpMap = FastMap.newInstance();
				if (data != "null") {
					tmpMap = analysisData.analysisDataCrm31(data);
				}
			}

		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		// 解析后得到
		result.put("parentMap", tmpMap.get("parentMap")); // 父节点菜单项
		result.put("childrenMap", tmpMap.get("childrenMap")); // 子节点菜单项

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 *  优惠券发放
	 *  会员在前端领取券的时候调CRM接口发放到CRM券账户
	 *  @description
	 * @param dct
	 * @param context
	 * @return
	 */
	public Map<String, Object> receiveCouponsCrm10(DispatchContext dct, Map<String,? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		// 输入参数
		String activityCode = (String) context.get("activityCode"); // 活动编号
		String isFlag = (String) context.get("isFlag"); // 核销标识
		String memberCodes = (String) context.get("memberCodes"); // 会员编号
		String openIds = (String) context.get("openIds"); // 会员openids
		String couponId = (String) context.get("couponId"); // 会员openids

		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(activityCode)) {
			params.put("activityCode", activityCode); // 活动编号
		}
		if (UtilValidate.isNotEmpty(isFlag)) {
			params.put("isFlag", isFlag); // 核销标识
		}
		if (UtilValidate.isNotEmpty(memberCodes)) {
			params.put("memberCodes", memberCodes); // 会员编号
		}
		if (UtilValidate.isNotEmpty(memberCodes)) {
			params.put("openIds", openIds); // 会员openids
		}
		if (UtilValidate.isNotEmpty(couponId)) {
			params.put("couponId", couponId); //优惠券id
		}


		// URL的设定
		String httpUrl = rootIp + source + "/couponInfo/pushCouponAccount";
		// 返回值
		String response = null;

		Map<String, Object> tmpMap = new HashMap<String, Object>();// 节点临时用map

		try {
			response= HttpUtil.post(httpUrl, headers, params);
//			response = mockData.mocJsonDataCrm34();
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
				// 输出参数
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				tmpMap = FastMap.newInstance();
				if (data != "null") {
					tmpMap = analysisData.analysisDataCrm31(data);
				}
			}

		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 * 优惠券校验(微信不做校验) To Crm端(CRM_09)
	 *
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> checkCouponAccountIsExitsCrm09(
			DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		// 输入参数
		String memberCode = (String) context.get("memberCode"); // 会员号
		String couponNo = (String) context.get("couponNo"); // 优惠券编号

		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(memberCode)) {
			params.put("memberCode", memberCode); // 会员号
		}
		if (UtilValidate.isNotEmpty(couponNo)) {
			params.put("couponNo", couponNo); // 优惠券编号
		}


		// URL的设定
		String httpUrl = rootIp + source + "/couponInfo/checkCouponAccountIsExits";
		// 返回值
		String response = null;

		Map<String, Object> tmpMap = new HashMap<String, Object>();// 节点临时用map

		try {
			response= HttpUtil.post(httpUrl, headers, params);
//			response = mockData.mocJsonDataCrm34();
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
				// 输出参数
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				tmpMap = FastMap.newInstance();
				if (data != "null") {
					tmpMap = analysisData.analysisDataCrm31(data);
				}
			}

		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		// 解析后得到
		result.put("parentMap", tmpMap.get("parentMap")); // 父节点菜单项
		result.put("childrenMap", tmpMap.get("childrenMap")); // 子节点菜单项

		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

	/**
	 * 钱包接口 To Crm端(CRM_3637)
	 *
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> sycnCustomerMoneyCrm3637(
			DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		// 输入参数
		String custId = (String) context.get("custId"); // 账号id,crm主键
		String custCode = (String) context.get("custCode"); // 账号code
		String curMoney = (String) context.get("money"); // 余额
		String remark = (String) context.get("remark"); // 备注
		String payType = (String) context.get("payType"); // 支付方式
		String orderNo = (String) context.get("orderNo"); // 订单号
		String createUser = (String) context.get("createUser"); // 创建者

		// 输出参数
		String data = null; // 结果集
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId); // 会员号
		}
		if (UtilValidate.isNotEmpty(custCode)) {
			params.put("custCode", custCode); // 账号code
		}
		if (UtilValidate.isNotEmpty(curMoney)) {
			params.put("curMoney", curMoney); // 余额
		}
		if (UtilValidate.isNotEmpty(remark)) {
			params.put("remark", remark); // 备注
		}
		if (UtilValidate.isNotEmpty(payType)) {
			params.put("payType", payType); // 支付方式
		}
		if (UtilValidate.isNotEmpty(createUser)) {
			params.put("createUser", createUser); // 创建者
		}
		if (UtilValidate.isNotEmpty(orderNo)) {
			params.put("orderNo", orderNo); // 订单号
		}

		// URL的设定
		String httpUrl = rootIp + source + "/cust/sycnCustomerMoney";
		// 返回值
		String response = null;

		Map<String, Object> tmpMap = new HashMap<String, Object>();// 节点临时用map

		try {
			response= HttpUtil.post(httpUrl, headers, params);
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
				// 输出参数
				if (UtilValidate.isNotEmpty(mainJsonMap.get("data"))) {
					data = mainJsonMap.get("data").toString();
				}
				tmpMap = FastMap.newInstance();
				if (data != "null") {
					tmpMap = analysisData.analysisDataCrm31(data);
				}
			}

		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code


		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap
					.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}
	
	/**
	 * 根据用户获取用户升级条件
	 * 
	 * @param ctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> sycnLevelCrm42(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		// 输入参数
		String custId = (String) context.get("custId"); // 账号id,crm主键
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId); // 会员号
		}
		// URL的设定
		String httpUrl = rootIp + source + "/cust/queryCustomerLevelIntegral";
		// 返回值
		String response = null;

		try {
			response= HttpUtil.post(httpUrl, headers, params);
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
			}

		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code


		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}
	
	/**
	 * 查询积分使用规则（积分价值）
	 * 
	 * @param ctx
	 * @param context
	 * @return 
	 */
	public static Map<String, Object> syncIntegralDescCrm43(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		// 输入参数
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有

		// 输入参数
		params.put("productBrandId", "1"); // 会员号
		// URL的设定
		String httpUrl = rootIp + source + "/integral/getIntegralValue";
		// 返回值
		String response = null;

		try {
			response= HttpUtil.post(httpUrl, headers, params);
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
			}

		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code


		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}
	
	/**
	 * 当前用户可用于分享的积分数（可赠送的积分数）
	 * 
	 * @param ctx
	 * @param context
	 * @return 
	 */
	public static Map<String, Object> syncTotalIntegralCrm44(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance(); // 结果集
		Map<String, Object> params = FastMap.newInstance(); // 请求参数
		Map<String, String> headers = FastMap.newInstance();// 请求头

		// 输入参数
		String custId = (String) context.get("custId"); // 账号id,crm主键
		// 输出参数
		Map<String, Object> mainJsonMap = FastMap.newInstance();
		// 中间件调用错误信息
		String iserr = "0"; // 0:没有 1：有
		
		// 输入参数
		if (UtilValidate.isNotEmpty(custId)) {
			params.put("custId", custId); // 会员号
		}
		// URL的设定
		String httpUrl = rootIp + source + "/integral/queryTotalIntegral";
		// 返回值
		String response = null;
		
		try {
			response= HttpUtil.post(httpUrl, headers, params);
			if (!UtilValidate.isEmpty(response)) {
				mainJsonMap = FastMap.newInstance();
				// ** 响应数据转换成json格式 *//
				mainJsonMap = analysisData.analysisDataForMainJson(response);
			}
			
		} catch (Exception e) {
			Debug.logError(e, "Problem connecting to Fedex server", module);
		}
		// 返回的参数
		result.put("expStatus", mainJsonMap.get("status")); // 标识
		result.put("code", mainJsonMap.get("code")); // 编码
		result.put("msg", mainJsonMap.get("msg")); // 信息
		result.put("data", mainJsonMap.get("data")); // 结果集
		result.put("receivetime", mainJsonMap.get("receivetime")); // 请求时间
		result.put("backtime", mainJsonMap.get("backtime")); // 返回时间
		result.put("syscode", mainJsonMap.get("syscode")); // 系统code
		result.put("itfcode", mainJsonMap.get("itfcode")); // 接口code
		
		
		// 中间件调用错误信息
		if (UtilValidate.isNotEmpty(mainJsonMap.get("err"))) {
			iserr = "1";// 1：有错误 0：无错误
			Map<String, Object> errInfoMap = (Map<String, Object>) mainJsonMap.get("errInfo");
			result.put("errInfoMap", errInfoMap); // 错误信息
		}
		result.put("iserr", iserr); // 是否有错
		return result;
	}

}
