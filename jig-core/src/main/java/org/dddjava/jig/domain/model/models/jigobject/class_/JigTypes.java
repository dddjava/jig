package org.dddjava.jig.domain.model.models.jigobject.class_;

import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JigTypes {

    private final List<JigType> list;

    public JigTypes(List<JigType> list) {
        this.list = list;
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
                .flatMap(jigType -> jigType.methods().list().stream())
                .filter(jigMethod -> jigMethod.declaration().identifier().equals(methodIdentifier))
                // 複数件Hitすることはないが、実装上はありえるのでany
                .findAny();
    }
}
