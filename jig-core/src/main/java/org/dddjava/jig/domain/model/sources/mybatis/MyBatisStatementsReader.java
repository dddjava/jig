package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.sources.Sources;

/**
 * SQL読み取り機
 */
public interface MyBatisStatementsReader {

    MyBatisStatements readFrom(Sources sources);
}
