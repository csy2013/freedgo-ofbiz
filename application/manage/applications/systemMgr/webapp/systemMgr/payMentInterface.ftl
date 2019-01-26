<style>
	.s-zdy{
		border: 1px solid #ccc;
		margin-top: 15px;
	}
	.s-bj-p{
		text-align: right;
	}
	.s-bj{
	
		margin-right: 15px;
	}
	.s-zdy p{ line-height: 24px; }
	.s-error,.s-message{margin-left: 5px;}
	.s-img{
		max-height: 230px;
		display: block;
	}
	.s-img-div{
		margin:0 10px 10px 10px; 
	}
	</style>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<!-- Main content -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">${uiLabelMap.PaymentInterface}<#if payMentList?has_content>(共${payMentList.size()?default('0')}个)</#if></h3>
        </div>
        <div class="box-body">			
            <!-- 分割线 start-->
            <div class="cut-off-rule bg-gray"></div>
			<!-- 分割线  start-->
            
            <div class="row">
            <#if AliPayList?has_content> 
            <#list AliPayList as payList>
            <!-- 循环体 start-->
            <div class="col-md-4 col-lg-3">
            <div class="s-zdy">
             <!--是否有编辑的权限-->
            <#if security.hasEntityPermission("PAYMENT_INTERFANCE", "_UPDATE", session)>
            <p class="s-bj-p"><a href="javascript:;" class="s-bj edit-info" id="AliPay">${uiLabelMap.Edit}</a></p>
            </#if>
            <input type="hidden"  name="paymentGatewayConfigTypeId" id="AliPayId" value="${payList.paymentGatewayConfigId?if_exists}"/>
            <p><span class="s-error">${payList.paymentName?if_exists}</span></p>
            <div class="s-img-div">
             <p>
             <div class="dp-tables_btn">
            <#assign src='/content/control/stream?contentId='>
            <#if payList.imgContentId?has_content>
            <#assign imgsrc = src +payList.imgContentId/>
            <img height="50" alt="" src="${imgsrc}" id="img" style="height:100px;width:100px;">
            </#if>
             </div>
             </p>
            </div>
            <p><span class="s-message">${payList.description?if_exists}</span></p>
            </div>
            </div><!-- 循环体 end-->
            </#list>
            </#if>
            
            <#if WeixinPayList?has_content> 
            <#list WeixinPayList as weixinList>
            <!-- 循环体 start-->
            <div class="col-md-4 col-lg-3">
            <div class="s-zdy">
            <!--是否有编辑的权限-->
            <#if security.hasEntityPermission("PAYMENT_INTERFANCE", "_UPDATE", session)>
            <p class="s-bj-p"><a href="javascript:;" class="s-bj edit-info" id="weixinPay">${uiLabelMap.Edit}</a></p>
            </#if>
            <input type="hidden"  id="weixinPayId" value="${weixinList.paymentGatewayConfigId?if_exists}"/>
            <p><span class="s-error">${weixinList.paymentName?if_exists}</span></p>
            <div class="s-img-div">
             <p>
             <div class="dp-tables_btn">
           <#assign src='/content/control/stream?contentId='>
            <#if weixinList.imgContentId?has_content>
            <#assign imgsrc = src +weixinList.imgContentId/>
            <img height="50" alt="" src="${imgsrc}" id="img" style="height:100px;width:100px;">
            </#if>
                </div>
             </p>
            </div>
            <p><span class="s-message">${weixinList.description?if_exists}</span></p>
            </div>
            </div><!-- 循环体 end-->
            </#list>
            </#if>
            
            <#if unionPayList?has_content> 
            <#list unionPayList as unionList>
            <!-- 循环体 start-->
            <div class="col-md-4 col-lg-3">
            <div class="s-zdy">
            <!--是否有编辑的权限-->
            <#if security.hasEntityPermission("PAYMENT_INTERFANCE", "_UPDATE", session)>
            <p class="s-bj-p"><a href="javascript:;" class="s-bj edit-info" id="unionPay">${uiLabelMap.Edit}</a></p>
            </#if>
            <input type="hidden"  name="paymentGatewayConfigTypeId" id="unionPayId"value="${unionList.paymentGatewayConfigId?if_exists}"/>
            <p><span class="s-error">${unionList.paymentName?if_exists}</span></p>
            <div class="s-img-div">
             <p>
             <div class="dp-tables_btn">
           <#assign src='/content/control/stream?contentId='>
            <#if unionList.imgContentId?has_content>
            <#assign imgsrc = src +unionList.imgContentId/>
            <img height="50" alt="" src="${imgsrc}" id="img" style="height:100px;width:100px;">
            </#if>
                </div>
             </p>
            </div>
            <p><span class="s-message">${unionList.description?if_exists}</span></p>
            </div>
            </div><!-- 循环体 end-->
            </#list>
            </#if>
            </div><!-- 表格区域end -->
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

   
 <!--支付宝编辑弹出框 -->
<div class="modal fade" id="edit_AliPay" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.EditinterfaceManage}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="AliPayForm" action="" method="post">
          
            <div class="row">
                     <div class="form-group " data-type="required" data-mark="${uiLabelMap.paymentName}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.paymentName} :</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="paymentName" name="paymentName" value="">
                               <input type="hidden" class="form-control dp-vd w-p50" id="paymentGatewayConfigId" name="paymentGatewayConfigId" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group " data-type="required" data-mark="Api-Key">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>Api-Key:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="apiKey" name="apiKey" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group " data-type="required" data-mark="Secret-key">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>Secret-key:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="secretKey" name="secretKey" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group " data-type="required" data-mark="${uiLabelMap.payAccount}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.payAccount}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="payAccount" name="payAccount" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
                 
              	<div class="row">
                     <div class="form-group " data-type="required" data-mark="${uiLabelMap.payUrl}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.payUrl}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="payUrl" name="payUrl" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.backUrl}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.backUrl}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="backUrl" name="backUrl" value="">
                                  <p class="dp-error-msg "style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group" data-type="required" data-mark="支付类型">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.payType}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="payType" name="payType" value="" readonly>
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group" >
                           <label for="title" class="col-sm-3 control-label">${uiLabelMap.mobileBindUrl}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="mobileBindUrl" name="mobileBindUrl" value="">
                               <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
              <div class="row">
			  <div class="form-group">
                <label for="recipient-name" class="col-sm-3 control-label">${uiLabelMap.payDescription}:</label>
                <div class="col-sm-16 w-p70">
                             <textarea id="description"  class="w-p60"name="description"  value="" rows="3" cols="20">
                             </textarea>
                </div>     
              </div>
              </div>
              <div class="row">
                <div class="form-group" >
                     <label for="title" class="col-sm-3 control-label">${uiLabelMap.payImg}:</label>
                     <div class="col-sm-16">
                     <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                     <input type="hidden" class="form-control dp-vd w-p50" id="contentId" name="contentId">
                     <input style="margin-left:5px;" type="button" id="" name="uploadedFile"   onclick="imageManage()" value="${uiLabelMap.chooseImg}"/>
                     </div>
                </div>
             </div>
			  <div class="row">
			  <div class="form-group">
                <label for="recipient-name" class="col-sm-3 control-label">${uiLabelMap.payProblem}:</label>
                <div class="col-sm-16 w-p70">
                <button type="button"  id="aliPay_button" class="btn btn-primary">${uiLabelMap.viewAndModify}</button>
                </div>     
              </div>
              </div>
            <div class="row">
            <div class="form-group" >
                    <label for="message-text" class="control-label col-sm-3">${uiLabelMap.isDefault}:</label>
                      <div class="radio">
                        <label>
                          <input type="radio" name="isDefault" value="Y" class="isDefault-Y" id="isDefault-Y" checked="checked">
                          ${uiLabelMap.Y}
                        </label>
                         <label>
                          <input type="radio" name="isDefault" value="N"  id="isDefault-N" class="isDefault-N">
                          ${uiLabelMap.N}
                        </label>
                      </div>
                    </div>  
             </div>  
             <div class="row">
        <div class="form-group">
                    <label for="message-text" class="control-label col-sm-3">是否启用:</label>
                      <div class="radio">
                        <label>
                          <input type="radio" name="isEnabled" value="Y" class="isEnabled-Y"  id="isEnabled-Y" checked="checked">
                          ${uiLabelMap.Y}
                        </label>
                         <label>
                          <input type="radio" name="isEnabled" value="N" class="isEnabled-N" id="isEnabled-N">
                          ${uiLabelMap.N}
                        </label>
                      </div>
                    </div>
                    </div>
          <div class="modal-footer" style="text-align:center;">
		    <button type="button"  id="AliPay_btn" class="btn btn-primary">${uiLabelMap.Save}</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--支付宝编辑弹出框 end-->

 
 <!--微信编辑弹出框 end-->
<div class="modal fade" id="edit_WeiXin" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.EditinterfaceManage}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="WeixinForm" action="" method="post">
          
            <div class="row">
                     <div class="form-group " data-type="required" data-mark="${uiLabelMap.paymentName}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.paymentName}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="paymentName" name="paymentName" value="">
                               <input type="hidden" class="form-control dp-vd w-p50" id="paymentGatewayConfigId" name="paymentGatewayConfigId" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group " data-type="required" data-mark="${uiLabelMap.publicId}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.publicId}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="apiKey" name="apiKey" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group " data-type="required" data-mark="App-Key">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>App-Key:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="secretKey" name="secretKey" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group " data-type="required" data-mark="${uiLabelMap.partner}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.partner}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="partner" name="partner" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
                 
              	<div class="row">
                     <div class="form-group " data-type="required" data-mark="${uiLabelMap.partnerKey}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.partnerKey}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="partnerKey" name="partnerKey" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.notifyUrl}">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.notifyUrl}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="notifyUrl" name="notifyUrl" value="">
                                  <p class="dp-error-msg" style="margin-left:160px;"></p>
                            </div>
                     </div>
                 </div>
              	<div class="row">
                     <div class="form-group" data-type="required" data-mark="支付类型">
                           <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>${uiLabelMap.payType}:</label>
                            <div class="col-sm-16">
                               <input type="text" class="form-control dp-vd w-p50" id="payType" name="payType" value="" readonly>
                                  <p class="dp-error-msg" ></p>
                            </div>
                     </div>
                 </div>
              <div class="row">
			  <div class="form-group">
                <label for="recipient-name" class="col-sm-3 control-label">${uiLabelMap.payDescription}:</label>
                <div class="col-sm-16 w-p70">
                             <textarea id="description"  class="w-p60"name="description"  value="" rows="3" cols="20" >
                             </textarea>
                </div>     
              </div>
              </div>
                <div class="row">
                <div class="form-group" >
                     <label for="title" class="col-sm-3 control-label">${uiLabelMap.payImg}:</label>
                     <div class="col-sm-16">
                     <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                     <input style="margin-left:5px;" type="button" id="" name="uploadedFile"  onclick="imageManage()" value="${uiLabelMap.chooseImg}"/>
                     <input type="hidden" class="form-control dp-vd w-p50" id="contentId" name="contentId">
                     </div>
                </div>
             </div>
             
			  <div class="row">
			  <div class="form-group">
                <label for="recipient-name" class="col-sm-3 control-label">${uiLabelMap.payProblem}:</label>
                <div class="col-sm-16 w-p70">
                <button type="button"  id="weixin_button" class="btn btn-primary">${uiLabelMap.payProblem}</button>
                </div>     
              </div>
              </div>
                         <div class="row">
            <div class="form-group" >
                    <label for="message-text" class="control-label col-sm-3">是否设为默认:</label>
                      <div class="radio">
                        <label>
                          <input type="radio"id="isDefault-Y" name="isDefault" value="Y" class="js-Y" checked="checked">
                          ${uiLabelMap.Y}
                        </label>
                         <label>
                          <input type="radio"  id="isDefault-N"name="isDefault" value="N" class="js-N">
                          ${uiLabelMap.N}
                        </label>
                      </div>
                    </div>  
             </div>  
             <div class="row">
        <div class="form-group">
                    <label for="message-text" class="control-label col-sm-3">是否启用:</label>
                      <div class="radio">
                        <label>
                          <input type="radio" id="isEnabled-Y"name="isEnabled" value="Y" class="js-Y" checked="checked">
                          ${uiLabelMap.Y}
                        </label>
                         <label>
                          <input type="radio" id="isEnabled-N" name="isEnabled" value="N" class="js-N">
                          ${uiLabelMap.N}
                        </label>
                      </div>
                    </div>
                    </div>
          <div class="modal-footer" style="text-align:center;">
		    <button type="button"  id="weixin_btn" class="btn btn-primary">${uiLabelMap.Save}</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--编辑弹出框 end-->

 

 
 <!--支付问题 start-->
<div class="modal fade" id="des_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.EditpayProblem}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="" method="post">
          
              <div class="row">
			  <div class="form-group">
                <div class="col-sm-12">
                 <div class="box-body pad" >
                    <textarea id="content" name="content"  value="2" >
                    </textarea>
                    <p class="dp-error-msg"></p>
                </div>
                </div>     
              </div>
              </div>
          <div class="modal-footer" style="text-align:center;">
		    <button type="button"  id="btn_des" class="btn btn-primary">${uiLabelMap.Save}</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--支付问题 end-->
 
 
 
 
<script>
      //判断支付接口类型
      var form_operation='';
$(function(){
        CKEDITOR.replace("content");
        
        //AliPay编辑按钮点击事件
	    $("#AliPay").click(function(){
	    form_operation='AliPay';
         $.ajax({
            type: 'post',
            url: '<@ofbizUrl>queryAliPay</@ofbizUrl>',
            data: {paymentGatewayConfigId: $('#AliPayId').val()},
            success: function (data) {
                $("#edit_AliPay #paymentGatewayConfigId").val(data.aliPay.paymentGatewayConfigId);//Id
                $("#edit_AliPay #paymentName").val(data.aliPay.paymentName);//支付名称
                $("#edit_AliPay #apiKey").val(data.aliPay.apiKey);//Api-Key
                $("#edit_AliPay #secretKey").val(data.aliPay.secretKey);//Secret-key
                $("#edit_AliPay #payAccount").val(data.aliPay.payAccount);//收款账号
                $("#edit_AliPay #payUrl").val(data.aliPay.payUrl);//后台回调地址
                $("#edit_AliPay #backUrl").val(data.aliPay.backUrl);//前台回调地址
                $("#edit_AliPay #mobileBindUrl").val(data.aliPay.mobileBindUrl);//手机支付回调
                $("#edit_AliPay #description").val(data.aliPay.description);//支付描述
                $("#edit_AliPay #payType").val(data.payType);//支付类型
                $("#edit_AliPay #isEnabled-"+data.aliPay.isEnabled).prop('checked',true);//启用状态
                $("#edit_AliPay #isDefault-"+data.aliPay.isDefault).prop('checked',true);//启用状态
                CKEDITOR.instances.content.setData(data.textData);//支付问题描述
                  if(data.aliPay.imgContentId!==null){//图标
	             var contentId="/content/control/stream?contentId="+data.aliPay.imgContentId;
	             $('#edit_AliPay #img').attr('src',contentId);
	             $('#edit_AliPay #contentId').val(data.aliPay.imgContentId);
	             }
            }
          });
        $('#edit_AliPay').modal('show');
        });
        
        
        //AliPay编辑取消按钮弹框关闭事件
		$('#edit_AliPay').on('hide.bs.modal', function () {
		  $('#AliPayForm').dpValidate({
		  	clear: true
		  });
           $("#edit_AliPay #paymentName").val('');//支付名称
           $("#edit_AliPay #apiKey").val('');//Api-Key
           $("#edit_AliPay #secretKey").val('');//Secret-key
           $("#edit_AliPay #payAccount").val('');//收款账号
           $("#edit_AliPay #payUrl").val('');//后台回调地址
           $("#edit_AliPay #backUrl").val('');//前台回调地址
           $("#edit_AliPay #mobileBindUrl").val('');//手机支付回调
           $("#edit_AliPay #description").val('');//支付描述
           $("#edit_AliPay #payType").val('');//支付类型
           $("#edit_AliPay #isEnabled-Y").prop('checked',true);//启用状态
           $("#edit_AliPay #isDefault-Y").prop('checked',true);//启用状态
           $('#edit_AliPay #img').attr('src','');
           $('#edit_AliPay #contentId').val();
		})
		
		//AliPay支付问题描述
	    $("#aliPay_button").click(function(){
        $('#des_Modal').modal('show');
        });
        
		//AliPay支付问题描述
	    $('#btn_des').click(function(){
	        $("#des_Modal").modal('toggle');
	    });
	    
		//AliPay编辑提交按钮点击事件
	    $('#AliPay_btn').click(function(){
	        $('#AliPayForm').dpValidate({
			  clear: true
			});
			$('#AliPayForm').submit();
	    });
		
		//AliPay支付宝编辑表单校验
       $('#AliPayForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "updateAliPay",
					type: "POST",
					data: $('#AliPayForm').serialize() + "&textData="+CKEDITOR.instances.content.getData(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#edit_AliPay').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>PayMentInterface</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_AliPay').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
		
        //weixinPay编辑按钮点击事件
	    $("#weixinPay").click(function(){
	     form_operation='weixinPay';
	     $.ajax({
            type: 'post',
            url: '<@ofbizUrl>queryWeixinPay</@ofbizUrl>',
            data: {paymentGatewayConfigId: $('#weixinPayId').val()},
            success: function (data) {
                $("#edit_WeiXin #paymentGatewayConfigId").val(data.weixinPay.paymentGatewayConfigId);//Id
                $("#edit_WeiXin #paymentName").val(data.weixinPay.paymentName);//支付名称
                $("#edit_WeiXin #apiKey").val(data.weixinPay.apiKey);//Api-Key
                $("#edit_WeiXin #secretKey").val(data.weixinPay.secretKey);//Secret-key
                $("#edit_WeiXin #partner").val(data.weixinPay.partner);//商户号
                $("#edit_WeiXin #partnerKey").val(data.weixinPay.partnerKey);//商户标识
                $("#edit_WeiXin #notifyUrl").val(data.weixinPay.notifyUrl);//通知URL
                $("#edit_WeiXin #description").val(data.weixinPay.description);//支付描述
                $("#edit_WeiXin #payType").val(data.payType);//支付类型
                $("#edit_WeiXin #isEnabled-"+data.weixinPay.isEnabled).prop('checked',true);//启用状态
                $("#edit_WeiXin #isDefault-"+data.weixinPay.isDefault).prop('checked',true);//启用状态
	             console.log(data.weixinPay);
	             if(data.weixinPay.imgContentId!==null){
	             var contentId="/content/control/stream?contentId="+data.weixinPay.imgContentId;
	             $('#edit_WeiXin #img').attr('src',contentId);
	             $('#edit_WeiXin #contentId').val(data.weixinPay.imgContentId);
	             }
                CKEDITOR.instances.content.setData(data.textData);//支付问题描述
            }
          });
        $('#edit_WeiXin').modal('show');
        });
        
         //weixin支付问题描述
	    $("#weixin_button").click(function(){
        $('#des_Modal').modal('show');
        });
        
        //WeiXin编辑取消按钮弹框关闭事件
		$('#edit_WeiXin').on('hide.bs.modal', function () {
		  $('#WeixinForm').dpValidate({
		  	clear: true
		  });
           $("#edit_WeiXin #paymentName").val('');//支付名称
           $("#edit_WeiXin #apiKey").val('');//Api-Key
           $("#edit_WeiXin #secretKey").val('');//Secret-key
           $("#edit_WeiXin #partner").val('');//收款账号
           $("#edit_WeiXin #partnerKey").val('');//后台回调地址
           $("#edit_WeiXin #notifyUrl").val('');//前台回调地址
           $("#edit_WeiXin #description").val('');//支付描述
           $("#edit_WeiXin #payType").val('');//支付类型
           $("#edit_WeiXin #isEnabled-Y").prop('checked',true);//启用状态
           $("#edit_WeiXin #isDefault-Y").prop('checked',true);//启用状态
           $('#edit_WeiXin #img').attr('src','');
           $('#edit_WeiXin #contentId').val();
		})
        
        //weixin编辑提交按钮点击事件
	    $('#weixin_btn').click(function(){
	        $('#WeixinForm').dpValidate({
			  clear: true
			});
			$('#WeixinForm').submit();
	    });
        //weixin微信编辑表单校验
       $('#WeixinForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "updateWeixinPay",
					type: "POST",
				    data : $('#WeixinForm').serialize() + "&textData="+CKEDITOR.instances.content.getData(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#edit_WeiXin').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>PayMentInterface</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_WeiXin').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
	    
	       //unionPay 银联编辑按钮点击事件
	    $("#unionPay").click(function(){
	     form_operation='unionPay';
	     $.ajax({
            type: 'post',
            url: '<@ofbizUrl>queryAliPay</@ofbizUrl>',
            data: {paymentGatewayConfigId: $('#unionPayId').val()},
            success: function (data) {
                $("#edit_AliPay #paymentGatewayConfigId").val(data.aliPay.paymentGatewayConfigId);//Id
                $("#edit_AliPay #paymentName").val(data.aliPay.paymentName);//支付名称
                $("#edit_AliPay #apiKey").val(data.aliPay.apiKey);//Api-Key
                $("#edit_AliPay #secretKey").val(data.aliPay.secretKey);//Secret-key
                $("#edit_AliPay #payAccount").val(data.aliPay.payAccount);//收款账号
                $("#edit_AliPay #payUrl").val(data.aliPay.payUrl);//后台回调地址
                $("#edit_AliPay #backUrl").val(data.aliPay.backUrl);//前台回调地址
                $("#edit_AliPay #mobileBindUrl").val(data.aliPay.mobileBindUrl);//手机支付回调
                $("#edit_AliPay #description").val(data.aliPay.description);//支付描述
                $("#edit_AliPay #payType").val(data.payType);//支付类型
                $("#edit_AliPay #isEnabled-"+data.aliPay.isEnabled).prop('checked',true);//启用状态
                $("#edit_AliPay #isDefault-"+data.aliPay.isDefault).prop('checked',true);//启用状态
                CKEDITOR.instances.content.setData(data.textData);//支付问题描述
                 if(data.aliPay.imgContentId!==null){
	             var contentId="/content/control/stream?contentId="+data.aliPay.imgContentId;
	             $('#edit_AliPay #img').attr('src',contentId);
	             $('#edit_AliPay #contentId').val(data.aliPay.imgContentId);
	             }
            }
          });
        $('#edit_AliPay').modal('show');
        });
          //图片上传控件初始化
         $.chooseImage.int({
                serverChooseNum: 5,
                getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
                submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
                submitServerImgUrl: '',
                submitNetworkImgUrl: ''
                });
           //图片控件保存按钮
           $('body').on('click','.img-submit-btn',function(){
               var obj = $.chooseImage.getImgData();
               $.chooseImage.choose(obj,function(data){
               var contentId="/content/control/stream?contentId="+data.uploadedFile0;
                 if(form_operation=='weixinPay'){
                  $('#edit_WeiXin #img').attr('src',contentId);
                  $('#edit_WeiXin #contentId').val(data.uploadedFile0);
                 } 
                 if( form_operation=='unionPay'||form_operation=='AliPay'){
                  $('#edit_AliPay #img').attr('src',contentId);
                  $('#edit_AliPay #contentId').val(data.uploadedFile0);
                 }
               })
             });
})
          function imageManage() {
                $.chooseImage.show()
            }
</script>