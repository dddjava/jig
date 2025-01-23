package org.dddjava.jig.domain.model.data;

import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

public interface JigDataProvider {

    MyBatisStatements fetchMybatisStatements();

    EnumModels fetchEnumModels();

    JigTypes fetchJigTypes();

    Terms fetchTerms();
}
