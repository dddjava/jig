package org.dddjava.jig.adapter;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSource;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;

public class SummaryAdapter {

    private final JigService jigService;

    public SummaryAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @HandleDocument(JigDocument.DomainSummary)
    public SummaryModel summaryModel(JigSource jigSource) {
        return SummaryModel.from(
                jigService.jigTypes(jigSource),
                jigService.businessRules(jigSource));
    }

    @HandleDocument({JigDocument.ApplicationSummary, JigDocument.UsecaseSummary})
    public SummaryModel servicesSummary(JigSource jigSource) {
        return SummaryModel.from(jigService.services(jigSource));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigSource jigSource) {
        return SummaryModel.from(jigService.jigTypes(jigSource), jigService.entrypoint(jigSource));
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigSource jigSource) {
        return SummaryModel.from(jigService.jigTypes(jigSource), jigService.categoryTypes(jigSource), jigSource.enumModels());
    }
}
