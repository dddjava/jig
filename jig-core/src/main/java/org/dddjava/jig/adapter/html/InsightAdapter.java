package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.knowledge.insight.Insights;
import org.dddjava.jig.domain.model.knowledge.insight.MethodInsight;
import org.dddjava.jig.domain.model.knowledge.insight.PackageInsight;
import org.dddjava.jig.domain.model.knowledge.insight.TypeInsight;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;


@HandleDocument
public class InsightAdapter {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public InsightAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.Insight)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        Insights result = jigService.insights(repository);
        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        String insightJson = buildJson(result);

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("insightData", insightJson);

        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(Insights result) {
        String packagesJson = result.packageInsightList().stream()
                .map(InsightAdapter::formatPackageJson)
                .collect(Collectors.joining(",", "[", "]"));

        TypeRelationships typeRelationships = result.typeRelationships();
        var jigTypes = result.jigTypes();
        String typesJson = result.typeInsightList().stream()
                .map(insight -> formatTypeJson(insight, typeRelationships, jigTypes))
                .collect(Collectors.joining(",", "[", "]"));

        String methodsJson = result.methodInsightList().stream()
                .map(InsightAdapter::formatMethodJson)
                .collect(Collectors.joining(",", "[", "]"));

        return """
                {"packages": %s, "types": %s, "methods": %s}
                """.formatted(packagesJson, typesJson, methodsJson);
    }

    private static String formatPackageJson(PackageInsight insight) {
        return Json.object("fqn", insight.fqn())
                .and("label", insight.label())
                .and("numberOfTypes", insight.numberOfTypes())
                .and("numberOfMethods", insight.numberOfMethods())
                .and("numberOfUsingTypes", insight.numberOfUsingTypes())
                .and("cyclomaticComplexity", insight.cyclomaticComplexity())
                .and("size", insight.size())
                .build();
    }

    private static String formatTypeJson(TypeInsight insight, TypeRelationships typeRelationships, JigTypes jigTypes) {
        return Json.object("fqn", insight.fqn())
                .and("label", insight.label())
                .and("numberOfMethods", insight.numberOfMethods())
                .and("numberOfUsingTypes", insight.numberOfUsingTypes())
                .and("numberOfUsedByTypes", insight.numberOfUsedByTypes(typeRelationships))
                .and("instability", Math.round(insight.instability(typeRelationships) * 100.0) / 100.0)
                .and("lcom", Math.round(insight.lcom(jigTypes) * 100.0) / 100.0)
                .and("cyclomaticComplexity", insight.cyclomaticComplexity())
                .and("size", insight.size())
                .and("packageFqn", insight.packageFqn())
                .build();
    }

    private static String formatMethodJson(MethodInsight insight) {
        return Json.object("fqn", insight.fqn())
                .and("label", insight.label())
                .and("cyclomaticComplexity", insight.cyclomaticComplexity())
                .and("numberOfUsingTypes", insight.numberOfUsingTypes())
                .and("numberOfUsingMethods", insight.numberOfUsingMethods())
                .and("numberOfUsingFields", insight.numberOfUsingFields())
                .and("numberOfUsingOwnFields", insight.numberOfUsingOwnFields())
                .and("numberOfUsingOwnMethods", insight.numberOfUsingOwnMethods())
                .and("size", insight.size())
                .and("packageFqn", insight.packageFqn())
                .and("typeFqn", insight.typeFqn())
                .build();
    }

}
