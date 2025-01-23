package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSource;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
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
        JigTypes supportJigTypes = jigService.jigTypes(jigSource);
        JigTypes jigTypes = jigService.coreDomainJigTypes(jigSource);
        return new SummaryModel(supportJigTypes, jigTypes, new EnumModels(List.of()));
    }

    @HandleDocument({JigDocument.ApplicationSummary, JigDocument.UsecaseSummary})
    public SummaryModel servicesSummary(JigSource jigSource) {
        JigTypes jigTypes = jigService.serviceTypes(jigSource);
        return new SummaryModel(jigTypes, jigTypes, new EnumModels(List.of()));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigSource jigSource) {
        JigTypes supportJigTypes = jigService.jigTypes(jigSource);
        Entrypoint entrypoint = jigService.entrypoint(jigSource);
        JigTypes jigTypes = entrypoint.jigTypes();
        var summaryModel = new SummaryModel(supportJigTypes, jigTypes, new EnumModels(List.of()));
        summaryModel.mermaidMap = entrypoint.mermaidMap(supportJigTypes);
        return summaryModel;
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigSource jigSource) {
        JigTypes supportJigTypes = jigService.jigTypes(jigSource);
        CategoryTypes categoryTypes = jigService.categoryTypes(jigSource);
        EnumModels enumModels = jigSource.enumModels();
        return new SummaryModel(supportJigTypes, categoryTypes.jigTypes(), enumModels);
    }

    @Override
    public List<Path> write(SummaryModel result, JigDocument jigDocument) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
