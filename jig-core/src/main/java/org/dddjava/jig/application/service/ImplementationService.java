package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.ByteCodeFactory;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.datasource.SqlReader;
import org.dddjava.jig.domain.model.implementation.analyzed.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.source.binary.ClassSources;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSourceFactory;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSourceLocations;
import org.dddjava.jig.domain.model.implementation.source.code.sqlcode.SqlSources;
import org.springframework.stereotype.Service;

/**
 * 取り込みサービス
 */
@Service
public class ImplementationService {

    AliasService aliasService;

    ByteCodeFactory byteCodeFactory;
    SqlReader sqlReader;

    RawSourceFactory rawSourceFactory;

    public ImplementationService(ByteCodeFactory byteCodeFactory, AliasService aliasService, SqlReader sqlReader, RawSourceFactory rawSourceFactory) {
        this.byteCodeFactory = byteCodeFactory;
        this.aliasService = aliasService;
        this.sqlReader = sqlReader;
        this.rawSourceFactory = rawSourceFactory;
    }

    public AnalyzedImplementation implementations(RawSourceLocations rawSourceLocations) {
        RawSource source = rawSourceFactory.createSource(rawSourceLocations);

        TypeByteCodes typeByteCodes = readProjectData(source);
        Sqls sqls = readSql(source.sqlSources());

        return new AnalyzedImplementation(source, typeByteCodes, sqls);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public TypeByteCodes readProjectData(RawSource rawSource) {
        TypeByteCodes typeByteCodes = readByteCode(rawSource.classSources());

        aliasService.loadAliases(rawSource.textSource());

        return typeByteCodes;
    }

    /**
     * ソースからバイトコードを読み取る
     */
    public TypeByteCodes readByteCode(ClassSources classSources) {
        return byteCodeFactory.readFrom(classSources);
    }

    /**
     * ソースからSQLを読み取る
     */
    public Sqls readSql(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
