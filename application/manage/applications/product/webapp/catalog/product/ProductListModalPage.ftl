<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>

<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
		<!-- 条件查询start -->
		<form id="ProductFM_QueryForm" class="form-inline clearfix">
			<input type="hidden" id="productTypeId" name="productTypeId" value="${parameters.productTypeId?if_exists}">
        	<div class="form-group">
            	<div class="input-group m-b-10">
        			<span class="input-group-addon">商品编码</span>
                    <input type="text" class="form-control" id="productId">
        		</div>
		        <div class="input-group m-b-10">
		        	<span class="input-group-addon">商品名称</span>
                    <input type="text" class="form-control" id="productName">
		        </div>
			</div>
			  <div class="form-group">
			  	<div class="input-group m-b-10">
        			<span class="input-group-addon">商品价格</span>
        			 <input type="text" name="productPriceStart" id = "productPriceStart" onkeyup="value=this.value.replace(/\D+/g,'')">
                     <span>-</span>
                     <input type="text" name="productPriceEnd" id = "productPriceEnd" onkeyup="value=this.value.replace(/\D+/g,'')">
        		</div>
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
        		<table id="ProductFM_data_tbl" class="table table-bordered table-hover js-checkparent"></table>
      		</div>
    	</div><!-- 表格区域end -->
    	
    	<!-- 分页条start -->
    	<div class="row" id="ProductFM_paginateDiv">
		</div><!-- 分页条end -->
	</div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script>
	var ProductFM_data_tbl;
	var ProductFM_ajaxUrl = "/catalog/control/ProductListForJson?VIEW_SIZE=5&productTypeId=" + $('#ProductFM_QueryForm #productTypeId').val();
	$(function(){
		ProductFM_data_tbl = $('#ProductFM_data_tbl').dataTable({
			ajaxUrl: ProductFM_ajaxUrl,
			columns:[
				{"title":"商品编码","code":"productId"},
				{"title":"商品名称","code":"productName"},
				{"title":"商品类型","code":"productTypeName"},
				{"title":"商品分类","code":"categoryName"},
				{"title":"商品价格","code":"price"},
				{"title":"商品图片","code":"imgUrl",
				 "handle":function(td,record){
				 	var img="";
				 	if(record.imgUrl){
				 		img = "<img class='img-responsive' style='max-height: 70px;max-width: 70px;' src='"+record.imgUrl+"'>";
				 	}
				 	var div =$("<div class='col-sm-12'  align='center'>"+img+"</div>");	
					td.html(div);
				 }
				},
				{"title":" ","code":"option",
				 "handle":function(td,record){
				 	var id = record.productId;
				 	var name = record.productName;
				 	var imgUrl = record.imgUrl;
					var price = record.price ;
				 	var btn = $("<div class='btn-group'>"+
  							  "<button type='button' class='btn btn-danger btn-sm btn-select' data-price='"+ price +"' data-id='"+id+"' data-name='"+name+"' data-imgUrl='"+imgUrl+"'>选择</button>"+
						      "</div>");
				 	td.append(btn);
				 }
				}
			],
			listName: "recordsList",
			paginateEL: "ProductFM_paginateDiv",
			headNotShow: true,
			midShowNum: 3
		});
		
		//查询按钮点击事件
		$('#ProductFM_QueryForm #searchBtn').on('click',function(){
			var productTypeId = $('#ProductFM_QueryForm #productTypeId').val();
			var productId = $('#ProductFM_QueryForm #productId').val();
			var productName = $('#ProductFM_QueryForm #productName').val();
			
			var productPriceStart = $.trim($('#ProductFM_QueryForm #productPriceStart').val());
			var productPriceEnd = $.trim($('#ProductFM_QueryForm #productPriceEnd').val());
			
			ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productTypeId",productTypeId);
			ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productId",productId);
			ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productName",productName);
			ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productPriceStart",productPriceStart);
			ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productPriceEnd",productPriceEnd);
			
			ProductFM_data_tbl.reload(ProductFM_ajaxUrl);
			return false;
		});
		
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
				onClick: onClick,
			}
		};
						
		$.ajax({
				url: "<@ofbizUrl>getProductCategoryList</@ofbizUrl>",
				type: "POST",
				dataType : "json",
				success: function(data){
					var productCategoryList = data.productCategoryList;
					var root = {"id":"null","pId":"-1","name":"全部","open":"true"};
					productCategoryList.push(root);
					$.fn.zTree.init($("#productCategoryTree"), setting,productCategoryList);
					
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
    				$('#modal_msg').modal();
				}
			});
			
		//隐藏树
		$(document).on('click',function(e){
		    if($(e.target).is('#ProductFM_QueryForm #productCategory')) return;
		    if($(e.target).closest('div').is('#ProductFM_QueryForm #menuContent')) return;
		    else{$("#ProductFM_QueryForm #menuContent").hide();}
		})
	});
	
//显示树
function showMenu(){
	$("#ProductFM_QueryForm #menuContent").toggle();
	return false;
}

//点击某个节点 然后将该节点的名称赋值值文本框
function onClick(e, treeId, treeNode){
	var zTree = $.fn.zTree.getZTreeObj("productCategoryTree");
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
	$("#ProductFM_QueryForm #productCategory").attr("value", v);
	$("#ProductFM_QueryForm #primaryProductCategoryId").attr("value", id);
	//隐藏zTree
	return false;
}

</script><!-- script区域end -->
