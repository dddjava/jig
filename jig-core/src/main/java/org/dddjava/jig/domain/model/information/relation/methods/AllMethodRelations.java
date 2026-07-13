package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;

/**
 * すべてのメソッド関連
 *
 * JigTypesの全命令から抽出した関連の全体集合。
 * calleeで引くインデックスを持ち、呼び出し元メソッドの逆引きに応える。
 */
public class AllMethodRelations implements CallerMethodsFactory {

    private final MethodRelations methodRelations;
    private final Map<JigMethodId, Set<JigMethodId>> calleeIndex;

    private AllMethodRelations(MethodRelations methodRelations) {
        this.methodRelations = methodRelations;
        this.calleeIndex = methodRelations.relations().stream()
                .collect(groupingBy(MethodRelation::to, mapping(MethodRelation::from, toSet())));
    }

    public static AllMethodRelations from(JigTypes jigTypes) {
        return new AllMethodRelations(new MethodRelations(jigTypes.orderedStream()
                .flatMap(jigType -> jigType.allJigMethodStream())
                .flatMap(jigMethod -> jigMethod.instructions().methodCallStream()
                        .filter(toMethod -> toMethod.isNotJSL()) // JSLを除く
                        .filter(toMethod -> !toMethod.isConstructor()) // コンストラクタ呼び出しを除く
                        .map(toMethod -> MethodRelation.from(jigMethod.jigMethodId(), toMethod.jigMethodId())))
                .toList()));
    }

    /**
     * 呼び出し元メソッドのフィルタリング
     */
    @Override
    public CallerMethods callerMethodsOf(JigMethodId jigMethodId) {
        return new CallerMethods(calleeIndex.getOrDefault(jigMethodId, Set.of()));
    }

    public MethodRelations filterApplicationComponent(JigTypes jigTypes) {
        return methodRelations.filterApplicationComponent(jigTypes);
    }
}
