package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.html.mermaid.EntrypointMermaidDiagram;
import org.dddjava.jig.adapter.html.mermaid.UsecaseMermaidDiagram;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigDataProvider;
import org.dddjava.jig.domain.model.information.JigTypesRepository;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.type.JigTypes;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@HandleDocument
public class SummaryAdapter implements Adapter<SummaryModel> {

    private final JigService jigService;
    private final ThymeleafSummaryWriter thymeleafSummaryWriter;

    public SummaryAdapter(JigService jigService, ThymeleafSummaryWriter thymeleafSummaryWriter) {
        this.jigService = jigService;
        this.thymeleafSummaryWriter = thymeleafSummaryWriter;
    }

    @HandleDocument(JigDocument.DomainSummary)
    public SummaryModel summaryModel(JigTypesRepository jigTypesRepository) {
        JigTypes jigTypes = jigService.coreDomainJigTypes(jigTypesRepository);
        return new SummaryModel(jigTypes);
    }

    @HandleDocument(JigDocument.ApplicationSummary)
    public SummaryModel applicationSummary(JigTypesRepository jigTypesRepository) {
        JigTypes jigTypes = jigService.serviceTypes(jigTypesRepository);
        return new SummaryModel(jigTypes);
    }

    @HandleDocument(JigDocument.UsecaseSummary)
    public SummaryModel usecaseSummary(JigTypesRepository jigTypesRepository) {
        JigTypes jigTypes = jigService.serviceTypes(jigTypesRepository);
        var usecaseMermaidDiagram = new UsecaseMermaidDiagram(jigTypes, MethodRelations.from(jigTypes).inlineLambda());
        return new SummaryModel(jigTypes, Map.of("mermaidDiagram", usecaseMermaidDiagram));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigTypesRepository jigTypesRepository) {
        JigTypes contextJigTypes = jigService.jigTypes(jigTypesRepository);
        Entrypoint entrypoint = jigService.entrypoint(jigTypesRepository);
        JigTypes jigTypes = entrypoint.jigTypes();
        var entrypointMermaidDiagram = new EntrypointMermaidDiagram(entrypoint, contextJigTypes);
        return new SummaryModel(jigTypes, Map.of("mermaidDiagram", entrypointMermaidDiagram));
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigDataProvider jigDataProvider) {
        JigTypes categoryTypes = jigService.categoryTypes(jigDataProvider);
        return new SummaryModel(categoryTypes, Map.of("enumModels", jigDataProvider.fetchEnumModels()));
    }

    @Override
    public List<Path> write(SummaryModel result, JigDocument jigDocument) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
