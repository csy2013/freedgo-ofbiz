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
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<!-- Main content -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">积分使用规则</h3>
        </div>
        <div class="box-body">			
            <!-- 分割线 start-->
            <div class="cut-off-rule bg-gray"></div>
			<!-- 分割线  start-->
        <div class="row"> 
        <form class="form-horizontal" role="form" action="" name="" class="">
		  <div class="form-group">
		      <label for="lastname" class="col-sm-2 control-label">兑换一块钱需要消耗的积分：</label>
		      <div class="col-sm-10">
		      <#if partyIntegralSetList?has_content>
		      <#list partyIntegralSetList as list>
		      <input type="text" class="form-control s-input-w js-s-i" name="integralValue" value="${list.integralValue?if_exists}">
		      <input type="hidden" class="form-control s-input-w js-s-i" name="partyIntegralSetId"  id="partyIntegralSetId" value="${list.partyIntegralSetId?if_exists}">
		      <span class="s-span-c  js-s-i-1">
		      ${list.integralValue?if_exists}
		      </span>
		      </#list>
		      <#else>
		      <input type="text" class="form-control s-input-w js-s-i" name="integralValue" value="">
		      </#if>
		      <!--是否有修改的权限-->
              <#if security.hasEntityPermission("PARTY_INTEGRAL", "_UPDATE", session)>
		      <button class="btn btn-primary btn-xs js-s-xg" type="button">修改</button>
		      </#if>
		      <button class="btn btn-default btn-xs s-qx js-s-qx" type="button">取消</button>
		      </div>
		  </div>
            </div><!-- 表格区域end -->
             </form>
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

<script>
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
	              var integralValue=$(this).siblings('.js-s-i').val();
	              var partyIntegralSetId=$('#partyIntegralSetId').val();
	              //是否为整数
	              var reg=/^[1-9]*[1-9][0-9]*$/;
	              console.log();
		          if(!reg.test(integralValue.trim())){
		          $(this).siblings('.js-s-i').addClass('has-error');
		            //设置提示弹出框内容 
				  $('#modal_msg #modal_msg_body').html('您输入的整数类型格式不正确!请输入大于零的整数!');
    			  $('#modal_msg').modal();
		            }else{
		          $.ajax({
						url: "updatePartyIntegralSet",
						type: "POST",
						data:{
	                        integralValue:integralValue,
	                        partyIntegralSetId:partyIntegralSetId
	                         },
						dataType : "json",
						success: function(data){
							//设置提示弹出框内容
							$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
		    				$('#modal_msg').modal('show');
		    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
		    				$('#modal_msg').off('hide.bs.modal');
		    				$('#modal_msg').on('hide.bs.modal', function () {
							  window.location.href='/membermgr/control/partyIntegralSet';
							})
						},
						error: function(data){
							//设置提示弹出框内容
							$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
		    				$('#modal_msg').modal();
						}
					});
		          }
	                  
			return false;
	         })
</script>