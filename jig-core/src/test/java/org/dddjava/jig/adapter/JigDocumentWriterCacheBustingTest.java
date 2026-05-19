package org.dddjava.jig.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JigDocumentWriterCacheBustingTest {

    @TempDir
    Path tempDir;

    @Test
    void HTMLコピー時にローカルアセット参照へバージョンクエリを付与する() throws IOException {
        JigDocumentWriter sut = new JigDocumentWriter(tempDir);
        sut.copyResourceTo("templates/", "glossary.html", tempDir);

        String html = Files.readString(tempDir.resolve("glossary.html"), StandardCharsets.UTF_8);
        String v = "?v=" + sut.assetVersion();

        assertTrue(html.contains("./assets/style.css" + v), html);
        assertTrue(html.contains("./assets/jig-bundle.js" + v), html);
        assertTrue(html.contains("./data/glossary-data.js" + v), html);
        // CDN は対象外
        assertTrue(html.contains("https://cdn.jsdelivr.net/npm/marked@15.0.7/marked.min.js\""), html);
        assertFalse(html.contains("https://cdn.jsdelivr.net/npm/marked@15.0.7/marked.min.js?v="), html);
    }

    @Test
    void インスタンスごとにassetVersionが異なる() throws Exception {
        String first = new JigDocumentWriter(tempDir).assetVersion();
        // System.currentTimeMillis() の解像度より十分長く待つ
        Thread.sleep(5);
        String second = new JigDocumentWriter(tempDir).assetVersion();

        assertNotEquals(first, second, "assetVersion はインスタンスごとに変わる必要がある");
    }

    @Test
    void 非HTMLリソースはバイトコピーのまま変更しない() throws IOException {
        new JigDocumentWriter(tempDir).copyResourceTo("templates/assets/", "style.css", tempDir);

        byte[] copied = Files.readAllBytes(tempDir.resolve("style.css"));
        byte[] original;
        try (var is = JigDocumentWriter.getResourceAsStream("templates/assets/style.css")) {
            original = is.readAllBytes();
        }
        assertTrue(java.util.Arrays.equals(original, copied));
    }
}
