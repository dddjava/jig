package org.dddjava.jig.gradle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JigConfigurationContextTest {

    @Test
    void モデル検出時の警告メッセージに設定で指定したモデルパターンが含まれること() {
        String settingTo = ".+\\.domain_model\\..+";
        JigConfig config = new JigConfig();
        config.setModelPattern(settingTo);
        JigConfigurationContext sut = new JigConfigurationContext(config);

        String message = sut.modelDetectionWarningMessage();

        assertThat(message).contains(String.format("modelPattern=%s", settingTo));
    }

}
