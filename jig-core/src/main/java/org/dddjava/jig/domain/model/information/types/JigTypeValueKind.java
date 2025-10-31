package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigFields;

import java.util.Collection;

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
        if (matchFieldType(instanceJigFields, TypeId.LIST) || matchFieldType(instanceJigFields, TypeId.SET)) {
            return コレクション;
        }
        if (matchFieldType(instanceJigFields, TypeId.STRING)) {
            return 文字列;
        }
        if (matchFieldType(instanceJigFields, TypeId.BIG_DECIMAL)
                || matchFieldType(instanceJigFields, TypeId.INTEGER)
                || matchFieldType(instanceJigFields, TypeId.LONG)
                || matchFieldType(instanceJigFields, TypeId.INT_PRIMITIVE)
                || matchFieldType(instanceJigFields, TypeId.LONG_PRIMITIVE)) {
            return 数値;
        }
        if (matchFieldType(instanceJigFields, TypeId.LOCAL_DATE)) {
            return 日付;
        }
        if (isDateRange(instanceJigFields)) {
            return 期間;
        }
        return 不明;
    }

    private static boolean matchFieldType(JigFields jigFields, TypeId typeId) {
        Collection<JigField> fields = jigFields.fields();
        if (fields.size() != 1) return false;
        return fields.stream().anyMatch(field -> field.typeId().equals(typeId));
    }

    private static boolean isDateRange(JigFields jigFields) {
        Collection<JigField> fields = jigFields.fields();
        if (fields.size() != 2) return false;
        return fields.stream().anyMatch(field -> field.typeId().equals(TypeId.LOCAL_DATE));
    }
}
