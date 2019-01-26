
<!-- Screenlet to add cart to shopping list. The shopping lists are presented in a dropdown box. -->

<#if (shoppingLists?exists) && (shoppingCartSize > 0)>
  <div class="screenlet">
    <div class="screenlet-title-bar">
        <div class="h3">${uiLabelMap.OrderAddOrderToShoppingList}</div>
    </div>
    <div class="screenlet-body">
      <table border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
            <#--<form method="post" name="addBulkToShoppingList" action="<@ofbizUrl>addBulkToShoppingList</@ofbizUrl>" style='margin: 0;'>-->
              <#assign index = 0/>
              <#list shoppingCart.items() as cartLine>
                <#if (cartLine.getProductId()?exists) && !cartLine.getIsPromo()>
                  <input type="hidden" name="selectedItem" value="${index}"/>
                </#if>
                <#assign index = index + 1/>
              </#list>
              <table border="0">
                <tr>
                  <td>
                    <div>
                    <select name='shoppingListId'>
                      <#list shoppingLists as shoppingList>
                        <option value='${shoppingList.shoppingListId}'>${shoppingList.getString("listName")}</option>
                      </#list>
                        <option value="">---</option>
                        <option value="">${uiLabelMap.OrderNewShoppingList}</option>
                    </select>
                    <input type="submit" class="smallSubmit" value="${uiLabelMap.OrderAddToShoppingList}"/>
                    </div>
                  </td>
                </tr>
              </table>
            <#--</form>-->
          </td>
        </tr>
      </table>
    </div>
  </div>
</#if>
