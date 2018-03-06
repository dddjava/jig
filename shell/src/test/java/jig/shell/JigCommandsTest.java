package jig.shell;

import jig.application.service.DiagramService;
import jig.domain.model.jdeps.AnalysisTarget;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.infrastructure.plantuml.DiagramRepositoryImpl;
import jig.infrastructure.plantuml.PlantumlDiagramConverter;
import jig.infrastructure.plantuml.PlantumlDiagramMaker;
import jig.infrastructure.plantuml.PlantumlNameFormatter;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class JigCommandsTest {

    JigCommands sut = initJigCommands();

    private JigCommands initJigCommands() {
        ShellApplication application = new ShellApplication();
        return new JigCommands(application.relationAnalyzer(), new DiagramService(
                new DiagramRepositoryImpl(),
                new PlantumlDiagramMaker(),
                new PlantumlDiagramConverter(new PlantumlNameFormatter(new JapaneseNameDictionary()))
        ));
    }

    @Test
    void jdeps() {
        sut.jdeps("../sut", "sut.*", AnalysisTarget.PACKAGE);
        // エラーにならなければOK
    }

    @Test
    void packageDiagram() throws Exception {
        File file = new File(Files.newTemporaryFolder(), "temp.png");
        file.deleteOnExit();
        sut.packageDiagram("../sut", "sut.*", file.toString());

        // 出力対象がない場合のPNGファイルが411byteなのでそれ以上で
        assertThat(file.length()).isGreaterThan(411);
    }
}