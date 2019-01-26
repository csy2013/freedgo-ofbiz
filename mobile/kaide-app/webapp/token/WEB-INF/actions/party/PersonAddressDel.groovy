resultData = [:]
userLogin = request.getAttribute("userLogin")
if (userLogin) {

    contactMechId = request.getParameter('contactMechId')
    partyId = userLogin.partyId


    println "contactMechId = $contactMechId"

    println "partyId = $partyId"

    serviceIn = [contactMechId: contactMechId,partyId:partyId,userLogin:userLogin];

    result = dispatcher.runSync("deletePartyContactMech", serviceIn);
    println "result = $result"
    resultData.put("result", result);
    request.setAttribute("resultData",resultData);
    return "success";

}

