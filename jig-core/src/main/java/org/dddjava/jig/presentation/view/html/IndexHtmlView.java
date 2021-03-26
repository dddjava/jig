package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.handler.HandleResult;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexHtmlView {

    public void render(List<HandleResult> handleResultList, JigDocumentWriter jigDocumentWriter) {
        HtmlDocumentTemplateEngine templateEngine = new HtmlDocumentTemplateEngine();

        Map<String, Object> context = new HashMap<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                List<String> list = handleResult.outputFileNames();
                if (list.size() == 1) {
                    context.put(handleResult.jigDocument().name(), list.get(0));
                } else {
                    Collections.sort(list);
                    context.put(handleResult.jigDocument().name(), list);
                }
            }
        }

        String htmlText = templateEngine.process(jigDocumentWriter, context);

        jigDocumentWriter.writeHtml(outputStream -> {
            outputStream.write(htmlText.getBytes(StandardCharsets.UTF_8));
        });
    }
}
