package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドのID
 *
 * @param value
 */
public record JigMethodIdentifier(String value) {

    /**
     * 完全なIDを生成するファクトリ
     */
    public static JigMethodIdentifier from(TypeIdentifier declaringType, String methodName, List<TypeIdentifier> parameterTypeIdentifiers) {
        return new JigMethodIdentifier("%s#%s(%s)".formatted(declaringType.fullQualifiedName(), methodName,
                parameterTypeIdentifiers.stream().map(TypeIdentifier::fullQualifiedName).collect(Collectors.joining(","))));
    }

    public String name() {
        return value.split("[#()]")[1];
    }

    public String namespace() {
        return value.split("[#()]")[0];
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
                tuple.parameterTypeIdentifiers().stream().map(TypeIdentifier::asSimpleName).collect(Collectors.joining(",")));
    }

    public record Tuple(String declaringTypeName, String name, List<String> parameterTypeNames) {
        public TypeIdentifier declaringTypeIdentifier() {
            return TypeIdentifier.valueOf(declaringTypeName);
        }

        public List<TypeIdentifier> parameterTypeIdentifiers() {
            return parameterTypeNames.stream().map(TypeIdentifier::valueOf).collect(Collectors.toList());
        }
    }
}
