package com.zoonza.coinalarm;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public MySQLContainer mysqlContainer() {
        return new MySQLContainer(DockerImageName.parse("mysql:8.4.9"))
                .withTmpFs(Map.of("/var/lib/mysql", "rw"));
    }
}
