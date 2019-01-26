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

<script language="JavaScript" type="text/javascript">
   var curproductFeatureValue="";
   var isCommitted = false;//表单是否已经提交标识，默认为false
   $(function(){
      var operateType ='${parameters.operateType?if_exists}';
      var productFeatureTypeId='${parameters.productFeatureTypeId?if_exists}';
      if(operateType=='update'){
        getProductFeatureByTypeId(productFeatureTypeId);
      }
      
      
      $("#btnCancel").click(function(){
      	  document.location.href="<@ofbizUrl>ProductFeature?lookupFlag=Y</@ofbizUrl>";
      })
      
      
      $("#btnFeatureSave").click(function(){
           $('#updateProductFeature').dpValidate({
			  clear: true
		   });
		   $('#updateProductFeature').submit();
      });
      $('#updateProductFeature').dpValidate({
      		validate: true,
      		callback: function(){
      			if(dosubmit()){
      			    var tFeatureInfos = "";
			        var curFeatureInfo="";
			        var viewSize='${parameters.VIEW_SIZE?if_exists}';
                    var viewIndex='${parameters.VIEW_INDEX?if_exists}';
                   
			        var sortFieldCur="${requestParameters.sortField?if_exists}";
			        var sortTypeCur="${requestParameters.sortType?if_exists}";
                    
                    var productFeatureTypeNameForFind="${requestParameters.productFeatureTypeNameForFind?if_exists}";
                    var descriptionForFind="${requestParameters.descriptionForFind?if_exists}";
                    
			        $('#tabFreatureValue>tbody').find("tr").each(function(){
			           // 读取tbody中tr的内容
		               curFeatureInfo="";
				       var tdArr = $(this).children();
				       var curproductFeatureId=tdArr.eq(0).find("#curproductFeatureId").val();//规格值ID
				       var curOptionType=tdArr.eq(0).find("#curOptionType").val();//操作类型
				       var productFeatureName = tdArr.eq(1).find("input").val();//规格值名称
				       var sequenceNum= tdArr.eq(2).find("input").val();//排序号
				        
				       curFeatureInfo=productFeatureName+"|"+sequenceNum+"|"+curproductFeatureId+"|"+curOptionType;
		               tFeatureInfos = tFeatureInfos + "," + curFeatureInfo;
				    });
				    $("#tFeatureInfos").val(tFeatureInfos.substr(1,tFeatureInfos.length));
				    //document.updateProductFeature.action="<@ofbizUrl>updateProductFeature</@ofbizUrl>";
      				//document.updateProductFeature.submit();
      				// 操作类型	
					var operateType='${parameters.operateType?if_exists}';
					// 规格类型ID                 
					var productFeatureTypeId="";
					if(operateType=="update"){
					    productFeatureTypeId='${parameters.productFeatureTypeId?if_exists}';
					}
					// 规格类型名	                      
					var productFeatureTypeName=$("#productFeatureTypeName").val();
					// 规格信息	              
					var tFeatureInfos=$("#tFeatureInfos").val();
					// 规格备注
					var description=$("#description").val();
					<#--if(operateType=="create"){-->
					     <#--document.updateProductFeature.action="<@ofbizUrl>createProductFeature</@ofbizUrl>";-->
      				     <#--document.updateProductFeature.submit();-->
					<#--}else{-->
						<#--jQuery.ajax({-->
							<#--url: '<@ofbizUrl>updateProductFeature</@ofbizUrl>',-->
							<#--type: 'POST',-->
							<#--data: {-->
								 <#--'operateType':operateType,-->
								 <#--'productFeatureTypeId':productFeatureTypeId,		-->
								 <#--'productFeatureTypeName':productFeatureTypeName,		-->
								 <#--'tFeatureInfos':tFeatureInfos,		-->
								 <#--'description':description,-->
								 <#---->
								 <#--'VIEW_SIZE' : viewSize,-->
					             <#--'VIEW_INDEX' : viewIndex,-->
					             <#--'productFeatureTypeNameForFind':productFeatureTypeNameForFind,		-->
								 <#--'descriptionForFind':descriptionForFind,-->
					             <#---->
					             <#--'sortField' : sortFieldCur,-->
					             <#--'sortType' : sortTypeCur-->
							<#--},-->
							<#--success: function(data) {-->
							    <#--var curViewIndex=data.curViewIndex;-->
								<#--document.location.href="<@ofbizUrl>ProductFeature?lookupFlag=Y&VIEW_SIZE="+viewSize+"&VIEW_INDEX="+curViewIndex+"&sortField="+sortFieldCur+"&sortType="+sortTypeCur+"&productFeatureTypeName="+productFeatureTypeNameForFind+"&description"+descriptionForFind+"</@ofbizUrl>";-->
							<#--}-->
						<#--});-->



					jQuery.ajax({
						url: '<@ofbizUrl>updateProductFeature</@ofbizUrl>',
						type: 'POST',
						data: {
							'operateType':operateType,
							'productFeatureTypeId':productFeatureTypeId,
							'productFeatureTypeName':productFeatureTypeName,
							'tFeatureInfos':tFeatureInfos,
							'description':description,

							'VIEW_SIZE' : viewSize,
							'VIEW_INDEX' : viewIndex,
							'productFeatureTypeNameForFind':productFeatureTypeNameForFind,
							'descriptionForFind':descriptionForFind,

							'sortField' : sortFieldCur,
							'sortType' : sortTypeCur
						},
						success: function(data) {
							document.location.href="<@ofbizUrl>ProductFeature?lookupFlag=Y</@ofbizUrl>";
						}
					});

      			}
      		}
      });
      
      $("#addFeatureValue").click(function(){
         var trHtml="<tr><td><input  type='checkbox' class='js-checkchild' name='ckb' value=''/><input type='hidden' id='curOptionType' value='create'></td><td><input type='text'/></td><td><input type='text'/></td><td><button type='button' class='js-button btn btn-danger btn-sm'>删除</button></td></tr>";
         $('#tabFreatureValue>tbody').append(trHtml);
      })
      
      //$("#btnFeatureValueDel").click(function(){
      //    delTr('ckb');
      //})
      
      
       $("#btnFeatureValueDel").click(function(){
		    var ids = "";
		    var checks = $("input[name='ckb']:checked");
		    //判断是否选中记录
	    	if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            

				});
				
				var checkedIds =ids.substr(1,ids.length);
		        if (checkedIds != ""){
		             
		            if('${parameters.operateType?if_exists}'=='update'){
		               $("#ids").val(checkedIds);
		               isFeatureForProduct(checkedIds); 
		            }else{
                        //设置删除弹出框内容
					    $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
						$('#modal_confirm').modal('show');
		            }
		        }else{
                    if('${parameters.operateType?if_exists}'=='create'){
                        //设置删除弹出框内容
                        $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
                        $('#modal_confirm').modal('show');
                    }
				}
	        }else{
	           //设置提示弹出框内容
    	  	  $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    	  	  $('#modal_msg').modal();
	        }
	   })
	   
      
       //删除弹出框删除按钮点击事件
	   $('#modal_confirm #ok').click(function(e){
	        if('${parameters.operateType?if_exists}'=='update'){
	            if($("#ids").val()!=""){
	               //delProductFeatureValueByIds();
                   if(!curproductFeatureValue){
	                   $('input[class="js-checkchild"]:checked').each(function(){ 
			               ids = ids + "," + $(this).val();
			               var curFeatureId=$(this).parent().parent().eq(0).find("#curproductFeatureId").val();
			               if(curFeatureId!=""){
			                  $(this).parent().parent().hide();
			                  $(this).parent().parent().find('#curOptionType').val('delete');
			               }
					   });
				   }else{
	               		curproductFeatureValue.hide();
                        
	               		curproductFeatureValue.find('#curOptionType').val('delete');
	               		curproductFeatureValue="";
	               }
	            }else{
	               curproductFeatureValue.remove();
	            }
	        }else{
	            if(curproductFeatureValue){
	                curproductFeatureValue.remove();
	                curproductFeatureValue="";
	            }else{
	                delTr('ckb');
	            }
	        }
	   });
      
      
      
      
      //删除按钮事件
  	  $(document).on('click','.js-button',function(){
          	
		   if('${parameters.operateType?if_exists}'=='update'){
		   var curproductFeatureId=$(this).parent().parent().find("#curproductFeatureId").val();
		      $("#ids").val(curproductFeatureId);
		      if($("#ids").val()!=""){
		         curproductFeatureValue=$(this).parent().parent();
		         isFeatureForProduct(curproductFeatureId);
		      }else{
		         curproductFeatureValue=$(this).parent().parent();
			     //设置删除弹出框内容
				 $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				 $('#modal_confirm').modal('show');
		      } 
		   
		   }else{
		     curproductFeatureValue=$(this).parent().parent();
		     //设置删除弹出框内容
			 $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
			 $('#modal_confirm').modal('show');
		   }
      })
   })
   
   function dosubmit(){
     if(isCommitted==false){
        isCommitted = true;//提交表单后，将表单是否已经提交标识设置为true
        return true;//返回true让表单正常提交
     }else{
        return false;//返回false那么表单将不提交
     }
   }
   
   
   
   
   // 保存商品规格值信息到session中
   function saveProductFeatureList(productFeatureName,sequenceNum){
	  if (productFeatureName != "" || sequenceNum != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>saveProductFeatureList</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productFeatureName' : productFeatureName,
	             'sequenceNum' : sequenceNum
	        },
	        success: function(data) {
	          
	        }
	    });
	  } 
	}
	
   // 根据规格类型取出商品规格值 
   function getProductFeatureByTypeId(productFeatureTypeId){
	  if (productFeatureTypeId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductFeatureByTypeId</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productFeatureTypeId' : productFeatureTypeId
	        },
	        success: function(data) {
	            var productFeatureByTypeIdList= data.productFeatureByTypeIdList;

		        for (var i=0;i<productFeatureByTypeIdList.length;i++){
		            var productFeatureId=(productFeatureByTypeIdList[i].productFeatureId);
                	var productFeatureName =(productFeatureByTypeIdList[i].productFeatureName);
            		var sequenceNum =(productFeatureByTypeIdList[i].sequenceNum);
                    var trHtml="<tr><td><input  type='checkbox' class='js-checkchild' name='ckb' value="+productFeatureId+"><input type='hidden' id='curproductFeatureId' value="+productFeatureId+"><input type='hidden' id='curOptionType' value='update'></td><td><input type='text' value="+productFeatureName+"></td><td><input type='text' value="+sequenceNum+"></td><td><button type='button' class='js-button btn btn-danger btn-sm'>删除</button></td></tr>";
			        $('#tabFreatureValue>tbody').append(trHtml);
			    } 
	        }
	    });
	  } 
	}
	
	 function saveProductFeatureList1(tFeatureInfos){
	  if (tFeatureInfos != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>saveProductFeatureList</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'tFeatureInfos' : tFeatureInfos
	        },
	        success: function(data) {
	        }
	    });
	  } 
	}
   
   
   function delTr(ckb){
     //获取选中的复选框，然后循环遍历删除
     var ckbs=$("input[name="+ckb+"]:checked");
     //if(ckbs.size()==0){
       // alert("要删除指定行，需选中要删除的行！");
       // return;
       //}
      ckbs.each(function(){
              $(this).parent().parent().remove();
      });
   }
   
   
   
    // 判断商品规格是否是被商品使用
    function isFeatureForProduct(ids){
	  if (ids != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>isFeatureForProduct</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'featureTypeValueIds' : ids
	        },
	        success: function(data) {
	           var isUsedFlg=data.isUsedFlg;
	           if(isUsedFlg=="Y"){
	              //设置提示弹出框内容
	    	  	  $('#modal_msg #modal_msg_body').html("该商品规格值已使用，不能删除");
	    	  	  $('#modal_msg').modal();
	           }else{
		       	//设置删除弹出框内容
				$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				$('#modal_confirm').modal('show');
	           }
	        }
	    });
	  } 
   }
   
   // 将选择的记录id删除
   function delProductFeatureValueByIds(){
       var checkedIds =$("#ids").val(); 
       var productFeatureTypeId=$("#productFeatureTypeId").val();
       // 选中的项目
       jQuery.ajax({
           url: '<@ofbizUrl>delProductFeatureValueByIds</@ofbizUrl>',
           type: 'POST',
           data: {
             'checkedIds' : checkedIds,
             'productFeatureTypeId':productFeatureTypeId
           },
           success: function(data) {
               if('${parameters.operateType?if_exists}'=='update'){
               document.location.href="<@ofbizUrl>EditProductFeature?productFeatureTypeId="+productFeatureTypeId+"&"+"operateType=update"+"&"+"productFeatureTypeName=${parameters.productFeatureTypeName?if_exists}"+"&"+"description=${parameters.description?if_exists}</@ofbizUrl>";
               }
           }
       });
   }  	 
   
</script>
<div class="box box-info">
	<div class="box-header with-border">
	  <h3 class="box-title"><#if parameters.operateType?if_exists=="create">添加规格<#else>编辑规格</#if></h3>
	</div>
	<div class="box-body">
	    <form class="form-horizontal"  id="updateProductFeature" type="post" action="" name="updateProductFeature">
      		<div class="form-group" data-type="required" data-mark="规格名称">
		        <label class="control-label col-sm-2"><i class="required-mark">*</i>规格名称:</label>
		        <div class="col-sm-3">
		             <input type="text" class="form-control dp-vd" id="productFeatureTypeName" name="productFeatureTypeName" value="${parameters.productFeatureTypeName?if_exists}">
		             <p class="dp-error-msg"></p>
		        </div>                
		    </div>
		    
		    <div class="form-group">
		        <label class="control-label col-sm-2">规格备注:</label>
		        <div class="col-sm-3">
		             <input type="text" class="form-control" id="description" name="description" value="${parameters.description?if_exists}">
		        </div>                
		    </div>
		    
		    
            <div class="row m-b-10">
                <div class="col-sm-6">
                    <div class="dp-tables_btn">
                        <button type="button" class="btn btn-primary" id="addFeatureValue">添加规格值</button>
                        <button type="button" class="btn btn-primary" id="btnFeatureValueDel"> 
	                        ${uiLabelMap.BrandDel}
	                    </button>
               	    </div>
                </div>
                
            </div>
		    
		    
		     <div class="row">
                <div class="col-sm-12">
                    <table class="table table-bordered table-hover js-checkparent" id="tabFreatureValue">
                        <thead>
                        <tr>
                            <th><input class="js-allcheck" type="checkbox" id="checkAll"></th>
                            <th>规格值名称</th>
                            <th>排序 </th>
                            <th>${uiLabelMap.BrandOption}</th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
		    
		    
            <input type="hidden" name="operateType" id="operateType" value="${parameters.operateType?if_exists}"/>
			<input type="hidden" name="productFeatureTypeId" id="productFeatureTypeId" value="${parameters.productFeatureTypeId?if_exists}"/>
		    <input type="hidden" name="tFeatureInfos" id="tFeatureInfos" value=""/>
		    <input id="ids" type="hidden"/>
            <div class="modal-footer">
	            <button type="button" class="btn btn-default" id="btnCancel">${uiLabelMap.BrandCancel}</button>
	            <button type="button" class="btn btn-primary" id="btnFeatureSave">保存</button>
	        </div>
        </form>
    </div><!-- /.box-body -->
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
	      <div class="modal-footer">
	        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.FacilityOk}</button>
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
	        <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
	      </div>
	      <div class="modal-body">
	        <h4 id="modal_confirm_body"></h4>
	      </div>
	      <div class="modal-footer">
	      	<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
	        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.BrandDel}</button>
	      </div>
	    </div>
	  </div>
	</div><!-- 删除确认弹出框end -->

		
