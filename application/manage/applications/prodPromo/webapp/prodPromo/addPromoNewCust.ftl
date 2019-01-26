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
<form class="form-horizontal" role="form" method="post" name="addPromoNewCust" id="addPromoNewCust" action="<@ofbizUrl>addPromoNewCust</@ofbizUrl>">
    <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">会员首次促销基本信息 </h3>
        </div>
        <div class="box-body">
            <input type="hidden" id="linkId"/>
            <input type="hidden" id="selectName"/>
            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="subTitle" class="col-sm-2 control-label"><i class="required-mark">*</i>促销类型</label>
                    <div class="col-md-9"><#--促销条件枚举值-->
                        <select class="form-control  dp-vd" id="paramEnumId" name="paramEnumId">
                        <#list promoTypes as promoType>
                            <option value="${(promoType.enumCode)?if_exists}"
                                    <#if promoType.enumCode='PROMO_ORDER_AMOUNT'>selected=selected</#if>>${(promoType.get("description"))?if_exists}
                            </option>
                        </#list>
                        </select>
                    </div>
                </div>
            </div>
            <div class="row" id="div_amdisc">
                <div class="form-group col-sm-6">
                    <label for="subTitle" class="col-sm-2 control-label"><i class="required-mark">*</i>促销规则</label>
                    <label for="amdisc_amount" class="col-sm-2 control-label">优惠</label>
                    <div class="col-sm-5">
                        <input type="text" class="form-control dp-vd" id="amdisc_amount" name="amdisc_amount">
                        <p class="dp-error-msg"></p>
                    </div>
                    <label for="amdisc_amount" class="col-sm-1 control-label">元</label>
                </div>
            </div>

            <div class="row" id="div_shipcharge">
                <div class="form-group col-sm-6">
                    <label for="subTitle" class="col-sm-2 control-label"><i class="required-mark">*</i>促销规则</label>
                    <label for="addisc_amount" class="col-sm-2 control-label">免运费</label>
                </div>
            </div>

            <div class="row" id="div_gwp">
                <div class="form-group col-sm-6">
                    <label for="subTitle" class="col-sm-2 control-label"><i class="required-mark">*</i>促销规则</label>
                    <label for="addisc_amount" class="col-sm-2 control-label">赠送商品</label>
                    <button id="addProducts" type="button" class="btn btn-primary">
                        <i class="fa fa-plus">添加商品</i>
                    </button>
                </div>
                <div class="table-responsive no-padding form-group col-sm-12">
                    <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                        <thead>
                        <tr>
                            <th>货品图片</th>
                            <th>货品规格</th>
                            <th>货品编号</th>
                            <th>货品名称</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="required,linkLt" id="fromDateGroup" data-compare-link="endGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="publishDate" class="col-sm-2 control-label"><i class="required-mark">*</i>开始时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="fromDate">
                        <input id="fromDate1" class="form-control" size="16" type="text" readonly>
                        <input id="fromDate" class="dp-vd" type="hidden" name="fromDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>


            </div>
            <div class="row">
                <div id="endGroup" class="form-group col-sm-6" data-type="required,linkGt" data-compare-link="fromDateGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="thruDate" class="col-sm-2 control-label"><i class="required-mark">*</i>结束时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="thruDate">
                        <input id="thruDate1" class="form-control" size="16" type="text" readonly>
                        <input id="thruDate" class="dp-vd" type="hidden" name="thruDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="promoType" class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用</label>
                    <div class="col-sm-9 p-t-5">
                        <label class="col-sm-3" title="启用"><input type="radio" name="promoStatus" value="ACTY_AUDIT_PASS" checked></input>启用</label>
                        <label class="col-sm-3" title="不启用"><input type="radio" name="promoStatus" value="ACTY_AUDIT_NOPASS"></input>不启用</label>
                    </div>
                </div>
            </div>

        </div>
    </div>




    <div class="box-footer text-center">
        <#if security.hasEntityPermission("PRODPROMO_COUPON", "_ADD", session)>
            <button id="save" class="btn btn-primary m-r-10" onclick="savePromoNewCust()">保存</button>
        </#if>
    <#-- <button type="button" class="btn btn-primary m-r-10" onclick="back()">返回</button>-->
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


<!-- 确认弹出框start -->
<div id="modal_confirm" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">修改</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">继续</button>
            </div>
        </div>
    </div>
</div><!--确认弹出框end -->

<!-- /.box-body -->
</div>
<!-- 内容end -->

<!-- script区域start -->
<script>
    var PartyFM_data_tbl;
    var PartyFM_ajaxUrl = "/membermgr/control/personListForJson?VIEW_SIZE=5";
    $(function () {
        $('#div_gwp').hide();
        $('#div_shipcharge').hide();
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
</div>
</div>
</div>
</div>

<script type="text/javascript">
    var currentIndex = 0;
    $(function () {

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
        });
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
        //下拉框选择变化
        $('#paramEnumId').change(function () {
            flag=true;
            var paramEnumId = $('#paramEnumId').val();
            if(paramEnumId=="PROMO_ORDER_AMOUNT"){//优惠金额
                $('#div_amdisc').show();
                $('#div_gwp').hide();
                $('#div_shipcharge').hide();
            }else if(paramEnumId=="PROMO_GWP"){//赠送商品
                $('#div_amdisc').hide();
                $('#div_gwp').show();
                $('#div_shipcharge').hide();
            }else if(paramEnumId=="PROMO_FREE_SHIPPING"){//包邮
                $('#div_amdisc').hide();
                $('#div_gwp').hide();
                $('#div_shipcharge').show();
            }
        });
    });

    function init(){
        //初始化操作,从后台查询到会员首次促销信息。
        $.ajax({
            url: "findPromoNewCustService",//addPromoGiftService
            type: "POST",
            async: false,
            data: {
            },
            dataType: "json",
            success: function (data) {

                if(data.productPromo==null){
                    return;
                }
                var productStorePromoAppls = data.productStorePromoAppls;
                var actionEnumId = data.productPromoActions[0].productPromoActionEnumId;
                $('#paramEnumId').val(actionEnumId)//促销类型赋值
                //开始时间，结束时间赋值
                var fromDate = timeStamp2String2(productStorePromoAppls.fromDate.time);
                var thruDate = timeStamp2String2(productStorePromoAppls.thruDate.time);
                $("#fromDate1").val(fromDate)
                $("#fromDate").val(fromDate)

                $("#thruDate1").val(thruDate)
                $("#thruDate").val(thruDate)
                //是否启用
                var promoStatus = data.productPromo.promoStatus;
                $("input[name='promoStatus'][value='"+promoStatus+"']").attr('checked','true');

                if(actionEnumId=="PROMO_ORDER_AMOUNT"){//优惠金额
                    $('#div_amdisc').show();
                    $('#div_gwp').hide();
                    $('#div_shipcharge').hide();
                    var Amount = data.productPromoActions[0].amount
                    $("#amdisc_amount").val(Amount)
                }else if(actionEnumId=="PROMO_GWP"){//赠送商品
                    $('#div_amdisc').hide();
                    $('#div_gwp').show();
                    $('#div_shipcharge').hide();
                    //获取商品id集合
                    var productIds ="";
                    var promoActions = data.productPromoActions;
                    for(var i=0;i<promoActions.length;i++){
                        if(i==0){
                            productIds=promoActions[i].productId;
                        }else{
                            productIds=productIds+","+promoActions[i].productId;
                        }
                    }
                    if(productIds!=null&&productIds!=""){
                        getProductGoodsInfoListByIds(productIds);
                    }


                }else if(actionEnumId=="PROMO_FREE_SHIPPING"){//包邮
                    $('#div_amdisc').hide();
                    $('#div_gwp').hide();
                    $('#div_shipcharge').show();
                }

            },
            error: function (data) {

            }
        });

    }

    var curProductIds="";
    var curProductNames="";
    /**
     * 根据商品编码取得商品信息列表
     * @param ids
     */
    function getProductGoodsInfoListByIds(ids){
        $.ajax({
            url: "/catalog/control/getProductGoodsListByIds${externalKeyParam}",
            type: "POST",
            data: {ids: ids},
            dataType: "json",
            success: function (data) {
                var productGoodInfoList = data.productGoodInfoList;
                for (var i=0;i<productGoodInfoList.length;i++) {
                    var productGoodInfo = productGoodInfoList[i];
                    var productInfo = productGoodInfo.productInfo;
                    var salesPrice = productGoodInfo.salesPrice;
                    var imgUrl=productGoodInfo.imgUrl;
                    var productGoodFeature = productGoodInfo.productGoodFeature;
                    var productId = productInfo.productId;
                    var productName = productInfo.productName;
                    if(chkProductIdIsSelected(productInfo.productId)){
                        var tr = '<tr id="' + productId + '">'
                                + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' + productId + '</td>'
                                + '<td>' + productName + '</td>'
                                + '<td class="fc_td"><button type="button" data-id="'+productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#productTable>tbody').append(tr);
                        if(curProductIds==""){
                            curProductIds=productId;
                            curProductNames=productName;
                        }else{
                            curProductIds+=','+productId;
                            curProductNames+=','+productName;
                        }
                    }

                };
                $("#linkId").val(curProductIds)
                $("#selectName").val(curProductNames)
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
        var chkFlg=true;
        var ids=curProductIds.split(",");
        for(var i=0;i<ids.length;i++){
            var curProductId=ids[i];
            if(curProductId==productId){
                chkFlg=false;
                return chkFlg;
            }
        }
        return chkFlg;
    }
    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        var id=$(this).data("id");
        $(this).parent().parent().remove();
        curProductIds=updateProductIdsInfo(id);
        $("#linkId").val(curProductIds);
    })

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
            if(productId!=curProductId){
                if(newIds==""){
                    newIds=curProductId;
                }else{
                    newIds=newIds+","+curProductId;
                }
            }
        }
        return newIds
    }

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
                    var price = productPrice(productId);
                    var tr = '<tr id="' + productId + '">'
                            + '<td>' + productId + '</td>'
                            + '<td>' + productName + '</td>'
                            + price
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

    function productPrice(prodcutId) {
        var td = "";
        var prodcutId = prodcutId;
        $.ajax({
            url: "findPrice",
            type: "POST",
            async: false,
            data: {
                'productId': prodcutId
            },
            success: function (data) {
                if (data.productList) {
                    var product = data.productList[0];
                    td = '<td>' + product.marketprice + '</td><td>' + product.defaultprice + '</td>'
                } else {
                    td = '<td>' + 0 + '</td><td>' + 0 + '</td>'
                }
            },
        });
        return td;
    }

    var flag = true;

    function savePromoNewCust() {
        $('#addPromoNewCust').dpValidate({
            clear: true
        });
        var paramEnumId = $('#paramEnumId').val();
        if(paramEnumId=="PROMO_ORDER_AMOUNT"){
            var int_reg = /^[1-9]\d{0,5}\.?\d{0,2}$/;
            //优惠价格
            var amdisc_amount = $('#amdisc_amount');
            amdisc_amount.removeClass("border-color");
            var amount =amdisc_amount.val();
            if (!int_reg.test(amount)) {
                amdisc_amount.parent().siblings().eq(0).css("color", "red")
                amdisc_amount.css("border-color", "#dd4b39");
                amdisc_amount.parent().find('.dp-error-msg').text('请填写不超过999999.99的数字!')
                flag = false;
            } else {
                amdisc_amount.parent().siblings().eq(0).css("color", "")
                amdisc_amount.css("border-color", "");
                amdisc_amount.parent().find('.dp-error-msg').text('')
                flag = true;
            }
        }

    }

    $("#addPromoNewCust").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (flag) {
                addPromoNewCust();
            }
        }
    });

    function addPromoNewCust() {
        /*选择促销的产品*/
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
        var paramEnumId = $('#paramEnumId').val();
        var Amount="";
        if(paramEnumId=="PROMO_GWP"){
            Amount=1;
            if(productIds==null||productIds==""){
                $('#modal_msg #modal_msg_body').html("请选择要赠送的商品！");
                $('#modal_msg').modal();
                return;
            }
        }else if(paramEnumId=="PROMO_ORDER_AMOUNT"){
            Amount=$('#amdisc_amount').val();
        }else if(paramEnumId=="PROMO_FREE_SHIPPING"){
            Amount=10;
        }
        var promoStatus = $("input[name='promoStatus']:checked").val();
        $.ajax({
            url: "addPromoNewCustService",//addPromoGiftService
            type: "POST",
            async: false,
            data: {
                productStoreId:$('#productStoreId').val(),
                paramEnumId:paramEnumId,
                Amount:Amount,
                fromDate: $('#fromDate').val(),
                thruDate: $('#thruDate').val(),
                promoStatus:promoStatus,
                productIds: productIds,
            },
            dataType: "json",
            success: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作成功！");
                $('#modal_msg').modal();
                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                /*  $('#modal_msg').on('hide.bs.modal', function () {
                      window.location.href = '<@ofbizUrl>findPromoGift</@ofbizUrl>';
                })*/
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
                /*$('#modal_msg').on('hide.bs.modal', function () {
                    window.location.reload();
                })*/
            }
        });
    }

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        $(this).parent().parent().remove();
    })



    function back() {
        window.location.href = '<@ofbizUrl>findPromoNewCust</@ofbizUrl>';
    }
</script>
