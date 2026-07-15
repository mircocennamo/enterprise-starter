package it.interno.platform.starter.security.config;

/**
 * @deprecated This class is no longer needed.
 * The SecurityFilterChain is now registered directly by
 * {@link it.interno.platform.autoconfigure.PlatformWebAutoConfiguration}
 * with {@code @ConditionalOnMissingBean(SecurityFilterChain.class)}.
 * <p>
 * To customize security, simply declare your own {@code SecurityFilterChain} bean
 * in your application's {@code @Configuration} class:
 * <pre>
 *   {@code @Bean}
 *   public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {
 *       // your custom configuration
 *       return http.build();
 *   }
 * </pre>
 * This class will be removed in a future release.
 */
@Deprecated(since = "0.0.3", forRemoval = true)
public class SecurityConfig {
    private SecurityConfig() {}
}
