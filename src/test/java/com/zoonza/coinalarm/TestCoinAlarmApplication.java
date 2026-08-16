package com.zoonza.coinalarm;

import org.springframework.boot.SpringApplication;

public class TestCoinAlarmApplication {

    public static void main(String[] args) {
        SpringApplication.from(CoinAlarmApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
