package org.dddjava.jig.domain.model.models.jigobject.class_;

import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelation;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class JigTypes {

    private final List<JigType> list;

    public JigTypes(List<JigType> list) {
        this.list = list;
    }

    public MethodRelations methodRelations() {
        return list().stream()
                .flatMap(jigType -> jigType.methodStream())
                // メソッドの関連に変換
                .flatMap(jigMethod -> jigMethod.methodInstructions().stream()
                        .filter(toMethod -> !toMethod.isJSL()) // JSLを除く
                        .filter(toMethod -> !toMethod.isConstructor()) // コンストラクタ呼び出しを除く
                        .map(toMethod -> new MethodRelation(jigMethod.declaration(), toMethod)))
                .collect(collectingAndThen(toList(), MethodRelations::new));
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
                .flatMap(jigType -> jigType.methodStream())
                .filter(jigMethod -> jigMethod.declaration().identifier().equals(methodIdentifier))
                // 複数件Hitすることはないが、実装上はありえるのでany
                .findAny();
    }
}
