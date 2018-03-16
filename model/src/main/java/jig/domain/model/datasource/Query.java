package jig.domain.model.datasource;

public class Query {

    String text;

    public Query(String text) {
        this.text = text;
    }

    public String extractTable(SqlType sqlType) {
        return sqlType.extractTable(text);
    }

    @Override
    public String toString() {
        return "Query{" +
                "text='" + text + '\'' +
                '}';
    }
}
