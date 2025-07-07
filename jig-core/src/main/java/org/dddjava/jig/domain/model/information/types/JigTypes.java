package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.data.types.TypeIds;
import org.dddjava.jig.domain.model.information.members.JigMethod;
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
    private final Map<TypeId, JigType> map;

    public JigTypes(List<JigType> list) {
        this.list = list.stream().sorted(Comparator.comparing(jigType -> jigType.id())).toList();
        this.map = list.stream().collect(Collectors.toMap(
                jigType -> jigType.id(),
                Function.identity(),
                (left, right) -> {
                    logger.warn("{} が重複しています。完全修飾名が同じクラスを一度にロードしていることが原因です。依存関係にない複数モジュール群でJIGを実行している場合、JIGの実行対象を減らすか、異なるクラスであれば該当クラスのパッケージ名もしくはクラス名を変更してください。依存関係にあるモジュール群で発生している場合は実行時に意図しないクラスが使用される可能性がある実装が懸念されます。JIGは片方を採用して処理は続行しますが、クラスの実装が異なる場合は意図せぬ出力になります。",
                            left.id().fullQualifiedName());
                    return left;
                }));
    }

    public List<JigType> listCollectionType() {
        return list.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.コレクション)
                .toList();
    }

    public List<JigType> list() {
        return list;
    }

    public List<JigType> listMatches(Predicate<JigType> predicate) {
        return list.stream()
                .filter(predicate)
                .toList();
    }

    public JigTypes filter(Predicate<JigType> predicate) {
        return new JigTypes(listMatches(predicate));
    }

    public Optional<JigMethod> resolveJigMethod(JigMethodId jigMethodId) {
        // 全くラスの全メソッドを舐めるので効率化が必要かもしれないが、一旦これで
        return list.stream()
                .flatMap(jigType -> jigType.allJigMethodStream())
                .filter(jigMethod -> jigMethod.jigMethodIdentifier().equals(jigMethodId))
                // 複数件Hitすることはないが、実装上はありえるのでany
                .findAny();
    }

    public Optional<JigType> resolveJigType(TypeId typeId) {
        return Optional.ofNullable(map.get(typeId));
    }

    public boolean isService(JigMethodId jigMethodId) {
        return resolveJigMethod(jigMethodId)
                .flatMap(jigMethod -> resolveJigType(jigMethod.declaringType()))
                .filter(jigType -> jigType.typeCategory() == TypeCategory.Usecase)
                .isPresent();
    }

    public boolean isApplicationComponent(TypeId typeId) {
        return resolveJigType(typeId)
                .stream()
                .anyMatch(jigType -> jigType.typeCategory().isApplicationComponent());
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public Stream<JigType> orderedStream() {
        return list.stream();
    }

    public TypeIds typeIdentifiers() {
        return new TypeIds(map.keySet());
    }

    public boolean contains(TypeId typeId) {
        return map.containsKey(typeId);
    }

    public Stream<JigType> stream() {
        return list.stream();
    }
}
