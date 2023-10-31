//package edu.mcw.rgd.configuration;
//
//import org.springframework.beans.factory.ListableBeanFactory;
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
//import org.springframework.boot.autoconfigure.web.WebProperties;
//import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
//import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
//import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
//
//public class WebConfig extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {
//    public WebConfig(WebProperties webProperties, WebMvcProperties mvcProperties, ListableBeanFactory beanFactory, ObjectProvider<HttpMessageConverters> messageConvertersProvider, ObjectProvider<WebMvcAutoConfiguration.ResourceHandlerRegistrationCustomizer> resourceHandlerRegistrationCustomizerProvider, ObjectProvider<DispatcherServletPath> dispatcherServletPath, ObjectProvider<ServletRegistrationBean<?>> servletRegistrations) {
//        super(webProperties, mvcProperties, beanFactory, messageConvertersProvider, resourceHandlerRegistrationCustomizerProvider, dispatcherServletPath, servletRegistrations);
//    }
//    @Override
//    public void configurePathMatch(PathMatchConfigurer configurer) {
//        configurer.setUseSuffixPatternMatch(false);
//    }
//}
