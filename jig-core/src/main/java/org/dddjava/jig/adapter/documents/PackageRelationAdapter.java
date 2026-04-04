package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;

import java.nio.file.Path;
import java.util.List;

/**
 * パッケージ関連
 */
@HandleDocument
public class PackageRelationAdapter {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public PackageRelationAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.PackageRelation)
    public List<Path> invoke(JigRepository jigRepository, JigDocument jigDocument) {
        var jigPackages = jigService.packages(jigRepository);
        var packageRelations = jigService.packageRelations(jigRepository);
        var typeRelationships = jigService.typeRelationships(jigRepository);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        var domainPackageRoots = jigService.coreDomainJigTypes(jigRepository).domainPackageRoots();

        jigDocumentWriter.writeData("packageData", buildJson(jigPackages, packageRelations, domainPackageRoots));

        var typeRelationsJson = Json.object("relations", Json.arrayObjects(typeRelationships.list().stream()
                .map(relation -> Json.object("from", relation.from().fqn())
                        .and("to", relation.to().fqn()))
                .toList())).build();
        jigDocumentWriter.writeData("typeRelationsData", typeRelationsJson, "type-relations-data");

        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(JigPackages jigPackages, PackageRelations packageRelations, List<String> domainPackageRoots) {
        return Json.object("packages", Json.arrayObjects(jigPackages.listPackage().stream()
                        .map(packageInfo -> Json.object("fqn", packageInfo.fqn())
                                .and("classCount", packageInfo.numberOfClasses()))
                        .toList()))
                .and("relations", Json.arrayObjects(packageRelations.listUnique().stream()
                        .map(relation -> Json.object("from", relation.from().asText())
                                .and("to", relation.to().asText()))
                        .toList()))
                .and("domainPackageRoots", Json.array(domainPackageRoots))
                .build();
    }
}
