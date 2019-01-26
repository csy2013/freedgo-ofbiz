import org.ofbiz.product.store.ProductStoreWorker;

productStore = ProductStoreWorker.getProductStore(request);
context.productStoreId = productStore.productStoreId;
context.productStore = productStore;

context.createAllowPassword = "Y".equals(productStore.allowPassword);
context.getUsername = !"Y".equals(productStore.usePrimaryEmailUsername);

previousParams = parameters._PREVIOUS_PARAMS_;
if (previousParams) {
    previousParams = "?" + previousParams;
} else {
    previousParams = "";
}
context.previousParams = previousParams;

//the parameters from janrain
userInfoMap = request.getAttribute("userInfoMap");
if (!userInfoMap) {
    userInfoMap = request.getSession(true).getAttribute("userInfoMap");
}
if (userInfoMap) {
    if (userInfoMap.givenName && userInfoMap.familyName) {
        requestParameters.USER_FIRST_NAME = userInfoMap.givenName;
        requestParameters.USER_LAST_NAME = userInfoMap.familyName;
    } else if (userInfoMap.formatted) {
        requestParameters.USER_FIRST_NAME = userInfoMap.formatted;
    }
    requestParameters.CUSTOMER_EMAIL = userInfoMap.email;
    requestParameters.preferredUsername = userInfoMap.preferredUsername;
    requestParameters.USERNAME = userInfoMap.preferredUsername;
    request.getSession().setAttribute("userInfoMap", userInfoMap);
}

donePage = "main";
context.donePage = donePage;
