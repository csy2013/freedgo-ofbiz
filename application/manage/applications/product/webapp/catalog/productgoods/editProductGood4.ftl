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
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/zzz.css</@ofbizContentUrl>" type="text/css"/>
<!-- Select2 -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/dist/css/AdminLTE.min.css</@ofbizContentUrl>">
<!-- Date Picker -->
<#--
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
-->
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>

<!-- Select2 -->
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>

<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>

<#assign curProductAdditionalImage1 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_1"})/>
<#assign curProductAdditionalImage2 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_2"})/>
<#assign curProductAdditionalImage3 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_3"})/>
<#assign curProductAdditionalImage4 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_4"})/>
<#assign curProductAdditionalImage5 = delegator.findByAnd("ProductContent", {"productId" : productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_5"})/>

<#if prod?has_content>

    <#if curProductAdditionalImage1?has_content>
        <#assign productAdditionalImage1="/content/control/getImage?contentId="+curProductAdditionalImage1.get(0).contentId?if_exists/>

    <#--<#assign productAdditionalImage1 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_1", locale, dispatcher))?if_exists />-->

    <#else>
        <#assign productAdditionalImage1 =""/>
    </#if>

    <#if curProductAdditionalImage2?has_content>
        <#assign productAdditionalImage2="/content/control/getImage?contentId="+curProductAdditionalImage2.get(0).contentId?if_exists/>
    <#--<#assign productAdditionalImage2 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_2", locale, dispatcher))?if_exists />-->
    <#else>
        <#assign productAdditionalImage2 =""/>
    </#if>


    <#if curProductAdditionalImage3?has_content>
        <#assign productAdditionalImage3="/content/control/getImage?contentId="+curProductAdditionalImage3.get(0).contentId?if_exists/>
    <#--<#assign productAdditionalImage3 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_3", locale, dispatcher))?if_exists />-->
    <#else>
        <#assign productAdditionalImage3 =""/>
    </#if>

    <#if curProductAdditionalImage4?has_content>
        <#assign productAdditionalImage4="/content/control/getImage?contentId="+curProductAdditionalImage4.get(0).contentId?if_exists/>
    <#--<#assign productAdditionalImage4 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_4", locale, dispatcher))?if_exists />-->
    <#else>
        <#assign productAdditionalImage4 =""/>
    </#if>

    <#if curProductAdditionalImage5?has_content>
        <#assign productAdditionalImage5="/content/control/getImage?contentId="+curProductAdditionalImage5.get(0).contentId?if_exists/>
    <#--<#assign productAdditionalImage5 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(prod, "ADDITIONAL_IMAGE_5", locale, dispatcher))?if_exists />-->
    <#else>
        <#assign productAdditionalImage5 =""/>
    </#if>

</#if>


<style type="text/css">
    #modPlateFormClass .modal-dialog {
        width: 1000px;
        margin: 30px auto;
    }
</style>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>              

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
            <#--
            <input type="hidden" name="productFacilityInfos" id="productFacilityInfos" value=""/>
             -->
            <input type="hidden" name="productContentInfos" id="productContentInfos" value=""/>
          
            <input type="hidden" name="productAssocInfos" id="productAssocInfos" value=""/>
            <input type="hidden" name="productFeatureInfos" id="productFeatureInfos" value=""/>
            
            <input type="hidden" id="pcDetailsContent"  value="<#if prod?has_content>${prod.pcDetails?if_exists}</#if>">
            <input type="hidden" id="mobileDetailsContent"  value="<#if prod?has_content>${prod.mobileDetails?if_exists}</#if>">
            <#--<input type="hidden" id="pcDetailsContent"  value="">-->
            <#--<input type="hidden" id="mobileDetailsContent"  value="">-->
            <input type="hidden" id="productTags" name="productTags" value="">
            
            <input type="hidden" id="productFeatureId" name="productFeatureId" value="">

	        <input type="hidden" id="linkId" />
	        <input type="hidden" id="selectName" />

			<!--店铺编码-->
            <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
            <!--自营与非自营用户标识 Y:自营 N:非自营-->
			<input type="hidden" id="isInner" name="isInner" value="${requestAttributes.isInner}"/>
            <#--${requestAttributes.userLogin.partyId?if_exists}-->

            <#--<input type="hidden" id="isInner" name="isInner" value="Y"/>-->

	        
	        
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
	                <div class="form-group col-sm-6" >
	                    <label for="subTitle" class="col-sm-3 control-label">商品描述:</label>
	                    <div class="col-sm-9" >
	                        <input type="text" class="form-control" id="productSubheadName" name="productSubheadName" value="${prod.productSubheadName?if_exists}">
	                    </div>
	                </div>
               </div>
               
               <div class="row">
                <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
                    <#assign produtTypeTag="ProdutTypeTag_1"/>
                <#elseif parameters.productTypeId?if_exists=="VIRTUAL_GOOD">
                    <#assign produtTypeTag="ProdutTypeTag_2"/>
                </#if>
                  <#assign productTagList = delegator.findByAnd("Tag", {"tagTypeId" : "${produtTypeTag?if_exists}","isDel":"N"})/>
                  <#assign curCategoryInfo = delegator.findByPrimaryKey("ProductCategory", Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", "${prod.primaryProductCategoryId?if_exists}"))>
	              
	                <div class="form-group col-sm-6">
		                <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品分类:</label>
		                <div class="col-sm-9">
		                     <input type="text" class="form-control" id="primaryProductCategoryId" name="primaryProductCategoryId" value="<#if curCategoryInfo?has_content>${curCategoryInfo.categoryName?if_exists}</#if>" readonly>
		                </div>
		            </div>
		            <div class="form-group col-sm-6">
	                  <label  class="col-sm-3 control-label">商品标签:</label>
	                  <div class="col-sm-7">
		                  <div class="checkbox clearfix js-product-tag">
		                    <#list productTagList as productTag>
		                         <label class="col-sm-3" title=""><input name="tag" id="${productTag.tagId}" value="${productTag.tagId}" type="checkbox">${productTag.tagName}</label>
		                    </#list>
		                  </div> 
	                   </div>
                       <div class="col-sm-2">
                            <button type="button" class="btn btn-primary js-addTag" id="btnAddTag">添加标签</button>
                       </div>
	               </div>
               </div>
	          
	          <#--是否上下架在更新的场合，不显示-->
               <input type="hidden" id="isOnline" name="isOnline" value="<#if prod?has_content>${prod.isOnline?if_exists}</#if>">
	           <#--
	            <div class="row">
		            <div class="form-group col-sm-6">
		                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>是否上架:</label>
		                <div class="col-sm-4">
		                    <div class="radio">
		                    
		                        <#if prod.isVerify?if_exists=="Y">
		                        <label class="col-sm-6"><input name="isOnline"  value="Y" type="radio" checked>上架</label>
	                            <label class="col-sm-6"><input name="isOnline" value="N" type="radio">下架</label>
	                            <#else>
	                            <label class="col-sm-6"><input name="isOnline"  value="Y" type="radio" >上架</label>
	                            <label class="col-sm-6"><input name="isOnline" value="N" type="radio" checked>下架</label>
	                            </#if>
		                    </div>
		                    <div class="dp-error-msg"></div>
		                </div>
		            </div>
		             <div class="form-group col-sm-6">
		                <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：商品上架后才可在店铺显示和下单销售</div></div>
		            </div>
                </div>
	            -->

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
                                        <#--<label class="col-sm-3" title=""><input name="supportServiceType" id="${supportServiceTypeInfo.enumId}" value="${supportServiceTypeInfo.enumId}" type="checkbox">${supportServiceTypeInfo.description}</label>-->
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
	                <#--
	                <div class="form-group col-sm-6 js-startTime" data-type="required,linkLt" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间">
	                -->
	                <div class="form-group col-sm-6 js-startTime" data-type="required" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间">
	                    <#--
	                    <label for="startTime" class="col-sm-3 control-label"><i class="required-mark">*</i>销售开始时间:</label>
	                    -->
	                    <label for="startTime" class="col-sm-3 control-label"><i class="required-mark">*</i>销售开始时间:</label>
	                    <div class="col-sm-9">
		                    <div class="input-group date form_datetime col-sm-12" data-link-field="startTime">
		                        <input class="form-control" size="16" type="text" value="<#if prod.introductionDate?has_content >${prod.introductionDate?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>" readonly>
		                        <input id="startTime" name="startTime" class="dp-vd" type="hidden" value="<#if prod.introductionDate?has_content >${prod.introductionDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>">
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
	                <div id="endTimeGroup" class="form-group col-sm-6">
	                    <label for="endTime" class="col-sm-3 control-label">销售结束时间:</label>
	                    <div class="col-sm-9">
		                    <div class="input-group date form_datetime col-sm-12"  data-link-field="endTime">
		                        <input class="form-control" size="16" type="text"  value="<#if prod.salesDiscontinuationDate?has_content >${prod.salesDiscontinuationDate?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>" readonly>
		                        <input id="endTime" name="endTime" class="dp-vd" type="hidden" value="<#if prod.salesDiscontinuationDate?has_content >${prod.salesDiscontinuationDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>">
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
		                    </div>
		                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
	                    </div>
	                </div>
	            </div>
	            <#---
	            <div class="row">
	                <div id="endTimeGroup" class="form-group col-sm-6">
	                    <label for="endTime" class="col-sm-3 control-label">销售结束时间:</label>
	                    <div class="col-sm-9">
		                    <div class="input-group date form_datetime col-sm-12"  data-link-field="endTime">
		                        <input class="form-control" size="16" type="text"  value="<#if prod.salesDiscontinuationDate?has_content >${prod.salesDiscontinuationDate?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>" readonly>
		                        <input id="endTime" name="endTime" class="dp-vd" type="hidden" value="<#if prod.salesDiscontinuationDate?has_content >${prod.salesDiscontinuationDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>">
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
		                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
		                    </div>
		                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
	                    </div>
	                </div>
	            </div>
	            -->
            
	            <div class="row"> 
	                <#--<div class="form-group col-sm-6" data-type="required" data-mark="商家名称">-->
	                      <#--<#assign mrchantNameList = delegator.findByAnd("GetMrchantNameList",{"auditStatus" : "1","statusId":"PARTY_ENABLED"})/>-->
	                    <#--<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商家名称:</label>-->
	                    <#--<div class="col-sm-9">-->
		                        <#--<select class="form-control select2MrchantName dp-vd" id="businessPartyId" name="businessPartyId">-->
				                      <#--<#if prod.businessPartyId?has_content>-->
					                        <#--<#list mrchantNameList as mrchantNameInfo>-->
						                        <#--<#if prod.businessPartyId==mrchantNameInfo.partyId>-->
				                                 <#--<option value="${mrchantNameInfo.partyId}" selected="selected">${mrchantNameInfo.businessName}</option>-->
				                                <#--<#else>-->
				                                  <#--<option value="${mrchantNameInfo.partyId}">${mrchantNameInfo.businessName}</option>-->
				                                <#--</#if>-->
			                                <#--</#list>-->
				                      <#--<#else>-->
					                       <#--<option value=""></option>-->
					                       <#--<#list mrchantNameList as mrchantNameInfo>-->
			                               	   <#--<option value="${mrchantNameInfo.partyId}">${mrchantNameInfo.businessName}</option>-->
				                      	   <#--</#list>-->
				                      <#--</#if>-->
			                    <#--</select>-->
	                        <#---->
	                         <#--&lt;#&ndash;-->
	                        <#--<input type="text" class="form-control dp-vd" id="businessPartyId" name="businessPartyId" value="${prod.businessPartyId?if_exists}">-->
	                        <#--&ndash;&gt;-->
	                        <#--<p class="dp-error-msg"></p>-->
	                         <#---->
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
	                <div class="form-group col-sm-6" >
	                    <#--<#assign productBrandList = delegator.findByAnd("ProductBrandCategory",{"productCategoryId" : "${prod.primaryProductCategoryId?if_exists}"})/>-->

	                    <label for="subTitle" class="col-sm-3 control-label" >商品品牌:</label>
	                    <div class="col-sm-9" >
		                    <select class="form-control select2Brand" id="brandId" name="brandId">
		                      <#--<#if prod.brandId?has_content>-->

			                      <#--<#list productBrandList as productBrand>-->
			                             <#--&lt;#&ndash;-->
			                            <#--<#assign  brandInfo = delegator.findByPrimaryKey("ProductBrand",{"productBrandId" : "${productBrand.productBrandId}"})>-->

			                            <#--<#assign  brandInfo = delegator.findByPrimaryKey("ProductBrand", Static["org.ofbiz.base.util.UtilMisc"].toMap("productBrandId", "${productBrand.productBrandId?if_exists}"))>-->
			                            <#--&ndash;&gt;-->
                                       <#--<#assign brandInfo=""/>-->

                                      <#--<#if requestAttributes.isInner?if_exists=="Y">-->
                                          <#--<#assign  brandInfos = delegator.findByAnd("ProductBrand",{"productBrandId" : productBrand.productBrandId})/>-->
                                      <#--<#else>-->
                                          <#--<#assign  brandInfos = delegator.findByAnd("ProductBrandByPsId",{"productBrandId" : productBrand.productBrandId,"productStoreId":"${requestAttributes.productStoreId}"})/>-->
                                      <#--</#if>-->

			                            <#--&lt;#&ndash;<#assign  brandInfos = delegator.findByAnd("ProductBrand",{"productBrandId" : productBrand.productBrandId})/>&ndash;&gt;-->
	                                    <#--<#if brandInfos?has_content>-->
						                   <#--<#assign brandInfo = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(brandInfos) />-->
						                <#--</#if>-->

			                            <#--<#if brandInfo?has_content>-->
			                                <#--<#if brandInfo.isUsed=="Y">-->
						                        <#--<#if prod.brandId==brandInfo.productBrandId>-->
				                                 <#--<option value="${brandInfo.productBrandId}" selected="selected">${brandInfo.brandName}</option>-->
				                                <#--<#else>-->
				                                 <#--<option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>-->
				                                <#--</#if>-->
			                                <#--</#if>-->
		                                <#--</#if>-->
			                      <#--</#list>-->
		                      <#--<#else>-->
		                      <#--<option value=""></option>-->

		                      <#--<#list productBrandList as productBrand>-->
		                           <#--<#assign  brandInfos = delegator.findByAnd("ProductBrand",{"productBrandId" : productBrand.productBrandId})/>-->
                                   <#--<#if brandInfos?has_content>-->
					                   <#--<#assign brandInfo = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(brandInfos) />-->
					               <#--</#if>-->
		                           <#--&lt;#&ndash;-->
		                           <#--<#assign  brandInfo = delegator.findByPrimaryKey("ProductBrand",{"productBrandId":productBrand.productBrandId})>-->
		                           <#--&ndash;&gt;-->
		                           <#--<#if brandInfo?has_content>-->
		                               <#--<#if brandInfo.isUsed=="Y">-->
	                               	       <#--<option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>-->
	                                   <#--</#if>-->
	                               <#--</#if>-->
		                      <#--</#list>-->
		                      <#--</#if>-->



                              <#if prod.brandId?has_content>


                                  <#assign brandInfos=""/>

                                  <#if requestAttributes.isInner?if_exists=="Y">
                                      <#assign  brandInfos = delegator.findByAnd("ProductBrand")/>
                                      <#if brandInfos?has_content>
                                          <#list brandInfos as brandInfo>
                                              <#if brandInfo.isUsed=="Y"&& brandInfo.auditStatus?default("0")="1">
                                                  <#if prod.brandId==brandInfo.productBrandId>
                                                      <option value="${brandInfo.productBrandId}" selected="selected">${brandInfo.brandName}</option>
                                                  <#else>
                                                      <option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>
                                                  </#if>
                                              </#if>
                                          </#list>
                                      </#if>
                                  <#else>
                                      <#assign  brandInfos = delegator.findByAnd("ProductBrandByPsId",{"productStoreId":"${requestAttributes.productStoreId}"})/>
                                      <#if brandInfos?has_content>
                                          <#list brandInfos as brandInfo>
                                              <#if brandInfo.isUsed=="Y" && brandInfo.auditStatus?default("0")="1" && brandInfo.pIsDel?default("N")='N' && brandInfo.pAuditStatus?default("0")='1'>
                                                  <#if prod.brandId==brandInfo.productBrandId>
                                                      <option value="${brandInfo.productBrandId}" selected="selected">${brandInfo.brandName}</option>
                                                  <#else>
                                                      <option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>
                                                  </#if>
                                              </#if>
                                          </#list>
                                      </#if>
                                  </#if>
                              <#else>
                                  <#assign brandInfos=""/>
                                  <#if requestAttributes.isInner?if_exists=="Y">
                                      <#assign  brandInfos = delegator.findByAnd("ProductBrand")/>
                                      <#if brandInfos?has_content>
                                          <#list brandInfos as brandInfo>
                                              <#if brandInfo.isUsed=="Y" && brandInfo.auditStatus="1">
                                                  <option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>
                                              </#if>
                                          </#list>

                                      </#if>
                                  <#else>
                                      <#assign  brandInfos = delegator.findByAnd("ProductBrandByPsId",{"productStoreId":"${requestAttributes.productStoreId}"})/>
                                      <#if brandInfos?has_content>
                                          <#list brandInfos as brandInfo>
                                              <#if brandInfo.isUsed=="Y" && brandInfo.auditStatus="1">
                                                  <option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>
                                              </#if>
                                          </#list>

                                      </#if>
                                  </#if>
                              </#if>

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

	                <div class="form-group col-sm-6" data-type="required,format" data-reg="/^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$/"  data-mark="销售价格">
	                    <label  class="col-sm-3 control-label"><i class="required-mark">*</i>销售价格(元):</label>
	                    <div class="col-sm-9">
		                    <input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="${priceSale?if_exists}" placeholder="" />
		                    <div class="dp-error-msg"></div>
	                    </div>
	                </div>
					<div class="form-group col-sm-6" >
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
                                <select class="form-control  dp-vd" id="integralDeductionType" name="integralDeductionType"   >
                                    <option value=""></option>
                                    <option value="1" selected="selected">不可使用积分</option>
                                    <option value="2">百分比抵扣</option>
                                    <option value="3">固定金额抵扣</option>
                                </select>

                                <p class="dp-error-msg"></p>

                            </div>
                        </div>

                        <div class="form-group col-sm-6 js-integral-upper" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="积分抵扣上限" >
                            <label for="title" class="col-sm-3 control-label">积分抵扣上限:</label>
                            <div class="col-sm-8">
                                <input type="text" class="form-control dp-vd" id="integralDeductionUpper" name="integralDeductionUpper" value="${prod.integralDeductionUpper?if_exists}" placeholder="" />
                                <p class="dp-error-msg"></p>
                            </div>
                            <label for="title" class="col-sm-1 js-integral-upper-name control-label"></label>
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
                        <div class="form-group col-sm-6" data-type="required,format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="代金券面额">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>代金券面额:</label>
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
                                    <div class="radio">
                                        <#if prod.isListShow?if_exists=='Y'>
                                            <label class="col-sm-6"><input name="isListShow"  value="Y" type="radio" checked>是</label>
                                            <label class="col-sm-6"><input name="isListShow" value="N" type="radio" >否</label>
                                        <#else>
                                            <label class="col-sm-6"><input name="isListShow"  value="Y" type="radio">是</label>
                                            <label class="col-sm-6"><input name="isListShow" value="N" type="radio" checked>否</label>
                                        </#if>
                                    </div>
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

                        <div class="form-group col-sm-6 js-startTime" data-type="required,linkLt" data-compare-link="useEndTimeGroup" data-mark="使用开始时间" data-compare-mark="使用结束时间">

                            <label for="startTime" class="col-sm-3 control-label"><i class="required-mark">*</i>使用开始时间:</label>
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

                        <div id="useEndTimeGroup" class="form-group col-sm-6" data-type="required,linkGt" data-compare-link="startTimeGroup" data-mark="结束时间" data-compare-mark="开始时间">
                            <label for="useEndTime" class="col-sm-3 control-label"><i class="required-mark">*</i>使用结束时间:</label>
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

                <div class="row title">
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
	                <div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="市场价格">
	                    <label for="title" class="col-sm-3 control-label">市场价格(元):</label>
	                    <div class="col-sm-9" >
	                        <input type="text" class="form-control dp-vd" id="marketPrice" name="marketPrice" value="${priceMarket?if_exists}" placeholder="" />
	                        <p class="dp-error-msg"></p>
	                    </div>
	                </div>
	                <div class="form-group col-sm-6"   data-type="required,format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="成本价格">
	                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>成本价格(元):</label>
	                    <div class="col-sm-9">
	                        <input type="text" class="form-control dp-vd" id="costPrice" name="costPrice" value="${priceCost?if_exists}" placeholder="" />
	                        <p class="dp-error-msg"></p>
	                    </div>
	                </div>
	            </div>

            <div class="row title">
                <div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="体积">
                    <label for="title" class="col-sm-3 control-label">体积(m³):</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="volume" name="volume" value="${prod.volume?if_exists}" placeholder="" />
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="重量">
                    <label for="subTitle" class="col-sm-3 control-label">重量(kg):</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="weight" name="weight" value="${prod.weight?if_exists}" placeholder="" />
                        <p class="dp-error-msg"></p>
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
				                              <td><i class="required-mark">*</i>${productCategoryAttribute.attrName}<input type="hidden" name="" id="" value="${productCategoryAttribute.productCategoryId}">
				                                   <input type="hidden" name="hidAttrName" id="hidAttrName" value="${productCategoryAttribute.attrName}">
				                              </td>
									          <td>
									              <#assign ProductCategoryattributeAssocList = delegator.findByAnd("ProductCategoryattributeAssoc", {"productId":"${prod.productId?if_exists}","productCategoryId":productCategoryAttribute.productCategoryId,"attrName":productCategoryAttribute.attrName})/>
									              <#assign productOptionList = delegator.findByAnd("ProductOption", {"productCategoryId" : "${prod.primaryProductCategoryId?if_exists}","attrName":productCategoryAttribute.attrName})/>
									              <div class="form-group  col-sm-12" data-type="required" data-mark="商品属性(${productCategoryAttribute.attrName})">
									                <select class="form-control dp-vd">
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
				                                    <p class="dp-error-msg"></p>
				                                   </div>
									          </td>
									          <#else>
									            <td>${productCategoryAttribute.attrName}<input type="hidden" name="" id="" value="${productCategoryAttribute.productCategoryId}">
									                <input type="hidden" name="hidAttrName" id="hidAttrName" value="${productCategoryAttribute.attrName}">
									            </td>
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
		                    <select class="form-control  dp-vd select2Feature" multiple="multiple" data-placeholder="Select a State">
		                      <#list productFeatureTypeList as productFeatureType>
		                          <#if productFeatureType.productFeatureTypeName?if_exists != "">
		                          <option value="${productFeatureType.productFeatureTypeId?if_exists}">${productFeatureType.productFeatureTypeName?if_exists}</option>
		                          </#if>
		                      </#list>
		                    </select>
		                </div>
		            </div>

                    <div class="form-group col-sm-6">
                        <div class="col-sm-9">
                            <button type="button" class="btn btn-primary js-addProductFeature" id="btnAddProductFeature">添加规格</button>
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
		            <div class="form-group col-sm-6" data-type="required" data-mark="商品库存">
		                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品库存:</label>
		                <div class="col-sm-9">
		                    <button type="button" class="btn btn-primary" id="btnAddProductFacility">选择仓库</button>
		                    <input type="hidden" class="form-control dp-vd" name="productFacilityInfos" id="productFacilityInfos" value=""/>
	                        <p class="dp-error-msg"></p>
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
	            <div class="form-group col-sm-6" data-type="required" data-mark="商品主图片">
	                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品图片:</label>
	                <div class="col-sm-9">
	                    <button type="button" class="btn btn-primary" id="btnAddProductContent">添加图片</button>
	                    <input id="mainImg" name="mainImg" class="dp-vd" type="hidden">
					    <p class="dp-error-msg"></p>
	                </div>
	            </div>

            </div> 				<div class="form-group col-sm-6">
                <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：最多可添加5张照片，第一张图为主图，请保证所有图片比例一致。大小为750*750px</div></div>
            </div>

            <#--<div class="row">-->
                <#--<div class="form-group col-sm-8">-->
                    <#--<label  class="col-sm-2 control-label"></label>-->
				    <#--<div class="col-sm-2 js-img1">-->
					     <#--<img height="150" alt="" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>" id="img1" style="height:100px;width:100px;"> -->
				         <#--<#if productAdditionalImage1?has_content>-->
	                          <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>-->
	                          <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg1">编辑</button>-->
                         <#--</#if>-->
				    <#--</div>-->
				    <#--<div class="col-sm-2 js-img2">-->
				         <#--<img height="150" alt="" src="<#if productAdditionalImage2?has_content><@ofbizContentUrl>${productAdditionalImage2}</@ofbizContentUrl></#if>" id="img2" style="height:100px;width:100px;"> -->
				          <#--<#if productAdditionalImage1?has_content>-->
                              <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>-->
                              <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg2">编辑</button>-->
                          <#--</#if>-->
				    <#--</div>-->
				    <#--<div class="col-sm-2 js-img3">-->
				          <#--<img height="150" alt="" src="<#if productAdditionalImage3?has_content><@ofbizContentUrl>${productAdditionalImage3}</@ofbizContentUrl></#if>" id="img3" style="height:100px;width:100px;">-->
					      <#--<#if productAdditionalImage1?has_content>-->
                              <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>-->
                              <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg3">编辑</button>-->
                          <#--</#if>-->
				    <#--</div>-->
				    <#--<div class="col-sm-2 js-img4">-->
				          <#--<img height="150" alt="" src="<#if productAdditionalImage4?has_content><@ofbizContentUrl>${productAdditionalImage4}</@ofbizContentUrl></#if>" id="img4" style="height:100px;width:100px;">-->
					      <#--<#if productAdditionalImage1?has_content>-->
                             <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>-->
                             <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg4">编辑</button>-->
                          <#--</#if>-->
				    <#--</div>-->
				    <#--<div class="col-sm-2 js-img5">-->
				          <#--<img height="150" alt="" src="<#if productAdditionalImage5?has_content><@ofbizContentUrl>${productAdditionalImage5}</@ofbizContentUrl></#if>" id="img5" style="height:100px;width:100px;">-->
					      <#--<#if productAdditionalImage1?has_content>-->
                              <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>-->
                              <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg5">编辑</button>-->
                          <#--</#if>-->
					      <#--&lt;#&ndash;-->
					      <#--<#if productAdditionalImage5?has_content>-->
					          <#--<img height="150" alt="" src="<@ofbizContentUrl>${productAdditionalImage5}</@ofbizContentUrl>" id="img5" style="height:100px;width:100px;">-->
					      <#--<#else>-->
					          <#--<img height="150" alt="" src="" id="img5" style="height:100px;width:100px;"> -->
					      <#--</#if>-->
					      <#--&ndash;&gt;-->
				    <#--</div>-->
				     <#---->
                <#--</div>-->
            <#--</div>-->

                <div class="row">
                    <div class="form-group col-sm-8">
                        <label  class="col-sm-2 control-label"></label>
                        <div class="col-sm-2 js-img1">
                            <img height="150" alt="" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>" id="img1" style="height:100px;width:100px;">

                            <input type="hidden" name="addImg1" id="addImg1" class="js-img-group" value="<#if productAdditionalImage1?has_content>${Static["org.ofbiz.entity.util.EntityUtil"].getFirst(curProductAdditionalImage1).contentId?if_exists}</#if>">
                            <#if productAdditionalImage1?has_content>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg1">编辑</button>
                            <#else>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg1">添加</button>
                            </#if>
                        </div>
                        <div class="col-sm-2 js-img2">
                            <img height="150" alt="" src="<#if productAdditionalImage2?has_content><@ofbizContentUrl>${productAdditionalImage2}</@ofbizContentUrl></#if>" id="img2" style="height:100px;width:100px;">
                            <input type="hidden" name="addImg2" id="addImg2" class="js-img-group" value="<#if productAdditionalImage2?has_content>${Static["org.ofbiz.entity.util.EntityUtil"].getFirst(curProductAdditionalImage2).contentId?if_exists}</#if>">
                            <#if productAdditionalImage2?has_content>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg2">编辑</button>
                            <#else>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg2">添加</button>
                            </#if>
                        </div>
                        <div class="col-sm-2 js-img3">
                            <img height="150" alt="" src="<#if productAdditionalImage3?has_content><@ofbizContentUrl>${productAdditionalImage3}</@ofbizContentUrl></#if>" id="img3" style="height:100px;width:100px;">
                            <input type="hidden" name="addImg3" id="addImg3" class="js-img-group" value="<#if productAdditionalImage3?has_content>${Static["org.ofbiz.entity.util.EntityUtil"].getFirst(curProductAdditionalImage3).contentId?if_exists}</#if>">
                            <#if productAdditionalImage3?has_content>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg3">编辑</button>
                            <#else>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg3">添加</button>
                            </#if>
                        </div>
                        <div class="col-sm-2 js-img4">
                            <img height="150" alt="" src="<#if productAdditionalImage4?has_content><@ofbizContentUrl>${productAdditionalImage4}</@ofbizContentUrl></#if>" id="img4" style="height:100px;width:100px;">
                            <input type="hidden" name="addImg4" id="addImg4" class="js-img-group" value="<#if productAdditionalImage4?has_content>${Static["org.ofbiz.entity.util.EntityUtil"].getFirst(curProductAdditionalImage4).contentId?if_exists}</#if>">
                            <#if productAdditionalImage4?has_content>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg4">编辑</button>
                            <#else>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg4">添加</button>
                            </#if>
                        </div>
                        <div class="col-sm-2 js-img5">
                            <img height="150" alt="" src="<#if productAdditionalImage5?has_content><@ofbizContentUrl>${productAdditionalImage5}</@ofbizContentUrl></#if>" id="img5" style="height:100px;width:100px;">
                            <input type="hidden" name="addImg5" id="addImg5" class="js-img-group" value="<#if productAdditionalImage5?has_content>${Static["org.ofbiz.entity.util.EntityUtil"].getFirst(curProductAdditionalImage5).contentId?if_exists}</#if>">
                            <#if productAdditionalImage5?has_content>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg5">编辑</button>
                            <#else>
                                <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>
                                <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg5">添加</button>
                            </#if>
                        <#--
                        <#if productAdditionalImage5?has_content>
                            <img height="150" alt="" src="<@ofbizContentUrl>${productAdditionalImage5}</@ofbizContentUrl>" id="img5" style="height:100px;width:100px;">
                        <#else>
                            <img height="150" alt="" src="" id="img5" style="height:100px;width:100px;">
                        </#if>
                        -->
                        </div>

                    </div>
                </div>


                <#if parameters.productTypeId?if_exists=="VIRTUAL_GOOD">
                <div class="row">
                    <div class="form-group col-sm-6">
                        <label  class="col-sm-3 control-label">关联商品:</label>
                        <div class="col-sm-3">
                            <button type="button" class="btn btn-primary" id="btnAddProductAssocGood">添加关联商品</button>
                        </div>
                        <div class="form-group col-sm-6">
                            <div class="col-sm-12"><div class="col-sm-12 dp-form-remarks">注：若无关联商品,则适用全渠道商品</div></div>
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
					               <th>商品编码</th><th>商品名称</th> <th>商品类型</th> <th>商品图片</th> <th>商品规格</th> <th>商品价格(元)</th><th>${uiLabelMap.BrandOption}</th>
					           </tr>
					      </thead>
					      <tbody>
					      </tbody>
		    		  </table>
                   </div>
                </div>
            </div>
			</#if>

            <div class="row">
	            <div class="form-group col-sm-6">
	               <label for="seo" class="col-sm-3 control-label">关键字:</label>
	            </div>
                <div class="form-group col-sm-6">
                    <div class="col-sm-12"><div class="col-sm-12 dp-form-remarks">注：输入关键字用空格分开</div></div>
                </div>
            </div>

            <div class="row">
                <div class="form-group">
                   <label  class="col-sm-1 control-label"></label>
                   <div class="col-sm-8">
                       <textarea class="form-control" name="seoKeyword" id="seoKeyword" rows="6" value="">${prod.seoKeyword?if_exists}</textarea>
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
            </div>
	        <#else>

	        <div class="row">
			    <div class="form-group col-sm-6">
	                <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品编号:</label>
	                <div class="col-sm-9">
	                     <input type="hidden" id="productId" name="productId" value="${parameters.productId?if_exists}">
	                     <input type="text" class="form-control" id="" name="" value="" readonly>
	                </div>
	            </div>
	            <div class="form-group col-sm-6">
	                <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品类型:</label>
	                <div class="col-sm-9">
	                     <input type="text" class="form-control" id="" name="" value="<#if parameters.productTypeId?if_exists=="FINISHED_GOOD">实物商品<#else>虚拟商品</#if>" readonly>
	                </div>
	            </div>
            </div>

            <div class="row title">
                <div class="form-group col-sm-6" data-type="required" data-mark="商品标题">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品标题:</label>
                    <div class="col-sm-9" >
                        <input type="text" class="form-control dp-vd" id="productName" name="productName" value="">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6" >
                    <label for="subTitle" class="col-sm-3 control-label">商品描述:</label>
                    <div class="col-sm-9" >
                        <input type="text" class="form-control" id="productSubheadName" name="productSubheadName" value="">
                    </div>
                </div>
            </div>

            <div class="row">

                <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
                    <#assign produtTypeTag="ProdutTypeTag_1"/>
                <#elseif parameters.productTypeId?if_exists=="VIRTUAL_GOOD">
                    <#assign produtTypeTag="ProdutTypeTag_2"/>
                </#if>
                <#assign productTagList = delegator.findByAnd("Tag", {"tagTypeId" : "${produtTypeTag?if_exists}","isDel":"N"})/>

                <#assign curCategoryInfo = delegator.findByPrimaryKey("ProductCategory", Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", "${parameters.productCategoryId?if_exists}"))>
                <div class="form-group col-sm-6">
	                <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品分类:</label>
	                <div class="col-sm-9">
	                     <input type="text" class="form-control" id="primaryProductCategoryId" name="primaryProductCategoryId" value="<#if curCategoryInfo?has_content>${curCategoryInfo.categoryName?if_exists}</#if>" readonly>
	                </div>
	            </div>

	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label">商品标签:</label>
	                <div class="col-sm-7">
	                   <div class="checkbox clearfix js-product-tag">
	                    <#list productTagList as productTag>
	                     <label class="col-sm-3" title=""><input name="tag" id="tag" value="${productTag.tagId}" type="checkbox">${productTag.tagName}</label>
	                    </#list>
	                   </div>
	                </div>
                    <div class="col-sm-2">
                        <#--<a href="">添加</a>-->
                        <button type="button" class="btn btn-primary js-addTag" id="btnAddTag">添加标签</button>
                    </div>
	            </div>
            </div>

            <div class="row">
	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>是否申请上架:</label>
	                <div class="col-sm-9">
	                    <div class="radio">
	                        <label class="col-sm-6"><input name="isOnline"  value="Y" type="radio">发起申请</label>
                            <label class="col-sm-6"><input name="isOnline" value="N" type="radio" checked>暂不申请</label>
	                    </div>
	                    <div class="dp-error-msg"></div>
	                </div>
	            </div>
	            <div class="form-group col-sm-6">
	                <div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">注：商品上架后才可在店铺显示和下单销售</div></div>
	            </div>
            </div>
			<#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
				<div class="row isFinished" >
					<#--<div class="form-group col-sm-6" style="display:none">-->
						<#--<label  class="col-sm-3 control-label">推荐到首页:</label>-->
						<#--<div class="col-sm-9">-->
							<#--<div class="radio">-->
								<#--<label class="col-sm-6"><input name="isRecommendHomePage"  value="Y" type="radio">是</label>-->
								<#--<label class="col-sm-6"><input name="isRecommendHomePage" value="N" type="radio" checked>否</label>-->
							<#--</div>-->
							<#--<div class="dp-error-msg"></div>-->
						<#--</div>-->
					<#--</div>-->
					<div class="form-group col-sm-6">
						<label  class="col-sm-3 control-label">是否保税商品:</label>
						<div class="col-sm-9">
							<div class="radio">
								<label class="col-sm-6"><input name="isBondedGoods"  value="Y" type="radio">是</label>
								<label class="col-sm-6"><input name="isBondedGoods" value="N" type="radio" checked>否</label>
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
                                        <option value="${providerInfo.providerId}">${providerInfo.providerName}</option>
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
								<#--<label class="col-sm-6"><input name="isRecommendHomePage"  value="Y" type="radio">是</label>-->
								<#--<label class="col-sm-6"><input name="isRecommendHomePage" value="N" type="radio" checked>否</label>-->
							<#--</div>-->
							<#--<div class="dp-error-msg"></div>-->
						<#--</div>-->
					<#--</div>-->
					<#--<div class="form-group col-sm-6">-->
						<#--<label  class="col-sm-3 control-label"><i class="required-mark">*</i>服务支持:</label>-->
						<#--<div class="col-sm-9">-->
							<#--<div class="checkbox clearfix">-->
								<#--<label class="col-sm-3" title=""><input name="isSupportService" id="isSupportService" value="1" type="checkbox" disabled="disabled">七天无理由退换货</label>-->
								<#--<#list supportServiceTypeList as supportServiceTypeInfo>-->
									<#--<#if supportServiceTypeInfo.enumId?if_exists=="SERVICE_SUPP_7DAYS">-->
                                    	<#--<label class="col-sm-3" title=""><input name="supportServiceType" id="${supportServiceTypeInfo.enumId}" value="${supportServiceTypeInfo.enumId}" type="checkbox">${supportServiceTypeInfo.description}</label>-->
									<#--</#if>-->
								<#--</#list>-->
							<#--</div>-->

							<#--<div class="dp-error-msg"></div>-->
						<#--</div>-->
					<#--</div>-->
                    <div class="form-group col-sm-6" data-type="" data-mark="">
                        <label for="title" class="col-sm-3 control-label">供应商:</label>
                        <#assign providerInfos = delegator.findByAnd("Provider")/>
                        <div class="col-sm-9" >
                            <select class="form-control  " id="providerId" name="providerId">
                                <option value=""></option>
                                <#list providerInfos as providerInfo>
                                    <option value="${providerInfo.providerId}">${providerInfo.providerName}</option>
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
							<#--</div>-->
							<div class="dp-error-msg"></div>
						</div>
					</div>
					<div class="form-group col-sm-6">
					</div>
				</div>
			</#if>


            <div class="row">

                <div class="form-group col-sm-6 js-startTime" data-type="required" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间">

                    <label for="startTime" class="col-sm-3 control-label"><i class="required-mark">*</i>销售开始时间:</label>
                    <div class="col-sm-9">
	                    <div class="input-group date form_datetime col-sm-12" data-link-field="startTime">
	                        <input class="form-control" size="16" type="text" readonly>
	                        <input id="startTime" name="startTime" class="dp-vd" type="hidden">
	                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
	                        <span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
	                    </div>
	                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>
                </div>

                <div id="endTimeGroup" class="form-group col-sm-6">
                    <label for="endTime" class="col-sm-3 control-label">销售结束时间:</label>
                    <div class="col-sm-9">
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
                <#--<div class="form-group col-sm-6" data-type="required" data-mark="商家名称">-->
                    <#--<#assign mrchantNameList = delegator.findByAnd("GetMrchantNameList",{"auditStatus" : "1","statusId":"PARTY_ENABLED"})/>-->
                    <#--<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商家名称:</label>-->
                    <#--<div class="col-sm-9" >-->
                        <#--<select class="form-control select2MrchantName dp-vd" id="businessPartyId" name="businessPartyId">-->
                           <#--<option value=""></option>-->
	                      <#--<#list mrchantNameList as mrchantNameInfo>-->
                                 <#--<option value="${mrchantNameInfo.partyId}">${mrchantNameInfo.businessName}</option>-->
	                      <#--</#list>-->
	                    <#--</select>-->

                        <#--<p class="dp-error-msg"></p>-->
                        <#---->
                    <#--</div>-->
                <#--</div>-->
				<div class="form-group col-sm-6" data-type="required" data-mark="主营分类">
					<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>主营分类:</label>
					<div class="col-sm-9" >
				        <#--<#if parameters.isInner?has_content>-->
						    <#--<#if parameters.isInner=="Y">-->
                                <#--<input type="text" class="form-control dp-vd" id="platformClassId" name="platformClassId" value="<#if curCategoryInfo?has_content>${curCategoryInfo.categoryName?if_exists}</#if>">-->
							<#--<#else>-->
                                <#--<input type="text" class="form-control dp-vd" id="platformClassId" name="platformClassId" value="">-->
							<#--</#if>-->
					    <#--</#if>-->
                        <#--<input type="text" class="form-control dp-vd" id="platformClassId" data-id="${parameters.productCategoryId?if_exists}" name="platformClassId" value="<#if curCategoryInfo?has_content>${curCategoryInfo.categoryName?if_exists}</#if>">-->
                        <#if requestAttributes.isInner?if_exists=="N">
                            <input type="text" class="form-control dp-vd" id="platformClassId" data-id="" name="platformClassId" value="">
                        <#else>
                            <input type="text" class="form-control dp-vd" id="platformClassId" data-id="${parameters.productCategoryId?if_exists}" name="platformClassId" value="<#if curCategoryInfo?has_content>${curCategoryInfo.categoryName?if_exists}</#if>">
                        </#if>
						<p class="dp-error-msg"></p>
					</div>
				</div>

                <div class="form-group col-sm-6" >
                    <#--<#assign productBrandList = delegator.findByAnd("ProductBrandCategory",{"productCategoryId" : "${parameters.productCategoryId?if_exists}"})/>-->
                    <label for="subTitle" class="col-sm-3 control-label" >商品品牌:</label>
                    <div class="col-sm-9" >
                        <select class="form-control select2Brand" id="brandId" name="brandId">
                          <option value=""></option>
	                      <#--<#list productBrandList as productBrand>-->
                                 <#--<#assign brandInfo=""/>-->
                                 <#--<#if requestAttributes.isInner?if_exists=="Y">-->
                                     <#--<#assign  brandInfos = delegator.findByAnd("ProductBrand",{"productBrandId" : productBrand.productBrandId})/>-->
                                 <#--<#else>-->
                                     <#--<#assign  brandInfos = delegator.findByAnd("ProductBrandByPsId",{"productBrandId" : productBrand.productBrandId,"productStoreId":"${requestAttributes.productStoreId}"})/>-->
                                 <#--</#if>-->
                                 <#--<#if brandInfos?has_content>-->
					                <#--<#assign brandInfo = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(brandInfos) />-->
					             <#--</#if>-->
                                 <#---->
                                 <#---->
		                         <#--<#if brandInfo?has_content>-->
		                             <#--<#if brandInfo.isUsed=="Y">-->
	                                    <#--<option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>-->
	                                   <#--</#if>-->
	                             <#--</#if>-->
	                      <#--</#list>-->


                            <#assign brandInfos=""/>
                            <#if requestAttributes.isInner?if_exists=="Y">
                                <#assign  brandInfos = delegator.findByAnd("ProductBrand")/>
                                <#if brandInfos?has_content>
                                    <#list brandInfos as brandInfo>
                                        <#if brandInfo.isUsed=="Y" && brandInfo.auditStatus?default("0")="1">
                                            <option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>
                                        </#if>
                                    </#list>

                                </#if>
                            <#else>
                                <#assign  brandInfos = delegator.findByAnd("ProductBrandByPsId",{"productStoreId":"${requestAttributes.productStoreId}"})/>
                                <#if brandInfos?has_content>
                                    <#list brandInfos as brandInfo>
                                        <#if brandInfo.isUsed=="Y" && brandInfo.auditStatus?default("0")="1" && brandInfo.pIsDel?default("N")='N' && brandInfo.pAuditStatus?default("0")='1'>
                                            <option value="${brandInfo.productBrandId}">${brandInfo.brandName}</option>
                                        </#if>
                                    </#list>

                                </#if>
                            </#if>
	                    </select>
                    </div>
				</div>
            </div>
            
           <div class="row">
                <div class="form-group col-sm-6" data-type="required,format"  data-reg="/^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$/" data-mark="销售价格">
                    <label  class="col-sm-3 control-label"><i class="required-mark">*</i>销售价格(元):</label>
                    <div class="col-sm-9">
	                    <input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="" placeholder="" />
	                    <div class="dp-error-msg"></div>
                    </div>
                    
                </div>
                <div class="form-group col-sm-6" >
                    <div class="col-sm-8"><div class="col-sm-12 dp-form-remarks">注：使用规格时，该价格作为商品的参考价展示</div></div>
                </div>
            </div>

			<#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
				<div class="row title isFinished">
					<div class="form-group col-sm-6" data-type="required" data-mark="积分抵扣">
						<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>积分抵扣:</label>
						<div class="col-sm-9" >
							<select class="form-control  dp-vd" id="integralDeductionType" name="integralDeductionType">
								<option value="1">不可使用积分</option>
								<option value="2">百分比抵扣</option>
								<option value="3">固定金额抵扣</option>
							</select>

							<p class="dp-error-msg"></p>

						</div>
					</div>

					<div class="form-group col-sm-6 js-integral-upper" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="积分抵扣上限">
						<label for="title" class="col-sm-3 control-label">积分抵扣上限:</label>
						<div class="col-sm-8">
							<input type="text" class="form-control dp-vd" id="integralDeductionUpper" name="integralDeductionUpper" value="" placeholder="" />
							<p class="dp-error-msg"></p>
						</div>
                        <label for="title" class="col-sm-1 js-integral-upper-name control-label"></label>
					</div>
				</div>


				<div class="row title isFinished">
					<div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="每人限购数量">
						<label for="title" class="col-sm-3 control-label">每人限购数量:</label>
						<div class="col-sm-9" >
							<input type="text" class="form-control dp-vd" id="purchaseLimitationQuantity" name="purchaseLimitationQuantity" value="" placeholder="" />
							<p class="dp-error-msg"></p>
						</div>
					</div>
					<div class="form-group col-sm-6" >
						<label for="subTitle" class="col-sm-3 control-label" >列表展示:</label>
						<div class="col-sm-9">
							<div class="radio">
								<label class="col-sm-6"><input name="isListShow"  value="Y" type="radio" checked>是</label>
								<label class="col-sm-6"><input name="isListShow" value="N" type="radio">否</label>
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
							<select class="form-control  dp-vd" id="integralDeductionType" name="integralDeductionType"  >
								<option value=""></option>
								<option value="1" selected="selected">不可使用积分</option>
								<option value="2">百分比抵扣</option>
								<option value="3">固定金额抵扣</option>
							</select>

							<p class="dp-error-msg"></p>

						</div>
					</div>

					<div class="form-group col-sm-6 js-integral-upper" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="积分抵扣上限" >
						<label for="title" class="col-sm-3 control-label">积分抵扣上限:</label>
						<div class="col-sm-8">
							<input type="text" class="form-control dp-vd" id="integralDeductionUpper" name="integralDeductionUpper" value="" placeholder="" />
							<p class="dp-error-msg"></p>
						</div>
                        <label for="title" class="col-sm-1 js-integral-upper-name control-label"></label>
					</div>
				</div>

				<div class="row title isVirtual">
					<div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="每人限购数量">
						<label for="title" class="col-sm-3 control-label">每人限购数量:</label>
						<div class="col-sm-8" >
							<input type="text" class="form-control dp-vd" id="purchaseLimitationQuantity" name="purchaseLimitationQuantity" value="" placeholder="" />
							<p class="dp-error-msg"></p>
						</div>

					</div>
					<div class="form-group col-sm-6" data-type="required,format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="代金券面额">
						<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>代金券面额:</label>
						<div class="col-sm-9" >
							<input type="text" class="form-control dp-vd" id="voucherAmount" name="voucherAmount" value="" placeholder="" />
							<p class="dp-error-msg"></p>
						</div>
					</div>

				</div>

				<div class="row title isVirtual">
					<div class="form-group col-sm-6" >
						<label for="subTitle" class="col-sm-3 control-label" >列表展示:</label>
						<div class="col-sm-9">
							<div class="radio">
								<label class="col-sm-6"><input name="isListShow"  value="Y" type="radio" checked>是</label>
								<label class="col-sm-6"><input name="isListShow" value="N" type="radio">否</label>
							</div>
							<div class="dp-error-msg"></div>
						</div>
					</div>
					<#--<div class="form-group col-sm-6" >-->
						<#--<label for="subTitle" class="col-sm-3 control-label" >使用限制:</label>-->
						<#--<div class="col-sm-9">-->

							<#--<div class="radio">-->
								<#--<label class="col-sm-6"><input name="useLimit"  value="Y" type="radio">是</label>-->
								<#--<label class="col-sm-6"><input name="useLimit" value="N" type="radio" checked>否</label>-->
							<#--</div>-->
							<#--<div class="dp-error-msg"></div>-->

						<#--</div>-->
					<#--</div>-->
				</div>


				<div class="row isVirtual">

					<div class="form-group col-sm-6 js-startTime" data-type="required,linkLt" data-compare-link="useEndTimeGroup" data-mark="使用开始时间" data-compare-mark="使用结束时间">

						<label for="startTime" class="col-sm-3 control-label"><i class="required-mark">*</i>使用开始时间:</label>
						<div class="col-sm-9">
							<div class="input-group date form_datetime col-sm-12" data-link-field="useStartTime">
								<input class="form-control" size="16" type="text" readonly>
								<input id="useStartTime" name="useStartTime" class="dp-vd" type="hidden">
								<span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
								<span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
							</div>
							<div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
						</div>
					</div>

					<div id="useEndTimeGroup" class="form-group col-sm-6" data-type="required,linkGt" data-compare-link="startTimeGroup"
                         data-mark="结束时间" data-compare-mark="开始时间">
						<label for="useEndTime" class="col-sm-3 control-label"><i class="required-mark">*</i>使用结束时间:</label>
						<div class="col-sm-9">
							<div class="input-group date form_datetime col-sm-12"  data-link-field="useEndTime">
								<input class="form-control" size="16" type="text" readonly>
								<input id="useEndTime" name="useEndTime" class="dp-vd" type="hidden">
								<span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
								<span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
							</div>
							<div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
						</div>
					</div>
				</div>
			</#if>
            
            <div class="row title">
                <div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="市场价格">
                    <label for="title" class="col-sm-3 control-label">市场价格(元):</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="marketPrice" name="marketPrice" value="" placeholder="" />
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6"   data-type="required,format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="成本价格">
                    <label for="subTitle" class="col-sm-3 control-label" ><i class="required-mark">*</i>成本价格(元):</label>
                    <div class="col-sm-9" >
                        <input type="text" class="form-control dp-vd" id="costPrice" name="costPrice" value="" placeholder="" />
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            
            
            <div class="row title">
                <div class="form-group col-sm-6" data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="体积">
                    <label for="title" class="col-sm-3 control-label">体积(m³):</label>
                    <div class="col-sm-9" >
                        <input type="text" class="form-control dp-vd" id="volume" name="volume" value="" placeholder="" />
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6"   data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="重量">
                    <label for="subTitle" class="col-sm-3 control-label" >重量(kg):</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="weight" name="weight" value="" placeholder="" />
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            
            
            <div class="row">
	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label">商品属性:</label>
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
				                              <td><i class="required-mark">*</i>${productCategoryAttribute.attrName}<input type="hidden" name="" id="" value="${productCategoryAttribute.productCategoryId}">
				                                   <input type="hidden" name="hidAttrName" id="hidAttrName" value="${productCategoryAttribute.attrName}">
				                              </td>
									                
									          <td>
									                <div class="form-group col-sm-12" data-type="required" data-mark="商品属性(${productCategoryAttribute.attrName})">
										                <#assign productOptionList = delegator.findByAnd("ProductOption", {"productCategoryId" : "${parameters.productCategoryId?if_exists}","attrName":productCategoryAttribute.attrName})/>
										                <select class="form-control dp-vd">
	                                                     <option value=""></option>
										                <#list productOptionList as productOption>
										                	<option value="${productOption.productOptionId}">${productOption.optionName}</option>
										                </#list>
					                                    </select>
					                                    <p class="dp-error-msg"></p>
				                                    </div>
									          </td>
				                          <#else>
				                              <td>${productCategoryAttribute.attrName}<input type="hidden" name="" id="" value="${productCategoryAttribute.productCategoryId}">
				                                   <input type="hidden" name="hidAttrName" id="hidAttrName" value="${productCategoryAttribute.attrName}">
				                              </td>
									          <td>
									                <#assign productOptionList = delegator.findByAnd("ProductOption", {"productCategoryId" : "${parameters.productCategoryId?if_exists}","attrName":productCategoryAttribute.attrName})/>
									                <select class="form-control">
                                                     <option value=""></option>
									                <#list productOptionList as productOption>
									                	<option value="${productOption.productOptionId}">${productOption.optionName}</option>
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

            <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
                <div class="row">
                    <div class="form-group col-sm-6" >
                        <label  class="col-sm-3 control-label"><i class="required-mark">*</i>是否使用规格:</label>
                        <div class="col-sm-9">
                            <div class="radio">
                                <label class="col-sm-6"><input name="isUsedFeature" value="N" type="radio" checked>直接创建商品</label>
                                <label class="col-sm-6"><input name="isUsedFeature" value="Y" type="radio">使用规格</label>
                            </div>
                            <div class="dp-error-msg"></div>
                        </div>

                    </div>
                </div>
            </#if>
            
            <div class="row js-feature-div" data-type="required"  data-mark="规格名称">
	            <div class="form-group col-sm-6">
	                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>规格名称:</label>
	                <div class="col-sm-9">
	                    <#assign productFeatureTypeList = delegator.findByAnd("ProductFeatureType")/>
	                    <select class="form-control dp-vd select2Feature" multiple="multiple" data-placeholder="Select a State">
	                      <#list productFeatureTypeList as productFeatureType>
	                       <#if productFeatureType.productFeatureTypeName?if_exists != "">
	                          <option value="${productFeatureType.productFeatureTypeId?if_exists}">${productFeatureType.productFeatureTypeName?if_exists}</option>
	                          </#if>
	                      </#list>
	                    </select>
	                </div>
	            </div>
                <div class="form-group col-sm-6">
                    <div class="col-sm-9">
                        <button type="button" class="btn btn-primary js-addProductFeature" id="btnAddProductFeature">添加规格</button>
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
		            <div class="form-group col-sm-6" data-type="required" data-mark="商品库存">
		                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品库存:</label>
		                <div class="col-sm-9">
		                    <button type="button" class="btn btn-primary" id="btnAddProductFacility">选择仓库</button>
		                    <input type="hidden" class="form-control dp-vd" name="productFacilityInfos" id="productFacilityInfos" value=""/>
	                        <p class="dp-error-msg"></p>
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
	            <div class="form-group col-sm-6" data-type="required" data-mark="商品主图片">
	                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品图片:</label>
	                <div class="col-sm-9">
	                    <input type="hidden" name="contentId" id="contentId" value=""/>
	                    <button type="button" class="btn btn-primary" id="btnAddProductContent">添加图片</button>
	                    <input id="mainImg" name="mainImg" class="dp-vd" type="hidden"> 
					    <p class="dp-error-msg"></p>
	                </div>
	            </div>
	            <div class="form-group col-sm-6">
	                <div class="col-sm-10"><div class="col-sm-12 dp-form-remarks">注：最多可添加5张照片，第一张图为主图，请保证所有图片比例一致。大小为750*750px</div></div>
	            </div>
            </div>
            
            <div class="row">
                <div class="form-group col-sm-8" >
                    <#--<label  class="col-sm-2 control-label"></label>-->
				    <#--<div class="col-sm-2 js-img1">-->
					     <#--<img height="150"  alt="" src="" id="img1" style="height:100px;width:100px;">-->
					     <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>-->
					     <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg1">编辑</button>-->
				    <#--</div>-->
				    <#--<div class="col-sm-2 js-img2">-->
				         <#--<img height="150" alt="" src="" id="img2" style="height:100px;width:100px;">-->
				         <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>-->
				         <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg2">编辑</button>-->
				    <#--</div>-->
				    <#--<div class="col-sm-2 js-img3">-->
				          <#--<img height="150" alt="" src="" id="img3" style="height:100px;width:100px;">-->
				          <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>-->
				          <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg3">编辑</button>-->
				    <#--</div>-->
				    <#--<div class="col-sm-2 js-img4">-->
				          <#--<img height="150" alt="" src="" id="img4" style="height:100px;width:100px;">-->
				          <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>-->
				          <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg4">编辑</button>-->
				    <#--</div>-->
				    <#--<div class="col-sm-2 js-img5">-->
				          <#--<img height="150" alt="" src="" id="img5" style="height:100px;width:100px;">-->
				          <#--<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>-->
				          <#--<button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg5">编辑</button>-->
				    <#--</div>-->

                    <label  class="col-sm-2 control-label"></label>
                    <div class="col-sm-2 js-img1">
                        <img height="150"  alt="" src="" id="img1" style="height:100px;width:100px;">
                        <input type="hidden" name="img1" id="addImg1" class="js-img-group">
                        <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>
                        <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg1">添加</button>
                    </div>
                    <div class="col-sm-2 js-img2">
                        <img height="150" alt="" src="" id="img2" style="height:100px;width:100px;">
                        <input type="hidden" name="img1" id="addImg2" class="js-img-group">
                        <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>
                        <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg2">添加</button>
                    </div>
                    <div class="col-sm-2 js-img3">
                        <img height="150" alt="" src="" id="img3" style="height:100px;width:100px;">
                        <input type="hidden" name="img1" id="addImg3" class="js-img-group">
                        <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>
                        <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg3">添加</button>
                    </div>
                    <div class="col-sm-2 js-img4">
                        <img height="150" alt="" src="" id="img4" style="height:100px;width:100px;">
                        <input type="hidden" name="img1" id="addImg4" class="js-img-group">
                        <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>
                        <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg4">添加</button>
                    </div>
                    <div class="col-sm-2 js-img5">
                        <img height="150" alt="" src="" id="img5" style="height:100px;width:100px;">
                        <input type="hidden" name="img1" id="addImg5" class="js-img-group">
                        <button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>
                        <button type="button"class="btn btn-danger btn-sm js-imgedit"  id="editProductContentImg5">添加</button>
                    </div>
                </div>
            </div>

            <#if parameters.productTypeId?if_exists=="VIRTUAL_GOOD">
                <div class="row">
                    <div class="form-group col-sm-6">
                        <label  class="col-sm-3 control-label">关联商品:</label>
                        <div class="col-sm-3">
                            <button type="button" class="btn btn-primary" id="btnAddProductAssocGood">添加关联商品</button>
                        </div>

                        <div class="form-group col-sm-6">
                            <div class="col-sm-12"><div class="col-sm-12 dp-form-remarks">注：若无关联商品,则适用全渠道商品</div></div>
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
					               <th>商品编码</th><th>商品名称</th> <th>商品类型</th> <th>商品图片</th> <th>商品规格</th> <th>商品价格(元)</th><th>${uiLabelMap.BrandOption}</th>
					           </tr>
					      </thead>
					      <tbody>
					      </tbody>
		    		  </table>
                   </div>
                </div>
            </div>
			</#if>
            <div class="row">
	            <div class="form-group col-sm-6">
	               <label for="seo" class="col-sm-3 control-label">关键字:</label>
	            </div>
                <div class="form-group col-sm-6">
                    <div class="col-sm-12"><div class="col-sm-12 dp-form-remarks">注：输入关键字用空格分开</div></div>
                </div>
            </div>
            
            <div class="row">
                <div class="form-group">
                   <label  class="col-sm-1 control-label"></label>
                   <div class="col-sm-8">
                         <textarea class="form-control" name="seoKeyword" id="seoKeyword" rows="6"></textarea>
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
            </div>
            
            
            </#if>
	       <div class="modal-footer">
	            <button type="button" class="btn btn-primary" id="btnGoBack">上一步</button>
	            <button type="button" class="btn btn-primary" id="btnModCategory">修改分类</button>
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

                <#--<#assign productFacilityList = delegator.findByAnd("Facility")/>-->
                <#assign productFacilityList = delegator.findByAnd("FacilityByPsId",{"productStoreId" : "${requestAttributes.productStoreId?if_exists}"})/>
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

				<div class="form-group" data-type="format" data-reg="/(\S)+[@]{1}(\S)+[.]{1}(\w)+/"  data-mark="邮箱">
					<label for="message-text" class="control-label col-sm-2">预警提醒人邮箱:</label>
					<div class="col-sm-10">
						<input type="text" class="form-control " name="warningMail" id="warningMail">
					</div>
                    <p class="dp-error-msg"></p>
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
            <table class="table table-bordered table-hover js-checkparent js-table-addfeature">
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
        <button type="button" class="btn btn-primary" id="btnSelectRepeat">重新选规格</button>
      </div>
    </div>
  </div>
</div>


<div class="modal fade" id="modCategory" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
	<div class="modal-dialog"  style="width:800px;">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">选择分类</h4>
        </div>
        <div class="modal-body">
             <form class="form-horizontal"  id="modProductCategory" method="post" action="" name="modProductCategory" enctype="multipart/form-data">
                <input type="hidden" id="ids"/>
		        <input type="hidden" name="curProductCategoryIdLevel1" id="curProductCategoryIdLevel1" value=""/>
			    <input type="hidden" name="curProductCategoryIdLevel2" id="curProductCategoryIdLevel2" value=""/>
			    <input type="hidden" name="curProductCategoryIdLevel3" id="curProductCategoryIdLevel3" value=""/>
			    
			    <input type="hidden" name="curProductCategoryNameLevel1" id="curProductCategoryNameLevel1" value=""/>
			    <input type="hidden" name="curProductCategoryNameLevel2" id="curProductCategoryNameLevel2" value=""/>
			    <input type="hidden" name="curProductCategoryNameLevel3" id="curProductCategoryNameLevel3" value=""/>
		        
		        <div class="xl_main_classfy clearfix">
		            <div class="xl_item">
		                <div class="xl_first_search">
		                <input type="text" name="first_classfy" class="xl_first first_search" placeholder="输入名称查找">
		                    <i class="glyphicon glyphicon-search xl_search_icon search_icon1 search_icon1_1"></i>
		                </div>
		                <ul class="xl_ul1 xl_first_ul" style="top:35px;">
		                    <li data-id="1">
		                        <span class="product_name"></span>
		                        <span class="xl_del_btn">
		                        </span>
		
		                    </li>
		                </ui>   
		            </div>
		            <div class="xl_item">
		                <div class="xl_first_search">
		                    <input type="text" name="first_classfy" class="xl_first second_search" placeholder="输入名称查找">
		                    <i class="glyphicon glyphicon-search xl_search_icon search_icon2 search_icon2_1"></i>
		                </div>
		                <ul class="xl_ul1 xl_second_ul" style="top:35px;">
		                    <li data-id="1">
		                        <span class="product_name"></span>
		                        <span class="xl_del_btn">
		                        </span>
		                    </li>
		                </ul>
		            </div>
		            <div class="xl_item">
		                <div class="xl_first_search">
		                    <input type="text" name="first_classfy" class="xl_first third_search" placeholder="输入名称查找">
		                    <i class="glyphicon glyphicon-search xl_search_icon search_icon3 search_icon3_1"></i>
		                </div>
		                <ul class="xl_ul1 xl_third_ul" style="top:35px;">
		                    <li data-id="1">
		                        <span class="product_name"></span>
		                        <span class="xl_del_btn">
		                        </span>
		
		                    </li>
		                </ul>
		            </div>
		        </div>
		        
		         <div class="form-group">
		              <div class="input-group m-b-5">
		                  <span class="js-productCount">当前选择的是：</span>
		              </div>
		         </div>
            </form>
          </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
        <button type="button" class="btn btn-primary" id="btnCategoryMod">${uiLabelMap.BrandSave}</button>
      </div>
    </div>
  </div>
</div>



<!--修改平台分类-->
<div class="modal fade" id="modPlateFormClass" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document" >
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">选择分类</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal"  id="modProductCategory" method="post" action="" name="modProductCategory" enctype="multipart/form-data">
                    <input type="hidden" id="ids"/>
                    <input type="hidden" name="curProductCategoryIdLevel1" id="curProductCategoryIdLevel1" value=""/>
                    <input type="hidden" name="curProductCategoryIdLevel2" id="curProductCategoryIdLevel2" value=""/>
                    <input type="hidden" name="curProductCategoryIdLevel3" id="curProductCategoryIdLevel3" value=""/>

                    <input type="hidden" name="curProductCategoryNameLevel1" id="curProductCategoryNameLevel1" value=""/>
                    <input type="hidden" name="curProductCategoryNameLevel2" id="curProductCategoryNameLevel2" value=""/>
                    <input type="hidden" name="curProductCategoryNameLevel3" id="curProductCategoryNameLevel3" value=""/>

                    <div class="xl_main_classfy clearfix">
                        <div class="xl_item">
                            <div class="xl_first_search">
                                <input type="text" name="first_classfy" class="xl_first first_search" placeholder="输入名称查找">
                                <i class="glyphicon glyphicon-search xl_search_icon search_icon1 search_icon1_1"></i>
                            </div>
                            <ul class="xl_ul1 xl_first_ul" style="top:35px;">
                                <li data-id="1">
                                    <span class="product_name"></span>
                                    <span class="xl_del_btn">
		                        </span>

                                </li>
                                </ui>
                        </div>
                        <div class="xl_item">
                            <div class="xl_first_search">
                                <input type="text" name="first_classfy" class="xl_first second_search" placeholder="输入名称查找">
                                <i class="glyphicon glyphicon-search xl_search_icon search_icon2 search_icon2_1"></i>
                            </div>
                            <ul class="xl_ul1 xl_second_ul" style="top:35px;">
                                <li data-id="1">
                                    <span class="product_name"></span>
                                    <span class="xl_del_btn">
		                        </span>
                                </li>
                            </ul>
                        </div>
                        <div class="xl_item">
                            <div class="xl_first_search">
                                <input type="text" name="first_classfy" class="xl_first third_search" placeholder="输入名称查找">
                                <i class="glyphicon glyphicon-search xl_search_icon search_icon3 search_icon3_1"></i>
                            </div>
                            <ul class="xl_ul1 xl_third_ul" style="top:35px;">
                                <li data-id="1">
                                    <span class="product_name"></span>
                                    <span class="xl_del_btn">
		                        </span>

                                </li>
                            </ul>
                        </div>
                    </div>

                    <div class="form-group" style="display: none">
                        <div class="input-group m-b-5">
                            <span class="js-productCount">当前选择的是：</span>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
                <button type="button" class="btn btn-primary" id="btnPlateFormMod">${uiLabelMap.BrandSave}</button>
            </div>
        </div>
    </div>
</div>


<!-- 提示弹出框start -->
<div id="modal_msg"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.FacilityOptionMsg}</h4>
      </div>
      <div class="modal-body">
        <h4 id="modal_msg_body"></h4>
      </div>
      <div class="modal-footer">
        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.FacilityOk}</button>
      </div>
    </div>
  </div>
</div><!-- 提示弹出框end -->

<!-- 确认弹出框start -->
<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
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
        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.BrandDel}</button>
      </div>
    </div>
  </div>
</div><!--确认弹出框end -->


<!-- 确认弹出框start -->
<div id="modal_confirm_save"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
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
        <button id="createPage" type="button" class="btn btn-primary" data-dismiss="modal">继续添加</button>
        <button id="rtnList" type="button" class="btn btn-primary" data-dismiss="modal">跳转到列表</button>
      </div>
    </div>
  </div>
</div><!--确认弹出框end -->


<!-- 确认弹出框start -->
<div id="modal_confirm_img"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
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
        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.BrandDel}</button>
      </div>
    </div>
  </div>
</div><!--确认弹出框end -->





<!--yabiz--->
<div class="modal fade" id="addProductFeature" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">选择规格</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal"  id="updateProductFeature" type="post" action="" name="updateProductFeature">
					<input  type="hidden" id="featureOperateType">
                    <input  type="hidden" id="productFeatureTypeId">

                    <div class="form-group" data-type="required" data-mark="规格名称">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>规格名称:</label>
                        <div class="col-sm-3">
                            <input type="text" class="form-control dp-vd" id="productFeatureTypeName" name="productFeatureTypeName" value="${parameters.productFeatureTypeName?if_exists}">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">规格备注:</label>
                        <div class="col-sm-3">
                            <input type="text" class="form-control" id="description" name="description" value="${parameters.description?if_exists}">
                        </div>
                    </div>
                    <div class="row m-b-10">
                        <div class="col-sm-6">
                            <div class="dp-tables_btn">
                                <button type="button" class="btn btn-primary" id="addFeatureValue">添加规格值</button>
                                <#--<button type="button" class="btn btn-primary" id="btnFeatureValueDel">-->
								<#--${uiLabelMap.BrandDel}-->
                                <#--</button>-->
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-12">
                            <table class="table table-bordered table-hover js-checkparent" id="tabFreatureValue">
                                <thead>
                                <tr>
                                    <#--<th><input class="js-allcheck" type="checkbox" id="checkAll"></th>-->
									<th></th>
                                    <th>规格值名称</th>
                                    <th>排序 </th>
                                    <#--<th>${uiLabelMap.BrandOption}</th>-->
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                        </div>
                    </div>


                    <input type="hidden" name="operateType" id="operateType" value="${parameters.operateType?if_exists}"/>
                    <input type="hidden" name="productFeatureTypeId" id="productFeatureTypeId" value="${parameters.productFeatureTypeId?if_exists}"/>
                    <input type="hidden" name="tFeatureInfos" id="tFeatureInfos" value=""/>

                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
                <button type="button" class="btn btn-primary" id="btnFeatureSave">${uiLabelMap.BrandSave}</button>
            </div>
        </div>
    </div>
</div>

<!--添加标签弹出框 start-->
<div class="modal fade" id="addTag_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">新增标签</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="AddTagForm" action="<@ofbizUrl>createTag</@ofbizUrl>" method="post">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="${uiLabelMap.TagType}">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.TagType}:</label>
                            <div class="col-sm-10">
                                <#--<input type="text" class="form-control dp-vd w-p50" id="treeName" name="treeName" onclick="addshowMenu()" readonly/>-->
                                <#--<input type="hidden" class="form-control dp-vd w-p50" id="tagTypeId" name="tagTypeId">-->
                                <#if parameters.productTypeId?if_exists=="FINISHED_GOOD">
                                <input type="text" class="form-control dp-vd w-p50" id="treeName" value="实物商品" name="treeName"  readonly></input>
                                <input type="hidden" class="form-control dp-vd w-p50" id="tagTypeId" name="tagTypeId" value="ProdutTypeTag_1">
                                <#elseif parameters.productTypeId?if_exists=="VIRTUAL_GOOD">
                                    <input type="text" class="form-control dp-vd w-p50" id="treeName" name="treeName"  value="虚拟商品" readonly></input>
                                    <input type="hidden" class="form-control dp-vd w-p50" id="tagTypeId" name="tagTypeId" value="ProdutTypeTag_2">
                                </#if>
                                <#--<div id="Addmenu" style="display:none; position: absolute;top:33px;left:15px;border:1px solid #ccc;background:white;z-index:1000;width:248px;">-->
                                    <#--<ul id="addtree" class="ztree" style="margin-top: 0; width: 110px;">-->
                                    <#--</ul>-->
                                <#--</div>-->
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="${uiLabelMap.TagName}">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.TagName}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="tagName" name="tagName">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <input type="hidden" class="form-control dp-vd w-p50" id="isDel" name="isDel" value="N">
                    <!-- 标签备注 end-->
                    <div class="modal-footer">
                        <button type="button"  id="btn_save" class="btn btn-primary">保存</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<!--添加标签弹出框 end-->



<script language="JavaScript" type="text/javascript">
    var isCommitted = false;//表单是否已经提交标识，默认为false
    var facilityItem ="";
    var imgContentTypeId="";// 商品图片类型ID
    var imgContentIndex="";// 商品图片Index

    var setting1 = {
        view: {
            selectedMulti: false //是否允许多选
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback: {
            //zTree节点的点击事件
            onClick: onAddClick
        }
    };

    $(function(){
        $(".select2Brand").select2();
        $(".select2MrchantName").select2();
        $(".select2Feature").select2({
	        tags: true,
	        maximumSelectionLength: 3  //最多能够选择的个数
	    });
    
//        $(".js-img1").hide();
//        $(".js-img2").hide();
//        $(".js-img3").hide();
//        $(".js-img4").hide();
//        $(".js-img5").hide();
        
        $(".js-pcdetails").hide();// 隐藏PC端详情的内容
          
        CKEDITOR.replace("pcDetails");
        //CKEDITOR.replace("mobileDetails");
        
        <#---
        CKEDITOR.replace("pcDetails",{
            filebrowserImageBrowseUrl: '/content/control/file?rootName=root&directory=/products/10133',

        });
        
        
        CKEDITOR.replace("mobileDetails",{
            filebrowserImageBrowseUrl: '/content/control/file?rootName=root&directory=/products/',

        });
        
         -->
        <#if parameters.productId?has_content>
             var name='${parameters.productId?if_exists}';
             if($("#productName").val()){
                 name=$("#productName").val()+'['+'${parameters.productId?if_exists}'+']';
             }
             CKEDITOR.replace("mobileDetails",{
            filebrowserImageBrowseUrl: '/content/control/file${externalKeyParam}&rootName='+name+'&directory=/products/'+'${parameters.productId?if_exists}',
                 <#--filebrowserImageBrowseUrl: '/content/control/file${externalKeyParam}&file?directory=/datasource'-->
        });
        
        </#if>
       
        var pcDetailsContent= $("#pcDetailsContent").val();
//        CKEDITOR.config.filebrowserImageBrowseUrl = '/content/control/file';
        var mobileDetailsContent= $("#mobileDetailsContent").val();
        if(mobileDetailsContent){
            CKEDITOR.instances.mobileDetails.setData(mobileDetailsContent);
            CKEDITOR.instances.conf
        }
        if(pcDetailsContent){
            CKEDITOR.instances.pcDetails.setData(pcDetailsContent);
        }
        var productTypeId=$("#productTypeId").val();
        var productCategoryId=$("#productCategoryId").val();


        <#if prod?has_content>
            $('#btnGoBack').text("取消");
            $("small").text("修改商品"); //标题的修改
            getSelectedTag();//标签选择初始化
            getSelectedSupService();// 支持服务初始化
            getFacilityByProductId('${parameters.productId?if_exists}'); //仓库信息初始化
            getAssocGoodListById('${parameters.productId?if_exists}'); //关联信息初始化
            
            <#if prod.isUsedFeature=="Y">
               $('.js-facility').hide();
	           $('.js-feature-div').show();
	           $('.select2Feature').attr("disabled","disabled");
	           $('#btnProductGoodSave').text("下一步");
	           $("#productFacilityInfos").removeClass("dp-vd");
	        <#else>
	             $('.js-facility').show();
		         $('.js-feature-div').hide();
		         $('#btnProductGoodSave').text("保存");
	        </#if>
            // 初始化商品规格
              getFeatureInfoByProductId('${parameters.productId?if_exists}');
		      <#--<#assign getFeatureIdListForProductIdList = delegator.findByAnd("GetFeatureIdListForProductId",{"productId" : "${prod.productId?if_exists}"})/>-->
		      <#--var ids="";-->
		      <#--var typeName="";-->
		      <#--var productTypeIdTemp="";-->
<#--//									 alert(getFeatureIdListForProductIdList.length);-->
		      <#--<#list getFeatureIdListForProductIdList as featureType>-->
		          <#--var curTypeId="${featureType.productFeatureTypeId}";-->
		          <#--var curTypeName="${featureType.productFeatureTypeName}";-->
		          <#--//$('.select2Feature').val([curTypeId]).trigger("change");-->
		          <#--<#assign featureList = delegator.findByAnd("GetFeatureIdList",{"productId" : "${prod.productId?if_exists}","productFeatureTypeId":"${featureType.productFeatureTypeId}"})/>-->
		          <#--var featureIds="";-->
		          <#--<#list featureList as featureInfo>-->
		             <#--featureIds=featureIds+"${featureInfo.productFeatureId}"+",";-->
		          <#--</#list>-->
		          <#--<#if featureType_index!=getFeatureIdListForProductIdList.size()-1>-->
		             <#--productTypeIdTemp=productTypeIdTemp+curTypeId+",";-->
		          <#--<#else> -->
		             <#--productTypeIdTemp=productTypeIdTemp+curTypeId;-->
		          <#--</#if>-->
		          <#--//alert(featureIds.substr(0,featureIds.length-1));-->
		          <#--getProductFeatureListById(curTypeId,curTypeName,featureIds.substr(0,featureIds.length-1));-->
		      <#--</#list>-->
		      <#--$(".select2Feature").val(productTypeIdTemp.split(',')).trigger("change");	-->

		      <#--<#if productAdditionalImage1?if_exists != "">-->
		          <#--var imgMain='${productAdditionalImage1}';-->
		          <#--$('#mainImg').attr('value',imgMain);-->
		          <#--$(".js-img1").show();-->
		      <#--</#if>-->
		      <#--<#if productAdditionalImage2?if_exists != "">-->
		           <#--$(".js-img2").show();-->
		      <#--</#if>-->
		      <#--<#if productAdditionalImage3?if_exists != "">-->
		           <#--$(".js-img3").show();-->
		      <#--</#if>-->
		      <#--<#if productAdditionalImage4?if_exists != "">-->
		           <#--$(".js-img4").show();-->
		      <#--</#if>-->
		      <#--<#if productAdditionalImage5?if_exists != "">-->
		           <#--$(".js-img5").show();-->
		      <#--</#if>-->

            <#if productAdditionalImage1?if_exists != "">
                var imgMain='${productAdditionalImage1}';
                $('#mainImg').attr('value',imgMain);
                $(".js-img1").show();
            <#else>
                $('#delProductContentImg1').hide();
            </#if>
            <#if productAdditionalImage2?if_exists != "">
                $(".js-img2").show();
            <#else>
                $('#delProductContentImg2').hide();
            </#if>
            <#if productAdditionalImage3?if_exists != "">
                $(".js-img3").show();
            <#else>
                $('#delProductContentImg3').hide();
            </#if>
            <#if productAdditionalImage4?if_exists != "">
                $(".js-img4").show();
            <#else>
                $('#delProductContentImg4').hide();
            </#if>
            <#if productAdditionalImage5?if_exists != "">
                $(".js-img5").show();
            <#else>
                $('#delProductContentImg5').hide();
            </#if>



            <#if prod.integralDeductionType?if_exists=="2">
                $('.js-integral-upper').show();
                $('.js-integral-upper-name').text('%');
            <#elseif prod.integralDeductionType?if_exists=="3">;
                $('.js-integral-upper').show();
                $('.js-integral-upper-name').text('元');
            <#else>
                $('.js-integral-upper').hide();
            </#if>

		<#else>
	      $('.js-facility').show();
	      $('.js-feature-div').hide();
	      
	      var productTypeIdForPrepare='${parameters.productTypeId?if_exists}';
	      var productCategoryIdForPrepare ='${parameters.productCategoryId?if_exists}';
	      //添加准备商品实体
	      setPrepareProductEntity(productTypeIdForPrepare,productCategoryIdForPrepare);
	      //<#assign testId=delegator.getNextSeqId("Product")/>
          //$("#productId").val("${testId}");
          $("#btnModCategory").hide();
            $("#delProductContentImg1").hide();
            $("#delProductContentImg2").hide();
            $("#delProductContentImg3").hide();
            $("#delProductContentImg4").hide();
            $("#delProductContentImg5").hide();
          
          
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
			   $("#productFacilityInfos").removeClass("dp-vd");
	        }else{
	           $('.js-facility').show();
	           $('.js-feature-div').hide();
	           $("#productFacilityInfos").addClass("dp-vd");
	        }
	    })
        var operateType='${parameters.operateType?if_exists}';
        var integralDeductionUpperTmp=$("#integralDeductionUpper").length==0?'':$("#integralDeductionUpper").val();
		if(integralDeductionUpperTmp==''){
            $('.js-integral-upper').hide();
		}


		$('#integralDeductionType').change(function(){
			if($(this).val()=='2'){
                $('.js-integral-upper').show();
				$('.js-integral-upper-name').text('%');
			}else if($(this).val()=='3'){
                $('.js-integral-upper').show();
                $('.js-integral-upper-name').text('元');
			}else{
				$('.js-integral-upper').hide();
			}
		})
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
	       $('#updateProductGood').dpValidate({
			  clear: true
		   });
		   <#--
		   if($("#endTime").val()){
		       $('.js-startTime').attr("data-type","required,linkLt");
		   }else{
		       $('.js-startTime').attr("data-type","required");
		   }
		   -->
		   if($("#endTime").val()){
		       $('.js-startTime').attr("data-type","linkLt");
		   }else{
		      
		   }



		   if ($('#integralDeductionType').val() != '1') {
			   $('.js-integral-upper').attr('data-type', "required,format");
		   }


            if($('#integralDeductionType').val()=='3') {
                if ($('#salePrice').val()) {
					if (parseInt($('#integralDeductionUpper').val()) > parseInt($('#salePrice').val())) {
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("固定金额抵扣”时，积分抵扣上限不允许大于销售价格！");
						$('#modal_msg').modal();
						return false;
					}
				}
            }

            if($('#integralDeductionType').val()=='2') {
                if (parseInt($('#integralDeductionUpper').val()) < parseInt("0") ||
                        parseInt($('#integralDeductionUpper').val()) > parseInt("100")) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("百分比抵扣”时，积分抵扣上限应该在（0~100）之间！");
                    $('#modal_msg').modal();
                    return false;
                }

            }

            if(parseFloat($('#salePrice').val())<=parseInt("0")){
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("销售价格必须大于0！");
                $('#modal_msg').modal();
                return false;
            }

		   // 取得商品库存信息
		   facilityInfos();
	       $('#updateProductGood').submit();
        });
        
        
        $('#updateProductGood').dpValidate({
      		validate: true,
      		callback: function(){
               var isUsedFeature=$('.radio').find('input[name=isUsedFeature]:checked').val();
		       if(isUsedFeature=='Y'){
                    <#if prod?has_content>
		         		//document.location.href="<@ofbizUrl>updateProductFeatureGood?productId="+${parameters.productId?if_exists}+"&operateType=update</@ofbizUrl>";
		         		//getUpdatePara();
		         		var productIdForUpdate=$("#productId").val();
		         		isSkuProduct(productIdForUpdate);
		       		<#else>
		       			getSelectedFeatures();
		       		</#if>
               }else{
  			   	   var operate='${parameters.operateType?if_exists}';
	  			   if(dosubmit()){

                       <!--ajax--> 
                       //var operate='${parameters.operateType?if_exists}';
                        // 操作类型	
					    var operateType='${parameters.operateType?if_exists}';
					    // 商品编号	                 
						var productId=$("#productId").val();
						// 商品类型	                      
						var productTypeId='${parameters.productTypeId?if_exists}';
						// 商品分类	              
						//var productCategoryId='${parameters.productCategoryId?if_exists}';
						var productCategoryId=$("#productCategoryId").val();
			            // 商品名称	
			            var productName=$("#productName").val();
			            // 商品描述	
			            var productSubheadName=$("#productSubheadName").val();
			            // 是否上架
			            var isOnline="";
			            <#if parameters.operateType?if_exists=="update">
			                isOnline=$("#isOnline").val();
			            <#else>
			                isOnline=$('.radio').find('input[name=isOnline]:checked').val();
			            </#if>
			            // 销售开始时间	
			            var startTime=$("#startTime").val();
			            // 销售结束时间	
			            var endTime=$("#endTime").val();
			            // 商家名称
//			            var businessPartyId=$("#businessPartyId").val();
                       var businessPartyId=$("#businessPartyId").length==0?'':$("#businessPartyId").val();
			            // 商品品牌	
			            var brandId=$("#brandId").val();
			            // 体积	
			            var volume=$("#volume").val();
			            // 重量	
			            var weight=$("#weight").val();
			            // 是否使用规格	
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
				  		// 商品规格信息
			            var productFeatureInfos=$("#productFeatureInfos").val();
				  			   
			            // 图片信息
			            var productContentInfos=$("#productContentInfos").val();
			            // 商品关系信息
			            assocProductGoodInfos();
			            var productAssocInfos=$("#productAssocInfos").val();

                        // Add by zhajh at 20180305 yabiz相关内容 Begin
						// 推荐到首页
						var isRecommendHomePage= $('.radio').find('input[name=isRecommendHomePage]').length==0?'':$('.radio').find('input[name=isRecommendHomePage]:checked').val();;
						// 支持服务类型
                        var supportServices = "";
                        $('input[name="supportServiceType"]:checked').each(function(){
                           supportServices = supportServices + "," + $(this).val();
                        });
                        var supportServiceType=supportServices.substr(1,supportServices.length);
						// 积分抵扣
						var integralDeductionType=$("#integralDeductionType").length==0?'':$("#integralDeductionType").val();
						// 积分抵扣上限
						var integralDeductionUpper=$("#integralDeductionUpper").length==0?'':$("#integralDeductionUpper").val();
						// 每人限购数量
						var purchaseLimitationQuantity=$("#purchaseLimitationQuantity").length==0?'':$("#purchaseLimitationQuantity").val();
						// 列表展示
						var isListShow=$('.radio').find('input[name=isListShow]').length==0?'':$('.radio').find('input[name=isListShow]:checked').val();
						// 代金券面额
						var voucherAmount=$("#voucherAmount").length==0?'':$("#voucherAmount").val();
						// 使用限制
						var useLimit=$('.radio').find('input[name=useLimit]').length==0?'':$('.radio').find('input[name=useLimit]:checked').val();
						// 使用开始时间
						var useStartTime=$("#useStartTime").length==0?'':$("#useStartTime").val();
						// 使用结束时间
						var useEndTime=$("#useEndTime").length==0?'':$("#useEndTime").val();
						// 是否保税商品
						var isBondedGoods=$('.radio').find('input[name=isBondedGoods]').length==0?'':$('.radio').find('input[name=isBondedGoods]:checked').val();
					    // 店铺信息
					    var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
					    // 自营与非自营标记
					    var isInner =$('#isInner').length==0?'':$('#isInner').val();
					    // 平台分类
					    var platformClassId=$('#platformClassId').length==0?'':$('#platformClassId').data("id");
                       // 供应商编码
                       var providerId=$('#providerId').length==0?'':$('#providerId').val();
//                       console.log(productStoreId+"||"+isInner+"||"+platformClassId);
                        // Add by zhajh at 20180305 yabiz相关内容 End

				        jQuery.ajax({
					        url: '<@ofbizUrl>updateProductGoodForAjax</@ofbizUrl>',
					        type: 'POST',
					        data: {
                                 'operateType':operateType,
								 'productId':productId,
								 'productTypeId':productTypeId,
								 'productCategoryId':productCategoryId,
								 'productName':productName,
								 'productSubheadName':productSubheadName,
								 'isOnline':isOnline,
								 'startTime':startTime,
								 'endTime':endTime,
								 'businessPartyId':businessPartyId,
								 'brandId':brandId,
								 'volume':volume,
								 'weight':weight,
								 'isUsedFeature':isUsedFeature,
								 'seoKeyword':seoKeyword,
								 'salePrice':salePrice,
								 'marketPrice':marketPrice,
								 'costPrice':costPrice,
								 'productTags':productTags,
								 'productParameterInfos':productParameterInfos1,
								 'productAttrInfos':productAttrInfos,
								 'productFacilityInfos':productFacilityInfos,
								 'pcDetails':pcDetails,
								 'mobileDetails':mobileDetails,
								 'productFeatureInfos':productFeatureInfos,
								 'productContentInfos':productContentInfos,
								 'productAssocInfos':productAssocInfos,
								 'isRecommendHomePage':isRecommendHomePage,
                                 'supportServiceType':supportServiceType,
                                 'integralDeductionType':integralDeductionType,
                                 'integralDeductionUpper':integralDeductionUpper,
                                 'purchaseLimitationQuantity':purchaseLimitationQuantity,
                                 'isListShow':isListShow,
                                 'voucherAmount':voucherAmount,
                                 'useLimit':useLimit,
                                 'useStartTime':useStartTime,
                                 'useEndTime':useEndTime,
                                 'isBondedGoods':isBondedGoods,
								 'productStoreId':productStoreId,
								 'isInner':isInner,
								 'platformClassId':platformClassId,
                                'providerId':providerId


					        },
					        success: function(data) {
					            if(operateType=="create"){
						            //设置删除弹出框内容
					   				$('#modal_confirm_save #modal_confirm_body').html("商品添加成功");
					   				$('#modal_confirm_save').modal('show');
				   				}else{
				   				    document.location.href="<@ofbizUrl>findProductGoods</@ofbizUrl>";
				   				}
					        }
					    });
			       }
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
            var featureIds="";
            $('input[class="js-checkchild"]:checked').each(function(){ 
	            ids = ids + "," + $(this).val();
                featureIds=featureIds+","+$(this).data("id");

			});
			//alert(ids.substr(1,ids.length));
            console.log(ids.substr(1,ids.length));
            console.log(featureIds.substr(1,featureIds.length));
			
			if(ids==""){
               //设置提示弹出框内容
	    	   $('#modal_msg #modal_msg_body').html("请选择Sku规格值！");
	    	   $('#modal_msg').modal();
            }else{
				///////////////////////////////////////////
				// var featureIds="";
				// $('.js-table-addfeature>tbody').find("tr").each(function(){
		         //   // 读取tbody中tr的内容
			     //   var tdArr = $(this).children();
			     //   var featureId = tdArr.eq(0).find("input").val();//规格ID
		         //   featureIds = featureIds + "," + featureId;
                // });
				//alert(featureIds.substr(1,featureIds.length));
				//////////////////////////////////////////
				
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
	            // 商品描述	
	            var productSubheadName=$("#productSubheadName").val();
	            // 是否上架	
	            //var isOnline=$('.radio').find('input[name=isOnline]:checked').val();
	            var isOnline="";
	            <#if parameters.operateType?if_exists=="update">	
	                isOnline=$("#isOnline").val();
	            <#else>
	                isOnline=$('.radio').find('input[name=isOnline]:checked').val();
	            </#if>
	            // 销售开始时间	
	            var startTime=$("#startTime").val();
	            // 销售结束时间	
	            var endTime=$("#endTime").val();
	            // 商家名称
	            var businessPartyId=$("#businessPartyId").length==0?'':$("#businessPartyId").val();
	            // 商品品牌	
	            var brandId=$("#brandId").val();
	            // 体积	
	            var volume=$("#volume").val();
	            // 重量	
	            var weight=$("#weight").val();
	            // 是否使用规格	
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
	  			//alert(productAttrInfos);
	  			// 取得商品库存信息
	  			facilityInfos();
	  			var productFacilityInfos=$("#productFacilityInfos").val();
	  			// 取得pc详情信息
	  			$("#pcDetails").val(CKEDITOR.instances.pcDetails.getData()); 
	  			var pcDetails=$("#pcDetails").val();
	            // 取得移动详情信息
		  		$("#mobileDetails").val(CKEDITOR.instances.mobileDetails.getData());
		  		var mobileDetails=$("#mobileDetails").val();
		  		// 商品规格信息
	            var productFeatureInfos=$("#productFeatureInfos").val();
		  			   
	            // 图片信息
	            var productContentInfos=$("#productContentInfos").val();
	            // 商品关系信息
	            assocProductGoodInfos();
	            var productAssocInfos=$("#productAssocInfos").val();



                // Add by zhajh at 20180305 yabiz相关内容 Begin
                // 推荐到首页
                var isRecommendHomePage= $('.radio').find('input[name=isRecommendHomePage]').length==0?'':$('.radio').find('input[name=isRecommendHomePage]:checked').val();;
                // 支持服务类型
                var supportServices = "";
                $('input[name="supportServiceType"]:checked').each(function(){
                    supportServices = supportServices + "," + $(this).val();
                });
                var supportServiceType=supportServices.substr(1,supportServices.length);
                // 积分抵扣
                var integralDeductionType=$("#integralDeductionType").length==0?'':$("#integralDeductionType").val();
                // 积分抵扣上限
                var integralDeductionUpper=$("#integralDeductionUpper").length==0?'':$("#integralDeductionUpper").val();
                // 每人限购数量
                var purchaseLimitationQuantity=$("#purchaseLimitationQuantity").length==0?'':$("#purchaseLimitationQuantity").val();
                // 列表展示
                var isListShow=$('.radio').find('input[name=isListShow]').length==0?'':$('.radio').find('input[name=isListShow]:checked').val();
                // 代金券面额
                var voucherAmount=$("#voucherAmount").length==0?'':$("#voucherAmount").val();
                // 使用限制
                var useLimit=$('.radio').find('input[name=useLimit]').length==0?'':$('.radio').find('input[name=useLimit]:checked').val();
                // 使用开始时间
                var useStartTime=$("#useStartTime").length==0?'':$("#useStartTime").val();
                // 使用结束时间
                var useEndTime=$("#useEndTime").length==0?'':$("#useEndTime").val();
                // 是否保税商品
                var isBondedGoods=$('.radio').find('input[name=isBondedGoods]').length==0?'':$('.radio').find('input[name=isBondedGoods]:checked').val();
                // 店铺信息
                var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
                // 自营与非自营标记
                var isInner =$('#isInner').length==0?'':$('#isInner').val();
                // 平台分类
                var platformClassId=$('#platformClassId').length==0?'':$('#platformClassId').data("id");
                // 供应商编码
                var providerId=$('#providerId').length==0?'':$('#providerId').val();
                // Add by zhajh at 20180305 yabiz相关内容 End

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
						    productAssocInfos+"^"+

							isRecommendHomePage+"^"+
							supportServiceType+"^"+
							integralDeductionType+"^"+
							integralDeductionUpper+"^"+
							purchaseLimitationQuantity+"^"+
							isListShow+"^"+
							voucherAmount+"^"+
							useLimit+"^"+
							useStartTime+"^"+
							useEndTime+"^"+
							isBondedGoods+"^"+
						    productStoreId+"^"+
						    isInner+"^"+
                            platformClassId+"^"+
                            providerId;
			  //////////////////////////////////////////
                console.log(featureIds.substr(1,featureIds.length));

			   <#--document.location.href="<@ofbizUrl>editProductFeatureGood?ids="+ids.substr(1,ids.length)+"&"+-->
			                                                   <#--"featureIds="+featureIds.substr(1,featureIds.length)+"&"+-->
															   <#--"operateType="+operateType+"&"+-->
															   <#--"productId="+productId+"&"+-->
															   <#--"productTypeId="+productTypeId+"&"+-->
															   <#--"productCategoryId="+productCategoryId+"&"+-->
															   <#--"productName="+productName+"&"+-->
															   <#--"productSubheadName="+productSubheadName+"&"+-->
															   <#--"isOnline="+isOnline+"&"+-->
															   <#--"startTime="+startTime+"&"+-->
															   <#--"endTime="+endTime+"&"+-->
															   <#--"businessPartyId="+businessPartyId+"&"+-->
															   <#--"brandId="+brandId+"&"+-->
															   <#--"volume="+volume+"&"+-->
															   <#--"weight="+weight+"&"+-->
															   <#--"isUsedFeature="+isUsedFeature+"&"+-->
															   <#--"seoKeyword="+seoKeyword+"&"+-->
															   <#--"salePrice="+salePrice+"&"+-->
															   <#--"marketPrice="+marketPrice+"&"+-->
															   <#--"costPrice="+costPrice+"&"+-->
															   <#--"productTags="+productTags+"&"+-->
															   <#--"productParameterInfos="+productParameterInfos1+"&"+-->
															   <#--"productAttrInfos="+productAttrInfos+"&"+-->
															   <#--"productFacilityInfos="+productFacilityInfos+"&"+-->
															   <#--"pcDetails="+pcDetails+"&"+-->
															   <#--"mobileDetails="+encodeURIComponent(mobileDetails)+"&"+-->
															   <#--"productFeatureInfos="+productFeatureInfos+"&"+-->
															   <#--"productContentInfos="+productContentInfos+"&"+-->
															   <#--"productAssocInfos="+productAssocInfos+"&"+-->
															   <#--"isRecommendHomePage="+isRecommendHomePage+"&"+-->
															   <#--"supportServiceType="+supportServiceType+"&"+-->
															   <#--"integralDeductionType="+integralDeductionType+"&"+-->
															   <#--"integralDeductionUpper="+integralDeductionUpper+"&"+-->
															   <#--"purchaseLimitationQuantity="+purchaseLimitationQuantity+"&"+-->
															   <#--"isListShow="+isListShow+"&"+-->
															   <#--"voucherAmount="+voucherAmount+"&"+-->
															   <#--"useLimit="+useLimit+"&"+-->
															   <#--"useStartTime="+useStartTime+"&"+-->
															   <#--"useEndTime="+useEndTime+"&"+-->
															   <#--"isBondedGoods="+isBondedGoods+"&"+-->
					                                           <#--"productStoreId="+productStoreId+"&"+-->
					                                           <#--"isInner="+isInner+"&"+-->
                                                               <#--"platformClassId="+platformClassId+"&"+-->
                                                               <#--"providerId="+providerId+-->
							                                   <#--"</@ofbizUrl>";-->
			 }



            $.ajax({
                url:'<@ofbizUrl>setSessionByParam</@ofbizUrl>',
                dataType:'json',
                type:'post',
                data:{
                    'attrName' : "mobileDetails",
                    'attrVal' : mobileDetails
                },
                success:function(data){
                    window.location.href="<@ofbizUrl>editProductFeatureGood?ids="+ids.substr(1,ids.length)+"&"+
                            "featureIds="+featureIds.substr(1,featureIds.length)+"&"+
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
                            "productParameterInfos="+productParameterInfos1+"&"+
                            "productAttrInfos="+productAttrInfos+"&"+
                            "productFacilityInfos="+productFacilityInfos+"&"+
                            "pcDetails="+pcDetails+"&"+
                            "mobileDetails="+"&"+
                            "productFeatureInfos="+productFeatureInfos+"&"+
                            "productContentInfos="+productContentInfos+"&"+
                            "productAssocInfos="+productAssocInfos+"&"+
                            "isRecommendHomePage="+isRecommendHomePage+"&"+
                            "supportServiceType="+supportServiceType+"&"+
                            "integralDeductionType="+integralDeductionType+"&"+
                            "integralDeductionUpper="+integralDeductionUpper+"&"+
                            "purchaseLimitationQuantity="+purchaseLimitationQuantity+"&"+
                            "isListShow="+isListShow+"&"+
                            "voucherAmount="+voucherAmount+"&"+
                            "useLimit="+useLimit+"&"+
                            "useStartTime="+useStartTime+"&"+
                            "useEndTime="+useEndTime+"&"+
                            "isBondedGoods="+isBondedGoods+"&"+
                            "productStoreId="+productStoreId+"&"+
                            "isInner="+isInner+"&"+
                            "platformClassId="+platformClassId+"&"+
                            "providerId="+providerId+
                                    "</@ofbizUrl>"
                },
                error:function(){
                    alert("操作失败")
                }
            })
													   
														   
		
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
            if($('#addFacility').find('input[name=warningMail]').val()!=''){
                $('#addFacility').find('input[name=warningMail]').addClass("dp-vd");
            }
           $("#updateProductFacility").submit();
        });
        
	    $("#updateProductFacility").dpValidate({
	        validate:true,
	        callback:function(){
	        	var $model = $('#addFacility');
				var facilitynameId =$model.find('select[name=facilityname]').val();//仓库Id
				var facilityname =$model.find('select[name=facilityname] option:selected').text();//仓库名称
				var accountingQuantityTotal=$model.find('input[name=accountingQuantityTotal]').val();//可用库存
				var warningQuantity="0";
				var warningMail=$model.find('input[name=warningMail]').val();// 预警提醒人邮箱
				if($model.find('input[name=warningQuantity]').val()){
				    warningQuantity=$model.find('input[name=warningQuantity]').val();//预警库存
				}
                if($model.find('input[name=warningMail]').val()){
                    warningMail=$model.find('input[name=warningMail]').val();// 预警提醒人邮箱
                }
				//alert(facilityname);
				//alert(accountingQuantityTotal);
				//alert(warningQuantity);
	             
	            var tr=' <tr><td><input type="hidden" name="" id="" value="'+facilitynameId+'">'+facilityname+'</td><td><input type="hidden" name="curInventoryItemId" id="curInventoryItemId" value="">'+accountingQuantityTotal+'</td><td>0</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td><td>'+warningMail+'</td><td class="fc_td"><button type="button" class="js-button-facility btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	 			$('.js-table_3>tbody').append(tr);
	 			$('#addFacility').modal('hide');
		    }
	    })
        
        
        
        //删除按钮事件
  	    $(document).on('click','.js-button-facility',function(){
  	       //$(this).parent().parent().remove();
  	       var facilityId=$(this).parent().parent().find("input").val();//仓库ID
  	       
  	       var productForFacliityId=$("#productId").val();
  	       facilityItem=$(this).parent().parent();
  	       isFacilityUsed(facilityId,productForFacliityId);
  	    
  	       
        })
        
        //删除弹出框删除按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
	       	facilityItem.remove();
	    });
        
        //关联商品行删除事件
  	    $(document).on('click','.js-button-assocgood',function(){
  	       $(this).parent().parent().remove();
        })
        
        //商品规格事件
        var $eventSelect = $(".select2Feature");
        //商品规格的选择
        $eventSelect.on("select2:select", function (e) {
         addFeature("select2:select", e);
        });
        // 商品规格的删除
        $eventSelect.on("select2:unselect", function (e) { 
           delFeature("select2:unselect", e); 
        });
        
        // 关联商品按钮的点击事件
        $("#btnAddProductAssocGood").click(function(){
            <#--
        	$.dataSelectModal({
				url: "/catalog/control/ProductListModalPage",
				width:	"800",
				title:	"选择商品",
				selectCallBack: function(el){
				    var productId = el.data('id');
				    getProductAssocGoodListByIds(productId);
				}
			});
			-->
			$.dataSelectModal({
				url: "/catalog/control/ProductListMultiModalPage?externalLoginKey=${externalLoginKey}",
				width:	"800",
				title:	"选择商品",
				selectId: "linkId",
	    	    selectName:	"selectName",
	    	    multi:  true,  
				selectCallBack: function(el){
				    $("#linkId").val(el.data('id'));
				    $("#selectName").val(el.data('name'));
				    var productId = el.data('id');
				    getProductAssocGoodListByIds(productId);
				}
			});
        })
        
        $("#btnAddProductContent").click(function(){
           imageManage();
        
        })

        // 上传图片
        $('body').on('click','.img-submit-btn',function(){
            var obj = $.chooseImage.getImgData();
            var imgProductId=$("#productId").val();
            $.chooseImage.choose(obj,function(data){
                imgUrl1="";
                imgUrl2="";
                imgUrl3="";
                imgUrl4="";
                imgUrl5="";
                if(data.uploadedFile0){
                    imgUrl1="/content/control/getImage?contentId="+data.uploadedFile0;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile0,"1");
                    //alert(imgUrl1);
                }
                if(data.uploadedFile1){
                    imgUrl2="/content/control/getImage?contentId="+data.uploadedFile1;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile1,"2");
                    //alert(imgUrl2);
                }
                if(data.uploadedFile2){
                    imgUrl3="/content/control/getImage?contentId="+data.uploadedFile2;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile2,"3");
                    //alert(imgUrl3);
                }
                if(data.uploadedFile3){
                    imgUrl4="/content/control/getImage?contentId="+data.uploadedFile3;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile3,"4");
                    //alert(imgUrl4);
                }
                if(data.uploadedFile4){
                    imgUrl5="/content/control/getImage?contentId="+data.uploadedFile4;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile4,"5");
                    //alert(imgUrl5);
                }
                getProductPicByContentId(imgUrl1,imgUrl2,imgUrl3,imgUrl4,imgUrl5);

            })
        });

       // 上传图片
//	   $('body').on('click','.img-submit-btn',function(){
//           var obj = $.chooseImage.getImgData();
//           $.chooseImage.choose(obj,function(data){
//             //var contentId="/content/control/stream?contentId="+data.uploadedFile0;
//             // $('#img').attr('src',contentId);
//             //$('#contentId').val(data.uploadedFile0);
//             var productId=$("#productId").val();
//             getProductPicById(productId);
//           })
//       });

       //创建成功后返回列表按钮点击事件
	    $('#modal_confirm_save #rtnList').click(function(e){
	        document.location.href="<@ofbizUrl>findProductGoods</@ofbizUrl>";
	    });
        
        //创建成功后继续添加按钮点击事件
	    $('#modal_confirm_save #createPage').click(function(e){
	       	document.location.href="<@ofbizUrl>createProductGood</@ofbizUrl>";
	    });
	    
	    
	     // 图片的删除
        $(document).on('click','.js-imgdel',function(){
             var curId=$(this).attr("id");
             if(curId=="delProductContentImg1"){
                 imgContentTypeId="ADDITIONAL_IMAGE_1";
             }else if(curId=="delProductContentImg2"){
                 imgContentTypeId="ADDITIONAL_IMAGE_2";
             }else if(curId=="delProductContentImg3"){
                 imgContentTypeId="ADDITIONAL_IMAGE_3";
             }else if(curId=="delProductContentImg4"){
                 imgContentTypeId="ADDITIONAL_IMAGE_4";
             }else if(curId=="delProductContentImg5"){
                 imgContentTypeId="ADDITIONAL_IMAGE_5";
             }
             $('#modal_confirm_img #modal_confirm_body').html("确定要删除该商品图片信息，是否继续删除");
			 $('#modal_confirm_img').modal('show');
        });
        
        //删除弹出框删除按钮点击事件
	    $('#modal_confirm_img #ok').click(function(e){
	       	delProductContentImg();
	    });
	    
	    //////////////////////////////////////////////////////////////////////////////////
	    getInitProductCategoryByLevel();
        getInitProductCategoryByLevelForPlatformClassByPsId();

        //选中li的时候显示那两个按钮
        $("#modCategory .xl_ul1").on("click","li",function(e){
            if($(e.target).is("a")){
                return;
            }else{
                $(this).find(".xl_del_btn").show();
                $(this).addClass("xl_active").siblings().removeClass("xl_active");
                $(this).siblings().find(".xl_del_btn").hide();
            }
        });
        //选中一级分类的时候向后台发送请求
        $("#modCategory .xl_first_ul").on("click","li",function(e){
            if($(e.target).is("a")){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                seclectdeItemProductCategoryLevelById("1",productCategoryId);
                
                $('#modCategory .xl_first_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                $("#curProductCategoryIdLevel1").val(productCategoryId);
                $("#curProductCategoryNameLevel1").val(productCategoryName);

                $("#modPlateFormClass #curProductCategoryIdLevel1").val(productCategoryId);
                $("#modPlateFormClass #curProductCategoryNameLevel1").val(productCategoryName);
            }
        });
        //选中二级分类的时候向后台发送请求
        $("#modCategory .xl_second_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                seclectdeItemProductCategoryLevelById("2",productCategoryId);
                
                $('#modCategory .xl_second_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                $("#modCategory #curProductCategoryIdLevel2").val(productCategoryId);
                $("#modCategory #curProductCategoryNameLevel2").val(productCategoryName);
            }
        });
        
        //选中三级分类的时候向后台发送请求
        $("#modCategory .xl_third_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                $('#modCategory.xl_third_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                $("#modCategory #curProductCategoryIdLevel3").val(productCategoryId);
                $("#modCategory #curProductCategoryNameLevel3").val(productCategoryName);


                var curProductCategoryId=$("#modCategory #curProductCategoryIdLevel3").val();
                if($("#modCategory #curProductCategoryIdLevel3").val()!=""){
	               getProductCountById($("#modCategory #curProductCategoryIdLevel3").val());
	            }else{
	               $(".js-productCount").text("当前选择的是："+ $("#modCategory #curProductCategoryNameLevel1").val()+">"+ $("#modCategory #curProductCategoryNameLevel2").val()+">"+ $("#modCategory #curProductCategoryNameLevel3").val());
	            }
            }
        });
        
        //一级分类搜索的时候向后台发送请求
        $("#modCategory .search_icon1").on("click",function(){
            var categoryName=$("#modCategory .first_search").val();
            searchProductCategoryLevelByName("1","",categoryName);
        });
        //二级分类搜索的时候向后台发送请求
        $("#modCategory .search_icon2").on("click",function(){
           var categoryName=$("#modCategory .second_search").val();
           var parentCategoryId=$("#modCategory #curProductCategoryIdLevel1").val();
           searchProductCategoryLevelByName("2",parentCategoryId,categoryName);
        });
        //三级分类搜索的时候向后台发送请求
        $("#modCategory .search_icon3").on("click",function(){
            var categoryName=$("#modCategory .third_search").val();
            var parentCategoryId=$("#modCategory #curProductCategoryIdLevel2").val();
            searchProductCategoryLevelByName("3",parentCategoryId,categoryName);
        });
       //添加level3的产品分类
       $("#addlevel3").click(function(){
           var productCategoryId=$("#curProductCategoryIdLevel2").val();
           document.location.href="<@ofbizUrl>CreateProductCategory?operateType=create&productCategoryLevel=3&productCategoryId="+productCategoryId+"</@ofbizUrl>";
           return false;
       })
      
       $("#btnCategoryMod").click(function(){
           var categoryInfo= $("#curProductCategoryIdLevel3").val();
           if(categoryInfo==""){
              $('#modal_msg #modal_msg_body').html("只能在第三级分类下新建商品!");
			  $('#modal_msg').modal();
           }else{
              //alert($("#curProductCategoryIdLevel3").val());
              $("#productCategoryId").val($("#curProductCategoryIdLevel3").val());
              $("#primaryProductCategoryId").val($("#curProductCategoryNameLevel3").val());
              // 更新品牌列表
              var curProductCategoryId=$("#curProductCategoryIdLevel3").val();
              //var productId=${parameters.productId?if_exists};
              getProductCategoryInfoById(curProductCategoryId);
              $('#modCategory').modal('hide');
           }
       })


        $("#btnPlateFormMod").click(function(){
            var categoryInfo= $("#modPlateFormClass #curProductCategoryIdLevel3").val();
            if(categoryInfo==""){
                $('#modal_msg #modal_msg_body').html("只能在第三级分类下新建商品!");
                $('#modal_msg').modal();
            }else{
                //alert($("#curProductCategoryIdLevel3").val());
                $("#platformClassId").val($("#curProductCategoryIdLevel3").val());
                $("#platformClassId").data("id",$("#modPlateFormClass #curProductCategoryIdLevel3").val());
                $("#platformClassId").val($("#modPlateFormClass #curProductCategoryNameLevel3").val());
                $('#modPlateFormClass').modal('hide');
            }
        })
	    
	    
	    
	    //修改商品分类
        $("#btnModCategory").click(function(){
            //设置弹出框内容
			$('#modCategory').modal();
        })


        //  平台分类
        //选中li的时候显示那两个按钮
        $("#modPlateFormClass .xl_ul1").on("click","li",function(e){
            if($(e.target).is("a")){
                return;
            }else{
                $(this).find(".xl_del_btn").show();
                $(this).addClass("xl_active").siblings().removeClass("xl_active");
                $(this).siblings().find(".xl_del_btn").hide();
            }
        });
        //选中一级分类的时候向后台发送请求
        $("#modPlateFormClass .xl_first_ul").on("click","li",function(e){
            if($(e.target).is("a")){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                seclectdeItemProductCategoryLevelByIdForPlatformClassByPsId("1",productCategoryId);

                $('#modPlateFormClass .xl_first_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                $("#modPlateFormClass #curProductCategoryIdLevel1").val(productCategoryId);
                $("#modPlateFormClass #curProductCategoryNameLevel1").val(productCategoryName);
            }
        });
        //选中二级分类的时候向后台发送请求
        $("#modPlateFormClass .xl_second_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                seclectdeItemProductCategoryLevelByIdForPlatformClassByPsId("2",productCategoryId);

                $('#modPlateFormClass .xl_second_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                $("#modPlateFormClass #curProductCategoryIdLevel2").val(productCategoryId);
                $("#modPlateFormClass #curProductCategoryNameLevel2").val(productCategoryName);
            }
        });

        //选中三级分类的时候向后台发送请求
        $("#modPlateFormClass .xl_third_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                $('#modPlateFormClass .xl_third_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                $("#modPlateFormClass #curProductCategoryIdLevel3").val(productCategoryId);
                $("#modPlateFormClass #curProductCategoryNameLevel3").val(productCategoryName);

                var curProductCategoryId=$("#modPlateFormClass #curProductCategoryIdLevel3").val();
                if($("#modPlateFormClass #curProductCategoryIdLevel3").val()!=""){
                    getProductCountById($("#modPlateFormClass #curProductCategoryIdLevel3").val());
                }else{
                    $(".js-productCount").text("当前选择的是："+ $("#modPlateFormClass #curProductCategoryNameLevel1").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel2").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel3").val());
                }
            }
        });

        //一级分类搜索的时候向后台发送请求
        $("#modPlateFormClass .search_icon1").on("click",function(){

            var categoryName=$("#modPlateFormClass .first_search").val();
            searchProductCategoryLevelByNameForPlatformClassByPsId("1","",categoryName);
        });
        //二级分类搜索的时候向后台发送请求
        $("#modPlateFormClass .search_icon2").on("click",function(){
            var categoryName=$("#modPlateFormClass .second_search").val();
            var parentCategoryId=$("#curProductCategoryIdLevel1").val();
            searchProductCategoryLevelByNameForPlatformClassByPsId("2",parentCategoryId,categoryName);
        });
        //三级分类搜索的时候向后台发送请求
        $("#modPlateFormClass .search_icon3").on("click",function(){
            var categoryName=$("#modPlateFormClass .third_search").val();
            var parentCategoryId=$("#curProductCategoryIdLevel2").val();
            searchProductCategoryLevelByNameForPlatformClassByPsId("3",parentCategoryId,categoryName);
        });


        //修改平台分类
        $("#platformClassId").click(function(){
            //设置弹出框内容
            $('#modPlateFormClass').modal();
        })
        
//          // 图片的编辑
//        $(document).on('click','.js-imgedit',function(){
//             var curId=$(this).attr("id");
//             if(curId=="editProductContentImg1"){
//                 imgContentTypeId="ADDITIONAL_IMAGE_1";
//             }else if(curId=="editProductContentImg2"){
//                 imgContentTypeId="ADDITIONAL_IMAGE_2";
//             }else if(curId=="editProductContentImg3"){
//                 imgContentTypeId="ADDITIONAL_IMAGE_3";
//             }else if(curId=="editProductContentImg4"){
//                 imgContentTypeId="ADDITIONAL_IMAGE_4";
//             }else if(curId=="editProductContentImg5"){
//                 imgContentTypeId="ADDITIONAL_IMAGE_5";
//             }
//             imageManageEdit();
//        });
        // 图片的编辑
        $(document).on('click','.js-imgedit',function(){
            var curId=$(this).attr("id");
            if(curId=="editProductContentImg1"){
                imgContentTypeId="ADDITIONAL_IMAGE_1";
                imgContentIndex="0";
            }else if(curId=="editProductContentImg2"){
                imgContentTypeId="ADDITIONAL_IMAGE_2";
                imgContentIndex="1";
            }else if(curId=="editProductContentImg3"){
                imgContentTypeId="ADDITIONAL_IMAGE_3";
                imgContentIndex="2";
            }else if(curId=="editProductContentImg4"){
                imgContentTypeId="ADDITIONAL_IMAGE_4";
                imgContentIndex="3";
            }else if(curId=="editProductContentImg5"){
                imgContentTypeId="ADDITIONAL_IMAGE_5";
                imgContentIndex="4";
            }
            imageManageEdit();
        });

		$('.js-addProductFeature').click(function(){
			// 清空内容
            $('#addProductFeature #productFeatureTypeName').val('');
            $('#addProductFeature #description').val('');
            $('#addProductFeature #tabFreatureValue>tbody').html('');
            //设置弹出框内容
            $('#addProductFeature #featureOperateType').val("create");
            $('#addProductFeature').modal();
		});
        $(document).on('click','.js-addProductFeatureValue',function(){
            // 清空内容
            $('#addProductFeature #productFeatureTypeName').val('');
            $('#addProductFeature #description').val('');
            $('#addProductFeature #tabFreatureValue>tbody').html('');
			var productFeatureTypeId=$(this).data("id");
			// 初始化规格值信息
			getProductFeatureByTypeId(productFeatureTypeId);


            //设置弹出框内容

//            $('#addProductFeature #featureOperateType').val("update");
//            $('#addProductFeature #productFeatureTypeId').val(productFeatureTypeId);
//            $('#addProductFeature').modal();
        });


		// 添加商品规格
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        <#--var operateType ='${parameters.operateType?if_exists}';-->
        <#--var productFeatureTypeId='${parameters.productFeatureTypeId?if_exists}';-->
//        if(operateType=='update'){
//            getProductFeatureByTypeId(productFeatureTypeId);
//        }


        <#--$("#btnCancel").click(function(){-->
            <#--document.location.href="<@ofbizUrl>ProductFeature?lookupFlag=Y</@ofbizUrl>";-->
        <#--})-->


        $("#btnFeatureSave").click(function(){
            $('#updateProductFeature').dpValidate({
                clear: true
            });
            $('#updateProductFeature').submit();
        });
        $('#updateProductFeature').dpValidate({
            validate: true,
            callback: function(){

                    var tFeatureInfos = "";
                    var curFeatureInfo="";


                    $('#addProductFeature #tabFreatureValue>tbody').find("tr").each(function(){
                        // 读取tbody中tr的内容
                        curFeatureInfo="";
                        var tdArr = $(this).children();
                        var curproductFeatureId=tdArr.eq(0).find("#curproductFeatureId").val();//规格值ID
                        var curOptionType=tdArr.eq(0).find("#curOptionType").val();//操作类型
                        var productFeatureName = tdArr.eq(1).find("input").val();//规格值名称
                        var sequenceNum= tdArr.eq(2).find("input").val();//排序号

                        curFeatureInfo=productFeatureName+"|"+sequenceNum+"|"+curproductFeatureId+"|"+curOptionType;
                        tFeatureInfos = tFeatureInfos + "," + curFeatureInfo;
                    });
                    $("#addProductFeature #tFeatureInfos").val(tFeatureInfos.substr(1,tFeatureInfos.length));

                    // 操作类型
                    var featureOperateType=$('#addProductFeature #featureOperateType').val();
                    // 规格类型ID
                    var productFeatureTypeId="";
                    if(featureOperateType=="update"){
                        productFeatureTypeId=$('#addProductFeature #productFeatureTypeId').val();
                    }
                    // 规格类型名
                    var productFeatureTypeName=$("#addProductFeature #productFeatureTypeName").val();
                    // 规格信息
                    var tFeatureInfos=$("#addProductFeature #tFeatureInfos").val();
                    // 规格备注
                    var description=$("#addProductFeature #description").val();
                    if($('#addProductFeature #featureOperateType').val()=="create"){
                        jQuery.ajax({
                            url: '<@ofbizUrl>createProductFeatureForPop</@ofbizUrl>',
                            type: 'POST',
                            data: {
                                'operateType':featureOperateType,
                                'productFeatureTypeId':productFeatureTypeId,
                                'productFeatureTypeName':productFeatureTypeName,
                                'tFeatureInfos':tFeatureInfos,
                                'description':description
                            },
                            success: function(data){
                                $(".select2Feature").append("<option value='"+data.productFeatureTypeId+"'>"+data.productFeatureTypeName+"</option>");
                                $('#addProductFeature').modal('hide');

                            }
                        });
                    }else{
                        jQuery.ajax({
                            url: '<@ofbizUrl>updateProductFeature</@ofbizUrl>',
                            type: 'POST',
                            data: {
                                'operateType':featureOperateType,
                                'productFeatureTypeId':productFeatureTypeId,
                                'productFeatureTypeName':productFeatureTypeName,
                                'tFeatureInfos':tFeatureInfos,
                                'description':description,
                                'isPop':'1'
                            },
                            success: function(data){
                                var curFeatureIds= $(".select2Feature").select2("val").join(',');
                                getProductFeatureTypeInfosByIds(curFeatureIds);
                            }
                        });
//                        alert("Selected value is: "+$(".select2Feature").select2("val"));

                    }
                }

        });

        $("#addFeatureValue").click(function(){
            //var trHtml="<tr><td><input  type='checkbox' class='js-checkchild' name='ckb' value=''/><input type='hidden' id='curOptionType' value='create'></td><td><input type='text'/></td><td><input type='text'/></td><td><button type='button' class='js-button btn btn-danger btn-sm'>删除</button></td></tr>";
            var trHtml="<tr><td><input type='hidden' id='curOptionType' value='create'></td><td><input type='text'/></td><td><input type='text'/></td></tr>";
			$('#tabFreatureValue>tbody').append(trHtml);
        })

        //$("#btnFeatureValueDel").click(function(){
        //    delTr('ckb');
        //})


        $("#btnFeatureValueDel").click(function(){
            var ids = "";
            var checks = $("input[name='ckb']:checked");
            //判断是否选中记录
            if(checks.size() > 0 ){
                $('input[class="js-checkchild"]:checked').each(function(){
                    ids = ids + "," + $(this).val();


                });

                var checkedIds =ids.substr(1,ids.length);
                if (checkedIds != ""){

                    if('${parameters.operateType?if_exists}'=='update'){
                        $("#ids").val(checkedIds);
                        isFeatureForProduct(checkedIds);
                    }else{
                        //设置删除弹出框内容
                        $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
                        $('#modal_confirm').modal('show');
                    }
                }
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
                $('#modal_msg').modal();
            }
        })


        //删除弹出框删除按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            if('${parameters.operateType?if_exists}'=='update'){
                if($("#ids").val()!=""){
                    //delProductFeatureValueByIds();
                    if(!curproductFeatureValue){
                        $('input[class="js-checkchild"]:checked').each(function(){
                            ids = ids + "," + $(this).val();
                            var curFeatureId=$(this).parent().parent().eq(0).find("#curproductFeatureId").val();
                            if(curFeatureId!=""){
                                $(this).parent().parent().hide();
                                $(this).parent().parent().find('#curOptionType').val('delete');
                            }
                        });
                    }else{
                        curproductFeatureValue.hide();

                        curproductFeatureValue.find('#curOptionType').val('delete');
                        curproductFeatureValue="";
                    }
                }else{
                    curproductFeatureValue.remove();
                }
            }else{
                if(curproductFeatureValue){
                    curproductFeatureValue.remove();
                    curproductFeatureValue="";
                }else{
                    delTr('ckb');
                }
            }
        });




        //删除按钮事件
        $(document).on('click','.js-button',function(){

            if('${parameters.operateType?if_exists}'=='update'){
                var curproductFeatureId=$(this).parent().parent().find("#curproductFeatureId").val();
                $("#ids").val(curproductFeatureId);
                if($("#ids").val()!=""){
                    curproductFeatureValue=$(this).parent().parent();
                    isFeatureForProduct(curproductFeatureId);
                }else{
                    curproductFeatureValue=$(this).parent().parent();
                    //设置删除弹出框内容
                    $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
                    $('#modal_confirm').modal('show');
                }

            }else{
                curproductFeatureValue=$(this).parent().parent();
                //设置删除弹出框内容
                $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
                $('#modal_confirm').modal('show');
            }
        })


        // Add by zhajh at 20180502 添加标签 Begin
        //添加按钮点击事件
        $('#btnAddTag').click(function(){
            form_operation='add';
//            $("#addTag_Modal #tagTypeId").val('');
            $("#addTag_Modal #tagName").val('');
//            $("#addTag_Modal #uploadedFile").attr("src", '');
//            $("#addTag_Modal #tagRemark").val('');
//            $("#addTag_Modal #treeName").val('');
            $('#addTag_Modal').modal('show');
        });

        //添加关闭事件
        $('#addTag_Modal').on('hide.bs.modal', function () {
            $('#AddTagForm').dpValidate({
                clear: true
            });
        })
        //添加提交按钮点击事件
        $('#AddTagForm #btn_save').click(function(){
            $('#AddTagForm').dpValidate({
                clear: true
            });
            $('#AddTagForm').submit();
        });
        //编辑表单校验
        $('#AddTagForm').dpValidate({
            validate: true,
            callback: function(){
                $.ajax({
                    url: "/content/control/createTag?externalLoginKey=${externalLoginKey}",
                    type: "POST",
                    data: $('#AddTagForm').serialize(),
                    dataType : "json",
                    success: function(data){
                        //隐藏新增弹出窗口
                        $('#addTag_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal('show');
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        var strTag='<label class="col-sm-3" title=""><input name="tag" id="'+data.tagId+'" value="'+data.tagId+'" type="checkbox">'+data.tagName+'</label>';
                        $('.js-product-tag').append(strTag);
                    },
                    error: function(data){
                        //隐藏新增弹出窗口
                        $('#addTag_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });


//        //添加隐藏树
//        $(document).on('click',function(e){
//            if($(e.target).is('#AddTagForm #treeName')) return;
//            if($(e.target).closest('div').is('#Addmenu')) {
//                if($(e.target).closest('a').is("[id$='_a']"))
//                {
//                    $("#Addmenu").hide();return false;}
//                else return;
//            }
//            else{$("#Addmenu").hide();}
//        })

        <#--$.ajax({-->
            <#--url: "/content/control/getTagTypeList",-->
            <#--type: "GET",-->
            <#--dataType : "json",-->
            <#--success: function(data){;-->
                <#--$.fn.zTree.init($("#addtree"), setting1, data.tagTypeList);-->
            <#--},-->
            <#--error: function(data){-->
                <#--//设置提示弹出框内容-->
                <#--$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");-->
                <#--$('#modal_msg').modal();-->
            <#--}-->
        <#--});-->
        // Add by zhajh at 20180502 添加标签 End

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
		console.log(parameterInfos)
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
                 //var attrName=tdArr.eq(i).text();
                 var attrName=tdArr.eq(i).find("#hidAttrName").val();//商品分类属性名称
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
	       var warningQuantity="0";//预警数量
		   var warningMail="";//预警提醒人邮箱
	       if(tdArr.eq(4).text()){
	          warningQuantity=tdArr.eq(4).text();
	       }
	       if(tdArr.eq(5).text()){
	       	  warningMail=tdArr.eq(5).text();
		   }
	       var inventoryItemId = tdArr.eq(1).find("input").val();//库存明细ID
	       curFacilityInfo=facilityId+"|"+inventoryItemId+"|"+accountingQuantityTotal+"|"+warningQuantity+"|"+warningMail;
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
	              
	              var tr1=' <tr><td><input type="hidden" name="" id="" value="'+facilityId+'">'+facilityName+'</td> <td><input type="hidden" name="curInventoryItemId" id="curInventoryItemId" value="'+inventoryItemId+'">'+totalNum+'</td><td>'+alreadyLockQuantitySum+'</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td><td>'+warningMail+'</td> <td class="fc_td"><button type="button" class="js-button-facility btn btn-danger btn-sm ">${uiLabelMap.BrandDel}</button></td></tr>';
	              
	 			  $('.js-table_3>tbody').append(tr1);
	 			  
	           }
	        }
	    });
	  } 
   }
   
 
   // 商品规格的添加 
   function addFeature(name, evt) {
      if (evt) {
         var featureName=evt.params.data.text;
         var productFeatureTypeId=evt.params.data.id;
         getProductFeatureListById(productFeatureTypeId,featureName)
      }
   }
   // 商品规格的删除
   function delFeature(name, evt) {
      if (evt) {
         var featureName=evt.params.data.text;
         var featureId=evt.params.data.id;
         var aad="#"+featureId;
         $(aad).remove();
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
                ' <td><label style="text-align:left;width:100%">'+featureName+'</label> </td><td><button data-id="'+productFeatureTypeId+'" type="button" class="btn btn-primary js-addProductFeatureValue"><i class="fa fa-plus">规格值</i> </button></td>'+
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
		            if(featureIdList){
			            for (var i=0;i<featureIdList.length;i++){
			                var curFeatureId=featureIdList[i];
			                if(ckVal==curFeatureId){
			                   $(this).attr("checked",true);
			                }
			            }
		            }
		        });
		        var operateType='${parameters.operateType?if_exists}';
		        if(operateType=='update'){
		             if(featureIds){
				        $(".js-feature").find("input[type=checkbox]").attr("disabled","disabled");
				     }
		        }
	        }
	    });
	  } 
   } 	
   
   
  
    
   // 取得选择的规格值
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
			   var chk1=tr.find('td').eq(0);//规格选择
			   var text1=tr.find('td').eq(1);//规格名称
			   var chk2=tr.find('td').eq(2);//规格选择
			   var text2=tr.find('td').eq(3);//规格名称
			   var chk3=tr.find('td').eq(4);//规格选择
			   var text3=tr.find('td').eq(5);//规格名称
			  
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
			var endFlg=trIdItem.substr(trIdItem.length-1,1);
			if(endFlg=="|"){
			   //alert("aaa");
			   trIdItem=trIdItem.substr(0,trIdItem.length-1);
			}
			tableItems=tableItems+","+trItem.substr(1,trItem.length);
			tableIdItems=tableIdItems+","+trIdItem.substr(1,trIdItem.length);
			$("#productFeatureInfos").val(tableIdItems.substr(1,tableIdItems.length));
            
			
		})
			jQuery.ajax({
		        url: '<@ofbizUrl>getProductFeatureInfoListByFeature</@ofbizUrl>',
		        type: 'POST',
		        data: {
		             'productFeatureInfos' : tableItems.substr(1,tableItems.length),
		             'productFeatureIdInfos' : tableIdItems.substr(1,tableIdItems.length)
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
		               var featureId=productFeatureInfoList[i].featureId;
		               //alert(name);
		               var tr='<tr><td style="text-align:left;"><input type="hidden" name="" id="" value="'+featureId+'"><input class="js-checkchild" type="checkbox" id="" data-id="'+featureId+'"  value="'+name+'"/>'+name+'</td></tr>';
		               $table.append(tr);
		           }
		           if(listSize==0){
		               //设置提示弹出框内容
			    	   $('#modal_msg #modal_msg_body').html("如果选择规格，需要选择商品规格属性！");
			    	   $('#modal_msg').modal();
		           }else{
			           //设置弹出框内容
					   $('#selectSku').modal();
				   }
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
			//var productCategoryId='${parameters.productCategoryId?if_exists}';
			var productCategoryId=$("#productCategoryId").val();;
            // 商品名称	
            var productName=$("#productName").val();
            // 商品描述	
            var productSubheadName=$("#productSubheadName").val();
            // 是否上架	
            var isOnline=$("#isOnline").val();
            // 销售开始时间	
            var startTime=$("#startTime").val();
            // 销售结束时间	
            var endTime=$("#endTime").val();
            // 商家名称	
            var businessPartyId=$("#businessPartyId").length==0?'':$("#businessPartyId").val();
            // 商品品牌	
            var brandId=$("#brandId").val();
            // 体积	
            var volume=$("#volume").val();
            // 重量	
            var weight=$("#weight").val();
            // 是否使用规格	
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
	  		// 商品规格信息
	  		getSelectedFeaturesForUpdate();
            var productFeatureInfos=$("#productFeatureInfos").val();
	  			   
            // 图片信息
            var productContentInfos=$("#productContentInfos").val();
            // 商品关系信息
            assocProductGoodInfos();
            var productAssocInfos=$("#productAssocInfos").val();

			 // Add by zhajh at 20180305 yabiz相关内容 Begin
         // 推荐到首页
         var isRecommendHomePage= $('.radio').find('input[name=isRecommendHomePage]').length==0?'':$('.radio').find('input[name=isRecommendHomePage]:checked').val();;
         // 支持服务类型
         var supportServices = "";
         $('input[name="supportServiceType"]:checked').each(function(){
             supportServices = supportServices + "," + $(this).val();
         });
         var supportServiceType=supportServices.substr(1,supportServices.length);
         // 积分抵扣
         var integralDeductionType=$("#integralDeductionType").length==0?'':$("#integralDeductionType").val();
         // 积分抵扣上限
         var integralDeductionUpper=$("#integralDeductionUpper").length==0?'':$("#integralDeductionUpper").val();
         // 每人限购数量
         var purchaseLimitationQuantity=$("#purchaseLimitationQuantity").length==0?'':$("#purchaseLimitationQuantity").val();
         // 列表展示
         var isListShow=$('.radio').find('input[name=isListShow]').length==0?'':$('.radio').find('input[name=isListShow]:checked').val();
         // 代金券面额
         var voucherAmount=$("#voucherAmount").length==0?'':$("#voucherAmount").val();
         // 使用限制
         var useLimit=$('.radio').find('input[name=useLimit]').length==0?'':$('.radio').find('input[name=useLimit]:checked').val();
         // 使用开始时间
         var useStartTime=$("#useStartTime").length==0?'':$("#useStartTime").val();
         // 使用结束时间
         var useEndTime=$("#useEndTime").length==0?'':$("#useEndTime").val();
         // 是否保税商品
         var isBondedGoods=$('.radio').find('input[name=isBondedGoods]').length==0?'':$('.radio').find('input[name=isBondedGoods]:checked').val();
         // 店铺信息
         var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
         // 自营与非自营标记
         var isInner =$('#isInner').length==0?'':$('#isInner').val();
         // 主营分类
         var platformClassId=$('#platformClassId').length==0?'':$('#platformClassId').data("id");
         // 供应商编码
         var providerId=$('#providerId').length==0?'':$('#providerId').val();
			 // Add by zhajh at 20180305 yabiz相关内容 End


            
		    //////////////////////////////////////////
         $.ajax({
             url:'<@ofbizUrl>setSessionByParam</@ofbizUrl>',
             dataType:'json',
             type:'post',
             data:{
                 'attrName' : "mobileDetails",
                 'attrVal' : mobileDetails
             },
             success:function(data){

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
                                                               "productParameterInfos="+productParameterInfos1+"&"+
                                                               "productAttrInfos="+productAttrInfos+"&"+
                                                               "productFacilityInfos="+productFacilityInfos+"&"+
                                                               "pcDetails="+pcDetails+"&"+
//    														   "mobileDetails="+mobileDetails+"&"+
                                                               "mobileDetails=&"+
                                                               "productFeatureInfos="+productFeatureInfos+"&"+
                                                               "productContentInfos="+productContentInfos+"&"+
                                                               "productAssocInfos="+productAssocInfos+"&"+
                                                               "isRecommendHomePage="+isRecommendHomePage+"&"+
                                                               "supportServiceType="+supportServiceType+"&"+
                                                               "integralDeductionType="+integralDeductionType+"&"+
                                                               "integralDeductionUpper="+integralDeductionUpper+"&"+
                                                               "purchaseLimitationQuantity="+purchaseLimitationQuantity+"&"+
                                                               "isListShow="+isListShow+"&"+
                                                               "voucherAmount="+voucherAmount+"&"+
                                                               "useLimit="+useLimit+"&"+
                                                               "useStartTime="+useStartTime+"&"+
                                                               "useEndTime="+useEndTime+"&"+
                                                               "isBondedGoods="+isBondedGoods+"&"+
                                                               "productStoreId="+productStoreId+"&"+
                                                               "isInner="+isInner+"&"+
                                                               "platformClassId="+platformClassId+"&"+
                                                               "providerId="+providerId+

                                    "</@ofbizUrl>";
                 },
                 error:function(){
                     alert("操作失败")
                 }
             })
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
	               var tr=' <tr><td><input type="hidden" name="" id="" value="'+productId+'">'+productId+'</td><td>'+productName+'</td><td>'+productTypeName+'</td><td>'+productContentId+'</td><td>'+productGoodFeature+'</td><td>'+salesPrice+'</td><td class="fc_td"><button type="button" class="js-button-assocgood btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	 			   $('.js-table_4>tbody').append(tr);
	           
	           
	           }
	        }
	    });
	  } 
   }
   
   
   // 取得关联商品信息
   function assocProductGoodInfos(){
	    var tassocProductGoodInfos = "";
        var curAssocProductGoodInfo="";
        $('.js-table_4>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           curAssocProductGoodInfo="";
	       var tdArr = $(this).children();
	       var productId = tdArr.eq(0).find("input").val();//关联商品ID
	       curAssocProductGoodInfo=productId;
           tassocProductGoodInfos = tassocProductGoodInfos + "," + curAssocProductGoodInfo;
	    }); 
	    var assocProductGoodInfos=tassocProductGoodInfos.substr(1,tassocProductGoodInfos.length);
	    $("#productAssocInfos").val(assocProductGoodInfos);
	}
	
	
   // 取得关联商品列表信息(更新用)
   function getAssocGoodListById(id){
	  if (id != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getAssocGoodListById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'id' : id
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
	               var tr=' <tr><td><input type="hidden" name="" id="" value="'+productId+'">'+productId+'</td><td>'+productName+'</td><td>'+productTypeName+'</td><td>'+productContentId+'</td><td>'+productGoodFeature+'</td><td>'+salesPrice+'</td><td class="fc_td"><button type="button" class="js-button-assocgood btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	 			   $('.js-table_4>tbody').append(tr);
	           
	           
	           }
	        }
	    });
	  } 
   }
	
   // 取得商品图片信息
   function getProductPicById(productId){
	  if (productId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductPicById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productId' : productId
	        },
	        success: function(data) {
	       	   var time = (new Date()).getTime();
	           var productAdditionalImage1=data.productAdditionalImage1;
	           var productAdditionalImage2=data.productAdditionalImage2;
	           var productAdditionalImage3=data.productAdditionalImage3;
	           var productAdditionalImage4=data.productAdditionalImage4;
	           var productAdditionalImage5=data.productAdditionalImage5;
	           
	           if(productAdditionalImage1!=""){
	               $('#img1').attr('src',productAdditionalImage1+'?v='+time);
	               $('#mainImg').attr('value',productAdditionalImage1+'?v='+time);
	               $(".js-img1").show();
	           }
	           if(productAdditionalImage2!=""){
	               $('#img2').attr('src',productAdditionalImage2+'?v='+time);
	               $(".js-img2").show();
	           }
	           if(productAdditionalImage3!=""){
	               $('#img3').attr('src',productAdditionalImage3+'?v='+time);
	               $(".js-img3").show();
	           }
	           if(productAdditionalImage4!=""){
	               $('#img4').attr('src',productAdditionalImage4+'?v='+time);
	               $(".js-img4").show();
	           }
	           if(productAdditionalImage5!=""){
	               $('#img5').attr('src',productAdditionalImage5+'?v='+time);
	               $(".js-img5").show();
	           }
	        }
	    });
	  } 
   }	
	
	
   function imageManage() {
     var imgType="product";
     var imgProductId=$("#productId").val();
     var curPartyId='${requestAttributes.userLogin.partyId?if_exists}';
       var curIsInner='${requestAttributes.isInner?if_exists}';
  	 $.chooseImage.int({
	    userId: 3446,
	    serverChooseNum: 5,
	    getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
	    submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
	    submitServerImgUrl: '',
	    submitNetworkImgUrl: '',
	    otherObj:{
	       type:imgType,
	       productId:imgProductId,
           ownerPartyId:curPartyId,
            isInner:curIsInner
	    }
	});
	$.chooseImage.show()
  }
  
  // 判断商品是否是Sku
  function isSkuProduct(productId){
	  if (productId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>isSkuProduct</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productId' : productId
	        },
	        success: function(data) {
	           var skuFlg=data.skuFlg;
	           if(skuFlg=="Y"){
	              getUpdatePara();
	           }else{
		       	  getSelectedFeatures();
		       	  
	           }
	        }
	    });
	  } 
   }
   
   // 判断库存是否是使用
  function isFacilityUsed(facilityId,productId){
	  if (facilityId!=""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>isFacilityForProduct</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productId' : productId,
	             'facilityId' : facilityId
	        },
	        success: function(data) {
	           var isUsedFlg=data.isUsedFlg;
	           if(isUsedFlg=="Y"){
	               //设置提示弹出框内容
	    	  	  $('#modal_msg #modal_msg_body').html("该商品的库存正在使用中，不能删除");
	    	  	  $('#modal_msg').modal();
	           }else{
		       	   //设置删除弹出框内容
				   $('#modal_confirm #modal_confirm_body').html("确定要删除该商品库存信息，是否继续删除");
				   $('#modal_confirm').modal('show');
		       	  
	           }
	        }
	    });
	  } 
   }
   
   // 添加准备商品实体
   function setPrepareProductEntity(productTypeId,productCategoryId){
	  if (productTypeId!="" && productCategoryId!=""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>setPrepareProductEntity</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productTypeId' : productTypeId,
	             'productCategoryId' : productCategoryId,
	             'isFeatureGoods' : "N"
	        },
	        success: function(data) {
	           var PrepareProductId=data.productId;
	           $("#productId").val(PrepareProductId);

		       CKEDITOR.replace("mobileDetails",{
		            filebrowserImageBrowseUrl: '/content/control/file${externalKeyParam}?rootName='+PrepareProductId+'&directory=/products/'+PrepareProductId,
                   <#--filebrowserImageBrowseUrl: '/content/control/file${externalKeyParam}&file?directory=/datasource',-->
	           });
	        }
	    });
	  } 
   }
   
   
   // 取得选择的规格值
   function getSelectedFeaturesForUpdate(){
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
			   var chk1=tr.find('td').eq(0);//规格选择
			   var text1=tr.find('td').eq(1);//规格名称
			   var chk2=tr.find('td').eq(2);//规格选择
			   var text2=tr.find('td').eq(3);//规格名称
			   var chk3=tr.find('td').eq(4);//规格选择
			   var text3=tr.find('td').eq(5);//规格名称
			  
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
			var endFlg=trIdItem.substr(trIdItem.length-1,1);
			if(endFlg=="|"){
			   //alert("aaa");
			   trIdItem=trIdItem.substr(0,trIdItem.length-1);
			}
			tableItems=tableItems+","+trItem.substr(1,trItem.length);
			tableIdItems=tableIdItems+","+trIdItem.substr(1,trIdItem.length);
			$("#productFeatureInfos").val(tableIdItems.substr(1,tableIdItems.length));
            
			
		})
    }
   
   <#--// 删除图片-->
   <#--function delProductContentImg(){-->
       <#--var productContentTypeId=imgContentTypeId;-->
       <#--var productId="${productId?if_exists}";-->
       <#--jQuery.ajax({-->
            <#--url: '<@ofbizUrl>delProductContentImg</@ofbizUrl>',-->
            <#--type: 'POST',-->
            <#--data: {-->
                <#--'productId' : productId,-->
                <#--'productContentTypeId' : productContentTypeId-->
            <#--},-->
            <#--success: function(data) {-->
                <#--var productFeatureGoodItem=data.productFeatureGoodItem;-->
                <#--if(productFeatureGoodItem.typeNo=='1'){-->
                   <#--$('#img1').attr('src','');-->
                   <#--$('#mainImg').attr('value','');-->
                   <#--$(".js-img1").hide();-->
                <#--}else if(productFeatureGoodItem.typeNo=='2'){-->
                   <#--$('#img2').attr('src','');-->
                   <#--$(".js-img2").hide();-->
                <#--}else if(productFeatureGoodItem.typeNo=='3'){-->
                   <#--$('#img3').attr('src','');-->
                   <#--$(".js-img3").hide();-->
                <#--}else if(productFeatureGoodItem.typeNo=='4'){-->
                   <#--$('#img4').attr('src','');-->
                   <#--$(".js-img4").hide();-->
                <#--}else if(productFeatureGoodItem.typeNo=='5'){-->
                   <#--$('#img5').attr('src','');-->
                   <#--$(".js-img5").hide();-->
                <#--}-->
            <#--}-->
        <#--});-->
    <#--}-->
    // 删除图片
    function delProductContentImg(){
        var productContentTypeId=imgContentTypeId;
        var productId="${productId?if_exists}";
        jQuery.ajax({
            url: '<@ofbizUrl>delProductContentImg</@ofbizUrl>',
            type: 'POST',
            data: {
                'productId' : productId,
                'productContentTypeId' : productContentTypeId
            },
            success: function(data) {
                var productFeatureGoodItem=data.productFeatureGoodItem;
                if(productFeatureGoodItem.typeNo=='1'){
                    $('#img1').attr('src','');
                    $('#mainImg').attr('value','');
                    $('#addImg1').attr('value','');
                    $('#editProductContentImg1').text("添加");
                    $('#delProductContentImg1').hide();
//                    $(".js-img1").hide();
                }else if(productFeatureGoodItem.typeNo=='2'){
                    $('#img2').attr('src','');
                    $('#addImg2').attr('value','');
                    $('#editProductContentImg2').text("添加");
                    $('#delProductContentImg2').hide();
//                    $(".js-img2").hide();
                }else if(productFeatureGoodItem.typeNo=='3'){
                    $('#img3').attr('src','');
                    $('#addImg3').attr('value','');
                    $('#editProductContentImg3').text("添加");
                    $('#delProductContentImg3').hide();
//                    $(".js-img3").hide();
                }else if(productFeatureGoodItem.typeNo=='4'){
                    $('#img4').attr('src','');
                    $('#addImg4').attr('value','');
                    $('#editProductContentImg4').text("添加");
                    $('#delProductContentImg4').hide();
//                    $(".js-img4").hide();
                }else if(productFeatureGoodItem.typeNo=='5'){
                    $('#img5').attr('src','');
                    $('#addImg5').attr('value','');
                    $('#editProductContentImg5').text("添加");
                    $('#delProductContentImg5').hide();
//                    $(".js-img5").hide();
                }
            }
        });
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    
    // 商品分类初始化
    function getInitProductCategoryByLevel(){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
	    jQuery.ajax({
	        url: '<@ofbizUrl>getInitProductCategoryByLevel</@ofbizUrl>',
	        type: 'POST',
            data:{
                'productStoreId' : productStoreId,
                'isInner':isInner
            },
	        success: function(data) {
	           var productCategoryLevel1List= data.productCategoryLevel1List;
	           var productCategoryLevel2List= data.productCategoryLevel2List;
	           var productCategoryLevel3List= data.productCategoryLevel3List;
	           var productCategoryLevel1Info= data.productCategoryLevel1Info;
	           var productCategoryLevel2Info= data.productCategoryLevel2Info;
	           var productCategoryLevel3Info= data.productCategoryLevel3Info;
	           
	           $("#modCategory .xl_first_ul").empty();
			   for (var i=0;i<productCategoryLevel1List.length;i++){
		            var productCategoryId =(productCategoryLevel1List[i].productCategoryId);
		            var categoryName =(productCategoryLevel1List[i].categoryName);
			        var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>"
			        $("#modCategory .xl_first_ul").append(liHtml);
			        
		       }
		       $("#modCategory .xl_second_ul").empty();
		       for (var i=0;i<productCategoryLevel2List.length;i++){
		            var productCategoryId =(productCategoryLevel2List[i].productCategoryId);
		            var categoryName =(productCategoryLevel2List[i].categoryName);
		            
		            var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>";
			        $("#modCategory .xl_second_ul").append(liHtml);
		       }
		       $("#modCategory .xl_third_ul").empty();
		       for (var i=0;i<productCategoryLevel3List.length;i++){
		            var productCategoryId =(productCategoryLevel3List[i].productCategoryId);
		            var categoryName =(productCategoryLevel3List[i].categoryName);
		            
		            var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>";
			        $("#modCategory .xl_third_ul").append(liHtml);
		       }
		       
		       // 设置选择项目
		       //根据a返回它的data-id
               $('#modCategory .xl_first_ul li[data-id='+productCategoryLevel1Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
               if(productCategoryLevel2Info){
               $('#modCategory .xl_second_ul li[data-id='+productCategoryLevel2Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
               }
               if(productCategoryLevel3Info){
               	  $('#modCategory .xl_third_ul li[data-id='+productCategoryLevel3Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
               }
               
		       $("#modCategory #curProductCategoryIdLevel1").val(productCategoryLevel1Info.productCategoryId);
		       if(productCategoryLevel2Info){
		       $("#modCategory #curProductCategoryIdLevel2").val(productCategoryLevel2Info.productCategoryId);
		       }
		       if(productCategoryLevel3Info){
		       	  $("#modCategory #curProductCategoryIdLevel3").val(productCategoryLevel3Info.productCategoryId);
		       }
		       
		       $("#modCategory #curProductCategoryNameLevel1").val(productCategoryLevel1Info.categoryName);
		       if(productCategoryLevel2Info){
		       $("#modCategory #curProductCategoryNameLevel2").val(productCategoryLevel2Info.categoryName);
		       }
		       if(productCategoryLevel3Info){
		       $("#modCategory #curProductCategoryNameLevel3").val(productCategoryLevel3Info.categoryName);
		       }
		       if($("#modCategory #curProductCategoryNameLevel3").val()!=""){
		          getProductCountById($("#modCategory #curProductCategoryIdLevel3").val());
		       }else{
		          $(".js-productCount").text("当前选择的是："+ $("#modCategory #curProductCategoryNameLevel1").val()+">"+ $("#modCategory #curProductCategoryNameLevel2").val()+">"+ $("#modCategory #curProductCategoryNameLevel3").val());
		       }
	        }
	    });
	}
	
	
	
	//分类级别的查找功能
	function searchProductCategoryLevelByName(productCategoryLevel,productCategoryId,categoryName){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
	    $.ajax({
            url:'<@ofbizUrl>searchProductCategoryLevelByName</@ofbizUrl>',
            dataType:'json',
            type:'post',
            beforeSend:function(){
               if(productCategoryLevel=="1"){
                 $('#modCategory .search_icon1_1').removeClass('search_icon1');
               }else if(productCategoryLevel=="2"){
                 $('#modCategory .search_icon2_1').removeClass('search_icon2');
               
               }else if(productCategoryLevel=="3"){
                 $('#modCategory .search_icon3_1').removeClass('search_icon3');
               }
            },
            data:{
                 'productCategoryLevel' : productCategoryLevel,
	             'productCategoryId':productCategoryId,
	             'categoryName':categoryName,
                'productStoreId' : productStoreId,
                'isInner':isInner
            },
            success:function(data){
                if(data.success){
                    //根据a返回它的data-id
                    var productCategoryLevel1List= data.productCategoryLevel1List;
		            var productCategoryLevel2List= data.productCategoryLevel2List;
		            var productCategoryLevel3List= data.productCategoryLevel3List;
		            var productCategoryLevel1Info= data.productCategoryLevel1Info;
		            var productCategoryLevel2Info= data.productCategoryLevel2Info;
		            var productCategoryLevel3Info= data.productCategoryLevel3Info;
		            
		            var productCategoryIdLevel1 ="";
		            var productCategoryIdLevel2 ="";
		            var productCategoryIdLevel3 ="";
		            
		            var productCategoryNameLevel1 ="";
		            var productCategoryNameLevel2 ="";
		            var productCategoryNameLevel3 ="";
		            if(productCategoryLevel1Info){
		               productCategoryIdLevel1 =productCategoryLevel1Info.productCategoryId;
		               productCategoryNameLevel1 =productCategoryLevel1Info.categoryName;
		            }
		            if(productCategoryLevel2Info){
		               productCategoryIdLevel2 =productCategoryLevel2Info.productCategoryId;
		               productCategoryNameLevel2 =productCategoryLevel2Info.categoryName;
		            }
		            if(productCategoryLevel3Info){
		               productCategoryIdLevel3 =productCategoryLevel3Info.productCategoryId;
		               productCategoryNameLevel3 =productCategoryLevel3Info.categoryName;
		            }
		            
		            
		            var level=data.productCategoryLevel;
		           
		            if(level=='1'){
		                //把返回的数据放到二级菜单
                        var b="";
                        for(var i=0;i<productCategoryLevel2List.length;i++){
                            b+='<li data-id="'+productCategoryLevel2List[i].productCategoryId+'">'+productCategoryLevel2List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $("#modCategory .xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != null){
                           $('#modCategory .xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#modCategory #curProductCategoryNameLevel2").val(productCategoryNameLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $("#modCategory .xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#modCategory #curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        
                        if(productCategoryIdLevel1 != ""){
                            $('#modCategory .xl_first_ul li[data-id='+productCategoryIdLevel1+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel1").val(productCategoryIdLevel1);
                        $("#modCategory #curProductCategoryNameLevel1").val(productCategoryNameLevel1);
                        
				        $('#modCategory .search_icon1_1').addClass('search_icon1');
				        if(productCategoryNameLevel3!=""){
                           getProductCountById($("#modCategory #curProductCategoryIdLevel3").val());
                        }else{
                           $(".js-productCount").text("当前选择的是："+ $("#modCategory #curProductCategoryNameLevel1").val()+">"+ $("#modCategory #curProductCategoryNameLevel2").val()+">"+ $("#modCategory #curProductCategoryNameLevel3").val());
                        }
			        }else if(level=='2'){
			            //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $("#modCategory .xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('#modCategory .xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#modCategory #curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        
                        
                        if(productCategoryIdLevel2 != ""){
                           $('#modCategory .xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#modCategory #curProductCategoryNameLevel2").val(productCategoryNameLevel2);
                        
			            $('#modCategory .search_icon2_1').addClass('search_icon2');
			            if(productCategoryNameLevel3!=""){
                           getProductCountById($("#modCategory #curProductCategoryIdLevel3").val());
                        }else{
                           $(".js-productCount").text("当前选择的是："+ $("#modCategory #curProductCategoryNameLevel1").val()+">"+ $("#modCategory #curProductCategoryNameLevel2").val()+">"+ $("#modCategory #curProductCategoryNameLevel3").val());
                        }
			        }else if(level='3'){
			            if(productCategoryIdLevel3 != ""){
                           $('#modCategory .xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#modCategory #curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryNameLevel3!=""){
                           getProductCountById($("#modCategory #curProductCategoryIdLevel3").val());
                        }else{
                           $(".js-productCount").text("当前选择的是："+ $("#modCategory #curProductCategoryNameLevel1").val()+">"+ $("#modCategory #curProductCategoryNameLevel2").val()+">"+ $("#modCategory#curProductCategoryNameLevel3").val());
                        }
			        }

                }else{
                    alert("操作失败！");
                }
            },
            error:function(){
                alert("操作失败！");
                  $('.search_icon2_1').addClass('search_icon2');
            }
        })
	}
	
	
	
	//分类级别的选中处理功能
	function seclectdeItemProductCategoryLevelById(productCategoryLevel,productCategoryId){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
	    $.ajax({
            url:'<@ofbizUrl>seclectdeItemProductCategoryLevelById</@ofbizUrl>',
            dataType:'json',
            type:'post',
            data:{
             'productCategoryLevel' : productCategoryLevel,
             'productCategoryId':productCategoryId,
                'productStoreId' : productStoreId,
                'isInner':isInner
            },
            success:function(data){
                //把返回的数据放到二级菜单
                if(data.success){
                    //根据a返回它的data-id
                    var productCategoryLevel1List= data.productCategoryLevel1List;
		            var productCategoryLevel2List= data.productCategoryLevel2List;
		            var productCategoryLevel3List= data.productCategoryLevel3List;
		            var productCategoryLevel1Info= data.productCategoryLevel1Info;
		            var productCategoryLevel2Info= data.productCategoryLevel2Info;
		            var productCategoryLevel3Info= data.productCategoryLevel3Info;
		            
		            
		            var productCategoryIdLevel1 ="";
		            var productCategoryIdLevel2 ="";
		            var productCategoryIdLevel3 ="";
		            
		            var productCategoryNameLevel1 ="";
		            var productCategoryNameLevel2 ="";
		            var productCategoryNameLevel3 ="";
		            if(productCategoryLevel1Info){
		               productCategoryIdLevel1 =productCategoryLevel1Info.productCategoryId;
		               productCategoryNameLevel1 =productCategoryLevel1Info.categoryName;
		            }
		            if(productCategoryLevel2Info){
		               productCategoryIdLevel2 =productCategoryLevel2Info.productCategoryId;
		               productCategoryNameLevel2 =productCategoryLevel2Info.categoryName;
		            }
		            if(productCategoryLevel3Info){
		               productCategoryIdLevel3 =productCategoryLevel3Info.productCategoryId;
		               productCategoryNameLevel3 =productCategoryLevel3Info.categoryName;
		            }
		            
		            var level=data.productCategoryLevel;
		            if(level=='1'){
                        //把返回的数据放到二级菜单
                        var b="";
                        for(var i=0;i<productCategoryLevel2List.length;i++){
                            b+='<li data-id="'+productCategoryLevel2List[i].productCategoryId+'">'+productCategoryLevel2List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $("#modCategory.xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != ""){
                           $('#modCategory .xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#modCategory #curProductCategoryNameLevel2").val(productCategoryNameLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        //alert(c);
                        $("#modCategory .xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                            $('#modCategory .xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#modCategory #curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryNameLevel3!=""){
                           getProductCountById($("#modCategory #curProductCategoryIdLevel3").val());
                        }else{
                           $(".js-productCount").text("当前选择的是："+ $("#modCategory #curProductCategoryNameLevel1").val()+">"+ $("#modCategory#curProductCategoryNameLevel2").val()+">"+ $("#modCategory #curProductCategoryNameLevel3").val());
                        }
                    }else if(level=='2'){
                       //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $("#modCategory .xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('#modCategory .xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modCategory #curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#modCategory #curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryIdLevel3 !=""){
                            getProductCountById($("#modCategory #curProductCategoryIdLevel3").val());
                        }else{
                            $(".js-productCount").text("当前选择的是："+ $("#modCategory #curProductCategoryNameLevel1").val()+">"+ $("#modCategory #curProductCategoryNameLevel2").val()+">"+ $("#modCategory #curProductCategoryNameLevel3").val());
                        }
                    }else if(level=='3'){
                         
                    }
                }else{
                    alert("操作失败")
                }
            },
            error:function(){
                alert("操作失败")
           }
        })
	}
	
   // 取得分类商品数量信息
   function getProductCountById(curProductCategoryId){
	  if (curProductCategoryId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductCountById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'curProductCategoryId' : curProductCategoryId
	        },
	        success: function(data) {
	           //alert(data.listSize);
	           var listSize=data.listSize;
	           $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val()+"【"+listSize+"件商品】");
	        }
	    });
	  } 
   }
   
   
  
    
    /////////////////////////////////////////////////////////////////////////////////////////
    
   // 取得商品分类品牌信息
   function getProductCategoryInfoById(curProductCategoryId){
	  if (curProductCategoryId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductCategoryInfoById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'curProductCategoryId' : curProductCategoryId
	        },
	        success: function(data) {
	           var  brandList=data.brandList;
	           var  listSize=data.listSize;
	           var  productOptionInfos=data.productOptionInfos;
	           var  aa=data.aa;
	           var  bb=data.bb;
	           var  cc=data.cc;
	           
	           $(".select2Brand").select2('val', '');
	           $(".select2Brand option").remove();
	           $('.select2Brand').append("<option value=''></option>"); 
	           for (var i=0;i<brandList.length;i++){
		            var selValData =(brandList[i].productBrandId);
		            var selNameData=(brandList[i].brandName);
		            $('#brandId').append("<option value='" + selValData + "'>" + selNameData + "</option>"); 
		       }
		       
		       var productCategoryAttributeTr='';
		       $('.js-table_2>tbody').empty();
		       if(listSize>0){
		           for(var j=0;j<cc;j++){
		               productCategoryAttributeTr=' <tr>';
		               for(var k=j*3;k<(j+1)*3;k++){
		                   if(k<listSize){
	 		                   var productOptionInfo=productOptionInfos[k];
	 		                   var optionList=productOptionInfo.optionList;
	 		                   var selOption='';
		 		               for(var m=0;m<optionList.length;m++){
		 		                      var optionInfo=optionList[m];
			                          selOption=selOption+'<option value="'+optionInfo.productOptionId+'">'+optionInfo.optionName+'</option>'; 
			                   }
			                   
			                   if(productOptionInfo.isRequired=='Y'){
		 		                     productCategoryAttributeTr=productCategoryAttributeTr+
		 		                         '<td><i class="required-mark">*</i>'+productOptionInfo.attrName+'<input type="hidden" name="" id="" value="'+productOptionInfo.productCategoryId+'">'+
			                             '     <input type="hidden" name="hidAttrName" id="hidAttrName" value="'+productOptionInfo.attrName+'">'+
			                             '</td>'+
			                             '<td>' +
			                             '    <div class="form-group col-sm-12" data-type="required" data-mark="商品属性('+productOptionInfo.attrName+')">'+
			                             '          <select class="form-control dp-vd">'+
			                             '               <option value=""></option>'+ selOption+
			                             '          </select>'+
			                             '          <p class="dp-error-msg"></p>'+
			                             '    </div>'+
			                             '</td>';
		 		                }else{
		 		                     productCategoryAttributeTr=productCategoryAttributeTr+
		 		                         '<td>'+productOptionInfo.attrName+'<input type="hidden" name="" id="" value="'+productOptionInfo.productCategoryId+'">'+
			                             '     <input type="hidden" name="hidAttrName" id="hidAttrName" value="'+productOptionInfo.attrName+'">'+
			                             '</td>'+
			                             '<td>' +
			                             '          <select class="form-control">'+
			                             '               <option value=""></option>'+ selOption+
			                             '          </select>'+
			                             '</td>';
		 		                }
	 		               }
		               
		               }
		               productCategoryAttributeTr=productCategoryAttributeTr+' </tr>';
		               $('.js-table_2>tbody').append(productCategoryAttributeTr);
		               //alert(productCategoryAttributeTr);
		           }
		       }
	        }
	    });
	  } 
   }
   
   
   function imageManageEdit() {
     var imgType="product";
     var imgProductId=$("#productId").val();
       var curPartyId='${requestAttributes.userLogin.partyId?if_exists}';
       var curIsInner='${requestAttributes.isInner?if_exists}';
  	 $.chooseImage.int({
	    userId: 3446,
	    serverChooseNum: 1,
	    getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
	    submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
	    submitServerImgUrl: '',
	    submitNetworkImgUrl: '',
         imgIndex:imgContentIndex,
	    otherObj:{
	       type:imgType,
	       productId:imgProductId,
	       curProductContentTypeId:imgContentTypeId,
            ownerPartyId:curPartyId,
            isInner:curIsInner
	    }
	});
	$.chooseImage.show()
  }



	//////////////////////////////////////////////
    // 保存商品规格值信息到session中
    function saveProductFeatureList(productFeatureName,sequenceNum){
        if (productFeatureName != "" || sequenceNum != ""){
            jQuery.ajax({
                url: '<@ofbizUrl>saveProductFeatureList</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productFeatureName' : productFeatureName,
                    'sequenceNum' : sequenceNum
                },
                success: function(data) {

                }
            });
        }
    }

    // 根据规格类型取出商品规格值
    function getProductFeatureByTypeId(productFeatureTypeId){
        if (productFeatureTypeId != ""){
            jQuery.ajax({
                url: '<@ofbizUrl>getProductFeatureByTypeId</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productFeatureTypeId' : productFeatureTypeId
                },
                success: function(data) {
                    var productFeatureByTypeIdList= data.productFeatureByTypeIdList;
                    var productFeatureTypeInfo= data.productFeatureTypeInfo;

                    for (var i=0;i<productFeatureByTypeIdList.length;i++){
                        var productFeatureId=(productFeatureByTypeIdList[i].productFeatureId);
                        var productFeatureName =(productFeatureByTypeIdList[i].productFeatureName);
                        var sequenceNum =(productFeatureByTypeIdList[i].sequenceNum);
                        //var trHtml="<tr><td><input  type='checkbox' class='js-checkchild' name='ckb' value="+productFeatureId+"><input type='hidden' id='curproductFeatureId' value="+productFeatureId+"><input type='hidden' id='curOptionType' value='update'></td><td><input type='text' value="+productFeatureName+"></td><td><input type='text' value="+sequenceNum+"></td><td><button type='button' class='js-button btn btn-danger btn-sm'>删除</button></td></tr>";
                        var trHtml="<tr><td><input type='hidden' id='curproductFeatureId' value="+productFeatureId+"><input type='hidden' id='curOptionType' value='update'></td><td><input type='text' value="+productFeatureName+"></td><td><input type='text' value="+sequenceNum+"></td></tr>";
                        $('#addProductFeature #tabFreatureValue>tbody').append(trHtml);
                    }
					//设置弹出框内容

					if(productFeatureTypeInfo){
                        $('#addProductFeature #productFeatureTypeName').val(productFeatureTypeInfo.productFeatureTypeName);
                        $('#addProductFeature #description').val(productFeatureTypeInfo.description);
					}
                    $('#addProductFeature #featureOperateType').val("update");
					$('#addProductFeature #productFeatureTypeId').val(productFeatureTypeId);


					$('#addProductFeature').modal();
                }
            });
        }
    }

    function saveProductFeatureList1(tFeatureInfos){
        if (tFeatureInfos != ""){
            jQuery.ajax({
                url: '<@ofbizUrl>saveProductFeatureList</@ofbizUrl>',
                type: 'POST',
                data: {
                    'tFeatureInfos' : tFeatureInfos
                },
                success: function(data) {
                }
            });
        }
    }


    function delTr(ckb){
        //获取选中的复选框，然后循环遍历删除
        var ckbs=$("input[name="+ckb+"]:checked");
        //if(ckbs.size()==0){
        // alert("要删除指定行，需选中要删除的行！");
        // return;
        //}
        ckbs.each(function(){
            $(this).parent().parent().remove();
        });
    }



    // 判断商品规格是否是被商品使用
    function isFeatureForProduct(ids){
        if (ids != ""){
            jQuery.ajax({
                url: '<@ofbizUrl>isFeatureForProduct</@ofbizUrl>',
                type: 'POST',
                data: {
                    'featureTypeValueIds' : ids
                },
                success: function(data) {
                    var isUsedFlg=data.isUsedFlg;
                    if(isUsedFlg=="Y"){
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("该商品规格值已使用，不能删除");
                        $('#modal_msg').modal();
                    }else{
                        //设置删除弹出框内容
                        $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
                        $('#modal_confirm').modal('show');
                    }
                }
            });
        }
    }

    // 将选择的记录id删除
    function delProductFeatureValueByIds(){
        var checkedIds =$("#ids").val();
        var productFeatureTypeId=$("#productFeatureTypeId").val();
        // 选中的项目
        jQuery.ajax({
            url: '<@ofbizUrl>delProductFeatureValueByIds</@ofbizUrl>',
            type: 'POST',
            data: {
                'checkedIds' : checkedIds,
                'productFeatureTypeId':productFeatureTypeId
            },
            success: function(data) {
                if('${parameters.operateType?if_exists}'=='update'){
                    document.location.href="<@ofbizUrl>EditProductFeature?productFeatureTypeId="+productFeatureTypeId+"&"+"operateType=update"+"&"+"productFeatureTypeName=${parameters.productFeatureTypeName?if_exists}"+"&"+"description=${parameters.description?if_exists}</@ofbizUrl>";
                }
            }
        });
    }
    //////////////////////////////////////////////////////////////////////////

    // 取得商品规格信息
    function getProductFeatureTypeInfosByIds(productFeatureTypeIds){
        if (productFeatureTypeIds != ""){
            jQuery.ajax({
                url: '<@ofbizUrl>getProductFeatureTypeInfosByIds</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productFeatureTypeIds':productFeatureTypeIds
                },
                success: function(data) {
                    var mapList=data.mapList
                    $(".js-feature").empty();
                    for(var i=0;i<mapList.length;i++){
                        var productFeatureList=mapList[i].productFeatureList;
                        var trCount=mapList[i].trCount;
                        var listSize=mapList[i].listSize;
                        var productFeatureTypeInfo=mapList[i].productFeatureTypeInfo;
                        getProductFeatureListByIdForPop(productFeatureList,trCount,listSize,productFeatureTypeInfo.productFeatureTypeName,productFeatureTypeInfo.productFeatureTypeId);
                    }
                    $('#addProductFeature').modal('hide');
                }
            });
        }
    }

    // 更新规格信息
    function getProductFeatureListByIdForPop(productFeatureList,trCount,listSize,featureName,productFeatureTypeId){
        var $eventLog = $(".js-feature");
        var tbDiv=' <div id="'+productFeatureTypeId+'">'+
                ' <table class="table table-bordered table_b_c">'+
                ' <thead>'+
                ' <tr>'+
                ' <td><label style="text-align:left;width:100%">'+featureName+'</label> </td><td><button data-id="'+productFeatureTypeId+'" type="button" class="btn btn-primary js-addProductFeatureValue"><i class="fa fa-plus">规格值</i> </button></td>'+
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

        <#--var operateType='${parameters.operateType?if_exists}';-->
        <#--if(operateType=='update'){-->
            <#--if(featureIds){-->
                <#--$("input[type=checkbox]").attr("disabled","disabled");-->
            <#--}-->
        <#--}-->
    }

    // Add by zhajh at 20180502 添加标签 Begin
    //添加显示树
    function addshowMenu() {
        $("#AddTagForm #Addmenu").toggle();
        return false;
    }

    //点击某个节点 然后将该节点的名称赋值值文本框
    function onAddClick(e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("addtree");
        //获得选中的节点
        var nodes = zTree.getSelectedNodes(),
                v = "";
        id = "";
        //根据id排序
        nodes.sort(function compare(a, b) { return a.id - b.id; });
        for (var i = 0, l = nodes.length; i < l; i++) {
            v += nodes[i].name + ",";
            id += nodes[i].id + ",";
        }
        //将选中节点的名称显示在文本框内
        if (v.length > 0) v = v.substring(0, v.length - 1);
        if (id.length > 0) id = id.substring(0, id.length - 1);
        $("#AddTagForm #treeName").attr("value", v);
        $("#AddTagForm #tagTypeId").attr("value", id);
        return false;
    }
    // Add by zhajh at 20180502 添加标签 End



    // 取得商品特征信息
    function getFeatureInfoByProductId(productId){
        if (productId != ""){
            jQuery.ajax({
                url: '<@ofbizUrl>getFeatureInfoByProductId</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productId' : productId
                },
                success: function(data) {
                    var featureInfoList=data.featureInfoList;
                    var productTypeIdTemp=data.productTypeIdTemp;
                    for (var i=0;i<featureInfoList.length;i++) {
                        var featureType=featureInfoList[i];
                        var curTypeId = featureType.productFeatureTypeId;
                        var curTypeName = featureType.productFeatureTypeName;
                        var curFeatureIds=featureType.featureIds;

                        getProductFeatureListById(curTypeId, curTypeName, curFeatureIds);
                    }
                    $(".select2Feature").val(productTypeIdTemp.split(',')).trigger("change");

                }
            });
        }
    }

    // 判断/创建商品图片信息
    function creatProductContentByIdForProduct(productId,contentId,picIndex){
        if (productId != ""&& contentId !="" && picIndex !=""){
            jQuery.ajax({
                url: '<@ofbizUrl>creatProductContentByIdForProduct</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productId' : productId,
                    'contentId' : contentId,
                    'picIndex' : picIndex
                },
                success: function(data) {
                    var productId=data.productId;
                    var contentId=data.contentId;
                    var picIndex=data.picIndex;
                }
            });
        }
    }

    // 取得商品图片信息
    function getProductPicByContentId(imgUrl1,imgUrl2,imgUrl3,imgUrl4,imgUrl5){
        var time = (new Date()).getTime();

//        alert(imgUrl1);
        if(imgUrl1!=""){
            $('#img1').attr('src',imgUrl1+'&v='+time);
            $('#mainImg').attr('value',imgUrl1+'&v='+time);
            $('#editProductContentImg1').text("编辑");
            $('#delProductContentImg1').show();
            $(".js-img1").show();
        }
        if(imgUrl2!=""){
            $('#img2').attr('src',imgUrl2+'&='+time);
            $('#editProductContentImg2').text("编辑");
            $('#delProductContentImg2').show();
            $(".js-img2").show();
        }
        if(imgUrl3!=""){
            $('#img3').attr('src',imgUrl3+'&='+time);
            $('#editProductContentImg3').text("编辑");
            $('#delProductContentImg3').show();
            $(".js-img3").show();
        }
        if(imgUrl4!=""){
            $('#img4').attr('src',imgUrl4+'&='+time);
            $('#editProductContentImg4').text("编辑");
            $('#delProductContentImg4').show();
            $(".js-img4").show();
        }
        if(imgUrl5!=""){
            $('#img5').attr('src',imgUrl5+'&='+time);
            $('#editProductContentImg5').text("编辑");
            $('#delProductContentImg5').show();
            $(".js-img5").show();
        }

    }


    // Add by zhajh at 20180523 主营分类 Begin
    // 主营分类初始化
    function getInitProductCategoryByLevelForPlatformClassByPsId(){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
        jQuery.ajax({
            url: '<@ofbizUrl>getInitProductCategoryByLevelForPlatformClassByPsId</@ofbizUrl>',
            type: 'POST',
            data:{
                'productStoreId' : productStoreId,
                'isInner':isInner
            },
            success: function(data) {
                var productCategoryLevel1List= data.productCategoryLevel1List;
                var productCategoryLevel2List= data.productCategoryLevel2List;
                var productCategoryLevel3List= data.productCategoryLevel3List;
                var productCategoryLevel1Info= data.productCategoryLevel1Info;
                var productCategoryLevel2Info= data.productCategoryLevel2Info;
                var productCategoryLevel3Info= data.productCategoryLevel3Info;

                $("#modPlateFormClass .xl_first_ul").empty();
                for (var i=0;i<productCategoryLevel1List.length;i++){
                    var productCategoryId =(productCategoryLevel1List[i].productCategoryId);
                    var categoryName =(productCategoryLevel1List[i].categoryName);
                    var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>"
                    $("#modPlateFormClass .xl_first_ul").append(liHtml);

                }
                $("#modPlateFormClass .xl_second_ul").empty();
                for (var i=0;i<productCategoryLevel2List.length;i++){
                    var productCategoryId =(productCategoryLevel2List[i].productCategoryId);
                    var categoryName =(productCategoryLevel2List[i].categoryName);

                    var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>";
                    $("#modPlateFormClass .xl_second_ul").append(liHtml);
                }
                $(".xl_third_ul").empty();
                for (var i=0;i<productCategoryLevel3List.length;i++){
                    var productCategoryId =(productCategoryLevel3List[i].productCategoryId);
                    var categoryName =(productCategoryLevel3List[i].categoryName);

                    var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>";
                    $("#modPlateFormClass .xl_third_ul").append(liHtml);
                }

                // 设置选择项目
                //根据a返回它的data-id
                $('#modPlateFormClass .xl_first_ul li[data-id='+productCategoryLevel1Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                if(productCategoryLevel2Info){
                    $('#modPlateFormClass .xl_second_ul li[data-id='+productCategoryLevel2Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                }
                if(productCategoryLevel3Info){
                    $('#modPlateFormClass .xl_third_ul li[data-id='+productCategoryLevel3Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                }

                $("#modPlateFormClass #curProductCategoryIdLevel1").val(productCategoryLevel1Info.productCategoryId);
                if(productCategoryLevel2Info){
                    $("#modPlateFormClass #curProductCategoryIdLevel2").val(productCategoryLevel2Info.productCategoryId);
                }
                if(productCategoryLevel3Info){
                    $("#modPlateFormClass #curProductCategoryIdLevel3").val(productCategoryLevel3Info.productCategoryId);
                }

                $("#modPlateFormClass #curProductCategoryNameLevel1").val(productCategoryLevel1Info.categoryName);
                if(productCategoryLevel2Info){
                    $("#modPlateFormClass #curProductCategoryNameLevel2").val(productCategoryLevel2Info.categoryName);
                }
                if(productCategoryLevel3Info){
                    $("#modPlateFormClass #curProductCategoryNameLevel3").val(productCategoryLevel3Info.categoryName);
                }
                if($("#modPlateFormClass #curProductCategoryNameLevel3").val()!=""){
                    getProductCountById($("#modPlateFormClass #curProductCategoryIdLevel3").val());
                }else{
                    $(".js-productCount").text("当前选择的是："+ $("#modPlateFormClass #curProductCategoryNameLevel1").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel2").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel3").val());
                }
            }
        });
    }



    //主营分类级别的查找功能
    function searchProductCategoryLevelByNameForPlatformClassByPsId(productCategoryLevel,productCategoryId,categoryName){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
        $.ajax({
            url:'<@ofbizUrl>searchProductCategoryLevelByNameForPlatformClassByPsId</@ofbizUrl>',
            dataType:'json',
            type:'post',
            beforeSend:function(){
                if(productCategoryLevel=="1"){
                    $('#modPlateFormClass .search_icon1_1').removeClass('search_icon1');
                }else if(productCategoryLevel=="2"){
                    $('#modPlateFormClass .search_icon2_1').removeClass('search_icon2');

                }else if(productCategoryLevel=="3"){
                    $('#modPlateFormClass .search_icon3_1').removeClass('search_icon3');
                }
            },
            data:{
                'productCategoryLevel' : productCategoryLevel,
                'productCategoryId':productCategoryId,
                'categoryName':categoryName,
                'productStoreId' : productStoreId,
                'isInner':isInner
            },
            success:function(data){
                if(data.success){
                    //根据a返回它的data-id
                    var productCategoryLevel1List= data.productCategoryLevel1List;
                    var productCategoryLevel2List= data.productCategoryLevel2List;
                    var productCategoryLevel3List= data.productCategoryLevel3List;
                    var productCategoryLevel1Info= data.productCategoryLevel1Info;
                    var productCategoryLevel2Info= data.productCategoryLevel2Info;
                    var productCategoryLevel3Info= data.productCategoryLevel3Info;

                    var productCategoryIdLevel1 ="";
                    var productCategoryIdLevel2 ="";
                    var productCategoryIdLevel3 ="";

                    var productCategoryNameLevel1 ="";
                    var productCategoryNameLevel2 ="";
                    var productCategoryNameLevel3 ="";
                    if(productCategoryLevel1Info){
                        productCategoryIdLevel1 =productCategoryLevel1Info.productCategoryId;
                        productCategoryNameLevel1 =productCategoryLevel1Info.categoryName;
                    }
                    if(productCategoryLevel2Info){
                        productCategoryIdLevel2 =productCategoryLevel2Info.productCategoryId;
                        productCategoryNameLevel2 =productCategoryLevel2Info.categoryName;
                    }
                    if(productCategoryLevel3Info){
                        productCategoryIdLevel3 =productCategoryLevel3Info.productCategoryId;
                        productCategoryNameLevel3 =productCategoryLevel3Info.categoryName;
                    }


                    var level=data.productCategoryLevel;

                    if(level=='1'){
                        //把返回的数据放到二级菜单
                        var b="";
                        for(var i=0;i<productCategoryLevel2List.length;i++){
                            b+='<li data-id="'+productCategoryLevel2List[i].productCategoryId+'">'+productCategoryLevel2List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $("#modPlateFormClass .xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != null){
                            $('#modPlateFormClass .xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modPlateFormClass #curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#modPlateFormClass #curProductCategoryNameLevel2").val(productCategoryNameLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $("#modPlateFormClass .xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                            $('#modPlateFormClass .xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modPlateFormClass #curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#modPlateFormClass #curProductCategoryNameLevel3").val(productCategoryNameLevel3);

                        if(productCategoryIdLevel1 != ""){
                            $('#modPlateFormClass .xl_first_ul li[data-id='+productCategoryIdLevel1+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modPlateFormClass #curProductCategoryIdLevel1").val(productCategoryIdLevel1);
                        $("#modPlateFormClass #curProductCategoryNameLevel1").val(productCategoryNameLevel1);

                        $('#modPlateFormClass .search_icon1_1').addClass('search_icon1');
                        if(productCategoryNameLevel3!=""){
                            getProductCountById($("#modPlateFormClass #curProductCategoryIdLevel3").val());
                        }else{
                            $(".js-productCount").text("当前选择的是："+ $("#modPlateFormClass #curProductCategoryNameLevel1").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel2").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel3").val());
                        }
                    }else if(level=='2'){
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $("#modPlateFormClass .xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                            $('#modPlateFormClass .xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modPlateFormClass #curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#modPlateFormClass #curProductCategoryNameLevel3").val(productCategoryNameLevel3);


                        if(productCategoryIdLevel2 != ""){
                            $('#modPlateFormClass .xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modPlateFormClass #curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#modPlateFormClass #curProductCategoryNameLevel2").val(productCategoryNameLevel2);

                        $('#modPlateFormClass .search_icon2_1').addClass('search_icon2');
                        if(productCategoryNameLevel3!=""){
                            getProductCountById($("#modPlateFormClass #curProductCategoryIdLevel3").val());
                        }else{
                            $(".js-productCount").text("当前选择的是："+ $("#modPlateFormClass #curProductCategoryNameLevel1").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel2").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel3").val());
                        }
                    }else if(level='3'){
                        if(productCategoryIdLevel3 != ""){
                            $('#modPlateFormClass .xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#modPlateFormClass #curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#modPlateFormClass #curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryNameLevel3!=""){
                            getProductCountById($("#modPlateFormClass #curProductCategoryIdLevel3").val());
                        }else{
                            $(".js-productCount").text("当前选择的是："+ $("#modPlateFormClass #curProductCategoryNameLevel1").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel2").val()+">"+ $("#modPlateFormClass #curProductCategoryNameLevel3").val());
                        }
                    }

                }else{
                    alert("操作失败！");
                }
            },
            error:function(){
                alert("操作失败！");
                $('.search_icon2_1').addClass('search_icon2');
            }
        })
    }



    //主营分类级别的选中处理功能
    function seclectdeItemProductCategoryLevelByIdForPlatformClassByPsId(productCategoryLevel,productCategoryId){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
        $.ajax({
            url:'<@ofbizUrl>seclectdeItemProductCategoryLevelByIdForPlatformClassByPsId</@ofbizUrl>',
            dataType:'json',
            type:'post',
            data:{
                'productCategoryLevel' : productCategoryLevel,
                'productCategoryId':productCategoryId,
                'productStoreId' : productStoreId,
                'isInner':isInner
            },
            success:function(data){
                //把返回的数据放到二级菜单
                if(data.success){
                    //根据a返回它的data-id
                    var productCategoryLevel1List= data.productCategoryLevel1List;
                    var productCategoryLevel2List= data.productCategoryLevel2List;
                    var productCategoryLevel3List= data.productCategoryLevel3List;
                    var productCategoryLevel1Info= data.productCategoryLevel1Info;
                    var productCategoryLevel2Info= data.productCategoryLevel2Info;
                    var productCategoryLevel3Info= data.productCategoryLevel3Info;


                    var productCategoryIdLevel1 ="";
                    var productCategoryIdLevel2 ="";
                    var productCategoryIdLevel3 ="";

                    var productCategoryNameLevel1 ="";
                    var productCategoryNameLevel2 ="";
                    var productCategoryNameLevel3 ="";
                    if(productCategoryLevel1Info){
                        productCategoryIdLevel1 =productCategoryLevel1Info.productCategoryId;
                        productCategoryNameLevel1 =productCategoryLevel1Info.categoryName;
                    }
                    if(productCategoryLevel2Info){
                        productCategoryIdLevel2 =productCategoryLevel2Info.productCategoryId;
                        productCategoryNameLevel2 =productCategoryLevel2Info.categoryName;
                    }
                    if(productCategoryLevel3Info){
                        productCategoryIdLevel3 =productCategoryLevel3Info.productCategoryId;
                        productCategoryNameLevel3 =productCategoryLevel3Info.categoryName;
                    }

                    var level=data.productCategoryLevel;
                    if(level=='1'){
                        //把返回的数据放到二级菜单
                        var b="";
                        for(var i=0;i<productCategoryLevel2List.length;i++){
                            b+='<li data-id="'+productCategoryLevel2List[i].productCategoryId+'">'+productCategoryLevel2List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != ""){
                            $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#curProductCategoryNameLevel2").val(productCategoryNameLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        //alert(c);
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                            $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryNameLevel3!=""){
                            getProductCountById($("#curProductCategoryIdLevel3").val());
                        }else{
                            $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
                        }
                    }else if(level=='2'){
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                            $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryIdLevel3 !=""){
                            getProductCountById($("#curProductCategoryIdLevel3").val());
                        }else{
                            $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
                        }
                    }else if(level=='3'){

                    }
                }else{
                    alert("操作失败")
                }
            },
            error:function(){
                alert("操作失败")
            }
        })
    }

    // Add by zhajh at 20180523 主营分类 End








    // Add by zhajh at 20180601 Begin
    //取得Url信息
    function getSkuUrlInfo(urlInfos){

        if(urlInfos){
            $.ajax({
                url:'<@ofbizUrl>getSkuUrlInfo</@ofbizUrl>',
                dataType:'json',
                type:'post',
                data:{
                    'urlInfos' : urlInfos
                },
                success:function(data){
                    //把返回的数据放到二级菜单
                    if(data.success){
                        //根据a返回它的data-id
                        var curUrlInfos= data.urlInfos;
                        window.location.href= curUrlInfos;
                    }else{
                        alert("操作失败")
                    }
                },
                error:function(){
                    alert("操作失败")
                }
            })
        }

    }

    // Add by zhajh at 20180523 取得Url信息 End
</script>

		
