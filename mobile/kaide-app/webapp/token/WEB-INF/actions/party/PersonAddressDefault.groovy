//设置会员默认的收货地址，当PartyProfileDefault 没有默认收货地址的时候，取最近的一个收货地址
import org.ofbiz.product.store.ProductStoreWorker

productStoreId = ProductStoreWorker.getProductStoreId(request);
userLogin = request.getAttribute("userLogin")
result = dispatcher.runSync("queryCustomDefaultPostAddress",[loginId:userLogin.get("userLoginId"),productStoreId:productStoreId,userLogin:userLogin])
request.setAttribute("resultData", result);
return "success";