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
<form class="form-horizontal" role="form" method="post" name="addPackage" id="addPackage"
      action="<@ofbizUrl>addPackage</@ofbizUrl>">
    <input type="hidden"  id="productPromoId" name="productPromoId" value="${productPromo.productPromoId?if_exists}">
    <input type="hidden" class="form-control dp-vd" id="promoCode" name="promoCode" value="${productPromo.promoCode}">
    <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">包邮基本信息</h3>
        </div>

        <div class="box-body">

            <div class="row" style="display: none">
                <input type="hidden" id="linkId"/>
                <input type="hidden" id="selectName"/>
                <div class="form-group col-sm-6" data-mark="促销编码">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>促销编码</label>
                    <div class="col-sm-9">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required,max" data-number="50" data-mark="促销名称">
                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>促销名称</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="promoName" name="promoName" value="${productPromo.promoName?if_exists}">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">

                <div class="form-group col-sm-6" data-type="required,linkLt" id="fromDateGroup"
                     data-compare-link="endGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="fromDate">
                        <input class="form-control" size="16" type="text"  value="${productStorePromoAppls.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}"  readonly>
                        <input id="fromDate" class="dp-vd" type="hidden" value="${productStorePromoAppls.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}" name="fromDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-3 col-sm-9"></div>
                </div>
                <div id="endGroup" class="form-group col-sm-6" data-type="required,linkGt"
                     data-compare-link="fromDateGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>
                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="thruDate">
                        <input class="form-control" size="16" type="text" value="${productStorePromoAppls.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}" readonly>
                        <input id="thruDate" class="dp-vd" type="hidden" value="${productStorePromoAppls.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}" name="thruDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-3 col-sm-9"></div>
                </div>



            </div>

            <div class="row">
                <div class="form-group col-sm-6" id="giftCond">
                    <label for="condValue" class="col-md-3 control-label"><i class="required-mark">*</i>全场购物满:</label>
                    <div class="col-sm-4" data-type="required">
                        <#--<input type="text" class="form-control dp-vd" id="condValue" name="condValue" value="" />-->
                        <input type="text" class="form-control dp-vd" id="condValue" name="condValue">
                        <#--<input type="text" class="form-control dp-vd" id="condValue" name="condValue" value="${productPromoConds.condValue}">-->
                        <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-md-3"><#--促销条件枚举值-->
                        <select class="form-control  dp-vd" id="paramEnumId" name="paramEnumId">
                            <#list promoPackageTypes as promoPackageType>
                            <option value="${(promoPackageType.enumCode)?if_exists}"
                                    <#if promoPackageType.enumId==productPromoConds[0].inputParamEnumId>selected=selected</#if>>${(promoPackageType.get("description"))?if_exists}
                            </option>
                            </#list>
                        </select>
                        <p class="dp-error-msg"></p>
                    </div>
                    <label class="col-md-2 control-label">包邮</label>
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

<!-- /.box-body -->
</div>
<!-- 内容end -->

<!-- script区域start -->

<script type="text/javascript">
    $('#num').hide();
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

        <#list productPromoConds as conds>
            var product = {};
            <#assign condValue = conds.get("condValue")>
            <#assign inputParamEnumId = conds.get("inputParamEnumId")>
             product.inputParamEnumId  = '${inputParamEnumId}';
             product.condValue = ${condValue};
        </#list>
        console.log('product.condValue --'+product.condValue);

        console.log('product.inputParamEnumId'+product.inputParamEnumId);

        $("#condValue").val(product.condValue);
        $("#paramEnumId").val(product.inputParamEnumId);

    });

    var flag = true;

    function savePromoReduce() {
        $('#addPackage').dpValidate({
            clear: true
        });

        var condObj = $('#giftCond').find('input[name="condValue"]').eq(0);
        var condValue = condObj.val();//满多少的条件
        condObj.removeClass("border-color");
        if (condValue == null || condValue == "") {
            condObj.parent().siblings().eq(0).css("color", "red")
            condObj.css("border-color", "#dd4b39");
            condObj.parent().siblings().find('.dp-error-msg').text('数据不能为空!')
            condObj.parent().siblings().find('.dp-error-msg').addClass('dp-error-msg')
            flag = false;
        } else {
            condObj.parent().siblings().eq(0).css("color", "")
            condObj.css("border-color", "");
            condObj.parent().siblings().find('.dp-error-msg').text('')
            condObj.parent().siblings().find('.dp-error-msg').addClass('dp-error-msg')
            flag = true;
        }

    }

    $("#addPackage").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (flag) {
                addPackage();
            }
        }
    });

    function addPackage() {

        var promo_CondActions =$("#condValue").val() + "," + $("#paramEnumId").val();

        $.ajax({
            url: "updatePackages",
            type: "POST",
            async: false,
            data: {
                productPromoId: $('#productPromoId').val(),
                // productPromoId: '10000',
                productStoreId: $('#productStoreId').val(),
                promoCode: $('#promoCode').val(),
                promoName: $('#promoName').val(),
                promoProductType:'PROMO_PRT_ALL',
                fromDate: $('#fromDate').val(),
                thruDate: $('#thruDate').val(),
                promoCondActions: promo_CondActions
            },
            dataType: "json",
            success: function (data) {

                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作成功！");
                $('#modal_msg').modal();
                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                $('#modal_msg').on('hide.bs.modal', function () {
                    window.location.href = '<@ofbizUrl>findPackage</@ofbizUrl>';
                })
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

    $(":radio").click(function () {
        var type = $(this).val();
        if (type == 'PROMO_PRT_ALL') {
            $('#product').hide();
        } else {
            $('#product').show();
        }
    });

    function back() {
        window.location.href = '<@ofbizUrl>findPackage</@ofbizUrl>';
    }
</script>
