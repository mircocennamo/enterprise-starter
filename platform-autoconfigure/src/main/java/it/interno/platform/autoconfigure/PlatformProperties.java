package it.interno.platform.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuration properties for the Interno Platform starter.
 * <p>
 * All properties are optional — sensible defaults are applied automatically.
 * Override any value in your {@code application.yml}:
 *
 * <pre>
 * interno:
 *   platform:
 *     http:
 *       connect-timeout: 2s       # default: 2s  (ISO-8601 or shorthand)
 *       read-timeout: 5s          # default: 5s
 *       platform-header: interno-platform  # default: interno-platform
 * </pre>
 */
@ConfigurationProperties(prefix = "interno.platform")
public class PlatformProperties {

    private final Http http;

    public PlatformProperties(@DefaultValue Http http) {
        this.http = http;
    }

    public Http getHttp() {
        return http;
    }

    /**
     * HTTP client settings (timeouts and common headers).
     */
    public static class Http {

        /**
         * Maximum time to wait when establishing a TCP connection.
         * Accepts Spring Boot Duration format: "2s", "500ms", "PT2S".
         * Default: 2 seconds.
         */
        private final Duration connectTimeout;

        /**
         * Maximum time to wait for data after the connection is established.
         * Accepts Spring Boot Duration format: "5s", "1m", "PT5S".
         * Default: 5 seconds.
         */
        private final Duration readTimeout;

        /**
         * Value sent in the {@code X-Platform} header on every outbound HTTP call.
         * Default: interno-platform.
         */
        private final String platformHeader;

        public Http(
                @DefaultValue("2s") Duration connectTimeout,
                @DefaultValue("5s") Duration readTimeout,
                @DefaultValue("interno-platform") String platformHeader) {
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            this.platformHeader = platformHeader;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public Duration getReadTimeout() {
            return readTimeout;
        }

        public String getPlatformHeader() {
            return platformHeader;
        }
    }
}
