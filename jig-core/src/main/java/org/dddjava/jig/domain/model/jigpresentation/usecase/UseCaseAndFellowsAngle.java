package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.jigdocument.*;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ユースケース複合図
 */
public class UseCaseAndFellowsAngle {

    private final List<UseCaseAndFellows> list;

    public UseCaseAndFellowsAngle(ServiceAngles serviceAngles) {
        this.list = serviceAngles.list().stream()
                .map(UseCaseAndFellows::new)
                .collect(Collectors.toList());
    }

    public DiagramSources diagramSource(JigDocumentContext jigDocumentContext, AliasFinder aliasFinder) {
        if (list.isEmpty()) {
            return DiagramSources.empty();
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.UseCaseAndFellowsDiagram);
        String text = list.stream()
                .map(useCaseAndFellows -> useCaseAndFellows.dotText(aliasFinder))
                .collect(Collectors.joining("\n", "digraph \"" + documentName.label() + "\" {\n" +
                        "layout=fdp;\n" +
                        "label=\"" + documentName.label() + "\";\n" +
                        "node[shape=box];\n" +
                        "", "}"));

        return DiagramSource.createDiagramSource(documentName, text);
    }
}
