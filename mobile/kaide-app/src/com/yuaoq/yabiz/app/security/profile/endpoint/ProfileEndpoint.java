package com.yuaoq.yabiz.app.security.profile.endpoint;


import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.app.service.impl.UserDetailsServiceImpl;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * End-point for retrieving logged-in user details.
 * 
 * @author vladimir.stankovic
 *
 * Aug 4, 2016
 */
@RestController
public class ProfileEndpoint {
    
    private final UserDetailsServiceImpl userService;
    
    public ProfileEndpoint(UserDetailsServiceImpl userService) {
        this.userService = userService;
    }
    
    @RequestMapping(value="/api/me", method= RequestMethod.GET)
    public @ResponseBody
    UserContext get(JwtAuthenticationToken token) {
        return (UserContext) token.getPrincipal();
    }
    
    @RequestMapping(value="/api/v1/s/me", method= RequestMethod.GET)
    public @ResponseBody Map getUserInfo(JwtAuthenticationToken token, HttpServletRequest request) {
        UserContext userContext =  (UserContext) token.getPrincipal();
        String userName = userContext.getUsername();
        GenericValue user = userService.loadUserLoginByUserName(userName);
        LocalDispatcher dispatcher = (LocalDispatcher)request.getAttribute("dispatcher");
        Map<String,Object> result = null;
        try {
            result = dispatcher.runSync("customBaseQuery", UtilMisc.toMap("loginId",user.get("userLoginId"),"userLogin",user));
        } catch (GenericServiceException e) {
            e.printStackTrace();
            throw  new UsernameNotFoundException("User not found: " + userName);
        }
        if(UtilValidate.isEmpty(user)){
          throw  new UsernameNotFoundException("User not found: " + userName);
        }
        result.put("userLoginId",user.get("userLoginId"));
        return result;
    }
}
