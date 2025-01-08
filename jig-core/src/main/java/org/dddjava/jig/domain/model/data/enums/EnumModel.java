package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Enum固有で取得するモデル
 */
public class EnumModel {
    TypeIdentifier typeIdentifier;

    /**
     * 列挙定数
     */
    List<EnumConstant> enumConstants;
    List<List<String>> constructorArgumentNamesList = new ArrayList<>();

    public EnumModel(TypeIdentifier typeIdentifier, List<EnumConstant> enumConstants) {
        this.typeIdentifier = typeIdentifier;
        this.enumConstants = enumConstants;
    }

    public List<String> paramOf(String name) {
        return enumConstants.stream()
                .filter(enumConstant -> enumConstant.name.equals(name))
                .map(enumConstant -> enumConstant.argumentExpressions)
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
