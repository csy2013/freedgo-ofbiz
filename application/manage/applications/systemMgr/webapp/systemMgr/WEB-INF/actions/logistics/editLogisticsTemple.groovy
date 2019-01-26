import org.ofbiz.base.util.UtilValidate

logisticsCompanys = delegator.findByAnd("LogisticsCompany", ["isEnabled": "Y"]);
if (logisticsCompanys) {
    context.logisticsCompanys = logisticsCompanys;
}
logisticsTempleId = parameters.logisticsTempleId;
context.logisticsTempleId = logisticsTempleId;
logisticsTemple = delegator.findByPrimaryKey("LogisticsTemple", ["logisticsTempleId": logisticsTempleId]);
logisticsTempleItems = delegator.findByAnd("LogisticsTempleItem", ["logisticsTempleId": logisticsTempleId]);
if (logisticsTemple) {
    context.logisticsTemple = logisticsTemple;
}
if (logisticsTempleItems) {
    context.logisticsTempleItems = logisticsTempleItems;
    for(int i=0;i<logisticsTempleItems.size();i++){
        if(UtilValidate.isEmpty(logisticsTempleItems.get(i).areas)){
            context.defaultItem = logisticsTempleItems.get(i);
        }
    }
}