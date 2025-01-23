package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.html.mermaid.UsecaseMermaidDiagram;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSource;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
        return new SummaryModel(supportJigTypes, jigTypes);
    }

    @HandleDocument(JigDocument.ApplicationSummary)
    public SummaryModel applicationSummary(JigSource jigSource) {
        JigTypes jigTypes = jigService.serviceTypes(jigSource);
        return new SummaryModel(jigTypes, jigTypes);
    }

    @HandleDocument(JigDocument.UsecaseSummary)
    public SummaryModel usecaseSummary(JigSource jigSource) {
        JigTypes jigTypes = jigService.serviceTypes(jigSource);
        var usecaseMermaidDiagram = new UsecaseMermaidDiagram(jigTypes, jigTypes.methodRelations().inlineLambda());
        return new SummaryModel(jigTypes, jigTypes, Map.of("mermaidDiagram", usecaseMermaidDiagram));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigSource jigSource) {
        JigTypes supportJigTypes = jigService.jigTypes(jigSource);
        Entrypoint entrypoint = jigService.entrypoint(jigSource);
        JigTypes jigTypes = entrypoint.jigTypes();
        var summaryModel = new SummaryModel(supportJigTypes, jigTypes);
        summaryModel.mermaidMap = entrypoint.mermaidMap(supportJigTypes);
        return summaryModel;
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigSource jigSource) {
        JigTypes supportJigTypes = jigService.jigTypes(jigSource);
        CategoryTypes categoryTypes = jigService.categoryTypes(jigSource);
        return new SummaryModel(supportJigTypes, categoryTypes.jigTypes(), Map.of("enumModels", jigSource.enumModels()));
    }

    @Override
    public List<Path> write(SummaryModel result, JigDocument jigDocument) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
