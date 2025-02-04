package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;

/**
 * Enum固有で取得するモデル
 */
public class EnumModels {
    List<EnumModel> list;

    public EnumModels(List<EnumModel> list) {
        this.list = list;
    }

    public EnumModel select(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(enumModel -> enumModel.typeIdentifier.equals(typeIdentifier))
                .findAny()
                .orElseGet(() -> new EnumModel(typeIdentifier, List.of()));
    }
}
