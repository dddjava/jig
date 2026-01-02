package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Comparator;
import java.util.List;

/**
 * Enum固有で取得するモデル
 *
 * @param enumConstants 列挙定数
 */
public record EnumModel(TypeId typeId,
                        List<EnumConstant> enumConstants,
                        List<List<String>> constructorParameterNameList) {

    public static EnumModel from(TypeId typeId, List<EnumConstant> constantsList, List<List<String>> constructorParameterNameList) {
        return new EnumModel(typeId, constantsList, constructorParameterNameList);
    }

    public List<String> paramOf(String name) {
        return enumConstants.stream()
                .filter(enumConstant -> enumConstant.name().equals(name))
                .map(enumConstant -> enumConstant.argumentExpressions())
                .findAny()
                .orElseGet(() -> List.of());
    }

    public List<String> constructorParameterNames() {
        return constructorParameterNameList.stream()
                // 複数ある場合にどれを使用するか定まらないので、もっとも引数が多いものを採用する
                .max(Comparator.comparing(List::size))
                .orElse(List.of());
    }

    public boolean hasConstructorParameter() {
        return !constructorParameterNames().isEmpty();
    }
}
