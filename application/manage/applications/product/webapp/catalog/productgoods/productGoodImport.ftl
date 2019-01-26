<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<script type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/upload/ajaxupload.js</@ofbizContentUrl>"></script>
<#-- 商品导入  -->
<div class="box box-info">
	<div class="box-body">
		<#-- 按钮组 -->
		<div class="row m-b-10">
			<div class="col-sm-6 dp-tables_btn">
				<button id="downTemplate" type="button" class="btn btn-primary">下载模板</button>
	  			<button id="goodsImport" type="button" class="btn btn-primary">商品导入</button>
			</div>
		</div>
		<#-- 分割线-->
		<div class="cut-off-rule bg-gray"></div>
		
	    <#-- 注意事项start -->
		<div class="row" id="hint">
			<div class="col-xs-12">
				<div class="box" style="border-top:0px">
		            <div class="box-header">
		              <h3 class="box-title">导入提示：</h3>
		            </div>
		            <div class="box-body no-padding" align="left">
		              <table class="table table-condensed" >
		                <tbody>
		                <tr>
		                  <td style="text-align:left">1.请先下载指定的模板进行导入</td>
		                </tr>
		                <tr>
		                  <td style="text-align:left">2.时间填写格式为：2016-03-11 23:59</td>
		                </tr>
		                <tr>
		                  <td style="text-align:left">3.所属商家、商品标题、商品分类、是否申请上架、服务支持、销售开始时间、主营分类、销售价格（元）、积分抵扣、是否使用规格为必填字段</td>
		                </tr>
		               
		              </tbody></table>
		            </div>
		        </div>
			</div>
		</div>
       <#-- 注意事项end -->
	</div> 
</div><!-- 内容end --> 


<#-- 导入弹出框start -->
<div id="modal_import"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_import_title">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="modal_import_title">商品导入</h4>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
					<div class="form-group">
						<label class="control-label col-sm-2">${uiLabelMap.SelectFile}:</label>
						<div class="col-sm-10 uploadFile">
							<input type="text" class="form-control w-p80" style="float: left" disabled id="doc">
							<input type="hidden" id="hidFileName" />
							<input type="button" class="btn btn-default m-l-5" id="btnUpload" value="${uiLabelMap.Upload}" />
						</div>                
					</div>
		        </form>
			</div>
			<div class="modal-footer">
				<button id="upload" type="button" class="btn btn-primary">${uiLabelMap.Import}</button>
				<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
			</div>
		</div>
	</div>
</div><!-- 导入弹出框end -->

<#-- 导入错误提示框start -->
<div id="modal_error"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_error_title">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="modal_error_title">${uiLabelMap.Error}</h4>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
					<!-- 错误提示start -->
					<div class="row" id="error_list">
						<div class="col-xs-12">
							<div class="box" style="border-top:0px">
					            <div class="box-header">
					              <h3 class="box-title" style="color: red">
					              	<icon class="fa fa-times-circle"></icon>${uiLabelMap.ImportFail}
					              </h3>
					            </div><!-- /.box-header -->
					            <div class="box-body no-padding" style="border: 1px solid #ddd;overflow-y: auto;height: 350px;" align="left">
					              <table class="table table-condensed" >
					                <tbody>
					              	</tbody>
					              </table>
					            </div><!-- /.box-body -->
					        </div>
						</div>
					</div><!-- 错误提示end -->
		        </form>
			</div>
			<div class="modal-footer">
				<button id="importAgain" type="button" class="btn btn-primary">${uiLabelMap.ImportAgain}</button>
				<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
			</div>
		</div>
	</div>
</div>
<#-- 导入错误提示框end -->

<#-- 提示弹出框start -->
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
</div>
<#-- 提示弹出框end -->



<script>

    // 创建一个上传参数
	var uploadOption =
	{
	    action: "<@ofbizUrl>goodsImport</@ofbizUrl>",    // 提交目标
	    name: "file",             // 服务端接收的名称
	    autoSubmit: false,        // 是否自动提交
	    // 选择文件之后…
	    onChange: function (file, extension) 
	    {
	        $('#doc').val(file);
	    },
	    // 开始上传文件
	    onSubmit: function (file, extension)
	    {
	        if (!(extension && /^(xls|XLS|xlsx|XLSX)$/.test(extension))) {  
                alert("只支持xls或xlsx格式文件！");  
                return false;  
            }
	    },
	    // 上传完成之后
	    onComplete: function (file, response) 
	    {
	    	var data = eval('(' + response + ')')
	    	if(data.success){
	    		$('#modal_import').modal('hide');
	    		//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html(data.successMsg);
				$('#modal_msg').modal();
				window.location.href="<@ofbizUrl>findProductGoods</@ofbizUrl>";
	    	}else{
	    		$('#error_list table tbody').empty();
	    		$('#error_list .box-header .box-title small').empty();
				$.each(data.errorMsg, function(){     
					$('#error_list table tbody').append("<tr><td style='text-align:left;color:red'>"+this.msg+"</td></tr>");    
				});
				$('#error_list .box-header .box-title').append("<small style='color:red'>（共"+data.errorMsg.length+"条错误）</small>");
				$('#modal_import').modal('hide');
				$('#modal_error').modal();  
	    	}
	    }
	}
    

   $(function(){
		
        //下载模板按钮点击事件
		$('#downTemplate').click(function(){
			window.location="<@ofbizContentUrl>/images/importTemplate/ProductGood/productGoodTemplate.xlsx</@ofbizContentUrl>";
		});
		
		//品牌导入按钮点击事件
		$('#goodsImport').click(function(){
			$('#doc').val("");
			$('#modal_import').modal();
		});

		// 初始化图片上传框
		var au = new AjaxUpload($('#btnUpload'), uploadOption);

		//导入按钮点击事件
		$('#upload').click(function(){
		   au.submit();
		});
	});



</script>
