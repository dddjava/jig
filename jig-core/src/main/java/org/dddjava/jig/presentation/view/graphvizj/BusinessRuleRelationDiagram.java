package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigdocument.DotText;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.presentation.view.JigDocumentContext;

public class BusinessRuleRelationDiagram implements DiagramSourceEditor<BusinessRuleNetwork> {

    PackageIdentifierFormatter packageIdentifierFormatter;
    AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public BusinessRuleRelationDiagram(PackageIdentifierFormatter packageIdentifierFormatter,
                                       AliasFinder aliasFinder) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotText edit(BusinessRuleNetwork network) {
        return network.relationDotText(jigDocumentContext, packageIdentifierFormatter, aliasFinder);
    }
}
