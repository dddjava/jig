package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigDocumentWriter;
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

public class RepositoryListAdapter implements Adapter<List<RepositoryListAdapter.OutputSummaryItem>> {
    private final JigService jigService;
    private final TemplateEngine templateEngine;
    private final JigDocumentContext jigDocumentContext;

    public RepositoryListAdapter(JigService jigService, TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
    }

    public record OutputSummaryItem(String port, String gateway, String adapter, String invocation) {
    }

    @HandleDocument(JigDocument.RepositorySummary)
    public List<OutputSummaryItem> invoke(JigRepository repository) {
        var jigTypes = jigService.jigTypes(repository);
        var outputAdapters = OutputAdapters.from(jigTypes);
        return outputAdapters.stream()
                // output adapterの実装しているoutput portのgatewayを
                .flatMap(outputAdapter -> outputAdapter.implementsPortStream(jigTypes)
                        .flatMap(outputPort -> outputPort.gatewayStream()
                                // 実装しているinvocationが
                                .flatMap(gateway -> outputAdapter.resolveInvocation(gateway).stream()
                                        .map(invocation -> new OutputSummaryItem(
                                                outputPort.jigType().label(),
                                                gateway.jigMethod().name(),
                                                outputAdapter.jigType().label(),
                                                invocation.jigMethod().name())))))
                .toList();
    }

    @Override
    public List<Path> write(List<RepositoryListAdapter.OutputSummaryItem> result, JigDocument jigDocument) {
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
