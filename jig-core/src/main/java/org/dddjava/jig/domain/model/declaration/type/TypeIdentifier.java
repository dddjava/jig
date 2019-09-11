package org.dddjava.jig.domain.model.declaration.type;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 型の識別子
 */
public class TypeIdentifier {

    String value;

    public TypeIdentifier(Class<?> clz) {
        this(clz.getName());
    }

    public TypeIdentifier(String value) {
        this.value = value.replace('/', '.');
    }

    public String fullQualifiedName() {
        return format(value -> value);
    }

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
        switch (value) {
            case "int":
            case "long":
            case "boolean":
            case "double":
            case "float":
            case "byte":
            case "char":
                return true;
        }
        return false;
    }

    public boolean isStream() {
        // java.util.streamパッケージかで見たほうがいいかも？
        return equals(new TypeIdentifier(Stream.class));
    }

    public boolean isVoid() {
        return value.equals("void");
    }
}
