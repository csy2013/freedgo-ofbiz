<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
	<form id="PTA_AddForm" class="form-horizontal">
		<div class="box-body">
			<div class="row">
				<div class="form-group col-sm-12" data-type="required"  data-mark="活动名称">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>活动名称:</label>
                    <div class="col-sm-10" >
                    	<div class="col-sm-5" style="padding-left: 0px;">
							<input type="text" class="form-control dp-vd" id="topicActivityName" />
							<p class="dp-error-msg"></p>
						</div>
                    </div>
                </div>
			</div>
		
			<#--<div class="row">-->
				<#--<div class="form-group col-sm-12" data-type="required" data-mark="图片（小图）">-->
                    <#--<label class="col-sm-2 control-label"><i class="required-mark">*</i>图片（小图）:</label>-->
                    <#--<div class="col-sm-10">-->
	                    <#--<img alt="" src="" id="img_smallImg" style="max-height: 100px;max-width: 200px;">-->
	                    <#--<input type="button" id="btn_smallImg" value="选择图片"/>-->
	                    <#--<input type="hidden" id="smallImg" class="dp-vd" />-->
	                    <#--<p class="dp-error-msg"></p>-->
                    <#--</div>-->
                <#--</div>-->
			<#--</div>-->
			
			<div class="row">
				<div class="form-group col-sm-12" data-type="required" data-mark="图片（大图）">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>图片（大图）:</label>
                    <div class="col-sm-4">
	                    <img alt="" src="" id="img_bigImg" style="max-height: 100px;max-width: 400px;">
	                    <input type="button" id="btn_bigImg" value="选择图片"/>
	                    <input type="hidden" id="bigImg" class="dp-vd" />
	                    <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-sm-6">
                        <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 490*250px</div>
                    </div>
                </div>
			</div>
			
    		<#--<div class="row">-->
                <#--<div class="form-group col-sm-12">-->
                    <#--<label class="col-sm-2 control-label">链接类型:</label>-->
                    <#--<div class="col-sm-10">-->
                    	<#--<div style="overflow:hidden;margin-bottom: 10px;">-->
	                    	<#--<select id="linkType" class="form-control" style="width:200px;float: left;margin-right: 20px;">-->
	                    		<#--&lt;#&ndash;<option value="">====请选择====</option>&ndash;&gt;-->
                                <#--&lt;#&ndash;<option value="CX">促销</option>&ndash;&gt;-->
                                <#--&lt;#&ndash;<option value="HD">活动</option>&ndash;&gt;-->

                                <#--<option value="">===请选择链接类型===</option>-->
								<#--<#if linkTypes?has_content && (linkTypes?size > 0)>-->
									<#--<#list linkTypes as linkType>-->
										<#--<#if linkType.enumId !="FLT_CXLJ" && linkType.enumId !="FLT_HDLJ" >-->
											<#--<option value="${linkType.enumId}">${linkType.description}</option>-->
										<#--</#if>-->
									<#--</#list>-->
								<#--</#if>-->
	                    	<#--</select>-->
	                    <#--</div>-->

                    	<#--<div id="selectViewDiv" style="margin-top: 10px;display:none;"  >-->
							<#--<span>已选择:</span>-->
							<#--<span id="linkName" style="color: blue;cursor: pointer;"></span>-->
							<#--<input type="hidden" id="linkId" />-->
							<#--<input type="hidden" id="linkUrl" />-->
						<#--</div>-->
                    <#--</div>-->
                <#--</div>-->
            <#--</div>-->


            <div class="row">
                <div class="form-group col-sm-12">
                    <label class="col-sm-2 control-label">链接类型:</label>
                    <div class="col-sm-10">
                        <div style="overflow:hidden;margin-bottom: 10px;">
                            <select id="linkType" class="form-control" style="width:200px;float: left;margin-right: 20px;">
                                <option value="">===请选择链接类型===</option>
                            <#if linkTypes?has_content && (linkTypes?size > 0)>
                                <#list linkTypes as linkType>
                                    <#if linkType.enumId !="FLT_CXLJ" && linkType.enumId !="FLT_HDLJ" >
                                        <option value="${linkType.enumId}">${linkType.description}</option>
                                    </#if>
                                </#list>
                            </#if>
                            </select>
                        </div>

                        <div id="selectViewDiv" style="margin-top: 10px;display:none;"  >
                            <span>已选择:</span>
                            <span id="linkName" style="margin-left: 10px;color: blue;cursor: pointer;"></span>
                            <input type="hidden" id="linkId" />
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div id="linkDiv" class="form-group col-sm-12" style="display:none;">
                    <label class="col-sm-2 control-label">链接地址:</label>
                    <div class="col-sm-10">
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <input type="text" class="form-control" id="linkUrl"/>
                        </div>
                    </div>
                </div>
            </div>

            <!--活动标签 start-->
            <div class="row">
                <div class="form-group col-sm-12"  data-type="required" data-mark="标签">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>活动标签:</label>
                    <#assign tagList = delegator.findByAnd("Tag",{"tagTypeId":"ActivityTag","isDel":"N"}) >
                    <div class="col-sm-10" >
                        <div class="checkbox clearfix">
                            <input type="hidden" class="dp-vd" name="tag" id="tag"/>
                            <#if tagList?has_content>
                                <#list tagList as tag>
                                    <label>
                                        <input type="checkbox" name="tagId"  data-id="${tag.tagId?if_exists}" value="${tag.tagId?if_exists}">
                                    ${tag.tagName?if_exists}
                                    </label>
                                </#list>
                            </#if>
                        </div>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <!--活动标签 end-->
            
            <div class="row">
				<div class="form-group col-sm-12" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">
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
				<div class="form-group col-sm-12">
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
				<div class="form-group col-sm-12" data-type="required" data-mark="站点">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>
                    <div class="col-sm-10">
                    	<div class="col-sm-5" style="padding-left: 0px;">
							<select id="webSite" class="form-control select2 dp-vd" multiple="multiple" data-placeholder="请选择站点">
								<#assign webSiteList = delegator.findByAnd("WebSite") >
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
			
			<#--<div class="row">-->
				<#--<div class="form-group col-sm-12" data-type="required" data-mark="社区">-->
                    <#--<label class="col-sm-2 control-label"><i class="required-mark">*</i>社区:</label>-->
                    <#--<div class="col-sm-10">-->
                    	<#--<div class="col-sm-5" style="padding-left: 0px;">-->
							<#--<select id="community" class="form-control select2 dp-vd" multiple="multiple" data-placeholder="请选择社区">-->
								<#--<#assign communityList = delegator.findByAnd("Community") >-->
		        				<#--<#if communityList?has_content>-->
		        					<#--<#list communityList as community>-->
					                  <#--<option value="${community.communityId}">${community.name}</option>-->
					                <#--</#list>-->
		        				<#--</#if>-->
		                    <#--</select>-->
		                    <#--<p id="c_error_msg" class="dp-error-msg"></p>-->
						<#--</div>-->
						<#--<div class="col-sm-3" style="padding-left: 0px;">-->
							<#--<div class="checkbox">-->
		                        <#--<label>-->
		                    		<#--<input id="isAllCommunity" type="checkbox">所有社区-->
		                        <#--</label>-->
		                    <#--</div>-->
						<#--</div>-->
                    <#--</div>-->
                <#--</div>-->
			<#--</div>-->
    
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
       		var contentId = data.uploadedFile0,
       			imgUrl = "/content/control/stream?contentId="+contentId;
       		
       		switch(witch_img){
       			case "smallImg" :{
       				$('#smallImg').val(contentId);
       				$('#img_smallImg').attr({"src":imgUrl});
       			}
       			break;
       			case "bigImg" :{
       				$('#bigImg').val(contentId);
       				$('#img_bigImg').attr({"src":imgUrl});
       			}
       			break;
       		}
       })
	});
    
    var witch_img;
    // 图片（小图）选择控件显示
    $('#btn_smallImg').click(function(){
    	witch_img = "smallImg";
        $.chooseImage.show();
    });
    
    // 图片（大图）选择控件显示
    $('#btn_bigImg').click(function(){
    	witch_img = "bigImg";
        $.chooseImage.show();
    });
    
    //已选的名称点击事件
    $('#linkName').on('click',function(){
    	$('#linkType').trigger('change');
    });

    //链接地址一的选项切换事件
     $('#linkType').on('change',function(){
//    	switch ($(this).val()) {
//    		case 'CX':
//	    		{
//	    			$.dataSelectModal({
//	    				url: "/prodPromo/control/PromoListModalPage",
//	    				width:	"800",
//	    				title:	"选择促销",
//	    				selectId: "linkId",
//	    				selectName:	"linkName",
//	    				selectCallBack: function(el){
//	    					$('#selectViewDiv').show();
//	    					var modalName = el.data('record').activityType;
//	    					$('#linkUrl').val('modalName='+modalName+'&id='+el.data('id'));
//	    				}
//	    			});
//	    		}
//    			break;
//    		case 'HD':
//	    		{
//	    			$.dataSelectModal({
//	    				url: "/prodPromo/control/ProActivityMgrListModalPage",
//	    				width:	"800",
//	    				title:	"选择活动",
//	    				selectId: "linkId",
//	    				selectName:	"linkName",
//	    				selectCallBack: function(el){
//	    					$('#selectViewDiv').show();
//	    					$('#linkUrl').val('modalName=HD&id='+el.data('id'));
//	    				}
//	    			});
//	    		}
//    			break;
//    		default:
//    			{
//    				$('#linkId').val('');
//    				$('#linkName').html('');
//    				$('#linkUrl').val('');
//    				$('#selectViewDiv').hide();
//    			}
//    			break;
//    	}
         switch ($(this).val()) {
             case 'FLT_WZLJ':
             {
                 $('#linkDiv').hide();
                 $.dataSelectModal({
                     url: "ArticleListModalPage",
                     width:	"800",
                     title:	"选择文章",
                     selectId: "linkId",
                     selectName:	"linkName",
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
                     selectName:	"linkName",
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
                     selectName:	"linkName",
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
                     selectName:	"linkName",
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
    
    //所有社区的选中事件
    $('#isAllCommunity').change(function(){
    	if($(this).prop("checked")){
    		$("#community").val(null).trigger("change");
    		$("#community").prop("disabled", true);
    		$("#community").removeClass('dp-vd');
    	}else{
    		$("#community").prop("disabled", false);
    		$("#community").addClass('dp-vd');
    	}
    });
    
    //表单校验方法
    $('#PTA_AddForm').dpValidate({
		validate: true,
        callback: function(){
        	var topicActivityName = $('#PTA_AddForm #topicActivityName').val();
        	var bigImg = $('#PTA_AddForm #bigImg').val();
        	var smallImg = $('#PTA_AddForm #smallImg').val();
        	var linkType = $('#PTA_AddForm #linkType').val();
        	var linkUrl = $('#PTA_AddForm #linkUrl').val();
        	var linkId = $('#PTA_AddForm #linkId').val();
	    	var linkName = $('#PTA_AddForm #linkName').html();
	    	var sequenceId = $('#PTA_AddForm #sequenceId').val();
	    	var isUse = $('input[name="isUse"]:checked').val();
	    	var isAllWebSite = $('#PTA_AddForm #isAllWebSite').is(':checked') ? 0 : 1;
	    	var webSite = $('#PTA_AddForm #webSite').val() !=null ? $('#PTA_AddForm #webSite').val().join(',') : '';
//	    	var isAllCommunity = $('#PTA_AddForm #isAllCommunity').is(':checked') ? 0 : 1;
//	    	var community = $('#PTA_AddForm #community').val() !=null ? $('#PTA_AddForm #community').val().join(',') : '';
            var tagIds=""
            $('#PTA_AddForm input[name="tagId"]:checked').each(function(){
                tagIds = tagIds + "," + $(this).val();
            });
            var tags=tagIds.substr(1,tagIds.length);
            console.log(tagIds);
        	//异步加载已选地区数据
			$.ajax({
				url: "/prodPromo/control/TopicActivityAdd?externalLoginKey=${externalLoginKey}",
				type: "POST",
				data : {topicActivityName:topicActivityName,
						smallImg:smallImg,
						bigImg:bigImg,
						linkType:linkType,
						linkUrl:linkUrl,
						linkId:linkId,
						linkName:linkName,
						sequenceId:sequenceId,
						isUse:isUse,
						isAllWebSite:isAllWebSite,
						webSite:webSite,
                        tagId:tags
//						isAllCommunity:isAllCommunity,
//						community:community
				},
				dataType : "json",
				success: function(data){
					$.tipLayer("操作成功！");
					window.location = '<@ofbizUrl>TopicActivityListPage</@ofbizUrl>';
				},
				error: function(data){
					$.tipLayer("操作失败！");
				}
			});
        }	
    });
    
    $('#save').click(function(){
    	$("#PTA_AddForm").dpValidate({
    		clear: true
    	});
    	$("#PTA_AddForm").submit();
    });
    $('input[name="tagId"]').click(function(){
        var curTagId=$(this).data("id");
        var curTagIds=""
        $('#PTA_AddForm input[name="tagId"]:checked').each(function(){
            curTagIds = curTagIds + "," + $(this).val();
        });
        $('#PTA_AddForm input[name="tag"]').val(curTagIds.substr(1,curTagIds.length));
//        console.log(curTagId);
    })
		
});
</script><!-- script区域end -->
