package com.zoonza.coinalarm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceMiniTicker(
        @JsonProperty("e") String eventType,   // 24hrMiniTicker
        @JsonProperty("E") Long eventTime,     // 이벤트 시간 (timestamp)
        @JsonProperty("s") String symbol,        // 심볼 (예: BTCUSDT)
        @JsonProperty("c") String closePrice,    // 현재가/종가
        @JsonProperty("o") String openPrice,     // 시가
        @JsonProperty("h") String highPrice,     // 고가
        @JsonProperty("l") String lowPrice,      // 저가
        @JsonProperty("v") String volume,        // 거래량 (Base asset)
        @JsonProperty("q") String quoteVolume    // 거래대금 (Quote asset)
) {
}
