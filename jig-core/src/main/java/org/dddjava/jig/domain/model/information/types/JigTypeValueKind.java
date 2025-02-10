package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.classes.field.JigField;
import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        if (matchFieldType(instanceJigFields, LocalDate.class, LocalDate.class)) {
            return 期間;
        }
        return 不明;
    }

    private static boolean matchFieldType(JigFields jigFields, Class<?>... classes) {
        List<JigField> list = jigFields.list();
        if (list.size() != classes.length) return false;
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).typeIdentifier().equals(TypeIdentifier.from(classes[i]))) {
                return false;
            }
        }
        return true;
    }
}
