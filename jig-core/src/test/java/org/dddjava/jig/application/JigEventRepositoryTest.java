package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.documents.Diagnostic;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.JigIssue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JigEventRepositoryTest {

    @Test
    void 記録がなければ診断も空() {
        var sut = new JigEventRepository(Locale.JAPANESE);

        assertTrue(sut.diagnostics().isEmpty());
    }

    @Test
    void 解析結果由来の事象は内容が欠けるドキュメントに紐づける() {
        var sut = new JigEventRepository(Locale.JAPANESE);
        sut.recordIssue(JigIssue.ハンドラメソッドなし);

        List<Diagnostic> diagnostics = sut.diagnostics();

        assertEquals(1, diagnostics.size());
        assertEquals(List.of(JigDocument.InboundInterface, JigDocument.ListOutput), diagnostics.get(0).jigDocuments());
        assertEquals(false, diagnostics.get(0).error());
    }

    @Test
    void 解析が成立しない事象はerrorとして全ドキュメントに関わるものにする() {
        var sut = new JigEventRepository(Locale.JAPANESE);
        sut.recordIssue(JigIssue.バイナリソースなし);

        Diagnostic diagnostic = sut.diagnostics().get(0);

        assertTrue(diagnostic.error());
        assertTrue(diagnostic.jigDocuments().isEmpty(), "特定ドキュメントに限らず全体に影響する");
    }

    @Test
    void 読み取り由来の事象は内容が欠けるドキュメントに紐づける() {
        var sut = new JigEventRepository(Locale.JAPANESE);
        sut.recordIssue(JigIssue.SQLなし);

        Diagnostic diagnostic = sut.diagnostics().get(0);

        assertEquals(false, diagnostic.error());
        assertEquals(List.of(JigDocument.OutboundInterface), diagnostic.jigDocuments());
    }
}
