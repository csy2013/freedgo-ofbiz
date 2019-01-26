<style>
	.box-left{
	    width:150px;
	    height: 450px;
		overflow-y: auto;
		overflow-x: hidden;
	    background-color:#fff;
	    border:1px solid #ccc;
	    float:left;
	}
	
	.first-menu{
		padding-left: 10px;
		padding-top: 10px;
		list-style: none;
	}
	
	.second-menu{
		padding-left: 0px;
		list-style: none;
	}
	
	.second-menu>li{
		position: relative;
		display: block;
	}
	
	.second-menu>li>a{
		position: relative;
		display: block;
		padding-left: 10px;
		padding-top: 5px;
		color: #666;
		cursor: pointer;
		border-right: 3px solid transparent;
	}
	
	.second-menu>li.active{
		
	}
	
	.second-menu>li.active>a{
		color: #3c8dbc;
		border-right-color: #3c8dbc;	
	}
	
	.second-menu>li>a:hover{
		color: #3c8dbc;
		border-right-color: #3c8dbc;	
	}
	
	.box-right{
    	height: 450px;
		overflow-y: auto;
		overflow-x: hidden;
        background-color:#fff;
        border-top:1px solid #ccc;
        border-right:1px solid #ccc;
        border-bottom:1px solid #ccc;
    }
</style>

<div class="box box-info">
	<div class="box-body">
		<div class="box-left">
			<ul class="first-menu">
				<li>
					<a href="javascript:;" >
						订单管理
						<i class="glyphicon glyphicon-chevron-down"></i>
					</a>
					<ul class="second-menu">
						<#--<li id="FIELD_INVOICE" class="active"><a href="javascript:;" >发票内容</a></li>-->
                       	<li id="FIELD_CANCEL" class="active"><a href="javascript:;" >订单取消原因</a></li>
                       	<li id="FIELD_RETURN"><a href="javascript:;" >退货原因</a></li>
					</ul>
				</li>
			</ul>
		</div>
		
		<div class="box-right" >
			<div class="btn-box m-b-10">
             	<div class="col-sm-6">
              		<div class="dp-tables_btn">
	                	<!--是否有新增的权限-->
                    	<#if security.hasEntityPermission("FIELD_TABLESET", "_CREATE", session)>
	                		<button id="btn_add" class="btn btn-primary" >
	                    		<i class="fa fa-plus"></i>${uiLabelMap.Add}
	                		</button>
	                	</#if>
	                	
	                	<!--是否有删除的权限-->
                    	<#if security.hasEntityPermission("FIELD_TABLESET", "_DELETE", session)>
	                		<button id="btn_del" class="btn btn-primary btn_del">
	                    		<i class="fa fa-trash"></i> ${uiLabelMap.Delete}
	                		</button>
	                	</#if>
	                </div>
				</div><!-- 操作按钮组end -->
            </div><!-- 工具栏end -->
            
            <!-- 表格区域start -->
		    <div class="row">
		      <div class="col-sm-12">
		        <table id="DataTbl" class="table table-bordered table-hover js-checkparent">
		        </table>
		      </div>
		    </div><!-- 表格区域end -->
		</div>
	</div>
</div>

<!-- 新增弹出框 start-->
<div class="modal fade" id="modal_add" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
	<div class="modal-dialog" role="document">
    	<div class="modal-content">
          	<div class="modal-header">
            	<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            	<h4 class="modal-title" id="h4_title"></h4>
          	</div>
          	<div class="modal-body">
            	<form class="form-horizontal" id="AddForm" method="post">
            		<input type="hidden" id="enumTypeId" name="enumTypeId"/>
             		<div class="row">
                		<div class="form-group" data-type="required" data-mark="编号">
                   			<label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>编号:</label>
                   			<div class="col-sm-10">
                      			<input type="text" class="form-control dp-vd w-p50"  id="enumId" name="enumId"/>
                         		<p class="dp-error-msg"></p>
                   			</div>
                		</div>
          			</div>
          			<div class="row">
                     	<div class="form-group" data-type="required"  id="gss_data" data-mark="描述">
                           	<label for="title" class="col-sm-2 control-label gss_name"><i class="required-mark">*</i>描述:</label>
                            <div class="col-sm-10">
                               	<input type="text" class="form-control dp-vd w-p50" id="description" name="description">
                              	<p class="dp-error-msg"></p>
                            </div>
                     	</div>
           			</div>
          			<div class="modal-footer">
		    			<button type="button"  id="btn_save" class="btn btn-primary">保存</button>
            			<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          			</div>
          		</form>
        	</div>
      	</div>
	</div>
</div>
<!--新增弹出框 end-->

<!-- 编辑弹出框 start-->
<div class="modal fade" id="modal_edit" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
	<div class="modal-dialog" role="document">
    	<div class="modal-content">
          	<div class="modal-header">
            	<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            	<h4 class="modal-title" id="h4_title"></h4>
          	</div>
          	<div class="modal-body">
            	<form class="form-horizontal" id="EditForm" method="post">
            		<input type="hidden" id="enumTypeId" name="enumTypeId"/>
             		<div class="row">
                		<div class="form-group" data-type="required" data-mark="编号">
                   			<label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>编号:</label>
                   			<div class="col-sm-10">
                      			<input type="text" class="form-control dp-vd w-p50" readonly id="enumId" name="enumId"/>
                         		<p class="dp-error-msg"></p>
                   			</div>
                		</div>
          			</div>
          			<div class="row">
                     	<div class="form-group" data-type="required"  id="gss_data" data-mark="描述">
                           	<label for="title" class="col-sm-2 control-label gss_name"><i class="required-mark">*</i>描述:</label>
                            <div class="col-sm-10">
                               	<input type="text" class="form-control dp-vd w-p50" id="description" name="description">
                              	<p class="dp-error-msg"></p>
                            </div>
                     	</div>
           			</div>
          			<div class="modal-footer">
		    			<button type="button"  id="btn_save" class="btn btn-primary">保存</button>
            			<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          			</div>
          		</form>
        	</div>
      	</div>
	</div>
</div>
<!--编辑弹出框 end-->

<!-- 提示弹出框start -->
<div id="modal_msg"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="modal_msg_title">操作提示</h4>
      </div>
      <div class="modal-body">
        <h4 id="modal_msg_body"></h4>
      </div>
      <div class="modal-footer">
        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
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
        <h4 class="modal-title" id="modal_confirm_title">删除提示</h4>
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
</div><!-- 删除确认弹出框end -->

<script>
var del_ids;
$(function(){
	var ajaxUrl="/systemMgr/control/GetEnumListByEnumTypeId?enumTypeId=FIELD_CANCEL";
	var data_tbl = $('#DataTbl').dataTable({
		ajaxUrl: ajaxUrl,
		columns:[
			<!-- 是否有删除权限-->
			<#if security.hasEntityPermission("FIELD_TABLESET", "_DELETE", session)>
			{"title":"复选框","code":"enumId","checked":true},
			</#if>
			{"title":"编号","code":"enumId"},
			{"title":"描述","code":"description"}
			<!-- 是否都有权限-->
			<#if security.hasEntityPermission("FIELD_TABLESET", "_UPDATE", session) || security.hasEntityPermission("FIELD_TABLESET", "_DELETE", session)>
			,
			{"title":"操作","code":"option",
			 "handle":function(td,record){
			 	var btnGroup = "<div class='btn-group'>"+
			 					<!-- 是否都有权限-->
			 					<#if security.hasEntityPermission("FIELD_TABLESET", "_UPDATE", session) && security.hasEntityPermission("FIELD_TABLESET", "_DELETE", session)>
	              			   "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:goEdit(\""+record.enumId+"\");'>编辑</button>"+
	              			   "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
	                		   "<span class='caret'></span>"+
	                		   "<span class='sr-only'>Toggle Dropdown</span>"+
	              			   "</button>"+
	              			   "<ul class='dropdown-menu' role='menu'>"+
	              			   "<li class='edit_li'><a href='javascript:goDel(\""+record.enumId+"\")'>删除</a></li>"+
	              		       "</ul>"+
	              		       <!-- 如果只有编辑权限-->
	              		       <#elseif security.hasEntityPermission("FIELD_TABLESET", "_UPDATE", session)>
	              		       "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:goEdit(\""+record.enumId+"\");'>编辑</button>"+
	              		       <!-- 如果只有删除权限-->
	              		       <#elseif security.hasEntityPermission("FIELD_TABLESET", "_DELETE", session)>
	              		       "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:goDel(\""+record.enumId+"\");'>删除</button>"+
	              		       </#if>
	                           "</div>";
	             td.html(btnGroup);
			 }
			}
			</#if>
		],
		listName: "recordsList",
		headNotShow: true
	});
	
	//左侧一级菜单的点击事件
	$(".first-menu a").click(function(e){
		if($(this).find('i').hasClass('glyphicon-chevron-down')){
			$(this).siblings('ul').slideUp('normal');
			$(this).find('i').removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
		}else{
			$(this).siblings('ul').slideDown('normal');
			$(this).find('i').removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
		}
	});
	
	//左侧二级菜单的点击事件
	$(".second-menu li").click(function(e){
		var li_id = $(this).attr("id");
		$('.first-menu').find('li.active').removeClass('active');
		$(this).addClass("active");
		ajaxUrl = changeURLArg(ajaxUrl,"enumTypeId",li_id);
        data_tbl.reload(ajaxUrl);
    });
    
    //新增按钮的点击事件
    $('#btn_add').click(function(){
    	var cur_li = $('.first-menu').find('li.active');
    	$('#modal_add #h4_title').text(cur_li.find('a').html()+"新增");
    	$('#modal_add #enumTypeId').val(cur_li.attr('id'));
    	$('#modal_add').modal();
    });
    
    //新增弹出窗关闭事件
	$('#modal_add').on('hide.bs.modal', function () {
		//清空form
	    clearForm($("#AddForm"));
	  	$('#AddForm').dpValidate({
	  		clear: true
	  	});
	})
	
	//新增弹出框保存按钮点击事件
    $('#modal_add #btn_save').click(function(){
        $("#AddForm").dpValidate({
    		clear: true
    	});
		$('#AddForm').submit();
    });
    
    //新增表单的校验方法
    $('#AddForm').dpValidate({
    	validate: true,
        callback: function(){
        	var enumId=$('#modal_add #enumId').val();
		  	$.ajax({
	            type: 'POST',
	            url: '/systemMgr/control/findEnumerationById?externalLoginKey=${externalLoginKey}',
	            data: {enumId: enumId},
	            dataType : "json",
	            success: function (data) {
	                if(data.isExits){
	                	//设置提示弹出框内容
	    				$('#modal_add #enumId').closest('.form-group').addClass('has-error');
	    				$('#modal_add #enumId').siblings('p').text('编号已存在！');
                	}else{
                		//异步调用新增方法
						$.ajax({
							url: "createEnumeration",
							type: "POST",
							data: $('#AddForm').serialize(),
							dataType : "json",
							success: function(data){
								//隐藏新增弹出窗口
								$('#modal_add').modal('toggle');
								//设置提示弹出框内容
								$('#modal_msg #modal_msg_body').html("保存成功!");
			    				$('#modal_msg').modal();
			    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
			    				$('#modal_msg').off('hide.bs.modal');
			    				$('#modal_msg').on('hide.bs.modal', function () {
			    					data_tbl.reload(ajaxUrl);
								})
							},
							error: function(data){
								//隐藏新增弹出窗口
								$('#modal_add').modal('toggle');
								//设置提示弹出框内容
								$('#modal_msg #modal_msg_body').html("保存失败！");
			    				$('#modal_msg').modal();
							}
						});
                	}
	        	}
	       	});
        }	
    });
    
    //修改弹出窗关闭事件
	$('#modal_edit').on('hide.bs.modal', function () {
	  	$('#EditForm').dpValidate({
	  		clear: true
	  	});
	})
	
	//修改弹出框保存按钮点击事件
    $('#modal_edit #btn_save').click(function(){
        $("#EditForm").dpValidate({
    		clear: true
    	});
		$('#EditForm').submit();
    });
    
    //修改表单的校验方法
    $('#EditForm').dpValidate({
    	validate: true,
        callback: function(){
               //异步调用修改方法
				$.ajax({
					url: "updateEnumeration",
					type: "POST",
					data: $('#EditForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#modal_edit').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("保存成功!");
	    				$('#modal_msg').modal();
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
	    					data_tbl.reload(ajaxUrl);
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#modal_edit').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("保存失败！");
	    				$('#modal_msg').modal();
					}
				}); 		
        }	
    });
    
    //删除按钮点击事件
    $('#btn_del').click(function(){
    	var checks = $('.js-checkparent .js-checkchild:checked');
    	//判断是否选中记录
    	if(checks.size() > 0 ){
    		del_ids = "";
	    	//编辑id字符串
    		checks.each(function(){ 
				del_ids += $(this).val() + ","; 
			});
			
			//设置删除弹出框内容
    		$('#modal_confirm #modal_confirm_body').html("确定删除此记录吗？");
    		$('#modal_confirm').modal('show');
    	}else{
    		//设置提示弹出框内容
    		$('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    		$('#modal_msg').modal();
    	}
    });
    
    //删除弹出框删除按钮点击事件
    $('#modal_confirm #ok').click(function(e){
		//异步调用删除方法
		$.ajax({
			url: "/systemMgr/control/delEnumeration?externalLoginKey=${externalLoginKey}",
			type: "GET",
			data: {ids : del_ids},
			dataType : "json",
			success: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("删除成功！");
				$('#modal_msg').modal();
				//提示弹出框隐藏事件，隐藏后重新加载当前页面
				$('#modal_msg').off('hide.bs.modal');
				$('#modal_msg').on('hide.bs.modal', function () {
					data_tbl.reload(ajaxUrl);
				})
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("网络异常！");
				$('#modal_msg').modal();
			}
		});
    });
});

//修改点击方法
function goEdit(id){
  	$.ajax({
    	type: 'post',
    	url: '/systemMgr/control/findEnumerationById?externalLoginKey=${externalLoginKey}',
    	data: {enumId: id},
    	success: function (data) {
        	$("#modal_edit #enumId").val(data.Enumeration.enumId);
            $("#modal_edit #enumTypeId").val(data.Enumeration.enumTypeId);
            $("#modal_edit #description").val(data.Enumeration.description);
            var cur_li = $('.first-menu').find('li.active');
    		$('#modal_edit #h4_title').text(cur_li.find('a').html()+"编辑");
    		$('#modal_edit #enumTypeId').val(cur_li.attr('id'));
            $("#modal_edit").modal();
        }
  	});
}   

//删除点击方法
function goDel(id){
	del_ids = id;
	//设置删除弹出框内容
	$('#modal_confirm #modal_confirm_body').html("确定删除此记录吗？");
	$('#modal_confirm').modal('show');
}
		
</script>