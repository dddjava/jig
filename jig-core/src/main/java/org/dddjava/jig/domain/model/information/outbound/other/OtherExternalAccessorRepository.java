package org.dddjava.jig.domain.model.information.outbound.other;

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
 * 外部アクセサ（その他）リポジトリ
 */
public record OtherExternalAccessorRepository(Collection<OtherExternalAccessor> values) {

    public static OtherExternalAccessorRepository empty() {
        return new OtherExternalAccessorRepository(List.of());
    }

    public static OtherExternalAccessorRepository from(JigTypes jigTypes) {
        List<OtherExternalAccessor> result = jigTypes.stream()
                .flatMap(jigType -> {
                    // インスタンスフィールドのうち、JIG範囲外かつJava標準型でない型を「外部型」として抽出
                    // TODO: 外部型でないものの条件を増やしたり、除外を設定で追加できるようにしたい
                    Set<TypeId> externalFieldTypes = jigType.instanceJigFields().fields().stream()
                            .map(jigField -> jigField.typeId())
                            .filter(typeId -> !jigTypes.contains(typeId))
                            .filter(typeId -> !typeId.isJavaStandardLanguageType())
                            .collect(Collectors.toSet());

                    // 外部型を持たないものは外部アクセサではない
                    if (externalFieldTypes.isEmpty()) {
                        return Stream.of();
                    }

                    // アクセサメソッドとその呼び出しを個別に収集
                    // インスタンスフィールドを使用するものなのでインスタンスメソッドだけとしたいところだが、
                    // private staticメソッドに引数でインスタンスを渡して呼ぶようなケースもあったりするので、当面はこれで。
                    var operations = jigType.allJigMethodStream()
                            // lambdaメソッドを除外する。ここで除外しておかないと二重で登録されることになる
                            .filter(jigMethod -> !jigMethod.jigMethodDeclaration().header().isLambdaSyntheticMethod())
                            .flatMap(jigMethod -> {
                                // lambdaの中のメソッド呼び出しも展開して探索する
                                var externalMethodCalls = jigMethod.lambdaInlinedMethodCallStream()
                                        .filter(mc -> externalFieldTypes.contains(mc.methodOwner()))
                                        .toList();
                                if (externalMethodCalls.isEmpty()) {
                                    return Stream.empty();
                                }
                                return Stream.of(new OtherExternalAccessorOperation(jigType.id(), jigMethod, externalMethodCalls));
                            })
                            .toList();

                    // フィールドのメソッドを呼び出している処理が一つもない場合は外部アクセサではない
                    if (operations.isEmpty()) {
                        return Stream.of();
                    }

                    return Stream.of(new OtherExternalAccessor(jigType.id(), operations));
                })
                .toList();
        return new OtherExternalAccessorRepository(result);
    }

    public Optional<OtherExternalAccessorOperation> findAccessorOperation(JigMethod jigMethod) {
        return values.stream()
                .filter(otherExternalAccessor -> otherExternalAccessor.typeId().equals(jigMethod.declaringType()))
                .flatMap(otherExternalAccessor -> otherExternalAccessor.operations().stream())
                .filter(operation -> operation.accessorJigMethod().jigMethodId().equals(jigMethod.jigMethodId()))
                .findAny();
    }
}
