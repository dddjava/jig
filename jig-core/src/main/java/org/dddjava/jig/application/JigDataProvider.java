package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlReadStatus;

import java.util.List;

public interface JigDataProvider {

    static JigDataProvider none() {
        return new JigDataProvider() {
            @Override
            public MyBatisStatements fetchMybatisStatements() {
                return new MyBatisStatements(SqlReadStatus.SQLなし);
            }

            @Override
            public EnumModels fetchEnumModels() {
                return new EnumModels(List.of());
            }
        };
    }

    MyBatisStatements fetchMybatisStatements();

    EnumModels fetchEnumModels();
}
