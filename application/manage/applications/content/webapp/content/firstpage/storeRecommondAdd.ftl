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
	<form id="StoreRecommondAddForm" class="form-horizontal">
        <!--店铺编码-->
        <input type="hidden" id="storeStoreId" name="storeStoreId" value="${requestAttributes.productStoreId}"/>
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
                <div class="form-group col-sm-8" data-type="required"  data-mark="热门店铺">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>热门店铺:</label>
                    <div class="col-sm-10">
                        <button id="addStores" type="button" class="btn btn-primary">
                            <i class="fa fa-plus">选择热门店铺</i>
                        </button>
                        <input type="hidden" name="selectedStoreIds" id="selectedStoreIds" class="form-control dp-vd w-p60"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group">
                    <div class="col-sm-12">
                        <div class="table-responsive no-padding">
                            <table class="table table-hover js-checkparent js-sort-list addStores" id="storeTable">
                                <thead>
                                <tr>
                                    <th>店铺名称</th>
                                    <th>商家编号</th>
                                    <th>店铺LOGO</th>
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
var curStoreIds="";
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


    // 添加店铺
    $('#addStores').click(function () {
        $.dataSelectModal({
            url: "/prodPromo/control/StoreListMultiModalPage?externalLoginKey=${externalLoginKey}",
            width: "800",
            title: "选择店铺",
            selectId: "linkId",
            selectName: "selectName",
            multi: true,
            selectCallBack: function (el) {
                var storeIds = el.data('id');
                console.log(storeIds);
                getStoreListByIds(storeIds);
            }
        });
        return false;
    })

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        var id=$(this).data("id");
        $(this).parent().parent().remove();
        curStoreIds=updateStoreIdsInfo(id);
        $("#selectedStoreIds").val(curStoreIds);
    })

    
    //表单校验方法
    $('#StoreRecommondAddForm').dpValidate({
		validate: true,
        callback: function(){

	    	var sequenceId = $('#StoreRecommondAddForm #sequenceId').val();
	    	var isAllWebSite = $('#StoreRecommondAddForm #isAllWebSite').is(':checked') ? 0 : 1;
	    	var webSite = $('#StoreRecommondAddForm #webSite').val() !=null ? $('#StoreRecommondAddForm #webSite').val().join(',') : '';
            var selStoreIds=$('#selectedStoreIds').val();
	    	
        	//异步添加热门店铺数据
			$.ajax({
				url: "storeRecommendAdd",
				type: "POST",
				data : {
				        ids:selStoreIds,
						sequenceId:sequenceId,
						isAllWebSite:isAllWebSite,
						webSite:webSite
				},
				dataType : "json",
				success: function(data){
					$.tipLayer("操作成功！");
					window.location = '<@ofbizUrl>storeRecommendPage</@ofbizUrl>';
				},
				error: function(data){
					$.tipLayer("操作失败！");
				}
			});
        }	
    });
    
    $('#save').click(function(){
    	$("#StoreRecommondAddForm").dpValidate({
    		clear: true
    	});
    	$("#StoreRecommondAddForm").submit();
    });

});

/**
 * 根据商品编码取得商品信息列表
 * @param ids
 */
function getStoreListByIds(ids){
    $.ajax({
        url: "/prodPromo/control/getStoreListByIds?externalLoginKey=${externalLoginKey}",
        type: "POST",
        data: {ids: ids},
        dataType: "json",
        success: function (data) {
            var storeInfoList = data.storeInfoList;
            for (var i=0;i<storeInfoList.length;i++) {
                var storeInfo = storeInfoList[i];
                var imgUrl=storeInfo.imgUrl;
                var storeId = storeInfo.productStoreId;
                var storeName='';
                if(storeInfo.partyName){
                    storeName = storeInfo.partyName;
                }
                var partyId = storeInfo.partyId;


                if(chkStoreIdIsSelected(storeId)){
                    var tr = '<tr id="' + storeId + '">'
                            + '<td>' + storeName + '</td>'
                            + '<td>' + partyId + '</td>'
                            + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                            + '<td class="fc_td"><button type="button" data-id="'+storeId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                            + '</tr>';
                    $('#storeTable>tbody').append(tr);

                    if(curStoreIds==""){
                        curStoreIds=storeId;
                    }else{
                        curStoreIds+=','+storeId;
                    }
                    $("#selectedStoreIds").val(curStoreIds);
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
 * @param storeId
 * @returns {number}
 */
function chkStoreIdIsSelected(storeId){
    var chkFlg=1;
    var ids=curStoreIds.split(",");
    for(var i=0;i<ids.length;i++){
        var cuStoreId=ids[i];
        if(cuStoreId==storeId){
            chkFlg=0;
            return chkFlg;
        }
    }
    return chkFlg;
}

/**
 * 更新商品编码集合
 * @param storeId
 * @returns {string}
 */
function updateStoreIdsInfo(storeId){
    var ids=curStoreIds.split(",");
    var newIds="";
    for(var i=0;i<ids.length;i++){
        var cuStoreId=ids[i];
        if(cuStoreId!=storeId){
            if(newIds==""){
                newIds= cuStoreId
            }else{
                newIds=newIds+','+cuStoreId;
            }
        }
    }
    return newIds
}
</script><!-- script区域end -->
