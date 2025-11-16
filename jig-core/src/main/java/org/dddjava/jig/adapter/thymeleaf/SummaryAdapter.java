package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.mermaid.EntrypointMermaidDiagram;
import org.dddjava.jig.adapter.mermaid.UsecaseMermaidDiagram;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;

import java.nio.file.Path;
import java.util.List;

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
        var jigTypes = jigService.coreDomainJigTypes(jigRepository);
        var enumModels = jigRepository.jigDataProvider().fetchEnumModels();
        var coreTypesAndRelations = jigService.coreTypesAndRelations(jigRepository);
        var packages = jigService.packages(jigRepository);
        return write(jigDocument, SummaryModel.forDomainSummary(jigTypes, packages, coreTypesAndRelations, enumModels));
    }

    @HandleDocument(JigDocument.ApplicationSummary)
    public List<Path> applicationSummary(JigRepository jigRepository, JigDocument jigDocument) {
        var jigTypes = jigService.serviceTypes(jigRepository);
        return write(jigDocument, SummaryModel.of(jigTypes, jigService.packages(jigRepository)));
    }

    @HandleDocument(JigDocument.UsecaseSummary)
    public List<Path> usecaseSummary(JigRepository jigRepository, JigDocument jigDocument) {
        var jigTypes = jigService.serviceTypes(jigRepository);
        var usecaseMermaidDiagram = new UsecaseMermaidDiagram(jigTypes, MethodRelations.lambdaInlined(jigTypes));
        var packages = jigService.packages(jigRepository);
        return write(jigDocument, SummaryModel.withMermaidDiagram(jigTypes, packages, usecaseMermaidDiagram));
    }

    @HandleDocument(JigDocument.Sequence)
    public List<Path> sequence(JigRepository jigRepository, JigDocument jigDocument) {
        var jigTypes = jigService.serviceTypes(jigRepository);
        return write(jigDocument, SummaryModel.of(jigTypes, jigService.packages(jigRepository)));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public List<Path> entrypointSummary(JigRepository jigRepository, JigDocument jigDocument) {
        var contextJigTypes = jigService.jigTypes(jigRepository);
        var inputAdapters = jigService.inputAdapters(jigRepository);
        var jigTypes = inputAdapters.jigTypes();
        var entrypointMermaidDiagram = new EntrypointMermaidDiagram(inputAdapters, contextJigTypes);
        var packages = jigService.packages(jigRepository);
        return write(jigDocument, SummaryModel.withMermaidDiagram(jigTypes, packages, entrypointMermaidDiagram));
    }

    @HandleDocument(JigDocument.EnumSummary)
    public List<Path> enumSummary(JigRepository jigRepository, JigDocument jigDocument) {
        var categoryTypes = jigService.categoryTypes(jigRepository);
        var packages = jigService.packages(jigRepository);
        var enumModels = jigRepository.jigDataProvider().fetchEnumModels();
        return write(jigDocument, SummaryModel.forEnumSummary(categoryTypes, packages, enumModels));
    }

    private List<Path> write(JigDocument jigDocument, SummaryModel result) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
