
<!-- 内容start -->
<div class="box box-info">
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="QueryForm" class="form-inline clearfix">
        <!--店铺编码-->
        <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
      <div class="form-group">
        <div class="input-group m-b-10">
          <span class="input-group-addon">${uiLabelMap.InventoryProductName}</span>
          <input type="text" id="productName" class="form-control" value="">
        </div>
        <div class="input-group m-b-10">
          <span class="input-group-addon">${uiLabelMap.InventoryProductId}</span>
          <input type="text" id="productId" class="form-control" value="">
        </div>
        <#--<div class="input-group m-b-10">-->
          <#--<span class="input-group-addon">选择仓库</span>-->
          <#--<select id="facilityId" class="form-control" style="min-width: 196px;">-->
            <#--<option value="">======请选择======</option>-->
            <#--<#assign facilityList = delegator.findByAnd("Facility",Static["javolution.util.FastMap"].newInstance())>-->
            <#--<#list facilityList as facility>-->
            <#--<option value="${facility.facilityId}">${facility.facilityName?if_exists}</option>-->
            <#--</#list>-->
          <#--</select>-->
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

<!-- 修改弹出框start -->
<div id="modal_edit"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="modal_edit_title">${uiLabelMap.InventoryEdit}</h4>
      </div>
      <div class="modal-body">
        <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>AddFacility</@ofbizUrl>">
          <input type="hidden" id="inventoryItemId" name="inventoryItemId" />
          <div class="form-group">
            <label class="control-label col-sm-2">${uiLabelMap.FacilityName}:</label>
            <div class="col-sm-10">
              <label class="control-label" style="font-weight: normal;" id="facilityName"></label>
            </div>                
          </div>
          <div class="form-group" data-type="required" data-mark="${uiLabelMap.InventoryAvailable}" >
            <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.InventoryAvailable}:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control dp-vd" id="available" name="available">
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
	var data_tbl;
	var ajaxUrl = "WarningInventoryListData?productStoreId="+$('#productStoreId').val();
	
	$(function(){
		data_tbl = $('#data_tbl').dataTable({
			ajaxUrl: ajaxUrl,
			columns:[
				{"title":"${uiLabelMap.InventoryProductName}","code":"productName","sort":true,
				 "handle":function(td,record){
				 	var img="";
				 	if(record.imgUrl){
				 		img = "<img class='img-responsive' style='max-height: 100px;max-width: 100px;' src='/content/control/stream?contentId="+record.imgUrl+"'>";
				 	}
				 	var div = "<div class='col-sm-4'  align='center'>"+img+"</div>"+
						      "<div class='col-sm-8' align='left'>"+
						      "<p>"+record.productName+record.features+
							  "</p>"+
							  "<p>¥"+record.price+"<p>"+
						      "</div>";
					td.html(div);
				 }
				},
				{"title":"${uiLabelMap.InventoryProductId}","code":"productId","sort":true},
				<#--{"title":"${uiLabelMap.InventoryPartyName}","code":"partyName"},-->
				{"title":"${uiLabelMap.FacilityName}","code":"facilityName"},
				{"title":"总库存","code":"totalQuantity"},
                {"title":"可用库存","code":"available"},
				{"title":"${uiLabelMap.InventoryWarningQuantity}","code":"warningQuantity"}
				<!-- 是否有编辑权限-->
				<#if security.hasEntityPermission("FACILITYMGR_LIST", "_UPDATE", session)>
				,
				{"title":"操作","code":"option","paramField":"inventoryItemId",
				 "handle":function(td,record){
				 	var btns = "<div class='btn-group'>"+
  							   "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:editInit("+record.inventoryItemId+");'>${uiLabelMap.InventoryEdit}</button>"+
						  	   "</div>";
					td.append(btns);
				 }
				}
				</#if>
			],
			listName: "inventoryList",
			paginateEL: "paginateDiv",
			viewSizeEL: "view_size"
		});
		
		//查询按钮点击事件
		$('#QueryForm #searchBtn').on('click',function(){
			var productName = $('#QueryForm #productName').val();
			var productId = $('#QueryForm #productId').val();
			var facilityId = $('#facilityId').val();
			ajaxUrl = changeURLArg(ajaxUrl,"productName",productName);
			ajaxUrl = changeURLArg(ajaxUrl,"productId",productId);
			ajaxUrl = changeURLArg(ajaxUrl,"facilityId",facilityId);
			data_tbl.reload(ajaxUrl);
			return false;
		});
	
		//修改弹出窗关闭事件
		$('#modal_edit').on('hide.bs.modal', function () {
		  $('#EditForm').dpValidate({
		  	clear: true
		  });
		})
	
		$('#EditForm').dpValidate({
			validate: true,
            callback: function(){
            	//异步调用修改方法
				$.ajax({
					url: "EditInventory",
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
            $('#EditForm').submit();
	    });
	    
	});
	
	//点击编辑按钮事件
    function editInit(id){
    	//清空form
	    clearForm($("#EditForm"));
    
    	//异步加载已选地区数据
		$.ajax({
			url: "GoInventoryEdit",
			type: "GET",
			data : {inventoryItemId:id},
			dataType : "json",
			success: function(data){
				if(data.inventoryList.length > 0 ){
					$('#modal_edit #inventoryItemId').val(data.inventoryList[0].inventoryItemId);
					$('#modal_edit #facilityName').html(data.inventoryList[0].facilityName);
					$('#modal_edit #available').val(data.inventoryList[0].accountingQuantityTotal);
					$('#modal_edit').modal();
				} 
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
				$('#modal_msg').modal();
			}
		});
    }
</script><!-- script区域end -->
