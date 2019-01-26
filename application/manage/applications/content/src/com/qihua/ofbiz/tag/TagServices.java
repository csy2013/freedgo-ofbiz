/*
 * 文件名：TagServices.java
 * 版权：启华
 * 描述：标签服务类
 * 修改人：gss
 * 修改时间：2015-12-23
 * 修改单号：
 * 修改内容：
 */

package com.qihua.ofbiz.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;



public class TagServices{
    public static final String module = TagServices.class.getName();
    public static final String resource = "ContentUiLabels";
	/**
	 * 修改标签分类名称 add by gss 2015/12/21
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateTagtypeName(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		//更新标签名称
		String tagTypeName = (String) context.get("tagTypeName");
		//更新标签ID
		String tagTypeId = (String) context.get("tagTypeId");
		//判断标签Id是否存在
		if (tagTypeId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "TagTypeNotFound", UtilMisc.toMap("tagTypeId", ""), locale));
	        }
		//定义实体类
		GenericValue tagType;
        try {
        	tagType = delegator.findByPrimaryKey("TagType", UtilMisc.toMap("tagTypeId", tagTypeId));
        } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
        //判断实体是否存在
        if (tagType != null) 
           {
        	String oldtagTypeName = tagType.getString("tagTypeName");
        	//判断标签名称是否修改
        	if(UtilValidate.isEmpty(oldtagTypeName))
        	  {
        		tagType.set("tagTypeName", tagTypeName);
        	  }else{
        		  if(!oldtagTypeName.equals(tagTypeName))
            	  {
            		tagType.set("tagTypeName", tagTypeName);
            	  }
        	  }
        	 try {
        		 tagType.store();
             } catch (GenericEntityException e) {
                 return ServiceUtil.returnError(e.getMessage());
             }
           }
        return ServiceUtil.returnSuccess();
	}
	/**
	 * 新增标签 add by gss 2015/12/25
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createTag(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Locale locale = (Locale) context.get("locale");
		 GenericValue Tag = delegator.makeValue("Tag");
         String tagId = delegator.getNextSeqId("Tag");
         String tagRemark = (String) context.get("tagRemark");
         String tagName = (String) context.get("tagName");
         String tagTypeId = (String) context.get("tagTypeId");
         String isDel = (String) context.get("isDel");
         //标签图片
         String contentId = (String) context.get("contentId");
         Tag.set("tagId", tagId);
         Tag.set("tagTypeId", tagTypeId);
         Tag.set("tagRemark", tagRemark);
         Tag.set("tagName", tagName);
         Tag.set("isDel", isDel);
 		 //判断是否上传了标签图片 
 		 if (contentId != null) 
 		 {
     		 Tag.set("contentId", contentId);
 		 }
         try {
			Tag.create();
			result.put("tagId",tagId);
			result.put("tagName",tagName);
		  } catch (GenericEntityException e) {
			e.printStackTrace();
		  }
		//return ServiceUtil.returnSuccess();
		return result;
	}
	
	/**
	 * 根据标签分类和名称查询 add by gss 2015-12-25
	 * @param dctx
	 * @param context
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 * @return
	 */
	public static Map<String, Object> findTagList(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		String tagTypeId = (String) context.get("tagTypeId");
		String tagName = (String) context.get("tagName");
		int tagListSize = 0;
		int lowIndex = 0;
		int highIndex = 0;
		// set the page parameters
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		if(UtilValidate.isNotEmpty(tagTypeId)&&!"-1".equals(tagTypeId))
		{
			try {
				GenericValue tagType=delegator.findByPrimaryKey("TagType", UtilMisc.toMap("tagTypeId",tagTypeId));
				result.put("tagTypeName", tagType.get("tagTypeName"));
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}
		result.put("viewIndex", Integer.valueOf(viewIndex));
		result.put("tagTypeId", tagTypeId);
		result.put("tagName", tagName);

		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		result.put("viewSize", Integer.valueOf(viewSize));
		// blank param list
        String paramList = "";
		
		List<GenericValue> tagList = null;
		DynamicViewEntity dve = new DynamicViewEntity();
		
		dve.addMemberEntity("TG", "Tag");
		dve.addMemberEntity("TY", "TagType");
		/*dve.addMemberEntity("CN", "Content");
		dve.addMemberEntity("DA", "DataResource");*/
		dve.addAliasAll("TG", "", null);
		// 标签ID
		dve.addAlias("TG", "tagId");
		// 标签名称
		dve.addAlias("TG", "tagName");
		// PC 端图片
		dve.addAlias("TG", "contentId");
		// 标签备注
		dve.addAlias("TG", "tagRemark");
		// 标签类型Id
		dve.addAlias("TG", "tagTypeId");
		// 标签分类名称
		dve.addAlias("TY", "tagTypeName");
		/*//图片ID
		dve.addAlias("DA", "dataResourceId");
		//图片地址
		dve.addAlias("DA", "objectInfo");*/
		dve.addViewLink("TG", "TY", Boolean.FALSE, ModelKeyMap.makeKeyMapList("tagTypeId","tagTypeId"));
		
		/*dve.addRelation("one", "", "Content", ModelKeyMap.makeKeyMapList("contentId"));
		dve.addViewLink("TG", "CN",Boolean.FALSE, ModelKeyMap.makeKeyMapList("contentId", "contentId"));
		dve.addViewLink("CN", "DA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("dataResourceId"));*/
		
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("tagId");
		fieldsToSelect.add("tagName");
		fieldsToSelect.add("contentId");
		//fieldsToSelect.add("objectInfo");
		fieldsToSelect.add("tagRemark");
		fieldsToSelect.add("tagTypeId");
		fieldsToSelect.add("tagTypeName");
		List<String> orderBy = FastList.newInstance();
		//orderBy.add("sequenceId");

		// define the main condition & expression list
		List<EntityCondition> andExprs = FastList.newInstance();
		EntityCondition mainCond = null;
		
		if (UtilValidate.isNotEmpty(tagName))
		{
			 paramList = paramList + "&tagName="+ tagName;
			andExprs.add(EntityCondition.makeCondition("tagName",
					EntityOperator.LIKE, "%" + tagName + "%"));
		}
		List<String> TypeIdList = new ArrayList<String>();
		if (UtilValidate.isNotEmpty(tagTypeId)&&!"-1".equals(tagTypeId))
		{
			//查询所有标签
			EntityConditionList<EntityExpr> Condition = EntityCondition.makeCondition(UtilMisc.toList(
	                EntityCondition.makeCondition("tagTypeId", EntityOperator.EQUALS, tagTypeId),
	                EntityCondition.makeCondition("parentTagTypeId", EntityOperator.EQUALS, tagTypeId)), EntityOperator.OR);
			List<GenericValue> typelists;
			try {
				typelists = delegator.findList("TagType", Condition, null, null, null, false);
				if (UtilValidate.isNotEmpty(typelists))
				{
					for (GenericValue typelist : typelists) {
						TypeIdList.add((String) typelist.get("tagTypeId"));
			    }
				   }
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			andExprs.add(EntityCondition.makeCondition("tagTypeId",
					EntityOperator.IN ,TypeIdList ));
			paramList = paramList + "&tagTypeId="+ tagTypeId;
		}
		andExprs.add(EntityCondition.makeCondition("isDel",
				EntityOperator.EQUALS ,"N"));
		   // build the main condition
        if (andExprs.size() > 0)
        {
        	mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
		try {
			lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
			// set distinct on so we only get one row per order
			EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
			// using list iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dve,
					mainCond, null, fieldsToSelect, orderBy, findOpts);
			tagList = pli.getPartialList(lowIndex, viewSize);
			// attempt to get the full size
			tagListSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > tagListSize)
			{
				highIndex = tagListSize;
			}
			// close the list iterator
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("tagList", tagList);
		result.put("tagListSize", Integer.valueOf(tagListSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("paramList", paramList);
		return result;
	}
	/**
	 * 删除标签   IsDel  == Y  add by gss 2016-1-7
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
    public static Map<String,Object> deleteTag(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        //删除标签Id
        String deleteId = (String)context.get("deleteId");
        try {
        //判断删除Id是否为空
        if(UtilValidate.isNotEmpty(deleteId))
          {
            String[] tagIds = deleteId.split(",");
                //查询标签
                List<GenericValue> tagList = delegator.findList("Tag", EntityCondition.makeCondition("tagId", EntityOperator.IN, Arrays.asList(tagIds)), null, null, null, false);
                if(UtilValidate.isNotEmpty(tagList))
                  { 
                	for (GenericValue tagLists :tagList) {
                		tagLists.set("isDel", "Y");
                		tagLists.store();
					}
                  }
         }
        }catch(GenericEntityException e){
            Debug.log(e.getMessage());
        }
        return result;
    }
	
	
	/**
	 * 根据ID查询标签add by gss 2016-1-7
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
    public static Map<String,Object> queryTag(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        //标签Id
        String tagId = (String)context.get("tagId");
        try {
        //判断删除Id是否为空
        if(UtilValidate.isNotEmpty(tagId))
          {
           //查询标签
           GenericValue tag = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", tagId));
           //判断标签是否为空
         if(UtilValidate.isNotEmpty(tag))
           {
        	 result.put("tag", tag);
        	 //获取图片内容ID
        	 String contentId = (String)tag.get("contentId");
        	 if(UtilValidate.isNotEmpty(contentId)){
        		//获取内容表数据
		     GenericValue Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", contentId)));
		     if(UtilValidate.isNotEmpty(Content))
		       {
		    	 GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId",Content.get("dataResourceId"))));
		        if(UtilValidate.isNotEmpty(dataResource))
		          {
		        	result.put("img", dataResource.get("objectInfo"));
		          }
		       }
        	 }else{
        		 result.put("img", "");
             }
        	 GenericValue tagType = delegator.findByPrimaryKey("TagType", UtilMisc.toMap("tagTypeId", tag.get("tagTypeId")));
            if(UtilValidate.isNotEmpty(tagType))
              {
            	result.put("treeName", tagType.get("tagTypeName"));
              }
           }
          }
        }catch(GenericEntityException e){
            Debug.log(e.getMessage());
        }
        return result;
    }
    
	/**
	 *更新标签信息 add by gss 2016-1-7
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateTag(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		//标签名称
		String tagName = (String) context.get("tagName");
		//标签ID
		String tagId = (String) context.get("tagId");
		//标签分类Id
		String tagTypeId = (String) context.get("tagTypeId");
		//标签备注
		String tagRemark = (String) context.get("tagRemark");
		//图片id
		String contentId = (String) context.get("contentId");
		//判断标签Id是否存在
		if (tagId == null) 
		{
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "TagNotFound", UtilMisc.toMap("tagTypeId", ""), locale));
	    }
		//定义实体类
		GenericValue tag;
        try {
        	tag = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", tagId));
        } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
        //判断实体是否存在
        if (tag != null) 
        {
            tag.set("tagTypeId", tagTypeId);
        	//标签名称是否修改
            tag.set("tagName", tagName);
        	//标签备注是否修改
        	tag.set("tagRemark", tagRemark);
        	//图片关联ID
        	tag.set("contentId", contentId);
        	 try {
        		 tag.store();
             } catch (GenericEntityException e) {
                 return ServiceUtil.returnError(e.getMessage());
             }
           }
        return ServiceUtil.returnSuccess();
	}
	
	 /**
     * 获取标签分类全部列表  add by gss 2016-1-7
     * @param dctx
     * @param context
     * @return
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, Object> getAllTagType(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //标签分类集合
        List<GenericValue> tagTypeList = FastList.newInstance();
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("T","TagType");
        dynamicView.addAlias("T","id","tagTypeId",null,false,false,null);
        dynamicView.addAlias("T","name","tagTypeName",null,false,false,null);
        dynamicView.addAlias("T","pId","parentTagTypeId",null,false,false,null);
        dynamicView.addAlias("T","tagTypeLevel","tagTypeLevel",null,false,false,null);
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        fieldsToSelect.add("id");
        fieldsToSelect.add("name");
        fieldsToSelect.add("pId");
        fieldsToSelect.add("tagTypeLevel");
        orderBy.add("pId");
        List NodeList = new ArrayList();
        try {
            //查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY,  true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, null, null, fieldsToSelect, orderBy, findOpts);
            tagTypeList = pli.getCompleteList();
            //关闭pli
            pli.close();
            Map map2=new HashMap();
            map2.put("id", "-1");
        	map2.put("name", "全部");
        	map2.put("pId", "0");
        	map2.put("open", true);
        	NodeList.add(map2);
            for (int i = 0; i < tagTypeList.size(); i++) {
            	Map map1=new HashMap();
            	if("Level1".equals(tagTypeList.get(i).get("tagTypeLevel")))
            	{
            		map1.put("id", tagTypeList.get(i).get("id"));
                	map1.put("name", tagTypeList.get(i).get("name"));
                	map1.put("pId", "-1");
            	}else{
            		map1.put("id", tagTypeList.get(i).get("id"));
                	map1.put("name", tagTypeList.get(i).get("name"));
                	map1.put("pId", tagTypeList.get(i).get("pId"));
            	}
            	NodeList.add(map1);
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, "Type " + e.toString(), module);
        }
		result.put("tagTypeList", NodeList);
        return result;
    }
    /**
     * 获取标签分类列表
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getTagTypeList(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
    	//标签分类集合
    	List<GenericValue> tagTypeList = FastList.newInstance();
    	//动态view
    	DynamicViewEntity dynamicView = new DynamicViewEntity();
    	dynamicView.addMemberEntity("T","TagType");
    	dynamicView.addAlias("T","id","tagTypeId",null,false,false,null);
    	dynamicView.addAlias("T","name","tagTypeName",null,false,false,null);
    	dynamicView.addAlias("T","pId","parentTagTypeId",null,false,false,null);
    	List<String> fieldsToSelect = FastList.newInstance();
    	List<String> orderBy = FastList.newInstance();
    	fieldsToSelect.add("id");
    	fieldsToSelect.add("name");
    	fieldsToSelect.add("pId");
    	orderBy.add("pId");
    	try {
    		//查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY,  true);
    		EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, null, null, fieldsToSelect, orderBy, findOpts);
    		tagTypeList = pli.getCompleteList();
    		//关闭pli
    		pli.close();
    		
    	} catch (GenericEntityException e) {
    		Debug.logError(e, "Type " + e.toString(), module);
    	}
    	result.put("tagTypeList", tagTypeList);
    	return result;
    }
    
}
