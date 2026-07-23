package it.interno.platform.autoconfigure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import it.interno.platform.starter.security.JwtAuthenticationFilter;
import it.interno.platform.starter.security.JwtTokenProvider;
import it.interno.platform.starter.web.advice.GlobalExceptionAdvice;
import it.interno.platform.starter.web.filter.LoggingFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

import java.net.http.HttpClient;

/**
 * Spring Boot AutoConfiguration for Interno Platform libraries.
 * <p>
 * Automatically registers all platform beans when the starter library is
 * imported:
 * - Global exception handling (GlobalExceptionAdvice)
 * - HTTP client configuration with JWT support
 * - JWT authentication provider (JwtTokenProvider)
 * - JWT authentication filter (JwtAuthenticationFilter)
 * - Default security filter chain (SecurityFilterChain)
 * <p>
 * Each bean is registered with @ConditionalOnMissingBean, allowing the
 * application
 * to override any component simply by declaring its own bean of the same type.
 * <p>
 * No additional configuration required - just add the starter to your
 * classpath.
 * <p>
 * NOTE: PongClient is NOT auto-registered as it's a service-specific client.
 * Applications can independently enable it via @ImportHttpServices or
 * configuration.
 */
@AutoConfiguration
@EnableConfigurationProperties(PlatformProperties.class)
@Slf4j
public class PlatformWebAutoConfiguration {

    /**
     * Registers JwtTokenProvider as a bean if not already defined by the
     * application.
     * Override by declaring a JwtTokenProvider @Bean in your @Configuration class.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider();
    }

    /**
     * Registers JwtAuthenticationFilter as a bean if not already defined by the
     * application.
     * Override by declaring a JwtAuthenticationFilter @Bean in your @Configuration
     * class.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Registers GlobalExceptionAdvice as a bean if not already defined by the
     * application.
     * Override by declaring a GlobalExceptionAdvice (or subclass) @Bean in
     * your @Configuration class.
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionAdvice globalExceptionAdvice() {
        return new GlobalExceptionAdvice();
    }

    /**
     * Registers a default stateless SecurityFilterChain with JWT filter if not
     * already defined.
     * Override by declaring a SecurityFilterChain @Bean in your @Configuration
     * class.
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                // Disable CSRF since REST APIs are stateless
                .csrf(csrf -> csrf.disable())
                // sameOrigin
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // Set stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Permit all by default - override SecurityFilterChain to restrict access
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .exceptionHandling(ex->ex.accessDeniedHandler
                        ((request, response, accessDeniedException) ->{
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Accesso Negato: permessi insufficienti\"}");
                                    }
                        ))


                // Add custom JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures all RestClient HTTP service groups with:
     * - Global X-Platform header
     * - JWT token propagation from SecurityContext
     * - Global timeout configuration
     * - Common error logging
     * <p>
     * NOTE: Multiple RestClientHttpServiceGroupConfigurer beans are allowed (one
     * global + service-specific),
     * so @ConditionalOnMissingBean is intentionally NOT used here.
     */
    @Bean
    public RestClientHttpServiceGroupConfigurer globalHttpServiceConfigurer(
            JwtTokenProvider jwtTokenProvider,
            ClientHttpRequestFactory requestFactory,
            PlatformProperties properties) {

        return groups -> groups.forEachClient((group, builder) -> {

            log.info("Configurazione globale applicata al gruppo [{}]", group.name());

            // Header comuni (configurabile via interno.platform.http.platform-header)
            builder.defaultHeader("X-Platform", properties.getHttp().getPlatformHeader());

            // Timeout globali
            builder.requestFactory(requestFactory);

            // Propagazione JWT
            builder.requestInterceptor((request, body, execution) -> {
                 jwtTokenProvider.getToken()
                         .ifPresent(token -> {
                             log.info("Propagating JWT token for request to {}", request.getURI());
                             request.getHeaders().setBearerAuth(token);

                         });
                 try {
                    return execution.execute(request, body);
                } catch (Exception ex) {
                    log.error("HTTP client exception verso [{}]: {}", request.getURI(), ex.getMessage(), ex);
                    throw ex;
                }
            });

            // Gestione errori comune
            builder.defaultStatusHandler(
                    status -> status.isError(),
                    (request, response) -> log.error(
                            "Errore {} durante chiamata a {}",
                            response.getStatusCode(),
                            request.getURI()));
        });
    }

    /**
     * Registers the default JDK-based HTTP client factory.
     * Timeouts are configurable via:
     * 
     * <pre>
     *   interno.platform.http.connect-timeout: 2s
     *   interno.platform.http.read-timeout: 5s
     * </pre>
     * 
     * Override entirely by declaring a ClientHttpRequestFactory @Bean in
     * your @Configuration class.
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientHttpRequestFactory clientHttpRequestFactory(PlatformProperties properties) {
        PlatformProperties.Http http = properties.getHttp();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(http.getConnectTimeout())
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(http.getReadTimeout());
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("Interno Platform API")
                        .version("v1")
                        .description("API  Interno Platform con autenticazione JWT."))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }


    @Bean
    @ConditionalOnMissingBean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }
}
