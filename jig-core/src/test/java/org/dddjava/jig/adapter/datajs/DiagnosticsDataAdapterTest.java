package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.domain.model.documents.Diagnostic;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.LocalizedMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiagnosticsDataAdapterTest {

    @Test
    void 表示時に言語を切り替えられるようロケールで解決せず日英の両方を出力する() {
        var diagnostic = new Diagnostic("ハンドラメソッドが見つからない", false,
                List.of(JigDocument.InboundInterface), new LocalizedMessage("見つかりません", "not found"));

        String json = DiagnosticsDataAdapter.buildJson(List.of(diagnostic));

        assertEquals("""
                {"diagnostics":[{"code":"ハンドラメソッドが見つからない","error":false,\
                "jigDocuments":["InboundInterface"],"ja":"見つかりません","en":"not found"}]}""", json);
    }

    @Test
    void 検出した事象がなければ空の配列を出力する() {
        assertEquals("""
                {"diagnostics":[]}""", DiagnosticsDataAdapter.buildJson(List.of()));
    }
}
