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
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<form class="form-horizontal" role="form" method="post" name="addStraightDown" id="addStraightDown">
    <input type="hidden" name="productStoreId" id="productStoreId" value="${requestAttributes.productStoreId}"/>
    <input type="hidden" id="productPromoId" name="productPromoId" value="${productPromo.productPromoId?if_exists}">
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">直降修改基本信息</h3>
        <#--Del by zhajh at 20160317 bug 2033 Begin--->
        <#---
        <div class=" pull-right">
        <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_ADD", session)>
            <button class="btn btn-success btn-flat" onclick="saveSecKill()">新增</button></#if>
        </div>
        -->
        <#--Del by zhajh at 20160317 bug 2033 End--->
        </div>

        <div class="box-body">
            <input type="hidden" id="linkId"/>
            <input type="hidden" id="selectName"/>
            <div class="row" style="display: none">
                <input type="hidden" id="promoType" value="PROMO_SPE_PRICE"/>
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
                        <input type="hidden" id="promoActionsAmount"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="linkLt" id="fromDateGroup" data-compare-link="endGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="fromDate">
                        <input class="form-control" size="16" type="text" <#if parameters.isPass=='Y'>disabled="disabled"</#if>
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
                        <input class="form-control" size="16" type="text" <#if parameters.isPass=='Y'>disabled="disabled"</#if>
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
                        <th>货品编号</th>
                        <th>货品名称</th>
                        <th>直降后商品金额</th>
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
    <#if security.hasEntityPermission("PRODPROMO_SPE_PRICE", "_UPDATE", session)>
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


        var productObj = [];
        var productIds = "";
    <#list productPromoActions as action>
        var product = {};
        <#assign productId = action.get("productId")>
        <#assign amount = action.get("amount")>
        product.productId = ${productId};
        productIds = ${productId} +"," + productIds;
        product.amount = ${amount};
        productObj.push(product);
    </#list>

        var productIds = productIds.split(",");
        // console.log("productIds.length长度"+productIds.length);
        // console.log('所有的id是productIds'+productIds);
        for (var i = 0; i < productIds.length; i++) {
            var productId = productIds[i];
            // console.log('productId--->'+productId);
            if (productId) {
                getProductGoodsInfoListByIds(productObj);
            }
        }
        $('#addProducts').click(function () {

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
            if (sDate >= eDate) {
                $.tipLayer("开始时间要小于等于结束时间！");
                return;
            }
            $.dataSelectModal({
                url: "/catalog/control/ProductListMultiModalPage2${externalKeyParam}&startDate=" + timestamp2 + "&endDate=" + timestamp3,
                width: "800",
                title: "选择商品",
                selectId: "linkId",
                selectName: "selectName",
                multi: true,
                selectCallBack: function (el) {
                    var productIds = el.data('id');
                    getProductGoodsInfoListById(productIds);
                }
            });
        });

    });

    var curProductIds = "";

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
                                + '<td><input name="price" type="text"/> </td>'
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
     * @param ids
     */
    function getProductGoodsInfoListByIds(productObj) {
        if (currentIndex >= productObj.length) {
            return;
        }
        var product = productObj[currentIndex];
        var ids = product.productId;
        if ($('#productTable>tbody').find('tr#' + ids).length > 0) {
            currentIndex++;
            getProductGoodsInfoListByIds(productObj);
        } else {
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
                        // var salesPrice = productGoodInfo.salesPrice;
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
                                    + '<td>' + "<input name='price' type='text' id='price' value=" + product.amount + " />" + '</td>'
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
                    currentIndex++;
                    getProductGoodsInfoListByIds(productObj);
                },
                error: function (data) {
                    $.tipLayer("操作失败！");
                }
            });
        }

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
        $('#addStraightDown').dpValidate({
            clear: true
        });
    }

    $("#addStraightDown").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (flag) {
                addStraightDown();
            }
        }
    });

    function addStraightDown() {

        var trs = $('#productTable>tbody').find("tr");
        if (trs.length == 0) {
            $('#modal_msg #modal_msg_body').html('请选择参加直降的商品!');
            $('#modal_msg').modal();
            return;
        }
        for (var i = 0; i < trs.length; i++) {
            var curTr = $(trs[i]);
            var int_reg = /^(([1-9][0-9]*)|(([0]\.\d{1,2}|[1-9][0-9]*\.\d{1,2})))$/;
            var price = curTr.find("input[name='price']").val();//直降价格
            var productPrice = curTr.find("input[name='productPrice']").val();//原价
            var productId = curTr.attr("id");//产品编号

            console.log(price + ",," + productPrice)
            if (!int_reg.test(price)) {
                $('#modal_msg #modal_msg_body').html('货品编号为' + productId + '的直降金额请填写不超过999999.99的数字!');
                $('#modal_msg').modal();
                return;
            }
            if (parseInt(price) > parseInt(productPrice)) {
                $('#modal_msg #modal_msg_body').html('货品编号为' + productId + '的直降金额不能大于销售原价!');
                $('#modal_msg').modal();
                return;
            }

        }

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
        $('#productTable>tbody').find("input[name=price]").each(function () {
            var promoCondAction = '';
            var tdArr = $(this);
            var price = tdArr.val();//产品编号
            promoCondAction = price + "|";
            promoCondActions += promoCondAction;

        });
        promo_CondAction = promoCondActions.substr(0, promoCondActions.length - 1);
        // console.log(promo_CondAction);
        // console.log(productIds)
        $.ajax({
            url: "updateStraightDowns",
            type: "POST",
            async: false,
            data: {
                promoCode: $('#promoCode').val(),
                promoName: $('#promoName').val(),
                promoType: 'PROMO_SPE_PRICE',
                productPromoId: $('#productPromoId').val(),
                fromDate: $('#fromDate').val(),
                thruDate: $('#thruDate').val(),
                productStoreId: $('#productStoreId').val(),
                promoCondActions: promo_CondAction,
                productIds: productIds
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
                            window.location.href = '<@ofbizUrl>findStraightDown</@ofbizUrl>';
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


    function back() {
        window.location.href = '<@ofbizUrl>findStraightDown</@ofbizUrl>';
    }
</script>
