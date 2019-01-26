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

.has-error {
  border-color: #ed5565;
}
	</style>
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
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
                     <label for="title" class="col-sm-3 control-label">是否允许退单:</label>
                     <div class="col-sm-9">
                     <input type="hidden" id="isReturn" value="${ruleList.isReturn?if_exists}">
                     <#if ruleList.isReturn=='Y' >
	                  <!--是否有修改的权限-->
	                 <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
	                 <input type="button" class="btn btn-primary" id="is_Return" value="是">
                     <#else>
                                                                    是
                     </#if>
                     <#else>
                     <!--是否有修改的权限-->
	                 <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
	                 <input type="button" class="btn btn-primary" id="is_Return" value="否">
                     <#else>
                                                                    否
                     </#if>
                     </#if>
                     </div>
            </div>
            
  <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">普通订单自动取消时间：</label>
      <div class="col-sm-9">
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
    <!--是否有修改的权限-->
    <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
    </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
  <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">团购订单自动取消时间：</label>
      <div class="col-sm-9">
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
               <!--是否有修改的权限-->
    <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
     </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
  <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">秒杀订单自动取消时间：</label>
      <div class="col-sm-9">
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
               <!--是否有修改的权限-->
    <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
     </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
  
    <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">订单自动确认收货时间：</label>
      <div class="col-sm-9">
      <input type="text" class="form-control s-input-w js-s-i"  name="confirmOrderStamp" value="${ruleList.confirmOrderStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.confirmOrderStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
       <!--是否有修改的权限-->
     <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
     </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
    <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">退单自动审核时间：</label>
      <div class="col-sm-9">
      <input type="text" class="form-control s-input-w js-s-i" name="returnToexamineStamp" value="${ruleList.returnToexamineStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.returnToexamineStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
       <!--是否有修改的权限-->
      <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
    <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">退单自动取消时间：</label>
      <div class="col-sm-9">
      <input type="text" class="form-control s-input-w js-s-i" name="returnCancelStamp" value="${ruleList.returnCancelStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.returnCancelStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
      <!--是否有修改的权限-->
      <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
    <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">退单自动确认收货时间：</label>
      <div class="col-sm-9">
      <input type="text" class="form-control s-input-w js-s-i"  name="returnConfirmStamp" value="${ruleList.returnConfirmStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.returnConfirmStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
      <!--是否有修改的权限-->
      <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
    <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">自动评价时间：</label>
      <div class="col-sm-9">
      <input type="text" class="form-control s-input-w js-s-i"  name="reviewStamp" value="${ruleList.reviewStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.reviewStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
       <!--是否有修改的权限-->
      <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
    <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">可提交退货时间：</label>
      <div class="col-sm-9">
      <input type="text" class="form-control s-input-w js-s-i"  name="returnCommitStamp" value="${ruleList.returnCommitStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.returnCommitStamp?if_exists}</span>
      <span class="s-span-c s-d-1">天</span>
       <!--是否有修改的权限-->
      <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   <!--预计退款时间-->
    <div class="form-group">
      <label for="lastname" class="col-sm-3 control-label">预计退款时间：</label>
      <div class="col-sm-9">
      <input type="text" class="form-control s-input-w js-s-i"  name="expectedRefundStamp" value="${ruleList.expectedRefundStamp?if_exists}">
      <span class="s-span-c  js-s-i-1">${ruleList.expectedRefundStamp?default("0")}</span>
      <span class="s-span-c s-d-1">天</span>
       <!--是否有修改的权限-->
      <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
      </#if>
      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
      </div>
   </div>
   
        <div class="row">
		  <div class="form-group">
            <label for="recipient-name" class="col-sm-3 control-label">退货说明:</label>
            <div class="col-sm-16 w-p70">
              <!--是否有修改的权限-->
            <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
            <button type="button"  id="return_button" class="btn btn-primary">查看并修改</button>
            <#else>
            <button type="button"  id="return_button" class="btn btn-primary">查看</button>
            </#if>
            </div>     
          </div>
          </div>
        <div class="row">
		  <div class="form-group">
            <label for="recipient-name" class="col-sm-3 control-label">退款说明:</label>
            <div class="col-sm-16 w-p70">
              <!--是否有修改的权限-->
            <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
            <button type="button"  id="refund_button" class="btn btn-primary">查看并修改</button>
            <#else>
            <button type="button"  id="refund_button" class="btn btn-primary">查看</button>
            </#if>
            </div>     
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
    
     <!--退货说明 start-->
   <div class="modal fade" id="retrun_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">编辑退货说明</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="" method="post">
              <div class="row">
			  <div class="form-group">
                <div class="col-sm-12">
                 <div class="box-body pad" >
                    <textarea id="return_content" name="return_content"  value="2" >
                    </textarea>
                    <p class="dp-error-msg"></p>
                </div>
                </div>     
              </div>
              </div>
          <div class="modal-footer" style="text-align:center;">
		     <!--是否有修改的权限-->
            <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
		    <button type="button"  id="btn_return" class="btn btn-primary">${uiLabelMap.Save}</button>
            </#if>
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--退货说明  end-->
     <!--退款说明 start-->
   <div class="modal fade" id="refund_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">编辑退款说明</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="" method="post">
          
              <div class="row">
			  <div class="form-group">
                <div class="col-sm-12">
                 <div class="box-body pad" >
                    <textarea id="refund_content" name="refund_content"  value="2" >
                    </textarea>
                    <p class="dp-error-msg"></p>
                </div>
                </div>     
              </div>
              </div>
          <div class="modal-footer" style="text-align:center;">
		    <!--是否有修改的权限-->
            <#if security.hasEntityPermission("ORDER_SET", "_UPDATE", session)>
		    <button type="button"  id="btn_refund" class="btn btn-primary">${uiLabelMap.Save}</button>
            </#if>
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--退款说明  end-->
    
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

   
 

 
<script>


$(function(){
	    CKEDITOR.replace("return_content");
	    CKEDITOR.replace("refund_content");
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
	         var span_q= $s.siblings('.js-s-qx').show();//取消按钮出现
	         var span_s= $s.siblings('.js-s-s-1').hide();//单位显示器隐藏
	         var span_i= $s.siblings('.js-s-i-1').hide();//显示器隐藏
	         var input_i=$s.siblings('.js-s-i').show();//编辑器出现
	         var select_s=$s.siblings('.js-s-s').show();//选择单位出现
	         //取消
	         span_q.click(function(){
	            $(this).hide();
	            $s.text('修改').removeClass('js-s-bc').addClass('js-s-xg');
	            span_s.show();span_i.show();input_i.hide();select_s.hide();
	             return false;
	         })
	        })
         //保存
       $(document).on('click','.js-s-bc',function(){
               var name=$(this).siblings('.js-s-i').prop("name");
              var nameValue=$(this).siblings('.js-s-i').val();
              var nameUom=$(this).siblings('.js-s-s').prop("name");
              var nameUomValue=$(this).siblings('.js-s-s').val();
              var orderRuleId=$('#orderRuleId').val();
              //是否为整数
	          var reg=/^[1-9]*[1-9][0-9]*$/;
              if(typeof(nameUom) == "undefined"){ 
                if(!reg.test(nameValue.trim())){
		          $(this).siblings('.js-s-i').addClass('has-error');
		            //设置提示弹出框内容 
				  $('#modal_msg #modal_msg_body').html('您输入的整数类型格式不正确!请输入大于零的整数!');
    			  $('#modal_msg').modal();
		            }else{
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
		        }
                 }else{
                 
                  if(!reg.test(nameValue.trim())){
		          $(this).siblings('.js-s-i').addClass('has-error');
		            //设置提示弹出框内容 
				  $('#modal_msg #modal_msg_body').html('您输入的整数类型格式不正确!请输入大于零的整数!');
    			  $('#modal_msg').modal();
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
               }
        })
	 
	   //是否允许退单
	 $('#is_Return').click(function(){
	    var isReturn=$('#isReturn').val();
	    var orderRuleId=$('#orderRuleId').val();
	      $.ajax({
	                url:'<@ofbizUrl>updateOrderRule</@ofbizUrl>',
	                type: 'post',
	                dataType: 'json',
	                data: {
	                    isReturn:isReturn,
	                    orderRuleId:orderRuleId
	                      },
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
	            })
	        })
	    
	    function findContent(type) {
	       var orderRuleId=$('#orderRuleId').val();
          $.ajax({
            type: 'post',
            url: '<@ofbizUrl>findOrdercontent</@ofbizUrl>',
            data: {
            orderRuleId: orderRuleId,
            type:type
            },
            success: function (data) {
               if(data.type=='return')
               {
               CKEDITOR.instances.return_content.setData(data.textData)
               }else{
               CKEDITOR.instances.refund_content.setData(data.textData)
               }
            }
        });
      }
        //退货说明弹出框
	    $("#return_button").click(function(){
        $('#retrun_Modal').modal('show');
	    findContent('return');
        });
	    //退货说明弹出框
	     $("#refund_button").click(function(){
		     findContent('refund');
	         $('#refund_Modal').modal('show');
        });
	     
	   //退货说明确定按钮
	 $('#btn_return').click(function(){
	    var isReturn=$('#isReturn').val();
	    var orderRuleId=$('#orderRuleId').val();
	    var return_content=CKEDITOR.instances.return_content.getData();
	      $.ajax({
	                url:'<@ofbizUrl>updaterReturnContent</@ofbizUrl>',
	                type: 'post',
	                dataType: 'json',
	                data: {
	                    return_content:return_content,
	                    orderRuleId:orderRuleId
	                      },
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
	            })
	        })
	   //退款说明确定按钮
	 $('#btn_refund').click(function(){
	    var refund_content=CKEDITOR.instances.refund_content.getData();
	    var orderRuleId=$('#orderRuleId').val();
	      $.ajax({
	                url:'<@ofbizUrl>updaterRefundContent</@ofbizUrl>',
	                type: 'post',
	                dataType: 'json',
	                data: {
	                    refund_content:refund_content,
	                    orderRuleId:orderRuleId
	                      },
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
	            })
	        })

</script>