package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * パッケージ概要
 */
public class PackageSummaryView {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public PackageSummaryView(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.PackageSummary)
    public List<Path> invoke(JigRepository jigRepository, JigDocument jigDocument) {
        var jigPackages = jigService.packages(jigRepository);
        var packageRelations = jigService.packageRelations(jigRepository);
        var typeRelationships = jigService.typeRelationships(jigRepository);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("packageData", buildJson(jigPackages, packageRelations, typeRelationships));

        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(JigPackages jigPackages, PackageRelations packageRelations, TypeRelationships typeRelationships) {
        String packagesJson = jigPackages.listPackage().stream()
                .map(packageInfo -> Json.object("fqn", packageInfo.fqn())
                        .and("name", packageInfo.label())
                        .and("description", packageInfo.term().description())
                        .and("classCount", packageInfo.numberOfClasses())
                        .build())
                .collect(Collectors.joining(",", "[", "]"));

        String packageRelationsJson = packageRelations.listUnique().stream()
                .map(PackageSummaryView::formatRelationJson)
                .collect(Collectors.joining(",", "[", "]"));

        String typeRelationsJson = typeRelationships.list().stream()
                .map(PackageSummaryView::formatTypeRelationJson)
                .collect(Collectors.joining(",", "[", "]"));

        return """
                {"packages": %s, "relations": %s, "causeRelationEvidence": %s}
                """.formatted(packagesJson, packageRelationsJson, typeRelationsJson);
    }

    private static String formatRelationJson(PackageRelation relation) {
        return Json.object("from", relation.from().asText())
                .and("to", relation.to().asText())
                .build();
    }

    private static String formatTypeRelationJson(TypeRelationship relation) {
        return Json.object("from", relation.from().fqn())
                .and("to", relation.to().fqn())
                .build();
    }
}
