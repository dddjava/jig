package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.types.JigType;

public class Nodes {

    public static Node businessRuleNodeOf(JigType jigType) {
        return new Node(jigType.id().fullQualifiedName())
                .label(jigType.term().titleAndSimpleName("\\n"))
                .url(jigType.id(), JigDocument.DomainSummary);
    }
}
