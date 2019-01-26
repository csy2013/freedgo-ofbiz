import org.ofbiz.base.util.UtilHttp
import org.ofbiz.product.store.ProductStoreWorker

resultData = [:]
userLogin = request.getAttribute("userLogin")
paramMap = UtilHttp.getCombinedMap(request)
if (userLogin) {
    productStoreId = ProductStoreWorker.getProductStoreId(request);
    println "userLogin = $userLogin"
    partyId = userLogin.partyId;
    defaultShipAddr = paramMap.get("defaultShipAddr");
    serviceIn = [productStoreId: productStoreId, defaultShipAddr: defaultShipAddr, partyId: partyId,userLogin: userLogin];
    println "serviceIn = $serviceIn"
    result = dispatcher.runSync("setPartyProfileDefaults", serviceIn);
    resultData.put("result",result)
    request.setAttribute("resultData", resultData);

}
return "success"