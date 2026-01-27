package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.knowledge.insight.Insights;
import org.dddjava.jig.domain.model.knowledge.insight.MethodInsight;
import org.dddjava.jig.domain.model.knowledge.insight.PackageInsight;
import org.dddjava.jig.domain.model.knowledge.insight.TypeInsight;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


@HandleDocument
public class InsightAdapter {

    private final JigService jigService;
    private final TemplateEngine templateEngine;
    private final JigDocumentContext jigDocumentContext;

    public InsightAdapter(JigService jigService, TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.templateEngine = templateEngine;
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

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "insightJson", insightJson
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
        return jigDocumentWriter.outputFilePaths();
    }

    private String formatPackageJson(PackageInsight insight) {
        return """
                {"fqn": "%s", "label": "%s", "numberOfTypes": %d, "numberOfMethods": %d, "numberOfUsingTypes": %d, "cyclomaticComplexity": %d, "size": %d}
                """.formatted(
                escape(insight.fqn()),
                escape(insight.label()),
                insight.numberOfTypes(),
                insight.numberOfMethods(),
                insight.numberOfUsingTypes(),
                insight.cyclomaticComplexity(),
                insight.size());
    }

    private String formatTypeJson(TypeInsight insight) {
        return """
                {"fqn": "%s", "label": "%s", "numberOfMethods": %d, "numberOfUsingTypes": %d, "cyclomaticComplexity": %d, "size": %d, "packageFqn": "%s"}
                """.formatted(
                escape(insight.fqn()),
                escape(insight.label()),
                insight.numberOfMethods(),
                insight.numberOfUsingTypes(),
                insight.cyclomaticComplexity(),
                insight.size(),
                escape(insight.packageFqn()));
    }

    private String formatMethodJson(MethodInsight insight) {
        return """
                {"fqn": "%s", "label": "%s", "cyclomaticComplexity": %d, "numberOfUsingTypes": %d, "numberOfUsingMethods": %d, "numberOfUsingFields": %d, "size": %d, "packageFqn": "%s", "typeFqn": "%s"}
                """.formatted(
                escape(insight.fqn()),
                escape(insight.label()),
                insight.cyclomaticComplexity(),
                insight.numberOfUsingTypes(),
                insight.numberOfUsingMethods(),
                insight.numberOfUsingFields(),
                insight.size(),
                escape(insight.packageFqn()),
                escape(insight.typeFqn()));
    }

    private String escape(String string) {
        return string
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
