package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;

/**
 * SQL読み取り機
 */
public interface SqlReader {

    MyBatisStatements readFrom(SqlSources sources);
}
