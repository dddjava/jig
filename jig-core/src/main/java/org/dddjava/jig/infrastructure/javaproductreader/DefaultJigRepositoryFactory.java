package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigDataProvider;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.sources.DefaultJigDataProvider;
import org.dddjava.jig.domain.model.sources.ReadStatus;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.dddjava.jig.domain.model.sources.mybatis.MyBatisStatementsReader;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.asm.ClassDeclaration;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisMyBatisStatementsReader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class DefaultJigRepositoryFactory {

    private final ClassOrJavaSourceCollector sourceCollector;

    private final AsmClassSourceReader asmClassSourceReader;
    private final JavaparserReader javaparserReader;
    private final MyBatisStatementsReader myBatisStatementsReader;

    private final JigEventRepository jigEventRepository;
    private final GlossaryRepository glossaryRepository;

    public DefaultJigRepositoryFactory(ClassOrJavaSourceCollector sourceCollector, AsmClassSourceReader asmClassSourceReader, JavaparserReader javaparserReader, MyBatisStatementsReader myBatisStatementsReader, JigEventRepository jigEventRepository, GlossaryRepository glossaryRepository) {
        this.sourceCollector = sourceCollector;
        this.asmClassSourceReader = asmClassSourceReader;
        this.javaparserReader = javaparserReader;
        this.myBatisStatementsReader = myBatisStatementsReader;
        this.glossaryRepository = glossaryRepository;
        this.jigEventRepository = jigEventRepository;
    }

    public static DefaultJigRepositoryFactory init(Configuration configuration) {
        return new DefaultJigRepositoryFactory(
                new ClassOrJavaSourceCollector(configuration.jigEventRepository()),
                new AsmClassSourceReader(),
                new JavaparserReader(),
                new MyBatisMyBatisStatementsReader(),
                configuration.jigEventRepository(), configuration.glossaryRepository()
        );
    }

    public JigRepository createJigRepository(SourceBasePaths sourceBasePaths) {
        Sources sources = sourceCollector.collectSources(sourceBasePaths);
        if (sources.emptyClassSources()) jigEventRepository.recordEvent(ReadStatus.バイナリソースなし);
        if (sources.emptyJavaSources()) jigEventRepository.recordEvent(ReadStatus.テキストソースなし);

        // errorが1つでもあったら読み取り失敗としてSourceを返さない
        if (jigEventRepository.hasError()) {
            return JigRepository.empty();
        }

        return jigTypesRepository(sources);
    }

    /**
     * プロジェクト情報を読み取る
     */
    private JigRepository jigTypesRepository(Sources sources) {
        JavaSources javaSources = sources.javaSources();

        javaSources.packageInfoPaths().forEach(
                path -> javaparserReader.loadPackageInfoJavaFile(path, glossaryRepository));

        JavaSourceModel javaSourceModel = javaSources.javaPaths().stream()
                .map(path -> javaparserReader.parseJavaFile(path, glossaryRepository))
                .reduce(JavaSourceModel::merge)
                .orElseGet(JavaSourceModel::empty);

        ClassSources classSources = sources.classSources();
        Collection<ClassDeclaration> classDeclarations = asmClassSourceReader.readClasses(classSources);

        MyBatisStatements myBatisStatements = readMyBatisStatements(sources, classDeclarations);

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

    private MyBatisStatements readMyBatisStatements(Sources sources, Collection<ClassDeclaration> classDeclarations) {
        // MyBatisの読み込み対象となるMapperインタフェース識別のためにJigTypeHeaderを抽出
        Collection<JigTypeHeader> jigTypeHeaders = classDeclarations.stream()
                .map(ClassDeclaration::jigTypeHeader)
                .toList();
        // MyBatisがMapperXMLやインタフェースclassを探すパス
        List<Path> classPaths = sources.sourceBasePaths().classSourceBasePaths();

        MyBatisStatements myBatisStatements = myBatisStatementsReader.readFrom(jigTypeHeaders, classPaths);

        if (myBatisStatements.status().not正常()) {
            jigEventRepository.recordEvent(ReadStatus.fromSqlReadStatus(myBatisStatements.status()));
        }
        return myBatisStatements;
    }
}
