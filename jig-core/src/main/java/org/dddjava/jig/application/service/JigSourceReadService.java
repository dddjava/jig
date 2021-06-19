package org.dddjava.jig.application.service;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.parts.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.SourceReader;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.file.text.AliasSource;
import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSources;
import org.dddjava.jig.domain.model.sources.file.text.sqlcode.SqlSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.sources.jigreader.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 取り込みサービス
 */
@Service
public class JigSourceReadService {

    final JigSourceRepository jigSourceRepository;
    final FactReader binarySourceReader;
    final SourceReader sourceReader;
    final SqlReader sqlReader;
    final TextSourceReader textSourceReader;

    public JigSourceReadService(JigSourceRepository jigSourceRepository, FactReader binarySourceReader, TextSourceReader textSourceReader, SqlReader sqlReader, SourceReader sourceReader) {
        this.jigSourceRepository = jigSourceRepository;
        this.binarySourceReader = binarySourceReader;
        this.textSourceReader = textSourceReader;
        this.sqlReader = sqlReader;
        this.sourceReader = sourceReader;
    }

    /**
     * パスからソースを読み取る
     */
    public ReadStatuses readSourceFromPaths(SourcePaths sourcePaths) {
        Sources source = sourceReader.readSources(sourcePaths);

        readProjectData(source);
        Sqls sqls = readSqlSource(source.sqlSources());

        List<ReadStatus> list = new ArrayList<>();

        if (source.nothingBinarySource()) {
            list.add(ReadStatus.バイナリソースなし);
        }

        if (source.nothingTextSource()) {
            list.add(ReadStatus.テキストソースなし);
        }

        // binarySourceがあってtypeByteCodesがない（ASMの解析で失敗する）のは現状実行時エラーになるのでここでは考慮しない

        if (sqls.status().not正常()) {
            list.add(ReadStatus.fromSqlReadStatus(sqls.status()));
        }

        return new ReadStatuses(list);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public TypeFacts readProjectData(Sources sources) {
        TypeFacts typeFacts = readBinarySources(sources.classSources());
        readTextSources(sources.aliasSource());
        return typeFacts;
    }

    /**
     * ソースからバイトコードを読み取る
     */
    TypeFacts readBinarySources(ClassSources classSources) {
        TypeFacts typeFacts = binarySourceReader.readTypeFacts(classSources);
        jigSourceRepository.registerTypeFact(typeFacts);
        return typeFacts;
    }

    /**
     * ソースからSQLを読み取る
     */
    public Sqls readSqlSource(SqlSources sqlSources) {
        Sqls sqls = sqlReader.readFrom(sqlSources);
        jigSourceRepository.registerSqls(sqls);
        return sqls;
    }

    /**
     * Javadocからパッケージ別名を取り込む
     */
    void readPackageInfoSources(PackageInfoSources packageInfoSources) {
        PackageComments packageComments = textSourceReader.readPackages(packageInfoSources);
        for (PackageComment packageComment : packageComments.list()) {
            jigSourceRepository.registerPackageComment(packageComment);
        }
    }

    /**
     * Javadocから別名を取り込む
     */
    void readJavaSources(JavaSources javaSources) {
        ClassAndMethodComments classAndMethodComments = textSourceReader.readJavaSources(javaSources);
        registerComments(classAndMethodComments);
    }

    /**
     * KtDocから別名を取り込む
     */
    void readKotlinSources(KotlinSources kotlinSources) {
        ClassAndMethodComments classAndMethodComments = textSourceReader.readKotlinSources(kotlinSources);
        registerComments(classAndMethodComments);
    }

    /**
     * ScalaDocから別名を取り込む
     */
    void readScalaSources(ScalaSources scalaSources) {
        ClassAndMethodComments classAndMethodComments = textSourceReader.readScalaSources(scalaSources);
        registerComments(classAndMethodComments);
    }

    /**
     * 型別名を取り込む
     */
    private void registerComments(ClassAndMethodComments classAndMethodComments) {
        for (ClassComment classComment : classAndMethodComments.list()) {
            jigSourceRepository.registerClassComment(classComment);
        }

        for (MethodComment methodComment : classAndMethodComments.methodList()) {
            jigSourceRepository.registerMethodComment(methodComment);
        }
    }

    /**
     * 別名を取り込む
     */
    void readTextSources(AliasSource aliasSource) {
        readJavaSources(aliasSource.javaSources());
        readKotlinSources(aliasSource.kotlinSources());
        readScalaSources(aliasSource.scalaSources());
        readPackageInfoSources(aliasSource.packageInfoSources());
    }
}
