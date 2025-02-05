package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.term.TermKind;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testing.xlsx.XlsxAssertions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JigDocumentGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void 用語一覧がtermをもとに出力できる() throws IOException {
        // data
        var terms = new Glossary(List.of(
                new Term(new TermIdentifier("hoge.fuga.piyo.Fizz"), "ふぃず", "テスト説明", TermKind.クラス),
                new Term(new TermIdentifier("hoge.fuga.piyo"), "PIYO", "package-description", TermKind.パッケージ)
        ));
        JigDataProvider jigDataProvider = mock(JigDataProvider.class); // termはmockで返すようにしているのでここは同じインスタンスであればいいので
        // environment
        var jigDocumentContextMock = mock(JigDocumentContext.class);
        var jigServiceMock = mock(JigService.class);
        when(jigServiceMock.terms(jigDataProvider)).thenReturn(terms);

        var sut = new JigDocumentGenerator(jigDocumentContextMock, jigServiceMock);

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