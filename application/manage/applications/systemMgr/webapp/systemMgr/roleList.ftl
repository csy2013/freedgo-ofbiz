<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "roleList?lookupFlag=Y"+ paramList +"&">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">${uiLabelMap.searchCondition}</h3>
    </div>
<#--${roleList}-->
    <div class="box-body">
        <form class="form-inline clearfix" role="form" method="post" action="<@ofbizUrl>roleList</@ofbizUrl>">
            <input type="hidden" name="lookupFlag" value="Y">

            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.roleId}</span>
                    <input type="text" class="form-control" name="groupId" placeholder="${uiLabelMap.roleId}" value="${groupId?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.roleName}</span>
                    <input type="text" class="form-control" name="name" placeholder="${uiLabelMap.roleName}" value="${name?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.description}</span>
                    <input type="text" class="form-control" name="description" placeholder="${uiLabelMap.description}" value="${description?default("")}">
                </div>
            </div>
            <div class="input-group pull-right">
                <button class="btn btn-success btn-flat">${uiLabelMap.CommonView}</button>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
        <div class="row m-b-10">
            <div class="col-sm-6">
            <div class="dp-tables_btn">
            <#if security.hasEntityPermission("SYSTEMMGR_GROUP", "_CREATE", session)>
                <button class="btn btn-primary" onclick="addRole()" ;>
                    <i class="fa fa-plus"></i>
                ${uiLabelMap.CommonAdd}
                </button>
            </#if>
            <#if security.hasEntityPermission("SYSTEMMGR_GROUP", "_DELETE", session)>
                <button id="btn_del" class="btn btn-primary">
                    <i class="fa fa-trash"></i>删除
                </button>
            </div>
            </#if>
            </div>
        <#assign commonUrl1 = commonUrl+"ORDER_FILED=${orderFiled}&amp;ORDER_BY=${orderBy}&amp;"/>
        <#if roleList?has_content>

            <div class="col-sm-6">
                <div class="dp-tables_length">
                    <label>
                        每页显示
                        <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                onchange="location.href='${commonUrl1}VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="20" <#if viewSize ==20>selected</#if>>20</option>
                            <option value="30" <#if viewSize==30>selected</#if>>30</option>
                            <option value="50" <#if viewSize==50>selected</#if>>50</option>
                            <option value="100" <#if viewSize==100>selected</#if>>100</option>
                        </select>
                        条
                    </label>
                </div>

            </div>
        </#if>
        </div>

    <#if roleList?has_content>
        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>${uiLabelMap.roleId}
                            <#if orderFiled == 'roleId'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=roleId&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=roleId&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=roleId&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>${uiLabelMap.roleName}
                            <#if orderFiled == 'name'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted  fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>${uiLabelMap.description}
                            <#if orderFiled == 'description'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=description&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=description&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=description&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>${uiLabelMap.systemMgrAction}</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list roleList as partyRow>

                        <tr>
                            <td><input value="${partyRow.groupId?if_exists}" class="js-checkchild" type="checkbox"></td>
                            <td><#if partyRow.containsKey("groupId")>${partyRow.groupId?default("N/A")}</#if></td>
                            <td><#if partyRow.containsKey("name")>${partyRow.name?default("N/A")}</#if></td>
                            <td><#if partyRow.containsKey("description")>${partyRow.description?default("N/A")}</#if></td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("SYSTEMMGR_GROUP", "_DETAIL", session)>
                                        <button type="button" class="btn btn-danger btn-sm" onclick="roleDetail('${partyRow.groupId?default("N/A")}')">查看</button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                <ul class="dropdown-menu" role="menu">
                                    <#if security.hasEntityPermission("SYSTEMMGR_GROUP", "_UPDATE", session)>
                                        <#--<#if partyRow.groupId?if_exists!="SUPPLIER">-->
                                            <li><a href="#" onclick="editRole('${partyRow.groupId?default("N/A")}')">编辑</a></li>
                                        <#--</#if>-->
                                    </#if>
                                    <#if security.hasEntityPermission("SYSTEMMGR_GROUP", "_DELETE", session)>
                                        <li><a href="#" onclick="delRole('${partyRow.groupId?default("N/A")}')">删除</a></li>
                                    </ul>
                                    </#if>
                                </div>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(roleListSize, viewSize) />

        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", roleListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl1 ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=roleListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />

    <#else>
        <div class="row">
            <div class="col-sm-10">
                <h3>${uiLabelMap.UserNoPartiesFound}</h3>
            </div>
        </div>
    </#if>
    </div>
    <!-- /.box-body -->
</div>




<!-- add user Modal -->
<div id="modal_add" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modal_add_title">>
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">${uiLabelMap.addRoleAction}</h4>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal" id="addForm" method="post" action="<@ofbizUrl>addRole</@ofbizUrl>">
                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.roleId}">
                        <label for="groupId" class="control-label col-sm-2"><span class="text-danger">*</span>${uiLabelMap.roleId}:</label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" name="groupId" id="groupId">

                            <p class="dp-error-msg"></p>
                        </div>

                    </div>

                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.roleName}">
                        <label for="name" class="control-label col-sm-2"><span class="text-danger">*</span>${uiLabelMap.roleName}:</label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" name="name" id="name">

                            <p class="dp-error-msg"></p>
                        </div>

                    </div>

                    <div class="form-group">
                        <input type="hidden" id="add_permissionIds" name="permissionIds"/>
                        <label class="control-label col-sm-2"><span style="color: red">*</span>${uiLabelMap.permissionId}:</label>

                        <div class="col-sm-10">
                            <div class="zTreeDemoBackground left">
                                <ul id="add_permission_area" class="ztree"></ul>
                            </div>
                        </div>
                    </div>

                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.roleDesc}">
                        <label for="description" class="control-label col-sm-2"><span class="text-danger">*</span>${uiLabelMap.roleDesc}:</label>

                        <div class="col-sm-10">
                            <textarea class="form-control dp-vd" name="description" id="description"></textarea>

                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                </form>
            </div>
            <div class="modal-footer">

                <button type="button" class="btn btn-primary" onclick="saveRole();">保存</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>

<#--修改管理员-->
<div id="modal_edit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_edit_title">${uiLabelMap.editRoleAction}</h4>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal" id="editForm" method="post" action="<@ofbizUrl>updateRole</@ofbizUrl>">

                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.roleId}">
                        <label for="groupId" class="control-label col-sm-2"><span class="text-danger">*</span>${uiLabelMap.roleId}:</label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" name="groupId" id="edit_groupId">

                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.roleName}">
                        <label for="edit_name" class="control-label col-sm-2"><span class="text-danger">*</span>${uiLabelMap.roleName}:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" name="name" id="edit_name">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group">
                        <input type="hidden" id="edit_permissionIds" name="permissionIds"/>
                        <label class="control-label col-sm-2"><span style="color: red">*</span>${uiLabelMap.permissionId}:</label>

                        <div class="col-sm-10">
                            <div class="zTreeDemoBackground left">
                                <ul id="edit_permission_area" class="ztree"></ul>
                            </div>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.roleDesc}">
                        <label for="edit_description" class="control-label col-sm-2"><span class="text-danger">*</span>${uiLabelMap.roleDesc}:</label>

                        <div class="col-sm-10">
                            <textarea type="text" class="form-control dp-vd" name="description" id="edit_description"></textarea>

                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="edit_modal_save_btn" onclick="updateRole();">保存</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
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

<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript">

    $(function () {
        $('#addForm').dpValidate({
            validate: true,
            clear: true,
            callback: function () {

                var treeObj = $.fn.zTree.getZTreeObj("add_permission_area"),
                        nodes = treeObj.getCheckedNodes(true),
                        ids = '';

                
                for (var i = 0; i < nodes.length; i++) {
                    //如果是商家新增权限，则添加人员新增、权限新增权限
                    if (nodes[i].id == "BUSINESSMGR_VIEW") {
                        ids += "PARTYMGR_UPDATE,PARTYMGR_CREATE,SECURITY_CREATE,";
                    }
                    //如果文章新增权限，则添加相关需要权限
                    if (nodes[i].id == "CONTENT_ARTICLE_VIEW" || nodes[i].id == "CONTENT_ARTICLELIST"||nodes[i].id == "CONTENT_ARTICLELIST_CREATE"||nodes[i].id == "CONTENT_VIEW"){
                        ids += "CONTENTMGR_CREATE,CONTENTMGR_ADMIN,";
                    }
                    //如果商品新增权限，则添加相关需要的权限
                    if (nodes[i].id == "PRODUCTGOODMGR_VIEW" || nodes[i].id == "PRODUCTGOODMGR_CREATE_VIEW"||nodes[i].id == "PRODUCTGOODMGR_LIST_UPDATE"){
                        ids += "CATALOG_PRICE_MAINT,CATALOG_CREATE,CATALOG_VIEW,CATALOG_UPDATE,CATALOG_DELETE,CATALOG_ROLE_CREATE,CATALOG_ROLE_UPDATE,CATALOG_ROLE_DELETE,";
                    }

                    //如果订单新增权限，则添加相关需要权限
                    if (nodes[i].id == "ORDERENTRY_LIST_VIEW" || nodes[i].id == "ORDER_ADD_VIEW"){
                        ids += "ORDERMGR_CREATE,ORDERMGR_UPDATE,ORDERMGR_SALES_ENTRY,ORDERMGR_SALES_CREATE,ORDERMGR_NOTE,PARTYMGR_CREATE,PAY_INFO_CREATE,ORDERMGR_CRQ_CREATE,";
                    }
                    if (nodes[i].id == "ORDER_LIST_VIEW"){
                        ids += "ORDERMGR_VIEW,";
                    }
                    //会员管理或会员列表增加会员操作权限
                    if (nodes[i].id == "PARTYMGR_VIEW" || nodes[i].id == "PARTYMGR_LIST_VIEW"){
                        ids += "PARTYMGR_ADMIN,PARTYMGR_DELETE,PARTYMGR_PCM_DELETE,PARTYMGR_PCM_ADMIN,";
                    }

                        ids += nodes[i].id + ",";

                    // add by zhajh at 20170210 添加CRM端菜单 End
                }


                $('#modal_add #add_permissionIds').val(ids);
                if(ids==''){
                    alert('请选择权限树');
                    return ;
                }

                $.ajax({
                    type: 'post',
                    url: '<@ofbizUrl>checkSecurityGroupExist</@ofbizUrl>',
                    data: {groupId: $("#groupId").val()},
                    async: false,
                    success: function (data) {
                        console.log(data)
                        if (data && data.group) {
//                            $("#usertip").html('<label for="userName" generated="true" class="error" style="display: inline-block;">管理员名称已存在</label>');
                            $("#permissionId").addClass('error');
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("功能权限已存在！");
                            $('#modal_msg').modal();
                        } else {
                            $("#usertip").html('');
                            $("#permissionId").removeClass('error');

                            //异步调用新增方法
                            $.ajax({
                                url: "addRole",
                                type: "POST",
                                async: false,
                                data: $('#addForm').serialize(),
                                dataType: "json",
                                success: function (data) {
                                    //alert(data.crmopt);
                                    //隐藏新增弹出窗口
                                    $('#modal_add').modal('toggle');
                                    //设置提示弹出框内容
                                    $('#modal_msg #modal_msg_body').html("操作成功！");
                                    $('#modal_msg').modal();
                                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                    $('#modal_msg').on('hide.bs.modal', function () {
                                        window.location.reload();
                                    })
                                },
                                error: function (data) {
                                    //隐藏新增弹出窗口
                                    $('#modal_add').modal('toggle');
                                    //设置提示弹出框内容
                                    $('#modal_msg #modal_msg_body').html("操作失败！");
                                    $('#modal_msg').modal();
                                }
                            });
                        }
                    }
                })
            }
        });


        $('#editForm').dpValidate({
            validate: true,
            clear: true,
            callback: function () {
                var treeObj = $.fn.zTree.getZTreeObj("edit_permission_area"),
                        nodes = treeObj.getCheckedNodes(true),
                        ids = '';

                for (var i = 0; i < nodes.length; i++) {
                    //如果是商家新增权限，则添加人员新增、权限新增权限
                    if (nodes[i].id == "BUSINESSMGR_VIEW") {
                        ids += "PARTYMGR_UPDATE,PARTYMGR_CREATE,SECURITY_CREATE,";
                    }
                    //如果订单新增权限，则添加相关需要权限
                    if (nodes[i].id == "ORDERENTRY_LIST_VIEW" || nodes[i].id == "ORDER_ADD_VIEW"){
                        ids += "ORDERMGR_CREATE,ORDERMGR_UPDATE,ORDERMGR_SALES_ENTRY,ORDERMGR_SALES_CREATE,ORDERMGR_NOTE,PARTYMGR_CREATE,PAY_INFO_CREATE,ORDERMGR_CRQ_CREATE,";
                    }
                    if (nodes[i].id == "ORDER_LIST_VIEW"){
                        ids += "ORDERMGR_VIEW,";
                    }
                    //如果文章新增权限，则添加相关需要权限
                    if (nodes[i].id == "CONTENT_ARTICLE_VIEW" || nodes[i].id == "CONTENT_ARTICLELIST"||nodes[i].id == "CONTENT_ARTICLELIST_CREATE"||nodes[i].id == "CONTENT_VIEW"){
                        ids += "CONTENTMGR_CREATE,CONTENTMGR_ADMIN,";
                    }
                    //如果商品新增权限，则添加相关需要的权限
                    if (nodes[i].id == "PRODUCTGOODMGR_VIEW" || nodes[i].id == "PRODUCTGOODMGR_CREATE_VIEW"||nodes[i].id == "PRODUCTGOODMGR_LIST_UPDATE"){
                        ids += "CATALOG_PRICE_MAINT,CATALOG_CREATE,CATALOG_VIEW,CATALOG_UPDATE,CATALOG_DELETE,CATALOG_ROLE_CREATE,CATALOG_ROLE_UPDATE,CATALOG_ROLE_DELETE,";
                    }//如果是系统管理操作
                    if (nodes[i].id == "SYSTEMMGR_USER_CREATE" || nodes[i].id == "SYSTEMMGR_USER_UPDATE"){
                        ids += "SECURITY_ADMIN,";
                    }
                    // Del by zhajh at 20160407 删除默认添加webtool模块 Begin
                    //if(nodes[i].id == "SYSTEMMGR_VIEW" ){
                        //ids += "WEBTOOLS_VIEW,UTIL_CACHE_VIEW,UTIL_CACHE_EDIT,";
                    //}
                    // Del by zhajh at 20160407 删除默认添加webtool模块 End

                    //会员管理或会员列表增加会员操作权限
                    if (nodes[i].id == "PARTYMGR_VIEW" || nodes[i].id == "PARTYMGR_LIST_VIEW"){
                        ids += "PARTYMGR_ADMIN,PARTYMGR_DELETE,PARTYMGR_PCM_DELETE,PARTYMGR_PCM_ADMIN,";
                    }

                        ids += nodes[i].id + ",";

                    
                }
                console.log(ids);
                $('#modal_edit #edit_permissionIds').val(ids);
                if(ids==''){
                    alert('请选择权限树');
                    return ;
                }
                //异步调用新增方法
                $.ajax({
                    url: "updateRole",
                    type: "POST",
                    async: false,
                    data: $('#editForm').serialize(),
                    dataType: "json",
                    success: function (data) {
                        //alert(data.crmopt);
                        //隐藏新增弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("操作成功！");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.reload();
                        })
                    },
                    error: function (data) {
                        //隐藏新增弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("操作失败！");
                        $('#modal_msg').modal();
                    }
                });
            }
        })
    });


    var setting = {
        check: {
            enable: true
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
    function addRole() {
        $.ajax({
            url: "perTree",
            type: "GET",
            dataType: "json",
            success: function (data) {
                console.log(data)
                var treeObj = $.fn.zTree.init($("#add_permission_area"), setting, data.permissionTree);
                treeObj.expandAll(true);
                
                //设置提示弹出框内容
                $('#modal_add').modal();
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("网络异常！");
                $('#modal_msg').modal();
            }
        });

        $("#groupId").val('');
        $("#name").val('');
        $('#permissionId').val('');
        $('#description').val('');
        $('#addForm').attr('action', '<@ofbizUrl>addRole</@ofbizUrl>');
        $('#modal_add').modal('show');
    }
    
    
    // 取得指定节点的所有子节点
	function getChildNodes(ztree,treeNode) {
	     var childNodes = ztree.transformToArray(treeNode);
	     var nodes = new Array();
	     for(i = 0; i < childNodes.length; i++) {
	          nodes[i] = childNodes[i].id;
	     }
	     return nodes;
	}
	

	

    function saveRole() {
        $('#addForm').dpValidate({
            clear: true
        });
        $('#addForm').submit();
    }


    function updateRole() {
        $('#editForm').dpValidate({
            clear: true
        });
        $('#editForm').submit();
    }

    function editRole(id) {

        $('#edit_description').removeAttr('readonly');
        $('#modal_edit_title').html('修改角色');
        $('#edit_name').removeAttr('readonly');
        $('#edit_modal_save_btn').show();
        $('#edit_groupId').attr('readonly', true);
        $('#editForm').attr('action', '<@ofbizUrl>updateRole</@ofbizUrl>');
        $.ajax({
            type: 'post',
            url: '<@ofbizUrl>queryRoleDetail</@ofbizUrl>',
            data: {groupId: id},
            success: function (data) {
                console.log(data)
//                    console.log(data.userLoginAndSecurityGroup.groupId)
                if (data && data.securityGroup) {
                    $("#edit_name").val(data.securityGroup.name);
                    $('#edit_description').val(data.securityGroup.description);
                    $("#edit_groupId").val(data.securityGroup.groupId);

                    //异步加载所有地区数据
                    $.ajax({
                        url: "perTree",
                        type: "GET",
                        dataType: "json",
                        success: function (treeData) {
//                            console.log(treeData.permissionTree)
                            var treeObj = $.fn.zTree.init($("#edit_permission_area"), setting, treeData.permissionTree);
                            //自动勾选复选框
                            $.each(data.securityGroupPermissions, function (i) {
//                                console.log(data.securityGroupPermissions[i].permissionId)
                                var node = treeObj.getNodeByParam("id", data.securityGroupPermissions[i].permissionId);
                                console.log(node)
                                if(node ) {
                                    treeObj.checkNode(node);
                                    //自动展开子节点
                                    if (!node.isParent) {
                                        treeObj.expandNode(node.getParentNode(), true, true, true);
                                    }
                                }
                            })
                            $('#modal_edit').modal();
                        },
                        error: function (data) {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("网络异常！");
                            $('#modal_msg').modal();
                        }
                    });
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("网络异常！");
                $('#modal_msg').modal();
            }
        });

        $("#editForm").validate();
        $('#modal_edit').modal('show');
    }

    function roleDetail(id) {
        editRole(id);
        $("#edit_groupId").attr("readonly", true);
        $('#edit_description').attr('readonly', true);
        $('#edit_permission_area').attr('readonly', true);
        $('#edit_name').attr('readonly', true);
        $('#editForm').attr('action', '');
        $('#edit_modal_save_btn').hide();
        $('#modal_edit_title').html('查看角色');
        $('#modal_edit').modal('show');
    }

    //行删除按钮事件
    function delRole(id) {
        //异步调用删除验证方法
        $.ajax({
            url: "chkRoles",
            type: "POST",
            data: {groupIds: id},
            dataType: "json",
            success: function (data) {
                if(data.chkFlg=="OK"){
                    //异步调用删除方法
                    $.ajax({
                        url: "deleteRole",
                        type: "POST",
                        data: {groupId: id},
                        dataType: "json",
                        success: function (data) {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作成功！");
                            $('#modal_msg').modal();
                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.reload();
                            })
                        },
                        error: function (data) {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作失败！");
                            $('#modal_msg').modal();
                        }
                    });
                }else{
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("角色【"+data.errGroupIds+"】正在使用中,不能删除！");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.reload();
                    })
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
            }
        });
    }


    //删除按钮点击事件
    $('#btn_del').click(function () {
        var checks = $('.js-checkparent .js-checkchild:checked');
        //判断是否选中记录
        if (checks.size() > 0) {
            //编辑id字符串
            var ids = "";
            checks.each(function () {
                ids += $(this).val() + ",";
            });
            //异步调用删除验证方法
            $.ajax({
                url: "chkRoles",
                type: "POST",
                data: {groupIds: ids},
                dataType: "json",
                success: function (data) {
                    if(data.chkFlg=="OK"){
                        //异步调用删除方法
                        $.ajax({
                            url: "deleteRoles",
                            type: "POST",
                            data: {groupIds: ids},
                            dataType: "json",
                            success: function (data) {
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("操作成功！");
                                $('#modal_msg').modal();
                                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.reload();
                                })
                            },
                            error: function (data) {
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("操作失败！");
                                $('#modal_msg').modal();
                            }
                        });
                    }else{
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("角色【"+data.errGroupIds+"】正在使用中,不能删除！");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.reload();
                        })
                    }
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("操作失败！");
                    $('#modal_msg').modal();
                }
            });
        } else {
            //设置提示弹出框内容
            $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
            $('#modal_msg').modal();
        }
    });
</script>