<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<style>
    .express_area {
    	width : 400px;
        position: relative;
        height: 25px;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .express_area p {
    	width : 400px;
        position: absolute;
        left: 0;
        top: 0;
        display: block;
        padding: 0 10px;
        line-height: 25px;
        word-wrap: break-word;
    }

    .express_area:hover {
        overflow: visible;
    }

    .express_area:hover p {
        background: #DFEDF7;
        z-index: 99;
    }
</style>

<!-- 内容start -->
<div class="box box-info">
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="OrderStatistics_QueryForm" class="form-inline clearfix">
      <div class="form-group">
      	<div class="input-group m-b-10 date form_date">
            <span class="input-group-addon">开始时间</span>
            <input class="form-control" size="16" type="text" readonly id="startTime">
            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
            <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
        </div>
        <div class="input-group m-b-10 input-group date form_date">
            <span class="input-group-addon">结束时间</span>
            <input class="form-control" size="16" type="text" readonly id="endTime">
            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
            <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
        </div>
        <div class="input-group m-b-10">
            <span class="input-group-addon">社区</span>
            <select id="communityId" class="form-control">
                <option value="">全部</option>
                <#list communitys as c >
                    <option value="${c.communityId}" <#if (paramMap.communityId)?default("") == "${c.communityId}"> selected="selected" </#if>>${c.name}</option>
                </#list>
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
    
	<!-- tab选项卡begin -->
	<ul id="OrderStatistics_Nav" class="nav nav-tabs" style="margin-bottom: 10px;">
		<li class="active"><a id="business" href="javascript:void(0);">商家统计</a></li>
		<li><a id="product" href="javascript:void(0);">商品统计</a></li>
	</ul><!-- tab选项卡end -->

	<!--工具栏start -->
    <div class="row m-b-10">      
      <!-- 操作按钮组start -->
      <div class="col-sm-6">
        <div class="dp-tables_btn">
          <!-- 是否有审核权限-->
          <button id="btn_export" class="btn btn-primary">
           		批量导出
          </button>
        </div>
      </div><!-- 操作按钮组end -->
    
      <!-- 列表当前分页条数start -->
      <div class="col-sm-6">
        <div id="OrderStatistics_ViewSize" class="dp-tables_length">
        </div>
      </div><!-- 列表当前分页条数end -->
    </div><!-- 工具栏end -->
    
	<!-- 表格区域start -->
    <div class="row">
      <div class="col-sm-12">
        <table id="OrderStatistics_DataTbl" class="table table-bordered table-hover js-checkparent">
        </table>
      </div>
    </div><!-- 表格区域end -->
    <!-- 分页条start -->
    <div class="row" id="OrderStatistics_Paginate">
	</div><!-- 分页条end -->
  </div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script>
var OrderStatistics_DataTbl;
var ajaxUrl = "getOrderStatisticsList?groupBy=business";
$(function(){
	$('.form_date').datetimepicker({
		format : 'yyyy-mm-dd',
        language:  'zh-CN',
        todayBtn:  1,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        minView: 2,
        forceParse: 0
    });

	OrderStatistics_DataTbl = $('#OrderStatistics_DataTbl').dataTable({
		ajaxUrl: ajaxUrl,
		columns:[
			{"title":"商家名称","code":"BUSINESS_NAME"},
			{"title":"销售金额","code":"sales_money"},
			{"title":"订单总数","code":"order_num"},
			{"title":"商品总数","code":"product_num"},
			{"title":"社区","code":"communityName","width":"400px",
				 "handle":function(td,record){
				 	if(record.communityName!=null){
				 		td.empty();
				 		var div = $("<div class='express_area'></div>");
				 		var express_area_nobr = $("<nobr>"+record.communityName+"</nobr>");
				 		var express_area_p = $("<p style='display:none;'>"+record.communityName+"</p>");
				 		div.append(express_area_nobr);
				 		div.append(express_area_p);
				 		td.append(div);
				 	}
				 }
			}
		],
		listName: "recordsList",
		paginateEL: "OrderStatistics_Paginate",
		viewSizeEL: "OrderStatistics_ViewSize"
	});
	
	//文字溢出
	$(document).on("mouseover",'.express_area',function(){
		$(this).find("nobr").hide();
		$(this).find("p").show();
	});
	
	$(document).on("mouseout",'.express_area',function(){
		$(this).find("p").hide();
		$(this).find("nobr").show();
	});
		
	//查询按钮点击事件
	$('#OrderStatistics_QueryForm #searchBtn').on('click',function(){
		var startTime = $('#OrderStatistics_QueryForm #startTime').val();
		var endTime = $('#OrderStatistics_QueryForm #endTime').val();
		var communityId = $('#OrderStatistics_QueryForm #communityId').val();
		
		ajaxUrl = changeURLArg(ajaxUrl,"startTime",startTime);
		ajaxUrl = changeURLArg(ajaxUrl,"endTime",endTime);
		ajaxUrl = changeURLArg(ajaxUrl,"communityId",communityId);
		OrderStatistics_DataTbl.reload(ajaxUrl);
		return false;
	});
	
	//切换选项卡
	$('#OrderStatistics_Nav a').click(function(e){
		if(!$(this).closest('li').hasClass('active')){
			$(this).closest('li').addClass('active').siblings('.active').removeClass('active');
		}
		var groupBy = $(this).attr('id');
		var startTime = $('#OrderStatistics_QueryForm #startTime').val();
		var endTime = $('#OrderStatistics_QueryForm #endTime').val();
		var communityId = $('#OrderStatistics_QueryForm #communityId').val();
		ajaxUrl = changeURLArg(ajaxUrl,"startTime",startTime);
		ajaxUrl = changeURLArg(ajaxUrl,"endTime",endTime);
		ajaxUrl = changeURLArg(ajaxUrl,"communityId",communityId);
		ajaxUrl = changeURLArg(ajaxUrl,"groupBy",groupBy);
		
		OrderStatistics_DataTbl.empty();
		if(groupBy=="product"){
			OrderStatistics_DataTbl=$('#OrderStatistics_DataTbl').dataTable({
				ajaxUrl: ajaxUrl,
				columns:[
					{"title":"商品名称","code":"PRODUCT_NAME"},
					{"title":"销售数量","code":"product_num"},
					{"title":"社区","code":"communityName","width":"400px",
					 "handle":function(td,record){
					 	if(record.communityName!=null){
					 		td.empty();
					 		var div = $("<div class='express_area'></div>");
					 		var express_area_nobr = $("<nobr>"+record.communityName+"</nobr>");
					 		var express_area_p = $("<p style='display:none;'>"+record.communityName+"</p>");
					 		div.append(express_area_nobr);
					 		div.append(express_area_p);
					 		td.append(div);
					 	}
					 }
					}
				],
				listName: "recordsList",
				paginateEL: "OrderStatistics_Paginate",
				viewSizeEL: "OrderStatistics_ViewSize"
			});
		}else{
			OrderStatistics_DataTbl=$('#OrderStatistics_DataTbl').dataTable({
				ajaxUrl: ajaxUrl,
				columns:[
					{"title":"商家名称","code":"BUSINESS_NAME"},
					{"title":"销售金额","code":"sales_money"},
					{"title":"订单总数","code":"order_num"},
					{"title":"商品总数","code":"product_num"},
					{"title":"社区","code":"communityName","width":"400px",
					 "handle":function(td,record){
					 	if(record.communityName!=null){
					 		td.empty();
					 		var div = $("<div class='express_area'></div>");
					 		var express_area_nobr = $("<nobr>"+record.communityName+"</nobr>");
					 		var express_area_p = $("<p style='display:none;'>"+record.communityName+"</p>");
					 		div.append(express_area_nobr);
					 		div.append(express_area_p);
					 		td.append(div);
					 	}
					 }
					}
				],
				listName: "recordsList",
				paginateEL: "OrderStatistics_Paginate",
				viewSizeEL: "OrderStatistics_ViewSize"
			});
		}
	});
	
	//导出
	$("#btn_export").click(function(){
		loading();
		var url = "exportOrderStatistics?" + ajaxUrl.split("?")[1];
		var groupBy = $("#OrderStatistics_Nav li.active a").attr("id");
		url = changeURLArg(url,"groupBy",groupBy);
		$.post("updateSessionForParam${externalKeyParam}",{attrName:"exportOrderListStatus",attrVal:"false"},function(data){
			window.location.href = url;
			var timer;
		    timer = window.setInterval(function() {
		    	$.post("getSessionForParam${externalKeyParam}",{attrName:"exportOrderListStatus"},function(data){
		    		if(data.attrVal == "success"){
			    		window.clearTimeout(timer);
			    		loading("hide");
			    		$.tipLayer("导出成功！");
			    	}
		    	});
		    }, 1000);
		});
	});
});
	
</script><!-- script区域end -->
