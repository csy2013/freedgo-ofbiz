<!-- 内容start -->
<div class="box box-info">
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="QueryForm" class="form-inline clearfix">
      <div class="form-group">
        <div class="input-group m-b-10">
          <span class="input-group-addon">活动编码</span>
          <input type="text" id="activityId" class="form-control" value="">
        </div>
        <div class="input-group m-b-10">
          <span class="input-group-addon">活动名称</span>
          <input type="text" id="activityName" class="form-control" value="">
        </div>
        <div class="input-group m-b-10">
          <span class="input-group-addon">活动类型</span>
          <select id="activityType" class="form-control">
            <option value="" selected>====请选择====</option>
            <#if activityTypeList?has_content>
	            <#list activityTypeList as activityType>
	            	<option value="${activityType.enumId}">${activityType.description}</option>
	            </#list>
            </#if>
          </select>
        </div>
        <div class="input-group m-b-10">
          <span class="input-group-addon">活动状态</span>
          <select id="activityAuditStatus" class="form-control">
            <option value="" selected>====请选择====</option>
            <option value="ACTY_AUDIT_UNBEGIN">未开始</option>
            <option value="ACTY_AUDIT_DOING">进行中</option>
            <option value="ACTY_AUDIT_END">已结束</option>
            <option value="ACTY_AUDIT_OFF">已下架</option>
          </select>
        </div>
      </div>
      <div class="input-group pull-right">
        <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
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

<script type="text/javascript">
	var data_tbl;
	var ajaxUrl = "/prodPromo/control/GetActivityCollectionList";
	var del_ids;
	
	$(function(){
		data_tbl = $('#data_tbl').dataTable({
			ajaxUrl: ajaxUrl,
			columns:[
				{"title":"活动编码","code":"activityId","sort":true},
				{"title":"活动名称","code":"activityName","sort":true},
				{"title":"活动类型","code":"activityTypeName","sort":true},
				{"title":"活动状态","code":"activityAuditStatus"},
				{"title":"产品编码","code":"productId"},
				{"title":"收藏次数","code":"collectionNum"},
				{"title":"操作","code":"option",
					 "handle":function(td,record){
					 	var btns = "<div class='btn-group'>"+
	  							   "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:goInfo(\""+record.activityId+"\");'>查看详情</button>"+
							  	   "</div>";
						td.append(btns);
					 }
				}
			],
			listName: "recordsList",
			paginateEL: "paginateDiv",
			viewSizeEL: "view_size"
		});
		
		//查询按钮点击事件
		$('#QueryForm #searchBtn').on('click',function(){
			var activityId = $('#QueryForm #activityId').val();
			var activityName = $('#QueryForm #activityName').val();
			var activityType = $('#QueryForm #activityType').val();
			var activityAuditStatus = $('#QueryForm #activityAuditStatus').val();
			
			ajaxUrl = changeURLArg(ajaxUrl,"activityId",activityId);
			ajaxUrl = changeURLArg(ajaxUrl,"activityName",activityName);
			ajaxUrl = changeURLArg(ajaxUrl,"activityType",activityType);
			ajaxUrl = changeURLArg(ajaxUrl,"activityAuditStatus",activityAuditStatus);
			data_tbl.reload(ajaxUrl);
			return false;
		});
	});
	
	function goInfo(id){
	     $.dataSelectModal({
			url: "/prodPromo/control/collectionPartyListModalPage?externalLoginKey=${externalLoginKey}&activityId="+id,
			width:	"800",
			title:	"收藏人员列表"
		});
	}
</script>