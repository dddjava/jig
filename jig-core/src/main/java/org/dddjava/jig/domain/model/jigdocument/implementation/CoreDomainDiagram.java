package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSources;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifierFormatter;

public class CoreDomainDiagram {

    BusinessRuleRelationDiagram businessRuleRelationDiagram;

    public CoreDomainDiagram(BusinessRuleRelationDiagram businessRuleRelationDiagram) {
        this.businessRuleRelationDiagram = businessRuleRelationDiagram;
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext, PackageIdentifierFormatter packageIdentifierFormatter) {
        return businessRuleRelationDiagram.sources(
                packageIdentifierFormatter,
                businessRuleRelationDiagram.businessRules.filterCore(),
                DocumentName.of(JigDocument.CoreBusinessRuleRelationDiagram));
    }
}
