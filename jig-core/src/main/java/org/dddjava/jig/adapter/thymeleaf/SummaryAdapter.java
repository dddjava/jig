package org.dddjava.jig.adapter.thymeleaf;

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
public class SummaryAdapter {

    private final JigService jigService;
    private final ThymeleafSummaryWriter thymeleafSummaryWriter;

    public SummaryAdapter(JigService jigService, ThymeleafSummaryWriter thymeleafSummaryWriter) {
        this.jigService = jigService;
        this.thymeleafSummaryWriter = thymeleafSummaryWriter;
    }

    @HandleDocument(JigDocument.DomainSummary)
    public List<Path> domainSummary(JigRepository jigRepository, JigDocument jigDocument) {
        JigTypes jigTypes = jigService.coreDomainJigTypes(jigRepository);
        return write(jigDocument, SummaryModel.of(jigTypes, jigService.packages(jigRepository)).withAdditionalMap(Map.of(TypeRelationMermaidDiagram.CONTEXT_KEY, jigService.coreTypesAndRelations(jigRepository))));
    }

    @HandleDocument(JigDocument.ApplicationSummary)
    public List<Path> applicationSummary(JigRepository jigRepository, JigDocument jigDocument) {
        JigTypes jigTypes = jigService.serviceTypes(jigRepository);
        return write(jigDocument, SummaryModel.of(jigTypes, jigService.packages(jigRepository)));
    }

    @HandleDocument(JigDocument.UsecaseSummary)
    public List<Path> usecaseSummary(JigRepository jigRepository, JigDocument jigDocument) {
        JigTypes jigTypes = jigService.serviceTypes(jigRepository);
        var usecaseMermaidDiagram = new UsecaseMermaidDiagram(jigTypes, MethodRelations.lambdaInlined(jigTypes));
        return write(jigDocument, SummaryModel.of(jigTypes, jigService.packages(jigRepository)).withAdditionalMap(Map.of("mermaidDiagram", usecaseMermaidDiagram)));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public List<Path> entrypointSummary(JigRepository jigRepository, JigDocument jigDocument) {
        JigTypes contextJigTypes = jigService.jigTypes(jigRepository);
        InputAdapters inputAdapters = jigService.entrypoint(jigRepository);
        JigTypes jigTypes = inputAdapters.jigTypes();
        var entrypointMermaidDiagram = new EntrypointMermaidDiagram(inputAdapters, contextJigTypes);
        return write(jigDocument, SummaryModel.of(jigTypes, jigService.packages(jigRepository)).withAdditionalMap(Map.of("mermaidDiagram", entrypointMermaidDiagram)));
    }

    @HandleDocument(JigDocument.EnumSummary)
    public List<Path> inputSummary(JigRepository jigRepository, JigDocument jigDocument) {
        JigTypes categoryTypes = jigService.categoryTypes(jigRepository);
        return write(jigDocument, SummaryModel.of(categoryTypes, jigService.packages(jigRepository)).withAdditionalMap(Map.of("enumModelMap", jigRepository.jigDataProvider().fetchEnumModels().toMap())));
    }

    private List<Path> write(JigDocument jigDocument, SummaryModel result) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
