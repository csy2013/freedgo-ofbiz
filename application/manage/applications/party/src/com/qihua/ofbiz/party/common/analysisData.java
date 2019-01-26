package com.qihua.ofbiz.party.common;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.ss.formula.ptg.MemErrPtg;
import org.ofbiz.base.util.UtilValidate;

public class analysisData {
	/**
	 * 获取商户菜单列表(全部菜单)
	 * @param menu
	 */
	 public static Map<String,Object> analysisDataCrm31(String menu){
		 Map<String,Object> menuInfo=new HashMap<String, Object>();
		 Map<String, Map<String, Object>> parentMap=new HashMap<String,Map<String, Object>>(); // 父亲节点map
		 Map <String, List<Map<String,Object>>> childrenMap=new HashMap<String, List<Map<String,Object>>>(); // 子节点map
		 Map<String,Object> tmpMap=new HashMap<String, Object>();//节点临时用map
		 
		 String rowId = "";   //菜单Id
         String title = "";   //菜单名称
         String subMenus = "";//子菜单
         String url="";	      //菜单连接
         String icon="";	  //菜单短连接
         Integer sort=null;	  //顺序号
         String createTime="";//创建时间
         Integer status=null; //菜单状态
         String parentId="";  //父id
         
		 JSONArray list = HttpUtil.convertToJSONArray(menu);
 		 for(int i = 0; i < list.size() ; i++){
 			  // 初始化
 			  tmpMap=new HashMap<String, Object>();//节点临时用map
 			  rowId = "";     //菜单Id
 			  title = "";     //菜单名称
 			  subMenus = "";  //子菜单
 			  url="";	      //菜单连接
 			  icon="";	      //菜单短连接
 			  sort=null;	  //顺序号
 			  createTime="";  //创建时间
 			  status=null;	  //菜单状态
 			  parentId="";	  //父id
 			  
              /** 获取json对象 */
              JSONObject jsonObject = list.getJSONObject(i);
              List<Map<String,Object>> childMenuList=FastList.newInstance();
              /** 主键ID */
              if(jsonObject.containsKey("rowId")){
            	  rowId = jsonObject.getString("rowId");//菜单Id
              }
              if(jsonObject.containsKey("title")){
            	  title = jsonObject.getString("title");//菜单名称
              }
              if(jsonObject.containsKey("subMenus")){
            	  subMenus = jsonObject.getString("subMenus");//子菜单
              }
              if(jsonObject.containsKey("url")){
            	  url=jsonObject.getString("url");//菜单连接
              }
              if(jsonObject.containsKey("icon")){
            	  icon=jsonObject.getString("icon");//菜单短连接
              }
              if(jsonObject.containsKey("sort")){
            	  sort=jsonObject.getInt("sort");//顺序号
              }
              if(jsonObject.containsKey("createTime")){
            	  createTime=jsonObject.getString("createTime");//创建时间
              }
              if(jsonObject.containsKey("status")){
            	  status=jsonObject.getInt("status");//菜单状态
              }
              if(jsonObject.containsKey("parentId")){
            	  parentId=jsonObject.getString("parentId");//父id
              }
              
              tmpMap.put("rowId", rowId);
              tmpMap.put("title", title);
              tmpMap.put("parentId", parentId);
              
              parentMap.put(rowId,  tmpMap);
              if(subMenus!="null" && subMenus!=""){
             	 childMenuList=analysisData.analysisMenuList(subMenus);
              }
              if(childMenuList.size()>0){
           	    childrenMap.put(rowId, childMenuList);
              }
              
              menuInfo.put("parentMap", parentMap);
              menuInfo.put("childrenMap", childrenMap);
         }
		 return menuInfo;
	 }
	 
	 /**
	  * 解析菜单列表
	  * @param menu
	  * @return
	  */
	 public static List<Map<String,Object>> analysisMenuList(String menu){
	        JSONArray list = HttpUtil.convertToJSONArray(menu);
	        List<Map<String,Object>> childMenuList=FastList.newInstance();
	        Map<String,Object> tmpMap=new HashMap<String, Object>();
			for(int i = 0; i < list.size() ; i++){
				 tmpMap=new HashMap<String, Object>();
	             /** 获取json对象 */
	             JSONObject jsonObject = list.getJSONObject(i);
	             // 菜单解析项目
	             String rowId="";	   //菜单Id
	             String title="";	   //菜单名称
	             String url="";	       //菜单连接
	             String icon="";	   //菜单图片
	             String shortUrl="";   //菜单短连接
	             Integer sort=null;	   //顺序号
	             String createTime=""; //创建时间
	             Integer status=null;  //菜单状态
	             String parentId="";   //父id
	             String subMenus="";   //子菜单
	             
	             if(jsonObject.containsKey("rowId")){
	            	 rowId = jsonObject.getString("rowId");
	             }
	             if(jsonObject.containsKey("title")){
	            	 title = jsonObject.getString("title");
	             }
	             if(jsonObject.containsKey("url")){
	            	 url = jsonObject.getString("url");
	             }
	             if(jsonObject.containsKey("icon")){
	            	 icon = jsonObject.getString("icon");
	             }
	            
	             if(jsonObject.containsKey("shortUrl")){
	            	 shortUrl = jsonObject.getString("shortUrl");
	             }
	             if(jsonObject.containsKey("sort")){
	            	 sort = jsonObject.getInt("sort");
	             }
	             if(jsonObject.containsKey("createTime")){
	            	 createTime = jsonObject.getString("createTime");
	             }
	             if(jsonObject.containsKey("status")){
	            	 status = jsonObject.getInt("status");
	             }
	             if(jsonObject.containsKey("parentId")){
	            	 parentId = jsonObject.getString("parentId");
	             }
	             if(jsonObject.containsKey("subMenus")){
	            	 subMenus = jsonObject.getString("subMenus");
	             }
	             

	             tmpMap.put("rowId",rowId);
	             tmpMap.put("title",title);
	             tmpMap.put("parentId",parentId);
	             
	             childMenuList.add(tmpMap);
	        }
			return childMenuList;
	    }
	 
	 
	 
	    /**
		 * 会员优惠券查询
		 * @param data
		 */
		 public static Map<String,Object> analysisDataCrm08(String data){
			    JSONObject dataInfo = HttpUtil.convertToJSONObject(data);
	            String current=dataInfo.getString("current");
	            String pageSize=dataInfo.getString("pageSize");
	            String totalRecord=dataInfo.getString("totalRecord");
	            String totalPage=dataInfo.getString("totalPage");
	            String pre=dataInfo.getString("pre");
	            String next=dataInfo.getString("next");
	            String couponInfos=dataInfo.getString("list");
	            
	            Map<String,Object> couponInfo=new HashMap<String, Object>();
	            List<Map<String,Object>> couponInfoList=FastList.newInstance();
	            Map<String,Object> tmpMap=new HashMap<String,Object>();
		        JSONArray list = HttpUtil.convertToJSONArray(couponInfos);
				for(int i = 0; i < list.size() ; i++){
		             /** 获取json对象 */
		             JSONObject jsonObject = list.getJSONObject(i);
		             tmpMap=new HashMap<String, Object>();
		             // 菜单解析项目
	                 String rowId = "";      //主键编号
	                 String brandId = "";    //商户id
	                 String couponId = "";   //优惠券id
	                 String couponCode="";	 //优惠券号码
	                 String custId="";       //客户id
	                 String activityId="";	 //活动id
	                 String batch="";	     //批次号
	                 String  startTime="";	 //起始时间
	                 String endTime="";	     //结束时间
	                 Integer status=0;	     //状态
	                 String createTime="";	 //创建时间
	                 String couponName="";	 //优惠券名称
	                 String typeId="";	     //优惠券类型
	                 String itemName="";	 //优惠券类型名称
	                 
	                 if(jsonObject.containsKey("rowId")){
	                	 rowId = jsonObject.getString("rowId");//主键编号
	                 }
	            	 if(jsonObject.containsKey("productBrandId")){
	            		 brandId = jsonObject.getString("productBrandId");//商户id
	            	 }
	            	 if(jsonObject.containsKey("couponId")){
	            	     couponId = jsonObject.getString("couponId");//优惠券id
	            	 }
	            	 if(jsonObject.containsKey("couponCode")){
	            		 couponCode=jsonObject.getString("couponCode");//优惠券号码
	            	 }
	            	 if(jsonObject.containsKey("custId")){
	            		 custId=jsonObject.getString("custId");//客户id
	            	 }
	            	 if(jsonObject.containsKey("activityId")){
	            		 activityId=jsonObject.getString("activityId");//活动id
	            	 }
	            	 if(jsonObject.containsKey("batch")){
	            		 batch=jsonObject.getString("batch");//批次号
	            	 }
	            	 if(jsonObject.containsKey("startTime")){
	            		 startTime=jsonObject.getString("startTime");//起始时间
	            	 }
	            	 if(jsonObject.containsKey("endTime")){
	            		 endTime=jsonObject.getString("endTime");//结束时间
	            	 }
	            	 if(jsonObject.containsKey("status")){
	            		 if(HttpUtil.checkJsonKey(jsonObject.get("status"))){
	            			 status=jsonObject.getInt("status");//状态
                		 }
	            	 }
	            	 if(jsonObject.containsKey("createTime")){
	            		 createTime=jsonObject.getString("createTime");//创建时间
	            	 }
	            	 if(jsonObject.containsKey("couponName")){
	            		 couponName=jsonObject.getString("couponName");//优惠券名称
	            	 }
	            	 if(jsonObject.containsKey ("typeId")){
	            		 typeId=jsonObject.getString("typeId");//优惠券类型
	            	 }
	            	 if(jsonObject.containsKey("itemName")){
	            		 itemName=jsonObject.getString("itemName");//优惠券类型名称
	            	 }
	                 
	                 tmpMap.put("rowId", rowId);
	                 tmpMap.put("productBrandId", brandId);
	                 tmpMap.put("couponId", couponId);
	                 tmpMap.put("couponCode", couponCode);
	                 tmpMap.put("custId", custId);
	                 tmpMap.put("activityId", activityId);
	                 tmpMap.put("batch", batch);
	                 tmpMap.put("startTime", startTime);
	                 tmpMap.put("endTime", endTime);
	                 tmpMap.put("status", status);
	                 tmpMap.put("createTime", createTime);
	                 tmpMap.put("couponName", couponName);
	                 tmpMap.put("typeId", typeId);
	                 tmpMap.put("itemName", itemName);
	                 
	                 couponInfoList.add(tmpMap);
		        }
				couponInfo.put("current", current);
				couponInfo.put("pageSize", pageSize);
				couponInfo.put("totalRecord", totalRecord);
				couponInfo.put("totalPage", totalPage);
				couponInfo.put("pre", pre);
				couponInfo.put("next", next);
				couponInfo.put("list", couponInfoList);
				return couponInfo;
		    }
		 
		 
		 
	    /**
		 * 会员权益查询
		 * @param data
		 */
		 public static Map<String,Object> analysisDataCrm06(String data){
			    JSONObject dataInfo = HttpUtil.convertToJSONObject(data);
	            String current=dataInfo.getString("current");
	            String pageSize=dataInfo.getString("pageSize");
	            String totalRecord=dataInfo.getString("totalRecord");
	            String totalPage=dataInfo.getString("totalPage");
	            String pre=dataInfo.getString("pre");
	            String next=dataInfo.getString("next");
	            String rightsInfos=dataInfo.getString("list");
	            
	            Map<String,Object> rightInfo=new HashMap<String, Object>();
	            List<Map<String,Object>> rightInfoList=FastList.newInstance();
	            Map<String,Object> tmpMap=new HashMap<String,Object>();
		        JSONArray list = HttpUtil.convertToJSONArray(rightsInfos);
				for(int i = 0; i < list.size() ; i++){
		             /** 获取json对象 */
		             JSONObject jsonObject = list.getJSONObject(i);
		             tmpMap=new HashMap<String, Object>();
		             // 解析项目
	                 String rowId="" ;          //主键编号
	                 String brandId="" ;        //商户id
	                 String rightsId="" ;       //权益id
	                 String rightsCode="" ;     //权益编号
	                 String startTime="" ;      //起始时间
	                 String endTime="" ;        //结束时间
	                 Integer rightsValue=0;     //权益价值
	                 String astrict="" ;        //限制次数
	                 String period="" ;         //周期
	                 String explainDescp="" ;   //权益说明
	                 String custId="" ;         //客户id
	                 String usedCount="" ;      //已使用的次数
	                 String rightsAmt="" ;      //权益金额
	                 String rightsDiscount="" ; //权益折扣
	                 String createTime="" ;     //创建时间
	                 String rightsPackageid="" ;//权益包id
	                 String useCount="" ;       //当前可以使用的次数
	                 String packageName="" ;    //所属权益包（名称）
	                 Integer status=0 ;         //状态
	                 String rightsPolicyId="" ; //权益政策
	                 
	                 if(jsonObject.containsKey("rowId")){
	                	 rowId=jsonObject.getString("rowId");//主键编号
	                 }
                	 if(jsonObject.containsKey("productBrandId")){
                		 brandId=jsonObject.getString("productBrandId");//商户id
                	 }
                	 if(jsonObject.containsKey("rightsId")){
                		 rightsId=jsonObject.getString("rightsId");//权益id
                	 }
                	 if(jsonObject.containsKey("rightsCode")){
                		 rightsCode=jsonObject.getString("rightsCode");//权益编号
                	 }
                	 if(jsonObject.containsKey("startTime")){
                		 startTime=jsonObject.getString("startTime");//起始时间
                	 }
                	 if(jsonObject.containsKey("endTime")){
                		 endTime=jsonObject.getString("endTime");//结束时间
                	 }
                	 if(jsonObject.containsKey("rightsValue")){
                		 if(HttpUtil.checkJsonKey(jsonObject.get("rightsValue"))){
                			 rightsValue=jsonObject.getInt("rightsValue");//权益价值
                		 }
                	 }
                	 if(jsonObject.containsKey("astrict")){
                		 astrict=jsonObject.getString("astrict");//限制次数
                	 }
                	 if(jsonObject.containsKey("period")){
                		 period=jsonObject.getString("period");//周期
                	 }
                	 if(jsonObject.containsKey("explainDescp")){
                		 explainDescp=jsonObject.getString("explainDescp");//权益说明
                	 }
                	 if(jsonObject.containsKey("custId")){
                		 custId=jsonObject.getString("custId");//客户id
                	 }
                	 if(jsonObject.containsKey("usedCount")){
                		 usedCount=jsonObject.getString("usedCount");//已使用的次数
                	 }
                	 if(jsonObject.containsKey("rightsAmt")){
                		 rightsAmt=jsonObject.getString("rightsAmt");//权益金额
                	 }
                	 if(jsonObject.containsKey("rightsDiscount")){
                		 rightsDiscount=jsonObject.getString("rightsDiscount");//权益折扣
                	 }
                	 if(jsonObject.containsKey("createTime")){
                		 createTime=jsonObject.getString("createTime");//创建时间
                	 }
                	 if(jsonObject.containsKey("rightsPackageid")){
                		 rightsPackageid=jsonObject.getString("rightsPackageid");//权益包id
                	 }
                	 if(jsonObject.containsKey("useCount")){
                		 useCount=jsonObject.getString("useCount");//当前可以使用的次数
                	 }
                	 if(jsonObject.containsKey("packageName")){
                		 packageName=jsonObject.getString("packageName");//所属权益包（名称）
                	 }
                	 if(jsonObject.containsKey("status")){
                		 if(HttpUtil.checkJsonKey(jsonObject.get("status"))){
                			 status=jsonObject.getInt("status");//状态
                		 }
                	 }
                	 if(jsonObject.containsKey("rightsPolicyId")){
                		 rightsPolicyId=jsonObject.getString("rightsPolicyId");//权益政策
                	 }
	                 
	                 tmpMap.put("rowId",rowId);//主键编号
	                 tmpMap.put("productBrandId",brandId);//商户id
	                 tmpMap.put("rightsId",rightsId);//权益id
	                 tmpMap.put("rightsCode",rightsCode);//权益编号
	                 tmpMap.put("startTime",startTime);//起始时间
	                 tmpMap.put("endTime",endTime);//结束时间
	                 tmpMap.put("rightsValue",rightsValue);//权益价值
	                 tmpMap.put("astrict",astrict);//限制次数
	                 tmpMap.put("period",period);//周期
	                 tmpMap.put("explainDescp",explainDescp);//权益说明
	                 tmpMap.put("custId",custId);//客户id
	                 tmpMap.put("usedCount",usedCount);//已使用的次数
	                 tmpMap.put("rightsAmt",rightsAmt);//权益金额
	                 tmpMap.put("rightsDiscount",rightsDiscount);//权益折扣
	                 tmpMap.put("createTime",createTime);//创建时间
	                 tmpMap.put("rightsPackageid",rightsPackageid);//权益包id
	                 tmpMap.put("useCount",useCount);//当前可以使用的次数
	                 tmpMap.put("packageName",packageName);//所属权益包（名称）
	                 tmpMap.put("status",status);//状态
	                 tmpMap.put("rightsPolicyId",rightsPolicyId);//权益政策
	                 
	                 rightInfoList.add(tmpMap);
		        }
				rightInfo.put("current", current);
				rightInfo.put("pageSize", pageSize);
				rightInfo.put("totalRecord", totalRecord);
				rightInfo.put("totalPage", totalPage);
				rightInfo.put("pre", pre);
				rightInfo.put("next", next);
				rightInfo.put("list", rightInfoList);
				return rightInfo;
		    }
		 
		 
		    /**
			 * 会员信息查询（基本信息）
			 * @param data
			 */
			 public static Map<String,Object> analysisDataCrm03(String data){
				    JSONObject dataInfo = HttpUtil.convertToJSONObject(data);
		            String current=dataInfo.getString("current");
		            String pageSize=dataInfo.getString("pageSize");
		            String totalRecord=dataInfo.getString("totalRecord");
		            String totalPage=dataInfo.getString("totalPage");
//		            String pre=dataInfo.getString("pre");
//		            String next=dataInfo.getString("next");
		            String custInfos=dataInfo.getString("list");
		            
		            Map<String,Object> custInfo=new HashMap<String, Object>();
		            List<Map<String,Object>> custInfoInfoList=FastList.newInstance();
		            Map<String,Object> tmpMap=new HashMap<String,Object>();
			        JSONArray list = HttpUtil.convertToJSONArray(custInfos);
					for(int i = 0; i < list.size() ; i++){
			             /** 获取json对象 */
			             JSONObject jsonObject = list.getJSONObject(i);
			             tmpMap=new HashMap<String, Object>();
			             // 解析项目
			             String custCode="";                   //客户编号
			             String vipCode="";                    //会员编号
			             String channelId="";                  //渠道id
			             String realName="";                   //真实名称
			             String picUrl="";                     //头像
			             String cust_type="";                  //客户类型
			             Integer gender=0;                     //性别
			             String wechat="";                     //微信openid
			             String sourceId="";                   //来源
			             String sourceSubId="";                //来源子渠道
			             String postCode="";                   //邮编
			             String address="";                    //地址
			             String storeId="";                    //门店（实体）
			             String birthday="";                   //生日
			             String occuption="";                  //职业
			             String qq="";                         //QQ
			             String blog="";                       //微博
			             String passportType="";               //证件类型
			             String passportNo="";                 //证件号
			             String workplace="";                  //工作单位地址
			             String hobby="";                      //爱好
			             String income="";                     //收入
			             String province="";                   //省份
			             String city="";                       //城市
			             String area="";                       //区域
			             String createId="";                   //创建人id
			             String modifyId="";                   //修改人id
			             String modifyTime="";                 //修改时间
			             Integer status=0;                     //状态
			             String createTime="";                 //创建时间
			             String isFollow="";                   //是否关注
			             Integer accountMac=0;                 //第三方会员编码
			             String custType="";                   //客户类型
			             String channelName="";                //客户来源名称
			             String curGradeName="";               //会员级别
			             String provinceName="";               //省份名称
			             String cityName="";                   //城市名称
			             BigDecimal tradeAmt=new BigDecimal(0);//交易金额
			             String tradeNum="";                   //交易次数
			             BigDecimal tradeAvg=new BigDecimal(0);//客单价
			             String lastTradeTime="";              //上次交易时间
			             String levelId="";                    //等级id
			             String levelInvalid="";               //等级失效日期
			             Integer levelSort=0;                  //等级序号
			             Integer rightsNum=0;                  //会员权益数量
			             Integer couponNum=0;                  //优惠券数量
			             Integer buyerQuantity=0;              //剩余数量
			             String channelRowId="";               //会员子表id
			             String levelName="";                  //等级名称
			             String curGrade="";                  //等级名称
			             String sourceName="";                 //来源名称
			             BigDecimal curMoney=new BigDecimal(0);// 钱包金额
			             Integer vehNum = 0;				   // 资产总数
			             
			             if(jsonObject.containsKey("custCode")){
			            	 custCode=jsonObject.getString("custCode");//客户编号
			             }
		            	 if(jsonObject.containsKey("vipCode")){
		            		 vipCode=jsonObject.getString("vipCode");//会员编号
		            	 }
		            	 if(jsonObject.containsKey("channelId")){
		            		 channelId=jsonObject.getString("channelId");//渠道id
		            	 }
		            	 if(jsonObject.containsKey("realName")){
		            		 realName=jsonObject.getString("realName");//真实名称
		            	 }
		            	 if(jsonObject.containsKey("picUrl")){
		            		 picUrl=jsonObject.getString("picUrl");//头像
		            	 }
		            	 if(jsonObject.containsKey("cust_type")){
		            		 cust_type=jsonObject.getString("cust_type");//客户类型
		            	 }
		            	 if(jsonObject.containsKey("gender")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("gender"))){
		            			 gender=jsonObject.getInt("gender");//性别
		            		 }
		            	 }
		            	 if(jsonObject.containsKey("wechat")){
		            		 wechat=jsonObject.getString("wechat");//微信openid
		            	 }
		            	 if(jsonObject.containsKey("sourceId")){
		            		 sourceId=jsonObject.getString("sourceId");//来源
		            	 }
		            	 if(jsonObject.containsKey("sourceSubId")){
		            		 sourceSubId=jsonObject.getString("sourceSubId");//来源子渠道
		            	 }
		            	 if(jsonObject.containsKey("postCode")){
		            		 postCode=jsonObject.getString("postCode");//邮编
		            	 }
		            	 if(jsonObject.containsKey("address")){
		            		 address=jsonObject.getString("address");//地址
		            	 }
		            	 if(jsonObject.containsKey("storeId")){
		            		 storeId=jsonObject.getString("storeId");//门店（实体）
		            	 }
		            	 if(jsonObject.containsKey("birthday")){
		            		 birthday=jsonObject.getString("birthday");//生日
		            	 }
		            	 if(jsonObject.containsKey("occuption")){
		            		 occuption=jsonObject.getString("occuption");//职业
		            	 }
		            	 if(jsonObject.containsKey("qq")){
		            		 qq=jsonObject.getString("qq");//QQ
		            	 }
		            	 if(jsonObject.containsKey("blog")){
		            		 blog=jsonObject.getString("blog");//微博
		            	 }
		            	 if(jsonObject.containsKey("passportType")){
		            		 passportType=jsonObject.getString("passportType");//证件类型
		            	 }
		            	 if(jsonObject.containsKey("passportNo")){
		            		 passportNo=jsonObject.getString("passportNo");//证件号
		            	 }
		            	 if(jsonObject.containsKey("workplace")){
		            		 workplace=jsonObject.getString("workplace");//工作单位地址
		            	 }
		            	 if(jsonObject.containsKey("hobby")){
		            		 hobby=jsonObject.getString("hobby");//爱好
		            	 }
		            	 if(jsonObject.containsKey("income")){
		            		 income=jsonObject.getString("income");//收入
		            	 }
		            	 if(jsonObject.containsKey("province")){
		            		 province=jsonObject.getString("province");//省份
		            	 }
		            	 if(jsonObject.containsKey("city")){
		            		 city=jsonObject.getString("city");//城市
		            	 }
		            	 if(jsonObject.containsKey("area")){
		            		 area=jsonObject.getString("area");//区域
		            	 }
		            	 if(jsonObject.containsKey("createId")){
		            		 createId=jsonObject.getString("createId");//创建人id
		            	 }
		            	 if(jsonObject.containsKey("modifyId")){
		            		 modifyId=jsonObject.getString("modifyId");//修改人id
		            	 }
		            	 if(jsonObject.containsKey("modifyTime")){
		            		 modifyTime=jsonObject.getString("modifyTime");//修改时间
		            	 }
		            	 if(jsonObject.containsKey("status")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("status"))){
		            			 status=jsonObject.getInt("status");//状态
		            		 }
		            	 }
		            	 if(jsonObject.containsKey("createTime")){
		            		 createTime=jsonObject.getString("createTime");//创建时间
		            	 }
		            	 if(jsonObject.containsKey("isFollow")){
		            		 isFollow=jsonObject.getString("isFollow");//是否关注
		            	 }
		            	 if(jsonObject.containsKey("accountMac")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("accountMac"))){
		            			 accountMac=jsonObject.getInt("accountMac");//第三方会员编码
		            		 }
		            	 }
		            	 if(jsonObject.containsKey("custType")){
		            		 custType=jsonObject.getString("custType");//客户类型
		            	 }
		            	 if(jsonObject.containsKey("channelName")){
		            		 channelName=jsonObject.getString("channelName");//客户来源名称
		            	 }
		            	 if(jsonObject.containsKey("curGradeName")){
		            		 curGradeName=jsonObject.getString("curGradeName");//会员级别
		            	 }
		            	 if(jsonObject.containsKey("provinceName")){
		            		 provinceName=jsonObject.getString("provinceName");//省份名称
		            	 }
		            	 if(jsonObject.containsKey("cityName")){
		            		 cityName=jsonObject.getString("cityName");//城市名称
		            	 }
		            	 if(jsonObject.containsKey("tradeAmt")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("tradeAmt"))){
		            			 tradeAmt=new BigDecimal(jsonObject.getDouble("tradeAmt"));//交易金额
		            		 }
		            	 }
		            	 if(jsonObject.containsKey("tradeNum")){
		            		 tradeNum=jsonObject.getString("tradeNum");//交易次数
		            	 }
		            	 if(jsonObject.containsKey("tradeAvg")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("tradeAvg"))){
		            			 tradeAvg=new BigDecimal(jsonObject.getDouble("tradeAvg"));//客单价
		            		 }
		            	 }
		            	 if(jsonObject.containsKey("lastTradeTime")){
		            		 lastTradeTime=jsonObject.getString("lastTradeTime");//上次交易时间
		            	 }
		            	 if(jsonObject.containsKey("levelId")){
		            		 levelId=jsonObject.getString("levelId");//等级id
		            	 }
		            	 if(jsonObject.containsKey("levelInvalid")){
		            		 levelInvalid=jsonObject.getString("levelInvalid");//等级失效日期
		            	 }
		            	 if(jsonObject.containsKey("levelSort")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("levelSort"))){
				            	 levelSort=jsonObject.getInt("levelSort");//等级序号
				             }
		            	 }
		            	 if(jsonObject.containsKey("rightsNum")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("rightsNum"))){
				            	 rightsNum=jsonObject.getInt("rightsNum");//会员权益数量
				             }
		            	 }
		            	 if(jsonObject.containsKey("couponNum")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("couponNum"))){
				            	 couponNum=jsonObject.getInt("couponNum");//优惠券数量
				             }
				             
				         }
		            	 if(jsonObject.containsKey("buyerQuantity")){
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("buyerQuantity"))){
				            	 buyerQuantity=jsonObject.getInt("buyerQuantity");//剩余数量
				             }
		            	 }
		            	 if(jsonObject.containsKey("channelRowId")){
		            		 channelRowId=jsonObject.getString("channelRowId");//会员子表id
		            	 }
		            	 if(jsonObject.containsKey("levelName")){
		            		 levelName=jsonObject.getString("levelName");//等级名称
		            	 }
		            	 if(jsonObject.containsKey("curGrade")){
		            		 curGrade=jsonObject.getString("curGrade");//等级名称
		            	 }
		            	 if(jsonObject.containsKey("sourceName")){
		            		 sourceName=jsonObject.getString("sourceName");//来源名称
		            	 }
		            	 if(jsonObject.containsKey("curMoney")) {
		            		 if(HttpUtil.checkJsonKey(jsonObject.get("curMoney"))){
		            			 curMoney=new BigDecimal(jsonObject.getDouble("curMoney"));//钱包金额
		            		 }
		            	 }
		            	 if(jsonObject.containsKey("vehNum")) {
		            		 vehNum = jsonObject.getInt("vehNum"); // 资产总数
		            	 }
			             
			             tmpMap.put("custCode",custCode);//客户编号
			             tmpMap.put("vipCode",vipCode);//会员编号
			             tmpMap.put("channelId",channelId);//渠道id
			             tmpMap.put("realName",realName);//真实名称
			             tmpMap.put("picUrl",picUrl);//头像
			             tmpMap.put("cust_type",cust_type);//客户类型
			             tmpMap.put("gender",gender);//性别
			             tmpMap.put("wechat",wechat);//微信openid
			             tmpMap.put("sourceId",sourceId);//来源
			             tmpMap.put("sourceSubId",sourceSubId);//来源子渠道
			             tmpMap.put("postCode",postCode);//邮编
			             tmpMap.put("address",address);//地址
			             tmpMap.put("storeId",storeId);//门店（实体）
			             tmpMap.put("birthday",birthday);//生日
			             tmpMap.put("occuption",occuption);//职业
			             tmpMap.put("qq",qq);//QQ
			             tmpMap.put("blog",blog);//微博
			             tmpMap.put("passportType",passportType);//证件类型
			             tmpMap.put("passportNo",passportNo);//证件号
			             tmpMap.put("workplace",workplace);//工作单位地址
			             tmpMap.put("hobby",hobby);//爱好
			             tmpMap.put("income",income);//收入
			             tmpMap.put("province",province);//省份
			             tmpMap.put("city",city);//城市
			             tmpMap.put("area",area);//区域
			             tmpMap.put("createId",createId);//创建人id
			             tmpMap.put("modifyId",modifyId);//修改人id
			             tmpMap.put("modifyTime",modifyTime);//修改时间
			             tmpMap.put("status",status);//状态
			             tmpMap.put("createTime",createTime);//创建时间
			             tmpMap.put("isFollow",isFollow);//是否关注
			             tmpMap.put("accountMac",accountMac);//第三方会员编码
			             tmpMap.put("custType",custType);//客户类型
			             tmpMap.put("channelName",channelName);//客户来源名称
			             tmpMap.put("curGradeName",curGradeName);//会员级别
			             tmpMap.put("provinceName",provinceName);//省份名称
			             tmpMap.put("cityName",cityName);//城市名称
			             tmpMap.put("tradeAmt",tradeAmt);//交易金额
			             tmpMap.put("tradeNum",tradeNum);//交易次数
			             tmpMap.put("tradeAvg",tradeAvg);//客单价
			             tmpMap.put("lastTradeTime",lastTradeTime);//上次交易时间
			             tmpMap.put("levelId",levelId);//等级id
			             tmpMap.put("levelInvalid",levelInvalid);//等级失效日期
			             tmpMap.put("levelSort",levelSort);//等级序号
			             tmpMap.put("rightsNum",rightsNum);//会员权益数量
			             tmpMap.put("couponNum",couponNum);//优惠券数量
			             tmpMap.put("buyerQuantity",buyerQuantity);//剩余数量
			             tmpMap.put("channelRowId",channelRowId);//会员子表id
			             tmpMap.put("levelName",levelName);//等级名称
			             tmpMap.put("curGrade",curGrade);//等级名称
			             tmpMap.put("sourceName",sourceName);//来源名称
			             
			             tmpMap.put("curMoney", curMoney); // 钱包金额
			             tmpMap.put("vehNum", vehNum); // 资产总数
		                 
			             custInfoInfoList.add(tmpMap);
			        }
					custInfo.put("current", current);
					custInfo.put("pageSize", pageSize);
					custInfo.put("totalRecord", totalRecord);
					custInfo.put("totalPage", totalPage);
//					custInfo.put("pre", pre);
//					custInfo.put("next", next);
					custInfo.put("list", custInfoInfoList);
					return custInfo;
			    }
			 
	/**
	 * 解析返回数据
	 * 
	 * @param data
	 * @return
	 */
	public static Map<String, Object> analysisDataCrm04(String data) {
		JSONObject jsonObject = HttpUtil.convertToJSONObject(data);
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		// 解析项目
		String memberCode = ""; // 客户编号
		String realName = ""; // 真实名称
		String custType = ""; // 客户类型
		Integer gender = 0; // 性别
		String mobile = ""; // 手机号码
		String province = ""; // 省份
		String city = ""; // 城市
		String area = ""; // 区域
		String postCode = ""; // 邮编
		String address = ""; // 地址
		String store = ""; // 门店（实体）
		String mail = ""; // 电子邮箱
		String sourceName = ""; // 会员来源
		String subSourceName = ""; // 会员子来源
		String registerDate = ""; // 注册日期+
		String birthday = ""; // 生日
		String occuption = ""; // 职业
		String qq = ""; // QQ
		String blog = ""; // 微博
		String wechat = ""; // 微信openid
		String passportType = ""; // 证件类型
		String passportNo = ""; // 证件号
		String workplace = ""; // 工作单位地址
		String income = ""; // 收入
		String hobby = ""; // 爱好
		Integer status = 0; // 状态
		String channelId = ""; // 渠道id
		String subchannel = ""; // 子渠道
		String channelDescp = ""; // 渠道描述
		String memberIcon = ""; // 会员头像
		String memberNick = ""; // 渠道会员昵称
		String thirdCode = ""; // 第三方编码
		String encodeChar = ""; // 加密字符
		String channelMemberAssets = ""; // 资产信息列表
		String memberRightsAccounts = ""; // 权益信息列表
		String couponAccounts = ""; // 优惠券信息列表

		if (jsonObject.containsKey("memberCode")) {
			memberCode = jsonObject.getString("memberCode");// 客户编号
		}
		if (jsonObject.containsKey("realName")) {
			realName = jsonObject.getString("realName");// 真实名称
		}
		if (jsonObject.containsKey("custType")) {
			custType = jsonObject.getString("custType");// 客户类型
		}
		if (jsonObject.containsKey("gender")) {
			if (HttpUtil.checkJsonKey(jsonObject.get("gender"))) {
				gender = jsonObject.getInt("gender");// 性别
			}
		}
		if (jsonObject.containsKey("mobile")) {
			mobile = jsonObject.getString("mobile");// 手机号码
		}
		if (jsonObject.containsKey("province")) {
			province = jsonObject.getString("province");// 省份
		}
		if (jsonObject.containsKey("city")) {
			city = jsonObject.getString("city");// 城市
		}
		if (jsonObject.containsKey("area")) {
			area = jsonObject.getString("area");// 区域
		}
		if (jsonObject.containsKey("postCode")) {
			postCode = jsonObject.getString("postCode");// 邮编
		}
		if (jsonObject.containsKey("address")) {
			address = jsonObject.getString("address");// 地址
		}
		if (jsonObject.containsKey("store")) {
			store = jsonObject.getString("store");// 门店（实体）
		}
		if (jsonObject.containsKey("mail")) {
			mail = jsonObject.getString("mail");// 电子邮箱
		}
		if (jsonObject.containsKey("sourceName")) {
			sourceName = jsonObject.getString("sourceName");// 会员来源
		}
		if (jsonObject.containsKey("subSourceName")) {
			subSourceName = jsonObject.getString("subSourceName");// 会员子来源
		}
		if (jsonObject.containsKey("registerDate")) {
			registerDate = jsonObject.getString("registerDate");// 注册日期
		}
		if (jsonObject.containsKey("birthday")) {
			birthday = jsonObject.getString("birthday");// 生日
		}
		if (jsonObject.containsKey("occuption")) {
			occuption = jsonObject.getString("occuption");// 职业
		}
		if (jsonObject.containsKey("qq")) {
			qq = jsonObject.getString("qq");// QQ
		}
		if (jsonObject.containsKey("blog")) {
			blog = jsonObject.getString("blog");// 微博
		}
		if (jsonObject.containsKey("wechat")) {
			wechat = jsonObject.getString("wechat");// 微信openid
		}
		if (jsonObject.containsKey("passportType")) {
			passportType = jsonObject.getString("passportType");// 证件类型
		}
		if (jsonObject.containsKey("passportNo")) {
			passportNo = jsonObject.getString("passportNo");// 证件号
		}
		if (jsonObject.containsKey("workplace")) {
			workplace = jsonObject.getString("workplace");// 工作单位地址
		}
		if (jsonObject.containsKey("hobby")) {
			hobby = jsonObject.getString("hobby");// 爱好
		}
		if (jsonObject.containsKey("income")) {
			income = jsonObject.getString("income");// 收入
		}
		if (jsonObject.containsKey("status")) {
			if (HttpUtil.checkJsonKey(jsonObject.get("status"))) {
				status = jsonObject.getInt("status");// 状态
			}
		}
		if (jsonObject.containsKey("channelId")) {
			channelId = jsonObject.getString("channelId");// 渠道id
		}
		if (jsonObject.containsKey("subchannel")) {
			subchannel = jsonObject.getString("subchannel");// 子渠道
		}
		if (jsonObject.containsKey("channelDescp")) {
			channelDescp = jsonObject.getString("channelDescp");// 渠道描述
		}
		if (jsonObject.containsKey("memberIcon")) {
			memberIcon = jsonObject.getString("memberIcon");// 会员头像
		}
		if (jsonObject.containsKey("memberNick")) {
			memberNick = jsonObject.getString("memberNick");// 渠道会员昵称
		}
		if (jsonObject.containsKey("thirdCode")) {
			thirdCode = jsonObject.getString("thirdCode");// 第三方编码
		}
		if (jsonObject.containsKey("encodeChar")) {
			encodeChar = jsonObject.getString("encodeChar");// 加密字符
		}
		if (jsonObject.containsKey("channelMemberAssets")) {
			channelMemberAssets = jsonObject.getString("channelMemberAssets");// 资产信息列表
		}

		List<Map<String, Object>> memberAssetsList = FastList.newInstance();
		Map<String, Object> memberAssetsMap = new HashMap<String, Object>();
		JSONArray list = HttpUtil.convertToJSONArray(channelMemberAssets);
		for (int i = 0; i < list.size(); i++) {
			JSONObject memberAssets = list.getJSONObject(i);
			memberAssetsMap = new HashMap<String, Object>();
			String assetsType = ""; // 资产类别
			String brand = ""; // 品牌
			String series = ""; // 系列
			String spec = ""; // 规格型号
			String sn = ""; // SN
			String imei = ""; // IMEI
			String buyDate = ""; // 购买日期
			String warrantyDate = ""; // 保修起始日期
			String warrantyperiod = ""; // 保修年限

			if (memberAssets.containsKey("assetsType")) {
				assetsType = memberAssets.getString("assetsType"); // 资产类别
			}
			if (memberAssets.containsKey("brand")) {
				brand = memberAssets.getString("brand"); // 品牌
			}
			if (memberAssets.containsKey("series")) {
				series = memberAssets.getString("series"); // 系列
			}
			if (memberAssets.containsKey("spec")) {
				spec = memberAssets.getString("spec"); // 规格型号
			}
			if (memberAssets.containsKey("sn")) {
				sn = memberAssets.getString("sn"); // SN
			}
			if (memberAssets.containsKey("imei")) {
				imei = memberAssets.getString("imei"); // IMEI
			}
			if (memberAssets.containsKey("buyDate")) {
				buyDate = memberAssets.getString("buyDate"); // 购买日期
			}
			if (memberAssets.containsKey("warrantyDate")) {
				warrantyDate = memberAssets.getString("warrantyDate"); // 保修起始日期
			}
			if (memberAssets.containsKey("warrantyperiod")) {
				warrantyperiod = memberAssets.getString("warrantyperiod"); // 保修年限
			}

			memberAssetsMap.put("assetsType", assetsType); // 资产类别
			memberAssetsMap.put("brand", brand); // 品牌
			memberAssetsMap.put("series", series); // 系列
			memberAssetsMap.put("spec", spec); // 规格型号
			memberAssetsMap.put("sn", sn); // SN
			memberAssetsMap.put("imei", imei); // IMEI
			memberAssetsMap.put("buyDate", buyDate); // 购买日期
			memberAssetsMap.put("warrantyDate", warrantyDate); // 保修起始日期
			memberAssetsMap.put("warrantyperiod", warrantyperiod); // 保修年限

			memberAssetsList.add(memberAssetsMap);
		}

		if (jsonObject.containsKey("memberRightsAccounts")) {
			memberRightsAccounts = jsonObject.getString("memberRightsAccounts");// 权益信息列表
		}

		List<Map<String, Object>> memberRightsList = FastList.newInstance();
		Map<String, Object> memberRightsMap = FastMap.newInstance();
		list = HttpUtil.convertToJSONArray(memberRightsAccounts);
		for (int i = 0; i < list.size(); i++) {
			JSONObject memberRights = list.getJSONObject(i);
			memberRightsMap = new HashMap<String, Object>();

			String rightsCode = ""; // 权益编号
			String rightsName = ""; // 权益名称
			String startDate = ""; // 起始时间
			String endDate = ""; // 结束时间
			String rightsValue = ""; // 价值
			String useCount = ""; // 使用次数
			String isAstrict = ""; // 是否限制次数
			String canUseCount = ""; // 当前次数
			String period = ""; // 周期（天）
			String explain = ""; // 权益说明

			if (memberRights.containsKey("rightsCode")) {
				rightsCode = memberRights.getString("rightsCode"); // 权益编号
			}
			if (memberRights.containsKey("rightsName")) {
				rightsName = memberRights.getString("rightsName"); // 权益名称
			}
			if (memberRights.containsKey("startDate")) {
				startDate = memberRights.getString("startDate"); // 起始时间
			}
			if (memberRights.containsKey("endDate")) {
				endDate = memberRights.getString("endDate"); // 结束时间
			}
			if (memberRights.containsKey("rightsValue")) {
				rightsValue = memberRights.getString("rightsValue"); // 价值
			}
			if (memberRights.containsKey("useCount")) {
				useCount = memberRights.getString("useCount"); // 使用次数
			}
			if (memberRights.containsKey("isAstrict")) {
				isAstrict = memberRights.getString("isAstrict"); // 是否限制次数
			}
			if (memberRights.containsKey("canUseCount")) {
				canUseCount = memberRights.getString("canUseCount"); // 当前次数
			}
			if (memberRights.containsKey("period")) {
				period = memberRights.getString("period"); // 周期（天）
			}
			if (memberRights.containsKey("explain")) {
				explain = memberRights.getString("explain"); // 权益说明
			}

			memberRightsMap.put("rightsCode", rightsCode); // 权益编号
			memberRightsMap.put("rightsName", rightsName); // 权益名称
			memberRightsMap.put("startDate", startDate); // 起始时间
			memberRightsMap.put("endDate", endDate); // 结束时间
			memberRightsMap.put("rightsValue", rightsValue); // 价值
			memberRightsMap.put("useCount", useCount); // 使用次数
			memberRightsMap.put("isAstrict", isAstrict); // 是否限制次数
			memberRightsMap.put("canUseCount", canUseCount); // 当前次数
			memberRightsMap.put("period", period); // 周期（天）
			memberRightsMap.put("explain", explain); // 权益说明

			memberRightsList.add(memberRightsMap);
		}

		if (jsonObject.containsKey("couponAccounts")) {
			couponAccounts = jsonObject.getString("couponAccounts");// 优惠券信息列表
		}

		List<Map<String, Object>> couponAccountsList = FastList.newInstance();
		Map<String, Object> couponAccountsMap = FastMap.newInstance();
		list = HttpUtil.convertToJSONArray(couponAccounts);
		for (int i = 0; i < list.size(); i++) {
			JSONObject coupon = list.getJSONObject(i);
			couponAccountsMap = new HashMap<String, Object>();

			String couponNo = ""; // 优惠券编号
			String activityBath = ""; // 批次
			String couponType = ""; // 分类
			String couponName = ""; // 优惠券名称
			String channelIds = ""; // 适用渠道
			String productList = ""; // 适用单品
			String couponDiscount = ""; // 折扣比例
			String couponPar = ""; // 金额
			String activeDate = ""; // 起始时间
			String endDate = ""; // 截止时间

			if (coupon.containsKey("couponNo")) {
				couponNo = coupon.getString("couponNo"); // 优惠券编号
			}
			if (coupon.containsKey("activityBath")) {
				activityBath = coupon.getString("activityBath"); // 批次
			}
			if (coupon.containsKey("couponType")) {
				couponType = coupon.getString("couponType"); // 分类
			}
			if (coupon.containsKey("couponName")) {
				couponName = coupon.getString("couponName"); // 优惠券名称
			}
			if (coupon.containsKey("channelIds")) {
				channelIds = coupon.getString("channelIds"); // 适用渠道
			}
			if (coupon.containsKey("productList")) {
				productList = coupon.getString("productList"); // 适用单品
			}
			if (coupon.containsKey("couponDiscount")) {
				couponDiscount = coupon.getString("couponDiscount"); // 折扣比例
			}
			if (coupon.containsKey("couponPar")) {
				couponPar = coupon.getString("couponPar"); // 金额
			}
			if (coupon.containsKey("activeDate")) {
				activeDate = coupon.getString("activeDate"); // 起始时间
			}
			if (coupon.containsKey("endDate")) {
				endDate = coupon.getString("endDate"); // 截止时间
			}

			couponAccountsMap.put("couponNo", couponNo); // 优惠券编号
			couponAccountsMap.put("activityBath", activityBath); // 批次
			couponAccountsMap.put("couponType", couponType); // 分类
			couponAccountsMap.put("couponName", couponName); // 优惠券名称
			couponAccountsMap.put("channelIds", channelIds); // 适用渠道
			couponAccountsMap.put("productList", productList); // 适用单品
			couponAccountsMap.put("couponDiscount", couponDiscount); // 折扣比例
			couponAccountsMap.put("couponPar", couponPar); // 金额
			couponAccountsMap.put("activeDate", activeDate); // 起始时间
			couponAccountsMap.put("endDate", endDate); // 截止时间

			couponAccountsList.add(couponAccountsMap);
		}

		tmpMap.put("memberCode", memberCode);// 客户编号
		tmpMap.put("realName", realName);// 真实名称
		tmpMap.put("custType", custType);// 客户类型
		tmpMap.put("gender", gender);// 性别
		tmpMap.put("mobile", mobile);// 手机号码
		tmpMap.put("province", province);// 省份
		tmpMap.put("city", city);// 城市
		tmpMap.put("area", area);// 区域
		tmpMap.put("postCode", postCode);// 邮编
		tmpMap.put("address", address);// 地址
		tmpMap.put("store", store);// 门店（实体）
		tmpMap.put("mail", mail);// 电子邮箱
		tmpMap.put("sourceName", sourceName);// 会员来源
		tmpMap.put("subSourceName", subSourceName);// 会员子来源
		tmpMap.put("registerDate", registerDate);// 注册日期
		tmpMap.put("birthday", birthday);// 生日
		tmpMap.put("occuption", occuption);// 职业
		tmpMap.put("qq", qq);// QQ
		tmpMap.put("blog", blog);// 微博
		tmpMap.put("wechat", wechat);// 微信openid
		tmpMap.put("passportType", passportType);// 证件类型
		tmpMap.put("passportNo", passportNo);// 证件号
		tmpMap.put("workplace", workplace);// 工作单位地址
		tmpMap.put("hobby", hobby);// 爱好
		tmpMap.put("income", income);// 收入
		tmpMap.put("status", status);// 状态
		tmpMap.put("channelId", channelId);// 渠道id
		tmpMap.put("subchannel", subchannel);// 子渠道
		tmpMap.put("channelDescp", channelDescp);// 渠道描述
		tmpMap.put("memberIcon", memberIcon);// 会员头像
		tmpMap.put("memberNick", memberNick);// 渠道会员昵称
		tmpMap.put("thirdCode", thirdCode);// 第三方编码
		tmpMap.put("encodeChar", encodeChar);// 加密字符
		tmpMap.put("channelMemberAssets", memberAssetsList);// 资产信息列表
		tmpMap.put("memberRightsAccounts", memberRightsList);// 权益信息列表
		tmpMap.put("couponAccounts", couponAccountsList);// 优惠券信息列表

		return tmpMap;
	}
	
			/**
			 * 主json对象的解析
			 * @param jsonMain
			 */
			 public static Map<String,Object> analysisDataForMainJson(String jsonMain){
				 
				 Map<String,Object> mainInfo=new HashMap<String, Object>();
				 Map<String,Object> errJsonMap=new HashMap<String, Object>();
				 // 输出参数
		         String  status= null;         // 标识    
		         Integer code = null;          // 编码    
		         String  msg = null;           // 信息    
		         String  data =null;           // 结果集  
		         String  receivetime =null;    // 请求时间
		         String  backtime =null;       // 返回时间
		         String  syscode =null;        // 系统code
		         String  itfcode =null;        // 接口code
		         
		         String err=null;             // 中间件处理错误结果
		         //** 响应数据转换成json格式 *//
		         JSONObject outInfo = HttpUtil.convertToJSONObject(jsonMain);
		         // 输出参数
		         if(outInfo.containsKey("status")){
		        	 status = outInfo.getString("status");
		         }

		         if(outInfo.containsKey("code")){
		        	 if(HttpUtil.checkJsonKey(outInfo.get("code"))){
		        		 String codeReturn = outInfo.get("code").toString();
		        		 if(codeReturn.length() > 1) {
		        			 code = Integer.parseInt(codeReturn.substring(0, 1));
		        		 } else {
			        		 code=Integer.parseInt(outInfo.get("code").toString());
		        		 }
		        	 }
		         }
	             if(outInfo.containsKey("msg")){
	            	 msg=outInfo.getString("msg");
	             }
	             if(outInfo.containsKey("data")){
	            	 data=outInfo.getString("data");
	             }
	             if(outInfo.containsKey("receivetime")){
	            	 if(HttpUtil.checkJsonKey(outInfo.get("receivetime"))){
	            		 receivetime=outInfo.getString("receivetime");
	            	 }
	             }
	             if(outInfo.containsKey("backtime")){
	            	 if(HttpUtil.checkJsonKey(outInfo.get("backtime"))){
	            		 backtime=outInfo.getString("backtime");
	            	 }
	             }
//	             if(outInfo.containsKey("syscode")){
//	            	 syscode = outInfo.getString("syscode");
//	             }
//	             if(outInfo.containsKey("itfcode")){
//	            	 itfcode=outInfo.getString("itfcode");
//	             }
	             
	             
	             if(outInfo.containsKey("err")){
	            	 err=outInfo.getString("err");
	            	 if(UtilValidate.isNotEmpty(err)){
	            		 errJsonMap=analysisDataForMiddleErrJson(err);
	            	 }
	             }
	             
	             // 处理参数
	             if(UtilValidate.isNotEmpty(receivetime)){
	            	 mainInfo.put("receivetime", Timestamp.valueOf(receivetime));
	             }else{
	            	 mainInfo.put("receivetime", null);
	             }
	             
	             if(UtilValidate.isNotEmpty(backtime)){
	            	 mainInfo.put("backtime", Timestamp.valueOf(backtime));
	             }else{
	            	 mainInfo.put("backtime", null);
	             }
	             mainInfo.put("status", status);
	             mainInfo.put("code", code);
	             mainInfo.put("msg", msg);
	             mainInfo.put("data", data);
	             mainInfo.put("syscode", syscode);
	             mainInfo.put("itfcode", itfcode);
	             mainInfo.put("err", err);
	             mainInfo.put("errInfo",errJsonMap);
				 
				 return mainInfo;
			 }
			 
			 
			 
			/**
			 * 中间件错误信息的解析
			 * @param jsonMain
			 */
			 public static Map<String,Object> analysisDataForMiddleErrJson(String jsonErr){	
				 Map<String,Object> errJsonMap=new HashMap<String, Object>();
            	 JSONObject errInfo = HttpUtil.convertToJSONObject(jsonErr);
            	 Integer errorcode = null;         // 错误编码    
            	 Integer stauts = null;            // 错误状态    
            	 String errormsg = null;           // 错误信息    
            	 
            	 if(errInfo.containsKey("errorcode")){
		        	 if(HttpUtil.checkJsonKey(errInfo.get("errorcode"))){
		        		 errorcode=(Integer)errInfo.get("errorcode");
		        	 }
		         }
            	 
            	 if(errInfo.containsKey("stauts")){
		        	 if(HttpUtil.checkJsonKey(errInfo.get("stauts"))){
		        		 stauts=(Integer)errInfo.get("stauts");
		        	 }
		         }
            	 if(errInfo.containsKey("errormsg")){
            		 errormsg=errInfo.getString("errormsg");
	             }
            	 
            	 errJsonMap.put("errorcode", errorcode);
            	 errJsonMap.put("stauts", stauts);
            	 errJsonMap.put("errormsg", errormsg);
				 return errJsonMap;
			 }


}
