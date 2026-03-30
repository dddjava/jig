package org.dddjava.jig.adapter.html.view;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.adapter.html.IndexView;
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

class IndexViewTest {

    @TempDir
    Path tempDir;

    @Test
    void 出力対象がない場合は一覧セクションを出力しない() throws IOException {
        var sut = new IndexView();

        sut.render(List.of(), tempDir);

        String actual = readIndex();
        assertFalse(actual.contains("<h2>設計情報: HTML</h2>"));
        assertFalse(actual.contains("<h2>一覧: HTML</h2>"));

        String navigationData = readNavigationData();
        assertTrue(navigationData.contains("globalThis.navigationData"));
        assertFalse(navigationData.contains("\"href\":\"package.html\""));
    }

    @Test
    void 出力対象がある場合は対応する一覧セクションを出力する() throws IOException {
        var sut = new IndexView();
        var results = List.of(
                HandleResult.withOutput(JigDocument.PackageRelation, List.of(Path.of("package.html"))),
                HandleResult.withOutput(JigDocument.ListOutput, List.of(Path.of("list-output.html")))
        );

        sut.render(results, tempDir);

        String navigationData = readNavigationData();
        assertTrue(navigationData.contains("\"href\":\"package.html\""));
        assertTrue(navigationData.contains("\"href\":\"list-output.html\""));
    }

    private String readIndex() throws IOException {
        return Files.readString(tempDir.resolve(IndexView.INDEX_FILE_NAME), StandardCharsets.UTF_8);
    }

    private String readNavigationData() throws IOException {
        return Files.readString(tempDir.resolve("data").resolve(IndexView.NAVIGATION_DATA_JS), StandardCharsets.UTF_8);
    }
}
