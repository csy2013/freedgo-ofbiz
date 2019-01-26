<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>

<style>
	.select-info {
		-moz-user-select: none;
		border: 1px solid #CECECE;
		border-radius: 3px 3px 3px 3px;
		box-shadow: 0 0 1px 1px #FFFFFF inset, 0 1px 5px rgba(0, 0, 0, 0.1);
		margin: 8px;
		overflow: hidden;
		padding: 14px 10px 14px 60px;
		position: relative;
	}
	
	.select-info .top-label {
		height: 20px;
		left: 5px;
		position: absolute;
		top: 20px;
		font-size: 14px;
		font-weight: bold;
		color: #5893B7;
	}
	
	.select-info li.current {
		background-color: #e4e4e4;
		border: 1px solid #aaa;
		border-radius: 4px;
		cursor: default;
		float: left;
		margin-right: 5px;
		margin-top: 5px;
		padding: 0 5px;
		background-color: #3c8dbc;
		border-color: #367fa9;
		padding: 1px 10px;
		color: #fff;
		list-style-type:none;
	}
	
	.select-info li.current span.icon-del{
		color: #999;
		cursor: pointer;
		display: inline-block;
		font-weight: bold;
		margin-right: 2px;
		margin-right: 5px;
		color: rgba(255,255,255,0.7);
	}
	
	.a-btn {
		-moz-transition: box-shadow 0.3s ease-in-out 0s;
		background: -moz-linear-gradient(center top , #FFFFFF 0%, #F6F6F6 74%, #EDEDED 100%) repeat scroll 0 0 transparent;
		border-radius: 6px 6px 6px 6px;
		box-shadow: 0 0 7px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(188, 188, 188, 0.1);
		display: block;
		float: right;
		margin: 2px;
		overflow: hidden;
		position: relative;
	}
	
	.a-btn-text {
		color: #3c8dbc;
		display: block;
		font-size: 12px;
		text-align: center;
		line-height: 16px;
		padding: 5px;
		text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.5);
	}
</style>

<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
		<!-- 条件查询start -->
        <form id="Business_QueryForm" class="form-inline clearfix">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">品牌名称</span>
                    <input type="text" id="brandName" class="form-control" value="">
                </div>

            </div>
            <div class="input-group pull-right">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
            </div>
        </form><!-- 条件查询end -->
		<!-- 分割线start -->
	    <div class="cut-off-rule bg-gray"></div>
	    <!-- 分割线end -->
	    
	   	<!-- 选中结果区域start -->
	   	<div class="select-info" style="display: none;">	
			<label class="top-label">已选：</label>
			<ul class="p-0">					
				
			</ul>
			<a id="menu-confirm" href="javascript:void(0);" class="a-btn btn-select">
				<span class="a-btn-text">确定</span>
			</a> 
		</div>
	   	<!-- 选中结果区域end -->
		
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
	var ProductFM_ajaxUrl = "getAllBusinessBrandList";
	$(function(){
		ProductFM_data_tbl = $('#ProductFM_data_tbl').dataTable({
			ajaxUrl: ProductFM_ajaxUrl,
			columns:[
				{"title":"复选框","code":"productBrandId","checked":"true",
				 "handle":function(td,record){
				 	var checkbox = td.find('.js-checkchild:checkbox');
				 	checkbox.attr({"data-name":record.brandName});
				 	//根据选中结果集默认选中
				 	$.each($('.select-info ul').find('li'),function(i,v){
				 		if(checkbox.val() == $(v).attr("id")){
				 			checkbox.prop("checked",true);
				 		}
				 	});
				 }
				},
                /*{"title":"品牌logo","code":"contentId"},*/

                {"title":"品牌logo","code":"contentId",
                    "handle":function(td,record){
                    <#--<#assign src='/content/control/stream?contentId='>
				<#assign imgsrc = src +record.contentId/>-->
                        if(record.contentId){
                            td.html("<img height='100' class='cssImgSmall' src='/content/control/stream?contentId="+record.contentId+"'/>");
                        }
                    }
                },
                {"title":"品牌名称","code":"brandName","sort":true},
                {"title":"品牌别名","code":"brandNameAlias"},
                {"title":"品牌详情","code":"brandDesc"}
			],
			listName: "recordsList",
			paginateEL: "ProductFM_paginateDiv",
			headNotShow: true,
			midShowNum: 3
		});
		
		//查询按钮点击事件
		$('#searchBtn').on('click',function(){
		    console.log("click")
			var brandName = $('#brandName').val();
			ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"brandName",brandName);
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
						

	});
	

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
