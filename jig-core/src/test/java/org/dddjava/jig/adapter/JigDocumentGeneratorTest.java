package org.dddjava.jig.adapter;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDiagramOption;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testing.xlsx.XlsxAssertions;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JigDocumentGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void 用語一覧がtermをもとに出力できる() {
        // data
        var terms = new Glossary(List.of(
                new Term(new TermId("hoge.fuga.piyo.Fizz"), "ふぃず", "テスト説明", TermKind.クラス),
                new Term(new TermId("hoge.fuga.piyo"), "PIYO", "package-description", TermKind.パッケージ)
        ));
        var jigDataProvider = mock(JigRepository.class); // termはmockで返すようにしているのでここは同じインスタンスであればいいので
        // environment
        var jigDocumentContextMock = mock(JigDocumentContext.class);
        when(jigDocumentContextMock.diagramOption()).thenReturn(new JigDiagramOption(
                JigDiagramFormat.DOT, Duration.ZERO, false
        ));
        var jigServiceMock = mock(JigService.class);
        when(jigServiceMock.glossary(any())).thenReturn(terms);

        var sut = new JigDocumentGenerator(jigDocumentContextMock, jigServiceMock);

        @SuppressWarnings("removal")
        var handleResult = sut.generateDocument(JigDocument.TermList, tempDir, jigDataProvider);

        assert handleResult.success();

        XlsxAssertions.assertTextValues(tempDir.resolve("term.xlsx"),
                List.of(
                        List.of("用語（英名）", "用語", "説明", "種類", "識別子"),
                        List.of("piyo", "PIYO", "package-description", "パッケージ", "hoge.fuga.piyo"),
                        List.of("Fizz", "ふぃず", "テスト説明", "クラス", "hoge.fuga.piyo.Fizz")
                ));
    }
}