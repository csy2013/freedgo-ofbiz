context.shoppingCart = session.getAttribute("shoppingCart");
context.integralPerMoney = request.getAttribute("integralPerMoney");
integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet",[partyIntegralSetId : "PARTY_INTEGRAL_SET"]).getLong("integralValue");
//integralPerMoney = 100
//assert integralPerMoney != null;
context.integralPerMoney = integralPerMoney

invoiceContents = delegator.findByAnd("Enumeration", [enumTypeId : "FIELD_INVOICE"], ["sequenceId"]);
context.invoiceContents = invoiceContents;