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
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/zzz.css</@ofbizContentUrl>" type="text/css"/>

<div class="box box-info">
    <div class="box-body">
        <input type="hidden" id="ids"/>
        <input type="hidden" name="curProductCategoryIdLevel1" id="curProductCategoryIdLevel1" value=""/>
	    <input type="hidden" name="curProductCategoryIdLevel2" id="curProductCategoryIdLevel2" value=""/>
	    <input type="hidden" name="curProductCategoryIdLevel3" id="curProductCategoryIdLevel3" value=""/>
	    
	    <input type="hidden" name="curProductCategoryNameLevel1" id="curProductCategoryNameLevel1" value=""/>
	    <input type="hidden" name="curProductCategoryNameLevel2" id="curProductCategoryNameLevel2" value=""/>
	    <input type="hidden" name="curProductCategoryNameLevel3" id="curProductCategoryNameLevel3" value=""/>
        <input type="hidden" id="isInner" name="isInner" value="${requestAttributes.isInner}"/>
        <!--店铺编码-->
        <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
        <p class="classification">添加商品</p>
        
        <form class="form-inline clearfix" role="form" action="">
            <#assign producttypeList = delegator.findByAnd("ProductType")>
           
        	<div class="form-group">
              <div class="input-group m-b-10">
                <span class="input-group-addon"><i class="required-mark">*</i>商品类型</span>
                <select class="form-control" id="productTypeId">
                <#list producttypeList as producttype >
                    <#if  requestAttributes.isInner =="Y">
                         <#if producttype.productTypeId=="FINISHED_GOOD" || producttype.productTypeId=="VIRTUAL_GOOD">
                             <option value="${producttype.productTypeId}">${producttype.get("description",locale)}</option>
                         </#if>
                    <#else>
                        <#if producttype.productTypeId=="FINISHED_GOOD">
                             <option value="${producttype.productTypeId}">${producttype.get("description",locale)}</option>
                        </#if>
                    </#if>
                </#list>
	            </select>
              </div>
            </div>
        </form>
        <div class="xl_main_classfy clearfix">
            <div class="xl_item">
                <div class="xl_first_search">
                <input type="text" name="first_classfy" class="xl_first first_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon1 search_icon1_1"></i>
                </div>
                <ul class="xl_ul1 xl_first_ul" style="top:35px;">
                    <li data-id="1">
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                        </span>

                    </li>
                </ui>   
            </div>
            <div class="xl_item">
                <div class="xl_first_search">
                    <input type="text" name="first_classfy" class="xl_first second_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon2 search_icon2_1"></i>
                </div>
                <ul class="xl_ul1 xl_second_ul" style="top:35px;">
                    <li data-id="1">
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                        </span>
                    </li>
                </ul>
            </div>
            <div class="xl_item">
                <div class="xl_first_search">
                    <input type="text" name="first_classfy" class="xl_first third_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon3 search_icon3_1"></i>
                </div>
                <ul class="xl_ul1 xl_third_ul" style="top:35px;">
                    <li data-id="1">
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                        </span>

                    </li>
                </ul>
            </div>
        </div>
        
         <div class="form-group">
              <div class="input-group m-b-5">
                  <span class="js-productCount">当前选择的是：</span>
              </div>
         </div>
            
         <div class="col-sm-offset-5 col-sm-10">
             <#if security.hasEntityPermission("PRODUCTGOODMGR_CREATE", "_VIEW",session)>
	         <button type="button" class="btn btn-primary" id="btnEditProductGood">选择分类并进入下一步</button>
	         </#if>
	     </div>
        
       
      </div>
       
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
 
 
 
<script language="JavaScript" type="text/javascript">
    $(function(){
         getInitProductCategoryByLevel();

        //选中li的时候显示那两个按钮
        $(".xl_ul1").on("click","li",function(e){
            if($(e.target).is("a")){
                return;
            }else{
                $(this).find(".xl_del_btn").show();
                $(this).addClass("xl_active").siblings().removeClass("xl_active");
                $(this).siblings().find(".xl_del_btn").hide();
            }
        });
        //选中一级分类的时候向后台发送请求
        $(".xl_first_ul").on("click","li",function(e){
            if($(e.target).is("a")){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                seclectdeItemProductCategoryLevelById("1",productCategoryId);
                
                $('.xl_first_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");   
                $("#curProductCategoryIdLevel1").val(productCategoryId);
                $("#curProductCategoryNameLevel1").val(productCategoryName);
            }
        });
        //选中二级分类的时候向后台发送请求
        $(".xl_second_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                seclectdeItemProductCategoryLevelById("2",productCategoryId);
                
                $('.xl_second_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");        
                $("#curProductCategoryIdLevel2").val(productCategoryId);
                $("#curProductCategoryNameLevel2").val(productCategoryName);
            }
        });
        
        //选中三级分类的时候向后台发送请求
        $(".xl_third_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                var productCategoryName=$(this).text();
                $('.xl_third_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");      
                $("#curProductCategoryIdLevel3").val(productCategoryId);
                $("#curProductCategoryNameLevel3").val(productCategoryName);
                var curProductCategoryId=$("#curProductCategoryIdLevel3").val();
                if($("#curProductCategoryIdLevel3").val()!=""){
	               getProductCountById($("#curProductCategoryIdLevel3").val());
	            }else{
	               $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
	            }
            }
        });
        
        //一级分类搜索的时候向后台发送请求
        $(".search_icon1").on("click",function(){
            var categoryName=$(".first_search").val();
            searchProductCategoryLevelByName("1","",categoryName);
        });
        //二级分类搜索的时候向后台发送请求
        $(".search_icon2").on("click",function(){
           var categoryName=$(".second_search").val();
           var parentCategoryId=$("#curProductCategoryIdLevel1").val();
           searchProductCategoryLevelByName("2",parentCategoryId,categoryName);
        });
        //三级分类搜索的时候向后台发送请求
        $(".search_icon3").on("click",function(){
            var categoryName=$(".third_search").val();
            var parentCategoryId=$("#curProductCategoryIdLevel2").val();
            searchProductCategoryLevelByName("3",parentCategoryId,categoryName);
        });
       //添加level3的产品分类
       $("#addlevel3").click(function(){
           var productCategoryId=$("#curProductCategoryIdLevel2").val();
           document.location.href="<@ofbizUrl>CreateProductCategory?operateType=create&productCategoryLevel=3&productCategoryId="+productCategoryId+"</@ofbizUrl>";
           return false;
       })
       
       $("#btnEditProductGood").click(function(){
           var categoryInfo= $("#curProductCategoryIdLevel3").val();
           if(categoryInfo==""){
              $('#modal_msg #modal_msg_body').html("只能在第三级分类下新建商品!");
			  $('#modal_msg').modal();
           }else{
              document.location.href="<@ofbizUrl>editProductGood?productCategoryId="+$("#curProductCategoryIdLevel3").val()+"&productTypeId="+$("#productTypeId").val()+"&operateType=create&gobackPage=createPage</@ofbizUrl>";
           }
           
       })
       
    })
    
    
     // 商品分类初始化
    function getInitProductCategoryByLevel(){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
	    jQuery.ajax({
	        url: '<@ofbizUrl>getInitProductCategoryByLevel</@ofbizUrl>',
	        type: 'POST',
            data:{
                'productStoreId' : productStoreId,
                'isInner':isInner
            },
	        success: function(data) {
	           var productCategoryLevel1List= data.productCategoryLevel1List;
	           var productCategoryLevel2List= data.productCategoryLevel2List;
	           var productCategoryLevel3List= data.productCategoryLevel3List;
	           var productCategoryLevel1Info= data.productCategoryLevel1Info;
	           var productCategoryLevel2Info= data.productCategoryLevel2Info;
	           var productCategoryLevel3Info= data.productCategoryLevel3Info;
	           
	           $(".xl_first_ul").empty();
			   for (var i=0;i<productCategoryLevel1List.length;i++){
		            var productCategoryId =(productCategoryLevel1List[i].productCategoryId);
		            var categoryName =(productCategoryLevel1List[i].categoryName);
			        var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>"
			        $(".xl_first_ul").append(liHtml);
			        
		       }
		       $(".xl_second_ul").empty();
		       for (var i=0;i<productCategoryLevel2List.length;i++){
		            var productCategoryId =(productCategoryLevel2List[i].productCategoryId);
		            var categoryName =(productCategoryLevel2List[i].categoryName);
		            
		            var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>";
			        $(".xl_second_ul").append(liHtml);
		       }
		       $(".xl_third_ul").empty();
		       for (var i=0;i<productCategoryLevel3List.length;i++){
		            var productCategoryId =(productCategoryLevel3List[i].productCategoryId);
		            var categoryName =(productCategoryLevel3List[i].categoryName);
		            
		            var liHtml="<li data-id="+productCategoryId+">"+categoryName+"</span>"+"</li>";
			        $(".xl_third_ul").append(liHtml);
		       }
		       
		       // 设置选择项目
		       //根据a返回它的data-id
                if(productCategoryLevel1Info) {
                    $('.xl_first_ul li[data-id=' + productCategoryLevel1Info.productCategoryId + ']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                }
                if(productCategoryLevel2Info){
                $('.xl_second_ul li[data-id='+productCategoryLevel2Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
               }
               if(productCategoryLevel3Info){
               	  $('.xl_third_ul li[data-id='+productCategoryLevel3Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
               }
               
		       $("#curProductCategoryIdLevel1").val(productCategoryLevel1Info.productCategoryId);
		       if(productCategoryLevel2Info){
		       $("#curProductCategoryIdLevel2").val(productCategoryLevel2Info.productCategoryId);
		       }
		       if(productCategoryLevel3Info){
		       	  $("#curProductCategoryIdLevel3").val(productCategoryLevel3Info.productCategoryId);
		       }
		       
		       $("#curProductCategoryNameLevel1").val(productCategoryLevel1Info.categoryName);
		       if(productCategoryLevel2Info){
		       $("#curProductCategoryNameLevel2").val(productCategoryLevel2Info.categoryName);
		       }
		       if(productCategoryLevel3Info){
		       $("#curProductCategoryNameLevel3").val(productCategoryLevel3Info.categoryName);
		       }
		       if($("#curProductCategoryNameLevel3").val()!=""){
		          getProductCountById($("#curProductCategoryIdLevel3").val());
		       }else{
		          $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
		       }
	        }
	    });
	}
	
	
	
	//分类级别的查找功能
	function searchProductCategoryLevelByName(productCategoryLevel,productCategoryId,categoryName){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
	    $.ajax({
            url:'<@ofbizUrl>searchProductCategoryLevelByName</@ofbizUrl>',
            dataType:'json',
            type:'post',
            beforeSend:function(){
               if(productCategoryLevel=="1"){
                 $('.search_icon1_1').removeClass('search_icon1');
               }else if(productCategoryLevel=="2"){
                 $('.search_icon2_1').removeClass('search_icon2');
               
               }else if(productCategoryLevel=="3"){
                 $('.search_icon3_1').removeClass('search_icon3');
               }
            },
            data:{
                 'productCategoryLevel' : productCategoryLevel,
	             'productCategoryId':productCategoryId,
	             'categoryName':categoryName,
                 'productStoreId' : productStoreId,
                 'isInner':isInner
            },
            success:function(data){
                if(data.success){
                    //根据a返回它的data-id
                    var productCategoryLevel1List= data.productCategoryLevel1List;
		            var productCategoryLevel2List= data.productCategoryLevel2List;
		            var productCategoryLevel3List= data.productCategoryLevel3List;
		            var productCategoryLevel1Info= data.productCategoryLevel1Info;
		            var productCategoryLevel2Info= data.productCategoryLevel2Info;
		            var productCategoryLevel3Info= data.productCategoryLevel3Info;
		            
		            var productCategoryIdLevel1 ="";
		            var productCategoryIdLevel2 ="";
		            var productCategoryIdLevel3 ="";
		            
		            var productCategoryNameLevel1 ="";
		            var productCategoryNameLevel2 ="";
		            var productCategoryNameLevel3 ="";
		            if(productCategoryLevel1Info){
		               productCategoryIdLevel1 =productCategoryLevel1Info.productCategoryId;
		               productCategoryNameLevel1 =productCategoryLevel1Info.categoryName;
		            }
		            if(productCategoryLevel2Info){
		               productCategoryIdLevel2 =productCategoryLevel2Info.productCategoryId;
		               productCategoryNameLevel2 =productCategoryLevel2Info.categoryName;
		            }
		            if(productCategoryLevel3Info){
		               productCategoryIdLevel3 =productCategoryLevel3Info.productCategoryId;
		               productCategoryNameLevel3 =productCategoryLevel3Info.categoryName;
		            }
		            
		            
		            var level=data.productCategoryLevel;
		           
		            if(level=='1'){
		                //把返回的数据放到二级菜单
                        var b="";
                        for(var i=0;i<productCategoryLevel2List.length;i++){
                            b+='<li data-id="'+productCategoryLevel2List[i].productCategoryId+'">'+productCategoryLevel2List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != null){
                           $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#curProductCategoryNameLevel2").val(productCategoryNameLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        
                        if(productCategoryIdLevel1 != ""){
                            $('.xl_first_ul li[data-id='+productCategoryIdLevel1+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel1").val(productCategoryIdLevel1);
                        $("#curProductCategoryNameLevel1").val(productCategoryNameLevel1);
                        
				        $('.search_icon1_1').addClass('search_icon1');
				        if(productCategoryNameLevel3!=""){
                           getProductCountById($("#curProductCategoryIdLevel3").val());
                        }else{
                           $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
                        }
			        }else if(level=='2'){
			            //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        
                        
                        if(productCategoryIdLevel2 != ""){
                           $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#curProductCategoryNameLevel2").val(productCategoryNameLevel2);
                        
			            $('.search_icon2_1').addClass('search_icon2');
			            if(productCategoryNameLevel3!=""){
                           getProductCountById($("#curProductCategoryIdLevel3").val());
                        }else{
                           $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
                        }
			        }else if(level='3'){
			            if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryNameLevel3!=""){
                           getProductCountById($("#curProductCategoryIdLevel3").val());
                        }else{
                           $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
                        }
			        }

                }else{
                    alert("操作失败！");
                }
            },
            error:function(){
                alert("操作失败！");
                  $('.search_icon2_1').addClass('search_icon2');
            }
        })
	}
	
	
	
	//分类级别的选中处理功能
	function seclectdeItemProductCategoryLevelById(productCategoryLevel,productCategoryId){
        // 店铺信息
        var productStoreId=$("#productStoreId").length==0?'':$('#productStoreId').val();
        // 自营与非自营标记
        var isInner =$('#isInner').length==0?'':$('#isInner').val();
	    $.ajax({
            url:'<@ofbizUrl>seclectdeItemProductCategoryLevelById</@ofbizUrl>',
            dataType:'json',
            type:'post',
            data:{
             'productCategoryLevel' : productCategoryLevel,
             'productCategoryId':productCategoryId,
             'productStoreId' : productStoreId,
             'isInner':isInner
            },
            success:function(data){
                //把返回的数据放到二级菜单
                if(data.success){
                    //根据a返回它的data-id
                    var productCategoryLevel1List= data.productCategoryLevel1List;
		            var productCategoryLevel2List= data.productCategoryLevel2List;
		            var productCategoryLevel3List= data.productCategoryLevel3List;
		            var productCategoryLevel1Info= data.productCategoryLevel1Info;
		            var productCategoryLevel2Info= data.productCategoryLevel2Info;
		            var productCategoryLevel3Info= data.productCategoryLevel3Info;
		            
		            
		            var productCategoryIdLevel1 ="";
		            var productCategoryIdLevel2 ="";
		            var productCategoryIdLevel3 ="";
		            
		            var productCategoryNameLevel1 ="";
		            var productCategoryNameLevel2 ="";
		            var productCategoryNameLevel3 ="";
		            if(productCategoryLevel1Info){
		               productCategoryIdLevel1 =productCategoryLevel1Info.productCategoryId;
		               productCategoryNameLevel1 =productCategoryLevel1Info.categoryName;
		            }
		            if(productCategoryLevel2Info){
		               productCategoryIdLevel2 =productCategoryLevel2Info.productCategoryId;
		               productCategoryNameLevel2 =productCategoryLevel2Info.categoryName;
		            }
		            if(productCategoryLevel3Info){
		               productCategoryIdLevel3 =productCategoryLevel3Info.productCategoryId;
		               productCategoryNameLevel3 =productCategoryLevel3Info.categoryName;
		            }
		            
		            var level=data.productCategoryLevel;
		            if(level=='1'){
                        //把返回的数据放到二级菜单
                        var b="";
                        for(var i=0;i<productCategoryLevel2List.length;i++){
                            b+='<li data-id="'+productCategoryLevel2List[i].productCategoryId+'">'+productCategoryLevel2List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != ""){
                           $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        $("#curProductCategoryNameLevel2").val(productCategoryNameLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        //alert(c);
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                            $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryNameLevel3!=""){
                           getProductCountById($("#curProductCategoryIdLevel3").val());
                        }else{
                           $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
                        }
                    }else if(level=='2'){
                       //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        $("#curProductCategoryNameLevel3").val(productCategoryNameLevel3);
                        if(productCategoryIdLevel3 !=""){
                            getProductCountById($("#curProductCategoryIdLevel3").val());
                        }else{
                            $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val());
                        }
                    }else if(level=='3'){
                         
                    }
                }else{
                    alert("操作失败")
                }
            },
            error:function(){
                alert("操作失败")
           }
        })
	}
	
   // 取得分类商品数量信息
   function getProductCountById(curProductCategoryId){
	  if (curProductCategoryId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductCountById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'curProductCategoryId' : curProductCategoryId
	        },
	        success: function(data) {
	           //alert(data.listSize);
	           var listSize=data.listSize;
	           $(".js-productCount").text("当前选择的是："+ $("#curProductCategoryNameLevel1").val()+">"+ $("#curProductCategoryNameLevel2").val()+">"+ $("#curProductCategoryNameLevel3").val()+"【"+listSize+"件商品】");
	        }
	    });
	  } 
   } 	
</script>
		
