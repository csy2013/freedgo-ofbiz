logisticsCompanys = delegator.findByAnd("LogisticsCompany", ["isEnabled": "Y"]);
if(logisticsCompanys){
    context.logisticsCompanys = logisticsCompanys;
}
logisticsDocumentsId = parameters.logisticsDocumentsId;
logisticsDocuments = delegator.findByPrimaryKey("LogisticsDocuments", ["logisticsDocumentsId": logisticsDocumentsId]);
if(logisticsDocuments){
    context.logisticsDocuments = logisticsDocuments;
}