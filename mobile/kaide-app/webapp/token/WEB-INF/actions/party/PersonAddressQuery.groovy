import javolution.util.FastMap
import org.ofbiz.product.store.ProductStoreWorker

userLogin = request.getAttribute("userLogin")
productStoreId = ProductStoreWorker.getProductStoreId(request);
result = dispatcher.runSync("customAddressQuery",[loginId:userLogin.get("userLoginId"),userLogin:userLogin])
result1 = dispatcher.runSync("queryCustomDefaultPostAddress",[loginId:userLogin.get("userLoginId"),productStoreId:productStoreId,userLogin:userLogin])
partyAndContactMech = result1.get("partyAndContactMech");
resultData = FastMap.newInstance()
if(partyAndContactMech){
    resultData.put("defaultAddressId",partyAndContactMech.get('contactMechId'))
}
if(result){
    resultData.put("adderess",result);
}
request.setAttribute("resultData", resultData);
return "success"