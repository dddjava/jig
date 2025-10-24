package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enum固有で取得するモデル
 */
public record EnumModels(List<EnumModel> list) {

    public Map<TypeId, EnumModel> toMap() {
        return list.stream()
                .collect(Collectors.toMap(enumModel -> enumModel.typeId, enumModel -> enumModel));
    }
}
