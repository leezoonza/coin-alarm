package com.zoonza.coinalarm;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class BinanceStreamServiceTests {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final BinanceStreamService service = new BinanceStreamService(new ObjectMapper(), meterRegistry);

    @Test
    void recordsMetricsForSuccessfullyParsedTickers() throws Exception {
        long eventTime = System.currentTimeMillis() - 100;
        String payload = """
                [
                  {"e":"24hrMiniTicker","E":%d,"s":"BTCUSDT","c":"60000","o":"59000","h":"61000","l":"58000","v":"10","q":"600000"},
                  {"e":"24hrMiniTicker","E":%d,"s":"ETHUSDT","c":"3000","o":"2900","h":"3100","l":"2800","v":"20","q":"60000"}
                ]
                """.formatted(eventTime, eventTime);

        service.handleTextMessage(null, new TextMessage(payload));

        assertThat(counter("coin.stream.messages.received")).isEqualTo(1.0);
        assertThat(counter("coin.stream.messages.parsed")).isEqualTo(1.0);
        assertThat(counter("coin.stream.tickers.created")).isEqualTo(2.0);
        assertThat(counter("coin.stream.parse.errors")).isZero();
        assertThat(summaryCount("coin.stream.payload.bytes")).isEqualTo(1L);
        assertThat(summaryTotal("coin.stream.tickers.per.message")).isEqualTo(2.0);
        assertThat(summaryCount("coin.stream.event.lag")).isEqualTo(1L);
        assertThat(meterRegistry.get("coin.stream.parse.duration").timer().count()).isEqualTo(1L);
        assertThat(meterRegistry.get("coin.stream.last.message.age").gauge().value()).isNotNegative();
    }

    @Test
    void recordsMetricsForParsingErrors() throws Exception {
        service.handleTextMessage(null, new TextMessage("not-json"));

        assertThat(counter("coin.stream.messages.received")).isEqualTo(1.0);
        assertThat(counter("coin.stream.messages.parsed")).isZero();
        assertThat(counter("coin.stream.tickers.created")).isZero();
        assertThat(counter("coin.stream.parse.errors")).isEqualTo(1.0);
        assertThat(meterRegistry.get("coin.stream.parse.duration").timer().count()).isEqualTo(1L);
    }

    private double counter(String name) {
        return meterRegistry.get(name).counter().count();
    }

    private long summaryCount(String name) {
        return meterRegistry.get(name).summary().count();
    }

    private double summaryTotal(String name) {
        return meterRegistry.get(name).summary().totalAmount();
    }
}
