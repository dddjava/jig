package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSources;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;

public class CoreDomainDiagram implements DiagramSourceWriter {

    BusinessRuleRelationDiagram businessRuleRelationDiagram;

    public CoreDomainDiagram(BusinessRuleRelationDiagram businessRuleRelationDiagram) {
        this.businessRuleRelationDiagram = businessRuleRelationDiagram;
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        return businessRuleRelationDiagram.sources(
                jigDocumentContext,
                businessRuleRelationDiagram.businessRules.filterCore(),
                DocumentName.of(JigDocument.CoreBusinessRuleRelationDiagram));
    }
}
