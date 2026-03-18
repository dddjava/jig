package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;

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

    private List<Path> write(JigDocument jigDocument, SummaryModel result) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
