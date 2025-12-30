package org.dddjava.jig.domain.model.data.enums;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Comparator;
import java.util.List;

/**
 * Enum固有で取得するモデル
 */
public class EnumModel {
    private final TypeId typeId;

    /**
     * 列挙定数
     */
    private final List<EnumConstant> enumConstants;
    private final List<List<String>> constructorParameterNameList;

    public EnumModel(TypeId typeId, List<EnumConstant> enumConstants, List<List<String>> constructorParameterNameList) {
        this.typeId = typeId;
        this.enumConstants = enumConstants;
        this.constructorParameterNameList = constructorParameterNameList;
    }

    public static EnumModel from(TypeId typeId, List<EnumConstant> constantsList, List<List<String>> constructorParameterNameList) {
        return new EnumModel(typeId, constantsList, constructorParameterNameList);
    }

    public TypeId typeId() {
        return typeId;
    }

    public List<String> paramOf(String name) {
        return enumConstants.stream()
                .filter(enumConstant -> enumConstant.name().equals(name))
                .map(enumConstant -> enumConstant.argumentExpressions())
                .findAny()
                .orElseGet(() -> List.of());
    }

    public List<String> constructorArgumentNames() {
        return constructorParameterNameList.stream()
                // 複数ある場合にどれを使用するか定まらないので、もっとも引数が多いものを採用する
                .max(Comparator.comparing(List::size))
                .orElse(List.of());
    }

    public boolean hasConstructorArguments() {
        return enumConstants.stream()
                .mapToInt(enumConstant -> enumConstant.argumentExpressions().size())
                .max()
                .orElse(0) > 0;
    }
}
