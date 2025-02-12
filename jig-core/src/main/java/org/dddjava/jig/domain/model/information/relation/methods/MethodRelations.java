package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * メソッドの使用しているメソッド一覧
 */
public class MethodRelations implements CallerMethodsFactory {
    private static final Logger logger = LoggerFactory.getLogger(MethodRelations.class);

    private final List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    public static MethodRelations from(JigTypes jigTypes) {
        return jigTypes.stream()
                .flatMap(jigType -> jigType.allJigMethodStream()
                        .flatMap(jigMethod -> jigMethod.usingMethods().invokedMethodStream()
                                .filter(toMethod -> !toMethod.isJSL()) // JSLを除く
                                .filter(toMethod -> !toMethod.isConstructor()) // コンストラクタ呼び出しを除く
                                .map(toMethod -> new MethodRelation(jigMethod.declaration().jigMethodIdentifier(), toMethod.jigMethodIdentifier()))))
                .collect(collectingAndThen(toList(), MethodRelations::new));
    }

    public static MethodRelations filterApplicationComponent(JigTypes jigTypes, MethodRelations methodRelations) {
        return methodRelations.list().stream()
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
    public CallerMethods callerMethodsOf(JigMethodIdentifier jigMethodIdentifier) {
        List<JigMethodIdentifier> callers = list.stream()
                .filter(methodRelation -> methodRelation.calleeMethodIs(jigMethodIdentifier))
                .map(MethodRelation::from)
                .collect(toList());
        return new CallerMethods(callers);
    }

    public String mermaidEdgeText(Function<JigMethodIdentifier, Optional<String>> converter) {
        // 型がMethodRelationではなくなるのでここで文字列化してしまう
        return list.stream()
                .flatMap(methodRelation ->
                        converter.apply(methodRelation.from()).flatMap(fromText -> converter.apply(methodRelation.to()).map(toText ->
                                "%s --> %s".formatted(fromText, toText))
                        ).stream()
                )
                .sorted()
                .distinct()
                .collect(Collectors.joining("\n"));
    }

    public MethodRelations filterFromRecursive(JigMethodIdentifier baseMethod) {
        var processedMethodId = new HashSet<JigMethodIdentifier>();

        return filterFromRecursiveInternal(baseMethod, (jigMethodIdentifier -> {
            if (processedMethodId.contains(jigMethodIdentifier)) return true;
            processedMethodId.add(jigMethodIdentifier);
            return false;
        })).collect(collectingAndThen(toList(), MethodRelations::new));
    }

    public MethodRelations filterFromRecursive(JigMethodIdentifier baseMethod, Predicate<JigMethodIdentifier> stopper) {
        var processedMethodId = new HashSet<JigMethodIdentifier>();

        return filterFromRecursiveInternal(baseMethod, stopper.or(jigMethodIdentifier -> {
            if (processedMethodId.contains(jigMethodIdentifier)) return true;
            processedMethodId.add(jigMethodIdentifier);
            return false;
        })).collect(collectingAndThen(toList(), MethodRelations::new));
    }

    private Stream<MethodRelation> filterFromRecursiveInternal(JigMethodIdentifier jigMethodIdentifier, Predicate<JigMethodIdentifier> stopper) {
        if (stopper.test(jigMethodIdentifier)) {
            logger.debug("stopped for {}", jigMethodIdentifier.value());
            return Stream.empty();
        }

        return list.stream()
                .filter(methodRelation -> methodRelation.from().equals(jigMethodIdentifier))
                .flatMap(methodRelation -> Stream.concat(
                        Stream.of(methodRelation),
                        filterFromRecursiveInternal(methodRelation.to(), stopper)));
    }

    public MethodRelations filterTo(JigMethodIdentifier jigMethodIdentifier) {
        return list.stream()
                .filter(methodRelation -> methodRelation.to().equals(jigMethodIdentifier))
                .collect(collectingAndThen(toList(), MethodRelations::new));
    }

    public MethodRelations merge(MethodRelations others) {
        return Stream.concat(list.stream(), others.list.stream())
                .collect(collectingAndThen(toList(), MethodRelations::new));
    }

    /**
     * lambdaの呼び出しを呼び出し元に展開する
     */
    public MethodRelations inlineLambda() {

        Map<JigMethodIdentifier, JigMethodIdentifier> replace = new HashMap<>();

        List<MethodRelation> inlined = new ArrayList<>();
        List<MethodRelation> pending = new ArrayList<>();

        for (MethodRelation methodRelation : list) {
            if (methodRelation.to().isLambda()) {
                // lambdaへの関連
                // この関連自体は残らない。ここで示されるfromにlambdaからの関連を置き換える
                replace.put(methodRelation.to(), methodRelation.from());
            } else if (methodRelation.from().isLambda()) {
                // lambdaからの関連
                // 置き換え対象だが、この時点では何に置き換えたらいいか確定しないので一旦据え置く
                pending.add(methodRelation);
            } else {
                // lambdaを含まないものはそのまま
                inlined.add(methodRelation);
            }
        }

        // 置き換え先がlambdaのものを展開する
        for (var entry : replace.entrySet()) {
            if (entry.getValue().isLambda()) {
                replace.replace(entry.getKey(), replace.get(entry.getValue()));
            }
        }

        var list2 = pending.stream()
                .map(methodRelation ->
                        new MethodRelation(
                                replace.getOrDefault(methodRelation.from(), methodRelation.from()),
                                replace.getOrDefault(methodRelation.to(), methodRelation.to())
                        ))
                .toList();

        inlined.addAll(list2);
        return new MethodRelations(inlined);
    }

    public List<MethodRelation> list() {
        return list;
    }

    public Stream<JigMethodIdentifier> jigMethodIdentifierStream() {
        return list.stream()
                .flatMap(methodRelation -> Stream.of(methodRelation.from(), methodRelation.to()))
                .distinct();
    }
}
