package org.dddjava.jig.gradle;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
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

    @Input
    public abstract Property<String> getDiagramFormat();

    @Input
    public abstract Property<Boolean> getDiagramTransitiveReduction();

    @Input
    public abstract Property<String> getDotTimeout();

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

        JigProperties jigProperties = new JigProperties(
                documentTypes,
                Optional.ofNullable(getModelPattern().getOrNull()).filter(s -> !s.isEmpty()),
                outputDirectory,
                JigDiagramFormat.valueOf(getDiagramFormat().get()),
                getDiagramTransitiveReduction().get(),
                parseDotTimeout(getDotTimeout().get())
        );

        Configuration configuration = Configuration.from(jigProperties);

        getLogger().info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------",
                jigProperties);

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

    private List<JigDocument> resolveDocumentTypes() {
        List<JigDocument> toExclude = getDocumentTypesExclude().get().stream()
                .map(JigDocument::valueOf)
                .toList();

        List<String> includeTypes = getDocumentTypes().get();
        List<JigDocument> toInclude = includeTypes.isEmpty()
                ? JigDocument.canonical()
                : includeTypes.stream().map(JigDocument::valueOf).toList();

        return toInclude.stream()
                .filter(each -> !toExclude.contains(each))
                .toList();
    }

    private Duration parseDotTimeout(String dotTimeout) {
        if (dotTimeout.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(dotTimeout.substring(0, dotTimeout.length() - 2)));
        }
        if (dotTimeout.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(dotTimeout.substring(0, dotTimeout.length() - 1)));
        }
        throw new IllegalArgumentException("dotTimeout must be end with ms or s. " + dotTimeout + " is invalid.");
    }
}
