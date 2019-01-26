<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>

<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
		<!-- 条件查询start -->
		<form id="ArticleFM_QueryForm" class="form-inline clearfix">
        	<div class="form-group">
            	<div class="input-group m-b-10">
        			<span class="input-group-addon">${uiLabelMap.articleType}</span>
                	<input type="text" class="form-control" id="articleTypeName" placeholder="全部" onclick="showMenu()"  readonly="readonly"/ >
                	<input type="hidden" class="form-control" id="articleTypeId"  name="articleTypeId"/>
                	<!-- zTree start-->
                	<div id="menuContent" class="menuContent" style="display:none; position: absolute;top:33px;left:81px;border:1px solid #ccc;background:white;z-index:1000;width:196px;">
                		<ul id="articleTypeTree" class="ztree" style="margin-top: 0;">
                		</ul>
                	</div>
                	<!-- zTree end-->
        		</div>
		        <div class="input-group m-b-10">
		        	<span class="input-group-addon">${uiLabelMap.articleTitle}</span>
                    <input type="text" class="form-control" id="articleTitle">
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
        		<table id="ArticleFM_data_tbl" class="table table-bordered table-hover js-checkparent"></table>
      		</div>
    	</div><!-- 表格区域end -->
    	
    	<!-- 分页条start -->
    	<div class="row" id="ArticleFM_paginateDiv">
		</div><!-- 分页条end -->
	</div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script>
	var ArticleFM_data_tbl;
	var ArticleFM_ajaxUrl = "/content/control/ArticleListForJson?VIEW_SIZE=5";
	$(function(){
		ArticleFM_data_tbl = $('#ArticleFM_data_tbl').dataTable({
			ajaxUrl: ArticleFM_ajaxUrl,
			columns:[
				{"title":"${uiLabelMap.articleType}","code":"description"},
				{"title":"${uiLabelMap.articleTitle}","code":"articleTitle"},
				{"title":" ","code":"option",
				 "handle":function(td,record){
				 	var id = record.articleId;
				 	var name = record.articleTitle;
				 	var btn = $("<div class='btn-group'>"+
  							  "<button type='button' class='btn btn-danger btn-sm btn-select' data-id="+id+" data-name="+name+">选择</button>"+
						      "</div>");
				 	td.append(btn);
				 }
				}
			],
			listName: "articleList",
			paginateEL: "ArticleFM_paginateDiv",
			viewSizeEL: "ArticleFM_view_size",
			headNotShow: true,
			midShowNum: 3
		});
		
		//查询按钮点击事件
		$('#ArticleFM_QueryForm #searchBtn').on('click',function(){
			var articleTitle = $('#ArticleFM_QueryForm #articleTitle').val();
			var articleTypeId = $('#ArticleFM_QueryForm #articleTypeId').val();
			ArticleFM_ajaxUrl = changeURLArg(ArticleFM_ajaxUrl,"articleTitle",articleTitle);
			ArticleFM_ajaxUrl = changeURLArg(ArticleFM_ajaxUrl,"articleTypeId",articleTypeId);
			ArticleFM_data_tbl.reload(ArticleFM_ajaxUrl);
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
				url: "getAllArticleType",
				type: "GET",
				dataType : "json",
				success: function(data){
					$.fn.zTree.init($("#ArticleFM_QueryForm #articleTypeTree"), setting, data.articleTypeList);
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
    				$('#modal_msg').modal();
				}
			});
			
		//隐藏树
		$(document).on('click',function(e){
		    if($(e.target).is('#ArticleFM_QueryForm #articleTypeName')) return;
		    if($(e.target).closest('div').is('#ArticleFM_QueryForm #menuContent')) return;
		    else{$("#ArticleFM_QueryForm #menuContent").hide();}
		})
	});
	
//显示树
function showMenu(){
	$("#ArticleFM_QueryForm #menuContent").toggle();
	return false;
}

//点击某个节点 然后将该节点的名称赋值值文本框
function onClick(e, treeId, treeNode){
	var zTree = $.fn.zTree.getZTreeObj("articleTypeTree");
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
	$("#ArticleFM_QueryForm #articleTypeName").attr("value", v);
	$("#ArticleFM_QueryForm #articleTypeId").attr("value", id);
	//隐藏zTree
	return false;
}

</script><!-- script区域end -->
