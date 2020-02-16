package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;

public class BusinessRuleRelationDiagram implements DiagramSourceEditor<BusinessRuleNetwork> {

    PackageIdentifierFormatter packageIdentifierFormatter;
    AliasFinder aliasFinder;
    ResourceBundleJigDocumentContext jigDocumentContext;

    public BusinessRuleRelationDiagram(PackageIdentifierFormatter packageIdentifierFormatter,
                                       AliasFinder aliasFinder) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = ResourceBundleJigDocumentContext.getInstance();
    }

    @Override
    public DiagramSource edit(BusinessRuleNetwork network) {
        return network.relationDotText(jigDocumentContext, packageIdentifierFormatter, aliasFinder);
    }
}
