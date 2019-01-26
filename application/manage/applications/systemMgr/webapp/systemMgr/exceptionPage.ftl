<style>
	.s-zdy{
		border: 1px solid #ccc;
		margin-top: 15px;
	}
	.s-bj-p{
		text-align: right;
	}
	.s-bj{
	
		margin-right: 15px;
	}
	.s-zdy p{ line-height: 24px; }
	.s-zdy img{
		width:100% !important;
		height:100% !important;
	}
	.s-error,.s-message{margin-left: 5px;}
	.s-img{
		max-height: 230px;
		display: block;
	}
	.s-img-div{
		margin:0 10px 10px 10px; 
	}
	</style>

<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<!-- Main content -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">${uiLabelMap.ExceptionPage}<#if exceptionPageList?has_content>(共${exceptionPageList.size()?default('0')}个)</#if></h3>
        </div>
        <div class="box-body">			
			<div class="form-inline clearfix">
            <!-- 添加按钮组start-->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                <#if security.hasEntityPermission("EXCEPTIONPAGE", "_CREATE", session)>
                <button id="btn_add" class="btn btn-primary" >
                    <i class="fa fa-plus"></i>${uiLabelMap.Add}
                </button>
                </#if>
                </div>
            </div><!-- 操作按钮组end --> 
            </div><!-- 操作按钮组end --> 
            <!-- 分割线 start-->
            <div class="cut-off-rule bg-gray"></div>
			<!-- 分割线  start-->
			
            <#if exceptionPageList?has_content> 
            <div class="row">
            <#list exceptionPageList as pageList>
            <!-- 循环体 start-->
            <div class="col-md-4 col-lg-3">
            <div class="s-zdy">
            <p class="s-bj-p">
            <#if security.hasEntityPermission("EXCEPTIONPAGE", "_UPDATE", session)>
            <a href="javascript:;" class="s-bj edit-info">${uiLabelMap.Edit}</a>
            </#if>
            </p>
           <input type="hidden"  name="exceptionPageId" value="${pageList.exceptionPageId?if_exists}"/>
            <p><span class="s-error">${pageList.exceptionPageId}(${pageList.pageTitle})</span></p>
            <div class="s-img-div" style="word-wrap:break-word;word-break:break-all;">
             <#if pageList.contentId?has_content&&pageList.contentId!="">
             <#assign content = delegator.findByPrimaryKey("Content",Static["org.ofbiz.base.util.UtilMisc"].toMap("contentId",pageList.contentId))>
             <#if content?has_content>
             <#assign dataResource = delegator.findByPrimaryKey("DataResource",Static["org.ofbiz.base.util.UtilMisc"].toMap("dataResourceId",content.dataResourceId))>
             </#if>
             <#if dataResource?has_content>
             <#assign electronicText = delegator.findByPrimaryKey("ElectronicText",Static["org.ofbiz.base.util.UtilMisc"].toMap("dataResourceId",dataResource.dataResourceId))>
             </#if>
             ${StringUtil.wrapString(electronicText.textData)?if_exists}
             </#if>
            </div>
            <p><span class="s-message">${uiLabelMap.LastUpdateTime}:${pageList.lastUpdatedStamp?string("yyyy-MM-dd")}</span></p>
            </div>
            </div><!-- 循环体 end-->
            </#list>
            </div><!-- 表格区域end -->
       <#else>
        <div id="noData" class="col-sm-12">
            <h3>${uiLabelMap.NoException}</h3>
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


 <!--添加弹出框 start-->
<div class="modal fade" id="add_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.Editexception}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="AddForm" action="<@ofbizUrl>createExceptionPage</@ofbizUrl>" method="post">
             <div class="row">
                <div class="form-group" data-type="required" data-mark="${uiLabelMap.exceptionId}">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.exceptionId}:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="exceptionPageId" name="exceptionPageId"  />
                      
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
          <div class="row">
                     <div class="form-group" >
                           <label for="title" class="col-sm-2 control-label">${uiLabelMap.pageTitle}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="pageTitle" name="pageTitle">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
             <div class="row">
            <div class="form-group">
                    <label for="message-text" class="control-label col-sm-2">${uiLabelMap.isEnabled}:</label>
                      <div class="radio">
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
             <!-- 异常页面描述start-->
              <div class="row">
			  <div class="form-group">
                <label for="recipient-name" class="control-label col-sm-2">${uiLabelMap.pageDescrption}:</label>
                <div class="col-sm-10">
                 <div class="box-body pad " >
                    <textarea id="content" name="content"  value="" ></textarea>
                    <p class="dp-error-msg"></p>
                </div>
                </div>     
              </div>   
			  </div> <!-- 异常页面描述end-->  
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
   
 <!--编辑弹出框 end-->
<div class="modal fade" id="edit_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.Editexception}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="" method="post">
             <div class="row">
                <div class="form-group" data-type="required" data-mark="${uiLabelMap.exceptionId}">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.exceptionId}:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="exceptionPageId" name="exceptionPageId"  readonly/>
                      
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
          <div class="row">
                     <div class="form-group" >
                           <label for="title" class="col-sm-2 control-label">${uiLabelMap.pageTitle}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="pageTitle" name="pageTitle">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
             <div class="row">
            <div class="form-group">
                    <label for="message-text" class="control-label col-sm-2">${uiLabelMap.isEnabled}:</label>
                      <div class="radio">
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
             <!-- 异常页面描述start-->
              <div class="row">
			  <div class="form-group">
                <label for="recipient-name" class="control-label col-sm-2">${uiLabelMap.pageDescrption}:</label>
                <div class="col-sm-10">
                 <div class="box-body pad " >
                    <textarea id="editcontent" name="editcontent" class="form-control w-p70" value="2" >
                    </textarea>
                    <p class="dp-error-msg"></p>
                </div>
                </div>     
              </div>   
			  </div> <!-- 异常页面描述end-->  
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
       CKEDITOR.replace("content");
       CKEDITOR.replace("editcontent");
       //添加按钮点击事件
        $('#btn_add').click(function(){
        $("#add_Modal #exceptionPageId").val('');
        $("#add_Modal #pageTitle").val('');
        //CKEDITOR.instances.content.setData('');
        $("#add_Modal input[name=isEnabled]:eq(0)").attr("checked", 'checked');
	    $('#add_Modal').modal('show');
	    });
	    
	     //编辑按钮点击事件
	    $(".edit-info").click(function(){
	    var exceptionPageId=$(this).parent().parent().find('input[name=exceptionPageId]').val();
        doSearchPage(exceptionPageId);
        $('#edit_Modal').modal('show');
        });
	    
	    function doSearchPage(id) {
        $.ajax({
            type: 'post',
            url: '<@ofbizUrl>findExceptionPage</@ofbizUrl>',
            data: {exceptionPageId: id},
            success: function (data) {
                $("#edit_Modal #exceptionPageId").val(data.exceptionPage.exceptionPageId);
                $("#edit_Modal #pageTitle").val(data.exceptionPage.pageTitle);
                CKEDITOR.instances.editcontent.setData(data.textData);
                $('.js-'+data.exceptionPage.isEnabled).prop('checked',true);//启用状态
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
	        $("#add_Modal #content").val(CKEDITOR.instances.content.getData());
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
					url: "createExceptionPage",
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
						  window.location.href='<@ofbizUrl>exceptionPage</@ofbizUrl>';
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
		})
      //编辑提交按钮点击事件
	    $('#btn_edit').click(function(){
	       $("#edit_Modal #editcontent").val(CKEDITOR.instances.editcontent.getData());
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
					url: "updateExceptionPage",
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
						  window.location.href='<@ofbizUrl>exceptionPage</@ofbizUrl>';
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
</script>