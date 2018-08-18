package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.basic.ConfigurationContext;

import java.text.MessageFormat;
import java.util.StringJoiner;

public class JigConfigurationContext implements ConfigurationContext {

    JigConfig config;

    public JigConfigurationContext(JigConfig config) {
        this.config = config;
    }

    @Override
    public String classFileDetectionWarningMessage() {
        return "プロジェクト上にクラスへコンパイルされる対象ソースが存在しない可能性があります。";
    }

    private final static String MODEL_DETECTION_WARNING = new StringJoiner(System.lineSeparator())
            .add("Jig Gradle Pluginの設定にて以下のパラメータを調整してみてください。")
            .add("jig {")
            .add("    {0}={1}")
            .add("}")
            .toString();

    @Override
    public String modelDetectionWarningMessage() {
        return MessageFormat.format(MODEL_DETECTION_WARNING, "modelPattern", config.modelPattern);
    }

}

