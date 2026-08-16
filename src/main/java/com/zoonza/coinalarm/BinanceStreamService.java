package com.zoonza.coinalarm;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceStreamService extends TextWebSocketHandler {
    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/!miniTicker@arr";

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();

        client.execute(this, BINANCE_WS_URL);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.setTextMessageSizeLimit(1024 * 1024);

        log.info("[바이낸스 스트림 세션 오픈: {}, 텍스트 제한: {}]",
                session.getId(),
                session.getTextMessageSizeLimit()
                );
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            List<BinanceMiniTicker> tickers = objectMapper.readValue(
                    message.getPayload(),
                    new TypeReference<List<BinanceMiniTicker>>() {}
            );

//            tickers.forEach(
//                    ticker -> log.info("[{}] 가격: {}", ticker.symbol(), ticker.closePrice())
//            );

        } catch (Exception e) {
            System.err.println("JSON 파싱 에러: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("[전송 오류 발생: {}]", exception.getMessage());

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[세션 종료: {},코드: {}, 사유: {}]",
                session.getId(),
                status.getCode(),
                status.getReason()
        );
    }
}
