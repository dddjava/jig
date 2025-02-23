package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enum固有で取得するモデル
 */
public class EnumModels {
    List<EnumModel> list;

    public EnumModels(List<EnumModel> list) {
        this.list = list;
    }

    public Map<TypeIdentifier, EnumModel> toMap() {
        return list.stream()
                .collect(Collectors.toMap(enumModel -> enumModel.typeIdentifier, enumModel -> enumModel));
    }
}
