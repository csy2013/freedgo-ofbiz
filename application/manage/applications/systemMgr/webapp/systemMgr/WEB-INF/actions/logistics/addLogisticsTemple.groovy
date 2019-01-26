String productStoreId = request.getAttribute("productStoreId");
logisticsCompanys = delegator.findByAnd("LogisticsCompany", ["isEnabled": "Y","productStoreId": productStoreId]);
if(logisticsCompanys){
    context.logisticsCompanys = logisticsCompanys;
}