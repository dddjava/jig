package jig.domain.model.datasource;

import jig.domain.model.thing.Name;

public interface SqlRepository {

    Sql get(Name name);

    void register(Sql sql);
}
