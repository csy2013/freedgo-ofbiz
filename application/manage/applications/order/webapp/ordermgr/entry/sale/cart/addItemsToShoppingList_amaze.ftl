<#if (shoppingLists?exists) && (shoppingCartSize > 0)>
  <div class="am-panel am-panel-default">
    <div class="am-panel-hd am-cf">
        ${uiLabelMap.OrderAddOrderToShoppingList}
    </div>
    <div class="am-panel-bd am-collapse am-in">
        <div class="am-g">

            <#--<form method="post" name="addBulkToShoppingList" action="<@ofbizUrl>addBulkToShoppingList</@ofbizUrl>" style='margin: 0;'>-->
              <#assign index = 0/>
              <#list shoppingCart.items() as cartLine>
                <#if (cartLine.getProductId()?exists) && !cartLine.getIsPromo()>
                  <input type="hidden" name="selectedItem" value="${index}"/>
                </#if>
                <#assign index = index + 1/>
              </#list>

                  <div class="am-u-md-2">

                    <select name='shoppingListId'>
                      <#list shoppingLists as shoppingList>
                        <option value='${shoppingList.shoppingListId}'>${shoppingList.getString("listName")}</option>
                      </#list>
                        <option value="">---</option>
                        <option value="">${uiLabelMap.OrderNewShoppingList}</option>
                    </select>
                    <div class="am-u-md-10 am-u-end">
                    <input type="submit" class="smallSubmit" value="${uiLabelMap.OrderAddToShoppingList}"/>
                    </div>
                  </div>

            <#--</form>-->

            </div>
      </div>
    </div>
</#if>
