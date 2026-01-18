package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
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

    public List<Path> write(Path outputDirectory, JigPackages jigPackages) {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);

        var packagesJson = jigPackages.listPackage().stream()
                .map(packageInfo -> """
                        {"fqn": "%s", "name": "%s", "description": "%s", "classCount": %d}
                        """.formatted(
                        escape(packageInfo.fqn()),
                        escape(packageInfo.label()),
                        escape(packageInfo.term().description()),
                        packageInfo.numberOfClasses()))
                .collect(Collectors.joining(",", "[", "]"));


        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "packagesJson", packagesJson
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
        return jigDocumentWriter.outputFilePaths();
    }

    private String escape(String string) {
        return string.replace("\n", "\\n");
    }
}
