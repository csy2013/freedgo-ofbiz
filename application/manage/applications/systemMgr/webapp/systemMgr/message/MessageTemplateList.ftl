<!-- 内容start -->
<div class="box box-info">
  <div class="box-body">
    <!-- 条件查询start -->
    <form id="Message_QueryForm" class="form-inline clearfix" onsubmit="return false;">
      <div class="form-group">
        <div class="input-group m-b-10">
          <span class="input-group-addon">模板类型</span>
            <select class="form-control" id="templateType" name="templateType" style="min-width: 200px;">
                <option value="">全部</option>
                <option value="0">订单类</option>
                <option value="1">商品类</option>
                <option value="2">活动类</option>
            </select>
        </div>
        <div class="input-group m-b-10">
          <span class="input-group-addon">模板名称</span>
          <input type="text" name="templateName" id="templateName" class="form-control" value="">
        </div>
          <div class="input-group m-b-10">
              <span class="input-group-addon">发送方式</span>
              <select class="form-control" id="sendMode" name="sendMode" style="min-width: 200px;">
                  <option value="">全部</option>
                  <option value="0">短信</option>
                  <option value="1">app消息</option>
                  <option value="2">pc系统消息</option>
              </select>
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
                  <button class="btn btn-primary" id="addMessage">添加</button>
              <#if security.hasEntityPermission("SYSTEM_MESSAGE", "_DELETE", session)>
                  <button class="btn btn-primary" id="deleteMessage">删除</button>
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
var mt={
    mtDataTable:'',
    init:function(){
        mt.loadData() ;
        //查询按钮点击事件
        $('#Message_QueryForm #searchBtn').on('click',function(){
            mt.searchByCondition() ;
        });

        $("#addMessage").click(function(){
            window.location.href = "<@ofbizUrl>"+mt.URL.addDataUrl()+"</@ofbizUrl>?operateType=create"
        });
        $("#deleteMessage").click(function(){
            mt.deleteData();
        });
    },
    URL:{
        getDataUrl:function(){return "getMessageTemplateForJson"} ,
        deleteUrl:function(){return "deleteMessageTemplate"},
        addDataUrl:function(){return "addMessageTemplatePage"},
        editDataUrl:function(){return "editMessageTemplatePage"},
        settingIsUseUrl:function(){return "settingIsUse"}
    },
    loadData:function(){
        mt.mtDataTable = $('#data_tbl').dataTable({
            ajaxUrl: mt.URL.getDataUrl(),
            columns:[
                <!-- 是否有审核权限-->
                {"title":"复选框","code":"messageTemplateId","checked":true},
                {"title":"消息编号","code":"messageTemplateId","sort":true},
                {"title":"模板类型","code":"templateType",
                    "handle":function(td,record){
                        if(record.templateType == "0"){
                            td.html("订单类");
                        }else if(record.templateType == "1"){
                            td.html("商品类");
                        }else if(record.templateType == "2"){
                            td.html("活动类");
                        }
                    }},
                {"title":"消息模板名称","code":"templateName"},
                {"title":"发送方式","code":"sendMode",
                    "handle":function(td,record){
                        if(record.sendMode == "0"){
                            td.html("短信");
                        }else if(record.sendMode == "1"){
                            td.html("app消息");
                        }else if(record.sendMode == "2"){
                            td.html("PC系统消息");
                        }
                    }},
                {"title":"是否启用","code":"isUse",
                    "handle":function(td,record){
                        td.empty();
                        <#if security.hasEntityPermission("SYSTEM_MESSAGE", "_UPDATE", session)>
                            if(record.isUse == "1"){
                                td.append("<button type='button' onclick='javascript:mt.changeIsUse("+record.messageTemplateId+",0)' class='btn btn-info'>是</button>");
                            }else{
                                td.append("<button type='button' onclick='javascript:mt.changeIsUse("+record.messageTemplateId+",1)' class='btn btn-default'>否</button>");
                            }
                        <#else>
                            if(record.isUse == "0"){
                                td.html("是");
                            }else{
                                td.html("否");
                            }
                        </#if>
                            <!-- 是否有审核权限-->
                        <#--<#if security.hasEntityPermission("BUSINESSMGR_LIST", "_AUDIT", session)>
                            if(record.statusId == "PARTY_ENABLED"){
                                td.html("<button type='button' onclick='javascript:editBusinessIsUse(\""+record.partyId+"\",\"PARTY_DISABLED\");' class='btn btn-info'>是</button>");
                            }else{
                                td.html("<button type='button' onclick='javascript:editBusinessIsUse(\""+record.partyId+"\",\"PARTY_ENABLED\");' class='btn btn-default'>否</button>");
                            }
                        <#else>
                            if(record.statusId == "0"){
                                td.html("是");
                            }else{
                                td.html("否");
                            }
                        </#if>
                        }else{
                            if(record.statusId == "0"){
                                td.html("是");
                            }else{
                                td.html("否");
                            }
                        }-->
                    }
                },
                {"title":"操作","code":"option",
                    "handle":function(td,record){
                        var btnGroup = "<div class='btn-group'>"
                        				+ "<button type='button' class='btn-info btn btn-danger btn-sm' onclick='javascript:mt.editData("
                        				+ record.messageTemplateId
                        				+ ")'>编辑</button><button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'><span class='caret'></span><span class='sr-only'>Toggle Dropdown</span></button><ul class='dropdown-menu' role='menu'><li><a href='javascript:del("
                        				+ record.messageTemplateId
                        				+ ")'>删除</a></li></ul></div>" ;
                        td.html(btnGroup);
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
        var templateType = $('#Message_QueryForm select[name=\'templateType\']').find("option:selected").val();
        var templateName = $('#Message_QueryForm input[name=\'templateName\']').val();
        var sendMode = $('#Message_QueryForm select[name=\'sendMode\']').find("option:selected").val();
        var search_AjaxUrl = mt.changeURLArg(mt.URL.getDataUrl(),"templateType",templateType);
        search_AjaxUrl = mt.changeURLArg(search_AjaxUrl,"templateName",templateName);
        search_AjaxUrl = mt.changeURLArg(search_AjaxUrl,"sendMode",sendMode);
        mt.mtDataTable.reload(search_AjaxUrl);
        return false;
    },
    editData:function(id){
        if(id){
            window.location.href = "<@ofbizUrl>"+mt.URL.editDataUrl()+"</@ofbizUrl>?messageTemplateId="+id ;
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
        $.post(mt.URL.deleteUrl(),{"mtIds":idArr.toString()},function(data){
            $.tipLayer("操作成功！");
            mt.mtDataTable.reload(mt.URL.getDataUrl());
        })
    },
    changeIsUse:function(id,isUse){
        $.post(mt.URL.settingIsUseUrl(),{"messageTemplateId":id,"isUse":isUse},function(data){
            //$.tipLayer("操作成功！");
            mt.mtDataTable.reload(mt.URL.getDataUrl());
        })
    }
}

// 删除操作
function del(id) {
	$.post(mt.URL.deleteUrl(),{"mtIds":id},function(data){
	    $.tipLayer("操作成功！");
	    mt.mtDataTable.reload(mt.URL.getDataUrl());
	})
}

$(function(){
    mt.init() ;
});
	

</script><!-- script区域end -->
