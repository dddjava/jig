package org.dddjava.jig.application;

import org.junit.jupiter.api.Test;
import testing.JigTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@JigTest
class JigServiceTest {

    @Test
    void serviceAngle(JigService jigService, JigRepository jigRepository) {
        var serviceAngles = jigService.serviceAngles(jigRepository);

        assertDoesNotThrow(() -> {
            serviceAngles.list();
        });
    }
}