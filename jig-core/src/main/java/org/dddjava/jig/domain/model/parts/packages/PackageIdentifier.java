package org.dddjava.jig.domain.model.parts.packages;

import java.util.*;

/**
 * パッケージ識別子
 */
public class PackageIdentifier {

    String value;

    public PackageIdentifier(String value) {
        this.value = value;
    }

    public PackageIdentifier applyDepth(PackageDepth packageDepth) {
        String[] split = value.split("\\.");
        if (split.length < packageDepth.value()) return this;

        StringJoiner sj = new StringJoiner(".");
        for (int i = 0; i < packageDepth.value(); i++) {
            sj.add(split[i]);
        }
        return new PackageIdentifier(sj.toString());
    }

    public PackageDepth depth() {
        return new PackageDepth(value.split("\\.").length);
    }

    public static PackageIdentifier defaultPackage() {
        return new PackageIdentifier("(default)");
    }

    public boolean contains(PackageIdentifier packageIdentifier) {
        return this.equals(packageIdentifier) || packageIdentifier.value.startsWith(this.value + ".");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageIdentifier that = (PackageIdentifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String asText() {
        return value;
    }

    public PackageIdentifier parent() {
        String[] split = value.split("\\.");

        if (split.length == 1) {
            return defaultPackage();
        }

        StringJoiner sj = new StringJoiner(".");
        for (int i = 0; i < split.length - 1; i++) {
            sj.add(split[i]);
        }
        return new PackageIdentifier(sj.toString());
    }

    public List<PackageIdentifier> genealogical() {
        if (!hasName()) {
            return Collections.emptyList();
        }

        // 最上位パッケージから全てのリスト
        String[] split = value.split("\\.");
        ArrayList<PackageIdentifier> list = new ArrayList<>();
        StringJoiner currentPackageName = new StringJoiner(".");
        for (String packageParts : split) {
            currentPackageName.add(packageParts);
            list.add(new PackageIdentifier(currentPackageName.toString()));
        }
        return list;
    }

    public boolean hasName() {
        return !value.equals("(default)");
    }

    public String simpleName() {
        if (value.lastIndexOf(".") == -1) return value;
        return value.substring(value.lastIndexOf(".") + 1);
    }

    @Override
    public String toString() {
        return value;
    }
}
