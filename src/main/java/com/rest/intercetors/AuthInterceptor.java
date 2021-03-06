package com.rest.intercetors;

import com.rest.annotation.NeedAuth;
import com.rest.bean.User;
import com.rest.constant.CookieConstants;
import com.rest.constant.SessionConstants;
import com.rest.converter.UserConverter;
import com.rest.domain.UserPO;
import com.rest.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by bruce.ge on 2016/11/13.
 */
@Component("authInterceptor")
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        // 连同filter一起搞了
//        说明有user存在
        //just try to deal with session etc. to gaoding cookie and make it true.
        //so every controller can acess with user now.
        if (httpServletRequest.getSession().getAttribute(SessionConstants.USER) == null) {
            setSessionOnCookie(httpServletRequest);
        }
        User user = (User) httpServletRequest.getSession().getAttribute(SessionConstants.USER);
        if (o instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) o;
            Method method =
                    handlerMethod.getMethod();
            if (method.isAnnotationPresent(NeedAuth.class)) {
                NeedAuth auth = method.getAnnotation(NeedAuth.class);
                return goauth(httpServletRequest, httpServletResponse, user, auth);
            } else {
                Class<? extends HandlerMethod> handlerMethodClass = handlerMethod.getClass();
                if (handlerMethodClass.isAnnotationPresent(NeedAuth.class)) {
                    NeedAuth auth = handlerMethodClass.getAnnotation(NeedAuth.class);
                    return goauth(httpServletRequest, httpServletResponse, user, auth);
                }
            }
        }
        return true;
    }

    private boolean goauth(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, User user, NeedAuth auth) throws IOException {
        switch (auth.value()) {
            case LOGIN:
                if (user.isLogin()) {
                    return true;
                } else {
                    if (!auth.redirectBack()) {
                        httpServletResponse.sendRedirect(auth.redirectPage());
                    } else {
                        httpServletRequest.getSession().setAttribute(SessionConstants.AUTHBACKPAGE,httpServletRequest.getServletPath());
                        httpServletResponse.sendRedirect(auth.redirectPage());
                    }
                    return false;
                }
            case ADMIN:
                if (user.isAdmin()) {
                    return true;
                } else {
                    if (!auth.redirectBack()) {
                        httpServletResponse.sendRedirect(auth.redirectPage());
                    } else {
                        httpServletRequest.getSession().setAttribute(SessionConstants.AUTHBACKPAGE,httpServletRequest.getServletPath());
                        httpServletResponse.sendRedirect(auth.redirectPage());
                    }
                    return false;
                }
        }
        return true;
    }

    private void setSessionOnCookie(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        String name = null;
        String password_cookie = null;
        if(cookies==null){
            httpServletRequest.getSession().setAttribute(SessionConstants.USER, new User());
            return;
        }
// TODO: 2016/11/14 never stop username and password the direct way
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(CookieConstants.USERNAME)) {
                name = cookie.getValue();
            } else if (cookie.getName().equals(CookieConstants.PASSWORD)) {
                password_cookie = cookie.getValue();
            }
        }
        if (name == null || password_cookie == null) {
            httpServletRequest.getSession().setAttribute(SessionConstants.USER, new User());
        } else {
            //will get lots of thing from database.
            UserPO dto = loginService.loginByCookie(name, password_cookie);
            if (dto == null) {
                httpServletRequest.getSession().setAttribute(SessionConstants.USER, new User());
            } else {
                httpServletRequest.getSession().setAttribute(SessionConstants.USER, UserConverter.convertToUser(dto));
            }
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //do something to get.
        if(modelAndView==null||modelAndView.getViewName().startsWith("redirect")){
            return;
        }
        User user = (User) httpServletRequest.getSession().getAttribute(SessionConstants.USER);
        if (user != null) {
            if (user.isAdmin()) {
                modelAndView.addObject("admin", true);
            }
            if(user.isLogin()){
                modelAndView.addObject("login",true);
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
