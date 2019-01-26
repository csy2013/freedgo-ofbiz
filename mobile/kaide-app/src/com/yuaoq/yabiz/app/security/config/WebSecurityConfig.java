package com.yuaoq.yabiz.app.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuaoq.yabiz.app.security.RestAuthenticationEntryPoint;
import com.yuaoq.yabiz.app.security.auth.ajax.AjaxAuthenticationProvider;
import com.yuaoq.yabiz.app.security.auth.ajax.AjaxLoginProcessingFilter;
import com.yuaoq.yabiz.app.security.auth.jwt.JwtAuthenticationProvider;
import com.yuaoq.yabiz.app.security.auth.jwt.JwtTokenAuthenticationProcessingFilter;
import com.yuaoq.yabiz.app.security.auth.jwt.SkipPathRequestMatcher;
import com.yuaoq.yabiz.app.security.auth.jwt.extractor.TokenExtractor;
import com.yuaoq.yabiz.app.security.filter.CustomCorsFilter;
import com.yuaoq.yabiz.app.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.List;

/**
 * WebSecurityConfig
 *
 * @author vladimir.stankovic
 * <p>
 * Aug 3, 2016
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String JWT_TOKEN_HEADER_PARAM = "X-Authorization";
    public static final String FORM_BASED_LOGIN_ENTRY_POINT = "/api/auth/login";
    public static final String TOKEN_BASED_AUTH_ENTRY_POINT = "^(/api/.*/v1/.*)|(/a/.*)$";
//    public static final String TOKEN_BASED_AUTH_ENTRY_POINT = "/*/api/v1/.*";`
    public static final String TOKEN_REFRESH_ENTRY_POINT = "/api/auth/token";
    public static final String FILE_RESOURCE = "/images/*";
    
    public static final String WEIXIN_AUTH = "/api/wx/v1/app";
    public static final String WEIXIN_LOGIN = "/api/wx/v1/getUserInfoByCode";
    public static final String WEIXIN_getMallByLan = "/api/wx/v1/getMallByLan";
    public static final String PING_WEBHOOKS = "/api/ping/v1/webhooks";
    public static final String ORDER_PAYMENT_CALLBACK = "/api/pay/v1/completePay";
    public static final String heartbeat = "/api/common/v1/heartbeat";
    public static final String PARTY_REGISTER = "/a/party.register";
    
    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private AuthenticationSuccessHandler successHandler;
    @Autowired
    private AuthenticationFailureHandler failureHandler;
    @Autowired
    private AjaxAuthenticationProvider ajaxAuthenticationProvider;
    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;
    @Autowired
    private UserDetailsServiceImpl userService;
    
    @Autowired
    private TokenExtractor tokenExtractor;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    protected AjaxLoginProcessingFilter buildAjaxLoginProcessingFilter() throws Exception {
        AjaxLoginProcessingFilter filter = new AjaxLoginProcessingFilter(FORM_BASED_LOGIN_ENTRY_POINT, successHandler, failureHandler, objectMapper);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }
    
    protected JwtTokenAuthenticationProcessingFilter buildJwtTokenAuthenticationProcessingFilter() throws Exception {
        List<String> pathsToSkip = Arrays.asList(TOKEN_REFRESH_ENTRY_POINT, FORM_BASED_LOGIN_ENTRY_POINT,FILE_RESOURCE,PARTY_REGISTER,WEIXIN_AUTH,ORDER_PAYMENT_CALLBACK,WEIXIN_LOGIN,PING_WEBHOOKS,WEIXIN_getMallByLan,heartbeat);
        SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip, TOKEN_BASED_AUTH_ENTRY_POINT);
        JwtTokenAuthenticationProcessingFilter filter
                = new JwtTokenAuthenticationProcessingFilter(failureHandler, tokenExtractor, matcher, userService);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }
    
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(ajaxAuthenticationProvider);
        auth.authenticationProvider(jwtAuthenticationProvider);
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() // We don't need CSRF for JWT based authentication
                .exceptionHandling()
                .authenticationEntryPoint(this.authenticationEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(FORM_BASED_LOGIN_ENTRY_POINT).permitAll() // Login end-point
                .antMatchers(TOKEN_REFRESH_ENTRY_POINT).permitAll() // Token refresh end-point
                .antMatchers(FILE_RESOURCE).permitAll()
                .antMatchers(WEIXIN_AUTH).permitAll()
                .antMatchers(PARTY_REGISTER).permitAll()
                .antMatchers(heartbeat).permitAll()
                .antMatchers(ORDER_PAYMENT_CALLBACK).permitAll()
                .antMatchers(WEIXIN_LOGIN).permitAll()
                .antMatchers(PING_WEBHOOKS).permitAll()
                .antMatchers(WEIXIN_getMallByLan).permitAll()
                .and()
                .authorizeRequests()
                .antMatchers(TOKEN_BASED_AUTH_ENTRY_POINT).authenticated()
                .and()
                .addFilterBefore(new CustomCorsFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildAjaxLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildJwtTokenAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
    }
    
 
}
