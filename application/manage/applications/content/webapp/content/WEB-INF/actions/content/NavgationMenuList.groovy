import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.*;

// 查询导航类型信息

def navTypeId = 'NAV_LINK_TYPE'

def cond = EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, navTypeId)



context.navTypes = delegator.findList("Enumeration",cond,null,null,null,false)



def linkTypeId = 'FIRST_LINK_TYPE'

def linkCond = EntityCondition.makeCondition('enumTypeId', EntityOperator.EQUALS, linkTypeId)

context.linkTypes = delegator.findList('Enumeration', linkCond,null,null,null,false)
println('navTypes=>' + context.navTypes)