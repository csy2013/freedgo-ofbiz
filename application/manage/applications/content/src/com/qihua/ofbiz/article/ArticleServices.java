/*
 * 文件名：ArticleServices.java
 * 版权：启华
 * 描述：文章服务类
 * 修改人：gss
 * 修改时间：2015-12-28
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.article;

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
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class ArticleServices {
	public static final String module = ArticleServices.class.getName();
	public static final String resource = "ContentUiLabels";

	/**
	 * 新增文章分类 add by gss 2015/12/28
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createArticleType(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Locale locale = (Locale) context.get("locale");
		GenericValue ArticleType = delegator.makeValue("ArticleType");
		// 文章分类ID
		String articleTypeId = delegator.getNextSeqId("ArticleType");
		// 文章分类parentID
		String parentTypeId = (String) context.get("parentTypeId");
		// 文章分类名称
		String description = (String) context.get("description");
		// 文章分类等级
		String type_Level = (String) context.get("typeLevel");
		String typeLevel = null;
		if (type_Level == null) {
			typeLevel = "Level1";
		}
		if (type_Level != null && "Level1".equals(type_Level)) {
			typeLevel = "Level2";
		}
		if (type_Level != null && "Level2".equals(type_Level)) {
			typeLevel = "Level3";
		}
		ArticleType.set("articleTypeId", articleTypeId);
		ArticleType.set("parentTypeId", parentTypeId);
		ArticleType.set("typeLevel", typeLevel);
		ArticleType.set("description", description);
		try {
			ArticleType.create();
			result.put("success", true);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 修改文章分类名称 add by gss 2015/12/28
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateArticleType(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		// 更新文章名称
		String description = (String) context.get("description");
		// 更新文章ID
		String articleTypeId = (String) context.get("articleTypeId");
		// 判断标签Id是否存在
		if (articleTypeId == null) {
			return ServiceUtil.returnError(UtilProperties.getMessage(resource,
					"ArticleTypeNotFound", UtilMisc.toMap("articleTypeId", ""),
					locale));
		}
		// 定义实体类
		GenericValue articleType;
		try {
			articleType = delegator.findByPrimaryKey("ArticleType",
					UtilMisc.toMap("articleTypeId", articleTypeId));
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		// 判断实体是否存在
		if (articleType != null) {
			String oldTypeName = articleType.getString("description");
			if (!oldTypeName.equals(description)) {
				articleType.set("description", description);
			}
			try {
				articleType.store();
			} catch (GenericEntityException e) {
				return ServiceUtil.returnError(e.getMessage());
			}
		}
		return ServiceUtil.returnSuccess();
	}

	/**
	 * 删除文章分类
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> delArticleType(DispatchContext dctx,
			Map<String, ? extends Object> context)
			throws GenericServiceException, GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Locale locale = (Locale) context.get("locale");
		String deleteId = (String) context.get("deleteId");
		List<String> TypeIdList = new ArrayList<String>();
		TypeIdList.add(deleteId);
		try {
			if (UtilValidate.isNotEmpty(deleteId)) {
				// 查询所有标签
				EntityConditionList<EntityExpr> Condition = EntityCondition
						.makeCondition(UtilMisc.toList(EntityCondition
								.makeCondition("articleTypeId",
										EntityOperator.EQUALS, deleteId),
								EntityCondition.makeCondition("parentTypeId",
										EntityOperator.EQUALS, deleteId)),
								EntityOperator.OR);
				List<GenericValue> typelists;
				try {
					typelists = delegator.findList("ArticleType", Condition,
							null, null, null, false);
					if (UtilValidate.isNotEmpty(typelists)) {
						for (GenericValue typelist : typelists) {
							TypeIdList.add((String) typelist
									.get("articleTypeId"));
						}
					}
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
				List<GenericValue> articleTypeList = delegator.findList(
						"ArticleType",
						EntityCondition.makeCondition("articleTypeId",
								EntityOperator.IN, TypeIdList), null, null,
						null, false);
				delegator.removeAll(articleTypeList);
			}
		} catch (GenericEntityException e) {
			Debug.log(e.getMessage());
			result.put("status", true);
		}
		result.put("status", true);
		return result;
	}

	/**
	 * 根据文章分类ID获取分类下文章信息
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getArticleTypeById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 获取id参数
		String deleteId = (String) context.get("deleteId");
		// 分类下所有分类Id 集合
		List<String> TypeIdList = new ArrayList<String>();
		TypeIdList.add(deleteId);
		if (UtilValidate.isNotEmpty(deleteId)) {
			// 查询所有标签
			EntityConditionList<EntityExpr> Condition = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition
							.makeCondition("articleTypeId",
									EntityOperator.EQUALS, deleteId),
							EntityCondition.makeCondition("parentTypeId",
									EntityOperator.EQUALS, deleteId)),
							EntityOperator.OR);
			List<GenericValue> typelists;
			try {
				typelists = delegator.findList("ArticleType", Condition, null,
						null, null, false);
				if (UtilValidate.isNotEmpty(typelists)) {
					for (GenericValue typelist : typelists) {
						TypeIdList.add((String) typelist.get("articleTypeId"));
					}
				}
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			List<GenericValue> Thisarticle;
			try {
				// 查询此文章分类下是否有文章
				Thisarticle = delegator.findByAnd("Article",
						UtilMisc.toMap("articleTypeId", deleteId));
				result.put("thisarticle", Thisarticle.size());
				// 查询此文章分类及其下级分类中是否有文章
				List<GenericValue> article = delegator.findList("Article",
						EntityCondition.makeCondition("articleTypeId",
								EntityOperator.IN, TypeIdList), null, null,
						null, false);
				result.put("article", article.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 新增文章分类 add by gss 2015/12/28
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createArticle(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		GenericValue Article = delegator.makeValue("Article");
		
		// 文章ID
		String articleId = delegator.getNextSeqId("Article");
		// 文章标题
		String articleTitle = (String) context.get("articleTitle");
		// 文章标签
		String tag = (String) context.get("tag");
		// 原文链接
		String articleLink = (String) context.get("articleLink");
		// 作者
		String articleAuthor = (String) context.get("articleAuthor");
		// 文章分类Id
		String articleTypeId = (String) context.get("articleTypeId");
		// 文章状态 0已保存 ,1待审核,2已审核,3已拒绝
		String articleStatus = (String) context.get("articleStatus");
		// 文章内容
		String articleCentent = (String) context.get("articleCentent");
		// 店铺
		String productStoreId=(String) context.get("productStoreId");
		// 文章配图
		String contentId = (String) context.get("contentId");
		// 关联商品
		String productIds=(String) context.get("productIds");

		Article.set("articleId", articleId);
		Article.set("articleTitle", articleTitle);
		//Article.set("tagId", tagId);
		Article.set("articleLink", articleLink);
		Article.set("articleAuthor", articleAuthor);
		Article.set("articleTypeId", articleTypeId);
		Article.set("articleStatus", articleStatus);
		Article.set("productStoreId", productStoreId);
		try {
			//添加文章
			Article.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String[] tags = null;
		if (UtilValidate.isNotEmpty(tag)) {
			tags = tag.split(",");// 生成商品标签数组
		}
		// 商品标签关系表数据的登陆
		if (UtilValidate.isNotEmpty(tags)) {
			for (int i = 0; i < tags.length; i++) {
				GenericValue articleTagAssoc = null;
				String articleTagAssocId = delegator
						.getNextSeqId("ArticleTagAssoc");
				articleTagAssoc = delegator.makeValue(
						"ArticleTagAssoc", UtilMisc.toMap(
								"articleTagAssocId",
								articleTagAssocId));
				// 商品ID
				articleTagAssoc.set("articleId", articleId);
				// 标签ID
				articleTagAssoc.set("tagId", tags[i]);
				// 创建表
				try {
					articleTagAssoc.create();
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
		}


		// 文章和商品关系数据的登陆
		if (UtilValidate.isNotEmpty(productIds)) {
			String[] productIdInfos = null;
			productIdInfos = productIds.split(",");// 选择商品数组

			for (int i = 0; i < productIdInfos.length; i++) {
				GenericValue articleProductAssoc = null;
				String articleProductAssocId = delegator.getNextSeqId("ArticleProductAssoc");
				articleProductAssoc = delegator.makeValue("ArticleProductAssoc",
						UtilMisc.toMap("articleProductAssocId",articleProductAssocId));
				// 文章ID
				articleProductAssoc.set("articleId", articleId);
				// 商品ID
				articleProductAssoc.set("productId", productIdInfos[i]);
				// 创建表
				try {
					articleProductAssoc.create();
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
		}


		// 文章关键字
		String articleKeyword = (String) context.get("articleKeyword");
		if (UtilValidate.isNotEmpty(articleKeyword)) {
			String regex = ",|，";
            String[] keyword = articleKeyword.split(regex);
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < keyword.length; i++) {
				if (!list.contains(keyword[i])) // 如果list数组不包括keyword[i]中的值的话，就返回true
				{
					list.add(keyword[i]); // 在list数组中加入keyword[i]的值。已经过滤过。
				}
			}
			for (int i = 0; i < list.size(); i++) {
				GenericValue ArticleKeyword = delegator
						.makeValue("ArticleKeyword");
				ArticleKeyword.set("articleId", articleId);
				ArticleKeyword.set("keyword", list.get(i));
				try {
					ArticleKeyword.create();
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
		}
		// 判断是否上传了文章配图
		if (contentId != null) {
			GenericValue ArticleContent = delegator.makeValue("ArticleContent");
			ArticleContent.set("articleId", articleId);
			ArticleContent.set("contentId", contentId);
			ArticleContent.set("articleContentTypeId", "ARTICLE_FIGURE");
			try {
				ArticleContent.create();
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}
		Map<String, Object> passedParams1 = FastMap.newInstance();
		String CcontentId = "";
		if (UtilValidate.isNotEmpty(articleCentent)) {
			passedParams1 = UtilMisc
					.toMap("dataResourceTypeId", "ELECTRONIC_TEXT",
							"dataTemplateTypeId", "NONE",
							"contentPurposeTypeId", "ARTICLE", "textData",
							articleCentent, "statusId", "CTNT_INITIAL_DRAFT",
							"userLogin", userLogin, "contentAssocTypeId",
							"SUB_CONTENT");
			try {
				result = dispatcher.runSync("createTextContent", passedParams1);
				if (UtilValidate.isNotEmpty(result)) {
					CcontentId = (String) result.get("contentId");
					GenericValue ArticleContent = delegator
							.makeValue("ArticleContent");
					ArticleContent.set("articleId", articleId);
					ArticleContent.set("contentId", CcontentId);
					ArticleContent.set("articleContentTypeId",
							"ARTICLE_CONTENT");
					try {
						ArticleContent.create();
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}
				}
			} catch (GenericServiceException e) {
				e.printStackTrace();
			}
		}
		return ServiceUtil.returnSuccess();
	}

	/**
	 * 文章查询 add by gss 2015/12/21
	 * 
	 * @param dctx
	 * @param context
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 * @return
	 */
	public static Map<String, Object> findArticle(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		// 文章标题
		String articleTitle = (String) context.get("articleTitle");
		// 文章类型
		String articleTypeId = (String) context.get("articleTypeId");
		// 文章状态
		String articleStatus = (String) context.get("articleStatus");
		String productStoreId = (String) context.get("productStoreId"); // 店铺编码
		// 查询文章分类的名称
		try {
			GenericValue articleType = delegator.findByPrimaryKey(
					"ArticleType",
					UtilMisc.toMap("articleTypeId", articleTypeId));
			if (UtilValidate.isNotEmpty(articleType)) {
				result.put("description", articleType.get("description"));
			}
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		int articleListSize = 0;
		int lowIndex = 0;
		int highIndex = 0;
		// set the page parameters
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		result.put("viewIndex", Integer.valueOf(viewIndex));
		result.put("articleTitle", articleTitle);
		result.put("articleTypeId", articleTypeId);
		result.put("articleStatus", articleStatus);

		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		result.put("viewSize", Integer.valueOf(viewSize));
		// blank param list
		String paramList = "";

		List<GenericValue> articleList = null;
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("AC", "Article");
		dve.addMemberEntity("AT", "ArticleType");
		dve.addAliasAll("AC", "", null);
		// 文章编号
		dve.addAlias("AC", "articleId");
		// 文章标题
		dve.addAlias("AC", "articleTitle");
		// 文章作者
		dve.addAlias("AC", "articleAuthor");
		// 文章类型
		dve.addAlias("AC", "articleTypeId");
		// 文章状态
		dve.addAlias("AC", "articleStatus");
		// 创建时间
		dve.addAlias("AC", "createdStamp");
		// 文章类型ID
		dve.addAlias("AT", "articleTypeId");
		// 类型描述
		dve.addAlias("AT", "description");
		// 店铺信息
		dve.addAlias("AC", "productStoreId");
		dve.addViewLink("AC", "AT", Boolean.FALSE,
				ModelKeyMap.makeKeyMapList("articleTypeId", "articleTypeId"));
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("articleId");
		fieldsToSelect.add("articleTitle");
		fieldsToSelect.add("articleAuthor");
		fieldsToSelect.add("articleTypeId");
		fieldsToSelect.add("articleStatus");
		fieldsToSelect.add("createdStamp");
		fieldsToSelect.add("description");
		fieldsToSelect.add("productStoreId");
		List<String> orderBy = FastList.newInstance();
		orderBy.add("createdStamp");

		// define the main condition & expression list
		List<EntityCondition> andExprs = FastList.newInstance();
		EntityCondition mainCond = null;

		if (UtilValidate.isNotEmpty(articleTitle)) {
			paramList = paramList + "&articleTitle=" + articleTitle;
			andExprs.add(EntityCondition.makeCondition("articleTitle",
					EntityOperator.LIKE, "%" + articleTitle + "%"));
		}
		if (UtilValidate.isNotEmpty(articleStatus)
				&& !"-1".equals(articleStatus)) {
			paramList = paramList + "&articleStatus=" + articleStatus;
			andExprs.add(EntityCondition.makeCondition("articleStatus",
					EntityOperator.EQUALS, articleStatus));
		}
		if (UtilValidate.isNotEmpty(articleTypeId)
				&& !"-1".equals(articleTypeId)) {
			paramList = paramList + "&articleTypeId=" + articleTypeId;
			andExprs.add(EntityCondition.makeCondition("articleTypeId",
					EntityOperator.EQUALS, articleTypeId));
		}

		// 店铺信息
		if(UtilValidate.isNotEmpty(productStoreId)){
			andExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
		}
		// build the main condition
		if (andExprs.size() > 0) {
			mainCond = EntityCondition.makeCondition(andExprs,
					EntityOperator.AND);
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
			articleList = pli.getPartialList(lowIndex, viewSize);
			// attempt to get the full size
			articleListSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > articleListSize) {
				highIndex = articleListSize;
			}
			// close the list iterator
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("articleList", articleList);
		result.put("articleListSize", Integer.valueOf(articleListSize));
		result.put("totalSize", Integer.valueOf(articleListSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("paramList", paramList);
		return result;
	}

	/**
	 * 删除文章 add by gss 2016-1-4
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> deleteArticle(DispatchContext dctx,
			Map<String, ? extends Object> context)
			throws GenericServiceException, GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Locale locale = (Locale) context.get("locale");
		// 删除文章Id
		String deleteId = (String) context.get("deleteId");
		try {
			// 判断删除Id是否为空
			if (UtilValidate.isNotEmpty(deleteId)) {
				String[] ArticleIds = deleteId.split(",");
				// 查询文章关联内容表
				List<GenericValue> articleContentList = delegator.findList(
						"ArticleContent", EntityCondition.makeCondition(
								"articleId", EntityOperator.IN,
								Arrays.asList(ArticleIds)), null, null, null,
						false);
				if (UtilValidate.isNotEmpty(articleContentList)) {
					// 查询文章关联内容表
					delegator.removeAll(articleContentList);
				}
				// 查询文章关键字
				List<GenericValue> articlekeyWord = delegator.findList(
						"ArticleKeyword", EntityCondition.makeCondition(
								"articleId", EntityOperator.IN,
								Arrays.asList(ArticleIds)), null, null, null,
						false);
				if (UtilValidate.isNotEmpty(articlekeyWord)) {
					// 删除文章关键字
					delegator.removeAll(articlekeyWord);
				}
				// 查询文章
				List<GenericValue> articleList = delegator.findList("Article",
						EntityCondition.makeCondition("articleId",
								EntityOperator.IN, Arrays.asList(ArticleIds)),
						null, null, null, false);
				if (UtilValidate.isNotEmpty(articleList)) {
					// 删除文章
					delegator.removeAll(articleList);
				}
			}
		} catch (GenericEntityException e) {
			Debug.log(e.getMessage());
			result.put("status", true);
		}
		result.put("status", true);
		return result;
	}

	/**
	 * 根据ID查询文章 add by gss 2016-1-4
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findArticleById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		// 所选文章ID
		String articleIds = (String) context.get("article_ids");
		// 判断文章ID 是否存在
		if (UtilValidate.isNotEmpty(articleIds)) {
			String[] articleId = articleIds.split(",");
			try {
				// 查询所以的文章
				List<GenericValue> ArticleList = delegator.findList("Article",
						EntityCondition.makeCondition("articleId",
								EntityOperator.IN, Arrays.asList(articleId)),
						null, null, null, false);
				if (UtilValidate.isNotEmpty(ArticleList)) {
					result.put("articleList", ArticleList);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 根据ID获取文章详情内容 add by gss 2016-1-4
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getArticleById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		// 文章ID
		String articleId = (String) context.get("articleId");
		if (UtilValidate.isNotEmpty(articleId)) {
			try {
				// 文章内容
				GenericValue article = delegator.findByPrimaryKey("Article",
						UtilMisc.toMap("articleId", articleId));
				List<String> typeIds = new ArrayList();
				if (UtilValidate.isNotEmpty(article)) {
					GenericValue articleType = delegator.findByPrimaryKey(
							"ArticleType",
							UtilMisc.toMap("articleTypeId",
									article.get("articleTypeId")));
					if (UtilValidate.isNotEmpty(articleType)) {
						result.put("articleTypeName",
								articleType.get("description"));
					}
					result.put("article", article);
					
					List<GenericValue> ArticleTagAssocList = delegator
					.findByAnd("ArticleTagAssoc", UtilMisc
							.toMap("articleId", articleId));
					//String tag_typeId = (String) article.get("tagId");
					/*if (UtilValidate.isNotEmpty(tag_typeId)) {
						boolean contains = tag_typeId.contains("{");
						if (contains) {
							String tag_typeIds = tag_typeId.substring(1,
									tag_typeId.length() - 1);
							String[] tagId = tag_typeIds.split(",");
							for (int i = 0; i < tagId.length; i++) {
								typeIds.add(tagId[i].trim());
							}
						} else {
							typeIds.add(tag_typeId);
						}
					}*/
					for (GenericValue articleTagAssocList : ArticleTagAssocList) {
						typeIds.add(articleTagAssocList.getString("tagId"));
					}
					result.put("typeIds", typeIds);
				}
				// 文章关键字
				List<GenericValue> articleKeyword = delegator.findByAnd(
						"ArticleKeyword",
						UtilMisc.toMap("articleId", articleId));
				String words = "";
				for (int i = 0; i < articleKeyword.size(); i++) {

					if (i == articleKeyword.size() - 1) {
						words += articleKeyword.get(i).get("keyword");
					} else {
						words += articleKeyword.get(i).get("keyword") + "，";
					}
				}
				if (UtilValidate.isNotEmpty(words)) {
					result.put("articleKeyword", words);
				}

				List<GenericValue> articleContentImg = delegator.findByAnd(
						"ArticleContent", UtilMisc.toMap("articleId",
								articleId, "articleContentTypeId",
								"ARTICLE_FIGURE"));
				GenericValue articleimg = EntityUtil
						.getFirst(articleContentImg);
				result.put("articleContentImg", articleimg);
				// 查询文章内容
				GenericValue articleConContent = EntityUtil.getFirst(delegator
						.findByAnd("ArticleContent", UtilMisc.toMap(
								"articleId", articleId, "articleContentTypeId",
								"ARTICLE_CONTENT")));
				if (UtilValidate.isNotEmpty(articleConContent)) {
					GenericValue Content = delegator.findByPrimaryKey(
							"Content",
							UtilMisc.toMap("contentId",
									articleConContent.get("contentId")));
					if (UtilValidate.isNotEmpty(Content)) {
						GenericValue Contenttext = delegator.findByPrimaryKey(
								"ElectronicText",
								UtilMisc.toMap("dataResourceId",
										Content.get("dataResourceId")));
						if (UtilValidate.isNotEmpty(Contenttext)) {
							result.put("articleContents", Contenttext);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 文章审核 add by gss 2016-1-4
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> approvalArticle(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Locale locale = (Locale) context.get("locale");
		// 文章ID
		String articleId = (String) context.get("articleId");
		// 审核意见
		String articleOpinion = (String) context.get("articleOpinion");
		// 文章状态
		String articleStatus = (String) context.get("articleStatus");
		GenericValue Article;
		try {
			Article = delegator.findByPrimaryKey("Article",
					UtilMisc.toMap("articleId", articleId));
			if (UtilValidate.isNotEmpty(Article)) {
				Article.set("articleOpinion", articleOpinion);
				Article.set("articleStatus", articleStatus);
				Article.store();
			}
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		return result;
	}

	/**
	 * 文章批量审核 add by gss 2016-1-4
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> batchArticle(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		// 审核意见
		String obj = (String) context.get("obj");
		String articleStatus = (String) context.get("articleStatus");
		// 截取字符串
		String[] m = obj.split(",");
		String opinion = null;
		// 文章Id
		String articleId = "";
		// 审核意见
		String articleOpinion = "";
		// 创建文章实体
		GenericValue Article;
		for (int i = 0; i < m.length; i++) {
			opinion = m[i];
			String[] n = opinion.split(":");
			if (n.length > 1) {
				articleId = n[0];
				articleOpinion = n[1];
			} else {
				articleId = n[0];
			}
			try {
				Article = delegator.findByPrimaryKey("Article",
						UtilMisc.toMap("articleId", articleId));
				if (UtilValidate.isNotEmpty(Article)) {
					// 更新文章状态
					Article.set("articleStatus", articleStatus);
					// 更新文章审核意见
					Article.set("articleOpinion", articleOpinion);
					Article.store();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 文章编辑 add by gss 2016-1-4
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> editArticle(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		LocalDispatcher dispatcher = dctx.getDispatcher();
		// 文章Id
		String articleId = (String) context.get("articleId");
		// 文章标题
		String articleTitle = (String) context.get("articleTitle");
		// 文章标签
		String tag = (String) context.get("tag");
		// 原文链接
		String articleLink = (String) context.get("articleLink");
		// 作者
		String articleAuthor = (String) context.get("articleAuthor");
		// 文章分类Id
		String articleTypeId = (String) context.get("articleTypeId");
		// 文章状态 0已保存 ,1待审核,2已审核,3已拒绝
		String articleStatus = (String) context.get("articleStatus");
		// 文章内容
		String articleCentent = (String) context.get("articleCentent");
		// 关键字
		String articleKeyword = (String) context.get("articleKeyword");
		// 文章配图
		String contentId = (String) context.get("contentId");
		// 选择商品
		String productIds = (String) context.get("productIds");
		String[] tags = null;
		if (UtilValidate.isNotEmpty(tag)) {
			tags = tag.split(",");// 生成商品标签数组
		}
		GenericValue article;
		try {
			article = delegator.findByPrimaryKey("Article",
					UtilMisc.toMap("articleId", articleId));
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		if (UtilValidate.isNotEmpty(article)) {
			// 文章标题是否修改
			article.set("articleTitle", articleTitle);
			// 文章原文链接是否修改
			article.set("articleLink", articleLink);
			// 文章作者是否修改
			article.set("articleAuthor", articleAuthor);
			// 文章分类是否修改
			article.set("articleTypeId", articleTypeId);
			// 文章当前状态
			if ("0".equals(articleStatus) || "2".equals(articleStatus) || "3".equals(articleStatus)) {
				articleStatus = "1";
			}
			article.set("articleStatus", articleStatus);
			try {
				// 更新文章基本信息
				article.store();
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}
		// 商品标签关系表数据的更新
		if (UtilValidate.isNotEmpty(tags)) {
			List<GenericValue> productTagAssocList;
			try {
				productTagAssocList = delegator
						.findByAnd("ArticleTagAssoc", UtilMisc
								.toMap("articleId", articleId));
				if (UtilValidate
						.isNotEmpty(productTagAssocList)) {
					delegator.removeAll(productTagAssocList);
				}
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			for (int i = 0; i < tags.length; i++) {
				GenericValue articleTagAssoc = null;
				String articleTagAssocId = delegator
						.getNextSeqId("ArticleTagAssoc");
				articleTagAssoc = delegator.makeValue(
						"ArticleTagAssoc", UtilMisc.toMap(
								"articleTagAssocId",
								articleTagAssocId));
				// 商品ID
				articleTagAssoc.set("articleId", articleId);
				// 标签ID
				articleTagAssoc.set("tagId", tags[i]);
				// 创建表
				try {
					articleTagAssoc.create();
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
		}else{
			List<GenericValue> articleTagAssocList;
			try {
				articleTagAssocList = delegator
						.findByAnd("ArticleTagAssoc", UtilMisc
								.toMap("articleId", articleId));
				if (UtilValidate
						.isNotEmpty(articleTagAssocList)) {
					delegator.removeAll(articleTagAssocList);
				}
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
		}


		// 文章和商品关系表数据的更新
		String[] productIdInfs = null;
		if (UtilValidate.isNotEmpty(productIds)) {

			List<GenericValue> productProductAssocList;
			try {
				productProductAssocList = delegator.findByAnd("ArticleProductAssoc", UtilMisc.toMap("articleId", articleId));
				if (UtilValidate.isNotEmpty(productProductAssocList)) {
					delegator.removeAll(productProductAssocList);
				}
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			productIdInfs = productIds.split(",");// 生成选择商品数组

			for (int i = 0; i < productIdInfs.length; i++) {
				GenericValue articleProductAssoc = null;
				String articleProductAssocId = delegator.getNextSeqId("ArticleProductAssoc");
				articleProductAssoc = delegator.makeValue(
						"ArticleProductAssoc", UtilMisc.toMap(
								"articleProductAssocId",
								 articleProductAssocId));
				// 文章ID
				articleProductAssoc.set("articleId", articleId);
				// 商品ID
				articleProductAssoc.set("productId", productIdInfs[i]);
				// 创建表
				try {
					articleProductAssoc.create();
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
		}else{
			List<GenericValue> productProductAssocList;
			try {
				productProductAssocList = delegator.findByAnd("ArticleProductAssoc", UtilMisc.toMap("articleId", articleId));
				if (UtilValidate
						.isNotEmpty(productProductAssocList)) {
					delegator.removeAll(productProductAssocList);
				}
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

		}

		// 查询文章关键字
		try {
			List<GenericValue> keywordList = delegator.findByAnd(
					"ArticleKeyword", UtilMisc.toMap("articleId", articleId));
			// 判断文章关键字是否存在
			if (UtilValidate.isNotEmpty(keywordList)) {
				delegator.removeAll(keywordList);
			}
			// 重新创建关键字
			if (UtilValidate.isNotEmpty(articleKeyword)) {
				String regex = ",|，";
				// 根据中英文逗号分割
                String[] keyword = articleKeyword.split(regex);
				// 循环添加关键字
				List<String> list = new ArrayList<String>();
				for (int i = 0; i < keyword.length; i++) {
					if (!list.contains(keyword[i])) // 如果list数组不包括keyword[i]中的值的话，就返回true
					{
						list.add(keyword[i]); // 在list数组中加入keyword[i]的值。已经过滤过。
					}
				}
				for (int i = 0; i < list.size(); i++) {
					GenericValue ArticleKeyword = delegator
							.makeValue("ArticleKeyword");
					ArticleKeyword.set("articleId", articleId);
					ArticleKeyword.set("keyword", list.get(i));
					try {
						ArticleKeyword.create();
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// 判断是否上传了文章配图
		if (contentId != null) {
			try {
				GenericValue articleimgContent = EntityUtil.getFirst(delegator
						.findByAnd("ArticleContent", UtilMisc.toMap(
								"articleId", articleId, "articleContentTypeId",
								"ARTICLE_FIGURE")));
				if (UtilValidate.isNotEmpty(articleimgContent)) {
					if (!contentId.equals(articleimgContent.get("contentId"))) {
						articleimgContent.remove();
						GenericValue ArticleContent = delegator
								.makeValue("ArticleContent");
						ArticleContent.set("articleId", articleId);
						ArticleContent.set("contentId", contentId);
						ArticleContent.set("articleContentTypeId",
								"ARTICLE_FIGURE");
						try {
							ArticleContent.create();
						} catch (GenericEntityException e) {
							e.printStackTrace();
						}
					}
				} else {
					GenericValue ArticleContent = delegator
							.makeValue("ArticleContent");
					ArticleContent.set("articleId", articleId);
					ArticleContent.set("contentId", contentId);
					ArticleContent
							.set("articleContentTypeId", "ARTICLE_FIGURE");
					try {
						ArticleContent.create();
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				GenericValue articleImgContent = EntityUtil.getFirst(delegator
						.findByAnd("ArticleContent", UtilMisc.toMap(
								"articleId", articleId, "articleContentTypeId",
								"ARTICLE_FIGURE")));
				if (UtilValidate.isNotEmpty(articleImgContent)) {
					articleImgContent.remove();
				}
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}
		// 文章内容
		try {
			Map<String, Object> passedParams1 = FastMap.newInstance();
			// 文章内容关联表
			GenericValue articleContent = EntityUtil.getFirst(delegator
					.findByAnd("ArticleContent", UtilMisc.toMap("articleId",
							articleId, "articleContentTypeId",
							"ARTICLE_CONTENT")));
			if (UtilValidate.isNotEmpty(articleContent)) {
				// 获取内容表数据
				GenericValue Content = EntityUtil.getFirst(delegator.findByAnd(
						"Content",
						UtilMisc.toMap("contentId",
								articleContent.get("contentId"))));
				if (UtilValidate.isNotEmpty(Content)) {
					GenericValue dataResource = EntityUtil.getFirst(delegator
							.findByAnd(
									"DataResource",
									UtilMisc.toMap("dataResourceId",
											Content.get("dataResourceId"))));
					// 查询文章内容
					GenericValue electronicText = delegator.findByPrimaryKey(
							"ElectronicText",
							UtilMisc.toMap("dataResourceId",
									Content.get("dataResourceId")));
					// 判断文章内容是否存在
					if (UtilValidate.isNotEmpty(electronicText)) {

						if (UtilValidate.isEmpty(articleCentent)) {
							articleContent.remove();
						} else if (!articleCentent
								.equals((String) electronicText.get("textData")))// 判断文章内容是否修改
						{
							// 更新文章内容
							electronicText.set("textData", articleCentent);
							electronicText.store();
						}
					}
				}
			} else if (articleCentent != null) {
				passedParams1 = UtilMisc.toMap("dataResourceTypeId",
						"ELECTRONIC_TEXT", "dataTemplateTypeId", "NONE",
						"contentPurposeTypeId", "ARTICLE", "textData",
						articleCentent, "statusId", "CTNT_INITIAL_DRAFT",
						"userLogin", userLogin, "contentAssocTypeId",
						"SUB_CONTENT");
				try {
					result = dispatcher.runSync("createTextContent",
							passedParams1);
					if (UtilValidate.isNotEmpty(result)) {
						GenericValue ArticleContent = delegator
								.makeValue("ArticleContent");
						ArticleContent.set("articleId", articleId);
						ArticleContent.set("contentId",
								(String) result.get("contentId"));
						ArticleContent.set("articleContentTypeId",
								"ARTICLE_CONTENT");
						try {
							ArticleContent.create();
						} catch (GenericEntityException e) {
							e.printStackTrace();
						}
					}
				} catch (GenericServiceException e) {
					e.printStackTrace();
				}
			}

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.clear();
		return result;
	}

	/**
	 * 获取所有文章分类 add by gss 2016-1-8
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getArticleType(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 文章分类集合
		List<GenericValue> articleTypeList = FastList.newInstance();
		// 动态view
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		dynamicView.addMemberEntity("T", "ArticleType");
		dynamicView.addAlias("T", "id", "articleTypeId", null, false, false,
				null);
		dynamicView.addAlias("T", "name", "description", null, false, false,
				null);
		dynamicView.addAlias("T", "pId", "parentTypeId", null, false, false,
				null);
		List<String> fieldsToSelect = FastList.newInstance();
		List<String> orderBy = FastList.newInstance();
		fieldsToSelect.add("id");
		fieldsToSelect.add("name");
		fieldsToSelect.add("pId");
		orderBy.add("pId");
		try {
			// 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, true);
			EntityListIterator pli = delegator.findListIteratorByCondition(
					dynamicView, null, null, fieldsToSelect, orderBy, findOpts);
			articleTypeList = pli.getCompleteList();
			// 关闭pli
			pli.close();
		} catch (GenericEntityException e) {
			Debug.logError(e, "Type " + e.toString(), module);
		}
		result.put("articleTypeList", articleTypeList);
		return result;
	}

	/**
	 * 获取带全部的所有文章分类 add by gss 2016-1-8
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> getAllType(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 文章分类集合
		List<GenericValue> articleTypeList = FastList.newInstance();
		// 动态view
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		dynamicView.addMemberEntity("T", "ArticleType");
		dynamicView.addAlias("T", "id", "articleTypeId", null, false, false,
				null);
		dynamicView.addAlias("T", "name", "description", null, false, false,
				null);
		dynamicView.addAlias("T", "pId", "parentTypeId", null, false, false,
				null);
		dynamicView.addAlias("T", "typeLevel", "typeLevel", null, false, false,
				null);
		List<String> fieldsToSelect = FastList.newInstance();
		List<String> orderBy = FastList.newInstance();
		fieldsToSelect.add("id");
		fieldsToSelect.add("name");
		fieldsToSelect.add("pId");
		fieldsToSelect.add("typeLevel");
		orderBy.add("pId");

		List NodeList = new ArrayList();
		try {
			// 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
			EntityListIterator pli = delegator.findListIteratorByCondition(
					dynamicView, null, null, fieldsToSelect, orderBy, null);
			articleTypeList = pli.getCompleteList();
			// 关闭pli
			pli.close();
			Map map2 = new HashMap();
			map2.put("id", "-1");
			map2.put("name", "全部");
			map2.put("pId", "0");
			map2.put("open", true);
			NodeList.add(map2);
			for (int i = 0; i < articleTypeList.size(); i++) {
				Map map1 = new HashMap();
				if ("Level1".equals(articleTypeList.get(i).get("typeLevel"))) {
					map1.put("id", articleTypeList.get(i).get("id"));
					map1.put("name", articleTypeList.get(i).get("name"));
					map1.put("pId", "-1");
				} else {
					map1.put("id", articleTypeList.get(i).get("id"));
					map1.put("name", articleTypeList.get(i).get("name"));
					map1.put("pId", articleTypeList.get(i).get("pId"));
				}
				NodeList.add(map1);
			}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Type " + e.toString(), module);
		}
		result.put("articleTypeList", NodeList);
		return result;
	}
	
	/**
	 * 根据文章id获得详情
	 * 
	 * @author spj
	 * @param dctx
	 * @param context
	 * @return TODO
	 */
	public static Map<String, Object> getArticleDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
//		Locale locale = (Locale) context.get("locale");
		// 文章ID
		String articleId = (String) context.get("articleId");
		if (UtilValidate.isNotEmpty(articleId)) {
			try {
				// 文章内容
				GenericValue article = delegator.findByPrimaryKey("Article", UtilMisc.toMap("articleId", articleId));
				List<String> typeIds = new ArrayList<String>();
				if (UtilValidate.isNotEmpty(article)) {
					GenericValue articleType = delegator.findByPrimaryKey("ArticleType", UtilMisc.toMap("articleTypeId", article.get("articleTypeId")));
					String articleTypeName = "";
					if (UtilValidate.isNotEmpty(articleType)) {
						articleTypeName = (String) articleType.get("description");
					}
					result.put("article", article);
					result.put("articleTypeName", articleTypeName);
					
					List<GenericValue> ArticleTagAssocList = delegator.findByAnd("ArticleTagAssoc", UtilMisc.toMap("articleId", articleId));
					for (GenericValue articleTagAssocList : ArticleTagAssocList) {
						typeIds.add(articleTagAssocList.getString("tagId"));
					}
					
					// 如果标签不为空，查询所有的标签
					if(typeIds != null && typeIds.size() > 0) {
						// 动态view
						DynamicViewEntity dynamicView = new DynamicViewEntity();
						dynamicView.addMemberEntity("T", "Tag");
						dynamicView.addMemberEntity("TY", "TagType");
						dynamicView.addAliasAll("T", "", null);
						dynamicView.addAliasAll("TY", "", null);
						// 标签ID
						dynamicView.addAlias("T", "tagId");
						// 标签名称
						dynamicView.addAlias("T", "tagName");
						dynamicView.addViewLink("T", "TY", Boolean.FALSE, ModelKeyMap.makeKeyMapList("tagTypeId", "tagTypeId"));
						List<String> fieldsToSelect = FastList.newInstance();
						List<String> orderBy = FastList.newInstance();
						fieldsToSelect.add("tagId");
						fieldsToSelect.add("tagName");
						orderBy.add("tagId");
						List<EntityCondition> andExprs = FastList.newInstance();
						EntityCondition mainCond = null;
						andExprs.add(EntityCondition.makeCondition("tagTypeId", EntityOperator.IN, typeIds));
						if (andExprs.size() > 0) {
							mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
						}
						// 文章tag集合
						List<GenericValue> articleTagList = FastList.newInstance();
						try {
							EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
							// 查询的数据Iterator
							EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
							articleTagList = pli.getCompleteList();
							// 关闭pli
							pli.close();
						} catch (GenericEntityException e) {
							Debug.logError(e, "Type " + e.toString(), module);
						}
						result.put("articleTagList", articleTagList);
					}
					
				}
				// 文章关键字
				List<GenericValue> articleKeyword = delegator.findByAnd("ArticleKeyword", UtilMisc.toMap("articleId", articleId));
				String words = "";
				for (int i = 0; i < articleKeyword.size(); i++) {
					if (i == articleKeyword.size() - 1) {
						words += articleKeyword.get(i).get("keyword");
					} else {
						words += articleKeyword.get(i).get("keyword") + "，";
					}
				}
				if (UtilValidate.isNotEmpty(words)) {
					result.put("articleKeyword", words);
				}

				List<GenericValue> articleContentImg = delegator.findByAnd("ArticleContent", UtilMisc.toMap("articleId", articleId, "articleContentTypeId", "ARTICLE_FIGURE"));
				GenericValue articleimg = EntityUtil.getFirst(articleContentImg);
				result.put("articleContentImg", articleimg.getString("contentId"));
				// 查询文章内容
				GenericValue articleConContent = EntityUtil.getFirst(delegator.findByAnd("ArticleContent", UtilMisc.toMap("articleId", articleId, "articleContentTypeId", "ARTICLE_CONTENT")));
				if (UtilValidate.isNotEmpty(articleConContent)) {
					GenericValue Content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", articleConContent.get("contentId")));
					if (UtilValidate.isNotEmpty(Content)) {
						GenericValue Contenttext = delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId")));
						if (UtilValidate.isNotEmpty(Contenttext)) {
							result.put("articleContents", Contenttext.get("textData"));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取带全部的所有文章标签 add by gss 2016-1-8
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getArticleTag(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 文章tag集合
		List<GenericValue> articleTagList = FastList.newInstance();
		// 动态view
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		dynamicView.addMemberEntity("T", "Tag");
		dynamicView.addMemberEntity("TY", "TagType");
		dynamicView.addAliasAll("T", "", null);
		dynamicView.addAliasAll("TY", "", null);
		// 标签ID
		dynamicView.addAlias("T", "tagId");
		// 标签名称
		dynamicView.addAlias("T", "tagName");
		dynamicView.addViewLink("T", "TY", Boolean.FALSE,
				ModelKeyMap.makeKeyMapList("tagTypeId", "tagTypeId"));
		List<String> fieldsToSelect = FastList.newInstance();
		List<String> orderBy = FastList.newInstance();
		fieldsToSelect.add("tagId");
		fieldsToSelect.add("tagName");
		orderBy.add("tagId");
		List<String> TypeIdList = new ArrayList<String>();
		// 查询所有标签
		EntityConditionList<EntityExpr> Condition = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"tagTypeId", EntityOperator.EQUALS, "Article"),
						EntityCondition.makeCondition("parentTagTypeId",
								EntityOperator.EQUALS, "Article")),
						EntityOperator.OR);
		List<GenericValue> typelists;
		try {
			typelists = delegator.findList("TagType", Condition, null, null,
					null, false);
			if (UtilValidate.isNotEmpty(typelists)) {
				for (GenericValue typelist : typelists) {
					TypeIdList.add((String) typelist.get("tagTypeId"));
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		List<EntityCondition> andExprs = FastList.newInstance();
		EntityCondition mainCond = null;
		andExprs.add(EntityCondition.makeCondition("tagTypeId",
				EntityOperator.IN, TypeIdList));
		if (andExprs.size() > 0) {
			mainCond = EntityCondition.makeCondition(andExprs,
					EntityOperator.AND);
		}
		try {
			EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY, true);
			// 查询的数据Iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(
					dynamicView, mainCond, null, fieldsToSelect, orderBy,
					findOpts);
			articleTagList = pli.getCompleteList();
			// 关闭pli
			pli.close();
		} catch (GenericEntityException e) {
			Debug.logError(e, "Type " + e.toString(), module);
		}
		result.put("articleTagList", articleTagList);
		return result;
	}


	/**
	 * 根据文章编码取得文章关联商品信息
	 *
	 * @param dispatchContext
	 * @param context
	 * @return
	 */
	public Map<String, Object> getArticleProduct(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String articleId = (String) context.get("articleId");
		Delegator delegator = dispatchContext.getDelegator();
		try {
			// 取得文章和商品的关联信息
			if (UtilValidate.isNotEmpty(articleId)) {
				List<GenericValue> articleProducts = delegator.findByAnd("ArticleProductAssoc", UtilMisc.toMap("articleId", articleId));

				List<Map<String, Object>> productList = FastList.newInstance();
				if (UtilValidate.isNotEmpty(articleProducts)) {
					for (GenericValue articleProductsInfo : articleProducts) {
						String productId = articleProductsInfo.getString("productId");
						GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
						Map<String, Object> map = FastMap.newInstance();
						map.put("productId", productId);// 商品编码
						map.put("productName", product.get("productName"));// 商品名称
						GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE")));
						if (UtilValidate.isNotEmpty(defaultprice)) {
							map.put("defaultprice", defaultprice.get("price")); // 销售价格
						} else {
							map.put("defaultprice", 0);
						}
						// 商品规格的取得
						String featureInfo = "";// 商品规格
						if (UtilValidate.isNotEmpty(productId)) {
							if (UtilValidate.isNotEmpty(product)) {
								if (UtilValidate.isNotEmpty(product.getString("mainProductId"))) {
									if (UtilValidate.isNotEmpty(product.getString("featureaProductId"))) {
										GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", product.getString("featureaProductId")));
										if (UtilValidate.isNotEmpty(productFeatureInfo)) {
											GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
											if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
												String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
												String productFeatureName = productFeatureInfo.getString("productFeatureName");
												featureInfo = productFeatureTypeName + ":" + productFeatureName;
											}
										}
									}
								}
							}
						}
						map.put("featureInfo", featureInfo);// 特征

						// 取得商品图片
						String imgUrl = "";// 商品图片
						// 根据商品ID获取商品图片url
						String productAdditionalImage1 = "";
						List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
								UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
						if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
							imgUrl = "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId");
						}
						map.put("imgUrl", imgUrl);
						productList.add(map);
					}
				}
				result.put("productList", productList);
			}
		} catch (GenericEntityException e) {

			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		return result;
	}


}
