package org.dddjava.jig.domain.model.models.jigobject.class_;

import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class JigTypes {

    private final List<JigType> list;
    private final Map<TypeIdentifier, JigType> map;

    public JigTypes(List<JigType> list) {
        this.list = list;
        this.map = list.stream().collect(Collectors.toMap(JigType::identifier, Function.identity()));
    }

    private MethodRelations methodRelations;

    public MethodRelations methodRelations() {
        if (methodRelations == null) {
            methodRelations = list().stream()
                    .flatMap(JigType::methodRelationStream)
                    .collect(collectingAndThen(toList(), MethodRelations::new));
        }
        return methodRelations;
    }

    public List<JigType> listCollectionType() {
        return list.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.コレクション)
                .sorted(Comparator.comparing(JigType::identifier))
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
        return list.stream()
                .filter(predicate)
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
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

    public boolean isApplication(MethodIdentifier methodIdentifier) {
        return resolveJigType(methodIdentifier.declaringType())
                .stream().anyMatch(type -> type.typeCategory() == TypeCategory.Application);
    }

    public boolean isEndpointOrApplication(TypeIdentifier typeIdentifier) {
        var typeCategory = resolveJigType(typeIdentifier)
                .map(jigType -> jigType.typeCategory())
                .orElse(TypeCategory.Others);
        return typeCategory.isApplicationComponent();
    }
}
