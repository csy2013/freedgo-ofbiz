<script src="<@ofbizContentUrl>/images/themes/coloradmin/plugins/jquery-file-upload/js/jquery.iframe-transport.js</@ofbizContentUrl>"></script>
<#--<script src="<@ofbizContentUrl>/images/themes/coloradmin/plugins/jquery-file-upload/js/jquery.fileupload-process.js</@ofbizContentUrl>"></script>-->
<#--<script src="<@ofbizContentUrl>/images/themes/coloradmin/plugins/jquery-file-upload/js/jquery.fileupload-ui.js</@ofbizContentUrl>"></script>-->
<script src="<@ofbizContentUrl>/images/themes/coloradmin/plugins/jquery-file-upload/js/jquery.fileupload.js</@ofbizContentUrl>"></script>

<link href="<@ofbizContentUrl>/images/themes/coloradmin/plugins/jquery-file-upload/blueimp-gallery/blueimp-gallery.min.css</@ofbizContentUrl>" rel="stylesheet">
<link href="<@ofbizContentUrl>/images/themes/coloradmin/plugins/jquery-file-upload/css/jquery.fileupload.css</@ofbizContentUrl>" rel="stylesheet">
<link href="<@ofbizContentUrl>/images/themes/coloradmin/plugins/jquery-file-upload/css/jquery.fileupload-ui.css</@ofbizContentUrl>" rel="stylesheet">



<div class="tab-pane fade" id="default-tab-2">
    <div id="step-content-region">
        <div class="form-horizontal fm-goods-info" novalidate="true">
            <input type="hidden" name="productCategoryId" id="productCategoryId" value="${product.get('primaryProductCategoryId')}"/>
            <input type="hidden" name="productStoreId" id="" value="${requestAttributes.productStoreId}"/>
            <input type="hidden" name="productId" id="productId" value="${product.get('productId')}"/>
            <div id="base-info-region" class="goods-info-group">
                <div class="goods-info-group-inner">
                    <div class="info-group-title">
                        <div class="group-inner">基本信息</div>
                    </div>
                    <div class="info-group-cont">
                        <div class="group-inner">

                            <div class="form-group  m-b-xs">
                                <label class="col-md-2  control-label">商品编码</label>
                                <div class="col-md-10">
                                    <input type="text" name="productCode" class="form-control input-sm normalInput" size="10" maxlength="255"
                                           id="EditProduct1_productCode" data-parsley-required="true" value="${product.productCode?default("")}"/></div>
                            </div>
                            <div class="form-group  m-b-xs">
                                <label class="col-md-2 control-label">商品类型<br><span class="f-s-12">(发布后不能修改) </span></label>
                                <div class="col-md-10">
                                    <label class="radio-inline">
                                        <input type="radio" name="productTypeId" value="FINISHED_GOOD" <#if product.productTypeId == 'FINISHED_GOOD'>checked</#if>>实物商品
                                        <span class="gray">（物流发货）</span>
                                    </label>
                                    <label class="radio-inline">
                                        <input type="radio" name="productTypeId" value="VIRTUAL_GOOD"  <#if product.productTypeId == "VIRTUAL_GOOD">checked</#if>>虚拟商品
                                        <span class="gray">（服务类、包括电子卡券无需物流）</span>
                                    </label>

                                </div>
                            </div>

                            <div class="form-group m-b-xs">
                                <label class="col-md-2 col-lg-2 control-label" id="EditProduct_brandId_title" for="EditProduct_brandId">品牌名称</label>
                                <div class="col-md-10">
                                    <select class="select2-brand" data-live-search="true" id="productBrandId" name="productBrandId" tabindex="-1" aria-hidden="true">
                                    </select>
                                </div>
                            </div>
                            <#assign isLongDate = true>
                            <#if product.salesDiscontinuationDate?exists && product.releaseDate?exists>
                                <#assign isLongDate = false>
                            </#if>

                            <div class="form-group">
                                <label class="col-md-2 control-label">商品有效期</label>
                                <div class="col-md-10 input-group" style="padding-left:15px;">
                                    <label class="radio-inline has-input">
                                        <input type="radio" name="valid_period" value="0" <#if !product.salesDiscontinuationDate?exists>checked</#if>>长期有效
                                    </label>
                                    <label class="radio-inline has-input">
                                        <input type="radio" name="valid_period" value="1" <#if product.salesDiscontinuationDate?exists && product.releaseDate?exists>checked</#if>>自定义有效期
                                    </label>
                                    <div class="valid-period" id="range_period" <#if isLongDate>style="display: none;"</#if>>
                                        <div class="input-append">
                                        <@htmlTemplate.renderDateTimeField name="releaseDate" id="releaseDate" value="${product.releaseDate?if_exists}"/>
                                        </div>
                                        至
                                        <div class="input-append">
                                        <@htmlTemplate.renderDateTimeField name="salesDiscontinuationDate" id="salesDiscontinuationDate" value="${product.salesDiscontinuationDate?if_exists}"/>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group m-b-xs">
                                <label class="col-md-2 col-lg-2 control-label" id="EditProduct_returnable_title" for="EditProduct_returnable">可退货</label>
                                <div class="col-md-2 col-lg-2">
                                    <div class="input-group">
                                        <label class="radio-inline has-input">
                                            <input type="radio" name="returnable" value="Y" <#if product.returnable == 'Y'>checked</#if>>是
                                        </label>
                                        <label class="radio-inline has-input">
                                            <input type="radio" name="returnable" value="N" <#if product.returnable == 'N'>checked</#if>>否
                                        </label>

                                    </div>
                                </div>
                                <label class="col-md-2 col-lg-2 control-label" id="EditProduct_includeInPromotions_title" for="EditProduct_includeInPromotions">是否使用促销</label>
                                <div class="col-md-2 col-lg-2 ">
                                    <div class="input-group">
                                        <label class="radio-inline has-input">
                                            <input type="radio" name="includeInPromotions" value="Y" <#if product.includeInPromotions == 'Y'>checked</#if>>是
                                        </label>
                                        <label class="radio-inline has-input">
                                            <input type="radio" name="includeInPromotions" value="N" <#if product.includeInPromotions == 'N'>checked</#if>>否
                                        </label>
                                    </div>
                                </div>
                                <label class="col-md-2 col-lg-2 control-label" id="EditProduct_taxable_title" for="EditProduct_taxable">自动创建关键字</label>
                                <div class="col-md-2 col-lg-2 ">
                                    <div class="input-group">
                                        <label class="radio-inline has-input">
                                            <input type="radio" name="autoCreateKeywords" value="Y" <#if product.autoCreateKeywords?default('') == 'Y'>checked</#if>>是
                                        </label>
                                        <label class="radio-inline has-input">
                                            <input type="radio" name="autoCreateKeywords" value="N" <#if product.autoCreateKeywords?default('') == 'N'>checked</#if>>否
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="sku-info-region" class="goods-info-group">
                <div class="goods-info-group-inner">
                    <div class="info-group-title vbox">
                        <div class="group-inner">库存/规格</div>
                    </div>
                    <div class="info-group-cont vbox">
                        <div class="group-inner">
                            <div class="form-group js-goods-sku control-group">
                                <label class="js-goods-sku-control-label col-md-2 control-label">商品规格</label>
                                <div id="sku-region" class="col-md-9">
                                    <div class="sku-group control">
                                        <div class="js-sku-list-container"></div>
                                        <div class="js-sku-group-opts" id="sku-group-opts1">
                                            <h3 class="sku-group-title">
                                                <button type="button" class="js-add-sku-group btn" id="js-add-sku-group1">添加规格项目</button>
                                            </h3>
                                        </div>
                                        <div class="js-sku-group-opts" id="sku-group-opts2">
                                            <h3 class="sku-group-title">
                                                <button type="button" class="js-add-sku-group btn" id="js-add-sku-group2">添加规格项目</button>
                                            </h3>
                                        </div>
                                        <div class="js-sku-group-opts" id="sku-group-opts3">
                                            <h3 class="sku-group-title">
                                                <button type="button" class="js-add-sku-group btn" id="js-add-sku-group3">添加规格项目</button>
                                            </h3>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="js-goods-stock control-group form-group" style="display: none;">
                                <label class="js-goods-stock-control-label col-md-2 control-label">商品库存</label>
                                <div id="stock-region" class="controls sku-stock col-md-10">
                                    <table class="table-sku-stock"></table>
                                </div>
                            </div>
                            <#assign  productInventoryAvailable =  dispatcher.runSync("getProductInventoryAvailable", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", product.productId, "userLogin", userLogin))/>

                            <div class="form-group m-b-xs control-group">
                                <label class="col-md-2 control-label"><em class="required">*</em>增加库存数：</label>
                                <div class="col-md-9">
                                    <input type="text" name="productFacilityAmount" value="0"
                                           class="form-control input-sm normalInput" size="10" maxlength="255" id="EditProduct1_internalName"
                                           data-parsley-required="true">
                                    <p class="help-desc quota-help-desc">原库存:${productInventoryAvailable.quantityOnHandTotal?if_exists}</p>

                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
            <div id="goods-info-region" class="goods-info-group">
                <div class="goods-info-group-inner">
                    <div class="info-group-title">
                        <div class="group-inner">商品信息</div>
                    </div>
                    <div class="info-group-cont">
                        <div class="group-inner">
                            <div class="form-group  m-b-xs">
                                <label class="col-md-2 control-label">
                                    <em class="required">*</em>商品名：
                                </label>
                                <div class="col-md-10">
                                    <input type="text" name="productName" value="${product.productName}" maxlength="100" class="input-xxlarge input-sm form-control"
                                           style="width: 250px;">
                                    <p class="help-block error-message">商品名长度不能少于一个字或者多于100个字</p>
                                </div>
                            </div>
                            <#assign productPrices =  dispatcher.runSync("calculateProductPrice",   Static["org.ofbiz.base.util.UtilMisc"].toMap("product", product, "userLogin", userLogin))/>
                            <div class="form-group error">
                                <label class="col-md-3 control-label"><em class="required">*</em>价格：</label>
                                <div class="col-md-9 input-group ">
                                    <div class="input-prepend input-group">
                                        <span class="input-group-addon">￥</span>
                                        <input data-stock-id="total" type="text" maxlength="10" name="DEFAULT_PRICE" value="${productPrices.defaultPrice}"
                                               class="js-price input-sm form-control input-small "
                                               style="width:100px;">
                                        <span class="input-group-addon">市场价</span>
                                        <input type="text" placeholder="原价：¥99.99" name="LIST_PRICE" value="${productPrices.marketPrice?if_exists}"
                                               class="input-sm form-control input-small " style="width:100px;">
                                        <span class="input-group-addon">成本价</span>
                                        <input type="text" placeholder="成本价：￥9.9" name="AVERAGE_COST" value="${productPrices.costPrice?if_exists}"
                                               class=" input-sm form-control input-small  " style="width:100px;">

                                    </div>
                                </div>
                            </div>
                            <#assign productContents =  dispatcher.runSync("productContent", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", product.productId, "userLogin", userLogin))/>
                            <#assign pcontents = productContents.productContents/>

                            <div class="form-group m-b-sm">
                                <label class="col-md-2 control-label"><em class="required">*</em>商品图：</label>
                                <div class="col-md-9">
                                     <#if pcontents.ADDITIONAL_IMAGE_1?exists>
                                        <input type="hidden" name="additionalImg1" value="${pcontents.ADDITIONAL_IMAGE_1}">
                                     <#else>
                                        <input type="hidden" name="additionalImg1">
                                     </#if>
                                     <#if pcontents.ADDITIONAL_IMAGE_2?exists>
                                        <input type="hidden" name="additionalImg2" value="${pcontents.ADDITIONAL_IMAGE_2}">
                                     <#else>
                                        <input type="hidden" name="additionalImg2">
                                     </#if>
                                     <#if pcontents.ADDITIONAL_IMAGE_3?exists>
                                        <input type="hidden" name="additionalImg3" value="${pcontents.ADDITIONAL_IMAGE_3}">
                                     <#else>
                                        <input type="hidden" name="additionalImg3">
                                     </#if>
                                     <#if pcontents.ADDITIONAL_IMAGE_4?exists>
                                        <input type="hidden" name="additionalImg4" value="${pcontents.ADDITIONAL_IMAGE_4}">
                                     <#else>
                                        <input type="hidden" name="additionalImg4">
                                     </#if>
                                     <#if pcontents.ADDITIONAL_IMAGE_5?exists>
                                        <input type="hidden" name="additionalImg5" value="${pcontents.ADDITIONAL_IMAGE_5}">
                                     <#else>
                                        <input type="hidden" name="additionalImg5">
                                     </#if>
                                     <#if pcontents.ADDITIONAL_IMAGE_6?exists>
                                        <input type="hidden" name="additionalImg6" value="${pcontents.ADDITIONAL_IMAGE_6}">
                                     <#else>
                                        <input type="hidden" name="additionalImg6">
                                     </#if>
                                    <#if pcontents.DETAIL_IMAGE_URL?exists>
                                        <input type="hidden" name="mainImg" value="${pcontents.DETAIL_IMAGE_URL}">
                                    <#else>
                                        <input type="hidden" name="mainImg">
                                    </#if>

                                    <div class="ui-uploadGroup ui-uploadGroup-normal ui-uploadGroup-s">
                                        <ul class="items" imgsku="all">
                                            <li class="item" id="sku_img_1" num="1">
                                                <div class="ui-uploadGroup-img">
                                                    <a class="file btng-ui-m" href="javascript:void(0);" title="宝贝图片格式为JPG,大小不能超过5M"><span>+</span>
                                                        <input class="filePrew" type="file" id="skuImg_uploadBtn_1" name="additionalImageOne" size="3"></a>
                                                </div>
                                                <div class="ui-uploadGroup-img hidden"><a class="img" target="_blank" href=""><img src="" width="64" height="64" alt="img"></a>
                                                    <span class="ui-uploadGroup-action">
                                                    <span class="btn-move-l" onclick="uploadThumbMoveL(this);" title="左移"><i class="fa  fa-arrow-circle-o-left"></i></span>
                                                    <span class="btn-move-r" onclick="uploadThumbMoveR(this);" title="右移">
                                                        <i class="fa  fa-arrow-circle-o-right"></i>
                                                    </span>
                                                    <span class="btn-delete" onclick="uploadThumbMoveDelete(this,1);" title="删除">
                                                                <i class="fa  fa-times-circle-o"></i>
                                                    </span>
                                                </span>
                                                </div>
                                            </li>
                                            <li class="item" id="sku_img_2" num="2">
                                                <div class="ui-uploadGroup-img"><a class="file btng-ui-m" href="javascript:void(0);"
                                                                                   title="宝贝图片格式为JPG,大小不能超过5M"><span>+</span>
                                                    <input class="filePrew"
                                                           type="file"
                                                           id="skuImg_uploadBtn_2"
                                                           name="additionalImageTwo"
                                                           size="3"></a></div>
                                                <div class="ui-uploadGroup-img hidden"><a class="img" target="_blank" href=""><img src="" width="64" height="64" alt="img"></a>
                                                    <span class="ui-uploadGroup-action">
                                                        <span class="btn-move-l" onclick="uploadThumbMoveL(this);" title="左移"><i class="fa  fa-arrow-circle-o-left"></i></span>
                                                        <span class="btn-move-r" onclick="uploadThumbMoveR(this);" title="右移"><i
                                                                class="fa  fa-arrow-circle-o-right"></i></span>
                                                        <span class="btn-delete" onclick="uploadThumbMoveDelete(this,2);" title="删除"><i class="fa  fa-times-circle-o"></i></span>
                                                    </span>
                                                </div>
                                            </li>
                                            <li class="item" id="sku_img_3" num="3">
                                                <div class="ui-uploadGroup-img"><a class="file btng-ui-m" href="javascript:void(0);"
                                                                                   title="宝贝图片格式为JPG,大小不能超过5M"><span>+</span>
                                                    <input class="filePrew"
                                                           type="file"
                                                           id="skuImg_uploadBtn_3"
                                                           name="additionalImageThree"
                                                           size="3"></a></div>
                                                <div class="ui-uploadGroup-img hidden"><a class="img" target="_blank" href=""><img src="" width="64" height="64" alt="img"></a>
                                                    <span class="ui-uploadGroup-action">
                                                        <span class="btn-move-l" onclick="uploadThumbMoveL(this);" title="左移"><i class="fa  fa-arrow-circle-o-left"></i></span>
                                                        <span class="btn-move-r" onclick="uploadThumbMoveR(this);" title="右移"><i
                                                                class="fa  fa-arrow-circle-o-right"></i></span>
                                                        <span class="btn-delete" onclick="uploadThumbMoveDelete(this,3);" title="删除"><i class="fa  fa-times-circle-o"></i></span>
                                                    </span>
                                                </div>
                                            </li>
                                            <li class="item" id="sku_img_4" num="4">
                                                <div class="ui-uploadGroup-img"><a class="file btng-ui-m" href="javascript:void(0);"
                                                                                   title="宝贝图片格式为JPG,大小不能超过5M"><span>+</span><input
                                                        class="filePrew"
                                                        type="file"
                                                        id="skuImg_uploadBtn_4"
                                                        name="additionalImageFour"
                                                        size="3"></a></div>
                                                <div class="ui-uploadGroup-img hidden"><a class="img" target="_blank" href=""><img src="" width="64" height="64" alt="img"></a><span
                                                        class="ui-uploadGroup-action">
                                                    <span class="btn-move-l" onclick="uploadThumbMoveL(this);" title="左移"><i class="fa  fa-arrow-circle-o-left"></i></span>
                                                    <span class="btn-move-r" onclick="uploadThumbMoveR(this);" title="右移"><i class="fa  fa-arrow-circle-o-right"></i></span>
                                                    <span class="btn-delete" onclick="uploadThumbMoveDelete(this,4);" title="删除">
                                                        <i class="fa  fa-times-circle-o"></i></span>
                                                </span>
                                                </div>
                                            </li>
                                            <li class="item" id="sku_img_5" num="5">
                                                <div class="ui-uploadGroup-img"><a class="file btng-ui-m" href="javascript:void(0);"
                                                                                   title="宝贝图片格式为JPG,大小不能超过5M"><span>+</span><input
                                                        class="filePrew"
                                                        type="file"
                                                        id="skuImg_uploadBtn_5"
                                                        name="additionalImageFive"
                                                        size="3"></a></div>
                                                <div class="ui-uploadGroup-img hidden"><a class="img" target="_blank" href=""><img src="" width="64" height="64" alt="img"></a>
                                                    <span class="ui-uploadGroup-action">
                                                    <span class="btn-move-l" onclick="uploadThumbMoveL(this);" title="左移"><i class="fa  fa-arrow-circle-o-left"></i></span>
                                                    <span class="btn-move-r" onclick="uploadThumbMoveR(this);" title="右移"><i class="fa  fa-arrow-circle-o-right"></i></span>
                                                    <span class="btn-delete" onclick="uploadThumbMoveDelete(this,5);" title="删除"><i class="fa  fa-times-circle-o"></i></span>
                                                </span>
                                                </div>
                                            </li>
                                            <li class="item" id="sku_img_6" num="6">
                                                <div class="ui-uploadGroup-img"><a class="file btng-ui-m" href="javascript:void(0);"
                                                                                   title="宝贝图片格式为JPG,大小不能超过5M"><span>+</span><input
                                                        class="filePrew"
                                                        type="file"
                                                        id="skuImg_uploadBtn_6"
                                                        name="additionalImageSix"
                                                        size="3"></a></div>
                                                <div class="ui-uploadGroup-img hidden"><a class="img" target="_blank" href=""><img src="" width="64" height="64" alt="img"></a><span
                                                        class="ui-uploadGroup-action"><span
                                                        class="btn-move-l" onclick="uploadThumbMoveL(this);" title="左移"><i class="fa  fa-arrow-circle-o-left"></i></span><span
                                                        class="btn-delete"
                                                        onclick="uploadThumbMoveDelete(this,6);"
                                                        title="删除"><i class="fa  fa-times-circle-o"></i></span></span>
                                                </div>
                                            </li>
                                        </ul>
                                    </div>
                                    <p class="help-desc">商品图至少有一张, 建议尺寸：640 x 640 像素；你可以拖拽图片调整图片顺序。</p>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-md-2 control-label">主图：</label>
                                <div class="col-md-9">
                                    <div class="ui-uploadGroup ui-uploadGroup-normal ui-uploadGroup-s">
                                        <ul class="items" imgsku="all">
                                            <li class="item" num="0">
                                                <div class="ui-uploadGroup-img"><a class="file btng-ui-m" href="javascript:void(0);"
                                                                                   title="宝贝图片格式为JPG,大小不能超过5M">
                                                    <span>+</span>
                                                    <input class="filePrew" type="file" id="mainImg_uploadFileInput" name="mainImg_uploadFileInput"
                                                           size="3"></a><span class="ui-uploadGroup-action">主图</span></div>
                                                <div class="ui-uploadGroup-img hidden">
                                                    <a class="img" target="_blank" href=""><img src="" width="64" height="64" alt="img"></a>
                                                    <span class="ui-uploadGroup-action">
                                                            <#--<span class="btn-move-r" onclick="uploadThumbMoveR(this);" title="右移"><i class="fa  fa-arrow-circle-o-right"></i></span>-->
                                                                <span class="btn-delete" onclick="uploadThumbMoveDelete(this,0);" title="删除"><i
                                                                        class="fa  fa-times-circle-o"></i></span>
                                                    </span>
                                                </div>
                                            </li>

                                    </div>
                                    <p class="help-desc">建议尺寸：640 x 640 像素</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div id="other-info-region" class="goods-info-group">
                    <div class="goods-info-group-inner">
                        <div class="info-group-title vbox">
                            <div class="group-inner">其他设置</div>
                        </div>
                        <div class="info-group-cont vbox">
                            <div class="group-inner">

                                <div class="form-group m-b-xs">
                                    <label class="col-md-2 col-lg-2 control-label" id="EditProduct_recommend_title" for="EditProduct_recommend">是否推荐首页</label>
                                    <div class="col-md-2 col-lg-2">
                                        <div class="input-group">
                                            <label class="radio-inline has-input">
                                                <input type="radio" name="isRecommendHomePage" value="Y" <#if product.isRecommendHomePage == 'Y'>checked</#if>>是
                                            </label>
                                            <label class="radio-inline has-input">
                                                <input type="radio" name="isRecommendHomePage" value="N" <#if product.isRecommendHomePage == 'N'>checked</#if>>否
                                            </label>

                                        </div>
                                    </div>
                                    <label class="col-md-2 col-lg-2 control-label" id="EditProduct_isListShow_title" for="EditProduct_isListShow">是否列表展现</label>
                                    <div class="col-md-2 col-lg-2 ">
                                        <div class="input-group">
                                            <label class="radio-inline has-input">
                                                <input type="radio" name="isListShow" value="Y" <#if product.isListShow == 'Y'>checked</#if>>是
                                            </label>
                                            <label class="radio-inline has-input">
                                                <input type="radio" name="isListShow" value="N" <#if product.isListShow == 'N'>checked</#if>>否
                                            </label>
                                        </div>
                                    </div>

                                </div>


                                <div class="form-group">
                                    <label class="col-md-2 control-label">体积：</label>
                                    <div class="col-md-9">
                                        <input type="text" name="volume" value="${product.volume}" class="input-sm form-control js-quota" style="width:100px;">
                                        <p class="help-desc quota-help-desc">单位:cm3</p>
                                    </div>

                                    <label class="col-md-2 control-label">重量：</label>
                                    <div class="col-md-9">
                                        <input type="text" name="weight" value="${product.weight}" class="input-sm form-control js-quota" style="width:100px;">
                                        <p class="help-desc quota-help-desc">kg</p>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-md-2 control-label">seo关键字：</label>
                                    <div class="col-md-7 input-group ">

                                        <textarea name="seoKeyword" class="input-sm form-control js-quota" rows="4">${product.seoKeyword?if_exists}</textarea>
                                        <p class="help-desc quota-help-desc">关键字逗号分隔</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="app-actions">
            <div class="text-center">
                <button type="button" class="btn btn-lg btn-primary" onclick="panelNext('third');">确认产品基本信息并进入下一步</button>
                <button type="button" class="btn btn-lg btn-default" onclick="window.location.href='findAllGoods.htm?menuId=3&amp;menuParentId=7&amp;myselfId=18'">返回列表</button>
            </div>
        </div>
    </div>
</div>

    <script type="text/javascript">

        $(function () {
            //设置sku group
            $('#sku-group-opts2').hide();
            $('#sku-group-opts3').hide();

            $.ajax({
                url: 'queryAllBrand.htm',
                data: '',
                method: 'post',
                success: function (data) {
                    data = data.brands;
                    var html = '';
                    if (data) {
                        for (var i = 0; i < data.length; i++) {
                            var brand = data[i];
                            if (brand.productBrandId == ${product.brandId}) {
                                html += '<option value=' + brand.productBrandId + ' selected>' + brand.brandName + '</option>';
                            } else {
                                html += '<option value=' + brand.productBrandId + '>' + brand.brandName + '</option>';
                            }
                        }
                    }
                    $("#productBrandId").html(html);
                    $('#productBrandId').select2();
                }
            });

            $("input[name='valid_period']").click(function () {

                if ($("input[name='valid_period']:checked").val() == '1') {
                    $('#range_period').show();
                }
            })


            //group1
            $('#js-add-sku-group1').click(function () {
                $(this).parent().parent().hide();
                //保证如果是3个group都show不做操作，如果另外两个都开或者其中一个开则显示按钮
                if ($(".sku-sub-group").length < 2) {
                    if ($("#sku-sub-group2").length > 0) {
                        $('#sku-group-opts3').show()
                    } else {
                        $('#sku-group-opts2').show()
                    }
                }

                var that = this;
                $.ajax({
                    url: 'findFeatureCategoriesByCategory.htm',
                    data: 'productCategoryId=' + $('#productCategoryId').val(),
                    method: 'post',
                    success: function (data) {
                        console.log(data)
                        data = data.productCategoryFeatures;
                        var sel = '';
                        if (data) {
                            sel = '<select class="select2-container js-states form-control js-sku-name" id="sku-group1-opt"   style="width:120px;"   data-placeholder="Select a state"></select>';
                            var skuGroup3 = $('#sku-group3-opt').val();
                            var skuGroup2 = $('#sku-group2-opt').val();
                            var fdata = new Array();
                            var j = 0;
                            for (var i = 0; i < data.length; i++) {
                                var feature = data[i];
                                var obj = {};
                                obj.id = feature.productFeatureTypeId;
                                obj.text = feature.productFeatureTypeName;
                                if ((skuGroup2 && obj.id == skuGroup2) || (skuGroup3 && obj.id == skuGroup3)) {

                                } else {
                                    fdata[j] = obj;
                                    j++;
                                }

                            }
                        }
                        var groupCount = $(".sku-sub-group").length + 1;
                        var categoryId = $('#productCategoryId').val();
                        var skuGroup = '<div class="sku-sub-group" id="sku-sub-group1">' +
                                '<input type="hidden" id="suk-sub-group1-sort" name="suk-sub-group1-sort" value="' + groupCount + '"><h3 class="sku-group-title">\n' +
                                sel + '<label for="js-addImg-function" class="addImg-radio">\n' +
                                '                <input type="checkbox" id="sku-group1-addImg-function" onclick="skuGroup1AddImgCheck(this)">添加规格图片</label>\n' +
                                '                如无规格可选择,点击<a href="CreateProductCategory?operateType=update&productCategoryLevel=3&productCategoryId=' + categoryId + '&productCategoryIdForUpdate=' + categoryId + '">添加分类下规格</a>\n' +
                                '        <a class="js-remove-sku-group remove-sku-group" id="sku-group1-opt-remove" onclick="removeSkuGroup1(this)">×</a>\n' +
                                '</h3></div>';

                        $('.js-sku-list-container').append(skuGroup);
                        $('#sku-group1-opt').select2({data: fdata, tags: true, placeholder: "请选择一个SKU", language: 'zh_CN'});
                        $('#sku-group1-opt').on("select2:select", function (e) {
                            var skuGroup3 = $('#sku-group3-opt').val();
                            var skuGroup2 = $('#sku-group2-opt').val();
                            var skuGroup1 = $('#sku-group1-opt').val();
                            if (skuGroup2 && skuGroup2 == skuGroup1) {
                                alert('当前SKU已经被选择');
                                $('#sku-group1-opt').val('').trigger('change');
                                return;
                            }
                            if (skuGroup3 && skuGroup3 == skuGroup1) {
                                alert('当前SKU已经被选择');
                                $('#sku-group1-opt').val('').trigger('change');
                                return;
                            }

                            addGroup1Feature(this, $('#sku-group1-opt').select2('data')[0].id, $('#sku-group1-opt').select2('data')[0].text);
                        }).on("select2:selecting", function (t) {

                        }).on("select2:opening", function () {

                        });
                        $('#sku-group1-opt').select2('open');
                        //默认第一次open选中的值
                        if ($('#sku-group1-opt').val()) {
                            addGroup1Feature(this, $('#sku-group1-opt').select2('data')[0].id, $('#sku-group1-opt').select2('data')[0].text);
                        }
                    }
                })
            });

            //group2
            $('#js-add-sku-group2').click(function () {
                $(this).parent().parent().hide();
                //保证如果是3个group都show不做操作，如果另外两个都开或者其中一个开则显示按钮
                if ($(".sku-sub-group").length < 2) {
                    if ($("#sku-sub-group1").length > 0) {
                        $('#sku-group-opts3').show()
                    } else {
                        $('#sku-group-opts1').show()
                    }
                }
                var that = this;
                $.ajax({
                    url: 'findFeatureCategoriesByCategory.htm',
                    data: 'productCategoryId=' + $('#productCategoryId').val(),
                    method: 'post',
                    success: function (data) {
                        console.log(data)
                        data = data.productCategoryFeatures;
                        var sel = '';
                        var fdata = new Array();
                        if (data) {
                            sel = '<select class="select2-container js-states form-control js-sku-name" id="sku-group2-opt"   style="width:120px;"  data-placeholder="Select a state"></select>';
                            var skuGroup3 = $('#sku-group3-opt').val();
                            var skuGroup1 = $('#sku-group1-opt').val();
                            var j = 0;
                            for (var i = 0; i < data.length; i++) {
                                var feature = data[i];
                                var obj = {};
                                obj.id = feature.productFeatureTypeId;
                                obj.text = feature.productFeatureTypeName;
                                if ((skuGroup1 && obj.id == skuGroup1) || (skuGroup3 && obj.id == skuGroup3)) {

                                } else {
                                    fdata[j] = obj;
                                    j++;
                                }
                            }
                            console.log(fdata)
                        }
                        var groupCount = $(".sku-sub-group").length + 1;
                        var skuGroup = '<div class="sku-sub-group" id="sku-sub-group2">' +
                                '<input type="hidden" name="suk-sub-group2-sort" id="suk-sub-group2-sort"  value="' + groupCount + '"><h3 class="sku-group-title">\n' +
                                sel + '<label for="js-addImg-function" class="addImg-radio">\n' +
                                '                <input type="checkbox" id="sku-group2-addImg-function" onclick="skuGroup2AddImgCheck(this)">添加规格图片</label>\n' +
                                '        <a class="js-remove-sku-group remove-sku-group" id="sku-group2-opt-remove" onclick="removeSkuGroup2(this)">×</a>\n' +
                                '</h3></div>';

                        $('.js-sku-list-container').append(skuGroup);
                        $('#sku-group2-opt').select2({data: fdata, tags: true, placeholder: "请选择一个SKU", language: 'zh_CN'});
                        $('#sku-group2-opt').on("select2:select", function (e) {
                            var skuGroup3 = $('#sku-group3-opt').val();
                            var skuGroup2 = $('#sku-group2-opt').val();
                            var skuGroup1 = $('#sku-group1-opt').val();
                            if (skuGroup1 && skuGroup1 == skuGroup2) {
                                alert('当前SKU已经被选择');
                                $('#sku-group2-opt').val('').trigger('change');
                                return;
                            }
                            if (skuGroup3 && skuGroup3 == skuGroup2) {
                                alert('当前SKU已经被选择');
                                $('#sku-group2-opt').val('').trigger('change');
                                return;
                            }
                            addGroup2Feature(this, $('#sku-group2-opt').select2('data')[0].id, $('#sku-group2-opt').select2('data')[0].text);
                        }).on("select2:selecting", function (t) {

                        }).on("select2:opening", function () {
                        });
                        $('#sku-group2-opt').select2('open');
                        //默认第一次open选中的值
                        if ($('#sku-group2-opt').val()) {
                            addGroup2Feature(that, $('#sku-group2-opt').select2('data')[0].id, $('#sku-group2-opt').select2('data')[0].text)
                        }
                    }
                })
            });
            //group3
            $('#js-add-sku-group3').click(function () {
                $(this).parent().parent().hide();
                //保证如果是3个group都show不做操作，如果另外两个都开或者其中一个开则显示按钮
                if ($(".sku-sub-group").length < 2) {
                    if ($("#sku-sub-group2").length > 0) {
                        $('#sku-group-opts1').show()
                    } else {
                        $('#sku-group-opts2').show()
                    }
                }
                var that = this;
                $.ajax({
                    url: 'findFeatureCategoriesByCategory.htm',
                    data: 'productCategoryId=' + $('#productCategoryId').val(),
                    method: 'post',
                    success: function (data) {
                        console.log(data)
                        data = data.productCategoryFeatures;
                        var sel = '';

                        if (data) {
                            sel = '<select class="select2-container js-states form-control js-sku-name" id="sku-group3-opt"   style="width:120px;"  data-placeholder="Select a state"></select>';
                            var skuGroup2 = $('#sku-group2-opt').val();
                            var skuGroup1 = $('#sku-group1-opt').val();
                            var fdata = new Array();
                            var j = 0;
                            for (var i = 0; i < data.length; i++) {
                                var feature = data[i];
                                var obj = {};
                                obj.id = feature.productFeatureTypeId;
                                obj.text = feature.productFeatureTypeName;
                                if ((skuGroup1 && obj.id == skuGroup1) || (skuGroup2 && obj.id == skuGroup2)) {
                                } else {
                                    fdata[j] = obj;
                                    j++;
                                }
                            }
                        }
                        console.log(fdata);
                        var groupCount = $(".sku-sub-group").length + 1;
                        var skuGroup = '<div class="sku-sub-group" id="sku-sub-group3">' +
                                '<input type="hidden" name="suk-sub-group3-sort"  id="suk-sub-group3-sort" value="' + groupCount + '"><h3 class="sku-group-title">\n' +
                                sel + '<label for="js-addImg-function" class="addImg-radio">\n' +
                                '                <input type="checkbox" id="sku-group3-addImg-function" onclick="skuGroup3AddImgCheck(this)">添加规格图片</label>\n' +
                                '        <a class="js-remove-sku-group remove-sku-group" id="sku-group3-opt-remove" onclick="removeSkuGroup3(this)">×</a>\n' +
                                '</h3></div>';

                        $('.js-sku-list-container').append(skuGroup);
                        $('#sku-group3-opt').select2({data: fdata, tags: true, placeholder: "请选择一个SKU", language: 'zh_CN'});
                        $('#sku-group3-opt').on("select2:select", function (e) {
                            //判断是否已经被选择了
                            var skuGroup3 = $('#sku-group3-opt').val();
                            var skuGroup2 = $('#sku-group2-opt').val();
                            var skuGroup1 = $('#sku-group1-opt').val();
                            if (skuGroup2 && skuGroup2 == skuGroup3) {
                                alert('当前SKU已经被选择');
                                $('#sku-group3-opt').val('').trigger('change');
                                return;
                            }
                            if (skuGroup1 && skuGroup1 == skuGroup3) {
                                alert('当前SKU已经被选择');
                                $('#sku-group3-opt').val('').trigger('change');
                                return;
                            }
//                            console.log($('#sku-group3-opt').select2('data'))
                            addGroup3Feature(that, $('#sku-group3-opt').select2('data')[0].id, $('#sku-group3-opt').select2('data')[0].text)
                        }).on("select2:selecting", function (t) {

                        }).on("select2:opening", function () {

                        });
                        $('#sku-group3-opt').select2('open');
                        //默认第一次open选中的值
                        if ($('#sku-group3-opt').val()) {
                            addGroup3Feature(that, $('#sku-group3-opt').select2('data')[0].id, $('#sku-group3-opt').select2('data')[0].text)
                        }
                    }
                })
            });

            //加载addition images
            for (var seq = 1; seq < 7; seq++) {
                var additionImage = $('input[name="additionalImg' + seq + '"]').val();
                if (additionImage) {
                    var objId = $('input[id^="skuImg_uploadBtn_' + seq + '"]');
                    skuImg_init(additionImage, objId, seq);
                }
            }

            //加载主图
            var mainImg = $('input[name="mainImg"]').val();
            if (mainImg) {
                var fileUrl = mainImg;
                $('#mainImg_uploadFileInput').parents("li:first").find("img").attr("src", fileUrl + "?t=" + generateMixed(6) + "${externalKeyParam?if_exists}");
                $('#mainImg_uploadFileInput').parents("li:first").find("a").attr("href", fileUrl + "?t=" + generateMixed(6) + "${externalKeyParam?if_exists}");
                $('#mainImg_uploadFileInput').parents("div:first").addClass("hidden");
                $('#mainImg_uploadFileInput').parents("li:first").find("div:last").removeClass("hidden");
                $('input[name="mainImg"]').val(fileUrl);
            }


        });

        // begin group1
        function addGroup1Feature(obj, value, name) {
            $('#sku-group1-opt-value-hidden-name').remove();
            $('#sku-group1-opt-value-hidden').remove();
            $('#sku-group1-opt-value').remove();

            $('#sku-sub-group1').append('<input type="hidden" name="sku-group1-opt-value-hidden" id="sku-group1-opt-value-hidden" value="' + value + '"/>' +
                    '<input type="hidden" name="sku-group1-opt-value-hidden-name" id="sku-group1-opt-value-hidden-name" value="' + name + '"/>' +
                    '<input type="hidden" name="group1FeaturesInDb" id="group1FeaturesInDb" value="">' +
                    '<div class="js-sku-atom-container sku-group-cont" id="sku-group1-opt-value"><div>' +
                    '<div class="js-sku-atom-list sku-atom-list" id="js-sku-group1-atom-list"></div>' +
                    '<a onclick="featureGroup1Popver(this,\'' + value + '\')" class="js-add-sku-atom add-sku" id="sku-group1-opt-popver" style="display: inline-block;"  data-container="body" data-toggle="popover" data-placement="bottom"' +
                    ' data-content="<div class=\'ui-popover top-center\'><select multiple id=\'group1FeatureId\' name=\'featureId\' class=\'select2-container select2-container-multi js-select2\' style=\'width: 242px;\'></select><button class=\'btn btn-primary js-save\' style=\'vertical-align: top\' id=\'sku-group1-opt-btn-s\' onclick=\'openCurrentGroup1Opt(this)\'>确定</button>' +
                    '<button class=\'btn btn-white js-cancel\' style=\'vertical-align: top\' id=\'sku-group1-opt-btn-c\' onclick=\'closeCurrentGroup1Opt(this)\'>取消</button></div>" ' +
                    'data-html="true">+添加</a></div></div>');
        }

        var group1OptDataSource = [];

        function featureGroup1Popver(o, value) {
            $('#sku-group1-opt-popver').popover('destroy');
            $(o).popover('show');

            $('#group1FeatureId').select2({
                allowClear: !0,
                multiple: !0,
                placeholder: "添加规格值",
                tags: true,
                ajax: {
                    url: 'findFeaturesByFeatureType.htm',
                    data: {featureTypeId: $('#sku-group1-opt').val()},
                    method: 'post',
                    dataType: 'json',
                    processResults: function (data) {
                        console.log(data)
                        var resultObj = {};
                        var result = data.features;
                        var results = [];
                        var dbFeatureIds = [];
                        if (result) {
                            var p = 0;
                            for (var i = 0; i < result.length; i++) {
                                var obj = {id: result[i].productFeatureId, text: result[i].productFeatureName}
                                results[i] = obj;
                                dbFeatureIds[i] = result[i].productFeatureName;
                                if (result[i].dataResourceId) {
                                    group1OptDataSource[p] = [result[i].productFeatureId, result[i].dataResourceId];
                                    p++;
                                }
                            }
                        }
                        $('#group1FeaturesInDb').val(dbFeatureIds.toString());
                        resultObj.results = results;
                        console.log(resultObj);
                        return resultObj;

                    }
                },
//                data: [{id: 1, text: '红'}, {id: 2, text: '黄'}, {id: 3, text: '绿'}],
                maximumInputLength: 20
            });
            $('#group1FeatureId').on("select2:select", function (e) {
                var oldFeature1s = $('input[name="sku-group1-atom-feature-name"]').val();//之前选择的值
                var featObjs = $('#group1FeatureId').select2("data"); //本次select 选择的值
                var newFeatures = [];
                var newFeaturestr = '';
                console.log('group1FeatureId.oldFeature1s=', oldFeature1s);
                console.log('group1FeatureId.featObjs=', featObjs);
                if (oldFeature1s && featObjs) {
                    var oldarrs = oldFeature1s.split(",");
                    var s = 0;
                    for (var i = 0; i < featObjs.length; i++) {
                        var hasVal = false;
                        for (var j = 0; j < oldarrs.length; j++) {
                            if (featObjs[i].text === oldarrs[j]) {
                                alert('当前值已经存在');
                                hasVal = true;
                            }
                        }
                        if (!hasVal) {
                            newFeaturestr += newFeaturestr + featObjs[i].id + ",";
                            newFeatures[s] = featObjs[i];
                            s++;
                        }
                    }
                    console.log('group1FeatureId.newFeaturestr=' + newFeaturestr);
                    $('#group1FeatureId').val(newFeaturestr.split(",")).trigger('change');
                } else {
                    newFeatures = featObjs;
                }

                $('#group1FeatureId').select2('open')

            }).on("select2:selecting", function (t) {

            }).on("select2:opening", function (d) {
            });
            $('#group1FeatureId').select2('open')
        }

        //打开sku选择其中一个
        function openCurrentGroup1Opt(obj) {
            var featureIds = $('#group1FeatureId').select2("data");
            if (featureIds) {
                var featureIdVals = "";
                var featureNameVals = "";
                var newFeatureNames = [];
                var newFeatureIndexs = [];
                var y = 0;
                for (var j = 0; j < featureIds.length; j++) {
                    if (featureIds[j].id === featureIds[j].text) {
                        newFeatureNames[y] = featureIds[j].text;
                        newFeatureIndexs[y] = j;
                        y++;
                    }
                }
                console.log('newFeatureIndexs=', newFeatureIndexs);

                if (newFeatureNames.length > 0) {
                    $.ajax({
                        url: 'addFeatures.html',
                        dataType: 'json',
                        data: {featureNames: newFeatureNames.toString(), featureCategoryId: $('#sku-group1-opt').val()},
                        method: 'post',
                        async: false,
                        success: function (data) {
                            var features = data.productFeatures;
                            for (var n = 0; n < newFeatureIndexs.length; n++) {
                                var h = newFeatureIndexs[n];
                                featureIds[h].id = features[n].productFeatureId;
                            }
                        }
                    });
                }

                for (var j = 0; j < featureIds.length; j++) {
                    if (j == 0) {
                        featureIdVals = featureIds[j].id;
                        featureNameVals = featureIds[j].text;
                    } else {
                        featureIdVals = featureIdVals + "," + featureIds[j].id;
                        featureNameVals = featureNameVals + "," + featureIds[j].text;
                    }
                }
                console.log('featureIdVals', featureIdVals);

                var featureHtml = "";
                if ($('input[name="sku-group1-atom-feature"]').length == 0) {
                    featureHtml += "<input type=\"hidden\" name=\"sku-group1-atom-feature\" id=\"sku-group1-atom-feature\" value=" + featureIdVals + ">";
                    featureHtml += "<input type=\"hidden\" name=\"sku-group1-atom-feature-name\" id=\"sku-group1-atom-feature-name\" value=" + featureNameVals + ">";
                } else {
                    $('input[name="sku-group1-atom-feature"]').val($('input[name="sku-group1-atom-feature"]').val() + "," + featureIdVals);
                    $('input[name="sku-group1-atom-feature-name"]').val($('input[name="sku-group1-atom-feature-name"]').val() + "," + featureNameVals);
                }

                for (var i = 0; i < featureIds.length; i++) {
                    var feat = featureIds[i];
                    var spanHtml = '';
                    var imageStr = '';
                    if (group1OptDataSource) {
                        for (var j = 0; j < group1OptDataSource.length; j++) {
                            var obj1 = group1OptDataSource[j];
                            if (obj1[0] === feat.id) {
                                var dataSource = obj1[1];
                                if (dataSource) {
                                    spanHtml = "<div class=\"add-image js-btn-add hidden\">+</div>\n";
                                    var fileUrl = '/content/control/img?imgId=' + dataSource + '${externalKeyParam?if_exists}';
                                    imageStr = "<div class=\"ui-uploadGroup-img1\"><a class=\"img\" target=\"_blank\" href=\"" + fileUrl + "\"><img src=\"" + fileUrl + "\" width=\"90\" height=\"90\" alt=\"img\"></a>";
                                }
                            }
                        }
                    }
                    if (spanHtml === '') {
                        spanHtml = "<div class=\"add-image js-btn-add\">+</div>\n";
                        imageStr = "<div class=\"ui-uploadGroup-img1 hidden\"><a class=\"img\" target=\"_blank\" href=\"\"><img src=\"\" width=\"90\" height=\"90\" alt=\"img\"></a>";
                    }

                    featureHtml += "<div class=\"sku-atom \"><span>" + feat.text + "</span>\n" +
                            "<div class=\"atom-close close-modal small js-remove-sku-atom\" onclick=\"closeSkuGroup1Atom(this,'" + feat.id + "','" + feat.text + "')\">×</div>" +
                            "<div class=\"upload-img-wrap \"><div class=\"arrow\"></div>\n" +
                            "<div class=\"js-upload-container\" style=\"position:relative;\">\n" +
                            spanHtml +
                            "<input class=\"filePrew\" type=\"file\" id=\"featrueOptImg_" + feat.id + "\" name=\"imageData\" size=\"3\" >" +
                            imageStr +
                            " <span class=\"ui-uploadGroup-action\">\n" +
                            " <span class=\"btn-delete\" onclick=\"uploadThumbMoveDelete1(this);\" title=\"删除\">\n" +
                            " <i class=\"fa  fa-times-circle-o\"></i> </span> </span>\n" +
                            "</div>" +
                            "</div></div></div>";
                }

                $('#js-sku-group1-atom-list').append(featureHtml);
                if (!$('#js-tip-instruction-1')) {
                    $('#sku-group1-opt-value').append("<div class=\"sku-group-cont\" id=\"js-tip-instruction-1\" style=\"padding: 0px 10px; display: block;\">\n" +
                            "                    <p class=\"help-desc\">目前只支持为第一个规格设置不同的规格图片</p>\n" +
                            "                    <p class=\"help-desc\">设置后，用户选择不同规格会显示不同图片</p>\n" +
                            "                    <p class=\"help-desc\">建议尺寸：640 x 640像素</p>\n" +
                            "                </div>")
                }

            }
            if ($('#sku-group1-addImg-function').is(':checked')) {
                $('#js-sku-group1-atom-list').find(".sku-atom").addClass("active")
                $('#js-sku-group1-atom-list').find(".upload-img-wrap").show();
            } else {
                $('#js-sku-group1-atom-list').find(".sku-atom").removeClass("active")
                $('#js-sku-group1-atom-list').find(".upload-img-wrap").hide();
            }
            $('#sku-group1-opt-popver').popover('destroy');
            createTableForSku();
        }

        function closeCurrentGroup1Opt(obj) {
            $('#sku-group1-opt-popver').popover('destroy')
            console.log('closeCurrentOpt')
        }

        function skuGroup1AddImgCheck(obj) {

            if ($('#sku-group1-addImg-function').is(':checked')) {
                $('#js-sku-group1-atom-list').find(".sku-atom").addClass("active")
                $('#js-sku-group1-atom-list').find(".upload-img-wrap").show();
            } else {
                $('#js-sku-group1-atom-list').find(".sku-atom").removeClass("active")
                $('#js-sku-group1-atom-list').find(".upload-img-wrap").hide();
            }
        }

        function closeSkuGroup1Atom(obj, value, name) {
            $(obj).parent().remove();
            var oldFeature1s = $('input[name="sku-group1-atom-feature"]').val();//之前选择的值
            var oldFeature1Names = $('input[name="sku-group1-atom-feature-name"]').val();//之前选择的值
            if (oldFeature1s == undefined) {
                return;
            }
            var newVal = "";
            var oldArr = oldFeature1s.split(",");
            for (var i = 0; i < oldArr.length; i++) {
                if (oldArr[i] == value) {

                } else {
                    newVal += oldArr[i] + ","
                }
            }

            var newNameVal = "";
            var oldNameArr = oldFeature1Names.split(",");
            for (var i = 0; i < oldNameArr.length; i++) {
                if (oldNameArr[i] == name) {

                } else {
                    newNameVal += oldNameArr[i] + ","
                }
            }
            if (newVal !== "") {
                newVal = newVal.substring(0, newVal.length - 1)
            }
            if (newNameVal !== "") {
                newNameVal = newNameVal.substring(0, newNameVal.length - 1)
            }
            $('input[name="sku-group1-atom-feature"]').val(newVal);
            $('input[name="sku-group1-atom-feature-name"]').val(newNameVal);
            createTableForSku();
        }

        function removeSkuGroup1(obj) {
            $('#sku-sub-group1').remove();
            //判断 group2，group3是否有display，如果没有show
            if ($('#sku-group-opts2').is(':visible') || $('#sku-group-opts3').is(':visible')) {
            } else {
                $('#sku-group-opts1').show();
            }
            $('#sku-group1-opt-value-hidden').val('');
            $('#sku-group1-opt-value-hidden-name').val('');
            createTableForSku();
        }

        // end group1

        // begin group2
        function addGroup2Feature(obj, value, name) {
            $('#sku-group2-opt-value-hidden-name').remove();
            $('#sku-group2-opt-value-hidden').remove();
            $('#sku-group2-opt-value').remove();
            $('#sku-sub-group2').append('<input type="hidden" name="" id="sku-group2-opt-value-hidden" value="' + value + '"/>' +
                    '<input type="hidden" name="" id="sku-group2-opt-value-hidden-name" value="' + name + '"/>' +
                    '<div class="js-sku-atom-container sku-group-cont" id="sku-group2-opt-value"><div>' +
                    '<div class="js-sku-atom-list sku-atom-list" id="js-sku-group2-atom-list"></div>' +
                    '<a onclick="featureGroup2Popver(this,\'' + value + '\')" class="js-add-sku-atom add-sku" id="sku-group2-opt-popver" style="display: inline-block;"  data-container="body" data-toggle="popover" data-placement="bottom"' +
                    ' data-content="<div class=\'ui-popover top-center\'><select multiple id=\'group2FeatureId\' name=\'featureId\' class=\'select2-container select2-container-multi js-select2\' style=\'width: 242px;\'></select><button class=\'btn btn-primary js-save\' style=\'vertical-align: top\' id=\'sku-group2-opt-btn-s\' onclick=\'openCurrentGroup2Opt(this)\'>确定</button>' +
                    '<button class=\'btn btn-white js-cancel\' style=\'vertical-align: top\' id=\'sku-group2-opt-btn-c\' onclick=\'closeCurrentGroup2Opt(this)\'>取消</button></div>" ' +
                    'data-html="true">+添加</a></div></div>');
        }

        var group2OptDataSource = [];

        function featureGroup2Popver(o, value) {
            $('#sku-group2-opt-popver').popover('destroy');
            $(o).popover('show');

            $('#group2FeatureId').select2({
                allowClear: !0,
                multiple: !0,
                placeholder: "添加规格值",
                tags: true,
                ajax: {
                    url: 'findFeaturesByFeatureType.htm',
                    data: {featureTypeId: $('#sku-group2-opt').val()},
                    method: 'post',
                    dataType: 'json',
                    processResults: function (data) {
                        console.log(data)
                        var resultObj = {};
                        var result = data.features;
                        var results = [];
                        var dbFeatureIds = [];
                        if (result) {
                            var p = 0;
                            for (var i = 0; i < result.length; i++) {
                                var obj = {id: result[i].productFeatureId, text: result[i].productFeatureName}
                                results[i] = obj;
                                dbFeatureIds[i] = result[i].productFeatureName;
                                if (result[i].dataResourceId) {
                                    group2OptDataSource[p] = [result[i].productFeatureId, result[i].dataResourceId];
                                    p++;
                                }
                            }
                        }
                        $('#group2FeaturesInDb').val(dbFeatureIds.toString());
                        resultObj.results = results;
                        console.log(resultObj);
                        return resultObj;

                    }
                },
//                data: [{id: 1, text: '红'}, {id: 2, text: '黄'}, {id: 3, text: '绿'}],
                maximumInputLength: 20
            });
            $('#group2FeatureId').on("select2:select", function (e) {
                var oldFeature2s = $('input[name="sku-group2-atom-feature-name"]').val();//之前选择的值
                var featObjs = $('#group2FeatureId').select2("data"); //本次select 选择的值
                var newFeatures = [];
                var newFeaturestr = '';
                console.log('group2FeatureId.oldFeature2s=', oldFeature2s);
                console.log('group2FeatureId.featObjs=', featObjs);
                if (oldFeature2s && featObjs) {
                    var oldarrs = oldFeature2s.split(",");
                    var s = 0;
                    for (var i = 0; i < featObjs.length; i++) {
                        var hasVal = false;
                        for (var j = 0; j < oldarrs.length; j++) {
                            if (featObjs[i].text === oldarrs[j]) {
                                alert('当前值已经存在');
                                hasVal = true;
                            }
                        }
                        if (!hasVal) {
                            newFeaturestr += newFeaturestr + featObjs[i].id + ",";
                            newFeatures[s] = featObjs[i];
                            s++;
                        }
                    }
                    console.log('group2FeatureId.newFeaturestr=' + newFeaturestr);
                    $('#group2FeatureId').val(newFeaturestr.split(",")).trigger('change');
                } else {
                    newFeatures = featObjs;
                }

                $('#group2FeatureId').select2('open')

            }).on("select2:selecting", function (t) {

            }).on("select2:opening", function (d) {
            });
            $('#group2FeatureId').select2('open')
        }

        //打开sku选择其中一个
        function openCurrentGroup2Opt(obj) {
            var featureIds = $('#group2FeatureId').select2("data");
            if (featureIds) {
                var featureIdVals = "";
                var featureNameVals = "";
                var newFeatureNames = [];
                var newFeatureIndexs = [];
                var y = 0;
                for (var j = 0; j < featureIds.length; j++) {
                    if (featureIds[j].id === featureIds[j].text) {
                        newFeatureNames[y] = featureIds[j].text;
                        newFeatureIndexs[y] = j;
                        y++;
                    }
                }
                console.log('newFeatureIndexs=', newFeatureIndexs);

                if (newFeatureNames.length > 0) {
                    $.ajax({
                        url: 'addFeatures.html',
                        dataType: 'json',
                        data: {featureNames: newFeatureNames.toString(), featureCategoryId: $('#sku-group2-opt').val()},
                        method: 'post',
                        async: false,
                        success: function (data) {
                            var features = data.productFeatures;
                            for (var n = 0; n < newFeatureIndexs.length; n++) {
                                var h = newFeatureIndexs[n];
                                featureIds[h].id = features[n].productFeatureId;
                            }
                        }
                    });
                }

                for (var j = 0; j < featureIds.length; j++) {
                    if (j == 0) {
                        featureIdVals = featureIds[j].id;
                        featureNameVals = featureIds[j].text;
                    } else {
                        featureIdVals = featureIdVals + "," + featureIds[j].id;
                        featureNameVals = featureNameVals + "," + featureIds[j].text;
                    }
                }
                console.log('featureIdVals', featureIdVals);

                var featureHtml = "";
                if ($('input[name="sku-group2-atom-feature"]').length == 0) {
                    featureHtml += "<input type=\"hidden\" name=\"sku-group2-atom-feature\" id=\"sku-group2-atom-feature\" value=" + featureIdVals + ">";
                    featureHtml += "<input type=\"hidden\" name=\"sku-group2-atom-feature-name\" id=\"sku-group2-atom-feature-name\" value=" + featureNameVals + ">";
                } else {
                    $('input[name="sku-group2-atom-feature"]').val($('input[name="sku-group2-atom-feature"]').val() + "," + featureIdVals);
                    $('input[name="sku-group2-atom-feature-name"]').val($('input[name="sku-group2-atom-feature-name"]').val() + "," + featureNameVals);
                }

                for (var i = 0; i < featureIds.length; i++) {
                    var feat = featureIds[i];
                    var spanHtml = '';
                    var imageStr = '';
                    if (group2OptDataSource) {
                        for (var j = 0; j < group2OptDataSource.length; j++) {
                            var obj1 = group2OptDataSource[j];
                            if (obj1[0] === feat.id) {
                                var dataSource = obj1[1];
                                if (dataSource) {
                                    spanHtml = "<div class=\"add-image js-btn-add hidden\">+</div>\n";
                                    var fileUrl = '/content/control/img?imgId=' + dataSource + '${externalKeyParam?if_exists}';
                                    imageStr = "<div class=\"ui-uploadGroup-img2\"><a class=\"img\" target=\"_blank\" href=\"" + fileUrl + "\"><img src=\"" + fileUrl + "\" width=\"90\" height=\"90\" alt=\"img\"></a>";
                                }
                            }
                        }
                    }
                    if (spanHtml === '') {
                        spanHtml = "<div class=\"add-image js-btn-add\">+</div>\n";
                        imageStr = "<div class=\"ui-uploadGroup-img2 hidden\"><a class=\"img\" target=\"_blank\" href=\"\"><img src=\"\" width=\"90\" height=\"90\" alt=\"img\"></a>";
                    }

                    featureHtml += "<div class=\"sku-atom \"><span>" + feat.text + "</span>\n" +
                            "<div class=\"atom-close close-modal small js-remove-sku-atom\" onclick=\"closeSkuGroup2Atom(this,'" + feat.id + "','" + feat.text + "')\">×</div>" +
                            "<div class=\"upload-img-wrap \"><div class=\"arrow\"></div>\n" +
                            "<div class=\"js-upload-container\" style=\"position:relative;\">\n" +
                            spanHtml +
                            "<input class=\"filePrew\" type=\"file\" id=\"featrueOptImg_" + feat.id + "\" name=\"imageData\" size=\"3\" >" +
                            imageStr +
                            " <span class=\"ui-uploadGroup-action\">\n" +
                            " <span class=\"btn-delete\" onclick=\"uploadThumbMoveDelete1(this);\" title=\"删除\">\n" +
                            " <i class=\"fa  fa-times-circle-o\"></i> </span> </span>\n" +
                            "</div>" +
                            "</div></div></div>";
                }

                $('#js-sku-group2-atom-list').append(featureHtml);
                if (!$('#js-tip-instruction-2')) {
                    $('#sku-group2-opt-value').append("<div class=\"sku-group-cont\" id=\"js-tip-instruction-2\" style=\"padding: 0px 20px; display: block;\">\n" +
                            "                    <p class=\"help-desc\">目前只支持为第一个规格设置不同的规格图片</p>\n" +
                            "                    <p class=\"help-desc\">设置后，用户选择不同规格会显示不同图片</p>\n" +
                            "                    <p class=\"help-desc\">建议尺寸：640 x 640像素</p>\n" +
                            "                </div>")
                }

            }
            if ($('#sku-group2-addImg-function').is(':checked')) {
                $('#js-sku-group2-atom-list').find(".sku-atom").addClass("active")
                $('#js-sku-group2-atom-list').find(".upload-img-wrap").show();
            } else {
                $('#js-sku-group2-atom-list').find(".sku-atom").removeClass("active")
                $('#js-sku-group2-atom-list').find(".upload-img-wrap").hide();
            }
            $('#sku-group2-opt-popver').popover('destroy');
            createTableForSku();
        }

        function closeCurrentGroup2Opt(obj) {
            $('#sku-group2-opt-popver').popover('destroy')
            console.log('closeCurrentOpt')
        }

        function skuGroup2AddImgCheck(obj) {

            if ($('#sku-group2-addImg-function').is(':checked')) {
                $('#js-sku-group2-atom-list').find(".sku-atom").addClass("active")
                $('#js-sku-group2-atom-list').find(".upload-img-wrap").show();
            } else {
                $('#js-sku-group2-atom-list').find(".sku-atom").removeClass("active")
                $('#js-sku-group2-atom-list').find(".upload-img-wrap").hide();
            }
        }

        function closeSkuGroup2Atom(obj, value, name) {
            $(obj).parent().remove();
            var oldFeature1s = $('input[name="sku-group2-atom-feature"]').val();//之前选择的值
            var oldFeature1Names = $('input[name="sku-group2-atom-feature-name"]').val();//之前选择的值
            if (oldFeature1s == undefined) {
                return;
            }
            var newVal = "";
            var oldArr = oldFeature1s.split(",");
            for (var i = 0; i < oldArr.length; i++) {
                if (oldArr[i] == value) {

                } else {
                    newVal += oldArr[i] + ","
                }
            }

            var newNameVal = "";
            var oldNameArr = oldFeature1Names.split(",");
            for (var i = 0; i < oldNameArr.length; i++) {
                if (oldNameArr[i] == name) {

                } else {
                    newNameVal += oldNameArr[i] + ","
                }
            }
            if (newVal !== "") {
                newVal = newVal.substring(0, newVal.length - 1)
            }
            if (newNameVal !== "") {
                newNameVal = newNameVal.substring(0, newNameVal.length - 1)
            }
            $('input[name="sku-group2-atom-feature"]').val(newVal);
            $('input[name="sku-group2-atom-feature-name"]').val(newNameVal);
            createTableForSku();
        }

        function removeSkuGroup2(obj) {
            $('#sku-sub-group2').remove();
            //增加特征的排序
            if ($('#sku-group-opts1').is(':visible') || $('#sku-group-opts3').is(':visible')) {
            } else {
                $('#sku-group-opts2').show();
            }
            $('#sku-group2-opt-value-hidden').val('');
            $('#sku-group2-opt-value-hidden-name').val('');
            createTableForSku();
        }

        // end group2


        // begin group3
        function addGroup3Feature(obj, value, name) {
            $('#sku-group3-opt-value-hidden-name').remove();
            $('#sku-group3-opt-value-hidden').remove();
            $('#sku-group3-opt-value').remove();
            $('#sku-sub-group3').append('<input type="hidden" name="" id="sku-group3-opt-value-hidden" value="' + value + '"/>' +
                    '<input type="hidden" name="" id="sku-group3-opt-value-hidden-name" value="' + name + '"/>' +
                    '<div class="js-sku-atom-container sku-group-cont" id="sku-group3-opt-value"><div>' +
                    '<div class="js-sku-atom-list sku-atom-list" id="js-sku-group3-atom-list"></div>' +
                    '<a onclick="featureGroup3Popver(this,\'' + value + '\')" class="js-add-sku-atom add-sku" id="sku-group3-opt-popver" style="display: inline-block;"  data-container="body" data-toggle="popover" data-placement="bottom"' +
                    ' data-content="<div class=\'ui-popover top-center\'><select multiple id=\'group3FeatureId\' name=\'featureId\' class=\'select2-container select2-container-multi js-select2\' style=\'width: 242px;\'></select><button class=\'btn btn-primary js-save\' style=\'vertical-align: top\' id=\'sku-group3-opt-btn-s\' onclick=\'openCurrentGroup3Opt(this)\'>确定</button>' +
                    '<button class=\'btn btn-white js-cancel\' style=\'vertical-align: top\' id=\'sku-group3-opt-btn-c\' onclick=\'closeCurrentGroup3Opt(this)\'>取消</button></div>" ' +
                    'data-html="true">+添加</a></div></div>');
        }

        var group3OptDataSource = [];

        function featureGroup3Popver(o, value) {
            $('#sku-group3-opt-popver').popover('destroy');
            $(o).popover('show');

            $('#group3FeatureId').select2({
                allowClear: !0,
                multiple: !0,
                placeholder: "添加规格值",
                tags: true,
                ajax: {
                    url: 'findFeaturesByFeatureType.htm',
                    data: {featureTypeId: $('#sku-group3-opt').val()},
                    method: 'post',
                    dataType: 'json',
                    processResults: function (data) {
                        console.log(data)
                        var resultObj = {};
                        var result = data.features;
                        var results = [];
                        var dbFeatureIds = [];
                        if (result) {
                            var p = 0;
                            for (var i = 0; i < result.length; i++) {
                                var obj = {id: result[i].productFeatureId, text: result[i].productFeatureName}
                                results[i] = obj;
                                dbFeatureIds[i] = result[i].productFeatureName;
                                if (result[i].dataResourceId) {
                                    group3OptDataSource[p] = [result[i].productFeatureId, result[i].dataResourceId];
                                    p++;
                                }
                            }
                        }
                        $('#group3FeaturesInDb').val(dbFeatureIds.toString());
                        resultObj.results = results;
                        console.log(resultObj);
                        return resultObj;

                    }
                },
//                data: [{id: 1, text: '红'}, {id: 2, text: '黄'}, {id: 3, text: '绿'}],
                maximumInputLength: 20
            });
            $('#group3FeatureId').on("select2:select", function (e) {
                var oldFeature3s = $('input[name="sku-group3-atom-feature-name"]').val();//之前选择的值
                var featObjs = $('#group3FeatureId').select2("data"); //本次select 选择的值
                var newFeatures = [];
                var newFeaturestr = '';
                console.log('group3FeatureId.oldFeature3s=', oldFeature3s);
                console.log('group3FeatureId.featObjs=', featObjs);
                if (oldFeature3s && featObjs) {
                    var oldarrs = oldFeature3s.split(",");
                    var s = 0;
                    for (var i = 0; i < featObjs.length; i++) {
                        var hasVal = false;
                        for (var j = 0; j < oldarrs.length; j++) {
                            if (featObjs[i].text === oldarrs[j]) {
                                alert('当前值已经存在');
                                hasVal = true;
                            }
                        }
                        if (!hasVal) {
                            newFeaturestr += newFeaturestr + featObjs[i].id + ",";
                            newFeatures[s] = featObjs[i];
                            s++;
                        }
                    }
                    console.log('group3FeatureId.newFeaturestr=' + newFeaturestr);
                    $('#group3FeatureId').val(newFeaturestr.split(",")).trigger('change');
                } else {
                    newFeatures = featObjs;
                }

                $('#group3FeatureId').select2('open')

            }).on("select2:selecting", function (t) {

            }).on("select2:opening", function (d) {
            });
            $('#group3FeatureId').select2('open')
        }

        //打开sku选择其中一个
        function openCurrentGroup3Opt(obj) {
            var featureIds = $('#group3FeatureId').select2("data");
            if (featureIds) {
                var featureIdVals = "";
                var featureNameVals = "";
                var newFeatureNames = [];
                var newFeatureIndexs = [];
                var y = 0;
                for (var j = 0; j < featureIds.length; j++) {
                    if (featureIds[j].id === featureIds[j].text) {
                        newFeatureNames[y] = featureIds[j].text;
                        newFeatureIndexs[y] = j;
                        y++;
                    }
                }
                console.log('newFeatureIndexs=', newFeatureIndexs);

                if (newFeatureNames.length > 0) {
                    $.ajax({
                        url: 'addFeatures.html',
                        dataType: 'json',
                        data: {featureNames: newFeatureNames.toString(), featureCategoryId: $('#sku-group3-opt').val()},
                        method: 'post',
                        async: false,
                        success: function (data) {
                            var features = data.productFeatures;
                            for (var n = 0; n < newFeatureIndexs.length; n++) {
                                var h = newFeatureIndexs[n];
                                featureIds[h].id = features[n].productFeatureId;
                            }
                        }
                    });
                }

                for (var j = 0; j < featureIds.length; j++) {
                    if (j == 0) {
                        featureIdVals = featureIds[j].id;
                        featureNameVals = featureIds[j].text;
                    } else {
                        featureIdVals = featureIdVals + "," + featureIds[j].id;
                        featureNameVals = featureNameVals + "," + featureIds[j].text;
                    }
                }
                console.log('featureIdVals', featureIdVals);

                var featureHtml = "";
                if ($('input[name="sku-group3-atom-feature"]').length == 0) {
                    featureHtml += "<input type=\"hidden\" name=\"sku-group3-atom-feature\" id=\"sku-group3-atom-feature\" value=" + featureIdVals + ">";
                    featureHtml += "<input type=\"hidden\" name=\"sku-group3-atom-feature-name\" id=\"sku-group3-atom-feature-name\" value=" + featureNameVals + ">";
                } else {
                    $('input[name="sku-group3-atom-feature"]').val($('input[name="sku-group3-atom-feature"]').val() + "," + featureIdVals);
                    $('input[name="sku-group3-atom-feature-name"]').val($('input[name="sku-group3-atom-feature-name"]').val() + "," + featureNameVals);
                }

                for (var i = 0; i < featureIds.length; i++) {
                    var feat = featureIds[i];
                    var spanHtml = '';
                    var imageStr = '';
                    if (group3OptDataSource) {
                        for (var j = 0; j < group3OptDataSource.length; j++) {
                            var obj1 = group3OptDataSource[j];
                            if (obj1[0] === feat.id) {
                                var dataSource = obj1[1];
                                if (dataSource) {
                                    spanHtml = "<div class=\"add-image js-btn-add hidden\">+</div>\n";
                                    var fileUrl = '/content/control/img?imgId=' + dataSource + '${externalKeyParam?if_exists}';
                                    imageStr = "<div class=\"ui-uploadGroup-img3\"><a class=\"img\" target=\"_blank\" href=\"" + fileUrl + "\"><img src=\"" + fileUrl + "\" width=\"90\" height=\"90\" alt=\"img\"></a>";
                                }
                            }
                        }
                    }
                    if (spanHtml === '') {
                        spanHtml = "<div class=\"add-image js-btn-add\">+</div>\n";
                        imageStr = "<div class=\"ui-uploadGroup-img3 hidden\"><a class=\"img\" target=\"_blank\" href=\"\"><img src=\"\" width=\"90\" height=\"90\" alt=\"img\"></a>";
                    }

                    featureHtml += "<div class=\"sku-atom \"><span>" + feat.text + "</span>\n" +
                            "<div class=\"atom-close close-modal small js-remove-sku-atom\" onclick=\"closeSkuGroup3Atom(this,'" + feat.id + "','" + feat.text + "')\">×</div>" +
                            "<div class=\"upload-img-wrap \"><div class=\"arrow\"></div>\n" +
                            "<div class=\"js-upload-container\" style=\"position:relative;\">\n" +
                            spanHtml +
                            "<input class=\"filePrew\" type=\"file\" id=\"featrueOptImg_" + feat.id + "\" name=\"imageData\" size=\"3\" >" +
                            imageStr +
                            " <span class=\"ui-uploadGroup-action\">\n" +
                            " <span class=\"btn-delete\" onclick=\"uploadThumbMoveDelete1(this);\" title=\"删除\">\n" +
                            " <i class=\"fa  fa-times-circle-o\"></i> </span> </span>\n" +
                            "</div>" +
                            "</div></div></div>";
                }

                $('#js-sku-group3-atom-list').append(featureHtml);
                if (!$('#js-tip-instruction-3')) {
                    $('#sku-group3-opt-value').append("<div class=\"sku-group-cont\" id=\"js-tip-instruction-3\" style=\"padding: 0px 10px; display: block;\">\n" +
                            "                    <p class=\"help-desc\">目前只支持为第一个规格设置不同的规格图片</p>\n" +
                            "                    <p class=\"help-desc\">设置后，用户选择不同规格会显示不同图片</p>\n" +
                            "                    <p class=\"help-desc\">建议尺寸：640 x 640像素</p>\n" +
                            "                </div>")
                }

            }
            if ($('#sku-group3-addImg-function').is(':checked')) {
                $('#js-sku-group3-atom-list').find(".sku-atom").addClass("active")
                $('#js-sku-group3-atom-list').find(".upload-img-wrap").show();
            } else {
                $('#js-sku-group3-atom-list').find(".sku-atom").removeClass("active")
                $('#js-sku-group3-atom-list').find(".upload-img-wrap").hide();
            }
            $('#sku-group3-opt-popver').popover('destroy');
            createTableForSku();
        }

        function closeCurrentGroup3Opt(obj) {
            $('#sku-group3-opt-popver').popover('destroy')
            console.log('closeCurrentOpt')
        }

        function skuGroup3AddImgCheck(obj) {

            if ($('#sku-group3-addImg-function').is(':checked')) {
                $('#js-sku-group3-atom-list').find(".sku-atom").addClass("active")
                $('#js-sku-group3-atom-list').find(".upload-img-wrap").show();
            } else {
                $('#js-sku-group3-atom-list').find(".sku-atom").removeClass("active")
                $('#js-sku-group3-atom-list').find(".upload-img-wrap").hide();
            }
        }

        function closeSkuGroup3Atom(obj, value, name) {
            $(obj).parent().remove();
            var oldFeature1s = $('input[name="sku-group3-atom-feature"]').val();//之前选择的值
            var oldFeature1Names = $('input[name="sku-group3-atom-feature-name"]').val();//之前选择的值
            if (oldFeature1s == undefined) {
                return;
            }
            var newVal = "";
            var oldArr = oldFeature1s.split(",");
            for (var i = 0; i < oldArr.length; i++) {
                if (oldArr[i] == value) {

                } else {
                    newVal += oldArr[i] + ","
                }
            }

            var newNameVal = "";
            var oldNameArr = oldFeature1Names.split(",");
            for (var i = 0; i < oldNameArr.length; i++) {
                if (oldNameArr[i] == name) {

                } else {
                    newNameVal += oldNameArr[i] + ","
                }
            }
            if (newVal !== "") {
                newVal = newVal.substring(0, newVal.length - 1)
            }
            if (newNameVal !== "") {
                newNameVal = newNameVal.substring(0, newNameVal.length - 1)
            }
            $('input[name="sku-group3-atom-feature"]').val(newVal);
            $('input[name="sku-group3-atom-feature-name"]').val(newNameVal);
            createTableForSku();
        }

        function removeSkuGroup3(obj) {
            $('#sku-sub-group3').remove();
            //增加特征的排序
            if ($('#sku-group-opts2').is(':visible') || $('#sku-group-opts1').is(':visible')) {
            } else {
                $('#sku-group-opts3').show();
            }
            $('#sku-group3-opt-value-hidden').val('');
            $('#sku-group3-opt-value-hidden-name').val('');
            createTableForSku();
        }

        // end group3

        //生成table显示sku产品的列表显示啊。累死了

        function selectionSort(arr) {
            var len = arr.length;
            var minIndex, temp;
            for (var i = 0; i < len - 1; i++) {
                minIndex = i;
                for (var j = i + 1; j < len; j++) {
                    if (arr[j][0] < arr[minIndex][0]) {     //寻找最小的数
                        minIndex = j;                 //将最小数的索引保存
                    }
                }
                temp = arr[i];
                arr[i] = arr[minIndex];
                arr[minIndex] = temp;
            }
            return arr;
        }

        function createTableForSku() {
            $('.js-goods-stock').hide();
            $('.table-sku-stock').html('');

            ////////////group1
            //suk-sub-group1-sort
            var sortGroup1 = $('#suk-sub-group1-sort').val();
            //sku1 group id,name
            var skuGroup1Val = $('#sku-group1-opt-value-hidden').val();
            var skuGroup1Name = $('#sku-group1-opt-value-hidden-name').val();
            //sku1 feature id,name
            var skuGroup1OptsVal = $('#sku-group1-atom-feature').val();
            var skuGroup1OptsName = $('#sku-group1-atom-feature-name').val();

            ////////////group2
            //suk-sub-group2-sort
            var sortGroup2 = $('#suk-sub-group2-sort').val();
            //sku2 group id,name
            var skuGroup2Val = $('#sku-group2-opt-value-hidden').val();
            var skuGroup2Name = $('#sku-group2-opt-value-hidden-name').val();
            //sku2 feature id,name
            var skuGroup2OptsVal = $('#sku-group2-atom-feature').val();
            var skuGroup2OptsName = $('#sku-group2-atom-feature-name').val();

            ////////////group3
            //suk-sub-group3-sort
            var sortGroup3 = $('#suk-sub-group3-sort').val();
            //sku3 group id,name
            var skuGroup3Val = $('#sku-group3-opt-value-hidden').val();
            var skuGroup3Name = $('#sku-group3-opt-value-hidden-name').val();
            //sku3 feature id,name
            var skuGroup3OptsVal = $('#sku-group3-atom-feature').val();
            var skuGroup3OptsName = $('#sku-group3-atom-feature-name').val();


            var group1 = [sortGroup1, [skuGroup1Val, skuGroup1Name], [skuGroup1OptsVal, skuGroup1OptsName]];
            var group2 = [sortGroup2, [skuGroup2Val, skuGroup2Name], [skuGroup2OptsVal, skuGroup2OptsName]];
            var group3 = [sortGroup3, [skuGroup3Val, skuGroup3Name], [skuGroup3OptsVal, skuGroup3OptsName]];
            var groupArrayTemp = [group1, group2, group3];
            var groupArray = new Array();
            var j = 0;
            for (var i = 0; i < groupArrayTemp.length; i++) {
                if (groupArrayTemp[i][0] !== undefined) {
                    groupArray[j] = groupArrayTemp[i];
                    j++;
                }
            }

            //groupArray sort 排序算法
            groupArray = selectionSort(groupArray);
            var thHtml = "";
            if (groupArray[0] && groupArray[0][2][0] !== "" && groupArray[0][2][1] !== "") {
                thHtml = "<thead><tr>"
                var oGroup1 = groupArray[0];
                var sku1 = oGroup1[1][1];
                thHtml += "<th class=\"text-center\">" + sku1 + "</th>";
                if (groupArray[1]) {
                    var oGroup2 = groupArray[1];
                    var sku2 = oGroup2[1][1];
                    thHtml += "<th class=\"text-center\">" + sku2 + "</th>";
                }
                if (groupArray[2]) {
                    var oGroup3 = groupArray[2];
                    var sku3 = oGroup3[1];
                    thHtml += "<th class=\"text-center\">" + sku3[1] + "</th>";
                }
                thHtml += "<th class=\"th-price\">价格（元）</th> <th class=\"th-stock\">增加库存</th> <th class=\"th-code\">商家编码</th> <th class=\"text-cost-price\">成本价</th><th class=\"text-right\">销量</th>"
                thHtml += "</thead></tr>"

            }
            var tbodyHtml = "";
            var othertdHtml = "<td> <input data-stock-id=\"0\" type=\"text\" name=\"sku_price\" class=\"form-control js-price input-mini\" value=\"\" maxlength=\"10\"> </td>\n" +
                    "<td> <input type=\"text\" name=\"stock_num\" class=\"form-control js-stock-num input-mini\" value=\"\" maxlength=\"9\">原库存:<span name=\"old_stock_num\"></span></td>\n" +
                    " <td> <input type=\"text\" name=\"code\" class=\"form-control  js-code input-small\" value=\"\"> </td>\n" +
                    " <td><input type=\"text\" name=\"cost_price\" class=\"form-control js-cost-price input-mini\" value=\"\">\n" +
                    " </td> <td class=\"text-right\">0</td>";
            console.log(groupArray)
            if (groupArray[0] && groupArray[0][2][0] !== "" && groupArray[0][2][1] !== "") {
                tbodyHtml = "<tbody>"
                var oGroup1 = groupArray[0];
                var skuOpts1 = oGroup1[2];
                var skuOpts1Ids = skuOpts1[0];
                var skuOpts1Names = skuOpts1[1];
                var skuOpts1IdsArray = skuOpts1Ids.split(",");
                var skuOpts1NamesArray = skuOpts1Names.split(",");
                var outputHtml = "";

                for (var j = 0; j < skuOpts1IdsArray.length; j++) {
                    var spanCount = 1;
                    var firstTr = "";
                    var secondTr = "";
                    var thirdTr = "";
                    if (groupArray[1] && groupArray[1][2][0] !== "" && groupArray[1][2][1] !== "") {
                        var oGroup2 = groupArray[1];
                        var skuOpts2 = oGroup2[2];
                        var skuOpts2Ids = skuOpts2[0];
                        var skuOpts2Names = skuOpts2[1];
                        var skuOpts2IdsArray = skuOpts2Ids.split(",");
                        var skuOpts2NamesArray = skuOpts2Names.split(",");
                        var spanCount = skuOpts2IdsArray.length;
                        var count3 = 1;

                        for (var k = 0; k < skuOpts2IdsArray.length; k++) {
                            if (groupArray[2] && groupArray[2][2][0] !== "" && groupArray[2][2][1] !== "") {
                                var oGroup3 = groupArray[2];
                                var skuOpts3 = oGroup3[2];
                                var skuOpts3Ids = skuOpts3[0];
                                var skuOpts3Names = skuOpts3[1];
                                var skuOpts3IdsArray = skuOpts3Ids.split(",");
                                var skuOpts3NamesArray = skuOpts3Names.split(",");
                                count3 = skuOpts3IdsArray.length;
                                spanCount = spanCount * count3;

                                for (var m = 0; m < skuOpts3IdsArray.length; m++) {
                                    if (m == 0 && k == 0) {
                                        thirdTr += "<input type='hidden' name='sku_opt_" + skuOpts1IdsArray[j] + "_" + skuOpts2IdsArray[k] + "_" + skuOpts3IdsArray[m] + "' value='" + skuOpts3IdsArray[m] + "'>";
                                        thirdTr += "<tr><td rowspan=\"" + spanCount + "\">" + skuOpts1NamesArray[j] + " </td>";
                                        thirdTr += "<td rowspan=\"" + skuOpts3NamesArray.length + "\">" + skuOpts2NamesArray[k] + " </td>";
                                        thirdTr += "<td>" + skuOpts3NamesArray[m] + " </td>";
                                    } else if (m == 0) {
                                        thirdTr += "<input type='hidden' name='sku_opt_" + skuOpts1IdsArray[j] + "_" + skuOpts2IdsArray[k] + "_" + skuOpts3IdsArray[m] + "' value='" + skuOpts3IdsArray[m] + "'>";
                                        thirdTr += "<tr><td rowspan='" + skuOpts3NamesArray.length + "'>" + skuOpts2NamesArray[k] + " </td>";
                                        thirdTr += "<td>" + skuOpts3NamesArray[m] + " </td>"
                                    } else {
                                        thirdTr += "<input type='hidden' name='sku_opt_" + skuOpts1IdsArray[j] + "_" + skuOpts2IdsArray[k] + "_" + skuOpts3IdsArray[m] + "' value='" + skuOpts3IdsArray[m] + "'>";
                                        thirdTr += "<tr><td>" + skuOpts3NamesArray[m] + " </td>"
                                    }
                                    thirdTr += othertdHtml + "</tr>";
                                }
                            } else {
                                if (k == 0) {
                                    secondTr += "<input type='hidden' name='sku_opt_" + skuOpts1IdsArray[j] + "_" + skuOpts2IdsArray[k] + "' value='" + skuOpts2IdsArray[k] + "'>"
                                    secondTr += "<tr><td rowspan=\"" + skuOpts2IdsArray.length + "\">" + skuOpts1NamesArray[j] + " </td>";
                                    secondTr += "<td>" + skuOpts2NamesArray[k] + " </td>"
                                } else {
                                    secondTr += "<input type='hidden' name='sku_opt_" + skuOpts1IdsArray[j] + "_" + skuOpts2IdsArray[k] + "' value='" + skuOpts2IdsArray[k] + "'>"
                                    secondTr += "<tr><td>" + skuOpts2NamesArray[k] + " </td>"
                                }

                                secondTr += othertdHtml + "</tr>";
                            }

                        }
                        if (thirdTr != "") {
                            outputHtml += thirdTr;
                        }
                        if (secondTr != "") {
                            outputHtml += secondTr;
                        }
                    } else {
                        firstTr += "<input type='hidden' name='sku_opt_" + skuOpts1IdsArray[j] + "' value='" + skuOpts1IdsArray[j] + "'>"
                        firstTr += "<tr><td rowspan=\"" + spanCount + "\">" + skuOpts1NamesArray[j] + " </td>";
                        firstTr += othertdHtml + "</tr>";
                        outputHtml += firstTr;

                    }

                }
                tbodyHtml += outputHtml;
                tbodyHtml += "</tbody>";
                var htmlstr = thHtml + tbodyHtml + "<tfoot>\n" +
                        "        <tr>\n" +
                        "            <td colspan=\"7\">\n" +
                        "                <div class=\"batch-opts\">\n" +
                        "                    批量设置：\n" +
                        "                    <span class=\"js-batch-type\" style=\"display: inline;\">\n" +
                        "                    <a class=\"js-batch-price\" href=\"javascript:;\" onclick='batchPrice();'>价格</a>\n" +
                        "                    <a class=\"js-batch-stock\" href=\"javascript:;\"  onclick='batchStock();'>库存</a></span>\n" +
                        "                    <span class=\"js-batch-form form-inline\" style=\"display: none;\">\n" +
                        "                    <input type=\"text\" class=\"js-batch-txt input-mini form-control\" placeholder=\"请输入库存\" maxlength=\"9\">\n" +
                        "                    <a class=\"btn btn-primary btn-xs js-batch-save\" href=\"javascript:;\" onclick='batchSave(this)'>保存</a>\n" +
                        "                    <a class=\"btn btn-primary btn-xs js-batch-cancel\" href=\"javascript:;\" onclick='closeBatch();'>取消</a>\n" +
                        "                    <p class=\"help-desc\"></p></span>\n" +
                        "                </div>\n" +
                        "            </td>\n" +
                        "        </tr>\n" +
                        "        </tfoot>";
//                console.log(htmlstr);
                $('.table-sku-stock').append(htmlstr);
                $('.js-goods-stock').show();
            }

        }

        function batchPrice() {
            $(".js-batch-form").show();
            $(".js-batch-txt").placeholder = '请输入价格';
            $('.js-batch-stock').hide();
        }

        function batchStock() {
            $(".js-batch-form").show();
            $(".js-batch-txt").placeholder = '请输入库存';
            $('.js-batch-price').hide();
        }

        function batchSave() {
            if ($('.js-batch-price').is(":visible")) {
                $('.js-price').val($('.js-batch-txt').val());
            } else {
                $('.js-stock-num').val($('.js-batch-txt').val());
            }
        }

        function closeBatch() {
            $('.js-batch-form ').hide();
            $('.js-batch-stock').show();
            $('.js-batch-price').show();
        }

        /*************************************************上传图片初始化BEGIN*******************************************************/

        var chars = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

        function generateMixed(n) {
            var res = "";
            for (var i = 0; i < n; i++) {
                var id = Math.ceil(Math.random() * 35);
                res += chars[id];
            }
            return res;
        }


        $(function () {

            $(document).on("click", 'input[id^="featrueOptImg_"]', function () {
                $(this).fileupload({
                    url: 'uploadFeatureImage.html',
                    dataType: 'json',
                    acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
                    maxFileSize: 999000,
                    formData: {contentTypeId: 'DOCUMENT', dataResourceTypeId: 'IMAGE_OBJECT', featureId: $(this).attr("id").substr('featrueOptImg_'.length)},
                    disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator.userAgent),
                    done: function (e, data) {
                        console.log(data)
                        if (data != "") {
                            if (data.result) {
                                var contentId = data.result.contentId;
                                var dataresourceId = data.result.dataResourceId;
                                var fileUrl = '/content/control/img?imgId=' + dataresourceId + '${externalKeyParam?if_exists}';
                                $(this).next().find("img").attr("src", fileUrl + "&t=" + generateMixed(6));
                                $(this).next().find("a").attr("href", fileUrl + "&t=" + generateMixed(6));
//                                $(this).parent().parent().addClass("hidden");
                                $(this).next().removeClass("hidden");
                                $(this).prev().addClass("hidden");
//                                $('input[name="mainImg"]').val(fileUrl);
                            } else {
                                alert(data.errorMessage);
                            }
                        }
                        $(this).prev("div").removeClass("_loading");
                    },
                    progressall: function (e, data) {
                        $(this).prev("div").addClass("_loading");
                    }
                });
            });


            $('#mainImg_uploadFileInput').fileupload({
                url: './uploadProductImage.html?productId=' + $('#productId').val() + '&upload_file_type=original',
                dataType: 'json',
                acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
                maxFileSize: 999000,
                disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator.userAgent),
                done: function (e, data) {
                    console.log(data)
                    if (data != "") {
                        if (data.result) {
                            var fileUrl = data.result.result.detail;
                            $('#mainImg_uploadFileInput').parents("li:first").find("img").attr("src", fileUrl + "?t=" + generateMixed(6) + "${externalKeyParam?if_exists}");
                            $('#mainImg_uploadFileInput').parents("li:first").find("a").attr("href", fileUrl + "?t=" + generateMixed(6) + "${externalKeyParam?if_exists}");
                            $('#mainImg_uploadFileInput').parents("div:first").addClass("hidden");
                            $('#mainImg_uploadFileInput').parents("li:first").find("div:last").removeClass("hidden");
                            $('input[name="mainImg"]').val(fileUrl);
                        } else {
                            alert(data.errorMessage);
                        }
                    }
                    $("#mainImg_uploadFileInput").prev("span").removeClass("_loading");
                },
                progressall: function (e, data) {
                    $('#mainImg_uploadFileInput').prev("span").addClass("_loading");
                }
            });

            $('input[id^="skuImg_uploadBtn_"]').fileupload({
                url: './addAdditionalImagesForProductPre',
                dataType: 'json',
                acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
                maxFileSize: 999000,
                formData: {productId: $('#productId').val()},
                disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator.userAgent),
                done: function (e, data) {

                    _skuImg_uploadFileAfter(data, this);

                },
                progressall: function (e, data) {
                    $(this).prev("span").addClass("_loading");
                }
            });
        });

        function uploadThumbMoveDelete1(obj) {
            $(obj).parent().parent().addClass("hidden");
            $(obj).parent().parent().parent().find("div:first").removeClass("hidden");
        }

        function _skuImg_uploadFileAfter(data, obj) {
            var uploadId = obj.id;
            var num = uploadId.substr("skuImg_uploadBtn_".length);
            skuImg_uploadFileAfter(data, obj, num);
        }

        function skuImg_uploadFileAfter(data, obj, seq) {
            if (data != "") {
                if (data.result) {
                    var fileUrl = '/images/products/' + $("#productId").val() + '/additional' + seq + '/' + data.result.files[0].name;
                    $(obj).parents("li:first").find("img").attr("src", fileUrl + "?t=" + generateMixed(6) + "${externalKeyParam?if_exists}");
                    $(obj).parents("li:first").find("a").attr("href", fileUrl + "?t=" + generateMixed(6) + "${externalKeyParam?if_exists}");
                    $(obj).parents("li:first").find("div:first").addClass("hidden");
                    $(obj).parents("li:first").find("div:last").removeClass("hidden");
                    var _imgsku = $(obj).parents("ul:first").attr("imgsku");
                    $('input[name="additionalImg' + seq + '"]').val(fileUrl);
                } else {
                    alert(data.errorMessage);
                }
            }
            $(obj).prev("span").removeClass("_loading");
        }

        function skuImg_init(data, obj, seq) {
            if (data != "") {

                var fileUrl = data;
                $(obj).parents("li:first").find("img").attr("src", fileUrl + "?t=" + generateMixed(6) + "${externalKeyParam?if_exists}");
                $(obj).parents("li:first").find("a").attr("href", fileUrl + "?t=" + generateMixed(6) + "${externalKeyParam?if_exists}");
                $(obj).parents("li:first").find("div:first").addClass("hidden");
                $(obj).parents("li:first").find("div:last").removeClass("hidden");
                var _imgsku = $(obj).parents("ul:first").attr("imgsku");
                $('input[name="additionalImg' + seq + '"]').val(fileUrl);

            }
            $(obj).prev("span").removeClass("_loading");
        }

        //图片左移操作
        function uploadThumbMoveL(obj) {
            //当前图片
            var imgUrl = $(obj).parent().prev("a").find("img").attr("src");
            var imgName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1, imgUrl.length).split("?")[0];
            var _imgsku = $(obj).parents("ul:first").attr("imgsku");
            var num = parseInt($(obj).parents("li:first").attr("num"));
            //左边图片
            var leftHasImg = $(obj).parents("li:first").prev().find("div:last").hasClass("hidden");
            var leftImgUrl = "";
            var leftImgName = "";
            if (!leftHasImg) { //如果左边有图片
                leftImgUrl = $(obj).parents("li:first").prev().find("img").attr("src");
                leftImgName = leftImgUrl.substring(leftImgUrl.lastIndexOf("/") + 1, leftImgUrl.length).split("?")[0];
            }
            //如果左边有图片则将左边的图片和当前图片互换，负责将当前图片清除
            if (!leftHasImg) { //如果左边有图片
                $(obj).parents("li:first").find("a").attr("href", leftImgUrl);
                $(obj).parents("li:first").find("img").attr("src", leftImgUrl);
                $(obj).parents("li:first").find("div:first").addClass("hidden");
                $(obj).parents("li:first").find("div:last").removeClass("hidden");
            } else {
                $(obj).parents("li:first").find("div:first").removeClass("hidden");
                $(obj).parents("li:first").find("div:last").addClass("hidden");
            }

            $(obj).parents("li:first").prev().find("a").attr("href", imgUrl);
            $(obj).parents("li:first").prev().find("img").attr("src", imgUrl);
            $(obj).parents("li:first").prev().find("div:first").addClass("hidden");
            $(obj).parents("li:first").prev().find("div:last").removeClass("hidden");

            $('input[name="additionalImg' + (num - 1) + '"]').val(imgUrl.split("?")[0]);
            $('input[name="additionalImg' + num + '"]').val(leftImgUrl.split("?")[0]);

        }

        //图片右移操作
        function uploadThumbMoveR(obj) {
            var imgUrl = $(obj).parent().prev("a").find("img").attr("src");
            var imgName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1, imgUrl.length).split("?")[0];
            var _imgsku = $(obj).parents("ul:first").attr("imgsku");
            var num = parseInt($(obj).parents("li:first").attr("num"));
            //右边图片
            var rightHasImg = $(obj).parents("li:first").next().find("div:last").hasClass("hidden");
            var rightImgUrl = "";
            var rightImgName = "";
            if (!rightHasImg) { //如果右边有图片
                rightImgUrl = $(obj).parents("li:first").next().find("img").attr("src");
                rightImgName = rightImgUrl.substring(rightImgUrl.lastIndexOf("/") + 1, rightImgUrl.length).split("?")[0];
            }
            //如果右边有图片则将左边的图片和当前图片互换，负责将当前图片清除
            if (!rightHasImg) { //如果右边有图片
                $(obj).parents("li:first").find("a").attr("href", rightImgUrl);
                $(obj).parents("li:first").find("img").attr("src", rightImgUrl);
                $(obj).parents("li:first").find("div:first").addClass("hidden");
                $(obj).parents("li:first").find("div:last").removeClass("hidden");
            } else {
                $(obj).parents("li:first").find("div:first").removeClass("hidden");
                $(obj).parents("li:first").find("div:last").addClass("hidden");
            }

            $(obj).parents("li:first").next().find("a").attr("href", imgUrl);
            $(obj).parents("li:first").next().find("img").attr("src", imgUrl);
            $(obj).parents("li:first").next().find("div:first").addClass("hidden");
            $(obj).parents("li:first").next().find("div:last").removeClass("hidden");
            $('input[name="additionalImg' + (num + 1) + '"]').val(imgUrl.split("?")[0]);
            $('input[name="additionalImg' + num + '"]').val(rightImgUrl.split("?")[0]);

        }

        function uploadThumbMoveDelete(obj,num) {
            $(obj).parents("div:first").addClass("hidden");
            $(obj).parents("li:first").find("div:first").removeClass("hidden");
            if(num>0) {
                $('input[name="additionalImg' + (num) + '"]').val("");
            }else{
                $('input[name="mainImg' + (num) + '"]').val("");
            }
        }


        /*************************************************上传图片初始化END*******************************************************/


        /************************************************* 加载规格BEGIN *******************************************************/

 <#assign  getVariantProductsInfo =  dispatcher.runSync("getVariantProductsInfo", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", product.productId, "userLogin", userLogin))/>
        $(function () {
        <#if getVariantProductsInfo.featureTypes?exists>
        <#--${getVariantProductsInfo}-->
            <#assign allFeatures = getVariantProductsInfo.allFeatures/>

            <#list getVariantProductsInfo.featureTypes as featureTypeId>

                <#assign allFea = allFeatures.get(featureTypeId)/>
                <#assign allFeaName = ''>
                <#list allFea as feature>
                    <#if feature_has_next>
                        <#assign allFeaName = allFeaName+feature+",">
                    <#else>
                        <#assign allFeaName = allFeaName+feature>
                    </#if>

                </#list>
                <#assign  usedFeatrueIds = getVariantProductsInfo.variantFeatureIds[featureTypeId]/>
                <#assign  usedFeatureNames = getVariantProductsInfo.variantFeatureNames[featureTypeId]/>
                 skuGroup${featureTypeId_index+1}Load('${featureTypeId}', '${usedFeatrueIds}', '${usedFeatureNames}', '${allFeaName}')
            </#list>
                 createTableForSku()
        </#if>

            <#if getVariantProductsInfo.variantProducts?exists>
                <#list getVariantProductsInfo.variantProducts as variantProduct>
                    <#assign featureProductId = variantProduct.featureProductId>
                    <#assign optSku = 'sku_opt_'+ featureProductId.replaceAll("\\|","_")>
                    $('input[name^="${optSku}"]').next().find('input[name="sku_price"]').val(${variantProduct.defaultPrice?default(0)?html});
                    $('input[name^="${optSku}"]').next().find('input[name="stock_num"]').val('0');
                     $('input[name^="${optSku}"]').next().find('span').text(${variantProduct.quantityOnHandTotal?default(0)?html});
                    $('input[name^="${optSku}"]').next().find('input[name="code"]').val(${variantProduct.productCode?default('')?html});
                    $('input[name^="${optSku}"]').next().find('input[name="cost_price"]').val(${variantProduct.costPrice?default(0)?html});

                </#list>
            </#if>
            //加载规格
            // skuGroup1Load('10010', '10010,10011', '大,中', '大,中,小')
            // skuGroup2Load('10003', '10007,10006,10008', '绿色,黄色,红色', '绿色,黄色,红色')
            // skuGroup3Load('10002', '10004,10005', '8G,16G', '8G,16G')


            loadVariantProductsInfo();
        })

        function skuGroup1Load(featureTypeId, selValues, selNames, allFeatureName) {
            var skuGroup1 = $('#js-add-sku-group1');
            $(skuGroup1).parent().parent().hide();
            //保证如果是3个group都show不做操作，如果另外两个都开或者其中一个开则显示按钮
            if ($(".sku-sub-group").length < 2) {
                if ($("#sku-sub-group2").length > 0) {
                    $('#sku-group-opts3').show()
                } else {
                    $('#sku-group-opts2').show()
                }
            }
            if ($(".sku-sub-group").length == 2) {
                $('#sku-group-opts1').hide();
                $('#sku-group-opts2').hide();
                $('#sku-group-opts3').hide();
            }
            $.ajax({
                url: 'findFeatureCategoriesByCategory.htm',
                data: 'productCategoryId=' + $('#productCategoryId').val(),
                async: false,
                method: 'post',
                success: function (data) {
                    console.log(data)
                    data = data.productCategoryFeatures;
                    var sel = '';
                    if (data) {
                        sel = '<select class="select2-container js-states form-control js-sku-name" id="sku-group1-opt"   style="width:120px;"   data-placeholder="Select a state"></select>';
                        var skuGroup3 = $('#sku-group3-opt').val();
                        var skuGroup2 = $('#sku-group2-opt').val();
                        var fdata = new Array();
                        var j = 0;
                        for (var i = 0; i < data.length; i++) {
                            var feature = data[i];
                            var obj = {};
                            obj.id = feature.productFeatureTypeId;
                            obj.text = feature.productFeatureTypeName;
                            if (obj.id == featureTypeId) {
                                //判断特征类型被选中
                                obj.selected = true;
                            }
                            if ((skuGroup2 && obj.id == skuGroup2) || (skuGroup3 && obj.id == skuGroup3)) {

                            } else {
                                fdata[j] = obj;
                                j++;
                            }

                        }
                    }

                    var groupCount = $(".sku-sub-group").length + 1;
                    var categoryId = $('#productCategoryId').val();
                    var skuGroup = '<div class="sku-sub-group" id="sku-sub-group1">' +
                            '<input type="hidden" id="suk-sub-group1-sort" name="suk-sub-group1-sort" value="' + groupCount + '"><h3 class="sku-group-title">\n' +
                            sel + '<label for="js-addImg-function" class="addImg-radio">\n' +
                            '<input type="checkbox" id="sku-group1-addImg-function" onclick="skuGroup1AddImgCheck(this)">添加规格图片</label>如无规格可选择,点击<a href="CreateProductCategory?operateType=update&productCategoryLevel=3&productCategoryId=' + categoryId + '&productCategoryIdForUpdate=' + categoryId + '">添加分类下规格</a>\n' +
                            '        <a class="js-remove-sku-group remove-sku-group" id="sku-group1-opt-remove" onclick="removeSkuGroup1(this)">×</a>\n' +
                            '</h3></div>';

                    $('.js-sku-list-container').append(skuGroup);
                    $('#sku-group1-opt').select2({data: fdata, tags: true, placeholder: "请选择一个SKU", language: 'zh_CN'});
                    $('#sku-group1-opt').on("select2:select", function (e) {
                        var skuGroup3 = $('#sku-group3-opt').val();
                        var skuGroup2 = $('#sku-group2-opt').val();
                        var skuGroup1 = $('#sku-group1-opt').val();
                        if (skuGroup2 && skuGroup2 == skuGroup1) {
                            alert('当前SKU已经被选择');
                            $('#sku-group1-opt').val('').trigger('change');
                            return;
                        }
                        if (skuGroup3 && skuGroup3 == skuGroup1) {
                            alert('当前SKU已经被选择');
                            $('#sku-group1-opt').val('').trigger('change');
                            return;
                        }

                        addGroup1Feature(skuGroup1, $('#sku-group1-opt').select2('data')[0].id, $('#sku-group1-opt').select2('data')[0].text);
                    }).on("select2:selecting", function (t) {

                    }).on("select2:opening", function () {

                    });
                    // $('#sku-group1-opt').select2('open');
                    //默认第一次open选中的值
                    if ($('#sku-group1-opt').val()) {
                        loadGroup1Feature(skuGroup1, $('#sku-group1-opt').select2('data')[0].id, $('#sku-group1-opt').select2('data')[0].text, selValues, selNames, allFeatureName);
                    }
                }
            })
        }


        function skuGroup2Load(featureTypeId, selValues, selNames, allFeatureName) {
            var skuGroup2 = $('#js-add-sku-group2');
            $(skuGroup2).parent().parent().hide();
            //保证如果是3个group都show不做操作，如果另外两个都开或者其中一个开则显示按钮
            if ($(".sku-sub-group").length < 2) {
                if ($("#sku-sub-group1").length > 0) {
                    $('#sku-group-opts3').show()
                } else {
                    $('#sku-group-opts1').show()
                }
            }
            if ($(".sku-sub-group").length == 2) {
                $('#sku-group-opts1').hide();
                $('#sku-group-opts2').hide();
                $('#sku-group-opts3').hide();
            }
            $.ajax({
                url: 'findFeatureCategoriesByCategory.htm',
                data: 'productCategoryId=' + $('#productCategoryId').val(),
                method: 'post',
                async: false,
                success: function (data) {
                    console.log(data)
                    data = data.productCategoryFeatures;
                    var sel = '';
                    var fdata = new Array();
                    if (data) {
                        sel = '<select class="select2-container js-states form-control js-sku-name" id="sku-group2-opt"   style="width:120px;"  data-placeholder="Select a state"></select>';
                        var skuGroup3 = $('#sku-group3-opt').val();
                        var skuGroup1 = $('#sku-group1-opt').val();
                        var j = 0;
                        for (var i = 0; i < data.length; i++) {
                            var feature = data[i];
                            var obj = {};
                            obj.id = feature.productFeatureTypeId;
                            obj.text = feature.productFeatureTypeName;
                            if (obj.id == featureTypeId) {
                                //判断特征类型被选中
                                obj.selected = true;
                            }
                            if ((skuGroup1 && obj.id == skuGroup1) || (skuGroup3 && obj.id == skuGroup3)) {

                            } else {
                                fdata[j] = obj;
                                j++;
                            }
                        }
                        console.log(fdata)
                    }
                    var groupCount = $(".sku-sub-group").length + 1;
                    var skuGroup = '<div class="sku-sub-group" id="sku-sub-group2">' +
                            '<input type="hidden" name="suk-sub-group2-sort" id="suk-sub-group2-sort"  value="' + groupCount + '"><h3 class="sku-group-title">\n' +
                            sel + '<label for="js-addImg-function" class="addImg-radio">\n' +
                            '                <input type="checkbox" id="sku-group2-addImg-function" onclick="skuGroup2AddImgCheck(this)">添加规格图片</label>\n' +
                            '        <a class="js-remove-sku-group remove-sku-group" id="sku-group2-opt-remove" onclick="removeSkuGroup2(this)">×</a>\n' +
                            '</h3></div>';

                    $('.js-sku-list-container').append(skuGroup);
                    $('#sku-group2-opt').select2({data: fdata, tags: true, placeholder: "请选择一个SKU", language: 'zh_CN'});
                    $('#sku-group2-opt').on("select2:select", function (e) {
                        var skuGroup3 = $('#sku-group3-opt').val();
                        var skuGroup2 = $('#sku-group2-opt').val();
                        var skuGroup1 = $('#sku-group1-opt').val();
                        if (skuGroup1 && skuGroup1 == skuGroup2) {
                            alert('当前SKU已经被选择');
                            $('#sku-group2-opt').val('').trigger('change');
                            return;
                        }
                        if (skuGroup3 && skuGroup3 == skuGroup2) {
                            alert('当前SKU已经被选择');
                            $('#sku-group2-opt').val('').trigger('change');
                            return;
                        }
                        addGroup2Feature(skuGroup2, $('#sku-group2-opt').select2('data')[0].id, $('#sku-group2-opt').select2('data')[0].text);
                    }).on("select2:selecting", function (t) {

                    }).on("select2:opening", function () {
                    });
                    // $('#sku-group2-opt').select2('open');
                    //默认第一次open选中的值
                    if ($('#sku-group2-opt').val()) {
                        loadGroup2Feature(skuGroup2, $('#sku-group2-opt').select2('data')[0].id, $('#sku-group2-opt').select2('data')[0].text, selValues, selNames, allFeatureName);
                    }
                }
            })
        }


        function skuGroup3Load(featureTypeId, selValues, selNames, allFeatureName) {
            var skuGroup3 = $('#js-add-sku-group3');
            //保证如果是3个group都show不做操作，如果另外两个都开或者其中一个开则显示按钮
            if ($(".sku-sub-group").length < 2) {
                if ($("#sku-sub-group2").length > 0) {
                    $('#sku-group-opts1').show()
                } else {
                    $('#sku-group-opts2').show()
                }
            }
            if ($(".sku-sub-group").length == 2) {
                $('#sku-group-opts1').hide();
                $('#sku-group-opts2').hide();
                $('#sku-group-opts3').hide();
            }
            console.log('xxxxx', $(".sku-sub-group").length);

            $.ajax({
                url: 'findFeatureCategoriesByCategory.htm',
                data: 'productCategoryId=' + $('#productCategoryId').val(),
                method: 'post',
                async: false,
                success: function (data) {
                    console.log(data)
                    data = data.productCategoryFeatures;
                    var sel = '';

                    if (data) {
                        sel = '<select class="select2-container js-states form-control js-sku-name" id="sku-group3-opt"   style="width:120px;"  data-placeholder="Select a state"></select>';
                        var skuGroup2 = $('#sku-group2-opt').val();
                        var skuGroup1 = $('#sku-group1-opt').val();
                        var fdata = new Array();
                        var j = 0;
                        for (var i = 0; i < data.length; i++) {
                            var feature = data[i];
                            var obj = {};
                            obj.id = feature.productFeatureTypeId;
                            obj.text = feature.productFeatureTypeName;
                            if (obj.id == featureTypeId) {
                                //判断特征类型被选中
                                obj.selected = true;
                            }
                            console.log(skuGroup1, skuGroup2)
                            if ((skuGroup1 && obj.id == skuGroup1) || (skuGroup2 && obj.id == skuGroup2)) {
                            } else {
                                fdata[j] = obj;
                                j++;
                            }
                        }
                    }
                    console.log('fadata', fdata);
                    var groupCount = $(".sku-sub-group").length + 1;
                    var skuGroup = '<div class="sku-sub-group" id="sku-sub-group3">' +
                            '<input type="hidden" name="suk-sub-group3-sort"  id="suk-sub-group3-sort" value="' + groupCount + '"><h3 class="sku-group-title">\n' +
                            sel + '<label for="js-addImg-function" class="addImg-radio">\n' +
                            '                <input type="checkbox" id="sku-group3-addImg-function" onclick="skuGroup3AddImgCheck(this)">添加规格图片</label>\n' +
                            '        <a class="js-remove-sku-group remove-sku-group" id="sku-group3-opt-remove" onclick="removeSkuGroup3(this)">×</a>\n' +
                            '</h3></div>';

                    $('.js-sku-list-container').append(skuGroup);
                    $('#sku-group3-opt').select2({data: fdata, tags: true, placeholder: "请选择一个SKU", language: 'zh_CN'});
                    $('#sku-group3-opt').on("select2:select", function (e) {
                        //判断是否已经被选择了
                        var skuGroup3 = $('#sku-group3-opt').val();
                        var skuGroup2 = $('#sku-group2-opt').val();
                        var skuGroup1 = $('#sku-group1-opt').val();
                        if (skuGroup2 && skuGroup2 == skuGroup3) {
                            alert('当前SKU已经被选择');
                            $('#sku-group3-opt').val('').trigger('change');
                            return;
                        }
                        if (skuGroup1 && skuGroup1 == skuGroup3) {
                            alert('当前SKU已经被选择');
                            $('#sku-group3-opt').val('').trigger('change');
                            return;
                        }
//                            console.log($('#sku-group3-opt').select2('data'))
                        addGroup3Feature(skuGroup3, $('#sku-group3-opt').select2('data')[0].id, $('#sku-group3-opt').select2('data')[0].text)
                    }).on("select2:selecting", function (t) {

                    }).on("select2:opening", function () {

                    });
                    // $('#sku-group3-opt').select2('open');
                    //默认第一次open选中的值
                    if ($('#sku-group3-opt').val()) {
                        loadGroup3Feature(skuGroup3, $('#sku-group3-opt').select2('data')[0].id, $('#sku-group3-opt').select2('data')[0].text, selValues, selNames, allFeatureName);
                    }
                }
            })
        }

        // begin group1
        function loadGroup1Feature(obj, value, name, selValues, selNames, allFeatureName) {


            $('#sku-group1-opt-value-hidden-name').remove();
            $('#sku-group1-opt-value-hidden').remove();
            $('#sku-group1-opt-value').remove();

            var disFeature = '';
            var selValArray = selValues.split(",");
            var selNameArray = selNames.split(",");
            for (var s = 0; s < selValArray.length; s++) {
                disFeature += '<div class="sku-atom"><span>' + selNameArray[s] + '</span>\n' +
                        '                    <div class="atom-close close-modal small js-remove-sku-atom" onclick="closeSkuGroup1Atom(this,\'' + selValArray[s] + '\',\'' + selNameArray[s] + '\')">×</div><div class="upload-img-wrap " style="display: none;"><div class="arrow"></div>\n' +
                        '            <div class="js-upload-container" style="position:relative;">\n' +
                        '                    <div class="add-image js-btn-add">+</div>\n' +
                        '                    <input class="filePrew" type="file" id="featrueOptImg_' + selValArray[s] + '" name="imageData" size="3"><div class="ui-uploadGroup-img1 hidden"><a class="img" target="_blank" href=""><img src="" width="90" height="90" alt="img"></a> <span class="ui-uploadGroup-action">\n' +
                        '                    <span class="btn-delete" onclick="uploadThumbMoveDelete1(this);" title="删除">\n' +
                        '                    <i class="fa fa-times-circle-o"></i> </span> </span>\n' +
                        '            </div></div></div></div>';
            }

            $('#sku-sub-group1').append('<input type="hidden" name="sku-group1-opt-value-hidden" id="sku-group1-opt-value-hidden" value="' + value + '"/>' +
                    '<input type="hidden" name="sku-group1-opt-value-hidden-name" id="sku-group1-opt-value-hidden-name" value="' + name + '"/>' +
                    '<input type="hidden" name="group1FeaturesInDb" id="group1FeaturesInDb" value="' + allFeatureName + '">' +
                    '<div class="js-sku-atom-container sku-group-cont" id="sku-group1-opt-value"><div>' +
                    '<div class="js-sku-atom-list sku-atom-list" id="js-sku-group1-atom-list">' +
                    '<input type="hidden" name="sku-group1-atom-feature" id="sku-group1-atom-feature" value="' + selValues + '">' +
                    '<input type="hidden" name="sku-group1-atom-feature-name" id="sku-group1-atom-feature-name" value="' + selNames + '">' +
                    disFeature + '</div>' +
                    '<a onclick="featureGroup1Popver(this,\'' + value + '\')" class="js-add-sku-atom add-sku" id="sku-group1-opt-popver" style="display: inline-block;"  data-container="body" data-toggle="popover" data-placement="bottom"' +
                    ' data-content="<div class=\'ui-popover top-center\'><select multiple id=\'group1FeatureId\' name=\'featureId\' class=\'select2-container select2-container-multi js-select2\' style=\'width: 242px;\'></select><button class=\'btn btn-primary js-save\' style=\'vertical-align: top\' id=\'sku-group1-opt-btn-s\' onclick=\'openCurrentGroup1Opt(this)\'>确定</button>' +
                    '<button class=\'btn btn-white js-cancel\' style=\'vertical-align: top\' id=\'sku-group1-opt-btn-c\' onclick=\'closeCurrentGroup1Opt(this)\'>取消</button></div>" ' +
                    'data-html="true">+添加</a></div>');
        }


        // begin group1
        function loadGroup2Feature(obj, value, name, selValues, selNames, allFeatureName) {


            $('#sku-group2-opt-value-hidden-name').remove();
            $('#sku-group2-opt-value-hidden').remove();
            $('#sku-group2-opt-value').remove();

            var disFeature = '';
            var selValArray = selValues.split(",");
            var selNameArray = selNames.split(",");
            for (var s = 0; s < selValArray.length; s++) {
                disFeature += '<div class="sku-atom"><span>' + selNameArray[s] + '</span>\n' +
                        '                    <div class="atom-close close-modal small js-remove-sku-atom" onclick="closeSkuGroup2Atom(this,\'' + selValArray[s] + '\',\'' + selNameArray[s] + '\')">×</div><div class="upload-img-wrap " style="display: none;"><div class="arrow"></div>\n' +
                        '            <div class="js-upload-container" style="position:relative;">\n' +
                        '                    <div class="add-image js-btn-add">+</div>\n' +
                        '                    <input class="filePrew" type="file" id="featrueOptImg_' + selValArray[s] + '" name="imageData" size="3"><div class="ui-uploadGroup-img2 hidden"><a class="img" target="_blank" href=""><img src="" width="90" height="90" alt="img"></a> <span class="ui-uploadGroup-action">\n' +
                        '                    <span class="btn-delete" onclick="uploadThumbMoveDelete1(this);" title="删除">\n' +
                        '                    <i class="fa fa-times-circle-o"></i> </span> </span>\n' +
                        '            </div></div></div></div>';
            }

            $('#sku-sub-group2').append('<input type="hidden" name="sku-group2-opt-value-hidden" id="sku-group2-opt-value-hidden" value="' + value + '"/>' +
                    '<input type="hidden" name="sku-group2-opt-value-hidden-name" id="sku-group2-opt-value-hidden-name" value="' + name + '"/>' +
                    '<input type="hidden" name="group2FeaturesInDb" id="group2FeaturesInDb" value="' + allFeatureName + '">' +
                    '<div class="js-sku-atom-container sku-group-cont" id="sku-group2-opt-value"><div>' +
                    '<div class="js-sku-atom-list sku-atom-list" id="js-sku-group2-atom-list">' +
                    '<input type="hidden" name="sku-group2-atom-feature" id="sku-group2-atom-feature" value="' + selValues + '">' +
                    '<input type="hidden" name="sku-group2-atom-feature-name" id="sku-group2-atom-feature-name" value="' + selNames + '">' +
                    disFeature + '</div>' +
                    '<a onclick="featureGroup2Popver(this,\'' + value + '\')" class="js-add-sku-atom add-sku" id="sku-group2-opt-popver" style="display: inline-block;"  data-container="body" data-toggle="popover" data-placement="bottom"' +
                    ' data-content="<div class=\'ui-popover top-center\'><select multiple id=\'group2FeatureId\' name=\'featureId\' class=\'select2-container select2-container-multi js-select2\' style=\'width: 242px;\'></select><button class=\'btn btn-primary js-save\' style=\'vertical-align: top\' id=\'sku-group2-opt-btn-s\' onclick=\'openCurrentGroup2Opt(this)\'>确定</button>' +
                    '<button class=\'btn btn-white js-cancel\' style=\'vertical-align: top\' id=\'sku-group2-opt-btn-c\' onclick=\'closeCurrentGroup2Opt(this)\'>取消</button></div>" ' +
                    'data-html="true">+添加</a></div>');
        }

        // begin group1
        function loadGroup3Feature(obj, value, name, selValues, selNames, allFeatureName) {


            $('#sku-group3-opt-value-hidden-name').remove();
            $('#sku-group3-opt-value-hidden').remove();
            $('#sku-group3-opt-value').remove();

            var disFeature = '';
            var selValArray = selValues.split(",");
            var selNameArray = selNames.split(",");
            for (var s = 0; s < selValArray.length; s++) {
                disFeature += '<div class="sku-atom"><span>' + selNameArray[s] + '</span>\n' +
                        '                    <div class="atom-close close-modal small js-remove-sku-atom" onclick="closeSkuGroup3Atom(this,\'' + selValArray[s] + '\',\'' + selNameArray[s] + '\')">×</div><div class="upload-img-wrap " style="display: none;"><div class="arrow"></div>\n' +
                        '            <div class="js-upload-container" style="position:relative;">\n' +
                        '                    <div class="add-image js-btn-add">+</div>\n' +
                        '                    <input class="filePrew" type="file" id="featrueOptImg_' + selValArray[s] + '" name="imageData" size="3"><div class="ui-uploadGroup-img3 hidden"><a class="img" target="_blank" href=""><img src="" width="90" height="90" alt="img"></a> <span class="ui-uploadGroup-action">\n' +
                        '                    <span class="btn-delete" onclick="uploadThumbMoveDelete1(this);" title="删除">\n' +
                        '                    <i class="fa fa-times-circle-o"></i> </span> </span>\n' +
                        '            </div></div></div></div>';
            }

            $('#sku-sub-group3').append('<input type="hidden" name="sku-group3-opt-value-hidden" id="sku-group3-opt-value-hidden" value="' + value + '"/>' +
                    '<input type="hidden" name="sku-group3-opt-value-hidden-name" id="sku-group3-opt-value-hidden-name" value="' + name + '"/>' +
                    '<input type="hidden" name="group3FeaturesInDb" id="group3FeaturesInDb" value="' + allFeatureName + '">' +
                    '<div class="js-sku-atom-container sku-group-cont" id="sku-group3-opt-value"><div>' +
                    '<div class="js-sku-atom-list sku-atom-list" id="js-sku-group3-atom-list">' +
                    '<input type="hidden" name="sku-group3-atom-feature" id="sku-group3-atom-feature" value="' + selValues + '">' +
                    '<input type="hidden" name="sku-group3-atom-feature-name" id="sku-group3-atom-feature-name" value="' + selNames + '">' +
                    disFeature + '</div>' +
                    '<a onclick="featureGroup3Popver(this,\'' + value + '\')" class="js-add-sku-atom add-sku" id="sku-group3-opt-popver" style="display: inline-block;"  data-container="body" data-toggle="popover" data-placement="bottom"' +
                    ' data-content="<div class=\'ui-popover top-center\'><select multiple id=\'group3FeatureId\' name=\'featureId\' class=\'select2-container select2-container-multi js-select2\' style=\'width: 242px;\'></select><button class=\'btn btn-primary js-save\' style=\'vertical-align: top\' id=\'sku-group3-opt-btn-s\' onclick=\'openCurrentGroup3Opt(this)\'>确定</button>' +
                    '<button class=\'btn btn-white js-cancel\' style=\'vertical-align: top\' id=\'sku-group3-opt-btn-c\' onclick=\'closeCurrentGroup3Opt(this)\'>取消</button></div>" ' +
                    'data-html="true">+添加</a></div>');
        }

        function loadVariantProductsInfo() {

        }

        /************************************************* 加载规格END*******************************************************/

    </script>


    <style>


        .input-group {
            left: 8px;
        }

        .error-message {
            font-size: 12px;
            top: 15px;
        }

        .input-xxlarge {
            width: 310px;
        }

        .input-mini {
            width: 60px;
        }

        .input-small {
            width: 200px;
        }

        .batch-opts {
            text-align: left;
        }

        td, th {
            padding: 0;
            text-align: center;
        }

        .popover {
            width: 380px;

            max-width: 500px;
        }

        .ui-popover {
            width: 380px;
        }

        .popover-content {
            width: 380px;
        }

        .select2-dropdown {
            z-index: 9999;
        }

        .close-modal.small {
            top: -8px;
            right: -8px;
            width: 18px;
            height: 18px;
            font-size: 14px;
            line-height: 16px;
            border-radius: 9px;
        }

        .close-modal {
            position: absolute;
            z-index: 2;
            top: -9px;
            right: -9px;
            width: 20px;
            height: 20px;
            font-size: 16px;
            line-height: 18px;
            color: #fff;
            text-align: center;
            cursor: pointer;
            background: rgba(153, 153, 153, 0.6);
            border-radius: 10px;
        }

        .js-upload-container {
            width: 84px;
            height: 84px;
        }

        .form-horizontal.fm-goods-info .form-group {
            margin-bottom: 20px;
            font-size: 12px;
        }

        .info-group-title {
            -webkit-box-flex: 1;
            -webkit-flex: 1;
            -moz-box-flex: 1;
            -ms-flex: 1;
            flex: 1;
            background-color: #f8f8f8;
            border-right: 2px solid #fff;
            border-bottom: 5px solid #fff;
            text-align: center;
            font-size: 14px;
            font-weight: bold;
        }

        .goods-info-group .info-group-title .group-inner {
            padding: 28px 10px 23px;
            text-align: center;
        }

        .goods-info-group .goods-info-group-inner {
            display: -webkit-box;
            display: -webkit-flex;
            display: -moz-box;
            display: -ms-flexbox;
            display: flex;
        }

        .goods-info-group .info-group-cont {
            -webkit-box-flex: 7;
            -webkit-flex: 7;
            -moz-box-flex: 7;
            -ms-flex: 7;
            flex: 7;
            background-color: #f8f8f8;
            border-bottom: 5px solid #fff;

        }

        .goods-info-group .group-inner {
            padding: 23px 20px 10px;
        }

        .normalInput {
            width: 200px;
        }

        .valid-period {
            display: inline-block;
            margin-left: 10px;
        }

        .input-append {
            display: inline-block;
            margin-bottom: 0;
            vertical-align: middle;

        }

        .widget-goods-klass {
            zoom: 1
        }

        .widget-goods-klass:after {
            content: "";
            display: table;
            clear: both
        }

        .widget-goods-klass .widget-goods-klass-item {
            position: relative;
            float: left;
            width: 183px;
            height: 45px;
            margin-right: 15px;
            margin-bottom: 15px;
            border: 1px solid #e5e5e5;
            color: #333;
            background: #F2F2F2;
            font-size: 14px;
            text-align: center;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            cursor: pointer
        }

        .widget-goods-klass .widget-goods-klass-item.current {
            border: 2px solid #f63;
            color: #f63;
            background: #fff
        }

        .widget-goods-klass .widget-goods-klass-item.current .widget-goods-klass-name {
            line-height: 43px
        }

        .widget-goods-klass .widget-goods-klass-item.current .widget-goods-klass-children {
            color: #333
        }

        .widget-goods-klass .widget-goods-klass-item.has-children:hover {
            border: 1px solid #e5e5e5
        }

        .widget-goods-klass .widget-goods-klass-item.has-children:hover .widget-goods-klass-name {
            line-height: 45px
        }

        .widget-goods-klass .widget-goods-klass-item.has-children .widget-goods-klass-name:after {
            content: ' ';
            font-size: 0;
            border-style: solid;
            border-color: #999 transparent transparent;
            border-width: 8px 6px;
            margin-left: 4px
        }

        .widget-goods-klass .widget-goods-klass-item:hover {
            background: #fff
        }

        .widget-goods-klass .widget-goods-klass-item:hover .widget-goods-klass-children {
            display: block
        }

        .widget-goods-klass .widget-goods-klass-name {
            line-height: 45px
        }

        .widget-goods-klass .widget-goods-klass-children {
            display: none;
            position: absolute;
            top: 43px;
            right: -1px;
            width: 579px;
            padding: 15px 20px;
            background: #fff;
            border: 1px solid #e5e5e5;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            z-index: 1
        }

        .widget-goods-klass .widget-goods-klass-children:after {
            content: ' ';
            position: absolute;
            top: -1px;
            right: 0;
            font-size: 0;
            height: 1px;
            width: 181px;
            background: #fff
        }

        .widget-goods-klass .widget-goods-klass-children li {
            float: left;
            width: 89px;
            text-align: left;
            margin: 5px 0
        }

        .widget-fenxiao-sku-change .form-horizontal .control-label {
            width: 90px
        }

        .widget-fenxiao-sku-change .form-horizontal .controls {
            margin-left: 110px
        }

        .widget-fenxiao-sku-change td.min-retail-price {
            border-right: none
        }

        .widget-fenxiao-sku-change td.max-retail-price {
            border-left: none
        }

        .widget-fenxiao-sku-change .manual-valid-error .error-message {
            max-width: 88px
        }

        .widget-fenxiao-sku-change .buttons {
            margin-top: 20px;
            text-align: center
        }

        .widget-fenxiao-sku-change .buttons .ui-btn {
            min-width: 56px
        }

        .widget-fenxiao-sku-change .buttons .ui-btn + .ui-btn {
            margin-left: 20px
        }

        .recharge-modal {
            width: 540px;
            margin-left: -260px;
            background: #fff
        }

        .recharge-modal.fade.in {
            top: 20%
        }

        .recharge-modal .balance-info h2 {
            color: #ed5050;
            font-size: 18px;
            line-height: 50px
        }

        .recharge-modal .balance-info .tips {
            font-size: 14px;
            margin: 15px 0
        }

        .recharge-modal .balance-info .btn {
            width: 120px;
            padding: 10px 0;
            font-size: 14px;
            margin: 0 10px
        }

        .widget-image-preview {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            z-index: 2001;
            text-align: center
        }

        .widget-image-preview .ui-centered-image {
            display: table-cell;
            vertical-align: middle;
            text-align: center;
            width: 60px;
            height: 60px
        }

        .widget-image-preview .preview-close {
            position: fixed;
            border-radius: 40px;
            background: rgba(255, 255, 255, 0.4);
            -webkit-box-shadow: 0 0 8px rgba(0, 0, 0, 0.4);
            box-shadow: 0 0 8px rgba(0, 0, 0, 0.4);
            right: 20px;
            top: 20px;
            width: 40px;
            height: 40px;
            font-size: 34px;
            line-height: 36px;
            cursor: pointer;
            z-index: 2002
        }

        .widget-image-preview .preview-close:hover {
            background: #fff
        }

        .widget-image-preview .preview-actions {
            position: fixed;
            border-radius: 40px;
            background: rgba(255, 255, 255, 0.4);
            -webkit-box-shadow: 0 0 8px rgba(0, 0, 0, 0.4);
            box-shadow: 0 0 8px rgba(0, 0, 0, 0.4);
            bottom: 20px;
            left: 50%;
            margin-left: -96px;
            height: 40px;
            line-height: 40px;
            color: #000;
            z-index: 2002
        }

        .widget-image-preview .preview-actions .preview-action {
            display: inline-block;
            cursor: pointer;
            padding: 0 30px
        }

        .widget-image-preview .preview-actions .preview-action:hover {
            background: #fff
        }

        .widget-image-preview .preview-actions .preview-action:first-child {
            border-radius: 40px 0 0 40px
        }

        .widget-image-preview .preview-actions .preview-action:last-child {
            border-radius: 0 40px 40px 0
        }

        .widget-image-preview-backdrop {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: #000;
            opacity: 0.6;
            z-index: 2000
        }

        .rc-upload .center, .rc-upload .text-center {
            text-align: center
        }

        .rc-upload.inline {
            display: inline
        }

        .rc-upload.zent-dialog-r {
            min-width: 810px !important
        }

        .rc-upload.zent-dialog-r .zent-dialog-r-footer {
            margin-top: 0;
            padding-top: 10px;
            text-align: center
        }

        .rc-upload.zent-dialog-r .zent-dialog-r-title {
            margin-bottom: 10px
        }

        .rc-upload-materials, .rc-upload-container {
            position: relative;
            padding: 0;
            width: 770px;
            height: 480px
        }

        .rc-upload-materials ul, .rc-upload-materials li, .rc-upload-container ul, .rc-upload-container li {
            list-style: none;
            margin: 0;
            padding: 0
        }

        .rc-upload-container {
            overflow-y: auto
        }

        .rc-upload-network-image-region, .rc-upload-local-image-region {
            padding: 30px 10px;
            overflow: hidden
        }

        .rc-upload-network-image-region .rc-upload-title, .rc-upload-local-image-region .rc-upload-title {
            float: left;
            font-size: 14px;
            font-weight: bold;
            text-align: right
        }

        .rc-upload-network-image-region .rc-upload-content, .rc-upload-local-image-region .rc-upload-content {
            background-color: #fff;
            float: left;
            position: relative;
            zoom: 1;
            width: 480px;
            margin-left: 10px
        }

        .rc-upload-network-image-region .rc-upload-content {
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-flex;
            display: -moz-box;
            display: flex
        }

        .rc-upload-network-image-region .zent-btn {
            float: right
        }

        .rc-upload-network-image-region .rc-upload-title {
            line-height: 30px
        }

        .rc-upload-network-image-region .rc-upload-input-append {
            margin-bottom: 10px;
            margin-right: 10px;
            -webkit-box-flex: 1;
            -ms-flex: 1;
            -webkit-flex: 1;
            -moz-box-flex: 1;
            flex: 1
        }

        .rc-upload-network-image-region .rc-upload-image-preview {
            max-width: 200px;
            max-height: 200px
        }

        .rc-upload-network-image-region .rc-upload-image-preview img {
            max-width: 200px;
            max-height: 200px
        }

        .rc-upload-local-image-region .rc-upload-local-tips {
            clear: both;
            padding-top: 20px;
            font-size: 12px
        }

        .rc-upload-local-image-region .upload-local-image-list {
            height: auto
        }

        .rc-upload-local-image-region .image-list .upload-local-image-item {
            position: relative;
            float: left;
            margin: 0 20px 20px 0px;
            width: 80px;
            height: 80px
        }

        .rc-upload-local-image-region .image-list .upload-local-image-item:hover .close-modal {
            display: block
        }

        .rc-upload-local-image-region .image-list .image-box {
            width: 80px;
            height: 80px;
            background-size: cover;
            background-position: 50% 50%
        }

        .rc-upload-local-image-region .image-list .image-progress {
            position: absolute;
            top: 0;
            left: 0;
            bottom: 0;
            right: 0;
            background: rgba(0, 0, 0, 0.6);
            color: #fff;
            text-align: center;
            line-height: 80px;
            font-size: 14px
        }

        .rc-upload-local-image-region .image-list .close-modal {
            display: none;
            position: absolute;
            z-index: 2;
            top: -9px;
            right: -9px;
            width: 20px;
            height: 20px;
            font-size: 16px;
            line-height: 18px;
            color: #fff;
            text-align: center;
            cursor: pointer;
            background: rgba(153, 153, 153, 0.6);
            border-radius: 10px
        }

        .rc-upload-local-image-region .image-list .close-modal.small {
            top: -8px;
            right: -8px;
            width: 18px;
            height: 18px;
            font-size: 14px;
            line-height: 16px;
            border-radius: 9px
        }

        .rc-upload-local-image-region .image-list .close-modal.small:hover {
            color: #fff;
            background: #000
        }

        .rc-upload-add-local-attachment {
            position: relative;
            width: 44px;
            height: 20px;
            line-height: 20px;
            cursor: pointer;
            overflow: hidden
        }

        .rc-upload-add-local-attachment input {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            direction: rtl;
            font-size: 23px;
            opacity: 0;
            cursor: pointer
        }

        .rc-upload-wrapper, .rc-upload-add-local-image-button, .rc-upload-trigger {
            position: relative
        }

        .rc-upload-wrapper input[type=file], .rc-upload-add-local-image-button input[type=file], .rc-upload-trigger input[type=file] {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            width: 100%;
            height: 100%;
            direction: rtl;
            font-size: 23px;
            opacity: 0;
            cursor: pointer;
            opacity: 0
        }

        .rc-upload-add-local-image-button, .rc-upload-trigger {
            display: inline-block;
            width: 80px;
            height: 80px;
            border: 2px dashed #ddd;
            line-height: 71px;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            text-align: center;
            font-size: 36px;
            color: #ddd;
            cursor: pointer;
            overflow: hidden;
            text-decoration: none
        }

        .rc-upload-add-local-image-button {
            margin: 0 20px 20px 0
        }

        .rc-upload-tips {
            line-height: 14px;
            font-size: 12px;
            margin-top: 6px;
            margin-bottom: 0;
            color: #666;
            position: absolute
        }

        .rc-upload-materials {
            overflow: hidden;
            overflow-y: hidden !important
        }

        .rc-upload-materials .category-list-region {
            text-align: right
        }

        .rc-upload-materials .attachment-list-region {
            padding: 10px 0
        }

        .rc-upload-materials .attachment-list-region .zent-btn {
            position: absolute;
            left: 180px;
            bottom: 16px
        }

        .rc-upload-materials .attachment-pagination .zent-pagination__info {
            line-height: 30px
        }

        .rc-upload-materials .attachment-selected {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            border: 2px solid #07d;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box
        }

        .rc-upload-materials .attachment-selected i {
            position: absolute;
            right: 1px;
            bottom: 1px;
            z-index: 2
        }

        .rc-upload-materials .attachment-selected::after {
            position: absolute;
            display: block;
            content: ' ';
            right: 0;
            bottom: 0;
            border: 14px solid #07d;
            border-left-color: transparent;
            border-top-color: transparent;
            z-index: 1
        }

        .rc-upload-materials .zenticon-check {
            font-size: 12px;
            color: #fff
        }

        .rc-upload-materials .image-list {
            height: 400px;
            margin-right: -15px
        }

        .rc-upload-materials .image-list .image-item {
            position: relative;
            float: left;
            width: 100px;
            height: 100px;
            margin-right: 10px;
            margin-bottom: 30px;
            cursor: pointer
        }

        .rc-upload-materials .image-list .image-box {
            background: #ccc;
            width: 100px;
            height: 100px;
            background-size: cover;
            background-position: 50% 50%
        }

        .rc-upload-materials .image-list .image-meta {
            position: absolute;
            width: 100px;
            height: 25px;
            line-height: 25px;
            color: #fff;
            text-align: center;
            background: rgba(0, 0, 0, 0.2);
            bottom: 0
        }

        .rc-upload-materials .image-list .image-title-wrap {
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-flex;
            display: -moz-box;
            display: flex
        }

        .rc-upload-materials .image-list .image-title, .rc-upload-materials .image-list .image-title-ext {
            margin-top: 5px;
            font-size: 12px;
            padding-bottom: 1px
        }

        .rc-upload-materials .image-list .image-title {
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden
        }

        .rc-upload-materials .voice-list {
            height: 400px;
            margin-left: 5px
        }

        .rc-upload-materials .voice-list .voice-item {
            position: relative;
            float: left;
            width: 300px;
            height: 60px;
            margin-left: 28px;
            margin-bottom: 20px;
            padding: 10px;
            border: 1px solid #e5e5e5;
            cursor: pointer;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box
        }

        .rc-upload-materials .voice-list .voice-icon {
            width: 40px;
            height: 40px;
            background: #ddd;
            background-size: 40px 40px;
            background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFAAAABQCAMAAAC5zwKfAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3hpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMDY3IDc5LjE1Nzc0NywgMjAxNS8wMy8zMC0yMzo0MDo0MiAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo3MjAyMDZjOC1iNzBkLTRiMmYtYTdlYS0yZjZmZWU2NjI3NGQiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6Q0NENzEwRDJCMTQxMTFFNUEyMkZBRDYyRTVGRDNDQjgiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6Q0NENzEwRDFCMTQxMTFFNUEyMkZBRDYyRTVGRDNDQjgiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTUgKE1hY2ludG9zaCkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo0NGFlMDZhYS1jMDdhLTQwODctODI1YS05NTE2ODljMjMyOWYiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6NzIwMjA2YzgtYjcwZC00YjJmLWE3ZWEtMmY2ZmVlNjYyNzRkIi8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+D9ZGRgAAAGxQTFRF7e3t/v7+6urq3t7e7u7u8/Pz7Ozs6Ojo6enp+Pj48vLy9vb25ubm+vr6/Pz839/f5OTk4eHh6+vr8fHx9fX19/f3+fn57+/v5+fn5eXl4uLi9PT04+Pj8PDw4ODg/f39+/v7////3d3d////RC54KwAAACR0Uk5T//////////////////////////////////////////////8AWCwNDQAAAeBJREFUeNrsmNuSgyAMhlXqsVrtUXvaNn3/h+wKEdOLokJudhZvJDP1m0B+fkKDF/MTeKAHeqAH/gtgeRWswDKEmBP4ywNI+IBdz4Ow4QIqHkBVMwFbwGfPNeX9QGy4inJEYM4F/NkgMXUFpld8T6c4C5gBFGp0QGLpBMx6RCSHF5RO7AKMJCK8yCBWwLC2B5Y4y7WMbhhlDhnGH6VF6VwdgGJNS7tTwcOlKIP8zjLAfG9WwJKWBaWTmxfRCExgpSZdScZGBncFbC2AifaBhEwzMlclMPNQLFsF2fXjs3n3fQcG6kOl52o0QoQ/lwKRp7JCLR76YQ1kQecDg097VpU4yjFuvmXA1cBDG1DxiQBhGVCfHyi+dgQK81b5NmWdovpBMfrD1m4NNfFMLCFmkE0lx3U4Zmsv7GA06pQYP269lcXWC1B5r9ea2PTJxRxOyukbGHU92NfFyr6wGaSOtTMXeZbBYsE3gsBjB2BKFdm4H1IZHsVPQY9RYQ/ERYOOnqKF+0FfEP2YGro5RemJudBNifYx+2YpgmorfeFBp+/UzkmeyKcTXNRwDj1JeOMB3geLbHmadt2yV4IHmKLA+S4+SIyYr2Z7zstjN30ZXXi97SZ5/j8HD/RAD/yrwLcAAwBxHj2bsIAMsgAAAABJRU5ErkJggg==")
        }

        .rc-upload-materials .voice-list .voice-icon.loading {
            background-image: url("https://b.yzcdn.cn/upload/image/loading@2x.gif")
        }

        .rc-upload-materials .voice-list .voice-icon.playing {
            background-image: url("https://b.yzcdn.cn/upload/image/playing@2x.gif")
        }

        .rc-upload-materials .voice-list .voice-name {
            position: absolute;
            width: 200px;
            height: 18px;
            line-height: 18px;
            top: 12px;
            left: 60px;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden
        }

        .rc-upload-materials .voice-list .voice-createtime {
            position: absolute;
            top: 34px;
            left: 60px;
            color: #999
        }

        .rc-upload-materials .voice-list .voice-duration {
            position: absolute;
            top: 12px;
            right: 10px
        }

        .rc-upload-materials .voice-list .voice-size {
            position: absolute;
            top: 34px;
            right: 10px;
            color: #999
        }

        .rc-upload-materials.attachment-search-result .attachment-list-region {
            width: auto;
            padding-left: 20px
        }

        .rc-upload-materials.attachment-search-result .attachment-pagination {
            width: 820px
        }

        .rc-upload-materials.attachment-search-result .voice-list {
            margin-left: -10px
        }

        .rc-upload-materials.attachment-search-result .voice-list .voice-item {
            width: 266px;
            margin-left: 10px
        }

        .rc-upload-materials.attachment-search-result .voice-list .voice-name {
            width: 160px
        }

        .rc-upload-link {
            color: #3283fa;
            cursor: pointer;
            font-weight: bold
        }

        .rc-upload-split {
            margin: 0 10px
        }

        .rc-video-upload__dialog {
            width: 665px
        }

        .rc-video-upload__dialog .zent-dialog-r-close {
            top: 14px;
            right: 12px;
            z-index: 1
        }

        .rc-video-upload__tabs .zent-tabs-nav-content {
            height: auto
        }

        .rc-video-upload__tabs .zent-tabs-nav {
            background: #fff;
            border-bottom: 1px solid #e8e8e8;
            margin-bottom: 20px
        }

        .rc-video-upload__tabs .zent-tabs-tab-inner {
            padding: 0 0 9px;
            font-size: 14px
        }

        .rc-video-upload__tabs .zent-tab-tabpanel {
            min-height: 510px;
            position: relative
        }

        .rc-video-upload__tabs.rc-video-upload__tabs--onlyone .zent-tabs-nav-ink-bar {
            display: none
        }

        .rc-video-upload__form {
            margin-bottom: 0;
            height: 510px;
            position: relative
        }

        .rc-video-upload__form .zent-form__control-label {
            width: 70px
        }

        .rc-video-upload__form .zent-form__help-desc {
            line-height: 16px
        }

        .rc-video-upload__form-input .zent-input-wrapper {
            width: 260px
        }

        .rc-video-upload__form-input .zent-select {
            width: 260px
        }

        .rc-video-upload__choose {
            position: relative;
            display: inline-block;
            width: 100px;
            height: 60px;
            border: 2px dashed #ddd;
            line-height: 51px;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            text-align: center;
            font-size: 36px;
            color: #ddd;
            cursor: pointer;
            overflow: hidden;
            text-decoration: none
        }

        .rc-video-upload__choose input[type=file] {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            width: 100%;
            height: 100%;
            direction: rtl;
            font-size: 23px;
            opacity: 0;
            cursor: pointer;
            opacity: 0
        }

        .rc-video-upload__progress {
            position: relative;
            margin-bottom: 20px
        }

        .rc-video-upload__progress-item {
            height: 40px;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            background: #f8f8f8
        }

        .rc-video-upload__progress-item:hover .rc-video-upload__progress-item-close {
            display: block
        }

        .rc-video-upload__progress-item-progress {
            position: absolute;
            left: 0;
            top: 0;
            right: 0;
            bottom: 0;
            width: 0;
            background: rgba(51, 136, 255, 0.2);
            -webkit-transition: width .25s;
            -moz-transition: width .25s;
            transition: width .25s
        }

        .rc-video-upload__progress-item-detail {
            font-size: 12px;
            padding: 10px 15px;
            line-height: 20px;
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-flex;
            display: -moz-box;
            display: flex;
            -webkit-box-pack: justify;
            -ms-flex-pack: justify;
            -webkit-justify-content: space-between;
            -moz-box-pack: justify;
            justify-content: space-between
        }

        .rc-video-upload__progress-item-detail-name {
            display: inline-block;
            max-width: 210px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap
        }

        .rc-video-upload__progress-item-close {
            display: none;
            position: absolute;
            z-index: 2;
            color: #fff;
            text-align: center;
            cursor: pointer;
            background: rgba(102, 102, 102, 0.6);
            width: 18px;
            height: 18px;
            top: -8px;
            right: -8px;
            font-size: 14px;
            line-height: 16px;
            border-radius: 9px
        }

        .rc-video-upload__publish {
            position: absolute;
            bottom: 0;
            right: 0
        }

        .rc-video-upload__publish .zent-checkbox-wrap {
            margin-right: 5px
        }

        .rc-video-upload__publish .zent-btn {
            width: 104px
        }

        .rc-video-upload__materials-empty {
            text-align: center;
            padding-top: 180px
        }

        .rc-video-upload__materials-empty a {
            cursor: pointer
        }

        .rc-video-upload__materials-filter {
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-flex;
            display: -moz-box;
            display: flex;
            -webkit-box-pack: end;
            -ms-flex-pack: end;
            -webkit-justify-content: flex-end;
            -moz-box-pack: end;
            justify-content: flex-end
        }

        .rc-video-upload__materials-filter .zent-select {
            font-size: 0;
            width: 148px
        }

        .rc-video-upload__materials-filter .zent-select .zent-select-text {
            font-size: 12px
        }

        .rc-video-upload__materials-filter .zent-search-input {
            display: inline-block;
            width: 148px
        }

        .rc-video-upload__materials-list {
            margin: 5px 0 20px -20px
        }

        .rc-video-upload__materials-list .zent-radio-wrap {
            margin: 0
        }

        .rc-video-upload__materials-list .zent-radio-wrap .zent-radio {
            display: none
        }

        .rc-video-upload__materials-list .zent-radio-wrap .zent-radio + span {
            margin: 0
        }

        .rc-video-upload__materials-list .zent-radio-checked .rc-video-upload__materials-item {
            border-color: #38f
        }

        .rc-video-upload__materials-list .zent-radio-checked .rc-video-upload__materials-item::after {
            position: absolute;
            bottom: 0;
            right: 0;
            display: inline-block;
            content: '';
            width: 24px;
            height: 24px;
            background: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAMAAABg3Am1AAAAilBMVEUAAACA//8zif8zif8zif8zif80if8zif8ziP8ziP8zif8zif8ziP8ziP8ziP8zif8ziP8zif9Vqv8ziP////82if/8/f/B2/+31f+71/+v0P84i/++2f+y0/+qzv+lyv+cxf+Btf/Y6P/F3f+TwP+Ku/92r/9uq/9Ilf8/j//n8f/h7f/M4f97sv+Oix2wAAAAE3RSTlMAAqGXjYeB+vXonJLw7ODk3doDPOVQ4wAAAP1JREFUSMeVy8dywkAQhGHjgHOQe5AQ0XLAOPD+r8cuW9CH2aqp7lMfvv9M2+tY86PzRvRoRI9G9HiRfA4knwPR40nwSjC6gxYUzyD2DESPZ8EziLwWXMAFiset4BkInkHgGcQ+Dui14B61PQbeB4Kf9JsUCH5h7cAg9r2ZrRiEfpr8DHio+Uv4Fc8g8svkl2AQ+dnRMwj9FAwiv8p+Uv6N4Bl4P3Tzk18n3xfvgiuUfZr9/zjPgJ6BvZfiLd0FPQP6vK9cbOl9QM/ib3vwXfLctfdlQ2v2uy7eB/TcLhX0LnAe2OTiY45qMEZl3y09A/pK0dEzcD6Y6qF6qB57Sw1W5fCHc8gAAAAASUVORK5CYII=") no-repeat;
            background-size: 24px auto
        }

        .rc-video-upload__materials-list::after {
            display: table;
            content: '';
            clear: both
        }

        .rc-video-upload__materials-item {
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            width: 302px;
            height: 60px;
            border: 1px solid #e5e5e5;
            padding: 8px 10px;
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-flex;
            display: -moz-box;
            display: flex;
            -webkit-box-align: center;
            -ms-flex-align: center;
            -webkit-align-items: center;
            -moz-box-align: center;
            align-items: center;
            -webkit-box-pack: justify;
            -ms-flex-pack: justify;
            -webkit-justify-content: space-between;
            -moz-box-pack: justify;
            justify-content: space-between;
            margin: 15px 0 0 20px;
            float: left;
            position: relative;
            cursor: pointer
        }

        .rc-video-upload__materials-item-image-cover {
            width: 40px;
            height: 40px;
            background-size: cover;
            background-position: center center;
            background-repeat: no-repeat
        }

        .rc-video-upload__materials-item-empty-cover {
            width: 40px;
            height: 40px;
            background: #333
        }

        .rc-video-upload__materials-item-details {
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-flex;
            display: -moz-box;
            display: flex;
            width: 230px;
            height: 100%;
            -webkit-box-orient: vertical;
            -webkit-box-direction: normal;
            -ms-flex-direction: column;
            -webkit-flex-direction: column;
            -moz-box-orient: vertical;
            -moz-box-direction: normal;
            flex-direction: column;
            -webkit-box-pack: justify;
            -ms-flex-pack: justify;
            -webkit-justify-content: space-between;
            -moz-box-pack: justify;
            justify-content: space-between;
            line-height: 16px
        }

        .rc-video-upload__materials-item-details .rc-video-upload__item-top {
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-flex;
            display: -moz-box;
            display: flex;
            -webkit-box-pack: justify;
            -ms-flex-pack: justify;
            -webkit-justify-content: space-between;
            -moz-box-pack: justify;
            justify-content: space-between;
            font-size: 14px
        }

        .rc-video-upload__materials-item-details .rc-video-upload__item-top-name {
            display: inline-block;
            max-width: 150px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            word-wrap: break-word;
            word-break: break-all
        }

        .rc-video-upload__materials-item-details .rc-video-upload__item-sub {
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-flex;
            display: -moz-box;
            display: flex;
            -webkit-box-pack: justify;
            -ms-flex-pack: justify;
            -webkit-justify-content: space-between;
            -moz-box-pack: justify;
            justify-content: space-between;
            font-size: 12px;
            color: #999
        }

        .rc-video-upload__materials-item-details .rc-video-upload__item-sub span {
            display: inline-block;
            max-width: 150px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            word-wrap: break-word;
            word-break: break-all
        }

        .rc-video-upload__cover .rc-video-upload__cover-upload {
            display: inline
        }

        .rc-video-upload__cover-img {
            position: relative;
            display: inline-block;
            margin-right: 5px;
            vertical-align: bottom;
            width: 60px;
            height: 60px;
            background-size: cover;
            background-position: center center;
            background-repeat: no-repeat
        }

        .rc-video-upload__cover-trigger {
            position: relative;
            padding-top: 5px;
            width: 52px;
            display: inline-block;
            cursor: pointer
        }

        .rc-video-upload__cover-trigger span {
            font-size: 0
        }

        .rc-video-upload__cover-trigger span::after {
            content: 'ä¸Šä¼ ';
            line-height: 18px;
            font-size: 12px;
            color: #38f
        }

        .rc-video-upload__cover-trigger input {
            width: 50px;
            height: 18px;
            position: absolute;
            top: 5px;
            left: 0;
            cursor: pointer;
            opacity: 0;
            font-size: 100px
        }

        .rc-video-upload__cover-trigger.has-cover span::after {
            content: 'ä¿®æ”¹'
        }

        .ui-nav-tab > li {
            width: 33.33%
        }

        .not-allowed {
            cursor: not-allowed
        }

        .mat5 {
            margin-top: 5px
        }

        .mat10 {
            margin-top: 10px
        }

        .radio > input[type="text"], .radio .input-append, .radio .input-prepend, .checkbox > input[type="text"], .checkbox .input-append, .checkbox .input-prepend {
            margin-top: -4px
        }

        .radio.inline.has-input {
            padding-top: 0;
            line-height: 30px
        }

        .radio.inline.has-input input[type="radio"] {
            margin-top: 8px
        }

        .circle-help {
            display: inline-block;
            width: 16px;
            height: 16px;
            margin: 0 5px;
            border-radius: 8px;
            text-align: center;
            font-size: 12px;
            line-height: 16px;
            color: #fff;
            background-color: #bbb;
            vertical-align: middle
        }

        .circle-help:hover {
            color: #fff
        }

        .checkbox.inline + .circle-help {
            margin-top: 5px
        }

        .valid-period {
            display: inline-block;
            margin-left: 10px
        }

        .important-msg {
            margin-top: 10px;
            border: 1px solid #f60;
            padding: 6px 8px;
            background: #fcf8e3;
            color: #333
        }

        .goods-info-group strong {
            font-weight: bold
        }

        .goods-info-group p {
            margin-bottom: 10px
        }

        .goods-info-group input[type="text"] + .radio, .goods-info-group input[type="text"] + .checkbox {
            padding-top: 0
        }

        .goods-info-group .goods-info-group-inner {
            display: -webkit-box;
            display: -webkit-flex;
            display: -moz-box;
            display: -ms-flexbox;
            display: flex
        }

        .goods-info-group .info-group-title {
            -webkit-box-flex: 1;
            -webkit-flex: 1;
            -moz-box-flex: 1;
            -ms-flex: 1;
            flex: 1;
            background-color: #f8f8f8;
            border-right: 2px solid #fff;
            border-bottom: 5px solid #fff;
            text-align: center;
            font-size: 14px;
            font-weight: bold
        }

        .goods-info-group .info-group-title .group-inner {
            padding: 28px 10px 23px
        }

        .goods-info-group .info-group-cont {
            -webkit-box-flex: 7;
            -webkit-flex: 7;
            -moz-box-flex: 7;
            -ms-flex: 7;
            flex: 7;
            background-color: #f8f8f8;
            border-bottom: 5px solid #fff;
            font-size: 12px
        }

        .goods-info-group .group-inner {
            padding: 23px 20px 10px
        }

        .form-horizontal.fm-goods-info {
            color: #333
        }

        .form-horizontal.fm-goods-info .control-group {
            margin-bottom: 20px
        }

        .form-horizontal.fm-goods-info .control-label {
            font-size: 12px;
            width: 130px;
            cursor: default
        }

        .form-horizontal.fm-goods-info .controls {
            margin-left: 15px;
            font-size: 12px
        }

        .form-horizontal.fm-goods-info .controls + .controls {
            margin-top: 15px
        }

        .form-horizontal.fm-goods-info .control-action {
            font-size: 12px
        }

        .form-horizontal.fm-goods-info label, .form-horizontal.fm-goods-info input, .form-horizontal.fm-goods-info button, .form-horizontal.fm-goods-info select, .form-horizontal.fm-goods-info textarea {
            font-size: 12px
        }

        .form-horizontal.fm-goods-info p {
            color: #999
        }

        .form-horizontal.fm-goods-info .static-value {
            font-size: 12px;
            vertical-align: middle
        }

        .form-horizontal.fm-goods-info .opt-link {
            font-size: 12px;
            display: inline-block;
            padding-top: 5px;
            margin-bottom: 0;
            vertical-align: middle
        }

        .form-horizontal.fm-goods-info .class-block {
            position: relative;
            background-color: #f8f8f8;
            margin-bottom: 10px;
            padding: 20px
        }

        .form-horizontal.fm-goods-info .class-block .controls {
            margin-left: 0;
            padding-left: 16px;
            zoom: 1
        }

        .form-horizontal.fm-goods-info .class-block .controls:after {
            content: "";
            display: table;
            clear: both
        }

        .form-horizontal.fm-goods-info .class-block .select2-container {
            margin-right: 10px
        }

        .form-horizontal.fm-goods-info .recent-used-class {
            position: absolute;
            top: 20px;
            right: 20px
        }

        .class-search {
            position: relative;
            border-bottom: 1px solid #e5e5e5
        }

        .class-search input, .class-search button {
            height: 32px;
            border: none;
            -webkit-box-shadow: none;
            box-shadow: none;
            border-radius: 0px
        }

        .class-search .txt-search {
            width: 100%;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box
        }

        .class-search .btn {
            position: absolute;
            top: 0px;
            right: 0px;
            border-left: 1px solid #e5e5e5
        }

        .class-group {
            background-color: #fff;
            width: 24%;
            margin-right: 1%;
            float: left;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            border: 1px solid #e5e5e5;
            height: 285px
        }

        .class-group .class-group-inner {
            background-color: #fff
        }

        .class-group ul {
            height: 250px;
            overflow-x: hidden;
            overflow-y: auto
        }

        .class-group ul > li {
            position: relative;
            z-index: 1
        }

        .class-group ul > li:hover {
            background-color: #efefef
        }

        .class-group ul > li.active {
            background-color: #e5e5e5
        }

        .class-group ul a {
            display: block;
            cursor: pointer;
            height: 28px;
            line-height: 28px;
            color: #333;
            padding: 0 20px 0 15px;
            display: block;
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
            word-wrap: normal
        }

        .class-group ul a:hover {
            text-decoration: none
        }

        .class-group ul span {
            position: absolute;
            top: 0px;
            right: 0px;
            height: 28px;
            width: 20px;
            line-height: 28px;
            text-align: center;
            color: #999
        }

        .class-path-name {
            clear: both;
            padding-top: 10px
        }

        .attributes {
            padding: 10px 10px 0 10px;
            background-color: #fff;
            border: 1px solid #e5e5e5
        }

        .attributes .attributes-list {
            zoom: 1
        }

        .attributes .attributes-list:after {
            content: "";
            display: table;
            clear: both
        }

        .attributes .attributes-list > li {
            display: inline-block;
            width: 50%;
            vertical-align: top;
            margin: 0 0 10px
        }

        .attributes .attributes-list > li .control-group {
            margin: 0
        }

        .attributes .attributes-list > li .control-label {
            width: 90px
        }

        .attributes .attributes-list > li .controls {
            margin-left: 100px
        }

        .sku-group {
            background-color: #fff;
            padding: 10px 10px 10px 10px;
            border: 1px solid #e5e5e5
        }

        .sku-group h3.sku-group-title {
            position: relative;
            padding: 7px 0 7px 10px;
            margin: 0;
            color: #666;
            background-color: #f8f8f8;
            font-size: 12px;
            line-height: 16px;
            font-weight: normal
        }

        .sku-group h4 {
            font-size: 12px;
            font-weight: bold;
            margin: 0
        }

        .sku-group .addImg-radio {
            display: inline-block;
            margin: 3px 0px 0 30px
        }

        .sku-group .addImg-radio input {
            vertical-align: 0px;
            margin-right: 6px
        }

        .sku-group .sku-sub-group:hover .remove-sku-group {
            display: block
        }

        .sku-group .remove-sku-group {
            display: none;
            position: absolute;
            top: 12px;
            right: 10px;
            color: #fff;
            text-align: center;
            cursor: pointer;
            width: 18px;
            height: 18px;
            font-size: 14px;
            line-height: 16px;
            background: rgba(153, 153, 153, 0.6);
            border-radius: 10px;
            text-indent: 0
        }

        .sku-group .sku-group-cont {
            padding: 10px;
            margin-bottom: 10px
        }

        .sku-group .sku-list ul {
            zoom: 1
        }

        .sku-group .sku-list ul:after {
            content: "";
            display: table;
            clear: both
        }

        .sku-group .sku-list ul > li {
            float: left;
            width: 20%;
            text-align: left
        }

        .sku-group .c-color-0 {
            color: #333
        }

        .sku-group .c-color-1 {
            color: #999
        }

        .sku-group .c-color-2 {
            color: #656565
        }

        .sku-group .c-color-3 {
            color: #ac6100
        }

        .sku-group .c-color-4 {
            color: #da0000
        }

        .sku-group .c-color-5 {
            color: #fe6b00
        }

        .sku-group .c-color-6 {
            color: #cdcb00
        }

        .sku-group .c-color-7 {
            color: #bf00cc
        }

        .sku-group .c-color-8 {
            color: #0036d2
        }

        .sku-group .c-color-9 {
            color: #1ea100
        }

        table.table-sku-color {
            width: 100%;
            font-size: 12px;
            text-align: left
        }

        table.table-sku-color td {
            width: 20%;
            text-align: left;
            padding-bottom: 15px;
            vertical-align: top
        }

        .sku-stock {
            padding: 0px 10px 20px;
            background-color: #fff;
            border: 1px solid #e5e5e5
        }

        table.table-sku-stock {
            width: 100%;
            background-color: #fff;
            text-align: left
        }

        table.table-sku-stock th {
            padding: 10px 8px;
            font-weight: normal
        }

        table.table-sku-stock th.th-price {
            width: 100px
        }

        table.table-sku-stock th.th-stock {
            width: 75px
        }

        table.table-sku-stock th.th-code {
            width: 90px
        }

        table.table-sku-stock td {
            border: 1px solid #e5e5e5;
            padding: 8px
        }

        table.table-sku-stock td:first-of-type {
            border-left: none
        }

        table.table-sku-stock td:last-of-type {
            border-right: none
        }

        table.table-sku-stock .error-message {
            display: none;
            color: #b94a48
        }

        .manual-valid-error {
            color: #b94a48
        }

        .manual-valid-error input {
            border-color: #b94a48;
            -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.075);
            box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.075)
        }

        .manual-valid-error .error-message {
            display: block !important
        }

        .sku-atom-list {
            display: inline-block
        }

        .sku-atom {
            border: 1px solid #AAA;
            padding: 4px;
            display: inline-block;
            margin-right: 20px;
            margin-bottom: 5px;
            margin-top: 10px;
            width: 90px;
            vertical-align: middle;
            text-align: center;
            position: relative;
            border-radius: 4px;
            cursor: pointer;
        }

        .sku-atom span {
            display: block;

            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap
        }

        .sku-atom .close-modal {
            display: none
        }

        .sku-atom:hover .atom-close {
            display: block
        }

        .sku-atom.active {
            margin-bottom: 100px
        }

        .sku-atom .upload-img-wrap {
            position: absolute;
            top: 35px;
            left: 0;
            padding: 2px;
            width: 90px;
            background: #fff;
            border-radius: 4px;
            border: 1px solid #dcdcdc;
        }

        .sku-atom .upload-img-wrap img {
            width: 84px;
            height: 84px;
            cursor: pointer
        }

        .sku-atom .upload-img-wrap .add-image {
            width: 84px;
            height: 84px;
            line-height: 84px;
            text-align: center;
            background: #fff;
            font-size: 30px;
            color: #e5e5e5;
            cursor: pointer
        }

        .sku-atom .upload-img-wrap .arrow {
            position: absolute;
            width: 0;
            height: 0;
            top: -5px;
            left: 44%;
            border-style: solid;
            border-color: transparent;
            border-left: 5px solid transparent;
            border-right: 5px solid transparent;
            border-bottom: 5px solid #000000
        }

        .sku-atom .upload-img-wrap .arrow::after {
            position: absolute;
            display: block;
            width: 0;
            height: 0;
            border-color: transparent;
            border-style: solid;
            top: -10px;
            margin-left: -10px;
            border-bottom-color: #fff;
            border-top-width: 0;
            border-width: 10px;
            content: ""
        }

        .sku-atom .upload-img-wrap .img-edit {
            cursor: pointer;
            display: none;
            position: absolute;
            bottom: 0px;
            left: 0px;
            width: 100%;
            color: #fff;
            opacity: 0.5;
            background: #000
        }

        .sku-atom--disabled {
            background: #eee
        }

        .add-sku {
            display: inline-block;
            padding: 0 5px;
            margin: 12px 5px 0 5px;
            vertical-align: top
        }

        .message-container .message-item {
            margin-bottom: 8px
        }

        .message-container .message-item:last-of-type {
            margin-bottom: 0
        }

        .message-container input, .message-container select {
            margin-right: 5px
        }

        .message-container input, .message-container select, .message-container .remove-message {
            vertical-align: middle;
            display: inline-block
        }

        .message-container .remove-message {
            padding-top: 5px;
            margin: 0 0 0 10px
        }

        .message-container select + .message-input.hide + .message-input {
            margin-left: 0
        }

        .goods-edit-area .app-actions .form-actions {
            padding: 10px;
            border-top: none;
            background-color: #ffc;
            margin: 0
        }

        .goods-details-block {
            padding: 30px 0;
            background: #e5e5e5;
            text-align: center;
            color: #666
        }

        .goods-details-block h4 {
            margin: 0;
            font-size: 16px;
            line-height: 24px
        }

        .goods-details-block p {
            margin: 0;
            font-size: 14px;
            line-height: 24px
        }

        .goods-detail-modal {
            width: 400px;
            margin-left: -200px
        }

        .goods-detail-modal.fade.in {
            top: 25%
        }

        .goods-detail-modal .goods-detail-modal-body {
            padding: 20px 40px;
            font-size: 13px
        }

        .goods-detail-modal .goods-detail-modal-body p {
            margin-bottom: 12px
        }

        .goods-detail-modal .goods-detail-modal-body label {
            min-width: 90px
        }

        .goods-detail-modal .paipai-type-error {
            color: #b94a48
        }

        .app-add-field .app-add-disabled {
            background-color: rgba(0, 0, 0, 0.1);
            width: 100%;
            height: 100%;
            position: absolute;
            top: 0;
            left: 0;
            z-index: 10
        }

        .app-sidebar .goods-sidebar-sub-title {
            margin-bottom: 18px;
            font-size: 12px
        }

        .app-sidebar .goods-sidebar-sub-title .input-sub-title {
            clear: both;
            width: 100%;
            margin: 10px 0 5px 0;
            resize: none;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box
        }

        .app-sidebar .goods-sidebar-goods-template {
            margin-bottom: 18px;
            font-size: 12px;
            margin-top: -200px
        }

        .ui-tag ul.select2-choices {
            background-image: none
        }

        .ui-tag ul.select2-choices li.select2-search-choice {
            background-color: #ddd;
            border: none;
            top: 3px
        }

        .ui-tag .select2-search-choice-close {
            top: 3px
        }

        .js-purchase-right-setting .checkbox {
            margin-top: 10px
        }

        .radio input.etd-type {
            margin-top: 7px
        }

        body.theme-new-ui .widget-goods-klass {
            width: 718px;
            margin: 0 auto
        }

        body.theme-new-ui .widget-goods-klass .widget-goods-klass-item {
            width: 164px
        }

        .valid-period-dialog .tips-content {
            margin: 0 0 10px 16px;
            font-size: 12px;
            line-height: 1.5;
            list-style: disc
        }

        .valid-period-dialog .tips-label {
            margin-left: 16px;
            font-size: 12px
        }

        .valid-period-dialog .tips-label input {
            margin: 0
        }

        .video-edit-wrap a {
            cursor: pointer
        }

        .video-edit-wrap .add-video {
            display: inline-block;
            width: 50px;
            height: 50px;
            line-height: 50px;
            border: 1px solid #ddd;
            background: #fff;
            position: relative;
            text-align: center
        }

        .video-edit-wrap .video-cover {
            display: inline-block;
            width: 54px;
            height: 54px;
            line-height: 54px;
            position: relative;
            text-align: center;
            cursor: pointer;
            margin-right: 5px;
            vertical-align: bottom
        }

        .video-edit-wrap .video-cover:hover .video-close {
            display: block
        }

        .video-edit-wrap .video-cover .image-cover {
            width: 54px;
            height: 54px;
            background-size: cover;
            background-position: center center;
            background-repeat: no-repeat
        }

        .video-edit-wrap .video-cover .empty-cover {
            width: 100%;
            height: 100%;
            background: #333
        }

        .video-edit-wrap .video-cover.is-success::after {
            content: '';
            position: absolute;
            width: 20px;
            height: 20px;
            left: 50%;
            top: 50%;
            margin-top: -10px;
            margin-left: -10px;
            background: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAMAAAC7IEhfAAAAbFBMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAACampoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACQkJA1NTX8/Pz5+fny8vLQ0NAgICAMDAzp6enf39/U1NS+vr66urqpqal0dHRubm5nZ2dUVFQ+Pj7////5EUOHAAAAI3RSTlNmAF1gM1UEn0RMLQ0JQiEgmXT69ezHbmjh1cu4tamLiYZ/d6mGIYQAAAEaSURBVDjLndWLboMwDAVQEzsZEAJt1617v/j/f5xbVBVzQxv1SgiJHBklShyqTGJbN57ZN3Ub7cgcduJpFi9dFgZhHXXOsUZfRMQSEPY6wo5mcaxPv4RyYZaKgaEmUobRj3WYQeNAXqAYB1LOsLcOZT/B4KxD6cIJCjFdDZMcYcdQEEpypxAKZksq9LbgYZsr6SuKZLPZfRJGWWsL0sM4vkJRpywhHB83ABM1CDVPzxYq88QANftvO21lCKd8GMjrcLe10K/Al1/765XJjO+H5WRSDu6/cHlagNPiwIJHogXU5cZE2BQ/b3+5TVG+zco3bvlRKD9c1XDruA6lDaC0pRQ3qXvanmbIN9LhjtZc2Ozh+kjT9ZGW18c/q1MPu+e1nD8AAAAASUVORK5CYII=") no-repeat;
            background-size: 20px auto
        }

        .video-edit-wrap .video-cover .video-close {
            display: none;
            position: absolute;
            top: -6px;
            right: -6px;
            width: 16px;
            height: 16px;
            line-height: 16px;
            color: #fff;
            text-align: center;
            background: rgba(0, 0, 0, 0.3);
            border-radius: 50%;
            z-index: 2
        }

        .video-edit-wrap .video-cover .video-close:hover {
            background: rgba(0, 0, 0, 0.6)
        }

        .video-edit-wrap .note {
            margin-top: 5px
        }

        .video-edit-wrap .note i {
            display: inline-block;
            width: 14px;
            height: 14px;
            background: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAMAAABF0y+mAAAAP1BMVEUAAAD/RET/RUX/RUX/Rkb/RET/RET/RET/RUX/SUn/Rkb/SEj/RET/RET/RUX/RET/RET/RUX/RET/RUX/RES0VLHWAAAAFHRSTlMA2ceCVPPsvz8xIRm15NSwpJR0ZMZStogAAACpSURBVCjPfVNbDsMgDHN4lFLaro/c/6yTtgniauC/yEnshICK7JPEKMlnPBFEKyQQtTgluKVx86QPTHPl9A9+7FLrqPbbuem9Slmb7sdnyy5AaVEAID1SgKzKbRsyvHbhkfpkgihZCGpFo4lu4DZhJPICLiKFHJA/IUMncFpDlHoAh21ES1gBXoIVdYCzkjTZDuw0tU3egK21GT726EzGBzY+zfFRD7/DG972KpvlGtprAAAAAElFTkSuQmCC") no-repeat;
            background-size: 14px auto;
            vertical-align: middle;
            margin-right: 5px
        }

        .video-edit-wrap .note span {
            display: inline-block;
            vertical-align: middle
        }

        .zent-select-popup {
            z-index: 2000;
            opacity: 1;
            height: auto
        }

        .module-goods-list li .add-goods, .module-goods-list li .add, .app-image-list li .add-goods, .app-image-list li .add {
            display: inline-block;
            width: 100%;
            height: 100%;
            line-height: 50px;
            text-align: center;
            cursor: pointer;
        }

        ol, ul {
            list-style: none;
        }

        .module-goods-list li, .app-image-list li {
            float: left;
            margin: 0 10px 10px 0;
            display: block;
            width: 50px;
            height: 50px;
            border: 1px solid #ddd;
            background-color: #fff;
            position: relative;
        }

        /*.label {
    background-color: #d1dade;
    color: #5e5e5e;
    font-family: 'Open Sans';
    font-size: 10px;
    font-weight: 600;
    padding: 3px 8px;
    text-shadow: none;
}*/

        .display-text {
            padding-top: 7px;

        }

        hr {
            margin-top: 10px;
            margin-bottom: 10px;
            border: 0;
            border-top: 1px solid #eee;
        }

        .panel-body {
            border: #eee 1px solid;
        }

        .ui-front {
            z-index: 9999;

        }

        .panel-body {
            border: #eee 1px solid;
            border-top: 0px;
        }

        .modal-custom {
            text-align: center;
        }

        .modal-custom .modal-dialog {
            display: inline-block;
            width: auto;
        }

        .modal-custom .modal-content {
            -webkit-border-radius: 0;
            -moz-border-radius: 0;
            border-radius: 0
        }

        .cate_set {
            padding: 0 20px 20px 20px;
        }

        .cate_set .cate_set_item {
            margin-right: 20px;
            background: #fff;
        }

        .cate_set_cont {
            border: 1px solid #F7F7F7;
        }

        .cate_search {
            position: relative;
            border-bottom: 1px solid #f7f7f7;
        }

        .cate_search .cate_search_box {
            width: 100%;
            height: 15px;
            line-height: 15px;
            padding: 15px 10px;
            border: 0;
        }

        .cate_search a {
            position: absolute;
            right: 0;
            top: 0;
            display: block;
            width: 50px;
            height: 45px;
            line-height: 30px;
            font-size: 18px;
            color: #555;
            text-align: center;
        }

        .cate_set_list {
            height: 500px;
            overflow-y: scroll;
        }

        .cate_set_list .cate_item {
            position: relative;
            height: 50px;
            line-height: 50px;
            cursor: pointer;
        }

        .cate_set_list .cate_item:hover {
            background: #E3E4E8;
        }

        .cate_set_list .cate_item h4 {
            margin: 0;
            padding: 0;
            text-indent: 10px;
            line-height: 50px;
            font-size: 16px;
        }

        .cate_set_list .cate_item .btns {
            display: none;
            position: absolute;
            right: 60px;
            top: 0;
        }

        .cate_set_list .cate_item .btns a {
            color: #A4AEB9;
            margin-left: 10px;
        }

        .cate_set_list .cate_item .c_btns {
            display: none;
            position: absolute;
            right: 5px;
            top: 15px;
            text-align: center;
            line-height: 25px;
            border-radius: 50%;
            font-size: 16px;
        }

        .cate_set_list .cate_item .c_btns a {
            margin-left: 3px;
            color: #999;
        }

        .cate_set_list .active {
            background: #f0f0f0;
        }

        .cate_set_list .active .btns, .cate_set_list .cate_item:hover .btns {
            display: block;
        }

        .cate_set_list .active .c_btns, .cate_set_list .cate_item:hover .c_btns {
            display: block;
        }

        .cate_set_list .add_cart_btn {
            display: block;
            height: 50px;
            line-height: 50px;
            text-indent: 10px;
            font-size: 16px;
            border: 1px dashed #dfdfdf;
        }

        .cate_set_list .add_cart_btn:hover {
            background: #f9f9f9;
        }

        ._loading {
            background: #ffffff url(/images/comImage/loading-white.gif) no-repeat 50% !important;;
        }

        /*ui-uploadGroup*/
        .ui-uploadGroup {
            overflow: hidden;
        }

        .ui-uploadGroup .items {
            margin-left: -10px;
        }

        .ui-uploadGroup .item {
            float: left;
            width: 110px;
            margin-left: 10px;
        }

        .ui-uploadGroup-img {
            position: relative;
            width: 108px;
            height: 108px;
            border: 1px solid #cfd1d7;
            overflow: hidden;
            cursor: pointer;
        }

        .ui-uploadGroup-img .file,
        .ui-uploadGroup-img .filePrew {
            width: 100%;
            height: 100%;
            cursor: pointer;
        }

        .ui-uploadGroup-img .file span {
            display: block;
            width: 100%;
            height: 100%;
            background: #ffffff url(/images/comImage/icon-uploadPlus.png) no-repeat 50%;
            font-size: 0;
            color: #ffffff;
        }

        .ui-uploadGroup-img .progress {
            position: absolute;
            left: 0;
            top: 50%;
            width: 100%;
            height: 14px;
            margin-top: -7px;
        }

        .ui-uploadGroup-img .progress-outer,
        .ui-uploadGroup-img .progress-inner,
        .ui-uploadGroup-img .progress-text {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(38, 53, 72, .5);
            background-color: #263548 \9;
            filter: alpha(opacity=50);
        }

        .ui-uploadGroup-img .progress-inner {
            width: 1%;
        }

        .ui-uploadGroup-img .progress-text {
            z-index: 9;
            line-height: 14px;
            text-align: center;
            color: #fff;
            font-family: Arial, Sans-serif;
            font-size: 12px;
            background-color: transparent;
        }

        .ui-uploadGroup-action {
            position: absolute;
            left: 0;
            bottom: 0;
            width: 100%;
            height: 20px;
            line-height: 20px;
            text-align: center;
            background-color: rgba(173, 173, 173, .7);
            background-color: #adadad \9;
            color: #ffffff;
        }

        .ui-uploadGroup .item:hover .file span {
            background-color: #fafafa;
        }

        .ui-uploadGroup .item-loading .ui-uploadGroup-img {
            background: #ffffff url(/images/comImage/loading-white.gif) no-repeat 50%;
        }

        .ui-uploadGroup-name {
            text-align: center;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
        }

        .ui-uploadGroup .item-loaded {
            position: relative;
        }

        .ui-uploadGroup .item-loaded .ui-uploadGroup-img img {
            display: block;
            width: 100%;
            height: 100%;
        }

        .ui-uploadGroup .item-loaded .ui-uploadGroup-deleteItem {
            position: absolute;
            right: 0;
            top: 0;
            display: none;
            overflow: hidden;
            line-height: 0;
        }

        .ui-uploadGroup .item-loaded:hover .ui-uploadGroup-deleteItem {
            display: block;
        }

        .ui-uploadGroup .item-loaded .ui-uploadGroup-action {
            background-color: rgba(0, 0, 0, .7);
            background-color: #333 \9;
        }

        .ui-uploadGroup-s .items {
            margin: -10px 0 0 -10px;
        }

        .ui-uploadGroup-s .item {
            width: 66px;
            height: 66px;
            margin: 10px 0 0 10px;
        }

        .ui-uploadGroup-s .ui-uploadGroup-img {
            width: 64px;
            height: 64px;
        }

        .ui-uploadGroup-fullBanner .item {
            height: 150px;
        }

        .ui-uploadGroup-fullBanner .ui-uploadGroup-img {
            width: 480px;
            height: 148px;
        }

        .file,
        .filePrew {
            display: inline-block;
            width: 82px;
            height: 24px;
            cursor: pointer;
        }

        .file {
            position: relative;
            border: 1px solid #bbbbbb;
            background: none repeat scroll 0 0 #f8f8f8;
            cursor: pointer;
            text-align: center;
            vertical-align: middle;
            overflow: hidden;
        }

        .file span {
            display: block;
            line-height: 24px;
        }

        .file em {
            display: inline-block;
            width: 12px;
            height: 12px;
            margin-right: 3px;
            background-position: 0 -56px;
            vertical-align: -2px;
            overflow: hidden;
            font-size: 0;
        }

        .file:hover {
            border-color: #5874d8;
        }

        .file:hover em {
            background-position: -14px -56px;
        }

        .filePrew {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            font-size: 100px; /* å¢žå¤§ä¸åŒæµè§ˆå™¨çš„å¯ç‚¹å‡»åŒºåŸŸ */
            opacity: 0; /* å®žçŽ°çš„å…³é”®ç‚¹ */
            filter: alpha(opacity=0); /* å…¼å®¹IE */
        }

        .btng-ui-m {
            display: inline-block;
            width: 84px;
            height: 32px;
            border: none;
            background-color: #d8def2;
            line-height: 32px;
            text-align: center;
            font-size: 12px;
            color: #525d71;
            border-radius: 3px;
        }


    </style>