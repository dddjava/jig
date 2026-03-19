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
    INSERT("insert\\s+into\\s+([^\\s()]+)"),
    SELECT("(?<!:)\\bfrom\\s+([^\\s,()]+)",
            "select\\s+(nextval\\('.+'\\))"),
    UPDATE("\\bupdate\\s+([^\\s,()]+)"),
    DELETE("delete\\s+from\\s+([^\\s()]+)\\b");

    private static final Logger logger = LoggerFactory.getLogger(PersistenceOperationType.class);
    private static final Pattern JOIN_PATTERN =
            Pattern.compile("\\bjoin\\s+([^\\s,()]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    // 関数呼び出し（word(...) 形式）にマッチ — サブクエリ除去の前処理用
    // (?!\s*select\b) で exists(SELECT ...) などサブクエリラッパーは除外する
    private static final Pattern FUNCTION_CALL_PATTERN =
            Pattern.compile("\\w+\\((?!\\s*select\\b)[^()]*\\)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    // SELECT サブクエリにマッチ（空の () はネスト除去後の置換値として許容）
    private static final Pattern INNER_SUBQUERY_PATTERN =
            Pattern.compile("\\(\\s*select(?:[^()]+|\\(\\))*\\)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    // SELECT サブクエリ内の FROM テーブルを抽出（空の () はネスト除去後の置換値として許容）
    private static final Pattern SUBQUERY_FROM_PATTERN =
            Pattern.compile("\\(\\s*select(?:[^()]+|\\(\\))*\\bfrom\\s+([^\\s,()]+)(?:[^()]+|\\(\\))*\\)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    private final List<Pattern> patterns;

    PersistenceOperationType(String... patterns) {
        this.patterns = Stream.of(patterns)
                .map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS))
                .toList();
    }

    /**
     * SQLから使用しているテーブルを抽出する
     *
     * JOINを含む複数テーブルの参照に対応。サブクエリ内FROMも対応。WITHなどは未対応。
     */
    public PersistenceTargetOperationTypes extractTable(Query query, PersistenceAccessorOperationId persistenceAccessorOperationId) {
        if (query.supported()) {
            String sql = query.normalizedQuery().replaceAll("\n", " ");
            List<PersistenceTargetOperationType> targets = new ArrayList<>();

            // サブクエリを除去したSQLに対してメインパターンとJOINを適用
            String sqlWithoutSubqueries = removeSubqueries(sql);
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(sqlWithoutSubqueries);
                while (matcher.find()) {
                    targets.add(PersistenceTargetOperationType.from(PersistenceTarget.fromSql(matcher.group(1)), this));
                }
            }

            Matcher joinMatcher = JOIN_PATTERN.matcher(sqlWithoutSubqueries);
            while (joinMatcher.find()) {
                targets.add(PersistenceTargetOperationType.from(PersistenceTarget.fromSql(joinMatcher.group(1)), SELECT));
            }

            // サブクエリ内FROMをSELECTとして追加
            for (String table : extractSubqueryFromTargets(sql)) {
                targets.add(PersistenceTargetOperationType.from(PersistenceTarget.fromSql(table), SELECT));
            }

            if (!targets.isEmpty()) {
                return new PersistenceTargetOperationTypes(targets);
            }

            logger.warn("{} を {} としてテーブル名が解析できませんでした。テーブル名は「解析失敗」と表示されます。JIGが認識しているSQL文=[{}]",
                    persistenceAccessorOperationId.logText(), this, sql);
        }

        return new PersistenceTargetOperationTypes(new PersistenceTargetOperationType(unexpectedTable(), this));
    }

    /**
     * (SELECT ...) を () に置換して繰り返すことでネストを除去
     */
    private static String removeSubqueries(String sql) {
        String result = sql;
        String prev;
        do {
            prev = result;
            result = INNER_SUBQUERY_PATTERN.matcher(result).replaceAll("()");
        } while (!result.equals(prev));
        return result;
    }

    /**
     * word(...) 形式の関数呼び出しを繰り返し除去し、括弧をサブクエリのみにする
     */
    private static String removeFunctionCalls(String sql) {
        String result = sql;
        String prev;
        do {
            prev = result;
            result = FUNCTION_CALL_PATTERN.matcher(result).replaceAll("()");
        } while (!result.equals(prev));
        return result;
    }

    /**
     * サブクエリ内のFROMテーブル名を収集（繰り返しでネスト対応）
     */
    private static List<String> extractSubqueryFromTargets(String sql) {
        // 関数呼び出しを先に除去してからサブクエリを抽出する
        String processed = removeFunctionCalls(sql);
        List<String> tables = new ArrayList<>();
        String current = processed;
        String prev;
        do {
            prev = current;
            Matcher m = SUBQUERY_FROM_PATTERN.matcher(current);
            while (m.find()) {
                tables.add(m.group(1));
            }
            current = INNER_SUBQUERY_PATTERN.matcher(current).replaceAll("()");
        } while (!current.equals(prev));
        return tables;
    }

    public PersistenceTarget unexpectedTable() {
        return new PersistenceTarget("（解析失敗）");
    }

    // FIXME: 半端な判定。これ自体なくせるはず。
    public static Optional<PersistenceOperationType> inferOperationTypeFromQuery(Query query) {
        String normalizedQuery = query.normalizedQuery().toLowerCase(Locale.ROOT);
        if (normalizedQuery.startsWith("insert")) return Optional.of(INSERT);
        if (normalizedQuery.startsWith("select")) return Optional.of(SELECT);
        if (normalizedQuery.startsWith("update")) return Optional.of(UPDATE);
        if (normalizedQuery.startsWith("delete")) return Optional.of(DELETE);
        return Optional.empty();
    }
}
