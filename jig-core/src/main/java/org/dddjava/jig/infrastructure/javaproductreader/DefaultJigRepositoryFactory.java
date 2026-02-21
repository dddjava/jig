package org.dddjava.jig.infrastructure.javaproductreader;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatement;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatements;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.sources.ReadStatus;
import org.dddjava.jig.domain.model.sources.filesystem.FilesystemSources;
import org.dddjava.jig.domain.model.sources.filesystem.JavaFilePaths;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.mybatis.MyBatisStatementsReader;
import org.dddjava.jig.domain.model.sources.mybatis.SqlReadStatus;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.asm.ClassDeclaration;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisStatementsReaderImpl;
import org.dddjava.jig.infrastructure.springdatajdbc.SpringDataJdbcStatementsReader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DefaultJigRepositoryFactory {

    private final ClassOrJavaSourceCollector sourceCollector;

    private final AsmClassSourceReader asmClassSourceReader;
    private final JavaparserReader javaparserReader;
    private final MyBatisStatementsReader myBatisStatementsReader;
    private final SpringDataJdbcStatementsReader springDataJdbcStatementsReader;

    private final JigEventRepository jigEventRepository;
    private final GlossaryRepository glossaryRepository;

    public DefaultJigRepositoryFactory(ClassOrJavaSourceCollector sourceCollector, AsmClassSourceReader asmClassSourceReader, JavaparserReader javaparserReader, MyBatisStatementsReader myBatisStatementsReader, SpringDataJdbcStatementsReader springDataJdbcStatementsReader, JigEventRepository jigEventRepository, GlossaryRepository glossaryRepository) {
        this.sourceCollector = sourceCollector;
        this.asmClassSourceReader = asmClassSourceReader;
        this.javaparserReader = javaparserReader;
        this.myBatisStatementsReader = myBatisStatementsReader;
        this.springDataJdbcStatementsReader = springDataJdbcStatementsReader;
        this.glossaryRepository = glossaryRepository;
        this.jigEventRepository = jigEventRepository;
    }

    public static DefaultJigRepositoryFactory init(Configuration configuration) {
        return new DefaultJigRepositoryFactory(
                new ClassOrJavaSourceCollector(configuration.jigEventRepository()),
                new AsmClassSourceReader(),
                new JavaparserReader(),
                new MyBatisStatementsReaderImpl(),
                new SpringDataJdbcStatementsReader(),
                configuration.jigEventRepository(), configuration.glossaryRepository()
        );
    }

    public JigRepository createJigRepository(SourceBasePaths sourceBasePaths) {
        Timer.Sample sample = Timer.start(io.micrometer.core.instrument.Metrics.globalRegistry);
        try {
            FilesystemSources sources = sourceCollector.collectSources(sourceBasePaths);
            if (sources.emptyClassSources()) jigEventRepository.recordEvent(ReadStatus.バイナリソースなし);
            if (sources.emptyJavaSources()) jigEventRepository.recordEvent(ReadStatus.テキストソースなし);

            // errorが1つでもあったら読み取り失敗として分析せず空を返す
            if (jigEventRepository.hasError()) {
                return JigRepository.empty();
            }

            return analyze(sources);
        } finally {
            sample.stop(Timer.builder("jig.analysis.time")
                    .description("Time taken for code analysis")
                    .tag("phase", "repository_creation")
                    .register(io.micrometer.core.instrument.Metrics.globalRegistry));
        }
    }

    /**
     * プロジェクト情報を読み取る
     */
    private JigRepository analyze(FilesystemSources sources) {
        var metricName = "jig.analysis.time";
        return Objects.requireNonNull(Metrics.timer(metricName, "phase", "code_analysis_total").record(() -> {
            JavaFilePaths javaFilePaths = sources.javaFilePaths();

            Metrics.timer(metricName, "phase", "package_info_parsing").record(() ->
                    javaFilePaths.packageInfoPaths().forEach(
                            path -> javaparserReader.loadPackageInfoJavaFile(path, glossaryRepository))
            );

            JavaSourceModel javaSourceModel = Objects.requireNonNull(Metrics.timer(metricName, "phase", "java_source_parsing").record(() ->
                    javaFilePaths.javaPaths().stream()
                            .map(path -> javaparserReader.parseJavaFile(path, glossaryRepository))
                            .reduce(JavaSourceModel::merge)
                            .orElseGet(JavaSourceModel::empty)));

            Collection<ClassDeclaration> classDeclarations = Objects.requireNonNull(
                    Metrics.timer(metricName, "phase", "class_file_parsing").record(() ->
                            asmClassSourceReader.readClasses(sources.classFilePaths())));

            SqlStatements sqlStatements = Objects.requireNonNull(Metrics.timer(metricName, "phase", "mybatis_reading").record(() ->
                    readMyBatisStatements(sources, classDeclarations)));

            return Metrics.timer(metricName, "phase", "jig_repository_creation").record(() -> {
                DefaultJigDataProvider defaultJigDataProvider = new DefaultJigDataProvider(javaSourceModel, sqlStatements);
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

                    @Override
                    public JigResult.JigSummary summary() {
                        return new JigResult.JigSummary(
                                sources.javaFilePaths().size(),
                                sources.classFilePaths().size(),
                                fetchJigTypes().typeIds().packageIds().size(),
                                fetchJigTypes().typeIds().size(),
                                fetchJigTypes().stream().mapToInt(jigType -> Math.toIntExact(jigType.allJigMethodStream().count())).sum()
                        );
                    }
                };
            });
        }));
    }

    private SqlStatements readMyBatisStatements(FilesystemSources sources, Collection<ClassDeclaration> classDeclarations) {
        // MyBatisの読み込み対象となるMapperインタフェース識別のためにJigTypeHeaderを抽出
        Collection<JigTypeHeader> jigTypeHeaders = classDeclarations.stream()
                .map(ClassDeclaration::jigTypeHeader)
                .toList();
        // MyBatisがMapperXMLやインタフェースclassを探すパス
        List<Path> classPaths = sources.sourceBasePaths().classSourceBasePaths();

        var myBatisReadResult = myBatisStatementsReader.readFrom(jigTypeHeaders, classPaths);
        var springDataJdbcStatements = springDataJdbcStatementsReader.readFrom(jigTypeHeaders, classPaths);

        SqlStatements mergedStatements = mergeStatements(myBatisReadResult.sqlStatements(), springDataJdbcStatements);

        SqlReadStatus sqlReadStatus = myBatisReadResult.status();
        if (sqlReadStatus == SqlReadStatus.SQLなし && mergedStatements.isEmpty()) {
            jigEventRepository.recordEvent(sqlReadStatus.toReadStatus());
        } else if (sqlReadStatus != SqlReadStatus.成功 && sqlReadStatus != SqlReadStatus.SQLなし) {
            jigEventRepository.recordEvent(myBatisReadResult.status().toReadStatus());
        }
        return mergedStatements;
    }

    private SqlStatements mergeStatements(SqlStatements myBatisStatements, List<SqlStatement> springDataJdbcStatements) {
        return new SqlStatements(Stream.concat(
                        myBatisStatements.list().stream(),
                        springDataJdbcStatements.stream())
                .distinct()
                .toList());
    }
}
