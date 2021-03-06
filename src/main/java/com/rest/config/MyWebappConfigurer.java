package com.rest.config;

import com.rest.intercetors.ExecutionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by bruce.ge on 2016/11/10.
 */
@Configuration
public class MyWebappConfigurer extends WebMvcConfigurerAdapter{
    @Autowired
    @Qualifier("authInterceptor")
    private HandlerInterceptor authInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //remove page will forward. //how to know the forward request real time cost?
        registry.addInterceptor(new ExecutionInterceptor());
        registry.addInterceptor(authInterceptor);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/").setCachePeriod(3600*24);
//        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/").setCachePeriod(3600*24);
//        registry.addResourceHandler("/img/**").setCachePeriod(3600*24);
    }
}