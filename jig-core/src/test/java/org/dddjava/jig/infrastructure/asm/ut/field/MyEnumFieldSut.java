package org.dddjava.jig.infrastructure.asm.ut.field;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

public enum MyEnumFieldSut {
    通常の列挙値1,
    通常の列挙値2,
    @Deprecated
    Deprecatedな列挙値
    ;

    static final MyEnumFieldSut static_finalフィールド = null;
    static MyEnumFieldSut staticフィールド;

    final MyEnumFieldSut finalフィールド = null;
    MyEnumFieldSut フィールド;

    @Deprecated
    Object deprecatedField;

    static JigDocument 別のenum型のstaticフィールド;
    JigDocument 別のenum型のフィールド;
}
