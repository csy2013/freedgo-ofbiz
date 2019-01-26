<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<!-- Select2 -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/dist/css/AdminLTE.min.css</@ofbizContentUrl>">
<!-- Date Picker -->
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<!-- Select2 -->
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<#assign curProductAdditionalImage1 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_1"})/>
<#assign curProductAdditionalImage2 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_2"})/>
<#assign curProductAdditionalImage3 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_3"})/>
<#assign curProductAdditionalImage4 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_4"})/>
<#assign curProductAdditionalImage5 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_5"})/>
     
<#if prod?has_content>
  
     <#if curProductAdditionalImage1?has_content>
     <#assign productAdditionalImage1 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_1", locale, dispatcher))?if_exists />
	<#else>
     <#assign productAdditionalImage1 =""/>
	</#if>

    <#if curProductAdditionalImage2?has_content>
     <#assign productAdditionalImage2 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_2", locale, dispatcher))?if_exists />
	<#else>
     <#assign productAdditionalImage2 =""/>
	</#if>
	

	<#if curProductAdditionalImage3?has_content>
     <#assign productAdditionalImage3 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_3", locale, dispatcher))?if_exists />
	<#else>
     <#assign productAdditionalImage3 =""/>
	</#if>

	<#if curProductAdditionalImage4?has_content>
     <#assign productAdditionalImage4 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_4", locale, dispatcher))?if_exists />
	<#else>
     <#assign productAdditionalImage4 =""/>
	</#if>

	<#if curProductAdditionalImage5?has_content>
     <#assign productAdditionalImage5 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_5", locale, dispatcher))?if_exists />
	<#else>
     <#assign productAdditionalImage5 =""/>
	</#if>

</#if>
<div class="box box-info">
	<div class="box-header with-border">
	  <h3 class="box-title">商品详情</h3>
	</div>
	<div class="box-body">
	    <form class="form-horizontal" method="post"  role="form" action="" name="updateProductGood"  id="updateProductGood"  class="" enctype="multipart/form-data">
		    <input id="productTypeId" type="hidden" name="productTypeId" value="${parameters.productTypeId?if_exists}" />
            <input id="productCategoryId" type="hidden" name="productCategoryId" value="${parameters.productCategoryId?if_exists}"/>
            <input type="hidden" name="operateType" id="operateType" value="${parameters.operateType?if_exists}"/>
            <input type="hidden" name="productAttrInfos" id="productAttrInfos" value=""/>
            <input type="hidden" name="productParameterInfos" id="productParameterInfos" value=""/>
            <input type="hidden" name="productFacilityInfos" id="productFacilityInfos" value=""/>
            <input type="hidden" name="productContentInfos" id="productContentInfos" value=""/>
            <input type="hidden" name="productAssocInfos" id="productAssocInfos" value=""/>
            <input type="hidden" id="pcDetailsContent"  value="${prod.pcDetails?if_exists}">
            <input type="hidden" id="mobileDetailsContent"  value="${prod.mobileDetails?if_exists}">
            <input type="hidden" id="productTags" name="productTags" value="">
            <input id="ids" type="hidden"/>
            
            
           <#if prod?has_content>
	         <div class="row">
				    <div class="form-group col-sm-6">
		                <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品编号:</label>
		                <div class="col-sm-9">
		                     <input type="text" class="form-control" id="productId" name="productId" value="${parameters.productId?if_exists}" readonly>
		                </div>
		            </div>
		            <div class="form-group col-sm-6">
		                <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品类型:</label>
		                <div class="col-sm-9">
		                     <input type="text" class="form-control" id="" name="" value="<#if prod.productTypeId?if_exists=="FINISHED_GOOD">实物商品<#else>虚拟商品</#if>" readonly>
		                </div>
		            </div>
	           </div>
	           
	           <div class="row title">
	                <div class="form-group col-sm-6" data-type="required" data-mark="商品名称">
	                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品名称:</label>
	                    <div class="col-sm-9">
	                        <input type="text" class="form-control dp-vd" id="productName" name="productName" value="${prod.productName?if_exists}">
	                        <p class="dp-error-msg"></p>
	                    </div>
	                </div>
	                <div class="form-group col-sm-6">
	                    <label for="subTitle" class="col-sm-3 control-label">商品描述:</label>
	                    <div class="col-sm-9" >
	                        <input type="text" class="form-control" id="productSubheadName" name="productSubheadName" value="${prod.productSubheadName?if_exists}">
	                    </div>
	                </div>
               </div>
               
               <div class="row">
				     <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
						 <#assign produtTypeTag="ProdutTypeTag_1"/>
					 <#elseif parameters.productTypeId?if_exists=="FINISHED_GOOD">
						 <#assign produtTypeTag="ProdutTypeTag_2"/>
					 </#if>
                   <#assign productTagList = delegator.findByAnd("Tag", {"tagTypeId" : "${produtTypeTag?if_exists}","isDel":"N"})/>

                  <#--<#assign productTagList = delegator.findByAnd("Tag", {"tagTypeId" : "ProdutTypeTag","isDel":"N"})/>-->
                  <#assign curCategoryInfo = delegator.findByPrimaryKey("ProductCategory", Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", "${prod.primaryProductCategoryId?if_exists}"))>
	              
                  <div class="form-group col-sm-6">
	                  <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品分类:</label>
	                  <div class="col-sm-9">
	                      <input type="text" class="form-control" id="primaryProductCategoryId" name="primaryProductCategoryId" value="<#if curCategoryInfo?has_content>${curCategoryInfo.categoryName?if_exists}</#if>" readonly>
	                  </div>
	              </div>
	              <div class="form-group col-sm-6">
	                  <label  class="col-sm-3 control-label">商品标签:</label>
	                  <div class="col-sm-9">
		                  <div class="checkbox clearfix">
		                    <#list productTagList as productTag>
		                         <label class="col-sm-3" title=""><input name="tag" id="${productTag.tagId}" value="${productTag.tagId}" type="checkbox">${productTag.tagName}</label>
		                    </#list>
		                  </div> 
	                   </div>
	               </div>
               </div>
	        
	            <div class="row">
		            <div class="form-group col-sm-6">
		                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>是否上架:</label>
		                <div class="col-sm-9">
		                   <#if prod.isVerify?if_exists=="Y">
		                                                     已上架
		                    <#else>
		                                                     未上架
		                    </#if>
		                </div>
		            </div>
	            </div>


			   <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
                   <div class="row isFinished">
                       <#--<div class="form-group col-sm-6">-->
                           <#--<label  class="col-sm-3 control-label">推荐到首页:</label>-->
                           <#--<div class="col-sm-9">-->
                               <#--<div class="radio">-->
								   <#--<#if prod.isRecommendHomePage?if_exists=="Y">-->
                                       <#--<label class="col-sm-6"><input name="isRecommendHomePage"  value="Y" type="radio" checked>是</label>-->
                                       <#--<label class="col-sm-6"><input name="isRecommendHomePage" value="N" type="radio" >否</label>-->
								   <#--<#else>-->
                                       <#--<label class="col-sm-6"><input name="isRecommendHomePage"  value="Y" type="radio">是</label>-->
                                       <#--<label class="col-sm-6"><input name="isRecommendHomePage" value="N" type="radio" checked>否</label>-->
								   <#--</#if>-->
                               <#--</div>-->
                               <#--<div class="dp-error-msg"></div>-->
                           <#--</div>-->
                       <#--</div>-->
                       <div class="form-group col-sm-6">
                           <label  class="col-sm-3 control-label">是否保税商品:</label>
                           <div class="col-sm-9">
                               <div class="radio">
								   <#if prod.isBondedGoods?if_exists=="Y">
                                       <label class="col-sm-6"><input name="isBondedGoods"  value="Y" type="radio" checked>是</label>
                                       <label class="col-sm-6"><input name="isBondedGoods" value="N" type="radio" >否</label>
								   <#else>
                                       <label class="col-sm-6"><input name="isBondedGoods"  value="Y" type="radio">是</label>
                                       <label class="col-sm-6"><input name="isBondedGoods" value="N" type="radio" checked>否</label>
								   </#if>
                               </div>
                               <div class="dp-error-msg"></div>
                           </div>
                       </div>

					   <div class="form-group col-sm-6" data-type="" data-mark="">
						   <label for="title" class="col-sm-3 control-label">供应商:</label>
						   <#assign providerInfos = delegator.findByAnd("Provider")/>
						   <div class="col-sm-9" >
							   <select class="form-control " id="providerId" name="providerId">
								   <option value=""></option>
								   <#list providerInfos as providerInfo>
									   <option value="${providerInfo.providerId}" <#if prod.providerId?if_exists==providerInfo.providerId>selected="selected"</#if>>${providerInfo.providerName}</option>
								   </#list>
							   </select>

							   <p class="dp-error-msg"></p>
						   </div>
					   </div>

                   </div>
			   </#if>
			   <#assign supportServiceTypeList = delegator.findByAnd("Enumeration",{"enumTypeId" : "SERVICE_SUPP_TYPE"})/>
			   <#if parameters.productTypeId?if_exists=="VIRTUAL_GOOD">
                   <div class="row isVirtual">
                       <#--<div class="form-group col-sm-6">-->
                           <#--<label  class="col-sm-3 control-label">推荐到首页:</label>-->
                           <#--<div class="col-sm-9">-->
                               <#--<div class="radio">-->
								   <#--<#if prod.isRecommendHomePage?if_exists=="Y">-->
                                       <#--<label class="col-sm-6"><input name="isRecommendHomePage"  value="Y" type="radio" checked>是</label>-->
                                       <#--<label class="col-sm-6"><input name="isRecommendHomePage" value="N" type="radio" >否</label>-->
								   <#--<#else>-->
                                       <#--<label class="col-sm-6"><input name="isRecommendHomePage"  value="Y" type="radio">是</label>-->
                                       <#--<label class="col-sm-6"><input name="isRecommendHomePage" value="N" type="radio" checked>否</label>-->
								   <#--</#if>-->
                               <#--</div>-->
                               <#--<div class="dp-error-msg"></div>-->
                           <#--</div>-->
                       <#--</div>-->
                       <#--<div class="form-group col-sm-6">-->
                           <#--<label  class="col-sm-3 control-label"><i class="required-mark">*</i>服务支持:</label>-->
                           <#--<div class="col-sm-9">-->
                               <#--<div class="checkbox clearfix">-->
                                   <#--<label class="col-sm-3" title=""><input name="isSupportService" id="isSupportService" value="1" type="checkbox" disabled="disabled">七天无理由退换货</label>-->
                               <#--</div>-->



							   <#--<#list supportServiceTypeList as supportServiceTypeInfo>-->
								   <#--<#if supportServiceTypeInfo.enumId?if_exists=="SERVICE_SUPP_7DAYS">-->
                                       <#--<label class="col-sm-5" title=""><input name="supportServiceType" id="${supportServiceTypeInfo.enumId}" value="${supportServiceTypeInfo.enumId}" type="checkbox">${supportServiceTypeInfo.description}</label>-->
								   <#--</#if>-->
							   <#--</#list>-->
                               <#--<div class="dp-error-msg"></div>-->
                           <#--</div>-->
                       <#--</div>-->
                           <div class="form-group col-sm-6" data-type="" data-mark="">
                               <label for="title" class="col-sm-3 control-label">供应商:</label>
							   <#assign providerInfos = delegator.findByAnd("Provider")/>
                               <div class="col-sm-9" >
                                   <select class="form-control " id="providerId" name="providerId">
                                       <option value=""></option>
									   <#list providerInfos as providerInfo>
                                           <option value="${providerInfo.providerId}" <#if prod.providerId?if_exists==providerInfo.providerId>selected="selected"</#if>>${providerInfo.providerName}</option>
									   </#list>
                                   </select>

                                   <p class="dp-error-msg"></p>
                               </div>
                           </div>

                   </div>
			   </#if>

			   <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
                   <div class="row isFinished">
                       <div class="form-group col-sm-6">
                           <label  class="col-sm-3 control-label"><i class="required-mark">*</i>服务支持:</label>
                           <div class="col-sm-9">
                               <#--<div class="checkbox clearfix">-->
								   <#list supportServiceTypeList as supportServiceTypeInfo>
                                       <label class="col-sm-5" title=""><input name="supportServiceType" id="supportServiceType" value="${supportServiceTypeInfo.enumId}" type="checkbox">${supportServiceTypeInfo.description}</label>
								   </#list>
							   <#--&lt;#&ndash;<#if prod.supportServiceType?if_exists=="1">-->
							   <#--<label class="col-sm-3" title=""><input name="supportServiceType" id="supportServiceType" value="1" type="checkbox" checked="checked">七天无理由退换货</label>-->
							   <#--<label class="col-sm-3" title=""><input name="supportServiceType" id="supportServiceType" value="2" type="checkbox">包邮</label>-->

							   <#--<#elseif prod.supportServiceType?if_exists=="2">-->
							   <#--<label class="col-sm-3" title=""><input name="supportServiceType" id="supportServiceType" value="1" type="checkbox">七天无理由退换货</label>-->
							   <#--<label class="col-sm-3" title=""><input name="supportServiceType" id="supportServiceType" value="2" type="checkbox" checked="checked">包邮</label>-->
							   <#--<#else>-->
							   <#--<label class="col-sm-3" title=""><input name="supportServiceType" id="supportServiceType" value="1" type="checkbox">七天无理由退换货</label>-->
							   <#--<label class="col-sm-3" title=""><input name="supportServiceType" id="supportServiceType" value="2" type="checkbox">包邮</label>-->
							   <#--</#if>&ndash;&gt;-->
                               <#--</div>-->
                               <div class="dp-error-msg"></div>
                           </div>
                       </div>
                       <div class="form-group col-sm-6">
                       </div>
                   </div>
			   </#if>


	            <div class="row">
	                <div class="form-group col-sm-6" data-type="linkLt" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间">
	                    <label for="startTime" class="col-sm-3 control-label">销售开始时间:</label>
	                    <div class="col-sm-9">
		                    <div class="input-group date form_datetime col-sm-12" data-link-field="startTime">
		                        <input class="form-control" size="16" type="text" value="<#if prod.introductionDate?has_content >${prod.introductionDate?string('yyyy-MM-dd HH:mm:ss')}</#if>" readonly>
		                        <input id="startTime" name="startTime" class="dp-vd" type="hidden">
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
		                    </div>
		                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
	                    </div>
	                </div>
	                <#---
	                <div class="form-group col-sm-6" data-type="linkLt" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间">
	                    
	                    <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：商品在销售时间内才可以下单销售</div></div>
	                   
	                </div>
	                 -->
	                <div id="endTimeGroup " class="form-group col-sm-6">
	                    <label for="endTime" class="col-sm-3 control-label">销售结束时间:</label>
	                    <div class="col-sm-9">
		                    <div class="input-group date form_datetime col-sm-12"  data-link-field="endTime">
		                        <input class="form-control" size="16" type="text"  value="<#if prod.salesDiscontinuationDate?has_content>${prod.salesDiscontinuationDate?string('yyyy-MM-dd HH:mm:ss')}</#if>" readonly>
		                        <input id="endTime" name="endTime" class="dp-vd" type="hidden">
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
		                    </div>
		                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
	                    </div>
	                </div>
	            </div>
	            <#---
	            
	            <div class="row">
	                <div id="endTimeGroup " class="form-group col-sm-6">
	                    <label for="endTime" class="col-sm-3 control-label">销售结束时间:</label>
	                    <div class="col-sm-9">
		                    <div class="input-group date form_datetime col-sm-12"  data-link-field="endTime">
		                        <input class="form-control" size="16" type="text"  value="<#if prod.salesDiscontinuationDate?has_content>${prod.salesDiscontinuationDate?string('yyyy-MM-dd HH:mm:ss')}</#if>" readonly>
		                        <input id="endTime" name="endTime" class="dp-vd" type="hidden">
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
		                    </div>
		                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
	                    </div>
	                </div>
	            </div>
                 -->
	            <div class="row title">
	                <#--<div class="form-group col-sm-6" data-type="required" data-mark="商家名称">-->
	                     <#--<#assign mrchantNameList = delegator.findByAnd("GetMrchantNameList",{"auditStatus" : "1","statusId":"PARTY_ENABLED"})/>-->
	                    <#--<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商家名称:</label>-->
	                    <#--<div class="col-sm-6">-->
	                          <#--<select class="form-control select2MrchantName dp-vd" id="businessPartyId" name="businessPartyId">-->
			                      <#--<#if prod.businessPartyId?has_content>-->
				                        <#--<#list mrchantNameList as mrchantNameInfo>-->
					                        <#--<#if prod.businessPartyId==mrchantNameInfo.partyId>-->
			                                 <#--<option value="${mrchantNameInfo.partyId}" selected="selected">${mrchantNameInfo.businessName}</option>-->
			                                <#--<#else>-->
			                                  <#--<option value="${mrchantNameInfo.partyId}">${mrchantNameInfo.businessName}</option>-->
			                                <#--</#if>-->
		                                <#--</#list>-->
			                      <#--</#if>-->
		                    <#--</select>-->
		                    <#--&lt;#&ndash;-->
	                        <#--<input type="text" class="form-control dp-vd" id="businessPartyId" name="businessPartyId" value="${prod.businessPartyId?if_exists}">-->
	                        <#--&ndash;&gt;-->
	                        <#--<p class="dp-error-msg"></p>-->
	                    <#--</div>-->
	                <#--</div>-->

					<div class="form-group col-sm-6" data-type="required" data-mark="主营分类">
						<#if prod.platformClassId?has_content>
							<#assign platformClassInfo = delegator.findByPrimaryKey("ProductCategory", Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", "${prod.platformClassId?if_exists}"))>
						</#if>

						<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>主营分类:</label>
						<div class="col-sm-9">
							<input type="text" class="form-control dp-vd" id="platformClassId" name="platformClassId" data-id="${prod.platformClassId?if_exists}" value="<#if platformClassInfo?has_content>${platformClassInfo.categoryName?if_exists}</#if>">
							<p class="dp-error-msg"></p>
						</div>
					</div>
	                <div class="form-group col-sm-6">
	                    <#assign productBrandList = delegator.findByAnd("ProductBrand")/>
	                    <label for="subTitle" class="col-sm-3 control-label">商品品牌:</label>
	                    <div class="col-sm-9" >
	                        <select class="form-control" id="brandId" name="brandId">
                                <option value=""></option>
	                             <#list productBrandList as productBrand>
	                                 <#if prod.brandId?has_content>
		                                 <#if prod.brandId==productBrand.productBrandId>
		                                     <option value="${productBrand.productBrandId}" selected="selected">${productBrand.brandName}</option>
		                                 <#else>
		                                     <option value="${productBrand.productBrandId}">${productBrand.brandName}</option>
		                                 </#if>
		                            <#else>
		                                <option value="${productBrand.productBrandId}">${productBrand.brandName}</option>     
	                                </#if>
	                             </#list>
	                        </select>
	                    </div>
					</div>
	            </div>
            
	           <div class="row">
	                <#assign productPriceSaleList = delegator.findByAnd("ProductPrice", {"productId" : "${prod.productId?if_exists}","productPriceTypeId":"DEFAULT_PRICE"})/>
	                <#if productPriceSaleList?has_content>
	                	<#assign priceSaleInfo = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(productPriceSaleList) />
	                	<#if priceSaleInfo?has_content>
	                	   <#assign priceSale =priceSaleInfo.price?if_exists/>
	                	</#if>
	                </#if>
	                
	                <div class="form-group col-sm-6" data-type="required" data-mark="销售价格">
	                    <label  class="col-sm-3 control-label"><i class="required-mark">*</i>销售价格(元):</label>
	                    <div class="col-sm-9">
		                    <input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="${priceSale?if_exists}" placeholder="" />
		                    <div class="dp-error-msg"></div>
	                    </div>
	                </div>
	                 <div class="form-group col-sm-6">
	                    <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：使用规格时，该价格作为商品的参考价展示</div></div>
	                </div>
	            </div>


			   <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
                   <div class="row title isFinished">
                       <div class="form-group col-sm-6" data-type="required" data-mark="积分抵扣">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>积分抵扣:</label>
                           <div class="col-sm-9" >
                               <select class="form-control  dp-vd" id="integralDeductionType" name="integralDeductionType">
                                   <option value="1" <#if prod.integralDeductionType=='1'>selected</#if>>不可使用积分</option>
                                   <option value="2" <#if prod.integralDeductionType=='2'>selected</#if>>百分比抵扣</option>
                                   <option value="3" <#if prod.integralDeductionType=='3'>selected</#if>>固定金额抵扣</option>
                               </select>

                               <p class="dp-error-msg"></p>
                           </div>
                       </div>

                       <div class="form-group col-sm-6 js-integral-upper" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="积分抵扣上限">
                           <label for="title" class="col-sm-3 control-label">积分抵扣上限:</label>
                           <div class="col-sm-8">
                               <input type="text" class="form-control dp-vd" id="integralDeductionUpper" name="integralDeductionUpper" value="${prod.integralDeductionUpper?if_exists}"" placeholder="" />
                               <p class="dp-error-msg"></p>
                           </div>
                           <label for="title" class="col-sm-1 js-integral-upper-name control-label"></label>
                       </div>
                   </div>


                   <div class="row title isFinished">
                       <div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="每人限购数量">
                           <label for="title" class="col-sm-3 control-label">每人限购数量:</label>
                           <div class="col-sm-9" >
                               <input type="text" class="form-control dp-vd" id="purchaseLimitationQuantity" name="purchaseLimitationQuantity" value="${prod.purchaseLimitationQuantity?if_exists}" placeholder="" />
                               <p class="dp-error-msg"></p>
                           </div>
                       </div>
                       <div class="form-group col-sm-6" >
                           <label for="subTitle" class="col-sm-3 control-label" >列表展示:</label>
                           <div class="col-sm-9">
                               <div class="radio">
								   <#if prod.isListShow?if_exists=='Y'>
                                       <label class="col-sm-6"><input name="isListShow"  value="Y" type="radio" checked>是</label>
                                       <label class="col-sm-6"><input name="isListShow" value="N" type="radio" >否</label>
								   <#else>
                                       <label class="col-sm-6"><input name="isListShow"  value="Y" type="radio">是</label>
                                       <label class="col-sm-6"><input name="isListShow" value="N" type="radio" checked>否</label>
								   </#if>
                               </div>
                               <div class="dp-error-msg"></div>
                           </div>
                       </div>
                   </div>

			   </#if>
			   <#if parameters.productTypeId?if_exists=="VIRTUAL_GOOD">
                   <div class="row title isVirtual">
                       <div class="form-group col-sm-6" data-type="required" data-mark="积分抵扣">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>积分抵扣:</label>
                           <div class="col-sm-9" >
                               <select class="form-control  dp-vd" id="integralDeductionType" name="integralDeductionType" readonly="readonly">
                                   <option value=""></option>
                                   <option value="1" selected="selected">不可使用积分</option>
                                   <option value="2">百分比抵扣</option>
                                   <option value="3">固定金额抵扣</option>
                               </select>

                               <p class="dp-error-msg"></p>

                           </div>
                       </div>

                       <div class="form-group col-sm-6 js-integral-upper" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="积分抵扣上限" style="display: none">
                           <label for="title" class="col-sm-3 control-label">积分抵扣上限:</label>
                           <div class="col-sm-9">
                               <input type="text" class="form-control dp-vd" id="integralDeductionUpper" name="integralDeductionUpper" value="${prod.integralDeductionUpper?if_exists}" placeholder="" />
                               <p class="dp-error-msg"></p>
                           </div>
                       </div>
                   </div>

                   <div class="row title isVirtual">
                       <div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="每人限购数量">
                           <label for="title" class="col-sm-3 control-label">每人限购数量:</label>
                           <div class="col-sm-9" >
                               <input type="text" class="form-control dp-vd" id="purchaseLimitationQuantity" name="purchaseLimitationQuantity" value="${prod.purchaseLimitationQuantity?if_exists}" placeholder="" />
                               <p class="dp-error-msg"></p>
                           </div>
                       </div>
                       <div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="每人限购数量">
                           <label for="title" class="col-sm-3 control-label">代金券面额:</label>
                           <div class="col-sm-9" >
                               <input type="text" class="form-control dp-vd" id="voucherAmount" name="voucherAmount" value="${prod.voucherAmount?if_exists}" placeholder="" />
                               <p class="dp-error-msg"></p>
                           </div>
                       </div>

                   </div>

                   <div class="row title isVirtual">
                       <div class="form-group col-sm-6" >
                           <label for="subTitle" class="col-sm-3 control-label" >列表展示:</label>
                           <div class="col-sm-9">
                               <div class="radio">
								   <#if prod.isListShow?if_exists=='Y'>
                                       <label class="col-sm-6"><input name="isListShow"  value="Y" type="radio" checked>是</label>
                                       <label class="col-sm-6"><input name="isListShow" value="N" type="radio" >否</label>
								   <#else>
                                       <label class="col-sm-6"><input name="isListShow"  value="Y" type="radio">是</label>
                                       <label class="col-sm-6"><input name="isListShow" value="N" type="radio" checked>否</label>
								   </#if>
                               </div>
                               <div class="dp-error-msg"></div>
                           </div>
                       </div>
                       <#--<div class="form-group col-sm-6" >-->
                           <#--<label for="subTitle" class="col-sm-3 control-label" >使用限制:</label>-->
                           <#--<div class="col-sm-9">-->

                               <#--<div class="radio">-->
								   <#--<#if prod.useLimit=='Y'>-->
                                       <#--<label class="col-sm-6"><input name="useLimit"  value="Y" type="radio" checked>是</label>-->
                                       <#--<label class="col-sm-6"><input name="useLimit" value="N" type="radio" >否</label>-->
								   <#--<#else>-->
                                       <#--<label class="col-sm-6"><input name="useLimit"  value="Y" type="radio">是</label>-->
                                       <#--<label class="col-sm-6"><input name="useLimit" value="N" type="radio" checked>否</label>-->
								   <#--</#if>-->
                               <#--</div>-->
                               <#--<div class="dp-error-msg"></div>-->

                           <#--</div>-->
                       <#--</div>-->
                   </div>


                   <div class="row isVirtual">

                       <div class="form-group col-sm-6 js-startTime" data-type="" data-compare-link="useEndTimeGroup" data-mark="使用开始时间" data-compare-mark="使用结束时间">

                           <label for="startTime" class="col-sm-3 control-label">使用开始时间:</label>
                           <div class="col-sm-9">
                               <div class="input-group date form_datetime col-sm-12" data-link-field="useStartTime">
                                   <input class="form-control" size="16" type="text" value="<#if prod.useStartTime?has_content >${prod.useStartTime?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>" readonly>
                                   <input id="useStartTime" name="useStartTime" class="dp-vd" value="<#if prod.useStartTime?has_content >${prod.useStartTime?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>" type="hidden">
                                   <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                                   <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
                               </div>
                               <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                           </div>
                       </div>

                       <div id="useEndTimeGroup" class="form-group col-sm-6">
                           <label for="useEndTime" class="col-sm-3 control-label">使用结束时间:</label>
                           <div class="col-sm-9">
                               <div class="input-group date form_datetime col-sm-12"  data-link-field="useEndTime">
                                   <input class="form-control" size="16" type="text" value="<#if prod.useEndTime?has_content >${prod.useEndTime?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>" readonly>
                                   <input id="useEndTime" name="useEndTime" class="dp-vd" value="<#if prod.useEndTime?has_content >${prod.useEndTime?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>"type="hidden">
                                   <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                                   <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
                               </div>
                               <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                           </div>
                       </div>
                   </div>
			   </#if>

	            <div class="row">
	                <#assign productPriceCostList = delegator.findByAnd("ProductPrice", {"productId" : "${prod.productId?if_exists}","productPriceTypeId":"COST_PRICE"})/>
	                <#assign productPriceMarketList = delegator.findByAnd("ProductPrice", {"productId" : "${prod.productId?if_exists}","productPriceTypeId":"MARKET_PRICE"})/>
	                <#if productPriceCostList?has_content>
	                	<#assign priceCostInfo = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(productPriceCostList) />
	                	<#if priceCostInfo?has_content>
	                	   <#assign priceCost =priceCostInfo.price?if_exists/>
	                	</#if>
	                </#if>
	                
	                <#if productPriceMarketList?has_content>
	                	<#assign priceMarketInfo = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(productPriceMarketList) />
	                	<#if priceMarketInfo?has_content>
	                	   <#assign priceMarket =priceMarketInfo.price?if_exists/>
	                	</#if>
	                </#if>
	                
		            <div class="form-group col-sm-6">
		                <label  class="col-sm-3 control-label">市场价格(元):</label>
		                <div class="col-sm-9">
		                    <input type="text" class="form-control" id="marketPrice" name="marketPrice" value="${priceMarket?if_exists}" placeholder="" />
		                </div>
		                
		            </div>
		             <div class="form-group col-sm-6">
		                
		                <label  class="col-sm-3 control-label">成本价格(元):</label>
		                <div class="col-sm-9">
		                    <input type="text" class="form-control" id="costPrice" name="costPrice" value="${priceCost?if_exists}" placeholder="" />
		                </div>
		            </div>
	            </div>
	            
	            
	            <div class="row">
		            <div class="form-group col-sm-6">
		                <label  class="col-sm-3 control-label">体积(m³):</label>
		                <div class="col-sm-9">
		                    <input type="text" class="form-control" id="volume" name="volume" value="${prod.volume?if_exists}" placeholder="" />
		                </div>
		                
		            </div>
		             <div class="form-group col-sm-6">
		                <label  class="col-sm-3 control-label">重量(kg):</label>
		                <div class="col-sm-9">
		                    <input type="text" class="form-control" id="weight" name="weight" value="${prod.weight?if_exists}" placeholder="" />
		                </div>
		            </div>
	            </div>
            
            <div class="row">
		            <div class="form-group col-sm-6">
		                <label  class="col-sm-3 control-label">商品属性:</label>
		            </div>
	            </div>
            <div class="row">
                <#assign productCategoryAttributeList = delegator.findByAnd("ProductCategoryAttribute", {"productCategoryId" : "${prod.primaryProductCategoryId?if_exists}"})/>
                
                <#assign listSize=productCategoryAttributeList.size()/>
                <#assign aa=listSize/3/>
                <#assign bb=listSize%3/>
                <#assign cc=0/>
                <#if bb gt 0>
                    <#assign cc=aa?int+1/>
                </#if>
                
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"></label>
	                <div class="col-sm-8">
	                    <table class="table table-bordered table_b_c js-table_2">
					      <tbody>
					          <#if listSize gt 0>
						          <#list 0..cc-1 as k>
								    <tr> 
								    <#list k*3..(k+1)*3-1 as ff>
								       <#list productCategoryAttributeList as productCategoryAttribute>
				                          <#if ff==productCategoryAttribute_index>
				                              <#if productCategoryAttribute.isRequired=="Y">
					                              <td><i class="required-mark">*</i>${productCategoryAttribute.attrName}<input type="hidden" name="" id="" value="${productCategoryAttribute.productCategoryId}"></td>
										          <td>
										                <#assign ProductCategoryattributeAssocList = delegator.findByAnd("ProductCategoryattributeAssoc", {"productId":"${prod.productId?if_exists}","productCategoryId":productCategoryAttribute.productCategoryId,"attrName":productCategoryAttribute.attrName})/>
										                <#assign productOptionList = delegator.findByAnd("ProductOption", {"productCategoryId" : "${prod.primaryProductCategoryId?if_exists}","attrName":productCategoryAttribute.attrName})/>
										                <select class="form-control">
										                 <option value=""></option>
										                <#list productOptionList as productOption>
										                	<#if ProductCategoryattributeAssocList?has_content>
										                        <#assign curOptionName = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(ProductCategoryattributeAssocList) />
										                        <#if curOptionName.productOptionId==productOption.productOptionId>
										                           <option value="${productOption.productOptionId}" selected="selected">${productOption.optionName}</option>
										                        <#else>
										                           <option value="${productOption.productOptionId}">${productOption.optionName}</option>
										                        </#if>
										                    <#else>
										                       	<option value="${productOption.productOptionId}">${productOption.optionName}</option>
										                    </#if>
										                </#list>
					                                    </select>
										          </td>
				                              <#else>
					                              <td>${productCategoryAttribute.attrName}<input type="hidden" name="" id="" value="${productCategoryAttribute.productCategoryId}"></td>
										          <td>
										                <#assign ProductCategoryattributeAssocList = delegator.findByAnd("ProductCategoryattributeAssoc", {"productId":"${prod.productId?if_exists}","productCategoryId":productCategoryAttribute.productCategoryId,"attrName":productCategoryAttribute.attrName})/>
										                <#assign productOptionList = delegator.findByAnd("ProductOption", {"productCategoryId" : "${prod.primaryProductCategoryId?if_exists}","attrName":productCategoryAttribute.attrName})/>
										                <select class="form-control">
										                 <option value=""></option>
										                <#list productOptionList as productOption>
										                	<option value="${productOption.productOptionId}">${productOption.optionName}</option>
										                	<#if ProductCategoryattributeAssocList?has_content>
										                        <#assign curOptionName = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(ProductCategoryattributeAssocList) />
										                        <#if curOptionName.productOptionId==productOption.productOptionId>
										                           <option value="${productOption.productOptionId}" selected="selected">${productOption.optionName}</option>
										                        <#else>
										                           <option value="${productOption.productOptionId}">${productOption.optionName}</option>
										                        </#if>
										                    <#else>
										                       	<option value="${productOption.productOptionId}">${productOption.optionName}</option>
										                    </#if>
										                </#list>
					                                    </select>
										          </td>
									          </#if>
				                          </#if>
				                       </#list>
								    </#list>
								    </tr>
								  </#list>
							  </#if>
					      </tbody>
		    		  </table>
	                </div>
	            </div>
            </div>
            
            <div class="row">
	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label">商品参数:</label>
	                <div class="col-sm-9">
	                    <button type="button" class="btn btn-primary js-addpara" id="btnAddProductParameter">添加参数</button>
	                </div>
	            </div>
            </div>
            <div class="row">
                <div class="form-group ">
                   <label  class="col-sm-2 control-label"></label>
                   <div class="col-sm-8">
                      <table class="table table-bordered table_b_c js-table_1">
					      <thead>
					           <tr>
					               <th>参数名称</th><th>参数详情</th><th>${uiLabelMap.BrandOption}</th>
					           </tr>
					      </thead>
					      <tbody>
					            <#assign productParameterList = delegator.findByAnd("ProductParameter", {"productId" : "${prod.productId?if_exists}"})/>
					            <#list productParameterList as productParameter>
					            <tr>
					                <td><input type="text" name="" id="" value="${productParameter.parameterName?if_exists}">
					                <input type="hidden" name="" id="" value="${productParameter.productParameterId?if_exists}"></td> 
					                <td><input type="text" name="" id="" value="${productParameter.parameterDescription?if_exists}"></td>
					                <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>
					            </tr>
					            </#list>
					      </tbody>
		    		  </table>
                   </div>
                </div>
            </div>

		   <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
            <div class="row">
	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>是否使用规格:</label>
	                <div class="col-sm-9">
	                    <div class="radio">
	                        <label class="col-sm-6"><input name="isUsedFeature" value="N" type="radio" <#if prod.isUsedFeature=='N'>checked</#if>>直接创建商品</label>
                            <label class="col-sm-6"><input name="isUsedFeature" value="Y" type="radio" <#if prod.isUsedFeature=='Y'>checked</#if>>使用规格</label>
	                    </div>
	                    <div class="dp-error-msg"></div>
	                </div>
	                 
	            </div>
            </div>
		    </#if>
            
            
            <div class="row js-feature-div">
	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>规格名称:</label>
	                <div class="col-sm-9">
	                    <#assign productFeatureTypeList = delegator.findByAnd("ProductFeatureType")/>
	                    <select class="form-control select2Feature" multiple="multiple" data-placeholder="Select a State">
	                      <#list productFeatureTypeList as productFeatureType>
	                          <option value="${productFeatureType.productFeatureTypeId?if_exists}">${productFeatureType.productFeatureTypeName?if_exists}</option>
	                      </#list>
	                    </select>
	                </div>
	            </div>
            </div>
            
             <#--规格值-->
            <div class="row js-feature-div">
               <div class="form-group">
               <label  class="col-sm-2 control-label"></label>
                <div class="col-sm-8 js-feature">
                 
                </div>
               </div>
            </div>
            <div class="js-facility">
	            <div class="row">
		            <div class="form-group col-sm-6">
		                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品库存:</label>
		                <div class="col-sm-9">
		                    <button type="button" class="btn btn-primary" id="btnAddProductFacility">选择仓库</button>
		                </div>
		            </div>
	            </div>
	            
	            <div class="row">
	                <div class="form-group">
	                   <label  class="col-sm-2 control-label"></label>
	                   <div class="col-sm-8">
	                      <table class="table table-bordered table_b_c js-table_3">
						      <thead>
						           <tr>
						               <th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>预警提醒人邮箱</th><th>${uiLabelMap.BrandOption}</th>
						           </tr>
						      </thead>
						      <tbody>
						      </tbody>
			    		  </table>
	                   </div>
	                </div>
	            </div>
            </div>
            <div class="row">
	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品图片:</label>
	                <div class="col-sm-9">
	                    <button type="button" class="btn btn-primary" id="btnAddProductContent">添加图片</button>
	                </div>
	            </div>
	            <div class="form-group col-sm-6">
	                <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：最多可添加5张照片，第一张图为主图，请保证所有图片比例一致。大小为640*640</div></div>
	            </div>
            </div>
            
            <div class="row">
                <div class="form-group col-sm-6">
                    <label  class="col-sm-2 control-label"></label>
				    <div class="col-sm-2 js-img1">
					     <img height="150" alt="" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>" id="img1" style="height:100px;width:100px;"> 
				    </div>
				    <div class="col-sm-2 js-img2">
				         <img height="150" alt="" src="<#if productAdditionalImage2?has_content><@ofbizContentUrl>${productAdditionalImage2}</@ofbizContentUrl></#if>" id="img2" style="height:100px;width:100px;"> 
				    </div>
				    <div class="col-sm-2 js-img3">
				          <img height="150" alt="" src="<#if productAdditionalImage3?has_content><@ofbizContentUrl>${productAdditionalImage3}</@ofbizContentUrl></#if>" id="img3" style="height:100px;width:100px;">
					      
				    </div>
				    <div class="col-sm-2 js-img4">
				          <img height="150" alt="" src="<#if productAdditionalImage4?has_content><@ofbizContentUrl>${productAdditionalImage4}</@ofbizContentUrl></#if>" id="img4" style="height:100px;width:100px;">
					    
				    </div>
				    <div class="col-sm-2 js-img5">
				          <img height="150" alt="" src="<#if productAdditionalImage5?has_content><@ofbizContentUrl>${productAdditionalImage5}</@ofbizContentUrl></#if>" id="img5" style="height:100px;width:100px;">
				    </div>
                </div>
            </div>


               <#if parameters.productTypeId?if_exists=="VIRTUAL_GOOD">
            <div class="row">
	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>关联商品:</label>
	                <div class="col-sm-9">
	                    <button type="button" class="btn btn-primary" id="btnAddProductAssocGood">添加关联商品</button>
	                </div>
	            </div>
            </div>
               </#if>
            
             <div class="row">
	            <div class="form-group col-sm-6">
	               <label for="seo" class="col-sm-3 control-label">搜索关键字:</label>
	            </div>
            </div>
            
            <div class="row">
                <div class="form-group">
                   <label  class="col-sm-1 control-label"></label>
                   <div class="col-sm-8">
                       <textarea class="form-control" name="seoKeyword" id="seoKeyword" rows="6" value="" readonly="readonly">${prod.seoKeyword?if_exists}</textarea>
                   </div>
                </div>
            </div>
            
            
            <div class="row js-pcdetails">
	            <div class="form-group col-sm-6">
	               <label for="seo" class="col-sm-3 control-label">PC端详情:</label>
	            </div>
            </div>
            <div class="row js-pcdetails">
                <div class="form-group">
                   <label  class="col-sm-1 control-label"></label>
                   <div class="col-sm-8">
                        <textarea id="pcDetails" name="pcDetails" rows="6" cols="80" value="" >
                        </textarea>
                   </div>
                </div>
            </div>
            
            
            <div class="row">
	            <div class="form-group col-sm-6">
	               <label for="seo" class="col-sm-3 control-label">移动端详情:</label>
	            </div>
            </div>
            
            <div class="row">
            <div class="form-group">
               <label  class="col-sm-1 control-label"></label>
               <div class="col-sm-8">
                    <textarea id="mobileDetails" name="mobileDetails" rows="6" cols="80" value="" >
                    </textarea>
               </div>
            </div>
            
            
            </#if>
	       <div class="modal-footer">
	            <button type="button" class="btn btn-primary" id="btnReturn">返回</button>
	            <#if prod.isUsedFeature=="Y">
	            <button type="button" class="btn btn-primary" id="btnGoodsList">商品列表</button>
	            </#if>
	            <button type="button" class="btn btn-primary" id="btnProductVerifyPass">审核通过</button>
	            <button type="button" class="btn btn-primary" id="btnProductVerifyNoPass">审核不通过</button>
	       </div>
	   </form>
    </div><!-- /.box-body -->
</div>

    <!-- 审核确认弹出框start -->
	<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
	  <input type="hidden" name="verifyStatus" id="verifyStatus" value=""/>
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
	      </div>
	      <div class="modal-body">
	        <h4 id="modal_confirm_body"></h4>
	      </div>
	      <div class="modal-footer">
	      	<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
	        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
	      </div>
	    </div>
	  </div>
	</div><!-- 审核确认弹出框end -->	


<script language="JavaScript" type="text/javascript">
  $(function(){
     CKEDITOR.replace("pcDetails");
     CKEDITOR.replace("mobileDetails");
     CKEDITOR.instances.pcDetails.setData($('#pcDetailsContent').val());
     CKEDITOR.instances.mobileDetails.setData($('#mobileDetailsContent').val());
     getSelectedTag();//标签选择初始化
      getSelectedSupService();// 支持服务初始化
     getFacilityByProductId('${parameters.productId?if_exists}'); //仓库信息初始化
     
     $("select").attr("disabled","disabled");
     $("input").attr("disabled","disabled");
     
      $(".select2Feature").select2({
	        tags: true,
	        maximumSelectionLength: 3  //最多能够选择的个数
	 });
     
     CKEDITOR.on('instanceReady', function (event) {
        editor=event.editor;
        editor.setReadOnly(true); //只读
     });
     
     $(".js-img1").hide();
     $(".js-img2").hide();
     $(".js-img3").hide();
     $(".js-img4").hide();
     $(".js-img5").hide();
     
     $(".js-pcdetails").hide();// 隐藏PC端详情的内容
     
      <#if prod?has_content>
	       <#if prod.isUsedFeature=="Y">
               $('.js-facility').hide();
	           $('.js-feature-div').show();
	           $('.select2Feature').attr("disabled","disabled");
	        <#else>
	             $('.js-facility').show();
		         $('.js-feature-div').hide();
	        </#if>
	      // 初始化商品规格
	      <#assign getFeatureIdListForProductIdList = delegator.findByAnd("GetFeatureIdListForProductId",{"productId" : "${prod.productId?if_exists}"})/>
	      var ids="";
	      var typeName="";
	      var productTypeIdTemp="";
	      <#list getFeatureIdListForProductIdList as featureType>
	          var curTypeId="${featureType.productFeatureTypeId}";
	          var curTypeName="${featureType.productFeatureTypeName}";
	          //$('.select2Feature').val([curTypeId]).trigger("change");
	          <#assign featureList = delegator.findByAnd("GetFeatureIdList",{"productId" : "${prod.productId?if_exists}","productFeatureTypeId":"${featureType.productFeatureTypeId}"})/>
	          var featureIds="";
	          <#list featureList as featureInfo>
	             featureIds=featureIds+"${featureInfo.productFeatureId}"+",";
	          </#list>
	           <#if featureType_index!=getFeatureIdListForProductIdList.size()-1>
	             productTypeIdTemp=productTypeIdTemp+curTypeId+",";
	           <#else> 
	             productTypeIdTemp=productTypeIdTemp+curTypeId;
	           </#if>
	          //alert(featureIds.substr(0,featureIds.length-1));
	          getProductFeatureListById(curTypeId,curTypeName,featureIds.substr(0,featureIds.length-1));
	      </#list>
	      $(".select2Feature").val(productTypeIdTemp.split(',')).trigger("change");
	     var productNames="";
	     
	     <#assign productNameList = delegator.findByAnd("Product",{"mainProductId" : "${parameters.productId?if_exists}"})/>
	     <#if productList?has_content>
	        <#list productList as productInfo>
	           var aa= "${productInfo.featureProductName?if_exists}";
	           productNames=productNames+","+aa;
	        </#list>
	     </#if>
	     $("#productFeatureNames").val(productNames.substr(1,productNames.length));
	      <#if productAdditionalImage1?if_exists != "">
	          $(".js-img1").show();
	      </#if>
	      <#if productAdditionalImage2?if_exists != "">
	           $(".js-img2").show();
	      </#if>
	      <#if productAdditionalImage3?if_exists != "">
	           $(".js-img3").show();
	      </#if>
	      <#if productAdditionalImage4?if_exists != "">
	           $(".js-img4").show();
	      </#if>
	      <#if productAdditionalImage5?if_exists != "">
	           $(".js-img5").show();
	      </#if>
     </#if>
     // 返回按钮的操作
     $("#btnReturn").click(function(){
         document.location.href="<@ofbizUrl>findProductGoodForVerify</@ofbizUrl>";
     })
     // 审核通过处理
     $("#btnProductVerifyPass").click(function(){
         //设置审核弹出框内容
		 var id="${parameters.productId?if_exists}";
		 updateVerifyPass(id);
     })
     
     // 审核不通过处理
     $("#btnProductVerifyNoPass").click(function(){
         //设置审核弹出框内容
		 var id="${parameters.productId?if_exists}";
		 updateVerifyPass(id);
     })
     
    //更新审核状态弹出框按钮点击事件
    $('#modal_confirm #ok').click(function(e){
        var verifyStatus=$('#modal_confirm').find('input[name=verifyStatus]').val();
        if(verifyStatus='Y'){
        updateProductVerifyStatusByIds('Y');
        }else{
        updateProductVerifyStatusByIds('N');
        }
    });
    // 查看商品列表    
    $("#btnGoodsList").click(function(){
         document.location.href="<@ofbizUrl>viewProductFeatureGoodForVerify?productId="+${parameters.productId?if_exists}+"</@ofbizUrl>";
    })
  })
  
  //查看审核通过按钮事件
  function updateVerifyPass(id){
	  $("#ids").val(id);
	  //设置审核弹出框内容
	  $('#modal_confirm #modal_confirm_body').html("审核通过后，商品在商城中可以销售！");
	  $('#modal_confirm').find('input[name=verifyStatus]').val('Y');
	  $('#modal_confirm').modal('show');
  }
  //查看审核不通过按钮事件
  function updateVerifyNoPass(id){
	  $("#ids").val(id);
	  //设置审核弹出框内容
	  $('#modal_confirm #modal_confirm_body').html("审核不通过，商品将不能上架！");
	  $('#modal_confirm').find('input[name=verifyStatus]').val('N');
	  $('#modal_confirm').modal('show');
  }  
  
  
  
   // 取得商品库存信息
   function getFacilityByProductId(productId){
	  if (productId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getFacilityByProductId</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productId' : productId
	        },
	        success: function(data) {
	           var facilityInfoList=data.facilityInfoList;
	           //alert(facilityInfoList);
	           for (var i=0;i<facilityInfoList.length;i++){
	              var facilityName=facilityInfoList[i].facilityName;
	              var alreadyLockQuantitySum=facilityInfoList[i].alreadyLockQuantitySum;
	              var accountingQuantityTotal=facilityInfoList[i].accountingQuantityTotal;
	              var warningQuantity=facilityInfoList[i].warningQuantity;
                   var warningMail="";
                   if(facilityInfoList[i].warningMail){
                       warningMail=facilityInfoList[i].warningMail;
                   }
	              var totalNum=facilityInfoList[i].totalNum;
	              var facilityId=facilityInfoList[i].facilityId;
                   var inventoryItemId=facilityInfoList[i].inventoryItemId;
	              
	              //alert(facilityId);
	              
	              //var tr='<tr><td>'+facilityName+'</td><tr>';
	              
	              <#--var tr1=' <tr><td><input type="hidden" name="" id="" value="'+facilityId+'">'+facilityName+'</td> <td>'+totalNum+'</td><td>'+alreadyLockQuantitySum+'</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td> <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';-->
                   var tr1=' <tr><td><input type="hidden" name="" id="" value="'+facilityId+'">'+facilityName+'</td> <td><input type="hidden" name="curInventoryItemId" id="curInventoryItemId" value="'+inventoryItemId+'">'+totalNum+'</td><td>'+alreadyLockQuantitySum+'</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td><td>'+warningMail+'</td> <td class="fc_td"><button type="button" class="js-button-facility btn btn-danger btn-sm ">${uiLabelMap.BrandDel}</button></td></tr>';
	             // var tr2='<tr><td>'+facilityname+'</td></tr>';
	 			  //alert(tr1);
	 			  $('.js-table_3>tbody').append(tr1);
	 			  $("button").attr("disabled","disabled");
	 			  
	 			  $("#btnReturn").removeAttr("disabled");
	 			  $("#btnProductVerifyPass").removeAttr("disabled");
	 			  $("#btnProductVerifyNoPass").removeAttr("disabled");
	 			  $("#modal_confirm").find("button").removeAttr("disabled");
	           }
	        }
	    });
	  } 
   }
  
  
  function getSelectedTag(){
  		<#assign productTagAssocList = delegator.findByAnd("ProductTagAssoc", {"productId" : "${prod.productId?if_exists}"})/>
	    <#list productTagAssocList as productTagAssoc>
	         $('input[name="tag"]').each(function(){ 
	            var tagValue=$(this).val();
	            if(tagValue=='${productTagAssoc.tagId?if_exists}'){
	                $(this).attr("checked","checked");
	            }
		     });
	     </#list>
  }
	
	
	
	
	
	// 将选择的记录id更新商品状态
    function updateProductVerifyStatusByIds(verifyStatus){
	  // 选中的项目
	  var checkedIds =$("#ids").val(); 
	  if (checkedIds != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>updateProductVerifyStatusByIds</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'checkedIds' : checkedIds,
	             'verifyStatus':verifyStatus
	        },
	        success: function(data) {
	           document.location.href="<@ofbizUrl>findProductGoodForVerify?lookupFlag=Y</@ofbizUrl>"; 
	        }
	    });
	  } 
	}
	
	
	
   // 取得商品规格信息
   function getProductFeatureListById(productFeatureTypeId,featureName,featureIds){
	  if (productFeatureTypeId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductFeatureListById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productFeatureTypeId' : productFeatureTypeId,
	             'featureIds' :featureIds
	        },
	        success: function(data) {
	           var map=data.productFeatureInfo;
	           var productFeatureList=map.productFeatureList;
	           var trCount=map.trCount;
	           var listSize=map.listSize;
	           var featureIdList=map.featureIdList;
	           
	           var $eventLog = $(".js-feature");
	           var tbDiv=' <div id="'+productFeatureTypeId+'">'+
                ' <table class="table table-bordered table_b_c">'+
                ' <thead>'+
                ' <tr>'+
                ' <label style="text-align:left;width:100%">'+featureName+'</label> '+
				' </tr>'+
				' </thead>';
				var tbBodyS =' <tbody>';
				var tr=	''; 
				if(listSize>0){
				    for (var i=0;i<trCount;i++){
				       tr=tr+'<tr>'; 
				       for(var j=i*3;j<(i+1)*3;j++){
				           if(j<listSize){ 
				           var productFeature=productFeatureList[j];
				           
				              
                              tr+='<td><input class="js-productFeature" type="checkbox" id="'+productFeature.productFeatureId+'" value="'+productFeature.productFeatureId+'"/><input type="hidden" name="" id="" value="'+productFeature.productFeatureId+'"></td>'+
					              '<td><input type="text" name="productFeatureName" id="" value="'+productFeature.productFeatureName+'"  disabled="true"></td>';
				           }
				       }
				       tr=tr+'</tr>';
				    }   
				}  
				var tbBodyE	=' </tbody>'+
						' </table>'+
						' </div>'; 
				var all=tbDiv+tbBodyS+tr+tbBodyE;		              
		        $eventLog.append(all);
		        // 勾选选中项目
		        $eventLog.find('input[type=checkbox]').each(function(){
		            var ckVal=$(this).val();
		            for (var i=0;i<featureIdList.length;i++){
		                var curFeatureId=featureIdList[i];
		                if(ckVal==curFeatureId){
		                   $(this).attr("checked",true);
		                }
		            }
		        });
		        $("input[type=checkbox]").attr("disabled","disabled");
		        
		        $("button").attr("disabled","disabled");
	 			$("#btnReturn").removeAttr("disabled");
	 			$("#btnGoodsList").removeAttr("disabled");
	 			$("#btnProductVerifyPass").removeAttr("disabled");
	 			$("#btnProductVerifyNoPass").removeAttr("disabled");
	 			$("#modal_confirm").find("button").removeAttr("disabled")
	        }
	    });
	  } 
   }

  // 取得服务支持类型
  function getSelectedSupService(){
  <#if prod?has_content>
	  <#assign supportServiceTypeForProductIdList = delegator.findByAnd("ProductSupportServiceAssoc",{"productId" : "${prod.productId?if_exists}"})/>
	  <#list supportServiceTypeForProductIdList as productSupportServiceAssoc>
          $('input[name="supportServiceType"]').each(function(){
              var tagValue=$(this).val();
              if(tagValue=='${productSupportServiceAssoc.enumId?if_exists}'){
                  $(this).attr("checked","checked");
              }
          });
	  </#list>
  </#if>
  }
</script>

		
