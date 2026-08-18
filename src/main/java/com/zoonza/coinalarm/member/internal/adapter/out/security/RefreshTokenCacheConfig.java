package com.zoonza.coinalarm.member.internal.adapter.out.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zoonza.coinalarm.member.internal.application.dto.RefreshTokenSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RefreshTokenCacheConfig {

    @Bean
    Cache<String, RefreshTokenSession> refreshTokenCache(
            AuthenticationProperties properties
    ) {
        return Caffeine.newBuilder()
                .maximumSize(properties.refreshTokenCacheMaximumSize())
                .expireAfterWrite(properties.refreshTokenTtl())
                .build();
    }
}
