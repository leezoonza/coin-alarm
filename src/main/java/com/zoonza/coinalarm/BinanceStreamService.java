package com.zoonza.coinalarm;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class BinanceStreamService extends TextWebSocketHandler {
    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/!miniTicker@arr";

    private final ObjectMapper objectMapper;
    private final Counter connectionAttempts;
    private final Counter connectionsEstablished;
    private final Counter connectionsClosed;
    private final Counter transportErrors;
    private final Counter messagesReceived;
    private final Counter messagesParsed;
    private final Counter tickersCreated;
    private final Counter parseErrors;
    private final DistributionSummary payloadBytes;
    private final DistributionSummary tickersPerMessage;
    private final DistributionSummary eventLag;
    private final Timer parseDuration;
    private final AtomicInteger connected = new AtomicInteger();
    private final AtomicLong lastMessageEpochMillis = new AtomicLong();

    public BinanceStreamService(ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.connectionAttempts = Counter.builder("coin.stream.connection.attempts")
                .description("Number of Binance WebSocket connection attempts")
                .register(meterRegistry);
        this.connectionsEstablished = Counter.builder("coin.stream.connection.established")
                .description("Number of established Binance WebSocket connections")
                .register(meterRegistry);
        this.connectionsClosed = Counter.builder("coin.stream.connection.closed")
                .description("Number of closed Binance WebSocket connections")
                .register(meterRegistry);
        this.transportErrors = Counter.builder("coin.stream.transport.errors")
                .description("Number of Binance WebSocket transport errors")
                .register(meterRegistry);
        this.messagesReceived = Counter.builder("coin.stream.messages.received")
                .description("Number of WebSocket text messages received before parsing")
                .register(meterRegistry);
        this.messagesParsed = Counter.builder("coin.stream.messages.parsed")
                .description("Number of WebSocket text messages parsed successfully")
                .register(meterRegistry);
        this.tickersCreated = Counter.builder("coin.stream.tickers.created")
                .description("Number of BinanceMiniTicker DTOs created")
                .register(meterRegistry);
        this.parseErrors = Counter.builder("coin.stream.parse.errors")
                .description("Number of WebSocket payload parsing errors")
                .register(meterRegistry);
        this.payloadBytes = DistributionSummary.builder("coin.stream.payload.bytes")
                .description("Size of received WebSocket text message payloads")
                .baseUnit("bytes")
                .register(meterRegistry);
        this.tickersPerMessage = DistributionSummary.builder("coin.stream.tickers.per.message")
                .description("Number of ticker DTOs created per successfully parsed message")
                .baseUnit("tickers")
                .register(meterRegistry);
        this.eventLag = DistributionSummary.builder("coin.stream.event.lag")
                .description("Delay between Binance event time and local receipt time")
                .baseUnit("milliseconds")
                .register(meterRegistry);
        this.parseDuration = Timer.builder("coin.stream.parse.duration")
                .description("Time spent deserializing a WebSocket payload")
                .register(meterRegistry);

        Gauge.builder("coin.stream.connected", connected, AtomicInteger::get)
                .description("Whether the Binance WebSocket is currently connected")
                .register(meterRegistry);
        Gauge.builder("coin.stream.last.message.age", lastMessageEpochMillis, this::lastMessageAgeSeconds)
                .description("Seconds elapsed since the last received WebSocket message")
                .baseUnit("seconds")
                .register(meterRegistry);
    }

    @PostConstruct
    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();

        connectionAttempts.increment();
        client.execute(this, BINANCE_WS_URL);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.setTextMessageSizeLimit(1024 * 1024);
        connected.set(1);
        connectionsEstablished.increment();

        log.info("[바이낸스 스트림 세션 오픈: {}, 텍스트 제한: {}]",
                session.getId(),
                session.getTextMessageSizeLimit()
                );
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        long startedAt = System.nanoTime();
        long receivedAt = System.currentTimeMillis();

        messagesReceived.increment();
        payloadBytes.record(message.getPayloadLength());
        lastMessageEpochMillis.set(receivedAt);

        try {
            List<BinanceMiniTicker> tickers = objectMapper.readValue(
                    message.getPayload(),
                    new TypeReference<List<BinanceMiniTicker>>() {}
            );

            messagesParsed.increment();
            tickersCreated.increment(tickers.size());
            tickersPerMessage.record(tickers.size());
            recordEventLag(tickers, receivedAt);
        } catch (Exception e) {
            parseErrors.increment();
            log.warn("JSON 파싱 오류", e);
        } finally {
            parseDuration.record(System.nanoTime() - startedAt, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        connected.set(0);
        transportErrors.increment();
        log.warn("[전송 오류 발생: {}]", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        connected.set(0);
        connectionsClosed.increment();
        log.info("[세션 종료: {},코드: {}, 사유: {}]",
                session.getId(),
                status.getCode(),
                status.getReason()
        );
    }

    private void recordEventLag(List<BinanceMiniTicker> tickers, long receivedAt) {
        if (tickers.isEmpty() || tickers.getFirst().eventTime() == null) {
            return;
        }

        eventLag.record(Math.max(0L, receivedAt - tickers.getFirst().eventTime()));
    }

    private double lastMessageAgeSeconds(AtomicLong timestamp) {
        long lastMessageAt = timestamp.get();
        if (lastMessageAt == 0L) {
            return -1.0;
        }

        return Math.max(0L, System.currentTimeMillis() - lastMessageAt) / 1_000.0;
    }
}
