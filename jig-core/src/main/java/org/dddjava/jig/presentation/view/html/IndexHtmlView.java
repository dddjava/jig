package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocumentType;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.JigDocumentWriter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

public class IndexHtmlView {

    TemplateEngine templateEngine;

    public IndexHtmlView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void render(List<HandleResult> handleResultList, JigDocumentWriter jigDocumentWriter) {
        Map<String, Object> contextMap = new HashMap<>();

        List<String> diagramFiles = new ArrayList<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                List<String> list = handleResult.outputFileNames();
                if (handleResult.jigDocument().jigDocumentType() == JigDocumentType.DIAGRAM) {
                    diagramFiles.addAll(list);
                } else {
                    contextMap.put(handleResult.jigDocument().name(), list.get(0));
                }
            }
        }
        contextMap.put("diagramFiles", diagramFiles);
        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
    }
}
