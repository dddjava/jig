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

    // 数値
    private static final TypeId INT_PRIMITIVE = TypeId.valueOf("int");
    private static final TypeId LONG_PRIMITIVE = TypeId.valueOf("long");
    private static final TypeId INTEGER = TypeId.valueOf("java.lang.Integer");
    private static final TypeId LONG = TypeId.valueOf("java.lang.Long");
    private static final TypeId BIG_DECIMAL = TypeId.valueOf("java.math.BigDecimal");
    // コレクション
    private static final TypeId LIST = TypeId.valueOf("java.util.List");
    private static final TypeId SET = TypeId.valueOf("java.util.Set");

    public static JigTypeValueKind from(JigType jigType) {
        TypeKind typeKind = jigType.typeKind();
        if (typeKind.isCategory()) {
            return 区分;
        }

        JigFields instanceJigFields = jigType.instanceJigFields();
        if (matchFieldType(instanceJigFields, LIST) || matchFieldType(instanceJigFields, SET)) {
            return コレクション;
        }
        if (matchFieldType(instanceJigFields, TypeId.STRING)) {
            return 文字列;
        }
        if (matchFieldType(instanceJigFields, BIG_DECIMAL)
                || matchFieldType(instanceJigFields, INTEGER)
                || matchFieldType(instanceJigFields, LONG)
                || matchFieldType(instanceJigFields, INT_PRIMITIVE)
                || matchFieldType(instanceJigFields, LONG_PRIMITIVE)) {
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
