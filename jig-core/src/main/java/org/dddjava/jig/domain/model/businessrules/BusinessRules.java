package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.declaration.type.Type;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * ビジネスルール一覧
 */
public class BusinessRules {

    List<BusinessRule> list;

    public BusinessRules(List<BusinessRule> list) {
        this.list = list;
    }

    public static BusinessRules from(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<BusinessRule> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (architecture.isBusinessRule(typeByteCode)) {
                list.add(new BusinessRule(typeByteCode));
            }
        }
        return new BusinessRules(list);
    }

    public List<BusinessRule> list() {
        return list;
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        for (BusinessRule businessRule : list) {
            if (businessRule.type().identifier().equals(typeIdentifier)) {
                return true;
            }
        }
        return false;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public TypeIdentifiers identifiers() {
        return list.stream()
                .map(BusinessRule::type)
                .map(Type::identifier)
                .collect(TypeIdentifiers.collector());
    }
}
