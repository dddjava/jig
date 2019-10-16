package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.presentation.view.JigDocumentContext;

public class BusinessRuleNetworkDiagram implements DotTextEditor<BusinessRuleNetwork> {

    PackageIdentifierFormatter packageIdentifierFormatter;
    AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public BusinessRuleNetworkDiagram(PackageIdentifierFormatter packageIdentifierFormatter,
                                      AliasFinder aliasFinder) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(BusinessRuleNetwork network) {
        return new DotTexts(network.toDotText(jigDocumentContext, packageIdentifierFormatter, aliasFinder));
    }
}
