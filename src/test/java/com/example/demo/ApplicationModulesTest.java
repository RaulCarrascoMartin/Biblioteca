package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Verifica con Spring Modulith los límites entre módulos (ciclos, accesos a internal, dependencias no permitidas). */
class ApplicationModulesTest {

    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(DemoApplication.class).verify();
    }
}
