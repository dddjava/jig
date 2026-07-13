package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * メソッドの関連一覧
 */
public class MethodRelations implements CallerMethodsFactory {
    private static final Logger logger = LoggerFactory.getLogger(MethodRelations.class);

    private final Collection<MethodRelation> relations;

    /** calleeで引くためのインデックス。callerMethodsOfで必要になったときに構築する */
    private volatile Map<JigMethodId, Set<JigMethodId>> calleeIndex = null;

    public MethodRelations(Collection<MethodRelation> relations) {
        this.relations = relations;
    }

    public Collection<MethodRelation> relations() {
        return relations;
    }

    public static MethodRelations from(JigTypes jigTypes) {
        return new MethodRelations(jigTypes.orderedStream()
                .flatMap(jigType -> jigType.allJigMethodStream())
                .flatMap(jigMethod -> jigMethod.instructions().methodCallStream()
                        .filter(toMethod -> toMethod.isNotJSL()) // JSLを除く
                        .filter(toMethod -> !toMethod.isConstructor()) // コンストラクタ呼び出しを除く
                        .map(toMethod -> MethodRelation.from(jigMethod.jigMethodId(), toMethod.jigMethodId())))
                .toList());
    }

    public MethodRelations filterApplicationComponent(JigTypes jigTypes) {
        return relations().stream()
                .filter(methodRelation ->
                        jigTypes.isApplicationComponent(methodRelation.fromType())
                                && jigTypes.isApplicationComponent(methodRelation.toType())
                )
                .collect(collectingAndThen(toList(), MethodRelations::new));
    }

    /**
     * 呼び出し元メソッドのフィルタリング
     */
    @Override
    public CallerMethods callerMethodsOf(JigMethodId jigMethodId) {
        var index = calleeIndex;
        if (index == null) {
            // 競合しても同じ結果になるので排他はしない
            index = relations.stream()
                    .collect(groupingBy(MethodRelation::to, mapping(MethodRelation::from, toSet())));
            calleeIndex = index;
        }
        return new CallerMethods(index.getOrDefault(jigMethodId, Set.of()));
    }

    public MethodRelations filterFromRecursive(JigMethodId baseMethod, Predicate<JigMethodId> stopper) {
        var processedMethodId = new HashSet<JigMethodId>();

        return filterFromRecursiveInternal(baseMethod, stopper.or(jigMethodId -> {
            if (processedMethodId.contains(jigMethodId)) return true;
            processedMethodId.add(jigMethodId);
            return false;
        })).collect(collectingAndThen(toList(), MethodRelations::new));
    }

    private Stream<MethodRelation> filterFromRecursiveInternal(JigMethodId jigMethodId, Predicate<JigMethodId> stopper) {
        if (stopper.test(jigMethodId)) {
            logger.debug("stopped for {}", jigMethodId.value());
            return Stream.empty();
        }

        return relations.stream()
                .filter(methodRelation -> methodRelation.from().equals(jigMethodId))
                .flatMap(methodRelation -> Stream.concat(
                        Stream.of(methodRelation),
                        filterFromRecursiveInternal(methodRelation.to(), stopper)));
    }

    /**
     * lambdaの呼び出しを呼び出し元に展開する
     */
    public MethodRelations inlineLambda() {

        Map<JigMethodId, JigMethodId> lambdaCaller = new HashMap<>();

        List<MethodRelation> inlined = new ArrayList<>();
        List<MethodRelation> pending = new ArrayList<>();

        for (MethodRelation methodRelation : relations) {
            if (methodRelation.to().isLambda()) {
                // lambdaへの関連
                // この関連自体は残らない。ここで示されるfromにlambdaからの関連を置き換える
                lambdaCaller.put(methodRelation.to(), methodRelation.from());
            } else if (methodRelation.from().isLambda()) {
                // lambdaからの関連
                // 置き換え対象だが、この時点では何に置き換えたらいいか確定しないので一旦据え置く
                pending.add(methodRelation);
            } else {
                // lambdaを含まないものはそのまま
                inlined.add(methodRelation);
            }
        }

        // pendingのtoはlambdaでない（lambdaへの関連は最初の分岐で除かれている）ので、fromの解決だけでよい
        pending.stream()
                .map(methodRelation -> MethodRelation.from(
                        resolveLambdaCaller(methodRelation.from(), lambdaCaller),
                        methodRelation.to()))
                .forEach(inlined::add);

        return new MethodRelations(inlined);
    }

    /**
     * lambdaでなくなるまで呼び出し元を辿る。解決できない場合（呼び出し元の欠落や循環）は据え置く。
     */
    private JigMethodId resolveLambdaCaller(JigMethodId jigMethodId, Map<JigMethodId, JigMethodId> lambdaCaller) {
        var visited = new HashSet<JigMethodId>();
        var current = jigMethodId;
        while (current.isLambda()) {
            if (!visited.add(current)) {
                logger.debug("lambdaの呼び出し元の解決が循環したため据え置きます: {}", jigMethodId.value());
                return jigMethodId;
            }
            var caller = lambdaCaller.get(current);
            if (caller == null) {
                logger.debug("lambdaの呼び出し元が見つからないため据え置きます: {}", jigMethodId.value());
                return jigMethodId;
            }
            current = caller;
        }
        return current;
    }
}
