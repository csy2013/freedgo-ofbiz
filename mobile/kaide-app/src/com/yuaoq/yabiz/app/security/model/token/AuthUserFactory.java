package com.yuaoq.yabiz.app.security.model.token;

import com.yuaoq.yabiz.app.security.model.AuthUser;

import org.ofbiz.entity.GenericValue;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Date;

public class AuthUserFactory {
    public static AuthUser create(GenericValue user) {
        if(user!=null) {
            return new AuthUser(
                    (String)user.get("partyId"),
                    (String) user.get("userLoginId"),
                    (String)user.get("currentPassword"),
                    "",
                    ((Date) user.get("lastLoginTime")),
                    AuthorityUtils.commaSeparatedStringToAuthorityList("USER"),user
            );
        }else{
            return null;
        }
    }
}
