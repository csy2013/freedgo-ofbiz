<!-- 内容start -->
<div class="box box-info">
    <form class="form-horizontal" action="<@ofbizUrl>salesFinalizeOrder</@ofbizUrl>" method="post" name="checkoutsetupform" id="checkoutsetupform">
        <input type="hidden" name="currenSalesShippingMethod" value="NO_SHIPPING@_NA_">
        <input type="hidden" name="currenSalesChannelId" value="MIDCON_SALES_CHANNEL">
        <input type="hidden" name="currenShippingContactMechId" value="">
        <div class="box-body">
            <div class="row">
                <div class="form-group col-sm-2">
                    <div class="col-sm-3">
                        <div class="dp-tables_btn">
                            <button id="back" type="button" class="btn btn-primary">
                                <i class="fa">返回修改会员与商品</i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row" style="display: none">
                <td width="26%" align="right" valign="top"><div>${uiLabelMap.CommonCountry}</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                    <select name="countryGeoId" id="checkoutsetupform_countryGeoId" >
                    ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                        <option selected="selected" value="${defaultCountryGeoId}">
                        <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
                        ${countryGeo.get("geoName",locale)}
                        </option>
                    </select>
                    *</td>
            </div>
            <div class="row">
                <div class="form-group col-sm-2">
                    <label class="col-sm-12 control-label"><i class="required-mark">*</i>收货人信息</label>
                </div>

                <div class="form-group col-sm-10">
                    <div class="form-group col-sm-12"  data-type="required" data-mark="收货人姓名">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>收货人姓名</label>
                        <div class="col-sm-3">
                            <input type="text" class="form-control dp-vd" id="consigneeName" name="consigneeName" value="" />
                            <p class="dp-error-msg"></p>
                        </div>
                        <div class="col-sm-1">
                            <div class="dp-tables_btn">
                                <button id="defaultAddress" type="button" class="btn btn-primary">
                                    <i class="fa">默认收货地址</i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="form-group col-sm-12">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>收货地区</label>
                        <div class="col-sm-3">
                            <select class="form-control" name="stateProvinceGeoId" id="checkoutsetupform_stateProvinceGeoId">
                                <option value=""></option>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                        <div class="col-sm-3">
                            <select class="form-control" name="city" id="checkoutsetupform_cityGeoId">
                                <option value=""></option>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                        <div class="col-sm-3">
                            <select class="form-control" name="countyGeoId" id="checkoutsetupform_countyGeoId">
                                <option value=""></option>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group col-sm-12"  data-type="required" data-mark="收货人地址">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>收货人地址</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="consigneeAddress" name="consigneeAddress" value="" />
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group col-sm-12"  data-type="required" data-mark="手机号码">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>手机</label>
                        <div class="col-sm-3">
                            <input type="text" class="form-control dp-vd" id="phone" name="phone" value="" />
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group col-sm-12">
                        <label  class="col-sm-2 control-label">&nbsp;&nbsp;固话</label>
                        <div class="col-sm-3">
                            <input type="text" class="form-control" id="tel" name="tel" value="" />
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group col-sm-12">
                        <label  class="col-sm-2 control-label">&nbsp;&nbsp;邮编</label>
                        <div class="col-sm-3">
                            <input type="text" class="form-control" id="postalCode" name="postalCode" value="" />
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-2">
                    <label class="col-sm-12 control-label"><i class="required-mark">*</i>配送方式</label>
                </div>

                <div class="form-group col-sm-2">
                    <select class="form-control" id="distributionMethod" name="distributionMethod">
                        <option value="ZMPS">周末配送</option>
                        <option value="GZRPS">工作日配送</option>
                        <option value="SMZT">上门自提</option>
                    </select>
                    <p class="dp-error-msg"></p>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-2">
                    <label class="col-sm-12 control-label"><i class="required-mark">*</i>支付方式选择</label>
                </div>

                <div class="form-group col-sm-6">
                    <div class="col-sm-12">
                        <div class="radio">
                            <label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_ALIPAY" checked>支付宝</label>
                            <label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_WEIXIN" checked>微信</label>
                            <label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_UNIONPAY" checked>银联</label>
                            <label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_COD" checked>货到付款</label>
                        </div>
                        <div class="dp-error-msg"></div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-2">
                    <label class="col-sm-12 control-label"><i class="required-mark">*</i>发票信息</label>
                </div>

                <div class="form-group col-sm-10">
                    <div class="form-group col-sm-12">
                        <div class="radio">
                            <label class="col-sm-3"><input name="invoiceType" type="radio" checked="checked" id="noInvoice" value="0">不需要发票</label>
                            <label class="col-sm-3"><input name="invoiceType" type="radio" id="usualInvoice" value="1">普通发票</label>
                        </div>
                    </div>

                    <div class="form-group col-sm-12" id="invoice" style="display: none">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>发票抬头</label>
                        <div class="col-sm-6">
                            <input type="text" class="form-control" id="invoiceTitle" name="invoiceTitle" value="" />
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group col-sm-12" id = "invoiceCon" style="display: none">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>发票内容</label>
                        <div class="col-sm-3">
                            <select class="form-control" id="invoiceContent" name="invoiceContent">
                                <#list invoiceContents as ic>
                                    <option value="${ic.enumId}">${ic.description}</option>
                                </#list>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-2">
                    <label class="col-sm-12 control-label"><i class="required-mark">*</i>支付信息</label>
                </div>

                <div class="form-group col-sm-10">
                    <div class="form-group col-sm-12">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>原始价格</label>
                        <div class="col-sm-2">
                            <input type="text" class="form-control" id="originalMoney" name="originalMoney" value="${shoppingCart.getGrandTotal()}" readonly/>
                            <p class="dp-error-msg"></p>
                        </div>
                        <label  class="col-sm-2 control-label">配送费用</label>
                        <div class="col-sm-2">
                            <input type="text" class="form-control" id="distributeMoney" name="distributeMoney" value="" onkeyup="value=this.value.replace(/\D+/g,'')"/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group col-sm-12">
                        <label class="col-sm-2 control-label">商家优惠</label>
                        <#--<div class="form-group col-sm-6 p-0">-->
                        <div class="form-group col-sm-6">
                            <#--<div class="row">
                                <div class="col-sm-4">
                                    <div class="checkbox clearfix m-0">
                                        <label class="col-sm-12" title="活动1"><input name="mark" type="checkbox">活动1</label>
                                    </div>
                                </div>
                                <label  class="col-sm-4 control-label"><i class="required-mark">*</i>优惠</label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="" name="" value="" />
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="checkbox clearfix m-0">
                                        <label class="col-sm-12" title="商家优惠卷"><input name="mark" type="checkbox">商家优惠卷</label>
                                    </div>
                                </div>
                                <label  class="col-sm-4 control-label"><i class="required-mark">*</i>优惠</label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="" name="" value="" />
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="checkbox clearfix m-0">
                                        <label class="col-sm-12" title="活动3"><input name="mark" type="checkbox">活动3</label>
                                    </div>
                                </div>
                                <label  class="col-sm-4 control-label"><i class="required-mark">*</i>优惠</label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="" name="" value="" />
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>-->

                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="checkbox clearfix m-0">
                                        <label class="col-sm-12" title="手动优惠"><input name="mark" type="checkbox" name="isBusinessHandDiscount" id="isBusinessHandDiscount">手动优惠</label>
                                    </div>
                                </div>
                                <label  class="col-sm-4 control-label">优惠</label>
                                <div class="col-sm-4">
                                    <input type="text"  class="form-control" readonly id="businessHandDiscount" name="businessHandDiscount" value="" onkeyup="value=this.value.replace(/\D+/g,'')"/>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-sm-4">
                                </div>
                                <label  class="col-sm-4 control-label">商家优惠小计</label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="businessDiscountTotal" name="businessDiscountTotal" value="" readonly/>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group col-sm-12">
                        <label class="col-sm-2 control-label">使用积分</label>
                        <div class="form-group col-sm-6">
                            <div class="row">
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="useIntegral" name="useIntegral" value="" onkeyup="value=this.value.replace(/\D+/g,'')"/>
                                </div>
                                <div  class="col-sm-3">积分 </div>
                                <div  class="col-sm-1">抵 </div>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="integralDiscount" name="integralDiscount" value="10" readonly/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <#--<div class="form-group col-sm-12">
                        <label class="col-sm-2 control-label">免运费</label>
                        <div class="form-group col-sm-6">
                            <div class="row">
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="" name="" value="" />
                                </div>
                            </div>
                        </div>
                    </div>-->


                    <div class="form-group col-sm-12">
                        <label class="col-sm-2 control-label">平台优惠</label>
                    <#--<div class="form-group col-sm-6 p-0">-->
                        <div class="form-group col-sm-6">
                            <#--<div class="row">
                                <div class="col-sm-4">
                                    <div class="checkbox clearfix m-0">
                                        <label class="col-sm-12" title="活动1"><input name="mark" type="checkbox">活动1</label>
                                    </div>
                                </div>
                                <label  class="col-sm-4 control-label"><i class="required-mark">*</i>优惠</label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="" name="" value="" />
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>-->
                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="checkbox clearfix m-0">
                                        <label class="col-sm-12"><input name="isPlatDiscount" id="isPlatDiscount" type="checkbox" >平台优惠券</label>
                                    </div>
                                </div>
                                <label  class="col-sm-4 control-label">优惠</label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" readonly id="platDiscount" name="platDiscount" value="" onkeyup="value=this.value.replace(/\D+/g,'')"/>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-4">
                                </div>
                                <label  class="col-sm-4 control-label">平台优惠小计</label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="platDiscountTotal" name="platDiscountTotal" value="" readonly/>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group col-sm-12">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>优惠小计</label>
                        <div class="col-sm-2">
                            <input type="text" class="form-control" id="discountMoney" name="discountMoney" value="" readonly/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group col-sm-12">
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>应付金额</label>
                        <div class="col-sm-2">
                            <input type="text" class="form-control" id="shouldPayMoney" name="shouldPayMoney" value="" readonly/>
                            <p class="dp-error-msg"></p>
                        </div>
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>获得积分</label>
                        <div class="col-sm-2">
                            <input type="text" class="form-control" id="getIntegral" name="getIntegral" value="" readonly/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group col-sm-12">
                        <label  class="col-sm-2 control-label">实付金额</label>
                        <div class="col-sm-2">
                            <input type="text" class="form-control" id="actualPayMoney" name="actualPayMoney" value="" onkeyup="clearNoNum(this)"/>
                            <p class="dp-error-msg"></p>
                        </div>
                        <label  class="col-sm-2 control-label"><i class="required-mark">*</i>未付金额</label>
                        <div class="col-sm-2">
                            <input type="text" class="form-control" id="notPayMoney" name="notPayMoney" value="" readonly/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>


            <div class="row">
                <div class="form-group col-sm-2">
                    <label class="col-sm-12 control-label">备注</label>
                </div>

                <div class="form-group col-sm-5">
                    <input type="text" class="form-control" id="" name="remarks" value="" />
                    <p class="dp-error-msg"></p>
                </div>
            </div>







            <div class="row">
                <div class="form-group col-sm-6">
                    <label class="col-sm-2 control-label"></label>
                    <div class="col-sm-10">
                        <div class="dp-tables_btn">
                            <button id="complete" class="btn btn-primary">
                                <i class="fa">完成</i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 表格区域start -->

        </div><!-- /.box-body -->
    </form>
</div>

<script>

    $("#noInvoice").click(function(){
        $("#invoice").hide();
        $("#invoiceCon").hide();
    });

    $("#usualInvoice").click(function(){
        $("#invoice").show();
        $("#invoiceCon").show();
    });

    $("#isBusinessHandDiscount").click(function(){
        if($(this).attr("checked") == "checked"){
            $("#businessHandDiscount").removeAttr("readonly");
        }else{
            $("#businessHandDiscount").attr("readonly","readonly");
        }
    });

    $("#isPlatDiscount").click(function(){
        if($(this).attr("checked") == "checked"){
            $("#platDiscount").removeAttr("readonly");
        }else{
            $("#platDiscount").attr("readonly","readonly");
        }
    });

    function clearNoNum(obj){
        obj.value = obj.value.replace(/[^\d.]/g,"");  //清除“数字”和“.”以外的字符
        obj.value = obj.value.replace(/^\./g,"");  //验证第一个字符是数字而不是.
        obj.value = obj.value.replace(/\.{2,}/g,"."); //只保留第一个. 清除多余的.
        obj.value = obj.value.replace(".","$#$").replace(/\./g,"").replace("$#$",".");
    }

    $('#complete').click(function(){
        $('#checkoutsetupform').dpValidate({
            validate: true,
            callback: function(){
                document.getElementById('checkoutsetupform').submit();
            }
        });

    });



    $("#defaultAddress").click(function(){
        $.post("<@ofbizUrl>getUserPostalAddress</@ofbizUrl>",{},function(data){
            if (data.addressInfos != undefined && data.addressInfos != null && data.addressInfos != ""){
                var postalAddress = data.addressInfos[0].postalAddress;
                $("#consigneeName").val(postalAddress.toName);
                $("#checkoutsetupform_stateProvinceGeoId").val(postalAddress.stateProvinceGeoId).trigger('change');
                $("#checkoutsetupform_cityGeoId").val(postalAddress.city).trigger('change');
                $("#checkoutsetupform_countyGeoId").val(postalAddress.countyGeoId);
                $("#consigneeAddress").val(postalAddress.address1);
                $("#phone").val(postalAddress.mobilePhone);
                $("#tel").val(postalAddress.tel);
            }
        })
    });

    $("#back").click(function(){
        //返回，不清掉session
        window.location.href = "<@ofbizUrl>addOrder?clear=N</@ofbizUrl>";
    });

    $(function(){
        setInterval(calcuPrice,500);
    });

    function calcuPrice(){
        var integralPerMoney =  parseFloat("${integralPerMoney}");
        var useIntegral = parseFloat(($("#useIntegral").val() != "")? $("#useIntegral").val() : 0);
        $("#integralDiscount").val(useIntegral / integralPerMoney);
        var businessHandDiscount = parseFloat(($("#businessHandDiscount").val() != "")?$("#businessHandDiscount").val() : 0);
        $("#businessDiscountTotal").val(businessHandDiscount);
        var platDiscount = parseFloat(($("#platDiscount").val() != "")?$("#platDiscount").val() : 0);
        $("#platDiscountTotal").val(platDiscount);
        var integralDiscount = parseFloat(($("#integralDiscount").val() != "")?$("#integralDiscount").val() : 0);
        var discountMoney = businessHandDiscount+ platDiscount + integralDiscount;
        $("#discountMoney").val(discountMoney);
        var originalMoney = parseFloat(($("#originalMoney").val() != "")?$("#originalMoney").val() : 0) ;
        var distributeMoney =  parseFloat(($("#distributeMoney").val() != "")?$("#distributeMoney").val() : 0) ;
        var shouldPayMoney = originalMoney + distributeMoney - discountMoney;
        $("#shouldPayMoney").val(shouldPayMoney);
        var actualPayMoney = parseFloat(($("#actualPayMoney").val())?$("#actualPayMoney").val() : 0);
        $("#getIntegral").val(actualPayMoney);
        var notPayMoney = shouldPayMoney - actualPayMoney;
        $("#notPayMoney").val(notPayMoney);
    }
</script>


