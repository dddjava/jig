package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.information.method.JigMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JigTypes {
    private static final Logger logger = LoggerFactory.getLogger(JigTypes.class);

    private final List<JigType> list;
    private final Map<TypeIdentifier, JigType> map;

    public JigTypes(List<JigType> list) {
        this.list = list.stream().sorted(Comparator.comparing(JigType::identifier)).toList();
        this.map = list.stream().collect(Collectors.toMap(
                JigType::identifier,
                Function.identity(),
                (left, right) -> {
                    logger.warn("{} が重複しています。完全修飾名が同じクラスを一度にロードしていることが原因です。依存関係にない複数モジュール群でJIGを実行している場合、JIGの実行対象を減らすか、異なるクラスであれば該当クラスのパッケージ名もしくはクラス名を変更してください。依存関係にあるモジュール群で発生している場合は実行時に意図しないクラスが使用される可能性がある実装が懸念されます。JIGは片方を採用して処理は続行しますが、クラスの実装が異なる場合は意図せぬ出力になります。",
                            left.identifier().fullQualifiedName());
                    return left;
                }));
    }

    public List<JigType> listCollectionType() {
        return list.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.コレクション)
                .collect(Collectors.toList());
    }

    public List<JigType> list() {
        return list;
    }

    public List<JigType> listMatches(Predicate<JigType> predicate) {
        return list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public JigTypes filter(Predicate<JigType> predicate) {
        return new JigTypes(listMatches(predicate));
    }

    public Optional<JigMethod> resolveJigMethod(MethodIdentifier methodIdentifier) {
        return list.stream()
                // 同じクラスでフィルタ
                .filter(jigType -> jigType.identifier().equals(methodIdentifier.declaringType()))
                // メソッドに絞り込み
                .flatMap(jigType -> jigType.allJigMethodStream())
                .filter(jigMethod -> jigMethod.declaration().identifier().equals(methodIdentifier))
                // 複数件Hitすることはないが、実装上はありえるのでany
                .findAny();
    }

    public Optional<JigType> resolveJigType(TypeIdentifier typeIdentifier) {
        return Optional.ofNullable(map.get(typeIdentifier));
    }

    public boolean isService(MethodIdentifier methodIdentifier) {
        return resolveJigType(methodIdentifier.declaringType())
                .stream().anyMatch(type -> type.typeCategory() == TypeCategory.Usecase);
    }

    public boolean isEndpointOrApplication(TypeIdentifier typeIdentifier) {
        return resolveJigType(typeIdentifier)
                .stream()
                .anyMatch(jigType -> jigType.typeCategory().isBoundary());
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public Stream<JigType> stream() {
        return list.stream();
    }

    public TypeIdentifiers typeIdentifiers() {
        return new TypeIdentifiers(map.keySet());
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return map.containsKey(typeIdentifier);
    }

}
