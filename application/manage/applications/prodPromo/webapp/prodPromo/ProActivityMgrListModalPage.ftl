<!-- 内容start -->
<div class="box box-info">
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="ProActityMgrFM_QueryForm" class="form-inline clearfix">
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
        </div>
      </div><!-- 操作按钮组end -->
      
      <!-- 列表当前分页条数start -->
      <div class="col-sm-6">
        <div id="ProActityMgrFM_ViewSize" class="dp-tables_length">
        </div>
      </div><!-- 列表当前分页条数end -->
    </div><!-- 工具栏end -->
    
	<!-- 表格区域start -->
    <div class="row">
      <div class="col-sm-12">
        <table id="ProActityMgrFM_DataTbl" class="table table-bordered table-hover js-checkparent">
        </table>
      </div>
    </div><!-- 表格区域end -->
    
    <!-- 分页条start -->
    <div class="row" id="ProActityMgrFM_Paginate">
	</div><!-- 分页条end -->
  </div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script>
	var ProActityMgrFM_data_tbl;
	var ProActityMgrFM_ajaxUrl = "/prodPromo/control/getActivityMgrListForJson?VIEW_SIZE=5";
	$(function(){
		ProActityMgrFM_data_tbl = $('#ProActityMgrFM_DataTbl').dataTable({
			ajaxUrl: ProActityMgrFM_ajaxUrl,
			columns:[
				{"title":"专题名称","code":"activityManagerName"},
//				{"title":"使用模板","code":"templateId","sort":true,
//				 "handle":function(td,record){
//				 	td.empty();
//				 	if(record.templateId == "1"){
//				 		td.append("图文专题模板");
//				 	}else{
//				 		td.append("图片专题模板");
//				 	}
//				 }
//				},
//                {"title":"创建时间","code":"createdStamp","handle":function(td,record){
//                    td.html((new Date(record.createdStamp.time)).Format("yyyy-MM-dd hh:mm:ss"));
//                }},
                {"title":"栏目","code":"columnName"},
				{"title":" ","code":"option",
				 "handle":function(td,record){
				 	var id = record.productActivityManagerId;
				 	var name = record.activityManagerName;
				 	var btn = $("<div class='btn-group'>"+
  							  "<button type='button' class='btn btn-danger btn-sm btn-select' data-id='"+id+"' data-name='"+name+"' data-record='"+JSON.stringify(record)+"'>选择</button>"+
						      "</div>");
				 	td.append(btn);
				 }
				}
			],
			listName: "recordsList",
			paginateEL: "ProActityMgrFM_Paginate",
			headNotShow: true,
			midShowNum: 3
		});
		
		//查询按钮点击事件
		$('#ProActityMgrFM_QueryForm #searchBtn').on('click',function(){
			var activityManagerName = $('#ProActityMgrFM_QueryForm #activityManagerName').val();
			ProActityMgrFM_ajaxUrl = changeURLArg(ProActityMgrFM_ajaxUrl,"activityManagerName",activityManagerName);
			ProActityMgrFM_data_tbl.reload(ProActityMgrFM_ajaxUrl);
			return false;
		});
	});
</script><!-- script区域end -->
