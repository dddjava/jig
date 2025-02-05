package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.sources.*;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceReader;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceReader;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;
import org.dddjava.jig.domain.model.sources.javasources.comment.PackageComment;
import org.dddjava.jig.domain.model.sources.mybatis.MyBatisStatementsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 取り込みサービス
 */
@Service
public class JigSourceReader {
    private static final Logger logger = LoggerFactory.getLogger(JigSourceReader.class);

    private final SourceCollector sourceCollector;
    private final GlossaryRepository glossaryRepository;

    private final JavaSourceReader javaSourceReader;
    private final ClassSourceReader classSourceReader;

    private final MyBatisStatementsReader myBatisStatementsReader;

    private final JigEventRepository jigEventRepository;

    public JigSourceReader(GlossaryRepository glossaryRepository, ClassSourceReader classSourceReader, JavaSourceReader javaSourceReader, MyBatisStatementsReader myBatisStatementsReader, SourceCollector sourceCollector, JigEventRepository jigEventRepository) {
        this.glossaryRepository = glossaryRepository;
        this.classSourceReader = classSourceReader;
        this.javaSourceReader = javaSourceReader;
        this.myBatisStatementsReader = myBatisStatementsReader;
        this.sourceCollector = sourceCollector;
        this.jigEventRepository = jigEventRepository;
    }

    public Optional<JigDataProvider> readPathSource(SourceBasePaths sourceBasePaths) {
        Sources sources = sourceCollector.collectSources(sourceBasePaths);
        if (sources.emptyClassSources()) jigEventRepository.recordEvent(ReadStatus.バイナリソースなし);
        if (sources.emptyJavaSources()) jigEventRepository.recordEvent(ReadStatus.テキストソースなし);

        // errorが1つでもあったら読み取り失敗としてSourceを返さない
        if (jigEventRepository.hasError()) {
            return Optional.empty();
        }

        var jigDataProvider = generateJigDataProvider(sources);
        return Optional.of(jigDataProvider);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public DefaultJigDataProvider generateJigDataProvider(Sources sources) {
        JavaSources javaSources = sources.javaSources();

        javaSources.packageInfoPaths().forEach(path -> {
            var term = javaSourceReader.parsePackageInfoJavaFile(path);
            term.ifPresent(glossaryRepository::register);
        });

        JavaSourceModel javaSourceModel = javaSourceReader.javaSourceModel(javaSources);
        for (ClassComment classComment : javaSourceModel.classCommentList()) {
            glossaryRepository.register(classComment);
        }
        for (PackageComment packageComment : javaSourceModel.packageComments()) {
            glossaryRepository.register(packageComment);
        }

        ClassSources classSources = sources.classSources();
        ClassSourceModel classSourceModel = classSourceReader.classSourceModel(classSources);

        // クラス名の解決や対象の選別にClassSourceModelを使用するようにしたいので、この位置。
        // 現状（すくなくとも2025.2.1時点まで）はClassSourceを作る際にASMを使用している。その時点でのASM使用をやめたい。
        MyBatisStatements myBatisStatements = readSqlSource(sources);
        if (myBatisStatements.status().not正常())
            jigEventRepository.recordEvent(ReadStatus.fromSqlReadStatus(myBatisStatements.status()));

        return DefaultJigDataProvider.from(classSourceModel, javaSourceModel, myBatisStatements, glossaryRepository);
    }

    /**
     * ソースからSQLを読み取る
     */
    public MyBatisStatements readSqlSource(Sources sources) {
        return myBatisStatementsReader.readFrom(sources);
    }
}
