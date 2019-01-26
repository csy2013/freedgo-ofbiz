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
<form class="form-horizontal" role="form" method="post" name="addDiscount" id="addDiscount"
      action="<@ofbizUrl>addDiscount</@ofbizUrl>">
    <input type="hidden" name="productStoreId" id="productStoreId" value="${requestAttributes.productStoreId}"/>
    <input type="hidden" id="productPromoId" name="productPromoId" value="${productPromo.productPromoId?if_exists}">
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">折扣修改基本信息</h3>
        </div>

        <div class="box-body">

            <div class="row" style="display: none">
                <input type="hidden" id="linkId"/>
                <input type="hidden" id="selectName"/>
                <div class="form-group col-sm-6" data-mark="促销编码">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>促销编码</label>
                    <div class="col-sm-9">
                        <input type="text" readonly class="form-control dp-vd" id="promoCode" name="promoCode"
                               value="${productPromo.promoCode?if_exists}"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>

            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="促销名称">
                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>促销名称</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="promoName" name="promoName"
                               value="${productPromo.promoName?if_exists}">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="linkLt" id="fromDateGroup" data-compare-link="endGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="fromDate">
                        <input class="form-control" size="16" type="text"  <#if parameters.isPass=='Y'>disabled="disabled"</#if>
                               value="${productStorePromoAppls.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}" readonly>
                        <input id="fromDate" class="dp-vd" type="hidden"
                               value="${productStorePromoAppls.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}"
                               name="fromDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

                <div id="endGroup" class="form-group col-sm-6" data-type="linkGt" data-compare-link="fromDateGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="thruDate">
                        <input class="form-control" size="16" type="text"  <#if parameters.isPass=='Y'>disabled="disabled"</#if>
                               value="${productStorePromoAppls.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}" readonly>
                        <input id="thruDate" class="dp-vd" type="hidden"
                               value="${productStorePromoAppls.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}"
                               name="thruDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>折扣类型</label>
                    <div class="col-md-9">
                        <select class="form-control  dp-vd" id="promoType" name="promoType">
                        <#list promoDiscountTypes as promoType>
                            <option value="${(promoType.enumCode)?if_exists}">${(promoType.get("description",locale))?if_exists}</option>
                        </#list>
                        </select>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>


                <div class="form-group col-sm-6">
                    <label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>折扣规则</label>
                    <div class="col-md-9" id="proPriceDiv">
                        <div id="proPrice">
                            <div class="p-0 col-sm-9">
                                <div class="input-group">
                                    <div class="input-group-addon"><span id="pre">满</span></div>
                                    <input type="text" class="form-control" name="condValue" placeholder="金额"
                                           size="3">
                                    <div class="input-group-addon"><span>打</span></div>
                                    <input type="text" class="form-control" name="amount" placeholder="折"
                                           size="5"/>
                                    <div class="input-group-addon"><span>折</span></div>
                                </div>
                                <div class="col-sm-12 p-0">
                                    <div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>
                                    <div class="col-sm-6 p-0 quantity" style="padding-left:40px !important;"></div>
                                </div>
                            </div>
                        </div>
                        <div class="input-group">
                            <input type="button" class="btn btn-success" onclick="addPrice('','')" value="+添加多级"/>
                        </div>
                    </div>
                    <div class="col-md-9" id="proQuantityDiv">
                        <div id="proQuantity">
                            <div class="p-0 col-sm-9">
                                <div class="input-group">
                                    <div class="input-group-addon"><span id="pre">满</span></div>
                                    <input type="text" class="form-control" name="condValue" placeholder="数量" size="3">
                                    <div class="input-group-addon"><span>打</span></div>
                                    <input type="text" class="form-control" name="amount" placeholder="折" size="5"/>
                                    <div class="input-group-addon"><span>折</span></div>
                                </div>
                                <div class="col-sm-12 p-0">
                                    <div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>
                                    <div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>
                                </div>
                            </div>
                        </div>
                        <div class="input-group">
                            <input type="button" class="btn btn-success" onclick="addQuantityPrice('','')"
                                   value="+添加多级"/>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6">
                    &nbsp;
                </div>
            </div>

        </div>
    </div>

    <div class="box box-info" id="product">
        <div class="box-header">
            <h3 class="box-title">参与商品</h3>
        </div>
        <div class="box-body table-responsive no-padding">
            <div class="dp-tables_btn p-l-15 p-b-15">
                <button id="addProducts" type="button" class="btn btn-primary">
                    <i class="fa fa-plus">添加商品</i>
                </button>
            </div>
            <div class="table-responsive no-padding">
                <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                    <thead>
                    <tr>
                        <th>货品图片</th>
                        <th>货品规格</th>
                        <th>货品编码</th>
                        <th>货品名称</th>
                    <#--<th>市场价</th>-->
                    <#--<th>销售价</th>-->
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="box-footer text-center">
    <#if security.hasEntityPermission("PRODPROMO_DISCOUNT", "_UPDATE", session)>
        <button id="save" class="btn btn-primary m-r-10" onclick="savePromoReduce()">保存</button>
    </#if>
        <button type="button" class="btn btn-primary m-r-10" onclick="back()">返回</button>
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

<script type="text/javascript">
    var currentIndex = 0;

    $(function () {

        <#if parameters.isPass=='N'>
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

        </#if>

        $('#addProducts').click(function () {
            <#--$.dataSelectModal({-->
                <#--url: "/catalog/control/ProductListMultiModalPage2${externalKeyParam}",-->
                <#--width: "800",-->
                <#--title: "选择商品",-->
                <#--selectId: "linkId",-->
                <#--selectName: "selectName",-->
                <#--multi: true,-->
                <#--selectCallBack: function (el) {-->
                    <#--var productIds = el.data('id');-->
                    <#--getProductGoodsInfoListById(productIds);-->
                <#--}-->
            <#--});-->
            var sDate = $('#fromDate').val();
            var eDate = $('#thruDate').val();
            var timestamp2 = Date.parse(new Date(sDate));
            var timestamp3 = Date.parse(new Date(eDate));
            timestamp2 = timestamp2 / 1000;
            timestamp3 = timestamp3 / 1000;
            if (!(sDate && eDate)) {
                $.tipLayer("请输入开始时间、结束时间！");
                return;
            }
            if(sDate>=eDate) {
                $.tipLayer("开始时间要小于等于结束时间！");
                return;
            }
            $.dataSelectModal({
                url: "/catalog/control/ProductListMultiModalPage2${externalKeyParam}&startDate="+timestamp2+"&endDate="+timestamp3,
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
        });

        var condValues = [];
        var amounts = [];
        var promoType = "";
    <#list condActList as condActions>
        <#assign condValue = condActions.get("condValue")>
        <#assign inputParamEnumId = condActions.get("inputParamEnumId")>
        <#assign amount = condActions.get("amount")>
        condValues.push(${condValue})
        amounts.push(${amount})
        promoType = '${inputParamEnumId}';
    </#list>
        $("#promoType").val('${inputParamEnumId}');
        if (promoType == 'PPIP_ORDER_TOTAL') {
            $('#proQuantityDiv').hide();
            $('#proPriceDiv').show();
        } else {
            $('#proQuantityDiv').show();
            $('#proPriceDiv').hide();
        }

        var productIdArrs = [];
    <#list productList as productLists>
        <#assign productId = productLists.get("productId")>
        productIdArrs.push('${productId}');
    </#list>

        var productIds = productIdArrs.join(",");

        getProductGoodsInfoListByIds(productIds);

        for (var i = 0; i < condValues.length; i++) {
            var condValue = condValues[i];
            var amount = amounts[i];

            if (promoType == 'PPIP_ORDER_TOTAL') {
                if (i == 0) {
                    $("#proPriceDiv").find("input[name='condValue']").eq(0).val(condValue)
                    $("#proPriceDiv").find("input[name='amount']").eq(0).val(amount)
                }else{
                    addPrice(condValue, amount);

                }
            } else {
                if (i == 0) {
                    $("#proQuantityDiv").find("input[name='condValue']").eq(0).val(condValue)
                    $("#proQuantityDiv").find("input[name='amount']").eq(0).val(amount)
                }else{
                    addQuantityPrice(condValue, amount);
                }
            }

//            if (condValue1) {
//                if (product.inputParamEnumId == 'PPIP_ORDER_TOTAL') {
//                    addPrice();
//                    $("#cond").val(condValue[i]);
//                    $("#quantity").val(amount[i]);
//                } else {
//                    addQuantityPrice();
//                    $("#cond").val(condValue[i]);
//                    $("#quantity").val(amount[i]);
//                }
//                $("#condValue").val(condValue[i + 1]);
//                $("#amount").val(amount[i + 1]);
//            }

        }
        // }

    });

    var curProductIds = "";

    function getProductGoodsInfoListByIds(productIds) {
        $.ajax({
            url: "/catalog/control/getProductGoodsListByIds?externalLoginKey=${externalLoginKey}",
            type: "POST",
            data: {ids: productIds},
            dataType: "json",
            success: function (data) {
                var productGoodInfoList = data.productGoodInfoList;
                for (var i = 0; i < productGoodInfoList.length; i++) {
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
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' + productId + '</td>'
                                + '<td>' + productName + '</td>'
                                + '<td class="fc_td"><button type="button" data-id="' + productId + '" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#productTable>tbody').append(tr);
                        if (curProductIds == "") {
                            curProductIds = productId;
                        } else {
                            curProductIds += ',' + productId;
                        }
                    }

                }
            },
            error: function (data) {
                $.tipLayer("操作失败！");
            }
        });

    }

    /**
     * 根据商品编码取得商品信息列表
     * 进行添加操作时用该方法
     * @param ids
     */
    function getProductGoodsInfoListById(ids) {
        $.ajax({
            url: "/catalog/control/getProductGoodsListByIds?externalLoginKey=${externalLoginKey}",
            type: "POST",
            data: {ids: ids},
            dataType: "json",
            success: function (data) {
                var productGoodInfoList = data.productGoodInfoList;
                for (var i = 0; i < productGoodInfoList.length; i++) {
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
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' + productId + '</td>'
                                + '<td>' + productName + '</td>'
                                + '<td class="fc_td"><button type="button" data-id="' + productId + '" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#productTable>tbody').append(tr);
                        if (curProductIds == "") {
                            curProductIds = productId;
                        } else {
                            curProductIds += ',' + productId;
                        }
                    }

                }
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

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        var id = $(this).data("id");
        $(this).parent().parent().remove();
        curProductIds = updateProductIdsInfo(id);
    });

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

    var flag = true;

    function savePromoReduce() {
        $('#addDiscount').dpValidate({
            clear: true
        });
        var promoType = $('#promoType').val();
        console.log(promoType)
        if (promoType == 'PPIP_ORDER_TOTAL') {
            $('#proPrice').find('.input-group').each(function () {
                var tdArr = $(this);
                // var num = tdArr.find("input[name=num]").val();//数量
                var price = tdArr.find("input[name=condValue]").val();//金额
                var quantity = tdArr.find("input[name=amount]").val();
                var int_reg = /^[0-9]\d*$/;
                var intReg = /^[0-9]{1}(\.[0-9])?$/;
                if (price) {
                    if (!int_reg.test(price)) {
                        tdArr.find("input[name=condValue]").parent().siblings().find('.price').text('金额格式不正确!')
                        tdArr.find("input[name=condValue]").parent().siblings().find('.price').addClass('dp-error-msg')
                        tdArr.find("input[name=condValue]").css("border-color", "#dd4b39");
                        flag = false;
                    } else {
                        tdArr.find("input[name=condValue]").css("border-color", "");
                        tdArr.find("input[name=condValue]").parent().siblings().find('.price').text('')
                        tdArr.find("input[name=condValue]").parent().siblings().find('.price').removeClass('dp-error-msg')
                        flag = true;
                    }
                } else {
                    tdArr.find("input[name=condValue]").parent().siblings().find('.price').text('金额不能为空!')
                    tdArr.find("input[name=condValue]").parent().siblings().find('.price').addClass('dp-error-msg')
                    tdArr.find("input[name=condValue]").css("border-color", "#dd4b39");
                    flag = false;
                }

                if (quantity) {
                    if (!intReg.test(quantity)) {
                        tdArr.find("input[name=amount]").css("border-color", "#dd4b39");
                        tdArr.find("input[name=amount]").parent().siblings().find('.quantity').text('折扣格式不正确!')
                        tdArr.find("input[name=amount]").parent().siblings().find('.quantity').addClass('dp-error-msg')
                        flag = false;
                    } else {
                        tdArr.find("input[name=amount]").css("border-color", "");
                        tdArr.find("input[name=amount]").parent().siblings().find('.quantity').text('')
                        tdArr.find("input[name=amount]").parent().siblings().find('.quantity').removeClass('dp-error-msg')
                        flag = true;
                    }
                } else {
                    tdArr.find("input[name=amount]").parent().siblings().find('.quantity').text('折扣不能为空!')
                    tdArr.find("input[name=amount]").parent().siblings().find('.quantity').addClass('dp-error-msg')
                    tdArr.find("input[name=amount]").css("border-color", "#dd4b39");
                    flag = false;
                }

            });

        } else if (promoType == 'PPIP_PRODUCT_QUANT') {
            $('#proQuantity').find('.input-group').each(function () {
                var tdArr = $(this);
                var num = tdArr.find("input[name=condValue]").val();//数量
                var price = tdArr.find("input[name=amount]").val();//金额
                var int_reg = /^[0-9]\d*$/;
                var intReg = /^[0-9]{1}(\.[0-9])?$/;
                if (num) {
                    if (!int_reg.test(num)) {
                        tdArr.find("input[name=condValue]").css("border-color", "#dd4b39");
                        tdArr.find("input[name=condValue]").parent().siblings().find('.num').text('数量格式不正确!')
                        tdArr.find("input[name=condValue]").parent().siblings().find('.num').addClass('dp-error-msg')
                        flag = false;
                    } else {
                        tdArr.find("input[name=condValue]").css("border-color", "");
                        tdArr.find("input[name=condValue]").parent().siblings().find('.num').text('')
                        tdArr.find("input[name=condValue]").parent().siblings().find('.num').removeClass('dp-error-msg')
                        flag = true;
                    }
                } else {
                    tdArr.find("input[name=condValue]").parent().siblings().find('.num').text('数量不能为空!')
                    tdArr.find("input[name=condValue]").parent().siblings().find('.num').addClass('dp-error-msg')
                    tdArr.find("input[name=condValue]").css("border-color", "#dd4b39");
                    flag = false;
                }
                if (price) {
                    if (!intReg.test(price)) {
                        tdArr.find("input[name=amount]").parent().siblings().find('.price').text('折扣格式不正确!')
                        tdArr.find("input[name=amount]").parent().siblings().find('.price').addClass('dp-error-msg')
                        tdArr.find("input[name=amount]").css("border-color", "#dd4b39");
                        flag = false;
                    } else {
                        tdArr.find("input[name=amount]").css("border-color", "");
                        tdArr.find("input[name=amount]").parent().siblings().find('.price').text('')
                        tdArr.find("input[name=amount]").parent().siblings().find('.price').removeClass('dp-error-msg')
                        flag = true;
                    }
                } else {
                    tdArr.find("input[name=amount]").parent().siblings().find('.price').text('折扣不能为空!')
                    tdArr.find("input[name=amount]").parent().siblings().find('.price').addClass('dp-error-msg')
                    tdArr.find("input[name=amount]").css("border-color", "#dd4b39");
                    flag = false;
                }

            });
        }
    }

    $("#addDiscount").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (flag) {
                addDiscount();
            }
        }
    });

    function addDiscount() {
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

        var promoCondActions = '';

        if($('#promoType').val()=='PPIP_ORDER_TOTAL'){
            //按金额
            $('#proPrice').find('.input-group').each(function () {
                var promoCondAction = '';
                var tdArr = $(this);
                var price = tdArr.eq(0).find("input[name='condValue']").val();//金额
                var quantity = tdArr.eq(0).find("input[name='amount']").val();//金额

                promoCondAction = price + "," + quantity + "|";
                promoCondActions += promoCondAction;

            });
        }else{
            //按商品件数
            $('#proQuantity').find('.input-group').each(function () {
                var promoCondAction = '';
                var tdArr = $(this);
                var num = tdArr.eq(0).find("input[name='condValue']").val();//数量
                var price = tdArr.eq(0).find("input[name='amount']").val();//金额
                promoCondAction = num + "," + price + "|";
                promoCondActions += promoCondAction;

            });
        }
        console.log(promoCondActions)

        var promo_CondAction = promoCondActions.substr(0, promoCondActions.length - 1);
        $.ajax({
            url: "updateDiscounts",
            type: "POST",
            async: false,
            data: {
                promoName: $('#promoName').val(),
                promoCode: $('#promoCode').val(),
                promoType: $('#promoType').val(),
                fromDate: $('#fromDate').val(),
                thruDate: $('#thruDate').val(),
                promoCondActions: promo_CondAction,
                productIds: productIds,
                productPromoId: $('#productPromoId').val(),
                promoProductType: 'PROMO_PRT_PART_IN',
                productStoreId: $('#productStoreId').val()
            },
            dataType: "json",
            success: function (data) {
                if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                    $.tipLayer(data._ERROR_MESSAGE_);
                }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                    $.tipLayer(data._ERROR_MESSAGE_LIST_);
                } else {
                    if(data.chkFlg=="N"){
                        $('#modal_msg #modal_msg_body').html(data.errorMsg);
                        $('#modal_msg').modal();
                        return;
                    }else{
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("操作成功！");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.href = '<@ofbizUrl>findDiscount</@ofbizUrl>';
                        })
                    }
                }

            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
                $('#modal_msg').on('hide.bs.modal', function () {
                    window.location.reload();
                })
            }
        });
    }

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        $(this).parent().parent().remove();
    });

    function addPrice(condValue, amount) {
        var tr = '<div class="col-sm-12 p-0">'
                + '<div class="input-group p-t-5 col-sm-9 pull-left">'
                + '<div class="input-group-addon"><span>满</span></div>'
                + '<input type="text" class="form-control"  placeholder="金额" name="condValue" value="' + condValue + '" size="3">'
                + '<div class="input-group-addon"><span>打</span></div>'
                + '<input type="text" class="form-control"  placeholder="折"  name="amount" value="' + amount + '" size="5"/>'
                + '<div class="input-group-addon"><span>折</span></div>'
                + '</div>'
                + '<div class="col-sm-3 p-t-5"><button type="button" class="js-button-proPrice btn btn-danger btn-sm">删除</button></div>'
                + '<div class="col-sm-9 p-0">'
                + '<div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>'
                + '<div class="col-sm-6 p-0 quantity" style="padding-left:40px !important;"></div>'
                + '</div>';
        $('#proPrice').append(tr);
    }

    function addQuantityPrice(condValue, amount) {
        var tr = '<div class="col-sm-12 p-0">'
                + '<div class="input-group p-t-5 col-sm-9 pull-left">'
                + '<div class="input-group-addon"><span>满</span></div>'
                + '<input type="text" class="form-control"  placeholder="数量" name="condValue" value="' + condValue + '" size="3">'
                + '<div class="input-group-addon"><span>打</span></div>'
                + '<input type="text" class="form-control" placeholder="折"  name="amount" value="' + amount + '" size="5"/>'
                + '<div class="input-group-addon"><span>折</span></div>'
                + '</div>'
                + '<div class="col-sm-3 p-t-5"><button type="button" class="js-button-proQuantity btn btn-danger btn-sm">删除</button></div>'
                + '<div class="col-sm-9 p-0">'
                + '<div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>'
                + '<div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>'
                + '</div>';
        $('#proQuantity').append(tr);
    }

    //满减删除按钮事件
    $(document).on('click', '.js-button-proPrice', function () {
        $(this).parent().parent().remove();
    });

    $(document).on('click', '.js-button-proQuantity', function () {
        $(this).parent().parent().remove();
    });

    $('#promoType').change(function () {
        var promoType = $('#promoType').val();

        $('#condValue').val('');
        $('#amount').val('');
        $('#cond').val('');
        $('#quantity').val('');
        //满减删除按钮事件
        $('.js-button-proPrice').parent().prev().remove();
        $('.js-button-proPrice').parent().remove();

        if (promoType == 'PPIP_ORDER_TOTAL') {

            $('#proPriceDiv').show();
            $('#proQuantityDiv').hide();
            // $('#proQuantityDiv').clean();

        } else if (promoType == 'PPIP_PRODUCT_QUANT') {

            $('#proPriceDiv').hide();
            $('#proQuantityDiv').show();
            // $('#proPriceDiv').clean();
        }

    });

    $(":radio").click(function () {
        var type = $(this).val();
        if (type == 'PROMO_PRT_ALL') {
            $('#product').hide();
        } else {
            $('#product').show();
        }
    });

    function back() {
        window.location.href = '<@ofbizUrl>findDiscount</@ofbizUrl>';
    }
</script>
