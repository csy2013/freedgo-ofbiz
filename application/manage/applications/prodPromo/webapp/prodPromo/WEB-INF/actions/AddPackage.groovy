import org.ofbiz.entity.condition.EntityCondition

promoPackageTypes = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: "PROMO_FREE_SHIPPING"]), null, ["sequenceId"], null, true);
context.promoPackageTypes = promoPackageTypes;