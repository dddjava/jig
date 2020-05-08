package org.dddjava.jig.domain.model.jigpresentation.diagram;

import org.dddjava.jig.domain.model.jigdocument.*;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.usecase.CompositeUsecases;

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

    public DiagramSources diagramSource(JigDocumentContext jigDocumentContext, AliasFinder aliasFinder) {
        if (list.isEmpty()) {
            return DiagramSources.empty();
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.CompositeUsecaseDiagram);
        String text = list.stream()
                .map(compositeUsecases -> compositeUsecases.dotText(aliasFinder))
                .collect(Collectors.joining("\n", "digraph \"" + documentName.label() + "\" {\n" +
                        "layout=fdp;\n" +
                        "label=\"" + documentName.label() + "\";\n" +
                        "node[shape=box];\n" +
                        "edge[arrowhead=none];\n" +
                        "", "}"));

        return DiagramSource.createDiagramSource(documentName, text);
    }
}
