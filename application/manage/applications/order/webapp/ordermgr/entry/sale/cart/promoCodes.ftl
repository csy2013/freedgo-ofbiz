<div class="screenlet">
    <div class="screenlet-title-bar">
        <div class="h3">&nbsp;${uiLabelMap.OrderPromotionCouponCodes}</div>
    </div>
    <div class="screenlet-body">
        <div>
            <#--<form method="post" action="<@ofbizUrl>addpromocode<#if requestAttributes._CURRENT_VIEW_?has_content>/${requestAttributes._CURRENT_VIEW_}</#if></@ofbizUrl>" name="addpromocodeform" style="margin: 0;">-->
                <input type="text" size="15" name="productPromoCodeId" value="" />
                <input type="submit" class="smallSubmit" value="${uiLabelMap.OrderAddCode}" />
                <#assign productPromoCodeIds = (shoppingCart.getProductPromoCodesEntered())?if_exists>
                <#if productPromoCodeIds?has_content>
                ${uiLabelMap.OrderEnteredPromoCodes}:
                    <#list productPromoCodeIds as productPromoCodeId>
                    ${productPromoCodeId}
                    </#list>
                </#if>
            <#--</form>-->
        </div>
    </div>
</div>