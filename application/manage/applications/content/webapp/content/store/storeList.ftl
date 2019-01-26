<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<script src="<@ofbizContentUrl>/images/getDependentDropdownValues.js</@ofbizContentUrl>" type="text/javascript"></script>
<!-- Main content -->
<#assign commonUrl = "TagList?"+ paramList+"&" />
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">店铺列表</h3>
        </div>

        <div class="box-body">
            <form class="form-inline clearfix"  id="searchForm" role="form" action="<@ofbizUrl>productStoreList</@ofbizUrl>">
                <div class="form-group" style="position:relative;">
                    <div class="input-group m-b-10">
                        <span class="input-group-addon">店铺名称</span>
                        <input type="text" class="form-control" placeholder="店铺名称" name="storeName" <#if storeName?has_content> value="${storeName?if_exists}"</#if>/>
                    </div>
                    <div class="input-group m-b-10">
                        <span class="input-group-addon">店铺编号</span>
                        <input type="text" class="form-control" placeholder="店铺编号" name="productStoreId" <#if productStoreId?has_content> value="${productStoreId?if_exists}"</#if>/>
                    </div>

                    <div class="input-group m-b-10">
                        <span class="input-group-addon">店铺地址</span>
                        <input type="text" class="form-control" placeholder="店铺地址" name="address" <#if address?has_content> value="${address?if_exists}"</#if>/>
                    </div>
                </div>
                <div class="input-group pull-right">
                    <!--是否有查询的权限-->
                <#if security.hasEntityPermission("CONTENT_TAGLIST", "_VIEW", session)>
                    <button class="btn btn-success btn-flat">${uiLabelMap.Search}</button>
                </#if>
                </div>
            </form>

            <div class="cut-off-rule bg-gray"></div>
        <div class="btn-box m-b-10">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <!--是否有新增的权限-->
          <#if security.hasEntityPermission("CONTENT_TAGLIST", "_CREATE", session)>
                <button id="btn_add" class="btn btn-primary" >
                    <i class="fa fa-plus"></i>${uiLabelMap.Add}
                </button>
          </#if>
                    <!--是否有删除的权限
          <#if security.hasEntityPermission("CONTENT_TAGLIST", "_DELETE", session)>
                <button class="btn btn-primary" id="btn_del">
                    <i class="fa fa-trash"></i> 禁用
                </button>
          </#if>  -->
                </div>
            </div><!-- 操作按钮组end -->

            <#if storeList?has_content>
                       
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
                    <table id="example2" class="table table-bordered table-hover js-checkparent">
                        <thead>
                        <tr>
                            <!--是否有删除的权限-->
                           <#if security.hasEntityPermission("CONTENT_TAGLIST", "_DELETE", session)>
                            <th><input type="checkbox" class="js-allcheck"></th>
                           </#if>
                            <th>店铺编号</th>
                            <th>店铺名称</th>
                            <th>店铺地址</th>
                        <#--<th>店铺标题</th>-->
                            <th>备注</th>
                            <th>是否启用</th>
                            <th>操作</th>
                            <!--是否有更新的权限-->
                            <!--<#if security.hasEntityPermission("CONTENT_TAGLIST", "_UPDATE", session)>
                            <th>${uiLabelMap.Operation}</th>
                            </#if> -->
                        </tr>
                        </thead>
                        <tbody>

                         <#list storeList as storeList>
                         <tr class="gss">
                             <!--是否有删除的权限-->
                           <#if security.hasEntityPermission("CONTENT_TAGLIST", "_DELETE", session)>
                           <td><input type="checkbox" class="js-checkchild" value="${storeList.productStoreId?if_exists}"></td>
                           </#if>
                         <#-- <td>
                          <#assign src='/content/control/getImage?contentId='>
                          <#if taglist.contentId?has_content>
                          <#assign imgsrc = src +taglist.contentId/>
                          <img height="50" alt="" src="${imgsrc}" id="img" style="height:100px;width:100px;">
                          </#if>
                          </td>-->
                             <td>${storeList.productStoreId?if_exists}</td>
                             <td>${storeList.storeName?if_exists}</td>
                            <#assign addresses="">
                            <#if storeList.stateProvinceGeoId?has_content>
                                <#assign countryGeos = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId", storeList.stateProvinceGeoId?if_exists))>
                                <#if countryGeos?has_content>
                                    <#assign addresses=addresses+countryGeos.geoName?if_exists>
                                </#if>
                            </#if>
                            <#if  storeList.cityGeoId?has_content>
                                <#assign cityGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId", storeList.cityGeoId?if_exists))>
                                <#if cityGeo?has_content>
                                    <#assign addresses=addresses+cityGeo.geoName?if_exists>
                                </#if>
                            </#if>
                            <#if  storeList.storeList?has_content>
                                <#assign countyGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId", storeList.countyGeoId?if_exists))>
                                <#if countyGeo?has_content>
                                    <#assign addresses=addresses+countyGeo.geoName?if_exists>
                                </#if>
                            </#if>
                             <td>${addresses?if_exists}${storeList.address?if_exists}</td>
                             <td>${storeList.remark?if_exists}</td>
                             <td >
                                <#if storeList.isEnabled?has_content&&storeList.isEnabled.equals('Y')>
                                    <button  class="gss_btn btn btn-primary" >${uiLabelMap.Y}</button>
                                <#else>
                                    <button  class="gss_btn btn btn-default" >${uiLabelMap.N}</button>
                                </#if>
                             </td>
                             <!--是否有更新的权限-->
                            <#if security.hasEntityPermission("CONTENT_TAGLIST", "_UPDATE", session)>
                            <td>
                                <div class="btn-group">
                                    <button type="button" class=" btn-info btn btn-danger btn-sm" data-val="${storeList.productStoreId?if_exists}">${uiLabelMap.Edit}</button>
                                    <!--是否有删除的权限-->
                                    <!--<#if security.hasEntityPermission("CONTENT_TAGLIST", "_DELETE", session)>
                                    <ul class="dropdown-menu" role="menu">
                                        <li><a href="javascript:del('${storeList.productStoreId?if_exists}')">禁用</a></li>
                                    </ul>
                                    </#if> -->
                                </div>
                            </td>
                            </#if>
                             <input type="hidden" name="isEnabled" value="${storeList.isEnabled}" />
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
                <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(storeListSize, viewSize) />
                <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", storeListSize)/>
                <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
                <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
                listSize=storeListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
                pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
                paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
       <!-- 分页条end -->
            <#else>
        <div id="noData" class="col-sm-12">
            <h3>没有店铺数据!</h3>
        </div>
            </#if>
        </div>
        <!-- /.box-body -->
    </div>

<!-- 提示弹出框start -->
<div id="modal_msg"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
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


<!-- 禁用确认弹出框start -->
<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">禁用</button>
            </div>
        </div>
    </div>
</div><!--禁用确认弹出框end -->

       

 <!--添加弹出框 start-->
<div class="modal fade" id="add_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">店铺新增/编辑</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="AddForm" action="<@ofbizUrl>createStore</@ofbizUrl>" method="post">
                    <input type="hidden" name="optionType" id="optionType" value="create">
                    <div class="row">
                        <div class="form-group"  data-mark="店铺编号">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>店铺编号:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="productStoreIds" name="productStoreIds" disabled="true"/>
                                <input type="hidden" class="form-control dp-vd w-p50" id="productStoreId" name="productStoreId" />
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="店铺名称">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>店铺名称:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="storeName" name="storeName">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.IsEnabled}:</label>
                            <div class="radio col-sm-10">
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
                    </div>
                    <!-- 店铺图标 start-->
                    <div class="row">
                        <div class="form-group" >
                            <label for="title" class="col-sm-2 control-label">店铺图标:</label>
                            <div class="col-sm-10">
                                <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                                <input type="hidden" class="form-control dp-vd w-p50" id="contentId" name="contentId">
                                <input style="margin-left:5px;" type="button" id="" name="uploadedFile"  onclick="imageManage()" value="选择图片"/>
                            </div>
                        </div>
                    </div>
                    <!-- 店铺图标 end-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="发货仓库">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>发货仓库:</label>
                            <div class="col-sm-10">
                                <select id="inventoryFacilityId" name="inventoryFacilityId" class="form-control dp-vd" style="width:150px">
                                    <option value="">====请选择====</option>
                        <#if facilityList?has_content && (facilityList?size > 0)>
                            <#list facilityList as fl>
                                <option value="${fl.facilityId}">${fl.facilityName}</option>
                            </#list>
                        </#if>
                                </select>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!--默认国家- 省start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="省">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>店铺地址:</label>
                            <div class="col-sm-10">
                                <div style="display:none;">
                                    <select name="countryGeoId" id="AddForm_countryGeoId" >
                                    ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                                <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
                                        <option selected="selected" value="${defaultCountryGeoId}">
                                    <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
                                        ${countryGeo.get("geoName",locale)}
                                        </option>
                                    </select>
                                </div>
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="stateProvinceGeoId" id="AddForm_stateProvinceGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                                <!--市start-->
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="cityGeoId" id="AddForm_cityGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                                <!-- 区start-->
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="countyGeoId" id="AddForm_countyGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="form-group" data-type="required" data-mark="详细地址">
                            <label for="title" class="col-sm-2 control-label"></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p70" id="address" name="address">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div> <!--默认国家- 省end-->
                    <div class="row">
                        <div class="form-group"  data-mark="营业时间" data-reg="/^[0-9\:\-\u4e00-\u9fa5]{1,50}$/" data-number="50">
                            <label for="title" class="col-sm-2 control-label">营业时间:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="businessTime" name="businessTime">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group"  data-mark="联系人">
                            <label for="title" class="col-sm-2 control-label">联系人:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="contacts" name="contacts">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group"  data-mark="联系电话">
                            <label for="title" class="col-sm-2 control-label">联系电话:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="telephone" name="telephone">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!-- 备注start-->
                    <div class="row">
                        <div class="form-group">
                            <label for="title" class="control-label col-sm-2">${uiLabelMap.Remark}:</label>
                            <div class="col-sm-10">
                                <textarea class="form-control w-p80" id="remark" name="remark"></textarea>
                            </div>
                        </div>
                    </div>
                    <input type="hidden" class="form-control dp-vd w-p50" id="isDel" name="isDel" value="N">
                    <!-- 标签备注 end-->
                    <div class="modal-footer" style="text-align:center;">
                        <button type="button"  id="btn_save" class="btn btn-primary">保存</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
   <!--添加弹出框 end-->
<script>
    var del_ids = "";
    $(function(){
        //添加按钮点击事件
        $('#btn_add').click(function(){
            form_operation='add';
            $("#add_Modal #tagTypeId").val('');
            $("#add_Modal #productStoreIds").val('');
            $("#add_Modal #productStoreId").val('');
            $("#add_Modal #tagName").val('');
            $("#add_Modal #uploadedFile").attr("src", '');
            $("#add_Modal #tagRemark").val('');
            $("#add_Modal #treeName").val('');
            $("#add_Modal #productStoreId").val("");
            $("#add_Modal #storeName").val("");
            $("#add_Modal #address").val("");
            $("#add_Modal #businessTime").val("");
            $("#add_Modal #contacts").val("");
            $("#add_Modal #telephone").val("");
            $("#add_Modal #contentId").val("");
            $("#add_Modal #remark").val("");
            $("#add_Modal #optionType").val("create");
            $('#add_Modal #img').attr('src',"");
            $('.js-'+"Y").prop('checked',true);//启用状态
            // $("#inventoryFacilityId  option[value="+inventoryFacilityId+"]").attr("selected", true);
            editStore('','','');
        });

        //编辑按钮点击事件
        $(".btn-info").click(function(){
            var id=$(this).data("val");
            doSearchStore(id);
            $('#add_Modal').modal('show');
        });

        //添加/编辑关闭事件
        $('#add_Modal').on('hide.bs.modal', function () {
            $('#AddForm').dpValidate({
                clear: true
            });
        });

        //添加/编辑提交按钮点击事件
        $('#btn_save').click(function(){
            $('#AddForm').dpValidate({
                clear: true
            });
            var businessTime=$('#businessTime').val();
            if(businessTime){
                $('#businessTime').parent().parent().attr("data-type", "max");
            }
            $('#AddForm').submit();
        });

        //添加/编辑表单校验
        $('#AddForm').dpValidate({
            validate: true,
            callback: function(){
                var url='';
                var optionType=$('#optionType').val();
                if(optionType=='create'){
                    url="createStore";
                }else{
                    url="updateStore";
                }
                $.ajax({
                    url: url,
                    type: "POST",
                    data: $('#AddForm').serialize(),
                    dataType : "json",
                    success: function(data){
                        //隐藏新增弹出窗口
                        $('#add_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal('show');
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.href='<@ofbizUrl>productStoreList</@ofbizUrl>';
                        })
                    },
                    error: function(data){
                        //隐藏新增弹出窗口
                        $('#add_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

        //更改站点状态
        $('.gss').on('click',".gss_btn",function(){
            var tr=$(this).closest('tr');
            var deleteId=tr.find('td').eq(1).text();//网站编号
            var isEnabled= tr.find('input[name=isEnabled]').val();//是否启用
            if (isEnabled == "Y") {
                isEnabled = "N";
            } else {
                isEnabled = "Y";
            }
            $.ajax({
                url:'<@ofbizUrl>disableStore</@ofbizUrl>',
                type: 'post',
                dataType: 'json',
                data: {
                    deleteId: deleteId,
                    isEnabled: isEnabled
                },
                success: function(data) {
                    if(isEnabled=='Y'){
                        var Y= '<button  class="gss_btn btn btn-primary" >${uiLabelMap.Y}</button>';
                        tr.find('td').eq(5).html(Y);
                        tr.find('input[name=isEnabled]').val(isEnabled);
                    }
                    if(isEnabled=='N'){
                        var N= '<button  class="gss_btn btn btn-default" >${uiLabelMap.N}</button>';
                        tr.find('td').eq(5).html(N);
                        tr.find('input[name=isEnabled]').val(isEnabled);
                    }
                }
            });
        });

        //禁用按钮点击事件
        $('#btn_del').click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if(checks.size() > 0 ){
                del_ids = "";
                //编辑id字符串
                checks.each(function(){
                    del_ids += $(this).val() + ",";
                });
                //设置删除弹出框内容
                $('#modal_confirm #modal_confirm_body').html("确定禁用店铺？");
                $('#modal_confirm').modal('show');
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
                $('#modal_msg').modal();
            }
        });

        //禁用弹出框删除按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用禁用方法
            $.ajax({
                url: "<@ofbizUrl>disableStore</@ofbizUrl>",
                type: "POST",
                data: {deleteId : del_ids},
                dataType : "json",
                success: function(data){
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.reload();
                    })
                },
                error: function(data){
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        });
        //图片弹框初始化
        $.chooseImage.int({
            serverChooseNum: 5,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: ''
        });
        //弹框保存按钮
        $('body').on('click','.img-submit-btn',function(){
            var obj = $.chooseImage.getImgData();
            $.chooseImage.choose(obj,function(data){
                var contentId="/content/control/getImage?contentId="+data.uploadedFile0;
                $('#add_Modal #img').attr('src',contentId);
                $('#add_Modal #contentId').val(data.uploadedFile0);
            })
        });

        //禁用店铺按钮事件
        function del(id){
            del_ids = id;
            //设置删除弹出框内容
            $('#modal_confirm #modal_confirm_body').html("确定禁用此店铺?");
            $('#modal_confirm').modal('show');
        }

        //编辑查询店铺
        function doSearchStore(id) {
            $.ajax({
                type: 'post',
                url: '<@ofbizUrl>queryStore</@ofbizUrl>',
                data: {productStoreId: id},
                success: function (data) {
                    $("#add_Modal #productStoreIds").val(data.store.productStoreId);
                    $("#add_Modal #productStoreId").val(data.store.productStoreId);
                    $("#add_Modal #storeName").val(data.store.storeName);
                    $("#add_Modal #address").val(data.store.address);
                    $("#add_Modal #businessTime").val(data.store.businessTime);
                    $("#add_Modal #contacts").val(data.store.contacts);
                    $("#add_Modal #telephone").val(data.store.telephone);
                    $("#add_Modal #contentId").val(data.store.contentId);
                    $("#add_Modal #remark").val(data.store.remark);
                    $("#add_Modal #optionType").val("update");
                    if(data.store.contentId!==null){
                        var imgUrl="/content/control/getImage?contentId="+data.store.contentId;
                        $('#add_Modal #img').attr('src',imgUrl);
                    }
                    $('.js-'+data.store.isEnabled).prop('checked',true);//启用状态
                    var inventoryFacilityId= data.store.inventoryFacilityId;
                    $("#inventoryFacilityId  option[value="+inventoryFacilityId+"]").attr("selected", true);
                    editStore(data.store.stateProvinceGeoId,data.store.cityGeoId,data.store.countyGeoId);
                }
            });
        }
        //省市区三级联动
        function editStore(stateProvinceGeoId,city,countyGeoId){
            if ($('#AddForm').length) {
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddForm_countryGeoId', 'AddForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName',stateProvinceGeoId);
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddForm_stateProvinceGeoId', 'AddForm_cityGeoId', 'stateList', 'geoId', 'geoName',city);
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddForm_cityGeoId', 'AddForm_countyGeoId', 'stateList', 'geoId', 'geoName',countyGeoId);
                //国家
                $("#AddForm_countryGeoId").change(function (e, data) {
                    getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddForm_countryGeoId', 'AddForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName');
                });
                $("#AddForm_stateProvinceGeoId").change(function (e, data) {
                    //省
                    getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddForm_stateProvinceGeoId', 'AddForm_cityGeoId', 'stateList', 'geoId', 'geoName');
                });
                $("#AddForm_cityGeoId").change(function (e, data) {
                    /* 市*/
                    getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddForm_cityGeoId', 'AddForm_countyGeoId', 'stateList', 'geoId', 'geoName');
                });
            }
            $('#add_Modal').modal('show');
        }

    });
    //图片弹框show
    function imageManage() {
        $.chooseImage.show()
    }
    //禁用店铺按钮事件
    function del(id){
        del_ids = id;
        //设置删除弹出框内容
        $('#modal_confirm #modal_confirm_body').html("确定禁用此店铺?");
        $('#modal_confirm').modal('show');
    }
</script>