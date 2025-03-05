package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;

import java.util.Collection;

/**
 * SQL読み取り機
 */
public interface MyBatisStatementsReader {

    MyBatisStatements readFrom(Collection<JigTypeHeader> sources, SourceBasePaths sourceBasePaths);
}
