package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.unit.ClassDeclaration;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.sources.*;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceReader;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceReader;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.dddjava.jig.domain.model.sources.mybatis.MyBatisStatementsReader;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 取り込みサービス
 */
@Service
public class JigSourceReader {

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

    public Optional<JigTypesRepository> readPathSource(SourceBasePaths sourceBasePaths) {
        Sources sources = sourceCollector.collectSources(sourceBasePaths);
        if (sources.emptyClassSources()) jigEventRepository.recordEvent(ReadStatus.バイナリソースなし);
        if (sources.emptyJavaSources()) jigEventRepository.recordEvent(ReadStatus.テキストソースなし);

        // errorが1つでもあったら読み取り失敗としてSourceを返さない
        if (jigEventRepository.hasError()) {
            return Optional.empty();
        }

        return Optional.of(jigTypeRepository(sources));
    }

    /**
     * プロジェクト情報を読み取る
     */
    public JigTypesRepository jigTypeRepository(Sources sources) {
        JavaSources javaSources = sources.javaSources();

        javaSources.packageInfoPaths().forEach(
                path -> javaSourceReader.loadPackageInfoJavaFile(path, glossaryRepository));

        JavaSourceModel javaSourceModel = javaSources.javaPaths().stream()
                .map(path -> javaSourceReader.parseJavaFile(path, glossaryRepository))
                .reduce(JavaSourceModel::merge)
                .orElseGet(JavaSourceModel::empty);

        ClassSources classSources = sources.classSources();
        ClassSourceModel classSourceModel = classSourceReader.classSourceModel(classSources);

        // クラス名の解決や対象の選別にClassSourceModelを使用するようにしたいので、この位置。
        // 現状（すくなくとも2025.2.1時点まで）はClassSourceを作る際にASMを使用している。その時点でのASM使用をやめたい。
        MyBatisStatements myBatisStatements = readSqlSource(sources);
        if (myBatisStatements.status().not正常())
            jigEventRepository.recordEvent(ReadStatus.fromSqlReadStatus(myBatisStatements.status()));

        DefaultJigDataProvider defaultJigDataProvider = new DefaultJigDataProvider(javaSourceModel, myBatisStatements, glossaryRepository.all());
        JigTypes jigTypes = initializeJigTypes(classSourceModel, glossaryRepository);

        return new JigTypesRepository() {
            @Override
            public JigTypes fetchJigTypes() {
                return jigTypes;
            }

            @Override
            public JigDataProvider jigDataProvider() {
                return defaultJigDataProvider;
            }
        };
    }

    private static JigTypes initializeJigTypes(ClassSourceModel classSourceModel, GlossaryRepository glossaryRepository) {
        return classSourceModel.classDeclarations()
                .stream()
                .map(classDeclaration -> {
                    return JigType.from(
                            classDeclaration.jigTypeHeader(),
                            getJigTypeMembers(glossaryRepository, classDeclaration),
                            glossaryRepository.collectJigTypeTerms(classDeclaration.jigTypeHeader().id())
                    );
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }

    public static JigTypeMembers getJigTypeMembers(GlossaryRepository glossaryRepository, ClassDeclaration classDeclaration) {
        Collection<JigMethod> jigMethods = classDeclaration.jigMethodDeclarations().stream()
                .map(jigMethodDeclaration -> new JigMethod(jigMethodDeclaration,
                        glossaryRepository.getMethodTermPossiblyMatches(jigMethodDeclaration.header().id())))
                .toList();
        return new JigTypeMembers(classDeclaration.jigFieldHeaders(), jigMethods);
    }

    /**
     * ソースからSQLを読み取る
     */
    public MyBatisStatements readSqlSource(Sources sources) {
        return myBatisStatementsReader.readFrom(sources);
    }
}
