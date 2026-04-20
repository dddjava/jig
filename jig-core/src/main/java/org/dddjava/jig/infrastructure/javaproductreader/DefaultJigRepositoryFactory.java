package org.dddjava.jig.infrastructure.javaproductreader;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessor;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.information.inbound.InboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.ExternalAccessorRepositories;
import org.dddjava.jig.domain.model.information.outbound.other.OtherExternalAccessorRepository;
import org.dddjava.jig.domain.model.information.outbound.springdata.SpringDataJdbcStatementsReader;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.sources.ReadStatus;
import org.dddjava.jig.domain.model.sources.filesystem.FilesystemSources;
import org.dddjava.jig.domain.model.sources.filesystem.JavaFilePaths;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.mybatis.SqlReadStatus;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.asm.ClassDeclaration;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisStatementsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultJigRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultJigRepositoryFactory.class);

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
                new MyBatisStatementsReader(),
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

            PersistenceAccessorRepository persistenceAccessorRepository = Objects.requireNonNull(Metrics.timer(metricName, "phase", "mybatis_reading").record(() ->
                    createPersistenceAccessorRepository(sources, classDeclarations)));

            JigTypes jigTypes = JigTypeFactory.createJigTypes(classDeclarations);

            // Swagger @Operation(summary) 由来の用語を登録する
            // Javadocに由来する用語が優先のため、TermIdが重複する場合はスキップしてログを出力する
            var existingTermIds = glossaryRepository.all().terms().stream()
                    .map(Term::id)
                    .collect(Collectors.toSet());
            InboundAdapters.from(jigTypes).listEntrypoint().forEach(entrypoint ->
                    entrypoint.swaggerSummary().ifPresent(summary -> {
                        var termId = new TermId(entrypoint.jigMethod().fqn());
                        if (existingTermIds.contains(termId)) {
                            logger.info("[JIG] {} はJavadocによる用語が登録済みのためSwagger @Operationをスキップします", termId.asText());
                        } else {
                            glossaryRepository.register(Term.simple(termId, summary, TermKind.メソッド));
                        }
                    })
            );

            Collection<PersistenceAccessor> springDataJdbcStatements = new SpringDataJdbcStatementsReader().readFrom(jigTypes);
            persistenceAccessorRepository.register(springDataJdbcStatements);

            OtherExternalAccessorRepository otherExternalAccessorRepository = OtherExternalAccessorRepository.from(jigTypes);
            ExternalAccessorRepositories externalAccessorRepositories = new ExternalAccessorRepositories(persistenceAccessorRepository, otherExternalAccessorRepository);

            return Metrics.timer(metricName, "phase", "jig_repository_creation").record(() -> {
                DefaultJigDataProvider defaultJigDataProvider = new DefaultJigDataProvider(javaSourceModel);

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

                    @Override
                    public ExternalAccessorRepositories externalAccessorRepositories() {
                        return externalAccessorRepositories;
                    }
                };
            });
        }));
    }

    /**
     * 永続化アクセサリポジトリの初期構築
     *
     * MyBatis関連はClassLoaderを使用する関係上、ここで処理しておく。
     */
    private PersistenceAccessorRepository createPersistenceAccessorRepository(FilesystemSources sources, Collection<ClassDeclaration> classDeclarations) {
        // MyBatisの読み込み対象となるMapperインタフェース識別のためにJigTypeHeaderを抽出
        Collection<JigTypeHeader> jigTypeHeaders = classDeclarations.stream()
                .map(ClassDeclaration::jigTypeHeader)
                .toList();
        // MyBatisがMapperXMLやインタフェースclassを探すパス
        List<Path> classPaths = sources.sourceBasePaths().classSourceBasePaths();

        var myBatisReadResult = myBatisStatementsReader.readFrom(jigTypeHeaders, classPaths);

        var persistenceAccessorsRepository = myBatisReadResult.persistenceAccessorRepository();

        SqlReadStatus sqlReadStatus = myBatisReadResult.status();
        if (sqlReadStatus == SqlReadStatus.SQLなし && persistenceAccessorsRepository.isEmpty()) {
            jigEventRepository.recordEvent(sqlReadStatus.toReadStatus());
        } else if (sqlReadStatus != SqlReadStatus.成功 && sqlReadStatus != SqlReadStatus.SQLなし) {
            jigEventRepository.recordEvent(myBatisReadResult.status().toReadStatus());
        }
        return persistenceAccessorsRepository;
    }
}
