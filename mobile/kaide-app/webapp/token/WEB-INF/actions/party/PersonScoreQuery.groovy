// call the getOrderedSummaryInformation service to get the sub-total of valid orders in last X months
monthsToInclude = 12;
userLogin = request.getAttribute("userLogin")
partyId = userLogin.partyId
serviceIn = [partyId : partyId, roleTypeId: "PLACING_CUSTOMER", orderTypeId: "SALES_ORDER",
             statusId: "ORDER_COMPLETED", monthsToInclude: monthsToInclude, userLogin: userLogin];
result = dispatcher.runSync("getOrderedSummaryInformation", serviceIn);

resultData = [:]
resultData.put("totalSubRemainingAmount", result.totalSubRemainingAmount);
resultData.put("totalOrders", result.totalOrders);
request.setAttribute("resultData", resultData);
return "success"