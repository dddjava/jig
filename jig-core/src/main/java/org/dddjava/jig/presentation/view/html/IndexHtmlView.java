package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocumentType;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.handler.HandleResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexHtmlView {

    public void render(List<HandleResult> handleResultList, JigDocumentWriter jigDocumentWriter) {
        HtmlDocumentTemplateEngine templateEngine = new HtmlDocumentTemplateEngine();

        Map<String, Object> context = new HashMap<>();

        List<String> diagramFiles = new ArrayList<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                List<String> list = handleResult.outputFileNames();
                if (handleResult.jigDocument().jigDocumentType() == JigDocumentType.DIAGRAM) {
                    diagramFiles.addAll(list);
                } else {
                    context.put(handleResult.jigDocument().name(), list.get(0));
                }
            }
        }
        context.put("diagramFiles", diagramFiles);

        String htmlText = templateEngine.process(jigDocumentWriter, context);

        jigDocumentWriter.writeHtml(outputStream -> {
            outputStream.write(htmlText.getBytes(StandardCharsets.UTF_8));
        });
    }
}
