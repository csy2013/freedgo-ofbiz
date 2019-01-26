<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="QueryForm" class="form-inline clearfix">
      <div class="form-group">
        <div class="input-group m-b-10">
          <span class="input-group-addon">站点</span>
          <input type="text" id="webSiteName" class="form-control" value="">
        </div>
        <#--<div class="input-group m-b-10">
          <span class="input-group-addon">地区</span>
          <input type="text" class="form-control" id="treeName" placeholder="全部" onclick="showMenu()" readonly="readonly"/ >
          <input type="hidden" class="form-control" id="geoId"  name="geoId" value="CHN"/>
          <!-- zTree start&ndash;&gt;
	          <div id="menuContent" class="menuContent" style="display:none; position: absolute;top:33px;border:1px solid #ccc;background:white;z-index:1000;width:196px;height: 200px;overflow-y: auto;">
	         	<ul id="treeDemo" class="ztree" style="margin-top: 0; width: 110px;">
	         	</ul>
	       	  </div>
       	  <!-- zTree end&ndash;&gt;
        </div>-->
        
        
      </div>
      <div class="input-group pull-right m-l-10">
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
        	<!-- 是否有新增权限-->
			<#if security.hasEntityPermission("BANNERMGR_LIST", "_CREATE", session)>
	          <button id="btn_add" class="btn btn-primary">
	            <i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}
	          </button>
	        </#if>
	        <!-- 是否有删除权限-->
			<#if security.hasEntityPermission("BANNERMGR_LIST", "_DEL", session)>
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
	var ajaxUrl = "BannerList";
	var del_ids;
	
	$(function(){
		data_tbl = $('#data_tbl').dataTable({
			ajaxUrl: ajaxUrl,
			columns:[
				{"title":"广告ID","code":"bannerId","checked":true},
				{"title":"图片","code":"bannerImgUrl",
				 "handle":function(td,record){
				 	var img="";
				 	if(record.bannerImgUrl){
				 		img = "<img class='img-responsive' style='max-height: 100px;max-width: 200px;' src='/content/control/getImage?contentId="+record.bannerImgUrl+"'>";
				 	}
				 	var div = "<div class='col-sm-12'  align='center'>"+img+"</div>";
					td.html(div);
				 }
				},
				{"title":"链接地址","code":"linkUrl"},
                {"title":"开始时间","code":"fromDate"},
                {"title":"结束时间","code":"thruDate"},
				{"title":"是否启用","code":"isUse",
				 "handle":function(td,record){
				 	td.empty();
				 	if(record.isUse == "0"){
				 		td.append("<button type='button' onclick='javascript:changeIsUse("+record.bannerId+",1)' class='btn btn-primary'>是</button>");
				 	}else{
				 		td.append("<button type='button' onclick='javascript:changeIsUse("+record.bannerId+",0)' class='btn btn-default'>否</button>");
				 	}
				 }},
				{"title":"序号","code":"sequenceId"},
				{"title":"站点","code":"webSiteName"}
				<!-- ,{"title":"地区","code":"geoName"}-->
				<!-- 是否有编辑权限-->
				<#if security.hasEntityPermission("BANNERMGR_LIST", "_UPDATE", session)>
					,
					{"title":"操作","code":"option",
					 "handle":function(td,record){
					 	var btns = "<div class='btn-group'>"+
	  							   "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:editInit("+record.bannerId+");'>编辑</button>"+
							  	   "</div>";
						td.append(btns);
					 }
					}
				</#if>
			],
			listName: "bannerList",
			paginateEL: "paginateDiv",
			viewSizeEL: "view_size"
		});
		
		/*var setting = {
         	view: {
         		selectedMulti: false //是否允许多选
        	},
  			data: {
  				simpleData: {
  					enable: true
        		}
       		},
   			callback: {
			   //zTree节点的点击事件
			   onClick: onClick,
        	}
        };
	    
	    $.ajax({
	        url: "/facility/control/GetGeoList${externalKeyParam}",
	        type: "GET",
	        dataType: "json",
	        async : false,
	        success: function (data) {
	        	var newNode = { id: "CHN", pId:"-1", name: "全部", open: true };
	        	data.geoList.push(newNode);
	            treeObj = $.fn.zTree.init($("#treeDemo"), setting, data.geoList);
	        },
	        error: function (data) {
	            //设置提示弹出框内容
	            $('#modal_msg #modal_msg_body').html("网络异常！");
	            $('#modal_msg').modal();
	        }
	    });
	    
		
		//隐藏树
		$(document).on('click',function(e){
		    if($(e.target).is('#QueryForm #treeName')) return;
		    if($(e.target).closest('div').is('#menuContent')) {
		    	if($(e.target).closest('a').is("[id$='_a']"))
		    	{
		    		$("#menuContent").hide();return false;}
		    	else return;
		    }
		    else{$("#menuContent").hide();}
		});*/
		
		//查询按钮点击事件
		$('#QueryForm #searchBtn').on('click',function(){
			var webSiteName = $('#QueryForm #webSiteName').val();
//			var geoId = $('#QueryForm #geoId').val();
			ajaxUrl = changeURLArg(ajaxUrl,"webSiteName",webSiteName);
//			ajaxUrl = changeURLArg(ajaxUrl,"geoId",geoId);
			data_tbl.reload(ajaxUrl);
			return false;
		});
		
		//添加按钮点击事件
	    $('#btn_add').click(function(){
	    	window.location = '<@ofbizUrl>BannerAddPage</@ofbizUrl>';
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
				url: "BannerDel",
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
			url: "EditBannerIsUse",
			type: "GET",
			data : {bannerId:id,isUse:val}, 
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
    	window.location = '<@ofbizUrl>BannerEditPage</@ofbizUrl>?bannerId='+id;
    }
    
    //显示树
//	function showMenu() {
//		$("#menuContent").toggle();
//		return false;
//	}
	
	//点击某个节点 然后将该节点的名称赋值值文本框
//	function onClick(e, treeId, treeNode) {
//		var zTree = $.fn.zTree.getZTreeObj("treeDemo");
//		//获得选中的节点
//		var nodes = zTree.getSelectedNodes(),
//		v = "";
//		id = "";
//		//根据id排序
//		nodes.sort(function compare(a, b) { return a.id - b.id; });
//		for (var i = 0, l = nodes.length; i < l; i++) {
//			v += nodes[i].name + ",";
//			id += nodes[i].id + ",";
//		}
//		//将选中节点的名称显示在文本框内
//		if (v.length > 0) v = v.substring(0, v.length - 1);
//		if (id.length > 0) id = id.substring(0, id.length - 1);
//		$("#QueryForm #treeName").attr("value", v);
//		$("#QueryForm #geoId").attr("value", id);
//		//隐藏zTree
//		return false;
//	}
</script><!-- script区域end -->
