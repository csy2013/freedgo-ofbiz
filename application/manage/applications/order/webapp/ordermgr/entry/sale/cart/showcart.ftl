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

<script language="JavaScript" type="text/javascript">
   /* function showQohAtp() {
        document.qohAtpForm.productId.value = document.salesentryform.add_product_id.value;
        document.qohAtpForm.submit();
    }*/
    function quicklookupGiftCertificate() {
        window.location='AddGiftCertificate';
    }
</script>
  <#assign target="getProductInventoryAvailable">
<div class="screenlet">
    <div class="screenlet-body">
        <div>
          <#if quantityOnHandTotal?exists && availableToPromiseTotal?exists && (productId)?exists>
            <ul>
              <li>
                <label>${uiLabelMap.ProductQuantityOnHand}</label>: ${quantityOnHandTotal}
              </li>
              <li>
                <label>${uiLabelMap.ProductAvailableToPromise}</label>: ${availableToPromiseTotal}
              </li>
            </ul>
          </#if>
        </div>
      <table border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
               <#--why?-->
         <#--   <form name="qohAtpForm" method="post" action="<@ofbizUrl>${target}</@ofbizUrl>">
              <fieldset>
                <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
                <input type="hidden" name="productId"/>
                <input type="hidden" id="ownerPartyId" name="ownerPartyId" value="${shoppingCart.getBillToCustomerPartyId()?if_exists}" />
              </fieldset>
            </form>-->
            <#--<form method="post" action="<@ofbizUrl>addsaleitem</@ofbizUrl>" name="quickaddform" style="margin: 0;">-->
              <table border="0">
                <tr>
                  <td align="right"><div>${uiLabelMap.ProductProductId} :</div></td>
                  <td>
                    <span class='tabletext'>
                      <#assign fieldFormName="LookupProduct">
                      <@htmlTemplate.lookupField formName="salesentryform" name="add_product_id" id="add_product_id" fieldFormName="${fieldFormName}"/>
                      <a href="javascript:quicklookup(document.salesentryform.add_product_id)" class="buttontext">${uiLabelMap.OrderQuickLookup}</a>
                    </span>
                  </td>
                </tr>
                <tr>
                  <td align="right"><div>${uiLabelMap.OrderQuantity} :</div></td>
                  <td><input type="text" size="6" name="quantity" value=""/></td>
                </tr>
                <tr>
                  <td align="right"><div>${uiLabelMap.OrderDesiredDeliveryDate} :</div></td>
                  <td>
                    <div>
                      <#if useAsDefaultDesiredDeliveryDate?exists> 
                        <#assign value = defaultDesiredDeliveryDate>
                      </#if>
                      <@htmlTemplate.renderDateTimeField name="itemDesiredDeliveryDate" value="${value!''}" className="" alert="" title="Format: yyyy-MM-dd HH:mm:ss.SSS" size="25" maxlength="30" id="item1" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
                      <input type="checkbox" name="useAsDefaultDesiredDeliveryDate" value="true"<#if useAsDefaultDesiredDeliveryDate?exists> checked="checked"</#if>/>
                      ${uiLabelMap.OrderUseDefaultDesiredDeliveryDate}
                    </div>
                  </td>
                </tr>
                <input type="hidden" name="shipAfterDate"/>
                  <input type="hidden" name="shipBeforeDate"/>
                <tr>
                  <td align="right"><div>${uiLabelMap.CommonComment} :</div></td>
                  <td>
                    <div>
                      <input type="text" size="25" name="itemComment" value="${defaultComment?if_exists}" />
                      <input type="checkbox" name="useAsDefaultComment" value="true" <#if useAsDefaultComment?exists>checked="checked"</#if> />
                      ${uiLabelMap.OrderUseDefaultComment}
                    </div>
                  </td>
                </tr>
                <tr>
                  <td></td>
                  <td><input type="button" onclick="document.salesentryform.submit();" class="smallSubmit" value="${uiLabelMap.OrderAddToOrder}"/></td>
                </tr>
              </table>
            <#--</form>-->
          </td>
        </tr>
      </table>
    </div>
</div>

<script language="JavaScript" type="text/javascript">
  document.salesentryform.add_product_id.focus();
</script>

<!-- Internal cart info: productStoreId=${shoppingCart.getProductStoreId()?if_exists} locale=${shoppingCart.getLocale()?if_exists} currencyUom=${shoppingCart.getCurrency()?if_exists} userLoginId=${(shoppingCart.getUserLogin().getString("userLoginId"))?if_exists} autoUserLogin=${(shoppingCart.getAutoUserLogin().getString("userLoginId"))?if_exists} -->
