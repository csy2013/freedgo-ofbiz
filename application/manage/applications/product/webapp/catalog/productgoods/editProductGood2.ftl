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
                           
                               
<div class="box box-info">
	<div class="box-header with-border">
	  <h3 class="box-title">基本信息</h3>
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
            <input type="hidden" name="productFeatureInfos" id="productFeatureInfos" value=""/>
            
            <input type="hidden" id="pcDetailsContent"  value="<#if prod?has_content>${prod.pcDetails?if_exists}</#if>">
            <input type="hidden" id="mobileDetailsContent"  value="<#if prod?has_content>${prod.mobileDetails?if_exists}</#if>">
            <input type="hidden" id="pcDetailsContent"  value="">
            <input type="hidden" id="mobileDetailsContent"  value="">
            <input type="hidden" id="productTags" name="productTags" value="">
            
            <input type="hidden" id="productFeatureId" name="productFeatureId" value="">
	        
	        
	        <#if prod?has_content>
	        <div class="row">
				    <div class="form-group">
		                <label for="number" class="col-sm-2 control-label"><i class="required-mark">*</i>商品编号:</label>
		                <div class="col-sm-3">
		                     <input type="text" class="form-control" id="productId" name="productId" value="${parameters.productId?if_exists}" readonly>
		                </div>
		            </div>
	           </div>
	           
	           <div class="row title">
	                <div class="form-group col-sm-6" data-type="required" data-mark="商品名称">
	                    <label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>商品名称:</label>
	                    <div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:15px;width:48.5%;">
	                        <input type="text" class="form-control dp-vd" id="productName" name="productName" value="${prod.productName?if_exists}">
	                        <p class="dp-error-msg"></p>
	                    </div>
	                </div>
	                <div class="form-group col-sm-6" style="margin-left:-34px">
	                    <label for="subTitle" class="col-sm-4 control-label" style="text-align:center;padding-left:0;padding-right:0;">商品副标题:</label>
	                    <div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:-10px;width:48.5%;">
	                        <input type="text" class="form-control" id="productSubheadName" name="productSubheadName" value="${prod.productSubheadName?if_exists}">
	                    </div>
	                </div>
               </div>
               
               <div class="row">
                  <#assign productTagList = delegator.findByAnd("Tag", {"tagTypeId" : "1000"})/>
                 
	              <div class="form-group">
	                  <label  class="col-sm-2 control-label">商品标签:</label>
	                  <div class="col-sm-10">
		                  <div class="checkbox clearfix">
		                    <#list productTagList as productTag>
		                         <label class="col-sm-3" title=""><input name="tag" id="${productTag.tagId}" value="${productTag.tagId}" type="checkbox">${productTag.tagName}</label>
		                    </#list>
		                  </div> 
	                   </div>
	               </div>
               </div>
	        
	           
	            <div class="row">
		            <div class="form-group">
		                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>是否上架:</label>
		                <div class="col-sm-3">
		                    <div class="radio">
		                        <#if prod.isOnline=="Y">
		                        <label class="col-sm-6"><input name="isOnline"  value="Y" type="radio" checked>上架</label>
	                            <label class="col-sm-6"><input name="isOnline" value="N" type="radio">下架</label>
	                            <#else>
	                            <label class="col-sm-6"><input name="isOnline"  value="Y" type="radio" >上架</label>
	                            <label class="col-sm-6"><input name="isOnline" value="N" type="radio" checked>下架</label>
	                            </#if>
		                    </div>
		                    <div class="dp-error-msg"></div>
		                </div>
		                <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：商品上架后才可在店铺显示和下单销售</div></div>
		            </div>
                </div>
	            
            
	            <div class="row">
	                <div class="form-group" data-type="linkLt" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间">
	                    <label for="startTime" class="col-sm-2 control-label"><i class="required-mark">*</i>销售开始时间:</label>
	                    <div class="col-sm-3">
		                    <div class="input-group date form_datetime col-sm-12" data-link-field="startTime">
		                        <input class="form-control" size="16" type="text" value="${prod.introductionDate?string('yyyy-MM-dd HH:mm')?if_exists}" readonly>
		                        <input id="startTime" name="startTime" class="dp-vd" type="hidden" value="${prod.introductionDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}">
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
		                    </div>
		                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
	                    </div>
	                    <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：商品在销售时间内才可以下单销售</div></div>
	                </div>
	            </div>
	            
	            
	            <div class="row">
	                <div id="endTimeGroup" class="form-group">
	                    <label for="endTime" class="col-sm-2 control-label">销售结束时间:</label>
	                    <div class="col-sm-3">
		                    <div class="input-group date form_datetime col-sm-12"  data-link-field="endTime">
		                        <input class="form-control" size="16" type="text"  value="${prod.salesDiscontinuationDate?string('yyyy-MM-dd HH:mm')?if_exists}" readonly>
		                        <input id="endTime" name="endTime" class="dp-vd" type="hidden" value="${prod.salesDiscontinuationDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}">
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
		                    </div>
		                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
	                    </div>
	                </div>
	            </div>
            
	            <div class="row title">
	                <div class="form-group col-sm-6" data-type="required" data-mark="商家名称">
	                    <label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>商家名称:</label>
	                    <div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:15px;width:48.5%;">
	                        <input type="text" class="form-control dp-vd" id="businessPartyId" name="businessPartyId" value="${prod.businessPartyId?if_exists}">
	                        <p class="dp-error-msg"></p>
	                    </div>
	                </div>
	                <div class="form-group col-sm-6" style="margin-left:-34px">
	                    <#assign productBrandList = delegator.findByAnd("ProductBrand")/>
	                    <label for="subTitle" class="col-sm-4 control-label" style="text-align:center;padding-left:0;padding-right:0;">商品品牌:</label>
	                    <div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:-10px;width:48.5%;">
		                    <select class="form-control select2Brand" id="brandId" name="brandId">
		                      <#if prod.brandId?has_content>
		                      
		                      <#list productBrandList as productBrand>
			                        <#if prod.brandId==productBrand.productBrandId>
	                                 <option value="${productBrand.productBrandId}" selected="selected">${productBrand.brandName}</option>
	                                <#else>
	                                 <option value="${productBrand.productBrandId}">${productBrand.brandName}</option>
	                                </#if>
		                      </#list>
		                      <#else>
		                      <#list productBrandList as productBrand>
	                               <option value="${productBrand.productBrandId}">${productBrand.brandName}</option>
		                      </#list>
		                      </#if>
		                    </select>
		                    
	                    </div>
					</div>
	            </div>
            
	           <div class="row">
	                <#assign productPriceSaleList = delegator.findByAnd("ProductPrice", {"productId" : "${prod.productId?if_exists}","productPriceTypeId":"SALE_PRICE"})/>
	                <#if productPriceSaleList?has_content>
	                	<#assign priceSaleInfo = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(productPriceSaleList) />
	                	<#if priceSaleInfo?has_content>
	                	   <#assign priceSale =priceSaleInfo.price?if_exists/>
	                	</#if>
	                </#if>
	                
	                <div class="form-group" data-type="required" data-mark="销售价格">
	                    <label  class="col-sm-2 control-label"><i class="required-mark">*</i>销售价格(元):</label>
	                    <div class="col-sm-3">
		                    <input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="${priceSale?if_exists}" placeholder="" />
		                    <div class="dp-error-msg"></div>
	                    </div>
	                    <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：使用特征时，该价格作为商品的参考价展示</div></div>
	                </div>
	            </div>
	            
	            
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
	                
		            <div class="form-group">
		                <label  class="col-sm-2 control-label">市场价格(元):</label>
		                <div class="col-sm-3">
		                    <input type="text" class="form-control" id="marketPrice" name="marketPrice" value="${priceMarket?if_exists}" placeholder="" />
		                </div>
		                
		                <label  class="col-sm-2 control-label">成本价格(元):</label>
		                <div class="col-sm-3">
		                    <input type="text" class="form-control" id="costPrice" name="costPrice" value="${priceCost?if_exists}" placeholder="" />
		                </div>
		            </div>
	            </div>
	            
	            
	            <div class="row">
		            <div class="form-group">
		                <label  class="col-sm-2 control-label">体积(m³):</label>
		                <div class="col-sm-3">
		                    <input type="text" class="form-control" id="volume" name="volume" value="${prod.volume?if_exists}" placeholder="" />
		                </div>
		                
		                <label  class="col-sm-2 control-label">重量(kg):</label>
		                <div class="col-sm-3">
		                    <input type="text" class="form-control" id="weight" name="weight" value="${prod.weight?if_exists}" placeholder="" />
		                </div>
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
	                <label  class="col-sm-2 control-label">商品属性:</label>
	                <div class="col-sm-8">
	                    <table class="table table-bordered table_b_c js-table_2">
					      <tbody>
					          <#if listSize gt 0>
						          <#list 0..cc-1 as k>
								    <tr> 
								    <#list k*3..(k+1)*3-1 as ff>
								       <#list productCategoryAttributeList as productCategoryAttribute>
				                          <#if ff==productCategoryAttribute_index>
				                              <td>${productCategoryAttribute.attrName}<input type="hidden" name="" id="" value="${productCategoryAttribute.productCategoryId}"></td>
									          <td>
									                <#assign productOptionList = delegator.findByAnd("ProductOption", {"productCategoryId" : "${prod.primaryProductCategoryId?if_exists}","attrName":productCategoryAttribute.attrName})/>
									                <select class="form-control">
									                <#list productOptionList as productOption>
									                	<option value="${productOption.productOptionId}">${productOption.optionName}</option>
									                </#list>
				                                    </select>
									          </td>
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
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">商品参数:</label>
	                <div class="col-sm-3">
	                    <button type="button" class="btn btn-primary js-addpara" id="btnAddProductParameter">添加参数</button>
	                </div>
	            </div>
            </div>
            <div class="row">
                <div class="form-group">
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
            
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>是否使用特征:</label>
	                <div class="col-sm-4">
	                    <div class="radio">
	                        <label class="col-sm-6"><input name="isUsedFeature" value="N" type="radio" <#if prod.isUsedFeature=='N'>checked</#if>>直接创建商品</label>
                            <label class="col-sm-6"><input name="isUsedFeature" value="Y" type="radio" <#if prod.isUsedFeature=='Y'>checked</#if>>使用特征</label>
	                    </div>
	                    <div class="dp-error-msg"></div>
	                </div>
	                 
	            </div>
            </div>
            
	            <div class="row js-feature-div">
		            <div class="form-group">
		                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>特征名称:</label>
		                <div class="col-sm-4">
		                    <#assign productFeatureTypeList = delegator.findByAnd("ProductFeatureType")/>
		                    <select class="form-control select2Feature" multiple="multiple" data-placeholder="Select a State">
		                      <#list productFeatureTypeList as productFeatureType>
		                          <#if productFeatureType.productFeatureTypeName?if_exists != "">
		                          <option value="${productFeatureType.productFeatureTypeId?if_exists}">${productFeatureType.productFeatureTypeName?if_exists}</option>
		                          </#if>
		                      </#list>
		                    </select>
		                </div>
		            </div>
	            </div>
	            
	             <#--特征值-->
	            <div class="row js-feature-div">
	               <div class="form-group">
	               <label  class="col-sm-2 control-label"></label>
	                <div class="col-sm-8 js-feature">
	                 
	                </div>
	               </div>
	            </div>
            <div class="js-facility">
	            <div class="row">
		            <div class="form-group">
		                <label  class="col-sm-2 control-label">商品库存:</label>
		                <div class="col-sm-3">
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
						               <th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>${uiLabelMap.BrandOption}</th>
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
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>商品图片:</label>
	                <div class="col-sm-3">
	                    <button type="button" class="btn btn-primary" id="btnAddProductContent">添加图片</button>
	                </div>
	                <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：最多可添加5张照片，第一张图为主图，请保证所有图片比例一致。大小为640*640</div></div>
	            </div>
            </div>
            
            <div class="row">
                <div class="form-group">
                    <label  class="col-sm-2 control-label"></label>
                    <div class="col-sm-3">
				         <img vspace="5" hspace="5" width="150" height="150" src="<@ofbizContentUrl>/images/products/10000/original.jpg</@ofbizContentUrl>" class="cssImgSmall" alt="" />
				     </div>
                </div>
            </div>
           
            
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>关联商品:</label>
	                <div class="col-sm-3">
	                    <button type="button" class="btn btn-primary" id="btnAddProductAssocGood">添加关联商品</button>
	                </div>
	            </div>
            </div>
            
            
            <div class="row">
                <div class="form-group" >
                    <label for="seo" class="col-sm-2 control-label">SEO关键字:</label>
                    <div class="col-sm-6">
                        <textarea class="form-control" name="seoKeyword" id="seoKeyword" rows="6" value="">${prod.seoKeyword?if_exists}</textarea>
                    </div>
                </div>
            </div>
          
            <div class="row">
                <div class="form-group" >
                    <label for="seo" class="col-sm-2 control-label">PC端详情:</label>
                    <div class="col-sm-6">
                        <textarea id="pcDetails" name="pcDetails" rows="6" cols="80" value="" >
                        </textarea>
                    </div>
                </div>
            </div>
            
            
            <div class="row">
                <div class="form-group" >
                    <label for="seo" class="col-sm-2 control-label">移动端详情:</label>
                    <div class="col-sm-6">
                        <textarea id="mobileDetails" name="mobileDetails" rows="6" cols="80" value="" >
                        </textarea>
                        
                    </div>
                </div>
            </div>
	        
	        <#else>
	        
	        <div class="row">
			    <div class="form-group">
	                <label for="number" class="col-sm-2 control-label"><i class="required-mark">*</i>商品编号:</label>
	                <div class="col-sm-3">
	                     <input type="text" class="form-control" id="productId" name="productId" value="${parameters.productId?if_exists}" readonly>
	                </div>
	            </div>
            </div>
            
            <div class="row title">
                <div class="form-group col-sm-6" data-type="required" data-mark="商品标题">
                    <label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>商品标题:</label>
                    <div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:15px;width:48.5%;">
                        <input type="text" class="form-control dp-vd" id="productName" name="productName" value="">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6" style="margin-left:-34px">
                    <label for="subTitle" class="col-sm-4 control-label" style="text-align:center;padding-left:0;padding-right:0;">商品副标题:</label>
                    <div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:-10px;width:48.5%;">
                        <input type="text" class="form-control" id="productSubheadName" name="productSubheadName" value="">
                    </div>
                </div>
            </div>
            
            <div class="row">
                <#assign productTagList = delegator.findByAnd("Tag", {"tagTypeId" : "1000"})/>
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">商品标签:</label>
	                <div class="col-sm-10">
	                   <div class="checkbox clearfix">
	                    <#list productTagList as productTag>
	                     <label class="col-sm-3" title=""><input name="tag" id="tag" value="${productTag.tagId}" type="checkbox">${productTag.tagName}</label>
	                    </#list>
	                   </div> 
	                </div>
	            </div>
            </div>
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>是否上架:</label>
	                <div class="col-sm-3">
	                    <div class="radio">
	                        <label class="col-sm-6"><input name="isOnline"  value="Y" type="radio" checked>上架</label>
                            <label class="col-sm-6"><input name="isOnline" value="N" type="radio">下架</label>
	                    </div>
	                    <div class="dp-error-msg"></div>
	                </div>
	                <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：商品上架后才可在店铺显示和下单销售</div></div>
	            </div>
            </div>
            
            <div class="row">
                <div class="form-group" data-type="linkLt" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间">
                    <label for="startTime" class="col-sm-2 control-label"><i class="required-mark">*</i>销售开始时间:</label>
                    <div class="col-sm-3">
	                    <div class="input-group date form_datetime col-sm-12" data-link-field="startTime">
	                        <input class="form-control" size="16" type="text" readonly>
	                        <input id="startTime" name="startTime" class="dp-vd" type="hidden">
	                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
	                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
	                    </div>
	                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>
                    <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：商品在销售时间内才可以下单销售</div></div>
                </div>
            </div>
            
            
            <div class="row">
                <div id="endTimeGroup" class="form-group">
                    <label for="endTime" class="col-sm-2 control-label">销售结束时间:</label>
                    <div class="col-sm-3">
	                    <div class="input-group date form_datetime col-sm-12"  data-link-field="endTime">
	                        <input class="form-control" size="16" type="text" readonly>
	                        <input id="endTime" name="endTime" class="dp-vd" type="hidden">
	                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
	                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
	                    </div>
	                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>
                </div>
            </div>
            
            <div class="row title">
                <div class="form-group col-sm-6" data-type="required" data-mark="商家名称">
                    <label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>商家名称:</label>
                    <div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:15px;width:48.5%;">
                        <input type="text" class="form-control dp-vd" id="businessPartyId" name="businessPartyId" value="">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6" style="margin-left:-34px">
                    <#assign productBrandList = delegator.findByAnd("ProductBrand")/>
                    <label for="subTitle" class="col-sm-4 control-label" style="text-align:center;padding-left:0;padding-right:0;">商品品牌:</label>
                    <div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:-10px;width:48.5%;">
                        <select class="form-control select2Brand" id="brandId" name="brandId">
	                      <#list productBrandList as productBrand>
                                 <option value="${productBrand.productBrandId}">${productBrand.brandName}</option>
	                      </#list>
	                    </select>
                    </div>
				</div>
            </div>
            
           <div class="row">
                <div class="form-group" data-type="required,format"  data-reg="/^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$/"   data-mark="销售价格">
                    <label  class="col-sm-2 control-label"><i class="required-mark">*</i>销售价格(元):</label>
                    <div class="col-sm-3">
	                    <input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="" placeholder="" />
	                     <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：使用特征时，该价格作为商品的参考价展示</div></div>
                </div>
            </div>
            
            
            <div class="row">
	            <div class="form-group" data-type="format" data-reg="/^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$/" data-mark="市场价格" >
	                <label  class="col-sm-2 control-label">市场价格(元):</label>
	                <div class="col-sm-3">
	                    <input type="text" class="form-control" id="marketPrice" name="marketPrice" value="" placeholder="" />
	                     <p class="dp-error-msg"></p>
	                </div>
	                
	                <label  class="col-sm-2 control-label">成本价格(元):</label>
	                <div class="col-sm-3">
	                    <input type="text" class="form-control" id="costPrice" name="costPrice" value="" placeholder="" />
	                     <p class="dp-error-msg"></p>
	                </div>
	            </div>
            </div>
            
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">体积(m³):</label>
	                <div class="col-sm-3">
	                    <input type="text" class="form-control" id="volume" name="volume" value="" placeholder="" />
	                </div>
	                
	                <label  class="col-sm-2 control-label">重量(kg):</label>
	                <div class="col-sm-3">
	                    <input type="text" class="form-control" id="weight" name="weight" value="" placeholder="" />
	                </div>
	            </div>
            </div>
            
            
            <div class="row">
                <#assign productCategoryAttributeList = delegator.findByAnd("ProductCategoryAttribute", {"productCategoryId" : "${parameters.productCategoryId?if_exists}"})/>
                
                <#assign listSize=productCategoryAttributeList.size()/>
                <#assign aa=listSize/3/>
                <#assign bb=listSize%3/>
                <#assign cc=0/>
                <#if bb gt 0>
                    <#assign cc=aa?int+1/>
                </#if>
                
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">商品属性:</label>
	                <div class="col-sm-8">
	                    <table class="table table-bordered table_b_c js-table_2">
					      <tbody>
					          <#if listSize gt 0>
						          <#list 0..cc-1 as k>
								    <tr> 
								    <#list k*3..(k+1)*3-1 as ff>
								       <#list productCategoryAttributeList as productCategoryAttribute>
				                          <#if ff==productCategoryAttribute_index>
				                              <td>${productCategoryAttribute.attrName}<input type="hidden" name="" id="" value="${productCategoryAttribute.productCategoryId}"></td>
									          <td>
									                <#assign productOptionList = delegator.findByAnd("ProductOption", {"productCategoryId" : "${parameters.productCategoryId?if_exists}","attrName":productCategoryAttribute.attrName})/>
									                <select class="form-control">
									                <#list productOptionList as productOption>
									                	<option value="${productOption.productOptionId}">${productOption.optionName}</option>
									                </#list>
				                                    </select>
									          </td>
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
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">商品参数:</label>
	                <div class="col-sm-3">
	                    <button type="button" class="btn btn-primary js-addpara" id="btnAddProductParameter">添加参数</button>
	                </div>
	            </div>
            </div>
            <div class="row">
                <div class="form-group">
                   <label  class="col-sm-2 control-label"></label>
                   <div class="col-sm-8">
                      <table class="table table-bordered table_b_c js-table_1">
					      <thead>
					           <tr>
					               <th>参数名称</th><th>参数详情</th><th>${uiLabelMap.BrandOption}</th>
					           </tr>
					      </thead>
					      <tbody>
					      </tbody>
		    		  </table>
                   </div>
                </div>
            </div>
            
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>是否使用特征:</label>
	                <div class="col-sm-4">
	                    <div class="radio">
	                        <label class="col-sm-6"><input name="isUsedFeature" value="N" type="radio" checked>直接创建商品</label>
                            <label class="col-sm-6"><input name="isUsedFeature" value="Y" type="radio">使用特征</label>
	                    </div>
	                    <div class="dp-error-msg"></div>
	                </div>
	                 
	            </div>
            </div>
            
            <div class="row js-feature-div">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>特征名称:</label>
	                <div class="col-sm-4">
	                    <#assign productFeatureTypeList = delegator.findByAnd("ProductFeatureType")/>
	                    <select class="form-control select2Feature" multiple="multiple" data-placeholder="Select a State">
	                      <#list productFeatureTypeList as productFeatureType>
	                       <#if productFeatureType.productFeatureTypeName?if_exists != "">
	                          <option value="${productFeatureType.productFeatureTypeId?if_exists}">${productFeatureType.productFeatureTypeName?if_exists}</option>
	                          </#if>
	                      </#list>
	                    </select>
	                </div>
	            </div>
            </div>
            
            <#--特征值-->
            <div class="row js-feature-div">
               <div class="form-group">
               <label  class="col-sm-2 control-label"></label>
                <div class="col-sm-8 js-feature">
                 
                </div>
               </div>
            </div>
            
            <div class="js-facility">
	            <div class="row">
		            <div class="form-group">
		                <label  class="col-sm-2 control-label">商品库存:</label>
		                <div class="col-sm-3">
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
						               <th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>${uiLabelMap.BrandOption}</th>
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
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>商品图片:</label>
	                <div class="col-sm-3">
	                    <button type="button" class="btn btn-primary" id="btnAddProductContent">添加图片</button>
	                </div>
	                <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：最多可添加5张照片，第一张图为主图，请保证所有图片比例一致。大小为640*640</div></div>
	            </div>
            </div>
            
            <div class="row">
                <div class="form-group">
                    <label  class="col-sm-2 control-label"></label>
                    <div class="col-sm-3">
				         <img vspace="5" hspace="5" width="150" height="150" src="<@ofbizContentUrl>/images/products/10000/original.jpg</@ofbizContentUrl>" class="cssImgSmall" alt="" />
				     </div>
                </div>
            </div>
           
            
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>关联商品:</label>
	                <div class="col-sm-3">
	                    <button type="button" class="btn btn-primary" id="btnAddProductAssocGood">添加关联商品</button>
	                </div>
	            </div>
            </div>
            
            
            <div class="row">
                <div class="form-group">
                   <label  class="col-sm-2 control-label"></label>
                   <div class="col-sm-8">
                      <table class="table table-bordered table_b_c js-table_4">
					      <thead>
					           <tr>
					               <th>商品编码</th><th>商品名称</th> <th>商品类型</th> <th>商品图片</th> <th>商品特征</th> <th>商品价格(元)</th><th>${uiLabelMap.BrandOption}</th>
					           </tr>
					      </thead>
					      <tbody>
					      </tbody>
		    		  </table>
                   </div>
                </div>
            </div>
            
            <div class="row">
                <div class="form-group" >
                    <label for="seo" class="col-sm-2 control-label">SEO关键字:</label>
                    <div class="col-sm-6">
                        <textarea class="form-control" name="seoKeyword" id="seoKeyword" rows="6"></textarea>
                    </div>
                </div>
            </div>
            
            <div class="row">
                <div class="form-group" >
                    <label for="seo" class="col-sm-2 control-label">PC端详情:</label>
                    <div class="col-sm-6">
                        <textarea id="pcDetails" name="pcDetails" rows="6" cols="80" value="" >
                        </textarea>
                    </div>
                </div>
            </div>
            
            
            <div class="row">
                <div class="form-group" >
                    <label for="seo" class="col-sm-2 control-label">移动端详情:</label>
                    <div class="col-sm-6">
                        <textarea id="mobileDetails" name="mobileDetails" rows="6" cols="80" value="" >
                        </textarea>
                        
                    </div>
                </div>
            </div>
            </#if>
	       <div class="modal-footer">
	            <button type="button" class="btn btn-primary" id="btnGoBack">上一步</button>
	            <button type="button" class="btn btn-primary" id="btnProductGoodSave">下一步</button>
	       </div>
	   </form>
    </div><!-- /.box-body -->
</div>



 <div class="modal fade" id="addFacility" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
	<div class="modal-dialog" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">选择仓库</h4>
        </div>
        <div class="modal-body">
             <form class="form-horizontal"  id="updateProductFacility" method="post" action="" name="updateProductFacility" enctype="multipart/form-data">
                <#assign productFacilityList = delegator.findByAnd("Facility")/>
			    <div class="form-group" data-type="required" data-mark="选择仓库">
		            <label class="control-label col-sm-2"><span style="color: red">*</span>选择仓库:</label>
		            <div class="col-sm-10">
		                <select class="form-control dp-vd" id="facilityname" name="facilityname">
	                        <#list productFacilityList as productFacility >
	                        <option value="${productFacility.facilityId?if_exists}">${productFacility.facilityName?if_exists}</option>
	                        </#list>
	                    </select>
	                    <p class="dp-error-msg"></p>
		            </div>                
		        </div>
			    
                <div class="form-group" data-type="required" data-mark="可用库存">
            		<label for="message-text" class="control-label col-sm-2"><span style="color: red">*</span>可用库存:</label>
	                <div class="col-sm-10">
	                  <input type="text" class="form-control dp-vd" name="accountingQuantityTotal" id="accountingQuantityTotal">
	                  <p class="dp-error-msg"></p>
	                </div>
          		</div>
          		
          		<div class="form-group">
            		<label for="message-text" class="control-label col-sm-2">库存预警数量:</label>
	                <div class="col-sm-10">
	                  <input type="text" class="form-control" name="warningQuantity" id="warningQuantity">
	                </div>
          		</div>
          		
          		<input type="hidden" name="operateType" id="operateType" value=""/>
            </form>
          </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
        <button type="button" class="btn btn-primary" id="btnFacilitySave">${uiLabelMap.BrandSave}</button>
      </div>
    </div>
  </div>
</div>

<#--选择新增Sku-->
 <div class="modal fade" id="selectSku" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
	<div class="modal-dialog" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">选择新增的Sku</h4>
        </div>
        <div class="modal-body">
            <table class="table table-bordered table-hover js-checkparent">
                <thead>
                <tr>
                    <th style="text-align:left;"><input class="js-allcheck" type="checkbox" id="checkAll">选择新增的sku</th>
                 </tr>   
                </thead>
                <tbody>
                    <form method="post" action="" name="addFeatureProduct" id="addFeatureProduct">
                        <tr>
	                        <td style="text-align:left;"><input class="js-checkchild" type="checkbox" id="" value=""/>gggge</td>
                        </tr>
                        <tr>
	                        <td style="text-align:left;"><input class="js-checkchild" type="checkbox" id="" value=""/>dd</td>
                        </tr>
                        <tr>
	                        <td style="text-align:left;"><input class="js-checkchild" type="checkbox" id="" value=""/>eee</td>
                        </tr>
                    </form>
                </tbody>
            </table>
        </div>
        
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
        <button type="button" class="btn btn-primary" id="btnEnter">确定</button>
        <button type="button" class="btn btn-primary" id="btnSelectRepeat">重新选特征</button>
      </div>
    </div>
  </div>
</div>



<script language="JavaScript" type="text/javascript">
    var isCommitted = false;//表单是否已经提交标识，默认为false
    $(function(){
        $(".select2Brand").select2();
        $(".select2Feature").select2({
	        tags: true,
	        maximumSelectionLength: 3  //最多能够选择的个数
	    });
    
        CKEDITOR.replace("pcDetails");
        CKEDITOR.replace("mobileDetails");
        var pcDetailsContent= $("#pcDetailsContent").val();
        var mobileDetailsContent= $("#mobileDetailsContent").val();
        if(mobileDetailsContent){
            CKEDITOR.instances.mobileDetails.setData(mobileDetailsContent);
        }
        if(pcDetailsContent){
            CKEDITOR.instances.pcDetails.setData(pcDetailsContent);
        }
        var productTypeId=$("#productTypeId").val();
        var productCategoryId=$("#productCategoryId").val();
        <#if prod?has_content>
            getSelectedTag();//标签选择初始化
            getFacilityByProductId('${parameters.productId?if_exists}'); //仓库信息初始化
            <#if prod.isUsedFeature=="Y">
               $('.js-facility').hide();
	           $('.js-feature-div').show();
	        <#else>
	             $('.js-facility').show();
		         $('.js-feature-div').hide();
	        </#if>
            // 初始化商品特征
		      <#assign getFeatureIdListForProductIdList = delegator.findByAnd("GetFeatureIdListForProductId",{"productId" : "${prod.productId?if_exists}"})/>
		      var ids="";
		      var typeName="";
		      <#list getFeatureIdListForProductIdList as featureType>
		          var curTypeId="${featureType.productFeatureTypeId}";
		          var curTypeName="${featureType.productFeatureTypeName}";
		          $('.select2Feature').val([curTypeId]).trigger("change");
		          <#assign featureList = delegator.findByAnd("GetFeatureIdList",{"productId" : "${prod.productId?if_exists}","productFeatureTypeId":"${featureType.productFeatureTypeId}"})/>
		          var featureIds="";
		          <#list featureList as featureInfo>
		             featureIds=featureIds+"${featureInfo.productFeatureId}"+",";
		          </#list>
		          //alert(featureIds.substr(0,featureIds.length-1));
		          getProductFeatureListById(curTypeId,curTypeName,featureIds.substr(0,featureIds.length-1));
		      </#list>
		<#else>
	      $('.js-facility').show();
	      $('.js-feature-div').hide();
        </#if>
      
      
        $(".select2Feature").select2({
	        tags: true,
	        maximumSelectionLength: 3  //最多能够选择的个数
	    });
	    
	    $('input:radio[name="isUsedFeature"]').change(function() { 
	        //alert($(this).val());
	        if($(this).val()=="Y"){
	           $('.js-facility').hide();
	           $('.js-feature-div').show();
	           
	           $(".select2Feature").select2({
			        tags: true,
			        maximumSelectionLength: 3  //最多能够选择的个数
			   });
	        }else{
	           $('.js-facility').show();
	           $('.js-feature-div').hide();
	        }
	    })
        var operateType='${parameters.operateType?if_exists}';
        
        
        $('.form_datetime').datetimepicker({
            language:  'zh-CN',
            todayBtn:  1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });
        
        $('.form_date').datetimepicker({
            language:  'zh-CN',
            todayBtn:  1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            minView: 2,
            forceParse: 0
        });
        
        $('.form_time').datetimepicker({
            language:  'zh-CN',
            autoclose: 1,
            startView: 1,
            forceParse: 0
        });
        
        
         // 保存处理
        $("#btnProductGoodSave").click(function(){
		    var isUsedFeature=$('.radio').find('input[name=isUsedFeature]:checked').val();
		    if(isUsedFeature=='Y'){
		       <#if prod?has_content>
		         //document.location.href="<@ofbizUrl>updateProductFeatureGood?productId="+${parameters.productId?if_exists}+"&operateType=update</@ofbizUrl>";
		         getUpdatePara();
		       <#else>
		       getSelectedFeatures();
		       </#if>
		    }else{
		       //var aa=$("#brandId").val();
		       //alert(aa);
		       $('#updateProductGood').submit();
		    }
        });
        
        
        $('#updateProductGood').dpValidate({
      		validate: true,
      	    callback: function(){
  			   var operate='${parameters.operateType?if_exists}';
  			   if(dosubmit()){
  			       // 取得商品标签
  			       var ids = "";
				   $('input[name="tag"]:checked').each(function(){ 
			            ids = ids + "," + $(this).val();
				   }); 
				   $("#productTags").val(ids.substr(1,ids.length));
  			       // 取得参数信息
  			       productParameterInfos();
  			       // 取得商品分类属性信息
  			       attrInfos();
  			       // 取得商品库存信息
  			       facilityInfos();
  			       $("#pcDetails").val(CKEDITOR.instances.pcDetails.getData()); 
	  			   $("#mobileDetails").val(CKEDITOR.instances.mobileDetails.getData());
			       document.updateProductGood.action="<@ofbizUrl>updateProductGood</@ofbizUrl>";
			       document.updateProductGood.submit();
		       }
      		}
        });
       
        
        // 重新选择处理
        $("#btnSelectRepeat").click(function(){
            $('input[class="js-checkchild"]').each(function(){ 
                 $(this).attr('checked',false);
			}); 
			$('input[class="js-allcheck"]').attr('checked',false);
        })
        
        // 确认处理
        $("#btnEnter").click(function(){
            var ids="";
            $('input[class="js-checkchild"]:checked').each(function(){ 
	            ids = ids + "," + $(this).val();
			});
			//alert(ids.substr(1,ids.length)); 
			
			///////////////////////////////////////////
			
			// 操作类型	
		    var operateType='${parameters.operateType?if_exists}';
		    // 商品编号	                 
			var productId=$("#productId").val();
			// 商品类型	                      
			var productTypeId='${parameters.productTypeId?if_exists}';
			// 商品分类	              
			var productCategoryId='${parameters.productCategoryId?if_exists}';
            // 商品名称	
            var productName=$("#productName").val();
            // 商品副标题	
            var productSubheadName=$("#productSubheadName").val();
            // 是否上架	
            var isOnline=$('.radio').find('input[name=isOnline]:checked').val();
            // 销售开始时间	
            var startTime=$("#startTime").val();
            // 销售结束时间	
            var endTime=$("#endTime").val();
            // 商家名称	
            var businessPartyId=$("#businessPartyId").val();
            // 商品品牌	
            var brandId=$("#brandId").val();
            // 体积	
            var volume=$("#volume").val();
            // 重量	
            var weight=$("#weight").val();
            // 是否使用特征	
            var isUsedFeature=$('.radio').find('input[name=isUsedFeature]:checked').val();
            // seo关键字
            var seoKeyword=$("#seoKeyword").val();
            // 销售价格	
            var salePrice=$("#salePrice").val();
            // 市场价格	
            var marketPrice=$("#marketPrice").val();
            // 成本价格	
            var costPrice=$("#costPrice").val();
            // 取得商品标签
  			var tagIds = "";
			$('input[name="tag"]:checked').each(function(){ 
			    tagIds = tagIds + "," + $(this).val();
			}); 
			var productTags=tagIds.substr(1,tagIds.length);
		    // 取得参数信息
			productParameterInfos();
			var productParameterInfos1=$("#productParameterInfos").val();
  			// 取得商品分类属性信息
  			attrInfos();
  			var productAttrInfos=$("#productAttrInfos").val();
  			// 取得商品库存信息
  			facilityInfos();
  			var productFacilityInfos=$("#productFacilityInfos").val();
  			// 取得pc详情信息
  			$("#pcDetails").val(CKEDITOR.instances.pcDetails.getData()); 
  			var pcDetails=$("#pcDetails").val();
            // 取得移动详情信息
	  		$("#mobileDetails").val(CKEDITOR.instances.mobileDetails.getData());
	  		var mobileDetails=$("#mobileDetails").val();
	  		// 商品特征信息
            var productFeatureInfos=$("#productFeatureInfos").val();
	  			   
            // 图片信息
            var productContentInfos=$("#productContentInfos").val();
            // 商品关系信息
            var productAssocInfos=$("#productAssocInfos").val();
            
			
			//var testStr=productFeatureInfos;
			
			var testStr=operateType+"^"+
		                productId+"^"+
					    productTypeId+"^"+
					    productCategoryId+"^"+
					    productName+"^"+
					    productSubheadName+"^"+
					    isOnline+"^"+
					    startTime+"^"+
					    endTime+"^"+
					    businessPartyId+"^"+
					    brandId+"^"+
					    volume+"^"+
					    weight+"^"+
					    isUsedFeature+"^"+
					    seoKeyword+"^"+
					    salePrice+"^"+
					    marketPrice+"^"+
					    costPrice+"^"+
					    productTags+"^"+
					    productParameterInfos1+"^"+
					    productAttrInfos+"^"+
					    productFacilityInfos+"^"+
					    pcDetails+"^"+
					    mobileDetails+"^"+
				        productFeatureInfos+"^"+
				        productContentInfos+"^"+
					    productAssocInfos;
			//alert(testStr);
		  //////////////////////////////////////////
		  
		   document.location.href="<@ofbizUrl>editProductFeatureGood?ids="+ids.substr(1,ids.length)+"&"+
														   "operateType="+operateType+"&"+
														   "productId="+productId+"&"+
														   "productTypeId="+productTypeId+"&"+
														   "productCategoryId="+productCategoryId+"&"+
														   "productName="+productName+"&"+
														   "productSubheadName="+productSubheadName+"&"+
														   "isOnline="+isOnline+"&"+
														   "startTime="+startTime+"&"+
														   "endTime="+endTime+"&"+
														   "businessPartyId="+businessPartyId+"&"+
														   "brandId="+brandId+"&"+
														   "volume="+volume+"&"+
														   "weight="+weight+"&"+
														   "isUsedFeature="+isUsedFeature+"&"+
														   "seoKeyword="+seoKeyword+"&"+
														   "salePrice="+salePrice+"&"+
														   "marketPrice="+marketPrice+"&"+
														   "costPrice="+costPrice+"&"+
														   "productTags="+productTags+"&"+
														   "productParameterInfos1="+productParameterInfos1+"&"+
														   "productAttrInfos="+productAttrInfos+"&"+
														   "productFacilityInfos="+productFacilityInfos+"&"+
														   "pcDetails="+pcDetails+"&"+
														   "mobileDetails="+mobileDetails+"&"+
														   "productFeatureInfos="+productFeatureInfos+"&"+
														   "productContentInfos="+productContentInfos+"&"+
														   "productAssocInfos="+productAssocInfos+"</@ofbizUrl>";
		 
        })
        
        $("#btnGoBack").click(function(){
           var goBackPage="${parameters.gobackPage?if_exists}";
           
           if (goBackPage=="listPage"){
               document.location.href="<@ofbizUrl>findProductGoods</@ofbizUrl>";
           }else if (goBackPage=="viewPage"){
               document.location.href="<@ofbizUrl>viewProductGood?productId="+$("#productId").val()+"&operateType=view</@ofbizUrl>";
           }else if (goBackPage=="createPage"){
           	   document.location.href="<@ofbizUrl>createProductGood</@ofbizUrl>";
           }
           
        });
        
        //删除按钮事件
  	    $(document).on('click','.js-button',function(){
  	       $(this).parent().parent().remove();
        })
        
        
        
        
        //添加参数事件
        $(document).on('click','.js-addpara',function(){
        	var tr=' <tr><td><input type="text" name="" id="" value=""></td> <td><input type="text" name="" id="" value=""><td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
 			$('.js-table_1>tbody').append(tr);
  		})
        
        //添加库存
        $("#btnAddProductFacility").click(function(){
            //设置弹出框内容
			$('#addFacility').modal();
        
        })
        
        
        
        $("#btnFacilitySave").click(function(){
           $("#updateProductFacility").submit();
        });
        
	    $("#updateProductFacility").dpValidate({
	        validate:true,
	        callback:function(){
	        	var $model = $('#addFacility');
				var facilitynameId =$model.find('select[name=facilityname]').val();//仓库Id
				var facilityname =$model.find('select[name=facilityname] option:selected').text();//仓库名称
				var accountingQuantityTotal=$model.find('input[name=accountingQuantityTotal]').val();//可用库存
				var warningQuantity=$model.find('input[name=warningQuantity]').val();//预警库存
				
				//alert(facilityname);
				//alert(accountingQuantityTotal);
				//alert(warningQuantity);
	             
	            var tr=' <tr><td><input type="hidden" name="" id="" value="'+facilitynameId+'">'+facilityname+'</td><td>'+accountingQuantityTotal+'</td><td>0</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td><td class="fc_td"><button type="button" class="js-button-facility btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	 			$('.js-table_3>tbody').append(tr);
	 			$('#addFacility').modal('hide');
		    }
	    })
        
        
        
        //删除按钮事件
  	    $(document).on('click','.js-button-facility',function(){
  	       $(this).parent().parent().remove();
        })
        
        //商品特征事件
        var $eventSelect = $(".select2Feature");
        //商品特征的选择
        $eventSelect.on("select2:select", function (e) {
         addFeature("select2:select", e);
        });
        // 商品特征的删除
        $eventSelect.on("select2:unselect", function (e) { 
           delFeature("select2:unselect", e); 
        });
        
        // 关联商品按钮的点击事件
        $("#btnAddProductAssocGood").click(function(){
        
        	$.dataSelectModal({
				url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
				width:	"800",
				title:	"选择商品",
				selectCallBack: function(el){
				    var productId = el.data('id');
				    getProductAssocGoodListByIds(productId);
				}
			});
        })
        
        $("#btnAddProductContent").click(function(){
           alert("dd");
           
           function imageManage() {
                    $.chooseImage.int({
                        userId: 3446,
                        serverChooseNum: 5,
                        getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
                        submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
                        submitServerImgUrl: '',
                        submitNetworkImgUrl: ''
                    });
                    $.chooseImage.show();
                }
                $(function(){
                   $('body').on('click','.img-submit-btn',function(){
                       var obj = $.chooseImage.getImgData();
                       $.chooseImage.choose(obj,function(data){
                          console.log(data)
                       })
                   })
                });
        
        
        })

    })
    
    
   	function dosubmit(){
        if(isCommitted==false){
	        isCommitted = true;//提交表单后，将表单是否已经提交标识设置为true
	        return true;//返回true让表单正常提交
	    }else{
	        return false;//返回false那么表单将不提交
	    }
    }
    
    
    // 取得参数信息
    function productParameterInfos(){
	    var tParameterInfos = "";
        var curParameterInfo="";
        $('.js-table_1>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           curParameterInfo="";
	       var tdArr = $(this).children();
	       var parameterName = tdArr.eq(0).find("input").val();//参数名
	       var parameterDetails= tdArr.eq(1).find("input").val();//参数详情
	       curParameterInfo=parameterName+"|"+parameterDetails;
           tParameterInfos = tParameterInfos + "," + curParameterInfo;
	    }); 
	    var parameterInfos=tParameterInfos.substr(1,tParameterInfos.length);
	    $("#productParameterInfos").val(parameterInfos);
	}
	
	
	// 取得商品分类属性信息
	function attrInfos(){
	    var tAttrInfos = "";
        var curAttrInfo="";
        $('.js-table_2>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           var tdArr = $(this).children();
           curAttrInfo="";
           curTdOdd="";
           for(var i=0;i<tdArr.length;i++){
              if(tdArr.eq(i).find("input").val()){
                 var attrName=tdArr.eq(i).text();//商品分类属性ID
                 var productCategoryId=tdArr.eq(i).find("input").val();//商品分类属性ID
                 curTdOdd=attrName+"|"+productCategoryId;
              }else{
                 var productOptionId= tdArr.eq(i).find("select").val();//操作项的值
                 if(curTdOdd){
                    curAttrInfo=curTdOdd+"|"+productOptionId;
                    curTdOdd="";
                 }
                 tAttrInfos = tAttrInfos + "," + curAttrInfo;
              }
               
           }
	    }); 
	    var attrInfos=tAttrInfos.substr(1,tAttrInfos.length);
	    $("#productAttrInfos").val(attrInfos);
	}
	
	
	// 取得仓库分信息
	function facilityInfos(){
	    var tFacilityInfos = "";
        var curFacilityInfo="";
        $('.js-table_3>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           curFacilityInfo="";
	       var tdArr = $(this).children();
	       var facilityId = tdArr.eq(0).find("input").val();//仓库ID
	       var accountingQuantityTotal= tdArr.eq(3).text();//可用数量
	       var warningQuantity= tdArr.eq(4).text();//预警数量
	       curFacilityInfo=facilityId+"|"+accountingQuantityTotal+"|"+warningQuantity;
           tFacilityInfos = tFacilityInfos + "," + curFacilityInfo;
	    }); 
	    var facilityInfos=tFacilityInfos.substr(1,tFacilityInfos.length);
	    $("#productFacilityInfos").val(facilityInfos);
	}
	
	function getSelectedTag(){
	    <#if prod?has_content>
	  		<#assign productTagAssocList = delegator.findByAnd("ProductTagAssoc", {"productId" : "${prod.productId?if_exists}"})/>
		    <#list productTagAssocList as productTagAssoc>
		         $('input[name="tag"]').each(function(){ 
		            var tagValue=$(this).val();
		            if(tagValue=='${productTagAssoc.tagId?if_exists}'){
		                $(this).attr("checked","checked");
		            }
			     });
		     </#list>
	   </#if>
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
	              var totalNum=facilityInfoList[i].totalNum;
	              var facilityId=facilityInfoList[i].facilityId;
	              
	              var tr1=' <tr><td><input type="hidden" name="" id="" value="'+facilityId+'">'+facilityName+'</td> <td>'+totalNum+'</td><td>'+alreadyLockQuantitySum+'</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td> <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	              
	 			  $('.js-table_3>tbody').append(tr1);
	 			  
	           }
	        }
	    });
	  } 
   }
   
 
   // 商品特征的添加 
   function addFeature(name, evt) {
      if (evt) {
         var featureName=evt.params.data.text;
         var featureId=evt.params.data.id;
         getProductFeatureListById(featureId,featureName)
      }
   }
   // 商品特征的删除
   function delFeature(name, evt) {
      if (evt) {
         var featureName=evt.params.data.text;
         var featureId=evt.params.data.id;
         var aad="#"+featureId;
         $(aad).remove();
      }
  }
   
   <#--
   // 取得商品特征信息
   function getProductFeatureListById(productFeatureTypeId,featureName){
	  if (productFeatureTypeId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductFeatureListById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productFeatureTypeId' : productFeatureTypeId
	        },
	        success: function(data) {
	           var map=data.productFeatureInfo;
	           var productFeatureList=map.productFeatureList;
	           var trCount=map.trCount;
	           var listSize=map.listSize;
	           
	           var $eventLog = $(".js-feature");
	           var tbDiv=' <div id="'+productFeatureTypeId+'">'+
                ' <table class="table table-bordered table_b_c">'+
                ' <thead>'+
                ' <tr>'+
                ' <th style="text-align:left;">'+featureName+'</th> '
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
	        }
	    });
	  } 
   } 
   
   -->
    // 取得商品特征信息
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
                ' <th style="text-align:left;">'+featureName+'</th> '
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
	        }
	    });
	  } 
   } 	
   
   
   
   // 取得选择的特征值
   function getSelectedFeatures(){
       var tableItems="";
	   var trItem="";
	   var curItem="";
	   var tableIdItems="";
	   var trIdItem="";
	   var curIdItem="";
	   $('.js-feature').find("div").each(function(){
	       trItem=""
	       trIdItem=""
		   $(this).find("table>tbody").find("tr").each(function(){
               curItem="";
               curIdItem="";
			   var tr=$(this);
			   var chk1=tr.find('td').eq(0);//特征选择
			   var text1=tr.find('td').eq(1);//特征名称
			   var chk2=tr.find('td').eq(2);//特征选择
			   var text2=tr.find('td').eq(3);//特征名称
			   var chk3=tr.find('td').eq(4);//特征选择
			   var text3=tr.find('td').eq(5);//特征名称
			  
			   //var chkId= chk1.find('input[type=checkbox]:checked').val();
			   var bischecked1=chk1.find('input[type=checkbox]').is(':checked');
			   var bischecked2=chk2.find('input[type=checkbox]').is(':checked');
			   var bischecked3=chk3.find('input[type=checkbox]').is(':checked');
			   
			   if(bischecked1){
			   var text1Name= text1.find('input[name=productFeatureName]').val();
			   var chk1id= chk1.find('input[type=checkbox]:checked').val();
			   curItem=curItem+"|"+text1Name;
			   curIdItem=curIdItem+"|"+chk1id;
			   }
			   
			   if(bischecked2){
			   var text2Name= text2.find('input[name=productFeatureName]').val();
			   var chk2id= chk2.find('input[type=checkbox]:checked').val();
			   curItem=curItem+"|"+text2Name;
			   curIdItem=curIdItem+"|"+chk2id;
			   }
			   
			   if(bischecked3){
			   var text3Name= text3.find('input[name=productFeatureName]').val();
			   var chk3id= chk3.find('input[type=checkbox]:checked').val();
			   curItem=curItem+"|"+text3Name;
			   curIdItem=curIdItem+"|"+chk3id;
			   
			   }
			   trItem=trItem+"|"+curItem.substr(1,curItem.length);
			   trIdItem=trIdItem+"|"+curIdItem.substr(1,curIdItem.length-1);
			})
			tableItems=tableItems+","+trItem.substr(1,trItem.length);
			tableIdItems=tableIdItems+","+trIdItem.substr(1,trIdItem.length);
			$("#productFeatureInfos").val(tableIdItems.substr(1,tableIdItems.length));
			
		})
		jQuery.ajax({
	        url: '<@ofbizUrl>getProductFeatureInfoListByFeature</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productFeatureInfos' : tableItems.substr(1,tableItems.length)
	        },
	        success: function(data) {
	           var map=data.productFeatureInfo;
	           var productFeatureInfoList=map.productFeatureInfoList;
	           var listSize=map.productFeatureInfoListSize;
	           
	           var $model = $('#selectSku');
	           var tr="";
	           var $table=$model.find("table>tbody");
	           $table.find("tr").remove();
	           for(var i=0;i<listSize;i++){
	               var name=productFeatureInfoList[i].featureName;
	               //alert(name);
	               var tr='<tr><td style="text-align:left;"><input class="js-checkchild" type="checkbox" id="" value="'+name+'"/>'+name+'</td></tr>';
	               $table.append(tr);
	           }
	           //设置弹出框内容
			   $('#selectSku').modal();
	        }
	    });
    }
    
    
     function getUpdatePara(){
	    
	        // 操作类型	
		    var operateType='${parameters.operateType?if_exists}';
		    // 商品编号	                 
			var productId=$("#productId").val();
			// 商品类型	                      
			var productTypeId='${parameters.productTypeId?if_exists}';
			// 商品分类	              
			var productCategoryId='${parameters.productCategoryId?if_exists}';
            // 商品名称	
            var productName=$("#productName").val();
            // 商品副标题	
            var productSubheadName=$("#productSubheadName").val();
            // 是否上架	
            var isOnline=$('.radio').find('input[name=isOnline]:checked').val();
            // 销售开始时间	
            var startTime=$("#startTime").val();
            // 销售结束时间	
            var endTime=$("#endTime").val();
            // 商家名称	
            var businessPartyId=$("#businessPartyId").val();
            // 商品品牌	
            var brandId=$("#brandId").val();
            // 体积	
            var volume=$("#volume").val();
            // 重量	
            var weight=$("#weight").val();
            // 是否使用特征	
            var isUsedFeature=$('.radio').find('input[name=isUsedFeature]:checked').val();
            // seo关键字
            var seoKeyword=$("#seoKeyword").val();
            // 销售价格	
            var salePrice=$("#salePrice").val();
            // 市场价格	
            var marketPrice=$("#marketPrice").val();
            // 成本价格	
            var costPrice=$("#costPrice").val();
            // 取得商品标签
  			var tagIds = "";
			$('input[name="tag"]:checked').each(function(){ 
			    tagIds = tagIds + "," + $(this).val();
			}); 
			var productTags=tagIds.substr(1,tagIds.length);
		    // 取得参数信息
			productParameterInfos();
			var productParameterInfos1=$("#productParameterInfos").val();
  			// 取得商品分类属性信息
  			attrInfos();
  			var productAttrInfos=$("#productAttrInfos").val();
  			// 取得商品库存信息
  			facilityInfos();
  			var productFacilityInfos=$("#productFacilityInfos").val();
  			// 取得pc详情信息
  			$("#pcDetails").val(CKEDITOR.instances.pcDetails.getData()); 
  			var pcDetails=$("#pcDetails").val();
            // 取得移动详情信息
	  		$("#mobileDetails").val(CKEDITOR.instances.mobileDetails.getData());
	  		var mobileDetails=$("#mobileDetails").val();
	  		// 商品特征信息
            var productFeatureInfos=$("#productFeatureInfos").val();
	  			   
            // 图片信息
            var productContentInfos=$("#productContentInfos").val();
            // 商品关系信息
            var productAssocInfos=$("#productAssocInfos").val();
            
		    //////////////////////////////////////////
		  
		   document.location.href="<@ofbizUrl>updateProductFeatureGood?operateType="+operateType+"&"+
														   "productId="+productId+"&"+
														   "productTypeId="+productTypeId+"&"+
														   "productCategoryId="+productCategoryId+"&"+
														   "productName="+productName+"&"+
														   "productSubheadName="+productSubheadName+"&"+
														   "isOnline="+isOnline+"&"+
														   "startTime="+startTime+"&"+
														   "endTime="+endTime+"&"+
														   "businessPartyId="+businessPartyId+"&"+
														   "brandId="+brandId+"&"+
														   "volume="+volume+"&"+
														   "weight="+weight+"&"+
														   "isUsedFeature="+isUsedFeature+"&"+
														   "seoKeyword="+seoKeyword+"&"+
														   "salePrice="+salePrice+"&"+
														   "marketPrice="+marketPrice+"&"+
														   "costPrice="+costPrice+"&"+
														   "productTags="+productTags+"&"+
														   "productParameterInfos1="+productParameterInfos1+"&"+
														   "productAttrInfos="+productAttrInfos+"&"+
														   "productFacilityInfos="+productFacilityInfos+"&"+
														   "pcDetails="+pcDetails+"&"+
														   "mobileDetails="+mobileDetails+"&"+
														   "productFeatureInfos="+productFeatureInfos+"&"+
														   "productContentInfos="+productContentInfos+"&"+
														   "productAssocInfos="+productAssocInfos+"</@ofbizUrl>";
		 }
   
   
   
   // 取得关联商品列表信息
   function getProductAssocGoodListByIds(ids){
	  if (ids != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductAssocGoodListByIds</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'ids' : ids
	        },
	        success: function(data) {
	           var productAssocGoodList=data.productAssocGoodList;
	           for (var i=0;i<productAssocGoodList.length;i++){
	               var productAssocGood=productAssocGoodList[i];
	               var productInfo=productAssocGood.productInfo;
	               var salesPrice=productAssocGood.salesPrice;
	               var productGoodFeature=productAssocGood.productGoodFeature;
	               
	               var productId=productInfo.productId;
	               var productName=productInfo.productName;
	               var productTypeName="";
	               if(productInfo.productTypeId=="FINISHED_GOOD"){
	                  productTypeName="实物商品";
	               }else{
	                  productTypeName="虚拟商品";
	               }
	               var productContentId="";
	               var tr=' <tr><td>'+productId+'</td><td>'+productName+'</td><td>'+productTypeName+'</td><td>'+productContentId+'</td><td>'+productGoodFeature+'</td><td>'+salesPrice+'</td><td class="fc_td"><button type="button" class="js-button-assocgood btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	 			   $('.js-table_4>tbody').append(tr);
	           
	           
	           }
	        }
	    });
	  } 
   } 	
   
</script>

		
