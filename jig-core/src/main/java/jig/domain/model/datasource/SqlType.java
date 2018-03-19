package jig.domain.model.datasource;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum SqlType {
    INSERT("insert +into +([^\\s(]+).+"),
    SELECT("select.+ from +([^\\s(]+)\\b.*", "select +(nextval\\('.+'\\)).*"),
    UPDATE("update +([^\\s(]+) .+"),
    DELETE("delete +from +([^\\s(]+)\\b.*");

    private static final Logger LOGGER = Logger.getLogger(SqlType.class.getName());
    private final List<Pattern> patterns;

    SqlType(String... patterns) {
        this.patterns = Stream.of(patterns)
                .map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
                .collect(toList());
    }

    public String extractTable(String sql) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(sql.replaceAll("\n", " "));
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        LOGGER.warning("テーブル名が解析できませんでした。 [" + sql + "]");
        return unexpectedTable();
    }

    public String unexpectedTable() {
        return "（解析失敗）";
    }
}
