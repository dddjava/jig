package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexViewTest {

    @TempDir
    Path tempDir;

    @Test
    void 出力対象がない場合は一覧セクションを出力しない() throws IOException {
        var sut = new IndexView(templateEngine(), JigDiagramFormat.SVG);

        sut.render(List.of(), tempDir);

        String actual = readIndex();
        assertFalse(actual.contains("<h2>概要: HTML</h2>"));
        assertFalse(actual.contains("<h2>一覧: HTML</h2>"));
        assertFalse(actual.contains("<h2>一覧: Excel</h2>"));
        assertFalse(actual.contains("<section class=\"diagram\""));
    }

    @Test
    void 出力対象がある場合は対応する一覧セクションを出力する() throws IOException {
        var sut = new IndexView(templateEngine(), JigDiagramFormat.SVG);
        var results = List.of(
                HandleResult.withOutput(JigDocument.PackageSummary, List.of(Path.of("package-summary.html"))),
                HandleResult.withOutput(JigDocument.ListOutput, List.of(Path.of("list-output.html"))),
                HandleResult.withOutput(JigDocument.ApplicationList, List.of(Path.of("application-list.xlsx"))),
                HandleResult.withOutput(JigDocument.CategoryDiagram, List.of(Path.of("category.svg")))
        );

        sut.render(results, tempDir);

        String actual = readIndex();
        assertTrue(actual.contains("<h2>概要: HTML</h2>"));
        assertTrue(actual.contains("<h2>一覧: HTML</h2>"));
        assertTrue(actual.contains("<h2>一覧: Excel</h2>"));
        assertTrue(actual.contains("<section class=\"diagram\""));
    }

    private String readIndex() throws IOException {
        return Files.readString(tempDir.resolve(IndexView.INDEX_FILE_NAME), StandardCharsets.UTF_8);
    }

    private TemplateEngine templateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
