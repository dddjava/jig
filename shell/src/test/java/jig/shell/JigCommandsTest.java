package jig.shell;

import jig.domain.model.jdeps.AnalysisTarget;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JigCommandsTest {

    JigCommands sut = initJigCommands();

    private JigCommands initJigCommands() {
        ShellApplication application = new ShellApplication();
        return new JigCommands(application.relationAnalyzer());
    }

    @Test
    void jdeps() {
        String actual = sut.jdeps("../sut", "sut.*", AnalysisTarget.PACKAGE);
        assertThat(actual).isNotNull();
    }
}