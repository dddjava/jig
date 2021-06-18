package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import testing.JigTestExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JigTestExtension.class)
class JigDocumentHandlersTest {

    @ParameterizedTest
    @EnumSource(value = JigDocument.class, mode = EnumSource.Mode.EXCLUDE, names = "Summary")
    void JigDocumentHandlerですべてのJigDocumentが処理できること(JigDocument jigDocument, @TempDir Path outputDirectory, JigDocumentHandlers sut) throws IOException {

        HandleResult handle = sut.handle(jigDocument, outputDirectory);

        assertEquals("skip", handle.failureMessage);
    }
}