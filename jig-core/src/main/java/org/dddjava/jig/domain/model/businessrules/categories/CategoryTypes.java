package org.dddjava.jig.domain.model.businessrules.categories;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final List<CategoryType> list;

    public CategoryTypes(TypeByteCodes typeByteCodes, Architecture architecture) {
        list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (!architecture.isBusinessRule(typeByteCode.typeIdentifier())) {
                continue;
            }
            if (typeByteCode.isEnum()) {
                list.add(new CategoryType(typeByteCode));
            }
        }
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return list.stream().anyMatch(categoryType -> categoryType.typeIdentifier.equals(typeIdentifier));
    }

    public List<CategoryType> list() {
        return list;
    }
}
