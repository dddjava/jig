package org.dddjava.jig.domain.model.data.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * SQLの種類
 */
public enum PersistenceOperationType {
    INSERT("insert\\s+into\\s+([^\\s(]+)"),
    SELECT("\\bfrom\\s+([^\\s,(]+)",
            "select\\s+(nextval\\('.+'\\))"),
    UPDATE("\\bupdate\\s+([^\\s,(]+)"),
    DELETE("delete\\s+from\\s+([^\\s(]+)\\b");

    private static final Logger logger = LoggerFactory.getLogger(PersistenceOperationType.class);
    private static final Pattern JOIN_PATTERN =
            Pattern.compile("\\bjoin\\s+([^\\s,(]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    private final List<Pattern> patterns;

    PersistenceOperationType(String... patterns) {
        this.patterns = Stream.of(patterns)
                .map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS))
                .toList();
    }

    /**
     * SQLから使用しているテーブルを抽出する
     *
     * JOINを含む複数テーブルの参照に対応。WITHなどは未対応。
     */
    public PersistenceTargets extractTable(Query query, PersistenceAccessorOperationId persistenceAccessorOperationId) {
        if (query.supported()) {
            String sql = query.normalizedQuery().replaceAll("\n", " ");
            List<PersistenceTarget> targets = new ArrayList<>();

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(sql);
                while (matcher.find()) {
                    targets.add(new PersistenceTarget(matcher.group(1)));
                }
            }

            Matcher joinMatcher = JOIN_PATTERN.matcher(sql);
            while (joinMatcher.find()) {
                targets.add(new PersistenceTarget(joinMatcher.group(1)));
            }

            if (!targets.isEmpty()) {
                return new PersistenceTargets(targets);
            }

            logger.warn("{} を {} としてテーブル名が解析できませんでした。テーブル名は「解析失敗」と表示されます。JIGが認識しているSQL文=[{}]",
                    persistenceAccessorOperationId.logText(), this, sql);
        }

        return new PersistenceTargets(unexpectedTable());
    }

    public PersistenceTarget unexpectedTable() {
        return new PersistenceTarget("（解析失敗）");
    }

    public static Optional<PersistenceOperationType> inferSqlTypeFromQuery(Query query) {
        String normalizedQuery = query.normalizedQuery().toLowerCase(Locale.ROOT);
        if (normalizedQuery.startsWith("insert")) return Optional.of(INSERT);
        if (normalizedQuery.startsWith("select")) return Optional.of(SELECT);
        if (normalizedQuery.startsWith("update")) return Optional.of(UPDATE);
        if (normalizedQuery.startsWith("delete")) return Optional.of(DELETE);
        return Optional.empty();
    }
}
