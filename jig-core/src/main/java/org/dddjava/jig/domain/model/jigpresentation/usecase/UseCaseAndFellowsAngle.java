package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.DiagramSources;
import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;

import java.util.List;
import java.util.stream.Collectors;

public class UseCaseAndFellowsAngle {

    private final List<UseCaseAndFellows> list;

    public UseCaseAndFellowsAngle(ServiceAngles serviceAngles) {
        this.list = serviceAngles.list().stream()
                .map(UseCaseAndFellows::new)
                .collect(Collectors.toList());
    }

    public DiagramSources diagramSource(AliasFinder aliasFinder) {
        if (list.isEmpty()) {
            return DiagramSources.empty();
        }

        String text = list.stream()
                .map(useCaseAndFellows -> useCaseAndFellows.dotText(aliasFinder))
                .collect(Collectors.joining("\n", "digraph JIG {\n" +
                        "layout=fdp;\n" +
                        "node[shape=box];\n" +
                        "", "}"));

        return DiagramSource.createDiagramSource(
                DocumentName.of(
                        JigDocument.UseCaseAndFellowsDiagram,
                        "ユースケース複合図"
                ),
                text
        );
    }
}
