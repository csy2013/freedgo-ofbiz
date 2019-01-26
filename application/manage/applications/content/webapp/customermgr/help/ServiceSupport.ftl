<!-- Select2 -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/dist/css/AdminLTE.min.css</@ofbizContentUrl>">
<style>.content-wrapper{padding-top: 0px;}</style>
<!-- Select2 -->
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
       <#-- <form id="QueryForm" class="form-inline clearfix" onsubmit="return false;">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">分类名称</span>
                    <input type="text" id="categoryName" class="form-control" value="">
                </div>

                <div class="input-group pull-right m-l-10">
                    <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
                </div>
            </div>
        </form>--><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <!--工具栏start -->
        <div class="row m-b-10">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <!-- 是否有新增权限-->
                <#--<#if security.hasEntityPermission("CONSULT_HELPCATEGORY", "_CREATE", session)>-->
                    <button id="btn_add" class="btn btn-primary">
                        <i class="fa fa-plus"></i>添加
                    </button>
               <#-- </#if>-->
                    <!-- 是否有删除权限-->
                <#--<#if security.hasEntityPermission("CONSULT_HELPCATEGORY", "_DELETE", session)>-->
                    <button id="btn_del" class="btn btn-primary">
                        <i class="fa fa-trash"></i>删除
                    </button>
               <#-- </#if>-->
                </div>
            </div><!-- 操作按钮组end -->
            <!-- 列表当前分页条数start -->
            <div class="col-sm-6">
                <div id="view_size" class="dp-tables_length">
                </div>
            </div><!-- 列表当前分页条数end -->
        </div><!-- 工具栏end -->
        <!-- 表格区域start -->
        <div class="row">
            <div class="col-sm-12">
                <table id="data_tbl" class="table table-bordered table-hover js-checkparent">
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <div class="row" id="paginateDiv">
        </div><!-- 分页条end -->
    </div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- 删除确认弹出框start -->
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
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">删除</button>
            </div>
        </div>
    </div>
</div><!-- 删除确认弹出框end -->

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

<!-- 新增弹出框start -->
<div id="modal_add"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">添加服务支持</h4>
            </div>
            <div class="modal-body">
                <form id="AddForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>savesaveServiceSupport</@ofbizUrl>" onsubmit="return false">
                    <div class="form-group">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>服务支持编号:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="serviceSupportId" name="serviceSupportId" readonly>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="服务支持名称">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>服务支持名称:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="serviceSupportName" name="serviceSupportName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">服务支持图标:</label>
                        <div class="col-sm-9">
                            <input style="margin-left:5px;" type="button" id="imgUrl" value="选择图片"/>
                            <input type="hidden" name="imgUrl" value="">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">预览图片:</label>
                        <div class="col-sm-9">
                            <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                        </div>
                    </div>

                    <div class="form-group" data-type="required" data-mark="帮助标题">
                    <#assign helpInfoList = delegator.findByAnd("HelpInfo",{})/>
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>帮助标题:</label>
                        <div class="col-sm-8">
                            <select class="form-control dp-vd" id="helpInfoId" name="helpInfoId">
                            	<option value="" selected="selected">======请选择======</option>
	                            <#list helpInfoList as helpInfoVO>
	                                <option value="${helpInfoVO.helpInfoId}">${helpInfoVO.helpTitle}</option>
	                            </#list>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 新增弹出框end -->

<!-- 修改弹出框start -->
<div id="modal_edit"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_edit_title">编辑服务支持</h4>
            </div>
            <div class="modal-body">
                <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>saveServiceSupport</@ofbizUrl>">
                    <div class="form-group">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>服务支持编号:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="serviceSupportId" name="serviceSupportId" readonly>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="服务支持名称">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>服务支持名称:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="serviceSupportName" name="serviceSupportName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">服务支持图标:</label>
                        <div class="col-sm-9">
                            <input style="margin-left:5px;" type="button" id="imgUrl_edit" value="选择图片"/>
                            <input type="hidden" name="imgUrl" value="">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">预览图片:</label>
                        <div class="col-sm-9">
                            <img alt="" src="" id="img_edit" style="max-height: 100px;max-width: 200px;">
                        </div>
                    </div>

                    <div class="form-group" data-type="required" data-mark="帮助标题">
                    <#assign helpInfoList = delegator.findByAnd("HelpInfo",{})/>
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>帮助标题:</label>
                        <div class="col-sm-8">
                            <select class="form-control dp-vd" id="helpInfoId" name="helpInfoId">
                                <option value="">======请选择======</option>
                            <#list helpInfoList as helpInfoVO>
                                <option value="${helpInfoVO.helpInfoId}">${helpInfoVO.helpTitle}</option>
                            </#list>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 修改弹出框end -->



<!-- script区域start -->
<script>
    var hc={
        hcDataTable:'',
        init:function(){
            hc.loadData() ;
            //查询按钮点击事件
            $('#QueryForm #searchBtn').on('click',function(){
                hc.searchByCondition() ;
            });

            // 删除操作
            $("#btn_del").click(function(){
                hc.deleteData();
            });

            //新增弹出窗关闭事件
            $('#modal_add').on('hide.bs.modal', function () {
                $('#AddForm').dpValidate({
                    clear: true
                });
            })

            //修改弹出窗关闭事件
            $('#modal_edit').on('hide.bs.modal', function () {
                $('#EditForm').dpValidate({
                    clear: true
                });
            });

            //新增按钮点击事件
            $('#btn_add').click(function(){
                //清空form
                clearForm($("#AddForm"));
                $('#img').attr({"src":""});
                $("#AddForm input[name='imgUrl']").val(null) ;
                //设置提示弹出框内容
                $('#modal_add').modal();
            });
        },
        URL:{
            getDataUrl:function(){return "getServiceSupportData"} ,
            deleteUrl:function(){return "deleteServiceSupport"},
            findOneUrl:function(){return "findServiceSupport"},
            saveDataUrl:function(){return "saveServiceSupport"}
        },
        loadData:function(){
            hc.hcDataTable = $('#data_tbl').dataTable({
                ajaxUrl: hc.URL.getDataUrl(),
                columns:[
                    <!-- 是否有审核权限-->
                    {"title":"复选框","code":"serviceSupportId","checked":true},
                    {"title":"服务支持编号","code":"serviceSupportId"},
                    {"title":"服务支持图标","code":"imgUrl",
                        "handle":function(td,record){
                            if(record.imgUrl){
                                td.html("<img src='"+record.imgUrl+"' style='width: 50px;height: 40px;'></img>");
                            }
                        }},
                    {"title":"服务支持名称","code":"serviceSupportName"},
                    {"title":"帮助标题","code":"helpTitle"},
                    {"title":"创建时间","code":"createdStamp","handle":function(td,record){
                        console.log(record.createdStamp.time) ;
                        td.html((new Date(record.createdStamp.time)).Format("yyyy-MM-dd hh:mm:ss"));
                    }},
                    {"title":"操作","code":"option",

                        "handle":function(td,record){
                            var btns = "<div class='btn-group'>"+
                                    <!-- 是否都有权限-->
                                    "<button type='button' class='btn btn-danger btn-sm'  onclick='javascript:hc.editData("+record.serviceSupportId+")'>编辑</button>"+
                                    "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
                                    "<span class='caret'></span>"+
                                    "<span class='sr-only'>Toggle Dropdown</span>"+
                                    "</button>"+
                                    "<ul class='dropdown-menu' role='menu'>"+
                                    "<li class='edit_li'>" +
                                    <#if security.hasEntityPermission("CONSULT_SERVICESUPPORT", "_DELETE", session)>
                                    "<a href=\"#\" class=\"gss_Up\"  onclick='javascript:hc.deleteOnlyOneData("+record.serviceSupportId+")'>删除</a> </li>"+
                                    </#if>
                                    "</ul>"+

                                    "</div>";
                            td.append(btns);
                        }
                    }
                ],
                listName: "recordsList",
                paginateEL: "paginateDiv",
                viewSizeEL: "view_size"
            });
        },
        changeURLArg:function(url,arg,arg_val){
            if ('undefined' == typeof arg_val || "" == arg_val)
                return  url ;
            var pattern=arg+'=([^&]*)';
            var replaceText=arg+'='+arg_val;
            if(url.match(pattern)){
                var tmp='/('+ arg+'=)([^&]*)/gi';
                tmp=url.replace(eval(tmp),replaceText);
                return tmp;
            }else{
                if(url.match('[\?]')){
                    return url+'&'+replaceText;
                }else{
                    return url+'?'+replaceText;
                }
            }
            return  url+'\n'+arg+'\n'+arg_val ;
        },
        searchByCondition:function(){
            var categoryName = $('#QueryForm #categoryName').val();
            var search_AjaxUrl = hc.changeURLArg(hc.URL.getDataUrl(),"categoryName",categoryName);
            hc.hcDataTable.reload(search_AjaxUrl);
            return false;
        },
        editData:function(id){
            if(id){
                //清空form
                clearForm($("#EditForm"));
                $('#img_edit').attr({"src":""});
                $("#EditForm input[name='imgUrl']").val(null) ;
                $.ajax({
                    url: hc.URL.findOneUrl(),
                    type: "GET",
                    data : {serviceSupportId:id},
                    dataType : "json",
                    success: function(data){
                        console.log(data) ;
                        $('#modal_edit #serviceSupportId').val(data.serviceSupportVO.serviceSupportId);
                        $("#modal_edit input[name='imgUrl']").val(data.serviceSupportVO.imgUrl);
                        $('#modal_edit #serviceSupportName').val(data.serviceSupportVO.serviceSupportName);
                        $('#modal_edit #img_edit').attr("src",data.serviceSupportVO.imgUrl);
                        $('#modal_edit #helpInfoId').find("option").each(function(i,e){
                            if($(e).val() == data.serviceSupportVO.helpInfoId){
                                $(e).attr("selected",true);
                            }
                        });
                        $(".select2HelpInfoName_edit").select2();
                        $('#modal_edit').modal();
                    },
                    error: function(data){
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                        $('#modal_msg').modal();
                    }
                });
            }
            return false ;
        },
        deleteData:function(){
            var idArr = new Array() ;
            $("#data_tbl .js-checkchild:checked").each(function(){
                idArr.push($(this).val()) ;
            });
            if(idArr.isEmpty()){
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
                return false;
            }
            $.post(hc.URL.deleteUrl(),{"ssIds":idArr.toString()},function(data){
                $.tipLayer("操作成功！");
                hc.hcDataTable.reload(hc.URL.getDataUrl());
            })
        },
        deleteOnlyOneData:function(id){
            $.post(hc.URL.deleteUrl(),{"ssIds":id},function(data){
                $.tipLayer("操作成功！");
                hc.hcDataTable.reload(hc.URL.getDataUrl());
            })
        },
        changeIsUse:function(id,isUse){
            $.post(hc.URL.settingIsUseUrl(),{"helpCategoryId":id,"isUse":isUse},function(data){
                $.tipLayer("操作成功！");
                hc.hcDataTable.reload(hc.URL.getDataUrl());
            })
        }
    }



    Date.prototype.Format = function(fmt){ //author: meizz
        var o = {
            "M+" : this.getMonth()+1,                 //月份
            "d+" : this.getDate(),                    //日
            "h+" : this.getHours(),                   //小时
            "m+" : this.getMinutes(),                 //分
            "s+" : this.getSeconds(),                 //秒
            "q+" : Math.floor((this.getMonth()+3)/3), //季度
            "S"  : this.getMilliseconds()             //毫秒
        };
        if(/(y+)/.test(fmt))
            fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        for(var k in o)
            if(new RegExp("("+ k +")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
        return fmt;
    }

    $(function(){
        hc.init() ;
        $("#AddForm").dpValidate({
            validate: true,
            callback: function(){
                //异步调用新增方法
                $.ajax({
                    url: hc.URL.saveDataUrl(),
                    type: "POST",
                    data: $('#AddForm').serialize(),
                    dataType : "json",
                    success: function(data){
                        console.log(data) ;
                        //隐藏新增弹出窗口
                        $('#modal_add').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            hc.hcDataTable.reload(hc.URL.getDataUrl());
                        })
                    },
                    error: function(data){
                        //隐藏新增弹出窗口
                        $('#modal_add').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });
        $("#EditForm").dpValidate({
            validate: true,
            callback: function(){
                //异步调用新增方法
                $.ajax({
                    url: hc.URL.saveDataUrl(),
                    type: "POST",
                    data: $('#EditForm').serialize(),
                    dataType : "json",
                    success: function(data){
                        console.log(data) ;
                        //隐藏新增弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            hc.hcDataTable.reload(hc.URL.getDataUrl());
                        })
                    },
                    error: function(data){
                        //隐藏新增弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });
        //新增弹出框保存按钮点击事件
        $('#modal_add #save').click(function(){
            $("#AddForm").dpValidate({
                clear: true
            });
            $('#AddForm').submit();
        });


        //修改弹出框保存按钮点击事件
        $('#modal_edit #save').click(function(){
            $("#EditForm").dpValidate({
                clear: true
            });
            $('#EditForm').submit();
        });


        $(".select2HelpInfoName").select2();
        /***************************************************************************************************/
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
        $('body').on('click','.img-submit-btn',function(){
            var obj = $.chooseImage.getImgData();
            $.chooseImage.choose(obj,function(data){
                $('#img_edit').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
                $('#img').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
                $("#EditForm input[name='imgUrl']").val("/content/control/getImage?contentId="+data.uploadedFile0) ;
                $("#AddForm input[name='imgUrl']").val("/content/control/getImage?contentId="+data.uploadedFile0) ;
            })
        });

        // 图片选择控件显示
        $('#imgUrl').click(function(){
            $.chooseImage.show();
        });
        $("#imgUrl_edit").click(function(){
            $.chooseImage.show();
        })
    });

</script><!-- script区域end -->
