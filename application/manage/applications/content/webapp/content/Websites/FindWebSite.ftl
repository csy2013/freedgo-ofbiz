<#assign commonUrl = "FindWebSite?"+ paramList+"&" />
<!-- Main content --> 
<!-- 内容start -->

    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">${uiLabelMap.WebsiteNum}(${webSiteListSize?default("0")})</h3>
        </div>
        <div class="box-body">
            <!-- 条件查询start -->
            <form class="form-inline clearfix" role="form" action="<@ofbizUrl>findWebSiteByName</@ofbizUrl>">
                <div class="form-group w-p100">
                    <div class="input-group m-b-10">
                        <span class="input-group-addon">${uiLabelMap.WebsiteName}</span>
                        <input type="text" class="form-control" name="siteName1" placeholder="${uiLabelMap.WebsiteName}" value="${siteName1?if_exists}">
                    </div>

                    <div class="input-group pull-right">
                        <button class="btn btn-success btn-flat">搜索</button>
                    </div>
                </div>
            </form><!-- 条件查询end -->

            <!-- 分割线start -->
            <div class="cut-off-rule bg-gray"></div>
            <!-- 分割线end -->

            <!--工具栏start -->
        <div class="row m-b-10">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
        <#if security.hasEntityPermission("CONTENT_WEBSITE", "_CREATE", session)>
            <button id="btn_add" class="btn btn-primary">
                <i class="fa fa-plus"></i>${uiLabelMap.Add}
            </button>
        </#if>
                </div>
            </div><!-- 操作按钮组end -->
            
            <#if webSites?has_content >
                <#assign size=webSites.size()/>
            <!-- 列表当前分页条数start -->
                <div class="col-sm-6">
                    <div class="dp-tables_length">
                        <label>
                            ${uiLabelMap.displayPage}
                            <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                    onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="10" <#if viewSize==10>selected</#if>>10</option>
                                <option value="20" <#if viewSize==20>selected</#if>>20</option>
                                <option value="30" <#if viewSize==30>selected</#if>>30</option>
                                <option value="40" <#if viewSize==40>selected</#if>>40</option>
                            </select>
                            ${uiLabelMap.brandItem}
                        </label>
                    </div>
                </div><!-- 列表当前分页条数end -->
            </div><!-- 工具栏end -->
        
            
            <div class="row">
                <div class="col-sm-12">
                    <table id="example2" class="table table-bordered table-hover">
                        <thead>
                        <tr>
                            <!--<th><input type="checkbox"></th>-->
                            <th>${uiLabelMap.WebsiteName}</th>
                            <th>${uiLabelMap.WebsiteId}</th>
                            <th>${uiLabelMap.WebsiteHttp}</th>
                            <th>${uiLabelMap.WebsiteTitle}</th>
                            <th>${uiLabelMap.Remark}</th>
                            <th>${uiLabelMap.IsEnabled}</th>
                            <th>${uiLabelMap.Operation}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list webSites as webSites>
                        <tr class="gss">
                            <td>${webSites.siteName?if_exists}</td>
                            <td>${webSites.webSiteId?if_exists}</td>
                            <td>${webSites.httpHost?if_exists}</td>
                            <td>${webSites.siteTitle?if_exists}</td>
                            <td>${webSites.siteRemark?if_exists}</td>
                            <td>
                             <#if webSites.isEnabled?has_content&&webSites.isEnabled.equals('Y')>
                             <#if security.hasEntityPermission("CONTENT_WEBSITE", "_UPDATE", session)>
                             <button class="gss_btn btn btn-primary">${uiLabelMap.Y}</button>
                             <#else>
                                 ${uiLabelMap.Y}
                             </#if>
                             <#else>
                                 <#if security.hasEntityPermission("CONTENT_WEBSITE", "_UPDATE", session)>
                             <button class="gss_btn btn btn-default">${uiLabelMap.N}</button>
                                 <#else>
                                     ${uiLabelMap.N}
                                 </#if>
                             </#if>
                            </td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("CONTENT_WEBSITE", "_UPDATE", session)>
                                    <button type="button" class="js-button btn btn-danger btn-sm" data-toggle="modal" data-target="#editexampleModal">${uiLabelMap.Edit}</button>
                                    <#else>
                                    <button type="button" disabled class="js-button btn btn-danger btn-sm" data-toggle="modal"
                                            data-target="#editexampleModal">${uiLabelMap.Edit}</button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                        <#if size gt 1>
                                            <#if webSites_index=0>
                                         <ul class="dropdown-menu" role="menu">
                                             <!-- <li><a href="<@ofbizUrl>deleteWebSite</@ofbizUrl>?deleteId=${webSites.webSiteId?if_exists}">${uiLabelMap.Delete}</a></li>-->
                                             <li><a href="#" class="gss_Down">${uiLabelMap.MoveDown}</a></li>
                                         </ul>
                                            <#elseif webSites_index=size-1>
                                         <ul class="dropdown-menu" role="menu">
                                             <!-- <li><a href="<@ofbizUrl>deleteWebSite</@ofbizUrl>?deleteId=${webSites.webSiteId?if_exists}">${uiLabelMap.Delete}</a></li>-->
                                             <li><a href="#" class="gss_Up">${uiLabelMap.Moveup}</a></li>
                                         </ul>
                                            <#elseif webSites_index gt 0&& webSites_index lt size-1 >
                                          <ul class="dropdown-menu" role="menu">
                                              <!-- <li><a href="<@ofbizUrl>deleteWebSite</@ofbizUrl>?deleteId=${webSites.webSiteId?if_exists}">${uiLabelMap.Delete}</a></li>-->
                                              <li><a href="#" class="gss_Up">${uiLabelMap.Moveup}</a></li>
                                              <li><a href="#" class="gss_Down">${uiLabelMap.MoveDown}</a></li>
                                          </ul>
                                            </#if>
                                        <#else>
                                        </#if>
                                </div>
                            </td>
                            <!--关键字-->
                            <input type="hidden" name="siteKeyword" value="${webSites.siteKeyword?if_exists}"/>
                            <!--简介-->
                            <input type="hidden" name="siteAbstract" value="${webSites.siteAbstract?if_exists}"/>
                            <!--介绍-->
                            <input type="hidden" name="siteDescription" value="${webSites.siteDescription?if_exists}"/>
                            <!--店铺id-->

                            <!--action-->
                            <input type="hidden" name="action" value="<@ofbizUrl>updateWebSite</@ofbizUrl>"/>
                            <input type="hidden" name="isEnabled" value="${webSites.isEnabled}"/>
                        </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </div><!-- 表格区域end -->
            <!-- 分页条start -->
                <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
                <#assign viewIndexFirst = 0/>
                <#assign viewIndexPrevious = viewIndex - 1/>
                <#assign viewIndexNext = viewIndex + 1/>
                <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(webSiteListSize, viewSize) />
                <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", webSiteListSize)/>
                <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
                <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
                listSize=webSiteListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
                pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
                paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
        <!-- 分页条end -->
            <#else>
        <div id="noData" class="col-sm-12">
            <h3>${uiLabelMap.webSiteNoData}</h3>
        </div>
            </#if>
        </div><!-- /.box-body -->
    </div>

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.FacilityOk}</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->

<!-- 添加编辑弹出框  start-->
<div class="modal fade" id="add_Modal" tabindex="-1" role="dialog" aria-labelledby="Add_Website">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.WebsiteEdit}</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="addForm" action="" method="post">
                    <div class="row">
                        <div class="form-group">
                            <label for="recipient-name" class="control-label col-sm-2">${uiLabelMap.WebsiteId}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control w-p60" id="webSiteId" name="webSiteId" value="" readonly>
                            </div>
                        </div>
                    </div>
                    <!--站点名称-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="${uiLabelMap.WebsiteName}">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.WebsiteName}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p60" id="siteName" name="siteName">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!--站点地址-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="${uiLabelMap.WebsiteHttp}">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.WebsiteHttp}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p60" id="httpHost" name="httpHost">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!--站点标题-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="${uiLabelMap.WebsiteTitle}">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.WebsiteTitle}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p60" id="siteTitle" name="siteTitle">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group">
                            <label for="title" class="control-label col-sm-2">${uiLabelMap.WebsiteKeyWord}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control w-p60" id="siteKeyword" name="siteKeyword">
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group">
                            <label for="message-text" class="control-label col-sm-2">${uiLabelMap.WebsiteAbstract}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control w-p60" id="siteAbstract" name="siteAbstract">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group">
                            <label for="message-text" class="control-label col-sm-2">${uiLabelMap.WebsiteDescription}:</label>
                            <div class="col-sm-10">
                                <textarea class="form-control w-p80" id="siteDescription" name="siteDescription"></textarea>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group">
                            <label for="message-text" class="control-label col-sm-2">${uiLabelMap.Remark}:</label>
                            <div class="col-sm-10">
                                <textarea class="form-control w-p80" id="siteRemark" name="siteRemark"></textarea>
                            </div>
                        </div>
                    </div>

                    <!-- radio -->
                    <div class="form-group">
                        <label for="message-text" class="control-label col-sm-2">${uiLabelMap.IsEnabled}:</label>
                        <div class="radio">
                            <label>
                                <input type="radio" name="isEnabled" id="optionsRadios1" value="Y" class="js-Y" checked>
                            ${uiLabelMap.Y}
                            </label>
                            <label>
                                <input type="radio" name="isEnabled" id="optionsRadios2" value="N" class="js-N">
                            ${uiLabelMap.N}
                            </label>
                        </div>
                    </div>
                    <input  type="hidden"  name="productStoreId" value="${requestAttributes.productStoreId?if_exists}"/>
            </div>
            <div class="modal-footer">
                <button type="button" id="save" class="btn btn-primary">${uiLabelMap.Save}</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
            </div>
            </form>
        </div>
    </div>
</div>
<!--添加编辑弹出框  end-->


<!-- 编辑弹出框  start-->
<div class="modal fade" id="editexampleModal" tabindex="-1" role="dialog" aria-labelledby="Edit_WebSite">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.WebsiteEdit}</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" action="" id="editForm" method="post">
                    <div class="row">
                        <div class="form-group">
                            <label for="recipient-name" class="control-label col-sm-2">${uiLabelMap.WebsiteId}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control w-p60" name="webSiteId" value="" readonly>
                            </div>
                        </div>
                    </div>
                    <!--站点名称-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="${uiLabelMap.WebsiteName}">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.WebsiteName}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p60" id="siteName" name="siteName">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!--站点地址-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="${uiLabelMap.WebsiteHttp}">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.WebsiteHttp}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p60" id="httpHost" name="httpHost">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!--站点标题-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="${uiLabelMap.WebsiteTitle}">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.WebsiteTitle}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p60" id="siteTitle" name="siteTitle">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group">
                            <label for="message-text" class="control-label col-sm-2">${uiLabelMap.WebsiteKeyWord}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control w-p60" name="siteKeyword">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group">
                            <label for="message-text" class="control-label col-sm-2">${uiLabelMap.WebsiteAbstract}:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control w-p60" name="siteAbstract">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group">
                            <label for="message-text" class="control-label col-sm-2">${uiLabelMap.WebsiteDescription}:</label>
                            <div class="col-sm-10">
                                <textarea class="form-control w-p80" name="siteDescription"></textarea>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group">
                            <label for="message-text" class="control-label col-sm-2">${uiLabelMap.Remark}:</label>
                            <div class="col-sm-10">
                                <textarea class="form-control w-p80" name="siteRemark"></textarea>
                            </div>
                        </div>
                    </div>

                    <!-- radio -->
                    <div class="form-group">
                        <label for="message-text" class="control-label col-sm-2">${uiLabelMap.IsEnabled}:</label>
                        <div class="radio">
                            <label>
                                <input type="radio" name="isEnabled" value="Y" class="js-Y" checked>
                            ${uiLabelMap.Y}
                            </label>
                            <label>
                                <input type="radio" name="isEnabled" value="N" class="js-N">
                            ${uiLabelMap.N}
                            </label>
                        </div>
                    </div>
                    <input type="hidden"  name="productStoreId" value="${requestAttributes.productStoreId?if_exists}"/>
            </div>
            <div class="modal-footer">
                <button type="button" id="edit" class="btn btn-primary">${uiLabelMap.Save}</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
            </div>
            </form>
        </div>
    </div>
</div>
<!--编辑弹出框  end-->
<script>
    $(function () {
        //添加按钮点击事件
        $('#btn_add').click(function () {
            $("#add_Modal #webSiteId").val('');
            $("#add_Modal #siteName").val('');
            $("#add_Modal #httpHost").val('');
            $("#add_Modal #siteTitle").val('');
            $("#add_Modal #siteKeyword").val('');
            $("#add_Modal #siteAbstract").val('');
            $("#add_Modal #siteDescription").val('');
            $("#add_Modal #siteRemark").val('');
            $(" #add_Modal input[name=isEnabled]:eq(0)").attr("checked", 'checked');
            $('#add_Modal').modal('show');
        });

        //编辑点击事件
        $('.js-button').on('click', function () {
            var $model = $('#editexampleModal');
            var tr = $(this).closest('tr');
            var webSiteId = tr.find('td').eq(1);//网站编号
            var siteName = tr.find('td').eq(0);//网站名称
            var httpHost = tr.find('td').eq(2);//网站地址
            var siteTitle = tr.find('td').eq(3);//网站标题
            var siteRemark = tr.find('td').eq(4);//网站备注
            var siteKeyword = tr.find('input[name=siteKeyword]');//关键字
            var siteAbstract = tr.find('input[name=siteAbstract]');//简介
            var siteDescription = tr.find('input[name=siteDescription]');//介绍
            var isEnabled = tr.find('input[name=isEnabled]').val();//是否启用
            var action = tr.find('input[name=action]');
            $model.find('input[name=webSiteId]').val(webSiteId.text());//网站编号
            $model.find('input[name=siteName]').val(siteName.text());//网站名称
            $model.find('input[name=httpHost]').val(httpHost.text());//网站地址
            $model.find('input[name=siteTitle]').val(siteTitle.text());//网站标题
            $model.find('input[name=siteKeyword]').val(siteKeyword.val());//关键字
            $model.find('input[name=siteAbstract]').val(siteAbstract.val());//简介
            $model.find('textarea[name=siteDescription]').val(siteDescription.val());//介绍
            $model.find('textarea[name=siteRemark]').val(siteRemark.text());//网站备注
            $model.find('form').attr('action', action.val());
            $('.js-' + isEnabled).prop('checked', true);//启用状态


        })
        //新增弹出窗关闭事件
        $('#add_Modal').on('hide.bs.modal', function () {
            $('#addForm').dpValidate({
                clear: true
            });
        })
        //添加提交按钮点击事件
        $('#save').click(function () {
            $('#addForm').dpValidate({
                clear: true
            });
            $('#addForm').submit();
        });

        //addForm编辑表单校验
        $('#addForm').dpValidate({
            validate: true,
            callback: function () {
                $.ajax({
                    url: "createWebSite",
                    type: "POST",
                    data: $('#addForm').serialize(),
                    dataType: "json",
                    success: function (data) {
                        //隐藏新增弹出窗口
                        $('#add_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal('show');
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.href = '<@ofbizUrl>FindWebSite</@ofbizUrl>';
                        })
                    },
                    error: function (data) {
                        //隐藏新增弹出窗口
                        $('#add_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });


        //编辑关闭事件
        $('#editexampleModal').on('hide.bs.modal', function () {
            $('#editForm').dpValidate({
                clear: true
            });
        })

        //编辑提交按钮点击事件
        $('#edit').click(function () {
            $('#editForm').dpValidate({
                clear: true
            });
            $('#editForm').submit();
        });

        //editForm编辑表单校验
        $('#editForm').dpValidate({
            validate: true,
            callback: function () {
                $.ajax({
                    url: "updateWebSite",
                    type: "POST",
                    data: $('#editForm').serialize(),
                    dataType: "json",
                    success: function (data) {
                        //隐藏新增弹出窗口
                        $('#editexampleModal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal('show');
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.href = '<@ofbizUrl>FindWebSite</@ofbizUrl>';
                        })
                    },
                    error: function (data) {
                        //隐藏新增弹出窗口
                        $('#editexampleModal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

//更改站点状态
        $('.gss').on('click', ".gss_btn", function () {
            var tr = $(this).closest('tr');
            var webSiteId = tr.find('td').eq(1).text();//网站编号
            var isEnabled = tr.find('input[name=isEnabled]').val();//是否启用
            $.ajax({
                url: '<@ofbizUrl>editSiteStatus</@ofbizUrl>',
                type: 'post',
                dataType: 'json',
                data: {
                    'webSiteId': webSiteId,
                    'isEnabled': isEnabled
                },
                success: function (data) {
                    if (data.status) {
                        if (data.isEnabled == 'Y') {
                            var Y = '<button  class="gss_btn btn btn-primary" >${uiLabelMap.Y}</button>';
                            tr.find('td').eq(5).html(Y);
                            tr.find('input[name=isEnabled]').val(data.isEnabled);
                        }
                        if (data.isEnabled == 'N') {
                            var N = '<button  class="gss_btn btn btn-default" >${uiLabelMap.N}</button>';
                            tr.find('td').eq(5).html(N);
                            tr.find('input[name=isEnabled]').val(data.isEnabled);
                        }
                    }
                }
            })
        })

//下移站点
        $('.gss_Down').on('click', function () {
            var tr = $(this).closest('tr');
            var webSiteId = tr.find('td').eq(1).text();//网站编号
            var nexttr = $(this).closest('tr').next();
            var nextwebSiteId = nexttr.find('td').eq(1).text();//网站编号
            $.ajax({
                url: '<@ofbizUrl>moveWebSite</@ofbizUrl>',
                type: 'post',
                dataType: 'json',
                data: {
                    'webSiteId': webSiteId,
                    'nextwebSiteId': nextwebSiteId
                },
                success: function (data) {
                    window.location.href = '<@ofbizUrl>findWebSiteByName</@ofbizUrl>';
                }
            })
        })

//上移站点
        $('.gss_Up').on('click', function () {
            var tr = $(this).closest('tr');
            var webSiteId = tr.find('td').eq(1).text();//网站编号
            var nexttr = $(this).closest('tr').prev();
            var nextwebSiteId = nexttr.find('td').eq(1).text();//网站编号
            $.ajax({
                url: '<@ofbizUrl>moveWebSite</@ofbizUrl>',
                type: 'post',
                dataType: 'json',
                data: {
                    'webSiteId': webSiteId,
                    'nextwebSiteId': nextwebSiteId
                },
                success: function (data) {
                    window.location.href = '<@ofbizUrl>findWebSiteByName</@ofbizUrl>';
                }
            })
        })

    })
</script>

<style>
    .dp_id_b {
        height: 83px;
        overflow: auto;
        border: 1px solid #ccc;
        margin-left: 15px;
    }
</style>
