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
	<form id="BrandRecommondAddForm" class="form-horizontal">
        <!--店铺编码-->
        <input type="hidden" id="brandStoreId" name="brandStoreId" value="${requestAttributes.productStoreId}"/>
        <input type="hidden" id="linkId"/>
        <input type="hidden" id="selectName"/>
		<div class="box-body">
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
                <div class="form-group col-sm-8" data-type="required"  data-mark="热门品牌">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>热门品牌:</label>
                    <div class="col-sm-10">
                        <button id="addBrands" type="button" class="btn btn-primary">
                            <i class="fa fa-plus">选择热门品牌</i>
                        </button>
                        <input type="hidden" name="selectedBrandIds" id="selectedBrandIds" class="form-control dp-vd w-p60"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group">
                    <div class="col-sm-12">
                        <div class="table-responsive no-padding">
                            <table class="table table-hover js-checkparent js-sort-list addBrands" id="brandTable">
                                <thead>
                                <tr>
                                    <th>品牌名称</th>
                                    <th>品牌LOGO</th>
                                    <th>品牌别名</th>
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
var curBrandIds="";
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


    // 添加品牌
    $('#addBrands').click(function () {
        $.dataSelectModal({
            url: "/prodPromo/control/BrandListMultiModalPage?externalLoginKey=${externalLoginKey}",
            width: "800",
            title: "选择品牌",
            selectId: "linkId",
            selectName: "selectName",
            multi: true,
            selectCallBack: function (el) {
                var brandIds = el.data('id');
                console.log(brandIds);
                getBrandListByIds(brandIds);
            }
        });
        return false;
    })

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        var id=$(this).data("id");
        $(this).parent().parent().remove();
        curBrandIds=updateBrandIdsInfo(id);
        $("#selectedBrandIds").val(curBrandIds);
    })

    
    //表单校验方法
    $('#BrandRecommondAddForm').dpValidate({
		validate: true,
        callback: function(){

	    	var sequenceId = $('#BrandRecommondAddForm #sequenceId').val();
	    	var isAllWebSite = $('#BrandRecommondAddForm #isAllWebSite').is(':checked') ? 0 : 1;
	    	var webSite = $('#BrandRecommondAddForm #webSite').val() !=null ? $('#BrandRecommondAddForm #webSite').val().join(',') : '';
            var selBrandIds=$('#selectedBrandIds').val();
	    	
        	//异步添加热门品牌数据
			$.ajax({
				url: "brandRecommendAdd",
				type: "POST",
				data : {
				        ids:selBrandIds,
						sequenceId:sequenceId,
						isAllWebSite:isAllWebSite,
						webSite:webSite
				},
				dataType : "json",
				success: function(data){
					$.tipLayer("操作成功！");
					window.location = '<@ofbizUrl>brandRecommendPage</@ofbizUrl>';
				},
				error: function(data){
					$.tipLayer("操作失败！");
				}
			});
        }	
    });
    
    $('#save').click(function(){
    	$("#BrandRecommondAddForm").dpValidate({
    		clear: true
    	});
    	$("#BrandRecommondAddForm").submit();
    });

});

/**
 * 根据商品编码取得商品信息列表
 * @param ids
 */
function getBrandListByIds(ids){
    $.ajax({
        url: "/prodPromo/control/getBrandListByIds?externalLoginKey=${externalLoginKey}",
        type: "POST",
        data: {ids: ids},
        dataType: "json",
        success: function (data) {
            var brandInfoList = data.brandInfoList;
            for (var i=0;i<brandInfoList.length;i++) {
                var brandInfo = brandInfoList[i];
                var imgUrl=brandInfo.imgUrl;
                var brandId = brandInfo.productBrandId;
                var brandName = brandInfo.brandName;
                var brandNameAlias = "";
                if(brandInfo.brandNameAlias){
                    brandNameAlias=brandInfo.brandNameAlias;
                }



                if(chkBrandIdIsSelected(brandId)){
                    var tr = '<tr id="' + brandId + '">'
                            + '<td>' + brandName + '</td>'
                            + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                            + '<td>' + brandNameAlias + '</td>'
                            + '<td class="fc_td"><button type="button" data-id="'+brandId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                            + '</tr>';
                    $('#brandTable>tbody').append(tr);

                    if(curBrandIds==""){
                        curBrandIds=brandId;
                    }else{
                        curBrandIds+=','+brandId;
                    }
                    $("#selectedBrandIds").val(curBrandIds);
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
 * @param brandId
 * @returns {number}
 */
function chkBrandIdIsSelected(brandId){
    var chkFlg=1;
    var ids=curBrandIds.split(",");
    for(var i=0;i<ids.length;i++){
        var cuBrandId=ids[i];
        if(cuBrandId==brandId){
            chkFlg=0;
            return chkFlg;
        }
    }
    return chkFlg;
}

/**
 * 更新商品编码集合
 * @param brandId
 * @returns {string}
 */
function updateBrandIdsInfo(brandId){
    var ids=curBrandIds.split(",");
    var newIds="";
    for(var i=0;i<ids.length;i++){
        var cuBrandId=ids[i];
        if(cuBrandId!=brandId){
            if(newIds==""){
                newIds= cuBrandId
            }else{
                newIds=newIds+','+cuBrandId;
            }
        }
    }
    return newIds
}
</script><!-- script区域end -->
