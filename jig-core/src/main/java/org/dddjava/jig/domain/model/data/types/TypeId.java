package org.dddjava.jig.domain.model.data.types;

import org.dddjava.jig.domain.model.data.packages.PackageId;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * *型の識別子
 */
public class TypeId implements Comparable<TypeId> {

    private final String value;

    protected TypeId(String value) {
        this.value = value.replace('/', '.');
    }

    public String value() {
        return value;
    }

    public static TypeId fromJvmBinaryName(String jvmBinaryName) {
        return new TypeId(jvmBinaryName.replace('/', '.'));
    }

    private static final Map<String, TypeId> cache = new ConcurrentHashMap<>();

    public static TypeId from(Class<?> clz) {
        return valueOf(clz.getName());
    }

    public static TypeId valueOf(String value) {
        if (cache.containsKey(value)) return cache.get(value);
        var instance = new TypeId(value);
        cache.put(value, instance);
        return instance;
    }

    /**
     * @return "org.dddjava.jig.domain.model.data.types.TypeId"
     */
    public String fullQualifiedName() {
        return value;
    }

    /**
     * パッケージなしのクラス名
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeId that = (TypeId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "TypeId{" +
                "value='" + value + '\'' +
                '}';
    }

    public TypeId normalize() {
        // コンパイラが生成する継承クラス名を元の名前にする
        // enumで Hoge$1 などになっているものが対象
        if (value.indexOf('$') == -1) {
            return this;
        }
        return valueOf(value.replaceFirst("\\$\\d+", ""));
    }

    public boolean isBoolean() {
        Class<?>[] booleanTypes = {Boolean.class, boolean.class};
        return Arrays.stream(booleanTypes).anyMatch(clazz -> clazz.getName().equals(value));
    }

    public boolean isPrimitive() {
        return value.matches("(int|long|boolean|double|float|byte|char|short)(\\[])?");
    }

    public boolean isStream() {
        // java.util.streamパッケージかで見たほうがいいかも？
        return equals(from(Stream.class));
    }

    public boolean isVoid() {
        return value.equals("void");
    }

    public boolean isJavaLanguageType() {
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
