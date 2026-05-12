package org.dddjava.jig.adapter.json;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodParameter;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON文字列生成ユーティリティ。
 * ドキュメント用のインラインJSONを組み立てる際の共通処理を提供する。
 */
public final class JsonSupport {

    private JsonSupport() {
    }

    /**
     * JSON文字列内の特殊文字をエスケープする。
     *
     * @param string エスケープ対象の文字列
     * @return エスケープ済みの文字列
     */
    public static String escape(String string) {
        return string
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    /**
     * 文字列コレクションをJSON配列文字列に変換する。
     *
     * @param values 文字列のコレクション
     * @return JSON配列形式の文字列（例: ["a","b"]）
     */
    public static String toJsonStringList(Collection<String> values) {
        return values.stream()
                .map(JsonSupport::escape)
                .map(value -> "\"" + value + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * キーが文字列・値がJSON断片のMapをJSONオブジェクト文字列に変換する。
     *
     * @param map キーとJSON断片のマップ
     * @return JSONオブジェクト形式の文字列（例: {"key1":value1,"key2":value2}）
     */
    public static String mapToJson(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> "\"%s\":%s".formatted(escape(e.getKey()), e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    /**
     * フィールドのJSONを組み立てる
     * {@code
     * @typedef {Object} JigField
     * @property {string} name
     * @property {TypeRef} typeRef
     * @property {boolean} isDeprecated
     * }
     */
    public static JsonObjectBuilder buildFieldJson(JigField field) {
        return Json.object("name", field.nameText())
                .and("typeRef", buildTypeRef(field.jigTypeReference()))
                .and("isDeprecated", field.isDeprecated());
    }

    /**
     * メソッドのJSONを組み立てる
     * {@code
     * @typedef {Object} JigMethod
     * @property {string} fqn
     * @property {MethodParameter[]} parameters
     * @property {TypeRef} returnTypeRef
     * @property {boolean} isDeprecated
     * }
     */
    public static JsonObjectBuilder buildMethodJson(JigMethod jigMethod) {
        return Json.object("fqn", jigMethod.fqn())
                .and("visibility", jigMethod.visibility())
                .and("parameters", Json.arrayObjects(jigMethod.parameterList().stream()
                        .map(JsonSupport::buildParameterJson)
                        .toList()))
                .and("returnTypeRef", buildTypeRef(jigMethod.returnType()))
                .and("isDeprecated", jigMethod.isDeprecated());
    }

    /**
     * メソッドパラメータのJSONを組み立てる
     * {@code
     * @typedef {Object} MethodParameter
     * @property {string} name
     * @property {string} nameSource
     * @property {TypeRef} typeRef
     * }
     */
    private static JsonObjectBuilder buildParameterJson(JigMethodParameter parameter) {
        return Json.object("name", parameter.name())
                .and("nameSource", parameter.nameSource().name())
                .and("typeRef", buildTypeRef(parameter.typeReference()));
    }

    /**
     * 型参照のJSONを組み立てる
     * {@code
     * @typedef {Object} TypeRef
     * @property {string} fqn
     * @property {TypeRef[]} [typeArgumentRefs]
     * }
     */
    private static JsonObjectBuilder buildTypeRef(JigTypeReference jigTypeReference) {
        var obj = Json.object("fqn", jigTypeReference.fqn());
        // 型引数がない場合は fqn だけのオブジェクトにする
        if (jigTypeReference.typeArgumentList().isEmpty()) return obj;

        return obj.and("typeArgumentRefs", Json.arrayObjects(jigTypeReference.typeArgumentList().stream()
                .map(JigTypeArgument::jigTypeReference)
                .map(JsonSupport::buildTypeRef)
                .toList()));
    }
}
