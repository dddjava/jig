package org.dddjava.jig.domain.model.data.external;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 外部アクセサリポジトリ
 * JIG読み取り範囲外のクラスのメソッドを呼び出しているクラスの一覧を保持する。
 */
public record ExternalAccessorRepository(List<ExternalAccessor> values) {

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
                        return java.util.stream.Stream.empty();
                    }

                    // アクセッサメソッドとその呼び出しを個別に収集
                    return jigType.allJigMethodStream()
                            .flatMap(jigMethod -> {
                                String accessorMethodName = jigMethod.name();
                                return jigMethod.usingMethods().invokedMethodStream()
                                        .filter(mc -> externalFieldTypes.contains(mc.methodOwner()))
                                        .map(mc -> new ExternalAccessor(
                                                jigType.id(),
                                                accessorMethodName,
                                                mc.methodOwner(),
                                                mc.methodName()
                                        ));
                            });
                })
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream().toList();
        return new ExternalAccessorRepository(result);
    }
}
