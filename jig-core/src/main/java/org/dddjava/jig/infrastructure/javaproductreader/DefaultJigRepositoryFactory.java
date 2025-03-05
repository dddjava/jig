package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigDataProvider;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.sources.*;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.dddjava.jig.domain.model.sources.mybatis.MyBatisStatementsReader;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.filesystem.ClassOrJavaSourceCollector;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisMyBatisStatementsReader;

import java.util.Collection;

public class DefaultJigRepositoryFactory {

    private final SourceCollector sourceCollector;
    private final GlossaryRepository glossaryRepository;
    private final JavaSourceReader javaSourceReader;
    private final MyBatisStatementsReader myBatisStatementsReader;
    private final JigEventRepository jigEventRepository;

    private JigRepository jigRepository;

    public DefaultJigRepositoryFactory(GlossaryRepository glossaryRepository, JavaSourceReader javaSourceReader, MyBatisStatementsReader myBatisStatementsReader, SourceCollector sourceCollector, JigEventRepository jigEventRepository) {
        this.glossaryRepository = glossaryRepository;
        this.javaSourceReader = javaSourceReader;
        this.myBatisStatementsReader = myBatisStatementsReader;
        this.sourceCollector = sourceCollector;
        this.jigEventRepository = jigEventRepository;
    }

    public static DefaultJigRepositoryFactory init(Configuration configuration) {
        return new DefaultJigRepositoryFactory(
                configuration.glossaryRepository(),
                new JavaparserReader(),
                new MyBatisMyBatisStatementsReader(),
                new ClassOrJavaSourceCollector(configuration.jigEventRepository()),
                configuration.jigEventRepository()
        );
    }

    public void readPathSource(SourceBasePaths sourceBasePaths) {
        Sources sources = sourceCollector.collectSources(sourceBasePaths);
        if (sources.emptyClassSources()) jigEventRepository.recordEvent(ReadStatus.バイナリソースなし);
        if (sources.emptyJavaSources()) jigEventRepository.recordEvent(ReadStatus.テキストソースなし);

        // errorが1つでもあったら読み取り失敗としてSourceを返さない
        if (jigEventRepository.hasError()) {
            jigRepository = JigRepository.empty();
        }

        jigRepository = jigTypesRepository(sources);
    }

    public JigRepository jigTypesRepository() {
        return jigRepository;
    }

    /**
     * プロジェクト情報を読み取る
     */
    private JigRepository jigTypesRepository(Sources sources) {
        JavaSources javaSources = sources.javaSources();

        javaSources.packageInfoPaths().forEach(
                path -> javaSourceReader.loadPackageInfoJavaFile(path, glossaryRepository));

        JavaSourceModel javaSourceModel = javaSources.javaPaths().stream()
                .map(path -> javaSourceReader.parseJavaFile(path, glossaryRepository))
                .reduce(JavaSourceModel::merge)
                .orElseGet(JavaSourceModel::empty);

        ClassSources classSources = sources.classSources();
        Collection<ClassDeclaration> classDeclarations = new AsmClassSourceReader().classSourceModel(classSources);

        // クラス名の解決や対象の選別にClassSourceModelを使用するようにしたいので、この位置。
        // 現状（すくなくとも2025.2.1時点まで）はClassSourceを作る際にASMを使用している。その時点でのASM使用をやめたい。
        MyBatisStatements myBatisStatements = readSqlSource(sources);
        if (myBatisStatements.status().not正常())
            jigEventRepository.recordEvent(ReadStatus.fromSqlReadStatus(myBatisStatements.status()));

        DefaultJigDataProvider defaultJigDataProvider = new DefaultJigDataProvider(javaSourceModel, myBatisStatements);
        JigTypes jigTypes = JigTypeFactory.createJigTypes(classDeclarations, glossaryRepository.all());

        return new JigRepository() {
            @Override
            public JigTypes fetchJigTypes() {
                return jigTypes;
            }

            @Override
            public JigDataProvider jigDataProvider() {
                return defaultJigDataProvider;
            }

            @Override
            public Glossary fetchGlossary() {
                return glossaryRepository.all();
            }
        };
    }

    /**
     * ソースからSQLを読み取る
     */
    public MyBatisStatements readSqlSource(Sources sources) {
        return myBatisStatementsReader.readFrom(sources);
    }
}
