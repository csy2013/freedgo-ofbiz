import org.ofbiz.base.util.UtilMisc

productCategorys = delegator.findByAnd("ProductCategory",UtilMisc.toMap("productCategoryLevel",1l,"isDel",null));
navigationRoots = delegator.findByAnd("Navigation",UtilMisc.toMap("parentId",null,"navigationGroupId",request.getParameter("navigationGroupId")),UtilMisc.toList("sequence"));
context.productCategorys = productCategorys;
context.navigationRoots = navigationRoots;