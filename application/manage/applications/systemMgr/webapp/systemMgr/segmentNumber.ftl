<!-- Main content -->
<#assign commonUrl = "SegmentNumber?" />
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">${uiLabelMap.SegmentNumber}</h3>
        </div>
        <div class="box-body">
            <div class="cut-off-rule bg-gray"></div>
            <div class="btn-box m-b-10">
             <div class="col-sm-6">
        <div class="dp-tables_btn">
                  <!--是否有新增的权限-->
                <#if security.hasEntityPermission("SEGMENT_NUMBER", "_CREATE", session)>
                <button id="btn_add" class="btn btn-primary" >
                    <i class="fa fa-plus"></i>${uiLabelMap.Add}
                </button>
                </#if>
                  <!--是否有删除的权限-->
                <#if security.hasEntityPermission("SEGMENT_NUMBER", "_DELETE", session)>
                <button class="btn btn-primary" id="btn_del">
                    <i class="fa fa-trash"></i> ${uiLabelMap.Delete}
                </button>
                </#if>
                </div>
            </div><!-- 操作按钮组end -->
            <#if segmentNoList?has_content>
                       
                <!-- 列表当前分页条数start -->
                <div class="col-sm-6">
                    <div class="dp-tables_length">
                        <label>
                        ${uiLabelMap.displayPage}
                            <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                    onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="5" <#if viewSize ==5>selected</#if>>5</option>
                                <option value="10" <#if viewSize==10>selected</#if>>10</option>
                                <option value="20" <#if viewSize==20>selected</#if>>20</option>
                                <option value="30" <#if viewSize==30>selected</#if>>30</option>
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
                          <#if security.hasEntityPermission("SEGMENT_NUMBER", "_DELETE", session)>
                            <th><input type="checkbox" class="js-allcheck"></th>
                          </#if>
                            <th>${uiLabelMap.NumberId}</th>
                            <th>${uiLabelMap.segmentNoFrom}</th>
                            <th>${uiLabelMap.segmentNoTo}</th>
                            <th>${uiLabelMap.isEnabled}</th>
                            <th>${uiLabelMap.isInside}</th>
                            <th>${uiLabelMap.currentNo}</th>
                           <!--是否有更新的权限-->
                          <#if security.hasEntityPermission("SEGMENT_NUMBER", "_UPDATE", session)>
                            <th>${uiLabelMap.systemMgrAction}</th>
                          </#if>
                        </tr>
                        </thead>
                        <tbody>
                         <#list segmentNoList as segmentlist>
                        <tr>
                          <!--是否有删除的权限-->
                          <#if security.hasEntityPermission("SEGMENT_NUMBER", "_DELETE", session)>
                            <td><input type="checkbox" class="js-checkchild" value="${segmentlist.segmentNumberId?if_exists}"></td>
                           </#if>
                            <td>${segmentlist.segmentNumberId?if_exists}</td>
                            <td>${segmentlist.segmentNoFrom?if_exists}</td>
                            <td>${segmentlist.segmentNoTo?if_exists}</td>
                            <td style="align:center">
                            <input name="isInside"  id="isEnabled" type="checkbox"  <#if segmentlist.isEnabled?has_content&&segmentlist.isEnabled=='Y'> checked="checked"</#if>value="Y">
                            </td>
                            <td align=center>
                            <input name="isInside"  id="isInside" type="checkbox"  <#if segmentlist.isInside?has_content&&segmentlist.isInside=='Y'> checked="checked"</#if>value="Y">
                            </td>
                            <td>${segmentlist.currentNo?if_exists}</td>
                             <!--是否有更新的权限-->
                          <#if security.hasEntityPermission("SEGMENT_NUMBER", "_UPDATE", session)>
                            <td>
                                <div class="btn-group">
                                    <button type="button" class=" btn-info btn btn-danger btn-sm">${uiLabelMap.Edit}</button>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                       <!--是否有删除的权限-->
                                  <#if security.hasEntityPermission("SEGMENT_NUMBER", "_DELETE", session)>
                                    <ul class="dropdown-menu" role="menu">
                                        <li><a href="javascript:del(${segmentlist.segmentNumberId?if_exists})">${uiLabelMap.Delete}</a></li>
                                    </ul>
                                  </#if>
                                </div>
                                 <input type="hidden" name="segmentNumberId" value="${segmentlist.segmentNumberId?if_exists}">
                            </td>
                         </#if>
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
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(segmentNoListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", segmentNoListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=segmentNoListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
       <!-- 分页条end -->
       <#else>
        <div id="noData" class="col-sm-12">
            <h3>${uiLabelMap.NoSegmentNumber}</h3>
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
        <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.OptionMsg}</h4>
      </div>
      <div class="modal-body">
        <h4 id="modal_msg_body"></h4>
      </div>
      <div class="modal-footer">
        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.Ok}</button>
      </div>
    </div>
  </div>
</div><!-- 提示弹出框end -->




<!-- 删除确认弹出框start -->
<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.OptionMsg}</h4>
      </div>
      <div class="modal-body">
        <h4 id="modal_confirm_body"></h4>
      </div>
      <div class="modal-footer">
      	<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.Delete}</button>
      </div>
    </div>
  </div>
</div><!-- 删除确认弹出框end -->

       

 <!--添加弹出框 start-->
<div class="modal fade" id="add_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.EditSegmentNumber}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="AddForm" action="<@ofbizUrl>createTag</@ofbizUrl>" method="post">
             <div class="row">
                <div class="form-group" data-type="required" data-mark="${uiLabelMap.NumberId}">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.NumberId}:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50"  id="segmentNumberId" name="segmentNumberId" />
                      <input type="hidden" class="form-control dp-vd w-p50"  id="currentNo" name="currentNo" />
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
          <!--号段自-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.segmentNoFrom}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.segmentNoFrom}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="segmentNoFrom"  name="segmentNoFrom">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div><!--号段自-->
           <!--号段至-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.segmentNoTo}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.segmentNoTo}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="segmentNoTo" name="segmentNoTo">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
         <!--号段至-->
          <div class="row p-l-10 p-r-10">
               <div class="form-group" data-type="minCheck" >
                   <div class="col-sm-11">
                       <div class="checkbox clearfix m-l-20" style="">
                       <label class="col-sm-3" title=""><input name="isInside"  id="isInside" type="checkbox" value="Y">${uiLabelMap.isInside}</label>
                       <label class="col-sm-3" title=""><input name="isEnabled"  id="isEnabled" type="checkbox" value="Y">${uiLabelMap.isEnabled}</label>
                   </div>
              <div class="dp-error-msg"></div>
              </div>
          </div>
          </div>
           
          <div class="modal-footer">
		    <button type="button"  id="btn_save" class="btn btn-primary">${uiLabelMap.Save}</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--添加弹出框 end-->

 <!--编辑弹出框 start-->
<div class="modal fade" id="edit_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel1">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.NumberId}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="<@ofbizUrl>updateTag</@ofbizUrl>" method="post">
         <div class="row">
                <div class="form-group" data-type="required" data-mark="${uiLabelMap.NumberId}">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.EditSegmentNumber}:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50"  id="segmentNumberId" name="segmentNumberId" readonly />
                      <input type="hidden" class="form-control dp-vd w-p50"  id="currentNo" name="currentNo" />
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
          <!--号段自-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.segmentNoFrom}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.segmentNoFrom}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="segmentNoFrom"  name="segmentNoFrom">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div><!--号段自-->
           <!--号段至-->
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.segmentNoTo}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.segmentNoTo}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="segmentNoTo" name="segmentNoTo">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
         <!--号段至-->
          <div class="row p-l-10 p-r-10">
               <div class="form-group" data-type="minCheck" >
                   <div class="col-sm-11">
                       <div class="checkbox clearfix m-l-20" style="">
                       <label class="col-sm-3" title=""><input name="isInside"  id="isInside" type="checkbox" value="Y">${uiLabelMap.isInside}</label>
                       <label class="col-sm-3" title=""><input name="isEnabled"  id="isEnabled" type="checkbox" value="Y">${uiLabelMap.isEnabled}</label>
                   </div>
              <div class="dp-error-msg"></div>
              </div>
          </div>
          </div>
          <div class="modal-footer">
		    <button type="button"  id="btn_edit" class="btn btn-primary">${uiLabelMap.Save}</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--编辑弹出框 end-->

<script>


$(function(){
       //添加按钮点击事件
        $('#btn_add').click(function(){
        $("#add_Modal #segmentNumberId").val('');
        $("#add_Modal #segmentNoFrom").val('');
        $("#add_Modal #segmentNoTo").attr("src", '');
        $("#add_Modal #isEnabled").attr("checked",false);
        $("#add_Modal #isInside").attr("checked",false);
	    $('#add_Modal').modal('show');
	    });
	    
	     //编辑按钮点击事件
	    $(".btn-info").click(function(){
	    var segmentNumberId=$(this).parent().parent().find('input[name=segmentNumberId]').val();
        doSearchNumber(segmentNumberId);
        $('#edit_Modal').modal('show');
        });
	    
	    function doSearchNumber(id) {
        $.ajax({
            type: 'post',
            url: '<@ofbizUrl>findSegmentNoById</@ofbizUrl>',
            data: {segmentNumberId: id},
            success: function (data) {
                $("#edit_Modal #segmentNumberId").val(data.segmentNumber.segmentNumberId);
                $("#edit_Modal #segmentNoFrom").val(data.segmentNumber.segmentNoFrom);
                $("#edit_Modal #segmentNoTo").val(data.segmentNumber.segmentNoTo);
                 console.log(data.segmentNumber.currentNo);
                 if (data.segmentNumber.currentNo!=null) {
                        $("#edit_Modal #segmentNoFrom").attr("readonly", 'true');
                        $("#edit_Modal #segmentNoTo").attr("readonly", 'true');
                    }
                 if (data.segmentNumber.isEnabled == 'Y') {
                        $("#edit_Modal #isEnabled").attr("checked", 'true');
                    }
                 if (data.segmentNumber.isInside == 'Y') {
                        $("#edit_Modal #isInside").attr("checked", 'true');
                    }
            }
        });
      }
	   
	    //添加关闭事件
		$('#add_Modal').on('hide.bs.modal', function () {
		  $('#AddForm').dpValidate({
		  	clear: true
		  });
		})
      //添加提交按钮点击事件
	    $('#btn_save').click(function(){
	        $('#AddForm').dpValidate({
			  clear: true
			});
			$('#AddForm').submit();
	    });
       //添加表单校验
       $('#AddForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "createSegmentNo",
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
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>SegmentNumber</@ofbizUrl>';
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
        
         //编辑弹框关闭事件
		$('#edit_Modal').on('hide.bs.modal', function () {
		  $('#EditForm').dpValidate({
		  	clear: true
		  });
		  $('#edit_Modal #segmentNoFrom').removeAttr('readonly');;
		  $('#edit_Modal #segmentNoTo').removeAttr('readonly');;
		  $("#edit_Modal #isEnabled").removeAttr('checked');
		  $("#edit_Modal #isInside").removeAttr('checked');
		})
      //编辑提交按钮点击事件
	    $('#btn_edit').click(function(){
	        $('#EditForm').dpValidate({
			  clear: true
			});
			$('#EditForm').submit();
	    });
       //编辑表单校验
       $('#EditForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "updateSegmentNo",
					type: "POST",
					data: $('#EditForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>SegmentNumber</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
})

 //删除按钮点击事件
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
				$('#modal_confirm #modal_confirm_body').html("确定要删除选中的号段?");
				$('#modal_confirm').modal('show');
	    	}else{
	    		//设置提示弹出框内容
	    		$('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
	    		$('#modal_msg').modal();
	    	}
	    });  
	    
	      //删除弹出框删除按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
			//异步调用删除方法
			$.ajax({
				url: "delsegmentNumber",
				type: "POST",
				data: {deleteId : del_ids},
				dataType : "json",
				success: function(data){
				//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
    				$('#modal_msg').modal();
    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
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
        //行删除按钮事件
	    function del(id){
		del_ids = id;
		//设置删除弹出框内容
				$('#modal_confirm #modal_confirm_body').html("确定要删除此号段?");
				$('#modal_confirm').modal('show');
		}
</script>