package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.sources.*;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceReader;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceReader;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.dddjava.jig.domain.model.sources.mybatis.MyBatisStatementsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 取り込みサービス
 */
@Service
public class JigSourceReader {
    private static final Logger logger = LoggerFactory.getLogger(JigSourceReader.class);

    private final SourceReader sourceReader;
    private final GlossaryRepository glossaryRepository;

    private final JavaSourceReader javaSourceReader;
    private final ClassSourceReader classSourceReader;

    private final MyBatisStatementsReader myBatisStatementsReader;

    private final JigEventRepository jigEventRepository;

    public JigSourceReader(GlossaryRepository glossaryRepository, ClassSourceReader classSourceReader, JavaSourceReader javaSourceReader, MyBatisStatementsReader myBatisStatementsReader, SourceReader sourceReader, JigEventRepository jigEventRepository) {
        this.glossaryRepository = glossaryRepository;
        this.classSourceReader = classSourceReader;
        this.javaSourceReader = javaSourceReader;
        this.myBatisStatementsReader = myBatisStatementsReader;
        this.sourceReader = sourceReader;
        this.jigEventRepository = jigEventRepository;
    }

    public Optional<JigDataProvider> readPathSource(SourceBasePaths sourceBasePaths) {
        List<ReadStatus> readEvents = new ArrayList<>();

        // ソースのチェック
        Sources source = sourceReader.readSources(sourceBasePaths);
        if (source.emptyClassSources()) readEvents.add(ReadStatus.バイナリソースなし);
        if (source.emptyJavaSources()) readEvents.add(ReadStatus.テキストソースなし);


        readEvents.forEach(readStatus -> {
            jigEventRepository.registerReadStatus(readStatus);
        });
        // errorが1つでもあったら読み取り失敗としてSourceを返さない
        if (readEvents.stream().anyMatch(event -> event.isError())) {
            return Optional.empty();
        }

        var jigDataProvider = readProjectData(source);

        // クラス名の解決や対象の選別にjigSource(jigType)を使用するため readProjectData の後で行う
        MyBatisStatements myBatisStatements = readSqlSource(source);
        if (myBatisStatements.status().not正常())
            jigEventRepository.registerReadStatus(ReadStatus.fromSqlReadStatus(myBatisStatements.status()));
        jigDataProvider.addSqls(myBatisStatements);

        return Optional.of(jigDataProvider);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public DefaultJigDataProvider readProjectData(Sources sources) {
        JavaSources javaSources = sources.javaSources();

        JavaSourceModel javaSourceModel = javaSourceReader.javaSourceModel(javaSources);
        for (ClassComment classComment : javaSourceModel.classCommentList()) {
            glossaryRepository.register(classComment);
        }
        for (PackageComment packageComment : javaSourceModel.packageComments()) {
            glossaryRepository.register(packageComment);
        }

        ClassSources classSources = sources.classSources();
        ClassSourceModel classSourceModel = classSourceReader.classSourceModel(classSources);

        return new DefaultJigDataProvider(classSourceModel, javaSourceModel);
    }

    /**
     * ソースからSQLを読み取る
     */
    public MyBatisStatements readSqlSource(Sources sources) {
        return myBatisStatementsReader.readFrom(sources);
    }
}
