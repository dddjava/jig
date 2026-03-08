package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * パッケージ概要
 */
public class PackageSummaryView {

    private final JigDocument jigDocument;
    private final TemplateEngine templateEngine;

    public PackageSummaryView(JigDocument jigDocument, TemplateEngine templateEngine) {
        this.jigDocument = jigDocument;
        this.templateEngine = templateEngine;
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

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "packagesJson", packageSummaryJson
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
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
