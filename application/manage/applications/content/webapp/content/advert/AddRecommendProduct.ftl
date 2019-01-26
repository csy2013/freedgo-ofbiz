<form method="post" action="<@ofbizUrl>createRecommendProduct</@ofbizUrl>" class="form-horizontal" name="addRecommendProductForm">
    <input type="hidden" name="siteId" value="${parameters.siteId?if_exists}"/>


    <div class="form-group">
        <label class="control-label col-md-3">${uiLabelMap.ProductProductId}</label>
        <div class="col-md-5 ">
        <@htmlTemplate.lookupField formName="addRecommendProductForm" name="productId" id="productId" fieldFormName="LookupProduct"/>
        </div>
    </div>

</form>