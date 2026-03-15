package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.html.TableView;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableViewTest {

    @Test
    void 用語集JSONはHTMLではなくglossaryDataとしてJSに書き出す() {
        var glossary = new Glossary(List.of(
                new Term(new TermId("app.Account"), "Account", "desc", TermKind.クラス)
        ));

        String js = "globalThis.glossaryData = " + TableView.buildJson(glossary);

        assertTrue(js.contains("globalThis.glossaryData = "));
        assertTrue(js.contains("\"terms\""));
        assertTrue(js.contains("Account"));
        assertFalse(js.contains("id=\"glossary-data\""));
    }
}
