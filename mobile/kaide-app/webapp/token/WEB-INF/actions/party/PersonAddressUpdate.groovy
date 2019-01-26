import org.ofbiz.base.util.UtilHttp
import org.ofbiz.base.util.UtilMisc
import org.ofbiz.entity.GenericValue
import org.ofbiz.entity.util.EntityUtil
import org.ofbiz.product.store.ProductStoreWorker

Map<String,Object> paramMap = UtilHttp.getCombinedMap(request);
resultData = [:]
userLogin = request.getAttribute("userLogin")

productStoreId = ProductStoreWorker.getProductStoreId(request);
if (userLogin) {

    contactMechId = paramMap.get('contactMechId');
    partyId = userLogin.partyId
    address1 = paramMap.get('address')
    city = paramMap.get('city')==null?"_NA_":paramMap.get('city')
    countyGeoId = paramMap.get('countyGeoId')==null?"_NA_":paramMap.get('countyGeoId')
    mobilePhone = paramMap.get('tel')
    postalCode = paramMap.get('postalCode')==null?"_NA_":paramMap.get('postalCode')
    stateProvinceGeoId = paramMap.get('stateProvinceGeoId');
    attnName = paramMap.get('name')

    is_def = paramMap.get('is_def')

    serviceIn = [contactMechId: contactMechId, address1: address1, city: city, partyId: partyId,
                 countyGeoId: countyGeoId, mobilePhone: mobilePhone, attnName: attnName,
                 postalCode: postalCode, stateProvinceGeoId: stateProvinceGeoId, userLogin: userLogin];

    result = dispatcher.runSync("updatePartyPostalAddress", serviceIn);

    println "result = $result"

    /*//设置为默认货物目的地地址
    //先检查是否已经是货物目的地址
    contactMechId = result.contactMechId;
    result1 = dispatcher.runSync("queryCustomMechByPurposeType",[loginId:loginId,contactMechPurposeTypeId:'SHIPPING_LOCATION',userLogin: userLogin]);
    println "result = $result1"

    for(partyContactMech in result1){

        partyContactMechId = partyContactMech.get(contactMechId);
        if(partyContactMechId.equals(contactMechId)){
            hasShippingLocation = true;
            break;
        }
    }*/


    //设置为默认货物目的地地址
//    先检查是否已经是货物目的地址
    contactMechId = result.contactMechId;
    partyContactMechs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId)), true);
    GenericValue partyContactMech = EntityUtil.getFirst(partyContactMechs);
    boolean hasShippingLocation = false;
    if(partyContactMech!=null) {
        partyContactMechPurposes = EntityUtil.filterByDate(partyContactMech.getRelated("PartyContactMechPurpose"), true);
        if (partyContactMechPurposes) {
            for (partyContactMechPurpose in partyContactMechPurposes) {
                if (partyContactMechPurpose.contactMechPurposeTypeId.equals('SHIPPING_LOCATION')) {
                    hasShippingLocation = true;
                    break;
                }
            }
        }
    }

    if (!hasShippingLocation) {
        contactMechPurposeTypeId = 'SHIPPING_LOCATION';
        serviceIn = [partyId: partyId, contactMechId: result.contactMechId, contactMechPurposeTypeId: contactMechPurposeTypeId, userLogin: userLogin]
        result = dispatcher.runSync("createPartyContactMechPurpose", serviceIn);
    }


    //设置为默认货物目的地地址
    if(is_def) {
        serviceIn = [productStoreId: productStoreId, defaultShipAddr: contactMechId, partyId: partyId, userLogin: userLogin];
        println "serviceIn = $serviceIn"
        result = dispatcher.runSync("setPartyProfileDefaults", serviceIn);
        resultData.put("result", result)
    }

    resultData.put("result", result);
    request.setAttribute("resultData",resultData);
    return "success";

}