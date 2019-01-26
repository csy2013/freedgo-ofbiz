userLogin = request.getAttribute("userLogin")
if (userLogin) {
    println "userLogin = $userLogin"
    nickname = request.getParameter("nickname");
    gender = request.getParameter("gender")
    email = request.getParameter("email")
    mobile = request.getParameter("mobile")
    firstName = request.getParameter("firstName")
    lastName = request.getParameter("lastName")
    birthDate = request.getParameter("birthDate")
    partyId = userLogin.partyId

    result = dispatcher.runSync('updatePerson', [partyId  : partyId, nickname: nickname, gender: gender, email: email, mobile: mobile,
                                                 firstName: firstName, lastName: lastName, birthDate: birthDate, userLogin: userLogin]);
    request.setAttribute("resultData", result);
}
return "success";