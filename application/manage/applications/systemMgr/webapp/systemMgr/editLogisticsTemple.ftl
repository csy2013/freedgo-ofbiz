<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>"
      type="text/css"/>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<style>

    .express_details {
        display: none;
        border: 1px solid #dfdfdf;
        margin-top: 10px;
    }

    .express_area {
        position: relative;
        height: 25px;
        overflow-y: hidden;
        z-index: 99;
    }

    .express_area p {
        position: absolute;
        left: 0;
        top: 0;
        display: block;
        padding: 0 10px;
        line-height: 25px;
    }

    .express_area:hover {
        overflow-y: visible;
    }

    .express_area:hover p {
        background: #DFEDF7;
    }

    table {
        border-collapse: collapse;
        border-spacing: 0;
        padd: expression(this.cellPadding=0);
    }

    table.dataTable thead th {
        background-color: #A4AEB9 !important;
        color: #fff;
    }

    table.table thead tr th {
        background: #A4AEB9;
        color: #fff;
        vertical-align: middle;
    }

    .city_area {
        border: 1px solid #ddd;
        padding: 10px;
        height: 300px;
        overflow-y: scroll;
    }

    .city_choose {
        margin-top: 80px;
    }

    .city_choosed {
        border: 1px solid #ddd;
        padding: 10px;
        height: 300px;
        overflow-y: scroll;
    }

    .mb10 {
        margin: 10px 5px;
    }


</style>

<div class="box box-info">
    <div class="box-body">
        <h3 class="">物流模板设置
        </h3>
        <div class="cut-off-rule bg-gray"></div>
        <form class="form-horizontal" action="<@ofbizUrl>editTemple</@ofbizUrl>" method="post" id="freightForm">
            <input type="hidden" name="logisticsTempleId" value="${logisticsTempleId}">
            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="模板名称">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>模板名称：</label>
                    <div class="col-sm-10">
                        <div class="col-sm-4">
                            <input type="text" class="form-control dp-vd" id="freightTemplateName"
                                   name="freightTemplateName" maxlength="7"
                                   value="${logisticsTemple.logisticsTempleName?if_exists}"/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row" style="display: none">
                <div class="form-group col-sm-12">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>运费承担：</label>
                    <div class="col-sm-10">
                        <label class="col-sm-2">
                            <input name="freightPackageMail" value="0" onchange="checkMail(this);"
                                   <#if logisticsTemple.logisticsPackageMail?has_content && logisticsTemple.logisticsPackageMail == "0">checked="checked"</#if>
                                   type="radio"> 买家承担运费
                        </label>
                        <label class="col-sm-2">
                            <input name="freightPackageMail" value="1" onchange="checkMail(this);"
                                   <#if logisticsTemple.logisticsPackageMail?has_content && logisticsTemple.logisticsPackageMail == "2">checked="checked"</#if>
                                   type="radio"> 卖家承担运费
                        </label>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-12">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>计价方式：</label>
                    <div class="col-sm-10">
                        <label class="col-sm-2">
                            <input name="freightMethods" value="0" onchange="friehgtMethod(this);"
                                   <#if logisticsTemple.logisticsMethods?has_content && logisticsTemple.logisticsMethods == "0">checked="checked"</#if>
                                   type="radio">按件计价
                        </label>
                        <label class="col-sm-2">
                            <input name="freightMethods" value="1" onchange="friehgtMethod(this);"
                                   <#if logisticsTemple.logisticsMethods?has_content && logisticsTemple.logisticsMethods == "1">checked="checked"</#if>
                                   type="radio"> 按重计价
                        </label>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-12">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>运送方式：</label>
                    <div class="col-sm-10 shipset">
                    <#if logisticsCompanys?has_content>
                        <#list logisticsCompanys as logisticsCompany>
                            <div class="col-sm-12">
                                <div class="checkbox1">
                                    <label class="lab">
                                        <input name="logComId" codename="${logisticsCompany.id?if_exists}"
                                               id="${logisticsCompany.id?if_exists}"
                                               value="${logisticsCompany.id?if_exists}"
                                               <#if logisticsTemple.logisticsCompanyId?has_content && logisticsTemple.logisticsCompanyId == logisticsCompany.id>checked="checked"</#if>
                                               type="radio">
                                    ${logisticsCompany.companyName?if_exists}
                                    </label>
                                </div>
                                <div class="express_details" id="detail-express${logisticsCompany.id?if_exists}" <#if logisticsTemple.logisticsCompanyId?has_content && logisticsTemple.logisticsCompanyId == logisticsCompany.id>style="display: block"</#if>>
                                    <div class="mb10">
                                        默认运费：
                                        <input id="${logisticsCompany.id?if_exists}_areas_n1"
                                               name="${logisticsCompany.id?if_exists}_areas" value="" type="hidden">
                                        <input name="${logisticsCompany.id?if_exists}_start"
                                               value="<#if logisticsTemple.logisticsCompanyId?has_content && logisticsTemple.logisticsCompanyId == logisticsCompany.id>${defaultItem.start?if_exists}<#else>1</#if>"
                                               id="${logisticsCompany.id?if_exists}_start" type="text"><span
                                            class="methods"><#if logisticsTemple.logisticsMethods?has_content && logisticsTemple.logisticsMethods == "0">件<#else>kg</#if>内，</span>
                                        <input name="${logisticsCompany.id?if_exists}_postage"
                                               value="<#if logisticsTemple.logisticsCompanyId?has_content && logisticsTemple.logisticsCompanyId == logisticsCompany.id>${defaultItem.postage?if_exists}<#else>0</#if>"
                                               id="${logisticsCompany.id?if_exists}_postage" type="text">元 每增加
                                        <input name="${logisticsCompany.id?if_exists}_plus"
                                               value="<#if logisticsTemple.logisticsCompanyId?has_content && logisticsTemple.logisticsCompanyId == logisticsCompany.id>${defaultItem.plus?if_exists}<#else>1</#if>"
                                               id="${logisticsCompany.id?if_exists}_plus" type="text"><span
                                            class="methods"><#if logisticsTemple.logisticsMethods?has_content && logisticsTemple.logisticsMethods == "0">件<#else>kg</#if>，增加运费</span>
                                        <input name="${logisticsCompany.id?if_exists}_postageplus"
                                               value="<#if logisticsTemple.logisticsCompanyId?has_content && logisticsTemple.logisticsCompanyId == logisticsCompany.id>${defaultItem.postagePlus?if_exists}<#else>0</#if>"
                                               id="${logisticsCompany.id?if_exists}_postageplus" type="text">元
                                        <a title="" data-original-title="" href="javascript:;"
                                           class="yunsong help_tips">
                                            <i class="icon iconfont"></i>
                                        </a>
                                    </div>
                                    <p>
                                        <button type="button" class="btn btn-primary J_AddRule"
                                                data-logcomid="${logisticsCompany.id?if_exists}"
                                                data-logcode="${logisticsCompany.id?if_exists}">添加收货地区
                                        </button>
                                    </p>
                                    <table class="table table-bordered table-hover">
                                        <thead>
                                        <tr>
                                            <th width="30%">收货地区</th>
                                            <th class="methodslist">首<#if logisticsTemple.logisticsMethods?has_content && logisticsTemple.logisticsMethods == "0">件<#else>重</#if></th>
                                            <th>首费(元)</th>
                                            <th class="methodslist">续<#if logisticsTemple.logisticsMethods?has_content && logisticsTemple.logisticsMethods == "0">件<#else>重</#if></th>
                                            <th>续费(元)</th>
                                            <th width="120">操作</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <#if logisticsTemple.logisticsCompanyId?has_content && logisticsTemple.logisticsCompanyId == logisticsCompany.id>
                                        <#list logisticsTempleItems as logisticsTempleItem>
                                        <#if logisticsTempleItem.id != defaultItem.id>
                                            <#assign areas = logisticsTempleItem.areas?split(",")>
                                            <#assign areaName = "">
                                            <#list areas as area>
                                                <#assign geo = delegator.findByPrimaryKey("Geo",{"geoId":area})>
                                                <#if geo?has_content>
                                                    <#if areaName == "">
                                                        <#assign areaName = geo.geoName>
                                                    <#else>
                                                        <#assign areaName = areaName + "、" + geo.geoName>
                                                    </#if>
                                                </#if>
                                            </#list>
                                            <tr data-group="n1" data-group-value="1" data-code="">
                                                <td>
                                                    <div class="express_area"><p>${areaName}</p>
                                                        <input type="hidden" id="${logisticsCompany.id?if_exists}_areas_n1" name="${logisticsCompany.id?if_exists}_areas" value="${logisticsTempleItem.areas?if_exists}">
                                                    </div>
                                                </td>
                                                <td><input type="hidden" value="${logisticsTempleItem.start?if_exists}" name="${logisticsCompany.id?if_exists}_start" id="${logisticsCompany.id?if_exists}_start_n1"><span>${logisticsTempleItem.start?if_exists}</span></td>
                                                <td><input type="hidden" value="${logisticsTempleItem.postage?if_exists}" name="${logisticsCompany.id?if_exists}_postage" id="${logisticsCompany.id?if_exists}_postage_n1"><span>${logisticsTempleItem.postage?if_exists}</span></td>
                                                <td><input type="hidden" value="${logisticsTempleItem.plus?if_exists}" name="${logisticsCompany.id?if_exists}_plus" id="${logisticsCompany.id?if_exists}_plus_n1"><span>${logisticsTempleItem.plus?if_exists}</span></td>
                                                <td><input type="hidden" value="${logisticsTempleItem.postagePlus?if_exists}" name="${logisticsCompany.id?if_exists}_postageplus" id="${logisticsCompany.id?if_exists}_postageplus_n1"><span>${logisticsTempleItem.postagePlus?if_exists}</span></td>
                                                <td>
                                                    <div class="btn-group">
                                                        <button type="button" class="btn btn-default" onclick="toEditCity(this,'${logisticsCompany.id?if_exists}','${logisticsCompany.id?if_exists}')">编辑</button>
                                                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"><span class="caret"></span> <span class="sr-only">Toggle Dropdown</span></button>
                                                        <ul class="dropdown-menu" role="menu">
                                                            <li><a href="javascript:void(0);" onclick="removeTr(this);">删除</a></li>
                                                        </ul>
                                                    </div>
                                                </td>
                                            </tr>
                                        </#if>
                                        </#list>
                                        </#if>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </#list>
                    </#if>

                        <div id="logcomTips" class="col-sm-12">
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-5 col-sm-17">
                        <button id="save" type="button" class="btn btn-primary">保存</button>
                        <button type="button" class="btn btn-default" onclick="quxiao();">取消</button>
                    </div>
                </div>
        </form>
    </div>
</div>
<div id="editExpress" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">添加收货地区</h4>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal" id="cityForm">
                    <div class="form-group" data-type="required" data-mark="收货地区">
                        <label class="control-label col-sm-3">收货地区：</label>
                        <div class="col-sm-9">
                            <div class="city_area col-sm-5">
                                <ul id="treeDemo" class="ztree"></ul>
                            </div>
                            <div class="city_choose col-sm-2">
                                <button type="button" class="btn btn-default" id="getCitys">&gt;&gt;</button>
                                <br><br>
                            </div>
                            <div class="city_choosed col-sm-5">
                                <p class="city_choosed_p" style="word-break:break-word;"></p>
                                <input type="hidden" value="" class="dp-vd" name="tempCity" id="tempCity"/>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="form-group" data-type="format" data-reg="/^[0-9]*$/" data-mark="首件个数">
                        <label class="control-label col-sm-3 Weight_First">首件个数：</label>
                        <div class="col-sm-9">
                            <input class="col-sm-6 dp-vd" type="text" id="viewstart" name="viewstart">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="format" data-reg="/^[0-9]+([.]{1}[0-9]+){0,1}$/"
                         data-mark="首件运费">
                        <label class="control-label col-sm-3 Weight_First_cost">首件运费：</label>
                        <div class="col-sm-9">
                            <span class="col-sm-1">￥</span>
                            <input class="col-sm-5 dp-vd" type="text" id="viewstartprice" name="viewstartprice">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="format" data-reg="/^[0-9]*$/" data-mark="续件个数">
                        <label class="control-label col-sm-3 Weight_Link">续件个数：</label>
                        <div class="col-sm-9">
                            <input class="col-sm-6 dp-vd" type="text" id="viewend" name="viewend">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="format" data-reg="/^[0-9]+([.]{1}[0-9]+){0,1}$/"
                         data-mark="续件运费">
                        <label class="control-label col-sm-3 Weight_Link_cost">续件运费：</label>
                        <div class="col-sm-9">
                            <span class="col-sm-1">￥</span>
                            <input class="col-sm-5 dp-vd" type="text" id="viewendprice" name="viewendprice">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">保存</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>
<script>


    /* 下面是关于树形菜单 */
    var setting = {
        check: {
            enable: true,
            chkboxType: {"Y": "ps", "N": "ps"}
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        view: {
            showIcon: false
        }

    };

    var flag = 0;//标识 添加或者修改
    var zTree;

    /* 下面是关于树形菜单 END */
    var logCode = '';
    var logComId = '';
    $(function () {
        //选择地区方法
        $(".J_AddRule").click(function () {
            //判断计价方式
            var freightMethods = $('input[name="freightMethods"]:checked').val();
            //用于 添加收货地区div显示
            if (freightMethods == 1) {
                $('.Weight_First').html('首重：');
                $('.Weight_Link').html('续重：');
                $('.Weight_First_cost').html('首重运费：');
                $('.Weight_Link_cost').html('续重运费：');
            } else {
                $('.Weight_First').html('首件个数：');
                $('.Weight_Link').html('续件个数：');
                $('.Weight_First_cost').html('首件运费：');
                $('.Weight_Link_cost').html('续件运费：');
            }
            flag = 0;
            $('.city_choosed .city_choosed_p').html('');
            $("#tempCity").val('');
            $("#viewstart").val('');
            $("#viewstartprice").val('');
            $("#viewend").val('');
            $("#viewendprice").val('');
            logCode = $(this).attr('data-logCode');
            logComId = $(this).attr('data-logComId');

            //加载省市
            $.ajax({
                type: 'post',
                url: '<@ofbizUrl>selectProvinceList</@ofbizUrl>',
                async: false,
                success: function (data) {
                    //查询出内容加载省份
                    var zNodes = new Array();
                    if (data.areaTree != null) {
                        var node = {
                            id: 1, pId: 0, name: '全国', open: true
                        };
                        zNodes.push(node);
                        for (var i = 0; i < data.areaTree.length; i++) {
                            var a = i + 1 + 1;
                            var node = {
                                id: a, pId: 1, name: data.areaTree[i].provinceName, open: false
                            };
                            zNodes.push(node);
                            //加载城市
                            if (data.areaTree[i].cityList != null) {
                                for (var j = 0; j < data.areaTree[i].cityList.length; j++) {
                                    var b = a + "" + (j + 1);
                                    var node = {
                                        id: b,
                                        pId: a,
                                        name: data.areaTree[i].cityList[j].cityName,
                                        cityId: data.areaTree[i].cityList[j].cityId,
                                        open: false
                                    };
                                    zNodes.push(node);
                                }
                            }
                        }
                    }

                    $.fn.zTree.init($("#treeDemo"), setting, zNodes);

                    var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                    var nodes = treeObj.getNodes();

                    for (var k = 0; k < treeObj.transformToArray(nodes).length; k++) {
                        var node = treeObj.transformToArray(nodes)[k];
                        $("input[name='" + logCode + "_areas']").each(function () {
                            var $arr = new Array();
                            var $val = $(this).val();
                            if ($val != undefined) {
                                $arr = $val.split(",");
                            }
                            for (var p = 0; p < $arr.length; p++) {
                                if ($arr[p] == node.cityId) {
                                    treeObj.setChkDisabled(node, true, false, true);
                                }
                            }

                        });
                    }
                }
            });

            if ($('input:radio[name="freightPackageMail"]:checked').val() == 0) {
                $("#viewstart").val(1);
                $("#viewstart").attr('readonly', false);
                $("#viewstartprice").val(0);
                $("#viewstartprice").attr('readonly', false);
                $("#viewend").val(1);
                $("#viewend").attr('readonly', false);
                $("#viewendprice").val(0);
                $("#viewendprice").attr('readonly', false);
            } else {
                $("#viewstart").val(1);
                $("#viewstart").attr('readonly', true);
                $("#viewstartprice").val(0);
                $("#viewstartprice").attr('readonly', true);
                $("#viewend").val(1);
                $("#viewend").attr('readonly', true);
                $("#viewendprice").val(0);
                $("#viewendprice").attr('readonly', true);
            }
            $('#editExpress').modal('show');
            $('#sign').html("添加收货地区");
        });

        //弹出框表单校验方法
        $('#cityForm').dpValidate({
            validate: true,
            callback: function () {
                doaddarea();
            }
        });

        //新增弹出框保存按钮点击事件
        $('#editExpress #save').click(function () {
            $('#cityForm').dpValidate({
                clear: true
            });
            $('#cityForm').submit();
        });

    });
    /* 保存收货地区 */
    function doaddarea() {
        if (flag == 0) {
            var htm = '';
            htm += ' <tr data-group="n1"  data-group-value="1" data-code="">';
            htm += ' <td>';
            htm += '  <div class="express_area">';
            htm += '    <p>' + $('.city_choosed .city_choosed_p').html() + '</p>  <input type="hidden" id="' + logCode + '_areas_n1" name="' + logCode + '_areas" value="' + $("#tempCity").val() + '">';
            htm += '  </div>';
            htm += ' <td>  <input type="hidden" value="' + $("#viewstart").val() + '" name="' + logCode + '_start" id="' + logCode + '_start_n1" ><span>' + $("#viewstart").val() + '</span></td>';
            htm += ' <td>  <input type="hidden" value="' + $("#viewstartprice").val() + '" name="' + logCode + '_postage" id="' + logCode + '_postage_n1"><span>' + $("#viewstartprice").val() + '</span></td>';
            htm += ' <td>  <input type="hidden" value="' + $("#viewend").val() + '" name="' + logCode + '_plus" id="' + logCode + '_plus_n1"/><span>' + $("#viewend").val() + '</span></td>';
            htm += '<td>  <input type="hidden" value="' + $("#viewendprice").val() + '" name="' + logCode + '_postageplus" id="' + logCode + '_postageplus_n1"><span>' + $("#viewendprice").val() + '</span></td>';
            htm += '<td>';
            htm += ' <div class="btn-group">';
            htm += '    <button type="button" class="btn btn-default" onclick="toEditCity(this,\'' + logCode + '\',\'' + logComId + '\')">编辑</button>';
            htm += '     <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">';
            htm += '      <span class="caret"></span>';
            htm += '    <span class="sr-only">Toggle Dropdown</span>';
            htm += '  </button>';
            htm += '   <ul class="dropdown-menu" role="menu">';
            htm += '     <li><a href="javascript:void(0);" onclick="removeTr(this);">删除</a></li>';
            htm += '    </ul>';
            htm += '  </div>';
            htm += ' </td>';
            htm += ' </tr>';
            $("#detail-express" + logComId).find(".table-bordered tbody").append(htm);
            $('#editExpress').modal('hide');
        } else {
            trline.find('p').text($('.city_choosed .city_choosed_p').html());
            trline.find('input[name="' + logCode + '_areas"]').val($("#tempCity").val());
            trline.find('input[name="' + logCode + '_start"]').val($("#viewstart").val());
            trline.find('input[name="' + logCode + '_start"]').next().text($("#viewstart").val());
            trline.find('input[name="' + logCode + '_postage"]').val($("#viewstartprice").val());
            trline.find('input[name="' + logCode + '_postage"]').next().text($("#viewstartprice").val());
            trline.find('input[name="' + logCode + '_plus"]').val($("#viewend").val());
            trline.find('input[name="' + logCode + '_plus"]').next().text($("#viewend").val());
            trline.find('input[name="' + logCode + '_postageplus"]').val($("#viewendprice").  val());
            trline.find('input[name="' + logCode + '_postageplus"]').next().text($("#viewendprice").val());
            $('#editExpress').modal('hide');
        }
    }

    /* 编辑收货地区 */
    var trline;
    function toEditCity(obj, logCodes, logComIds) {
        flag = 1;
        //判断计价方式
        var freightMethods = $('input[name="freightMethods"]:checked').val();
        //用于 添加收货地区div显示
        if (freightMethods == 1) {
            $('.Weight_First').html('首重：');
            $('.Weight_Link').html('续重：');
            $('.Weight_First_cost').html('首重运费：');
            $('.Weight_Link_cost').html('续重运费：');
        } else {
            $('.Weight_First').html('首件个数：');
            $('.Weight_Link').html('续件个数：');
            $('.Weight_First_cost').html('首件运费：');
            $('.Weight_Link_cost').html('续件运费：');
        }
        $('.city_choosed .city_choosed_p').html('');
        $("#tempCity").val('');
        $("#viewstart").val('');
        $("#viewstartprice").val('');
        $("#viewend").val('');
        $("#viewendprice").val('');

        logCode = logCodes;
        logComId = logComIds;
        trline = $(obj).parents('tr');
        var areas = trline.find('input[name="' + logCodes + '_areas"]').val();
        var start = trline.find('input[name="' + logCodes + '_start"]').val();
        var postage = trline.find('input[name="' + logCodes + '_postage"]').val();
        var plus = trline.find('input[name="' + logCodes + '_plus"]').val();
        var postageplus = trline.find('input[name="' + logCodes + '_postageplus"]').val();
        //加载省市
        $.ajax({
            type: 'post',
            url: 'selectProvinceList',
            async: false,
            success: function (data) {
                //查询出内容加载省份
                var zNodes = new Array();
                if (data.areaTree != null) {
                    var node = {
                        id: 1, pId: 0, name: '全国', open: true
                    };
                    zNodes.push(node);
                    for (var i = 0; i < data.areaTree.length; i++) {
                        var a = i + 1 + 1;
                        var node = {
                            id: a, pId: 1, name: data.areaTree[i].provinceName, open: false
                        };
                        zNodes.push(node);
                        //加载城市
                        if (data.areaTree[i].cityList != null) {
                            for (var j = 0; j < data.areaTree[i].cityList.length; j++) {
                                var b = a + "" + (j + 1);
                                var node = {
                                    id: b,
                                    pId: a,
                                    name: data.areaTree[i].cityList[j].cityName,
                                    cityId: data.areaTree[i].cityList[j].cityId,
                                    open: false
                                };
                                zNodes.push(node);
                            }
                        }
                    }
                }

                $.fn.zTree.init($("#treeDemo"), setting, zNodes);
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var nodes = treeObj.getNodes();

                for (var k = 0; k < treeObj.transformToArray(nodes).length; k++) {
                    var node = treeObj.transformToArray(nodes)[k];
                    $("input[name='" + logCode + "_areas']").each(function () {
                        var $arr = new Array();
                        var $val = $(this).val();
                        if ($val != undefined) {
                            $arr = $val.split(",");
                        }
                        for (var p = 0; p < $arr.length; p++) {
                            if ($arr[p] == node.cityId) {
                                treeObj.setChkDisabled(node, true, false, true);
                            }
                        }

                    });
                }

                var v = "";
                for (var k = 0; k < treeObj.transformToArray(nodes).length; k++) {
                    var node = treeObj.transformToArray(nodes)[k];

                    if (areas != null && areas != '') {
                        $arr = areas.split(',');
                        $("#tempCity").val(areas);
                        for (var p = 0; p < $arr.length; p++) {
                            if ($arr[p] == node.cityId) {
                                treeObj.setChkDisabled(node, false, false, true);
                                treeObj.checkNode(node, true, true);
                                v += node.name + ",";
                            }
                        }
                    }
                }
                if (v != '') {
                    $('.city_choosed .city_choosed_p').html(v.substring(0, v.length - 1));
                }
            }
        });

        if ($('input:radio[name="freightPackageMail"]:checked').val() == 0) {
            $("#viewstart").val(start);
            $("#viewstart").attr('readonly', false);
            $("#viewstartprice").val(postage);
            $("#viewstartprice").attr('readonly', false);
            $("#viewend").val(plus);
            $("#viewend").attr('readonly', false);
            $("#viewendprice").val(postageplus);
            $("#viewendprice").attr('readonly', false);
        } else {
            $("#viewstart").val(start);
            $("#viewstart").attr('readonly', true);
            $("#viewstartprice").val(postage);
            $("#viewstartprice").attr('readonly', true);
            $("#viewend").val(plus);
            $("#viewend").attr('readonly', true);
            $("#viewendprice").val(postageplus);
            $("#viewendprice").attr('readonly', true);
        }
        $('#editExpress').modal('show');
        $('#sign').html("编辑收货地区");
    }

    //移除已选择的收货地区
    function removeTr(obj) {
        $(obj).parents('tr').remove();
    }

    //物流模板设置 运费承担
    function checkMail(obj) {
        //买家承担运费
        if ($(obj).val() == '0') {
            $("input[name='logComId']").each(function () {
                var $logComCode = $(this).attr('codename');
                $("input[name='" + $logComCode + "_start']").each(function () {
                    $(this).val(1);
                    $(this).attr('readonly', false);
                });
                $("input[name='" + $logComCode + "_postage']").each(function () {
                    $(this).val(0);
                    $(this).attr('readonly', false);
                });
                $("input[name='" + $logComCode + "_plus']").each(function () {
                    $(this).val(1);
                    $(this).attr('readonly', false);
                });
                $("input[name='" + $logComCode + "_postageplus']").each(function () {
                    $(this).val(0);
                    $(this).attr('readonly', false);
                });
            });
        } else {
            //卖家承担运费
            $("input[name='logComId']").each(function () {
                var $logComCode = $(this).attr('codename');
                $("input[name='" + $logComCode + "_start']").each(function () {
                    $(this).val(1);
                    $(this).attr('readonly', true);
                });
                $("input[name='" + $logComCode + "_postage']").each(function () {
                    $(this).val(0);
                    $(this).next().text(0);
                    $(this).attr('readonly', true);
                });
                $("input[name='" + $logComCode + "_plus']").each(function () {
                    $(this).val(1);
                    $(this).attr('readonly', true);
                });
                $("input[name='" + $logComCode + "_postageplus']").each(function () {
                    $(this).val(0);
                    $(this).next().text(0);
                    $(this).attr('readonly', true);
                });
            });
        }
    }

    /*计价方式*/
    function friehgtMethod(obj) {
        if (obj.checked == true) {
            //按件计价
            if ($(obj).val() == 0) {
                $(".methods").each(function () {
                    $(this).html($(this).html().replace('kg', '件'));
                });
                $(".methodslist").each(function () {
                    $(this).html($(this).html().replace('重', '件'));
                });
            }
            //按重计价
            if ($(obj).val() == 1) {
                $(".methods").each(function () {
                    $(this).html($(this).html().replace('件', 'kg'));
                });
                $(".methodslist").each(function () {
                    $(this).html($(this).html().replace('件', '重'));
                });
            }
        }
    }


</script>
<script>
    var num = 0;
    $(function () {
//        $("#freightForm").validate();
        /* 为选定的select下拉菜单开启搜索提示 */
//        $('select[data-live-search="true"]').select2();
        /* 为选定的select下拉菜单开启搜索提示 END */

        /* 下面是表单里面的填写项提示相关的 */
        $('.yunsong').popover({
            content: '除指定地区外，其余地区的运费采用“默认运费”。至少要选择一项运送方式。',
            trigger: 'hover'
        });

        /* 点击展开运送方式设置 */
        $('.shipset .checkbox1 input').change(function () {
            $(".express_details").slideUp('fast');
            if ($(this).is(':checked')) {
                $(this).parent().parent().next().slideDown('fast');
            }
            else {
                $(this).parent().parent().next().slideUp('fast');
            }
        });

        /* 点击选中城市 */
        $('#getCitys').click(function () {
            var treeObj = $.fn.zTree.getZTreeObj('treeDemo');
            var nodes = treeObj.getCheckedNodes(true);
            var v = '';
            var tempCityId = '';
            for (var i = 0; i < nodes.length; i++) {
                if (!(nodes[i].isParent)) {
                    v += nodes[i].name + '、';
                    tempCityId += nodes[i].cityId + ',';
                }
            }

            if (tempCityId != '') {
                $('.city_choosed .city_choosed_p').html(v.substring(0, v.length - 1));
                $('#tempCity').val(tempCityId.substring(0, tempCityId.length - 1));
            } else {
                $('.city_choosed .city_choosed_p').html('');
                $('#tempCity').val('');
            }

            $("#cityForm").valid();
        });


        //模板保存按钮点击事件
        $('#freightForm #save').click(function () {
            $('#freightForm').dpValidate({
                clear: true
            });
            $('#freightForm').submit();
        });
        //模板表单校验方法
        $('#freightForm').dpValidate({
            validate: true,
            callback: function () {
                addFreight();
            }
        });
    });

    /* 保存该模板 */
    function addFreight() {
        var b = true;

        var a = 0;
        $("input[name='logComId']").each(function () {
            if (this.checked == true) {
                a++;
            }
        });
        if (a == 0) {
            $("#logcomTips").html('<label style="color: red">至少选择一种运送方式</label>');
            b = false && b;
        } else {
            $("#logcomTips").html('');
            b = true && b;
        }

        $("input[name='logComId']").each(function () {
            if (this.checked == true) {
                var $temp = $(this).parents(".checkbox1").next().find("div");
                var $tempcode = $(this).attr("codename");
                if (/^\d{1,}$/.test($temp.find("input[name='" + $tempcode + "_start']").val())) {
                    $temp.find("input[name='" + $tempcode + "_start']").removeClass("error");
                    b = true && b;
                } else {
                    $temp.find("input[name='" + $tempcode + "_start']").addClass("error");
                    b = false && b;
                }

                if (/^[0-9]+([.]{1}[0-9]+){0,1}$/.test($temp.find("input[name='" + $tempcode + "_postage']").val())) {
                    $temp.find("input[name='" + $tempcode + "_postage']").removeClass("error");
                    b = true && b;
                } else {
                    $temp.find("input[name='" + $tempcode + "_postage']").addClass("error");
                    b = false && b;
                }

                if (/^\d{1,}$/.test($temp.find("input[name='" + $tempcode + "_plus']").val())) {
                    $temp.find("input[name='" + $tempcode + "_plus']").removeClass("error");
                    b = true && b;
                } else {
                    $temp.find("input[name='" + $tempcode + "_plus']").addClass("error");
                    b = false && b;
                }


                if (/^[0-9]+([.]{1}[0-9]+){0,1}$/.test($temp.find("input[name='" + $tempcode + "_postageplus']").val())) {
                    $temp.find("input[name='" + $tempcode + "_postageplus']").removeClass("error");
                    b = true && b;
                } else {
                    $temp.find("input[name='" + $tempcode + "_postageplus']").addClass("error");
                    b = false && b;
                }


            }
        });


        if (b && num == 0) {
            num += 1;
            $.ajax({
                url: "<@ofbizUrl>editLogisticsTempleJson</@ofbizUrl>",
                type: "POST",
                data: $('#freightForm').serialize(),
                dataType: "json",
                success: function (data) {
                    if(data.status){
                        $.tipLayer("操作成功！");
                        window.location.href = "<@ofbizUrl>logisticsTemple</@ofbizUrl>";
                    }else{
                        num -= 1;
                        $.tipLayer(data.info);
                    }
                },
                error: function (data) {
                    $.tipLayer("操作失败！");
                }
            });
        }
    }

    function quxiao() {
        history.go(-1);
    <#--window.location.href = "<@ofbizUrl>logisticsTemple</@ofbizUrl>";-->
    }
</script>