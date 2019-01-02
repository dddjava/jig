package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.Implementations;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.SqlReader;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.raw.*;
import org.springframework.stereotype.Service;

/**
 * 取り込みサービス
 */
@Service
public class ImplementationService {

    GlossaryService glossaryService;

    ByteCodeFactory byteCodeFactory;
    SqlReader sqlReader;

    RawSourceFactory rawSourceFactory;

    public ImplementationService(ByteCodeFactory byteCodeFactory, GlossaryService glossaryService, SqlReader sqlReader, RawSourceFactory rawSourceFactory) {
        this.byteCodeFactory = byteCodeFactory;
        this.glossaryService = glossaryService;
        this.sqlReader = sqlReader;
        this.rawSourceFactory = rawSourceFactory;
    }

    public Implementations implementations(RawSourceLocations rawSourceLocations) {
        RawSource source = rawSourceFactory.createSource(rawSourceLocations);

        TypeByteCodes typeByteCodes = readProjectData(source);
        Sqls sqls = readSql(source.sqlSources());

        return new Implementations(source, typeByteCodes, sqls);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public TypeByteCodes readProjectData(RawSource rawSource) {
        TypeByteCodes typeByteCodes = readByteCode(rawSource.binarySource().classSources());

        glossaryService.importJapanese(rawSource.textSource().javaSources());
        glossaryService.importJapanese(rawSource.textSource().packageInfoSources());

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
