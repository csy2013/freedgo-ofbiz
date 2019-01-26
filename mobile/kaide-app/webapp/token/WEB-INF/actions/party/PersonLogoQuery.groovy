userLogin = request.getAttribute("userLogin")
println "userLogin ======= $userLogin"
partyContents = dispatcher.runSync("queryCustomContent",[loginId:userLogin.get("userLoginId"),userLogin:userLogin,partyContentTypeId:'LGOIMGURL']);
request.setAttribute('resultData',partyContents)
return "success"