import javolution.util.FastMap
import org.ofbiz.base.util.UtilHttp
import org.ofbiz.product.store.ProductStoreWorker

/*
donePage:viewprofile, contactMechId:10240,
partyContactMech:
[lastUpdatedStamp:2015-09-18 18:04:31.0, thruDate:null, yearsWithContactMech:null, contactMechId:10240, allowSolicitation:Y, extension:null, roleTypeId:null, createdTxStamp:2015-09-18 18:04:31.0, fromDate:2015-09-18 18:04:31.0, createdStamp:2015-09-18 18:04:31.0, partyId:10020, verified:null, monthsWithContactMech:null, lastUpdatedTxStamp:2015-09-18 18:04:31.0, comments:null],
contactMech:
[lastUpdatedStamp:2015-09-18 18:04:31.0, createdTxStamp:2015-09-18 18:04:31.0, createdStamp:2015-09-18 18:04:31.0, contactMechId:10240, contactMechTypeId:POSTAL_ADDRESS, lastUpdatedTxStamp:2015-09-18 18:04:31.0, infoString:null],
contactMechTypeId:POSTAL_ADDRESS,
contactMechType:
[lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:Y, description:Postal Address, parentTypeId:null, contactMechTypeId:POSTAL_ADDRESS, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
purposeTypes:
[[lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, description:Billing (AP) Address, contactMechPurposeTypeId:BILLING_LOCATION, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, description:General Correspondence Address, contactMechPurposeTypeId:GENERAL_LOCATION, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, description:Payment (AR) Address, contactMechPurposeTypeId:PAYMENT_LOCATION, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, description:Previous Address, contactMechPurposeTypeId:PREVIOUS_LOCATION, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, description:Primary Address, contactMechPurposeTypeId:PRIMARY_LOCATION, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, description:Purchase Return Address, contactMechPurposeTypeId:PUR_RET_LOCATION, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, description:Shipping Destination Address, contactMechPurposeTypeId:SHIPPING_LOCATION, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, description:Shipping Origin Address, contactMechPurposeTypeId:SHIP_ORIG_LOCATION, lastUpdatedTxStamp:2014-10-29 14:05:44.0]],
requestName:updatePostalAddress,
postalAddress:
[lastUpdatedStamp:2015-09-18 18:04:31.0, toName:csy, postalCodeExt:null, contactMechId:10240, address1:南京浦口区     下关合成路, stateProvinceGeoId:CN-11, address2:null, city:南京, directions:null, postalCode:210011, createdTxStamp:2015-09-18 18:04:31.0, mobilePhone:null, countryGeoId:CHN, createdStamp:2015-09-18 18:04:31.0, attnName:csy, countyGeoId:null, postalCodeGeoId:null, lastUpdatedTxStamp:2015-09-18 18:04:31.0, geoPointId:null],
tryEntity:true,
contactMechTypes:
[[lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:N, description:Internet Domain Name, parentTypeId:ELECTRONIC_ADDRESS, contactMechTypeId:DOMAIN_NAME, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:N, description:Electronic Address, parentTypeId:null, contactMechTypeId:ELECTRONIC_ADDRESS, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:N, description:Email Address, parentTypeId:ELECTRONIC_ADDRESS, contactMechTypeId:EMAIL_ADDRESS, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:N, description:Internal Note via partyId, parentTypeId:ELECTRONIC_ADDRESS, contactMechTypeId:INTERNAL_PARTYID, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:N, description:Internet IP Address, parentTypeId:ELECTRONIC_ADDRESS, contactMechTypeId:IP_ADDRESS, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:56.0, createdTxStamp:2014-10-29 14:05:55.0, createdStamp:2014-10-29 14:05:56.0, hasTable:N, description:LDAP URL, parentTypeId:ELECTRONIC_ADDRESS, contactMechTypeId:LDAP_ADDRESS, lastUpdatedTxStamp:2014-10-29 14:05:55.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:Y, description:Postal Address, parentTypeId:null, contactMechTypeId:POSTAL_ADDRESS, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:Y, description:Phone Number, parentTypeId:null, contactMechTypeId:TELECOM_NUMBER, lastUpdatedTxStamp:2014-10-29 14:05:44.0],
 [lastUpdatedStamp:2014-10-29 14:05:44.0, createdTxStamp:2014-10-29 14:05:44.0, createdStamp:2014-10-29 14:05:44.0, hasTable:N, description:Web URL/Address, parentTypeId:ELECTRONIC_ADDRESS, contactMechTypeId:WEB_ADDRESS, lastUpdatedTxStamp:2014-10-29 14:05:44.0]]
*/

/* puts the following in the context: "contactMech", "contactMechId",
        "partyContactMech", "partyContactMechPurposes", "contactMechTypeId",
        "contactMechType", "purposeTypes", "postalAddress", "telecomNumber",
        "requestName", "donePage", "tryEntity", "contactMechTypes"
 */
target = [:];
userLogin = request.getAttribute("userLogin");
Map<String,Object> paramMap = UtilHttp.getCombinedMap(request);
String productStoreId = ProductStoreWorker.getProductStoreId(request);
contactMechId = paramMap.get("contactMechId")
result = dispatcher.runSync("customAddressDetail",[loginId:userLogin.get("userLoginId"),contactMechId:contactMechId,userLogin:userLogin]);

result1 = dispatcher.runSync("queryCustomDefaultPostAddress",[loginId:userLogin.get("userLoginId"),productStoreId:productStoreId,userLogin:userLogin])
partyAndContactMech = result1.get("partyAndContactMech");
resultData=[:]
if(partyAndContactMech){
    resultData.put("defaultAddressId",partyAndContactMech.get('contactMechId'))
}


resultData.put("contactMechData",result.get("contactMech"));
resultData.put("partyContactMechData",result.get("partyContactMech"));
resultData.put("postalAddressData",result.get("postalAddress"));
resultData.put("telecomNumberData",result.get("telecomNumber"));

request.setAttribute("resultData",resultData);
return "success";

