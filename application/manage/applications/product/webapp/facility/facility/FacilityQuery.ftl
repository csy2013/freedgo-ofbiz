<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
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
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="QueryForm" class="form-inline clearfix">
      <div class="form-group">
        <div class="input-group m-b-10">
          <span class="input-group-addon">${uiLabelMap.FacilityName}</span>
          <input type="text" id="facilityName" class="form-control" value="">
        </div>
        <div class="input-group m-b-10">
          <span class="input-group-addon">${uiLabelMap.FacilityCode}</span>
          <input type="text" id="facilityId" class="form-control" value="">
        </div>
        <#--<div class="input-group m-b-10">-->
          <#--<span class="input-group-addon">${uiLabelMap.FacilityInnerCode}</span>-->
          <#--<input type="text" id="facilityInnerCode" class="form-control" value="">-->
        <#--</div>-->
      </div>
      <div class="input-group pull-right">
        <button id="searchBtn" class="btn btn-success btn-flat">${uiLabelMap.BrandSearch}</button>
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
			<#--<#if security.hasEntityPermission("FACILITYMGR_LIST", "_CREATE", session)>-->
	          <#--<button id="btn_add" class="btn btn-primary">-->
	            <#--<i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}-->
	          <#--</button>-->
	        <#--</#if>-->
	        <!-- 是否有删除权限-->
			<#--<#if security.hasEntityPermission("FACILITYMGR_LIST", "_DEL", session)>-->
	          <#--<button id="btn_del" class="btn btn-primary">-->
	            <#--<i class="fa fa-trash"></i>${uiLabelMap.BrandDel}-->
	          <#--</button>-->
	        <#--</#if>-->
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

<!-- 新增弹出框start -->
<div id="modal_add"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="modal_add_title">${uiLabelMap.FacilityAdd}</h4>
      </div>
      <div class="modal-body">
        <form id="AddForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>AddFacility</@ofbizUrl>">
          <div class="form-group">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.FacilityCode}:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="facilityId" name="facilityId" readonly>
            </div>                
          </div>
          <div class="form-group" data-type="required" data-mark="${uiLabelMap.FacilityName}">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.FacilityName}:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control dp-vd" id="facilityName" name="facilityName">
              <p class="dp-error-msg"></p>
            </div>                
          </div>
          <div class="form-group">
            <label class="control-label col-sm-2">${uiLabelMap.FacilityInnerCode}:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="facilityInnerCode" name="facilityInnerCode">
            </div>                
          </div>
          <div class="form-group">
            <label class="control-label col-sm-2">${uiLabelMap.FacilityDescription}:</label>
            <div class="col-sm-10">
              <textarea id="description" name="description" class="form-control" rows="3" style="resize: none;"></textarea>
            </div>                
          </div>
          <div class="form-group" data-type="required" data-mark="${uiLabelMap.FacilityCoverageArea}">
          	<input type="hidden" id="facilityCoverageArea" name="facilityCoverageArea" class="dp-vd" />
            <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.FacilityCoverageArea}:</label>
            <div class="col-sm-10">
              <div class="zTreeDemoBackground left">
                <ul id="add_facility_coverage_area" class="ztree"></ul>
              </div>
              <p class="dp-error-msg"></p>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
      </div>
    </div>
  </div>
</div><!-- 新增弹出框end -->

<!-- 修改弹出框start -->
<div id="modal_edit"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="modal_edit_title">${uiLabelMap.FacilityEdit}</h4>
      </div>
      <div class="modal-body">
        <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>AddFacility</@ofbizUrl>">
          <div class="form-group">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.FacilityCode}:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="facilityId" name="facilityId" readonly>
            </div>                
          </div>
          <div class="form-group" data-type="required" data-mark="${uiLabelMap.FacilityName}">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.FacilityName}:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control dp-vd" id="facilityName" name="facilityName">
              <p class="dp-error-msg"></p>
            </div>                
          </div>
          <div class="form-group" style="display: none">
            <label class="control-label col-sm-2">${uiLabelMap.FacilityInnerCode}:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="facilityInnerCode" name="facilityInnerCode">
            </div>                
          </div>
          <div class="form-group">
            <label class="control-label col-sm-2">${uiLabelMap.FacilityDescription}:</label>
            <div class="col-sm-10">
              <textarea id="description" name="description" class="form-control" rows="3" style="resize: none;"></textarea>
            </div>                
          </div>
          <div class="form-group" data-type="" data-mark="${uiLabelMap.FacilityCoverageArea}" style="display: none">
          	<input type="hidden" id="facilityCoverageArea" name="facilityCoverageArea" class="dp-vd"/>
            <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.FacilityCoverageArea}:</label>
            <div class="col-sm-10">
              <div class="zTreeDemoBackground left">
                <ul id="edit_facility_coverage_area" class="ztree"></ul>
              </div>
              <p class="dp-error-msg"></p>
            </div>                
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
      </div>
    </div>
  </div>
</div><!-- 修改弹出框end -->

<!-- script区域start -->
<script>
	var setting = {
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
	  }
	};
	
	var del_ids = "";
	var data_tbl;
	var ajaxUrl = "FacilityList";
	$(function(){
		data_tbl = $('#data_tbl').dataTable({
			ajaxUrl: ajaxUrl,
			columns:[
				{"title":"仓库ID","code":"facilityId","checked":true},
				{"title":"仓库编号","code":"facilityId","sort":true},
				{"title":"仓库名称","code":"facilityName","sort":true},
                    // {"title":"内部编号","code":"facilityInnerCode","sort":true},
				{"title":"备注","code":"description"},
				{"title":"操作","code":"option",
				 "handle":function(td,record){
				 	var btns = "<div class='btn-group'>"+
	                  		   "<button type='button' class='btn btn-danger btn-sm' onclick='location.href=\"InventoryList?facilityId="+record.facilityId+"\"'>${uiLabelMap.FacilityInfo}</button>"+
	                  		   "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
	                    	   "<span class='caret'></span>"+
	                    	   "<span class='sr-only'>Toggle Dropdown</span>"+
	                  		   "</button>"+
	                  		   "<ul class='dropdown-menu' role='menu'>"+
			                   <!-- 是否有编辑权限-->
							   <#if security.hasEntityPermission("FACILITYMGR_LIST", "_UPDATE", session)>
	                    	     "<li><a href='javascript:editInit(\""+record.facilityId+"\")'>${uiLabelMap.BrandEdit}</a></li>"+
	                    	   </#if>
	                    	   <!-- 是否有删除权限-->
							   <#if security.hasEntityPermission("FACILITYMGR_LIST", "_DEL", session)>
			                     "<li><a href='javascript:del(\""+record.facilityId+"\")'>${uiLabelMap.BrandDel}</a></li>"+
			                   </#if>
	                  		   "</ul>"+
	                		   "</div>";
	                td.append(btns);
				 }
				}
			],
			listName: "facilityList",
			paginateEL: "paginateDiv",
			viewSizeEL: "view_size"
		});
		
		//查询按钮点击事件
		$('#QueryForm #searchBtn').on('click',function(){
			var facilityName = $('#QueryForm #facilityName').val();
			var facilityId = $('#QueryForm #facilityId').val();
			// var facilityInnerCode = $('#QueryForm #facilityInnerCode').val();
			ajaxUrl = changeURLArg(ajaxUrl,"facilityName",facilityName);
			ajaxUrl = changeURLArg(ajaxUrl,"facilityId",facilityId);
			// ajaxUrl = changeURLArg(ajaxUrl,"facilityInnerCode",facilityInnerCode);
			data_tbl.reload(ajaxUrl);
			return false;
		});
	
		//新增弹出窗关闭事件
		$('#modal_add').on('hide.bs.modal', function () {
		  $('#AddForm').dpValidate({
		  	clear: true
		  });
		})
		
		//修改弹出窗关闭事件
		$('#modal_edit').on('hide.bs.modal', function () {
		  $('#EditForm').dpValidate({
		  	clear: true
		  });
		})
		
	    //新增按钮点击事件
	    $('#btn_add').click(function(){
	    	//清空form
	    	clearForm($("#AddForm"));
	    	//异步加载地区数据
			$.ajax({
				url: "GoAddPage",
				type: "GET",
				dataType : "json",
				success: function(data){
					$('#modal_add #facilityId').val(data.facilityId);
					$.fn.zTree.init($("#add_facility_coverage_area"), setting, data.geoList);
					//设置提示弹出框内容
					$('#modal_add').modal();
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
    				$('#modal_msg').modal();
				}
			});
	    });
	     
	    $('#AddForm').dpValidate({
	    	validate: true,
            callback: function(){
            	//异步调用新增方法
				$.ajax({
					url: "AddFacility",
					type: "POST",
					data: $('#AddForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#modal_add').modal('toggle');
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
						//隐藏新增弹出窗口
						$('#modal_add').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
            }	
        });
	    
	    //新增弹出框保存按钮点击事件
	    $('#modal_add #save').click(function(){
	    	var treeObj=$.fn.zTree.getZTreeObj("add_facility_coverage_area"),
	    		nodes=treeObj.getCheckedNodes(true),
	    		ids='';
	    	for(var i=0;i<nodes.length;i++){
            	ids += nodes[i].id + ",";
            }
            $('#modal_add #facilityCoverageArea').val(ids);
            $("#AddForm").dpValidate({
	    		clear: true
	    	});
			$('#AddForm').submit();
	    });
	    
	    $('#EditForm').dpValidate({
	    	validate: true,
            callback: function(){
            	//异步调用修改方法
				$.ajax({
					url: "EditFacility",
					type: "POST",
					data: $('#EditForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏修改弹出窗口
						$('#modal_edit').modal('toggle');
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
						//隐藏修改弹出窗口
						$('#modal_edit').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
            }	
        });
	    
	    //修改弹出框保存按钮点击事件
	    $('#modal_edit #save').click(function(){
	    	var treeObj=$.fn.zTree.getZTreeObj("edit_facility_coverage_area"),
	    		nodes=treeObj.getCheckedNodes(true),
	    		ids='';
	    	for(var i=0;i<nodes.length;i++){
            	ids += nodes[i].id + ",";
            }
            $('#modal_edit #facilityCoverageArea').val(ids);
            $("#EditForm").dpValidate({
	    		clear: true
	    	});
            $('#EditForm').submit();
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
				
	    		$.ajax({
					url: "GetProductByFacilityId",
					type: "GET",
					data: {ids : del_ids},
					dataType : "json",
					success: function(data){
						if(data.productCount > 0){
							//设置提示弹出框内容
							$('#modal_msg #modal_msg_body').html("${uiLabelMap.FacilityHasProduct}");
							$('#modal_msg').modal();
						}else{
							//设置删除弹出框内容
				    		$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.FacilityNotHasProduct}");
				    		$('#modal_confirm').modal('show');
						}
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
						$('#modal_msg').modal();
					}
				});
	    	}else{
	    		//设置提示弹出框内容
	    		$('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
	    		$('#modal_msg').modal();
	    	}
	    });
	    
	    //删除弹出框删除按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
			//异步调用删除方法
			$.ajax({
				url: "DeleteFacility",
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
	
	//行删除按钮事件
	function del(id){
		del_ids = id;
		$.ajax({
			url: "GetProductByFacilityId",
			type: "GET",
			data: {ids : del_ids},
			dataType : "json",
			success: function(data){
				if(data.productCount > 0){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.FacilityHasProduct}");
					$('#modal_msg').modal();
				}else{
					//设置删除弹出框内容
		    		$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.FacilityNotHasProduct}");
		    		$('#modal_confirm').modal('show');
				}
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
				$('#modal_msg').modal();
			}
		});
	}
	
	//点击编辑按钮事件
    function editInit(id){
    	//清空form
	   	clearForm($("#EditForm"));
    	//异步加载已选地区数据
		$.ajax({
			url: "GetFacilityById",
			type: "GET",
			data : {facilityId:id},
			dataType : "json",
			success: function(facilityData){
				$('#modal_edit #facilityId').val(facilityData.paramMap.facilityId);
				$('#modal_edit #facilityName').val(facilityData.paramMap.facilityName);
				$('#modal_edit #facilityInnerCode').val(facilityData.paramMap.facilityInnerCode);
				$('#modal_edit #description').val(facilityData.paramMap.description);
				//异步加载所有地区数据
				$.ajax({
					url: "GetGeoList",
					type: "GET",
					dataType : "json",
					success: function(geoData){
						var treeObj = $.fn.zTree.init($("#edit_facility_coverage_area"), setting, geoData.geoList);
						//自动勾选复选框
						$.each(facilityData.paramMap.fcaList,function(){
							var node = treeObj.getNodeByParam("id", this.geoId);
							treeObj.checkNode(node);
							//自动展开子节点
							if(!node.isParent){
								treeObj.expandNode(node.getParentNode(), true, true, true);
							}
						})
						$('#modal_edit').modal();
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
	    				$('#modal_msg').modal();
					}
				});
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
				$('#modal_msg').modal();
			}
		});
    }
	
	
</script><!-- script区域end -->
