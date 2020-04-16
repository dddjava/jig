package org.dddjava.jig.domain.model.jigloader;

import org.dddjava.jig.domain.model.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigmodel.architecture.Architecture;
import org.dddjava.jig.domain.model.jigmodel.architecture.BuildingBlock;
import org.dddjava.jig.domain.model.jigmodel.businessrules.*;
import org.dddjava.jig.domain.model.jigmodel.validations.ValidationAngle;
import org.dddjava.jig.domain.model.jigmodel.validations.ValidationAngles;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                new CategoryType(typeByteCode.typeIdentifier(), typeByteCode.hasField(), typeByteCode.hasInstanceMethod(), typeByteCode.canExtend()));
    }

    public static ValidationAngles createValidationAngles(AnalyzedImplementation implementations) {
        return new ValidationAngles(implementations.typeByteCodes().validationAnnotatedMembers().list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList()));
    }
}
