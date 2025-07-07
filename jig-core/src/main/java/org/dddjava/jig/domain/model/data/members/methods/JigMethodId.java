package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドのID
 */
public record JigMethodId(String value) implements Comparable<JigMethodId> {

    /**
     * 完全なIDを生成するファクトリ
     */
    public static JigMethodId from(TypeId declaringType, String methodName, List<TypeId> parameterTypeIds) {
        return new JigMethodId("%s#%s(%s)".formatted(declaringType.fullQualifiedName(), methodName,
                parameterTypeIds.stream().map(TypeId::fullQualifiedName).collect(Collectors.joining(","))));
    }

    public String name() {
        return value.split("[#()]")[1];
    }

    /**
     * メソッドの名前空間（＝定義されたクラスのFQN）を取得する
     */
    public String namespace() {
        return value.split("#")[0];
    }

    public Tuple tuple() {
        String[] split = value.split("[#()]");
        if (split.length == 2) {
            return new Tuple(split[0], split[1], List.of());
        }
        return new Tuple(split[0], split[1], Arrays.stream(split[2].split(",")).toList());
    }

    public boolean isLambda() {
        return value.contains("#lambda$");
    }

    public String simpleText() {
        Tuple tuple = tuple();
        return "%s.%s(%s)".formatted(
                tuple.declaringTypeIdentifier().asSimpleName(),
                tuple.name(),
                tuple.parameterTypeIdentifiers().stream().map(TypeId::asSimpleName).collect(Collectors.joining(",")));
    }

    public record Tuple(String declaringTypeName, String name, List<String> parameterTypeNames) {
        public TypeId declaringTypeIdentifier() {
            return TypeId.valueOf(declaringTypeName);
        }

        public List<TypeId> parameterTypeIdentifiers() {
            return parameterTypeNames.stream().map(TypeId::valueOf).toList();
        }
    }

    @Override
    public int compareTo(JigMethodId o) {
        return this.value.compareTo(o.value());
    }
}
