package org.dddjava.jig.domain.model.data;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;

import java.util.List;

public interface JigDataProvider {

    static JigDataProvider none() {
        return new JigDataProvider() {
            @Override
            public MyBatisStatements fetchMybatisStatements() {
                return MyBatisStatements.empty();
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
