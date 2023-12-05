package com.distasilucas.cryptobalancetracker.configuration

import jakarta.servlet.DispatcherType
import jakarta.servlet.Filter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.handler.HandlerMappingIntrospector

import java.util.EnumSet

/**
 * TODO - This can be removed when logging issue is fixed.
 * https://github.com/spring-projects/spring-framework/issues/31588
 */

@Configuration
class CacheHandlerMappingIntrospectorConfig {

    companion object {

        @Bean
        @JvmStatic
        fun handlerMappingIntrospectorCacheFilter(hmi: HandlerMappingIntrospector): FilterRegistrationBean<Filter> {
            val registrationBean = FilterRegistrationBean(hmi.createCacheFilter())
            registrationBean.order = Ordered.HIGHEST_PRECEDENCE
            registrationBean.setDispatcherTypes(EnumSet.allOf(DispatcherType::class.java))

            return registrationBean
        }

    }
}