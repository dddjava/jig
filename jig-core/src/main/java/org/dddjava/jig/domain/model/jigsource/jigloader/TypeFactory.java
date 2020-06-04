package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleFields;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryType;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.BuildingBlock;

import java.util.ArrayList;
import java.util.List;

public class TypeFactory {

    public static BusinessRules from(TypeFacts typeFacts, Architecture architecture) {
        List<BusinessRule> list = new ArrayList<>();
        for (TypeFact typeFact : typeFacts.list()) {
            if (BuildingBlock.BUSINESS_RULE.satisfy(typeFact, architecture)) {
                list.add(createBusinessRule(typeFact));
            }
        }
        return new BusinessRules(list);
    }

    public static BusinessRule createBusinessRule(TypeFact typeFact) {
        return new BusinessRule(
                new BusinessRuleFields(typeFact.fieldDeclarations()),
                typeFact.typeIdentifier(),
                typeFact.isEnum(),
                typeFact.typeDeclaration(),
                typeFact.methodDeclarations(),
                new CategoryType(
                        typeFact.typeIdentifier(),
                        typeFact.hasField(),
                        typeFact.hasInstanceMethod(),
                        typeFact.canExtend()));
    }
}
