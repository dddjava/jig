package jig.shell;

import jig.domain.model.jdeps.*;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.nio.file.Paths;
import java.util.Collections;

@ShellComponent
public class JigCommands {

    RelationAnalyzer relationAnalyzer;

    public JigCommands(RelationAnalyzer relationAnalyzer) {
        this.relationAnalyzer = relationAnalyzer;
    }

    @ShellMethod("環境テスト用")
    public void test() {
    }

    @ShellMethod("jdeps")
    public void jdeps(String classDir, String pattern, AnalysisTarget target) {
        AnalysisCriteria criteria = new AnalysisCriteria(
                new SearchPaths(Collections.singletonList(Paths.get(classDir))),
                new AnalysisClassesPattern(pattern),
                new DependenciesPattern(pattern),
                target
        );
        relationAnalyzer.analyzeRelations(criteria);
    }
}
