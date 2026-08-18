package com.zoonza.coinalarm;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@Import(MySqlTestcontainersConfiguration.class)
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
}
