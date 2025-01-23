package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSource;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

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
        Map<PackageIdentifier, List<JigType>> map = jigService.coreDomainJigTypes(jigSource).stream()
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(supportJigTypes, map, new EnumModels(List.of()));
    }

    @HandleDocument({JigDocument.ApplicationSummary, JigDocument.UsecaseSummary})
    public SummaryModel servicesSummary(JigSource jigSource) {
        JigTypes jigTypes = jigService.serviceTypes(jigSource);
        Map<PackageIdentifier, List<JigType>> map = jigTypes.stream()
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(jigTypes, map, new EnumModels(List.of()));
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public SummaryModel entrypointSummary(JigSource jigSource) {
        JigTypes supportJigTypes = jigService.jigTypes(jigSource);
        Entrypoint entrypoint = jigService.entrypoint(jigSource);
        Map<PackageIdentifier, List<JigType>> map = entrypoint.list().stream()
                .map(entrypointGroup -> entrypointGroup.jigType())
                .collect(groupingBy(JigType::packageIdentifier));

        var summaryModel = new SummaryModel(supportJigTypes, map, new EnumModels(List.of()));
        summaryModel.mermaidMap = entrypoint.mermaidMap(supportJigTypes);
        return summaryModel;
    }

    @HandleDocument(JigDocument.EnumSummary)
    public SummaryModel inputSummary(JigSource jigSource) {
        JigTypes supportJigTypes = jigService.jigTypes(jigSource);
        CategoryTypes categoryTypes = jigService.categoryTypes(jigSource);
        EnumModels enumModels = jigSource.enumModels();
        Map<PackageIdentifier, List<JigType>> map = categoryTypes.list().stream()
                .map(CategoryType::jigType)
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(supportJigTypes, map, enumModels);
    }

    @Override
    public List<Path> write(SummaryModel result, JigDocument jigDocument) {
        return thymeleafSummaryWriter.write(jigDocument, result);
    }
}
