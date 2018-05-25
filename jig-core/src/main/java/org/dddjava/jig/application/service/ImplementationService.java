package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationSources;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;
import org.dddjava.jig.domain.model.implementation.datasource.SqlReader;
import org.dddjava.jig.domain.model.implementation.datasource.SqlSources;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.sourcecode.PackageNameSources;
import org.dddjava.jig.domain.model.implementation.sourcecode.TypeNameSources;
import org.springframework.stereotype.Service;

/**
 * 取り込みサービス
 */
@Service
public class ImplementationService {

    final GlossaryService glossaryService;

    final ImplementationFactory implementationFactory;
    final SqlReader sqlReader;

    public ImplementationService(ImplementationFactory implementationFactory, GlossaryService glossaryService, SqlReader sqlReader) {
        this.implementationFactory = implementationFactory;
        this.glossaryService = glossaryService;
        this.sqlReader = sqlReader;
    }

    /**
     * プロジェクト情報を読み取る
     */
    public ProjectData readProjectData(ImplementationSources implementationSources, SqlSources sqlSources, TypeNameSources typeNameSources, PackageNameSources packageNameSources) {
        Implementations implementations = readImplementation(implementationSources);
        Sqls sqls = readSql(sqlSources);

        ProjectData projectData = ProjectData.from(implementations, sqls);

        glossaryService.importJapanese(typeNameSources);
        glossaryService.importJapanese(packageNameSources);

        return projectData;
    }

    /**
     * ソースから実装を読み取る
     */
    public Implementations readImplementation(ImplementationSources implementationSources) {
        if (implementationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        return implementationFactory.readFrom(implementationSources);
    }

    /**
     * ソースからSQLを読み取る
     */
    public Sqls readSql(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
