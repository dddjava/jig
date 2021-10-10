package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.parts.term.Term;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("TERM")
public class TermReport {

    Term term;

    public TermReport(Term term) {
        this.term = term;
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "用語（英名）", order = 1)
    public String simpleName() {
        return term.identifier().simpleText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "用語", order = 2)
    public String term() {
        return term.title();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "説明", order = 3)
    public String description() {
        return term.description();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "種類", order = 4)
    public String kind() {
        return term.termKind().toString();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "識別子", order = 5)
    public String identifier() {
        return term.identifier().asText();
    }
}
