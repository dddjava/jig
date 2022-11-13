package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import testing.JigTestExtension;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(JigTestExtension.class)
class JigDocumentHandlersTest {
    @TempDir
    Path outputDirectory;

    @EnabledForJreRange(max = JRE.JAVA_11) // FIXME 17で動作しない
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
        String text = Files.readString(actualPath);

        assertTrue(text.contains("概要: HTML"));
        assertTrue(text.contains("ドメイン概要"));
    }
}