
<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
		<!-- 条件查询start -->
		<form id="PartyFM_QueryForm" class="form-inline clearfix">
        	<div class="row">
        	<div class="form-group col-sm-6">
               <label for="subTitle" class="col-sm-4 control-label">优惠券编码:</label>
               <div class="col-sm-3">
               <span id="couponCode">${parameters.couponCode?if_exists}</span></div>
            </div>
            <div class="form-group col-sm-6">
               <label for="subTitle" class="col-sm-4 control-label">优惠券数量:</label>
               <div class="col-sm-3">
               <span id="couponCode">${parameters.couponQuantity?if_exists}</span></div>
            </div>
            </div>
            <div class="row p-t-10">
        	<div class="form-group col-sm-6">
               <label for="subTitle" class="col-sm-4 control-label">已发放数量:</label>
               <div class="col-sm-3">
               <span id="couponCode">${parameters.userCount?if_exists}</span></div>
            </div>
            <div class="form-group col-sm-6">
               <label for="subTitle" class="col-sm-4 control-label">已消费数量:</label>
               <div class="col-sm-3">
               <span id="couponCode">${parameters.orderCount?if_exists}</span></div>
            </div>
            </div>
        	
        	<div class="form-group p-t-10 w-p100">
            	<div class="input-group m-b-10">
        			<span class="input-group-addon">领取人</span>
                    <input type="text" class="form-control" id="userLoginId">
        		</div>
		        <div class="input-group m-b-10">
		        	<span class="input-group-addon">优惠券状态</span>
                    <select class="form-control  dp-vd" id="codeStatus" name="codeStatus">
                        <option value="">全部</option>
                        <option value="COUPON_CODE_GET">未使用</option>
                        <option value="COUPON_CODE_USED">已使用</option>
                        <option value="COUPON_CODE_DISABLE">已失效</option>
                    </select>
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
	var PartyFM_ajaxUrl = "/prodPromo/control/findCouponCodeJson?VIEW_SIZE=5&&couponCode="+${parameters.couponCode};
	$(function(){
		PartyFM_data_tbl = $('#PartyFM_data_tbl').dataTable({
			ajaxUrl: PartyFM_ajaxUrl,

			columns:[
				{"title":"券号","code":"productPromoCodeId"},
				{"title":"优惠券状态","code":"codeStatus",
				 "handle":function(td,record){
			 	if(record.codeStatus == "COUPON_CODE_DISABLE"){
			 		td.html("已过期");
			 	}else if(record.codeStatus == "COUPON_CODE_GET"){
			 		td.html("已领取");
			 	}else if(record.codeStatus == "COUPON_CODE_USED"){
			 		td.html("已使用");
			 	}
			     }
				},
				{"title":"领取人","code":"userLoginId"},
				{"title":"领取时间","code":"useDate"},
				{"title":"使用时间","code":"endDate"},
			],
			listName: "recordsList",
			paginateEL: "PartyFM_paginateDiv",
			headNotShow: true,
			midShowNum: 3,
			isModal: true
		});
		
		//查询按钮点击事件
		$('#PartyFM_QueryForm #searchBtn').on('click',function(){
			var userLoginId = $('#PartyFM_QueryForm #userLoginId').val();
			var codeStatus = $('#PartyFM_QueryForm #codeStatus').val();
			
			PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl,"userLoginId",userLoginId);
			PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl,"codeStatus",codeStatus);
			PartyFM_data_tbl.reload(PartyFM_ajaxUrl);
			return false;
		});
		
	});
</script><!-- script区域end -->
