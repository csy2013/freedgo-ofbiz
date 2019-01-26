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
	<form id="ProductRecommondAddForm" class="form-horizontal">
        <!--店铺编码-->
        <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
        <input type="hidden" id="linkId"/>
        <input type="hidden" id="selectName"/>
		<div class="box-body">
            <#--<div class="row">-->
				<#--<div class="form-group col-sm-8" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">-->
                    <#--<label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>-->
                    <#--<div class="col-sm-10" >-->
                    	<#--<div class="col-sm-5" style="padding-left: 0px;">-->
							<#--<input type="text" class="form-control dp-vd" id="sequenceId" />-->
							<#--<p class="dp-error-msg"></p>-->
						<#--</div>-->
                    <#--</div>-->
                <#--</div>-->
			<#--</div>-->

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

            <div class="row">
                <div class="form-group col-sm-8" data-type="required"  data-mark="推荐商品">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>推荐商品:</label>
                    <div class="col-sm-10">
                        <button id="addProducts" type="button" class="btn btn-primary">
                            <i class="fa fa-plus">选择推荐的商品</i>
                        </button>
                        <input type="hidden" name="selectedProductIds" id="selectedProductIds" class="form-control dp-vd w-p60"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group">
				<#--<label class="col-sm-2 control-label"></label>-->
                    <div class="col-sm-12">
                        <div class="table-responsive no-padding">
                            <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                                <thead>
                                <tr>
                                    <th>商品图片</th>
                                    <th>商品编码</th>
                                    <th>商品名称</th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
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
var select_id;
var curProductIds="";
$(function(){
	//初始化select2
	$(".select2").select2({
		closeOnSelect:false
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


    // 添加商品
    $('#addProducts').click(function () {
        $.dataSelectModal({
            url: "/catalog/control/ProductListMultiModalPage${externalKeyParam}",
            width: "800",
            title: "选择商品",
            selectId: "linkId",
            selectName: "selectName",
            multi: true,
            selectCallBack: function (el) {
                var productIds = el.data('id');
                getProductGoodsInfoListByIds(productIds);
            }
        });
    })

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        var id=$(this).data("id");
        $(this).parent().parent().remove();
        curProductIds=updateProductIdsInfo(id);
        $("#selectedProductIds").val(curProductIds);
    })

    
    //表单校验方法
    $('#ProductRecommondAddForm').dpValidate({
		validate: true,
        callback: function(){

	    	var sequenceId = $('#ProductRecommondAddForm #sequenceId').val();
	    	var isAllWebSite = $('#ProductRecommondAddForm #isAllWebSite').is(':checked') ? 0 : 1;
	    	var webSite = $('#ProductRecommondAddForm #webSite').val() !=null ? $('#ProductRecommondAddForm #webSite').val().join(',') : '';
            var selProductIds=$('#selectedProductIds').val();
	    	
        	//异步添加推荐商品数据
			$.ajax({
				url: "prodRecommendAdd",
				type: "POST",
				data : {
				        ids:selProductIds,
						sequenceId:sequenceId,
						isAllWebSite:isAllWebSite,
						webSite:webSite
				},
				dataType : "json",
				success: function(data){
					$.tipLayer("操作成功！");
					window.location = '<@ofbizUrl>prodRecommendPage</@ofbizUrl>';
				},
				error: function(data){
					$.tipLayer("操作失败！");
				}
			});
        }	
    });
    
    $('#save').click(function(){
    	$("#ProductRecommondAddForm").dpValidate({
    		clear: true
    	});
    	$("#ProductRecommondAddForm").submit();
    });

});

/**
 * 根据商品编码取得商品信息列表
 * @param ids
 */
function getProductGoodsInfoListByIds(ids){
    $.ajax({
        url: "/catalog/control/getProductGoodsListByIds?externalLoginKey=${externalLoginKey}",
        type: "POST",
        data: {ids: ids},
        dataType: "json",
        success: function (data) {
            var productGoodInfoList = data.productGoodInfoList;
            for (var i=0;i<productGoodInfoList.length;i++) {
                var productGoodInfo = productGoodInfoList[i];
                var productInfo = productGoodInfo.productInfo;
                var imgUrl=productGoodInfo.imgUrl;
                var productGoodFeature = productGoodInfo.productGoodFeature;
                var productId = productInfo.productId;
                var productName = productInfo.productName;

                if(chkProductIdIsSelected(productInfo.productId)){
                    var tr = '<tr id="' + productId + '">'
                            + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                            + '<td>' + productId + '</td>'
                            + '<td>' + productName + '</td>'
                            + '<td class="fc_td"><button type="button" data-id="'+productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                            + '</tr>';
                    $('#productTable>tbody').append(tr);

                    if(curProductIds==""){
                        curProductIds=productId;
                    }else{
                        curProductIds+=','+productId;
                    }
                    $("#selectedProductIds").val(curProductIds);
                }
            };
        },
        error: function (data) {
            $.tipLayer("操作失败！");
        }
    });
}
/**
 * 验证商品编码是否使用
 * @param productId
 * @returns {number}
 */
function chkProductIdIsSelected(productId){
    var chkFlg=1;
    var ids=curProductIds.split(",");
    for(var i=0;i<ids.length;i++){
        var curProductId=ids[i];
        if(curProductId==productId){
            chkFlg=0;
            return chkFlg;
        }
    }
    return chkFlg;
}

/**
 * 更新商品编码集合
 * @param productId
 * @returns {string}
 */
function updateProductIdsInfo(productId){
    var ids=curProductIds.split(",");
    var newIds="";
    for(var i=0;i<ids.length;i++){
        var curProductId=ids[i];
        if(curProductId!=productId){
            if(newIds==""){
                newIds= curProductId
            }else{
                newIds=newIds+','+curProductId;
            }
        }
    }
    return newIds
}
</script><!-- script区域end -->
