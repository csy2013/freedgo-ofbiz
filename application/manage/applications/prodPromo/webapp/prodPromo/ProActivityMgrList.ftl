<!-- 内容start -->
<div class="box box-info">
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="ProActityMgr_QueryForm" class="form-inline clearfix">
      <div class="form-group">
        <div class="input-group m-b-10">
          <span class="input-group-addon">专题名称</span>
          <input type="text" id="activityManagerName" class="form-control" value="">
        </div>

        <div class="input-group pull-right m-l-10">
			<button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
		</div>
      </div>
    </form><!-- 条件查询end -->

    <!-- 分割线start -->
    <div class="cut-off-rule bg-gray"></div>
    <!-- 分割线end -->

    <!--工具栏start -->
    <div class="row m-b-10">
      <!-- 操作按钮组start -->
      <div class="col-sm-6">
        <div class="dp-tables_btn">
          <!-- 是否有新增权限-->
		  <#if security.hasEntityPermission("ACTIVITYMGR_LIST", "_CREATE", session)>
		  <button id="btn_add" class="btn btn-primary" >
		   		添加
		  </button>
		  </#if>
		  <!-- 是否有删除权限-->
		  <#if security.hasEntityPermission("ACTIVITYMGR_LIST", "_DEL", session)>
          <button id="btn_del" class="btn btn-primary" >
            	删除
          </button>
          </#if>
        </div>
      </div><!-- 操作按钮组end -->

      <!-- 列表当前分页条数start -->
      <div class="col-sm-6">
        <div id="ProActityMgr_ViewSize" class="dp-tables_length">
        </div>
      </div><!-- 列表当前分页条数end -->
    </div><!-- 工具栏end -->

	<!-- 表格区域start -->
    <div class="row">
      <div class="col-sm-12">
        <table id="ProActityMgr_DataTbl" class="table table-bordered table-hover js-checkparent">
        </table>
      </div>
    </div><!-- 表格区域end -->

    <!-- 分页条start -->
    <div class="row" id="ProActityMgr_Paginate">
	</div><!-- 分页条end -->
  </div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- 提示弹出框start -->
<div id="modal_msg"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="modal_msg_title">操作信息</h4>
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

<!-- script区域start -->
<script>
var ProActityMgr_DataTbl;
var ProActityMgr_AjaxUrl = "/prodPromo/control/getActivityMgrListForJson";
var del_ids;

$(function(){
	ProActityMgr_DataTbl = $('#ProActityMgr_DataTbl').dataTable({
		ajaxUrl: ProActityMgr_AjaxUrl,
		columns:[
			{"title":"复选框","code":"productActivityManagerId","checked":true},
			{"title":"专题名称","code":"activityManagerName"},
            {"title":"栏目","code":"columnName"},
            {"title":"开始时间","code":"fromDate"},
            {"title":"结束时间","code":"thruDate"},
            {"title":"是否启用","code":"isUsed",
                "handle": function (td, record) {
                    if (record.isUsed == "Y") {
                        td.html("是");
                    }else {
                        td.html("否");
                    }
                }
            },
            {"title":"序号","code":"sequenceId"},
			{"title":"操作","code":"option",
			 "handle":function(td,record){
			 	var btnGroup = "<div class='btn-group'>"+
	              			   "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:goView("+record.productActivityManagerId+")'>查看</button>"+
	              			   "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
	                		   "<span class='caret'></span>"+
	                		   "<span class='sr-only'>Toggle Dropdown</span>"+
	              			   "</button>"+
	              			   "<ul class='dropdown-menu' role='menu'>"+
	              			   <!-- 是否有编辑权限-->
							   <#if security.hasEntityPermission("ACTIVITYMGR_LIST", "_UPDATE", session)>
	              			   "<li class='edit_li'><a href='javascript:goEdit(\""+record.productActivityManagerId+"\")'>编辑</a></li>"+
	              			   </#if>
	              			   <!-- 是否有删除权限-->
							   <#if security.hasEntityPermission("ACTIVITYMGR_LIST", "_DEL", session)>
	              			   "<li class='edit_li'><a href='javascript:goDel(\""+record.productActivityManagerId+"\")'>删除</a></li>"+
	              			   </#if>
	              		       "</ul>"+
	                           "</div>";
	             td.html(btnGroup);
			 }
			}
		],
		listName: "recordsList",
		paginateEL: "ProActityMgr_Paginate",
		viewSizeEL: "ProActityMgr_ViewSize"
	});

	//查询按钮点击事件
	$('#ProActityMgr_QueryForm #searchBtn').on('click',function(){
		var activityManagerName = $('#ProActityMgr_QueryForm #activityManagerName').val();
		ProActityMgr_AjaxUrl = changeURLArg(ProActityMgr_AjaxUrl,"activityManagerName",activityManagerName);
		ProActityMgr_DataTbl.reload(ProActityMgr_AjaxUrl);
		return false;
	});

	//添加按钮点击事件
	$('#btn_add').click(function(){
		window.location = '<@ofbizUrl>activityMgrAddPage</@ofbizUrl>';
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
    		$('#modal_confirm #modal_confirm_body').html("确认删除勾选的记录吗？");
    		$('#modal_confirm').modal('show');
    	}else{
    		//设置提示弹出框内容
    		$('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
    		$('#modal_msg').modal();
    	}
    });

    //删除弹出框删除按钮点击事件
    $('#modal_confirm #ok').click(function(e){
		//异步调用删除方法
		$.ajax({
			url: "/prodPromo/control/activityMgrDel?externalLoginKey=${externalLoginKey}",
			type: "GET",
			data: {ids : del_ids},
			dataType : "json",
			success: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
				$('#modal_msg').modal();
				//提示弹出框隐藏事件，隐藏后重新加载当前页面
				$('#modal_msg').off('hide.bs.modal');
				$('#modal_msg').on('hide.bs.modal', function () {
					ProActityMgr_DataTbl.reload(ProActityMgr_AjaxUrl);
				})
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
				$('#modal_msg').modal();
			}
		});
    });
});

//跳转修改页面
function goEdit(id){
	window.location = '<@ofbizUrl>activityMgrEditPage</@ofbizUrl>?activityManagerId='+id;
}

//跳转查看页面
function goView(id){
    window.location = '<@ofbizUrl>activityMgrViewPage</@ofbizUrl>?activityManagerId='+id;
}

//单项删除
function goDel(id){
	del_ids = id;
	//设置删除弹出框内容
	$('#modal_confirm #modal_confirm_body').html("确认删除该条记录吗？");
	$('#modal_confirm').modal('show');
}

</script><!-- script区域end -->
