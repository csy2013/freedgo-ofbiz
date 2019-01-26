resultData = [:]
String countryGeoId = request.getParameter("countryGeoId");
String listOrderBy = request.getParameter("listOrderBy");
serviceIn = [countryGeoId: countryGeoId, listOrderBy: listOrderBy];
result = dispatcher.runSync("getAssociatedStateListJson", serviceIn);
request.setAttribute("resultData", result);
return "success";
