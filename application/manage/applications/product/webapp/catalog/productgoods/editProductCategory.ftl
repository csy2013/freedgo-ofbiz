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
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title"><#if parameters.operateType=="update">编辑分类<#else>新增分类</#if></h3>
    </div>
    <div class="box-body">
        <form class="form-horizontal" method="post" role="form" action="" name="updateProductCategory" id="updateProductCategory" class="" enctype="multipart/form-data">
		   
		    <#if parameters.operateType?if_exists =='update' >
                <#if pc?has_content>
		          <div class="form-group" data-type="required" data-mark="分类名称">
                      <label for="firstname" class="col-sm-2 control-label"><i class="s_req">*</i>${uiLabelMap.ProductCategoryName}：</label>
                      <div class="col-sm-3">
                          <input type="text" class="form-control dp-vd" id="categoryName" name="categoryName" value="${pc.categoryName?if_exists}" placeholder="">
                          <p class="dp-error-msg"></p>
                      </div>
                  </div>
				   <div class="form-group">
                       <label for="lastname" class="col-sm-2 control-label"><i class="s_req">*</i>${uiLabelMap.ProductParentCategory}：</label>
                       <div class="col-sm-3">
                           <select class="form-control" name="primaryParentCategoryId" id="primaryParentCategoryId">
                               <option></option>
                           </select>
                       </div>
                   </div>
				   <div class="form-group" data-type="required,format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/" data-mark="分类排序">
                       <label for="lastname" class="col-sm-2 control-label"><i class="s_req">*</i>${uiLabelMap.CategorySequenceNum}：</label>
                       <div class="col-sm-3">
                           <input type="text" class="form-control dp-vd" id="sequenceNum" name="sequenceNum" value="${pc.sequenceNum?if_exists}" placeholder="">
                           <p class="dp-error-msg"></p>
                       </div>
                   </div>
				   
				   
				    <div class="form-group">
                        <label for="lastname" class="col-sm-2 control-label">分类图片：</label>
                        <div class="col-sm-3">

                            <input type="hidden" name="contentId" id="contentId" value="${pc.contentId?if_exists}"/>
                            <button type="button" class="btn btn-primary" id="btnAddPic" onclick="imageManage()">${uiLabelMap.AddPic}</button>
                        </div>
                        <div class="col-sm-7">
                            <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 100*100</div>
                        </div>
                    </div>
				   
				  <div class="form-group">
                      <div class="col-sm-2">
                      </div>
	                 <#if pc.contentId?has_content>
                         <#assign src='/content/control/stream?contentId='>
                         <#assign imgsrc = src +pc.contentId/>
	                 <div class="col-sm-3">
                         <img height="50" alt="" src="${imgsrc}" id="img" style="height:100px;width:100px;">
                     </div>
                     <#else>
		                 <div class="col-sm-3">
                             <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                         </div>
                     </#if>
                  </div>
                </#if>
            <#else>
		       <div class="form-group" data-type="required" data-mark="分类名称">
                   <label for="firstname" class="col-sm-2 control-label"><i class="s_req">*</i>${uiLabelMap.ProductCategoryName}：</label>
                   <div class="col-sm-3">
                       <input type="text" class="form-control dp-vd" id="categoryName" name="categoryName" value="${parameters.categoryName?if_exists}" placeholder="">
                       <p class="dp-error-msg"></p>
                   </div>
               </div>
			   <div class="form-group">
                   <label for="lastname" class="col-sm-2 control-label"><i class="s_req">*</i>${uiLabelMap.ProductParentCategory}：</label>
                   <div class="col-sm-3">
                       <select class="form-control" name="primaryParentCategoryId" id="primaryParentCategoryId">
                           <option></option>

                       </select>
                   </div>
               </div>
			   <div class="form-group" data-type="required,format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/" data-mark="分类排序">
                   <label for="lastname" class="col-sm-2 control-label"><i class="s_req">*</i>${uiLabelMap.CategorySequenceNum}：</label>
                   <div class="col-sm-3">
                       <input type="text" class="form-control dp-vd" id="sequenceNum" name="sequenceNum" value="${parameters.sequenceNum?if_exists}" placeholder="">
                       <p class="dp-error-msg"></p>
                   </div>
               </div>
			   
			   
			   <div class="form-group">
                   <label for="lastname" class="col-sm-2 control-label">分类图片：</label>
                   <div class="col-sm-3">

                       <input type="hidden" name="contentId" id="contentId" value=""/>
                       <button type="button" class="btn btn-primary" id="btnAddPic" onclick="imageManage()">${uiLabelMap.AddPic}</button>
                   </div>
                   <div class="col-sm-7">
                       <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 100*100px</div>
                   </div>
               </div>
			   
			  <div class="form-group">
                  <div class="col-sm-2">
                  </div>
                  <div class="col-sm-3">
                      <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                  </div>
              </div>


            </#if>
            <div class="form-group js-isHasExtendAttr">
                <div class="col-sm-offset-1 col-sm-10">
                    <div class="checkbox js-checkbox">
                        <label>
                            <input type="hidden" value="0" name="valx">
                            <input type="radio" name="isHasExtendAttr" value="N">${uiLabelMap.IsHasExtendAttrN}
                        </label>
                        <label>
                            <input type="radio" name="isHasExtendAttr" value="Y">${uiLabelMap.IsHasExtendAttrY}
                        </label>
                    </div>
                </div>
            </div>

            <div class="Dragigng_frame">
                <button class="btn btn-primary add_sx js-add" type="button" id="addExtendAttr">${uiLabelMap.AddExtendAttr}</button>
                <table class="table table-bordered table_b_c js-table_1">
                    <thead>
                    <tr>
                        <th style="width:20%"><i class="s_req">*</i>${uiLabelMap.ExtendAttrName}</th>
                        <th style="width:50%"><i class="s_req">*</i>${uiLabelMap.ExtendAttrOption}</th>
                        <th style="width:20%"><i class="s_req">*</i>${uiLabelMap.IsRequired}</th>
                        <th style="width:10%">${uiLabelMap.BrandOption}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#--
			        <tr>
			            <td><input type="text" name="" id="" value="价格"></td> <td><span class="sx_xx js-sx_xx"><input type="text" value="" name=""><i class="js-x_i">x</i></span></td> <td> <select class="form-control">
			            <option>必填</option>
			            <option selected>非必填</option>
			            </select></td> 
			            <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">删除</button></td>
			        </tr>
			        -->
                    </tbody>
                </table>
            </div>

            <div class="useCategoryFeature">
                <label class="control-label col-sm-2">关联特征:</label>
                <div class="col-sm-10" id="categoryFeaturesDiv">
                </div>
            </div>

            <input id="productId" type="hidden" name="productId" value="GoodsCategory"/>
            <input id="productContentTypeId" type="hidden" name="productContentTypeId" value="GOODSCATEGORY_IMG"/>

            <input type="hidden" name="productOptionId" id="productOptionId" value=""/>

            <input type="hidden" name="attrName" id="attrName" value=""/>
            <input type="hidden" name="attrProductCategoryId" id="attrProductCategoryId" value=""/>

            <input type="hidden" name="extendAttrInfos" id="extendAttrInfos" value=""/>
            <input type="hidden" name="extendOptionInfos" id="extendOptionInfos" value=""/>
            <input type="hidden" name="operateType" id="operateType" value="${parameters.operateType?if_exists}"/>
            <input type="hidden" name="productCategoryId" id="productCategoryId" value="${parameters.productCategoryIdForUpdate?if_exists}"/>
            <input type="hidden" name="productCategoryLevel" value="${parameters.productCategoryLevel?if_exists}"/>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" id="btnCancel">${uiLabelMap.BrandCancel}</button>
                <button type="button" class="btn btn-primary" id="btnCategorySave">${uiLabelMap.BrandSave}</button>
            </div>
        </form>
    </div><!-- /.box-body -->
</div>


<!-- 确认弹出框start -->
<div id="modal_confirm" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
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
</div><!--确认弹出框end -->

<!-- 确认弹出框start -->
<div id="modal_confirm_option" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
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
</div><!-- 确认弹出框end -->

    <!-- 提示弹出框start -->
	<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
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
    var isCommitted = false;//表单是否已经提交标识，默认为false
    var tempParent = ""; //传参用
    $(function () {
        var operateType = '${parameters.operateType?if_exists}';
        if (operateType == 'create') {
            $('input:radio[name="isHasExtendAttr"][value="N"]').prop('checked', true);
            $(".Dragigng_frame").hide();

        } else {
           <#if pc?has_content>
               <#assign productCategoryAttributes = delegator.findByAnd("ProductCategoryAttribute", {"productCategoryId" : pc.productCategoryId})>
               <#list productCategoryAttributes as productCategoryAttribute>
               var attrName = '${productCategoryAttribute.attrName?if_exists}';
               var isRequired = '${productCategoryAttribute.isRequired?if_exists}';
               var productCategoryId = '${productCategoryAttribute.productCategoryId?if_exists}';
                   <#assign productOptions = delegator.findByAnd("ProductOption", {"productCategoryId" : pc.productCategoryId,"attrName":productCategoryAttribute.attrName})>
               
               var tdAtrrName = '<td><input type="text" name="" id="" value="' + attrName + '"  class="form-control" readonly><input type="hidden" name="" id="productCategoryId" value="' + productCategoryId + '"><input type="hidden" id=curOptionType value="update"</td>';
               // 修改的场合OptionName的HTML需要加个class"js-x_i_new",最后还需加个空白的框
               var tdOption = '<td>';
                   <#list productOptions as productOption>
                  var optionName = '${productOption.optionName?if_exists}';
                  var productOptionId = '${productOption.productOptionId?if_exists}'
                  tdOption = tdOption + '<span class="sx_xx js-sx_xx"><input type="text" value="' + optionName + '" name=""><input type="hidden" name="" id="productOptionId" value="' + productOptionId + '"><input type="hidden" id="curOptionOptionType" value="update"><i class="js-x_i js-x_i_new">x</i></span>';
                   </#list>
               tdOption = tdOption + '<span class="sx_xx js-sx_xx"><input type="text" value="" name=""><input type="hidden" id="curOptionOptionType" value="create"><input type="hidden" name="" id="productOptionId" value=""><i class="js-x_i">x</i></span>' + '</td>';
               var tdIsRequired = "";
               if (isRequired == 'N') {
                   tdIsRequired = '<td><select class="form-control"><option value="Y" >${uiLabelMap.IsRequiredY}</option><option value="N" selected>${uiLabelMap.IsRequiredN}</option></select></td>';
               } else {
                   tdIsRequired = '<td><select class="form-control"><option value="Y" selected>${uiLabelMap.IsRequiredY}</option><option value="N">${uiLabelMap.IsRequiredN}</option></select></td>';
               }
               var tdDelBtn = '<td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td>';
               
               var tr = '<tr>' + tdAtrrName + tdOption + tdIsRequired + tdDelBtn + '</tr>';
               $('.js-table_1>tbody').append(tr);
               </#list>
           var temp = '${pc.isHasExtendAttr?if_exists}'
           if (temp == 'Y') {
               $('input:radio[name="isHasExtendAttr"][value="Y"]').prop('checked', true);
               $(".Dragigng_frame").show();
           } else {
               $('input:radio[name="isHasExtendAttr"][value="N"]').prop('checked', true);
               $(".Dragigng_frame").hide();
           }
           </#if>
        }
        var level = '${parameters.productCategoryLevel?if_exists}';
        var productCategoryId = '${parameters.productCategoryId?if_exists}'

        if (level == 1) {
            $("#primaryParentCategoryId").attr("disabled", "disabled");
            $('.js-isHasExtendAttr').hide();
        } else if (level == 2) {
            getProductCategoryByLevel('1', productCategoryId);
            $('.js-isHasExtendAttr').hide();
        } else if (level == 3) {
            getProductCategoryByLevel('2', productCategoryId);
        }

        // 取消处理
        $("#btnCancel").click(function () {
            document.location.href = "<@ofbizUrl>ProductCategory?lookupFlag=Y</@ofbizUrl>";
        })

        // 保存处理
        $("#btnCategorySave").click(function () {
            $('#updateProductCategory').dpValidate({
                clear: true
            });
            $('#updateProductCategory').submit();
        });

        $('#updateProductCategory').dpValidate({
            validate: true,
            callback: function () {
                var operate = '${parameters.operateType}';
                extendAttrInfos();
                //alert($("#extendAttrInfos").val());
                //alert($("#extendOptionInfos").val());

                if (dosubmit()) {
                    if (operate == "create") {
                        $("#productCategoryId").val("");
                    }
                    document.updateProductCategory.action = "<@ofbizUrl>updateProductCategoryIco</@ofbizUrl>";
                    document.updateProductCategory.submit();
                }
            }
        });

        //阻止enter键提交表单
        $(document).on('keypress', '.js-sx_xx input', function (e) {
            if (e.keyCode == 13) {
                $(this).blur();
                e.preventDefault();
            }
        })


        //checkbox事件
        $('.js-checkbox').on('click', 'input[type=radio]', function () {
            var value = $('.js-checkbox').find('input:checked').val();
            $('.js-checkbox').find('input[name=valx]').val(value);
            if (value == 'Y') {
                $('.Dragigng_frame').show();
            }
            else {
                $('.Dragigng_frame').hide();
            }

        })
        //添加扩展属性事件
        $(document).on('click', '.js-add', function () {
            var tr = ' <tr><td style="width:20%"><input type="text" name="" id="" value=""><input type="hidden" id=curOptionType value="create"><input type="hidden" id=productCategoryId value=""></td> <td style="width:50%"><span class="sx_xx js-sx_xx"><input type="text" value="" name=""><input type="hidden" id="curOptionOptionType" value="create"><input type="hidden" id="productOptionId" value=""><i class="js-x_i">x</i></span></td> <td style="width:20%"> <select class="form-control"><option value="Y">${uiLabelMap.IsRequiredY}</option><option value="N" selected>${uiLabelMap.IsRequiredN}</option></select></td> <td class="fc_td" style="width:10%"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
            $('.js-table_1>tbody').append(tr);
        })


        //删除按钮事件
        $(document).on('click', '.js-button', function () {
            var productCategoryId = $(this).parent().parent().find("#productCategoryId").val();
            var attrName = $(this).parent().parent().find("input").val();
            $("#attrName").val(attrName);
            $("#attrProductCategoryId").val(productCategoryId);
            isProductExtendAttrForProduct(productCategoryId, attrName);
            tempParent = $(this).parent().parent();

        })

        //删除弹出框删除按钮点击事件
        $('#modal_confirm #ok').click(function (e) {
            //delProductExtendAttr();
            tempParent.hide();
            tempParent.find('#curOptionType').val('delete');
        });


        //可选项插入事件
        $(document).on('change', '.js-sx_xx', function () {
            var $this = $(this);
            var val = $this.find('input').val();
            if ($.trim(val).length > 0) {
                $this.find('i').show();
                $('<span class="sx_xx js-sx_xx"><input type="text" value="" name=""><input type="hidden" id="curOptionOptionType" value="create"><i class="js-x_i">x</i></span>').insertAfter($this);
            }

        })
        //x事件
        $(document).on('click', '.js-x_i', function () {
            var tmp = $(this).parent().find("#productOptionId").val();
            var productCategoryId = $(this).parent().parent().parent().find("#productCategoryId").val();
            var attrName = $(this).parent().parent().parent().find("input").val();
            //alert(attrName);
            //alert(productCategoryId);
            //alert(tmp);
            $("#productOptionId").val(tmp);
            // $('#modal_confirm_option #modal_confirm_body').html("删除属性会影响分类的筛选条件和商品属性，是否继续删除");
            //$('#modal_confirm_option').modal('show');
            tempParent = $(this);
            isProductExtendAttrOptionForProduct(productCategoryId, attrName, tmp);
        })
        //删除弹出框删除按钮点击事件
        $('#modal_confirm_option #ok').click(function (e) {
            //delProductExtendOption();
            tempParent.closest('span').hide();
            tempParent.closest('span').find('#curOptionOptionType').val('delete');
            ;
        });

        // 上传图片
        $('body').on('click', '.img-submit-btn', function () {

            var obj = $.chooseImage.getImgData();
            $.chooseImage.choose(obj, function (data) {
                var contentId = "/content/control/stream?contentId=" + data.uploadedFile0;
                $('#img').attr('src', contentId);
                $('#contentId').val(data.uploadedFile0);
            })
        });

        if( '${parameters.productCategoryLevel?default("0")}'== '3'){
          $('.useCategoryFeature').show();
        }else{
          $('.useCategoryFeature').hide();
        }

        getProductCategoryFeatureList('${parameters.productCategoryIdForUpdate?if_exists}');

    })

    // 取得分类对应的特征类别
    function getProductCategoryFeatureList(productCategoryId) {
        // 选中的项目
        $.ajax({
            url: "<@ofbizUrl>getProductCategoryFeatureList</@ofbizUrl>",
            type: "POST",
            data: {productCategoryId: productCategoryId},
            dataType: "json",
            success: function (cateFeatureData) {
                //异步加载所有分类数据
                $.ajax({
                    url: "<@ofbizUrl>getProductFeatureList</@ofbizUrl>",
                    type: "POST",
                    dataType: "json",
                    success: function (data) {
                        //自动勾选复选框
                        $.each(data.productFeatueTypes, function () {
                           var categoryFeatures =   cateFeatureData.productCategoryFeatures;
                           var hasChecked = false;
                           console.log(categoryFeatures)
                           if(categoryFeatures){
                             for(var i=0;i<categoryFeatures.length;i++){
                               var cateFeatureType = categoryFeatures[i];
                               if(cateFeatureType.productFeatureTypeId === this.productFeatureTypeId){
                                 hasChecked = true;
                                 break;
                               }
                             }
                           }
                           if(this.productFeatureTypeName) {
                               var html = "<div class=\"checkbox col-sm-2\"> <label> <input name=\"categoryFeatureTypeId\"  value=\"" + this.productFeatureTypeId + "\"";
                               if (hasChecked) {
                                   html += " checked ";
                               }
                               html += " type=\"checkbox\" >" + this.productFeatureTypeName + "</label></div>";

                               $("#categoryFeaturesDiv").append(html);
                               console.log(html)
                           }


                        })

                    },
                    error: function (data) {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                        $('#modal_msg').modal();
                    }
                });
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    }

    function getProductCategoryByLevel(productCategoryLevel, productCategoryId) {
        var createPrimaryParentCategory = productCategoryId;
        jQuery.ajax({
            url: '<@ofbizUrl>getProductCategoryByLevel</@ofbizUrl>',
            type: 'POST',
            data: {
                'productCategoryId': productCategoryId,
                'productCategoryLevel': productCategoryLevel
            },
            success: function (data) {
                var productCategoryList = data.productCategoryList;
                $("#primaryParentCategoryId").empty();
                for (var i = 0; i < productCategoryList.length; i++) {
                    var productCategoryId = (productCategoryList[i].productCategoryId);
                    var categoryName = (productCategoryList[i].categoryName);
		            <#if parameters.operateType?if_exists =='update' >
                        <#if pc?has_content>
		                   var pcproductCategoryId = '${pc.primaryParentCategoryId?if_exists}';
		                   if (pcproductCategoryId != '') {
                               if (productCategoryId == pcproductCategoryId) {
                                   $('#primaryParentCategoryId').append("<option value='" + productCategoryId + "'selected=selected>" + categoryName + "</option>");
                               } else {
                                   $('#primaryParentCategoryId').append("<option value='" + productCategoryId + "'>" + categoryName + "</option>");
                               }
                           }
                        </#if>
                    <#else>
		               var pcproductCategoryId = createPrimaryParentCategory;
	                   if (pcproductCategoryId != '') {
                           if (productCategoryId == pcproductCategoryId) {
                               $('#primaryParentCategoryId').append("<option value='" + productCategoryId + "'selected=selected>" + categoryName + "</option>");
                           } else {
                               $('#primaryParentCategoryId').append("<option value='" + productCategoryId + "'>" + categoryName + "</option>");
                           }
                       }
		               //$('#primaryParentCategoryId').append("<option value='" + productCategoryId + "'>" + categoryName + "</option>"); 
                    </#if>
                }
            }
        });
    }


    function extendAttrInfos() {
        var tExtendAttrInfos = "";
        var curExtendAttrInfo = "";
        var tExtendAttrOptionInfos = "";
        var curExtendAttrOptionInfo = "";
        $('.js-table_1>tbody').find("tr").each(function () {
            // 读取tbody中tr的内容
            curExtendAttrInfo = "";
            var tdArr = $(this).children();
            var attrName = tdArr.eq(0).find("input").val();//属性名
            var isRequired = tdArr.eq(2).find("select").val();//是否必填
            var curOptionType = tdArr.eq(0).find("#curOptionType").val();//操作类型
            // alert(curOptionType);
            curExtendAttrOptionInfo = "";

            //可选项的读取
            tdArr.eq(1).find("span").each(function () {
                var option = ""
                var curOptionOptionType = "";
                option = $(this).find("input").val();
                optiocurOptionOptionType = $(this).find("#curOptionOptionType").val();
                productOptionId = $(this).find("#productOptionId").val();
                // alert(optiocurOptionOptionType);
                if (option != "") {
                    var curOption = option + "*" + productOptionId + "*" + optiocurOptionOptionType;
                    curExtendAttrOptionInfo = curExtendAttrOptionInfo + "^" + curOption;
                }
            })
            //tExtendAttrOptionInfos=tExtendAttrOptionInfos+","+curExtendAttrOptionInfo.substr(1,curExtendAttrOptionInfo.length)
            //curExtendAttrInfo=attrName+"|"+isRequired+"|"+curExtendAttrOptionInfo.substr(1,curExtendAttrOptionInfo.length);
            curExtendAttrInfo = attrName + "|" + isRequired + "|" + curOptionType + "|" + curExtendAttrOptionInfo.substr(1, curExtendAttrOptionInfo.length);
            tExtendAttrInfos = tExtendAttrInfos + "," + curExtendAttrInfo;
        });

        var extendAttrInfos = tExtendAttrInfos.substr(1, tExtendAttrInfos.length);
        //var extendOptionInfos=tExtendAttrOptionInfos.substr(1,tExtendAttrOptionInfos.length);
        $("#extendAttrInfos").val(extendAttrInfos);
        //$("#extendOptionInfos").val(extendOptionInfos);
    }


    function dosubmit() {
        if (isCommitted == false) {
            isCommitted = true;//提交表单后，将表单是否已经提交标识设置为true
            return true;//返回true让表单正常提交
        } else {
            return false;//返回false那么表单将不提交
        }
    }


    // 将选择的记录id删除
    function delProductExtendAttr() {
        // 选中的项目
        var attrName = $("#attrName").val();
        var productCategoryId = $("#attrProductCategoryId").val();
        if (productCategoryId != "") {
            jQuery.ajax({
                url: '<@ofbizUrl>delProductExtendAttr</@ofbizUrl>',
                type: 'POST',
                data: {
                    'attrName': attrName,
                    'productCategoryId': productCategoryId
                },
                success: function (data) {
                    tempParent.remove();
                }
            });
        } else {
            tempParent.remove();
        }
    }

    // 将选择的记录id删除
    function delProductExtendOption() {
        // 选中的项目
        var productOptionId = $("#productOptionId").val();
        if (productOptionId != "") {
            jQuery.ajax({
                url: '<@ofbizUrl>delProductExtendOption</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productOptionId': productOptionId
                },
                success: function (data) {
                    tempParent.closest('span').remove();
                }
            });
        } else {
            tempParent.closest('span').remove();
        }
    }

    function imageManage() {
        $.chooseImage.int({
            userId: 3446,
            serverChooseNum: 5,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: ''
        });
        $.chooseImage.show()
    }


    // 判断扩展属性是否被商品使用
    function isProductExtendAttrForProduct(productCategoryId, attrName) {
        if (productCategoryId != "" && attrName != "") {
            jQuery.ajax({
                url: '<@ofbizUrl>isProductExtendAttrForProduct</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productCategoryId': productCategoryId,
                    'attrName': attrName
                },
                success: function (data) {
                    var isUsedFlg = data.isUsedFlg;
                    if (isUsedFlg == "Y") {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("该商品分类的扩展属性已使用，不能删除");
                        $('#modal_msg').modal();
                    } else {
                        //设置删除弹出框内容
                        $('#modal_confirm #modal_confirm_body').html("删除属性会影响分类的筛选条件和商品属性，是否继续删除");
                        $('#modal_confirm').modal('show');
                    }
                }
            });
        }
    }

    // 判断扩展属性的选项是否被商品使用
    function isProductExtendAttrOptionForProduct(productCategoryId, attrName, productOptionId) {
        if (productCategoryId != "" && attrName != "" && productOptionId != "") {
            jQuery.ajax({
                url: '<@ofbizUrl>isProductExtendAttrOptionForProduct</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productCategoryId': productCategoryId,
                    'attrName': attrName,
                    'productOptionId': productOptionId
                },
                success: function (data) {
                    var isUsedFlg = data.isUsedFlg;
                    if (isUsedFlg == "Y") {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("该商品分类扩展属性的选项已使用，不能删除");
                        $('#modal_msg').modal();

                    } else {
                        //设置删除弹出框内容
                        $('#modal_confirm_option #modal_confirm_body').html("删除属性会影响分类的筛选条件和商品属性，是否继续删除");
                        $('#modal_confirm_option').modal('show');
                    }
                }
            });
        }
    }


</script>

		
