package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.JigDocumentAdapter;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * パッケージ関連
 */
public class PackageRelationAdapter implements JigDocumentAdapter {

    private final JigService jigService;
    private final Path outputDirectory;

    public PackageRelationAdapter(JigService jigService, Path outputDirectory) {
        this.jigService = jigService;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public JigDocument supportedDocument() {
        return JigDocument.PackageRelation;
    }

    @Override
    public List<Path> write(JigDocument jigDocument, JigRepository jigRepository) {
        var jigPackages = jigService.packages(jigRepository);
        var packageRelations = jigService.packageRelations(jigRepository);
        var typeRelationships = jigService.typeRelationships(jigRepository);
        var domainPackageRoots = jigService.coreDomainJigTypes(jigRepository).domainPackageRoots();

        var paths = new ArrayList<Path>();
        paths.add(JigDocumentWriter.writeData(outputDirectory, jigDocument, "packageData", buildJson(jigPackages, packageRelations, domainPackageRoots)));

        var typeRelationsJson = Json.object("relations", Json.arrayObjects(typeRelationships.list().stream()
                .map(relation -> Json.object("from", relation.from().fqn())
                        .and("to", relation.to().fqn()))
                .toList())).build();
        paths.add(JigDocumentWriter.writeData(outputDirectory, "type-relations-data", "typeRelationsData", typeRelationsJson));

        return paths;
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
