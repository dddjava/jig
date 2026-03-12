package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import testing.JigTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

@JigTest
class OutputsSummaryAdapterTest {

    @Test
    void 外部利用概要は連結データJSONを埋め込む(JigService jigService,
                              JigDocumentContext jigDocumentContext,
                              JigRepository jigRepository) throws IOException {
        var sut = new OutputsSummaryAdapter(jigService, templateEngine(), jigDocumentContext);

        var outputPaths = sut.invoke(jigRepository, JigDocument.OutputsSummary);
        String actual = Files.readString(outputPaths.getFirst(), StandardCharsets.UTF_8);

        assertTrue(actual.contains("id=\"outputs-data\""));
        assertTrue(actual.contains("\"links\":"));
        assertTrue(actual.contains("\"ports\":"));
        assertTrue(actual.contains("\"operations\":"));
        assertTrue(actual.contains("\"adapters\":"));
        assertTrue(actual.contains("\"executions\":"));
        assertTrue(actual.contains("\"persistenceAccessors\":"));
        assertTrue(actual.contains("\"group\":"));
        assertTrue(actual.contains("stub.infrastructure.datasource.springdata.SpringDataJdbcNameRepository.save"));

        assertTrue(actual.contains("タブ共通の設定"));
        assertTrue(actual.contains("name=\"display-mode\""));
        assertTrue(!actual.contains("in-page-sidebar__section outputs-display-mode"));

        int commonSettingsIndex = actual.indexOf("タブ共通の設定");
        int sidebarIndex = actual.indexOf("id=\"outputs-sidebar\"");
        assertTrue(commonSettingsIndex != -1 && sidebarIndex != -1 && commonSettingsIndex < sidebarIndex);
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
