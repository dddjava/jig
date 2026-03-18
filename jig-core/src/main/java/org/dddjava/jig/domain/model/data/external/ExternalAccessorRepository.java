package org.dddjava.jig.domain.model.data.external;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 外部アクセサリポジトリ
 * JIG読み取り範囲外のクラスのメソッドを呼び出しているクラスの一覧を保持する。
 */
public record ExternalAccessorRepository(Collection<ExternalAccessor> values) {

    public static ExternalAccessorRepository empty() {
        return new ExternalAccessorRepository(List.of());
    }

    public static ExternalAccessorRepository from(JigTypes jigTypes) {
        List<ExternalAccessor> result = jigTypes.stream()
                .flatMap(jigType -> {
                    // インスタンスフィールドのうち、JIG範囲外かつJava標準型でない型を抽出
                    Set<TypeId> externalFieldTypes = jigType.instanceJigFields().fields().stream()
                            .map(jigField -> jigField.typeId())
                            .filter(typeId -> !jigTypes.contains(typeId))
                            .filter(typeId -> !typeId.isJavaLanguageType())
                            .collect(Collectors.toSet());

                    if (externalFieldTypes.isEmpty()) {
                        return Stream.of();
                    }

                    // アクセッサメソッドとその呼び出しを個別に収集
                    var operations = jigType.allJigMethodStream()
                            .map(jigMethod -> {
                                var externalMethodCalls = jigMethod.usingMethods().invokedMethodStream()
                                        .filter(mc -> externalFieldTypes.contains(mc.methodOwner()))
                                        .toList();
                                return new ExternalAccessorOperation(jigType.id(), jigMethod, externalMethodCalls);
                            }).toList();
                    return Stream.of(new ExternalAccessor(jigType.id(), operations));
                })
                .toList();
        return new ExternalAccessorRepository(result);
    }

    public Optional<ExternalAccessorOperation> findAccessorOperation(JigMethod jigMethod) {
        return values.stream()
                .filter(externalAccessor -> externalAccessor.typeId().equals(jigMethod.declaringType()))
                .flatMap(externalAccessor -> externalAccessor.operations().stream())
                .filter(operation -> operation.accessorJigMethod().jigMethodId().equals(jigMethod.jigMethodId()))
                .findAny();
    }
}
