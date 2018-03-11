package jig.shell;

import jig.application.service.DiagramService;
import jig.domain.model.diagram.Diagram;
import jig.domain.model.jdeps.*;
import jig.domain.model.relation.Relations;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModelBuilder;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

@ShellComponent
public class DiagramCommands {

    private static final Logger LOGGER = Logger.getLogger(DiagramCommands.class.getName());

    RelationAnalyzer relationAnalyzer;
    DiagramService diagramService;

    private String classDir = "build/classes/java/main";
    private String pattern = ".+\\.model\\..+";
    private AnalysisTarget kind = AnalysisTarget.PACKAGE;

    public DiagramCommands(RelationAnalyzer relationAnalyzer, DiagramService diagramService) {
        this.relationAnalyzer = relationAnalyzer;
        this.diagramService = diagramService;
    }

    @ShellMethod("読み取りディレクトリ、解析対象パターン、CLASS/PACKAGEを指定してjdepsを実行します")
    public void jdeps(String classDir, String pattern, AnalysisTarget target) {
        analyzeRelations(classDir, pattern, target);
    }

    @ShellMethod("設定の確認と上書き")
    public Table diagramSetting(@ShellOption(defaultValue = ShellOption.NULL) String classDir,
                                @ShellOption(defaultValue = ShellOption.NULL) String pattern,
                                @ShellOption(defaultValue = ShellOption.NULL) String kind) {
        if (classDir != null) {
            this.classDir = classDir;
        }
        if (pattern != null) {
            this.pattern = pattern;
        }
        if (kind != null) {
            this.kind = AnalysisTarget.valueOf(kind.toUpperCase());
        }

        return new TableBuilder(
                new TableModelBuilder<>()
                        .addRow().addValue("class directory").addValue(this.classDir)
                        .addRow().addValue("target pattern").addValue(this.pattern)
                        .addRow().addValue("output kind").addValue(this.kind)
                        .build())
                .addFullBorder(BorderStyle.fancy_light)
                .build();
    }

    @ShellMethod("ダイアグラムを出力")
    public void diagramOutput(@ShellOption(defaultValue = "diagram.png") String outputPath) throws Exception {
        LOGGER.info("classDir: " + classDir);
        LOGGER.info("pattern : " + pattern);
        LOGGER.info("kind    : " + kind);

        Relations relations = analyzeRelations(classDir, pattern, kind);
        Diagram diagram = diagramService.generateFrom(relations);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(outputPath)))) {
            outputStream.write(diagram.getBytes());
        }

        LOGGER.info(outputPath + " を出力しました。");
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
