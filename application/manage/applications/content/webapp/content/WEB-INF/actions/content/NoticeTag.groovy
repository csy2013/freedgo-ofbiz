import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.*;

// 查询公告标签

def tagTypeId = 'NoticeTag'
def linkTypeId = 'FIRST_LINK_TYPE'

def cond = EntityCondition.makeCondition("tagTypeId", EntityOperator.EQUALS, tagTypeId)
def linkCond = EntityCondition.makeCondition('enumTypeId', EntityOperator.EQUALS, linkTypeId)

context.tagList = delegator.findList("Tag",cond,null,null,null,false)
context.linkTypes = delegator.findList('Enumeration', linkCond,null,null,null,false)
