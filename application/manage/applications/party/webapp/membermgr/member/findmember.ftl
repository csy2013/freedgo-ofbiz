<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" class="form-inline clearfix" >
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.MemberNickName}</span>
                    <input type="text" id="nickname" class="form-control"
                           value="">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.MemberTelphone}</span>
                    <input type="text" id="telphone" class="form-control" value="">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.MemberCommunity}</span>
                    <input type="text" id="communtiy" class="form-control"
                           value="">
                </div>
            </div>
            <div class="input-group pull-right">
                 <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
            </div>
        </form>
        <!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->
    	
    	<!--工具栏start -->
	    <div class="row m-b-10">
	      <!-- 操作按钮组start -->
	      <div class="col-sm-6">
	        <div class="dp-tables_btn">
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
    </div>
    <!-- /.box-body -->
</div><!-- 内容end -->

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.MemberOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary"
                        data-dismiss="modal">${uiLabelMap.MemberOk}</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->

<!-- 修改弹出框start -->
<div id="modal_edit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_edit_title">${uiLabelMap.AdjustEdit} <span style="color:red;font-size:4px;">(调整余额可填写正负数，正数增加余额，负数减少余额)</span></h4>

            </div>
            <div class="modal-body">
                <form id="EditForm" method="post" class="form-horizontal" role="form"
                      action="<@ofbizUrl>adjustAmount</@ofbizUrl>">
                    <input type="hidden" id="partyId" name="partyId" />
                    <div class="form-group">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.MemberAmount}:</label>
                        <div class="col-sm-10">
                            <p class="form-control-static" id="oldamount" name="oldamount"></p>
                        </div>
                    </div>
                    <div class="form-group"  data-type="format" data-reg="/^\-?[1-9]+\d*(\.\d{1,2})?$/"  data-mark="${uiLabelMap.AdjustAcount}">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.AdjustAcount}:</label>


                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" id="adjustamount" maxlength="11" name="amount">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.AdjustCause}" >
                        <label class="control-label col-sm-2" ><i class="required-mark">*</i>${uiLabelMap.AdjustCause}:</label>
                        <div class="col-sm-10">
                            <textarea class="form-control dp-vd" rows="6" name="adjustCause"  id="adjustCause"></textarea>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default"                                                                          j
                        data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 修改弹出框end -->

<!-- 余额明细begin -->
<div id="modal_list" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_list_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_list_title">${uiLabelMap.AdjustList}</h4>
            </div>
            <div class="modal-body">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr class="js-sort-list">
                        <th>${uiLabelMap.AdjustAmount}</th>
                        <th>${uiLabelMap.Operator}</th>
                        <th>${uiLabelMap.EditDate}</th>
                        <th>${uiLabelMap.AdjustDescription}</th>
                    </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<!-- 余额明细end -->

<!-- script区域start -->
<script>
	var data_tbl;
	var ajaxUrl = "findmemberForJson";
    $(function () {
    	data_tbl = $('#data_tbl').dataTable({
			ajaxUrl: ajaxUrl,
			columns:[
				{"title":"会员编码","code":"partyId","sort":true},
				{"title":"昵称","code":"nickname","sort":true},
				{"title":"真实姓名","code":"name","sort":true},
				{"title":"会员等级","code":"partyLevel"},
				{"title":"性别","code":"gender"},
				{"title":"手机号","code":"mobile"},
				{"title":"社区","code":"communtity"},
				{"title":"账户余额","code":"amount"},
				{"title":"操作","code":"option",
				 "handle":function(td,record){
				 	var btns = "<div class='btn-group'>"+
	                  			"<button type='button' class='btn btn-danger btn-sm' onclick='location.href=\"memberDetail?partyId="+record.partyId+"\"'>查看详情</button>"+
	                  			"<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
	                    		"<span class='caret'></span>"+
	                    		"<span class='sr-only'>Toggle Dropdown</span>"+
	                  			"</button>"+
	                  			"<ul class='dropdown-menu' role='menu'>"+
	                  			<!-- 是否有余额明细权限-->
							   	<#if security.hasEntityPermission("PARTYMGR_AMOUNT", "_VIEW", session)>
	                    			"<li><a href='javascript:detailView(\""+record.partyId+"\")'>余额明细</a></li>"+
	                    		</#if>
	                    		<!-- 是否有余额调整权限-->
							   	<#if security.hasEntityPermission("PARTYMGR_AMOUNT", "_UPDATE", session)>
	                    			"<li><a href='javascript:editInit(\""+record.partyId+"\",\""+record.amount+"\")'>余额调整</a></li>"+
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
		
		//查询按钮点击事件
		$('#QueryForm #searchBtn').on('click',function(){
			var nickname = $('#QueryForm #nickname').val();
			var telphone = $('#QueryForm #telphone').val();
			var communtiy = $('#QueryForm #communtiy').val();
			
			ajaxUrl = changeURLArg(ajaxUrl,"nickname",nickname);
			ajaxUrl = changeURLArg(ajaxUrl,"telphone",telphone);
			ajaxUrl = changeURLArg(ajaxUrl,"communtiy",communtiy);
			data_tbl.reload(ajaxUrl);
			return false;
		});
    
        //修改弹出框保存按钮点击事件
        $('#modal_edit #save').click(function () {
            $('#EditForm').dpValidate({
                clear: true
            });

            //减少余额不能大于实际金额
            var oldamount = $('#modal_edit #oldamount').text() || 0,
                    amount = parseFloat($("#modal_edit #adjustamount").val());
            if((parseFloat(oldamount)+amount) < 0){
                $("#modal_edit #adjustamount").next('p').text('调整余额不能大于账户余额');
                return false;
            }
            $('#EditForm').submit();
        });
        //表单验证
        $('#EditForm').dpValidate({
            validate: true,
            callback: function () {
            	var partyId = $("#modal_edit #partyId").val();
            	var amount = parseFloat($("#modal_edit #adjustamount").val());
            	var adjustCause = $("#modal_edit #adjustCause").val();
            
                //异步调用修改方法
                $.ajax({
                    url: "adjustAmount",
                    type: "POST",
                    data: {
                    	partyId : partyId,
                    	amount : amount,
                    	adjustCause : adjustCause
                    },
                    dataType: "json",
                    success: function (data) {
                        //隐藏修改弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            data_tbl.reload(ajaxUrl);
                        })
                    },
                    error: function (data) {
                        //隐藏修改弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

    })
    //点击编辑按钮事件
    function editInit(id,value) {
    	//清空form
	    clearForm($("#EditForm"));
        $("#modal_edit #partyId").val(id);
        $("#modal_edit #oldamount").text(value);
        $('#modal_edit').modal();
    }
    //点击查看余额明细
    function detailView(id){
        $.ajax({
            url: "adjustAmountHistory",
            type: "POST",
            data: {partyId: id},
            dataType: "json",
            success: function (data) {
                var content,
                        list = data.partyAccountDetailList;
                //清空表格,避免叠加
                $('#modal_list tbody').empty();
                for(var i=0; i<list.length;i++){
                    content += "<tr>";
                    content += '<td>'+list[i].amount+'</td>';
                    content += '<td>'+list[i].operator+'</td>';
                    content += '<td>'+list[i].createDate+'</td>';
                    content += '<td>'+list[i].description+'</td>';
                    content += '</tr>';
                }
                $('#modal_list tbody').append(content);
                //明细列表窗口
                $('#modal_list').modal();
            },
            error: function (data) {

            }
        });
    }

</script>
