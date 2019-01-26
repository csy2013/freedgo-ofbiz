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
<#--<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>-->
<#--<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>-->
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "findProductGoods?lookupFlag=Y"+ paramList +"&">
<div class="box box-info">
    <#--
	<div class="box-header with-border">
	  <h3 class="box-title">${uiLabelMap.ProductBrand}</h3>
	</div>
	-->
	
	<div class="box-body">
	    <input type="hidden" name="curProductCategoryIdLevel1" id="curProductCategoryIdLevel1" value=""/>
	    <input type="hidden" name="curProductCategoryIdLevel2" id="curProductCategoryIdLevel2" value=""/>
	    <input type="hidden" name="curProductCategoryIdLevel3" id="curProductCategoryIdLevel3" value=""/>
	  
	    <input type="hidden" name="delStatus" id="delStatus" value=""/>
        <!--店铺编码-->
        <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
        <!--自营与非自营用户标识 Y:自营 N:非自营-->
        <input type="hidden" id="isInner" name="isInner" value="${requestAttributes.isInner}"/>
	    
    	<form class="form-inline clearfix" role="form" action="<@ofbizUrl>findProductGoods</@ofbizUrl>">
    	    
    	    <div class="form-group">
               <div class="input-group m-b-10">
                   <span class="input-group-addon">商品名称</span>
                   <input type="text" class="form-control"  id="productName" name="productName" value="${requestParameters.productName?if_exists}"/>
               </div>
              
               <div class="input-group m-b-10">
                   <span class="input-group-addon">商品编号</span>
                   <input type="text" class="form-control"  name="productId" value="${requestParameters.productId?if_exists}"/>
               </div>
              
               <div class="input-group m-b-10">
                  <#assign productBrandList = delegator.findByAnd("ProductBrand")/>
                   <span class="input-group-addon">商品品牌</span>
                   <select class="form-control" id="brandId" name="brandId">
                       <option value="">全部</option>
                       <#list productBrandList as productBrand>
	                       <#if requestParameters.brandId?has_content>
		                       <#if requestParameters.brandId==productBrand.productBrandId>
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
               
               <div class="input-group m-b-10">
	                <span class="input-group-addon">一级分类</span>
	                <select class="form-control" id="levelFirst" name="levelFirst">
	                <option value="">全部</option>
		            <#list productCategoryList as productCategory>
					    <#if productCategory.productStoreId=="${requestAttributes.productStoreId}">
                            <#if "${productCategory.isDel?if_exists}" !='Y'>
                                <#if requestParameters.levelFirst?has_content>
                                    <#if requestParameters.levelFirst==productCategory.productCategoryId>
                                        <option value="${productCategory.productCategoryId}" selected="selected">${productCategory.categoryName}</option>
                                    <#else>
                                        <option value="${productCategory.productCategoryId}">${productCategory.categoryName}</option>
                                    </#if>
                                <#else>
                                    <option value="${productCategory.productCategoryId}">${productCategory.categoryName}</option>
                                </#if>
                            </#if>
						</#if>
		            </#list>
		            </select>
	           </div>
	              
	           <div class="input-group m-b-10">
	                <span class="input-group-addon">二级分类</span>
	                <select class="form-control" id="levelSecond" name="levelSecond">
		                <option value="">全部</option>
		                <#if requestParameters.levelSecond?has_content>
		                       <#assign productCategoryInfo = delegator.findByPrimaryKey("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId","${requestParameters.levelSecond?if_exists}"))/>
                               <#if productCategoryInfo?has_content>
                                   <#assign primaryParentCategoryId=productCategoryInfo.primaryParentCategoryId>
                                   <#assign productCategory2List = delegator.findByAnd("ProductCategory", {"primaryParentCategoryId" : primaryParentCategoryId?if_exists})>
                                   <#list productCategory2List as productCategory>
                                        <#if requestParameters.levelSecond==productCategory.productCategoryId>
						                    <option value="${productCategory.productCategoryId}" selected="selected">${productCategory.categoryName}</option>
						                <#else>
						            	    <option value="${productCategory.productCategoryId}">${productCategory.categoryName}</option>
						            	</#if>
                                   </#list>
                               </#if>
                        <#else>
                            <#if requestParameters.levelFirst?has_content>
                                <#assign productCategory2List = delegator.findByAnd("ProductCategory", {"primaryParentCategoryId" : requestParameters.levelFirst?if_exists})>
                                <#list productCategory2List as productCategory>
					            	<option value="${productCategory.productCategoryId}">${productCategory.categoryName}</option>
                                </#list>
                            </#if>        
		                </#if>
		            </select>
	           </div>
	              
	           <div class="input-group m-b-10">
	               <span class="input-group-addon">三级分类</span>
	               <select class="form-control" id="levelThird" name="levelThird">
		               <option value="">全部</option>
                       <#if requestParameters.levelThird?has_content>
		                       <#assign productCategoryInfo = delegator.findByPrimaryKey("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId","${requestParameters.levelThird?if_exists}"))/>
                               <#if productCategoryInfo?has_content>
                                   <#assign primaryParentCategoryId=productCategoryInfo.primaryParentCategoryId>
                                   <#assign productCategory3List = delegator.findByAnd("ProductCategory", {"primaryParentCategoryId" : primaryParentCategoryId?if_exists})>
                                   <#list productCategory3List as productCategory>
                                        <#if requestParameters.levelThird==productCategory.productCategoryId>
						                    <option value="${productCategory.productCategoryId}" selected="selected">${productCategory.categoryName}</option>
						                <#else>
						            	    <option value="${productCategory.productCategoryId}">${productCategory.categoryName}</option>
						            	</#if>
                                   </#list>
                               </#if>
                       <#else>
                            <#if requestParameters.levelSecond?has_content>
                                <#assign productCategory3List = delegator.findByAnd("ProductCategory", {"primaryParentCategoryId" : requestParameters.levelSecond?if_exists})>
                                <#list productCategory3List as productCategory>
					            	<option value="${productCategory.productCategoryId}">${productCategory.categoryName}</option>
                                </#list>
                            </#if>        
		               </#if>
                       
		            </select>
	            </div>
	              
	            <div class="input-group m-b-10">
	                <span class="input-group-addon">是否上架</span>
	                <select class="form-control" id="isOnline" name="isOnline">
	                    <option value="">全部</option>
		                
		                <#if requestParameters.isOnline?has_content>
	                       <#if requestParameters.isOnline=="Y" >
	                           <option value="Y" selected="selected">是</option>
		                       <option value="N">否</option>
	                       <#elseif requestParameters.isOnline=="N" >
	                           <option value="Y">是</option>
		                       <option value="N" selected="selected">否</option>
	                       <#else>
	                           <option value="Y">是</option>
		                       <option value="N">否</option>
	                       </#if>
	                    <#else> 
		                   <option value="Y">是</option>
		                   <option value="N">否</option>
		                </#if>
		            </select>
	            </div>
	              
	            <div class="input-group m-b-10">
	                <span class="input-group-addon">商品类型</span>
	                <select class="form-control" id="productTypeId" name="productTypeId">
	                    <option value="">全部</option>
	                    <#if requestParameters.productTypeId?has_content>
	                       <#if requestParameters.productTypeId=="FINISHED_GOOD" >
	                          <option value="FINISHED_GOOD" selected="selected">实物商品</option>
		                      <option value="VIRTUAL_GOOD">虚拟商品</option>
	                       <#elseif requestParameters.productTypeId=="VIRTUAL_GOOD" >
	                          <option value="FINISHED_GOOD">实物商品</option>
		                      <option value="VIRTUAL_GOOD" selected="selected">虚拟商品</option>
	                       <#else>
	                          <option value="FINISHED_GOOD">实物商品</option>
		                      <option value="VIRTUAL_GOOD">虚拟商品</option>
	                       </#if>
	                    <#else> 
		                    <option value="FINISHED_GOOD">实物商品</option>
		                    <option value="VIRTUAL_GOOD">虚拟商品</option>
		                </#if>
		            </select>
	            </div>
                <div class="input-group pull-right">
                    <button class="btn btn-success btn-flat" id="btnSearch">${uiLabelMap.BrandSearch}</button>
                </div>
            </div>
            

        </form>
        <div class="cut-off-rule bg-gray"></div>
        </div>
        
        <#if productList?has_content>
		   <input id="ids" type="hidden"/>
           <div class="row m-b-10">
               <div class="col-sm-6  p-l-20">
                   <div class="dp-tables_btn">
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_LIST", "_DEL",session)>
                        <button class="btn btn-primary" id="btnProductDel">
                            <i class="fa fa-trash"></i>
                            ${uiLabelMap.BrandDel}
                        </button>
                        </#if>
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_BATCH", "_ONLINE",session)>
                        <button class="btn btn-primary" id="batchProductOnline">批量上架</button>
                        </#if>
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_BATCH", "_OFFLINE",session)>
                        <button class="btn btn-primary" id="batchProductOffline">批量下架</button>
                        </#if>
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_LIST", "_EXPORT",session)>
                        <div class="btn-group">
                            <button type="button" class="btn btn-primary" id="exportAll">${uiLabelMap.ExportAll}</button>
                            <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
                                <span class="caret"></span>
                                <span class="sr-only">Toggle Dropdown</span>
                            </button>
                            <ul class="dropdown-menu" role="menu">
                                <li><a href="#" id="curPageExport" >${uiLabelMap.ExprotCurPage}</a></li>
                                <li><a href="#" id="selectedItemExport">${uiLabelMap.ExportSelectedItem}</a></li>
                            </ul>
                        </div>
                        </#if>
                   </div>
               </div>

                <div class="col-sm-6">
                    <div class="dp-tables_length">
                        <label>
                            ${uiLabelMap.DisplayPage}
                            <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                    onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="5" <#if viewSize ==5>selected</#if>>5</option>
                                <option value="15" <#if viewSize==15>selected</#if>>15</option>
                                <option value="20" <#if viewSize==20>selected</#if>>20</option>
                                <option value="25" <#if viewSize==25>selected</#if>>25</option>
                            </select>
                            ${uiLabelMap.BrandItem}
                        </label>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <table class="table table-bordered table-hover js-checkparent js-sort-list">
                        <thead>
                        <tr>
                            <th><input class="js-allcheck" type="checkbox" id="checkAll"></th>
                            <th>商品名称
                            	<a class="fa fa-sort-amount-desc text-muted js-sort" data-key="productName" id="a_productName"></a>
                            	<#--
                            	<#if orderFiled == 'productName'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=productName&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=productName&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=productName&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>商品编号
                            	<a class="fa fa-sort-amount-desc text-muted js-sort" data-key="productId" id="a_productId"></a>
                            		<#--
                            	<#if orderFiled == 'productId'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=productId&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=productId&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=productId&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>是否上架
                            	<a class="fa fa-sort-amount-desc text-muted js-sort" data-key="isOnline" id="a_isOnline"></a>
                            		<#--
                            	<#if orderFiled == 'isOnline'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=isOnline&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=isOnline&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=isOnline&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>三级分类
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="primaryProductCategoryId" id="a_primaryProductCategoryId"></a>
                                	<#--
                                <#if orderFiled == 'primaryProductCategoryId'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=primaryProductCategoryId&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=primaryProductCategoryId&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=primaryProductCategoryId&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            
                            <th>品牌
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="brandId" id="a_brandId"></a>
                               	<#--
                                <#if orderFiled == 'brandId'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=brandId&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=brandId&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=brandId&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            
                             <#--<th>所属商家-->
                                <#--<a class="fa fa-sort-amount-desc text-muted js-sort" data-key="businessPartyId" id="a_businessPartyId"></a>-->
                               	<#--&lt;#&ndash;-->
                                <#--<#if orderFiled == 'businessPartyId'>-->
                                    <#--<#if orderBy == 'DESC'>-->
                                        <#--<a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=businessPartyId&amp;ORDER_BY=ASC"></a>-->
                                    <#--<#else>-->
                                        <#--<a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=businessPartyId&amp;ORDER_BY=DESC"></a>-->
                                    <#--</#if>-->
                                <#--<#else>-->
                                    <#--<a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=businessPartyId&amp;ORDER_BY=ASC"></a>-->
                                <#--</#if>-->
                                <#--&ndash;&gt;-->
                            <#--</th>-->
                            
                             <th>审核状态
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="isVerify" id="a_isVerify"></a>
                               	<#--
                                <#if orderFiled == 'isVerify'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=isVerify&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=isVerify&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=isVerify&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>${uiLabelMap.BrandOption}</th>
                        </tr>
                        </thead>
                        
                        <tbody>
                            <form method="post" action="" name="editProductBrand" id="editProductBrand">
	                            <#list productList as productRow>
	                               
		                            <tr>
				                        <td><input class="js-checkchild" type="checkbox" id="${productRow.productId?if_exists}" value="${productRow.productId?if_exists}"/>
				                        </td>
				                        <td>
                                           <div class="form-group">
                                              <#assign curProductAdditionalImage1 = delegator.findByAnd("ProductContent", {"productId" : productRow.productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_1"})/>
                                              <#if curProductAdditionalImage1?has_content>
											     <#assign productAdditionalImage1 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(productRow, "ADDITIONAL_IMAGE_1", locale, dispatcher))?if_exists />
											  <#else>
											     <#assign productAdditionalImage1 =""/>
											  </#if>
											  <span class="col-sm-6" style="display:inline-block;">
		                                       <img height="100" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>" class="cssImgSmall" alt="" />
		                                      </span>
		                                      <span class="col-sm-6" style="display:inline-block;">
                                                ${productRow.productName?if_exists}
                                              </span>
                                          </div> 
				                        </td>
				                        <td>${productRow.productId?if_exists}
                                           <input type="hidden" name="productId"  value="${productRow.productId?if_exists}"/>
                                           <input type="hidden" name="isUsedFeature"   value="${productRow.isUsedFeature?if_exists}"/>
                                           <input type="hidden" name="productTypeId"   value="${productRow.productTypeId?if_exists}"/>
                                           <input type="hidden" name="productCategoryId"  value="${productRow.primaryProductCategoryId?if_exists}"/>
                                        </td>
				                        <td><#if productRow.isVerify?if_exists=='Y' >已上架<#else>未上架</#if></td>
				                        <td>
				                        <#assign productCategoryList = delegator.findByAnd("ProductCategory", {"productCategoryId" : productRow.primaryProductCategoryId?if_exists})>
				                        <#if productCategoryList?has_content>
				                            ${productCategoryList[0].categoryName?if_exists}
				                        </#if>
				                        </td>
				                        <td>

				                        <#if productRow.brandId?has_content> 
				                      
					                         <#assign brandInfo = delegator.findByPrimaryKey("ProductBrand",Static["org.ofbiz.base.util.UtilMisc"].toMap("productBrandId","${productRow.brandId?if_exists}"))/>
					                         <#--<#assign brandInfo = delegator.findByAnd("ProductBrand",Static["org.ofbiz.base.util.UtilMisc"].toMap("productBrandId","${productRow.brandId?if_exists}"))/>-->
                                             <#if brandInfo?has_content>
					                             ${brandInfo.brandName?if_exists}
					                         </#if> 
				                         </#if>
				                          
				                        </td>
				                        <#--<td>${productRow.businessPartyId?if_exists}</td>-->
				                        <td>
				                        <#--
				                        <#if productRow.isOnline?if_exists=='Y' &&  productRow.isVerify?if_exists==''>上架审核中
				                        <#elseif productRow.isOnline?if_exists=='Y' &&  productRow.isVerify?if_exists=='Y'>审核通过
				                        <#elseif productRow.isOnline?if_exists=='N' &&  productRow.isVerify?if_exists==''>未提交审核
				                        <#elseif productRow.isOnline?if_exists=='Y' && productRow.isVerify?if_exists=='N'>审核不通过
				                        </#if>
				                        -->
				                        
				                        <#if productRow.isOnline?if_exists=='Y' &&  (productRow.isVerify?if_exists==''||productRow.isVerify?if_exists==' '|| productRow.isVerify?if_exists=='N')>上架审核中
				                        <#elseif productRow.isVerify?if_exists=='Y'>审核通过
				                        <#elseif productRow.isOnline?if_exists=='N' && (productRow.isVerify?if_exists==' ' || productRow.isVerify?if_exists=='')>未提交审核
				                        <#elseif (productRow.isOnline?if_exists=='Y'|| productRow.isOnline?if_exists=='N') && productRow.isVerify?if_exists=='N'>审核不通过
				                        </#if>
				                        </td>
				                        <td>
				                        	<div class="btn-group">
                                                <button type="button" class="js-button btn btn-danger btn-sm" >查看</button>
					                            
                                                <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
					                              <span class="caret"></span>
					                              <span class="sr-only">Toggle Dropdown</span>
					                            </button>
					                            <#if !(productRow.isVerify?if_exists=="Y" || productRow.isOnline?if_exists=="Y")>
					                            <ul class="dropdown-menu" role="menu">
	                                                   <#if security.hasEntityPermission("PRODUCTGOODMGR_LIST", "_UPDATE",session)>
						                               <li><a href="<@ofbizUrl>productDetailUpdate.htm${externalKeyParam}&productId=${productRow.productId?if_exists}</@ofbizUrl>">编辑</a></li>
						                               </#if>
	                                                   <#if security.hasEntityPermission("PRODUCTGOODMGR_LIST", "_DEL",session)>
						                               <li><a href="javascript:del(${productRow.productId?if_exists})">${uiLabelMap.BrandDel}</a></li>
						                               </#if>
					                            </ul>
					                            </#if>
				                        	</div>
				                        </td>
			                        </tr>
	                            </#list>
	                            <input type="hidden" name="productId" name="productId" value=""/>
		                        <input type="hidden" name="operateType" name="operateType"/>
                            </form>
                        </tbody>
                    </table>
                </div>
            </div>
            <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
            <#assign viewIndexFirst = 0/>
            <#assign viewIndexPrevious = viewIndex - 1/>
            <#assign viewIndexNext = viewIndex + 1/>
            <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(productListSize, viewSize) />
            <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", productListSize)/>
            <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
            <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
            listSize=productListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
            pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
            paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
        <#else>
            <div id="">
                <h3>没有商品数据</h3>
            </div>
        </#if>
        </div><!-- /.box-body -->
    </div>



    
   <div class="modal fade" id="addBrand" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
   		<div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.CreateBrand}</h4>
            </div>
            <div class="modal-body">
                 <form class="form-horizontal"  id="updateProductBrand" method="post" action="" name="updateProductBrand" enctype="multipart/form-data">
                 
				    <div class="form-group" data-type="required" data-mark="品牌名称">
			            <label class="control-label col-sm-2"><span style="color: red">*</span>${uiLabelMap.BrandName}:</label>
			            <div class="col-sm-10">
			               <input type="text" class="form-control dp-vd" name="brandName" id="brandName" value="${parameters.brandName?if_exists}">
			               <p class="dp-error-msg"></p>
			            </div>                
			        </div>
				    
                    <div class="form-group">
                		<label for="message-text" class="control-label col-sm-2">${uiLabelMap.BrandNameAlias}:</label>
		                <div class="col-sm-10">
		                  <input type="text" class="form-control" name="brandNameAlias" id="brandNameAlias">
		                </div>
              		</div>
              		<#--
              		<div class="form-group">
                		<label class="control-label col-sm-2"><span style="color: red">*</span>${uiLabelMap.LogoPic}</label>
		                <div class="col-sm-10">
		                  <button type="button" class="btn btn-primary" id="btnAddPic">${uiLabelMap.AddPic}</button>
		                </div>
              		</div>
              		-->
              		
              		<div class="form-group">
			            <label class="control-label col-sm-2"><span style="color: red">*</span>${uiLabelMap.LogoPic}</label>
			            <div class="col-sm-10">
			            </div>                
			        </div>
			        <div class="form-group">
			            <label class="control-label col-sm-2">&nbsp;</label>
			            <div class="col-sm-10">
			                <input type="file" class="form-control"  name="uploadedFile" name="uploadedFile">
			            </div>                
			        </div>
			        
              		<div class="form-group">
			          	<input type="hidden" id="productCategoryIds" name="productCategoryIds"/>
			            <label class="control-label col-sm-2">商品分类:</label>
			            <div class="col-sm-10">
			              <div class="zTreeDemoBackground left">
			                <ul id="addProductCategoryArea" class="ztree"></ul>
			              </div>
			            </div>                
			        </div>
              		
              		<input id="productId" type="hidden" name="productId" value="GoodsBrand" />
                    <input id="productContentTypeId" type="hidden" name="productContentTypeId" value="GOODSBRAND_IMG"/>
              		<input type="hidden" name="operateType" id="operateType" value=""/>
					<input type="hidden" name="productBrandId" value=""/>
                    <input type="hidden" name="contentId" id="contentId" value=""/>
	            </form>
	          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            <button type="button" class="btn btn-primary" id="btnBrandSave">${uiLabelMap.BrandSave}</button>
          </div>
        </div>
      </div>
    </div>
    
    
    
    
    
    <div class="modal fade" id="impBrandByExcel" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
   		<div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">添加商品品牌信息</h4>
            </div>
            <div class="modal-body">
                  <form id="importBrandForm" class="form-horizontal" method="post" action="<@ofbizUrl>importProdcutBrandByExcel</@ofbizUrl>" name="importBrandForm" enctype="multipart/form-data">
                    <div class="form-group">
	                    <label for="recipient-name" class="control-label col-sm-1"></label>
		                <div class="col-sm-10">
		                  <input type="file" class="form-control" id="uploadedFile" name="uploadedFile">
		                  <p class="help-block">(支持xls和xlsx格式的Excel文件)</p>
		                </div>  
		                <input type="hidden" name="filePath" id="filePath" value=""/>
		               <div class="modal-footer">
			            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
			            <button type="submit" class="btn btn-primary" id="btnBrandImp">上传</button>
			          </div>           
	                </div>
	            </form>
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
	
	
	<!-- 删除确认弹出框start -->
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
	</div><!-- 删除确认弹出框end -->		



<script language="JavaScript" type="text/javascript">
   
    $(function() {
    	//初始化排序字段
    	var sort_field = "${requestParameters.sortField?if_exists}",
    		sort_type = "${requestParameters.sortType?if_exists}";
    	if(sort_field){
    		if(sort_type=='asc'){
	    		$('#a_'+sort_field).removeClass('fa-sort-amount-desc').addClass('fa-sort-amount-asc').removeClass('text-muted');
	    	}else{
	    		$('#a_'+sort_field).removeClass('text-muted');
	    	}
    	}	
    	
        // 没有数据的场合按钮不能用
        <#---
	        <#if !productList?has_content>
	            $("button").attr("disabled","disabled");
	            $("#btnSearch").removeAttr("disabled");
	        </#if>
        -->



        $(document).on('change','#jumpPage',function(){
		    <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(productListSize, viewSize) />
			var curLastPageIndex=parseInt('${viewIndexLast}')+parseInt(1);
        	var chkVal =$(this).val();
            if(chkVal==0){
            	$(this).val(1);
			}
			if(eval(chkVal) > eval(curLastPageIndex)){
				$(this).val(curLastPageIndex);
			}
        });

          
        // 选中一级分类的处理
        $("#levelFirst").change(function(){
           var productCategoryId=$(this).val();
           //alert(productCategoryId);
           seclectdeItemProductCategoryLevelById("1",productCategoryId);
           $("#curProductCategoryIdLevel1").val(productCategoryId);
        });
        // 选择二级分类的处理
        $("#levelSecond").change(function(){
           var productCategoryId=$(this).val();
           seclectdeItemProductCategoryLevelById("2",productCategoryId);
           $("#curProductCategoryIdLevel2").val(productCategoryId);
        });
        // 选择三级分类的处理
        $("#levelThird").change(function(){
           var productCategoryId=$(this).val();
           $("#curProductCategoryIdLevel3").val(productCategoryId);
        });
        // 商品的删除处理
		$("#btnProductDel").click(function(){
		    var checks = $('.js-checkparent .js-checkchild:checked');
		    var ids = "";
		    if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
				}); 
				
				var checkedIds =$("#ids").val(); 
		        if (checkedIds != ""){
		            chkDelProductByIds();
		        }
	        }else{
	           //设置提示弹出框内容
    	  	   $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    	  	   $('#modal_msg').modal();
	        }
		})
		
		//删除弹出框删除按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
	        delProductDataById();
	    });
		
		// 批量上架
		$("#batchProductOnline").click(function(){
		    var ids = "";
		    var checks = $('.js-checkparent .js-checkchild:checked');
		    
		    if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
				}); 
				var checkedIds =$("#ids").val();
                var isInner =$("#isInner").val();
				if (checkedIds != ""){
		           	 // 选中的项目
		           	 jQuery.ajax({
		                 url: '<@ofbizUrl>updateProductIsOnlineStatus</@ofbizUrl>',
		                type: 'POST',
		                data: {
		                   'checkedIds' : checkedIds,
		                   'onlineStatus':'Y',
                           'isInner':isInner
		                },
		                success: function(data) {
		                    document.location.href="<@ofbizUrl>findProductGoods?lookupFlag=Y</@ofbizUrl>"; 
		                }
		             });
		        
		        }
	        }else{
	            //设置提示弹出框内容
    	  	   $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    	  	   $('#modal_msg').modal();
	        }
		})
		// 批量下架
		$("#batchProductOffline").click(function(){
		   var checks = $('.js-checkparent .js-checkchild:checked');
		   if(checks.size() > 0 ){
			   $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
			   }); 
			   var checkedIds =$("#ids").val();
               var isInner =$("#isInner").val();
			   if (checkedIds != ""){
			       
			        // 选中的项目
		           	jQuery.ajax({
		                 url: '<@ofbizUrl>updateProductIsOnlineStatus</@ofbizUrl>',
		                type: 'POST',
		                data: {
		                   'checkedIds' : checkedIds,
		                   'onlineStatus': 'N',
                           'isInner':isInner
		                },
		                success: function(data) {
		                    document.location.href="<@ofbizUrl>findProductGoods?lookupFlag=Y</@ofbizUrl>"; 
		                }
		            });
			   }
		   }else{
		      //设置提示弹出框内容
    	  	  $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    	  	  $('#modal_msg').modal();
		   }
		})
		
		// 商品查看处理
		$('.js-button').on('click',function(){
			var tr=$(this).closest('tr');
			var productId=tr.find('input[name=productId]').val();//商品Id
			var isUsedFeature=tr.find('input[name=isUsedFeature]').val();//是否使用特征
            var productCategoryId=tr.find('input[name=productCategoryId]').val();//商品分类
            var productTypeId=tr.find('input[name=productTypeId]').val();//商品类型
            document.location.href="<@ofbizUrl>productDetail.htm?productId="+productId+"</@ofbizUrl>";
            
		});
		
		
		
			<#--$("#exportAll").click(function(){-->
			     <#--var checks = $('.js-checkparent .js-checkchild:checked');-->
		         <#--if(checks.size() > 0 ){-->
				     <#--var ids ="";-->
					 <#--document.location.href="<@ofbizUrl>productGoodListReport.xls?ids="+ids+"</@ofbizUrl>";-->
				 <#--}else{-->
				     <#--//设置提示弹出框内容-->
		    	  	 <#--$('#modal_msg #modal_msg_body').html("当前没有商品数据");-->
		    	  	 <#--$('#modal_msg').modal();-->
				 <#--}-->
			<#--})-->


			$("#exportAll").click(function(){
				var ids ="";
				<#--document.location.href="<@ofbizUrl>productGoodListReport.xls?ids="+ids+"</@ofbizUrl>";-->
                var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
                <#--document.location.href="<@ofbizUrl>exportProductGoodsByIds?ids="+ids+"&productStoreId="+productStoreId+"</@ofbizUrl>";-->

                // loading();
                var url = "exportProductGoodsByIds?ids="+ids+"&productStoreId="+productStoreId;
                window.location.href = url;
                <#--$.post("updateSessionForParam${externalKeyParam}",{attrName:"exportOrderListStatus",attrVal:"false"},function(data){-->
                    <#--window.location.href = url;-->
                    <#--var timer;-->
                    <#--timer = window.setInterval(function() {-->
                        <#--$.post("getSessionForParam${externalKeyParam}",{attrName:"exportOrderListStatus"},function(data){-->
                            <#--if(data.attrVal == "success"){-->
                                <#--window.clearTimeout(timer);-->
                                <#--// loading("hide");-->
                                <#--$.tipLayer("导出成功！");-->
                            <#--}-->
                        <#--});-->
                    <#--}, 1000);-->
                <#--});-->

			})
			
			$("#curPageExport").click(function(){
			    var checks = $('.js-checkparent .js-checkchild')
                var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
		        if(checks.size() > 0 ){
				    var ids ="";
				    $('input[class="js-checkchild"]').each(function(){ 
			            ids = ids + "," + $(this).val();
			            $("#ids").val(ids.substr(1,ids.length));
					}); 
					var idsView =$("#ids").val();
					if(idsView!=""){
						<#--document.location.href="<@ofbizUrl>productGoodListReport.xls?ids="+idsView+"</@ofbizUrl>";-->
                        var url = "exportProductGoodsByIds?ids="+ids+"&productStoreId="+productStoreId;
                        window.location.href = url;
					}
				}else{
				     //设置提示弹出框内容
		    	  	 $('#modal_msg #modal_msg_body').html("当前页没有商品数据");
		    	  	 $('#modal_msg').modal();
				}
			})
		
		
			$("#selectedItemExport").click(function(){
                var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
			    var checks = $('.js-checkparent .js-checkchild:checked');
		        if(checks.size() > 0 ){
				    var ids ="";
				    $('input[class="js-checkchild"]:checked').each(function(){ 
			            ids = ids + "," + $(this).val();
			            $("#ids").val(ids.substr(1,ids.length));
					}); 
					var idsView =$("#ids").val();
					if(idsView!=""){
					  <#--document.location.href="<@ofbizUrl>productGoodListReport.xls?ids="+idsView+"</@ofbizUrl>";-->
                        var url = "exportProductGoodsByIds?ids="+ids+"&productStoreId="+productStoreId;
                        window.location.href = url;
				    }
			    }else{
			        //设置提示弹出框内容
		    	  	$('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
		    	  	$('#modal_msg').modal();
			    }
			})
		 
    });
    
    // 将选择的记录id删除
    function delProductDataById(){
	  // 选中的项目
	  var checkedIds =$("#ids").val(); 
	  if (checkedIds != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>delProductByIds</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'checkedIds' : checkedIds
	        },
	        success: function(data) {
	           document.location.href="<@ofbizUrl>findProductGoods?lookupFlag=Y</@ofbizUrl>"; 
	        }
	    });
	  } 
	}
	// 检查删除ID的在线状态
	function chkDelProductByIds(){
	  // 选中的项目
	  var checkedIds =$("#ids").val(); 
	  if (checkedIds != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>chkDelProductByIds</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'checkedIds' : checkedIds
	        },
	        success: function(data) {
	           var listSize=data.listSize;
	           if(listSize==0){
	                //设置删除弹出框内容
					$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
					$('#modal_confirm').modal('show');
	           }else{
	                //设置提示弹出框内容
    	  	  		$('#modal_msg #modal_msg_body').html("状态为“已上架”或“上架申请中”的商品不能删除");
    	  	 		$('#modal_msg').modal();
	           }
	           
	           
	        }
	    });
	  } 
	}
	
    //行删除按钮事件
    function del(id){
	    $("#ids").val(id);
	   	//设置删除弹出框内容
		//$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
		//$('#modal_confirm').modal('show');
		chkDelProductByIds();
		
    } 
   
   
   
   	//分类级别的选中处理功能
	function seclectdeItemProductCategoryLevelById(productCategoryLevel,productCategoryId){
	    $.ajax({
            url:'<@ofbizUrl>seclectdeItemProductCategoryLevelById</@ofbizUrl>',
            dataType:'json',
            type:'post',
            data:{
             'productCategoryLevel' : productCategoryLevel,
             'productCategoryId':productCategoryId
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
		            
		            var productCategoryIdLevel1 =data.productCategoryIdLevel1;
		            var productCategoryIdLevel2 =data.productCategoryIdLevel2;
		            var productCategoryIdLevel3 =data.productCategoryIdLevel3;
		            
		            var level=data.productCategoryLevel;
		            if(level=='1'){
                        //把返回的数据放到二级菜单
                        var b="<option value=''>全部</option>";
                        for(var i=0;i<productCategoryLevel2List.length;i++){
                            b+='<option value="'+productCategoryLevel2List[i].productCategoryId+'">'+productCategoryLevel2List[i].categoryName+'</option>'
                        }
                        $('#levelSecond').html(b);
                        if(productCategoryIdLevel2 != null){
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        //把返回的数据放到三级菜单
                        var c="<option value=''>全部</option>";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<option value="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' </option>'
                        }
                        $("#levelThird").html(c);
                        if(productCategoryIdLevel3 != null){
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                    }else if(level=='2'){
                       //把返回的数据放到三级菜单
                        var c="<option value=''>全部</option>";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<option value="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' </option>'
                        }
                        $("#levelThird").html(c);
                        if(productCategoryIdLevel3 != null){
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                       
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

   
   
</script>

