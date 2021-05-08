package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSources;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;

public class ClassRelationCoreDiagram implements DiagramSourceWriter {

    ClassRelationDiagram classRelationDiagram;

    public ClassRelationCoreDiagram(ClassRelationDiagram classRelationDiagram) {
        this.classRelationDiagram = classRelationDiagram;
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        return classRelationDiagram.sources(
                jigDocumentContext,
                classRelationDiagram.businessRules.filterCore(),
                DocumentName.of(JigDocument.CoreBusinessRuleRelationDiagram));
    }
}
