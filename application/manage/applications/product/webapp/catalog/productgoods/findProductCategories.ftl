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

        <input type="hidden" id="isInner" name="isInner" value="${requestAttributes.isInner}"/>
        <!--店铺编码-->
        <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
        <p class="classification">分类配置</p>
        <div class="xl_main_classfy clearfix">
            <div class="xl_item">
                <h4 class="xl_p1">
                 <#if security.hasEntityPermission("PRODUCTGOODMGR_TYPE", "_CREATE",session)>
                <a href="<@ofbizUrl>CreateProductCategory?operateType=create&productCategoryLevel=1</@ofbizUrl>">${uiLabelMap.AddLevelOne}</a>
                </#if>
                </h4>
                <div class="xl_first_search">
                <input type="text" name="first_classfy" class="xl_first first_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon1 search_icon1_1"></i>
                </div>
                <ul class="xl_ul1 xl_first_ul">
                    <li data-id="1">
                        <#---
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                            <a href="javascript:;"  class=" btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>
                            <a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>
                        </span>
                        -->

                    </li>
                </ui>   
            </div>
            <div class="xl_item">
                <h4 class="xl_p1"> 
                <#if security.hasEntityPermission("PRODUCTGOODMGR_TYPE", "_CREATE",session)>
                <a  id="addlevel2" href="<@ofbizUrl>CreateProductCategory?operateType=create&productCategoryLevel=2</@ofbizUrl>">${uiLabelMap.AddLevelTwo}</a>
                </#if>
                </h4>
                <div class="xl_first_search">
                    <input type="text" name="first_classfy" class="xl_first second_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon2 search_icon2_1"></i>
                </div>
                <ul class="xl_ul1 xl_second_ul">
                    <li data-id="1">
                        <#--
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                            <a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>
                            <a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>
                        </span>
                        -->
                    </li>
                </ul>
            </div>
            <div class="xl_item">
                <h4 class="xl_p1">
                <#if security.hasEntityPermission("PRODUCTGOODMGR_TYPE", "_CREATE",session)>
                <a id="addlevel3" href="">${uiLabelMap.AddLevelThree}</a>
                </#if>
                </h4>
                <div class="xl_first_search">
                    <input type="text" name="first_classfy" class="xl_first third_search" placeholder="输入名称查找">
                    <i class="glyphicon glyphicon-search xl_search_icon search_icon3 search_icon3_1"></i>
                </div>
                <ul class="xl_ul1 xl_third_ul">
                    <li data-id="1">
                        <#--
                        <span class="product_name"></span>
                        <span class="xl_del_btn">
                            <a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>
                            <a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>
                        </span>
                        -->

                    </li>
                </ul>
            </div>
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
	
	
	<!-- ${uiLabelMap.BrandDel}确认弹出框start -->
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
	</div><!-- ${uiLabelMap.BrandDel}确认弹出框end -->
 
 
<script language="JavaScript" type="text/javascript">
    $(function(){
         getInitProductCategoryByLevel();
         
         
		 //删除按钮事件
		 $(document).on('click','.xl_first_ul .xl_delte',function(){
			 var productCategoryId=$(this).parent().parent().attr("data-id");
			 $("#ids").val(productCategoryId);
			 
			 delProductCategoryByIdForCheck("1");
	        // if (productCategoryId != ""){
	           	//设置删除弹出框内容
				//$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				//$('#modal_confirm').modal('show');
	         //}
		  })
		 
		  $(document).on('click','.xl_second_ul .xl_delte',function(){
		     var productCategoryId=$(this).parent().parent().attr("data-id");
		     $("#ids").val(productCategoryId);
		     delProductCategoryByIdForCheck("2");
	         //if (productCategoryId != ""){
	           	//设置删除弹出框内容
				//$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				//$('#modal_confirm').modal('show');
	         //}
		 })
		 
		  $(document).on('click','.xl_third_ul .xl_delte',function(){
		     var productCategoryId=$(this).parent().parent().attr("data-id");
		     $("#ids").val(productCategoryId);
		     delProductCategoryByIdForCheck("3");
	         //if (productCategoryId != ""){
	           	//设置删除弹出框内容
				//$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				//$('#modal_confirm').modal('show');
	         //}
		 })
		 
		 
		 //编辑按钮事件
		 $(document).on('click','.xl_first_ul .xl_edit',function(){
		    var productCategoryIdForUpdate=$(this).parent().parent().attr("data-id");
		    var productCategoryId="";
			document.location.href="<@ofbizUrl>CreateProductCategory?operateType=update&productCategoryLevel=1&productCategoryId="+productCategoryId+"&productCategoryIdForUpdate="+productCategoryIdForUpdate+"</@ofbizUrl>";
		 })
		 
		 $(document).on('click','.xl_second_ul .xl_edit',function(){
		    var productCategoryIdForUpdate=$(this).parent().parent().attr("data-id");
		    var productCategoryId=$("#curProductCategoryIdLevel1").val();
			document.location.href="<@ofbizUrl>CreateProductCategory?operateType=update&productCategoryLevel=2&productCategoryId="+productCategoryId+"&productCategoryIdForUpdate="+productCategoryIdForUpdate+"</@ofbizUrl>";
		 })
		 
		 $(document).on('click','.xl_third_ul .xl_edit',function(){
		    var productCategoryIdForUpdate=$(this).parent().parent().attr("data-id");
		    var productCategoryId=$("#curProductCategoryIdLevel2").val();
			document.location.href="<@ofbizUrl>CreateProductCategory?operateType=update&productCategoryLevel=3&productCategoryId="+productCategoryId+"&productCategoryIdForUpdate="+productCategoryIdForUpdate+"</@ofbizUrl>";
		 })

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
                seclectdeItemProductCategoryLevelById("1",productCategoryId);
                
                $('.xl_first_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");   
                $("#curProductCategoryIdLevel1").val(productCategoryId);
            }
        });
        //选中二级分类的时候向后台发送请求
        $(".xl_second_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                seclectdeItemProductCategoryLevelById("2",productCategoryId);
                
                $('.xl_second_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");        
                $("#curProductCategoryIdLevel2").val(productCategoryId);
            }
        });
        
        //选中三级分类的时候向后台发送请求
        $(".xl_second_ul").on("click","li",function(e){
            if($(e.target)=='a'){
                return;
            }else{
                var productCategoryId=$(this).attr("data-id");
                $('.xl_third_ul li[data-id='+productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");      
                $("#curProductCategoryIdLevel3").val(productCategoryId);
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
       
	   //删除弹出框删除按钮点击事件
	   $('#modal_confirm #ok').click(function(e){
	       delProductCategoryDataById();
	   });
	   // 添加level2的产品分类
	   $("#addlevel2").click(function(){
	      var productCategoryId=$("#curProductCategoryIdLevel1").val();
	      document.location.href="<@ofbizUrl>CreateProductCategory?operateType=create&productCategoryLevel=2&productCategoryId="+productCategoryId+"</@ofbizUrl>";
	      return false;
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
			        var liHtml="<li data-id="+productCategoryId+">"+categoryName+"<span class='xl_del_btn'>"+"<a href='javascript:;' class='btn btn-default btn-xs xl_edit'>${uiLabelMap.BrandEdit}</a>" +"<a href='javascript:;' class='btn btn-default btn-xs xl_delte'>${uiLabelMap.BrandDel}</a>"+"</span>"+"</li>"
			        $(".xl_first_ul").append(liHtml);
			        
		       }
		       $(".xl_second_ul").empty();
		       for (var i=0;i<productCategoryLevel2List.length;i++){
		            var productCategoryId =(productCategoryLevel2List[i].productCategoryId);
		            var categoryName =(productCategoryLevel2List[i].categoryName);
		            
		            var liHtml="<li data-id="+productCategoryId+">"+categoryName+"<span class='xl_del_btn'>"+"<a href='javascript:;' class='btn btn-default btn-xs xl_edit'>${uiLabelMap.BrandEdit}</a>" +"<a href='javascript:;' class='btn btn-default btn-xs xl_delte'>${uiLabelMap.BrandDel}</a>"+"</span>"+"</li>";
			        $(".xl_second_ul").append(liHtml);
		       }
		       $(".xl_third_ul").empty();
		       for (var i=0;i<productCategoryLevel3List.length;i++){
		            var productCategoryId =(productCategoryLevel3List[i].productCategoryId);
		            var categoryName =(productCategoryLevel3List[i].categoryName);
		            
		            var liHtml="<li data-id="+productCategoryId+">"+categoryName+"<span class='xl_del_btn'>"+"<a href='javascript:;' class='btn btn-default btn-xs xl_edit'>${uiLabelMap.BrandEdit}</a>" +"<a href='javascript:;' class='btn btn-default btn-xs xl_delte'>${uiLabelMap.BrandDel}</a>"+"</span>"+"</li>";
			        $(".xl_third_ul").append(liHtml);
		       }
		       
		       // 设置选择项目
		       //根据a返回它的data-id
		       if(productCategoryLevel1Info){
		           $('.xl_first_ul li[data-id='+productCategoryLevel1Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
		           $("#curProductCategoryIdLevel1").val(productCategoryLevel1Info.productCategoryId);
		       }
               
               if(productCategoryLevel2Info){
               	   $('.xl_second_ul li[data-id='+productCategoryLevel2Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
               	   $("#curProductCategoryIdLevel2").val(productCategoryLevel2Info.productCategoryId);
               }
               if(productCategoryLevel3Info){
                   $("#curProductCategoryIdLevel3").val(productCategoryLevel3Info.productCategoryId);
                   $('.xl_third_ul li[data-id='+productCategoryLevel3Info.productCategoryId+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
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
		            
		            //var productCategoryIdLevel1 =data.productCategoryIdLevel1;
		            //var productCategoryIdLevel2 =data.productCategoryIdLevel2;
		            //var productCategoryIdLevel3 =data.productCategoryIdLevel3;
		            
		            
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
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != ""){
                           $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        
                        
                        if(productCategoryIdLevel1 != ""){
                            $('.xl_first_ul li[data-id='+productCategoryIdLevel1+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel1").val(productCategoryIdLevel1);
                        
				        $('.search_icon1_1').addClass('search_icon1');
			        }else if(level=='2'){
			            //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                        
                        if(productCategoryIdLevel2 != ""){
                           $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        
			            $('.search_icon2_1').addClass('search_icon2');
			        }else if(level='3'){
			            if(productCategoryIdLevel3 != ""){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").find(".xl_del_btn").show().parent().siblings().removeClass("xl_active").find(".xl_del_btn").hide();
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
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
		            
		            var productCategoryIdLevel1 =data.productCategoryIdLevel1;
		            var productCategoryIdLevel2 =data.productCategoryIdLevel2;
		            var productCategoryIdLevel3 =data.productCategoryIdLevel3;
		            
		            var level=data.productCategoryLevel;
		            if(level=='1'){
                        //把返回的数据放到二级菜单
                        var b="";
                        for(var i=0;i<productCategoryLevel2List.length;i++){
                            b+='<li data-id="'+productCategoryLevel2List[i].productCategoryId+'">'+productCategoryLevel2List[i].categoryName+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_second_ul").html(b);
                        if(productCategoryIdLevel2 != null){
                           $('.xl_second_ul li[data-id='+productCategoryIdLevel2+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel2").val(productCategoryIdLevel2);
                        //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != null){
                            $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                    }else if(level=='2'){
                       //把返回的数据放到三级菜单
                        var c="";
                        for(var i=0;i<productCategoryLevel3List.length;i++){
                            c+='<li data-id="'+productCategoryLevel3List[i].productCategoryId+'">'+productCategoryLevel3List[i].categoryName+' <span class="xl_del_btn">'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_edit">${uiLabelMap.BrandEdit}</a>'
                                    +'<a href="javascript:;" class="btn btn-default btn-xs xl_delte">${uiLabelMap.BrandDel}</a>'
                                    +'</span>'
                                    +'</li>'
                        }
                        $(".xl_third_ul").html(c);
                        if(productCategoryIdLevel3 != null){
                           $('.xl_third_ul li[data-id='+productCategoryIdLevel3+']').addClass("xl_active").find(".xl_del_btn").show().siblings().removeClass("xl_active");
                        }
                        $("#curProductCategoryIdLevel3").val(productCategoryIdLevel3);
                       
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
	
	// 将选择的记录id删除
    function delProductCategoryDataById(){
	  // 选中的项目
	  var productCategoryId =$("#ids").val(); 
	  if (productCategoryId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>delProductCategoryById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productCategoryId' : productCategoryId
	        },
	        success: function(data) {
	           document.location.href="<@ofbizUrl>ProductBrandType?lookupFlag=Y</@ofbizUrl>"; 
	        }
	    });
	  } 
	}
	
	// 将选择的记录删除的检查
    function delProductCategoryByIdForCheck(level){
	  // 选中的项目
	  var productCategoryId =$("#ids").val(); 
	  if (productCategoryId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>delProductCategoryByIdForCheck</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productCategoryId' : productCategoryId,
	             'productCategoryLevel':level
	        },
	        success: function(data) {
	           var checkFlg= data.checkFlg;
	           var errType= data.errType;
	           if(checkFlg=='N'){
	               if(data.errType=="Category"){
	                  //alert("aa");
	                  //设置提示弹出框内容
					  $('#modal_msg #modal_msg_body').html("该分类有下级分类，无法删除");
    				  $('#modal_msg').modal();
	               }else if(data.errType=="Product"){
	                  //alert("bb");
	                  //设置提示弹出框内容
					  $('#modal_msg #modal_msg_body').html("该分类有商品，无法删除");
    				  $('#modal_msg').modal();
	               }
	           }else{
	              //设置删除弹出框内容
				  $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				  $('#modal_confirm').modal('show');
	           }
	        }
	    });
	  } 
	}  
	
</script>
		
