<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->

        <#--<form id="QueryForm" class="form-inline clearfix" onsubmit="return false;" >-->
            <#--<div class="form-group">-->
                <#--<div class="input-group m-b-10">-->
                    <#--<span class="input-group-addon">礼品卡片名称</span>-->
                    <#--<input type="text" id="cardName" name="cardName" class="form-control" value="">-->
                <#--</div>-->

            <#--</div>-->
                <#--<div class="input-group pull-right m-l-10">-->
                    <#--<button id="searchBtn" class="btn btn-success btn-flat">搜索</button>-->
                <#--</div>-->
        <#--</form>-->
        <!-- 条件查询end -->

        <!-- 分割线start -->
        <#--<div class="cut-off-rule bg-gray"></div>-->
        <!-- 分割线end -->

        <!--工具栏start -->
        <div class="row m-b-10">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <!-- 是否有新增权限-->
                <#if security.hasEntityPermission("PRESENTCARD_LIST", "_CREATE", session)>
                    <button id="btn_add" class="btn btn-primary">
                        <i class="fa fa-plus"></i>添加
                    </button>
                </#if>
                    <!-- 是否有删除权限-->
                <#--<#if security.hasEntityPermission("PRESENTCARD_LIST", "_DELETE", session)>-->
                    <#--<button id="btn_del" class="btn btn-primary">-->
                        <#--<i class="fa fa-trash"></i>删除-->
                    <#--</button>-->
                <#--</#if>-->
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

<!-- 删除确认弹出框start -->
<div id="modal_confirm_batch"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
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
                <h4 class="modal-title" id="modal_add_title">新增卡片</h4>
            </div>
            <div class="modal-body">
                <form id="AddForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>savePresentCard</@ofbizUrl>">
                    <input type="hidden" id="status" name="status" value="Y"/>
                    <input type="hidden" id="presentCardId" name="presentCardId"/>
                    <div class="form-group" data-type="required" data-mark="卡片名称">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>卡片名称:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="cardName" name="cardName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group" data-type="required" data-mark="卡片图片">
                        <label class="col-sm-3 control-label"><i class="required-mark">*</i>卡片图片:</label>
                        <div class="col-sm-4" >
                            <input style="margin-left:5px;" type="button" id="imgUrl" value="选择图片"/>
                            <#--<input type="hidden" name="imgUrl" id="imgUrlHidden" value="">-->
                            <input type="hidden" class="dp-vd" name="contentId" id="contentId"/>
                            <p class="dp-error-msg"></p>
                        </div>

                        <div class="col-sm-5">
                            <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 200*200px</div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">预览图片:</label>
                        <div class="col-sm-9">
                            <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">卡片描述:</label>
                        <div class="col-sm-9">
                            <textarea class="form-control" name="cardDesc" id="cardDesc" rows="6" value=""></textarea>
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
                <h4 class="modal-title" id="modal_edit_title">编辑卡片</h4>
            </div>
            <div class="modal-body">
                <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>saveHelpCategory</@ofbizUrl>">
                    <input type="hidden" id="status" name="status" value=""/>
                    <input type="hidden" class="form-control" id="presentCardId" name="presentCardId">
                    <div class="form-group" data-type="required" data-mark="卡片名称">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>卡片名称:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="cardName" name="cardName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="卡片图片">
                        <label class="col-sm-3 control-label"><i class="required-mark">*</i>分类图片:</label>
                        <div class="col-sm-4">
                            <input style="margin-left:5px;" type="button" id="imgUrl_edit" value="选择图片"/>
                            <#--<input type="hidden" name="imgUrl" value="">-->
                            <input type="hidden" class="dp-vd" name="contentId" id="contentId"/>
                        </div>
                        <div class="col-sm-5">
                            <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 200*200px</div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">预览图片:</label>
                        <div class="col-sm-9">
                            <img alt="" src="" id="img_edit" style="max-height: 100px;max-width: 200px;">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">卡片描述:</label>
                        <div class="col-sm-9">
                            <textarea class="form-control" name="cardDesc" id="cardDesc" rows="6" value=""></textarea>
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
                //清空预览图片
                $('#modal_add #img').attr("src",null);
                $('#modal_add #contentId').val('');
                $("#AddForm input[name='isShow']").eq(0).prop("checked",true) ;
                //设置提示弹出框内容
                $('#modal_add').modal();
            });
        },
        URL:{
            getDataUrl:function(){return "getPresentCardData"} ,
            deleteUrl:function(){return "deletePresentCard"},
            findOneUrl:function(){return "findPresentCardById"},
            saveDataUrl:function(){return "savePresentCard"}
        },
        loadData:function(){
            hc.hcDataTable = $('#data_tbl').dataTable({
                ajaxUrl: hc.URL.getDataUrl(),
                columns:[
                    <!-- 是否有审核权限-->
//                    {"title":"复选框","code":"presentCardId","checked":true},
                    {"title":"卡片名称","code":"cardName"},
                    {"title":"卡片图片","code":"contentId",
                        "handle":function(td,record){
                            if(record.contentId){
                                td.html("<img  src='/content/control/getImage?contentId="+record.contentId+"' style='width: 50px;height: 40px;'></img>");
                            }
                        }},
                    {"title":"卡片描述","code":"cardDesc"}

                    <!-- 是否有编辑权限-->
                    <#if security.hasEntityPermission("PRESENTCARD_LIST", "_DELETE", session)>
                    ,
                        {"title":"操作","code":"option",
                            "handle":function(td,record){
                                var btns = "<div class='btn-group'>"+
                                        "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:setDelete("+record.presentCardId+");'>删除</button>"+
                                        "</div>";
                                td.append(btns);
                            }
                        }
                    </#if>
                    <#--{"title":"操作","code":"option",-->

                        <#--"handle":function(td,record){-->
                            <#--var btns = "<div class='btn-group'>"+-->
                                    <#--<!-- 是否都有权限&ndash;&gt;-->
                                    <#--"<button type='button' class='btn btn-danger btn-sm'  onclick='javascript:hc.editData("+record.presentCardId+")'>编辑</button>"+-->
                                    <#--"<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+-->
                                    <#--"<span class='caret'></span>"+-->
                                    <#--"<span class='sr-only'>Toggle Dropdown</span>"+-->
                                    <#--"</button>"+-->
                                    <#--"<ul class='dropdown-menu' role='menu'>"+-->
                                    <#--"<li class='edit_li'>" +-->
                                    <#--<#if security.hasEntityPermission("PRESENTCARD_LIST", "_DELETE", session)>-->
                                    <#--"<a href=\"#\" class=\"gss_Up\"  onclick='javascript:setDelete("+record.presentCardId+")'>删除</a> </li>"+-->
                                    <#--</#if>-->
                                    <#--"</ul>"+-->

                                    <#--"</div>";-->
                            <#--td.append(btns);-->
                        <#--}-->
                    <#--}-->
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
            var cardName = $('#QueryForm #cardName').val();
            var search_AjaxUrl = hc.changeURLArg(hc.URL.getDataUrl(),"cardName",cardName);
            hc.hcDataTable.reload(search_AjaxUrl);
            return false;
        },

        editData:function(id){
            if(id){
                //清空form
                clearForm($("#EditForm"));
                $.ajax({
                    url: hc.URL.findOneUrl(),
                    type: "GET",
                    data : {presentCardId:id},
                    dataType : "json",
                    success: function(data){
                        $('#modal_edit #presentCardId').val(data.presentCard.presentCardId);
                        $("#modal_edit input[name='contentId']").val(data.presentCard.contentId);
                        $("#modal_edit input[name='status']").val(data.presentCard.status);
                        $('#modal_edit #cardName').val(data.presentCard.cardName);
                        $('#modal_edit #cardDesc').val(data.presentCard.cardDesc);
                        $('#modal_edit #img_edit').attr("src","/content/control/getImage?contentId="+data.presentCard.contentId);
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
            
	        //设置提示弹出框内容
	        $('#modal_confirm_batch #modal_confirm_title').html("删除提示");
	        $('#modal_confirm_batch #modal_confirm_body').html("帮助分类删除后无法再被使用，是否继续？");
	        $('#modal_confirm_batch').modal('show');
	        
            //删除弹出框确定按钮点击事件
	        $('#modal_confirm_batch #ok').click(function(e){
	            $.post(hc.URL.deleteUrl(),{"hcIds":idArr.toString()},function(data){
	                $.tipLayer("操作成功！");
	                hc.hcDataTable.reload(hc.URL.getDataUrl());
	            });
	        });
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

    var del_ids;  //行删除Id
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
                        // 清空图片地址
                        $("#AddForm #contentId").val('');
                        //隐藏新增弹出窗口
                        $('#modal_add').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            console.log("新增跳转："+hc.URL.getDataUrl());
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

        //删除弹出框确定按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用删除方法
            $.ajax({
                url: hc.URL.deleteUrl(),
                type: "GET",
                data: {pcIds : del_ids},
                dataType : "json",
                success: function(data){
                    //弹出提示信息
                    $.tipLayer("操作成功！");
                    hc.hcDataTable.reload(hc.URL.getDataUrl());
                },
                error: function(data){
                    //弹出提示信息
                    $.tipLayer("操作成功！");
                }
            });
        });


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
                $('#img_edit').attr({"src":"/content/control/getImage/stream?contentId="+data.uploadedFile0});
                $('#img').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
                $("input[name='imgUrl']").val("/content/control/getImage?contentId="+data.uploadedFile0) ;
                $("#modal_add #contentId").val(data.uploadedFile0);
                $("#modal_edit #contentId").val(data.uploadedFile0);
                $('#modal_add').modal();
            })
        });

        // 图片选择控件显示
        $('#imgUrl').click(function(){
            $.chooseImage.show()
            $('#modal_add').modal('hide');
        });
        $("#imgUrl_edit").click(function(){
            $.chooseImage.show();
            $('#modal_add').modal('hide');
        })
    });

    //行删除操作
    function setDelete(id){
        del_ids = id;
        //设置提示弹出框内容
        $('#modal_confirm #modal_confirm_title').html("删除提示");
        $('#modal_confirm #modal_confirm_body').html("该礼品卡片信息删除后将无法使用，是否继续？");
        $('#modal_confirm').modal('show');
    }
</script><!-- script区域end -->
