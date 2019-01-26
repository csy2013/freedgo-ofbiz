import org.ofbiz.entity.condition.EntityCondition

promoTypes = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: "PROMO_NEWCUST_PARAM"]), null, ["sequenceId"], null, true);
context.promoTypes = promoTypes;

