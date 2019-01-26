<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>"
      type="text/css"/>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<style type="text/css">
    ul.ztree {
        margin-top: 10px;
        border: 1px solid #d2d6de;
        height: 250px;
        overflow-y: scroll;
        overflow-x: auto;
    }
</style>

<!-- 内容start -->
<div class="box box-info">
    <form id="BannerEditForm" class="form-horizontal">
        <div class="box-body">
            <div class="row">
                <div class="form-group col-sm-8" data-type="required" data-mark="广告图片">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>广告图片:</label>
                    <div class="col-sm-4">
                        <img alt="" src="/content/control/getImage?contentId=${parameters.contentId?if_exists}" id="img"
                             style="max-height: 100px;max-width: 200px;">
                        <input class="btn btn-primary" style="margin-left:5px;" type="button" id="uploadedFile" name="uploadedFile"
                               value="选择图片"/>
                        <input type="hidden" id="contentId" class="dp-vd" value="${parameters.contentId?if_exists}"/>
                        <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-sm-6">
                        <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 750*335px</div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-8">
                    <label class="col-sm-2 control-label">链接类型:</label>
                    <div class="col-sm-10">
                        <div style="overflow:hidden;margin-bottom: 10px;">
                            <select id="firstLinkType" class="form-control"
                                    style="width:200px;float: left;margin-right: 20px;">
                                <option value="">===请选择链接类型===</option>
                            <#if linkTypes?has_content && (linkTypes?size > 0)>
                                <#list linkTypes as linkType>
                                    <#if linkType.enumId !="FLT_CXLJ">
                                        <option value="${linkType.enumId}"><#if linkType.enumId="FLT_HDLJ">
                                            促销链接<#else> ${linkType.description}</#if></option>
                                    </#if>
                                </#list>
                            </#if>
                            </select>
                        </div>

                        <div id="selectViewDiv" style="margin-top: 10px;display:none;">
                            <span>已选择:</span>
                            <span id="selectName"
                                  style="margin-left: 10px;color: blue;cursor: pointer;">${parameters.linkName?if_exists}</span>
                            <input type="hidden" id="linkId" value="${parameters.linkId?if_exists}"/>
                        </div>

                    </div>
                </div>
            </div>

            <div class="row">
                <div id="linkDiv" class="form-group col-sm-8" style="display:none;">
                    <label class="col-sm-2 control-label">链接地址:</label>
                    <div class="col-sm-10">
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <input type="text" class="form-control" id="linkUrl"
                                   value="${parameters.linkUrl?if_exists}"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-8" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>
                    <div class="col-sm-10">
                        <div id="linkDiv" class="col-sm-5" style="padding-left: 0px;">
                            <input type="text" class="form-control dp-vd" id="sequenceId"
                                   value="${parameters.sequenceId?if_exists}"/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>

        <#--<div class="row">-->
        <#--<div class="form-group col-sm-8">-->
        <#--<label class="col-sm-2 control-label">适用范围:</label>-->
        <#--<div class="col-sm-10">-->
        <#--<div class="radio col-sm-12">-->
        <#--<#if parameters.applyScope?if_exists == 'Dining'>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" checked value="Dining">餐饮</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Relaxation">休闲</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Cinema">电影</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Hotel">酒店</label>-->
        <#--<#elseif parameters.applyScope?if_exists == 'Relaxation'>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Dining">餐饮</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio"  checked value="Relaxation">休闲</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Cinema">电影</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Hotel">酒店</label>-->
        <#--<#elseif parameters.applyScope?if_exists == 'Cinema'>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Dining">餐饮</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Relaxation">休闲</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio"  checked value="Cinema">电影</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Hotel">酒店</label>-->
        <#--<#elseif parameters.applyScope?if_exists == 'Hotel'>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Dining">餐饮</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Relaxation">休闲</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Cinema">电影</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" checked value="Hotel">酒店</label>-->
        <#--<#else>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Dining">餐饮</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Relaxation">休闲</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Cinema">电影</label>-->
        <#--<label class="col-sm-3"><input name="applyScope" type="radio" value="Hotel">酒店</label>-->
        <#--</#if>-->
        <#--</div>-->
        <#--</div>-->
        <#--</div>-->
        <#--</div>-->

            <div class="row">
                <div class="form-group col-sm-8">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用:</label>
                    <div class="col-sm-10">
                        <div class="radio col-sm-3">
                        <#if parameters.isUse?if_exists == '0'>
                            <label class="col-sm-6"><input name="isUse" type="radio" checked value="0">是</label>
                            <label class="col-sm-6"><input name="isUse" type="radio" value="1">否</label>
                        <#else>
                            <label class="col-sm-6"><input name="isUse" type="radio" value="0">是</label>
                            <label class="col-sm-6"><input name="isUse" type="radio" checked value="1">否</label>
                        </#if>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-8" data-type="required" data-mark="站点">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>
                    <div class="col-sm-10">
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <select id="webSite" class="form-control select2 dp-vd" multiple="multiple"
                                    data-placeholder="请选择站点">
                            <#assign webSiteList = delegator.findByAnd("WebSite",{"isEnabled":"Y"}) >
                            <#if webSiteList?has_content>
                                <#list webSiteList as webSite>
                                    <option value="${webSite.webSiteId}">${webSite.siteName}</option>
                                </#list>
                            </#if>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                        <div class="col-sm-3" style="padding-left: 0px;">
                            <div class="checkbox">
                                <label>
                                    <input id="isAllWebSite" type="checkbox">所有站点
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-8" data-type="required,linkLt" id="fromDateGroup"
                     data-compare-link="endGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="publishDate" class="col-sm-2 control-label"><i class="required-mark">*</i>开始时间</label>

                    <div class="input-group date form_datetime col-sm-4 p-l-15 p-r-15" data-link-field="fromDate">
                        <input id="fromDate1" class="form-control" size="16" type="text" value="${parameters.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}" readonly>
                        <input id="fromDate" class="dp-vd" type="hidden" name="fromDate" value="${parameters.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>
            </div>
            <div class="row">
                <div id="endGroup" class="form-group col-sm-8" data-type="required,linkGt"
                     data-compare-link="fromDateGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="thruDate" class="col-sm-2 control-label"><i class="required-mark">*</i>结束时间</label>
                    <div class="input-group date form_datetime col-sm-4 p-l-15 p-r-15" data-link-field="thruDate">
                        <input id="thruDate1" class="form-control" size="16" type="text" value="${parameters.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}" readonly>
                        <input id="thruDate" class="dp-vd" type="hidden" name="thruDate" value="${parameters.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

            </div>
        <#--<div class="row">
            <div class="form-group col-sm-8" data-type="required" data-mark="地区">
                <input type="hidden" id="geo" name="geo" class="dp-vd"/>
                <label class="col-sm-2 control-label"><i class="required-mark">*</i>地区:</label>
                <div class="col-sm-10">
                    <div class="col-sm-5" style="padding-left: 0px;">
                        <div class="zTreeDemoBackground left">
                            <ul id="add_geo_area" class="ztree"></ul>
                        </div>
                        <p id="c_error_msg" class="dp-error-msg"></p>
                    </div>
                    <div class="col-sm-3" style="padding-left: 0px;">
                        <div class="checkbox">
                            <label>
                                <input id="isAllGeo" type="checkbox">所有地区
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </div>-->

        </div><!-- box-body end -->

        <!-- 按钮组 -->
        <div class="box-footer text-center">
            <button id="save" type="button" class="btn btn-primary m-r-10">保存</button>
        </div>
    </form>
</div><!-- 内容end -->

<!-- script区域start -->
<script>

    // 链接类型初始化
    ;(function () {
        var linkType = '${parameters.firstLinkType?if_exists}'
        var linkUrl = '${parameters.linkUrl?if_exists}'

        $('#firstLinkType').val(linkType)

        switch (linkType) {
                // 自定义链接
            case 'FLT_ZDYLJ':
                $('#linkDiv').show();
                $('#selectViewDiv').hide();
                break;
            default:
                if (linkUrl) {
                    $('#selectViewDiv').show();
                    $('#linkDiv').hide();
                }
        }

    })()

    var select_id;
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
        //初始化站点选择框
        if ('${parameters.isAllWebSite?if_exists}' == '0') {
            $('#isAllWebSite').prop("checked", "true");
            $("#webSite").prop("disabled", true);
            $("#webSite").removeClass('dp-vd');
        } else {
            $("#webSite").val('${parameters.webSite?if_exists}'.split(',')).trigger("change");
        }

        //初始化地区选择框
    <#--if('${parameters.isAllGeo?if_exists}' == '0'){-->
    <#--$('#isAllGeo').prop("checked","true");-->
    <#--$("#geo").val('${parameters.geo?if_exists}');-->
    <#--}else{-->
    <#--$("#geo").val('${parameters.geo?if_exists}');-->
    <#--}-->

        //初始化select2
        $(".select2").select2({
            closeOnSelect: false
        });

        // 初始化图片选择
        $.chooseImage.int({
            userId: '',
            serverChooseNum: 1,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: ''
        });

        //图片保存按钮事件
        $('body').on('click', '.img-submit-btn', function () {
            var obj = $.chooseImage.getImgData();
            $.chooseImage.choose(obj, function (data) {
                $('#contentId').val(data.uploadedFile0);
                $('#img').attr({"src": "/content/control/getImage?contentId=" + data.uploadedFile0});
            })
        });

        // 图片选择控件显示
        $('#uploadedFile').click(function () {
            $.chooseImage.show();
        });

        // 链接类型选择链接回调处理
        var selectLinkHandler = function (type) {
            switch (type) {
                case 'FLT_WZLJ': {
                    $('#linkDiv').hide();
                    $.dataSelectModal({
                        url: "ArticleListModalPage",
                        width: "800",
                        title: "选择文章",
                        selectId: "linkId",
                        selectName: "selectName",
                        selectCallBack: function (el) {
                            $('#selectViewDiv').show();
                            $('#linkUrl').val('modalName=WZ&id=' + el.data('id'));
                        }
                    });
                }
                    break;
                case 'FLT_HDLJ': {
                    $('#linkDiv').hide();
                    $.dataSelectModal({
                        url: "/prodPromo/control/ProActivityMgrListModalPage?externalLoginKey=${externalLoginKey}",
                        width: "800",
                        title: "选择活动",
                        selectId: "linkId",
                        selectName: "selectName",
                        selectCallBack: function (el) {
                            $('#selectViewDiv').show();
                            $('#linkUrl').val('modalName=HD&id=' + el.data('id'));
                        }
                    });
                }
                    break;
            <#-- 自定义链接  -->
                case 'FLT_ZDYLJ': {
                    $('#selectViewDiv').hide();
                    $('#linkId').val('');
                    $('#selectName').html('');
                    $('#linkUrl').val('');
                    $('#linkDiv').show();
                }
                    break;
            <#-- 商品选择添加 -->
                case 'FLT_SPLJ': {
                    $('#linkDiv').hide();
                    $.dataSelectModal({
                        url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
                        width: "800",
                        title: "选择商品",
                        selectId: "linkId",
                        selectName: "selectName",
                        selectCallBack: function (el) {
                            $('#selectViewDiv').show();
                            $('#linkUrl').val('modalName=SP&id=' + el.data('id'));
                        }
                    });
                }
                    break;
                    // 促销类型
                case 'FLT_CXLJ': {
                    $('#linkDiv').hide();
                    $.dataSelectModal({
                        url: "/prodPromo/control/PromoListModalPage?externalLoginKey=${externalLoginKey}",
                        width: "800",
                        title: "选择促销",
                        selectId: "linkId",
                        selectName: "selectName",
                        selectCallBack: function (el) {
                            $('#selectViewDiv').show();
                            var modalName = el.data('record').activityType;
                            $('#linkUrl').val('modalName=' + modalName + '&id=' + el.data('id'));
                        }
                    });
                }
                default: {
                    $('#linkId').val('');
                    $('#selectName').html('');
                    $('#linkUrl').val('');
                    $('#linkDiv').hide();
                    $('#selectViewDiv').hide();
                }
            }
        }

        //已选的名称点击事件
        $('#selectName').on('click', function () {
            var flt = $('#firstLinkType').val()
            selectLinkHandler(flt)
        });

        //链接地址一的选项切换事件
        $('#firstLinkType').on('change', function () {
            var type = $(this).val()
            selectLinkHandler(type)
        });


        //所有站点的选中事件
        $('#isAllWebSite').change(function () {
            if ($(this).prop("checked")) {
                $("#webSite").val(null).trigger("change");
                $("#webSite").prop("disabled", true);
                $("#webSite").removeClass('dp-vd');
            } else {
                $("#webSite").prop("disabled", false);
                $("#webSite").addClass('dp-vd');
            }
        });

    <#--var setting = {-->
    <#--check: {-->
    <#--enable: true-->
    <#--},-->
    <#--data: {-->
    <#--simpleData: {-->
    <#--enable: true-->
    <#--}-->
    <#--},-->
    <#--view: {-->
    <#--showIcon: false-->
    <#--},-->
    <#--callback: {-->
    <#--onCheck: geoCheck-->
    <#--}-->
    <#--};-->

    <#--//异步加载所有地区数据-->
    <#--$.ajax({-->
    <#--url: "/facility/control/GetGeoList${externalKeyParam}",-->
    <#--type: "GET",-->
    <#--dataType: "json",-->
    <#--success: function (treeData) {-->
    <#--treeObj = $.fn.zTree.init($("#add_geo_area"), setting, treeData.geoList);-->
    <#--var geo = $('#BannerEditForm #geo').val();-->
    <#--//初始化社区选择框-->
    <#--if('${parameters.isAllGeo?if_exists}' == '0'){-->
    <#--$('#isAllGeo').prop("checked","true").trigger("change");-->
    <#--}else{-->
    <#--if(geo){-->
    <#--var geos = geo.split(',');-->
    <#--//自动勾选复选框-->
    <#--$.each(geos, function (i) {-->
    <#--var node = treeObj.getNodeByParam("id", geos[i]);-->
    <#--if(node ) {-->
    <#--treeObj.checkNode(node);-->
    <#--}-->
    <#--});-->
    <#--}-->
    <#--}-->
    <#--},-->
    <#--error: function (data) {-->
    <#--//设置提示弹出框内容-->
    <#--$('#modal_msg #modal_msg_body').html("网络异常！");-->
    <#--$('#modal_msg').modal();-->
    <#--}-->
    <#--});-->
    <#---->
    <#--//所有地区的选中事件-->
    <#--$('#isAllGeo').change(function(){-->
    <#--if($(this).prop("checked")){-->
    <#--treeObj.checkAllNodes(false);-->
    <#--var nodes = treeObj.getNodesByFilter(function (node) { return node.level == 0 });-->
    <#--for (var i=0, l=nodes.length; i < l; i++) {-->
    <#--treeObj.setChkDisabled(nodes[i], true, true, true);-->
    <#--}-->
    <#--$("#geo").removeClass('dp-vd');-->
    <#--$('#geo').val('');-->
    <#--}else{-->
    <#--var nodes = treeObj.getNodesByFilter(function (node) { return node.level == 0 });-->
    <#--for (var i=0, l=nodes.length; i < l; i++) {-->
    <#--treeObj.setChkDisabled(nodes[i], false, true, true);-->
    <#--}-->
    <#--treeObj.checkAllNodes(false);-->
    <#--$("#geo").addClass('dp-vd');-->
    <#--}-->
    <#--});-->

        //表单校验方法
        $('#BannerEditForm').dpValidate({
            validate: true,
            callback: function () {
                var contentId = $('#BannerEditForm #contentId').val();
                var firstLinkType = $('#BannerEditForm #firstLinkType').val();
                // var secondLinkType = $('#BannerEditForm #secondLinkType').val();
                var linkUrl = $('#BannerEditForm #linkUrl').val();
                var linkId = $('#BannerEditForm #linkId').val();
                var linkName = $('#BannerEditForm #selectName').html();
                var sequenceId = $('#BannerEditForm #sequenceId').val();
                var isUse = $('input[name="isUse"]:checked').val();
                var isAllWebSite = $('#BannerEditForm #isAllWebSite').is(':checked') ? 0 : 1;
                var webSite = $('#BannerEditForm #webSite').val() != null ? $('#BannerEditForm #webSite').val().join(',') : '';
//	    	var isAllGeo = $('#BannerEditForm #isAllGeo').is(':checked') ? 0 : 1;
//            var geo = $('#BannerEditForm #
//            var applyScope = $('input[name="applyScope"]:checked').val();


                //异步加载已选地区数据
                $.ajax({
                    url: "BannerEdit",
                    type: "POST",
                    data: {
                        bannerId: '${parameters.bannerId}',
                        contentId: contentId,
                        firstLinkType: firstLinkType,
                        // secondLinkType:secondLinkType,
                        linkUrl: linkUrl,
                        linkId: linkId,
                        linkName: linkName,
                        sequenceId: sequenceId,
                        isUse: isUse,
                        isAllWebSite: isAllWebSite,
                        webSite: webSite,
                        fromDate: $('#fromDate').val(),
                        thruDate: $('#thruDate').val()
//                        applyScope:applyScope
//					,
//						isAllGeo:isAllGeo,
//						geo:geo
                    },
                    dataType: "json",
                    success: function (data) {
                        $.tipLayer("操作成功！");
                        window.location = '<@ofbizUrl>BannerListPage</@ofbizUrl>';
                    },
                    error: function (data) {
                        $.tipLayer("操作失败！");
                    }
                });
            }
        });

        $('#save').click(function () {
            $("#BannerEditForm").dpValidate({
                clear: true
            });
            $("#BannerEditForm").submit();
        });

        //地区树点击事件
//    function geoCheck(event, treeId, treeNode){
//    	var nodes = treeObj.getCheckedNodes(true);
//    	var ids = "";
//    	for(var i=0;i<nodes.length;i++){
//    		ids += nodes[i].id;
//    		if(i < nodes.length -1){
//    			ids += ",";
//    		}
//    	}
//        $('#geo').val(ids);
//    }
    });
</script><!-- script区域end -->
