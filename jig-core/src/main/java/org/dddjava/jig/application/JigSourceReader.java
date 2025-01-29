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
import org.dddjava.jig.domain.model.sources.mybatis.SqlReader;
import org.dddjava.jig.domain.model.sources.mybatis.SqlSources;
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

    final CommentRepository commentRepository;

    final SourceReader sourceReader;

    final ClassSourceReader classSourceReader;
    final JavaSourceReader javaSourceReader;
    final SqlReader sqlReader;
    private final JigReporter jigReporter;

    public JigSourceReader(CommentRepository commentRepository, ClassSourceReader classSourceReader, JavaSourceReader javaSourceReader, SqlReader sqlReader, SourceReader sourceReader, JigReporter jigReporter) {
        this.commentRepository = commentRepository;
        this.classSourceReader = classSourceReader;
        this.javaSourceReader = javaSourceReader;
        this.sqlReader = sqlReader;
        this.sourceReader = sourceReader;
        this.jigReporter = jigReporter;
    }

    public Optional<JigDataProvider> readPathSource(SourceBasePaths sourceBasePaths) {
        List<ReadStatus> readEvents = new ArrayList<>();

        // ソースのチェック
        Sources source = sourceReader.readSources(sourceBasePaths);
        if (source.emptyClassSources()) readEvents.add(ReadStatus.バイナリソースなし);
        if (source.emptyJavaSources()) readEvents.add(ReadStatus.テキストソースなし);

        MyBatisStatements myBatisStatements = readSqlSource(source.sqlSources());
        if (myBatisStatements.status().not正常())
            readEvents.add(ReadStatus.fromSqlReadStatus(myBatisStatements.status()));

        readEvents.forEach(readStatus -> {
            if (readStatus.isError()) {
                logger.error("{}", readStatus.localizedMessage());
            } else {
                logger.warn("{}", readStatus.localizedMessage());
            }
        });
        // errorが1つでもあったら読み取り失敗としてSourceを返さない
        if (readEvents.stream().anyMatch(event -> event.isError())) {
            return Optional.empty();
        }

        var jigSource = readProjectData(source);
        jigSource.addSqls(myBatisStatements);
        return Optional.of(jigSource);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public DefaultJigDataProvider readProjectData(Sources sources) {
        JavaSources javaSources = sources.javaSources();

        JavaSourceModel javaSourceModel = javaSourceReader.javaSourceModel(javaSources);
        for (ClassComment classComment : javaSourceModel.classCommentList()) {
            commentRepository.register(classComment);
        }
        for (PackageComment packageComment : javaSourceModel.packageComments()) {
            commentRepository.register(packageComment);
        }

        ClassSources classSources = sources.classSources();
        ClassSourceModel classSourceModel = classSourceReader.classSourceModel(classSources);

        return new DefaultJigDataProvider(classSourceModel, javaSourceModel);
    }

    /**
     * ソースからSQLを読み取る
     */
    public MyBatisStatements readSqlSource(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
