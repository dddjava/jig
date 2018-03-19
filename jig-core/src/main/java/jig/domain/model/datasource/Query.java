package jig.domain.model.datasource;

public class Query {

    String text;

    public Query(String text) {
        this.text = text;
    }

    public String extractTable(SqlType sqlType) {
        if (text == null) {
            return sqlType.unexpectedTable();
        }
        return sqlType.extractTable(text);
    }

    public static Query unsupported() {
        return new Query(null);
    }
}
