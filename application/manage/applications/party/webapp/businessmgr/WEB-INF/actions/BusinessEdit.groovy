
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.*;
// 查询商家类型信息

def businessType = 'BUSINESS_TYPE'

def cond = EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, businessType)

context.businessTypes = delegator.findList("Enumeration",cond,null,null,null,false)
