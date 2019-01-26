package com.yuaoq.yabiz.app.service.impl;

import com.yuaoq.yabiz.app.security.model.token.AuthUserFactory;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Value("${entity.delegator.name}")
    private String delegatorName;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        GenericValue user = null;
        String delegatorName = "default";
        String userLoginId = username;
        if(username.indexOf("#")!=-1) {
            delegatorName = username.substring(0, username.indexOf("#"));
            userLoginId = username.substring(username.indexOf("#") + 1);
        }
        Delegator delegator = null;
        try {
            if(delegatorName.equals("default")){
                  delegator = DelegatorFactory.getDelegator("default");
            }else {
                  delegator = DelegatorFactory.getDelegator("default#" + delegatorName);
            }
            user = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId",userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return AuthUserFactory.create(user);
        }
    }
    
    
   
    public GenericValue loadUserLoginByUserName(String username) throws UsernameNotFoundException {
        GenericValue user = null;
        String delegatorName = "default";
        String userLoginId = username;
        if(username.indexOf("#")!=-1) {
            delegatorName = username.substring(0, username.indexOf("#"));
            userLoginId = username.substring(username.indexOf("#") + 1);
        }
        Delegator delegator = null;
        try {
            if(delegatorName.equals("default")){
                delegator = DelegatorFactory.getDelegator("default");
            }else {
                delegator = DelegatorFactory.getDelegator("default#" + delegatorName);
            }
            user = delegator.findByPrimaryKeyCache("UserLogin", UtilMisc.toMap("userLoginId",userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return user;
        }
    }
    
    

}
