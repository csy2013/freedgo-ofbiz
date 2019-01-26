<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<style>
    .s-zdy{
        border: 1px solid #ccc;
        margin-top: 15px;
    }
    .s-bj-p{
        text-align: right;
    }
    .s-bj{

        margin-right: 15px;
    }
    .s-zdy p{ line-height: 24px; }
    .s-error,.s-message{margin-left: 5px;}
    .s-img{
        max-height: 230px;
        display: block;
    }
    .s-img-div{
        margin:0 10px 10px 10px;
    }


    .s-input-w{
        width: 80px;
        display: none;

    }
    .s-input-w_l{
        width: 80px;
        display:inline-block;
    }
    .s-span-c {
        height: 34px;
        padding: 6px 12px;
        display: inline-block;
        line-height: 22px;
    }
    .s-qx{
        display: none;
    }

    .has-error {
        border-color: #ed5565;
    }
</style>

    <!-- 内容start -->
<div class="box box-info">
    <form id="PTA_AddForm" class="form-horizontal">
        <div class="box-body">
            <input type="hidden" id="questionId"/>
            <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
            <div class="row">
                <div class="form-group col-sm-12" >
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>答题积分设置:</label>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12" data-type="required,format" data-reg="/^[1-9]\d*$/" data-mark="答题积分设置" >
                    <div class="col-sm-2"></div>
                    <div class="col-sm-10">
                        <span class="s-span-c s-d-1">每答对1题奖励固定积分</span>
                        <#--<input type="text" class="form-control s-input-w_l js-s-i dp-vd"  id="scoreQuestNums" name="scoreQuestNums" value="">-->
                        <#--<span class="s-span-c s-d-1">题奖励</span>-->
                        <#--<input type="text" class="form-control s-input-w_l js-s-i dp-vd"  id="scoreNums" name="scoreNums" value="">-->
                        <#--<span class="s-span-c s-d-1">积分</span>-->
                        <#--<span class="s-span-c s-d-1 dp-error-msg"></span>-->
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-12" data-type="required,format" data-reg="/^[1-9]\d*$/" data-mark="答题积分设置" >
                    <div class="col-sm-2"></div>
                    <div class="col-sm-10">
                        <span class="s-span-c s-d-1">每连续答对</span>
                        <input type="text" class="form-control s-input-w_l js-s-i dp-vd"  id="scoreQuestNums" name="scoreQuestNums" value="">
                        <span class="s-span-c s-d-1">题奖励固定积分</span>
                        <#--<input type="text" class="form-control s-input-w_l js-s-i dp-vd"  id="scoreNums" name="scoreNums" value="">-->
                        <#--<span class="s-span-c s-d-1">积分</span>-->
                        <#--<span class="s-span-c s-d-1 dp-error-msg"></span>-->
                    </div>
                </div>
            </div>





            <div class="row">
                <div class="form-group col-sm-12" >
                    <label class="col-sm-2 control-label">答题代金券设置:</label>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12" data-type="required,format" data-reg="/^[1-9]\d*$/" data-mark="答题代金券设置">
                    <div class="col-sm-2"></div>
                    <div class="col-sm-10">
                        <span class="s-span-c s-d-1">每连续答对</span>
                        <input type="text" class="form-control s-input-w_l js-s-i dp-vd" id="couponQuestNums" name="couponQuestNums" value="">
                        <span class="s-span-c s-d-1">题发放代金券</span>
                        <span class="s-span-c s-d-1 dp-error-msg"></span>
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
        // 取得答题设置信息
        getQuestionSettingInfo();

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
                var questionId = $('#PTA_AddForm #questionId').val();
                var scoreQuestNums = $('#PTA_AddForm #scoreQuestNums').val();
                // var scoreNums = $('#PTA_AddForm #scoreNums').val();
                var couponQuestNums = $('#PTA_AddForm #couponQuestNums').val();
                var promoCouponIds=$('#promoIds').val();

                // console.log(scoreQuestNums+"****"+promoCouponIds+"****"+scoreNums+"****"+couponQuestNums+"****");
                //编辑问题设置数据
                $.ajax({
                    url: "/prodPromo/control/updateQuestionSettingService?externalLoginKey=${externalLoginKey}",
                    type: "POST",
                    data : {questionId:questionId,
                        scoreQuestNums:scoreQuestNums,
                        // scoreNums:scoreNums,
                        couponQuestNums:couponQuestNums,
                        promoCouponIds:promoCouponIds
                    },
                    dataType : "json",
                    success: function(data){
                        $.tipLayer("操作成功！");
                        window.location = '<@ofbizUrl>answerTemplateSetPage</@ofbizUrl>';
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
                    var accessCount = promoCouponInfo.accessCount;

                    if(chkProductIdIsSelected(promoCouponInfo.couponCode)){
                        var tr = '<tr id="' + couponCode + '">'
                                + '<td>' + couponName + '</td>'
                                + '<td>' + payReduce + '</td>'
                                + '<td>' + payFill + '</td>'
                                + '<td>' +validitDays+ '</td>'
                                + '<td>' +applyScope+ '</td>'
//                                + '<td>' +couponQuantity+ '</td>'
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
     * 取得问题设置信息
     */
    function getQuestionSettingInfo(){
        curCouponIds="";

        //红包设置信息的取得
        jQuery.ajax({
            url: '/prodPromo/control/questionSettingDetail?externalLoginKey=${externalLoginKey}',
            type: 'POST',
            data: {

            },
            success: function(data){

                var questionSetting = data.questionSetting;
                if(questionSetting){
                    $('#PTA_AddForm #questionId').val(questionSetting.questionId);
                    $('#PTA_AddForm #scoreQuestNums').val(questionSetting.scoreQuestNums)
                    $('#PTA_AddForm #scoreNums').val(questionSetting.scoreNums);
                    $('#PTA_AddForm #couponQuestNums').val(questionSetting.couponQuestNums);
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
//                                + '<td>' +couponQuantity+ '</td>'
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
                $('#modal_msg #modal_msg_body').html("问题设置取得失败");
                $('#modal_msg').modal();
            }
        });
    }


</script><!-- script区域end -->
