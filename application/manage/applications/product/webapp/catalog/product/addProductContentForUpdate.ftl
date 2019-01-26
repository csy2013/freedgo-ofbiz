<div class="tab-pane fade" id="default-tab-3">
<#if product?exists>
    <#assign nowTimestampString = Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() >
    <#assign uploadType = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("content","content.image.upload.type")>
    <#if uploadType == "qiniu">
        <#assign imageServerUrl = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("content","img.qiniu.domain")>
    <#elseif uploadType == "local">
        <#assign imageServerUrl = "">
    </#if>

    <script language="JavaScript" type="text/javascript">
        function insertNowTimestamp(field) {
            eval('document.productForm.' + field + '.value="${nowTimestampString}";');
        }

        function insertImageName(type, nameValue) {
            eval('document.productForm.' + type + 'ImageUrl.value=nameValue;');
        }

    </script>

    <#if fileType?has_content>
        <h3>${uiLabelMap.ProductResultOfImageUpload}</h3>
        <#if !(clientFileName?has_content)>
            <div>${uiLabelMap.ProductNoFileSpecifiedForUpload}.</div>
        <#else>
            <div>${uiLabelMap.ProductTheFileOnYourComputer}: <b>${clientFileName?if_exists}</b></div>
            <div>${uiLabelMap.ProductServerFileName}: <b>${fileNameToUse?if_exists}</b></div>
            <div>${uiLabelMap.ProductServerDirectory}: <b>${imageServerPath?if_exists}</b></div>
            <div>${uiLabelMap.ProductTheUrlOfYourUploadedFile}: <b><a
                    href="<@ofbizContentUrl>${imageUrl?if_exists}</@ofbizContentUrl>">${imageUrl?if_exists}</a></b></div>
        </#if>
        <br/>
    </#if>

    <div class="form-group">
        <label class="control-label col-md-2">${uiLabelMap.ProductLongDescription}</label>

        <div class="col-md-9">
            <@htmlTemplate.renderTextareaField name="longDescription" className="dojo-ResizableTextArea" alert="false"
            value="${(product.pcDetails)?if_exists}" cols="60" rows="15" id="longDescription" readonly="" visualEditorEnable="true" language="zh_CN" buttons="maxi" />

        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-2">${uiLabelMap.ProductLongWapDescription}</label>

        <div class="col-md-9">
            <@htmlTemplate.renderTextareaField name="longWapDescription" className="dojo-ResizableTextArea" alert="false"
            value="${(product.mobileDetails)?if_exists}" cols="100" rows="15" id="longWapDescription" readonly="" visualEditorEnable="true" language="zh_CN" buttons="maxi" />

        </div>
    </div>

    <#--<script language="JavaScript" type="text/javascript">
        function setUploadUrl(newUrl) {
            var toExec = 'document.imageUploadForm.action="' + newUrl + '";';
            eval(toExec);
        }
    </script>-->

</#if>
    <div class="app-actions">
        <div class="text-center">
            <button type="button" class="btn btn-lg btn-primary" onclick="submitForm();">确认添加商品</button>
            <button type="button" class="btn btn-lg btn-default" onclick="window.location.href='findAllGoods.htm?menuId=3&amp;menuParentId=7&amp;myselfId=18'">返回列表</button>
        </div>
    </div>
</div>
</div>
</form>
<script type="text/javascript">
    function submitForm() {
        var productCategoryId = $('#productCategoryId').val();
        if (productCategoryId === '') {
            tipLayer({msg: "请选择商品对应的分类设置（在第三级分类下选择）！"});
            return;
        }
        var productCode = $('input[name="productCode"]').val();
        if (productCode === '') {
            tipLayer({msg: "请输入商品编码！"});
            $('#productCode').focus();
            return;
        }
        var brandId = $('#productBrandId').val();
        var productTypeId = $('input[name="productTypeId"]:checked').val();
        var valid_period = $('input[name="valid_period"]:checked').val();
        var salesDiscontinuationDate = '';
        var releaseDate = '';
        if (valid_period === '1') {
            salesDiscontinuationDate = $('input[name="salesDiscontinuationDate"]').val();
            releaseDate = $('input[name="releaseDate"]').val();
        }
        var autoCreateKeywords = $("input[name='autoCreateKeywords']:checked").val();
        var productName = $("input[name='productName']").val();
        var returnable = $("input[name='returnable']:checked").val();
        var includeInPromotions = $("input[name='includeInPromotions']:checked").val();
        var shipment = {};
        if ($("input[name='delivery']").val() === '1') {
            shipment.shipmentPrice = $("input[name='shipmentPrice']").val();
        } else {
            shipment.shipmentId = $("input[name='shipmentId']").val();
        }
        var defaultPrice = $("input[name='DEFAULT_PRICE']").val();
        if (!defaultPrice) {
            tipLayer({msg: "请输入商品价格"});
        }
        var averageCost = $("input[name='AVERAGE_COST']").val();
        var listPrice = $("input[name='LIST_PRICE']").val();


        var longDescription = CKEDITOR.instances.longDescription.getData();
        var longWapDescription = CKEDITOR.instances.longWapDescription.getData();

        var mainImg = $("input[name='mainImg']").val();
        if (!mainImg) {
            tipLayer({msg: "请上传主图"});
        }
        var additionalImg1 = $("input[name='additionalImg1']").val();
        var additionalImg2 = $("input[name='additionalImg2']").val();
        var additionalImg3 = $("input[name='additionalImg3']").val();
        var additionalImg4 = $("input[name='additionalImg4']").val();
        var additionalImg5 = $("input[name='additionalImg5']").val();
        var additionalImg6 = $("input[name='additionalImg6']").val();
        var productFacilityAmount = $("input[name='productFacilityAmount']").val();
        var isRecommendHomePage =  $("input[name='isRecommendHomePage']:checked").val();
        var isListShow =  $("input[name='isListShow']:checked").val();
        var weight =  $("input[name='weight']").val();
        var volume =  $("input[name='volume']").val();
        var seoKeyword =  $("textarea[name='seoKeyword']").val();
        if(!productFacilityAmount){
            tipLayer({msg: "请输入商品总库存"});
        }
        var data = {
            productCategoryId: productCategoryId,
            productCode: productCode,
            brandId: brandId,
            productTypeId: productTypeId,
            salesDiscontinuationDate: salesDiscontinuationDate,
            releaseDate: releaseDate,
            includeInPromotions: includeInPromotions,
            autoCreateKeywords: autoCreateKeywords,
            productName: productName,
            returnable: returnable,
            shipment: shipment,
            longDescription: longDescription,
            longWapDescription: longWapDescription,
            productFacilityAmount:productFacilityAmount,
            valid_period:valid_period,
            isRecommendHomePage: isRecommendHomePage,
            isListShow:isListShow,
            volume:volume,
            weight:weight,
            seoKeyword:seoKeyword

        };
        data.defaultPrice = defaultPrice;
        data.averageCost =  averageCost;
        data.listPrice = listPrice;
        data.productImage = mainImg;
        data.additionalImg1 = additionalImg1;
        data.additionalImg2 = additionalImg2;
        data.additionalImg3 = additionalImg3;
        data.additionalImg4 = additionalImg4;
        data.additionalImg5 = additionalImg5;
        data.additionalImg6 = additionalImg6;
        var featureIds = [];
        var featurePrices = [];
        var featureStockNum = [];
        var featureCode = [];
        var featureCostPrice = [];
        $('input[name^="sku_opt_"]').each(function (index) {
            var optName = $(this).attr('name');
            var skuPrice = $(this).next().find('input[name="sku_price"]').val();
            var stockNum = $(this).next().find('input[name="stock_num"]').val();
            var code = $(this).next().find('input[name="code"]').val();
            var costPrice = $(this).next().find('input[name="cost_price"]').val();
            featureIds[index] = optName;
            featurePrices[index] = skuPrice;
            featureStockNum[index] = stockNum;
            featureCode[index] = code;
            featureCostPrice[index] = costPrice;

        });
        data.featureIds = featureIds.toString();
        data.featurePrices = featurePrices.toString();
        data.featureStockNum = featureStockNum.toString();
        data.featureCode = featureCode.toString();
        data.featureCostPrice = featureCostPrice.toString();
        data.productId = $('#productId').val();
        console.log(data)
        $.ajax({
            url: 'updateProduct.htm',
            data: data,
            method: 'post',
            success: function (data) {
                console.log(data);
                if(data.product){
                    tipLayer({msg: "产品创建成功！"});
                    $('#tipLayer').on('hide.bs.modal', function () {
                        window.location.href = '<@ofbizUrl>FindProduct</@ofbizUrl>';
                    })

                }
            }
        });


    }

</script>
