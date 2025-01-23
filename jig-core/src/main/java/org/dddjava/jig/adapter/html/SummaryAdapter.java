package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSource;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.nio.file.Path;
import java.util.List;

public class SummaryAdapter implements Adapter<SummaryModel> {

    private final JigService jigService;
    private final ThymeleafSummaryWriter thymeleafSummaryWriter;

    public SummaryAdapter(JigService jigService, ThymeleafSummaryWriter thymeleafSummaryWriter) {
        this.jigService = jigService;
        this.thymeleafSummaryWriter = thymeleafSummaryWriter;
    }

    @HandleDocument(JigDocument.DomainSummary)
    public SummaryModel summaryModel(JigSource jigSource) {
        return SummaryModel.from(
                jigService.jigTypes(jigSource),
                jigService.coreDomainJigTypes(jigSource));
    }

    @HandleDocument({JigDocument.ApplicationSummary, JigDocument.UsecaseSummary})
    public SummaryModel servicesSummary(JigSource jigSource) {
        JigTypes jigTypes = jigService.serviceTypes(jigSource);
        return SummaryModel.from(jigTypes, jigTypes);
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigSource jigSource) {
        return SummaryModel.from(jigService.jigTypes(jigSource), jigService.entrypoint(jigSource));
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigSource jigSource) {
        return SummaryModel.from(jigService.jigTypes(jigSource), jigService.categoryTypes(jigSource), jigSource.enumModels());
    }

    @Override
    public List<Path> write(SummaryModel result, JigDocument jigDocument) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
