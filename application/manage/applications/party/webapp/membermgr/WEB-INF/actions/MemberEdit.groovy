import org.ofbiz.base.util.UtilMisc
import org.ofbiz.entity.GenericValue
import org.ofbiz.entity.util.EntityFindOptions
import org.ofbiz.entity.util.EntityUtil
import org.ofbiz.party.contact.ContactHelper
import org.ofbiz.party.contact.ContactMechWorker
import org.ofbiz.party.party.PartyRelationshipHelper
import org.ofbiz.entity.util.EntityListIterator
import org.ofbiz.entity.model.DynamicViewEntity
import org.ofbiz.entity.condition.EntityOperator
import org.ofbiz.entity.model.ModelKeyMap
import org.ofbiz.entity.condition.EntityCondition

/**
 * 文件名：MemberDetail.groovy
 * 版权：启华
 * 描述：会员明细：根据会员编号获取基本信息和收货地址
 * 修改人：
 * 修改时间：
 * 修改单号：〈修改单号〉
 * 修改内容：〈修改内容〉
 */

//获取参数
partyId = parameters.partyId;
context.partyId = partyId;

//查询会员基本信息
if(partyId) {
    party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));
    person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
    suppliers = PartyRelationshipHelper.getActiveSupplier(delegator,UtilMisc.toMap("partyIdTo",partyId,"roleTypeIdFrom","SUPPLIER","roleTypeIdTo","EMPLOYEE","partyRelationshipTypeId","OWNER"));
    context.personDetail = person;
    context.party = party;
    context.suppliers = suppliers;
    
    //会员等级
    DynamicViewEntity pl_dv = new DynamicViewEntity();
	pl_dv.addMemberEntity("PL","PartyLevel");
	pl_dv.addAlias("PL","partyId");
    
	pl_dv.addMemberEntity("PLT","PartyLevelType");
	pl_dv.addAlias("PLT","levelName");
	pl_dv.addViewLink("PL","PLT", Boolean.FALSE,ModelKeyMap.makeKeyMapList("levelId", "levelId"));
    findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true)
	EntityListIterator pl_pli = delegator.findListIteratorByCondition(pl_dv, EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,partyId), null, null, null, findOpts);
	String partyLevel = "";
	for(GenericValue pl_gv : pl_pli.getCompleteList()){
		partyLevel = pl_gv.getString("levelName");
	}
	pl_pli.close();
	context.partyLevel = partyLevel;
	
    //会员所在社区
    partyCommunityList = delegator.findByAnd("PartyCommunity",UtilMisc.toMap("partyId",partyId));
    context.partyCommunityList = partyCommunityList;

    //账户余额
    partyAccount = delegator.findByPrimaryKey("PartyAccount",UtilMisc.toMap("partyId",partyId));
    context.partyAccount = partyAccount;
    
    //会员积分
    partyScore = delegator.findByPrimaryKey("PartyScore",UtilMisc.toMap("partyId",partyId));
    context.partyScore = partyScore;
    //会员成长值
    partyAttribute = EntityUtil.getFirst(delegator.findByAnd("PartyAttribute", UtilMisc.toMap("partyId", partyId, "attrName", "EXPERIENCE")));
    context.partyAttribute = partyAttribute;
    
    //会员成长值
    userLogin = EntityUtil.getFirst(delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId)));
    context.userLoginId = userLogin.userLoginId;
    
    //收货地址
    List<Map<Object,Object>> shippingContactMechList = ContactHelper.getContactMechByType(party,  "POSTAL_ADDRESS", false);
    shippingContactMechList = EntityUtil.orderBy(shippingContactMechList,["contactMechId"])
    context.shippingContactMechList = shippingContactMechList;
}



