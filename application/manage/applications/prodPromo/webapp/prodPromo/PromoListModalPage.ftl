<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
		<!-- 条件查询start -->
		<form id="PromoFM_QueryForm" class="form-inline clearfix">
        	<div class="form-group">
            	<div class="input-group m-b-10">
            		<span class="input-group-addon">促销类型</span>
        			<select id="activityType" class="form-control">
        				<option value="">======&nbsp;请选择&nbsp;======</option>
        				<#assign enumList = delegator.findByAnd("Enumeration", {"enumTypeId" :"ACTY_TYPE"}, Static["org.ofbiz.base.util.UtilMisc"].toList("sequenceId"))>
        				<#if enumList?has_content>
        					<#list enumList as enum>
			                  <option value="${enum.enumId}">${enum.description}</option>
			                </#list>
        				</#if>
        			</select>
        		</div>
		        <#--<div class="input-group m-b-10">-->
		        	<#--<span class="input-group-addon">社区名称</span>-->
                    <#--<input type="text" class="form-control" id="activityArea">-->
		        <#--</div>-->
			</div>
        	<div class="form-group">
            	<div class="input-group m-b-10">
        			<span class="input-group-addon">促销名称</span>
                    <input type="text" class="form-control" id="activityName">
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
        		<table id="PromoFM_data_tbl" class="table table-bordered table-hover js-checkparent"></table>
      		</div>
    	</div><!-- 表格区域end -->
    	
    	<!-- 分页条start -->
    	<div class="row" id="PromoFM_paginateDiv">
		</div><!-- 分页条end -->
	</div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script>
	var PromoFM_data_tbl;
	var PromoFM_ajaxUrl = "/prodPromo/control/PromoListForJson?VIEW_SIZE=5";
	$(function(){
		PromoFM_data_tbl = $('#PromoFM_data_tbl').dataTable({
			ajaxUrl: PromoFM_ajaxUrl,
			columns:[
				{"title":"促销类型","code":"activityTypeName"},
				{"title":"促销名称","code":"activityName"},
//				{"title":"社区","code":"communityName"},
				{"title":"状态","code":"audityStatus"},
				{"title":" ","code":"option",
				 "handle":function(td,record){
				 	var id = record.activityId;
				 	var name = record.activityName;
				 	var btn = $("<div class='btn-group'>"+
  							  "<button type='button' class='btn btn-danger btn-sm btn-select' data-id='"+id+"' data-name='"+name+"' data-record='"+JSON.stringify(record)+"'>选择</button>"+
						      "</div>");
				 	td.append(btn);
				 }
				}
			],
			listName: "recordsList",
			paginateEL: "PromoFM_paginateDiv",
			headNotShow: true,
			midShowNum: 3
		});
		
		//查询按钮点击事件
		$('#PromoFM_QueryForm #searchBtn').on('click',function(){
			var activityType = $('#PromoFM_QueryForm #activityType').val();
//			var activityArea = $('#PromoFM_QueryForm #activityArea').val();
			var activityName = $('#PromoFM_QueryForm #activityName').val();
			
			PromoFM_ajaxUrl = changeURLArg(PromoFM_ajaxUrl,"activityType",activityType);
//			PromoFM_ajaxUrl = changeURLArg(PromoFM_ajaxUrl,"activityArea",activityArea);
			PromoFM_ajaxUrl = changeURLArg(PromoFM_ajaxUrl,"activityName",activityName);
			PromoFM_data_tbl.reload(PromoFM_ajaxUrl);
			return false;
		});
	});
</script><!-- script区域end -->
