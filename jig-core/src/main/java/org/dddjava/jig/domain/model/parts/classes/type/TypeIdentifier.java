package org.dddjava.jig.domain.model.parts.classes.type;

import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * *型の識別子
 */
public class TypeIdentifier implements Comparable<TypeIdentifier> {

    String value;

    public TypeIdentifier(Class<?> clz) {
        this(clz.getName());
    }

    public TypeIdentifier(String value) {
        this.value = value.replace('/', '.');
    }

    public static TypeIdentifier of(Class<?> clz) {
        return new TypeIdentifier(clz);
    }

    /**
     * @return "org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier"
     */
    public String fullQualifiedName() {
        return format(value -> value);
    }

    /**
     * パッケージなしのクラス名
     *
     * @return "TypeIdentifier"
     */
    public String asSimpleText() {
        return hasPackage() ? format(value -> value.substring(value.lastIndexOf(".") + 1)) : value;
    }

    public String format(TypeIdentifierFormatter formatter) {
        return formatter.format(value);
    }

    private boolean hasPackage() {
        return value.contains(".");
    }

    public PackageIdentifier packageIdentifier() {
        if (!hasPackage()) {
            return PackageIdentifier.defaultPackage();
        }
        return new PackageIdentifier(value.substring(0, value.lastIndexOf(".")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeIdentifier that = (TypeIdentifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "TypeIdentifier{" +
                "value='" + value + '\'' +
                '}';
    }

    public TypeIdentifier normalize() {
        // コンパイラが生成する継承クラス名を元の名前にする
        // enumで Hoge$1 などになっているものが対象
        if (value.indexOf('$') == -1) {
            return this;
        }
        return new TypeIdentifier(value.replaceFirst("\\$\\d+", ""));
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
        return equals(TypeIdentifier.of(Stream.class));
    }

    public boolean isVoid() {
        return value.equals("void");
    }

    public boolean isJavaLanguageType() {
        return isPrimitive() || isVoid() || value.startsWith("java");
    }

    public boolean isEnum() {
        return equals(TypeIdentifier.of(Enum.class));
    }

    @Override
    public int compareTo(TypeIdentifier others) {
        return value.compareTo(others.value);
    }

    public TypeIdentifier unarray() {
        return new TypeIdentifier(value.replace("[L", "").replace(";", "").replace("[]", ""));
    }

    public boolean anyEquals(String... ids) {
        for (String id : ids) {
            if (fullQualifiedName().equals(id)) return true;
        }
        return false;
    }

    public String htmlIdText() {
        // 英数字以外を_に置換する
        return value.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public boolean isArray() {
        return value.endsWith("[]");
    }

    public String packageAbbreviationText() {
        return packageIdentifier().abbreviationText() + "." + asSimpleText();
    }
}
