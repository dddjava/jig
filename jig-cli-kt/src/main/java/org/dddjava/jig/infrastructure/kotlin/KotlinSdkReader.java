package org.dddjava.jig.infrastructure.kotlin;

import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSource;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.sources.jigreader.ClassAndMethodComments;
import org.dddjava.jig.domain.model.sources.jigreader.KotlinTextSourceReader;
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.com.intellij.psi.PsiManager;
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.psi.KtFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * KotlinのSDKを使用して別名を読み取る
 */
public class KotlinSdkReader implements KotlinTextSourceReader {

    @Override
    public ClassAndMethodComments readClasses(KotlinSources sources) {
        KotlinSourceVisitor visitor = new KotlinSourceVisitor();

        for (KotlinSource kotlinSource : sources.list()) {
            KtFile ktFile = readKotlinSource(kotlinSource);
            if (ktFile == null) {
                continue;
            }

            ktFile.accept(visitor);
        }

        return new ClassAndMethodComments(visitor.typeJapaneseAliases, visitor.methodList);
    }

    private KtFile sourceToKtFile(KotlinSource kotlinSource, String source) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.Companion.getNONE());
        KotlinCoreEnvironment environment = KotlinCoreEnvironment.createForProduction(() -> {
        }, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES);
        Project project = environment.getProject();
        LightVirtualFile virtualFile = new LightVirtualFile(kotlinSource.sourceFilePath().fineName(), KotlinFileType.INSTANCE, source);
        return (KtFile) PsiManager.getInstance(project).findFile(virtualFile);
    }

    private KtFile readKotlinSource(KotlinSource source) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source.toInputStream(), StandardCharsets.UTF_8))) {
            String sourceCode = bufferedReader.lines().collect(Collectors.joining("\n"));
            return sourceToKtFile(source, sourceCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
