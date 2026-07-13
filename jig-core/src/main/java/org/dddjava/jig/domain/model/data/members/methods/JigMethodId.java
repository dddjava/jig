package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * メソッドのID
 *
 * メソッドのIDは、宣言されたクラスのFQN、メソッド名、引数のFQNリストから生成される。
 * 引数の型引数は持たない。同一引数型で型引数違いのメソッドはJavaで実装できないし、
 * 呼び出し側（MethoRef）も扱わないので、含めてしまうと色々無理が出てくるため。
 *
 * 例: com.example.Foo#bar(com.example.Baz, java.util.List)
 */
public record JigMethodId(String value) implements Comparable<JigMethodId> {

    /**
     * 完全なIDを生成するファクトリ
     */
    public static JigMethodId from(TypeId declaringType, String methodName, List<TypeId> parameterTypeIds) {
        return new JigMethodId("%s#%s(%s)".formatted(declaringType.fqn(), methodName,
                parameterTypeIds.stream().map(TypeId::fqn).collect(joining(","))));
    }

    public String name() {
        return value.substring(value.indexOf('#') + 1, value.indexOf('('));
    }

    /**
     * メソッドの名前空間（＝定義されたクラスのFQN）を取得する
     */
    public String namespace() {
        return value.substring(0, value.indexOf('#'));
    }

    public Tuple tuple() {
        // ホットパスなので正規表現splitでなくindexOfで分解する
        int hashIndex = value.indexOf('#');
        int parenIndex = value.indexOf('(', hashIndex);
        String parametersText = value.substring(parenIndex + 1, value.length() - 1);
        return new Tuple(
                value.substring(0, hashIndex),
                value.substring(hashIndex + 1, parenIndex),
                parametersText.isEmpty() ? List.of() : List.of(parametersText.split(",")));
    }

    public boolean isLambda() {
        return value.contains("#lambda$");
    }

    public String simpleText() {
        Tuple tuple = tuple();
        return "%s.%s(%s)".formatted(
                tuple.declaringTypeId().asSimpleName(),
                tuple.name(),
                tuple.parameterTypeIdList().stream().map(TypeId::asSimpleName).collect(joining(",")));
    }

    public String fqn() {
        return value;
    }

    public record Tuple(String declaringTypeName, String name, List<String> parameterTypeNameList) {
        public TypeId declaringTypeId() {
            return TypeId.valueOf(declaringTypeName);
        }

        public List<TypeId> parameterTypeIdList() {
            return parameterTypeNameList.stream().map(TypeId::valueOf).toList();
        }
    }

    @Override
    public int compareTo(JigMethodId o) {
        return this.value.compareTo(o.value);
    }
}
