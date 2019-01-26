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
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<!-- Main content -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">${uiLabelMap.PaymentType}<#if typeList?has_content>(共${typeList.size()?default('0')}个)</#if></h3>
        </div>
        <div class="box-body">			
            <!-- 分割线 start-->
            <div class="cut-off-rule bg-gray"></div>
			<!-- 分割线  start-->
            <#if typeList?has_content> 
            <div class="row">
            <#list typeList as typeList>
            <!-- 循环体 start-->
            <div class="col-md-4 col-lg-3">
            <div class="s-zdy">
            <!--是否有编辑的权限-->
            <#if security.hasEntityPermission("PAYMENT_TYPE", "_VIEW", session)>
            <p class="s-bj-p"><a href="javascript:;" class="s-bj edit-info">${uiLabelMap.Edit}</a></p>
            </#if>
            <input type="hidden"  name="paymentGatewayConfigTypeId" value="${typeList.paymentGatewayConfigTypeId?if_exists}"/>
            <p><span class="s-error">${typeList.description?if_exists}</span></p>
            <div class="s-img-div">
             <p>
             <div class="dp-tables_btn">
                <button id="btn_add" class="btn btn-primary" disabled >
                <#if typeList.hasTable=='Y' >
                ${uiLabelMap.Effect} 
                <#else>
               ${uiLabelMap.isStop} 
                </#if>                               
                </button>
                </div>
             </p>
            </div>
            <p><span class="s-message">${uiLabelMap.LastUpdateTime}:${typeList.lastUpdatedStamp?string("yyyy-MM-dd HH:mm:ss")}</span></p>
            </div>
            </div><!-- 循环体 end-->
            </#list>
            </div><!-- 表格区域end -->
       <#else>
        <div id="noData" class="col-sm-12">
            <h3>${uiLabelMap.NoPaymentTypeData}</h3>
        </div>
	  </#if>
        </div>
        <!-- /.box-body -->
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

   
 <!--编辑弹出框 end-->
<div class="modal fade" id="edit_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.PaymentTypeEdit}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="" method="post">
          <!-- 是否启用start-->
             <div class="row">
            <div class="form-group">
                    <label for="message-text" class="control-label col-sm-2">${uiLabelMap.isEnabled}:</label>
                      <div class="radio">
                        <label>
                          <input type="radio" name="hasTable" id="optionsRadios1" value="Y" class="js-Y" checked>
                         ${uiLabelMap.Y}
                        </label>
                         <label>
                          <input type="radio" name="hasTable" id="optionsRadios2" value="N" class="js-N">
                        ${uiLabelMap.N}
                        </label>
                      </div>
             </div>
             </div>
             <!-- 是否启用end-->
             <!-- 支付方式名称start-->
             <div class="row">
                <div class="form-group" data-type="required" data-mark=" ${uiLabelMap.PaymentTypeName}">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.PaymentTypeName}:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="description" name="description"/>
                      <input type="hidden" class="form-control dp-vd w-p50"  name="paymentGatewayConfigTypeId" id="paymentGatewayConfigTypeId"/>
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
          <!-- 支付方式名称end-->
          <div class="modal-footer">
		    <button type="button"  id="btn_edit" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--编辑弹出框 end-->

 
<script>


$(function(){
	    
	     //编辑按钮点击事件
	    $(".edit-info").click(function(){
	    var paymentGatewayConfigTypeId=$(this).parent().parent().find('input[name=paymentGatewayConfigTypeId]').val();
        doSearchPage(paymentGatewayConfigTypeId);
        $('#edit_Modal').modal('show');
        });
	    
	    function doSearchPage(id) {
        $.ajax({
            type: 'post',
            url: '<@ofbizUrl>findPayMentById</@ofbizUrl>',
            data: {paymentGatewayConfigTypeId: id},
            success: function (data) {
                $("#edit_Modal #paymentGatewayConfigTypeId").val(data.payMent.paymentGatewayConfigTypeId);
                $("#edit_Modal #description").val(data.payMent.description);
                $('.js-'+data.payMent.hasTable).prop('checked',true);//启用状态
            }
        });
      }
	  
         //编辑弹框关闭事件
		$('#edit_Modal').on('hide.bs.modal', function () {
		  $('#EditForm').dpValidate({
		  	clear: true
		  });
		})
      //编辑提交按钮点击事件
	    $('#btn_edit').click(function(){
	        $('#EditForm').dpValidate({
			  clear: true
			});
			$('#EditForm').submit();
	    });
       //编辑表单校验
       $('#EditForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "updatePayMent",
					type: "POST",
					data: $('#EditForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>PaymentType</@ofbizUrl>';
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
})
</script>