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
	
	
	.s-input-w{
    width: 80px;
    display: none;

}
.s-input-w_l{
     width: 80px;
    display:inline-block;
}
.s-span-c {
         height: 34px;
    padding: 6px 12px;
    display: inline-block;
    line-height: 22px;
}
.s-qx{
    display: none;
}
	</style>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<!-- Main content -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">订单规则</h3>
        </div>
        <div class="box-body">			
            <!-- 分割线 start-->
            <div class="cut-off-rule bg-gray"></div>
			<!-- 分割线  start-->
            <#if orderRuleList?has_content> 
            <div class="row">
            <#list orderRuleList as ruleList>
            <form class="form-horizontal" role="form" action="" name="" class="">
            <div class="form-group" >
                     <label for="title" class="col-sm-2 control-label">是否允许退单:</label>
                     <div class="col-sm-10">
                     <#if ruleList.isReturn=='Y' >
                <button id="btn_add" class="btn btn-primary" >
                  是
                </button>
                     <#else>
                <button id="btn_add" class="btn btn-primary" >
                  否
                </button>
                     </#if>
                     </div>
            </div>
            
  <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">普通订单自动取消时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i" name="ordinaryCancelStamp" value="${ruleList.ordinaryCancelStamp}">
      <span class="s-span-c  js-s-i-1">
      ${ruleList.ordinaryCancelStamp?if_exists} 
      </span>
        <select class="form-control s-input-w  js-s-s" name="ordinaryCancelUom">
         <option <#if ruleList.ordinaryCancelUom=='d'> selected</#if> value="d">天</option>
         <option <#if ruleList.ordinaryCancelUom=='h'> selected</#if>value="h">小时</option>
         <option <#if ruleList.ordinaryCancelUom=='min'> selected</#if>value="min">分钟</option>
      </select>
      <span class="s-span-c js-s-s-1">        
       <#if ruleList.ordinaryCancelUom=='d'>
              天
             <#elseif ruleList.ordinaryCancelUom=='h'>
              小时
             <#elseif ruleList.ordinaryCancelUom=='min'>
              分
              </#if></span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
  <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">团购订单自动取消时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i" name="groupCancelStamp" value="${ruleList.groupCancelStamp}">
      <span class="s-span-c  js-s-i-1">
      ${ruleList.groupCancelStamp?if_exists} 
      </span>
        <select class="form-control s-input-w  js-s-s" name="groupCancelUom">
         <option <#if ruleList.groupCancelUom=='d' > selected</#if> value="d">天</option>
         <option <#if ruleList.groupCancelUom=='h'> selected</#if> value="h">小时</option>
         <option <#if ruleList.groupCancelUom=='min'> selected</#if> value="min">分钟</option>
      </select>
      <span class="s-span-c js-s-s-1">        
       <#if ruleList.groupCancelUom=='d'>
              天
             <#elseif ruleList.groupCancelUom=='h'>
              小时
             <#elseif ruleList.groupCancelUom=='min'>
              分
              </#if></span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
  <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">秒杀订单自动取消时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i" name="seckillCancelStamp" value="${ruleList.seckillCancelStamp?if_exists}">
      <input type="hidden" class="form-control s-input-w js-s-i" name="orderRuleId"  id="orderRuleId" value="${ruleList.orderRuleId?if_exists}">
      <span class="s-span-c  js-s-i-1">
      ${ruleList.seckillCancelStamp?if_exists} 
      </span>
        <select class="form-control s-input-w  js-s-s" name="seckillCancelUom">
         <option <#if ruleList.seckillCancelUom=='d'> selected</#if> value="d">天</option>
         <option <#if ruleList.seckillCancelUom=='h'> selected</#if> value="h">小时</option>
         <option <#if ruleList.seckillCancelUom=='min'> selected</#if> value="min">分钟</option>
      </select>
      <span class="s-span-c js-s-s-1">        
       <#if ruleList.seckillCancelUom=='d'>
              天
             <#elseif ruleList.seckillCancelUom=='h'>
              小时
             <#elseif ruleList.seckillCancelUom=='min'>
              分
              </#if></span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
  
    <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">订单自动确认收货时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i"  name="confirmOrderStamp" value="${ruleList.confirmOrderStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.confirmOrderStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
    <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">退单自动审核时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i" name="returnToexamineStamp" value="${ruleList.returnToexamineStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.returnToexamineStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
    <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">退单自动取消时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i" name="returnCancelStamp" value="${ruleList.returnCancelStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.returnCancelStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
    <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">退单自动确认收货时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i"  name="returnConfirmStamp" value="${ruleList.returnConfirmStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.returnConfirmStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
    <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">自动评价时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i"  name="reviewStamp" value="${ruleList.reviewStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.reviewStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
    <div class="form-group">
      <label for="lastname" class="col-sm-2 control-label">可提交退货时间：</label>
      <div class="col-sm-10">
      <input type="text" class="form-control s-input-w js-s-i"  name="returnCommitStamp" value="${ruleList.returnCommitStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.returnCommitStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
            
         <div class="form-group" style="width:100%;float:left;" >
                     <label for="title" class="col-sm-2 control-label">退货说明:</label>
                     <div class="col-sm-10">
         <a href="<@ofbizUrl>RetrunExplain</@ofbizUrl>">查看并修改</a>
                     </div>
            </div>
            <div class="form-group" style="width:100%;float:left;" >
                     <label for="title" class="col-sm-2 control-label">退款说明:</label>
                     <div class="col-sm-10">
        <a href="<@ofbizUrl>RefundExplain</@ofbizUrl>">查看并修改</a>
                     </div>
            </div>
            </#list>
            </div><!-- 表格区域end -->
             </form>
       <#else>
        <div id="noData" class="col-sm-12">
            <h3>没有订单规则!</h3>
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

   
 <!--编辑弹出框 end-->
<div class="modal fade" id="edit_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">支付方式编辑</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="" method="post">
          
             <div class="row">
            <div class="form-group">
                    <label for="message-text" class="control-label col-sm-2">启用标识:</label>
                      <div class="radio">
                        <label>
                          <input type="radio" name="hasTable" id="optionsRadios1" value="Y" class="js-Y" checked>
                          是
                        </label>
                         <label>
                          <input type="radio" name="hasTable" id="optionsRadios2" value="N" class="js-N">
                        否
                        </label>
                      </div>
             </div>
             </div>
             <div class="row">
                <div class="form-group" data-type="required" data-mark="支付方式名称">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>支付方式:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="description" name="description"/>
                      <input type="hidden" class="form-control dp-vd w-p50"  name="paymentGatewayConfigTypeId" id="paymentGatewayConfigTypeId"/>
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
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



 $(document).on('click','.js-s-xg',function(){
        //修改
         var $s=$(this);
         $s.text('保存').removeClass('js-s-xg').addClass('js-s-bc');//编辑状态
         var span_q=   $s.siblings('.js-s-qx').show();//取消按钮出现
         var span_s=   $s.siblings('.js-s-s-1').hide();//单位显示器隐藏
         var span_i= $s.siblings('.js-s-i-1').hide();//显示器隐藏
         var input_i=$s.siblings('.js-s-i').show();//编辑器出现
         var select_s=$s.siblings('.js-s-s').show();//选择单位出现
         //取消
         span_q.click(function(){
            $(this).hide();
            $s.text('修改').removeClass('js-s-bc').addClass('js-s-xg');
            span_s.show();span_i.show();input_i.hide();select_s.hide();
         })
         //保存
         $('.js-s-bc').click(function(){ 
               var name=$(this).siblings('.js-s-i').prop("name");
              var nameValue=$(this).siblings('.js-s-i').val();
              var nameUom=$(this).siblings('.js-s-s').prop("name");
              var nameUomValue=$(this).siblings('.js-s-s').val();
              var orderRuleId=$('#orderRuleId').val();
              if(typeof(nameUom) == "undefined"){ 
                 $.ajax({
					url: "updateOrderRule",
					type: "POST",
					data:{
                         name:   name,
                         nameValue:   nameValue,
                         orderRuleId:orderRuleId
                         },
					dataType : "json",
					success: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>OrderRule</@ofbizUrl>';
						})
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				  });
                 }else{
                  $.ajax({
					url: "updateOrderRule",
					type: "POST",
					data:{
                         name:   name,
                         nameValue:   nameValue,
                         nameUom:   nameUom,
                         nameUomValue:   nameUomValue,
                         orderRuleId:orderRuleId
                         },
					dataType : "json",
					success: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>OrderRule</@ofbizUrl>';
						})
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
               }
         })
        })


</script>