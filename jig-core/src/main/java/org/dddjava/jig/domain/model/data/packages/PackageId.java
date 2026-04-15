package org.dddjava.jig.domain.model.data.packages;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * パッケージ識別子
 */
public class PackageId implements Comparable<PackageId> {

    private final String value;

    private PackageId(String value) {
        this.value = value;
    }

    private static final Map<String, PackageId> cache = new ConcurrentHashMap<>();

    public static PackageId valueOf(String value) {
        return cache.computeIfAbsent(value, PackageId::new);
    }

    public static PackageId defaultPackage() {
        return valueOf("(default)");
    }

    public boolean isSubpackageOf(PackageId otherPackageId) {
        return this.value.startsWith(otherPackageId.value + ".");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageId that = (PackageId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String asText() {
        return value;
    }

    public Optional<PackageId> parentIfExist() {
        int lastDot = value.lastIndexOf(".");
        if (lastDot == -1) return Optional.empty();
        return Optional.of(valueOf(value.substring(0, lastDot)));
    }

    public String simpleName() {
        if (value.lastIndexOf(".") == -1) return value;
        return value.substring(value.lastIndexOf(".") + 1);
    }

    @Override
    public String toString() {
        return value;
    }

    public PackageId subpackageOf(String... packages) {
        return PackageId.valueOf(value + "." + String.join(".", packages));
    }

    @Override
    public int compareTo(PackageId o) {
        return value.compareTo(o.value);
    }
}
