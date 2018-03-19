package jig.domain.model.datasource;

import jig.domain.model.thing.Name;

public interface SqlRepository {

    Sql get(SqlIdentifier identifier);

    void register(Sql sql);

    Sql find(Name name);
}
