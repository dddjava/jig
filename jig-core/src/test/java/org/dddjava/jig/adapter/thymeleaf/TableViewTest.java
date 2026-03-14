package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.html.TableView;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableViewTest {

    @TempDir
    Path tempDir;

    @Test
    void 用語集JSONはHTMLではなくglossaryDataとしてJSに書き出す() throws IOException {
        Files.createDirectories(tempDir.resolve("data"));
        var sut = new TableView(JigDocument.Glossary);

        var glossary = new Glossary(List.of(
                new Term(new TermId("app.Account"), "Account", "desc", TermKind.クラス)
        ));

        var outputPaths = sut.write(tempDir, glossary);

        assertTrue(outputPaths.contains(tempDir.resolve("glossary.html")));
        assertTrue(outputPaths.contains(tempDir.resolve("data/glossary-data.js")));

        String html = Files.readString(tempDir.resolve("glossary.html"), StandardCharsets.UTF_8);
        assertTrue(html.contains("<script src=\"./data/glossary-data.js\"></script>"));
        assertFalse(html.contains("id=\"glossary-data\""));

        String js = Files.readString(tempDir.resolve("data/glossary-data.js"), StandardCharsets.UTF_8);
        assertTrue(js.contains("globalThis.glossaryData = "));
        assertTrue(js.contains("\"terms\""));
        assertTrue(js.contains("Account"));
    }
}

