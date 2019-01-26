package org.ofbiz.product.product;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * 商品品牌管理 新增 查询
 * author cc
 *
 */
public class BusinessBrandServices {
	public static final String module = BusinessBrandServices.class.getName();
	public static final String resource = "PartyUiLabels";

	/**
	 * 修改成商品品牌查询 需要修改
	 * 还有bug需要处理
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getBusinessBrandList(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// LocalDispatcher对象
		LocalDispatcher dispatcher = dctx.getDispatcher();
		// 记录集合
		List<GenericValue> recordsList = FastList.newInstance();

		// 总记录数
		int totalSize = 0;
		// 查询开始条数
		int lowIndex = 0;
		// 查询结束条数
		int highIndex = 0;

		// 跳转的页数
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		result.put("viewIndex", Integer.valueOf(viewIndex));

		// 每页显示记录条数
		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		result.put("viewSize", Integer.valueOf(viewSize));

		// 动态view
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		// 查询条件集合，用于数据库查询
		List<EntityCondition> filedExprs = FastList.newInstance();
		EntityCondition mainCond = null;
		// 排序字段集合
		List<String> orderBy = FastList.newInstance();
		// 显示字段集合
		List<String> fieldsToSelect = FastList.newInstance();
		//品牌表 中 img 品牌名称 别名
		//中间表
		dynamicView.addMemberEntity("PB", "PartyBusinessBrand");
		dynamicView.addAlias("PB", "partyId");
		dynamicView.addAlias("PB", "productBrandId");

		//商家表
		dynamicView.addMemberEntity("P", "PartyBusiness");
		dynamicView.addAlias("P", "partyId");
		dynamicView.addAlias("P", "auditStatus");
		dynamicView.addAlias("P", "logoImg");
		dynamicView.addViewLink("PB", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

		//品牌表
		dynamicView.addMemberEntity("B", "ProductBrand");
		dynamicView.addAlias("B", "productBrandId");
		dynamicView.addAlias("B", "brandName");
		dynamicView.addAlias("B", "brandNameAlias");
		dynamicView.addAlias("B", "brandDesc");
		dynamicView.addAlias("B", "createdStamp");
		dynamicView.addViewLink("PB", "B", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));

		fieldsToSelect.add("partyId");
		fieldsToSelect.add("productBrandId");
		fieldsToSelect.add("auditStatus");
		fieldsToSelect.add("logoImg");
		fieldsToSelect.add("brandName");
		fieldsToSelect.add("brandNameAlias");
		fieldsToSelect.add("brandDesc");
		fieldsToSelect.add("createdStamp");

		//品牌名称 模糊查询
		if (UtilValidate.isNotEmpty(context.get("brandName"))) {
			filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("brandName"),
					EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("brandName") + "%")));
		}

		// 按法人名称模糊查询
//		if (UtilValidate.isNotEmpty(context.get("legalPersonName"))) {
//			filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("legalPersonName"),
//					EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("legalPersonName") + "%")));
//		}

		// 按审核状态精确查询
		if (UtilValidate.isNotEmpty(context.get("auditStatus"))) {
			filedExprs.add(
					EntityCondition.makeCondition("auditStatus", EntityOperator.EQUALS, context.get("auditStatus")));
		}

		// 排序字段名称 可能要加
		String sortField = "productBrandId";
		if (UtilValidate.isNotEmpty(context.get("sortField"))) {
			sortField = (String) context.get("sortField");
		}
		// 排序类型
		String sortType = "";
		if (UtilValidate.isNotEmpty(context.get("sortType"))) {
			sortType = (String) context.get("sortType");
		}
		orderBy.add(sortType + sortField);

		// 添加where条件
		if (filedExprs.size() > 0) {
			mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
		}

		try {
			lowIndex = viewIndex * viewSize + 1;
			highIndex = (viewIndex + 1) * viewSize;

			// 去除重复数据
			EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
			// 查询的数据Iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect,
					orderBy, findOpts);
			// 获取分页所需的记录集合
			recordsList = pli.getPartialList(lowIndex, viewSize);

			// 获取总记录数
			totalSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > totalSize) {
				highIndex = totalSize;
			}

			// 关闭 iterator
			pli.close();
		} catch (GenericEntityException e) {
			String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
			Debug.logError(e, errMsg, module);
		}

		// 返回的参数
		result.put("recordsList", recordsList);
		result.put("totalSize", Integer.valueOf(totalSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));

		return result;
	}

	//品牌查询

	/**
	 * 商家品牌新增
	 * @param dcx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> addProductBrand(DispatchContext dcx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		GenericValue userLogin = (GenericValue) context.get("userLogin");

//		String productBrandId = (String) context.get("productBrandId");//商品品牌ID
		String brandName = (String) context.get("brandName"); //品牌名称
		String brandNameAlias = (String) context.get("brandNameAlias"); //品牌别名
//        String contentId = (String)context.get("contentId");//log图片
		//创建还是修改
//		String operateType = (String) context.get("operateType");//操作类型
//		String productCategoryIds = (String) context.get("productCategoryIds");//商品分类信息

		//用来获取logoImg 对应logo图片
		String contentId = (String) context.get("contentId");//商品分类信息
//		String fileName = (String) context.get("_uploadedFile_fileName");
//		String contentType = (String) context.get("_uploadedFile_contentType");
//		ByteBuffer imageData = (ByteBuffer) context.get("uploadedFile");
//		String productContentTypeId = (String) context.get("productContentTypeId");
//		String productId = (String) context.get("productId");
		LocalDispatcher dispatcher = dcx.getDispatcher();
		Delegator delegator = dcx.getDelegator();
//		Locale locale = (Locale) context.get("locale");

//		if (operateType.equals("create") || operateType.equals("update")) {
			// 品牌名称输入验证
//			if (UtilValidate.isEmpty(brandName)) {
//				String errMsg = "品牌名称不能为空";
//				return ServiceUtil.returnError(errMsg);
//			}
			// 品牌logo输入验证

//		    if (UtilValidate.isNotEmpty(fileName)){
//	            Map<String, Object> passedParams = FastMap.newInstance();
//	            passedParams.put("userLogin", userLogin);
//	            passedParams.put("_uploadedFile_fileName", fileName);
//	            passedParams.put("_uploadedFile_contentType", contentType);
//	            passedParams.put("uploadedFile", imageData);
//	            passedParams.put("productId", productId);
//	            passedParams.put("productContentTypeId", productContentTypeId);
//	            Map<String, Object> resultMap = FastMap.newInstance();
//	            try {
//	            	resultMap = dispatcher.runSync("addProductCategoryPicUpload", passedParams);
//	            } catch (GenericServiceException e) {
//					return ServiceUtil.returnError(e.getMessage());
//				}
//	            contentId = (String) resultMap.get("contentId");
//	        }else{
//	        	// String errMsg = "品牌logo不能为空";
//	        	// return ServiceUtil.returnError(errMsg);
//	        }

//		}
//		if (UtilValidate.isEmpty(productBrandId) && operateType.equals("create")) {
			try {
				String productBrandIdCreate = delegator.getNextSeqId("ProductBrand");
				GenericValue productBrand = null;
				productBrand = delegator.makeValue("ProductBrand", UtilMisc.toMap("productBrandId", productBrandIdCreate));
				// 品牌名称
				productBrand.set("brandName", brandName);

				// 品牌别名
				if (UtilValidate.isNotEmpty(brandNameAlias)) {
					productBrand.set("brandNameAlias", brandNameAlias);
				}
				// 品牌Logo
				if (UtilValidate.isNotEmpty(contentId)) {
					productBrand.set("contentId", contentId);
				}
				//品牌证书
//				if (UtilValidate.isNotEmpty(contentId)) {
//					productBrand.set("contentId", contentId);
//				}

				// 是否启用标识
				productBrand.set("isUsed", "Y");
				productBrand.set("isDel", "B");
				// 创建表
				productBrand.create();
				// 品牌分类关系表
//				if (UtilValidate.isNotEmpty(productCategoryIds)) {
//					GenericValue productBrandCategory = null;
//					String productBrandCategoryCreate = "";
//					String[] productCategoryIdsArry = productCategoryIds.split(",");
//					for (String productCategoryId : productCategoryIdsArry) {
//						productBrandCategoryCreate = delegator.getNextSeqId("ProductBrandCategory");
//						// 品牌和分类的关系表
//						productBrandCategory = delegator.makeValue("ProductBrandCategory", UtilMisc.toMap("productBrandCategoryId", productBrandCategoryCreate));
//						// 品牌Id
//						productBrandCategory.set("productBrandId", productBrandIdCreate);
//						// 分类Id
//						productBrandCategory.set("productCategoryId", productCategoryId);
//						// 创建表
//						productBrandCategory.create();
//					}
//				}
			} catch (Exception e) {
				// TODO: handle exception
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			}

//		}
		result = ServiceUtil.returnSuccess();
		return result;
	}


	/**
	 * 商家详情 add by qianjin 2016.01.27
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getBusinessInfoById(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 参数
		String businessId = (String) context.get("businessId");
		// 动态view
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		// 显示字段集合
		List<String> fieldsToSelect = FastList.newInstance();
		dynamicView.addMemberEntity("PB", "PartyBusiness");
		dynamicView.addAlias("PB", "partyId");
		dynamicView.addAlias("PB", "businessName");
		dynamicView.addAlias("PB", "legalPersonName");
		dynamicView.addAlias("PB", "tel");
		dynamicView.addAlias("PB", "logoImg");
		dynamicView.addAlias("PB", "address");
		dynamicView.addAlias("PB", "idCard");
		dynamicView.addAlias("PB", "businessLicense");
		dynamicView.addAlias("PB", "idCardProsImg");
		dynamicView.addAlias("PB", "idCardConsImg");
		dynamicView.addAlias("PB", "businessLicenseImg");
		dynamicView.addAlias("PB", "auditStatus");
		dynamicView.addAlias("PB", "description");
		dynamicView.addAlias("PB", "createdStamp");
		dynamicView.addAlias("PB", "businessType");

		dynamicView.addMemberEntity("P", "Party");
		dynamicView.addAlias("P", "statusId");
		dynamicView.addViewLink("PB", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

		dynamicView.addMemberEntity("PR", "PartyRelationship");
		dynamicView.addAlias("PR", "partyIdTo");
		dynamicView.addViewLink("PB", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyIdFrom"));

		dynamicView.addMemberEntity("G1", "Geo");
		dynamicView.addAlias("G1", "provinceId", "geoId", null, false, false, null);
		dynamicView.addAlias("G1", "provinceName", "geoName", null, false, false, null);
		dynamicView.addViewLink("PB", "G1", Boolean.FALSE, ModelKeyMap.makeKeyMapList("province", "geoId"));

		dynamicView.addMemberEntity("G2", "Geo");
		dynamicView.addAlias("G2", "cityId", "geoId", null, false, false, null);
		dynamicView.addAlias("G2", "cityName", "geoName", null, false, false, null);
		dynamicView.addViewLink("PB", "G2", Boolean.FALSE, ModelKeyMap.makeKeyMapList("city", "geoId"));

		dynamicView.addMemberEntity("G3", "Geo");
		dynamicView.addAlias("G3", "countyId", "geoId", null, false, false, null);
		dynamicView.addAlias("G3", "countyName", "geoName", null, false, false, null);
		dynamicView.addViewLink("PB", "G3", Boolean.FALSE, ModelKeyMap.makeKeyMapList("county", "geoId"));

		dynamicView.addMemberEntity("EN", "Enumeration");
		dynamicView.addAlias("EN", "businessTypeId", "enumId", null, false, false, null);
		dynamicView.addAlias("EN", "businessTypeName", "description", null, false, false, null);
		dynamicView.addViewLink("PB", "EN", Boolean.FALSE, ModelKeyMap.makeKeyMapList("businessType", "enumId"));

		fieldsToSelect.add("businessName");
		fieldsToSelect.add("partyId");
		fieldsToSelect.add("partyIdTo");
		fieldsToSelect.add("provinceId");
		fieldsToSelect.add("provinceName");
		fieldsToSelect.add("cityId");
		fieldsToSelect.add("cityName");
		fieldsToSelect.add("countyId");
		fieldsToSelect.add("countyName");
		fieldsToSelect.add("address");
		fieldsToSelect.add("tel");
		fieldsToSelect.add("legalPersonName");
		fieldsToSelect.add("idCard");
		fieldsToSelect.add("businessLicense");
		fieldsToSelect.add("auditStatus");
		fieldsToSelect.add("statusId");
		fieldsToSelect.add("idCardProsImg");
		fieldsToSelect.add("idCardConsImg");
		fieldsToSelect.add("businessLicenseImg");
		fieldsToSelect.add("logoImg");
		fieldsToSelect.add("createdStamp");
		fieldsToSelect.add("description");
		fieldsToSelect.add("businessTypeId");
		fieldsToSelect.add("businessTypeName");
		// 条件
		EntityCondition whereCond = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, businessId);

		try {
			// 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY,  true);
			EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, whereCond, null, fieldsToSelect,
					null, findOpts);
			Map record_map = FastMap.newInstance();
			for (GenericValue gv : pli.getCompleteList()) {
				record_map.put("businessName", gv.getString("businessName"));
				record_map.put("partyId", gv.getString("partyId"));
				record_map.put("partyIdTo", gv.getString("partyIdTo"));
				record_map.put("provinceId", gv.getString("provinceId"));
				record_map.put("provinceName", gv.getString("provinceName"));
				record_map.put("cityId", gv.getString("cityId"));
				record_map.put("cityName", gv.getString("cityName"));
				record_map.put("countyId", gv.getString("countyId"));
				record_map.put("countyName", gv.getString("countyName"));
				record_map.put("address", gv.getString("address"));
				record_map.put("tel", gv.getString("tel"));
				record_map.put("legalPersonName", gv.getString("legalPersonName"));
				record_map.put("idCard", gv.getString("idCard"));
				record_map.put("businessLicense", gv.getString("businessLicense"));
				record_map.put("businessTypeId", gv.getString("businessTypeId"));
				record_map.put("businessTypeName", gv.getString("businessTypeName"));
				// 替换审核状态
				String auditStatus = "";
				if ("0".equals(gv.getString("auditStatus"))) {
					auditStatus = "待审核";
				} else if ("1".equals(gv.getString("auditStatus"))) {
					auditStatus = "已通过";
				} else if ("2".equals(gv.getString("auditStatus"))) {
					auditStatus = "已拒绝";
				}
				record_map.put("auditStatus", auditStatus);
				// 替换启用状态
				String statusId = "";
				if ("PARTY_ENABLED".equals(gv.getString("statusId"))) {
					statusId = "是";
				} else {
					statusId = "否";
				}
				record_map.put("isUse", gv.getString("statusId"));
				record_map.put("statusId", statusId);
				record_map.put("idCardProsImg", gv.getString("idCardProsImg"));
				record_map.put("idCardConsImg", gv.getString("idCardConsImg"));
				record_map.put("businessLicenseImg", gv.getString("businessLicenseImg"));
				record_map.put("logoImg", gv.getString("logoImg"));
				record_map.put("createdStamp", gv.getString("createdStamp"));
				record_map.put("description", gv.getString("description"));

				// 获取商家的品牌，并编辑成字符串格式
				// 动态view
				DynamicViewEntity pbb_dv = new DynamicViewEntity();
				pbb_dv.addMemberEntity("PBB", "PartyBusinessBrand");
				pbb_dv.addAlias("PBB", "partyId");
				pbb_dv.addAlias("PBB", "productBrandId");
				pbb_dv.addMemberEntity("PB", "ProductBrand");
				pbb_dv.addAlias("PB", "brandName");
				pbb_dv.addViewLink("PBB", "PB", false, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));
				// 条件
				EntityCondition cond = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						gv.getString("partyId"));

				EntityListIterator brand_list = delegator.findListIteratorByCondition(pbb_dv, cond, null, null, null,
						findOpts);
				String brands_str = "";
				String brandIds_str = "";
				for (int i = 0; i < brand_list.getCompleteList().size(); i++) {
					brands_str += brand_list.getCompleteList().get(i).getString("brandName");
					brandIds_str += brand_list.getCompleteList().get(i).getString("productBrandId");
					if (i != brand_list.getCompleteList().size() - 1) {
						brands_str += "、";
						brandIds_str += ",";
					}
				}
				record_map.put("brands", brands_str);
				record_map.put("brandIds", brandIds_str);
				brand_list.close();
			}
			result.put("record", record_map);
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 商家修改 add by qianjin 2016.01.28
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> businessEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		// 参数
		String businessName = (String) context.get("businessName");
		String businessId = (String) context.get("businessId");
		List<String> brands = UtilValidate.isEmpty(context.get("brands[]")) ? FastList.newInstance()
				: (List) context.get("brands[]");
		String logoImg = (String) context.get("logoImg");
		String province = (String) context.get("province");
		String city = (String) context.get("city");
		String county = (String) context.get("county");
		String address = (String) context.get("address");
		String tel = (String) context.get("tel");
		String isUse = (String) context.get("isUse");
		String legalPersonName = (String) context.get("legalPersonName");
		String idCard = (String) context.get("idCard");
		String businessLicense = (String) context.get("businessLicense");
		String idCardProsImg = (String) context.get("idCardProsImg");
		String idCardConsImg = (String) context.get("idCardConsImg");
		String businessLicenseImg = (String) context.get("businessLicenseImg");
		String description = (String) context.get("description");
		// 商家类型
		String businessType = (String) context.get("businessType");

		try {
			// 调用账号启用服务
			dispatcher.runSync("editBusinessIsUse", UtilMisc.toMap("businessId", businessId, "isUse", isUse));

			// 根据ID获取商家记录并修改
			GenericValue pb_gv = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", businessId));
			pb_gv.setString("businessName", businessName);
			pb_gv.setString("logoImg", logoImg);
			pb_gv.setString("province", province);
			pb_gv.setString("city", city);
			pb_gv.setString("county", county);
			pb_gv.setString("address", address);
			pb_gv.setString("tel", tel);
			pb_gv.setString("legalPersonName", legalPersonName);
			pb_gv.setString("idCard", idCard);
			pb_gv.setString("businessLicense", businessLicense);
			pb_gv.setString("idCardProsImg", idCardProsImg);
			pb_gv.setString("idCardConsImg", idCardConsImg);
			pb_gv.setString("businessLicenseImg", businessLicenseImg);
			pb_gv.setString("description", description);
			pb_gv.setString("businessType", businessType);

			pb_gv.store();

			// 删除该商家的品牌数据
			delegator.removeByAnd("PartyBusinessBrand", UtilMisc.toMap("partyId", businessId));
			// 新增商家和品牌关系
			for (String brandId : brands) {
				GenericValue brand_gv = delegator.makeValue("PartyBusinessBrand",
						UtilMisc.toMap("partyId", businessId, "productBrandId", brandId));
				brand_gv.create();
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 商家新增 add by qianjin 2016.01.26
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> businessAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		// 参数
		String businessName = (String) context.get("businessName");
		String partyId = (String) context.get("partyId");
		List<String> brands = UtilValidate.isEmpty(context.get("brands[]")) ? FastList.newInstance()
				: (List) context.get("brands[]");
		String logoImg = (String) context.get("logoImg");
		String province = (String) context.get("province");
		String city = (String) context.get("city");
		String county = (String) context.get("county");
		String address = (String) context.get("address");
		String tel = (String) context.get("tel");
		String isUse = UtilValidate.isEmpty(context.get("isUse")) ? "PARTY_DISABLED" : (String) context.get("isUse");
		String legalPersonName = (String) context.get("legalPersonName");
		String idCard = (String) context.get("idCard");
		String businessLicense = (String) context.get("businessLicense");
		String idCardProsImg = (String) context.get("idCardProsImg");
		String idCardConsImg = (String) context.get("idCardConsImg");
		String businessLicenseImg = (String) context.get("businessLicenseImg");
		String auditStatus = (String) context.get("auditStatus");
		String description = (String) context.get("description");
		String businessType = (String) context.get("businessType");

		try {
			// 调用检查人员是否已申请商家服务
			Map cpb_map = dispatcher.runSync("checkPartyIsBusiness", UtilMisc.toMap("partyId", partyId, "equals", "1"));
			int isHas = (Integer) cpb_map.get("status");
			// 0:有申请记录 1：没有申请记录
			if (isHas == 0) {
				// 修改申请记录的状态，并开通账号启用状态
				List<GenericValue> b_list = (List) cpb_map.get("recordsList");
				for (GenericValue gv : b_list) {
					String b_id = gv.getString("partyId");
					dispatcher.runSync("editBusinessIsUse", UtilMisc.toMap("businessId", b_id, "isUse", isUse));
					// 根据ID获取商家记录并修改
					GenericValue pb_gv = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", b_id));
					pb_gv.setString("businessName", businessName);
					pb_gv.setString("logoImg", logoImg);
					pb_gv.setString("province", province);
					pb_gv.setString("city", city);
					pb_gv.setString("county", county);
					pb_gv.setString("address", address);
					pb_gv.setString("tel", tel);
					pb_gv.setString("legalPersonName", legalPersonName);
					pb_gv.setString("idCard", idCard);
					pb_gv.setString("businessLicense", businessLicense);
					pb_gv.setString("idCardProsImg", idCardProsImg);
					pb_gv.setString("idCardConsImg", idCardConsImg);
					pb_gv.setString("businessLicenseImg", businessLicenseImg);
					pb_gv.setString("auditStatus", "1");
					pb_gv.setString("description", description);
					pb_gv.setString("businessType", businessType);
					pb_gv.store();
				}
			} else {
				String business_id = getBusinessPartyId(delegator);
				// 调用创建party服务
				Map pg_map = dispatcher.runSync("createPartyGroup", UtilMisc.toMap("groupName", businessName, "partyId",
						business_id, "statusId", isUse, "partyCategory", "BUSINESS"));

				// 新增一条商家记录
				GenericValue business_gv = delegator.makeValue("PartyBusiness", UtilMisc.toMap("partyId", business_id));
				business_gv.setString("businessName", businessName);
				business_gv.setString("logoImg", logoImg);
				business_gv.setString("province", province);
				business_gv.setString("city", city);
				business_gv.setString("county", county);
				business_gv.setString("address", address);
				business_gv.setString("tel", tel);
				business_gv.setString("legalPersonName", legalPersonName);
				business_gv.setString("idCard", idCard);
				business_gv.setString("businessLicense", businessLicense);
				business_gv.setString("idCardProsImg", idCardProsImg);
				business_gv.setString("idCardConsImg", idCardConsImg);
				business_gv.setString("businessLicenseImg", businessLicenseImg);
				business_gv.setString("businessType", businessType);
				if (UtilValidate.isEmpty(auditStatus)) {
					auditStatus = "1";
					// 调用创建userLogin服务
					dispatcher.runSync("createUserLogin",
							UtilMisc.toMap("userLogin", (GenericValue) context.get("userLogin"), "userLoginId",
									business_id, "enabled", "Y", "currentPassword", "123456", "currentPasswordVerify",
									"123456", "partyId", business_id));

					// 调用创建登录用户权限服务
					dispatcher.runSync("addUserLoginToSecurityGroup",
							UtilMisc.toMap("userLogin", (GenericValue) context.get("userLogin"), "userLoginId",
									business_id, "groupId", "SUPPLIER"));
				}
				business_gv.setString("auditStatus", auditStatus);
				business_gv.setString("description", description);
				business_gv.create();

				// 调用创建party角色服务，创建用户角色
				dispatcher.runSync("ensurePartyRole", UtilMisc.toMap("userLogin",
						(GenericValue) context.get("userLogin"), "partyId", partyId, "roleTypeId", "EMPLOYEE"));
				// 调用创建party角色服务，创建商家角色
				dispatcher.runSync("ensurePartyRole", UtilMisc.toMap("userLogin",
						(GenericValue) context.get("userLogin"), "partyId", business_id, "roleTypeId", "SUPPLIER"));

				// 调用创建商家和会员的关联关系服务
				dispatcher.runSync("createPartyRelationship",
						UtilMisc.toMap("userLogin", (GenericValue) context.get("userLogin"), "partyIdFrom", business_id,
								"partyIdTo", partyId, "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE",
								"partyRelationshipTypeId", "OWNER"));
				if (UtilValidate.isNotEmpty(brands)) {
					// 新增商家和品牌关系
					for (String brandId : brands) {
						GenericValue brand_gv = delegator.makeValue("PartyBusinessBrand",
								UtilMisc.toMap("partyId", business_id, "productBrandId", brandId));
						brand_gv.create();
					}
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 根据下一个商家partyID，确保该partyID创建的登录账号唯一 add by qianjin 2016.01.26
	 * 
	 * @param delegator
	 * @return
	 */
	private static String getBusinessPartyId(Delegator delegator) {
		String business_id = "SJ_" + delegator.getNextSeqId("Party");
		try {
			GenericValue ul_gv = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", business_id));
			if (UtilValidate.isNotEmpty(ul_gv)) {
				business_id = getBusinessPartyId(delegator);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return business_id;
	}

	/**
	 * 检查该用户是否已是商家 add by qianjin 2016.01.26
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> checkPartyIsBusiness(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 参数
		String partyId = (String) context.get("partyId");

		// 动态view
		DynamicViewEntity pr_dv = new DynamicViewEntity();
		pr_dv.addMemberEntity("PB", "PartyBusiness");
		pr_dv.addAlias("PB", "partyId");
		pr_dv.addAlias("PB", "auditStatus");

		pr_dv.addMemberEntity("PR", "PartyRelationship");
		pr_dv.addAlias("PR", "partyIdFrom");
		pr_dv.addAlias("PR", "partyIdTo");
		pr_dv.addAlias("PR", "roleTypeIdFrom");
		pr_dv.addViewLink("PB", "PR", false, ModelKeyMap.makeKeyMapList("partyId", "partyIdFrom"));
		// 查询条件集合，用于数据库查询
		List<EntityCondition> exprs = FastList.newInstance();
		EntityCondition cond = null;
		exprs.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, partyId));
		exprs.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "SUPPLIER"));

		// 添加where条件
		if (exprs.size() > 0) {
			cond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
		}
		// 显示字段集合
		List<String> fields = FastList.newInstance();
		List<String> order = FastList.newInstance();

		try {
			// 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY,  true);
			EntityListIterator pr_li = delegator.findListIteratorByCondition(pr_dv, cond, null, fields, order, findOpts);
			// 判断该人员是否已经是商家，0:是 1：否
			if (pr_li.getResultsSizeAfterPartialList() == 0) {
				result.put("status", 1);
			} else {
				result.put("status", 0);
			}
			result.put("recordsList", pr_li.getCompleteList());
			pr_li.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 商家修改是否启用状态 add by qianjin 2016/01/27
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> editBusinessIsUse(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 商家ID
		String businessId = (String) context.get("businessId");
		// 是否启用状态
		String isUse = (String) context.get("isUse");

		try {
			// 根据ID获取商家记录并修改
			GenericValue party_gv = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", businessId));
			party_gv.setString("statusId", isUse);
			party_gv.store();

			// 判断启用状态，更改账号的启用状态
			String enabled = "Y";
			if ("PARTY_DISABLED".equals(isUse)) {
				enabled = "N";
			}
			GenericValue ul_gv = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", businessId));
			ul_gv.setString("enabled", enabled);
			ul_gv.store();

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 商家审核 add by qianjin 2016.01.28
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> businessAudit(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 参数
		String businessId = (String) context.get("businessId");
		String auditStatus = (String) context.get("auditStatus");
		String auditContent = (String) context.get("auditContent");

		try {
			GenericValue pb_gv = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", businessId));
			pb_gv.setString("auditStatus", auditStatus);
			pb_gv.store();

			String pba_id = delegator.getNextSeqId("PartyBusinessAudit");
			// 新增一条商家审核记录
			GenericValue pba_gv = delegator.makeValue("PartyBusinessAudit",
					UtilMisc.toMap("partyBusinessAuditId", pba_id));
			pba_gv.setString("partyId", businessId);
			pba_gv.setString("auditContent", auditContent);
			pba_gv.setString("auditStatus", auditStatus);
			pba_gv.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 商家拒绝意见查询 add by qianjin 2016.01.28
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> businessRefuseReason(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 参数
		String businessId = (String) context.get("businessId");

		try {
			List<GenericValue> pba_list = delegator.findByAnd("PartyBusinessAudit",
					UtilMisc.toMap("partyId", businessId, "auditStatus", "2"), UtilMisc.toList("-createdStamp"));
			if (UtilValidate.isNotEmpty(pba_list)) {
				GenericValue pba_gv = EntityUtil.getFirst(pba_list);
				result.put("auditContent", pba_gv.getString("auditContent"));
			} else {
				result.put("auditContent", "无内容");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 商家导入
	 * @param request
	 * @param response
	 */
	public static void businessImport(HttpServletRequest request, HttpServletResponse response) {
		// 返回信息，json格式
		String returnJson = "";
		// 当前用户
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		// Delegator对象
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		// LocalDispatcher对象
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		try {
			// 调用Excel导入方法
			Map rs = dispatcher.runSync("excelImport", UtilMisc.toMap("request", request, "xmlUrl",
					"src/com/qihua/ofbiz/business/BusinessValidate.xml", "validateCellData", "businessValidateCell"));
			// 获取导入的信息
			returnJson = rs.get("msg").toString();
			// 获取导入的数据list
			List<Map> listDatas = (List<Map>) rs.get("listDatas");

			// 遍历list，进行新增或修改操作
			for (Map record : listDatas) {
				String businessName = (String) record.get("business_name");
				String partyId = (String) record.get("party_id");
				String province = (String) record.get("province");
				String city = (String) record.get("city");
				String county = (String) record.get("county");
				String address = (String) record.get("address");
				String tel = (String) record.get("tel");
				String isUse = (String) record.get("is_use");
				String legalPersonName = (String) record.get("legal_person_name");
				String businessTypeName = (String) record.get("business_type");
				try {
					// 设置商家参数map
					Map business_map = FastMap.newInstance();
					business_map.put("businessName", businessName);
					business_map.put("partyId", partyId);
					// 获取省ID
					List<GenericValue> province_list = delegator.findByAnd("Geo",
							UtilMisc.toMap("geoName", province, "geoTypeId", "PROVINCE"));
					business_map.put("province", EntityUtil.getFirst(province_list).getString("geoId"));
					// 获取市ID
					List<GenericValue> city_list = delegator.findByAnd("Geo",
							UtilMisc.toMap("geoName", city, "geoTypeId", "CITY"));
					business_map.put("city", EntityUtil.getFirst(city_list).getString("geoId"));
					// 获取区ID
					List<GenericValue> county_list = delegator.findByAnd("Geo",
							UtilMisc.toMap("geoName", county, "geoTypeId", "COUNTY"));
					business_map.put("county", EntityUtil.getFirst(county_list).getString("geoId"));
					business_map.put("address", address);
					business_map.put("tel", tel);
					business_map.put("legalPersonName", legalPersonName);

					// 获取商品类型id
					List<GenericValue> businessTypeList = delegator.findByAnd("Enumeration",
							UtilMisc.toMap("description", businessTypeName, "enumTypeId", "BUSINESS_TYPE"));
					business_map.put("businessType", EntityUtil.getFirst(businessTypeList).getString("enumId"));
					
					if ("是".equals(isUse)) {
						business_map.put("isUse", "PARTY_ENABLED");
					} else {
						business_map.put("isUse", "PARTY_DISABLED");
					}
					// 存放userLogin
					business_map.put("userLogin", userLogin);

					dispatcher.runSync("businessAdd", business_map);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
		} catch (GenericServiceException e) {
			e.printStackTrace();
		} finally {
			PrintWriter out = null;
			try {
				out = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			out.write(returnJson);
			out.flush();
			out.close();
		}
	}
}
