<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>

<style>
    .express_area {
    	width : 200px;
        position: relative;
        height: 25px;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .express_area p {
    	width : 200px;
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
    <form id="QueryForm" class="form-inline clearfix">
      <div class="form-group w-p100">
        <div class="input-group m-b-10">
          <span class="input-group-addon">标签名称</span>
          <input type="text" id="name" class="form-control" value="">
        </div>
        <div class="input-group m-b-10">
          <span class="input-group-addon">标签编号</span>
          <input type="text" id="reviewLabelId" class="form-control" value="">
        </div>
        <div class="input-group m-b-10">
          	<span class="input-group-addon">评价级别</span>
          	<select id="star" class="form-control">
				<option value="">======&nbsp;全部&nbsp;======</option>
				<option value="1">1星</option>
				<option value="2">2星</option>
				<option value="3">3星</option>
				<option value="4">4星</option>
				<option value="5">5星</option>
			</select>
        </div>
        <div class="input-group m-b-10">
          	<span class="input-group-addon">启用状态</span>
          	<select id="isUse" class="form-control">
				<option value="">======&nbsp;全部&nbsp;======</option>
				<option value="0">是</option>
				<option value="1">否</option>
			</select>
        </div>
        
        <div class="input-group pull-right">
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
        	<!-- 是否有新增权限-->
			<#if security.hasEntityPermission("REVIEWMGR_LABEL", "_CREATE", session)>
	          <button id="btn_add" class="btn btn-primary">
	            <i class="fa fa-plus"></i>添加
	          </button>
	        </#if>
	        <!-- 是否有删除权限-->
			<#if security.hasEntityPermission("REVIEWMGR_LABEL", "_DEL", session)>
	          <button id="btn_del" class="btn btn-primary">
	            <i class="fa fa-trash"></i>删除
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

<!-- 新增弹出框start -->
<div id="modal_add"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="modal_add_title">添加标签</h4>
      </div>
      <div class="modal-body">
        <form id="AddForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>addReviewLabel</@ofbizUrl>">
          <div class="form-group">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>标签编号:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control w-p60" id="reviewLabelId" name="reviewLabelId" readonly>
            </div>                
          </div>

			<div class="form-group" data-type="required" data-mark="${uiLabelMap.TagType}">
                <label class="control-label col-sm-2"><i class="required-mark">*</i>商品分类:</label>
                    <div class="col-sm-10">
                        <input type="text" class="form-control dp-vd w-p60" id="treeName" name="treeName" onclick="addshowMenu()" readonly/>
                        <input type="hidden" class="form-control dp-vd w-p50" id="productCategoryId" name="productCategoryId">
                        <div id="Addmenu" style="display:none; position: absolute;top:33px;left:15px;border:1px solid #ccc;background:white;z-index:1000;width:282px;">
                            <ul id="addtree" class="ztree" style="margin-top: 0; width: 110px;">
                            </ul>
                        </div>
                        <p class="dp-error-msg"></p>
                    </div>
            </div>

          <div class="form-group" data-type="required" data-mark="标签名称">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>标签名称:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control dp-vd  w-p60" id="name" name="name">
              <p class="dp-error-msg"></p>
            </div>                
          </div>
          <div class="form-group" data-type="required" data-mark="评价星级">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>评价星级:</label>
            <div class="col-sm-10">
              	<select id="star" name="star" class="form-control dp-vd w-p60">
					<option value="1">1星</option>
					<option value="2">2星</option>
					<option value="3">3星</option>
					<option value="4">4星</option>
					<option value="5">5星</option>
				</select>
            </div>                
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label">图片:</label>
            <div class="col-sm-10">
                <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                <input style="margin-left:5px;" type="button" class="uploadedFile" id="uploadedFile" name="uploadedFile" value="选择图片"/>
                <input type="hidden" id="contentId" name="contentId" />
            </div>
         </div>
         <div class="form-group">
            <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用:</label>
            <div class="col-sm-10">
            	<div class="radio col-sm-3">
                    <label class="col-sm-6"><input name="isUse" type="radio" checked value="0">是</label>
                    <label class="col-sm-6"><input name="isUse" type="radio" value="1">否</label>
                </div>
            </div>
         </div>
         <div class="form-group">
            <label class="control-label col-sm-2 ">内容:</label>
            <div class="col-sm-10">
              <textarea id="description" name="description" class="form-control " rows="3" style="resize: none;"></textarea>
            </div>                
          </div>
          
        </form>
      </div>
      <div class="modal-footer" style="text-align: center;">
        <button id="save" type="button" class="btn btn-primary">保存</button>
        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
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
        <h4 class="modal-title" id="modal_add_title">编辑标签</h4>
      </div>
      <div class="modal-body">
        <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>editReviewLabel</@ofbizUrl>">
          <div class="form-group">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>标签编号:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control w-p60" id="reviewLabelId" name="reviewLabelId" readonly>
            </div>                
          </div>

            <div class="form-group" data-type="required" data-mark="商品分类">
                <label class="control-label col-sm-2"><i class="required-mark">*</i>商品分类:</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control dp-vd w-p60" id="treeName" name="treeName" onclick="EditshowMenu()" readonly/>
                    <input type="hidden" class="form-control dp-vd w-p50" id="productCategoryId" name="productCategoryId">
                    <div id="Editmenu" style="display:none; position: absolute;top:33px;left:15px;border:1px solid #ccc;background:white;z-index:1000;width:282px;">
                        <ul id="edittree" class="ztree" style="margin-top: 0; width: 110px;">
                        </ul>
                    </div>
                    <p class="dp-error-msg"></p>
                </div>
            </div>
          <div class="form-group" data-type="required" data-mark="标签名称">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>标签名称:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control dp-vd w-p60" id="name" name="name">
              <p class="dp-error-msg"></p>
            </div>                
          </div>
          <div class="form-group" data-type="required" data-mark="评价星级">
            <label class="control-label col-sm-2"><i class="required-mark">*</i>评价星级:</label>
            <div class="col-sm-10">
              	<select id="star" name="star" class="form-control dp-vd w-p60">
					<option value="1">1星</option>
					<option value="2">2星</option>
					<option value="3">3星</option>
					<option value="4">4星</option>
					<option value="5">5星</option>
				</select>
            </div>                
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label">图片:</label>
            <div class="col-sm-10">
                <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                <input style="margin-left:5px;" type="button" class="uploadedFile" id="uploadedFile" name="uploadedFile" value="选择图片"/>
                <input type="hidden" id="contentId" name="contentId" />
            </div>
         </div>
         <div class="form-group">
            <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用:</label>
            <div class="col-sm-10">
            	<div class="radio col-sm-3">
                    <label class="col-sm-6"><input name="isUse" type="radio" checked value="0">是</label>
                    <label class="col-sm-6"><input name="isUse" type="radio" value="1">否</label>
                </div>
            </div>
         </div>
         <div class="form-group">
            <label class="control-label col-sm-2">内容:</label>
            <div class="col-sm-10">
              <textarea id="description" name="description" class="form-control" rows="3" style="resize: none;"></textarea>
            </div>                
          </div>
          
        </form>
      </div>
      <div class="modal-footer">
        <button id="save" type="button" class="btn btn-primary">保存</button>
        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
      </div>
    </div>
  </div>
</div><!-- 修改弹出框end -->

<!-- script区域start -->
<script>
	var data_tbl;
	var ajaxUrl = "getReviewLabelList";
	var del_ids;
	var modalName;
	
	$(function(){
		data_tbl = $('#data_tbl').dataTable({
			ajaxUrl: ajaxUrl,
			columns:[
				{"title":"标签ID","code":"reviewLabelId","checked":true},
				{"title":"标签编号","code":"reviewLabelId"},
				{"title":"标签名称","code":"name"},
                {"title":"商品分类","code":"categoryName"},
				{"title":"标签图片","code":"contentId",
				 "handle":function(td,record){
				 	var img="";
				 	if(record.contentId){
				 		img = "<img class='img-responsive' style='max-height: 100px;max-width: 200px;' src='/content/control/getImage?contentId="+record.contentId+"'>";
				 	}
				 	var div = "<div class='col-sm-12'  align='center'>"+img+"</div>";
					td.html(div);
				 }
				},
				{"title":"评价星级","code":"star",
				 "handle":function(td,record){
					td.html(record.star +"星");
				 }
				},
				{"title":"内容","code":"description","width":"200px",
				 "handle":function(td,record){
				 	if(record.description!=null){
				 		td.empty();
				 		var div = $("<div class='express_area'></div>");
				 		var express_area_nobr = $("<nobr>"+record.description+"</nobr>");
				 		var express_area_p = $("<p style='display:none;'>"+record.description+"</p>");
				 		div.append(express_area_nobr);
				 		div.append(express_area_p);
				 		td.append(div);
				 	}
				 }
				},
				{"title":"启用状态","code":"isUse",
				 "handle":function(td,record){
				 	td.empty();
				 	if(record.isUse == "0"){
				 		td.append("<button type='button' onclick='javascript:changeIsUse("+record.reviewLabelId+",1)' class='btn btn-primary'>是</button>");
				 	}else{
				 		td.append("<button type='button' onclick='javascript:changeIsUse("+record.reviewLabelId+",0)' class='btn btn-default'>否</button>");
				 	}
				 }}
				<!-- 是否都有权限-->
				<#if security.hasEntityPermission("REVIEWMGR_LABEL", "_UPDATE", session) || security.hasEntityPermission("REVIEWMGR_LABEL", "_DEL", session)>
				,
				{"title":"操作","code":"option",
				 "handle":function(td,record){
				 	var btns = "<div class='btn-group'>"+
				 			   <!-- 是否都有权限-->
						  	   <#if security.hasEntityPermission("REVIEWMGR_LABEL", "_UPDATE", session) && security.hasEntityPermission("REVIEWMGR_LABEL", "_DEL", session)>
  							   "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:goEdit("+record.reviewLabelId+");'>编辑</button>"+
  							   "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
	                		   "<span class='caret'></span>"+
	                		   "<span class='sr-only'>Toggle Dropdown</span>"+
	              			   "</button>"+
	              			   "<ul class='dropdown-menu' role='menu'>"+
	              			   "<li class='edit_li'><a href='javascript:goDel(\""+record.reviewLabelId+"\")'>删除</a></li>"+
	              		       "</ul>"+
	              		       <!-- 如果只有编辑权限-->
	              		       <#elseif security.hasEntityPermission("REVIEWMGR_LABEL", "_UPDATE", session)>
	              		       "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:goEdit("+record.reviewLabelId+");'>编辑</button>"+
	              		       <!-- 如果只有删除权限-->
	              		       <#elseif security.hasEntityPermission("REVIEWMGR_LABEL", "_DEL", session)>
	              		       "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:goDel("+record.reviewLabelId+");'>删除</button>"+
	              		       </#if>
						  	   "</div>";
					td.append(btns);
				 }
				}
				</#if>
			],
			listName: "recordsList",
			paginateEL: "paginateDiv",
			viewSizeEL: "view_size"
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
		$('#QueryForm #searchBtn').on('click',function(){
			var name = $('#QueryForm #name').val();
			var reviewLabelId = $('#QueryForm #reviewLabelId').val();
			var star = $('#QueryForm #star').val();
			var isUse = $('#QueryForm #isUse').val();
			
			ajaxUrl = changeURLArg(ajaxUrl,"name",name);
			ajaxUrl = changeURLArg(ajaxUrl,"reviewLabelId",reviewLabelId);
			ajaxUrl = changeURLArg(ajaxUrl,"star",star);
			ajaxUrl = changeURLArg(ajaxUrl,"isUse",isUse);
			data_tbl.reload(ajaxUrl);
			return false;
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
	       		$('#'+modalName+' #contentId').val(data.uploadedFile0);
	       		$('#'+modalName+' #img').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
	       })
		});
	    
	    // 图片选择控件显示
	    $('.uploadedFile').click(function(){
	        $.chooseImage.show();
	    });
		
		//添加按钮点击事件
	    $('#btn_add').click(function(){
	    	//清空form
	    	clearForm($("#AddForm"));
	    	$('#modal_add #img').attr("src","");
	    	$('#modal_add #contentId').val("");
	    	$('#modal_add #star').val(1);
	    	$('#modal_add input[name="isUse"][value="0"]').prop("checked",true);
	    
	    	//异步调用新增方法
			$.ajax({
				url: "getReviewLabelNextSeqId",
				type: "GET",
				dataType : "json",
				success: function(data){
					$('#modal_add #reviewLabelId').val(data.nextSeqId);
					modalName = "modal_add";
					//隐藏新增弹出窗口
					$('#modal_add').modal();
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
    				$('#modal_msg').modal();
				}
			});
	    });
	    
	    //新增弹出框保存按钮点击事件
	    $('#modal_add #save').click(function(){
	    	$("#AddForm").dpValidate({
	    		clear: true
	    	});
			$('#AddForm').submit();
	    });
	    
	    $('#AddForm').dpValidate({
	    	validate: true,
            callback: function(){
            	//异步调用新增方法
				$.ajax({
					url: "addReviewLabel",
					type: "POST",
					data: $('#AddForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#modal_add').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("保存成功！");
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
        
        //修改弹出框保存按钮点击事件
	    $('#modal_edit #save').click(function(){
	    	$("#EditForm").dpValidate({
	    		clear: true
	    	});
			$('#EditForm').submit();
	    });
	    
	    $('#EditForm').dpValidate({
	    	validate: true,
            callback: function(){
            	//异步调用新增方法
				$.ajax({
					url: "editReviewLabel",
					type: "POST",
					data: $('#EditForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏修改弹出窗口
						$('#modal_edit').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("编辑成功！");
	    				$('#modal_msg').modal();
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
	    					data_tbl.reload(ajaxUrl);
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#modal_edit').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
            }	
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
	    		$('#modal_confirm #modal_confirm_body').html("标签删除后无法再被使用，是否继续？");
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
				url: "delReviewLabel",
				type: "GET",
				data: {ids : del_ids},
				dataType : "json",
				success: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("删除成功！");
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
		$.ajax({
			url: "editReviewLabelIsUse",
			type: "GET",
			data : {reviewLabelId:id,isUse:val}, 
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
    function goEdit(id){
    	//异步加载标签信息
		$.ajax({
			url: "getReviewLabelById",
			type: "GET",
			data : {
				reviewLabelId : id
			}, 
			dataType : "json",
			success: function(data){
				$("#modal_edit #reviewLabelId").val(data.record.reviewLabelId);
				$("#modal_edit #treeName").val(data.treeName);
				$("#modal_edit #productCategoryId").val(data.record.productCategoryId);
				$("#modal_edit #name").val(data.record.name);
				$("#modal_edit #star").val(data.record.star);
				$("#modal_edit #contentId").val(data.record.contentId);
				if(data.record.contentId){
					$("#modal_edit #img").attr("src","/content/control/getImage?contentId="+data.record.contentId);
				}else{
					$("#modal_edit #img").attr("src","");
				}
				$('#modal_edit input[name="isUse"][value="'+data.record.isUse+'"]').prop("checked",true);
				$("#modal_edit #description").val(data.record.description);
				modalName = "modal_edit";
				$("#modal_edit").modal();
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
				$('#modal_msg').modal();
			}
		});
    }
    
    //行删除按钮事件
	function goDel(id){
		del_ids = id;
		//设置删除弹出框内容
		$('#modal_confirm #modal_confirm_body').html("标签删除后无法再被使用，是否继续？");
		$('#modal_confirm').modal('show');
	}




    var setting = {
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
            onClick: onAddClick,
        }
    };
    var settings = {
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
            onClick: onEditClick,
        }
    };

    $(function(){
        $.ajax({
            url: "getProductCategoryLists",
            type: "POST",
            dataType : "json",
            success: function(data){
                $.fn.zTree.init($("#addtree"), setting, data.productCategoryList);
                $.fn.zTree.init($("#edittree"), settings, data.productCategoryList);
            },
            error: function(data){
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    });

    //添加显示树
    function addshowMenu() {
        $("#Addmenu").toggle();
        return false;
    }

    //添加隐藏树
    $(document).on('click',function(e){
        if($(e.target).is('#AddForm #treeName')) return;
        if($(e.target).closest('div').is('#Addmenu')) {
            if($(e.target).closest('a').is("[id$='_a']"))
            {
                $("#Addmenu").hide();return false;}
            else return;
        }
        else{$("#Addmenu").hide();}
    })

    //点击某个节点 然后将该节点的名称赋值值文本框
    function onAddClick(e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("addtree");
        //获得选中的节点
        var nodes = zTree.getSelectedNodes(),
                v = "";
        id = "";
        //根据id排序
        nodes.sort(function compare(a, b) { return a.id - b.id; });
        for (var i = 0, l = nodes.length; i < l; i++) {
            v += nodes[i].name + ",";
            id += nodes[i].id + ",";
        }
        //将选中节点的名称显示在文本框内
        if (v.length > 0) v = v.substring(0, v.length - 1);
        if (id.length > 0) id = id.substring(0, id.length - 1);
        $("#AddForm #treeName").attr("value", v);
        $("#AddForm #productCategoryId").attr("value", id);
        return false;
    }
    //编辑显示树
    function EditshowMenu() {
        $("#Editmenu").toggle();
        return false;
    }
    //编辑隐藏树
    $(document).on('click',function(e){
        if($(e.target).is('#EditForm #treeName')) return;
        if($(e.target).closest('div').is('#Editmenu')) {
            if($(e.target).closest('a').is("[id$='_a']"))
            {
                $("#Editmenu").hide();return false;}
            else return;
        }
        else{$("#Editmenu").hide();}
    })
    //点击某个节点 然后将该节点的名称赋值值文本框
    function onEditClick(e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("edittree");
        //获得选中的节点
        var nodes = zTree.getSelectedNodes(),
                v = "";
        id = "";
        //根据id排序
        nodes.sort(function compare(a, b) { return a.id - b.id; });
        for (var i = 0, l = nodes.length; i < l; i++) {
            v += nodes[i].name + ",";
            id += nodes[i].id + ",";
        }
        //将选中节点的名称显示在文本框内
        if (v.length > 0) v = v.substring(0, v.length - 1);
        if (id.length > 0) id = id.substring(0, id.length - 1);
        $("#EditForm #treeName").attr("value", v);
        $("#EditForm #productCategoryId").attr("value", id);
        return false;
    }

</script><!-- script区域end -->
