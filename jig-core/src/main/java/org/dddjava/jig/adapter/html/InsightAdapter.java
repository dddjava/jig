package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
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

        String packagesJson = result.packageInsightList().stream()
                .map(this::formatPackageJson)
                .collect(Collectors.joining(",", "[", "]"));

        String typesJson = result.typeInsightList().stream()
                .map(this::formatTypeJson)
                .collect(Collectors.joining(",", "[", "]"));

        String methodsJson = result.methodInsightList().stream()
                .map(this::formatMethodJson)
                .collect(Collectors.joining(",", "[", "]"));

        String insightJson = """
                {"packages": %s, "types": %s, "methods": %s}
                """.formatted(packagesJson, typesJson, methodsJson);

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("insightData", insightJson);

        return jigDocumentWriter.outputFilePaths();
    }

    private String formatPackageJson(PackageInsight insight) {
        return Json.object("fqn", insight.fqn())
                .and("label", insight.label())
                .and("numberOfTypes", insight.numberOfTypes())
                .and("numberOfMethods", insight.numberOfMethods())
                .and("numberOfUsingTypes", insight.numberOfUsingTypes())
                .and("cyclomaticComplexity", insight.cyclomaticComplexity())
                .and("size", insight.size())
                .build();
    }

    private String formatTypeJson(TypeInsight insight) {
        return Json.object("fqn", insight.fqn())
                .and("label", insight.label())
                .and("numberOfMethods", insight.numberOfMethods())
                .and("numberOfUsingTypes", insight.numberOfUsingTypes())
                .and("cyclomaticComplexity", insight.cyclomaticComplexity())
                .and("size", insight.size())
                .and("packageFqn", insight.packageFqn())
                .build();
    }

    private String formatMethodJson(MethodInsight insight) {
        return Json.object("fqn", insight.fqn())
                .and("label", insight.label())
                .and("cyclomaticComplexity", insight.cyclomaticComplexity())
                .and("numberOfUsingTypes", insight.numberOfUsingTypes())
                .and("numberOfUsingMethods", insight.numberOfUsingMethods())
                .and("numberOfUsingFields", insight.numberOfUsingFields())
                .and("size", insight.size())
                .and("packageFqn", insight.packageFqn())
                .and("typeFqn", insight.typeFqn())
                .build();
    }

}
