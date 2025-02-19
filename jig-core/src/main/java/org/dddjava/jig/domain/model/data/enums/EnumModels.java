package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.List;

/**
 * Enum固有で取得するモデル
 */
public class EnumModels {
    List<EnumModel> list;

    public EnumModels(List<EnumModel> list) {
        this.list = list;
    }

    public EnumModel select(JigType jigType) {
        var typeIdentifier = jigType.id();
        return list.stream()
                .filter(enumModel -> enumModel.typeIdentifier.equals(typeIdentifier))
                .findAny()
                .orElseGet(() -> new EnumModel(typeIdentifier, List.of()));
    }
}
