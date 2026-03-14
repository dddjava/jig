package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * パッケージ概要
 */
public class PackageSummaryView {

    private final JigDocument jigDocument;

    public PackageSummaryView(JigDocument jigDocument) {
        this.jigDocument = jigDocument;
    }

    public List<Path> write(Path outputDirectory, JigPackages jigPackages, PackageRelations packageRelations, TypeRelationships typeRelationships) {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);

        String packagesJson = jigPackages.listPackage().stream()
                .map(packageInfo -> Json.object("fqn", packageInfo.fqn())
                        .and("name", packageInfo.label())
                        .and("description", packageInfo.term().description())
                        .and("classCount", packageInfo.numberOfClasses())
                        .build())
                .collect(Collectors.joining(",", "[", "]"));

        String packageRelationsJson = packageRelations.listUnique().stream()
                .map(this::formatRelationJson)
                .collect(Collectors.joining(",", "[", "]"));

        String typeRelationsJson = typeRelationships.list().stream()
                .map(this::formatTypeRelationJson)
                .collect(Collectors.joining(",", "[", "]"));

        String packageSummaryJson = """
                {"packages": %s, "relations": %s, "causeRelationEvidence": %s}
                """.formatted(packagesJson, packageRelationsJson, typeRelationsJson);

        String fileName = jigDocumentWriter.jigDocument().fileName();
        jigDocumentWriter.write(
                outputStream -> {
                    try (var resource = PackageSummaryView.class.getResourceAsStream("/templates/" + fileName + ".html")) {
                        Objects.requireNonNull(resource).transferTo(outputStream);
                    }
                },
                fileName + ".html"
        );
        jigDocumentWriter.write(
                outputStream -> {
                    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        writer.write("globalThis.packageData = " + packageSummaryJson);
                    }
                },
                "data/" + fileName + "-data.js"
        );
        return jigDocumentWriter.outputFilePaths();
    }

    private String formatRelationJson(PackageRelation relation) {
        return Json.object("from", relation.from().asText())
                .and("to", relation.to().asText())
                .build();
    }

    private String formatTypeRelationJson(TypeRelationship relation) {
        return Json.object("from", relation.from().fqn())
                .and("to", relation.to().fqn())
                .build();
    }
}
