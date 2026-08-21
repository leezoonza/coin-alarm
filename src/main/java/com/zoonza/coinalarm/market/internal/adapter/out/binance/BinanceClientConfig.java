package com.zoonza.coinalarm.market.internal.adapter.out.binance;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class BinanceClientConfig {
    private static final String BINANCE_BASE_URL = "https://fapi.binance.com";

    @Bean
    RestClient binanceRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(BINANCE_BASE_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
