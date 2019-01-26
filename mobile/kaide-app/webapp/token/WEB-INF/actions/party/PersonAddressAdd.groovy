import org.ofbiz.base.util.UtilHttp
import org.ofbiz.product.store.ProductStoreWorker
//gender: "male", name: "常胜永", tel: "13705188361", is_def: false, address: "江苏省南京市鼓楼区中山北路45"}
resultData = [:]
userLogin = request.getAttribute("userLogin")
String productStoreId = ProductStoreWorker.getProductStoreId(request);
Map<String,Object> paramMap = UtilHttp.getCombinedMap(request);
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

    serviceIn = [ address1: address1, city: city,partyId:partyId,
                 countyGeoId: countyGeoId, mobilePhone: mobilePhone,attnName:attnName,
                 postalCode: postalCode, stateProvinceGeoId: stateProvinceGeoId,userLogin:userLogin];

    result = dispatcher.runSync("createPartyPostalAddress", serviceIn);
    //设置为默认货物目的地地址

    contactMechId = result.contactMechId;
    contactMechPurposeTypeId = 'SHIPPING_LOCATION';
    serviceIn = [partyId: partyId, contactMechId: contactMechId, contactMechPurposeTypeId: contactMechPurposeTypeId,userLogin: userLogin]
    result = dispatcher.runSync("createPartyContactMechPurpose", serviceIn);

    //设置为默认货物目的地地址
    if(is_def) {
        serviceIn = [productStoreId: productStoreId, defaultShipAddr: contactMechId, partyId: partyId, userLogin: userLogin];
        println "serviceIn = $serviceIn"
        result = dispatcher.runSync("setPartyProfileDefaults", serviceIn);
        resultData.put("result", result)
    }


    println "result = $result"
    resultData.put("result", result);
    request.setAttribute("resultData",resultData);
    return "success";

}