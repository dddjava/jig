package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.LinkPrefix;
import org.dddjava.jig.domain.model.jigsource.jigloader.SourceCodeAliasReader;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JigDocumentHandlersTest {

    @ParameterizedTest
    @EnumSource(value = JigDocument.class, mode = EnumSource.Mode.MATCH_ALL, names = ".*Diagram")
    void diagrams(JigDocument jigDocument, @TempDir Path temp) throws IOException {
        Configuration configuration = new Configuration(new JigProperties("", "", "", "", new OutputOmitPrefix(""), LinkPrefix.disable()), new SourceCodeAliasReader(null));

        JigDocumentHandlers sut = configuration.documentHandlers();
        HandleResult handle = sut.handle(jigDocument, temp);

        assertEquals("skip", handle.failureMessage);
    }
}