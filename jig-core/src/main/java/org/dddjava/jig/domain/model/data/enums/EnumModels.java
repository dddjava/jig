package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.Optional;

/**
 * Enum固有で取得するモデル
 */
public record EnumModels(Collection<EnumModel> values) {

    public Optional<EnumModel> find(TypeId id) {
        return values.stream()
                .filter(enumModel -> enumModel.typeId().equals(id))
                .findAny();
    }
}
