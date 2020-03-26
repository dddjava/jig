package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.DiagramSources;
import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;

/**
 * ユースケースと愉快な仲間たち
 */
public class UseCaseAndFellows {
    UseCase useCase;

    public DiagramSources diagramSource() {
        String text = "digraph JIG { a -> b }";
        return DiagramSource.createDiagramSource(
                DocumentName.of(
                        JigDocument.UseCaseAndFellowsDiagram,
                        "ユースケース複合図"
                ),
                text
        );
    }
}
