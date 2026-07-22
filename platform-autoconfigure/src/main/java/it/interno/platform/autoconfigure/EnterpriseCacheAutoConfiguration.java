package it.interno.platform.autoconfigure;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;


@EnableCaching
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
@Slf4j
public class EnterpriseCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager(CacheProperties properties) {
        log.info("Initializing Caffeine CacheManager with default settings");
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        Caffeine<Object, Object> builder  = Caffeine.newBuilder()
                .maximumSize(properties.getMaximunSize())
                .expireAfterWrite(properties.getExpireAfterWrite())
                .expireAfterAccess(properties.getExpireAfterAccess());
        if(properties.isRecordStats()) {
            //abilita la raccolta delle statistiche di utilizzo della cache
            builder.recordStats();
        }
        cacheManager.setCaffeine(builder);
        return cacheManager;
    }

}
