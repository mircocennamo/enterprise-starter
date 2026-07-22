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
 *     cache:
 *      enabled: true
 *      maximun-size: 10000
 *      expire-after-write: 10m
 *      expire-after-access: 5m
 *      record-stats: true
 *
 *

 * </pre>
 */
@ConfigurationProperties(prefix = "interno.platform.cache")
public class CacheProperties {

    private boolean enabled = true;
    private long maximunSize = 10000;
    private Duration expireAfterWrite = Duration.ofMinutes(10);
    private Duration expireAfterAccess = Duration.ofMinutes(5);
    private boolean recordStats = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getMaximunSize() {
        return maximunSize;
    }

    public void setMaximunSize(long maximunSize) {
        this.maximunSize = maximunSize;
    }

    public Duration getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(Duration expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public Duration getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(Duration expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public boolean isRecordStats() {
        return recordStats;
    }

    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }
}

