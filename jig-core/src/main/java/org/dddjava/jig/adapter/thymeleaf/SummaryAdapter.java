package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.mermaid.EntrypointMermaidDiagram;
import org.dddjava.jig.adapter.mermaid.TypeRelationMermaidDiagram;
import org.dddjava.jig.adapter.mermaid.UsecaseMermaidDiagram;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@HandleDocument
public class SummaryAdapter implements Adapter {

    private final JigService jigService;
    private final ThymeleafSummaryWriter thymeleafSummaryWriter;

    public SummaryAdapter(JigService jigService, ThymeleafSummaryWriter thymeleafSummaryWriter) {
        this.jigService = jigService;
        this.thymeleafSummaryWriter = thymeleafSummaryWriter;
    }

    @HandleDocument(JigDocument.DomainSummary)
    public SummaryModel domainSummary(JigRepository jigRepository) {
        JigTypes jigTypes = jigService.coreDomainJigTypes(jigRepository);
        return SummaryModel.of(jigTypes, jigService.packages(jigRepository)).withAdditionalMap(Map.of(TypeRelationMermaidDiagram.CONTEXT_KEY, jigService.coreTypesAndRelations(jigRepository)));
    }

    @HandleDocument(JigDocument.ApplicationSummary)
    public SummaryModel applicationSummary(JigRepository jigRepository) {
        JigTypes jigTypes = jigService.serviceTypes(jigRepository);
        return SummaryModel.of(jigTypes, jigService.packages(jigRepository));
    }

    @HandleDocument(JigDocument.UsecaseSummary)
    public SummaryModel usecaseSummary(JigRepository jigRepository) {
        JigTypes jigTypes = jigService.serviceTypes(jigRepository);
        var usecaseMermaidDiagram = new UsecaseMermaidDiagram(jigTypes, MethodRelations.lambdaInlined(jigTypes));
        return SummaryModel.of(jigTypes, jigService.packages(jigRepository)).withAdditionalMap(Map.of("mermaidDiagram", usecaseMermaidDiagram));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigRepository jigRepository) {
        JigTypes contextJigTypes = jigService.jigTypes(jigRepository);
        InputAdapters inputAdapters = jigService.entrypoint(jigRepository);
        JigTypes jigTypes = inputAdapters.jigTypes();
        var entrypointMermaidDiagram = new EntrypointMermaidDiagram(inputAdapters, contextJigTypes);
        return SummaryModel.of(jigTypes, jigService.packages(jigRepository)).withAdditionalMap(Map.of("mermaidDiagram", entrypointMermaidDiagram));
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigRepository jigRepository) {
        JigTypes categoryTypes = jigService.categoryTypes(jigRepository);
        return SummaryModel.of(categoryTypes, jigService.packages(jigRepository)).withAdditionalMap(Map.of("enumModelMap", jigRepository.jigDataProvider().fetchEnumModels().toMap()));
    }

    @Override
    public List<Path> write(Object result, JigDocument jigDocument) {
        return thymeleafSummaryWriter.write(jigDocument, (SummaryModel) result);
    }
}
