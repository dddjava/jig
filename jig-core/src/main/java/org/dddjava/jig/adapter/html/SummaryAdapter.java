package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.html.mermaid.EntrypointMermaidDiagram;
import org.dddjava.jig.adapter.html.mermaid.UsecaseMermaidDiagram;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

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
    public SummaryModel summaryModel(JigRepository jigRepository) {
        JigTypes jigTypes = jigService.coreDomainJigTypes(jigRepository);
        return new SummaryModel(jigTypes);
    }

    @HandleDocument(JigDocument.ApplicationSummary)
    public SummaryModel applicationSummary(JigRepository jigRepository) {
        JigTypes jigTypes = jigService.serviceTypes(jigRepository);
        return new SummaryModel(jigTypes);
    }

    @HandleDocument(JigDocument.UsecaseSummary)
    public SummaryModel usecaseSummary(JigRepository jigRepository) {
        JigTypes jigTypes = jigService.serviceTypes(jigRepository);
        var usecaseMermaidDiagram = new UsecaseMermaidDiagram(jigTypes, MethodRelations.from(jigTypes).inlineLambda());
        return new SummaryModel(jigTypes, Map.of("mermaidDiagram", usecaseMermaidDiagram));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigRepository jigRepository) {
        JigTypes contextJigTypes = jigService.jigTypes(jigRepository);
        Entrypoints entrypoints = jigService.entrypoint(jigRepository);
        JigTypes jigTypes = entrypoints.jigTypes();
        var entrypointMermaidDiagram = new EntrypointMermaidDiagram(entrypoints, contextJigTypes);
        return new SummaryModel(jigTypes, Map.of("mermaidDiagram", entrypointMermaidDiagram));
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigRepository jigRepository) {
        JigTypes categoryTypes = jigService.categoryTypes(jigRepository);
        return new SummaryModel(categoryTypes, Map.of("enumModels", jigRepository.jigDataProvider().fetchEnumModels()));
    }

    @Override
    public List<Path> write(SummaryModel result, JigDocument jigDocument) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
