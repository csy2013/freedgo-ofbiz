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

<#assign commonUrl = "findHotSearchIco?lookupFlag=Y"+ paramList +"&">
<div class="box box-info">
    <#--
	<div class="box-header with-border">
	  <h3 class="box-title">${uiLabelMap.ProductBrand}</h3>
	</div>
	-->
	<div class="box-body">
       <form class="form-inline clearfix" role="form" action="<@ofbizUrl>findHotSearchIco</@ofbizUrl>">
        	<div class="form-group">
              <div class="input-group m-b-10">
                <span class="input-group-addon">站点</span>
                <input type="text" class="form-control" value="" id="webSiteName" name="webSiteName"/>
              </div>
              
              
              <div class="input-group m-b-10">
                <span class="input-group-addon">社区</span>
                <input type="text" class="form-control" value="" id="communityName" name="communityName"/>
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
                        <#if security.hasEntityPermission("CONTENT_HOTSEARCH", "_CREATE", session)>
				        <button id="btnAdd" class="btn btn-primary">
			              <i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}
			            </button>
			            </#if>
			            <#if security.hasEntityPermission("CONTENT_HOTSEARCH", "_DEL", session)>
                        <button class="btn btn-primary" id="btnDel"> 
                            <i class="fa fa-trash"></i>${uiLabelMap.BrandDel}
	                    </button>
	                    </#if>
               	    </div>
                </div>
            </div>
        </div>
        
        <#if hotSearchList?has_content>
		   <input id="ids" type="hidden"/>
           <div class="row m-b-10">
                <div class="col-sm-12">
                    <div class="dp-tables_length">
                        <label>
                            ${uiLabelMap.DisplayPage}
                            <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                    onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="10" <#if viewSize==10>selected</#if>>10</option>
                                <option value="20" <#if viewSize==20>selected</#if>>20</option>
                                <option value="30" <#if viewSize==30>selected</#if>>30</option>
                                <option value="40" <#if viewSize==40>selected</#if>>40</option>
                            </select>
                            ${uiLabelMap.BrandItem}
                        </label>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <table class="table table-bordered table-hover js-checkparent">
                        <thead>
                        <tr>
                            <th><input class="js-allcheck" type="checkbox" id="checkAll"></th>
                            <th>热门搜索</th>
                            <th>站点</th>
                            <th>社区</th>
                            <th>${uiLabelMap.BrandOption}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <form method="post" action="" name="editHotSearch" id="editHotSearch">
	                            <#list hotSearchList as hotSearchRow>
		                            <tr>
				                        <td><input class="js-checkchild" type="checkbox" id="${hotSearchRow.contentHotSearchId?if_exists}" value="${hotSearchRow.contentHotSearchId?if_exists}"/>
				                        </td>
				                      
				                        <td>${hotSearchRow.hotSearchKeyName?if_exists}
				                           <input type="hidden" name="contentHotSearchId" id="contentHotSearchId" value="${hotSearchRow.contentHotSearchId?if_exists}"/>  
				                        </td>
				                        <td>
				                        <#if hotSearchRow.isAllWebSite?if_exists=="0">
				                                                                所有站点
				                        <#else>
				                           ${hotSearchRow.siteName?if_exists}
				                        </#if>
				                        </td>
				                        <td>
				                        <#if hotSearchRow.isAllCommunity?if_exists=="0">
				                                                               所有社区
				                        <#else>
				                          ${hotSearchRow.name?if_exists}
				                        </#if>   
				                        </td>
				                        <td>
				                        	<div class="btn-group">
				                        	    <#if security.hasEntityPermission("CONTENT_HOTSEARCH", "_UPDATE", session)>
					                            	<button type="button" class="js-button btn btn-danger btn-sm" >${uiLabelMap.BrandEdit}</button>
					                            <#else>
					                            	<button type="button"  disabled class="js-button btn btn-danger btn-sm" >${uiLabelMap.BrandEdit}</button>
					                            </#if>
					                            <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
					                              <span class="caret"></span>
					                              <span class="sr-only">Toggle Dropdown</span>
					                            </button>
					                            <ul class="dropdown-menu" role="menu">
					                              <#if security.hasEntityPermission("CONTENT_HOTSEARCH", "_DEL", session)>
					                              	 <li><a href="javascript:del(${hotSearchRow.contentHotSearchId?if_exists})">${uiLabelMap.BrandDel}</a></li>
					                              </#if>
					                              <#if security.hasEntityPermission("CONTENT_HOTSEARCH", "_UPDATE", session)>
						                              <#if hotSearchList.size() gt 1>
				                                           <#if hotSearchRow_index=0>
					                                         	<li><a href="#" class="js_Down">${uiLabelMap.MoveDown}</a></li>
				                                           <#elseif hotSearchRow_index=hotSearchList.size()-1>
				                                                 <li><a href="#" class="js_Up">${uiLabelMap.Moveup}</a></li>
				                                           <#elseif hotSearchRow_index gt 0 && hotSearchRow_index lt hotSearchList.size()-1 >
				                                                 <li><a href="#" class="js_Up">${uiLabelMap.Moveup}</a></li>
				                                                 <li><a href="#" class="js_Down">${uiLabelMap.MoveDown}</a></li>
				                                           </#if>
				                                      <#else>
				                                      </#if>
			                                      </#if>
					                            </ul>
				                        	</div>
				                        </td>
			                        </tr>
	                            </#list>
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
            <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(hotSearchListSize, viewSize) />
            <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", hotSearchListSize)/>
            <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
            <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
            listSize=hotSearchListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
            pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
            paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />    
        <#else>
            <div id="">
                <h3>没有热门搜索数据</h3>
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
	
<script language="JavaScript" type="text/javascript">
	$(function(){
	var viewIndexCur='${viewIndex?if_exists}';
	var viewSizeCur='${viewSize?if_exists}';
	//alert(testSize);
	//alert(test);
	   //下移站点
	   $('.js_Down').on('click',function(){
		 var tr=$(this).closest('tr');
		 var contentHotSearchId=tr.find('input[name=contentHotSearchId]').val();//热门搜索Id
		 var nexttr=$(this).closest('tr').next();
		 var nextContentHotSearchId=nexttr.find('input[name=contentHotSearchId]').val();//热门搜索Id
		 
         //var viewSizeCur='${parameters.VIEW_SIZE?if_exists}';
         //var viewIndexCur='${parameters.VIEW_INDEX?if_exists}';
         
         var webSiteName=$("#webSiteName").val();
         var communityName=$("#communityName").val();
         
		 $.ajax({ 
		          url:'<@ofbizUrl>moveHotSearch</@ofbizUrl>',
		          type: 'post',
		          dataType: 'json',
		          data: {
	               'contentHotSearchId': contentHotSearchId,
	               'nextContentHotSearchId': nextContentHotSearchId,
	               'VIEW_SIZE' : viewSizeCur,
				   'VIEW_INDEX' : viewIndexCur,
				   'webSiteName' : webSiteName,
                   'communityName' : communityName
	                },
	                success: function(data) {
	                 	//window.location.href='<@ofbizUrl>findHotSearchIco?lookupFlag=Y</@ofbizUrl>';
	                 	var curViewIndex=data.curViewIndex;
	                 	document.location.href="<@ofbizUrl>findHotSearchIco?lookupFlag=Y&VIEW_SIZE="+viewSizeCur+"&VIEW_INDEX="+curViewIndex+"&webSiteName="+webSiteName+"&communityName"+communityName+"</@ofbizUrl>";
	                }
		      })
		})
		
		//上移站点
		$('.js_Up').on('click',function(){
		   var tr=$(this).closest('tr');
		   var contentHotSearchId=tr.find('input[name=contentHotSearchId]').val();//热门搜索Id
		   var nexttr=$(this).closest('tr').prev();
		   var nextContentHotSearchId=nexttr.find('input[name=contentHotSearchId]').val();//热门搜索Id
		   
		   //var viewSizeCur='${parameters.VIEW_SIZE?if_exists}';
	       //var viewIndexCur='${parameters.VIEW_INDEX?if_exists}';
	         
	       var webSiteName=$("#webSiteName").val();
	       var communityName=$("#communityName").val();
		   $.ajax({ 
		          url:'<@ofbizUrl>moveHotSearch</@ofbizUrl>',
		          type: 'post',
		          dataType: 'json',
		          data: {
		               'contentHotSearchId': contentHotSearchId,
		               'nextContentHotSearchId': nextContentHotSearchId,
		               'VIEW_SIZE' : viewSizeCur,
				       'VIEW_INDEX' : viewIndexCur,
				       'webSiteName' : webSiteName,
                       'communityName' : communityName
		                },
		                 success: function(data) {
		                     //window.location.href='<@ofbizUrl>findHotSearchIco?lookupFlag=Y</@ofbizUrl>';
		                     var curViewIndex=data.curViewIndex;
	                 	     document.location.href="<@ofbizUrl>findHotSearchIco?lookupFlag=Y&VIEW_SIZE="+viewSizeCur+"&VIEW_INDEX="+curViewIndex+"&webSiteName="+webSiteName+"&communityName"+communityName+"</@ofbizUrl>";
		                }
		      })
		})
			   
	   
	   
	   //添加按钮点击事件
	   $('#btnAdd').click(function(){
	    	window.location = '<@ofbizUrl>editHotSearch?operateType=create</@ofbizUrl>';
	   });
	   
	   
	   // 热门搜索的编辑处理
	   $('.js-button').on('click',function(){
			var tr=$(this).closest('tr');
			var contentHotSearchId=tr.find('input[name=contentHotSearchId]').val();//热门搜索Id
			
            //var viewSize='${parameters.VIEW_SIZE?if_exists}';
            //var viewIndex='${parameters.VIEW_INDEX?if_exists}';
            var webSiteNameForFind=$("#webSiteName").val();
            var communityNameForFind=$("#communityName").val();
            document.location.href="<@ofbizUrl>editHotSearch?contentHotSearchId="+contentHotSearchId+"&operateType=update"+"&"+"VIEW_SIZE="+viewSizeCur+"&"+"VIEW_INDEX="+viewIndexCur+"&"+"webSiteNameForFind="+webSiteNameForFind+"&"+"communityNameForFind="+communityNameForFind+"</@ofbizUrl>";
	  })
	  
	  
	  // 热门搜索的删除处理
	  $("#btnDel").click(function(){
	     var ids = "";
	     $('input[class="js-checkchild"]:checked').each(function(){ 
            ids = ids + "," + $(this).val();
            $("#ids").val(ids.substr(1,ids.length));
		 }); 
		
		 var checkedIds =$("#ids").val(); 
         if (checkedIds != ""){
            //设置删除弹出框内容
			$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
			$('#modal_confirm').modal('show');
         }else{
            //设置提示弹出框内容
	  	    $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
	  	    $('#modal_msg').modal();
         }
	  })
	
	  //删除弹出框删除按钮点击事件
      $('#modal_confirm #ok').click(function(e){
           delHotSearchDataById();
      });
	})
	
	
	
	// 将选择的记录id删除
    function delHotSearchDataById(){
       var viewIndexCur='${viewIndex?if_exists}';
	   var viewSizeCur='${viewSize?if_exists}';
	   
       var webSiteName=$("#webSiteName").val();
       var communityName=$("#communityName").val();
    
	   // 选中的项目
	   var checkedIds =$("#ids").val(); 
	   if (checkedIds != ""){
	       jQuery.ajax({
	        url: '<@ofbizUrl>delHotSearchByIds</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'checkedIds' : checkedIds,
	             'VIEW_SIZE' : viewSizeCur,
	             'VIEW_INDEX' : viewIndexCur,
	             'webSiteName' : webSiteName,
	             'communityName' : communityName
	        },
	        success: function(data) {
	           var curViewIndex=data.curViewIndex;
	           document.location.href="<@ofbizUrl>findHotSearchIco?lookupFlag=Y&VIEW_SIZE="+viewSizeCur+"&VIEW_INDEX="+curViewIndex+"&webSiteName="+webSiteName+"&communityName"+communityName+"</@ofbizUrl>"; 
	        }
	    });
	  } 
	}
	
	//行删除按钮事件
    function del(id){
	    $("#ids").val(id);
	   	//设置删除弹出框内容
		$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
		$('#modal_confirm').modal('show');
		
    } 
	


 
</script>	

