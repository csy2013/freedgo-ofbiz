/**
 * Created by dongxiao on 2016/4/20.
 */

import org.ofbiz.base.util.UtilMisc
navigationBrands = delegator.findByAnd("NavigationBrand",UtilMisc.toMap("navigationId",request.getParameter("navigationId")),UtilMisc.toList("seq"));
context.navigationBrands = navigationBrands;
context.navigationId = request.getParameter("navigationId");
if (request.getParameter("navigationId")) {
	navigationInfo = delegator.findByPrimaryKey("Navigation", ["id" : request.getParameter("navigationId")]);
	//navigationInfos = delegator.findByAnd("Navigation",UtilMisc.toMap("parentId",null,"id",request.getParameter("navigationId")),UtilMisc.toList("sequence"));
	if(navigationInfo){
		context.navigationGroupId = navigationInfo.navigationGroupId;
	}
}