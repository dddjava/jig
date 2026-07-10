package org.dddjava.jig.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class JigDocumentWriterCacheBustingTest {

    @TempDir
    Path tempDir;

    @Test
    void HTMLコピー時にローカルアセット参照へバージョンクエリを付与する() throws IOException {
        JigDocumentWriter sut = new JigDocumentWriter(tempDir, Locale.JAPANESE);
        sut.copyResourceTo("templates/", "glossary.html", tempDir);

        String html = Files.readString(tempDir.resolve("glossary.html"), StandardCharsets.UTF_8);
        String v = "?v=" + sut.assetVersion();

        assertTrue(html.contains("./assets/common.css" + v), html);
        assertTrue(html.contains("./assets/glossary.css" + v), html);
        assertTrue(html.contains("./assets/jig-bundle.js" + v), html);
        assertTrue(html.contains("./data/glossary-data.js" + v), html);
        // CDN は対象外
        assertTrue(html.contains("https://cdn.jsdelivr.net/npm/marked@15.0.7/marked.min.js\""), html);
        assertFalse(html.contains("https://cdn.jsdelivr.net/npm/marked@15.0.7/marked.min.js?v="), html);
    }

    @Test
    void assetVersionはHTMLのプレースホルダー置換に指定した値がそのまま使われる() {
        JigDocumentWriter sut = new JigDocumentWriter(tempDir, Locale.JAPANESE, "fixed-version");

        String resolved = sut.resolvePlaceholders("<script src=\"./assets/common.css?v={{assetVersion}}\"></script>");

        assertTrue(resolved.contains("?v=fixed-version"), resolved);
    }

    @Test
    void 非HTMLリソースはバイトコピーのまま変更しない() throws IOException {
        new JigDocumentWriter(tempDir, Locale.JAPANESE).copyResourceTo("templates/assets/", "common.css", tempDir);

        byte[] copied = Files.readAllBytes(tempDir.resolve("common.css"));
        byte[] original;
        try (var is = JigDocumentWriter.getResourceAsStream("templates/assets/common.css")) {
            original = is.readAllBytes();
        }
        assertTrue(java.util.Arrays.equals(original, copied));
    }
}
