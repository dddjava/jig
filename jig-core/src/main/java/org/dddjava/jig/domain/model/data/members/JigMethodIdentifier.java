package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドのID
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
}
