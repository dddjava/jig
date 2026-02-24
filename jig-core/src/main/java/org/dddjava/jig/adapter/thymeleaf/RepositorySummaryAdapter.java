package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.outputs.OutputAdapters;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RepositorySummaryAdapter {
    private final JigService jigService;
    private final TemplateEngine templateEngine;
    private final JigDocumentContext jigDocumentContext;

    public RepositorySummaryAdapter(JigService jigService, TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
    }

    public record OutputSummaryItem(String port, String portOperation, String adapter, String invocation) {
    }

    @HandleDocument(JigDocument.RepositorySummary)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        var jigTypes = jigService.jigTypes(repository);
        var outputAdapters = OutputAdapters.from(jigTypes);
        var result = outputAdapters.stream()
                // output adapterが実装しているoutput portのoperationを
                .flatMap(outputAdapter -> outputAdapter.implementsPortStream(jigTypes)
                        .flatMap(outputPort -> outputPort.operationStream()
                                // 実装しているinvocationが
                                .flatMap(outputPortOperation -> outputAdapter.resolveInvocation(outputPortOperation).stream()
                                        .map(invocation -> new OutputSummaryItem(
                                                outputPort.jigType().label(),
                                                outputPortOperation.jigMethod().name(),
                                                outputAdapter.jigType().label(),
                                                invocation.jigMethod().name())))))
                .toList();
        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "outputs", result
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
        return jigDocumentWriter.outputFilePaths();
    }
}
