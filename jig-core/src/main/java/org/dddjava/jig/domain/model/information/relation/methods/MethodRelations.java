package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
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
public record MethodRelations(List<MethodRelation> list) implements CallerMethodsFactory {
    private static final Logger logger = LoggerFactory.getLogger(MethodRelations.class);

    public static MethodRelations lambdaInlined(JigTypes jigTypes) {
        return new MethodRelations(jigTypes.orderedStream()
                .flatMap(jigType -> jigType.allJigMethodStream())
                .flatMap(jigMethod -> {
                    Instructions instructions = jigMethod.instructions();
                    return instructions.lambdaInlinedMethodCallStream()
                            .filter(methodCall -> !methodCall.isJSL()) // JSLを除く
                            .filter(methodCall -> !methodCall.isConstructor()) // コンストラクタ呼び出しを除く
                            .map(methodCall -> MethodRelation.from(jigMethod.jigMethodId(), methodCall.jigMethodId()));
                }).toList());
    }

    public static MethodRelations from(JigTypes jigTypes) {
        return new MethodRelations(jigTypes.orderedStream()
                .flatMap(jigType -> jigType.allJigMethodStream()
                        .flatMap(jigMethod -> jigMethod.usingMethods().invokedMethodStream()
                                .filter(toMethod -> !toMethod.isJSL()) // JSLを除く
                                .filter(toMethod -> !toMethod.isConstructor()) // コンストラクタ呼び出しを除く
                                .map(toMethod -> MethodRelation.from(jigMethod.jigMethodId(), toMethod.jigMethodId()))))
                .toList());
    }

    public MethodRelations filterApplicationComponent(JigTypes jigTypes) {
        return list().stream()
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
        return new CallerMethods(list.stream()
                .filter(methodRelation -> methodRelation.calleeMethodIs(jigMethodId))
                .map(MethodRelation::from)
                .collect(Collectors.toSet()));
    }

    public String mermaidEdgeText(Function<JigMethodId, Optional<String>> converter) {
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

    public MethodRelations filterFromRecursive(JigMethodId baseMethod) {
        var processedMethodId = new HashSet<JigMethodId>();

        return filterFromRecursiveInternal(baseMethod, (jigMethodId -> {
            if (processedMethodId.contains(jigMethodId)) return true;
            processedMethodId.add(jigMethodId);
            return false;
        })).collect(collectingAndThen(toList(), MethodRelations::new));
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

        return list.stream()
                .filter(methodRelation -> methodRelation.from().equals(jigMethodId))
                .flatMap(methodRelation -> Stream.concat(
                        Stream.of(methodRelation),
                        filterFromRecursiveInternal(methodRelation.to(), stopper)));
    }

    public MethodRelations filterTo(JigMethodId jigMethodId) {
        return list.stream()
                .filter(methodRelation -> methodRelation.to().equals(jigMethodId))
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

        Map<JigMethodId, JigMethodId> replace = new HashMap<>();

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
                        MethodRelation.from(
                                replace.getOrDefault(methodRelation.from(), methodRelation.from()),
                                replace.getOrDefault(methodRelation.to(), methodRelation.to())
                        ))
                .toList();

        inlined.addAll(list2);
        return new MethodRelations(inlined);
    }

    public Stream<JigMethodId> toJigMethodIdStream() {
        return list.stream()
                .flatMap(methodRelation -> Stream.of(methodRelation.from(), methodRelation.to()))
                .distinct();
    }
}
