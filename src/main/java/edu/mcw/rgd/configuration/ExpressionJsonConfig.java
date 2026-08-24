package edu.mcw.rgd.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.mcw.rgd.datamodel.expression.ExpressionDataIndexObject;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

/**
 * Configures the JSON written by the REST controllers.
 *
 * <p>The deployed WAR is bootstrapped from web.xml, not Spring Boot, so {@code spring.jackson.*}
 * properties and Jackson2ObjectMapperBuilderCustomizer beans are never read -- the ObjectMapper has to
 * be configured directly. This adjusts the converter that {@code <mvc:annotation-driven/>} already
 * created, rather than contributing one through {@code <mvc:message-converters>}: custom converters are
 * prepended to the list, which would put Jackson ahead of the String and byte[] converters and corrupt
 * responses that are already-serialized JSON, such as springdoc's /rgd-api-docs.
 */
@Component
public class ExpressionJsonConfig implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof MappingJackson2HttpMessageConverter converter) {
            // omit null-valued fields rather than emitting them as "field": null
            converter.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            converter.getObjectMapper().addMixIn(ExpressionDataIndexObject.class, ExpressionDataIndexObjectMixin.class);
        }
        return bean;
    }
}
