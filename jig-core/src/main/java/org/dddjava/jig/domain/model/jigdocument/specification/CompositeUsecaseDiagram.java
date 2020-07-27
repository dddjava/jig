package org.dddjava.jig.domain.model.jigdocument.specification;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSources;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceAngles;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ユースケース複合図
 */
public class CompositeUsecaseDiagram {

    private final List<CompositeUsecases> list;

    public CompositeUsecaseDiagram(ServiceAngles serviceAngles) {
        this.list = serviceAngles.list().stream()
                .map(CompositeUsecases::new)
                .collect(Collectors.toList());
    }

    public DiagramSources diagramSource(JigDocumentContext jigDocumentContext) {
        if (list.isEmpty()) {
            return DiagramSources.empty();
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.CompositeUsecaseDiagram);
        String text = list.stream()
                .map(compositeUsecases -> compositeUsecases.dotText(jigDocumentContext))
                .collect(Collectors.joining("\n", "digraph \"" + documentName.label() + "\" {\n" +
                        "layout=fdp;\n" +
                        "label=\"" + documentName.label() + "\";\n" +
                        "node[shape=box];\n" +
                        "edge[arrowhead=none];\n" +
                        "", "}"));

        return DiagramSource.createDiagramSource(documentName, text);
    }
}
