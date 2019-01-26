import org.ofbiz.base.util.UtilDateTime
import org.ofbiz.entity.condition.EntityCondition
context.nowDate = UtilDateTime.nowDate();
activityStatusEnums = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: "ACTY_AUDIT_STUATS"]), null, ["sequenceId"], null, true);
context.activityStatusEnums = activityStatusEnums;
activityTypeEnums = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: 'ACTY_TYPE']), null, ['sequenceId'], null, true);
context.activityTypeEnums = activityTypeEnums;
activityStatusEnums = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: 'ACTY_AUDIT_STUATS']), null, ['sequenceId'], null, true);
context.activityStatusEnums = activityStatusEnums;
activityShipmentEnums = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: 'ACTY_SHIPMENT_TYPE']), null, ['sequenceId'], null, true);
context.activityShipmentEnums = activityShipmentEnums;

activityPayTypes = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: 'ACTY_PAY_TYPE']), null, ['sequenceId'], null, true);
context.activityPayTypes = activityPayTypes;


partyLevels = delegator.findList("PartyLevelType",null, null, ['levelId'], null, true);
context.partyLevels = partyLevels;

communities = delegator.findList("Community",null, null, ['code'], null, true);
context.communities = communities;

productStores = delegator.findList("ProductStore",null,null,['productStoreId'],null,true);
context.productStores = productStores;