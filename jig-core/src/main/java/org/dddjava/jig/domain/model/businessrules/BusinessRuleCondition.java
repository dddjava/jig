package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * ビジネスルールの条件
 */
public class BusinessRuleCondition {

    String typeIdentifierPattern;

    public BusinessRuleCondition(String typeIdentifierPattern) {
        this.typeIdentifierPattern = typeIdentifierPattern;
    }

    boolean judge(TypeIdentifier identifier) {
        String fullQualifiedName = identifier.fullQualifiedName();
        if (fullQualifiedName.matches(typeIdentifierPattern)) {
            // コンパイラの生成するクラスを除外
            return !fullQualifiedName.matches(".+\\$\\d+");
        }
        return false;
    }

    public BusinessRules sorting(TypeByteCodes typeByteCodes) {
        List<BusinessRule> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            TypeIdentifier typeIdentifier = typeByteCode.typeIdentifier();
            if (judge(typeIdentifier)) {
                list.add(new BusinessRule(typeByteCode));
            }
        }
        return new BusinessRules(list);
    }
}
