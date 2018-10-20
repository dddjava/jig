package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ProjectData;

import java.util.ArrayList;
import java.util.List;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final List<CategoryType> list;

    public CategoryTypes(ProjectData typeByteCodes) {
        list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
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
