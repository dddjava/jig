package org.dddjava.jig.domain.model.data.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * SQLの種類
 */
public enum SqlType {
    INSERT("insert\\s+into\\s+([^\\s(]+).+"),
    SELECT("select.+\\sfrom\\s+([^\\s(]+)\\b.*",
            "select\\s+(nextval\\('.+'\\)).*"),
    UPDATE("update\\s+([^\\s(]+)\\s.+"),
    DELETE("delete\\s+from\\s+([^\\s(]+)\\b.*");

    private static final Logger logger = LoggerFactory.getLogger(SqlType.class);
    private final List<Pattern> patterns;

    SqlType(String... patterns) {
        this.patterns = Stream.of(patterns)
                .map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS))
                .toList();
    }

    /**
     * SQLから使用しているテーブルを抽出する
     *
     * 現在は1テーブルのみ対応
     * 複問い合わせやWITHなどは未対応
     */
    public PersistenceTargets extractTable(Query query, PersistenceOperationId persistenceOperationId) {
        if (query.supported()) {
            String sql = query.normalizedQuery();
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(sql.replaceAll("\n", " "));
                if (matcher.matches()) {
                    return new PersistenceTargets(new PersistenceTarget(matcher.group(1)));
                }
            }

            logger.warn("{} を {} としてテーブル名が解析できませんでした。テーブル名は「解析失敗」と表示されます。JIGが認識しているSQL文=[{}]",
                    persistenceOperationId.logText(), this, sql);
        }

        return new PersistenceTargets(unexpectedTable());
    }

    public PersistenceTarget unexpectedTable() {
        return new PersistenceTarget("（解析失敗）");
    }

    public static Optional<SqlType> inferSqlTypeFromQuery(Query query) {
        String normalizedQuery = query.normalizedQuery().toLowerCase(Locale.ROOT);
        if (normalizedQuery.startsWith("insert")) return Optional.of(INSERT);
        if (normalizedQuery.startsWith("select")) return Optional.of(SELECT);
        if (normalizedQuery.startsWith("update")) return Optional.of(UPDATE);
        if (normalizedQuery.startsWith("delete")) return Optional.of(DELETE);
        return Optional.empty();
    }
}
