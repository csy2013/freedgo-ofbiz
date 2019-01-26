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

.has-error {
  border-color: #ed5565;
}
	</style>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<!-- Main content -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">消费积分设置</h3>
        </div>
        <div class="box-body">			
            <!-- 分割线 start-->
            <div class="cut-off-rule bg-gray"></div>
			<!-- 分割线  start-->
            <form class="form-horizontal" role="form"  id="IntegralForm" action="" name="" class="">
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">一般积分设置</label>
	                <div class="col-sm-3">
	                    <!--是否有新增的权限 -->
                        <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
	                    <button type="button" class="btn btn-primary js-addpara" id="btnAddProductParameter">
	                    <i class="fa fa-plus"></i>${uiLabelMap.Add}
	                    </button>
	                    </#if>
	                </div>
	            </div>
            </div>
           
            <div class="row">
                <div class="form-group ">
                   <label  class="col-sm-2 control-label"></label>
                   <div class="col-sm-8 simple-vip">
                    <#if normalList?has_content>
                      <table class="table table-bordered table_b_c js-table_1">
					      <thead>
					           <tr>
					               <th>会员等级</th>
					               <th>会员类型</th>
					               <th>每积一分消费金额</th>
					               <!--是否有新增的权限-->
					               <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
					               <th>${uiLabelMap.BrandOption}</th>
					               </#if>
					           </tr>
					      </thead>
					      <tbody>
					      <#list normalList as noList>
					      <tr>
        	      <td>
        	      <select class="form-control level myGrade" >
        	      <#if noList.levelId?has_content>
        	      <option value="0">请选择</option>
        	      <option value="-1" >All</option>
        	      <#if partyLevelList?has_content>
        	      <#list partyLevelList as levelList>
        	      <option <#if noList.levelId==levelList.levelId> selected</#if> value="${levelList.levelId?if_exists}">${levelList.levelName?if_exists}</option>
        	      </#list>
        	      </#if>
        	      <#else>
        	      <option value="0">请选择</option>
        	      <option value="-1" selected>All</option>
        	      <#if partyLevelList?has_content>
        	      <#list partyLevelList as levelList>
        	      <option  value="${levelList.levelId?if_exists}">${levelList.levelName?if_exists}</option>
        	      </#list>
        	      </#if>
        	      </#if>
        	      </select>
        	      </td>
        	      <td>
        	       <#if noList.levelId?has_content>
        	       <#if noList.levelId!='-1'> <#assign partyLevelType = delegator.findByPrimaryKey("PartyLevelType", Static["org.ofbiz.base.util.UtilMisc"].toMap("levelId", "${noList.levelId?if_exists}"))></#if>
        	      <#if partyLevelType1?has_content>
        	        <#if partyLevelType1.partyType=='MEMBER'>
        	        个人会员
        	        <#else>
        	        企业会员
        	        </#if>
        	        </#if>
        	       </#if>
        	       
        	       
        	       </td>
        	      <td>
                  <input type="text" class="dp-vd " name="integralValue" id="" value="${noList.integralValue?if_exists}">
        	      <input type="hidden" name="productCategoryId" id="" value="${noList.productCategoryId?if_exists}">
        	      <input type="hidden" name="productId" id="" value="${noList.productId?if_exists}">
        	      <input type="hidden" name="partyIntegralType" id="" value="${noList.partyIntegralType?if_exists}">
        	      </td>
        	      
        	      <!--是否有新增的权限-->
        	      <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
        	      <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>
        	      </#if>
        	      
        	             </tr>
        	      </#list>
					      </tbody>
		    		  </table>
		    		  </#if>
                   </div>
                </div>
                
            </div>
            
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">特殊分类积分设置</label>
	                <div class="col-sm-3">
	                    <!--是否有新增的权限-->
                        <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
	                    <button type="button" class="btn btn-primary js-addspecial" id="">
	                    <i class="fa fa-plus"></i>${uiLabelMap.Add}
	                    </button>
	                    </#if>
	                    </button>
	                </div>
	            </div>
            </div>
            <div class="row">
                <div class="form-group">
                   <label  class="col-sm-2 control-label"></label>
                   <div class="col-sm-8">
                      <table class="table table-bordered table_b_c js-table_special">
					      <thead>
					           <tr>
					               <th>会员等级</th>
					               <th>会员类型</th>
					               <th>商品分类</th>
					               <th>每积一分消费金额</th>
					                <!--是否有新增的权限-->
					               <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
					               <th>${uiLabelMap.BrandOption}</th>
					               </#if>
					               
					           </tr>
					      </thead>
					      <tbody>
					      <#if specialList?has_content>
					      <#list specialList as spList>
					      <tr>
        	      <td>
        	      <select class="form-control level" >
        	      <#if spList.levelId?has_content>
        	      <option value="-1" >All</option>
        	      <#if partyLevelList?has_content>
        	      <#list partyLevelList as levelList>
        	      <option <#if spList.levelId==levelList.levelId> selected</#if> value="${levelList.levelId?if_exists}">${levelList.levelName?if_exists}</option>
        	      </#list>
        	      </#if>
        	      <#else>
        	      <option value="-1" selected>All</option>
        	      <#if partyLevelList?has_content>
        	      <#list partyLevelList as levelList>
        	      <option  value="${levelList.levelId?if_exists}">${levelList.levelName?if_exists}</option>
        	      </#list>
        	      </#if>
        	      </#if>
        	      </select>
        	      </td>
        	      <td>
        	       <#if spList.levelId?has_content>
        	       <#if spList.levelId!='-1'> <#assign partyLevelType1 = delegator.findByPrimaryKey("PartyLevelType", Static["org.ofbiz.base.util.UtilMisc"].toMap("levelId", "${spList.levelId?if_exists}"))></#if>
        	       <#if partyLevelType1?has_content>
        	        <#if partyLevelType1.partyType=='MEMBER'>
        	        个人会员
        	        <#else>
        	        企业会员
        	        </#if>
        	        </#if>
        	       </#if>
        	      </td>
        	      <td>
        	      <input type="text" name="productCategoryId" id=""  class="treeName" value="${spList.productCategoryId?if_exists}">
        	      </td> 
        	      <td><input type="text" name="integralValue" id="" value="${spList.integralValue?if_exists}">
        	      <input type="hidden" name="productId" id="" value="${spList.productId?if_exists}">
        	      <input type="hidden" name="partyIntegralType" id="" value="${spList.partyIntegralType?if_exists}">
        	      </td>
        	      <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
        	      <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td> 
        	      </#if>
        	      </tr>
					      </#list>
					      </#if>
					      </tbody>
		    		  </table>
                   </div>
                </div>
            </div>
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">特殊商品积分设置</label>
	                <div class="col-sm-3">
	                    <!--是否有新增的权限-->
                        <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
	                    <button type="button" class="btn btn-primary js-addproduct" id="btnAddproduct">
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
                      <table class="table table-bordered table_b_c js-table_addproduct">
					      <thead>
					           <tr>
					               <th>会员等级</th>
					               <th>会员类型</th>
					               <th>商品编码</th>
					               <th>每积一分消费金额</th>
					               <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
					               <th>${uiLabelMap.BrandOption}</th>
					               </#if>
					           </tr>
					      </thead>
					      <tbody>
					      <#if specialProList?has_content>
					      <#list specialProList as proList>
					      <tr>
		        	      <td>
		        	      <select class="form-control level" >
		        	      <#if proList.levelId?has_content>
		        	      <option value="-1" >All</option>
		        	      <#if partyLevelList?has_content>
		        	      <#list partyLevelList as levelList>
		        	      <option <#if proList.levelId==levelList.levelId> selected</#if> value="${levelList.levelId?if_exists}">${levelList.levelName?if_exists}</option>
		        	      </#list>
		        	      </#if>
		        	      <#else>
		        	      <option value="-1" selected>All</option>
		        	      <#if partyLevelList?has_content>
		        	      <#list partyLevelList as levelList>
		        	      <option  value="${levelList.levelId?if_exists}">${levelList.levelName?if_exists}</option>
		        	      </#list>
		        	      </#if>
		        	      </#if>
		        	      </select>
		        	      </td>
		        	      <td>
		        	      
		        	      <#if proList.levelId?has_content>
        	       <#if proList.levelId!='-1'> <#assign partyLevelType2 = delegator.findByPrimaryKey("PartyLevelType", Static["org.ofbiz.base.util.UtilMisc"].toMap("levelId", "${proList.levelId?if_exists}"))></#if>
        	       <#if partyLevelType2?has_content>
        	        <#if partyLevelType2.partyType=='MEMBER'>
        	        个人会员
        	        <#else>
        	        企业会员
        	        </#if>
        	        </#if>
        	       </#if>
		        	      </td>
		        	      <td><input type="text" name="productId" id="" class="gss_product" value="${proList.productId?if_exists}" readonly></td>
		        	      <td><input type="text" name="integralValue" id="" value="${proList.integralValue?if_exists}">
		        	      <input type="hidden" name="productCategoryId" id="productCategoryId" value="${proList.productCategoryId?if_exists}">
		        	      <input type="hidden" name="partyIntegralType" id="" value="${proList.partyIntegralType?if_exists}">
		        	      </td>
		        	      <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
		        	      <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>
		        	      </#if>
		        	      </tr>
					      </#list>
					      </#if>
					      </tbody>
		    		  </table>
                   </div>
                </div>
            </div>
            <input type="hidden" id="option" value="${option?if_exists}">
            <input type="hidden" id="level" value="${level?if_exists}">
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">不积分分类设置</label>
	                <div class="col-sm-3">
	                    <!--是否有新增的权限-->
                        <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
	                    <button type="button" class="btn btn-primary" id="noAdd">
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
                      <table class="table table-bordered table_b_c js_table_noncategory">
					      <thead>
					           <tr>
					               <th>商品分类</th>
					               <th>分类名称</th>
					               <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
					               <th>${uiLabelMap.BrandOption}</th>
					               </#if>
					           </tr>
					      </thead>
					      
					      <tbody>
					      <#if nonCategoryList?has_content>
					      <#list nonCategoryList as noncategoryList>
						  <tr>
			    	      <td><input type="text" name="productCategoryId" id="" class="productCategory" value="${noncategoryList.productCategoryId?if_exists}"  readonly/>
			    	      <input type="hidden" name="productId" id="" class="" value="${noncategoryList.productId?if_exists}">
			    	      <input type="hidden" name="integralValue" id="" class="" value="${noncategoryList.integralValue?if_exists}">
			    	      <input type="hidden" name="partyIntegralType" id="" class="" value="${noncategoryList.partyIntegralType?if_exists}">
			    	      <input type="hidden" name="levelId" id="" class="" value="${noncategoryList.levelId?if_exists}">
			    	      </td>
			    	      <#assign product_category = delegator.findByPrimaryKey("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId",noncategoryList.productCategoryId?if_exists))/>
					      <#if product_category?has_content>
					     <td><input type="text" name="productCategoryName" id="" class="" value="${product_category.categoryName?if_exists}" readonly /></td>
					      <#else>
					      <td><input type="text" name="productCategoryName" id="" class="" value="" readonly /></td>
					      </#if>
					      <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
			    	      <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>
        	              </#if>
        	              </tr>
					      </#list>
					      </#if>
					      </tbody>
		    		  </table>
                   </div>
                </div>
            </div>
            
            <div class="row">
	            <div class="form-group">
	                <label  class="col-sm-2 control-label">不积分商品设置</label>
	                <div class="col-sm-3">
	                    <!--是否有新增的权限-->
                        <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
	                    <button type="button" class="btn btn-primary" id="btnNoProduct">
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
                      <table class="table table-bordered table_no_pro">
					      <thead>
					           <tr>
					               <th>商品编码</th>
					               <th>商品名称</th>
					                <!--是否有新增的权限-->
                                   <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
					               <th>${uiLabelMap.BrandOption}</th>
					               </#if>
					           </tr>
					      </thead>
					      <tbody>
					      <#if nonProductList?has_content>
					      <#list nonProductList as nonProList>
					      <tr>
					      <td><input type="text" name="productId" id="" class="gss_product1" value="${nonProList.productId?if_exists}"  readonly/>
					      <input type="hidden" name="productCategoryId" id="" class="" value="${nonProList.productCategoryId?if_exists}">
					      <input type="hidden" name="integralValue" id="" class="" value="${nonProList.integralValue?if_exists}">
					      <input type="hidden" name="partyIntegralType" id="" class="" value="${nonProList.partyIntegralType?if_exists}">
					      <input type="hidden" name="levelId" id="" class="" value="${nonProList.levelId?if_exists}">
					      </td>
					      <#assign product  = delegator.findByPrimaryKey("Product",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",nonProList.productId?if_exists))/>
					      <#if product?has_content>
					      <td><input type="text" name="productName" id="" class="" value="${product.productName?if_exists}" readonly /></td>
					      <#else>
					      <td><input type="text" name="productName" id="" class="" value="" readonly /></td>
					      </#if>
					      <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
					      <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>
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
        <#if security.hasEntityPermission("PARTY_CONSUME", "_CREATE", session)>
        <button id="save" type="button" class="btn btn-primary m-l-20">保存</button>
        </#if>
        <button id="btnReturn" type="button" class="btn btn-primary m-l-20" data-dismiss="modal">返回</button>
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
    
    var option="<option value='-1'>All</option>";
    option=option+$('#option').val();
    
    /**加载商品 分类*/
    function loadProductType() {
        $.post("<@ofbizUrl>getProductCategoryList</@ofbizUrl>", function (data) {
            console.log(data)
        $.fn.zTree.init($("#treeDemo"), setting, data.productCategoryList);
        });
      }
       //定义一个数组接受等级的所有value和值
       var allGrade= $('#level').val();
       allGrade = $.parseJSON(allGrade)
       //定义被选中的存在这个数组里
        var selectGrade = [];
        var all=[];
        var data={"num":"0"};
        var add=[];
        for(var i=0;i<allGrade.length;i++){
            var a=allGrade[i].value;
            all.push(a);
        }
    
        //点击添加按钮的时候，如果下面没有创建等级，要连表头也去创建,判断table是否为空
            $(document).on("click",'.js-addpara',function(){
            selectGrade=[];
            //一般积分设置
           $('.js-table_1>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           var tdArr = $(this);
           if(tdArr.length>0){
           for(var i=0;i<tdArr.length;i++){
            var levelId= tdArr.eq(i).find('td').eq(0).find("select").val();//会员等级
             selectGrade.push(levelId);
              }
           }
           });
            console.log(selectGrade);
                        if (!$(".simple-vip").find('.table').length) {
                        var th='<table class="table table-bordered table_b_c js-table_1"><thead> '
                                + '<tr><th>会员等级</th> <th>会员类型</th> <th>每积一分消费金额</th> <th>操作</th> </tr>'
                                + '</thead>'
                                + '<tbody>'
                                + '<tr><td><select class="form-control level myGrade">' + gradDisabled() + '</select>'
                                + '</td>'
                                +'<td></td>'
                                + '<td><input type="text" name="integralValue" value="" placeholder="请输入整数">'
                                + '<input type="hidden" name="productCategoryId" value="">'
                                + '<input type="hidden" name="productId" value="">'
                                + '<input type="hidden" name="partyIntegralType" value="NORMAL">'
                                + '</td>'
                                + '<td class="fc_td">'
                                + '<button type="button" class="js-button btn btn-danger btn-sm simple-del">删除 </button>'
                                + '</td>'
                                + '</tr>'
                                + '</tbody>'
                                + '</table>';
                        //把创建的表放到div里面
                        $('.simple-vip').append(th);
                    }else{
					    if(($.inArray('-1',selectGrade)!=-1)){
					     //设置提示弹出框内容 
						  $('#modal_msg #modal_msg_body').html('已添加全部等级');
		    			  $('#modal_msg').modal();
					    console.log('已添加全部等级');
                        return false;
                          }
                        if($(".myGrade").length==all.length-1){
                        return false;
                         }
                        var tr = '<tr>'
                                + '<td><select class="form-control level myGrade" >' + gradDisabled() + '</select></td>'//等级
                                +'<td></td>'
                                + '<td><input type="text" name="integralValue" id="" value="" placeholder="请输入整数">'//每积一分消费金额
                                + '<input type="hidden" name="productCategoryId" id="" value="">'//商品分类ID
                                + '<input type="hidden" name="productId" id="" value="">'//商品Id
                                + '<input type="hidden" name="partyIntegralType" id="" value="NORMAL">'//积分类型
                                + '</td>'
                                + '<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm simple-del">删除</button></td>'
                                + '</tr>';
                        $('.js-table_1>tbody').append(tr);
                        }
            });
            
          var levelArray=[];
        $(".simple-vip").on("change",".myGrade",function(){
            
              var  special=$(this)
                   levelId=$(this).val();
              if(levelId!='-1'){
                    $.ajax({
				url: "getLevelType",
				type: "POST",
				data: {levelId:$(this).val()},
				dataType : "json",
				success: function(data){
				if(data.partyLevel){
				 var partyType=data.partyLevel.partyType;
				 if(partyType&&partyType=='MEMBER'){
				 special.parent().next().html('个人会员');
				 }else{
				 special.parent().next().html('企业会员');
				 }
				}
				}
			});
              }
	
            
            selectGrade=[];
             //一般积分设置
           $('.js-table_1>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           var tdArr = $(this);
           if(tdArr.length>0){
           for(var i=0;i<tdArr.length;i++){
            var levelId= tdArr.eq(i).find('td').eq(0).find("select").val();//会员等级
             levelArray.push(levelId);
              }
           }
           });
            $(".myGrade").each(function(){
                var item=$(this).find("option:selected").val();
                //如果选中的是all
                if(item==-1){
                    add.push(-1);
                    selectGrade=all;
                }else{
                    //当改变选择的时候，如果之前选择的是all，要在不选all的情况下把flag变为true
                    flag=true;
                    $('.js-addpara').on("click");
                    if(item!=0&&$.inArray(item,levelArray)!=-1){
                        selectGrade.push(item);
                        add=[];
                    }else{
                        add=[];
                    }
                }
            });
            if(selectGrade.length>0&&($.inArray("-1",selectGrade)==-1)){
                selectGrade.push("-1");
            }
            if(selectGrade.length==all.length){
                add.push(-1);
            }
            bulidOtherSelect();
        });
//        点击删除按钮
        $(".simple-vip").on("click",".simple-del",function(){
          var b=$(this).parent().parent().find(".myGrade option:selected").val();
            //            更新那个存储被选中的数组里的内容
            if(b==-1){
                selectGrade=[];
                $(".myGrade").each(function(){
                    $(this).html(gradDisabled());
                });
            }else{
                for(var i=0;i<selectGrade.length;i++){
                    if(selectGrade[i]==b){
                        selectGrade.splice(i,1);
                        break;
                    }
                }
                bulidOtherSelect();
            }
            add=[];
            $(this).parent().parent().remove();
        });


        
        // 判断在新建的时候哪些等级要加上disabled
        function gradDisabled(){
            var optionStr='<option value="0">请选择</option>';
            $.each(allGrade,function(idx,item){
             console.log(selectGrade.length);
            if($.inArray(item.value,selectGrade)!=-1){
               optionStr+='<option value="'+item.value+'" disabled="disabled">'+item.view+'</option>';
             }else{
             if(item.value==-1&&selectGrade.length!=0&$.inArray(-1,selectGrade)==-1){
             optionStr+='<option value="'+item.value+'" disabled="disabled">'+item.view+'</option>';
             }else{
             optionStr+='<option value="'+item.value+'">'+item.view+'</option>';
             }
                }
            });
            console.log(optionStr);
            return optionStr;
        }
        //重构除当前点的这个其他的所有的option
        function bulidOtherSelect(){
            $(".myGrade").each(function(){
              $(this).children().not(":selected").each(function(){
                  var thisOption=$(this);
                  thisOption.removeAttr("disabled");
                  if($.inArray(thisOption.val(),selectGrade)!=-1){
                      thisOption.attr("disabled","disabled");
                  }
              });
            })
        }
       
       
       //删除按钮事件
  	    $(document).on('click','.js-button',function(){
  	       $(this).parent().parent().remove();
        })
        
        //添加特殊分类积分事件
        $(document).on('click','.js-addspecial',function(){
            loadProductType();
        	var tr='<tr>'
        	      +'<td><select class="form-control level select2 specialType" >+'+option+'+</select></td>'
        	      +'<td>'
        	      +'</td>'
        	      +'<td><input type="text" name="productCategoryId"  value="" class="treeName" >'//商品分类ID
        	      +'</td>' 
        	      +'<td><input type="text" name="integralValue" id="" value="" placeholder="请输入整数">'//每积一分消费金额
        	      +'<input type="hidden" name="productId" id="" value="">'//商品Id
        	      +'<input type="hidden" name="partyIntegralType" id="" value="SPECIALCATEGORY">'//积分类型
        	      +'</td>'
        	      +'<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>' 
        	      +'</tr>';
 			$('.js-table_special>tbody').append(tr);
  		})
  		
  		 //添加特殊分类积分事件(商品分类)
  		 $(document).on('click','.treeName',function(){
  		    var tdArr = $(this);
  		    var levelId=tdArr.closest('tr').find(':selected').val();//获取点击等级Id
  		    var tr=$('.js-table_special').find('tbody > tr');
  		    var array=[];
  	        $('#secondLinkType').show();
		    $.dataSelectModal({
			url: "/catalog/control/CategoryListModalPage?externalLoginKey=${externalLoginKey}",
			width:	"800",
			title:	"选择分类",
			selectCallBack: function(el){
			    if(tr.size() > 0){
	    				 tr.each(function(){ 
	    				 var level_id=$(this).find(':selected').val();//等级ID 
                         var productCategoryId=$(this).find('td').eq(1).find('input[name=productCategoryId]').val();//商品分类编号
                          if(levelId==level_id&&productCategoryId!=""||level_id=='-1'){
                          array.push(productCategoryId);
                          }
	    				 });
                         console.log(array);
                         
                         if($.inArray(String(el.data('id')), array)==-1){
                          tdArr.val(el.data('id'));
                          }else{ 
		                   //设置提示弹出框内容 
						  $('#modal_msg #modal_msg_body').html('['+el.data('id')+']'+'已在列表中');
		    			  $('#modal_msg').modal();
                          }
	    				 }else{
	    				 tdArr.val(el.data('id'));
	    				 }
			   }
		     });
         })
  		 
        //添加特殊积商品分事件
        $(document).on('click','.js-addproduct',function(){
        	var tr='<tr>'
        	      +'<td><select class="form-control specialLevel" >+'+option+'+</select></td>'
        	      +'<td></td>'
        	      +'<td><input type="text" name="productId" id="" class="gss_product" value="" readonly="readonly"></td>'
        	      +'<td><input type="text" name="integralValue" id="" value="" placeholder="请输入整数">'
        	      +'<input type="hidden" name="productCategoryId" id="productCategoryId" value="">'//商品分类ID
        	      +'<input type="hidden" name="partyIntegralType" id="" value="SPECIALPRODUCT">'//积分类型
        	      +'</td>' 
        	      +'<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>'
        	      +'</tr>';
 			$('.js-table_addproduct>tbody').append(tr);
  		})
	
	    //添加特殊商品积分事件(商品编码)
  		 $(document).on('click','.gss_product',function(){
  		    var tdArr = $(this);
  		    var levelId=tdArr.closest('tr').find(':selected').val();//获取点击等级Id
  		    var tr=$('.js-table_addproduct').find('tbody > tr');
  		    var array=[];
  	        $.dataSelectModal({
	    				url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
	    				width:	"800",
	    				title:	"选择商品",
	    				selectCallBack: function(el){
	    				 if(tr.size() > 0){
	    				 tr.each(function(){ 
	    				 var level_id=$(this).find(':selected').val();//等级ID 
                         var productId=$(this).find('td').eq(1).find('input[name=productId]').val();//商品编号
                          if(levelId==level_id&&productId!=""||level_id=='-1'){
                          array.push(productId);
                          }
	    				 });
                         console.log(array);
                         if($.inArray(String(el.data('id')), array)==-1){
                          tdArr.val(el.data('id'));
                          }else{                                                          
		                   //设置提示弹出框内容 
						  $('#modal_msg #modal_msg_body').html('['+el.data('id')+']'+'已在列表中');
		    			  $('#modal_msg').modal();
                          }
	    				 }else{
	    				 tdArr.val(el.data('id'));
	    				 }
	    				}
	    			});
         })
         
         
         
	  //不积分分类添加按钮
    $('#noAdd').click(function(){
        $('#secondLinkType').show();
		$.dataSelectModal({
			url: "/catalog/control/CategoryListModalPage?externalLoginKey=${externalLoginKey}",
			width:	"800",
			title:	"选择分类",
			selectCallBack: function(el){
			    var checks = $('.js_table_noncategory>tbody').find("tr");
				var array=[];
			  if(checks.size() > 0 ){
			   checks.each(function(){ 
			   var productCategoryId=$(this).find('input[name=productCategoryId]').val()
			   array.push(productCategoryId);
			   });
			  if($.inArray(String(el.data('id')), array)==-1){
			   var tr='<tr>'
        	      +'<td><input type="text" name="productCategoryId" id="" class="productCategory" value="'+el.data('id')+'"  readonly/>'
        	      +'<input type="hidden" name="productId" id="" class="" value="">'
        	      +'<input type="hidden" name="integralValue" id="" class="" value="">'
        	      +'<input type="hidden" name="partyIntegralType" id="" class="" value="NONCATEGORY">'
        	      +'<input type="hidden" name="levelId" id="" class="" value="">'
        	      +'</td>'
        	      +'<td><input type="text" name="productCategoryName" id="" class="" value="'+el.data('name')+'" readonly /></td>'
        	      +'<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>'
        	      +'</tr>';
 			   $('.js_table_noncategory>tbody').append(tr);
			     }else{
			     //设置提示弹出框内容 
				  $('#modal_msg #modal_msg_body').html('['+el.data('name')+']'+'已在列表中');
    			  $('#modal_msg').modal();
			     }
			  }else{
			  var tr='<tr>'
        	      +'<td><input type="text" name="productCategoryId" id="" class="productCategory" value="'+el.data('id')+'"  readonly/>'
        	      +'<input type="hidden" name="productId" id="" class="" value="">'
        	      +'<input type="hidden" name="integralValue" id="" class="" value="">'
        	      +'<input type="hidden" name="partyIntegralType" id="" class="" value="NONCATEGORY">'
        	      +'<input type="hidden" name="levelId" id="" class="" value="">'
        	      +'</td>'
        	      +'<td><input type="text" name="productCategoryName" id="" class="" value="'+el.data('name')+'" readonly /></td>'
        	      +'<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>'
        	      +'</tr>';
 			   $('.js_table_noncategory>tbody').append(tr);
			  }
			}
		});
    });
    
         //不积分分类(商品分类)
  		 $(document).on('click','.productCategory',function(){
  		    var tdArr = $(this);
  	        $('#secondLinkType').show();
		    $.dataSelectModal({
			url: "/catalog/control/CategoryListModalPage?externalLoginKey=${externalLoginKey}",
			width:	"800",
			title:	"选择分类",
			selectCallBack: function(el){
			    var checks = $('.js_table_noncategory>tbody').find("tr");
				var array=[];
				if(checks.size() > 0 ){
				checks.each(function(){ 
			   var productCategoryId=$(this).find('input[name=productCategoryId]').val()
			   array.push(productCategoryId);
			   });
			    if($.inArray(String(el.data('id')), array)==-1){
			    tdArr.val(el.data('id'));
			    }else{
			     //设置提示弹出框内容 
				  $('#modal_msg #modal_msg_body').html('['+el.data('name')+']'+'已在列表中');
    			  $('#modal_msg').modal();
			    }
				}else{
				tdArr.val(el.data('id'));
				}
				
			   }
		     });
         })
  		 
  		 
  		  //不积分商品积分事件(商品编码)
  		 $(document).on('click','.gss_product1',function(){
  		    var tdArr = $(this);
  		    var levelId=tdArr.closest('tr').find(':selected').val();//获取点击等级Id
  		    var tr=$('.table_no_pro').find('tbody > tr');
  		    var array=[];
  	        $.dataSelectModal({
	    				url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
	    				width:	"800",
	    				title:	"选择商品",
	    				selectCallBack: function(el){
	    				 if(tr.size() > 0){
	    				 tr.each(function(){ 
	    				 var level_id=$(this).find(':selected').val();//等级ID 
                         var productId=$(this).find('td').eq(0).find('input[name=productId]').val();//商品编号
                          if(levelId==level_id&&productId!=""){
                          array.push(productId);
                          }
	    				 });
                         console.log(array);
                         if($.inArray(String(el.data('id')), array)==-1){
                          tdArr.val(el.data('id'));
                          }else{ 
		                   //设置提示弹出框内容 
						  $('#modal_msg #modal_msg_body').html('['+el.data('id')+']'+'已在列表中');
		    			  $('#modal_msg').modal();
                          }
	    				 }else{
	    				 tdArr.val(el.data('id'));
	    				 }
	    				}
	    			});
         })
         
	 // 不积分商品设置
    $('#btnNoProduct').click(function(){
		$.dataSelectModal({
			url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
			width:	"800",
			title:	"选择商品",
			selectCallBack: function(el){
			var checks = $('.table_no_pro>tbody').find("tr");
			var array=[];
			if(checks.size() > 0 ){
			checks.each(function(){ 
				var productId=$(this).find('input[name=productId]').val();
				array.push(productId);
					});
			 if($.inArray(String(el.data('id')), array)==-1){
			 var tr='<tr>'
			      +'<td><input type="text" name="productId" id="" class="" value="'+el.data('id')+'"  readonly/>'
			      +'<input type="hidden" name="productCategoryId" id="" class="" value="">'
			      +'<input type="hidden" name="integralValue" id="" class="" value="">'
			      +'<input type="hidden" name="partyIntegralType" id="" class="" value="NONPRODUCT">'
			      +'<input type="hidden" name="levelId" id="" class="" value="">'
			      +'</td>'
			      +'<td><input type="text" name="productName" id="" class="" value="'+el.data('name')+'" readonly /></td>'
			      +'<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>'
			      +'</tr>';
 			$('.table_no_pro>tbody').append(tr);
			 }else{
			 //设置提示弹出框内容 
				  $('#modal_msg #modal_msg_body').html('['+el.data('name')+']'+'已在列表中');
    			  $('#modal_msg').modal();
			 }
			}else{
			var tr='<tr>'
			      +'<td><input type="text" name="productId" id="" class="" value="'+el.data('id')+'"  readonly/>'
			      +'<input type="hidden" name="productCategoryId" id="" class="" value="">'
			      +'<input type="hidden" name="integralValue" id="" class="" value="">'
			      +'<input type="hidden" name="partyIntegralType" id="" class="" value="NONPRODUCT">'
			      +'<input type="hidden" name="levelId" id="" class="" value="">'
			      +'</td>'
			      +'<td><input type="text" name="productName" id="" class="" value="'+el.data('name')+'" readonly /></td>'
			      +'<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>'
			      +'</tr>';
 			$('.table_no_pro>tbody').append(tr);
			}
	    	 }
	    			});
        });
        
        //添加提交按钮点击事件
	    $('#save').click(function(){
	        $('#IntegralForm').dpValidate({
			  clear: true
			});
			$('#IntegralForm').submit();
	    });
       //编辑表单校验
       $('#IntegralForm').dpValidate({
        validate: true,
        callback: function(){
        	var integral="";
	        var normalInfo="";
	        var normalInfoFlag=true;
	        //是否为整数
	        var reg=/^\d*(.\d{1,2})?$/;
	     //一般积分设置
        $('.js-table_1>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           var tdArr = $(this);
           for(var i=0;i<tdArr.length;i++){
           var levelId= tdArr.eq(i).find('td').eq(0).find("select").val();//会员等级
           if(levelId==0){
           tdArr.eq(i).find('td').eq(0).find("select").addClass('has-error')
           normalInfoFlag=false;
           }
           var td= tdArr.eq(i).find('td').eq(2);
           var integralValue=td.find("input[name=integralValue]").val();//每积一分消费金额
           var r = integralValue.match(reg); 
           console.log(r);
           if(r==null){
            td.find("input[name=integralValue]").addClass('has-error');
            normalInfoFlag=false;
           } 
           var productCategoryId=td.find("input[name=productCategoryId]").val();//商品分类ID
           var productId=td.find("input[name=productId]").val();//商品Id
           var partyIntegralType=td.find("input[name=partyIntegralType]").val();//积分类型
           normalInfo+=levelId+":"+integralValue+":"+productCategoryId+":"+productId+":"+partyIntegralType+",";
           }
	    }); 
	    //  特殊分类积分设置
	    var specialInfo="";
	    var specialInfoFlag=true;
        $('.js-table_special>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           var tdArr = $(this);
           for(var i=0;i<tdArr.length;i++){
           var levelId= tdArr.eq(i).find('td').eq(0).find("select").val();//会员等级
           var td= tdArr.eq(i).find('td').eq(2);
           var productCategoryId=td.find("input[name=productCategoryId]").val();//商品分类ID
           var td1= tdArr.eq(i).find('td').eq(3);
           var integralValue=td1.find("input[name=integralValue]").val();//每积一分消费金额
           var r = integralValue.match(reg); 
           if(r==null){
            td1.find("input[name=integralValue]").addClass('has-error');
            specialInfoFlag=false;
           } 
           var productId=td1.find("input[name=productId]").val();//商品Id
           var partyIntegralType=td1.find("input[name=partyIntegralType]").val();//积分类型
           specialInfo+=levelId+":"+integralValue+":"+productCategoryId+":"+productId+":"+partyIntegralType+",";
           }
	    }); 
	    //特殊商品积分设置
	    var specialProInfo="";
	    var specialProFlag=true;
        $('.js-table_addproduct>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           var tdArr = $(this);
           for(var i=0;i<tdArr.length;i++){
           var levelId= tdArr.eq(i).find('td').eq(0).find("select").val();//会员等级
           var td= tdArr.eq(i).find('td').eq(2);
           var productId=td.find("input[name=productId]").val();//商品Id
           var td1= tdArr.eq(i).find('td').eq(3);
           var integralValue=td1.find("input[name=integralValue]").val();//每积一分消费金额
           var r = integralValue.match(reg);
           if(r==null){
            td1.find("input[name=integralValue]").addClass('has-error');
            specialProFlag=false;
           } 
           var productCategoryId=td1.find("input[name=productCategoryId]").val();//商品分类Id
           var partyIntegralType=td1.find("input[name=partyIntegralType]").val();//积分类型
           specialProInfo+=levelId+":"+integralValue+":"+productCategoryId+":"+productId+":"+partyIntegralType+",";
           }
	    }); 
	    //不积分分类设置
	    var nonCategoryInfo="";
        $('.js_table_noncategory>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           var tdArr = $(this);
           for(var i=0;i<tdArr.length;i++){
           var td= tdArr.eq(i).find('td').eq(0);
           var productCategoryId=td.find("input[name=productCategoryId]").val()//商品分类Id
           var productId=td.find("input[name=productId]").val();//商品Id
           var integralValue=td.find("input[name=integralValue]").val();//每积一分消费金额
           var partyIntegralType=td.find("input[name=partyIntegralType]").val();//积分类型
           var levelId=td.find("input[name=levelId]").val();//等级ID
           nonCategoryInfo+=levelId+":"+integralValue+":"+productCategoryId+":"+productId+":"+partyIntegralType+",";
           }
	    }); 
	    //不积分分类设置
	    var noProInfo="";
        $('.table_no_pro>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           var tdArr = $(this);
           for(var i=0;i<tdArr.length;i++){
           var td= tdArr.eq(i).find('td').eq(0);
           var productCategoryId=td.find("input[name=productCategoryId]").val()//商品分类Id
           var productId=td.find("input[name=productId]").val();//商品Id
           var integralValue=td.find("input[name=integralValue]").val();//每积一分消费金额
           var partyIntegralType=td.find("input[name=partyIntegralType]").val();//积分类型
           var levelId=td.find("input[name=levelId]").val();//等级ID
           noProInfo+=levelId+":"+integralValue+":"+productCategoryId+":"+productId+":"+partyIntegralType+",";
           }
	    }); 
	    integral=normalInfo+specialInfo+specialProInfo+nonCategoryInfo+noProInfo;
	    if(normalInfoFlag&&specialInfoFlag&&specialProFlag)
	    {
	    			//异步调用添加积分规则
			$.ajax({
				url: "createPartyIntegral",
				type: "POST",
				data: {integral:integral},
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
	    }else{
	     //设置提示弹出框内容 
				  $('#modal_msg #modal_msg_body').html('请正确填写必要信息!');
    			  $('#modal_msg').modal();
	       }
          }
        })
        
	   $(".js-table_addproduct").on("change",".specialLevel",function(){
              var  special=$(this)
                   levelId=$(this).val();
              if(levelId!='-1'){
	      $.ajax({
				url: "getLevelType",
				type: "POST",
				data: {levelId:$(this).val()},
				dataType : "json",
				success: function(data){
				if(data.partyLevel){
				 var partyType=data.partyLevel.partyType;
				 if(partyType&&partyType=='MEMBER'){
				 special.parent().next().html('个人会员');
				 }else{
				 special.parent().next().html('企业会员');
				 }
				}
				}
			});
		  }
	    });
	    
	   $(".js-table_special").on("change",".specialType",function(){
	     var  special=$(this)
                   levelId=$(this).val();
              if(levelId!='-1'){
	      $.ajax({
				url: "getLevelType",
				type: "POST",
				data: {levelId:$(this).val()},
				dataType : "json",
				success: function(data){
				if(data.partyLevel){
				 var partyType=data.partyLevel.partyType;
				 if(partyType&&partyType=='MEMBER'){
				 special.parent().next().html('个人会员');
				 }else{
				 special.parent().next().html('企业会员');
				 }
				}
				}
			});
			}
	    });
	    
	 // 返回按钮的操作
     $("#btnReturn").click(function(){
         window.history.go(-1);
     })
	    
</script>