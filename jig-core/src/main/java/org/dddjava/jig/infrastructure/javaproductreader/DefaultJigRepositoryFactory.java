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
import org.dddjava.jig.domain.model.data.terms.*;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.Diagnostic;
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
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.asm.ClassDeclaration;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisStatementsReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DefaultJigRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultJigRepositoryFactory.class);

    private final AsmClassSourceReader asmClassSourceReader;
    private final JavaparserReader javaparserReader;
    private final MyBatisStatementsReader myBatisStatementsReader;
    private final Supplier<AnalysisState> analysisStateFactory;

    DefaultJigRepositoryFactory(AsmClassSourceReader asmClassSourceReader, JavaparserReader javaparserReader, MyBatisStatementsReader myBatisStatementsReader, Supplier<AnalysisState> analysisStateFactory) {
        this.asmClassSourceReader = asmClassSourceReader;
        this.javaparserReader = javaparserReader;
        this.myBatisStatementsReader = myBatisStatementsReader;
        this.analysisStateFactory = analysisStateFactory;
    }

    public static DefaultJigRepositoryFactory init(Configuration configuration) {
        return new DefaultJigRepositoryFactory(
                new AsmClassSourceReader(),
                new JavaparserReader(),
                new MyBatisStatementsReader(),
                () -> new AnalysisState(
                        new JigEventRepository(configuration.settings().locale()),
                        new OnMemoryGlossaryRepository())
        );
    }

    public JigRepository createJigRepository(SourceBasePaths sourceBasePaths) {
        return createJigRepository(sourceBasePaths, Optional.empty());
    }

    public JigRepository createJigRepository(SourceBasePaths sourceBasePaths, Optional<Path> repositoryRoot) {
        AnalysisState analysisState = analysisStateFactory.get();
        JigEventRepository jigEventRepository = analysisState.jigEventRepository();
        GlossaryRepository glossaryRepository = analysisState.glossaryRepository();
        Timer.Sample sample = Timer.start(io.micrometer.core.instrument.Metrics.globalRegistry);
        try {
            FilesystemSources sources = new ClassOrJavaSourceCollector(jigEventRepository).collectSources(sourceBasePaths);
            if (sources.emptyClassSources()) jigEventRepository.recordEvent(ReadStatus.バイナリソースなし);
            if (sources.emptyJavaSources()) jigEventRepository.recordEvent(ReadStatus.テキストソースなし);

            // errorが1つでもあったら読み取り失敗として分析せず空を返す
            if (jigEventRepository.hasError()) {
                return JigRepository.empty(jigEventRepository.diagnostics());
            }

            return analyze(sources, repositoryRoot, jigEventRepository, glossaryRepository);
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
    private JigRepository analyze(FilesystemSources sources, Optional<Path> repositoryRoot, JigEventRepository jigEventRepository, GlossaryRepository glossaryRepository) {
        var metricName = "jig.analysis.time";
        return Objects.requireNonNull(Metrics.timer(metricName, "phase", "code_analysis_total").record(() -> {
            JavaFilePaths javaFilePaths = sources.javaFilePaths();

            Map<PackageId, Path> packageSourcePathMap = new HashMap<>();
            List<JavaparserReader.PackageInfoParseResult> packageInfoParseResults = Metrics.timer(metricName, "phase", "package_info_parsing").record(() ->
                    javaFilePaths.packageInfoPaths().stream()
                            .map(path -> javaparserReader.parsePackageInfoJavaFile(path, glossaryRepository))
                            .toList());
            packageInfoParseResults.forEach(result -> result.packageId()
                    .ifPresent(packageId -> packageSourcePathMap.put(packageId, result.sourcePath())));

            List<JavaparserReader.ParseResult> parseResults = Objects.requireNonNull(Metrics.timer(metricName, "phase", "java_source_parsing").record(() ->
                    javaFilePaths.javaPaths().stream()
                            .map(path -> javaparserReader.parseJavaFile(path, glossaryRepository))
                            .toList()));
            if (packageInfoParseResults.stream().anyMatch(result -> !result.succeeded())
                    || parseResults.stream().anyMatch(result -> !result.succeeded())) {
                jigEventRepository.recordEvent(ReadStatus.テキストソース読み込み一部失敗);
            }
            JavaSourceModel javaSourceModel = parseResults.stream()
                    .map(JavaparserReader.ParseResult::sourceModel)
                    .reduce(JavaSourceModel::merge)
                    .orElseGet(JavaSourceModel::empty);
            Map<TypeId, Path> typeSourcePathMap = new HashMap<>();
            for (var result : parseResults) {
                for (var typeId : result.declaredTypeIds()) {
                    typeSourcePathMap.put(typeId, result.sourcePath());
                }
            }
            TypeSourcePaths typeSourcePaths = new TypeSourcePaths(Map.copyOf(typeSourcePathMap), Map.copyOf(packageSourcePathMap));

            Collection<ClassDeclaration> classDeclarations = Objects.requireNonNull(
                    Metrics.timer(metricName, "phase", "class_file_parsing").record(() ->
                            asmClassSourceReader.readClasses(sources.classFilePaths())));

            PersistenceAccessorRepository persistenceAccessorRepository = Objects.requireNonNull(Metrics.timer(metricName, "phase", "mybatis_reading").record(() ->
                    createPersistenceAccessorRepository(sources, classDeclarations, jigEventRepository)));

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
                        registerSwaggerTerm(glossaryRepository, existingTermIds, termId, summary, TermKind.メソッド, "@Operation");
                    })
            );
            // @Schema(description) 由来のクラス用語
            var schemaTypeId = TypeId.valueOf("io.swagger.v3.oas.annotations.media.Schema");
            jigTypes.stream().forEach(jigType ->
                    jigType.annotationValueOf(schemaTypeId, "description").ifPresent(description -> {
                        var termId = new TermId(jigType.fqn());
                        registerSwaggerTerm(glossaryRepository, existingTermIds, termId, description, TermKind.クラス, "@Schema");
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
                    public List<Diagnostic> diagnostics() {
                        return jigEventRepository.diagnostics();
                    }

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

    private void registerSwaggerTerm(GlossaryRepository glossaryRepository, Set<TermId> existingTermIds, TermId termId, String value, TermKind kind, String annotationName) {
        if (existingTermIds.contains(termId)) {
            logger.debug("[JIG] {} はJavadocによる用語が登録済みのためSwagger {}をスキップします", termId.asText(), annotationName);
        } else {
            glossaryRepository.register(new Term(termId, value, "", kind, TermOrigin.Swagger));
        }
    }

    /**
     * 永続化アクセサリポジトリの初期構築
     *
     * MyBatis関連はClassLoaderを使用する関係上、ここで処理しておく。
     */
    private PersistenceAccessorRepository createPersistenceAccessorRepository(FilesystemSources sources, Collection<ClassDeclaration> classDeclarations, JigEventRepository jigEventRepository) {
        // MyBatisの読み込み対象となるMapperインタフェース識別のためにJigTypeHeaderを抽出
        Collection<JigTypeHeader> jigTypeHeaders = classDeclarations.stream()
                .map(ClassDeclaration::jigTypeHeader)
                .toList();
        // MyBatisがMapperXMLやインタフェースclassを探すパス
        List<Path> classPaths = sources.sourceBasePaths().classSourceBasePaths();

        var myBatisReadResult = myBatisStatementsReader.readFrom(jigTypeHeaders, classPaths);

        myBatisReadResult.readStatus().ifPresent(jigEventRepository::recordEvent);
        return myBatisReadResult.persistenceAccessorRepository();
    }

    record AnalysisState(JigEventRepository jigEventRepository, GlossaryRepository glossaryRepository) {
    }
}
