<style>

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
</style>
<div class="box box-info">
    <div class="box-body">
        <h3 class="">物流模板设置
        </h3>
        <div class="cut-off-rule bg-gray"></div>

        <!--工具栏start -->
        <div class="row m-b-10">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                <#if security.hasEntityPermission("LOGISTICS_TEMPLE", "_CREATE", session)>
                    <button class="btn btn-primary glyphicon glyphicon-plus" onclick="location.href='<@ofbizUrl>addLogisticsTemple</@ofbizUrl>'">添加物流模板</button>
                </#if>
                </div>
            </div><!-- 操作按钮组end -->
        </div><!-- 工具栏end -->

        <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
        <#assign index = 0>
        <#if defaultLogisticsTemples?has_content>
            <#list defaultLogisticsTemples as logisticsTemple>
                <#assign index = index+1>
                <div class="panel panel-default">
                    <div class="panel-heading" role="tab" id="headingOne_${index}">
                        <h4 class="panel-title">
                            <div class="pull-right">
                            <#--<#if security.hasEntityPermission("LOGISTICS_TEMPLE", "_COPY", session)>-->
                                <#--<a href="javascript:void(0);" onclick="copytemp('${logisticsTemple.logisticsTempleId}');">复制</a>-->
                            <#--</#if>-->
                            <#if security.hasEntityPermission("LOGISTICS_TEMPLE", "_UPDATE", session)>
                                <a href="javascript:void(0);" onclick="toupdate('${logisticsTemple.logisticsTempleId}')">编辑</a>
                            </#if>
                            <#if security.hasEntityPermission("LOGISTICS_TEMPLE", "_DELETE", session)>
                                <a href="javascript:void(0);" onclick="setDelete('${logisticsTemple.logisticsTempleId}');">删除</a>
                            </#if>
                            </div>
                            <span class="pull-right mr20">最后编辑时间：${logisticsTemple.lastUpdatedStamp?string("yyyy-MM-dd HH:mm:ss")}</span>
                            <a class="collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapseOne_${index}" aria-expanded="false" aria-controls="collapseOne_${index}">
                                <#assign logisticsCompany = delegator.findByPrimaryKey("LogisticsCompany",{"id":logisticsTemple.logisticsCompanyId})>
                            ${logisticsTemple.logisticsTempleName?if_exists}
                                (默认模板)
                            </a>
                        </h4>
                    </div>
                    <div style="height: 0px;" aria-expanded="false" id="collapseOne_${index}" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingOne_${index}">
                        <div class="panel-body p20">
                            <h5>运送方式：${logisticsCompany.companyName?if_exists}</h5>
                            <table class="table table-hover table-bordered">
                                <thead>
                                <tr>
                                    <th width="30%">运送至</th>
                                    <th>首<#if logisticsTemple.logisticsMethods == "0">件(个)<#else>重(g)</#if></th>
                                    <th>运费(元)</th>
                                    <th>续<#if logisticsTemple.logisticsMethods == "0">件(个)<#else>重(g)</#if></th>
                                    <th>运费(元)</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#assign logisticsTempleItems = delegator.findByAnd("LogisticsTempleItem",{"logisticsTempleId":logisticsTemple.logisticsTempleId})>
                                    <#list logisticsTempleItems as logisticsTempleItem>
                                        <#if !logisticsTempleItem.areas?has_content || logisticsTempleItem.areas == "">
                                        <tr>
                                            <td>全国</td>
                                            <td>${logisticsTempleItem.start}</td>
                                            <td>${logisticsTempleItem.postage}</td>
                                            <td>${logisticsTempleItem.plus}</td>
                                            <td>${logisticsTempleItem.postagePlus}</td>
                                        </tr>
                                        <#else>
                                        <tr>
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
                                            <td><div class="express_area"><p>${areaName}</p></td>
                                            <td>${logisticsTempleItem.start}</td>
                                            <td>${logisticsTempleItem.postage}</td>
                                            <td>${logisticsTempleItem.plus}</td>
                                            <td>${logisticsTempleItem.postagePlus}</td>
                                        </tr>
                                        </#if>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </#list>
        </#if>
        <#if logisticsTemples?has_content>
            <#list logisticsTemples as logisticsTemple>
            <#assign index = index+1>
            <#assign defaultLogisticsTemple = delegator.findByAnd("DefaultLogisticsTemple",{"logisticsTempleId":logisticsTemple.logisticsTempleId,"isDefault":"Y"})>
            <div class="panel panel-default">
                <div class="panel-heading" role="tab" id="headingOne_${index}">
                    <h4 class="panel-title">
                        <div class="pull-right">
                            <#--<#if security.hasEntityPermission("LOGISTICS_TEMPLE", "_COPY", session)>-->
                                <#--<a href="javascript:void(0);" onclick="copytemp('${logisticsTemple.logisticsTempleId}');">复制</a>-->
                            <#--</#if>-->
                            <#if security.hasEntityPermission("LOGISTICS_TEMPLE", "_UPDATE", session)>
                                <a href="javascript:void(0);" onclick="toupdate('${logisticsTemple.logisticsTempleId}')">编辑</a>
                            </#if>
                            <#if security.hasEntityPermission("LOGISTICS_TEMPLE", "_DELETE", session)>
                                <a href="javascript:void(0);" onclick="setDelete('${logisticsTemple.logisticsTempleId}');">删除</a>
                            </#if>
                            <#if security.hasEntityPermission("LOGISTICS_TEMPLE", "_UPDATE", session) && !defaultLogisticsTemple?has_content>
                            <a href="javascript:void(0);" onclick="setDefault('${logisticsTemple.logisticsTempleId}');">设为默认</a>
                            </#if>
                        </div>
                        <span class="pull-right mr20">最后编辑时间：${logisticsTemple.lastUpdatedStamp?string("yyyy-MM-dd HH:mm:ss")}</span>
                        <a class="collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapseOne_${index}" aria-expanded="false" aria-controls="collapseOne_${index}">
                            <#assign logisticsCompany = delegator.findByPrimaryKey("LogisticsCompany",{"id":logisticsTemple.logisticsCompanyId})>
                            ${logisticsTemple.logisticsTempleName?if_exists}
                            <#if defaultLogisticsTemple?has_content>（默认模板）</#if>
                        </a>
                    </h4>
                </div>
                <div style="height: 0px;" aria-expanded="false" id="collapseOne_${index}" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingOne_${index}">
                    <div class="panel-body p20">
                        <h5>运送方式：${logisticsCompany.companyName?if_exists}</h5>
                        <table class="table table-hover table-bordered">
                            <thead>
                            <tr>
                                <th width="30%">运送至</th>
                                <th>首<#if logisticsTemple.logisticsMethods == "0">件(个)<#else>重(g)</#if></th>
                                <th>运费(元)</th>
                                <th>续<#if logisticsTemple.logisticsMethods == "0">件(个)<#else>重(g)</#if></th>
                                <th>运费(元)</th>
                            </tr>
                            </thead>
                            <tbody>
                            <#assign logisticsTempleItems = delegator.findByAnd("LogisticsTempleItem",{"logisticsTempleId":logisticsTemple.logisticsTempleId})>
                            <#list logisticsTempleItems as logisticsTempleItem>
                                <#if !logisticsTempleItem.areas?has_content || logisticsTempleItem.areas == "">
                                <tr>
                                    <td>全国</td>
                                    <td>${logisticsTempleItem.start}</td>
                                    <td>${logisticsTempleItem.postage}</td>
                                    <td>${logisticsTempleItem.plus}</td>
                                    <td>${logisticsTempleItem.postagePlus}</td>
                                </tr>
                                <#else>
                                <tr>
                                    <#assign areas = logisticsTempleItem.areas?split(",")>
                                    <#assign areaName = "">
                                    <#list areas as area>
                                        <#assign geo = delegator.findByPrimaryKey("Geo",{"geoId":area})>
                                        <#if geo?has_content>
                                            <#if areaName == "">
                                                <#assign areaName = geo.geoName>
                                            <#else>
                                                <#assign areaName = areaName + "、"  + geo.geoName>
                                            </#if>
                                        </#if>
                                    </#list>
                                    <td><div class="express_area"><p>${areaName}</p></div></td>
                                    <td>${logisticsTempleItem.start}</td>
                                    <td>${logisticsTempleItem.postage}</td>
                                    <td>${logisticsTempleItem.plus}</td>
                                    <td>${logisticsTempleItem.postagePlus}</td>
                                </tr>
                                </#if>
                            </#list>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            </#list>
        </#if>
        </div>
    </div>
</div>
<!-- 显示或隐藏确认弹出框start -->
<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title"></h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div><!-- 显示或隐藏确认弹出框end -->
<script>
    var logisticsTempleId;
    $(function (){
        //删除确定按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用显示方法
            $.ajax({
                url: "deleteLogisticsTemple",
                type: "GET",
                data: {logisticsTempleId : logisticsTempleId},
                dataType : "json",
                success: function(data){
                    $.tipLayer("删除成功！");
                    window.location.href = "<@ofbizUrl>logisticsTemple</@ofbizUrl>";
                },
                error: function(data){
                    $.tipLayer("删除失败！");
                }
            });
        });
    });
    //复制
    function copytemp(id){
        $.ajax({
            url: "copyLogisticsTemple",
            type: "GET",
            data: {logisticsTempleId : id},
            dataType : "json",
            success: function(data){
                $.tipLayer("操作成功！");
                window.location.href = "<@ofbizUrl>logisticsTemple</@ofbizUrl>";
            },
            error: function(data){
                $.tipLayer("操作失败！");
            }
        });
    }
    //编辑
    function toupdate(id){
        window.location.href = "<@ofbizUrl>editLogisticsTemple</@ofbizUrl>?logisticsTempleId="+id;
    }
    //删除
    function setDelete(id){
        logisticsTempleId = id;
        //设置提示弹出框内容
        $('#modal_confirm #modal_confirm_title').html("删除模板");
        $('#modal_confirm #modal_confirm_body').html("确定要删除此模板吗？");
        $('#modal_confirm').modal('show');
    }
    //设为默认
    function setDefault(id){
        $.ajax({
            url: "setDefaultLogisticsTemple",
            type: "GET",
            data: {logisticsTempleId : id},
            dataType : "json",
            success: function(data){
                $.tipLayer("操作成功！");
                window.location.href = "<@ofbizUrl>logisticsTemple</@ofbizUrl>";
            },
            error: function(data){
                $.tipLayer("操作失败！");
            }
        });
    }
</script>