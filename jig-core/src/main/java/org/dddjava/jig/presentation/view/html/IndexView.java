package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocumentType;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.JigDocumentWriter;
import org.thymeleaf.TemplateEngine;

import java.util.ArrayList;
import java.util.List;

public class IndexView extends AbstractThymeleafView {

    public IndexView(TemplateEngine templateEngine) {
        super(templateEngine);
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
}
