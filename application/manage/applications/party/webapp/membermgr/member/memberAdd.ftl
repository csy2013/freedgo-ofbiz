
<!-- begin 基本信息 -->
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">基本信息</h3>
    </div>
    <div class="box-body">
        <form class="form-horizontal" role="form" id="AddForm"  method="post" action="" >
          <div class="form-group m-b-xs row">
                     <div class="form-group col-sm-6" >
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>会员编号:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control  w-p60" id="partyId" name="partyId" value="${partyId}" readonly />
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                     <div class="form-group col-sm-6" data-type="required" data-mark="会员类型">
                            <label for="title" class="col-md-6 col-lg-6  control-label">会员类型:</label>
                            <div class="col-md-6 col-lg-6">
                               <select class="form-control w-p60" id="partyCategory" name="partyCategory">
                              <option value="MEMBER">个人会员</option>
                              <#--<option value="COMPANY">企业会员</option>-->
                              </select>
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
          
              <div id="COMPANY" hidden> <!-- 企业会员 new begin-->
                   <div class="form-group m-b-xs row">
                     <div class="form-group col-sm-6" data-type="required" data-mark="企业名称" >
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>企业名称:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control  w-p70 " id="company_name" name="company_name">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                     <div class="form-group col-sm-6" data-type="required" data-mark="联系电话">
                            <label for="title" class="col-md-6 col-lg-6  control-label">联系电话:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control  w-p60" id="company_mobile" name="company_mobile">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
         
          <div class="form-group m-b-xs">
               <div class="row">
                     <div class="form-group" data-type="required" data-mark="企业地址">
                           <label for="title" class="col-md-3 col-lg-3  control-label">企业地址:</label>
                            <div class="col-md-3 col-lg-3">
                               <input type="text" class="form-control  w-p100" id="description" name="description">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
             </div>
          </div>
          
          <div class="form-group m-b-xs row">
                   <!--
                     <div class="form-group col-sm-6" data-type="required" data-mark="登录账号">
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>登录账号:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control" id="company_userLoginId" name="company_userLoginId" onblur="Validate1(this.value)">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                     <div class="form-group col-sm-6" data-type="min"  data-number="6" data-mark="登录密码">
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>登录密码:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="password" class="form-control " id="company_currentPassword" name="company_currentPassword" >
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                     -->
                     <div class="form-group col-sm-6" data-type="required" data-mark="登录账号">
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>登录账号:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control w-p70" id="company_userLoginId" name="company_userLoginId" onblur="Validate1(this.value)">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                     <div class="form-group col-sm-6" data-type="min"  data-number="6" data-mark="登录密码">
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>登录密码:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="password" class="form-control w-p70" id="company_currentPassword" name="company_currentPassword">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
          
          
          <div class="form-group m-b-xs">
               <div class="row">
                     <div class="form-group" data-type="required" data-mark="会员昵称">
                           <label for="title" class="col-md-3 col-lg-3  control-label">会员昵称:</label>
                            <div class="col-md-3 col-lg-3">
                               <input type="text" class="form-control  w-p80" id="company_nickname" name="company_nickname">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
             </div>
          </div>
       </div><!--   企业会员 new div end-->
          
          
          <div  id="MEMBER"> <!-- 个人会员 new begin-->
          <div class="form-group m-b-xs row">
                     <div class="form-group col-sm-6" >
                            <label for="title" class="col-md-6 col-lg-6  control-label">真实姓名:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control dp-vd w-p70 " id="name" name="name">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                     <div class="form-group col-sm-6" data-type="required" data-mark="性别">
                            <label for="title" class="col-md-6 col-lg-6  control-label">性别:</label>
                            <div class="col-md-6 col-lg-6">
                              <select class="form-control w-p40" id="gender" name="gender">
                              <option value="M">男</option>
                              <option value="F">女</option>
                              </select>
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
          
          <div class="form-group m-b-xs row">
                     <div class="form-group col-sm-6" data-type="format" data-reg="/^1(?:3[0-9]|5[0-35-9]|7[6-8]|8[0-9])\d{8}$/">
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>手机号:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control dp-vd w-p70 " id="mobile" name="mobile">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                     <div class="form-group col-sm-6" data-type="required" data-mark="会员昵称">
                            <label for="title" class="col-md-6 col-lg-6  control-label">会员昵称:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control  w-p70 " id="nickname" name="nickname">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
          
                  <div class="form-group m-b-xs row">
                     <div class="form-group col-sm-6" data-type="required" data-mark="登录账号">
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>登录账号:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="text" class="form-control dp-vd  w-p70" id="userLoginId" name="userLoginId" onblur="Validate(this.value)">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                     <div class="form-group col-sm-6" data-type="min"  data-number="6" data-mark="登录密码">
                            <label for="title" class="col-md-6 col-lg-6  control-label"><i class="required-mark">*</i>登录密码:</label>
                            <div class="col-md-6 col-lg-6">
                               <input type="password" class="form-control dp-vd w-p70" id="currentPassword" name="currentPassword">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
       </div><!-- new div end-->
          
     </form>
    </div>
</div><!-- end 基本信息 -->

<div >
    <form method="post" action="<@ofbizUrl>editcontactmechforcustomer</@ofbizUrl>" class="am-form am-form-horizontal" name="createcontactmechform">
        <input type="hidden" name="partyId" value="${partyId}" />
        <input type="hidden" name="preContactMechTypeId" value="POSTAL_ADDRESS" />
        <input type="hidden" name="isFirst" value="${shippingContactMechList?has_content?string('0','1')}" /> <!-- 是否为第一条地址 -->
    </form>
</div>
<!-- begin 收货地址 -->
<#--<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">收货地址</h3>
        <a id="btn_add" class="btn btn-primary">创建</a>
    </div>
    <div class="box-body">
        <form class="form-horizontal" role="form" id="addressForms">
        
        </form>
    </div>
</div>--><!-- end 收货地址 -->
 <div class="form-group" style="TEXT-ALIGN: center;">
        <button id="btn_save" type="button" class="btn btn-primary m-l-20">保存</button>
        <button  type="button" class="btn btn-primary m-l-20" onclick="back()" data-dismiss="modal">取消</button>
      </div>


<form name="partyDeleteContact" method="post" action="<@ofbizUrl>deleteContactMechforcustomer</@ofbizUrl>">
    <input name="partyId" value="${partyId}" type="hidden"/>
    <input name="contactMechId" id="contactMechId" type="hidden"/>
    <input name="isDefault" id="isDefault" type="hidden"/>
</form>


 <!--新增收货地址 start-->
<div class="modal fade" id="add_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">收货地址编辑/新增</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="MemberAddressForm" action="<@ofbizUrl>createTag</@ofbizUrl>" method="post">
             <!-- 收货人 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="收货人">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>收货人:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="toName" name="toName">
                      <input type="hidden" class="form-control dp-vd w-p50" id="status" name="status">
                      <input type="hidden" class="form-control dp-vd w-p50" id="sque" name="sque">
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div><!-- 收货人 end-->
          
          <!-- 联系方式 start-->
          <div class="row">
                     <div class="form-group" data-type="format" data-reg="/^1(?:3[0-9]|5[0-35-9]|7[6-8]|8[0-9])\d{8}$/" data-mark="联系方式">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>联系方式:</label>
                            <div class="col-sm-10" >
                               <input type="text" class="form-control dp-vd w-p50" id="mobilePhone" name="mobilePhone">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
           <!-- 联系方式 end-->
           
           <!--默认国家- 省start-->
               <div class="row">
                <div class="form-group" data-type="required" data-mark="省">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>省:</label>
                    <div class="col-sm-10">
                    	<div style="display:none;">
	                    	<select name="countryGeoId" id="MemberAddressForm_countryGeoId" >
		                    	${screens.render("component://common/widget/CommonScreens.xml#countries")}
			                    <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
		                        <option selected="selected" value="${defaultCountryGeoId}">
		                        <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
		                        ${countryGeo.get("geoName",locale)}
		                        </option>
		                    </select>
	                    </div>
			            <div class="col-sm-3" style="padding-left: 0px;">
                            <select class="form-control" name="stateProvinceGeoId" id="MemberAddressForm_stateProvinceGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                    </div>
                </div>
            </div> <!--默认国家- 省end-->
            
            <!--市start-->
               <div class="row">
                <div class="form-group" data-type="required" data-mark="市">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>市:</label>
                    <div class="col-sm-10">
			            <div class="col-sm-3" style="padding-left: 0px;">
                            <select class="form-control" name="stateProvinceGeoId" id="MemberAddressForm_cityGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                    </div>
                </div>
            </div> <!-- 市end-->
            
            <!-- 区start-->
               <div class="row">
                <div class="form-group" data-type="required" data-mark="区">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>区:</label>
                    <div class="col-sm-10">
			            <div class="col-sm-3" style="padding-left: 0px;">
                            <select class="form-control" name="stateProvinceGeoId" id="MemberAddressForm_countyGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                    </div>
                </div>
            </div> <!-- 区end-->
          
         <!-- 详细地址start-->
          <div class="row">
          <div class="form-group" data-type="required" data-mark="详细地址">
                    <label for="title" class="control-label col-sm-2"> <i class="required-mark">*</i>详细地址:</label>
                    <div class="col-sm-10">
                        <textarea class="form-control dp-vd w-p80" id="address1" name="address1"></textarea>
                    <p class="dp-error-msg"></p>
                    </div>
         </div>
         </div>
          <!-- 详细地址end-->
          
         <!-- 详细地址start-->
          <div class="row">
          <div class="form-group" data-type="required" data-mark="默认地址">
                    <label for="title" class="control-label col-sm-2"> <i class="required-mark">*</i>默认地址:</label>
                    <div class="col-sm-10">
                     <input name="isDefault" value="1" type="radio"   checked="checked"/>&nbsp;&nbsp;是
                     <input name="isDefault" value="0" type="radio"  />&nbsp;&nbsp;否
                    </div>
         </div>
         </div>
          <!-- 详细地址end-->
          <div class="modal-footer">
		    <button type="button"  id="address_btn" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--新增收货地址弹出框 end-->
   
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

<!--编辑收货地址 start-->
<div class="modal fade" id="edit_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">收货地址编辑/新增</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="editMemberAddressForm" action="<@ofbizUrl>updatePostalAddressforcustomer</@ofbizUrl>" method="post">
             <input type="hidden" class="form-control dp-vd w-p50" id="contactMechId" name="contactMechId">
             <input type="hidden" class="form-control dp-vd w-p50" id="contactMechTypeId" name="contactMechTypeId" value="POSTAL_ADDRESS">
             <input type="hidden" class="form-control dp-vd w-p50" id="partyId" name="partyId">
             <!-- 收货人 start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark="收货人">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>收货人:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="toName" name="toName">
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div><!-- 收货人 end-->
          
          <!-- 联系方式 start-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="联系方式">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>联系方式:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="mobilePhone" name="mobilePhone">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
           <!-- 联系方式 end-->
           
           <!--默认国家- 省start-->
               <div class="row">
                <div class="form-group" data-type="required" data-mark="省">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>省:</label>
                    <div class="col-sm-10">
                    	<div style="display:none;">
	                    	<select name="countryGeoId" id="editMemberAddressForm_countryGeoId" >
		                    	${screens.render("component://common/widget/CommonScreens.xml#countries")}
			                    <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
		                        <option selected="selected" value="${defaultCountryGeoId}">
		                        <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
		                        ${countryGeo.get("geoName",locale)}
		                        </option>
		                    </select>
	                    </div>
			            <div class="col-sm-3" style="padding-left: 0px;">
                            <select class="form-control" name="stateProvinceGeoId" id="editMemberAddressForm_stateProvinceGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                    </div>
                </div>
            </div> <!--默认国家- 省end-->
            
            <!--市start-->
               <div class="row">
                <div class="form-group" data-type="required" data-mark="市">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>市:</label>
                    <div class="col-sm-10">
			            <div class="col-sm-3" style="padding-left: 0px;">
                            <select class="form-control" name="city" id="editMemberAddressForm_cityGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                    </div>
                </div>
            </div> <!-- 市end-->
            
            <!-- 区start-->
               <div class="row">
                <div class="form-group" data-type="required" data-mark="区">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>区:</label>
                    <div class="col-sm-10">
			            <div class="col-sm-3" style="padding-left: 0px;">
                            <select class="form-control" name="countyGeoId" id="editMemberAddressForm_countyGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                    </div>
                </div>
            </div> <!-- 区end-->
            
         <!-- 详细地址start-->
          <div class="row">
          <div class="form-group" data-type="required" data-mark="详细地址">
                    <label for="title" class="control-label col-sm-2"> <i class="required-mark">*</i>详细地址:</label>
                    <div class="col-sm-10">
                        <textarea class="form-control w-p80" id="address1" name="address1"></textarea>
                    </div>
         </div>
         </div>
          <!-- 详细地址end-->
          
         <!-- 详细地址start-->
          <div class="row">
          <div class="form-group" data-type="required" data-mark="默认地址">
                    <label for="title" class="control-label col-sm-2"> <i class="required-mark">*</i>默认地址:</label>
                    <div class="col-sm-10">
                     <input name="isDefault" value="1" type="radio" checked="checked"/>&nbsp;&nbsp;是
                     <input name="isDefault" value="0" type="radio" />&nbsp;&nbsp;否
                    </div>
         </div>
         </div>
          <!-- 详细地址end-->
          <div class="modal-footer">
		    <button type="button"  id="Member_btn" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--编辑收货地址 end-->
   
   
   <!-- 驳回原因弹出框start -->
	<div id="modal_Fullconfirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title" id="modal_confirm_title">操作提示</h4>
	      </div>
	      <div class="modal-body">
	        <h4 id="modal_confirm_body"></h4>
	      </div>
	      <div class="modal-footer" style="text-align: center;">
	      	 <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">确定</button>
	      </div>
	    </div>
	  </div>
	</div><!-- ${uiLabelMap.BrandDel}确认弹出框end -->
<script>
     var  flag=true;
    function deleteContactMechforcustomer(contactMechId){
        $("#contactMechId").val(contactMechId);
        var isDefault = "";
        if ($("#"+contactMechId).attr("checked") == "checked"){
            isDefault = "1";
        }
        $("#isDefault").val(isDefault);
        this.document.partyDeleteContact.submit();
    }
    $(function(){
	     $('#btn_add').click(function(){
         $('#MemberAddressForm').dpValidate({
			  clear: true
			});
        $("#add_Modal #toName").val('');
        $("#add_Modal #mobilePhone").val('');
        $("#add_Modal #address1").val('');
        var size=$('#addressForms .gss_delete').size();
        if(size==0){
        $('#add_Modal :radio[value=1]').prop("checked",true);
        $('#add_Modal :radio').prop("disabled",true);
        }else{
        $('#add_Modal :radio').prop("disabled",false);
        }
	    $('#add_Modal').modal('show');
	    });
      //会员类型一的选项切换事件
     $('#partyCategory').on('change',function(){
    	switch ($(this).val()) {
    		case 'MEMBER':
	    		{
	    		     $('#AddForm').dpValidate({
			         clear: true
			          });
	    			$('#COMPANY').hide();
	    			$('#COMPANY #company_name').val(''); 
	    			$('#COMPANY #company_nickname').val('');
	    			$('#COMPANY #company_mobile').val('');
	    			$('#COMPANY #description').val('');
	    			$('#COMPANY #company_userLoginId').val('');
	    			$('#COMPANY #company_currentPassword').val('');
	    			$('#COMPANY #company_name').removeClass("dp-vd"); 
	    			$('#COMPANY #company_userLoginId').removeClass("dp-vd");
	    			$('#COMPANY #company_currentPassword').removeClass("dp-vd");
	    		    $('#MEMBER #currentPassword').addClass("dp-vd");
	    			$('#MEMBER #userLoginId').addClass("dp-vd");
	    			$('#MEMBER #mobile').addClass("dp-vd");
	    			$('#MEMBER').show();
	    		}
    			break;
    		case 'COMPANY':
	    		{
	    		   $('#AddForm').dpValidate({
			         clear: true
			          });
	    			$('#COMPANY').show();
	    			$('#MEMBER').hide();
	    			$('#MEMBER #name').val('');
	    			$('#MEMBER #nickname').val('');
	    			$('#MEMBER #mobile').val('');
	    			$('#MEMBER #userLoginId').val('');
	    			$("#MEMBER #gender").find("option[value='M']").attr("selected",true);
	    			$('#MEMBER #currentPassword').val('');
	    			$('#MEMBER #currentPassword').removeClass("dp-vd");
	    			$('#MEMBER #userLoginId').removeClass("dp-vd");
	    			$('#MEMBER #mobile').removeClass("dp-vd");
	    			$('#COMPANY #company_name').addClass("dp-vd"); 
	    			$('#COMPANY #company_userLoginId').addClass("dp-vd");
	    			$('#COMPANY #company_currentPassword').addClass("dp-vd");
	    		}
    			break;
    	}
    });
    
     //地址保存按钮
    $('#address_btn').click(function(){
         $('#MemberAddressForm').dpValidate({
			  clear: true
			});
    	$('#MemberAddressForm').submit();
    });
    
       //地址新增表单校验
       $('#MemberAddressForm').dpValidate({
        validate: true,
        callback: function(){
        var toname=$('#MemberAddressForm #toName').val().trim()
    	    mobilePhone=$('#MemberAddressForm #mobilePhone').val()
    	    stateProvinceGeoId=$('#MemberAddressForm #MemberAddressForm_stateProvinceGeoId option:selected').text()
    	    stateProvinceGeoIds=$('#MemberAddressForm #MemberAddressForm_stateProvinceGeoId').val()
    	    cityGeoId=$(' #MemberAddressForm #MemberAddressForm_cityGeoId option:selected').text()
    	    cityGeoIds=$(' #MemberAddressForm #MemberAddressForm_cityGeoId').val()
    	    countyGeoId=$(' #MemberAddressForm #MemberAddressForm_countyGeoId option:selected').text()
    	    countyGeoIds=$(' #MemberAddressForm #MemberAddressForm_countyGeoId').val()
    	    address1=$('#MemberAddressForm #address1').val()
    	    isDefault=$('#MemberAddressForm :radio:checked').val()
    	    sques=$('#MemberAddressForm #sque').val()
    	    status=$('#MemberAddressForm #status').val();
    	    var sque=  $('#addressForms .gss_delete').size()+1;
    	    var address= stateProvinceGeoId +' '+cityGeoId+' '+countyGeoId+' '+address1;
    	     if(isDefault=='1'){
    	        $('#addressForms :radio').each(function(){
    	        $(this).prop("checked",false);
    	        $(this).parents('.gss_delete').find('input[name=isDefault]').val('0');
    	        })
    	       }
    	    if(status&&status=='update'){
    	      $('#addressForms .gss_delete').each(function(){
    	      var sq= $(this).find('input[name=sque]').val();
    	       if(sq==sques){
    	       $(this).find('input[name=sque]').val(sq);
    	       $(this).find('input[name=toname]').val(toname);
    	       $(this).find('input[name=mobilePhone]').val(mobilePhone);
    	       $(this).find('input[name=stateProvinceGeoIds]').val(stateProvinceGeoIds);
    	       $(this).find('input[name=cityGeoIds]').val(cityGeoIds);
    	       $(this).find('input[name=countyGeoIds]').val(countyGeoIds);
    	       $(this).find('input[name=address1]').val(address1);
    	       $(this).find('input[name=isDefault]').val(isDefault);
    	       $(this).find('.toname').text(toname);
    	       $(this).find('.mobilePhone').text(mobilePhone);
    	       $(this).find('.geo').text(address);
    	       }
    	      });
    	    }else{
    	      
    	      
			  var tr='<div class="gss_delete">'
			        +'<div class="gss">'
			        +'<input type="hidden" value ="'+ toname+'" name="toname"/>'
			        +'<input type="hidden" value ="'+ sque+'" name="sque"/>'
			        +'<input type="hidden" value ="'+ mobilePhone+'" name="mobilePhone"/>'
			        +'<input type="hidden" value ="'+ stateProvinceGeoIds+'" name="stateProvinceGeoId"/>'
			        +'<input type="hidden" value ="'+ cityGeoIds+'" name="cityGeoId"/>'
			        +'<input type="hidden" value ="'+ countyGeoIds+'" name="countyGeoId"/>'
			        +'<input type="hidden" value ="'+ address1+'" name="address1"/>'
			        +'<input type="hidden" value ="'+isDefault+'" name="isDefault"/>'
			        +'</div>'
			        +'<div class="cut-off-rule bg-gray"></div>'
			       +'<div class="form-group m-b-xs">'
			       +'<label class="col-md-3 col-lg-3  control-label" >收货人：</label>'
			       +'<div class="col-md-3 col-lg-3 ">'
			       +'<p class="form-control-static toname">'
			       + toname +'</p>'
			       +'</div>'
			       +'<label class="col-md-3 col-lg-3  control-label" >联系方式：</label>'
			       +' <div class="col-md-3 col-lg-3 ">'
			       +'<p class="form-control-static mobilePhone">'+
			       mobilePhone+'</p>'
			       +'</div>'
			       +'</div>'
			       +'<div class="form-group m-b-xs">'
			       +'<label class="col-md-3 col-lg-3  control-label" >地址：</label>'
			       +'<div class="col-md-3 col-lg-3 ">'
			       +'<p class="form-control-static geo">'
			       + stateProvinceGeoId +' '+cityGeoId+' '+countyGeoId+' '+address1
			       +'</p>'
			       +'</div>'
			       +'<div class="col-md-1 col-lg-1 ">设为默认</div>'
			       +'<div class="col-md-2 col-lg-2 ">';
			    if(isDefault=='1'){
			       tr=tr
			       +' <input type="radio" value = "" name="setDefault" id="" checked = "checked" />'
			       +'</div>';
			    }else{
			       tr=tr
			       +' <input type="radio" value = "" name="setDefault" id=""  />'
			       +'</div>';
			    }
			    tr=tr
			    +'<div class="col-md-3 col-lg-3 "><p class="form-control-static">'
			    +'<a  class="btn btn-primary edit_Address "  href="javascript:editAddress(\''+toname+'\',\''+mobilePhone+'\',\''+stateProvinceGeoIds+'\',\''+cityGeoIds+'\',\''+countyGeoIds+'\',\''+address1+'\',\''+isDefault+'\',\''+sque+'\');">更新</a>'
			    +'&nbsp;'
			    +'<a  class="btn btn-primary js-delete"  href="javascript:void(0)" >删除</a>'
			    +'</p>'
			    +'</div>'
			    +'</div>'
			      $('#addressForms').append(tr); 
    	    }
    	 $('#add_Modal').modal('hide');
          }
        });
         
         //保存按钮点击事件
	    $('#btn_save').click(function(){
	        $('#AddForm').dpValidate({
			  clear: true
			});
			if(flag){
			$('#AddForm').submit()
			}
	    });
	    //保存按钮表单校验
       $('#AddForm').dpValidate({
        console: true,
        validate: true,
        callback: function(){
            
            var all_address="";
            $('#addressForms .gss').each(function(){
            var Arr = $(this);
            for(var i=0;i<Arr.length;i++){
             var toname=Arr.find("input[name=toname]").val(); //收货人
             var mobilePhone=Arr.find("input[name=mobilePhone]").val();//手机号
             var stateProvinceGeoId=Arr.find("input[name=stateProvinceGeoId]").val();//省
             var cityGeoId=Arr.find("input[name=cityGeoId]").val();//市
             var countyGeoId=Arr.find("input[name=countyGeoId]").val();//区
             var address1=Arr.find("input[name=address1]").val();//详细地址
             var isDefault=Arr.find("input[name=isDefault]").val();//默认地址
              all_address+=toname+":"+mobilePhone+":"+stateProvinceGeoId+":"+cityGeoId+":"+countyGeoId+":"+address1+":"+isDefault+",";
             }
            });
           $.ajax({
					url: "createCustomerforIco",
					type: "POST",
					data: $('#AddForm').serialize() + "&allAddress="+all_address,
					dataType : "json",
					success: function(data){
					    if(data.status){
					    //设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html(data.info);
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>partyMasterManage</@ofbizUrl>';
						});
					    }else{
					     $('#modal_Fullconfirm #modal_confirm_body').html(data.info);
	  	                  $('#modal_Fullconfirm').modal('show');
					    }
						
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#add_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
  });
  
     function Validate(userLoginId){
       $.ajax({
                url: "findUserloginIdById",
				type: "POST",
				data: {
				      userLoginId : userLoginId
				      },
				dataType : "json",
				success: function(data){
				 if(!data.status){
				 $('#userLoginId').siblings(".dp-error-msg").text(data.info)
				  flag=false;
				 }else{
				 flag=true;
				 $('#userLoginId').siblings(".dp-error-msg").text('');
				 }
			}
        });
    }
     function Validate1(userLoginId){
       $.ajax({
                url: "findUserloginIdById",
				type: "POST",
				data: {
				      userLoginId : userLoginId
				      },
				dataType : "json",
				success: function(data){
				 if(!data.status){
				 $('#company_userLoginId').siblings(".dp-error-msg").text(data.info)
				  flag=false;
				 }else{
				 flag=true;
				 $('#company_userLoginId').siblings(".dp-error-msg").text('');
				 }
			}
        });
    }
    
    function editAddress(toName,mobilePhone,stateProvinceGeoId,city,countyGeoId,address1,isDefault,sque){
         $('#MemberAddressForm #toName').val(toName);
         $('#MemberAddressForm #mobilePhone').val(mobilePhone);
         $('#MemberAddressForm #address1').val(address1);
         $('#MemberAddressForm #address1').val(address1);
         $('#MemberAddressForm #status').val('update');
         $('#MemberAddressForm #sque').val(sque);
         $('#MemberAddressForm :radio[value='+isDefault+']').prop("checked",true);
         if ($('#MemberAddressForm').length) {
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName',stateProvinceGeoId);
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'MemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName',city);
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_cityGeoId', 'MemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName',countyGeoId);
            //国家
             $("#editMemberAddressForm_countryGeoId").change(function (e, data) {
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName');
            });
             $("#MemberAddressForm_stateProvinceGeoId").change(function (e, data) {
                //省
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'MemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName');
            });
             $("#editMemberAddressForm_cityGeoId").change(function (e, data) {
              /* 市*/
               getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_cityGeoId', 'MemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName');
            });
        }
         $('#add_Modal').modal('show');
    }
        
         //删除按钮事件
  	    $(document).on('click','.js-delete',function(){
  	       $(this).parents('.gss_delete').remove();
  	       var size=$('#addressForms .gss_delete').size();
  	       if(size==1){
  	        $('#addressForms :radio').each(function(){
    	        $(this).prop("checked",true);
    	        $(this).parents('.gss_delete').find('input[name=isDefault]').val('1');
    	     })
  	       }
      })
     
     
     function back(){
    	window.location.href = '<@ofbizUrl>partyMasterManage</@ofbizUrl>';
    }
</script>