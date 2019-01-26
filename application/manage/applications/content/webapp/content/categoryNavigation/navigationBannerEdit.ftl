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
	<form id="BannerEditForm" class="form-horizontal">
		<div class="box-body">
			<div class="row">
				<div class="form-group col-sm-8" data-type="required" data-mark="广告图片">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>广告图片:</label>
                    <div class="col-sm-10">
                    	<#if (parameters.contentId)?has_content>
	                    	<img alt="" src="/content/control/getImage?contentId=${parameters.contentId?if_exists}" id="img" style="max-height: 100px;max-width: 200px;">
	                    <#else>
	                    	<img alt="" src="/images/datasource/default/default_img.png" id="img" style="max-height: 100px;max-width: 200px;">
	                    </#if>
	                    <input style="margin-left:5px;" type="button" id="uploadedFile" name="uploadedFile" value="选择图片"/>
	                    <input type="hidden" id="contentId" class="dp-vd" value="${parameters.contentId?if_exists}" />
	                    <p class="dp-error-msg"></p>
                    </div>
                </div>
			</div>
			
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
							<span id="selectName" style="margin-left: 10px;color: blue;cursor: pointer;">${parameters.linkName?if_exists}</span>
							<input type="hidden" id="linkId" value="${parameters.linkId?if_exists}" />
						</div>
                    	
                    </div>
                </div>
            </div>
            
            <div class="row">
            	<div id="linkDiv" class="form-group col-sm-8" style="display:none;">
                    <label class="col-sm-2 control-label">链接地址:</label>
                    <div class="col-sm-10">
                    	<div class="col-sm-5" style="padding-left: 0px;">
                    		<input type="text" class="form-control" id="linkUrl" value="${parameters.linkUrl?if_exists}"/>
                    	</div>
                    </div>
				</div>
            </div>

    
		</div><!-- box-body end -->
		
		<!-- 按钮组 -->
		<div class="box-footer text-center">
            <button id="save" type="button" class="btn btn-primary m-r-10">保存</button>
        </div>
  	</form>
</div><!-- 内容end -->

<!-- script区域start -->
<script>

// 链接类型初始化
;(function(){
	var linkType = '${parameters.firstLinkType?if_exists}'
	var linkUrl = '${parameters.linkUrl?if_exists}'
	
	$('#firstLinkType').val(linkType)
	
	switch (linkType)
		{
		// 自定义链接
		case 'FLT_ZDYLJ':
			 $('#linkDiv').show();
			 $('#selectViewDiv').hide();
		  break;
		default:
			if(linkUrl){
				$('#selectViewDiv').show();
		   		$('#linkDiv').hide();
			}
		}
	
})()

var select_id;
$(function(){
	

	//初始化select2
	$(".select2").select2({
		closeOnSelect:false
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
					$('#linkId').val('');
					$('#selectName').html('');
					$('#linkUrl').val('');
	    			$('#linkDiv').hide();
	    			$('#selectViewDiv').hide();
				}
    	}
    }
    
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
    
    //表单校验方法
    $('#BannerEditForm').dpValidate({
		validate: true,
        callback: function(){
        	var contentId = $('#BannerEditForm #contentId').val();
	    	var firstLinkType = $('#BannerEditForm #firstLinkType').val();
	    	// var secondLinkType = $('#BannerEditForm #secondLinkType').val();
	    	var linkUrl = $('#BannerEditForm #linkUrl').val();
	    	var linkId = $('#BannerEditForm #linkId').val();
	    	var linkName = $('#BannerEditForm #selectName').html();

        	//异步加载已选地区数据
			$.ajax({
				url: "navigationBannerEdit",
				type: "POST",
				data : {
                    navigationId:'${parameters.navigationId}',
					contentId:contentId,
					firstLinkType:firstLinkType,
					// secondLinkType:secondLinkType,
					linkUrl:linkUrl,
					linkId:linkId,
					linkName:linkName
				},
				dataType : "json",
				success: function(data){
					$.tipLayer("操作成功！");
					window.location = '<@ofbizUrl>navigation?navigationGroupId=${requestParameters.navigationGroupId}</@ofbizUrl>';
				},
				error: function(data){
					$.tipLayer("操作失败！");
				}
			});
        }	
    });
    
    $('#save').click(function(){
    	$("#BannerEditForm").dpValidate({
    		clear: true
    	});
    	$("#BannerEditForm").submit();
    });
    
});
</script><!-- script区域end -->
