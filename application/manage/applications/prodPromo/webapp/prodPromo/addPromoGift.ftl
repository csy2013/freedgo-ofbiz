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
<form class="form-horizontal" role="form" method="post" name="addPromoReduce" id="addPromoReduce"
      action="<@ofbizUrl>addPromoReduce</@ofbizUrl>">
    <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
    <input type="hidden" id="linkId"/>
    <input type="hidden" id="selectName"/>
    <input type="hidden" id="linkId1"/>
    <input type="hidden" id="selectName1"/>
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">满赠基本信息</h3>
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

            <div class="row" style="display: none">

                <div class="form-group col-sm-6"  data-mark="促销编码">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>促销编码</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="promoCode" name="promoCode">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>

            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required,max" data-number="30" data-mark="促销名称">
                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>促销名称</label>
                    <div class="col-sm-9">
                        <input type="text"  class="form-control dp-vd" id="promoName" name="promoName">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required,linkLt" id="fromDateGroup" data-compare-link="endGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="fromDate">
                        <input class="form-control" size="16" type="text" readonly>
                        <input id="fromDate" class="form-control dp-vd" type="hidden" name="fromDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-3 col-sm-9"></div>
                </div>

            </div>
            <div class="row">
                <div id="endGroup" class="form-group col-sm-6" data-type="required,linkGt" data-compare-link="fromDateGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="thruDate">
                        <input class="form-control" size="16" type="text" readonly>
                        <input id="thruDate" class="dp-vd" type="hidden" name="thruDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-3 col-sm-9"></div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6" id="giftCond">
                    <label for="condValue" class="col-md-3 control-label"><i class="required-mark">*</i>满</label>
                    <div class="col-sm-4" >
                        <input type="text" class="form-control dp-vd" id="condValue" name="condValue">
                        <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-md-3"><#--促销条件枚举值-->
                        <select class="form-control" id="paramEnumId" name="paramEnumId">
                            <option value="PPIP_ORDER_TOTAL" selected>元</option>
                            <option value="PPIP_PRODUCT_QUANT">件</option>
                        </select>
                    </div>
                    <label class="col-md-2 control-label">赠送商品</label>
                </div>

            </div>
        </div>
    </div>


    <div class="box box-info" id="product">
        <div class="box-header">
            <h3 class="box-title">选择促销商品</h3>
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
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="box box-info" id="product">
        <div class="box-header">
            <h3 class="box-title">选择赠送商品</h3>
        </div>
        <div class="box-body table-responsive no-padding">
            <div class="dp-tables_btn p-l-15 p-b-15">
                <button id="addProducts1" type="button" class="btn btn-primary">
                    <i class="fa fa-plus">添加商品</i>
                </button>
            </div>
            <div class="table-responsive no-padding">
                <table class="table table-hover js-checkparent js-sort-list addProducts1" id="productTable1">
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
    </div>


    <div class="box-footer text-center">
    <#if security.hasEntityPermission("PRODPROMO_REDUCE", "_ADD", session)>
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
<script>
    var PartyFM_data_tbl;
    var PartyFM_ajaxUrl = "/membermgr/control/personListForJson?VIEW_SIZE=5";
    $(function () {
        $('#promtype_Add').hide();
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
            /*$.dataSelectModal({
                url: "/catalog/control/ProductListMultiModalPage${externalKeyParam}",
                width: "800",
                title: "选择商品",
                selectId: "linkId",
                selectName: "selectName",
                multi: true,
                selectCallBack: function (el) {
                    var product_Ids = $(el).data("id").toString().split(",");
                    var proIds_arr = $('#productTable>tbody').find('tr');
                    //删除未选中项
                    $.each(proIds_arr, function (i, v) {
                        var index = product_Ids.indexOf($(v).attr('id'));
                        if (index < 0) {
                            $(v).remove();
                        }
                    });
                    createPromoTable(product_Ids);
                    currentIndex = 0;
                }
            });*/
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
        })

        $('#addProducts1').click(function () {
            $.dataSelectModal({
                url: "/catalog/control/ProductListMultiModalPage${externalKeyParam}",
                width: "800",
                title: "选择商品",
                selectId: "linkId1",
                selectName: "selectName1",
                multi: true,
                selectCallBack: function (el) {
                    var productIds = el.data('id');
                    getProductGoodsInfoListByIds1(productIds);
                }
            });
        })
    });


    var curProductIds="";
    var curProductIds1="";
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
                        }else{
                            curProductIds+=','+productId;
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
    function createPromoTable1(ids_arr) {
        if (currentIndex >= ids_arr.length) {
            return;
        }
        var productId = ids_arr[currentIndex];
        if ($('#productTable1>tbody').find('tr#' + productId).length > 0) {
            currentIndex++;
            createPromoTable1(ids_arr);
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
                    $('#productTable1>tbody').append(tr);
                    currentIndex++;
                    createPromoTable1(ids_arr);
                },
                error: function (data) {
                    currentIndex++;
                    createPromoTable1(ids_arr);
                    $.tipLayer("操作失败！");
                }
            });
        }
    }



    /**
     * 根据商品编码取得商品信息列表
     * @param ids
     */
    function getProductGoodsInfoListByIds1(ids){
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
                    var salesPrice = productGoodInfo.salesPrice;
                    var imgUrl=productGoodInfo.imgUrl;
                    var productGoodFeature = productGoodInfo.productGoodFeature;
                    var productId = productInfo.productId;
                    var productName = productInfo.productName;
                    if(chkProductIdIsSelected1(productInfo.productId)){
                        var tr = '<tr id="' + productId + '">'
                                + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' + productId + '</td>'
                                + '<td>' + productName + '</td>'
                                + '<td class="fc_td"><button type="button" data-id="'+productId+'" class="js-button-assocgood1 btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#productTable1>tbody').append(tr);
                        if(curProductIds1==""){
                            curProductIds1=productId;
                        }else{
                            curProductIds1+=','+productId;
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
    function chkProductIdIsSelected1(productId){
        var chkFlg=true;
        var ids=curProductIds1.split(",");
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
    $(document).on('click', '.js-button-assocgood1', function () {
        var id=$(this).data("id");
        $(this).parent().parent().remove();
        curProductIds1=updateProductIdsInfo1(id);
        $("#linkId1").val(curProductIds1);
    })

    /**
     * 更新商品编码集合
     * @param productId
     * @returns {string}
     */
    function updateProductIdsInfo1(productId){
        var ids=curProductIds1.split(",");
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
    function createPromoTable1(ids_arr) {
        if (currentIndex >= ids_arr.length) {
            return;
        }
        var productId = ids_arr[currentIndex];
        if ($('#productTable1>tbody').find('tr#' + productId).length > 0) {
            currentIndex++;
            createPromoTable1(ids_arr);
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
                    $('#productTable1>tbody').append(tr);
                    currentIndex++;
                    createPromoTable1(ids_arr);
                },
                error: function (data) {
                    currentIndex++;
                    createPromoTable1(ids_arr);
                    $.tipLayer("操作失败！");
                }
            });
        }
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

    function savePromoReduce() {
        $('#addPromoReduce').dpValidate({
            clear: true
        });

//        var condObj = $('#giftCond').find('input[name="condValue"]').eq(0);
//        var condValue = condObj.val();//满多少的条件
//        condObj.removeClass("border-color");
//        if (condValue == null || condValue == "") {
//            condObj.parent().siblings().eq(0).css("color", "red")
//            condObj.css("border-color", "#dd4b39");
//            condObj.parent().siblings().find('.dp-error-msg').text('数据不能为空!')
//            condObj.parent().siblings().find('.dp-error-msg').addClass('dp-error-msg')
//            flag = false;
//        } else {
//            condObj.parent().siblings().eq(0).css("color", "")
//            condObj.css("border-color", "");
//            condObj.parent().siblings().find('.dp-error-msg').text('')
//            condObj.parent().siblings().find('.dp-error-msg').addClass('dp-error-msg')
//            flag = true;
//        }
    }

    $("#addPromoReduce").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (flag) {
                addPromoReduce();
            }
        }
    });

    function addPromoReduce() {

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
        /*选择赠送的产品*/
        var productIds1 = "";
        $('#productTable1>tbody').find("tr").each(function () {
            var tdArr = $(this);
            for (var i = 0; i < tdArr.length; i++) {
                var productId =  tdArr.eq(i).attr("id");//产品编号
                if (productId) {
                    productIds1 += productId + ","
                }
            }
        });
        if (productIds1 == "") {
            $('#modal_msg #modal_msg_body').html("请添加需要赠送的商品！！");
            $('#modal_msg').modal();
            return;
        }

        var reg1 =/^(\d{1,8})(\.?)(\d{1,2})$/;
        var reg2 = /^[1-9]+\d*$/;
        var paramEnum = $("#paramEnumId").val();
        var condValue = $("#condValue").val();
        var promo_CondAction = '';
        if ("PPIP_ORDER_TOTAL" == paramEnum){//元
            //reg1.test($("#condValue").val())
            //alert('金额数'+condValue)
            if (!reg1.test(condValue)){
                alert('---->'+reg1.test(condValue))
                $('#modal_msg #modal_msg_body').html("请输入正确规格的金额！！");
                $('#modal_msg').modal();
                return;
            }else {
                promo_CondAction = paramEnum + ','+condValue;
                //alert('promo_CondAction----->'+promo_CondAction+'<-----promo_CondAction')
            }
        }else {
            if (!reg2.test($("#condValue").val())){
                $('#modal_msg #modal_msg_body').html("请输入正确规格的件数！！");
                $('#modal_msg').modal();
                return;
            }else {
                promo_CondAction = paramEnum + ','+condValue;
                //alert('promo_CondAction----->'+promo_CondAction+'<-----promo_CondAction')
            }

        }

        //满赠的条件 PPIP_ORDER_TOTAL,11
        //var promo_CondAction = $("#paramEnumId").val() + "," + condValue;
        if (promo_CondAction){
            $.ajax({
                url: "addPromoGiftService",//addPromoGiftService
                type: "POST",
                async: false,
                data: {
                    productStoreId:$('#productStoreId').val(),
                    promoCode: $('#promoCode').val(),
                    promoName: $('#promoName').val(),
                    fromDate: $('#fromDate').val(),
                    thruDate: $('#thruDate').val(),
                    promoCondActions: promo_CondAction,
                    productIds: productIds,
                    productIds1: productIds1,
                    levelIds: "",
                    promoText: ""// CKEDITOR.instances['promoText'].getData(),去掉促销描述
                },
                dataType: "json",
                success: function (data) {
                    if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                        $.tipLayer(data._ERROR_MESSAGE_);
                    }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                        $.tipLayer(data._ERROR_MESSAGE_LIST_);
                    } else {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("操作成功！");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.href = '<@ofbizUrl>findPromoGift</@ofbizUrl>';
                        })
                    }

                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("操作失败！");
                    $('#modal_msg').modal();
                    $('#modal_msg').on('hide.bs.modal', function () {
//                    window.location.reload();
                    })
                }
            });
        }

    }



    /* function addprice() {
         var tr = '<div class="col-sm-12 p-0">'
                 + '<div class="input-group p-t-5 col-sm-9 pull-left">'
                 + '<div class="input-group-addon"><span>满</span></div>'
                 + '<input type="text" class="form-control" placeholder="数量" name="num" size="3">'
                 + '<div class="input-group-addon"><span>减</span></div>'
                 + '<input type="text" class="form-control" placeholder="金额"  name="price" size="5"/>'
                 + '</div>'
                 + '<div class="col-sm-3 p-t-5"><button type="button" class="js-button-proPrice btn btn-danger btn-sm">删除该满减</button></div>'
                 + '<div class="col-sm-9 p-0">'
                 + '<div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>'
                 + '<div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>'
                 + '</div>';
         $('#proPrice').append(tr);
     }*/

    //满减删除按钮事件
    $(document).on('click', '.js-button-proPrice', function () {
        $(this).parent().prev().remove();
        $(this).parent().remove();
    })

    /*  $('#promoType').change(function () {
          var promoType = $('#promoType').val();

          $('#proPrice').find('.input-group').each(function () {
              var tdArr = $(this);
              tdArr.find("input[name=num]").css("border-color", "");
              tdArr.find("input[name=num]").parent().siblings().find('.num').text('')
              tdArr.find("input[name=num]").parent().siblings().find('.num').removeClass('dp-error-msg')
              tdArr.find("input[name=price]").parent().siblings().find('.price').text('')
              tdArr.find("input[name=price]").parent().siblings().find('.price').removeClass('dp-error-msg')
              tdArr.find("input[name=price]").css("border-color", "");

              tdArr.find("input[name=num]").val("");//数量
              tdArr.find("input[name=price]").val("");//金额
          });
          //满减删除按钮事件
          $('.js-button-proPrice').parent().prev().remove();
          $('.js-button-proPrice').parent().remove();
          if (promoType == 'PROMO_REDUCE') {
              $('#promtype_Add').show();
              $('#pre').text("满");
          } else if (promoType == 'PROMO_PRE_REDUCE') {
              $('#pre').text("每满");
              $("#pre").parent().parent().parent().nextAll().hide();
              $('#promtype_Add').hide();
          }
      });
  */
    $(":radio").click(function () {
        var type = $(this).val();
        if (type == 'PROMO_PRT_ALL') {
            $('#product').hide();
        } else {
            $('#product').show();
        }
    });

    function back() {
        window.location.href = '<@ofbizUrl>findPromoGift</@ofbizUrl>';
    }
</script>
