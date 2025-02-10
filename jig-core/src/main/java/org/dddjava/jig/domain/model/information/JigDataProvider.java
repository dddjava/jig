package org.dddjava.jig.domain.model.information;

import org.dddjava.jig.application.JigTypesRepository;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Glossary;

public interface JigDataProvider extends JigTypesRepository {

    MyBatisStatements fetchMybatisStatements();

    EnumModels fetchEnumModels();

    Glossary fetchGlossary();
}
