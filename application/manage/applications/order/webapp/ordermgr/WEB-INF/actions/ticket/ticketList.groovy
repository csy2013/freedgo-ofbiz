import org.ofbiz.base.util.UtilMisc
import org.ofbiz.entity.GenericValue

GenericValue userLogin= request.getSession().getAttribute("userLogin");
if(userLogin){
    String partyId = userLogin.get("partyId");
    println "partyId========== = $partyId"
    GenericValue partyBusiness = delegator.findByPrimaryKey("PartyBusiness",[partyId:partyId]);
    println "partyBusiness============ = $partyBusiness"
    if(partyBusiness!=null){
        println "partyBusiness============ = $partyBusiness"
        context.isBusiness  = true;
        context.partyBusinessId = partyId;
    }else{
        context.isBusiness = false;
    }
}