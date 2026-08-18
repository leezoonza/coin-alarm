package com.zoonza.coinalarm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(TestcontainersConfiguration.class)
class CoinAlarmApplicationTests {

    @MockitoBean
    private BinanceStreamService binanceStreamService;

    @Test
    void contextLoads() {
    }

}
