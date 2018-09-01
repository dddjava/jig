package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.categories.CategoryTypes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 値の可能性のある型一覧
 */
public class PotentiallyValueTypes {
    private List<PotentiallyValueType> list;

    public PotentiallyValueTypes(List<PotentiallyValueType> list) {
        this.list = list;
    }

    public ValueTypes toValueTypes(CategoryTypes categoryTypes) {
        List<ValueType> valueTypeList = list.stream()
                .filter(potentiallyValueType -> !categoryTypes.contains(potentiallyValueType.typeIdentifier))
                .map(PotentiallyValueType::toValueType)
                .collect(Collectors.toList());
        return new ValueTypes(valueTypeList);
    }
}
