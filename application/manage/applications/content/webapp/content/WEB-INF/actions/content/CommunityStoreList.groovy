import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.*;

//查询社区店类型（COMMUNITY_STORE_TYPE）

def typeId = 'COMMUNITY_STORE_TYPE'

def cond = EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, typeId)

context.communityStoreTypes = delegator.findList("Enumeration",cond,null,null,null,false)