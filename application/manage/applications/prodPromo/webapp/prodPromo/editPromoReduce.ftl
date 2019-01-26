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
<input type="hidden"  id="productIds" name="productIds" value="${productIds?if_exists}">
<form class="form-horizontal" role="form" method="post" name="addPromoReduce" id="addPromoReduce"
      action="<@ofbizUrl>addPromoReduce</@ofbizUrl>">
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">满减基本信息</h3>
        </div>

        <div class="box-body">
            <input type="hidden" id="linkId"/>
            <input type="hidden" id="selectName"/>
            <input type="hidden"  id="productPromoId" name="productPromoId" value="${productPromo.productPromoId?if_exists}">
            <input type="hidden"  id="promoCode" name="promoCode" value="${productPromo.promoCode?if_exists}">
            <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required,max" data-number="50" data-mark="促销名称">
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
                        <input class="form-control" size="16" type="text"
                               value="${productStorePromoAppls.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}"  <#if parameters.isPass=='Y'>disabled="disabled"</#if>
                               readonly>
                        <input id="fromDate" class="dp-vd" type="hidden" name="fromDate"
                               value="${productStorePromoAppls.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

            </div>
            <div class="row">
                <div id="endGroup" class="form-group col-sm-6" data-type="linkGt" data-compare-link="fromDateGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="thruDate">
                        <input class="form-control" size="16" type="text"
                               value="${productStorePromoAppls.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}" <#if parameters.isPass=='Y'>disabled="disabled"</#if>
                               readonly>
                        <input id="thruDate" class="dp-vd" type="hidden" name="thruDate" disabled="disabled"
                               value="${productStorePromoAppls.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}">
                        <span class="input-group-addon"><span disabled="disabled" class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

            </div>
            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>满减类型</label>
                    <div class="col-md-9">
                        <select class="form-control  dp-vd" id="promoType" name="promoType">
                        <#list promoTypes as promoType>
                            <option value="${(promoType.enumId)?if_exists}" <#if productPromo.promoType==promoType.enumId>selected</#if>>${(promoType.get("description",locale))?if_exists}</option>
                        </#list>
                        </select>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            <#-- <div class="form-group col-sm-6" data-type="minCheck" data-number="1" data-mark="参与方式">
                 <label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>参与方式 </label>
                 <div class="col-md-9 p-l-15 p-r-15">
                  <#list promoProTypes as promoProType>
                  <label class="radio-inline">
                         <input type="radio" class="dp-vd" name="promoProductType" value="${(promoProType.enumId)?if_exists}">${(promoProType.get("description",locale))?if_exists}
                  </label>
                  </#list>
                  <div class="dp-error-msg"></div>
                 </div>
             </div>-->
            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="促销名称">
                    <label for="promoType" class="col-sm-3 control-label"><i class="required-mark">*</i>活动规则</label>
                    <div class="col-md-9">
                        <div id="proPrice">
                            <div class="p-0 col-sm-9">
                                <div class="input-group">
                                    <div class="input-group-addon"><span id="pre">每满</span></div>
                                    <input type="text" class="form-control" id="firstNum" name="num" placeholder="金额" size="3">
                                    <div class="input-group-addon"><span>减</span></div>
                                    <input type="text" class="form-control" id="firstPrice" name="price" placeholder="金额" size="5"/>
                                </div>
                                <div class="col-sm-12 p-0">
                                    <div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>
                                    <div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>
                                </div>
                            </div>
                        </div>
                        <div class="p-0 col-sm-9" id="promtype_Add"
                             <#if productPromo.promoType=='PROMO_PRE_REDUCE'>style="display:none;"</#if>>
                            <input type="button" class="btn btn-success" onclick="addprice()" value="+添加多级满减"/>
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



<script type="text/javascript">
    var curProductIds="";
    var curProductNames="";
    $(function () {
        var DetailsContent = $("#DetailsContent").val();
        if (DetailsContent) {
            CKEDITOR.instances.promoText.setData(DetailsContent);
            CKEDITOR.instances.conf
        }
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
        init();
    });
    function init() {
        var productIds = $("#productIds").val();
        if (productIds != null && productIds != "") {
            //商品回显
            getProductGoodsInfoListByIds(productIds)

        }

        //满减，每满减回显
        var promoType = '${productPromo.promoType}';
        console.log(promoType)
        if (promoType == 'PROMO_REDUCE') {
            $('#promtype_Add').show();
            $('#pre').text("满");

            <#if (condActList?size==1)>
                <#list condActList as condAct>
                    var condValue = ${condAct.condValue}
                    var amount = ${condAct.amount}
                    $("#firstNum").val(condValue)
                    $("#firstPrice").val(amount)
                </#list>
            <#elseif (condActList?size>1)>
                <#assign temp=0 />
                <#list condActList as condAct>
                    var condValue = ${condAct.condValue}
                    var amount = ${condAct.amount}
                    if(${temp}==0){
                        $("#firstNum").val(condValue)
                        $("#firstPrice").val(amount)
                    }else{
                        var tr = '<div class="input-group p-t-5 col-sm-9 pull-left">'
                                + '<div class="input-group-addon"><span>满</span></div>'
                                + '<input type="text" class="form-control" placeholder="金额" name="num" size="3" value="'+condValue+'">'
                                + '<div class="input-group-addon"><span>再减</span></div>'
                                + '<input type="text" class="form-control" placeholder="金额"  name="price" size="5" value="'+amount+'"/></div>'
                                + '<div class="col-sm-3 p-t-5"><button type="button" class="js-button-proPrice btn btn-danger btn-sm">删除该满减</button></div>';
                        $('#proPrice').append(tr);
                     }
                    <#assign temp=temp+1 />
                </#list>

            </#if>


        } else if (promoType == 'PROMO_PRE_REDUCE') {
            $('#pre').text("每满");
            $('#promtype_Add').hide();
            <#list condActList as condAct>
                var condValue = ${condAct.condValue}
                var amount = ${condAct.amount}
                $("#firstNum").val(condValue)
                $("#firstPrice").val(amount)
            </#list>

        }
    }

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
                console.log(data)
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



    function savePromoReduce() {
        $('#addPromoReduce').dpValidate({
            clear: true
        });
        $('#proPrice').find('.input-group').each(function () {
            var tdArr = $(this);
            var num = tdArr.find("input[name=num]").val();//数量
            var price = tdArr.find("input[name=price]").val();//金额
            var int_reg = /^[1-9]\d{0,5}\.?\d{0,2}$/;
            if (!int_reg.test(num)) {
                tdArr.find("input[name=num]").css("border-color", "#dd4b39");
                tdArr.find("input[name=num]").parent().siblings().find('.num').text('请填写不超过999999.99的数字!')
                tdArr.find("input[name=num]").parent().siblings().find('.num').addClass('dp-error-msg')
                flag = false;
            } else {
                tdArr.find("input[name=num]").css("border-color", "");
                tdArr.find("input[name=num]").parent().siblings().find('.num').text('')
                tdArr.find("input[name=num]").parent().siblings().find('.num').removeClass('dp-error-msg')
                flag = true;
                if(int_reg.test(price)&&parseFloat(num)<=parseFloat(price)){
                    tdArr.find("input[name=num]").css("border-color", "#dd4b39");
                    tdArr.find("input[name=num]").parent().siblings().find('.num').text('左边金额必须大于右边金额!')
                    tdArr.find("input[name=num]").parent().siblings().find('.num').addClass('dp-error-msg')
                    flag = false;
                }else{
                    tdArr.find("input[name=num]").css("border-color", "");
                    tdArr.find("input[name=num]").parent().siblings().find('.num').text('')
                    tdArr.find("input[name=num]").parent().siblings().find('.num').removeClass('dp-error-msg')
                    flag = true;
                }

            }
            if (!int_reg.test(price)) {
                tdArr.find("input[name=price]").parent().siblings().find('.price').text('请填写不超过999999.99的数字!!')
                tdArr.find("input[name=price]").parent().siblings().find('.price').addClass('dp-error-msg')
                tdArr.find("input[name=price]").css("border-color", "#dd4b39");
                flag = false;
            } else {
                tdArr.find("input[name=price]").css("border-color", "");
                tdArr.find("input[name=price]").parent().siblings().find('.price').text('')
                tdArr.find("input[name=price]").parent().siblings().find('.price').removeClass('dp-error-msg')
                flag = true;
                if(int_reg.test(num)&&parseFloat(num)<=parseFloat(price)){
                    tdArr.find("input[name=price]").css("border-color", "#dd4b39");
                    tdArr.find("input[name=price]").parent().siblings().find('.price').text('右边金额必须小于左边金额!')
                    tdArr.find("input[name=price]").parent().siblings().find('.price').addClass('dp-error-msg')
                    flag = false;
                }else{
                    tdArr.find("input[name=price]").css("border-color", "");
                    tdArr.find("input[name=price]").parent().siblings().find('.price').text('')
                    tdArr.find("input[name=price]").parent().siblings().find('.price').removeClass('dp-error-msg')
                    flag = true;
                }
            }
        });
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
                var productId = tdArr.eq(i).attr("id");//产品编号
                if (productId) {
                    productIds += productId + ","
                }
            }
        });

        var promoCondActions = '';
        var promo_CondAction = '';
        $('#proPrice').find('.input-group').each(function () {
            var promoCondAction = '';
            var tdArr = $(this);
            var num = tdArr.eq(0).find("input[name=num]").val();//数量
            var price = tdArr.eq(0).find("input[name=price]").val();//金额
            promoCondAction = num + "," + price + "|"
            promoCondActions += promoCondAction;
        });
        promo_CondAction = promoCondActions.substr(0, promoCondActions.length - 1);
        $.ajax({
            url: "updatePromoReduce",
            type: "POST",
            async: false,
            data: {
                productStoreId: $('#productStoreId').val(),
                productPromoId: $('#productPromoId').val(),
//                productStoreId:$('#productStoreId').val(),
                promoCode: $('#promoCode').val(),//促销编码，后台自动生成
                promoName: $('#promoName').val(),
                promoType: $('#promoType').val(),
                fromDate: $('#fromDate').val(),
                thruDate: $('#thruDate').val(),
                promoProductType: $("input[name='promoProductType']:checked").val(),
                promoCondActions: promo_CondAction,
                productIds: productIds,
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
                            window.location.href = '<@ofbizUrl>findPromoReduce</@ofbizUrl>';
                        })
                    }
                }

            },
            error: function (data) {
                //设置提示弹出框内容
                tipLayer({msg:"操作失败！"});

                $('#tipLayer').on('hide.bs.modal', function () {
                    window.location.reload();
                })
            }
        });
    }



    function addprice() {
        var tr = '<div class="col-sm-12 p-0">'
                + '<div class="input-group p-t-5 col-sm-9 pull-left">'
                + '<div class="input-group-addon"><span>满</span></div>'
                + '<input type="text" class="form-control" placeholder="金额" name="num" size="3">'
                + '<div class="input-group-addon"><span>再减</span></div>'
                + '<input type="text" class="form-control" placeholder="金额"  name="price" size="5"/>'
                + '</div>'
                + '<div class="col-sm-3 p-t-5"><button type="button" class="js-button-proPrice btn btn-danger btn-sm">删除该满减</button></div>'
                + '<div class="col-sm-9 p-0">'
                + '<div class="col-sm-6 p-0 num" style="padding-left:40px !important;"></div>'
                + '<div class="col-sm-6 p-0 price" style="padding-left:40px !important;"></div>'
                + '</div>';
        $('#proPrice').append(tr);
    }

    //满减删除按钮事件
    $(document).on('click', '.js-button-proPrice', function () {
        $(this).parent().prev().remove();
        $(this).parent().remove();
    })

    $('#promoType').change(function () {
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
        console.log(promoType)
        if (promoType == 'PROMO_REDUCE') {
            $('#promtype_Add').show();
            $('#pre').text("满");
        } else if (promoType == 'PROMO_PRE_REDUCE') {
            $('#pre').text("每满");
            $("#pre").parent().parent().parent().nextAll().hide();
            $('#promtype_Add').hide();
        }
    });

    $(":radio").click(function () {
        product
        var type = $(this).val();
        if (type == 'PROMO_PRT_ALL') {
            $('#product').hide();
        } else {
            $('#product').show();
        }
    });

    function back() {
        window.location.href = '<@ofbizUrl>findPromoReduce</@ofbizUrl>';
    }
</script>
