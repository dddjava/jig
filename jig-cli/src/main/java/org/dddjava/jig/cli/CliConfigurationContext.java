package org.dddjava.jig.cli;
import org.dddjava.jig.domain.basic.ConfigurationContext;

import java.text.MessageFormat;
import java.util.StringJoiner;

public class CliConfigurationContext implements ConfigurationContext {

    CliConfig cliConfig;

    public CliConfigurationContext(CliConfig cliConfig) {
        this.cliConfig = cliConfig;
    }

    private final static String CLASSFILE_DETECTION_WARNING = new StringJoiner(System.lineSeparator())
            .add("以下のコマンドラインパラメータを調整してみてください。この値はディレクトリの絞り込みに使用されます。")
            .add("-{0}={1}")
            .toString();

    @Override
    public String classFileDetectionWarningMessage() {
        return MessageFormat.format(CLASSFILE_DETECTION_WARNING, "directory.classes", cliConfig.directoryClasses);
    }

    private final static String MODEL_DETECTION_WARNING = new StringJoiner(System.lineSeparator())
            .add("以下のコマンドラインパラメータを調整してみてください。")
            .add("-{0}={1}")
            .toString();

    @Override
    public String  modelDetectionWarningMessage() {
        return MessageFormat.format(MODEL_DETECTION_WARNING, "jig.model.pattern", cliConfig.modelPattern);
    }

}

