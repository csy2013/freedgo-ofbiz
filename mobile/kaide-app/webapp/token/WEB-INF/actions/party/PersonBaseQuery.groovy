import org.ofbiz.entity.GenericValue

GenericValue userLogin = request.getAttribute("userLogin");
println "userLogin = $userLogin"
resultData = dispatcher.runSync("customBaseQuery", [loginId: userLogin.get("userLoginId"), userLogin: userLogin]);
request.setAttribute("resultData", resultData);

return "success"