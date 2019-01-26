<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
	<form id="PTA_AddForm" class="form-horizontal">
		<div class="box-body">
            <input type="hidden" id="packageId"/>
            <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
			<div class="row">
				<div class="form-group col-sm-12" data-type="required,format" data-reg="/^[1-9]\d*$/" data-mark="活动有效期">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>活动有效期:</label>
                    <div class="col-sm-10">
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <input type="text" class="form-control dp-vd" id="hourRange" name="hourRange" />
                            <p class="dp-error-msg"></p>
                        </div>
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <lebel>小时</label>
                        </div>
                    </div>
                </div>
			</div>
		
			<div class="row">
				<div class="form-group col-sm-12" data-type="required" data-mark="参与人数">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>参与人数:</label>
                    <div class="col-sm-10">
                        <div style="overflow:hidden;margin-bottom: 10px;">
                            <select id="nums" name="nums" class="form-control" style="width:150px;">
                                <option value="2">2人</option>
                                <option value="4">4人</option>
                                <option value="6">6人</option>
                                <option value="8">8人</option>
                            </select>
                        </div>
	                    <p class="dp-error-msg"></p>
                    </div>
                </div>
			</div>


			
			<div class="row">
				<div class="form-group col-sm-12">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用:</label>
                    <div class="col-sm-10">
                    	<div class="radio col-sm-3">
                            <label class="col-sm-6"><input name="isUsed" type="radio" checked value="Y">是</label>
                            <label class="col-sm-6"><input name="isUsed" type="radio" value="N">否</label>
                        </div>
                    </div>
                </div>
			</div>

            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="代金券">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>代金券:</label>
                    <div class="col-sm-10">
                        <button id="btn_promo_add" class="btn btn-primary" >
                            添加
                        </button>
                        <input type="hidden" id="promoIds"/>
                        <input type="hidden" id="promoNames" />
                        <table id="promo_tbl" class="table table-bordered table-hover m-t-10">
                            <thead>
                            <tr>
                                <th>代金券名称</th>
                                <th>代金券金额</th>
							    <th>使用条件</th>
                                <th>代金券有效期</th>
                                <th>适用范围</th>
                                <#--<th>发放数量</th>-->
                                <th>剩余数量</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            </tbody>
                        </table>
                        <p class="dp-error-msg"></p>
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
var curCouponIds="";
$(function(){
    // 取得红包设置信息
    getPromoRedPackectSettingInfo();

    $('#btn_promo_add').click(function(){
        $("#promoIds").val("");
        $("#promoNames").val("");
        $.dataSelectModal({
            url: "/prodPromo/control/PromoCouponListMultiModalPage?externalLoginKey=${externalLoginKey}&isRedPackage=Y&productStoreId="+$("#productStoreId").val(),
            width:	"800",
            title:	"选择代金券",
            multi:	true,
            selectId: "promoIds",
            selectName:	"promoNames",
            selectCallBack: function(el){
                var ids = el.data('id');
                getPromoCouponInfoListByIds(ids);
            }
        });
        return false;
    });



    //表单校验方法
    $('#PTA_AddForm').dpValidate({
		validate: true,
        callback: function(){
            var packageId = $('#PTA_AddForm #packageId').val();
        	var hourRange = $('#PTA_AddForm #hourRange').val();
        	var nums = $('#PTA_AddForm #nums').val();
	    	var isUsed = $('input[name="isUsed"]:checked').val();
            var promoCouponIds=$('#promoIds').val();
            //console.log(hourRange+"****"+promoCouponIds+"****"+nums+"****"+isUsed+"****");
	    	
        	//异步加载已选地区数据
			$.ajax({
				url: "/prodPromo/control/updatePromoRedPacketSettingService?externalLoginKey=${externalLoginKey}",
				type: "POST",
				data : {packageId:packageId,
                        hourRange:hourRange,
                        nums:nums,
                        isUsed:isUsed,
                        promoCouponIds:promoCouponIds

				},
				dataType : "json",
				success: function(data){
					$.tipLayer("操作成功！");
					window.location = '<@ofbizUrl>activityOpenRedSetPage</@ofbizUrl>';
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

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        var id=$(this).data("id");
        $(this).parent().parent().remove();
        curCouponIds=updateCouponIdsInfo(id);
        $('#promoIds').val(curCouponIds);
    })
		
});

/**
 * 根据代金券编码取得代金券信息列表
 * @param ids
 */
function getPromoCouponInfoListByIds(ids){
    $.ajax({
        url: "/prodPromo/control/getPromoCouponListByIds?externalLoginKey=${externalLoginKey}",
        type: "POST",
        data: {ids: ids},
        dataType: "json",
        success: function (data) {
            var promoCouponInfoList = data.promoCouponInfoList;
            for (var i=0;i<promoCouponInfoList.length;i++) {
                var promoCouponInfo = promoCouponInfoList[i];

                var couponCode = promoCouponInfo.couponCode;
                var couponName = promoCouponInfo.couponName;
                var payReduce=promoCouponInfo.payReduce;
                var payFill = promoCouponInfo.payFill;
                var validitDays = promoCouponInfo.validitDays;
                var applyScope = promoCouponInfo.applyScope;
                var couponQuantity = promoCouponInfo.couponQuantity;
                var accessCount=promoCouponInfo.accessCount;

                if(chkProductIdIsSelected(promoCouponInfo.couponCode)){
                    var tr = '<tr id="' + couponCode + '">'
                            + '<td>' + couponName + '</td>'
                            + '<td>' + payReduce + '</td>'
                            + '<td>' + payFill + '</td>'
                            + '<td>' +validitDays+ '</td>'
                            + '<td>' +applyScope+ '</td>'
//                            + '<td>' +couponQuantity+ '</td>'
                            + '<td>' +accessCount+ '</td>'
                            + '<td class="fc_td"><button type="button" data-id="'+couponCode+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                            + '</tr>';
                    $('#promo_tbl>tbody').append(tr);

                    if(curCouponIds==""){
                        curCouponIds=couponCode;
                    }else{
                        curCouponIds+=','+couponCode;
                    }
                }

            }
            $('#promoIds').val(curCouponIds);
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
function chkProductIdIsSelected(id){
    var chkFlg=1;
    var ids=curCouponIds.split(",");
    for(var i=0;i<ids.length;i++){
        var curCouponId=ids[i];
        if(curCouponId==id){
            chkFlg=0;
            return chkFlg;
        }
    }
    return chkFlg;
}

/**
 * 更新代金券编码集合
 * @param id
 * @returns {string}
 */
function updateCouponIdsInfo(id){
    var ids=curCouponIds.split(",");
    var newIds="";
    for(var i=0;i<ids.length;i++){
        var curCouponId=ids[i];
        if(curCouponId!=id){
            if(newIds==""){
                newIds= curCouponId
            }else{
                newIds=newIds+','+curCouponId  ;
            }
        }
    }
    return newIds
}



/**
 * 取得红包设置信息
 */
function getPromoRedPackectSettingInfo(){
    curCouponIds="";

    //红包设置信息的取得
    jQuery.ajax({
        url: '/prodPromo/control/promoRedPacketDetail?externalLoginKey=${externalLoginKey}',
        type: 'POST',
        data: {

        },
        success: function(data){

            var redPackageSetting = data.redPackageSetting;
            if(redPackageSetting){
                $('#PTA_AddForm #packageId').val(redPackageSetting.packageId);
                $('#PTA_AddForm #hourRange').val(redPackageSetting.hourRange);
                $('#PTA_AddForm #nums').val(redPackageSetting.nums);
                if(redPackageSetting.isUsed=='Y'){
                    $("input:radio[name='isUsed']").eq(0).attr("checked",'checked');
                }else{
                    $('input:radio[name="isUsed"]').eq(1).attr("checked",'checked');
                }

            }

            if (data.promoCouponList) {
                var coupon_List = data.promoCouponList;
                $('#promo_tbl>tbody').empty();
                var tr1 = "";
                for (var i = 0; i < coupon_List.length; i++) {
                    var couponCode = coupon_List[i].couponCode;
                    var couponName = coupon_List[i].couponName;
                    var payReduce=coupon_List[i].payReduce;
                    var payFill = coupon_List[i].payFill;
                    var validitDays = coupon_List[i].validitDays;
                    var applyScope = coupon_List[i].applyScope;
                    var couponQuantity = coupon_List[i].couponQuantity;
                    var accessCount=coupon_List[i].accessCount;

                    var tr = '<tr id="' + couponCode + '">'
                            + '<td>' + couponName + '</td>'
                            + '<td>' + payReduce + '</td>'
                            + '<td>' + payFill + '</td>'
                            + '<td>' +validitDays+ '</td>'
                            + '<td>' +applyScope+ '</td>'
//                            + '<td>' +couponQuantity+ '</td>'
                            + '<td>' +accessCount+ '</td>'
                            + '<td class="fc_td"><button type="button" data-id="'+couponCode+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                            + '</tr>';
                    $('#promo_tbl>tbody').append(tr);


                    if(i==0){
                        curCouponIds=couponCode;
                    }else{
                        curCouponIds+=','+couponCode;
                    }
                }
                $('#promoIds').val(curCouponIds);

            }
        },
        error: function (data) {
            //隐藏新增弹出窗口
            $('#modal_audit_productGrouping').modal('toggle');
            //设置提示弹出框内容
            $('#modal_msg #modal_msg_body').html("红包设置取得失败");
            $('#modal_msg').modal();
        }
    });
}


</script><!-- script区域end -->
