package org.dddjava.jig.domain.model.models.domains.categories.enums;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.List;

/**
 * Enum固有で取得するモデル
 */
public class EnumModel {
    TypeIdentifier typeIdentifier;

    /** 列挙定数 */
    List<EnumConstant> enumConstants;

    public EnumModel(TypeIdentifier typeIdentifier, List<EnumConstant> enumConstants) {
        this.typeIdentifier = typeIdentifier;
        this.enumConstants = enumConstants;
    }
}
