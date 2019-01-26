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

<#assign commonUrl = "findProductGoodForVerify?lookupFlag=Y"+ paramList +"&">
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
    	<form class="form-inline clearfix" role="form" action="<@ofbizUrl>findProductGoodForVerify</@ofbizUrl>">
    	    
    	    <div class="form-group">
               <div class="input-group m-b-10">
                   <span class="input-group-addon">商品名称</span>
                   <input type="text" class="form-control" value="${requestParameters.productName?if_exists}" id="productName" name="productName" />
               </div>
              
               <div class="input-group m-b-10">
                   <span class="input-group-addon">商品编号</span>
                   <input type="text" class="form-control" value="${requestParameters.productId?if_exists}" id="productId" name="productId"/>
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
					    <!-- Mod by zhajh at 20180723 只看平台分类-->
						<#if "10000"=="${requestAttributes.productStoreId}">

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
	              
	           <#--<div class="input-group m-b-10">-->
                   <#--<span class="input-group-addon">商家名称</span>-->
                   <#--<input type="text" class="form-control" value="${requestParameters.businessPartyId?if_exists}" id="businessPartyId" name="businessPartyId"/>-->
               <#--</div>-->

                <div class="input-group m-b-10">
                    <span class="input-group-addon">商家店铺</span>
				   <#assign productStoreList = delegator.findByAnd("ProductStore")/>
                    <select class="form-control" id="productStoreIdForVerify" name="productStoreIdForVerify">
                        <option value="">全部</option>
						<#list productStoreList as productStoreInfo>
							<#if requestParameters.productStoreIdForVerify?has_content>
								<#if requestParameters.productStoreIdForVerify==productStoreInfo.productStoreId>
									<option value="${productStoreInfo.productStoreId}" selected="selected">${productStoreInfo.storeName}</option>
								<#else>
									<option value="${productStoreInfo.productStoreId}">${productStoreInfo.storeName}</option>
								</#if>
							<#else>
								<option value="${productStoreInfo.productStoreId}">${productStoreInfo.storeName}</option>
							</#if>
						</#list>

                    </select>
                </div>

            </div>
            
            <div class="input-group pull-right">
                <button class="btn btn-success btn-flat">${uiLabelMap.BrandSearch}</button>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
            <div class="row m-b-10">
                <div class="col-sm-6">
                    <div class="dp-tables_btn">
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_VERIFY", "_PASS",session)>
                        <button class="btn btn-primary" id="batchProductVerifyPass">审核通过</button>
                        </#if>
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_VERIFY", "_NOPASS",session)>
                        <button class="btn btn-primary" id="batchProductVerifyNoPass">审核不通过</button>
                        </#if>
               	    </div>
                </div>
            </div>
        </div>
        
        <#if productForVerifyList?has_content>
		   <input id="ids" type="hidden"/>
           <div class="row m-b-10">
                <div class="col-sm-12">
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
                            
                             <th>状态
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
							<th>所属店铺<a class="fa fa-sort-amount-desc text-muted js-sort" data-key="storeName" id="a_storeName"></a></th>
                            <th>${uiLabelMap.BrandOption}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <form method="post" action="" name="editProductBrand" id="editProductBrand">
	                            <#list productForVerifyList as productForVerifyRow>
		                            <tr>
				                        <td><input class="js-checkchild" type="checkbox" id="${productForVerifyRow.productId?if_exists}" value="${productForVerifyRow.productId?if_exists}"/>
				                            <input type="hidden" name="productId" id="productId" value="${productForVerifyRow.productId?if_exists}"/>
                                            <input type="hidden" name="isUsedFeature" id="isUsedFeature" value="${productForVerifyRow.isUsedFeature?if_exists}"/>
                                            <input type="hidden" name="productTypeId" id="productTypeId" value="${productForVerifyRow.productTypeId?if_exists}"/>
                                            <input type="hidden" name="productCategoryId" id="productCategoryId" value="${productForVerifyRow.primaryProductCategoryId?if_exists}"/>
				                        </td>
				                        <td>
                                           <#assign curProductAdditionalImage1 = delegator.findByAnd("ProductContent", {"productId" : productForVerifyRow.productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_1"})/>
                                           <#if curProductAdditionalImage1?has_content>
										     <#assign productAdditionalImage1 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(productForVerifyRow, "ADDITIONAL_IMAGE_1", locale, dispatcher))?if_exists />
										   <#else>
										     <#assign productAdditionalImage1 =""/>
										   </#if>
										   <span class="col-sm-6" style="display:inline-block;">
	                                        <img height="100" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>" class="cssImgSmall" alt="" />
	                                       </span>
	                                       <span class="col-sm-6" style="display:inline-block;">
                                            ${productForVerifyRow.productName?if_exists}
                                           </span>
                                           
				                        </td>
				                        
				                        <td>${productForVerifyRow.productId?if_exists}
                                        </td>
                                        <td>
				                        <#--<#assign productCategoryList = delegator.findByAnd("ProductCategory", {"productCategoryId" : productForVerifyRow.primaryProductCategoryId?if_exists})>-->
										<#assign productCategoryList = delegator.findByAnd("ProductCategory", {"productCategoryId" : productForVerifyRow.platformClassId?if_exists})>
										<#if productCategoryList?has_content>
				                            ${productCategoryList[0].categoryName?if_exists}
				                        </#if>
				                        </td>
				                        <td>
				                        <#if productForVerifyRow.brandId?has_content>
					                         <#assign brandInfo = delegator.findByAnd("ProductBrand",Static["org.ofbiz.base.util.UtilMisc"].toMap("productBrandId","${productForVerifyRow.brandId?if_exists}"))/>
					                         <#if brandInfo?has_content>
					                             ${brandInfo[0].brandName?if_exists}
					                         </#if> 
				                         </#if>
				                        </td>
				                        <#--<td>${productForVerifyRow.businessPartyId?if_exists}</td>-->
				                        <td>
				                        <#--
				                        <#if productForVerifyRow.isOnline?if_exists=='Y' &&  productForVerifyRow.isVerify?if_exists==''>上架审核中
				                        <#elseif productForVerifyRow.isOnline?if_exists=='Y' &&  productForVerifyRow.isVerify?if_exists=='Y'>审核通过
				                        <#elseif productForVerifyRow.isOnline?if_exists=='N'>未提交审核
				                        <#elseif productForVerifyRow.isVerify?if_exists=='N'>审核不通过
				                        </#if>
				                        -->
				                        
				                        <#if productForVerifyRow.isOnline?if_exists=='Y' &&  (productForVerifyRow.isVerify?if_exists==''|| productForVerifyRow.isVerify?if_exists==' '|| productForVerifyRow.isVerify?if_exists=='N')>上架审核中
				                        <#elseif productForVerifyRow.isVerify?if_exists=='Y'>审核通过
				                        <#elseif productForVerifyRow.isOnline?if_exists=='N' && productForVerifyRow.isVerify?if_exists==''>未提交审核
				                        <#elseif productForVerifyRow.isVerify?if_exists=='N'>审核不通过
				                        </#if>
				                        </td>
										<td>${productForVerifyRow.storeName?if_exists}</td>
				                        <td>
				                        	<div class="btn-group">
                                                <button type="button" class="js-button btn btn-danger btn-sm" >查看</button>
				                        	</div>
				                        </td>
			                        </tr>
	                            </#list>
	                            <input type="hidden" name="productId" id="productId" value=""/>
		                        <input type="hidden" name="operateType" id="operateType"/>
                            </form>
                        </tbody>
                    </table>
                </div>
            </div>
            <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
            <#assign viewIndexFirst = 0/>
            <#assign viewIndexPrevious = viewIndex - 1/>
            <#assign viewIndexNext = viewIndex + 1/>
            <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(productForVerifyListSize, viewSize) />
            <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", productForVerifyListSize)/>
            <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
            <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
            listSize=productForVerifyListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
            pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
            paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />    
        <#else>
            <div id="">
                <h3>没有商品审核数据</h3>
            </div>
        </#if>
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
		
		
		// 审核通过处理
		$("#batchProductVerifyPass").click(function(){
		    
		    var ids = "";
		    var checks = $('.js-checkparent .js-checkchild:checked');
		    //判断是否选中记录
	    	if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
				}); 
				
				var checkedIds =$("#ids").val(); 
		        if (checkedIds != ""){
		           	//设置删除弹出框内容
					$('#modal_confirm #modal_confirm_body').html("审核通过后，商品在商城中可以销售！");
					$('#modal_confirm').find('input[name=verifyStatus]').val('Y');
					$('#modal_confirm').modal('show');
		        }
	        }else{
	             //设置提示弹出框内容
	    	  	 $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
	    	  	 $('#modal_msg').modal();
	        }
		})
		
		
		// 审核不通过处理
		$("#batchProductVerifyNoPass").click(function(){
		    var ids = "";
		    var checks = $('.js-checkparent .js-checkchild:checked');
		     //判断是否选中记录
	    	if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
				}); 
				
				var checkedIds =$("#ids").val(); 
		        if (checkedIds != ""){
		           	//设置删除弹出框内容
					$('#modal_confirm #modal_confirm_body').html("审核不通过，商品将不能上架！");
					$('#modal_confirm').find('input[name=verifyStatus]').val('N');
					$('#modal_confirm').modal('show');
		        }
	        }else{
	             //设置提示弹出框内容
    	  	  $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    	  	  $('#modal_msg').modal();
	        }
		})
		
		
		
	    //更新审核状态弹出框按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
	        var verifyStatus=$('#modal_confirm').find('input[name=verifyStatus]').val();
	        if(verifyStatus=='Y'){
	        updateProductVerifyStatusByIds('Y');
	        }else{
	        updateProductVerifyStatusByIds('N');
	        }
	    });
		
		
		// 商品查看处理
		$('.js-button').on('click',function(){
			var tr=$(this).closest('tr');
			var productId=tr.find('input[name=productId]').val();//商品Id
            var productTypeId=tr.find('input[name=productTypeId]').val();//商品类型

            document.location.href="<@ofbizUrl>viewProductGoodForVerify?productId="+productId+"&productTypeId="+productTypeId+"&operateType=view</@ofbizUrl>";
		})
    });
    
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

