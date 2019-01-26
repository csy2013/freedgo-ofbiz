import org.ofbiz.entity.condition.EntityCondition

promoTypes = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: "PRO_PROMO_TYPE"]), null, ["sequenceId"], null, true);
context.promoTypes = promoTypes;

promoProTypes = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: "PRO_PROMO_PRT_TYPE"]), null, ["sequenceId"], null, true);
context.promoProTypes = promoProTypes;

partyLevels = delegator.findList("PartyLevelType",null, null, ['levelId'], null, true);
context.partyLevels = partyLevels;

promoGiftTypes = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: "PROMO_GIFT_PARAM"]), null, ["sequenceId"], null, true);
context.promoGiftTypes = promoGiftTypes;

promoDiscountTypes = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: "PROMO_DISCOUNT"]), null, ["sequenceId"], null, true);
context.promoDiscountTypes = promoDiscountTypes;

promoStraightDowns = delegator.findList("Enumeration", EntityCondition.makeCondition([enumTypeId: "PROMO_SPE_PRICE"]), null, ["sequenceId"], null, true);
context.promoStraightDowns = promoStraightDowns;
