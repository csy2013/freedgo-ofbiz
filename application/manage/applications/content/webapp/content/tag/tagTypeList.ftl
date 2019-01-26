<style>
 #example2>div{
    border-top: 1px solid #f4f4f4;
}
#example2>div.xl_head{
    border-top: none;
}
.xl_head span{
    padding: 8px;
}
#example2 span{
    display: inline-block;
    width: 45%;
}
.xl_head .xl_mod{
    padding-left: 33px;
}
.xl_write{
    padding: 8px;
}

.classfy_name{
    padding-left: 8px;
}
.first_tr .second_tr,.third_tr{
    padding:8px 0px 8px 8px ;
    border-top: 1px solid #f4f4f4;
}
.second_td{
    padding-left: 10px;
    padding-bottom: 8px;
}
.third_td{
    padding-left: 50px;

}
.form-group .col-sm-2{
    width: 18.666%;
}
.form-group .col-sm-10{
    width: 80.666%;
}
.p_name,.p_name1{
    padding-left: 20px;
}
.xl_icon{
    display: inline-block;
    color: black;
    margin-left: 10px;
    cursor: pointer;
}
.xl_icon,.name1{
    vertical-align: middle;
}
.second_tr .xl_icon{
    padding-left: 10px;
}
.second_tr,.third_tr{
    display: none;
}
.second_btn_group{
    margin-left: -16px;
}
.third_btn_group{
    margin-left: -26px;
}
</style>
 <!-- Main content -->
      
            <div class="box box-info">
                <div class="box-header with-border">
                    <h3 class="box-title m-t-10">标签分类</h3>
                </div>
                <div class="box-body">
                    <div class="row">
                        <div class="col-sm-12">
                            <input type="hidden" class="add_name">
                            <div id="example2" class="table table-bordered table-hover w-p50">
                                <div class="xl_head">
                                    <span>分类名称</span>
                                    <span class="xl_mod">操作</span>
                                </div>
								<!--循环所有一级分类 start-->
                                <#if tagTypeList?has_content>
                                <#list tagTypeList as typelist>
                               <!--一级分类  start-->
                                        <div class="first_tr" data-first="1">
                                            <i class="glyphicon glyphicon-plus-sign xl_icon first_icon"></i><span class="classfy_name name1">${typelist.tagTypeName?if_exists}</span>
                                            
                                            <span class="xl_write">
                                                <div class="btn-group">
                                                <!--是否有编辑的权限-->
                                                <#if security.hasEntityPermission("CONTENT_TAGTYPE", "_UPDATE", session)>
                                                    <button type="button" class=" js-button btn btn-danger btn-sm xl_bj" data-toggle="modal">编辑</button>
                                                </#if>
                                                </div>
                                                <input type="hidden" name="tagTypeId" value="${typelist.tagTypeId?if_exists}">
                                            </span>
                                            <#assign tagTypelevel2 = delegator.findByAnd("TagType", {"parentTagTypeId" : typelist.tagTypeId?if_exists})>
                                            <#if tagTypelevel2?has_content>
                                            <#list tagTypelevel2 as tagType2>
                                            <!--start子分类-->
                                            <div class="second_tr" data-second="2">
                                                <i class="glyphicon glyphicon-plus-sign xl_icon second_icon"></i><span class="second_td name1">${tagType2.tagTypeName}</span>
                                            <span class="second_td">
                                            <div class="btn-group second_btn_group">
                                             <!--是否有编辑的权限-->
                                            <#if security.hasEntityPermission("CONTENT_TAGTYPE", "_UPDATE", session)>
                                            <button type="button" class=" js-button btn btn-danger btn-sm xl_bj" data-toggle="modal" >编辑</button>
                                            </#if>
                                            <input type="hidden" name="tagTypeId" value="${tagType2.tagTypeId?if_exists}">
                                            </div>
                                            </span>
                                             <#assign tagTypelevel3 = delegator.findByAnd("TagType", {"parentTagTypeId" : tagType2.tagTypeId?if_exists})>
                                             <#if tagTypelevel3?has_content>
                                            <#list tagTypelevel3 as tagType3>
                                               <!--start三级分类-->
                                           <div class="third_tr"  data-third="3">
                                           <span class="third_td name1">${tagType3.tagTypeName}</span>
                                            <span class="third_td">
                                            <div class="btn-group third_btn_group">
                                            <!--是否有编辑的权限-->
                                            <#if security.hasEntityPermission("CONTENT_TAGTYPE", "_UPDATE", session)>
                                            <button type="button" class=" js-button btn btn-danger btn-sm xl_bj"  >编辑</button>
                                            </#if>
                                            <input type="hidden" name="tagTypeId" value="${tagType3.tagTypeId?if_exists}">
                                           </div>
                                          </span>
                                                </div><!-- end三级分类-->
                                            </#list>
                                            </#if>
                                            </div> <!--子分类-->
                                            </#list>
                                            </#if>
                                            </div><!--一级分类  end-->
                                         </#list>
                                </#if><!--循环所有一级分类 end-->
                            </div>
                        </div>
                    </div>
                </div>
            </div>
    <!--content -->
        
        
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
        
        
        <!--编辑弹出框 start-->
        <div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">分类编辑</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="<@ofbizUrl>updateTagtypeName</@ofbizUrl>" method="post">
              
              <div class="row">
                     <div class="form-group" data-type="required" data-mark="分类名称">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>分类名称:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="tagTypeName" name="tagTypeName">
                               <input type="hidden" class="form-control dp-vd w-p50" id="tagTypeId" name="tagTypeId">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
          </div>
          <div class="modal-footer">
		    <button type="button" class="btn btn-primary edit_btn_save">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
   <!--编辑弹出框 end-->

<script>
    $(function(){
    
       //编辑弹出框
        $("#example2").on("click",".xl_bj",function(){
           var a=$(this).parent().parent().prev().html();
           var tagTypeId=$(this).parent().parent().find('input[name=tagTypeId]').val();
            $(this).parent().parent().prev().addClass("new_name");
            $('#exampleModal').modal('show');
            $("#exampleModal").find('input[name=tagTypeName]').val(a);
            $("#exampleModal").find('input[name=tagTypeId]').val(tagTypeId);
        });
      
       //编辑关闭事件
		$('#exampleModal').on('hide.bs.modal', function () {
		  $('#EditForm').dpValidate({
		  	clear: true
		  });
		})
		
      //编辑提交按钮点击事件
	    $('.edit_btn_save').click(function(){
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
					url: "updateTagtypeName",
					type: "POST",
					data: $('#EditForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#exampleModal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal();
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>TagTypeList</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#exampleModal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });

       // 点击小图标展开下一级菜单
        $("#example2").on("click",".first_icon",function(){
            if($(this).hasClass("glyphicon-plus-sign")){
                $(this).removeClass("glyphicon-plus-sign").addClass("glyphicon-minus-sign");
                $(this).parent().find(".second_tr").show();
            }else{
                $(this).removeClass("glyphicon-minus-sign").addClass("glyphicon-plus-sign");
                $(this).parent().find(".second_tr").hide();
            }

        });
       //二级展开三级的图标
        $("#example2").on("click",".second_icon",function(){
            if($(this).hasClass("glyphicon-plus-sign")){
                $(this).removeClass("glyphicon-plus-sign").addClass("glyphicon-minus-sign");
                $(this).parent().find(".third_tr").show();
            }else{
                $(this).removeClass("glyphicon-minus-sign").addClass("glyphicon-plus-sign");
                $(this).parent().find(".third_tr").hide();
            }
        });
    })
</script>


