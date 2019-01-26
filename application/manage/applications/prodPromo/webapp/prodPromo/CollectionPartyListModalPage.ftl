<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
		<!-- 表格区域start -->
		<div class="row">
      		<div class="col-sm-12">
        		<table id="CPartyFM_data_tbl" class="table table-bordered table-hover js-checkparent"></table>
      		</div>
    	</div><!-- 表格区域end -->
    	
    	<!-- 分页条start -->
    	<div class="row" id="CPartyFM_paginateDiv">
		</div><!-- 分页条end -->
	</div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script>
	var CPartyFM_data_tbl;
	var CPartyFM_ajaxUrl = "/prodPromo/control/collectionPartyListForJson?activityId="+${parameters.activityId?if_exists}+"&VIEW_SIZE=5";
	$(function(){
		CPartyFM_data_tbl = $('#CPartyFM_data_tbl').dataTable({
			ajaxUrl: CPartyFM_ajaxUrl,
			columns:[
				{"title":"关注会员ID","code":"partyId"},
				{"title":"会员等级","code":"levelName"},
				{"title":"手机号","code":"mobile"},
				{"title":"Email","code":"email"},
				{"title":"收藏时间","code":"createdStamp",
				 "handle":function(td,record){
				 	var date = timeStamp2String(record.createdStamp.time);
				 	td.append(date);
				 }}
			],
			listName: "partyList",
			paginateEL: "CPartyFM_paginateDiv",
			headNotShow: true,
			midShowNum: 3
		});
	});
	
	function timeStamp2String(time){
	    var datetime = new Date();
	    datetime.setTime(time);
	    var year = datetime.getFullYear();
	    var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
	    var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
	    var hour = datetime.getHours()< 10 ? "0" + datetime.getHours() : datetime.getHours();
	    var minute = datetime.getMinutes()< 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
	    var second = datetime.getSeconds()< 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
	    return year + "-" + month + "-" + date+" "+hour+":"+minute+":"+second;
	}
</script><!-- script区域end -->
