<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="Message_QueryForm" class="form-inline clearfix" onsubmit="return false;">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">帮助标题</span>
                    <input type="text" name="helpTitle" id="helpTitle" class="form-control" value="">
                </div>

            </div>
            <div class="input-group pull-right">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
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
                    <!-- 是否有修改权限-->
                <#if security.hasEntityPermission("CONSULT_HELPINFO", "_CREATE", session)>
                    <button class="btn btn-primary" id="btnAddData">添加</button>
                </#if>
                <#if security.hasEntityPermission("CONSULT_HELPINFO", "_DELETE", session)>
                    <button class="btn btn-primary" id="btnDeleteData">删除</button>
                </#if>
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

<!-- 提示弹出框start -->
<div id="modal_msg"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">操作信息</h4>
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

<!-- script区域start -->
<script>
    var hi={
        hiDataTable:'',
        init:function(){
            hi.loadData() ;
            //查询按钮点击事件
            $('#Message_QueryForm #searchBtn').on('click',function(){
                hi.searchByCondition() ;
            });

            $("#btnAddData").click(function(){
                window.location.href = "<@ofbizUrl>"+hi.URL.addDataUrl()+"</@ofbizUrl>?operateType=create"
            });
            $("#btnDeleteData").click(function(){
                hi.deleteData();
            });
        },
        URL:{
            getDataUrl:function(){return "getHelpInfoData"} ,
            deleteUrl:function(){return "deleteHelpInfo"},
            addDataUrl:function(){return "addHelpInfoPage"},
            editDataUrl:function(){return "editHelpInfoPage"}
        },
        loadData:function(){
            hi.hiDataTable = $('#data_tbl').dataTable({
                ajaxUrl: hi.URL.getDataUrl(),
                columns:[
                    <!-- 是否有审核权限-->
                    {"title":"复选框","code":"helpInfoId","checked":true},
                    {"title":"帮助编号","code":"helpInfoId","sort":true},
                    {"title":"分类名称","code":"categoryName"},
                    {"title":"帮助标题","code":"helpTitle"},
                    {"title":"作者","code":"helpAuthor"},
                    {"title":"分类排序","code":"sequenceNum"},
                    {"title":"创建时间","code":"createdStamp","handle":function(td,record){
                        console.log(record.createdStamp.time) ;
                        td.html((new Date(record.createdStamp.time)).Format("yyyy-MM-dd hh:mm:ss"));
                    }},
                    {"title":"操作","code":"option","handle":function(td,record){
                        var btns = "<div class='btn-group'>"+
                                <!-- 是否都有权限-->
                                "<button type='button' class='btn btn-danger btn-sm'  onclick='javascript:hi.editData("+record.helpInfoId+")'>编辑</button>"+
                                "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
                                "<span class='caret'></span>"+
                                "<span class='sr-only'>Toggle Dropdown</span>"+
                                "</button>"+
                                "<ul class='dropdown-menu' role='menu'>"+
                                "<li class='edit_li'>" +
                                <#if security.hasEntityPermission("CONSULT_HELPINFO", "_DELETE", session)>
                                "<a href=\"#\" class=\"gss_Up\"  onclick='javascript:hi.deleteOne("+record.helpInfoId+")'>删除</a> </li>"+
                                </#if>
                                "</ul></div>";
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
            var helpTitle = $('#Message_QueryForm input[name=\'helpTitle\']').val();
            var search_AjaxUrl = hi.changeURLArg(hi.URL.getDataUrl(),"helpTitle",helpTitle);
            hi.hiDataTable.reload(search_AjaxUrl);
            return false;
        },
        editData:function(id){
            if(id){
                 window.location.href = "<@ofbizUrl>"+hi.URL.editDataUrl()+"</@ofbizUrl>?helpInfoId="+id ;
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
            $.post(hi.URL.deleteUrl(),{"hIds":idArr.toString()},function(data){
                if(data._ERROR_MESSAGE_){
                    $.tipLayer(data._ERROR_MESSAGE_) ;
                }else{
                    $.tipLayer("操作成功！");
                    hi.hiDataTable.reload(hi.URL.getDataUrl());
                }
            })
        },
        deleteOne:function(id){
            if(!id)
                return false ;
            $.post(hi.URL.deleteUrl(),{"hIds":id},function(data){
                if(data._ERROR_MESSAGE_){
                    $.tipLayer(data._ERROR_MESSAGE_) ;
                }else{
                    $.tipLayer("操作成功！");
                    hi.hiDataTable.reload(hi.URL.getDataUrl());
                }
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

    $(function(){
        hi.init() ;
    });


</script><!-- script区域end -->
