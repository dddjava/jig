package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enum固有で取得するモデル
 */
public record EnumModels(Collection<EnumModel> values) {

    public Map<TypeId, EnumModel> toMap() {
        return values.stream()
                .collect(Collectors.toMap(enumModel -> enumModel.typeId, enumModel -> enumModel));
    }
}
