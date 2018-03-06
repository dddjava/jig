package jig.shell;

import jig.application.service.DiagramService;
import jig.domain.model.diagram.Diagram;
import jig.domain.model.jdeps.*;
import jig.domain.model.relation.Relations;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

@ShellComponent
public class JigCommands {

    RelationAnalyzer relationAnalyzer;
    DiagramService diagramService;

    public JigCommands(RelationAnalyzer relationAnalyzer, DiagramService diagramService) {
        this.relationAnalyzer = relationAnalyzer;
        this.diagramService = diagramService;
    }

    @ShellMethod("環境テスト用")
    public void test() {
    }

    @ShellMethod("読み取りディレクトリ、解析対象パターン、CLASS/PACKAGEを指定してjdepsを実行します")
    public void jdeps(String classDir, String pattern, AnalysisTarget target) {
        analyzeRelations(classDir, pattern, target);
    }

    @ShellMethod("パッケージ依存図")
    public void packageDiagram(String classDir, String pattern, String outputPath) throws Exception {
        Relations relations = analyzeRelations(classDir, pattern, AnalysisTarget.PACKAGE);

        Diagram diagram = diagramService.generateFrom(relations);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(outputPath)))) {
            outputStream.write(diagram.getBytes());
        }
    }

    private Relations analyzeRelations(String classDir, String pattern, AnalysisTarget target) {
        AnalysisCriteria criteria = new AnalysisCriteria(
                new SearchPaths(Collections.singletonList(Paths.get(classDir))),
                new AnalysisClassesPattern(pattern),
                new DependenciesPattern(pattern),
                target);
        return relationAnalyzer.analyzeRelations(criteria);
    }
}
