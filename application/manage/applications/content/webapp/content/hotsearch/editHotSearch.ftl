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
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<div class="box box-info">
	<form id="hotSearchEditForm" name="hotSearchEditForm"  method="post" class="form-horizontal">
		<div class="box-body">
		
		   <#if hotSearchInfo?has_content>
		       <div class="row">
	                <div class="form-group col-sm-8">
	                    <label class="col-sm-2 control-label">热门搜索:</label>
	                    
	                    <div class="col-sm-10">
	                        <div class="col-sm-5" style="padding-left: 0px;">
								<input type="text" class="form-control dp-vd" id="hotSearchKeyName" name="hotSearchKeyName" value="${hotSearchInfo.hotSearchKeyName?if_exists}"/>
								<p class="dp-error-msg"></p>
							</div>
	                    </div>
		                    	
	                </div>
	            </div>
	            
				<div class="row">
					<div class="form-group col-sm-8" data-type="required" data-mark="站点">
	                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>
	                    <div class="col-sm-10">
	                    	<div class="col-sm-5" style="padding-left: 0px;">
								<select id="webSiteId" name="webSiteId" class="form-control select2WebSite dp-vd" multiple="multiple" data-placeholder="请选择站点">
									<#assign webSiteList = delegator.findByAnd("WebSite",{"isEnabled":"Y"}) >
			        				<#if webSiteList?has_content>
			        					<#list webSiteList as webSite>
						                  <option value="${webSite.webSiteId}">${webSite.siteName}</option>
						                </#list>
			        				</#if>
			                    </select>
			                    <p class="dp-error-msg"></p>
							</div>
							<div class="col-sm-3" style="padding-left: 0px;">
								<div class="checkbox">
			                        <label>
			                    		<input id="isAllWebSite" type="checkbox">所有站点
			                    		<input type="hidden" name="isAllWebSiteId" id="isAllWebSiteId" value=""/>
			                        </label>
			                    </div>
							</div>
	                    </div>
	                </div>
				</div>
				
				<div class="row">
					<div class="form-group col-sm-8" data-type="required" data-mark="社区">
	                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>社区:</label>
	                    <div class="col-sm-10">
	                    	<div class="col-sm-5" style="padding-left: 0px;">
								<select id="communityId" name="communityId" class="form-control select2Community dp-vd" multiple="multiple" data-placeholder="请选择社区">
									<#assign communityList = delegator.findByAnd("Community") >
			        				<#if communityList?has_content>
			        					<#list communityList as community>
						                  <option value="${community.communityId}">${community.name}</option>
						                </#list>
			        				</#if>
			                    </select>
			                    <p id="c_error_msg" class="dp-error-msg"></p>
							</div>
							<div class="col-sm-3" style="padding-left: 0px;">
								<div class="checkbox">
			                        <label>
			                    		<input id="isAllCommunity" type="checkbox">所有社区
			                    		<input type="hidden" name="isAllCommunityId" id="isAllCommunityId" value=""/>
			                        </label>
			                    </div>
							</div>
	                    </div>
	                </div>
				</div>
				
				<div class="row">
					<div class="form-group col-sm-8" data-type="required" data-mark="序号">
	                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>
	                    <div class="col-sm-10" >
	                    	<div class="col-sm-5" style="padding-left: 0px;">
								<input type="text" class="form-control dp-vd" id="sequenceNum" name="sequenceNum" value="${hotSearchInfo.sequenceNum?if_exists}" />
								<p class="dp-error-msg"></p>
							</div>
	                    </div>
	                </div>
				</div>
		   
		   
		   <#else>
	    		<div class="row">
	                <div class="form-group col-sm-8">
	                    <label class="col-sm-2 control-label">热门搜索:</label>
	                    
	                    <div class="col-sm-10">
	                        <div class="col-sm-5" style="padding-left: 0px;">
								<input type="text" class="form-control dp-vd" id="hotSearchKeyName" name="hotSearchKeyName"/>
								<p class="dp-error-msg"></p>
							</div>
	                    </div>
		                    	
	                </div>
	            </div>
	            
				<div class="row">
					<div class="form-group col-sm-8" data-type="required" data-mark="站点">
	                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>
	                    <div class="col-sm-10">
	                    	<div class="col-sm-5" style="padding-left: 0px;">
								<select id="webSiteId" name="webSiteId" class="form-control select2WebSite dp-vd" multiple="multiple" data-placeholder="请选择站点">
									<#assign webSiteList = delegator.findByAnd("WebSite",{"isEnabled":"Y"}) >
			        				<#if webSiteList?has_content>
			        					<#list webSiteList as webSite>
						                  <option value="${webSite.webSiteId}">${webSite.siteName}</option>
						                </#list>
			        				</#if>
			                    </select>
			                    <p class="dp-error-msg"></p>
							</div>
							<div class="col-sm-3" style="padding-left: 0px;">
								<div class="checkbox">
			                        <label>
			                    		<input id="isAllWebSite" type="checkbox">所有站点
			                    		<input type="hidden" name="isAllWebSiteId" id="isAllWebSiteId" value=""/>
			                        </label>
			                    </div>
							</div>
	                    </div>
	                </div>
				</div>
				
				<div class="row">
					<div class="form-group col-sm-8" data-type="required" data-mark="社区">
	                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>社区:</label>
	                    <div class="col-sm-10">
	                    	<div class="col-sm-5" style="padding-left: 0px;">
								<select id="communityId" name="communityId" class="form-control select2Community dp-vd" multiple="multiple" data-placeholder="请选择社区">
									<#assign communityList = delegator.findByAnd("Community") >
			        				<#if communityList?has_content>
			        					<#list communityList as community>
						                  <option value="${community.communityId}">${community.name}</option>
						                </#list>
			        				</#if>
			                    </select>
			                    <p id="c_error_msg" class="dp-error-msg"></p>
							</div>
							<div class="col-sm-3" style="padding-left: 0px;">
								<div class="checkbox">
			                        <label>
			                    		<input id="isAllCommunity" type="checkbox">所有社区
			                    		<input type="hidden" name="isAllCommunityId" id="isAllCommunityId" value=""/>
			                        </label>
			                    </div>
							</div>
	                    </div>
	                </div>
				</div>
				
				 <div class="row">
					<div class="form-group col-sm-8" data-type="required" data-mark="序号">
	                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>
	                    <div class="col-sm-10" >
	                    	<div class="col-sm-5" style="padding-left: 0px;">
								<input type="text" class="form-control dp-vd" id="sequenceNum" name="sequenceNum" />
								<p class="dp-error-msg"></p>
							</div>
	                    </div>
	                </div>
				</div>
			</#if>
    
		</div><#-- box-body end -->
		<input id="contentHotSearchId" type="hidden" name="contentHotSearchId" value="${parameters.contentHotSearchId?if_exists}" />
	    <input type="hidden" name="operateType" id="operateType" value="${parameters.operateType?if_exists}"/>
		<div class="box-footer text-center">
            <button id="btnHotSearchSave" type="button" class="btn btn-primary m-r-10">保存</button>
        </div>
  	</form>
</div>
<script language="JavaScript" type="text/javascript">
var isCommitted = false;//表单是否已经提交标识，默认为false
$(function(){
    $(".select2Community").select2();
    $(".select2WebSite").select2();
    <#if hotSearchInfo?has_content>
      //初始化站点选择框
      <#if hotSearchInfo.isAllWebSite?if_exists=="0">
         $('#isAllWebSite').prop("checked","true");
		 $("#webSiteId").prop("disabled", true);
		 $("#webSiteId").removeClass('dp-vd');
      <#else>
        // 初始化站点
        var webSiteTemp="";
        <#if webSiteListForEdit?has_content >
	      <#list webSiteListForEdit as website>
	          var webSiteId = '${website.webSiteId}';
	          //$('.select2WebSite').val(webSiteId).trigger("change");
	          <#if website_index!=webSiteListForEdit.size()-1>
	             webSiteTemp=webSiteTemp+webSiteId+",";
	          <#else> 
	             webSiteTemp=webSiteTemp+webSiteId;
	          </#if>
	      </#list>
        </#if>
        $(".select2WebSite").val(webSiteTemp.split(',')).trigger("change");		 
      </#if>
      
	  //初始化社区选择框
	  <#if hotSearchInfo.isAllCommunity?if_exists=="0">
		$('#isAllCommunity').prop("checked","true");
		$("#communityId").prop("disabled", true);
		$("#communityId").removeClass('dp-vd');
	  <#else>
	    // 初始化社区
	    var communityTemp="";
        <#if communityListForEdit?has_content>
          
	      <#list communityListForEdit as community>
	          var communityId="${community.communityId}";
	          //$('.select2Community').val([communityId]).trigger("change");
	          <#if community_index!=communityListForEdit.size()-1>
	             communityTemp=communityTemp+communityId+",";
	          <#else> 
	             communityTemp=communityTemp+communityId;
	          </#if>
	      </#list>
        </#if>
        $(".select2Community").val(communityTemp.split(',')).trigger("change");
	  </#if>
	
    </#if>
	//所有站点的选中事件
    $('#isAllWebSite').change(function(){
    	if($(this).prop("checked")){
    		$("#webSiteId").val(null).trigger("change");
    		$("#webSiteId").prop("disabled", true);
    		$("#webSiteId").removeClass('dp-vd');
    	}else{
    		$("#webSiteId").prop("disabled", false);
    		$("#webSiteId").addClass('dp-vd');
    	}
    });
    
    //所有社区的选中事件
    $('#isAllCommunity').change(function(){
    	if($(this).prop("checked")){
    		$("#communityId").val(null).trigger("change");
    		$("#communityId").prop("disabled", true);
    		$("#communityId").removeClass('dp-vd');
    	}else{
    		$("#communityId").prop("disabled", false);
    		$("#communityId").addClass('dp-vd');
    	}
    });
    
    
    // 保存处理
    $("#btnHotSearchSave").click(function(){
        $('#hotSearchEditForm').dpValidate({
		  clear: true
	   });
	   $('#hotSearchEditForm').submit();
	   //alert($("#webSiteId").val());
	   
	   
    });
    
    $('#hotSearchEditForm').dpValidate({
  		validate: true,
  		callback: function(){
		   var operate='${parameters.operateType?if_exists}';
		  
		   var viewSize='${parameters.VIEW_SIZE?if_exists}';
           var viewIndex='${parameters.VIEW_INDEX?if_exists}';
            
           var webSiteNameForFind="${requestParameters.webSiteNameForFind?if_exists}";
           var communityNameForFind="${requestParameters.communityNameForFind?if_exists}";

		   
		 
		   
		   
	       if(dosubmit()){ 
	           <#--
	           if(operate=="create"){
	              $("#contentHotSearchId").val("");
	           }
	           -->
	           $("#isAllWebSiteId").val($('#isAllWebSite').is(':checked') ? 0 : 1);
	           $("#isAllCommunityId").val($('#isAllCommunity').is(':checked') ? 0 : 1);
		       if(operate=="create"){
	              $("#contentHotSearchId").val("");
	              document.hotSearchEditForm.action="<@ofbizUrl>createHotSearchIco</@ofbizUrl>";
		          document.hotSearchEditForm.submit();
	           }else{
	           
	              // 排序号	                      
				  var sequenceNum=$("#sequenceNum").val();
				  // 社区ID	                      
				  var communityIdForUpdate=$('#communityId').val() !=null ? $('#communityId').val().join(',') : '';
				  // 站点ID	                      
				  var webSiteIdForUpdate=$('#webSiteId').val() !=null ? $('#webSiteId').val().join(',') : '';
				  // 热门搜索名称	                      
				  var hotSearchKeyName=$("#hotSearchKeyName").val();
				  // 热门搜索Id	                      
				  var contentHotSearchId=$("#contentHotSearchId").val();
	              
	              // 全部社区                      
			      var isAllCommunityId=$("#isAllCommunityId").val();
				  // 全部站点	                      
				  var isAllWebSiteId=$("#isAllWebSiteId").val();
		          jQuery.ajax({
					  url: '<@ofbizUrl>updateHotSearchIco</@ofbizUrl>',
					  type: 'POST',
					  data: {
							 'operateType':operate,
							 'contentHotSearchId':contentHotSearchId,		
							 'hotSearchKeyName':hotSearchKeyName,	
							 'webSiteIdForUpdate':webSiteIdForUpdate,	
							 'sequenceNum':sequenceNum,
							 'communityIdForUpdate':communityIdForUpdate,		
							 'isAllWebSiteId':isAllWebSiteId,	
							 'isAllCommunityId':isAllCommunityId,	
							 'VIEW_SIZE' : viewSize,
						     'VIEW_INDEX' : viewIndex,
						     'webSiteNameForFind':webSiteNameForFind,		
						     'communityNameForFind':communityNameForFind
							},
					  success: function(data) {
						    var curViewIndex=data.curViewIndex;
						    document.location.href="<@ofbizUrl>findHotSearchIco?lookupFlag=Y&VIEW_SIZE="+viewSize+"&VIEW_INDEX="+curViewIndex+"&webSiteName="+webSiteNameForFind+"&communityName="+communityNameForFind+"</@ofbizUrl>";
					  }
			      });
			      
			     
	           }
		       
	       }
  		}
    });
});

   function dosubmit(){
      if(isCommitted==false){
        isCommitted = true;//提交表单后，将表单是否已经提交标识设置为true
        return true;//返回true让表单正常提交
      }else{
        return false;//返回false那么表单将不提交
      }
   }
</script>
