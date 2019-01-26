import org.ofbiz.base.util.UtilMisc
navigationProducts = delegator.findByAnd("NavigationProduct",UtilMisc.toMap("navigationId",request.getParameter("navigationId")),UtilMisc.toList("seq"));
context.navigationProducts = navigationProducts;
context.navigationId = request.getParameter("navigationId");