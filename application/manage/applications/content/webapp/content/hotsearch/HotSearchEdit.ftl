<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<style type="text/css">
  ul.ztree {
    margin-top: 10px;
    border: 1px solid #d2d6de;
    height:250px;
    overflow-y:scroll;
    overflow-x:auto;
  }
</style>

<!-- 内容start -->
<div class="box box-info">
	<form id="HotSearchEditForm" class="form-horizontal">
		<div class="box-body">
			<div class="row">
				<div class="form-group col-sm-8" data-type="required,max" data-number="40" data-mark="关键词">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>关键词:</label>
                    <div class="col-sm-10" >
                    	<div class="col-sm-5" style="padding-left: 0px;">
							<input type="text" class="form-control dp-vd" id="hotSearchKeyName" />
							<p class="dp-error-msg"></p>
						</div>
                    </div>
                </div>
			</div>
            
            <div class="row">
				<div class="form-group col-sm-8" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>
                    <div class="col-sm-10" >
                    	<div class="col-sm-5" style="padding-left: 0px;">
							<input type="text" class="form-control dp-vd" id="sequenceId" />
							<p class="dp-error-msg"></p>
						</div>
                    </div>
                </div>
			</div>
			
			<div class="row">
				<div class="form-group col-sm-8" data-type="required" data-mark="站点">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>
                    <div class="col-sm-10">
                    	<div class="col-sm-5" style="padding-left: 0px;">
							<select id="webSite" class="form-control select2 dp-vd" multiple="multiple" data-placeholder="请选择站点">
								<#assign webSiteList = delegator.findByAnd("WebSite",{"isEnabled":"Y"}) >
		        				<#if webSiteList?has_content>
		        					<#list webSiteList as webSite>
					                  <option value="${webSite.webSiteId}">${webSite.siteName}</option>
					                </#list>
		        				</#if>
		                    </select>
		                    <p class="dp-error-msg"></p>
						</div>
						<div class="col-sm-3" style="padding-left: 0px;">
							<div class="checkbox">
		                        <label>
		                    		<input id="isAllWebSite" type="checkbox">所有站点
		                        </label>
		                    </div>
						</div>
                    </div>
                </div>
			</div>
			
			<#--<div class="row">
				<div class="form-group col-sm-8" data-type="required" data-mark="地区">
                    <input type="hidden" id="geo" name="geo" class="dp-vd"/>
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>地区:</label>
                    <div class="col-sm-10">
                    	<div class="col-sm-5" style="padding-left: 0px;">
                            <div class="zTreeDemoBackground left">
                                <ul id="add_geo_area" class="ztree"></ul>
                            </div>
		                    <p id="c_error_msg" class="dp-error-msg"></p>
						</div>
						<div class="col-sm-3" style="padding-left: 0px;">
							<div class="checkbox">
		                        <label>
		                    		<input id="isAllGeo" type="checkbox">所有地区
		                        </label>
		                    </div>
						</div>
                    </div>
                </div>
			</div>-->
    
		</div><!-- box-body end -->
		
		<!-- 按钮组 -->
		<div class="box-footer text-center">
            <button id="save" type="button" class="btn btn-primary m-r-10">保存</button>
            <button id="cancel" type="button" class="btn btn-default m-r-10 img-cancel-btn">取消</button>
        </div>
  	</form>
</div><!-- 内容end -->

<!-- script区域start -->
<script>
var select_id;
$(function(){
	//初始化select2
	$(".select2").select2({
		closeOnSelect:false
	});
	
	// 取消
	$("#cancel").on("click", function(){
		window.location = '<@ofbizUrl>HotSearchListPage</@ofbizUrl>';
	});
	
	//初始化数据
	$.ajax({
		url: "/content/control/getHotSearchById${externalKeyParam}",
		type: "POST",
		data : {hotSearchId:'${request.getParameter('hotSearchId')}'},
		dataType : "json",
		async : false,
		success: function(data){
			$('#hotSearchKeyName').val(data.hotSearchKeyName);
			$('#sequenceId').val(data.sequenceId);
			//初始化站点选择框
			if(data.isAllWebSite == '0'){
				$('#isAllWebSite').prop("checked","true");
				$("#webSite").prop("disabled", true);
				$("#webSite").removeClass('dp-vd');
			}else{
				$("#webSite").val(data.webSite.split(',')).trigger("change");
			}
			
			//初始化地区选择框
//			if(data.isAllGeo == '0'){
//				$('#isAllGeo').prop("checked","true");
//		        $("#geo").val(data.geo);
//			}else{
//				$("#geo").val(data.geo);
//			}
		},
		error: function(data){
			$.tipLayer("操作失败！");
		}
	});

	// 初始化图片选择
    $.chooseImage.int({
        userId: '',
        serverChooseNum: 1,
        getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
        submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
        submitServerImgUrl: '',
        submitNetworkImgUrl: ''
    });
    
    //图片保存按钮事件
    $('body').on('click','.img-submit-btn',function(){
       var obj = $.chooseImage.getImgData();
       $.chooseImage.choose(obj,function(data){
       		$('#contentId').val(data.uploadedFile0);
       		$('#img').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
       })
	});
    
    // 图片选择控件显示
    $('#uploadedFile').click(function(){
        $.chooseImage.show();
    });
    
    //所有站点的选中事件
    $('#isAllWebSite').change(function(){
    	if($(this).prop("checked")){
    		$("#webSite").val(null).trigger("change");
    		$("#webSite").prop("disabled", true);
    		$("#webSite").removeClass('dp-vd');
    	}else{
    		$("#webSite").prop("disabled", false);
    		$("#webSite").addClass('dp-vd');
    	}
    });
    
    /*var setting = {
        check: {
            enable: true
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        view: {
            showIcon: false
        },
        callback: {
        	onCheck: geoCheck
        }
    };
    
    $.ajax({
        url: "/facility/control/GetGeoList${externalKeyParam}",
        type: "GET",
        dataType: "json",
        async : false,
        success: function (data) {
            treeObj = $.fn.zTree.init($("#add_geo_area"), setting, data.geoList);
            var geo = $('#HotSearchEditForm #geo').val();
            //初始化地区选择框
            if($('#isAllGeo').prop("checked")){
            	treeObj.checkAllNodes(false);
            	var nodes = treeObj.getNodesByFilter(function (node) { return node.level == 0 });
	            for (var i=0, l=nodes.length; i < l; i++) {
					treeObj.setChkDisabled(nodes[i], true, true, true);
				}
				$("#HotSearchEditForm #geo").removeClass('dp-vd');
				$('#HotSearchEditForm #geo').val('');
            }else{
            	if(geo){
	                var geos = geo.split(',');
	                //自动勾选复选框
	                $.each(geos, function (i) {
	                    var node = treeObj.getNodeByParam("id", geos[i]);
	                    if(node ) {
	                        treeObj.checkNode(node);
	                    }
	                });
	            }
            }
        },
        error: function (data) {
            //设置提示弹出框内容
            $('#modal_msg #modal_msg_body').html("网络异常！");
            $('#modal_msg').modal();
        }
    });

    //所有地区的选中事件
    $('#isAllGeo').change(function(){
    	if($(this).prop("checked")){
            treeObj.checkAllNodes(false);
            var nodes = treeObj.getNodesByFilter(function (node) { return node.level == 0 });
            for (var i=0, l=nodes.length; i < l; i++) {
				treeObj.setChkDisabled(nodes[i], true, true, true);
			}
			$("#HotSearchEditForm #geo").removeClass('dp-vd');
			$('#HotSearchEditForm #geo').val('');
    	}else{
    		var nodes = treeObj.getNodesByFilter(function (node) { return node.level == 0 });
            for (var i=0, l=nodes.length; i < l; i++) {
				treeObj.setChkDisabled(nodes[i], false, true, true);
			}
            treeObj.checkAllNodes(false);
            $("#HotSearchEditForm #geo").addClass('dp-vd');
    	}
    });*/
    
    //表单校验方法
    $('#HotSearchEditForm').dpValidate({
		validate: true,
        callback: function(){
        	var hotSearchKeyName = $('#HotSearchEditForm #hotSearchKeyName').val();
	    	var sequenceId = $('#HotSearchEditForm #sequenceId').val();
	    	var isAllWebSite = $('#HotSearchEditForm #isAllWebSite').is(':checked') ? 0 : 1;
	    	var webSite = $('#HotSearchEditForm #webSite').val() !=null ? $('#HotSearchEditForm #webSite').val().join(',') : '';
//	    	var isAllGeo = $('#HotSearchEditForm #isAllGeo').is(':checked') ? 0 : 1;
//            var geo = $('#HotSearchEditForm #geo').val();
	    	
        	//异步加载已选地区数据
			$.ajax({
				url: "editHotSearch",
				type: "POST",
				data : {hotSearchId:'${request.getParameter('hotSearchId')}',
						hotSearchKeyName:hotSearchKeyName,
						sequenceId:sequenceId,
						isAllWebSite:isAllWebSite,
						webSite:webSite
//					,
//						isAllGeo:isAllGeo,
//						geo:geo
				},
				dataType : "json",
				success: function(data){
					$.tipLayer("操作成功！");
					window.location = '<@ofbizUrl>HotSearchListPage</@ofbizUrl>';
				},
				error: function(data){
					$.tipLayer("操作失败！");
				}
			});
        }	
    });
    
    $('#save').click(function(){
    	$("#HotSearchEditForm").dpValidate({
    		clear: true
    	});
    	$("#HotSearchEditForm").submit();
    });
    
    //地区树点击事件
//    function geoCheck(event, treeId, treeNode){
//    	var treeObj = $.fn.zTree.getZTreeObj(treeId);
//    	var nodes = treeObj.getCheckedNodes(true);
//    	var ids = "";
//    	for(var i=0;i<nodes.length;i++){
//    		ids += nodes[i].id;
//    		if(i < nodes.length -1){
//    			ids += ",";
//    		}
//    	}
//        $('#HotSearchEditForm #geo').val(ids);
//    }
		
});
</script><!-- script区域end -->
