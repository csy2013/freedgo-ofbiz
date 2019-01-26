<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<#--<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>-->
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<#--<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>-->
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
	<form id="NoticeAddForm" class="form-horizontal">
		<div class="box-body">
			<div class="row">
				<div class="form-group col-sm-8" data-type="required" data-mark="公告标题">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>公告标题:</label>
                    <div class="col-sm-10">
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <input type="text" class="form-control dp-vd" id="noticeTitle" />
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
			</div>
			
			<!--公告标签 start-->
      	      <div class="row"  style="display:none">
                        <div class="form-group col-sm-8"  data-number="1">
                            <label class="col-sm-2 control-label">公告标签:</label>
                            <div class="col-sm-10">
                                <div class="checkbox clearfix">
                                   <#if tagList?has_content>
                                   <#list tagList as tag>
                                   <label>
                                   <input type="radio" name="noticeTag"  value="${tag.tagId?if_exists}">
                                   ${tag.tagName?if_exists}
                                    </label>
                                   </#list>
                                   </#if>
                                </div>
                                <div class="dp-error-msg"></div>
                            </div>
                        </div>
                   </div>
                <!--文章标签 end-->
			
    		<div class="row">
                <div class="form-group col-sm-8">
                    <label class="col-sm-2 control-label">链接类型:</label>
                    <div class="col-sm-10">
                    	<div style="overflow:hidden;margin-bottom: 10px;">
	                    	<select id="firstLinkType" class="form-control" style="width:200px;float: left;margin-right: 20px;">
	                    		<option value="">===请选择链接类型===</option>
	                    		<#if linkTypes?has_content && (linkTypes?size > 0)>
	                    			<#list linkTypes as linkType>
	                                    <option value="${linkType.enumId}">${linkType.description}</option>
	                                </#list>
	                    		</#if>
	                    	</select>
	                    	
	                    </div>
	                    	
                    	<div id="selectViewDiv" style="margin-top: 10px;display:none;"  >
							<span>已选择:</span>
							<span id="selectName" style="margin-left: 10px;color: blue;cursor: pointer;"></span>
							<input type="hidden" id="linkId" />
						</div>
                    	
                    </div>
                </div>
            </div>
            
             <div class="row">
            	<div id="linkDiv" class="form-group col-sm-8" style="display:none;">
                    <label class="col-sm-2 control-label">链接地址:</label>
                    <div class="col-sm-10">
                    	<div class="col-sm-5" style="padding-left: 0px;">
                    		<input type="text" class="form-control" id="linkUrl"/>
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


            <#--<div class="row">-->
                <#--<div class="form-group col-sm-8">-->
                    <#--<label class="col-sm-2 control-label">适用范围:</label>-->
                    <#--<div class="col-sm-10">-->
                        <#--<div class="radio col-sm-6">-->
                            <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Dining">餐饮</label>-->
                            <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Relaxation">休闲</label>-->
                            <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Cinema">电影</label>-->
                            <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Hotel">酒店</label>-->
                        <#--</div>-->
                    <#--</div>-->
                <#--</div>-->
            <#--</div>-->
			
			<div class="row">
				<div class="form-group col-sm-8">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用:</label>
                    <div class="col-sm-10">
                    	<div class="radio col-sm-3">
                            <label class="col-sm-6"><input name="isUse" type="radio" checked value="0">是</label>
                            <label class="col-sm-6"><input name="isUse" type="radio" value="1">否</label>
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
                    <input type="hidden" id="geo" name="geo"/>
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
        </div>
  	</form>
</div><!-- 内容end -->

<!-- script区域start -->
<script>
var select_id;
$(function(){
    var treeObj;
    //初始化select2
	$(".select2").select2({
		closeOnSelect:false
	});
	
    // 链接类型选择链接回调处理
    var selectLinkHandler = function(type){
    	switch (type) {
    		case 'FLT_WZLJ':
	    		{
	    			$('#linkDiv').hide();
	    			$.dataSelectModal({
	    				url: "ArticleListModalPage",
	    				width:	"800",
	    				title:	"选择文章",
	    				selectId: "linkId",
	    				selectName:	"selectName",
	    				selectCallBack: function(el){
	    					$('#selectViewDiv').show();
	    					$('#linkUrl').val('modalName=WZ&id='+el.data('id'));
	    				}
	    			});
	    		}
    			break;
    		case 'FLT_HDLJ':
	    		{
	    			$('#linkDiv').hide();
	    			$.dataSelectModal({
	    				url: "/prodPromo/control/ProActivityMgrListModalPage?externalLoginKey=${externalLoginKey}",
	    				width:	"800",
	    				title:	"选择活动",
	    				selectId: "linkId",
	    				selectName:	"selectName",
	    				selectCallBack: function(el){
	    					$('#selectViewDiv').show();
	    					$('#linkUrl').val('modalName=HD&id='+el.data('id'));
	    				}
	    			});
	    		}
    			break;
    		<#-- 自定义链接  -->
    		case 'FLT_ZDYLJ':
	    		{
	    			$('#selectViewDiv').hide();
	    			$('#linkId').val('');
					$('#selectName').html('');
					$('#linkUrl').val('');
	    			$('#linkDiv').show();
	    		}
    			break;
    		<#-- 商品选择添加 -->
    		case 'FLT_SPLJ':
	    		{
	    			$('#linkDiv').hide();
	    			$.dataSelectModal({
	    				url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
	    				width:	"800",
	    				title:	"选择商品",
	    				selectId: "linkId",
	    				selectName:	"selectName",    
	    				selectCallBack: function(el){
	    					$('#selectViewDiv').show();
	    					$('#linkUrl').val('modalName=SP&id='+el.data('id'));
	    				}
	    			});
	    		}
    			break;
    		// 促销类型
    		case 'FLT_CXLJ':
    			{
    				$('#linkDiv').hide();
    				$.dataSelectModal({
	    				url: "/prodPromo/control/PromoListModalPage?externalLoginKey=${externalLoginKey}",
	    				width:	"800",
	    				title:	"选择促销",
	    				selectId: "linkId",
	    				selectName:	"selectName",    
	    				selectCallBack: function(el){
	    					$('#selectViewDiv').show();
	    					var modalName = el.data('record').activityType;
	    					$('#linkUrl').val('modalName='+modalName+'&id='+el.data('id'));
	    				}
	    			});
    			}
    		default:
				{
					$('#selectViewDiv').hide();
					$('#linkId').val('');
					$('#selectName').html('');
					$('#linkUrl').val('');
	    			$('#linkDiv').hide();
				}
    	}
    }
    
    <#-- 页面已选择链接按钮  -->
    //已选的名称点击事件
    $('#selectName').on('click',function(){
    	var flt =  $('#firstLinkType').val()
    	selectLinkHandler(flt)
    	
    });
    
    //链接地址一的选项切换事件
     $('#firstLinkType').on('change',function(){
    	var type = $(this).val()
    	selectLinkHandler(type)
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
        }
    };
    $.ajax({
        url: "<@ofbizUrl>areaTree</@ofbizUrl>",
        type: "GET",
        dataType: "json",
        success: function (data) {
            treeObj = $.fn.zTree.init($("#add_geo_area"), setting, data.areaTree);
            treeObj.expandAll(false);
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
            var nodes = treeObj.getNodes();
            treeObj.setChkDisabled(nodes[0], true, true, true);
    	}else{
            var nodes = treeObj.getNodes();
            treeObj.setChkDisabled(nodes[0], false, true, true);
            treeObj.checkAllNodes(false);
    	}
    });*/
    
    //表单校验方法
    $('#NoticeAddForm').dpValidate({
		validate: true,
        callback: function(){
//            var isAllGeo = $('#NoticeAddForm #isAllGeo').is(':checked') ? 0 : 1;
//
//            var ids = '';
//            var nodes = treeObj.getCheckedNodes(true);
//            if(nodes){
//                for (var i = 0; i < nodes.length; i++) {
//                    ids += nodes[i].id + ",";
//                }
//            }
//            $('#NoticeAddForm #geo').val(ids);
//            if(ids=='' && isAllGeo){
//                alert('请选择地区树');
//                return ;
//            }
//            var geo = $('#NoticeAddForm #geo').val();
        	var noticeTitle = $('#NoticeAddForm #noticeTitle').val();
	    	var firstLinkType = $('#NoticeAddForm #firstLinkType').val();
	    	//var secondLinkType = $('#NoticeAddForm #secondLinkType').val();
	    	var linkUrl = $('#NoticeAddForm #linkUrl').val();
	    	var linkId = $('#NoticeAddForm #linkId').val();
	    	var linkName = $('#NoticeAddForm #selectName').html();
	    	var sequenceId = $('#NoticeAddForm #sequenceId').val();
	    	var isUse = $('input[name="isUse"]:checked').val();
	    	var isAllWebSite = $('#NoticeAddForm #isAllWebSite').is(':checked') ? 0 : 1;
	    	var webSite = $('#NoticeAddForm #webSite').val() !=null ? $('#NoticeAddForm #webSite').val().join(',') : '';
	    	var noticeTag =  $('.radio').find('#NoticeAddForm input[name=noticeTag]').length==0?'':$('#NoticeAddForm input:radio[name="noticeTag"]:checked').val();
//            var applyScope = $('input[name="applyScope"]:checked').val();
	    	
        	//异步加载已选地区数据
			$.ajax({
				url: "<@ofbizUrl>NoticeAdd</@ofbizUrl>",
				type: "POST",
				data : {
                    noticeTitle:noticeTitle,
                    firstLinkType:firstLinkType,
                    //secondLinkType:secondLinkType,
                    linkUrl:linkUrl,
                    linkId:linkId,
                    linkName:linkName,
                    sequenceId:sequenceId,
                    isUse:isUse,
                    isAllWebSite:isAllWebSite,
                    webSite:webSite,
//                    isAllGeo:isAllGeo,
//                    geo:geo,
                    noticeTag: noticeTag
//                    applyScope:applyScope
				},
				dataType : "json",
				success: function(data){
					$.tipLayer("操作成功！");
					window.location = '<@ofbizUrl>NoticeListPage</@ofbizUrl>';
				},
				error: function(data){
					$.tipLayer("操作失败！");
				}
			});
        }	
    });
    
    $('#save').click(function(){
    	$("#NoticeAddForm").dpValidate({
    		clear: true
    	});
    	$("#NoticeAddForm").submit();
    });
		
});
</script><!-- script区域end -->
