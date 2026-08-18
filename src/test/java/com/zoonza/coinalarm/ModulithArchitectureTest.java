package com.zoonza.coinalarm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTest {

    @Test
    @DisplayName("모듈 구조가 정의된 의존성 규칙을 준수한다")
    void verifiesModuleStructure() {
        ApplicationModules.of(CoinAlarmApplication.class).verify();
    }
}
