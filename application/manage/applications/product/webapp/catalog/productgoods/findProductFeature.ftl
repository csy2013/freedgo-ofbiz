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
<script language="JavaScript" type="text/javascript">
   $(function(){
      
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
      
      $("#btn_add").click(function(){
           document.location.href="<@ofbizUrl>EditProductFeature?operateType=create</@ofbizUrl>";
      })
      
      $('.js-button').on('click',function(){
            var viewSize='${parameters.VIEW_SIZE?if_exists}';
            var viewIndex='${parameters.VIEW_INDEX?if_exists}';
            var sortFieldCur="${requestParameters.sortField?if_exists}";
            var sortTypeCur="${requestParameters.sortType?if_exists}";
			var tr=$(this).closest('tr');
			var productFeatureTypeId=tr.find('input[name=productFeatureTypeId]').val();//特征类型Id
			var productFeatureTypeName=tr.find('td').eq(1);//特征名称
		    var description=tr.find('td').eq(2);//特征备注
		    
		    var productFeatureTypeNameForFind=$("#productFeatureTypeName").val();
            var descriptionForFind=$("#description").val();
			document.location.href="<@ofbizUrl>EditProductFeature?productFeatureTypeId="+productFeatureTypeId+ "&"+"operateType=update"+"&"+"productFeatureTypeName="+productFeatureTypeName.text()+"&"+"description="+description.text()+"&"+"VIEW_SIZE="+viewSize+"&"+"VIEW_INDEX="+viewIndex+"&"+"productFeatureTypeNameForFind="+productFeatureTypeNameForFind+"&"+"descriptionForFind="+descriptionForFind+"&sortField="+sortFieldCur+"&sortType="+sortTypeCur+"</@ofbizUrl>";
	   }) 
	   
	   $("#btnFeatureDel").click(function(){
		    var ids = "";
		    var checks = $('.js-checkparent .js-checkchild:checked');
		     //判断是否选中记录
	    	if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
				});
				isFeatureForProduct(); 
				//var checkedIds =$("#ids").val(); 
		        //if (checkedIds != ""){
		          
		           	//设置删除弹出框内容
				//	$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				//	$('#modal_confirm').modal('show');
		        //}
	        }else{
	           //设置提示弹出框内容
    	  	  $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    	  	  $('#modal_msg').modal();
	        }
	   })
	   //删除弹出框删除按钮点击事件
	   $('#modal_confirm #ok').click(function(e){
	        delFeatureDataById();
	   });
   })
   
   // 将选择的记录id删除
   function delFeatureDataById(){
       var viewSizeCur='${parameters.VIEW_SIZE?if_exists}';
       var viewIndexCur='${parameters.VIEW_INDEX?if_exists}';
       
       var sortFieldCur="${requestParameters.sortField?if_exists}";
       var sortTypeCur="${requestParameters.sortType?if_exists}";
       
       var productFeatureTypeName=$("#productFeatureTypeName").val();
       var description=$("#description").val();
     
       if(!(viewSizeCur==""&&viewIndexCur=="")){
          if(viewIndexCur!="${viewIndex?if_exists}"){
             viewIndexCur="${viewIndex?if_exists}";
          }
       }
       var checkedIds =$("#ids").val(); 
       // 选中的项目
       jQuery.ajax({
           url: '<@ofbizUrl>delProductFeatureByIds</@ofbizUrl>',
           type: 'POST',
           data: {
             'checkedIds' : checkedIds,
             'VIEW_SIZE' : viewSizeCur,
             'VIEW_INDEX' : viewIndexCur,
             'productFeatureTypeName' : productFeatureTypeName,
             'description' : description,
             'sortField' : sortFieldCur,
             'sortType' : sortTypeCur
           },
           success: function(data) {
               var curViewIndex=data.curViewIndex;
               document.location.href="<@ofbizUrl>ProductFeature?lookupFlag=Y&VIEW_SIZE="+viewSizeCur+"&VIEW_INDEX="+curViewIndex+"&sortField="+sortFieldCur+"&sortType="+sortTypeCur+"&productFeatureTypeName="+productFeatureTypeName+"&description"+description+"</@ofbizUrl>"; 
           }
       });
   }
   
   
   //行删除按钮事件
   function del(id){
	    $("#ids").val(id);
	    isFeatureForProduct();
	   	//设置删除弹出框内容
		//$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
		//$('#modal_confirm').modal('show');
   }
   
   
   
    // 判断商品特征是否是被商品使用
    function isFeatureForProduct(){
      var ids=$("#ids").val();
	  if (ids != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>isFeatureForProduct</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'featureTypeIds' : ids
	        },
	        success: function(data) {
	           var isUsedFlg=data.isUsedFlg;
	           if(isUsedFlg=="Y"){
	              //设置提示弹出框内容
	    	  	  $('#modal_msg #modal_msg_body').html("该商品特征已使用，不能删除");
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
</script>

<#assign commonUrl = "ProductFeature?lookupFlag=Y"+ paramList +"&">
<div class="box box-info">
	<div class="box-body">
    	<form class="form-inline clearfix" role="form" action="<@ofbizUrl>ProductFeature</@ofbizUrl>">
        	<div class="form-group">
              <div class="input-group m-b-10">
                <span class="input-group-addon">${uiLabelMap.ProductFeatureName}</span>
                <input type="text" class="form-control" value="${requestParameters.productFeatureTypeName?if_exists}" id="productFeatureTypeName" name="productFeatureTypeName"/>
              </div>
              
              
              <div class="input-group m-b-10">
                <span class="input-group-addon">${uiLabelMap.ProductFeatureDescription}</span>
                <input type="text" class="form-control" value="${requestParameters.description?if_exists}" id="description" name="description"/>
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
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_FEATURE", "_CREATE",session)>
                        <button id="btn_add" class="btn btn-primary">
				            <i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}
				        </button>
                        </#if>
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_FEATURE", "_DEL", session)>
                        <button class="btn btn-primary" id="btnFeatureDel"> 
                            <i class="fa fa-trash"></i>
	                        ${uiLabelMap.BrandDel}
	                    </button>
                        </#if>
               	    </div>
                </div>
                
            </div>
        </div>
        
        <#if productFeatureList?has_content>
		   <input id="ids" type="hidden"/>
           <div class="row m-b-10">
                <div class="col-sm-12">
                    <div class="dp-tables_length">
                        <label>
                            ${uiLabelMap.DisplayPage}
                            <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                    onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="5" <#if viewSize==5>selected</#if>>5</option>
                                <option value="15"<#if viewSize==15>selected</#if>>15</option>
                                <option value="20"<#if viewSize==20>selected</#if>>20</option>
                                <option value="25"<#if viewSize==25>selected</#if>>25</option>
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
                            <th>${uiLabelMap.ProductFeatureName}
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="productFeatureTypeName" id="a_productFeatureTypeName"></a>
                                <#--
                                <#if orderFiled == 'productFeatureTypeName'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=productFeatureTypeName&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=productFeatureTypeName&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=productFeatureTypeName&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>${uiLabelMap.FeatureNameDescription}
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="description" id="a_description"></a>
                                <#--
                                <#if orderFiled == 'description'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=description&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=description&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=description&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                           
                            <th>${uiLabelMap.BrandOption}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <form method="post" action="" name="editProducFeature" id="editProducFeature">
	                            <#list productFeatureList as productFeatureRow>
		                            <#if productFeatureRow.productFeatureTypeName?if_exists != "">
		                            <tr>
				                        <td><input class="js-checkchild" type="checkbox" id="${productFeatureRow.productFeatureTypeId?if_exists}" value="${productFeatureRow.productFeatureTypeId?if_exists}"/>
				                            <input type="hidden" name="productFeatureTypeId" id="productFeatureTypeId" value="${productFeatureRow.productFeatureTypeId?if_exists}"/>
				                        </td>
				                       
				                        <td>${productFeatureRow.productFeatureTypeName?if_exists}</td>
				                        <td>${productFeatureRow.description?if_exists}</td>
				                        <td>
				                        	<div class="btn-group">
				                        	    <#if security.hasEntityPermission("PRODUCTGOODMGR_FEATURE", "_UPDATE",session)>
					                            <button type="button" class="js-button btn btn-danger btn-sm" id="editFeature">${uiLabelMap.BrandEdit}</button>
					                            </#if>
					                            <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
					                              <span class="caret"></span>
					                              <span class="sr-only">Toggle Dropdown</span>
					                            </button>
					                            <ul class="dropdown-menu" role="menu">
					                             <#-- <li><a href="<@ofbizUrl>updateProductFeature?productFeatureTypeId=${productFeatureRow.productFeatureTypeId?if_exists}&operateType=delete</@ofbizUrl>">${uiLabelMap.BrandDel}</a></li>-->
					                              <#if security.hasEntityPermission("PRODUCTGOODMGR_FEATURE", "_DEL",session)>
                                                    <li><a href="javascript:del(${productFeatureRow.productFeatureTypeId?if_exists})">${uiLabelMap.BrandDel}</a></li>
                                                  </#if>
                                                  </ul>
				                        	</div>
				                        </td>
			                        </tr>
			                    </#if>    
	                            </#list>
	                            <input type="hidden" name="productBrandId" id="productFeatureTypeId" value=""/>
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
            <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(productFeatureListSize, viewSize) />
            <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", productFeatureListSize)/>
            <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
            <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
            listSize=productFeatureListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
            pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
            paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />    
        <#else>
            <div id="">
                <h3>没有商品特征数据</h3>
            </div>
        </#if>
        </div><!-- /.box-body -->
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
	
	


    
    

		
