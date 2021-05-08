package org.dddjava.jig.domain.model.documents.implementation;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSources;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;

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
