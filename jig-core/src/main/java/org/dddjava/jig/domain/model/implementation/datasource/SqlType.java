package org.dddjava.jig.domain.model.implementation.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum SqlType {
    INSERT("insert\\s+into\\s+([^\\s(]+).+"),
    SELECT("select.+\\sfrom\\s+([^\\s(]+)\\b.*",
            "select\\s+(nextval\\('.+'\\)).*"),
    UPDATE("update\\s+([^\\s(]+)\\s.+"),
    DELETE("delete\\s+from\\s+([^\\s(]+)\\b.*");

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlType.class);
    private final List<Pattern> patterns;

    SqlType(String... patterns) {
        this.patterns = Stream.of(patterns)
                .map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
                .collect(toList());
    }

    public Table extractTable(String sql) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(sql.replaceAll("\n", " "));
            if (matcher.matches()) {
                return new Table(matcher.group(1));
            }
        }

        LOGGER.warn("{} としてテーブル名が解析できませんでした。 [{}]", this, sql);
        return unexpectedTable();
    }

    public Table unexpectedTable() {
        return new Table("（解析失敗）");
    }
}
