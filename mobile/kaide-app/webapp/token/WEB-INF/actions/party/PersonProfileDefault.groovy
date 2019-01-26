import org.ofbiz.product.store.ProductStoreWorker

userLogin = request.getAttribute("userLogin")
if (userLogin) {
    productStoreId = ProductStoreWorker.getProductStoreId(request);
    resultData = dispatcher.runSync("customProfileDefault",[loginId:userLogin.get("userLoginId"),productStoreId:productStoreId,userLogin:userLogin]);
    request.setAttribute("resultData",resultData);
}
return "success"