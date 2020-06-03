package org.dddjava.jig.domain.model.jigdocumenter.diagram;

import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigdocumenter.DiagramSource;
import org.dddjava.jig.domain.model.jigdocumenter.DiagramSources;
import org.dddjava.jig.domain.model.jigdocumenter.JigDocumentContext;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
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
