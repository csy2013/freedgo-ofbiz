package com.qihua.ofbiz.product.promo;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProProductService {
    /**
     * 根据选择商品编码取得商品信息详情
     * @return
     */
    public static Map<String, Object> getProductGoodById(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        // 关联商品信息
        Delegator delegator = dcx.getDelegator();
        String productId = (String)context.get("id");
        Map<String, Object> map = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        List<GenericValue> productPirceList = FastList.newInstance();//价格信息
        // 取得关联商品信息
        try {
            GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            BigDecimal salesPrice = new BigDecimal(0);//价格
            String productGoodFeature = "";//商品特征
            productPirceList = delegator.findList("ProductPrice", EntityCondition.makeCondition("productId", productInfo.get("productId")), null, orderBy, null, true);
            for (GenericValue genericValue : productPirceList) {
                if ("DEFAULT_PRICE".equals(genericValue.get("productPriceTypeId"))) {
                    salesPrice = genericValue.getBigDecimal("price");
                }
            }

            // 商品特征
            if(UtilValidate.isNotEmpty(productInfo)){
                if(UtilValidate.isNotEmpty(productInfo.getString("mainProductId"))){
                    if(UtilValidate.isNotEmpty(productInfo.getString("featureaProductId"))){
                        GenericValue productFeatureInfo=delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productInfo.getString("featureaProductId")));
                        if(UtilValidate.isNotEmpty(productFeatureInfo)){
                            GenericValue productFeatureTypeInfo=delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
                            if(UtilValidate.isNotEmpty(productFeatureTypeInfo)){
                                String productFeatureTypeName=productFeatureTypeInfo.getString("productFeatureTypeName");
                                String productFeatureName=productFeatureInfo.getString("productFeatureName");
                                productGoodFeature=productFeatureTypeName+":"+productFeatureName;
                            }
                        }
                    }
                }
            }

            // 商品图片
            String imgUrl="";// 商品图片
            // 根据商品ID获取商品图片url
            String productAdditionalImage1 = "";
            List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                    UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
            if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                imgUrl="/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId");
            }
            result.put("productName", productInfo.getString("productName"));
            result.put("productId", productInfo.getString("productId"));
            result.put("salesPrice", salesPrice);
            result.put("productGoodFeature", productGoodFeature);
            result.put("imgUrl", imgUrl);
            return result;

        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }

    }

}
