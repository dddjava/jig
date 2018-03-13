package jig.domain.model.datasource;

public interface SqlRepository {

    Sql get(SqlIdentifier sqlIdentifier);

    void register(Sql sql);
}
