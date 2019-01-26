package com.qihua.ofbiz.party.common;

public class mockData {
	
	/**
	 * 获取商户菜单列表(全部菜单) crm31
	 * @return
	 */
	 public static String mocJsonDataCrm31(){
	    	String strJsonTest="";
	    	StringBuffer temp2=new StringBuffer();
	    	temp2.append("{");
	    	temp2.append("'status':'SUCCESS',");
	    	temp2.append("'code':1,");
	    	temp2.append("'msg':'操作成功',");
	    	temp2.append("'data':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'23',");
	    	temp2.append("'title':'客户洞察',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':'tj',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':2,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'5',");
	    	temp2.append("'title':'客户分析',");
	    	temp2.append("'url':'../cust/custAnalysisPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'23',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':1,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'28',");
	    	temp2.append("'title':'会员指标',");
	    	temp2.append("'url':'../cust/getCustomerIndexAnalysisReportInit.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'23',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':2,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'26',");
	    	temp2.append("'title':'商品分析',");
	    	temp2.append("'url':'../analysis/analysisTradePage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'23',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':3,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'24',");
	    	temp2.append("'title':'经营分析',");
	    	temp2.append("'url':'../analysis/analysisManagePage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'23',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':4,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("}");
	    	temp2.append("]");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'12',");
	    	temp2.append("'title':'营销活动',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':'wxzf',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':3,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'39',");
	    	temp2.append("'title':'客户分组',");
	    	temp2.append("'url':'../marketing/goToGroupActivity.do',");
	    	temp2.append("'icon':'',");
	    	temp2.append("'parentId':'12',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':1,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'14',");
	    	temp2.append("'title':'营销活动',");
	    	temp2.append("'url':'../marketing/goToActivityList.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'12',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':3,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("}");
	    	temp2.append("]");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'8',");
	    	temp2.append("'title':'忠诚度管理',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':'zc',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':5,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'9',");
	    	temp2.append("'title':'积分专区',");
	    	temp2.append("'url':'../integral/integralPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'8',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':1,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'10',");
	    	temp2.append("'title':'等级专区',");
	    	temp2.append("'url':'../level/levelPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'8',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':2,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'11',");
	    	temp2.append("'title':'特权专区',");
	    	temp2.append("'url':'../rights/rightPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'8',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':3,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("}");
	    	temp2.append("]");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'2',");
	    	temp2.append("'title':'订单中心',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':'order',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':6,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'41',");
	    	temp2.append("'title':'商品管理',");
	    	temp2.append("'url':'../product/opendProductPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'2',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':1,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'31',");
	    	temp2.append("'title':'订单监控',");
	    	temp2.append("'url':'../order/opendOrderMonitorPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'2',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':2,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'34',");
	    	temp2.append("'title':'订单记录',");
	    	temp2.append("'url':'../order/searchOrder.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'2',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':4,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("}");
	    	temp2.append("]");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'15',");
	    	temp2.append("'title':'储值卡应用',");
	    	temp2.append("'url':'../cardInfo/cardListPage.do',");
	    	temp2.append("'icon':'czk',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':7,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[]");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'3',");
	    	temp2.append("'title':'客户中心',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':'gn',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':8,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'4',");
	    	temp2.append("'title':'客户管理',");
	    	temp2.append("'url':'../cust/custListPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'3',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':1,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'40',");
	    	temp2.append("'title':'客户标签',");
	    	temp2.append("'url':'../marketing/goToTagActivity.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'3',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':2,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'6',");
	    	temp2.append("'title':'分组画像',");
	    	temp2.append("'url':'../cust/custPortraitPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'3',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':3,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'42',");
	    	temp2.append("'title':'客户合并',");
	    	temp2.append("'url':'../cust/memberMerger.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'3',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':4,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("}");
	    	temp2.append("]");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'29',");
	    	temp2.append("'title':'应用设置',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':'sz',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':11,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'30',");
	    	temp2.append("'title':'基础设置',");
	    	temp2.append("'url':'../apply/applyBasic.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'29',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':1,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'18',");
	    	temp2.append("'title':'微信设置',");
	    	temp2.append("'url':'../wechatSet/wechatSetPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'29',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':5,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'19',");
	    	temp2.append("'title':'流量充值',");
	    	temp2.append("'url':'../application/flowSetPage.do?msgType\u003d1',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'29',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':6,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("}");
	    	temp2.append("]");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'35',");
	    	temp2.append("'title':'开发（API）',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':'kf',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':12,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'36',");
	    	temp2.append("'title':'接口指南',");
	    	temp2.append("'url':'../itf/interfacePage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'35',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':1,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'37',");
	    	temp2.append("'title':'联调工具',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'35',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':2,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'38',");
	    	temp2.append("'title':'访问分析',");
	    	temp2.append("'url':'../analysis/logAnalyze.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'35',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':3,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("}");
	    	temp2.append("]");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'43',");
	    	temp2.append("'title':'内容管理',");
	    	temp2.append("'url':null,");
	    	temp2.append("'icon':'content',");
	    	temp2.append("'parentId':'0',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':13,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':[");
	    	temp2.append("{");
	    	temp2.append("'rowId':'44',");
	    	temp2.append("'title':'站点管理',");
	    	temp2.append("'url':'../site/gotoStieManagerPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'43',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':1,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'45',");
	    	temp2.append("'title':'图片管理',");
	    	temp2.append("'url':'../picing/gotoImageManager.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'43',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':2,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'46',");
	    	temp2.append("'title':'广告管理',");
	    	temp2.append("'url':'../adv/gotoAdvPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'43',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':3,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("},");
	    	temp2.append("{");
	    	temp2.append("'rowId':'47',");
	    	temp2.append("'title':'文章管理',");
	    	temp2.append("'url':'../article/gotoArticleOptPage.do',");
	    	temp2.append("'icon':null,");
	    	temp2.append("'parentId':'43',");
	    	temp2.append("'shortUrl':null,");
	    	temp2.append("'sort':4,");
	    	temp2.append("'status':0,");
	    	temp2.append("'createTime':null,");
	    	temp2.append("'subMenus':null");
	    	temp2.append("}");
	    	temp2.append("]");
	    	temp2.append("}");
	    	temp2.append("],");
	    	temp2.append("'requestId':'2017-01-23f1bd6ea9-e78b-408a-ae8f-030f7fb2a738',");
	    	temp2.append("'receivetime':'2017-01-23 13:44:33',");
	    	temp2.append("'backtime':'2017-01-23 13:44:33',");
	    	temp2.append("'syscode':null,");
	    	temp2.append("'itfcode':null");
//	    	temp2.append("'err':{");
//			temp2.append("'errorcode':1,");
//			temp2.append("'stauts':0,");
//			temp2.append("'errormsg':'入参不正确：缺少userid参数'");
//			temp2.append("}");
	    	temp2.append("}");
	    	
	    	strJsonTest=temp2.toString();
	  		return 	strJsonTest;					
	    											
	    											
	    }
	
	 /**
	  * 根据ID取得角色菜单列表_CRM34
	  * @return
	  */
	 public static String mocJsonDataCrm34(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功',");
		 temp.append("'data':[");
		 temp.append("{");
		 temp.append("'rowId':'2',");
		 temp.append("'title':'订单中心',");
		 temp.append("'url':null,");
		 temp.append("'icon':'order',");
		 temp.append("'parentId':'0',");
		 temp.append("'shortUrl':null,");
		 temp.append("'sort':6,");
		 temp.append("'status':0,");
		 temp.append("'createTime':null,");
		 temp.append("'subMenus':null");
		 temp.append("},");
		 temp.append("{");
		 temp.append("'rowId':'3',");
		 temp.append("'title':'客户中心',");
		 temp.append("'url':null,");
		 temp.append("'icon':'gn',");
		 temp.append("'parentId':'0',");
		 temp.append("'shortUrl':null,");
		 temp.append("'sort':8,");
		 temp.append("'status':0,");
		 temp.append("'createTime':null,");
		 temp.append("'subMenus':null");
		 temp.append("},");
		 temp.append("{");
		 temp.append("'rowId':'4',");
		 temp.append("'title':'客户管理',");
		 temp.append("'url':'../cust/custListPage.do',");
		 temp.append("'icon':null,");
		 temp.append("'parentId':'3',");
		 temp.append("'shortUrl':null,");
		 temp.append("'sort':1,");
		 temp.append("'status':0,");
		 temp.append("'createTime':null,");
		 temp.append("'subMenus':null");
		 temp.append("}");
		 temp.append("],");
		 temp.append("'requestId':'2017-01-23b2fd3f72-6ae4-47bc-8fff-9ca9df2e7d31',");
		 temp.append("'receivetime':'2017-01-23 16:02:21',");
		 temp.append("'backtime':'2017-01-23 16:02:22',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 /**
	  * 保存角色权限（菜单）_CRM3233
	  * @return
	  */
	 public static String mocJsonDataCrm3233(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功',");
		 temp.append("'data':null,");
		 temp.append("'requestId':'2017-01-230ea0803d-c3b7-4c1d-8bea-2c246d74d786',");
		 temp.append("'receivetime':'2017-01-23 15:56:15',");
		 temp.append("'backtime':'2017-01-23 15:56:15',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 /**
	  * 修改角色信息_CRM30
	  * @return
	  */
	 public static String mocJsonDataCrm30(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功',");
		 temp.append("'data':null,");
		 temp.append("'requestId':'2017-01-23d9d01ac8-102b-49b9-8d6c-ab93f144137d',");
		 temp.append("'receivetime':'2017-01-23 13:39:56',");
		 temp.append("'backtime':'2017-01-23 13:39:56',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 /**
	  * 新增角色信息_CRM29
	  * @return
	  */
	 public static String mocJsonDataCrm29(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功',");
		 temp.append("'data':null,");
		 temp.append("'requestId':'2017-01-23d9d01ac8-102b-49b9-8d6c-ab93f144137d',");
		 temp.append("'receivetime':'2017-01-23 13:39:56',");
		 temp.append("'backtime':'2017-01-23 13:39:56',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 
	 /**
	  * 修改管理用户信息_CRM28
	  * @return
	  */
	 public static String mocJsonDataCrm28(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功',");
		 temp.append("'data':null,");
		 temp.append("'requestId':'2017-01-23ca817385-6ba0-45cd-b8cd-d2ff9e0c36d5',");
		 temp.append("'receivetime':'2017-01-23 13:39:56',");
		 temp.append("'backtime':'2017-01-23 13:39:56',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 /**
	  * 新增管理用户信息_CRM27
	  * @return
	  */
	 public static String mocJsonDataCrm27(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功',");
		 temp.append("'data':null,");
		 temp.append("'requestId':'2017-01-23ca817385-6ba0-45cd-b8cd-d2ff9e0c36d5',");
		 temp.append("'receivetime':'2017-01-23 13:39:56',");
		 temp.append("'backtime':'2017-01-23 13:39:56',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 
	 
	 
	 
	 /**
	  * 优惠券消耗_CRM11
	  * @return
	  */
	 public static String mocJsonDataCrm11(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功',");
		 temp.append("'data':null,");
		 temp.append("'requestId':'2017-01-239b56b9f1-2b91-40a9-8ffa-bbb18c54974a',");
		 temp.append("'receivetime':'2017-01-23 15:07:23',");
		 temp.append("'backtime':'2017-01-23 15:07:23',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 
	 
	 /**
	  * 会员优惠券查询_CRM8
	  * @return
	  */
	 public static String mocJsonDataCrm8(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功！',");
		 temp.append("'data':{");
		 temp.append("'current':1,");
		 temp.append("'pageSize':10,");
		 temp.append("'totalRecord':39,");
		 temp.append("'totalPage':4,");
		 temp.append("'pre':null,");
		 temp.append("'next':null,");
		 temp.append("'list':[{");
		 temp.append("'rowId':'0243432a6ad211e697c200163e001b38',");
		 temp.append("'brandId':'1',");
		 temp.append("'couponId':'ac0eaa825fe841f9ad5b9f7af5aca252',");
		 temp.append("'couponCode':'24696090456490197',");
		 temp.append("'custId':'1',");
		 temp.append("'activityId':'1ffddcb9036d4f47a4b6e4c13814a58d',");
		 temp.append("'batch':'277231',");
		 temp.append("'startTime':'2016-08-2518:30:51',");
		 temp.append("'endTime':'2016-08-3118:30:51',");
		 temp.append("'status':0,");
		 temp.append("'createTime':'2016-08-2522:41:29',");
		 temp.append("'couponName':'2000元现金券',");
		 temp.append("'typeId':null,");
		 temp.append("'itemName':'现金劵'");
		 temp.append("}");
		 temp.append("],");
		 temp.append("'sql':null,");
		 temp.append("'start':0");
		 temp.append("},");
		 temp.append("'requestId':'2017-01-239b56b9f1-2b91-40a9-8ffa-bbb18c54974a',");
		 temp.append("'receivetime':'2017-01-23 15:07:23',");
		 temp.append("'backtime':'2017-01-23 15:07:23',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 								
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 
	 /**
	  * 会员权益查询_CRM6
	  * @return
	  */
	 public static String mocJsonDataCrm6(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功！',");
		 temp.append("'data':{");
		 temp.append("'current':1,");
		 temp.append("'pageSize':10,");
		 temp.append("'totalRecord':13,");
		 temp.append("'totalPage':2,");
		 temp.append("'pre':null,");
		 temp.append("'next':null,");
		 temp.append("'list':[{");
		 temp.append("'rowId':'1',");
		 temp.append("'brandId':'1',");
		 temp.append("'rightsId':'4bb9ec0399584fe2b29a76ae94f82be5',");
		 temp.append("'rightsCode':'125603141',");
		 temp.append("'rightsName':'权益测试1',");
		 temp.append("'startTime':'2016-07-13 13:45:48',");
		 temp.append("'endTime':'2016-07-13 13:45:50',");
		 temp.append("'rightsValue':null,");
		 temp.append("'astrict':null,");
		 temp.append("'period':null,");
		 temp.append("'explainDescp':'1',");
		 temp.append("'custId':'1',");
		 temp.append("'usedCount':null,");
		 temp.append("'rightsAmt':null,");
		 temp.append("'rightsDiscount':null,");
		 temp.append("'createTime':'2016-07-13 13:46:51',");
		 temp.append("'rightsPackageid':'1',");
		 temp.append("'useCount':11.0,");
		 temp.append("'packageName':'权益包1',");
		 temp.append("'status':0,");
		 temp.append("'rightsPolicyId':null");
		 temp.append("}");
		 temp.append("],");
		 temp.append("'sql':null,");
		 temp.append("'start':0");
		 temp.append("},");
		 temp.append("'requestId':'2017-01-23d43cb4b8-2c64-4653-9d1d-7ff66f7f7f9f',");
		 temp.append("'receivetime':'2017-01-23 14:49:55',");
		 temp.append("'backtime':'2017-01-23 14:49:55',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");		
		 strJson=temp.toString();
		 return strJson;
	 }

	 
	 
	 /**
	  * 会员信息查询（基本信息）_CRM3
	  * @return
	  */
	 public static String mocJsonDataCrm3(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功！',");
		 temp.append("'data':{");
		 temp.append("'current':1,");
		 temp.append("'pageSize':10,");
		 temp.append("'totalRecord':33,");
		 temp.append("'totalPage':4,");
		 temp.append("'pre':null,");
		 temp.append("'next':null,");
		 temp.append("'list':[{");
		 temp.append("'rowId':'1',");
		 temp.append("'brandId':'1',");
		 temp.append("'nick':'江湖bai晓生',");
		 temp.append("'mobile':'13951796153',");
		 temp.append("'mail':'wozhx123@163.com',");
		 temp.append("'custCode':'10001',");
		 temp.append("'vipCode':null,");
		 temp.append("'channelId':null,");
		 temp.append("'realName':'张祥总',");
		 temp.append("'picUrl':null,");
		 temp.append("'cust_type':null,");
		 temp.append("'gender':1,");
		 temp.append("'wechat':'okKutswSx2zAoVfiCvFXcAwRt45M',");
		 temp.append("'sourceId':'1',");
		 temp.append("'sourceSubId':'0101',");
		 temp.append("'postCode':'225200',");
		 temp.append("'address':'江苏南京',");
		 temp.append("'storeId':'1',");
		 temp.append("'birthday':'1987-02-25 00:00:00',");
		 temp.append("'occuption':'产品经理',");
		 temp.append("'qq':'12339498',");
		 temp.append("'blog':'wozhx123@sina.com',");
		 temp.append("'passportType':'1',");
		 temp.append("'passportNo':'3210.......',");
		 temp.append("'workplace':'江苏南京浦口',");
		 temp.append("'hobby':'羽毛球、乒乓球',");
		 temp.append("'income':'23',");
		 temp.append("'province':'110000',");
		 temp.append("'city':'110107',");
		 temp.append("'area':'',");
		 temp.append("'mergeMemberIds':null,");
		 temp.append("'createId':'2016-05-29',");
		 temp.append("'modifyId':null,");
		 temp.append("'modifyTime':null,");
		 temp.append("'status':1,");
		 temp.append("'createTime':'2016-06-16 00:00:00',");
		 temp.append("'isFollow':1,");
		 temp.append("'custType':'1',");
		 temp.append("'channelName':null,");
		 temp.append("'curGradeName':null,");
		 temp.append("'provinceName':null,");
		 temp.append("'cityName':null,");
		 temp.append("'tradeAmt':null,");
		 temp.append("'tradeNum':null,");
		 temp.append("'tradeAvg':null,");
		 temp.append("'lastTradeTime':null,");
		 temp.append("'levelId':'3',");
		 temp.append("'levelInvalid':'2016-12-30 00:00:00',");
		 temp.append("'levelSort':null,");
		 temp.append("'rightsNum':null,");
		 temp.append("'couponNum':null,");
		 temp.append("'buyerQuantity':null,");
		 temp.append("'channelRowId':null,");
		 temp.append("'levelName':'银牌会员',");
		 temp.append("'sourceName':'淘宝'");
		 temp.append("}");
		 temp.append("],");
		 temp.append("'sql':null,");
		 temp.append("'start':0");
		 temp.append("},");
		 temp.append("'requestId':'2017-01-23872bd3ed-28f7-4770-ab25-f75b8ef69b60',");
		 temp.append("'receivetime':'2017-01-23 09:50:16',");
		 temp.append("'backtime':'2017-01-23 09:50:16',");
		 temp.append("'syscode':null,");
		 temp.append("'itfcode':null");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 
	 /**
	  * 会员信息修改（基本信息）_CRM2
	  * @return
	  */
	 public static String mocJsonDataCrm2(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功！',");
		 temp.append("'requestId':'2016-05-039ded1a05-6a9b-4f0f-8800-6bf4bffa9cf4',");
		 temp.append("'receivetime':'2016-05-03 09:10:40',");
		 temp.append("'backtime':'2016-05-03 09:10:40',");
		 temp.append("'syscode':'abc',");
		 temp.append("'itfcode':'def'");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 strJson=temp.toString();
		 return strJson;
	 }
	 
	 /**
	  * 会员信息新增（基本信息）_CRM1
	  * @return
	  */
	 public static String mocJsonDataCrm1(){
		 String strJson="";
		 StringBuffer temp=new StringBuffer();
		 temp.append("{");
		 temp.append("'status':'SUCCESS',");
		 temp.append("'code':1,");
		 temp.append("'msg':'操作成功！',");
		 temp.append("'requestId':'2016-05-039ded1a05-6a9b-4f0f-8800-6bf4bffa9cf4',");
		 temp.append("'receivetime':'2016-05-03 09:10:40',");
		 temp.append("'backtime':'2016-05-03 09:10:40',");
		 temp.append("'syscode':'abc',");
		 temp.append("'itfcode':'def'");
//		 temp.append("'err':{");
//		 temp.append("'errorcode':1,");
//		 temp.append("'stauts':0,");
//		 temp.append("'errormsg':'入参不正确：缺少userid参数'");
//		 temp.append("}");
		 temp.append("}");
		 strJson=temp.toString();
		 return strJson;
	 }

}
