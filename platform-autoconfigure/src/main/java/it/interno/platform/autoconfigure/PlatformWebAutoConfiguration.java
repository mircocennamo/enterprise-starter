package it.interno.platform.autoconfigure;

import it.interno.platform.starter.web.advice.GlobalExceptionAdvice;

import it.interno.platform.starter.security.JwtAuthenticationFilter;
import it.interno.platform.starter.security.JwtTokenProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot AutoConfiguration for Interno Platform libraries.
 * 
 * Automatically registers all platform beans when the starter library is imported:
 * - Global exception handling (GlobalExceptionAdvice)
 * - HTTP client configuration with JWT support (HttpExchangeConfig)
 * - JWT authentication provider (JwtTokenProvider)
 * - JWT authentication filter (JwtAuthenticationFilter)
 * 
 * No additional configuration required - just add the starter to your classpath.
 * 
 * NOTE: PongClient is NOT auto-registered as it's a service-specific client.
 * Applications can independently enable it via @ImportHttpServices or configuration.
 */
@AutoConfiguration
@ComponentScan({
    "it.interno.platform.starter.core",
    "it.interno.platform.starter.web",
    "it.interno.platform.starter.security"
})
@Import({
    GlobalExceptionAdvice.class,
    JwtTokenProvider.class,
    JwtAuthenticationFilter.class
})
public class PlatformWebAutoConfiguration {

    /**
     * Registers JwtTokenProvider as a bean if not already defined by the application.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider();
    }

    /**
     * Registers JwtAuthenticationFilter as a bean if not already defined by the application.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Registers GlobalExceptionAdvice as a bean if not already defined by the application.
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionAdvice globalExceptionAdvice() {
        return new GlobalExceptionAdvice();
    }

}
