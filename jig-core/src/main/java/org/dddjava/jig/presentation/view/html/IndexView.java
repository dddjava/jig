package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocumentType;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.JigDocumentWriter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndexView {

    final Map<String, Object> contextMap;
    final TemplateEngine templateEngine;

    public IndexView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.contextMap = new ConcurrentHashMap<>();
    }

    public void render(List<HandleResult> handleResultList, JigDocumentWriter jigDocumentWriter) {
        List<String> diagramFiles = new ArrayList<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                List<String> list = handleResult.outputFileNames();
                if (handleResult.jigDocument().jigDocumentType() == JigDocumentType.DIAGRAM) {
                    list.stream().filter(item -> !item.endsWith(".txt")).forEach(diagramFiles::add);
                } else {
                    putContext(handleResult.jigDocument().name(), list.get(0));
                }
            }
        }

        putContext("diagramFiles", diagramFiles);
        write(jigDocumentWriter);
    }

    protected void write(JigDocumentWriter jigDocumentWriter) {
        contextMap.put("title", jigDocumentWriter.jigDocument().label());
        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
    }

    protected void putContext(String key, Object variable) {
        contextMap.put(key, variable);
    }
}
