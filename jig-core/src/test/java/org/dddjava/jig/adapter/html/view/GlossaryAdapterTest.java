package org.dddjava.jig.adapter.html.view;

import org.dddjava.jig.adapter.html.GlossaryAdapter;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlossaryAdapterTest {

    @Test
    void 用語集JSONはHTMLではなくglossaryDataとしてJSに書き出す() {
        var glossary = new Glossary(List.of(
                new Term(new TermId("app.Account"), "Account", "desc", TermKind.クラス)
        ));

        String js = "globalThis.glossaryData = " + GlossaryAdapter.buildJson(glossary, List.of());

        assertTrue(js.contains("globalThis.glossaryData = "));
        assertTrue(js.contains("\"app.Account\""));
        assertTrue(js.contains("Account"));
        assertFalse(js.contains("id=\"glossary-data\""));
    }
}
