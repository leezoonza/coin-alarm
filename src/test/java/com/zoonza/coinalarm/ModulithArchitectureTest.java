package com.zoonza.coinalarm;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTest {

    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(CoinAlarmApplication.class).verify();
    }
}
