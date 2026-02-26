package org.dddjava.jig.domain.model.data;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.persistence.SqlStatements;

import java.util.List;

public interface JigDataProvider {

    static JigDataProvider none() {
        return new JigDataProvider() {
            @Override
            public SqlStatements fetchSqlStatements() {
                return SqlStatements.empty();
            }

            @Override
            public EnumModels fetchEnumModels() {
                return new EnumModels(List.of());
            }
        };
    }

    SqlStatements fetchSqlStatements();

    EnumModels fetchEnumModels();
}
