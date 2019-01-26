<div class="box box-info">
    <div class="box-body">

        <div class="box-header">
            <h3 class="box-title m-t-10">导航分组列表</h3>
        </div>
        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <div class="row m-b-12" style="margin-bottom:15px;">
            <div class="col-sm-6">
                <div class="dp-tables_btn" >
                    <button id="btn_add" class="btn btn-primary">
                        <i class="fa fa-plus">添加</i>
                    </button>
                </div>
            </div>
        </div>



        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover">
                    <thead>
                    <tr>
                        <!--<th><input type="checkbox"></th>-->
                        <th>导航分组名称</th>
                        <th>站点</th>
                        <th>是否启用</th>
                        <th>是否显示品牌</th>
                        <th>操作</th>

                    </tr>
                    </thead>
                    <tbody>
                    <#if navigationGroupList?has_content>
                        <#list navigationGroupList as ng>

                            <tr>
                            <#--<td></td>-->
                                <td>${ng.navigationGroupName}</td>
                                <td>
                                    <#if ng.isAllSite?default("0") == "1">
                                        全部站点
                                    <#else>
                                        <#assign sites = delegator.findByAnd("NavigationGroupSite",{"navigationGroupId" : ng.navigationGroupId}) />
                                        <#list sites as s>
                                            <#assign site = delegator.findByPrimaryKey("WebSite",{"webSiteId" : s.siteId}) />
                                            ${site.siteName}&nbsp;&nbsp;
                                        </#list>
                                    </#if>

                                </td>
                                <td>
                                    <#if (ng.isEnable)?default("0") == "0">
                                        <button type='button' class='btn btn-defult' value="0" id="${ng.navigationGroupId}" onclick="changeNavigationGroup(this,'isEnable')">否</button>
                                    <#else>
                                        <button type='button' class='btn btn-primary' value="1" id="${ng.navigationGroupId}" onclick="changeNavigationGroup(this,'isEnable')">是</button>
                                    </#if>
                                </td>
                                <td>
                                    <#if (ng.isShowBrand)?default("0") == "0">
                                        <button type='button' class='btn btn-defult' value="0" id="${ng.navigationGroupId}" onclick="changeNavigationGroup(this,'isShowBrand')">否</button>
                                    <#else>
                                        <button type='button' class='btn btn-primary' value="1" id="${ng.navigationGroupId}" onclick="changeNavigationGroup(this,'isShowBrand')">是</button>
                                    </#if>
                                </td>
                                <td>
                                    <div class='btn-group'>
                                        <button type='button' class='btn btn-danger btn-sm' onclick='baseSet("${ng.navigationGroupId}")'>基本设置</button>
                                        <button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>
                                            <span class='caret'></span>
                                            <span class='sr-only'>Toggle Dropdown</span>
                                        </button>
                                        <ul class='dropdown-menu' role='menu'>
                                            <li><a href='javascript:setNavigation("${ng.navigationGroupId}")'>导航设置</a></li>
                                            <li><a href='javascript:delCategoryNavigation("${ng.navigationGroupId}")'>删除</a></li>
                                        </ul>
                                    </div>
                                </td>
                            </tr>
                        </#list>
                    <#else>
                        <tr>
                            <td colspan="5">没有任何数据</td>
                        </tr>
                    </#if>


                    </tbody>
                </table>
            </div>
        </div>
    </div>
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

<script>
    $(function() {
        $("#btn_add").click(function () {
            window.location.href = "<@ofbizUrl>editCategoryNavigation</@ofbizUrl>";
        });

    });

    function delCategoryNavigation(id){
        if (confirm("确定删除你所选的数据？")){
            $.ajax({
                url: "delCategoryNavigation",
                type: "GET",
                data: {
                    "navigationGroupIds":id
                },
                dataType : "json",
                success: function(data){
                    window.location.href = "<@ofbizUrl>categoryNavigation</@ofbizUrl>";
                },
                error: function(data){
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        }
    }

    function setNavigation(id){
        window.location.href = "<@ofbizUrl>navigation?navigationGroupId=" + id  + "</@ofbizUrl>";
    }

    function baseSet(id){
        window.location.href = "<@ofbizUrl>editCategoryNavigation?navigationGroupId=" + id  + "</@ofbizUrl>";
    }
    
    function changeNavigationGroup(obj,type){
        var obj = $(obj);
        var newStatus = "";
        var navigationGroupId =  obj.attr("id");
        if (obj.attr("value") == "0"){
            newStatus = "1";
        }else{
            newStatus = "0";
        }
        $.post("changeNavigationGroup",{newStatus : newStatus,navigationGroupId : navigationGroupId,type : type},function(data){
	        window.location.reload(); 
        });
    }
</script>

