String productStoreId = request.getAttribute("productStoreId");
println "productStoreId = $productStoreId"
logisticsDocumentsList = delegator.findByAnd("LogisticsDocuments",  [productStoreId: productStoreId]);
if(logisticsDocumentsList){
    context.logisticsDocumentsList = logisticsDocumentsList;
}