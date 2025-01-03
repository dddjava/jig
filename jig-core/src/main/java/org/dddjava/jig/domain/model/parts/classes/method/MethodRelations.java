package org.dddjava.jig.domain.model.parts.classes.method;

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
public class MethodRelations {
    private static final Logger logger = LoggerFactory.getLogger(MethodRelations.class);

    private final List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    /**
     * 呼び出し元メソッドのフィルタリング
     */
    public CallerMethods callerMethodsOf(MethodDeclaration calleeMethod) {
        List<MethodDeclaration> callers = list.stream()
                .filter(methodRelation -> methodRelation.calleeMethodIs(calleeMethod))
                .map(MethodRelation::from)
                .collect(toList());
        return new CallerMethods(callers);
    }

    public String mermaidEdgeText() {
        return list.stream()
                .map(MethodRelation::mermaidEdgeText)
                .collect(Collectors.joining("\n"));
    }

    public String mermaidEdgeText(Function<MethodDeclaration, Optional<String>> converter) {
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

    public MethodRelations filterFromRecursive(MethodDeclaration methodDeclaration, Predicate<MethodIdentifier> stopper) {
        var processedMethodId = new HashSet<MethodIdentifier>();

        return filterFromRecursiveInternal(methodDeclaration, stopper.or(methodIdentifier -> {
            if (processedMethodId.contains(methodIdentifier)) return true;
            processedMethodId.add(methodIdentifier);
            return false;
        }))
                .collect(collectingAndThen(toList(), MethodRelations::new));
    }

    private Stream<MethodRelation> filterFromRecursiveInternal(MethodDeclaration baseMethod, Predicate<MethodIdentifier> stopper) {
        if (stopper.test(baseMethod.identifier())) {
            logger.debug("stopped for {}", baseMethod.asFullNameText());
            return Stream.empty();
        }

        return list.stream()
                .filter(methodRelation -> methodRelation.from().sameIdentifier(baseMethod))
                .flatMap(methodRelation -> Stream.concat(
                        Stream.of(methodRelation),
                        filterFromRecursiveInternal(methodRelation.to(), stopper)));
    }

    public Set<MethodIdentifier> methodIdentifiers() {
        return list.stream()
                .flatMap(methodRelation -> Stream.of(methodRelation.from(), methodRelation.to()))
                .map(MethodDeclaration::identifier)
                .collect(Collectors.toSet());
    }

    public MethodRelations filterTo(MethodDeclaration declaration) {
        return list.stream()
                .filter(methodRelation -> methodRelation.to().sameIdentifier(declaration))
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

        Map<MethodIdentifier, MethodDeclaration> replace = new HashMap<>();

        List<MethodRelation> inlined = new ArrayList<>();
        List<MethodRelation> pending = new ArrayList<>();

        for (MethodRelation methodRelation : list) {
            if (methodRelation.to().isLambda()) {
                // lambdaへの関連
                // この関連自体は残らない。ここで示されるfromにlambdaからの関連を置き換える
                replace.put(methodRelation.to().identifier(), methodRelation.from());
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
                replace.replace(entry.getKey(), replace.get(entry.getValue().identifier()));
            }
        }

        var list2 = pending.stream()
                .map(methodRelation ->
                        new MethodRelation(
                                replace.getOrDefault(methodRelation.from().identifier(), methodRelation.from()),
                                replace.getOrDefault(methodRelation.to().identifier(), methodRelation.to())
                        ))
                .toList();

        inlined.addAll(list2);
        return new MethodRelations(inlined);
    }

    public List<MethodRelation> list() {
        return list;
    }

}
