package com.zoonza.coinalarm.global.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class TimeConfigTest {

    @Test
    void providesSeoulClock() {
        Clock clock = new TimeConfig().clock();

        assertThat(clock.getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }
}
