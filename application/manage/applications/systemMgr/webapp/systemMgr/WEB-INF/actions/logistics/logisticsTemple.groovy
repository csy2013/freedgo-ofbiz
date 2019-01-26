import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator

productStoreId = request.getAttribute("productStoreId")
condSec = EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId) ;
osisFields = ["logisticsTempleId", "logisticsTempleName", "logisticsPackageMail", "logisticsMethods"] as Set;
osisFields.add("isUrgent");
osisFields.add("lastUpdatedStamp");
osisFields.add("logisticsCompanyId");
logisticsTemples = delegator.findList("LogisticsTempleAndCompany", condSec, osisFields, null, null, false);

if (logisticsTemples) {
    context.logisticsTemples = logisticsTemples;
}