package org.dddjava.jig.infrastructure.javaproductreader;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessor;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.terms.TermOrigin;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.TypeId;
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
import org.dddjava.jig.domain.model.sources.javasources.TypeSourcePaths;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
        return createJigRepository(sourceBasePaths, Optional.empty());
    }

    public JigRepository createJigRepository(SourceBasePaths sourceBasePaths, Optional<Path> repositoryRoot) {
        Timer.Sample sample = Timer.start(io.micrometer.core.instrument.Metrics.globalRegistry);
        try {
            FilesystemSources sources = sourceCollector.collectSources(sourceBasePaths);
            if (sources.emptyClassSources()) jigEventRepository.recordEvent(ReadStatus.バイナリソースなし);
            if (sources.emptyJavaSources()) jigEventRepository.recordEvent(ReadStatus.テキストソースなし);

            // errorが1つでもあったら読み取り失敗として分析せず空を返す
            if (jigEventRepository.hasError()) {
                return JigRepository.empty();
            }

            return analyze(sources, repositoryRoot);
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
    private JigRepository analyze(FilesystemSources sources, Optional<Path> repositoryRoot) {
        var metricName = "jig.analysis.time";
        return Objects.requireNonNull(Metrics.timer(metricName, "phase", "code_analysis_total").record(() -> {
            JavaFilePaths javaFilePaths = sources.javaFilePaths();

            Map<PackageId, Path> packageSourcePathMap = new HashMap<>();
            Metrics.timer(metricName, "phase", "package_info_parsing").record(() ->
                    javaFilePaths.packageInfoPaths().forEach(path ->
                            javaparserReader.loadPackageInfoJavaFile(path, glossaryRepository)
                                    .ifPresent(packageId -> packageSourcePathMap.put(packageId, path)))
            );

            Map<TypeId, Path> typeSourcePathMap = new HashMap<>();
            JavaSourceModel javaSourceModel = Objects.requireNonNull(Metrics.timer(metricName, "phase", "java_source_parsing").record(() ->
                    javaFilePaths.javaPaths().stream()
                            .map(path -> javaparserReader.parseJavaFile(path, glossaryRepository))
                            .peek(result -> result.declaredTypeIds().forEach(typeId -> typeSourcePathMap.put(typeId, result.sourcePath())))
                            .map(JavaparserReader.ParseResult::sourceModel)
                            .reduce(JavaSourceModel::merge)
                            .orElseGet(JavaSourceModel::empty)));
            TypeSourcePaths typeSourcePaths = new TypeSourcePaths(Map.copyOf(typeSourcePathMap), Map.copyOf(packageSourcePathMap));

            Collection<ClassDeclaration> classDeclarations = Objects.requireNonNull(
                    Metrics.timer(metricName, "phase", "class_file_parsing").record(() ->
                            asmClassSourceReader.readClasses(sources.classFilePaths())));

            PersistenceAccessorRepository persistenceAccessorRepository = Objects.requireNonNull(Metrics.timer(metricName, "phase", "mybatis_reading").record(() ->
                    createPersistenceAccessorRepository(sources, classDeclarations)));

            JigTypes jigTypes = JigTypeFactory.createJigTypes(classDeclarations);

            // Swagger アノテーション由来の用語を登録する
            // Javadocに由来する用語が優先のため、TermIdが重複する場合はスキップしてログを出力する
            var existingTermIds = glossaryRepository.all().terms().stream()
                    .map(Term::id)
                    .collect(Collectors.toSet());
            // @Operation(summary) 由来のメソッド用語
            InboundAdapters.from(jigTypes).listEntrypoint().forEach(entrypoint ->
                    entrypoint.swaggerSummary().ifPresent(summary -> {
                        var termId = new TermId(entrypoint.jigMethod().fqn());
                        registerSwaggerTerm(existingTermIds, termId, summary, TermKind.メソッド, "@Operation");
                    })
            );
            // @Schema(description) 由来のクラス用語
            var schemaTypeId = TypeId.valueOf("io.swagger.v3.oas.annotations.media.Schema");
            jigTypes.stream().forEach(jigType ->
                    jigType.annotationValueOf(schemaTypeId, "description").ifPresent(description -> {
                        var termId = new TermId(jigType.fqn());
                        registerSwaggerTerm(existingTermIds, termId, description, TermKind.クラス, "@Schema");
                    })
            );

            Collection<PersistenceAccessor> springDataJdbcStatements = new SpringDataJdbcStatementsReader().readFrom(jigTypes);
            persistenceAccessorRepository = persistenceAccessorRepository.merging(springDataJdbcStatements);

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

                    @Override
                    public TypeSourcePaths typeSourcePaths() {
                        return typeSourcePaths;
                    }

                    @Override
                    public Optional<Path> repositoryRoot() {
                        return repositoryRoot;
                    }
                };
            });
        }));
    }

    private void registerSwaggerTerm(Set<TermId> existingTermIds, TermId termId, String value, TermKind kind, String annotationName) {
        if (existingTermIds.contains(termId)) {
            logger.info("[JIG] {} はJavadocによる用語が登録済みのためSwagger {}をスキップします", termId.asText(), annotationName);
        } else {
            glossaryRepository.register(new Term(termId, value, "", kind, TermOrigin.Swagger));
        }
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
