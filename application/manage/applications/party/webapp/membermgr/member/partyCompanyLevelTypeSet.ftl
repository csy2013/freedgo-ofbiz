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
.s-error,.s-message{margin-left: 5px;}
.s-img{
	max-height: 230px;
	display: block;
}
.s-img-div{
	margin:0 10px 10px 10px; 
}		
.s-input-w{
	width: 80px;
	display: none;
}
.s-input-w_l{
    width: 80px;
    display:inline-block;
}
.s-span-c {
    height: 34px;
    padding: 6px 12px;
    display: inline-block;
    line-height: 22px;
}
.s-qx{
    display: none;
}
</style>
<!-- Main content -->
<div class="box box-info">
    <div class="box-header with-border">
        <ul class="nav nav-tabs">
                <li role="presentation" ><a href="<@ofbizUrl>partyLevelTypeSet</@ofbizUrl>">个人会员</a></li>
                <li role="presentation"  class="active"><a href="<@ofbizUrl>partyCompanyLevelTypeSet</@ofbizUrl>" >企业会员</a></li>
       </ul>
    </div>
    <div class="box-body">
    	<!-- 错误提示框 -->
    	<div id="error_alert" class="alert alert-danger alert-dismissable" style="display:none;">
		    <button id="btn_close" type="button" class="close" aria-hidden="true">×</button>
		    <h4><i class="icon fa fa-ban"></i> 错误提示:</h4>
		    <p id="error_content">
		    </p>
		</div>
    			
        
		
        <form class="form-horizontal" role="form" action="" name="" class="">
	        <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label"></label>
	                <div class="col-sm-3">
	                    <!--是否有新增的权限-->
	                    <#if security.hasEntityPermission("PARTY_LEVEL", "_CREATE", session)>
		                    <button type="button" class="btn btn-primary js-addpara" id="btnAddProductParameter">
		                    	<i class="fa fa-plus"></i>${uiLabelMap.Add}
		                    </button>
	                    </#if>
	                    
	                </div>
	            </div>
	        </div>
	        
	        <div class="row">
	            <div class="form-group">
	               	<label  class="col-sm-2 control-label"></label>
	               	<div class="col-sm-8">
	                  	<table class="table table-bordered table_b_c js-table_1">
					      	<thead>
					           	<tr>
					               	<th>会员等级</th>
					               	<th>会员等级描述</th>
					              	<th>会员成长值</th>
					               	<th>会员折扣</th>
	                               	<#if security.hasEntityPermission("PARTY_LEVEL", "_CREATE", session)>
					               		<th>${uiLabelMap.BrandOption}</th>
					               	</#if> 
					           	</tr>
					      	</thead>
					      	<tbody>
					      	<#if companylevelList?has_content>
					      		<#list companylevelList as levList>
					      			<tr>
	    	      						<td>
							    	      	<input type="hidden" name="levelId" id="" value="${levList.levelId?if_exists}">
							    	      	<input type="text" name="levelName" id="" value="${levList.levelName?if_exists}" readOnly />
	    	      						</td>
	    	      						<td>
	    	      							<input type="text"  name="levelDescrption" id="" value="${levList.levelDescrption?if_exists} " readOnly />
	    	      						</td>
	    	      						<td>
	    	      							<input type="text" name="levelExperience" id="" value="${levList.levelExperience?if_exists}" placeholder="请输入整数">
	    	      						</td>
	    	      						<td>
	    	      							<input type="text" name="levelDiscount" id="" value="${levList.levelDiscount?if_exists}" placeholder="请输入0到1的正数">
	    	      							<input type="hidden" name="operation" id="" value="update"/>
	    	      						</td>
	              						<#if security.hasEntityPermission("PARTY_LEVEL", "_CREATE", session)>
	    	      							<td class="fc_td">
	    	      								<button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button>
	    	      							</td>
	    	      						</#if>
	    	             			</tr>
	    	      				</#list>
	    	       			</#if>
				      		</tbody>
	    		  		</table>
	           		</div>
	        	</div>
	    	</div>
	    
	    	<div class="form-group" style="TEXT-ALIGN: center;">
		  		<!--是否有新增的权限-->
			    <#if security.hasEntityPermission("PARTY_LEVEL", "_CREATE", session)>
			    	<button id="save" type="button" class="btn btn-primary m-l-20">保存</button>
			    </#if>
	    		<button id="" type="button" class="btn btn-primary m-l-20" data-dismiss="modal" onclick="history.back();">返回</button>
			</div>
			
    	</form>
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
		  	<input type="hidden" value="" id="productAttrInfos"/>
		  	<div class="modal-footer">
		    	<button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.FacilityOk}</button>
		  	</div>
    	</div>
  	</div>
</div><!-- 提示弹出框end -->

<script>
//添加一般积分
$(document).on('click','.js-addpara',function(){
	var tr='<tr>'
	      +'<td><input type="text" name="levelName" id="" value=""><input type="hidden" name="levelId" id="" value=""></td>'//等级
	      +'<td><input type="text" name="levelDescrption" id="" value="">'//
	      +'<td><input type="text" name="levelExperience" id="" value="" placeholder="请输入整数"></td>'//等级
	      +'<td><input type="text" name="levelDiscount" id="" value="" placeholder="请输入0到1的正数"><input type="hidden" name="operation" id="" value="create"> <input type="hidden" name="partyType" id="" value="COMPANY"></td>'//等级
	      +'<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>'
	      +'</tr>';
	$('.js-table_1>tbody').append(tr);
})
	
//删除按钮事件
$(document).on('click','.js-button',function(){
	if($(this).closest('tr').find('td:eq(0)').find('input[name=levelId]').val()){
		$(this).parent().parent().hide();
		$(this).closest('tr').find('td:eq(3)').find('input[name=operation]').val('delete');
	}else{
		$(this).parent().parent().remove();
	}
})

 //列表长度
var length;

 //每行获取等级类型数据
function  getPartyLevelData(i){
   	var tdArr =$('.js-table_1>tbody').find("tr")
   	var levelId= tdArr.eq(i).find('td').eq(0).find("input[name=levelId]").val();//等级Id
   	var levelName= tdArr.eq(i).find('td').eq(0).find("input[name=levelName]").val();//等级名称
   	var levelDescrption= tdArr.eq(i).find('td').eq(1).find("input[name=levelDescrption]").val();//等级名称
   	var levelExperience= tdArr.eq(i).find('td').eq(2).find("input[name=levelExperience]").val();//成长值
   	var levelDiscount= tdArr.eq(i).find('td').eq(3).find("input[name=levelDiscount]").val();//折扣
   	var operation= tdArr.eq(i).find('td').eq(3).find("input[name=operation]").val();//折扣
   	var partyType= tdArr.eq(i).find('td').eq(3).find("input[name=partyType]").val();//
   	var obj = {};
   	obj.levelId=levelId;
   	obj.levelName=levelName;
   	obj.levelDescrption=levelDescrption;
   	obj.levelExperience=levelExperience;
   	obj.levelDiscount=levelDiscount;
   	obj.operation=operation;
   	obj.partyType=partyType;
   	return obj;
}

function updatePartyLevel(i){
 	var obj=getPartyLevelData(i);
 	var operation=obj.operation;
 	var url='';
 	if(operation=='delete'){
  		url='/membermgr/control/deletePartyLevelType';
 	}else if(operation=='create'){
  		url='/membermgr/control/createPartyLevelType';
 	}else if(operation=='update'){
  		url='/membermgr/control/updatePartyLevelType';
 	}
 	$.ajax({
		url: url,
		type: "POST",
		data: obj,
		dataType : "json",
		success: function(data){
		    //设置提示弹出框内容
			$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
			$('#modal_msg').modal();
			//提示弹出框隐藏事件，隐藏后重新加载当前页面
			$('#modal_msg').off('hide.bs.modal');
			$('#modal_msg').on('hide.bs.modal', function () {
		  		window.location.href='<@ofbizUrl>partyCompanyLevelTypeSet</@ofbizUrl>';
			})
			if (++i <= length)
				updatePartyLevel(i);
		},
		error: function(data){
			flag=false;
		}
	});
}

//错误提示框的关闭按钮事件
$("#error_alert #btn_close").click(function(){
	$("#error_alert").hide();
});

//保存按钮点击事件
$('#save').click(function(e){
	var error_msg = validate();
	//错误信息
	if(error_msg){
		//弹出错误信息提示框
		$("#error_alert #error_content").html(error_msg);
		$("#error_alert").show();
		return false;
	}else{
		//关闭错误信息提示框
		$("#error_alert").hide();
	}
	length = $('.js-table_1>tbody').find("tr").length;
   if(length>0){
	updatePartyLevel(0);
   }
}); 

//表单校验规则
function validate(){
	var error_msg="";
	$.each($('.js-table_1>tbody').find("tr:visible"),function(i,tr){
		var levelName = $(tr).find("input[name='levelName']").val();
		var levelExperience = $(tr).find("input[name='levelExperience']").val();
		var levelDiscount = $(tr).find("input[name='levelDiscount']").val();
		//判断会员等级是否为空
		if(!levelName){
			error_msg = "会员等级不能为空！";
			return error_msg;
		}
		//判断会员成长值是否为空
		if(!levelExperience){
			error_msg = "会员成长值不能为空！";
			return error_msg;
		}else{
			var int_reg = /^[0-9]\d*$/;
			//判断会员成长值是否为整数
			if(!int_reg.test(levelExperience)){
				error_msg = "会员成长值必须为正整数！";
				return error_msg;
			}
		}
		//判断会员折扣是否为两位小数
		var float_reg = /^[01](\.\d{1,2})?$/;
		if(levelDiscount && !float_reg.test(levelDiscount)){
			error_msg = "会员折扣必须为 >0 且 <=1 之间的数字，最多2位小数！";
			return error_msg;
		}
		
		//获取同级的元素进行对比
		$.each($(tr).siblings('tr:visible'),function(s_i,s_tr){
			var s_levelName = $(s_tr).find("input[name='levelName']").val();
			var s_levelExperience = $(s_tr).find("input[name='levelExperience']").val();
			//校验会员等级名称不能重复
			if(levelName == s_levelName){
				error_msg = "会员等级：【"+s_levelName+"】已存在！";
				return error_msg;
			}
			
			//校验等级成长值不能小于等于之前的成长值
			if($(s_tr).index() < $(tr).index() && parseInt(s_levelExperience) >= parseInt(levelExperience)){
				error_msg = "会员成长值：不能小于等于之前等级的成长值！";
				return error_msg;
			}
		});
	});
	return error_msg;
}
</script>