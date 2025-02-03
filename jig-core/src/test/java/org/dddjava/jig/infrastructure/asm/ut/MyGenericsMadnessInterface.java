package org.dddjava.jig.infrastructure.asm.ut;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// テスト用のぐちゃぐちゃなやつ
public interface MyGenericsMadnessInterface<T extends List<Predicate<T>>>
        extends Consumer<Consumer<Function<T, Consumer<Integer>>>> {
}

// 再帰型境界
interface RecursiveGenerics<T extends RecursiveGenerics<T>> {
}

// ネストされたジェネリクスの組み合わせ
interface NestedGenerics<T extends Map<String, List<Function<T, Integer>>>> {
}

// 多重インターフェース継承
interface ComplexInterface<A, B, C>
        extends Consumer<Function<A, B>>, Function<A, C> {
}