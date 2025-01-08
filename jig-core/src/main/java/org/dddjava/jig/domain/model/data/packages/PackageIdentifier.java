package org.dddjava.jig.domain.model.data.packages;

import org.dddjava.jig.JigContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * パッケージ識別子
 */
public class PackageIdentifier {

    private final String value;

    private PackageIdentifier(String value) {
        this.value = value;
    }

    private static final Map<String, PackageIdentifier> cache = new ConcurrentHashMap<>();

    public static PackageIdentifier valueOf(String value) {
        if (cache.containsKey(value)) return cache.get(value);

        var instance = new PackageIdentifier(value);
        cache.put(value, instance);
        return instance;
    }

    public PackageIdentifier applyDepth(PackageDepth packageDepth) {
        String[] split = value.split("\\.");
        if (split.length < packageDepth.value()) return this;

        StringJoiner sj = new StringJoiner(".");
        for (int i = 0; i < packageDepth.value(); i++) {
            sj.add(split[i]);
        }
        return valueOf(sj.toString());
    }

    public PackageDepth depth() {
        return new PackageDepth(value.split("\\.").length);
    }

    public static PackageIdentifier defaultPackage() {
        return valueOf("(default)");
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
        return valueOf(sj.toString());
    }

    /**
     * @return hoge.fuga.piyo => [hoge, hoge.fuga, hoge.fuga.piyo]
     */
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
            list.add(valueOf(currentPackageName.toString()));
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

    public String htmlIdText() {
        // 英数字以外を_に置換する
        return value.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * 省略表記フィールド
     * FQNは長くなりすぎるため省略表記が必要な場合がある。
     * 全てのパッケージで必要なものでもなく、それなりの計算量となるため、生成時にキャッシュする形とするためフィールドで持つ。
     * このインスタンス自体に持たせない方がいい気はする。
     */
    private String abbreviationText = null;

    /**
     * 省略表記
     */
    public String abbreviationText() {
        if (abbreviationText != null) {
            return abbreviationText;
        }
        if (JigContext.packageAbbreviationMode.value().equalsIgnoreCase("numeric")) {
            // internationalization -> i18n
            if (value.length() <= 2) {
                return abbreviationText = value;
            }
            char firstChar = value.charAt(0);
            char lastChar = value.charAt(value.length() - 1);
            int middleCount = value.length() - 2;

            return abbreviationText = "%c%d%c".formatted(firstChar, middleCount, lastChar);
        } else {
            // hoge.fuga.piyo -> h.f.p
            String[] parts = value.split("\\.");
            return abbreviationText = Arrays.stream(parts)
                    .map(value -> String.valueOf(value.charAt(0)))
                    .collect(Collectors.joining("."));
        }
    }
}
