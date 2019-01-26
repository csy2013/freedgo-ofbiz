<style>

    .express_area {
        position: relative;
        height: 25px;
        overflow-y: hidden;
        z-index: 99;
    }

    .express_area p {
        position: absolute;
        left: 0;
        top: 0;
        display: block;
        padding: 0 10px;
        line-height: 25px;
    }

    .express_area:hover {
        overflow-y: visible;
    }

    .express_area:hover p {
        background: #DFEDF7;
    }
</style>
<!-- 内容start -->
<div class="box box-info">
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="QueryForm" class="form-inline clearfix">
      <div class="form-group">
        <div class="input-group m-b-10">
          <span class="input-group-addon">公告标题</span>
          <input type="text" id="noticeTitle" class="form-control" value="">
        </div>
      </div>
      <div class="input-group pull-right">
        <button id="searchBtn" class="btn btn-success btn-flat">${uiLabelMap.BrandSearch}</button>
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
			<#if security.hasEntityPermission("NOTICEMGR_LIST", "_CREATE", session)>
	          <button id="btn_add" class="btn btn-primary">
	            <i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}
	          </button>
	        </#if>
	        <!-- 是否有删除权限-->
			<#if security.hasEntityPermission("NOTICEMGR_LIST", "_DEL", session)>
	          <button id="btn_del" class="btn btn-primary">
	            <i class="fa fa-trash"></i>${uiLabelMap.BrandDel}
	          </button>
	        </#if>
        </div>
      </div><!-- 操作按钮组end -->
      <!-- 列表当前分页条数start -->
      <div class="col-sm-6">
        <div id="view_size" class="dp-tables_length">
        </div>
      </div><!-- 列表当前分页条数end -->
    </div><!-- 工具栏end -->
	<!-- 表格区域start -->
    <div class="row">
      <div class="col-sm-12">
        <table id="data_tbl" class="table table-bordered table-hover js-checkparent">
        </table>
      </div>
    </div><!-- 表格区域end -->
    <!-- 分页条start -->
    <div class="row" id="paginateDiv">
	</div><!-- 分页条end -->
  </div><!-- /.box-body -->
</div><!-- 内容end -->

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

<!-- script区域start -->
<script>
	var data_tbl;
	var ajaxUrl = "NoticeList";
	var del_ids;
	
	$(function(){
		data_tbl = $('#data_tbl').dataTable({
			ajaxUrl: ajaxUrl,
			columns:[
				{"title":"公告ID","code":"noticeId","checked":true},
				{"title":"公告标题","code":"noticeTitle"},
				{"title":"链接地址","code":"linkUrl"},
				{"title":"是否启用","code":"isUse",
				 "handle":function(td,record){
				 	td.empty();
				 	if(record.isUse == "0"){
				 		td.append("<button type='button' onclick='javascript:changeIsUse("+record.noticeId+",1)' class='btn btn-primary'>是</button>");
				 	}else{
				 		td.append("<button type='button' onclick='javascript:changeIsUse("+record.noticeId+",0)' class='btn btn-default'>否</button>");
				 	}
				 }},
				{"title":"序号","code":"sequenceId"},
				{"title":"站点","code":"webSiteName"}
				<!-- 是否有编辑权限-->
				<#if security.hasEntityPermission("NOTICEMGR_LIST", "_UPDATE", session)>
					,
					{"title":"操作","code":"option",
					 "handle":function(td,record){
					 	var btns = "<div class='btn-group'>"+
	  							   "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:editInit("+record.noticeId+");'>编辑</button>"+
							  	   "</div>";
						td.append(btns);
					 }
					}
				</#if>
			],
			listName: "noticeList",
			paginateEL: "paginateDiv",
			viewSizeEL: "view_size"
		});
		
		//查询按钮点击事件
		$('#QueryForm #searchBtn').on('click',function(){
			var noticeTitle = $('#QueryForm #noticeTitle').val();
			ajaxUrl = changeURLArg(ajaxUrl,"noticeTitle",noticeTitle);
			data_tbl.reload(ajaxUrl);
			return false;
		});
		
		//添加按钮点击事件
	    $('#btn_add').click(function(){
	    	window.location = '<@ofbizUrl>NoticeAddPage</@ofbizUrl>';
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
				url: "NoticeDel",
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
						data_tbl.reload(ajaxUrl);
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
	
	//点击是否启用按钮事件
	function changeIsUse(id,val){
		//异步加载已选地区数据
		$.ajax({
			url: "EditNoticeIsUse",
			type: "GET",
			data : {noticeId:id,isUse:val},
			dataType : "json",
			success: function(data){
				data_tbl.reload(ajaxUrl);
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
				$('#modal_msg').modal();
			}
		});
	}
	
	//点击编辑按钮事件
    function editInit(id){
    	window.location = '<@ofbizUrl>NoticeEditPage</@ofbizUrl>?noticeId='+id;
    }
</script><!-- script区域end -->
