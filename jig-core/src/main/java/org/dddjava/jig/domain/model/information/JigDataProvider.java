package org.dddjava.jig.domain.model.information;

import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.information.type.JigTypes;

public interface JigDataProvider {

    MyBatisStatements fetchMybatisStatements();

    EnumModels fetchEnumModels();

    JigTypes fetchJigTypes();

    Glossary fetchGlossary();
}
