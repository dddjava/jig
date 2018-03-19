package jig.domain.model.datasource;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SqlType {
    INSERT("insert +into +([^\\s(]+).+"),
    SELECT("select.+ from +([^\\s(]+)\\b.*"),
    UPDATE("update +([^\\s(]+) .+"),
    DELETE("delete +from +([^\\s(]+)\\b.*");

    private static final Logger LOGGER = Logger.getLogger(SqlType.class.getName());
    private final Pattern pattern;

    SqlType(String regex) {
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public String extractTable(String sql) {
        Matcher matcher = pattern.matcher(sql.replaceAll("\n", " "));
        if (matcher.matches()) {
            return matcher.group(1);
        }

        LOGGER.warning("テーブル名が解析できないSQLです。 " + sql);
        return unexpectedTable();
    }

    public String unexpectedTable() {
        return "（解析失敗）";
    }
}
