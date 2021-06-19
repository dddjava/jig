package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import testing.JigTestExtension;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(JigTestExtension.class)
class JigDocumentHandlersTest {
    @TempDir
    Path outputDirectory;

    @ParameterizedTest
    @EnumSource(value = JigDocument.class, mode = EnumSource.Mode.EXCLUDE, names = "Summary")
    void JigDocumentHandlerですべてのJigDocumentが処理できること(JigDocument jigDocument, JigDocumentHandlers sut) {
        HandleResult handle = sut.handle(jigDocument, outputDirectory);

        // sourceを読み込んでいないのですべて空でスキップされて出力されない
        assertEquals("skip", handle.failureMessage);
    }

    @Disabled
    @Test
    void indexHTMLがUTF8で出力されていること(JigDocumentHandlers sut) throws Exception {
        // defaultCharsetはstaticフィールドにキャッシュされるため、無理矢理クリアする
        System.setProperty("file.encoding", "us-ascii");
        Field field = Charset.class.getDeclaredField("defaultCharset");
        field.setAccessible(true);
        field.set(null, null);

        List<HandleResult> results = Collections.singletonList(new HandleResult(JigDocument.DomainSummary, Collections.singletonList(outputDirectory.resolve("dummy"))));
        sut.writeIndexHtml(outputDirectory, results);

        Path actualPath = outputDirectory.resolve("index.html");
        String text = new String(Files.readAllBytes(actualPath), StandardCharsets.UTF_8);

        assertTrue(text.contains("概要: HTML"));
        assertTrue(text.contains("ドメイン概要"));
    }
}