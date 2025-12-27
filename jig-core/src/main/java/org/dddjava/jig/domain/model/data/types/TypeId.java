package org.dddjava.jig.domain.model.data.types;

import io.micrometer.core.instrument.Metrics;
import org.dddjava.jig.domain.model.data.packages.PackageId;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 型の識別子
 */
public record TypeId(String value) implements Comparable<TypeId> {

    // 判定に使用する型
    public static final TypeId DEPRECATED_ANNOTATION = TypeId.valueOf("java.lang.Deprecated");
    public static final TypeId OBJECT = TypeId.valueOf("java.lang.Object");
    public static final TypeId ENUM = TypeId.valueOf("java.lang.Enum");
    public static final TypeId RECORD = TypeId.valueOf("java.lang.Record");
    // 文字列
    public static final TypeId STRING = TypeId.valueOf("java.lang.String");
    // 日付
    public static final TypeId LOCAL_DATE = TypeId.valueOf("java.time.LocalDate");

    private static final Map<String, TypeId> cache = new ConcurrentHashMap<>();

    /**
     * 与えられた文字列のままのTypeIdを生成するファクトリ。
     */
    public static TypeId valueOf(String value) {
        // FIXME 定数の初期化時点でcacheが初期化されていないためNPEが起こる。回避するためにnullチェック。
        if (cache == null) {
            return new TypeId(value);
        }
        if (cache.containsKey(value)) {
            Metrics.counter("cache.gets", "cache", "typeId", "result", "hit").increment();
            return cache.get(value);
        }
        Metrics.counter("cache.gets", "cache", "typeId", "result", "miss").increment();
        var instance = new TypeId(value);
        cache.put(value, instance);
        return instance;
    }

    /**
     * @return "org.dddjava.jig.domain.model.data.types.TypeId"
     */
    public String fqn() {
        return value;
    }

    /**
     * パッケージなしのクラス名
     * ネストクラスでは `Hoge$Fuga` のように外側のクラス名を含む。
     *
     * TODO Class#getSimpleName() はネストクラスでも単純名を返すためこの名前はよくない
     *
     * @return "TypeId"
     */
    public String asSimpleText() {
        int lastDotIndex = value.lastIndexOf('.');
        return (lastDotIndex != -1) ? value.substring(lastDotIndex + 1) : value;
    }

    /**
     * ネストしている場合も考慮した単純名
     */
    public String asSimpleName() {
        String text = asSimpleText();
        // $を含んでいた場合、一番後ろの$以降を単純名とする。
        // 一番後ろなのはクラスは多重ネストが可能なため。
        // class Hoge$Fuga {} のようなクラスの記述は可能でこの場合は Fuga となってしまうが、
        // バイトコード上は区別できず、慣習的にも作らないのでこのケースは考慮しない。
        int lastDollarIndex = text.lastIndexOf('$');
        return (lastDollarIndex != -1) ? text.substring(lastDollarIndex + 1) : text;
    }

    private boolean hasPackage() {
        return value.contains(".");
    }

    public PackageId packageId() {
        if (!hasPackage()) {
            return PackageId.defaultPackage();
        }
        return PackageId.valueOf(value.substring(0, value.lastIndexOf(".")));
    }

    public TypeId normalize() {
        // コンパイラが生成する継承クラス名を元の名前にする
        // enumで Hoge$1 などになっているものが対象
        if (value.indexOf('$') == -1) {
            return this;
        }
        // $の後ろが数字の場合のみ除去する。
        // こうしないとネストクラスが外側のクラスとして扱われてしまう。
        // class Hoge$1 {} のようなクラスの記述は可能だが、慣習的にも作らないのでこのケースは考慮しない。
        return valueOf(value.replaceFirst("\\$\\d+", ""));
    }

    public boolean isBoolean() {
        Class<?>[] booleanTypes = {Boolean.class, boolean.class};
        return Arrays.stream(booleanTypes).anyMatch(clazz -> clazz.getName().equals(value));
    }

    public boolean isPrimitive() {
        return value.matches("(int|long|boolean|double|float|byte|char|short)(\\[])?");
    }

    public boolean isStreamAPI() {
        return value.startsWith("java.util.stream.");
    }

    public boolean isVoid() {
        return value.equals("void");
    }

    public boolean isJavaLanguageType() {
        // TODO これだとパッケージ名 javarista などjava&javax以外も含まれてしまうような。。。
        return isPrimitive() || isVoid() || value.startsWith("java");
    }

    @Override
    public int compareTo(TypeId others) {
        return value.compareTo(others.value);
    }

    public TypeId unarray() {
        return valueOf(value.replace("[L", "").replace(";", "").replace("[]", ""));
    }

    public boolean isArray() {
        return value.endsWith("[]");
    }

    public String packageAbbreviationText() {
        return packageId().abbreviationText() + "." + asSimpleText();
    }

    public TypeId convertArray() {
        return valueOf(value + "[]");
    }
}
