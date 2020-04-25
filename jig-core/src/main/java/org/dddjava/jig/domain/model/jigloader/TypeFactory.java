package org.dddjava.jig.domain.model.jigloader;

import org.dddjava.jig.domain.model.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigloader.architecture.BuildingBlock;
import org.dddjava.jig.domain.model.jigmodel.businessrules.*;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

public class TypeFactory {

    public static CategoryTypes createCategoryTypes(BusinessRules businessRules) {
        List<CategoryType> list = new ArrayList<>();
        for (BusinessRule businessRule : businessRules.listCategory()) {
            list.add(businessRule.categoryType());

        }
        return new CategoryTypes(list);
    }

    public static BusinessRules from(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<BusinessRule> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (BuildingBlock.BUSINESS_RULE.satisfy(typeByteCode, architecture)) {
                list.add(createBusinessRule(typeByteCode));
            }
        }
        return new BusinessRules(list);
    }

    public static BusinessRule createBusinessRule(TypeByteCode typeByteCode) {
        return new BusinessRule(new BusinessRuleFields(typeByteCode.fieldDeclarations()),
                typeByteCode.typeIdentifier(),
                typeByteCode.isEnum(),
                typeByteCode.type(),
                typeByteCode.methodDeclarations(),
                new CategoryType(
                        typeByteCode.typeIdentifier(),
                        typeByteCode.hasField(),
                        typeByteCode.hasInstanceMethod(),
                        typeByteCode.canExtend()));
    }
}
