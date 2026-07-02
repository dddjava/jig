package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.datajs.GlossaryDataAdapter;
import org.dddjava.jig.domain.model.data.terms.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlossaryAdapterTest {

    @Test
    void 用語集JSONはHTMLではなくglossaryDataとしてJSに書き出す() {
        var glossary = new Glossary(List.of(
                new Term(new TermId("app.Account"), "Account", "desc", TermKind.クラス, TermOrigin.その他)
        ));

        String js = "globalThis.glossaryData = " + GlossaryDataAdapter.buildGlossaryJson(glossary, List.of());

        assertTrue(js.contains("globalThis.glossaryData = "));
        assertTrue(js.contains("\"app.Account\""));
        assertTrue(js.contains("Account"));
        assertFalse(js.contains("id=\"glossary-data\""));
    }

    @Test
    void 用語の有無に関わらず全型のソースパスをsourcePathsとして書き出す() {
        var glossary = new Glossary(List.of(
                new Term(new TermId("app.Account"), "Account", "desc", TermKind.クラス, TermOrigin.その他)
        ));

        String json = GlossaryDataAdapter.buildGlossaryJson(glossary, List.of(), Map.of(
                "app.Account", "src/main/java/app/Account.java",
                "app.NoJavadoc", "src/main/java/app/NoJavadoc.java"
        ));

        assertTrue(json.contains("\"sourcePaths\":"));
        assertTrue(json.contains("\"app.Account\":\"src/main/java/app/Account.java\""));
        assertTrue(json.contains("\"app.NoJavadoc\":\"src/main/java/app/NoJavadoc.java\""));
    }
}
