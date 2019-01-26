<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>

<!-- 内容start -->
<div class="box box-info">
	<div class="box-body">
    	<!-- 树形列表start -->
    	<div class="row">
    		<div class="categoryTreeBackground left">
				<ul id="categoryTree" class="ztree"></ul>
			</div>
		</div><!-- 树形列表end -->
	</div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<SCRIPT type="text/javascript">
	var curMenu = null, zTree_Menu = null;
	var setting = {
		view: {
			showIcon: false,
			showLine: false,
			addDiyDom: addDiyDom
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		callback: {
			onClick: onClick
		}
	};
	//自定义节点
	function addDiyDom(treeId, treeNode) {
		//设置左边距
		var level = treeNode.level;
		$("#" + treeNode.tId + "_switch").css({"margin-left":(level*20)+"px"});
		//公共属性，弹出框需要用
		$("#" + treeNode.tId + "_a").addClass("btn-select").data("name",treeNode.name);
		$("#" + treeNode.tId + "_a").attr("data-id",treeNode.id);
		$("#" + treeNode.tId + "_a").attr("data-name",treeNode.name);
		//移除a标签的点击样式
		$("#"+treeId).find('a').removeClass('curSelectedNode');
	}
	
	function onClick(event,treeId, treeNode) {
		$("#"+treeId).find('a').removeClass('curSelectedNode');
	}
	
	$(document).ready(function(){
		//异步加载分类数据
		$.ajax({
			url: "<@ofbizUrl>getProductCategoryList</@ofbizUrl>",
			type: "POST",
			dataType : "json",
			success: function(data){
				$.fn.zTree.init($("#categoryTree"), setting, data.productCategoryList);
				//var treeObj = $.fn.zTree.getZTreeObj("categoryTree");
				//treeObj.expandAll(true); 
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
				$('#modal_msg').modal();
			}
		});
		
		//树节点的hover事件，改变背景色
		$(document).on("mouseover mouseout",'#categoryTree li',function(event){
			var isHover = $('#categoryTree li').hasClass("li-hover");
			if(event.type == "mouseover" && !isHover){
				$(this).addClass('li-hover');
			}else if(event.type == "mouseout"){
				$(this).removeClass('li-hover');
			}
		});
	});
</SCRIPT>
<style type="text/css">
	.ztree * {font-size: 10pt;font-family:"Microsoft Yahei",Verdana,Simsun,"Segoe UI Web Light","Segoe UI Light","Segoe UI Web Regular","Segoe UI","Segoe UI Symbol","Helvetica Neue",Arial}
	.ztree{margin: 0;padding: 0px;color: #333;border: 1px solid #ddd;}
	.ztree li ul{margin: 0;padding: 0px;color: #333;border-top: 1px solid #ddd;}
	.ztree li {line-height:30px;position: relative;display: block;margin-bottom: -1px;background-color: #fff;border-top: 1px solid #ddd;}
	.ztree li:first-child{border-top:none;}
	.ztree li ul span.button.switch {margin-left:20px;}
	.ztree li.curSelectedNode {background-color:#D4D4D4;border:0;height:30px;}
	.ztree li.li-hover{text-decoration:none; background-color: #E7E7E7;}
</style><!-- script区域end -->
