package org.dddjava.jig.domain.model.data.members.methods;

/**
 * メソッドパラメータ名の取得元
 */
public enum ParameterNameSource {
    /** MethodParameters属性から取得した名前 */
    METHOD_PARAMETERS,
    /** LocalVariableTable属性から取得した名前 */
    LOCAL_VARIABLE,
    /** 名前情報なし。位置ベースで生成（arg0等） */
    POSITIONAL
}
