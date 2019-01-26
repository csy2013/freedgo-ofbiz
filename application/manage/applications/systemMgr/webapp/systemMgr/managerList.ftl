<#assign commonUrl = "userList?lookupFlag=Y"+ paramList +"&">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">${uiLabelMap.searchCondition}</h3>
    </div>
<#--${userList}-->
    <div class="box-body">
        <form class="form-inline clearfix" role="form" method="post" action="<@ofbizUrl>userList</@ofbizUrl>">
            <input type="hidden" name="lookupFlag" value="Y">

            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.userLoginName}</span>
                    <input type="text" class="form-control" name="userLoginId1" placeholder="${uiLabelMap.userLoginName}" >
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.userName}</span>
                    <input type="text" class="form-control" name="userName1" placeholder="${uiLabelMap.userName}"  >
                </div>
            </div>
            <div class="input-group pull-right">
            <#if security.hasEntityPermission("SYSTEMMGR_USER", "_VIEW", session)>
                <button class="btn btn-success btn-flat">${uiLabelMap.CommonView}</button></#if>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
        <div class="row m-b-10">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                <#if security.hasEntityPermission("SYSTEMMGR_USER", "_CREATE", session)>
                    <button class="btn btn-primary" onclick="addUser()" ;>
                    ${uiLabelMap.CommonAdd}
                    </button>
                </#if>
                </div>
            </div>
        <#assign commonUrl1 = commonUrl+"ORDER_FILED=${orderFiled}&amp;ORDER_BY=${orderBy}&amp;"/>
        <#if userList?has_content>

            <div class="col-sm-6">
                <div class="dp-tables_length">
                    <label>
                        每页显示
                        <select id="dp-tables_length" name="tables_length" class="form-control input-sm" onchange="location.href='${commonUrl1}VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
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

    <#if userList?has_content>
        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>${uiLabelMap.CommonUsername}
                            <#if orderFiled == 'userLoginId'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=userLoginId&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=userLoginId&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=userLoginId&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>${uiLabelMap.userName}
                            <#if orderFiled == 'name'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>${uiLabelMap.isEffect}
                            <#if orderFiled == 'enabled'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=enabled&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=enabled&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=enabled&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>${uiLabelMap.FormFieldTitle_lastLoginTime}
                            <#if orderFiled == 'lastLoginTime'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=lastLoginTime&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=lastLoginTime&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=lastLoginTime&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>${uiLabelMap.systemMgrAction}</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list userList as partyRow>

                        <tr>
                            <td><input class="js-checkchild" type="checkbox"></td>
                            <td><#if partyRow.containsKey("userLoginId")>${partyRow.userLoginId?default("N/A")}</#if></td>
                            <td><#if partyRow.containsKey("name")>${partyRow.name?default("N/A")}</#if></td>
                            <td>
                                <#if partyRow.containsKey("enabled")>
                                    <#if partyRow.enabled?default("N") == 'Y'>
                                ${uiLabelMap.CommonEnabled}
                                <#else>
                                ${uiLabelMap.CommonDisabled}
                                </#if>
                                </#if>
                            </td>
                            <td><#if partyRow.lastLoginTime?has_content>${partyRow.lastLoginTime?string("yyyy-MM-dd hh:mm")}</#if></td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("SYSTEMMGR_USER", "_DETAIL", session)>
                                        <button type="button" class="btn btn-danger btn-sm" onclick="userDetail('${partyRow.userLoginId?default("N/A")}')">查看</button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#if security.hasEntityPermission("SYSTEMMGR_USER", "_UPDATE", session)>
                                            <#assign userLoginSecurityGroups = delegator.findByAnd("UserLoginSecurityGroup",{"userLoginId" : partyRow.userLoginId})/>
                                            <#assign userFlg="P"/>
                                            <#if userLoginSecurityGroups?has_content>
                                                <#list userLoginSecurityGroups as userLoginSecurityGroup>
                                                    <#if userLoginSecurityGroup.groupId=='SUPPLIER'>
                                                        <#assign userFlg="B"/>
                                                    </#if>
                                                </#list>
                                            </#if>
                                            <#if userFlg!="B">
                                                <li><a href="#" onclick="updateUser('${partyRow.userLoginId?default("N/A")}')">编辑</a></li>
                                            </#if>
                                        </#if>
                                    </ul>
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
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(userListSize, viewSize) />

        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", userListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl1 ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=userListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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
<div id="modal_add" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">>
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">${uiLabelMap.AddUserAction}</h4>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal" id="addForm" method="post" action="<@ofbizUrl>addManager</@ofbizUrl>" enctype="multipart/form-data">

                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.PartyUserName}">
                        <label for="partyUserName" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyUserName}：</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" name="userName" id="userName">
                            <p class="dp-error-msg"></p>
                        </div>

                    </div>
                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.PartyNewPassword}" id="passwordGroup">
                        <label for="PartyNewPassword" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyNewPassword}：</label>
                        <div class="col-sm-9">
                            <input type="password" class="form-control dp-vd" name="password" id="password">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group" data-type="linkEq" data-msg="密码不相等" data-compare-link="passwordGroup" >
                        <label for="PartyNewPasswordVerify" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyNewPasswordVerify}：</label>
                        <div class="col-sm-9">
                            <input type="password" class="form-control dp-vd" name="rePassword" id="rePassword">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group"  data-type="required" data-mark="${uiLabelMap.PartyName}">
                        <label for="PartyName" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyName}：</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" name="name" id="name">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.PartyContactMobilePhoneNumber}">
                        <label for="PartyContactMobilePhoneNumber" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyContactMobilePhoneNumber}：</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" name="mobile" id="mobile">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="PartyRole" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyRole}：</label>

                        <div class="col-sm-9">
                            <select name="groupId" id="groupId" data-live-search="true" class="select2 select2-hidden-accessible" style="width:80%;" tabindex="-1" aria-hidden="true">
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="uploadedFile" class="control-label col-sm-3">${uiLabelMap.userUploadPhoto}：</label>

                        <div class="col-sm-9">
                            <img height="50" alt="" src="" id="img" style="height:50px;width:50px;">
                            <input style="margin-left:5px;" type="file" id="uploadedFile" name="uploadedFile" value="${uiLabelMap.CommonSelect}"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="FormFieldTitle_roleTypeId" class="control-label col-sm-3">${uiLabelMap.isEffect}：</label>

                        <div class="col-sm-9">
                            <label class="radio-inline">
                                <input type="radio" name="enabled" id="open1" value="Y" checked> ${uiLabelMap.CommonTrue}
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="enabled" id="open2" value="N"> ${uiLabelMap.CommonFalse}
                            </label>
                        </div>
                    </div>


                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="saveUser();">保存</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>

<#--修改管理员-->
<div id="modal_edit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">>
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="edit_modal-title">${uiLabelMap.ModifyUserAction}</h4>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal" id="editForm" method="post" action="<@ofbizUrl>updateManager</@ofbizUrl>" enctype="multipart/form-data">
                    <input type="hidden" name="partyId" id="edit_partyId"/>
                    <input type="hidden" name="userLoginId" id="edit_userLoginId"/>
                    <input type="hidden" name="oldGroupId" id="edit_oldGroupId"/>
                    <input type="hidden" name="oldMobile" id="edit_oldMobile">
                    <input type="hidden" name="contactMechId" id="edit_contactMechId"/>
                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.PartyUserName}">
                        <label for="partyUserName" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyUserName}：</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" name="userName" id="edit_userName" readonly>
                            <p class="dp-error-msg"></p>
                        </div>
                        <span id="usertip"></span>
                    </div>
                    <div class="form-group">
                        <label for="PartyNewPassword" class="control-label col-sm-3">${uiLabelMap.PartyNewPassword}：</label>
                        <div class="col-sm-9">
                            <input type="password" class="form-control dp-vd" name="password" id="edit_password">

                        </div>
                    </div>
                    <div class="form-group">
                        <label for="PartyNewPasswordVerify" class="control-label col-sm-3">${uiLabelMap.PartyNewPasswordVerify}：</label>
                        <div class="col-sm-9">
                            <input type="password" class="form-control dp-vd" name="rePassword" id="edit_rePassword">
                        </div>
                    </div>
                    <div class="form-group"  data-type="required" data-mark="${uiLabelMap.PartyName}">
                        <label for="PartyName" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyName}：</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" name="name" id="edit_name">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group"  data-type="required" data-mark="${uiLabelMap.PartyContactMobilePhoneNumber}">
                        <label for="PartyContactMobilePhoneNumber" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyContactMobilePhoneNumber}：</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" name="mobile" id="edit_mobile">
                            <p class="dp-error-msg"></p>

                        </div>
                    </div>
                    <div class="form-group">
                        <label for="PartyRole" class="control-label col-sm-3"><span class="required-mark">*</span>${uiLabelMap.PartyRole}：</label>

                        <div class="col-sm-9">
                            <select name="groupId" id="edit_groupId" data-live-search1="true" class="select2 select2-hidden-accessible" style="width:80%;" tabindex="-1" aria-hidden="true">
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="uploadedFile" class="control-label col-sm-3">${uiLabelMap.userUploadPhoto}：</label>

                        <div class="col-sm-9">
                            <img height="50" alt="" src="" id="edit_img" style="height:50px;width:50px;">
                            <input style="margin-left:5px;" type="file" id="edit_uploadedFile" name="uploadedFile" value="${uiLabelMap.CommonSelect}"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="FormFieldTitle_roleTypeId" class="control-label col-sm-3">${uiLabelMap.isEffect}：</label>

                        <div class="col-sm-9">
                            <label class="radio-inline">
                                <input type="radio" name="enabled" id="edit_open1" value="Y" checked> ${uiLabelMap.CommonTrue}
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="enabled" id="edit_open2" value="N"> ${uiLabelMap.CommonFalse}
                            </label>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" id="editUserBtn" class="btn btn-primary" onclick="editUser();">保存</button>
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

    $(function() {
        $('#addForm').dpValidate({
            validate: true,
            clear: true,
            callback: function () {
                console.log("start ajax");
                document.getElementById("addForm").submit();
            }
        })


        $('#editForm').dpValidate({
            validate: true,
            clear: true,
            callback: function () {
                console.log("start ajax");
                document.getElementById("editForm").submit();
            }
        })
    });

    function addUser() {
        $("#userName").val('');
        $('#password').val('');
        $('#rePassword').val('');
        $('#name').val('');
        $('#groupId').val('');
        $("#mobile").val('');
        $("#uploadedFile").attr("src", '');
        $("input[name=enabled]:eq(0)").attr("checked", 'checked');
        $('#addForm').attr('action', '<@ofbizUrl>addManager</@ofbizUrl>');
        loadAuthority();
        $('#modal_add').modal('show');
    }

    function saveUser() {
        $('#addForm').dpValidate({
            clear: true
        });

        var password = $('#password').val();
        var repassword = $('#rePassword').val();
        if(password !== repassword){
            $.tipLayer('密码不一致!')
            return;
        }
        console.log(password.length)
        if(password.length<5){
            $.tipLayer('密码至少5个字符!')
            return;
        }
        $.ajax({
            type: 'post',
            url: '<@ofbizUrl>checkManagerExist</@ofbizUrl>',
            data: {userLoginId: $("#userName").val()},
            async: false,
            success: function (data) {
                console.log(data)
                if (data && data.userLogin) {
//                            $("#usertip").html('<label for="userName" generated="true" class="error" style="display: inline-block;">管理员名称已存在</label>');
                    $("#userName").addClass('error');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("管理员名称已存在！");
                    $('#modal_msg').modal();
                } else {
                    $("#usertip").html('');
                    $("#userName").removeClass('error');
                    $("#addForm").submit();
                }
            }
        });
    }


    function editUser() {
        $('#editForm').dpValidate({
            clear: true
        });
       /* var password = $('#edit_password').val();
        var repassword = $('#edit_rePassword').val();
        if(password !== repassword){
            $.tipLayer('密码不一致!')
            return;
        }
        if(password.length<5){
            $.tipLayer('密码至少5个字符!')
            return;
        }*/
        $("#editForm").submit();

    }

    function updateUser(userName) {
        $('#edit_modal-title').text('修改管理员');
        $("#edit_userName").removeAttr("readonly");
        $('#edit_name').removeAttr('readonly');
        $('#password').removeAttr('readonly');
        $('#rePassword').removeAttr('readonly');
        $("#edit_mobile").removeAttr('readonly')
        $("#edit_uploadedFile").removeAttr('disabled');
        $('#edit_groupId').removeAttr('disabled');
        $("input[name=enabled]").removeAttr('disabled');

        $("#edit_userName").val('');
        $('#edit_password').val('');
        $('#edit_rePassword').val('');
        $('#edit_name').val('');
        $('#edit_groupId').val('');
        $("#edit_mobile").val('');
        $("#edit_uploadedFile").attr("src", '');

        $("input[name=enabled]:eq(0)").attr("checked", 'checked');
        $('#editForm').attr('action', '<@ofbizUrl>updateManager</@ofbizUrl>');
        doSearchManager(userName)
        if(userName == '${userLogin.userLoginId}'){
            $('#edit_password').parent().parent().hide();
            $('#edit_rePassword').parent().parent().hide();
        }else{
            $('#edit_password').parent().parent().show();
            $('#edit_rePassword').parent().parent().show();
        }
        loadEditAuthority();
//        $("#editForm").validate();
        $('#modal_edit').modal('show');
    }

    function doSearchManager(id) {
        $.ajax({
            type: 'post',
            url: '<@ofbizUrl>queryManager</@ofbizUrl>',
            data: {userLoginId: id},
            success: function (data) {
                console.log(data)
//                    console.log(data.userLoginAndSecurityGroup.groupId)
                if (data && data.person && data.userLogin) {
                    $("#edit_userName").val(data.userLogin.userLoginId);
                    $('#edit_name').val(data.person.name);
                    $("#edit_userLoginId").val(data.userLogin.userLoginId);
                    $("#edit_partyId").val(data.userLogin.partyId);
                    if (data.userLoginAndSecurityGroup) {
                        loadEditAuthority(data.userLoginAndSecurityGroup.groupId);
                    }
                    if (data.userMobileContact) {
                        $("#edit_mobile").val(data.userMobileContact.contactNumber);
                    }
                    if (data.userLogin.enabled == 'N') {
                        $('#edit_open2').attr("checked", 'checked');
                    }
                    if (data.partyContent && data.partyContent.dataResourceId) {
                        $("#edit_img").attr('src', "<@ofbizUrl>personLogo?imgId="+ data.partyContent.dataResourceId</@ofbizUrl>);
                    }
                    $("#edit_oldGroupId").val(data.userLoginAndSecurityGroup.groupId);
                    $("#edit_oldMobile").val(data.userMobileContact.contactNumber);
                    $("#edit_contactMechId").val(data.userMobileContact.contactMechId)
                    $('select[data-live-search1="true"]').select2();
                }
            }
        });
    }


    function userDetail(id) {
        doSearchManager(id);
        $("#edit_userName").attr("readonly", true);
        $('#edit_name').attr('readonly', true);
        $('#edit_password').parent().parent().hide();
        $('#edit_rePassword').parent().parent().hide();
        $('#edit_groupId').val('');
        $("#edit_mobile").attr('readonly', true)
        $("#edit_uploadedFile").attr('disabled', true);
        $('#edit_groupId').attr('disabled', true);
        $("input[name=enabled]").attr('disabled', true);
        $('#edit_modal-title').text('查看管理员');
        $("#editUserBtn").remove();
        $('#modal_edit').modal('show');
    }
    /**加载权限列表*/
    function loadAuthority() {
        $.post("<@ofbizUrl>queryAllSecurityGroup</@ofbizUrl>", function (data) {
            console.log(data)
            var options = "";
            for (var i = 0; i < data.securityGroups.length; i++) {
                var auth = data.securityGroups[i];
                options += "<option value='" + auth.groupId + "'>" + auth.name + "</option>";
            }
            $('#groupId').html(options);
            /* 为选定的select下拉菜单开启搜索提示 END */
            $('select[data-live-search="true"]').select2();
        });


    }

    /**加载权限列表*/
    function loadEditAuthority(groupId) {
        $.post("<@ofbizUrl>queryAllSecurityGroup</@ofbizUrl>", function (data) {
            console.log(data)
            var options = "";
            for (var i = 0; i < data.securityGroups.length; i++) {
                var auth = data.securityGroups[i];
                if (groupId == auth.groupId) {
                    options += "<option selected value='" + auth.groupId + "'>" + auth.description + "</option>";
                } else {
                    options += "<option value='" + auth.groupId + "'>" + auth.name + "</option>";
                }
            }
            $('#edit_groupId').html(options);
            /* 为选定的select下拉菜单开启搜索提示 END */
            $('select[data-live-search1="true"]').select2();
        });


    }




</script>