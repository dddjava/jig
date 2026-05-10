package org.dddjava.jig.domain.model.knowledge.external;

import org.dddjava.jig.domain.model.data.packages.PackageId;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解析対象外（外部ライブラリ・JDK 等）のパッケージをグルーピングするルール。
 *
 * <p>初版ではユーザー設定はサポートせず、既知ライブラリの prefix→表示名マップをハードコードする。
 * 既知マップに該当しないパッケージは先頭から{@value #DEFAULT_DEPTH}階層で自動集約する。</p>
 */
public class ExternalGroupingRule {

    private static final int DEFAULT_DEPTH = 2;

    /** 既知ライブラリの prefix→表示名マップ。prefix の長い順に評価する。 */
    private static final Map<String, String> KNOWN_GROUPS = buildKnownGroups();

    private static Map<String, String> buildKnownGroups() {
        Map<String, String> map = new LinkedHashMap<>();
        // Spring 系（より具体的なものを先に）
        map.put("org.springframework.boot", "spring-boot");
        map.put("org.springframework.web", "spring-web");
        map.put("org.springframework.data", "spring-data");
        map.put("org.springframework.security", "spring-security");
        map.put("org.springframework", "spring");
        // MyBatis
        map.put("org.apache.ibatis", "mybatis");
        map.put("org.mybatis", "mybatis");
        // テスト系
        map.put("org.junit", "junit");
        map.put("org.mockito", "mockito");
        map.put("org.assertj", "assertj");
        // ロギング
        map.put("org.slf4j", "slf4j");
        map.put("ch.qos.logback", "logback");
        // Jakarta EE
        map.put("jakarta.persistence", "jakarta-persistence");
        map.put("jakarta.servlet", "jakarta-servlet");
        // Apache POI
        map.put("org.apache.poi", "apache-poi");
        // ASM, JavaParser など JIG 内部依存（参考用）
        map.put("org.objectweb.asm", "asm");
        map.put("com.github.javaparser", "javaparser");
        return map;
    }

    public static ExternalGroupingRule defaultRule() {
        return new ExternalGroupingRule();
    }

    public Group groupOf(PackageId packageId) {
        String fqn = packageId.asText();

        boolean isJdk = fqn.startsWith("java.") || fqn.equals("java")
                || fqn.startsWith("javax.") || fqn.equals("javax");
        if (isJdk) {
            return new Group("jdk", "jdk", true);
        }

        // 既知マップ：prefix の長い順にマッチ
        String matchedKey = KNOWN_GROUPS.keySet().stream()
                .filter(prefix -> fqn.equals(prefix) || fqn.startsWith(prefix + "."))
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
        if (matchedKey != null) {
            String displayName = KNOWN_GROUPS.get(matchedKey);
            return new Group(displayName, displayName, false);
        }

        // 未知のパッケージは先頭から DEFAULT_DEPTH 階層で集約
        String[] parts = fqn.split("\\.");
        int depth = Math.min(DEFAULT_DEPTH, parts.length);
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < depth; i++) {
            sb.append('.').append(parts[i]);
        }
        String groupId = sb.toString();
        return new Group(groupId, groupId, false);
    }

    /**
     * グルーピング結果。
     *
     * @param id          ノード ID（Mermaid 用の安定識別子）
     * @param displayName 表示名
     * @param isJdk       JDK（java.* / javax.*）由来か
     */
    public record Group(String id, String displayName, boolean isJdk) {
    }
}
