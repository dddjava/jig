package org.dddjava.jig.adapter.poi;

import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.nio.file.Path;
import java.util.List;

/**
 * 用語集一覧のAdapter
 *
 * @see org.dddjava.jig.domain.model.documents.documentformat.JigDocument#TermList
 */
public class GlossaryAdapter {

    private static List<ReportItem<Term>> reporter() {
        return List.of(
                ReportItem.ofString("用語（英名）", term -> term.simpleText()),
                ReportItem.ofString("用語", term -> term.title()),
                ReportItem.ofString("説明", term -> term.description()),
                ReportItem.ofString("種類", term -> term.termKind().name()),
                ReportItem.ofString("識別子", term -> term.id().asText())
        );
    }

    public static List<Path> invoke(Glossary glossary, JigDocument jigDocument, Path outputDirectory) {
        var modelReports = new ReportBook(new ReportSheet<>("TERM", GlossaryAdapter.reporter(), glossary.list()));
        return modelReports.writeXlsx(jigDocument, outputDirectory);
    }
}
