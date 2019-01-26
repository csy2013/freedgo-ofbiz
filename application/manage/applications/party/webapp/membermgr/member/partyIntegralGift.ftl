<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<!-- Date Picker -->
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<!-- Daterange picker -->
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker-bs3.css</@ofbizContentUrl>">
<!-- daterangepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/moment.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker.js</@ofbizContentUrl>"></script>
<!-- datetimepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<form class="form-horizontal" role="form" method="post" name="addGift" id="addGift"
      action="<@ofbizUrl>saveIntegralGift</@ofbizUrl>">
<#--<input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>-->
    <input type="hidden" id="linkId"/>
    <input type="hidden" id="selectName"/>
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">积分赠送规则</h3>
        </div>
        <div class="box-body">

            <div class="row">

                <div class="form-group col-sm-6" data-type="required" data-mark="活动名称">
                    <label for="subTitle" class="col-sm-4 control-label">订单满额赠送</label>
                    <div class="col-sm-8">
                        <div class="radio clearfix">
                        <#if orderIntegralRule?? && orderIntegralRule.isFullOpen == 'Y'>
                            <label class="col-sm-4" title="开启"><input name="isFullOpen" id="isFullOpen1" type="radio"
                                                                      value="Y" checked>开启</label>
                        <#--<input type="hidden" name="isAnyReturn" value="N"/>-->
                            <label class="col-sm-4" title="关闭"><input name="isFullOpen" id="isFullOpen2" value="N"
                                                                      type="radio">关闭</label>
                        <#else>
                            <label class="col-sm-4" title="开启"><input name="isFullOpen" id="isFullOpen1" type="radio"
                                                                      value="Y">开启</label>
                        <#--<input type="hidden" name="isAnyReturn" value="N"/>-->
                            <label class="col-sm-4" title="关闭"><input name="isFullOpen" id="isFullOpen2" value="N"
                                                                      type="radio" checked>关闭</label>
                        </#if>
                        <#-- <label class="col-sm-3" title="活动可积分"><input name="isSupportScore" id="isSupportScore"  type="checkbox" value="Y">活动可积分</label>
                         <label class="col-sm-3" title="退货返回积分"><input name="isSupportReturnScore"    id="isSupportReturnScore" value="Y"  type="checkbox">退货返回积分</label>
                         <label class="col-sm-3" title="推荐到首页"><input name="isShowIndex" id="isShowIndex" type="checkbox" value="Y">推荐到首页</label>
                         <label class="col-sm-3" title="包邮"><input name="isPostageFree" id="isPostageFree" type="checkbox" value="Y">包邮</label>-->
                        </div>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row" id="gift1" <#if orderIntegralRule?? && orderIntegralRule.isFullOpen == 'Y'> style="display: block"<#else>style="display: none" </#if>>
                <div class="form-group col-sm-6" data-type="linkLt" id="startTimeGroup" data-compare-link="endTimeGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label class="col-sm-4 control-label">订单满</label>
                    <div class="input-group date form_datetime col-sm-8 p-l-15 p-r-15" data-link-field="startTime">
                        <input class="form-control s-input-w js-s-i" type="text" style="width: 70px;" value="" name="orderMoney" id="orderMoney">
                        <p class="col-sm-4 text-control">元赠送固定积分</p>
                    </div>

                </div>
            </div>
            <div class="row">
                <div id="endTimeGroup" class="form-group col-sm-6" data-type="linkGt" data-compare-link="startTimeGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="endTime" class="col-sm-4 control-label">指定商品赠送</label>
                    <div class="col-sm-8">
                        <div class="radio clearfix">
                        <#if orderIntegralRule?? && orderIntegralRule.isAssignOpen == 'Y'>
                            <label class="col-sm-4" title="开启"><input name="isAssignOpen" id="isAssignOpen1"
                                                                      type="radio" value="Y" checked>开启</label>
                        <#--<input type="hidden" name="isAnyReturn" value="N"/>-->
                            <label class="col-sm-4" title="关闭"><input name="isAssignOpen" id="isAssignOpen2" value="N"
                                                                      type="radio">关闭</label>
                        <#else>
                            <label class="col-sm-4" title="开启"><input name="isAssignOpen" id="isAssignOpen1"
                                                                      type="radio" value="Y">开启</label>
                        <#--<input type="hidden" name="isAnyReturn" value="N"/>-->
                            <label class="col-sm-4" title="关闭"><input name="isAssignOpen" id="isAssignOpen2" value="N"
                                                                      type="radio" checked>关闭</label>
                        </#if>
                        <#-- <label class="col-sm-3" title="活动可积分"><input name="isSupportScore" id="isSupportScore"  type="checkbox" value="Y">活动可积分</label>
                         <label class="col-sm-3" title="退货返回积分"><input name="isSupportReturnScore"    id="isSupportReturnScore" value="Y"  type="checkbox">退货返回积分</label>
                         <label class="col-sm-3" title="推荐到首页"><input name="isShowIndex" id="isShowIndex" type="checkbox" value="Y">推荐到首页</label>
                         <label class="col-sm-3" title="包邮"><input name="isPostageFree" id="isPostageFree" type="checkbox" value="Y">包邮</label>-->
                        </div>
                        <div class="dp-error-msg col-sm-offset-4 col-sm-8"></div>
                    </div>
                </div>

                <div class="row" id="gift2" <#if orderIntegralRule?? && orderIntegralRule.isAssignOpen == 'Y'> style="display: block"<#else>style="display: none" </#if>  >
                    <div class="form-group col-sm-8" >
                    <#--<label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>选择商品</label>-->
                        <div class="control-label col-sm-4">
                        <#--  <@htmlTemplate.lookupField formName="addGroupOrder" position="center" name="productId" id="productId" fieldFormName="LookupProduct"/>
                              <p class="dp-error-msg"></p>-->
                            <button id="addProducts" type="button" class="control-label btn btn-primary">
                                <i class="fa fa-plus">选择商品</i>
                            </button>
                        </div>
                    </div>
                </div>

                <div class="row" id="gift3" <#if orderIntegralRule?? && orderIntegralRule.isAssignOpen == 'Y'> style="display: block"<#else>style="display: none" </#if>>
                    <div class="form-group col-sm-12">
                        <label for="title" class="col-sm-2 control-label">已选择商品</label>

                        <div class="col-sm-10">
                            <div class="table-responsive no-padding">
                                <table class="table table-hover js-checkparent js-sort-list addProducts"
                                       id="productTable">
                                    <thead>
                                    <tr style="background-color: #DDDDDD">
                                        <th>商品图片</th>
                                        <th>商品名称</th>
                                        <th>规格</th>
                                        <th>商品编号</th>
                                        <th>积分code</th>
                                        <th>操作</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
    <div class="box-footer text-center">
    <#if security.hasEntityPermission("PARTY_INTEGRALGIFT", "_CREATE", session)>
        <button id="save" class="btn btn-primary m-r-10" onclick="saveGroupOrder();">保存</button>
    </#if>
    <#--<button type="button" class="btn btn-primary m-r-10" onclick="back()">返回</button>-->
    </div>
</form>

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_msg_title">操作提示</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->


<!-- script区域start -->
<script>
    var PartyFM_data_tbl;
    var PartyFM_ajaxUrl = "/membermgr/control/personListForJson?VIEW_SIZE=5";
    $(function () {
        $('#gift1').hide();
        $('#gift2').hide();
        $('#gift3').hide();
        init();
        PartyFM_data_tbl = $('#PartyFM_data_tbl').dataTable({
            ajaxUrl: PartyFM_ajaxUrl,
            columns: [
                {"title": "商品编码", "code": "productCode"},
                {"title": "商品名称", "code": "productName"},
                {"title": "品牌", "code": "brandName"},
                {
                    "title": " ", "code": "option",
                    "handle": function (td, record) {
                        var id = record.partyId;
                        var name = record.name;
                        var btn = $("<div class='btn-group'>" +
                                "<button type='button' class='btn btn-danger btn-sm btn-select' data-id='" + id + "' data-name='" + name + "'>选择</button>" +
                                "</div>");
                        td.append(btn);
                    }
                }
            ],
            listName: "recordsList",
            paginateEL: "PartyFM_paginateDiv",
            headNotShow: true,
            midShowNum: 3,
            isModal: true
        });
        //查询按钮点击事件
        $('#PartyFM_QueryForm #searchBtn').on('click', function () {
            var partyId = $('#PartyFM_QueryForm #partyId').val();
            var name = $('#PartyFM_QueryForm #name').val();

            PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl, "partyId", partyId);
            PartyFM_ajaxUrl = changeURLArg(PartyFM_ajaxUrl, "name", name);
            PartyFM_data_tbl.reload(PartyFM_ajaxUrl);
            return false;
        });
    });
</script>

<script type="text/javascript">

    /*$(function () {
        $('.form_datetime').datetimepicker({
            language: 'zh-CN',
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });

        $('.form_date').datetimepicker({
            language: 'zh-CN',
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            minView: 2,
            forceParse: 0
        });

        $('.form_time').datetimepicker({
            language: 'zh-CN',
            autoclose: 1,
            startView: 1,
            forceParse: 0
        });*/

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

    function init(){
        //初始化操作,从后台查询到会员首次促销信息。
        $.ajax({
            url: "findIntegralNewService",//addPromoGiftService
            type: "POST",
            async: false,
            data: {
            },
            dataType: "json",
            success: function (data) {
                console.log(data)
                if(data.orderIntegralRuleProd==null){
                    return;
                }
                var orderIntegralRuleProd = data.orderIntegralRuleProd;
                /* var actionEnumId = data.productPromoActions[0].productPromoActionEnumId;
                 $('#paramEnumId').val(actionEnumId)//促销类型赋值
                 //开始时间，结束时间赋值
                 var fromDate = timeStamp2String2(productStorePromoAppls.fromDate);
                 var thruDate = timeStamp2String2(productStorePromoAppls.thruDate);
                 $("#fromDate1").val(fromDate)
                 $("#fromDate").val(fromDate)
                 $("#thruDate1").val(thruDate)
                 $("#thruDate").val(thruDate)*/
                //是否启用
                var isFullOpen = data.orderIntegralRule.isFullOpen;
                var isAssignOpen = data.orderIntegralRule.isAssignOpen;
                var orderMoney = data.orderIntegralRule.orderMoney;
                $("input[name='isFullOpen'][value='"+isFullOpen+"']").attr('checked','true');
                $("input[name='isAssignOpen'][value='"+isAssignOpen+"']").attr('checked','true');
                $("input[name='orderMoney']").val(orderMoney);
                if(isFullOpen=="Y"){//是否开启
                    $('#gift1').show();
                    $("#orderMoney").val(orderMoney)
                }
                if(isAssignOpen=="Y"){//赠送商品
                    $('#gift2').show();
                    $('#gift3').show();
                    //获取商品id集合
                    var productIds ="";
                    var promoActions = data.orderIntegralRuleProd;
                    for(var i=0;i<promoActions.length;i++){
                        if(i==0){
                            productIds=promoActions[i].partyGiftProdId;
                        }else{
                            productIds=productIds+","+promoActions[i].partyGiftProdId;
                        }
                    }

                    //获取积分赠送集合
                    var integralCodes ="";
                    for(var i=0;i<promoActions.length;i++){
                        if(i==0){
                            integralCodes=promoActions[i].integralCodeNo;
                        }else{
                            integralCodes=integralCodes+","+promoActions[i].integralCodeNo;
                        }
                    }


                    if(productIds!=null&&productIds!=""){
                        getProductGoodsInfoListByIds(productIds,integralCodes);
                    }
                }

            },
            error: function (data) {

            }
        });

    }

    var curProductIds = "";
    /**
     * 根据商品编码取得商品信息列表
     * @param ids
     */
    function getProductGoodsInfoListByIds(ids,codes) {
        if(codes!=null){
            var integralCodeArr = codes.split(",");
        }
        $.ajax({
            url: "/catalog/control/getProductGoodsListByIds?externalLoginKey=${externalLoginKey}",
            type: "POST",
            data: {ids: ids},
            dataType: "json",
            success: function (data) {
                var productGoodInfoList = data.productGoodInfoList;
                for (var i = 0; i < productGoodInfoList.length; i++) {
                    if(integralCodeArr!=null){
                        var integralCode = integralCodeArr[i];
                    }
                    var productGoodInfo = productGoodInfoList[i];
                    var productInfo = productGoodInfo.productInfo;
                    var salesPrice = productGoodInfo.salesPrice;
                    var imgUrl = productGoodInfo.imgUrl;
                    var productGoodFeature = productGoodInfo.productGoodFeature;
                    var productId = productInfo.productId;
                    var productName = productInfo.productName;

                    if (chkProductIdIsSelected(productInfo.productId)) {
                        var tr = '<tr id="' + productId + '">'
                                + '<td><img height="100" src="' + imgUrl + '" class="cssImgSmall" alt="" /></td>'
                                + '<td>' + productName + '</td>'
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' + productId + '</td>'
//                                + '<td><input name="activityPersonNum" type="number"></td>'
                                + '<td><select  name="integralCodeNo" class="form-control select2 dp-vd"  data-placeholder="请选择积分">'
                    <#assign integralCodeList = delegator.findByAnd("IntegralCode",{"status":"Y"})><#if integralCodeList?has_content>
                        <#list integralCodeList as integralCode>
                            if((integralCode=='${integralCode.integralCodeNo}')&&integralCode!=null){
                                tr+='<option  value="${integralCode.integralCodeNo}" selected="selected">'
                                        +'${integralCode.integralCodeNo}'
                                        +'</option>'
                            }else{
                                tr+='<option  value="${integralCode.integralCodeNo}">'
                                        +'${integralCode.integralCodeNo}'
                                        +'</option>'
                            }
                        </#list>
                    </#if>
                        tr+='</select>'
                                +'</td>'
                                + '<td class="fc_td"><button type="button" data-id="' + productId + '" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '<td><input type="hidden" name="PartyGiftProdId" value="' + productId + '"></td>'
                                + '</tr>';
                        $('#productTable>tbody').append(tr);
                        if (curProductIds == "") {
                            curProductIds = productId;
                        } else {
                            curProductIds += ',' + productId;
                        }
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
    function chkProductIdIsSelected(productId) {
        var chkFlg = true;
        var ids = curProductIds.split(",");
        for (var i = 0; i < ids.length; i++) {
            var curProductId = ids[i];
            if (curProductId == productId) {
                chkFlg = false;
                return chkFlg;
            }
        }
        return chkFlg;
    }
    /**
     * 返回处理
     */
    function back() {
        window.location.href = '<@ofbizUrl>findGroupOrder</@ofbizUrl>';
    }

    var currentIndex = 0;
    //异步递归创建组合信息表格
    function createPromoTable(ids_arr) {
        if (currentIndex >= ids_arr.length) {
            return;
        }
        var productId = ids_arr[currentIndex];
        if ($('#productTable>tbody').find('tr#' + productId).length > 0) {
            currentIndex++;
            createPromoTable(ids_arr);
        } else {
            //异步查询促销活动信息
            $.ajax({
                url: "/catalog/control/find_ProductbyId?externalLoginKey=${externalLoginKey}",
                type: "POST",
                data: {idToFind: productId},
                dataType: "json",
                success: function (data) {
                    var product = data.product;
                    var productName = product.productName;
                    var tr = '<tr id="' + productId + '">'
                            + '<td>' + productId + '</td>'
                            + '<td>' + productId + '</td>'
                            + '<td>' + productId + '</td>'
                            + '<td>' + productName + '</td>'
                            + '<td><input name="activityQuantity" type="number"></td>'
                            + '<td><input name="activityPersonNum" type="number"></td>'
                            + '<td><input name="activityPrice" type="text"></td>'
                            + '<td class="fc_td"><button type="button" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                            + '</tr>';
                    $('#productTable>tbody').append(tr);
                    currentIndex++;
                    createPromoTable(ids_arr);
                },
                error: function (data) {
                    currentIndex++;
                    createPromoTable(ids_arr);
                    $.tipLayer("操作失败！");
                }
            });
        }
    }
    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        var id = $(this).data("id");
        $(this).parent().parent().remove();
        curProductIds = updateProductIdsInfo(id);
    })
    /**
     * 更新商品编码集合
     * @param productId
     * @returns {string}
     */
    function updateProductIdsInfo(productId) {
        var ids = curProductIds.split(",");
        var newIds = "";
        for (var i = 0; i < ids.length; i++) {
            var curProductId = ids[i];
            if (productId != curProductId) {
                if (newIds == "") {
                    newIds = curProductId;
                } else {
                    newIds = newIds + "," + curProductId;
                }
            }
        }
        return newIds
    }
    var numFlag = true;
    function saveGroupOrder() {
        $('#addGift').dpValidate({
            clear: true
        });

    }
    $("#addGift").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (numFlag) {
                addGroupOrder();
            }
        }
    });
    function addGroupOrder() {
        var productIds = "";
        $('#productTable>tbody').find("tr").each(function () {
            var tdArr = $(this);
            for (var i = 0; i < tdArr.length; i++) {
                var productId = tdArr.eq(i).attr("id");//产品编号
                if (productId) {
                    productIds += productId + ","
                }
            }
        });

        $('#addGift').dpValidate({
            clear: true
        });
        /*var lineLen = $('#productTable>tbody').find("tr").length;
        if (lineLen == 0) {
            $('#modal_msg #modal_msg_body').html("请选择参加团购的商品");
            $('#modal_msg').modal();
            return;
        }*/
        //获取团购商品的详细信息
        var productLineList = "";
        var lines = $('#productTable>tbody').find("tr");
        for (var i = 0; i < lines.length; i++) {
            var tdArr = $(lines[i]);
            var productId = tdArr.find('td').eq(3).text();
            console.log(tdArr.find('td').eq(4).find("select"))
            var integralCodeNo = tdArr.find('td').eq(4).find("select").eq(0).val();//积分code值
            /*if (integralCodeNo == null || integralCodeNo == "") {
                $('#modal_msg #modal_msg_body').html("商品编码为：" + productId + " 的积分Code不能为空");
                $('#modal_msg').modal();
                return;
            }*/
            var line = productId + "," + integralCodeNo ;
            productLineList = productLineList + line + "|"
        }
        var money = $("input[name='orderMoney']").val();
        //是否为整数
        var reg=/^[1-9]*[1-9][0-9]*$/;
        if(money!=null&&money!=''){
            if(!reg.test(money.trim())){
                $(this).siblings('.js-s-i').addClass('has-error');
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html('您输入的整数类型格式不正确!请输入大于零的整数!');
                $('#modal_msg').modal();
                return;
            }
        }

        var data = {
            productLineList:productLineList,
            isFullOpen: $("input[name='isFullOpen']:checked").val(),
            isAssignOpen: $("input[name='isAssignOpen']:checked").val(),
            orderMoney: $("input[name='orderMoney']").val(),
            /*activityEndDate: $('#activityEndDate').val(),
            limitQuantity: $('#limitQuantity').val(),//每个id限购数量
            isAnyReturn: isAnyReturn,
            isSupportOverTimeReturn: isSupportOverTimeReturn,
            isSupportScore: "N",
            isSupportReturnScore: "N",
            isShowIndex: "N",
            isPostageFree: "N",
            activityDesc: CKEDITOR.instances['textData'].getData(),*/
        }

        $.ajax({
            url: "saveIntegralGift",
            type: "POST",
            async: false,
            data: data,
            dataType: "json",
            success: function (data) {
                if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                    $.tipLayer(data._ERROR_MESSAGE_);
                } else {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("操作成功！");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    /*$('#modal_msg').on('hide.bs.modal', function () {
                        window.location.href = '';  //999
                    })*/
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
                /*$('#modal_msg').on('hide.bs.modal', function () {
                    window.location.reload();
                })*/
            }
        })
    }


</script>

<script>
    $("input[name='isFullOpen']").click(function () {
        if ($("input[name='isFullOpen']:checked").val() == "Y") {
            $("#gift1").show();
        }else {
            $("#gift1").hide();
        }
    })
    $("input[name='isAssignOpen']").click(function () {
        if ($("input[name='isAssignOpen']:checked").val() == "Y") {
            $("#gift2").show();
            $("#gift3").show();
        }else {
            $("#gift2").hide();
            $("#gift3").hide();
        }
    })



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
</script>
<script>
</script>

