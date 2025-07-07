package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Enum固有で取得するモデル
 */
public class EnumModel {
    TypeId typeId;

    /**
     * 列挙定数
     */
    List<EnumConstant> enumConstants;
    List<List<String>> constructorArgumentNamesList = new ArrayList<>();

    public EnumModel(TypeId typeId, List<EnumConstant> enumConstants) {
        this.typeId = typeId;
        this.enumConstants = enumConstants;
    }

    public List<String> paramOf(String name) {
        return enumConstants.stream()
                .filter(enumConstant -> enumConstant.name().equals(name))
                .map(enumConstant -> enumConstant.argumentExpressions())
                .findAny()
                .orElseGet(() -> List.of());
    }

    public List<String> constructorArgumentNames() {
        return constructorArgumentNamesList.stream().max(Comparator.comparing(List::size))
                .orElse(List.of());
    }

    public void addConstructorArgumentNames(List<String> argumentNames) {
        constructorArgumentNamesList.add(argumentNames);
    }
}
