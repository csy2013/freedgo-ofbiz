loginId = request.getParameter("loginId");
dataCategoryId = request.getParameter("dataCategoryId");
contentTypeId = request.getParameter("contentTypeId");
statusId = request.getParameter("statusId");
roleTypeId = request.getParameter("roleTypeId");
partyContentTypeId = request.getParameter("partyContentTypeId");
mimeTypeId = request.getParameter("mimeTypeId");
uploadedFile = request.getParameter("uploadedFile");
println "uploadedFile = $uploadedFile"
println "loginId = $loginId"
println "dataCategoryId = $dataCategoryId"
println "statusId = $statusId"
println "roleTypeId = $roleTypeId"
println "partyContentTypeId = $partyContentTypeId"
println "mimeTypeId = $mimeTypeId"

userLogin = delegator.findByPrimaryKey("UserLogin", [userLoginId: loginId])
partyId = userLogin.partyId
serviceIn = [dataCategoryId: dataCategoryId, contentTypeId: contentTypeId, statusId: statusId, partyId: partyId,
             roleTypeId    : roleTypeId, partyContentTypeId: partyContentTypeId, mimeTypeId: mimeTypeId, userLogin: userLogin];
result = dispatcher.runSync("uploadPartyContentFile", serviceIn);
