package jig.shell;

import jig.application.service.DiagramService;
import jig.domain.model.diagram.Diagram;
import jig.domain.model.jdeps.*;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.infrastructure.javaparser.ClassCommentLibrary;
import jig.infrastructure.javaparser.PackageInfoLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

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
    public void packageDiagram(String classDir, String pattern,
                               @ShellOption(defaultValue = "package-diagram.png") String outputPath) throws Exception {
        Relations relations = analyzeRelations(classDir, pattern, AnalysisTarget.PACKAGE);

        Diagram diagram = diagramService.generateFrom(relations);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(outputPath)))) {
            outputStream.write(diagram.getBytes());
        }
    }

    @ShellMethod("クラス依存図")
    public void classDiagram(String classDir, String pattern,
                             @ShellOption(defaultValue = "class-diagram.png") String outputPath) throws Exception {
        Relations relations = analyzeRelations(classDir, pattern, AnalysisTarget.CLASS);

        Diagram diagram = diagramService.generateFrom(relations);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(outputPath)))) {
            outputStream.write(diagram.getBytes());
        }
    }

    @Autowired
    JapaneseNameDictionary dictionary;

    @ShellMethod("日本語読み込み（package-info Javadoc）")
    public void readPackageInfoJavadoc(@ShellOption(defaultValue = "./src/main/java") String sourceDirectory) {
        PackageInfoLibrary packageInfoLibrary = new PackageInfoLibrary(Paths.get(sourceDirectory));
        dictionary.merge(packageInfoLibrary.borrow());
    }

    @ShellMethod("日本語読み込み（class Javadoc）")
    public void readClassJavadoc(@ShellOption(defaultValue = "./src/main/java") String sourceDirectory) {
        ClassCommentLibrary packageInfoLibrary = new ClassCommentLibrary(Paths.get(sourceDirectory));
        dictionary.merge(packageInfoLibrary.borrow());
    }

    @ShellMethod("日本語辞書表示")
    public String showJapaneseDictionary() {
        return dictionary.asText();
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
