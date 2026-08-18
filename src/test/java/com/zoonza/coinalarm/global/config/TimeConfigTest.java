package com.zoonza.coinalarm.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class TimeConfigTest {

    @Test
    @DisplayName("서울 시간대의 Clock을 제공한다")
    void providesSeoulClock() {
        Clock clock = new TimeConfig().clock();

        assertThat(clock.getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }
}
