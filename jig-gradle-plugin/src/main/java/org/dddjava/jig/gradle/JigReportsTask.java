package org.dddjava.jig.gradle;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;
import org.dddjava.jig.infrastructure.configuration.JigSettingsLoader;
import org.dddjava.jig.infrastructure.configuration.PartialJigSettings;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.DisableCachingByDefault;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

@DisableCachingByDefault(because = "JigReportsTask generates files via JigExecutor whose outputs are not fully declarable")
public abstract class JigReportsTask extends DefaultTask {

    @Input
    public abstract Property<String> getModelPattern();

    @Input
    public abstract ListProperty<String> getDocumentTypes();

    @Input
    public abstract ListProperty<String> getDocumentTypesExclude();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getClassFiles();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSourceFiles();

    @Internal
    public abstract Property<Boolean> getJavaPluginApplied();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    void outputReports() {
        if (!getJavaPluginApplied().getOrElse(false)) {
            throw new IllegalStateException("Java プラグインが適用されていません。");
        }

        List<JigDocument> documentTypes = resolveDocumentTypes();
        Path outputDirectory = getOutputDirectory().getAsFile().get().toPath();

        PartialJigSettings explicit = PartialJigSettings.builder()
                .outputDirectory(outputDirectory)
                .domainPattern(getModelPattern().getOrNull())
                .jigDocuments(documentTypes)
                .build();
        JigSettings settings = JigSettingsLoader.loadStandard(explicit);
        Configuration configuration = Configuration.from(settings);

        getLogger().info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------",
                settings);

        long startTime = System.currentTimeMillis();

        Set<Path> classPaths = getClassFiles().getFiles().stream()
                .map(File::toPath)
                .collect(toSet());
        Set<Path> sourcePaths = getSourceFiles().getFiles().stream()
                .map(File::toPath)
                .collect(toSet());
        SourceBasePaths sourceBasePaths = new SourceBasePaths(
                new SourceBasePath(classPaths),
                new SourceBasePath(sourcePaths)
        );

        JigResult jigResult = JigExecutor.standard(configuration, sourceBasePaths);
        List<HandleResult> handleResultList = jigResult.listResult();

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
                .collect(joining("\n"));
        if (!resultLog.isBlank()) {
            resultLog += "\n";
        }
        resultLog += "index : [ " + jigResult.indexFilePath() + " ]";
        getLogger().info("-- Output Complete {} ms -------------------------------------------\n{}\n------------------------------------------------------------",
                System.currentTimeMillis() - startTime, resultLog);
    }

    /**
     * include / exclude を解決して最終的なドキュメント一覧を求める。
     *
     * exclude（{@code documentTypesExclude}）は Gradle 拡張固有の利便機能であり、ここで解決してから
     * コアへ最終リストとして渡す。jig-core の設定モデル（{@code jig.document.types}）は最終的な
     * include リストのみを持ち、減算的な exclude の概念は持たない（全設定ソースで一貫した
     * first-non-empty-wins マージを保つための意図的な設計）。CLI / properties に exclude は無い。
     */
    private List<JigDocument> resolveDocumentTypes() {
        List<JigDocument> toExclude = getDocumentTypesExclude().get().stream()
                .map(JigDocument::valueOf)
                .toList();

        List<String> includeTypes = getDocumentTypes().get();
        List<JigDocument> toInclude = includeTypes.isEmpty()
                ? JigDocument.canonical()
                : includeTypes.stream().map(JigDocument::valueOf).toList();

        List<JigDocument> resolved = toInclude.stream()
                .filter(each -> !toExclude.contains(each))
                .toList();
        if (resolved.isEmpty()) {
            // 空リストはコアの first-non-empty-wins マージで読み飛ばされ canonical（全種別）に
            // フォールバックしてしまう。利用者の意図（全除外）と真逆になるため明示的に弾く。
            throw new IllegalStateException(
                    "出力対象のドキュメントがありません。documentTypes %s から documentTypesExclude %s を除外した結果が空です。"
                            .formatted(toInclude, toExclude));
        }
        return resolved;
    }
}
