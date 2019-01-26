
<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
		<!-- 条件查询start -->
		<form id="PartyFM_QueryForm" class="form-inline clearfix">
        	<div class="form-group">
            	<div class="input-group m-b-10">
        			<span class="input-group-addon">会员编码</span>
                    <input type="text" class="form-control" id="partyId">
        		</div>
		        <div class="input-group m-b-10">
		        	<span class="input-group-addon">会员名称</span>
                    <input type="text" class="form-control" id="name">
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
	var PartyFM_ajaxUrl = "/membermgr/control/personListForJson?VIEW_SIZE=5";
	$(function(){
		PartyFM_data_tbl = $('#PartyFM_data_tbl').dataTable({
			ajaxUrl: PartyFM_ajaxUrl,
			columns:[
				{"title":"会员编码","code":"partyId"},
				{"title":"会员名称","code":"name"},
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
			listName: "recordsList",
			paginateEL: "PartyFM_paginateDiv",
			headNotShow: true,
			midShowNum: 3
		});
		
		//查询按钮点击事件
		$('#PartyFM_QueryForm #searchBtn').on('click',function(){
			var partyId = $('#PartyFM_QueryForm #partyId').val();
			var name = $('#PartyFM_QueryForm #name').val();
            var mobile = $('#PartyFM_QueryForm #mobile').val();
			
			PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl,"partyId",partyId);
			PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl,"name",name);
            PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl,"mobile",mobile);
			PartyFM_data_tbl.reload(PartyFM_ajaxUrl);
			return false;
		});
		
	});
</script><!-- script区域end -->
