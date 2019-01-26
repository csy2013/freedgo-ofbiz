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

<div class="box box-info">
    <div class="box-body">
        <input type="hidden" id="ids"/>
        <input type="hidden" name="country" id="country" value="CHN"/>
	    <input type="hidden" name="state" id="state" value=""/>
	    <input type="hidden" name="city" id="city" value=""/>
        <p class="classification">地区管理</p>
               <div class="dp-tables_btn">
                <!--是否有更新的权限-->
                <#if security.hasEntityPermission("GEO", "_UPDATE", session)>
                <button id="btn_address" class="btn btn-primary" >
                    <i class="fa fa-plus "></i>设为默认地址
                </button>
                </#if>
                <#if defaultAddressList?has_content>
                默认地址:
                <#list defaultAddressList as  lists>
                 <#assign countryGeo = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",lists.stateProvinceGeoId)))>
                 <#assign countryGeoCity = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",lists.cityGeoId)))>
                 <#assign countryGeoCounty = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",lists.countyGeoId)))>
                 <#if countryGeo != "">${(countryGeo.get("geoName",locale))?default('')}</#if>-
                 <#if countryGeoCity != "">${(countryGeoCity.get("geoName",locale))?default('')}</#if>-
                 <#if countryGeoCounty != "">${(countryGeoCounty.get("geoName",locale))?default('')}</#if>
                </#list>
                </#if>
                </div>
            </div><!-- 操作按钮组end -->
        <div class="xl_main_classfy clearfix">
            <div class="xl_item">
                <h4 class="xl_p1">
                </h4>
             <div class="xl_first_search" style="text-align:center;">
                <input type="text" name="first_classfy" style="width:95%;" class="xl_first first_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon1 search_icon1_1"></i>
                </div>
                <h4 class="xl_p1">
                <#if security.hasEntityPermission("GEO", "_CREATE",session)>
                <a href="javascript:;" id="state_btn">添加省份</a>
                </#if>
                </h4>
                
                <ul class="xl_ul1 xl_first_ul">
                    <li data-id="1">
                        <#---
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                            <a href="javascript:;"  class=" btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>
                            <a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>
                        </span>
                        -->

                    </li>
                </ui>   
            </div>
            <div class="xl_item">
                <h4 class="xl_p1">
                </h4>
                <div class="xl_first_search" style="text-align:center;">
                    <input type="text" name="first_classfy" style="width:95%;" class="xl_first second_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon2 search_icon2_1"></i>
                </div>
                <h4 class="xl_p1"> 
                <#if security.hasEntityPermission("GEO", "_CREATE",session)>
                <a  id="stateProvince_btn" href="javascript:;">添加城市</a>
                </#if>
                </h4>
                <ul class="xl_ul1 xl_second_ul">
                    <li data-id="1">
                        <#--
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                            <a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>
                            <a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>
                        </span>
                        -->
                    </li>
                </ul>
            </div>
            <div class="xl_item">
                <h4 class="xl_p1">
                </h4>
                <div class="xl_first_search" style="text-align:center;">
                    <input type="text" name="first_classfy" style="width:95%;" class="xl_first third_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon3 search_icon3_1"></i>
                </div>
                <h4 class="xl_p1">
                <a id="city_btn" href="javascript:;">添加地区</a>
                </h4>
               
                <ul class="xl_ul1 xl_third_ul">
                    <li data-id="1">
                        <#--
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                            <a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>
                            <a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>
                        </span>
                        -->

                    </li>
                </ul>
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
	        <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.OptionMsg}</h4>
	      </div>
	      <div class="modal-body">
	        <h4 id="modal_msg_body"></h4>
	      </div>
	      <div class="modal-footer">
	        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.Ok}</button>
	      </div>
	    </div>
	  </div>
	</div><!-- 提示弹出框end -->
	
	
	<!-- ${uiLabelMap.BrandDel}确认弹出框start -->
	<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.OptionMsg}</h4>
	      </div>
	      <div class="modal-body">
	        <h4 id="modal_confirm_body"></h4>
	      </div>
	      <div class="modal-footer">
	      	<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
	        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">删除</button>
	      </div>
	    </div>
	  </div>
	</div><!-- ${uiLabelMap.BrandDel}确认弹出框end -->
	
	<!-- ${uiLabelMap.BrandDel}确认弹出框start -->
	<div id="defalut_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.OptionMsg}</h4>
	      </div>
	      <div class="modal-body">
	        <h4 id="modal_confirm_body"></h4>
	      </div>
	      <div class="modal-footer">
	        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
	      	<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
	      </div>
	    </div>
	  </div>
	</div><!-- ${uiLabelMap.BrandDel}确认弹出框end -->
 
   <!--新增省弹出框 start-->
<div class="modal fade" id="country_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">添加省份</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="countryForm" action="" method="post">
             <!-- 省份名称 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="省份名称">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>省份名称:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="geoName" name="geoName">
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div><!-- 省份名称 end-->
          <!-- 排序 start-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="排序">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>排序:</label>
                            <div class="col-sm-10" >
                               <input type="text" class="form-control dp-vd w-p50" id="sequenceNum" name="sequenceNum">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
           <!-- 排序 end-->
          <div class="modal-footer">
		    <button type="button"  id="countryForm_btn" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--新增省弹出框 end-->
   
   <!--编辑省弹出框 start-->
<div class="modal fade" id="updateCountry_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">编辑省份</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="updateCountryForm" action="" method="post">
             <!-- 省份名称 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="省份名称">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>省份名称:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="geoName" name="geoName">
                      <input type="hidden" class="form-control dp-vd w-p50" id="geoId" name="geoId">
                      <input type="hidden" class="form-control dp-vd w-p50" id="geoIdFrom" name="geoIdFrom">
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div><!-- 省份名称 end-->
          <!-- 排序 start-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="排序">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>排序:</label>
                            <div class="col-sm-10" >
                               <input type="text" class="form-control dp-vd w-p50" id="sequenceNum" name="sequenceNum">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
           <!-- 排序 end-->
           
            <!-- 所属 start-->
             <div class="row">
                            <div class="form-group" data-type="required" data-mark="${uiLabelMap.SubTypeName}">
                                <label  class="control-label col-sm-2">所属:</label>
                                <div class="col-sm-10">
                                <label  class="control-label col-sm-2">全国</label>
                                </div>
                            </div>
             </div>
            <!-- 所属 end-->
         
          <div class="modal-footer">
		    <button type="button"  id="updateCountry_btn" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--编辑省弹出框 end-->
   
   <!--新增市弹出框 end-->
<div class="modal fade" id="stateProvince_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">添加城市</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="stateProvinceForm" action="" method="post">
             <!-- 市名称 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="城市名称">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>城市名称:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="geoName" name="geoName">
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div><!-- 市名称 end-->
          
          <!-- 排序 start-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="排序">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>排序:</label>
                            <div class="col-sm-10" >
                               <input type="text" class="form-control dp-vd w-p50"  name="sequenceNum">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
           <!-- 排序 end-->
           
           <!-- 所属省份 start-->
             <div class="row">
                   <div style="display:none;">
                    	<select name="countryGeoId" id="stateProvinceForm_countryGeoId" >
	                    	${screens.render("component://common/widget/CommonScreens.xml#countries")}
		                    <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
	                        <option selected="selected" value="${defaultCountryGeoId}">
	                        <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
	                        ${countryGeo.get("geoName",locale)}
	                        </option>
	                    </select>
                    </div>
                <div class="form-group" data-type="required" data-mark="所属省份">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>所属省份:</label>
                   <div class="col-sm-10">
                     <select class="form-control w-p50" name="stateProvinceGeoId" id="stateProvinceForm_stateProvinceGeoId">
                        <option value=""></option>
                      </select>
                   </div>
                </div>
          </div><!-- 所属省份 end-->
           
          <div class="modal-footer">
		    <button type="button"  id="stateForm_btn" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--新增市弹出框 end-->
   
   <!--更新市弹出框 end-->
<div class="modal fade" id="updateStateProvince_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">编辑城市</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="updateStateProvinceForm" action="" method="post">
             <!-- 市名称 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="城市名称">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>城市名称:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="geoName" name="geoName">
                      <input type="hidden" class="form-control dp-vd w-p50" id="geoId" name="geoId"/>
                      <input type="hidden" class="form-control dp-vd w-p50" id="geoIdFrom" name="geoIdFrom"/>
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div><!-- 市名称 end-->
          
          <!-- 排序 start-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="排序">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>排序:</label>
                            <div class="col-sm-10" >
                               <input type="text" class="form-control dp-vd w-p50"  id="sequenceNum" name="sequenceNum">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
           <!-- 排序 end-->
           
             <!-- 所属 start-->
             <div class="row">
                            <div class="form-group" data-type="required" data-mark="${uiLabelMap.SubTypeName}">
                                <label  class="control-label col-sm-2">所属:</label>
                                <div class="col-sm-10">
                                <label  class="control-label col-sm-2" id="State_geoToName"></label>
                                </div>
                            </div>
             </div>
            <!-- 所属 end-->
            
              <!-- 仓库start-->
             <div class="row">
                            <div class="form-group">
                                <label  class="control-label col-sm-2">配送仓库:</label>
                            <div id="facility"></div>
                            </div>
             </div>
            <!-- 仓库 end-->
           
          <div class="modal-footer">
		    <button type="button"  id="updateStateForm_btn" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--更新市弹出框 end-->
   
   <!--新增区弹出框 end-->
<div class="modal fade" id="city_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">添加地区</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="cityForm" action="" method="post">
             <!-- 省份名称 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="地区名称">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>地区名称:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="geoName" name="geoName">
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div><!-- 省份名称 end-->
          <!-- 排序 start-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="排序">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>排序:</label>
                            <div class="col-sm-10" >
                               <input type="text" class="form-control dp-vd w-p50" id="sequenceNum" name="sequenceNum">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
           <!-- 排序 end-->
              <!-- 所属省份 start-->
             <div class="row">
                   <div style="display:none;">
                    	<select name="countryGeoId" id="cityForm_countryGeoId" >
	                    	${screens.render("component://common/widget/CommonScreens.xml#countries")}
		                    <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
	                        <option selected="selected" value="${defaultCountryGeoId}">
	                        <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
	                        ${countryGeo.get("geoName",locale)}
	                        </option>
	                    </select>
                    </div>
                <div class="form-group" data-type="required" data-mark="所属省份">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>所属省份:</label>
                   <div class="col-sm-10">
                     <select class="form-control w-p50" name="stateProvinceGeoId" id="cityForm_stateProvinceGeoId">
                        <option value=""></option>
                      </select>
                   </div>
                </div>
          </div><!-- 所属省份 end-->
          
            <!-- 所属城市 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="所属城市">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>所属城市:</label>
                   <div class="col-sm-10">
                     <select class="form-control w-p50" name="cityGeoId" id="cityForm_cityGeoId">
                        <option value=""></option>
                      </select>
                   </div>
                </div>
          </div><!-- 所属城市 end-->
          
          <div class="modal-footer">
		    <button type="button"  id="cityForm_btn" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--新增区弹出框 end-->
   
   <!--更新区弹出框 end-->
<div class="modal fade" id="updateCity_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">编辑地区</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="updateCityForm" action="" method="post">
             <!-- 省份名称 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="地区名称">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>地区名称:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="geoName" name="geoName">
                      <input type="hidden" class="form-control dp-vd w-p50" id="geoId" name="geoId">
                      <input type="hidden" class="form-control dp-vd w-p50" id="geoIdFrom" name="geoIdFrom">
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div><!-- 省份名称 end-->
          <!-- 排序 start-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="排序">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>排序:</label>
                            <div class="col-sm-10" >
                               <input type="text" class="form-control dp-vd w-p50" id="sequenceNum" name="sequenceNum">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
           <!-- 排序 end-->
          
           <!-- 所属 start-->
             <div class="row">
                            <div class="form-group" data-type="required" data-mark="${uiLabelMap.SubTypeName}">
                                <label  class="control-label col-sm-2">所属:</label>
                                <div class="col-sm-10">
                                <label  class="control-label col-sm-4" id="city_geoToName"></label>
                                </div>
                            </div>
             </div>
            <!-- 所属 end-->
            
          <div class="modal-footer">
		    <button type="button"  id="updateCityForm_btn" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--新增区弹出框 end-->
 
<script language="JavaScript" type="text/javascript">
    $(function(){
         //seclectdeItemById('CHN','country','');
         seclectdeItemByName('CHN','country','');
         seclectdeItemByName('CHN','country','');
         var del_ids = "";
		 //省删除按钮事件
		 $(document).on('click','.xl_first_ul .xl_delte',function(){
			 var geoId=$(this).parent().parent().attr("data-id");
			 del_ids=geoId;
			  $.ajax({
			        url: '<@ofbizUrl>findGeoByName</@ofbizUrl>',
	                data: {'geoId' : geoId },
					type: "POST",
					dataType : "json",
					success: function(data){
						if(data.stateList.length>0){
							//设置提示弹出框内容
							$('#modal_msg #modal_msg_body').html("该省份下存在城市信息，无法删除！");
							$('#modal_msg').modal();
						}else{
							//设置删除弹出框内容
				    		$('#modal_confirm #modal_confirm_body').html("确认删除这条记录吗？");
				    		$('#modal_confirm').modal('show');
						}
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
						$('#modal_msg').modal();
					}
				});
		  })
		 //市删除事件
		  $(document).on('click','.xl_second_ul .xl_delte',function(){
		     var geoId=$(this).parent().parent().attr("data-id");
			   del_ids=geoId;
			  $.ajax({
			        url: '<@ofbizUrl>findGeoByName</@ofbizUrl>',
	                data: {'geoId' : geoId },
					type: "POST",
					dataType : "json",
					success: function(data){
						if(data.stateList.length>0){
							//设置提示弹出框内容
							$('#modal_msg #modal_msg_body').html("该城市下存在地区信息，无法删除！");
							$('#modal_msg').modal();
						}else{
							//设置删除弹出框内容
				    		$('#modal_confirm #modal_confirm_body').html("确认删除这条记录吗？");
				    		$('#modal_confirm').modal('show');
						}
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
						$('#modal_msg').modal();
					}
				});
		 })
		 //区删除事件
		  $(document).on('click','.xl_third_ul .xl_delte',function(){
		     var geoId=$(this).parent().parent().attr("data-id");
		     	 geoId=geoId.trim();
		     	 del_ids=geoId;
		     	 $('#modal_confirm #modal_confirm_body').html("确认删除这条记录吗？");
				 $('#modal_confirm').modal('show');
		 })
		  //删除弹出框删除按钮点击事件
	   $('#modal_confirm #ok').click(function(e){
	        $.ajax({
					url: "delGeo",
					type: "POST",
					data: {geoId : del_ids},
					dataType : "json",
					success: function(data){
						if(data.status){
							//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
    				$('#modal_msg').modal();
    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
    				$('#modal_msg').off('hide.bs.modal');
    				$('#modal_msg').on('hide.bs.modal', function () {
					  window.location.reload();
					})
				      }
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
						$('#modal_msg').modal();
					}
				});
	   });
		 
		 //编辑省按钮事件
		 $(document).on('click','.xl_first_ul .xl_edit',function(){
		    var geoId=$(this).parent().parent().attr("data-id");
		    geoId=geoId.trim();
		    $.ajax({
            type: 'post',
            url: '<@ofbizUrl>findGeoById</@ofbizUrl>',
            data: {geoId: geoId},
            success: function (data) {
                $("#updateCountryForm #geoId").val(data.geoId);
                $("#updateCountryForm #geoName").val(data.geoName);
                $("#updateCountryForm #sequenceNum").val(data.sequenceNum);
                $("#updateCountryForm #geoIdFrom").val(data.geoIdFrom);
                }
             });
		    $('#updateCountry_Modal').modal('show');
		 })
		 
		 //编辑省点提交按钮点击事件
	    $('#updateCountry_btn').click(function(){
	        $('#updateCountryForm').dpValidate({
			  clear: true
			});
			$('#updateCountryForm').submit();
	    });
		  //编辑省表单校验
       $('#updateCountryForm').dpValidate({
        validate: true,
        callback: function(){
            var geoName=$('#updateCountryForm  #geoName').val();
           $.ajax({
					url: "updateGeo",
					type: "POST",
					data: $('#updateCountryForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#updateCountry_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
	    				  var html= geoName+'<span class="xl_del_btn" style="display: block;"><a href="javascript:;" class="btn btn-default btn-xs xl_edit">编辑</a><a href="javascript:;" class="btn btn-default btn-xs xl_delte">删除</a></span>'
	    				  $(".xl_first_ul").find('.xl_active').html(html);
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
        
           //编辑市按钮事件
		 $(document).on('click','.xl_second_ul .xl_edit',function(){
		    var geoId=$(this).parent().parent().attr("data-id");
		       geoId=geoId.trim();
		       $("#facility").empty();
		       $.ajax({
                 type: 'post',
                 url: '<@ofbizUrl>findGeoById</@ofbizUrl>',
                 data: {geoId: geoId},
                 success: function (data) {
                 
                 var facilityList=data.facilityList
                 if(data.facilityList){
                 var parentClass = '',
		             childClass = '',
		             childLabel = '<label class="control-label col-sm-3">优先等级</label>';
		             
                 for (var i=0;i<facilityList.length;i++){
		            var facility =(facilityList[i]);
		            var liHtml;
		            
		            if (i && !parentClass) {
		            	parentClass = 'col-sm-offset-2';
		            	childClass = 'col-sm-offset-3';
		            } 
		            
		            liHtml = '<div class="col-sm-10 '+parentClass+'">'
		                   + '<div class="col-sm-3">'
		                   + '<input type="hidden" class="form-control dp-vd" name="facilityId" value="'+facility.facilityCoverageAreaId+'">'
		                   + '<input type="text" class="form-control dp-vd m-b-10" name="facilityName" value="'+facility.facilityName+'" readonly>'
		                   + '</div>' + childLabel;
		                  
		                  if(facility.sequenceId==null){
		                  	liHtml=liHtml+'<div class="col-sm-3 m-b-10 '+childClass+'"><input type="text" class="form-control dp-vd" name="sequenceId" value=""></div></div>'
		                  } else{
		                  	liHtml=liHtml+'<div class="col-sm-3 m-b-10 '+childClass+'"><input type="text" class="form-control dp-vd" name="sequenceId" value="'+facility.sequenceId+'"></div></div>'
		                  }
		            
		            if (childLabel) childLabel = '';
		            
                    $("#updateStateProvinceForm #facility").append(liHtml);
		          }
                 }
                 
                 
                $("#updateStateProvinceForm #geoId").val(data.geoId);
                $("#updateStateProvinceForm #geoName").val(data.geoName);
                $("#updateStateProvinceForm #sequenceNum").val(data.sequenceNum);
                $("#updateStateProvinceForm #State_geoToName").text(data.geoToName);
                $("#updateStateProvinceForm #geoIdFrom").val(data.geoIdFrom);
                }
             });
			$('#updateStateProvince_Modal').modal('show');
		 })
		 
		  //编辑市点提交按钮点击事件
	    $('#updateStateForm_btn').click(function(){
	        $('#updateStateProvinceForm').dpValidate({
			  clear: true
			});
			$('#updateStateProvinceForm').submit();
	    });
		  //编辑省表单校验
       $('#updateStateProvinceForm').dpValidate({
        validate: true,
        callback: function(){
        var  geoName=$('#updateStateProvinceForm #geoName').val();
         var facility='';
            $('#facility .col-sm-10').each(function(){
            var tdArr = $(this);
            for(var i=0;i<tdArr.length;i++){
             var facilityId=tdArr.find("input[name=facilityId]").val();//仓库编号
             var sequenceId=tdArr.find("input[name=sequenceId]").val();//每积一分消费金额
             facility+=facilityId+":"+sequenceId+",";
             }
            });
           $.ajax({
					url: "updateGeo",
					type: "POST",
					data: $('#updateStateProvinceForm').serialize()+ "&facility="+facility,
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#updateStateProvince_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  var html= geoName+'<span class="xl_del_btn" style="display: block;"><a href="javascript:;" class="btn btn-default btn-xs xl_edit">编辑</a><a href="javascript:;" class="btn btn-default btn-xs xl_delte">删除</a></span>'
						 $(".xl_second_ul").find('.xl_active').html(html);
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
        
		 //编辑区按钮事件
		 $(document).on('click','.xl_third_ul .xl_edit',function(){
		    var geoId=$(this).parent().parent().attr("data-id");
		        geoId=geoId.trim();
		       $.ajax({
                 type: 'post',
                 url: '<@ofbizUrl>findCityGeoById</@ofbizUrl>',
                 data: {geoId: geoId},
                 success: function (data) {
                $("#updateCityForm #geoId").val(data.geoId);
                $("#updateCityForm #geoName").val(data.geoName);
                $("#updateCityForm #city_geoToName").text(data.geoToName);
                $("#updateCityForm #sequenceNum").val(data.sequenceNum);
                $("#updateCityForm #geoIdFrom").val(data.geoIdFrom);
                }
             });
             $('#updateCity_Modal').modal('show');
		 })
		 
		   //编辑区点提交按钮点击事件
	    $('#updateCityForm_btn').click(function(){
	        $('#updateCityForm').dpValidate({
			  clear: true
			});
			$('#updateCityForm').submit();
	    });
	    
		 //编辑区表单校验
       $('#updateCityForm').dpValidate({
        validate: true,
        callback: function(){
         var geoName=$('#updateCityForm #geoName').val();
        
           $.ajax({
					url: "updateGeo",
					type: "POST",
					data: $('#updateCityForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#updateCity_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						    var html= geoName+'<span class="xl_del_btn" style="display: block;"><a href="javascript:;" class="btn btn-default btn-xs xl_edit">编辑</a><a href="javascript:;" class="btn btn-default btn-xs xl_delte">删除</a></span>'
						   $(".xl_third_ul").find('.xl_active').html(html);
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
    	
        
        //选中li的时候显示那两个按钮
        $(".xl_ul1").on("click","li",function(e){
            if($(e.target).is("a")){
                return;
            }else{
                $(this).find(".xl_del_btn").show();
                $(this).addClass("xl_active").siblings().removeClass("xl_active");
                $(this).siblings().find(".xl_del_btn").hide();
            }

        });
        //选中一级分类的时候向后台发送请求
        $(".xl_first_ul").on("click","li",function(e){
            if($(e.target).is("a")){
                return;
            }else{
                var geoId=$(this).attr("data-id");
                $('#state').val(geoId.trim());
                if(geoId){
                seclectdeItemByName(geoId.trim(),'state','');
                } 
                $('.xl_first_ul li[data-id='+geoId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");   
            }
        });
        //选中市的时候向后台发送请求
        $(".xl_second_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var geoId=$(this).attr("data-id");
                 $('#city').val(geoId); 
                 if(geoId){
                 seclectdeItemByName(geoId.trim(),'city','');
                 }
                $('.xl_second_ul li[data-id='+geoId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");        
            }
        });
        
        //选中三级分类的时候向后台发送请求
        $(".xl_second_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                $('.xl_third_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");      
                $("#curProductCategoryIdLevel3").val(productCategoryId);
            }
        });
        
        //一级分类搜索的时候向后台发送请求
        $(".search_icon1").on("click",function(){
            var geoName=$(".first_search").val();
            seclectdeItemByName('CHN','country',geoName)
        });
        
        //二级分类搜索的时候向后台发送请求
        $(".search_icon2").on("click",function(){
           var geoName=$(".second_search").val();
           var geoId=$("#state").val();
           seclectdeItemByName(geoId,'state',geoName)
        });
        //三级分类搜索的时候向后台发送请求
        $(".search_icon3").on("click",function(){
            var geoName=$(".third_search").val();
            var geoId=$("#city").val();
           seclectdeItemByName(geoId,'city',geoName)
        });
       
       
	  
	 

    })
    
	//分类级别的查找功能
	function searchProductCategoryLevelByName(productCategoryLevel,productCategoryId,categoryName){
	    $.ajax({
            url:'<@ofbizUrl>searchProductCategoryLevelByName</@ofbizUrl>',
            dataType:'json',
            type:'post',
            beforeSend:function(){
               if(productCategoryLevel=="1"){
                 $('.search_icon1_1').removeClass('search_icon1');
               }else if(productCategoryLevel=="2"){
                 $('.search_icon2_1').removeClass('search_icon2');
               
               }else if(productCategoryLevel=="3"){
                 $('.search_icon3_1').removeClass('search_icon3');
               }
            },
            data:{
                 'productCategoryLevel' : productCategoryLevel,
	             'productCategoryId':productCategoryId,
	             'categoryName':categoryName
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
		            
		            //var productCategoryIdLevel1 =data.productCategoryIdLevel1;
		            //var productCategoryIdLevel2 =data.productCategoryIdLevel2;
		            //var productCategoryIdLevel3 =data.productCategoryIdLevel3;
		            
		            
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
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != ""){
                           $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        
                        
                        if(productCategoryIdLevel1 != ""){
                            $('.xl_first_ul li[data-id='+productCategoryIdLevel1+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel1").val(productCategoryIdLevel1);
                        
				        $('.search_icon1_1').addClass('search_icon1');
			        }else if(level=='2'){
			            //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        
                        if(productCategoryIdLevel2 != ""){
                           $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        
			            $('.search_icon2_1').addClass('search_icon2');
			        }else if(level='3'){
			            if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
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
	
	
	//省市级别的选中处理功能
	function seclectdeItemById(geoId,flag){
	    $.ajax({
            url:'<@ofbizUrl>getAssociatedStateList</@ofbizUrl>',
            dataType:'json',
            type:'post',
            data:{
             'countryGeoId' : geoId
            },
            success:function(data){
                //把返回的数据放到二级菜单
              var stateList=data.stateList;
                if(flag=='country'){
	                 $(".xl_first_ul").empty();
			     for (var i=0;i<stateList.length;i++){
		            var geo =(stateList[i]);
		            var geos=[];
		            geos=geo.split(":");
			        var liHtml="<li data-id="+geos[1]+">"+geos[0]+"<span class='xl_del_btn'>"+"<a href='javascript:;' class='btn btn-default btn-xs xl_edit'>编辑</a>" +"<a href='javascript:;' class='btn btn-default btn-xs xl_delte'>删除</a>"+"</span>"+"</li>"
			        $(".xl_first_ul").append(liHtml);
		           }
		           $(".xl_first_ul").find('li:eq(0)').click();
                 }else if(flag=='state'){
                        //把返回的数据放到市菜单
                        var stateLi="";
                        for(var i=0;i<stateList.length;i++){
                          var geo =(stateList[i]);
		                  var geos=[];
		                  geos=geo.split(":");
                            stateLi+='<li data-id="'+geos[1]+'">'+geos[0]+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">编辑</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">删除</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_second_ul").html(stateLi);
                        $(".xl_second_ul").find('li:eq(0)').click();
                    }else if(flag=='city'){
                       //把返回的数据放到三级菜单
                        var cityLi="";
                        for(var i=0;i<stateList.length;i++){
                             var geo =(stateList[i]);
		                     var geos=[];
		                     geos=geo.split(":");
                            cityLi+='<li data-id="'+geos[1]+'">'+geos[0]+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">编辑</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">删除</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(cityLi);
                        $(".xl_third_ul").find('li:eq(0)').click();
                    }
            },
            error:function(){
                alert("操作失败")
           }
        })
	}
	
	//省市级别的选中处理功能
	function seclectdeItemByName(geoId,flag,geoName){
	    $.ajax({
            url:'<@ofbizUrl>findGeoByName</@ofbizUrl>',
            dataType:'json',
            type:'post',
            data:{
             'geoId' : geoId,
             'geoName' : geoName
            },
            success:function(data){
                //把返回的数据放到二级菜单
                    //根据a返回它的data-id
                var stateList= data.stateList;
                if(flag=='country'){
	                 $(".xl_first_ul").empty();
			     for (var i=0;i<stateList.length;i++){
		            var geo =(stateList[i]);
		            var geos=[];
		            geos=geo.split(":");
			        var liHtml="<li data-id="+geos[1]+">"+geos[0]+"<span class='xl_del_btn'>"
			                  +"<#if security.hasEntityPermission("GEO", "_UPDATE", session)>"
			                  +"<a href='javascript:;' class='btn btn-default btn-xs xl_edit'>编辑</a>" 
			                  +"</#if>"
			                  +"<#if security.hasEntityPermission("GEO", "_DELETE", session)>"
			                  +"<a href='javascript:;' class='btn btn-default btn-xs xl_delte'>删除</a>"
			                  +"</#if>"
			                  +"</span>"+"</li>"
			                  
			        $(".xl_first_ul").append(liHtml);
		           }
		           if($(".xl_first_ul").find('li').length>0){
		           $(".xl_first_ul").find('li:eq(0)').click();
		           }
                 }else if(flag=='state'){
                       $(".xl_second_ul").empty();
                        //把返回的数据放到市菜单
                        var stateLi="";
                        for(var i=0;i<stateList.length;i++){
                          var geo =(stateList[i]);
		                  var geos=[];
		                  geos=geo.split(":");
                            stateLi+='<li data-id="'+geos[1]+'">'+geos[0]+' <span class="xl_del_btn">'
                                    +"<#if security.hasEntityPermission("GEO", "_UPDATE", session)>"
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">编辑</a>'
                                    +"</#if>"
                                    +"<#if security.hasEntityPermission("GEO", "_DELETE", session)>"
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">删除</a>'
                                    +"</#if>"
                                    +'</span>'
                                    +'</li>'
                        }
                        
                        $(".xl_second_ul").html(stateLi);
                        if($(".xl_second_ul").find('li').length>0){
		                   $(".xl_second_ul").find('li:eq(0)').click();
		                 }else{
		                  $(".xl_third_ul").empty();
		                 }
                       
                    }else if(flag=='city'){
                        $(".xl_third_ul").empty();
                       //把返回的数据放到三级菜单
                        var cityLi="";
                        $(".xl_third_ul").find("li").remove();
                        for(var i=0;i<stateList.length;i++){
                             var geo =(stateList[i]);
		                     var geos=[];
		                     geos=geo.split(":");
                            cityLi+='<li data-id="'+geos[1]+'">'+geos[0]+' <span class="xl_del_btn">'
                                    +"<#if security.hasEntityPermission("GEO", "_UPDATE", session)>"
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">编辑</a>'
                                    +"</#if>"
                                    +"<#if security.hasEntityPermission("GEO", "_UPDATE", session)>"
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">删除</a>'
                                    +"</#if>"
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(cityLi);
                        
                        $(".xl_third_ul").find('li:eq(0)').click();
                    }
            },
            error:function(){
                alert("操作失败")
           }
        })
	}
	
	
	// 将选择的记录id删除
    function delProductCategoryDataById(){
	  // 选中的项目
	  var productCategoryId =$("#ids").val(); 
	  if (productCategoryId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>delProductCategoryById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productCategoryId' : productCategoryId
	        },
	        success: function(data) {
	           document.location.href="<@ofbizUrl>ProductBrandType?lookupFlag=Y</@ofbizUrl>"; 
	        }
	    });
	  } 
	}
	
	// 将选择的记录删除的检查
    function delProductCategoryByIdForCheck(level){
	  // 选中的项目
	  var productCategoryId =$("#ids").val(); 
	  if (productCategoryId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>delProductCategoryByIdForCheck</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productCategoryId' : productCategoryId,
	             'productCategoryLevel':level
	        },
	        success: function(data) {
	           var checkFlg= data.checkFlg;
	           var errType= data.errType;
	           if(checkFlg=='N'){
	               if(data.errType=="Category"){
	                  //alert("aa");
	                  //设置提示弹出框内容
					  $('#modal_msg #modal_msg_body').html("该分类有下级分类，无法删除");
    				  $('#modal_msg').modal();
	               }else if(data.errType=="Product"){
	                  //alert("bb");
	                  //设置提示弹出框内容
					  $('#modal_msg #modal_msg_body').html("该分类有商品，无法删除");
    				  $('#modal_msg').modal();
	               }
	           }else{
	              //设置删除弹出框内容
				  $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				  $('#modal_confirm').modal('show');
	           }
	        }
	    });
	  } 
	}  
	
	    //添加省点击事件
        $('#state_btn').click(function(){
	     $('#countryForm').dpValidate({
			  clear: true
			});
	     $('#country_Modal').modal('show');
	    });
	     //添加省点提交按钮点击事件
	    $('#countryForm_btn').click(function(){
	        $('#countryForm').dpValidate({
			  clear: true
			});
			$('#countryForm').submit();
	    });
	     //添加省表单校验
       $('#countryForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "createGeo",
					type: "POST",
					data: $('#countryForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#country_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>RegionalManage</@ofbizUrl>?geoId=';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });

  
	    //添加市按钮点击事件
        $('#stateProvince_btn').click(function(){
          var geoName=$(".xl_first_ul").find('.xl_active').data('id');
        $('#stateProvinceForm').dpValidate({
			  clear: true
			});
        getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'stateProvinceForm_countryGeoId', 'stateProvinceForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName',geoName);
	    $('#stateProvince_Modal').modal('show');
	    });
	   	  //添加市点提交按钮点击事件
	    $('#stateForm_btn').click(function(){
	        $('#stateProvinceForm').dpValidate({
			  clear: true
			});
			$('#stateProvinceForm').submit();
	    });
	     //添加省表单校验
       $('#stateProvinceForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "createGeo",
					type: "POST",
					data: $('#stateProvinceForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#stateProvince_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>RegionalManage</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        }); 
	    
	    //添加区按钮点击事件
        $('#city_btn').click(function(){
			var geoName=$(".xl_first_ul").find('.xl_active').data('id');
         	getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'cityForm_countryGeoId', 'cityForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName');
         	getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'cityForm_stateProvinceGeoId', 'cityForm_cityGeoId', 'stateList', 'geoId', geoName);
            
            // 获取省份和城市 spj
           	$("#cityForm_stateProvinceGeoId option").each(function(i, data) {
           		if($(data).val() == geoName) {
           			$(data).attr("selected",true);
           		}
           	});
           	
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'cityForm_stateProvinceGeoId', 'cityForm_cityGeoId', 'stateList', 'geoId', geoName);
			var cityGeoId=$(".xl_second_ul").find('.xl_active').data('id');
           	$("#cityForm_cityGeoId option").each(function(i, data) {
           		if($(data).val() == cityGeoId) {
           			$(data).attr("selected",true);
           		}
           	});
            
       		$('#cityForm').dpValidate({
			  	clear: true
			});
	    	$('#city_Modal').modal('show');
	    });
	    
	    //添加省点提交按钮点击事件
	    $('#cityForm_btn').click(function(){
	        $('#cityForm').dpValidate({
			  clear: true
			});
			$('#cityForm').submit();
	    });
	     //添加省表单校验
       $('#cityForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "createGeo",
					type: "POST",
					data: $('#cityForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#city_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>RegionalManage</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
        
      //设置默认地址  
	$('#btn_address').click(function(){
	    //设置删除弹出框内容
		$('#defalut_confirm #modal_confirm_body').html("确认要把选中的地址设为默认地址吗？");
		$('#defalut_confirm').modal('show');
	});
	
	//设置默认地址按钮点击事件
	   $('#defalut_confirm #ok').click(function(e){
	   var stateProvinceGeoId=$(".xl_first_ul li.xl_active").data("id").trim()
	    cityGeoId=$(".xl_second_ul li.xl_active").data("id").trim()
	    countyGeoId=$(".xl_third_ul li.xl_active").data("id").trim();
	        $.ajax({
					url: "createDefaultAdd",
					type: "POST",
					data: {
					stateProvinceGeoId : stateProvinceGeoId,
					cityGeoId:cityGeoId,
					countyGeoId:countyGeoId
					},
					dataType : "json",
					success: function(data){
					   //设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>RegionalManage</@ofbizUrl>';
						})
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
						$('#modal_msg').modal();
					}
				});
	   });
</script>
		
