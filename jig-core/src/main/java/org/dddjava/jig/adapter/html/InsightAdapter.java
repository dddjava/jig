package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.thymeleaf.TemplateEngine;

import java.nio.file.Path;
import java.util.List;


@HandleDocument
public class InsightAdapter<T> implements Adapter<T> {

    public InsightAdapter(JigService jigService, TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {

    }

    @HandleDocument(JigDocument.Insight)
    public T invoke(JigRepository repository) {
        return null;
    }

    @Override
    public List<Path> write(T result, JigDocument jigDocument) {
        return List.of();
    }
}
