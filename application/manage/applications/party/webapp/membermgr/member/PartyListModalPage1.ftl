
<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
		<!-- 条件查询start -->
		<form id="PartyFM_QueryForm" class="form-inline clearfix">
        	<div class="form-group">
            	<div class="input-group m-b-10">
        			<span class="input-group-addon">顾问名称</span>
                    <input type="text" class="form-control" id="userName">
        		</div>
		        <div class="input-group m-b-10">
		        	<span class="input-group-addon">联系方式</span>
                    <input type="text" class="form-control" id="mobile">
		        </div>
				<div class="input-group pull-right m-l-10">
					<button id="searchBtn" class="btn btn-success btn-flat">${uiLabelMap.BrandSearch}</button>
				</div>
			</div>
    	</form><!-- 条件查询end -->

		<!-- 分割线start -->
	    <div class="cut-off-rule bg-gray"></div>
	    <!-- 分割线end -->
		
		<!-- 表格区域start -->
		<div class="row">
      		<div class="col-sm-12">
        		<table id="PartyFM_data_tbl" class="table table-bordered table-hover js-checkparent"></table>
      		</div>
    	</div><!-- 表格区域end -->
    	
    	<!-- 分页条start -->
    	<div class="row" id="PartyFM_paginateDiv">
		</div><!-- 分页条end -->
	</div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script>
	var PartyFM_data_tbl;
	var PartyFM_ajaxUrl = "/systemMgr/control/managerListForJson?VIEW_SIZE=5&groupId=FWGW";
	$(function(){
		PartyFM_data_tbl = $('#PartyFM_data_tbl').dataTable({
			ajaxUrl: PartyFM_ajaxUrl,
            dataType: 'jsonp',
            columns:[
				{"title":"用户名","code":"userLoginId"},
				{"title":"顾问名称","code":"name"},
				{"title":"联系方式","code":"mobile"},
				{"title":" ","code":"option",
				 "handle":function(td,record){
				 	var id = record.partyId;
				 	var name = record.name;
				    var mobile = record.mobile;
				 	var btn = $("<div class='btn-group'>"+
  							  "<button type='button' class='btn btn-danger btn-sm btn-select' data-id='"+id+"' data-name='"+name+"' data-mobile='"+mobile+"'>选择</button>"+
						      "</div>");
				 	td.append(btn);
				 }
				}
			],
			listName: "userList",
			paginateEL: "PartyFM_paginateDiv",
			headNotShow: true,
			midShowNum: 3
		});
		
		//查询按钮点击事件
		$('#PartyFM_QueryForm #searchBtn').on('click',function(){
			var userName = $('#PartyFM_QueryForm #userName').val();
			var mobile = $('#PartyFM_QueryForm #mobile').val();

			PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl,"userName",userName);
			PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl,"mobile",mobile);
			PartyFM_data_tbl.reload(PartyFM_ajaxUrl);
			return false;
		});
		
	});
</script><!-- script区域end -->
