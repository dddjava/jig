package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigFields;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 値の種類
 */
public enum JigTypeValueKind {
    文字列,
    数値,
    日付,
    期間,
    区分,
    コレクション,
    不明;

    public static JigTypeValueKind from(JigType jigType) {
        TypeKind typeKind = jigType.typeKind();
        if (typeKind.isCategory()) {
            return 区分;
        }

        JigFields instanceJigFields = jigType.instanceJigFields();
        if (matchFieldType(instanceJigFields, List.class) || matchFieldType(instanceJigFields, Set.class)) {
            return コレクション;
        }
        if (matchFieldType(instanceJigFields, String.class)) {
            return 文字列;
        }
        if (matchFieldType(instanceJigFields, BigDecimal.class)
                || matchFieldType(instanceJigFields, Integer.class)
                || matchFieldType(instanceJigFields, Long.class)
                || matchFieldType(instanceJigFields, int.class)
                || matchFieldType(instanceJigFields, long.class)) {
            return 数値;
        }
        if (matchFieldType(instanceJigFields, LocalDate.class)) {
            return 日付;
        }
        if (isDateRange(instanceJigFields)) {
            return 期間;
        }
        return 不明;
    }

    private static boolean matchFieldType(JigFields jigFields, Class<?> clz) {
        Collection<JigField> fields = jigFields.fields();
        if (fields.size() != 1) return false;
        return fields.stream().anyMatch(field -> field.typeId().equals(TypeId.from(clz)));
    }

    private static boolean isDateRange(JigFields jigFields) {
        Collection<JigField> fields = jigFields.fields();
        if (fields.size() != 2) return false;
        return fields.stream().anyMatch(field -> field.typeId().equals(TypeId.from(LocalDate.class)));
    }
}
