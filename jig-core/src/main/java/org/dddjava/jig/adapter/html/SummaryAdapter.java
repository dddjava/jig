package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.html.mermaid.EntrypointMermaidDiagram;
import org.dddjava.jig.adapter.html.mermaid.UsecaseMermaidDiagram;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;

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
    public SummaryModel summaryModel(JigDataProvider jigDataProvider) {
        JigTypes jigTypes = jigService.coreDomainJigTypes(jigDataProvider);
        return new SummaryModel(jigTypes);
    }

    @HandleDocument(JigDocument.ApplicationSummary)
    public SummaryModel applicationSummary(JigDataProvider jigDataProvider) {
        JigTypes jigTypes = jigService.serviceTypes(jigDataProvider);
        return new SummaryModel(jigTypes);
    }

    @HandleDocument(JigDocument.UsecaseSummary)
    public SummaryModel usecaseSummary(JigDataProvider jigDataProvider) {
        JigTypes jigTypes = jigService.serviceTypes(jigDataProvider);
        var usecaseMermaidDiagram = new UsecaseMermaidDiagram(jigTypes, jigTypes.methodRelations().inlineLambda());
        return new SummaryModel(jigTypes, Map.of("mermaidDiagram", usecaseMermaidDiagram));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigDataProvider jigDataProvider) {
        JigTypes contextJigTypes = jigService.jigTypes(jigDataProvider);
        Entrypoint entrypoint = jigService.entrypoint(jigDataProvider);
        JigTypes jigTypes = entrypoint.jigTypes();
        var entrypointMermaidDiagram = new EntrypointMermaidDiagram(entrypoint, contextJigTypes);
        return new SummaryModel(jigTypes, Map.of("mermaidDiagram", entrypointMermaidDiagram));
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigDataProvider jigDataProvider) {
        CategoryTypes categoryTypes = jigService.categoryTypes(jigDataProvider);
        return new SummaryModel(categoryTypes.jigTypes(), Map.of("enumModels", jigDataProvider.fetchEnumModels()));
    }

    @Override
    public List<Path> write(SummaryModel result, JigDocument jigDocument) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
