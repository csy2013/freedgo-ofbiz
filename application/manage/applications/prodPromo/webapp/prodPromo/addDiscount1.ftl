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

    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">折扣新增基本信息</h3>
        </div>

        <div class="box-body">

            <div class="row" style="display: none">
                <input type="hidden" id="linkId"/>
                <input type="hidden" id="selectName"/>
                <div class="form-group col-sm-6" data-mark="促销编码">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>促销编码</label>
                    <div class="col-sm-9">
                        <input type="text" disabled class="form-control dp-vd" id="promoCode" name="promoCode" readonly>

                        <p class="dp-error-msg"></p>
                    </div>
                </div>

            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="促销名称">
                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>促销名称</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="promoName" name="promoName">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="linkLt" id="fromDateGroup" data-compare-link="endGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="fromDate">
                        <input class="form-control" size="16" type="text" readonly>
                        <input id="fromDate" class="dp-vd" type="hidden" name="fromDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

                <div id="endGroup" class="form-group col-sm-6" data-type="linkGt" data-compare-link="fromDateGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="thruDate">
                        <input class="form-control" size="16" type="text" readonly>
                        <input id="thruDate" class="dp-vd" type="hidden" name="thruDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

            </div>
            <#--<div class="row">-->
                <#--<div class="form-group col-sm-6">-->
                    <#--<label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>满减类型</label>-->
                    <#--<div class="col-md-9">-->
                        <#--<select class="form-control  dp-vd" id="promoType" name="promoType">-->
                        <#--<#list promoTypes as promoType>-->
                            <#--<option value="${(promoType.enumId)?if_exists}"-->
                                    <#--<#if promoType.enumId='PROMO_PRE_REDUCE'>selected=selected</#if>>${(promoType.get("description",locale))?if_exists}</option>-->
                        <#--</#list>-->
                        <#--</select>-->
                        <#--<p class="dp-error-msg"></p>-->
                    <#--</div>-->
                <#--</div>-->
            <#--&lt;#&ndash; <div class="form-group col-sm-6" data-type="minCheck" data-number="1" data-mark="参与方式">-->
                 <#--<label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>参与方式 </label>-->
                 <#--<div class="col-md-9 p-l-15 p-r-15">-->
                  <#--<#list promoProTypes as promoProType>-->
                  <#--<label class="radio-inline">-->
                         <#--<input type="radio" class="dp-vd" name="promoProductType" value="${(promoProType.enumId)?if_exists}">${(promoProType.get("description",locale))?if_exists}-->
                  <#--</label>-->
                  <#--</#list>-->
                  <#--<div class="dp-error-msg"></div>-->
                 <#--</div>-->
             <#--</div>&ndash;&gt;-->
            <#--</div>-->

            <#--<div class="row">-->
                <#--<div class="form-group col-sm-6">-->
                    <#--<label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>活动规则</label>-->
                    <#--<div class="col-md-9">-->
                        <#--<div id="proPrice">-->
                            <#--<div class="p-0 col-sm-9">-->
                                <#--<div class="input-group">-->
                                    <#--<div class="input-group-addon"><span id="pre">每满</span></div>-->
                                    <#--<input type="text" class="form-control" name="num" placeholder="金额" size="3">-->
                                    <#--<div class="input-group-addon"><span>减</span></div>-->
                                    <#--<input type="text" class="form-control" name="price" placeholder="金额" size="5"/>-->
                                <#--</div>-->
                                <#--<div class="col-sm-12 p-0">-->
                                    <#--<div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>-->
                                    <#--<div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>-->
                                <#--</div>-->
                            <#--</div>-->
                        <#--</div>-->
                        <#--<div class="input-group p-t-10" id="promtype_Add">-->
                            <#--<input type="button" class="btn btn-success" onclick="addprice()" value="+添加多级满减"/>-->
                        <#--</div>-->
                    <#--</div>-->
                <#--</div>-->
                <#--<div class="col-sm-6">-->
                    <#--&nbsp;-->
                <#--</div>-->
            <#--</div>-->

            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>折扣类型</label>
                    <div class="col-md-9">
                        <select class="form-control  dp-vd" id="promoType" name="promoType">
                        <#list promoDiscountTypes as promoType>
                            <option value="${(promoType.enumId)?if_exists}"
                            <#-- 折扣类型默认选择金额 -->
                                <#if promoType.enumId='PROMO_AMOUNT_REDUCE'>selected=selected</#if>>${(promoType.get("description",locale))?if_exists}</option>
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
                                    <input type="text" class="form-control" name="price" placeholder="金额" size="3">
                                    <div class="input-group-addon"><span>减</span></div>
                                    <input type="text" class="form-control" name="quantity" placeholder="金额" size="5"/>
                                </div>
                                <div class="col-sm-12 p-0">
                                    <div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>
                                    <div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>
                                </div>
                            </div>
                        </div>
                        <div class="input-group p-t-10" id="promtype_Add">
                            <input type="button" class="btn btn-success" onclick="addPrice()" value="+添加多级"/>
                        </div>
                    </div>

                    <div class="col-md-9"  id="proQuantityDiv">
                        <div id="proQuantity">
                            <div class="p-0 col-sm-9">
                                <div class="input-group">
                                    <div class="input-group-addon"><span id="pre">满</span></div>
                                    <input type="text" class="form-control" name="num" placeholder="数量" size="3">
                                    <div class="input-group-addon"><span>减</span></div>
                                    <input type="text" class="form-control" name="price" placeholder="金额" size="5"/>
                                </div>
                                <div class="col-sm-12 p-0">
                                    <div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>
                                    <div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>
                                </div>
                            </div>
                        </div>
                        <div class="input-group p-t-10" id="promtype_Add">
                            <input type="button" class="btn btn-success" onclick="addQuantityPrice()" value="+添加多级"/>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6">
                    &nbsp;
                </div>
            </div>


        <#--<div class="row">
            <div class="form-group  col-sm-12">
                <label for="seo" class="control-label col-sm-1">促销描述</label>

                <div class="col-sm-8">
                &lt;#&ndash;<textarea class="form-control" name="activityDesc" id="activityDesc" rows="6"></textarea>&ndash;&gt;
                &lt;#&ndash; <@htmlTemplate.renderTextareaField name="activityDesc" className="dojo-ResizableTextArea" alert="false" value=""
                   cols="60" rows="7" id="textData" \ visualEditorEnable="true" language="zh_CN" buttons="maxi" />&ndash;&gt;
                <@htmlTemplate.renderTextareaField name="promoText" className="dojo-ResizableTextArea" alert="false"
                value="" cols="60" rows="15" id="promoText" readonly="" visualEditorEnable="true" language="zh_CN" buttons="maxi" />
                    <p class="dp-error-msg"></p>
                </div>
            </div>
        </div>-->
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
                        <th>货品规格</th><!--货品规格 是按照商品特征名 加 特征值来进行定义的-->
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

<#--   <div class="box box-info js-party-level">
       <div class="box-header">
           <h3 class="box-title">参加的会员等级</h3>
       </div>
       <div class="box-body table-responsive no-padding">
           <table class="table table-hover js-checkparent js-sort-list" id="partyLevelTable">
               <tr>
                   <th>序号</th>
                   <th>会员等级</th>
                   <th>参加标志<input class="js-allcheck" type="checkbox" id="checkAll"/></th>
               </tr>
           <#list partyLevels as partyLevel>
               <tr>
                   <td>${partyLevel_index+1}</td>
                   <td>${partyLevel.levelName}</td>
                   <td><input class="js-checkchild" type="checkbox" name="partyLevels" value="${partyLevel.levelId}"></td>
               </tr>
           </#list>
           </table>
       </div>
   </div>-->

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
        $('#manNum').hide();
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
    $('#proQuantityDiv').hide();
    $('#proPriceDiv').show();
    $('#manNum').hide();
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
                url: "/catalog/control/ProductPromoListMultiModalPage${externalKeyParam}",
                width: "800",
                title: "选择商品",
                selectId: "linkId",
                selectName: "selectName",
                multi: true,
                selectCallBack: function (el) {
                    // console.log(el)
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
            });
        })

    });


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
                    // var price = productPrice(productId);
                    var tr = '<tr id="' + productId + '">'
                            + '<td>' + productId + '</td>'
                            + '<td>' + productId + '</td>'
                            + '<td>' + productId + '</td>'
                            + '<td>' + productName + '</td>'
                            // + price
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
    var promoType = $('#promoType').val();
    function savePromoReduce() {
        $('#addPromoReduce').dpValidate({
            clear: true
        });
        if(promoType == 'PROMO_AMOUNT_REDUCE'){
            $('#proPrice').find('.input-group').each(function () {
                var tdArr = $(this);
                var num = tdArr.find("input[name=num]").val();//数量
                var price = tdArr.find("input[name=price]").val();//金额
                var int_reg = /^[0-9]\d*$/;
                if (num) {
                    if (!int_reg.test(num)) {
                        tdArr.find("input[name=num]").css("border-color", "#dd4b39");
                        tdArr.find("input[name=num]").parent().siblings().find('.num').text('金额格式不正确!')
                        tdArr.find("input[name=num]").parent().siblings().find('.num').addClass('dp-error-msg')
                        flag = false;
                    } else {
                        tdArr.find("input[name=num]").css("border-color", "");
                        tdArr.find("input[name=num]").parent().siblings().find('.num').text('')
                        tdArr.find("input[name=num]").parent().siblings().find('.num').removeClass('dp-error-msg')
                        flag = true;
                    }
                } else {
                    tdArr.find("input[name=num]").parent().siblings().find('.num').text('金额不能为空!')
                    tdArr.find("input[name=num]").parent().siblings().find('.num').addClass('dp-error-msg')
                    tdArr.find("input[name=num]").css("border-color", "#dd4b39");
                    flag = false;
                }
                if (price) {
                    if (!int_reg.test(price)) {
                        tdArr.find("input[name=price]").parent().siblings().find('.price').text('金额格式不正确!')
                        tdArr.find("input[name=price]").parent().siblings().find('.price').addClass('dp-error-msg')
                        tdArr.find("input[name=price]").css("border-color", "#dd4b39");
                        flag = false;
                    } else {
                        tdArr.find("input[name=price]").css("border-color", "");
                        tdArr.find("input[name=price]").parent().siblings().find('.price').text('')
                        tdArr.find("input[name=price]").parent().siblings().find('.price').removeClass('dp-error-msg')
                        flag = true;
                    }
                } else {
                    tdArr.find("input[name=price]").parent().siblings().find('.price').text('金额不能为空!')
                    tdArr.find("input[name=price]").parent().siblings().find('.price').addClass('dp-error-msg')
                    tdArr.find("input[name=price]").css("border-color", "#dd4b39");
                    flag = false;
                }
            });

        }else if(promoType == 'PROMO_NUM_REDUCE'){
            $('#proQuantity').find('.input-group').each(function () {
                var tdArr = $(this);
                var num = tdArr.find("input[name=num]").val();//数量
                var price = tdArr.find("input[name=price]").val();//金额
                var int_reg = /^[0-9]\d*$/;
                if (num) {
                    if (!int_reg.test(num)) {
                        tdArr.find("input[name=num]").css("border-color", "#dd4b39");
                        tdArr.find("input[name=num]").parent().siblings().find('.num').text('金额格式不正确!')
                        tdArr.find("input[name=num]").parent().siblings().find('.num').addClass('dp-error-msg')
                        flag = false;
                    } else {
                        tdArr.find("input[name=num]").css("border-color", "");
                        tdArr.find("input[name=num]").parent().siblings().find('.num').text('')
                        tdArr.find("input[name=num]").parent().siblings().find('.num').removeClass('dp-error-msg')
                        flag = true;
                    }
                } else {
                    tdArr.find("input[name=num]").parent().siblings().find('.num').text('金额不能为空!')
                    tdArr.find("input[name=num]").parent().siblings().find('.num').addClass('dp-error-msg')
                    tdArr.find("input[name=num]").css("border-color", "#dd4b39");
                    flag = false;
                }
                if (price) {
                    if (!int_reg.test(price)) {
                        tdArr.find("input[name=price]").parent().siblings().find('.price').text('金额格式不正确!')
                        tdArr.find("input[name=price]").parent().siblings().find('.price').addClass('dp-error-msg')
                        tdArr.find("input[name=price]").css("border-color", "#dd4b39");
                        flag = false;
                    } else {
                        tdArr.find("input[name=price]").css("border-color", "");
                        tdArr.find("input[name=price]").parent().siblings().find('.price').text('')
                        tdArr.find("input[name=price]").parent().siblings().find('.price').removeClass('dp-error-msg')
                        flag = true;
                    }
                } else {
                    tdArr.find("input[name=price]").parent().siblings().find('.price').text('金额不能为空!')
                    tdArr.find("input[name=price]").parent().siblings().find('.price').addClass('dp-error-msg')
                    tdArr.find("input[name=price]").css("border-color", "#dd4b39");
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
        /* var levels = "";
         $("input[name='partyLevels']").each(function () {
              if (this.checked) {
                  console.log($(this).val());
                  levels += $(this).val() + ","
              }
          });
          if (levels == '') {
              $('#modal_msg #modal_msg_body').html("请设置参加的会员等级！");
              $('#modal_msg').modal();
              return;
          }
          if (levels.substring(levels.length - 1) == ',') {
              levels = levels.substring(0, levels.length - 1);
          }*/
        var productIds = "";
        $('#productTable>tbody').find("tr").each(function () {
            var tdArr = $(this);
            for (var i = 0; i < tdArr.length; i++) {
                var productId = tdArr.eq(i).find('td').eq(0).text();//产品编号
                if (productId) {
                    productIds += productId + ","
                }
            }
        });
        //去掉参与方式
        /*    var promoProductType= $("input[name='promoProductType']:checked").val();
             //不参与商品
            if(promoProductType=='PROMO_PRT_PART_EX'&&productIds==''){
             $('#modal_msg #modal_msg_body').html("请设置不参与的商品！");
                 $('#modal_msg').modal();
                 return;
            }else if(promoProductType=='PROMO_PRT_PART_IN'&&productIds==''){
            $('#modal_msg #modal_msg_body').html("请设置参与的商品！");
                 $('#modal_msg').modal();
                 return;
            }*/
        var promoCondActions = '';
        var promo_CondAction = '';
        $('#proPrice').find('.input-group').each(function () {
            var promoCondAction = '';
            var tdArr = $(this);
            var num = tdArr.eq(0).find("input[name=num]").val();//数量
            var price = tdArr.eq(0).find("input[name=price]").val();//金额
            var quantity = tdArr.eq(0).find("input[name=quantity]").val();//金额
            // promoCondAction = num + "," + price + "|"


            if(promoType == 'PROMO_AMOUNT_REDUCE'){
                promoCondAction = price + "," + quantity + "|";
            }else if(promoType == 'PROMO_NUM_REDUCE'){
                promoCondAction = num + "," + price + "|";
            }
            promoCondActions += promoCondAction;
        });
        promo_CondAction = promoCondActions.substr(0, promoCondActions.length - 1)
        $.ajax({
            url: "addDiscounts",
            type: "POST",
            async: false,
            data: {
                // promoCode: $('#promoCode').val(),//促销编码，后台自动生成
                promoName: $('#promoName').val(),
                promoType: $('#promoType').val(),
                fromDate: $('#fromDate').val(),
                thruDate: $('#thruDate').val(),
                // promoProductType: $("input[name='promoProductType']:checked").val(),
                promoCondActions: promo_CondAction,
                productIds: productIds,
                // levelIds: "",
                // promoText: ""// CKEDITOR.instances['promoText'].getData(),去掉促销描述
            },
            dataType: "json",
            success: function (data) {

                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作成功！");
                $('#modal_msg').modal();
                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                $('#modal_msg').on('hide.bs.modal', function () {
                    window.location.href = '<@ofbizUrl>findDiscount</@ofbizUrl>';
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

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        $(this).parent().parent().remove();
    });


    function addPrice() {
        var tr = '<div class="col-sm-12 p-0">'
                + '<div class="input-group p-t-5 col-sm-9 pull-left">'
                + '<div class="input-group-addon"><span>满</span></div>'
                + '<input type="text" class="form-control" placeholder="金额" name="num" size="3">'
                + '<div class="input-group-addon"><span>减</span></div>'
                + '<input type="text" class="form-control" placeholder="金额"  name="price" size="5"/>'
                + '</div>'
                + '<div class="col-sm-3 p-t-5"><button type="button" class="js-button-proPrice btn btn-danger btn-sm">删除该满减</button></div>'
                + '<div class="col-sm-9 p-0">'
                + '<div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>'
                + '<div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>'
                + '</div>';
        $('#proPrice').append(tr);
    }

    function addQuantityPrice() {
        var tr = '<div class="col-sm-12 p-0">'
                + '<div class="input-group p-t-5 col-sm-9 pull-left">'
                + '<div class="input-group-addon"><span>满</span></div>'
                + '<input type="text" class="form-control" placeholder="数量" name="num" size="3">'
                + '<div class="input-group-addon"><span>减</span></div>'
                + '<input type="text" class="form-control" placeholder="金额"  name="price" size="5"/>'
                + '</div>'
                + '<div class="col-sm-3 p-t-5"><button type="button" class="js-button-proQuantity btn btn-danger btn-sm">删除该满减</button></div>'
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

     /*   $('#proPrice').find('.input-group').each(function () {
            var tdArr = $(this);
            tdArr.find("input[name=num]").css("border-color", "");
            tdArr.find("input[name=num]").parent().siblings().find('.num').text('')
            tdArr.find("input[name=num]").parent().siblings().find('.num').removeClass('dp-error-msg')
            tdArr.find("input[name=price]").parent().siblings().find('.price').text('')
            tdArr.find("input[name=price]").parent().siblings().find('.price').removeClass('dp-error-msg')
            tdArr.find("input[name=price]").css("border-color", "");

            tdArr.find("input[name=num]").val("");//数量
            tdArr.find("input[name=price]").val("");//金额
            tdArr.find("input[name=goodsAccount]").val("");//折数
        });*/
        //满减删除按钮事件
        $('.js-button-proPrice').parent().prev().remove();
        $('.js-button-proPrice').parent().remove();

        if (promoType == 'PROMO_AMOUNT_REDUCE') {

          $('#proPriceDiv').show();
          $('#proQuantityDiv').hide();

        } else if (promoType == 'PROMO_NUM_REDUCE') {

            $('#proPriceDiv').hide();
            $('#proQuantityDiv').show();

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
