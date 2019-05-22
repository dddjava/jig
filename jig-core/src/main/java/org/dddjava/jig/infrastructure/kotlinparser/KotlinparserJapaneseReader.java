package org.dddjava.jig.infrastructure.kotlinparser;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.*;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.*;
import org.dddjava.jig.infrastructure.codeparser.SourceCodeParser;
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiManager;
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class KotlinparserJapaneseReader implements SourceCodeParser {
    @Override
    public PackageNames readPackages(PackageInfoSources packageInfoSources) {
        return new PackageNames(new ArrayList<>());
    }

    @Override
    public TypeNames readTypes(SourceCodes<? extends SourceCode> sources) {
        KotlinSources kotlinSources = (KotlinSources) sources;

        List<TypeAlias> typeJapaneseNames = new ArrayList<>();
        List<MethodAlias> methodList = new ArrayList<>();

        for (KotlinSource kotlinSource : kotlinSources.list()) {
            String sourceCode = parseKotlinFile(kotlinSource);
            if (sourceCode == null) {
                continue;
            }

            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.Companion.getNONE());
            KotlinCoreEnvironment environment = KotlinCoreEnvironment.createForProduction(() -> { }, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES);
            Project project = environment.getProject();
            LightVirtualFile virtualFile = new LightVirtualFile(kotlinSource.sourceFilePath().fineName(), KotlinFileType.INSTANCE, sourceCode);
            KtFile file = (KtFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (file == null) {
                continue;
            }

            KtTreeVisitorVoid sourceVisitor = new KtTreeVisitorVoid() {
                @Override
                public void visitClass(KtClass klass) {
                    super.visitClass(klass);
                    KDoc docComment = klass.getDocComment();
                    if (docComment == null) {
                        return;
                    }
                    KDocSection comment = docComment.getDefaultSection();
                    String text = comment.getContent();
                    FqName fullClassName = klass.getFqName();
                    if (fullClassName == null) {
                        return;
                    }
                    TypeIdentifier identifier = new TypeIdentifier(fullClassName.asString());
                    typeJapaneseNames.add(new TypeAlias(identifier, new Alias(text)));
                }

                @Override
                public void visitNamedFunction(KtNamedFunction function) {
                    super.visitNamedFunction(function);
                    KDoc docComment = function.getDocComment();
                    if (docComment == null) {
                        return;
                    }
                    KDocSection comment = docComment.getDefaultSection();
                    String text = comment.getContent();
                    String methodName = function.getName();
                    KtClass ktClass = findKtClass(function);
                    if (ktClass == null) {
                        return;
                    }
                    ArrayList<TypeIdentifier> args = new ArrayList<>();
                    TypeIdentifier identifier = new TypeIdentifier(ktClass.getFqName().asString());

                    for (KtParameter parameter : function.getValueParameters()) {
                        KtTypeReference typeReference = parameter.getTypeReference();
                        String string = asString(typeReference);
                        args.add(new TypeIdentifier(string));
                    }

                    MethodIdentifier methodIdentifier = new MethodIdentifier(identifier, new MethodSignature(methodName, new Arguments(args)));
                    methodList.add(new MethodAlias(methodIdentifier, new Alias(text)));
                }
            };

            file.accept(sourceVisitor);
        }

        return new TypeNames(typeJapaneseNames, methodList);
    }

    private String asString(KtTypeReference typeReference) {
        String referenceText = typeReference.getText();
        KtImportDirective importDirective = typeReference.getContainingKtFile().getImportDirectives().stream()
                .filter(it -> Objects.equals(it.getAliasName(), referenceText) || Objects.equals(it.getImportedFqName().shortName().getIdentifier(), referenceText))
                .findFirst().orElse(null);
        if (importDirective != null) {
            return importDirective.getImportedFqName().asString();
        }

        Optional<KtDeclaration> first = typeReference.getContainingKtFile().getDeclarations().stream()
                .filter(it -> it instanceof KtClassOrObject)
                .filter(it -> Objects.equals(it.getName(), referenceText))
                .findFirst();

        return first.map(KtDeclaration::getName).orElse(referenceText);
    }

    private KtClass findKtClass(KtNamedFunction function) {
        PsiElement parent = function.getParent();
        if (!(parent instanceof KtClassBody)) {
            return null;
        }

        parent = parent.getParent();

        return parent instanceof KtClass ? (KtClass) parent : null;
    }

    private String parseKotlinFile(KotlinSource source) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source.toInputStream(), Charset.forName("utf8")))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isSupport(PackageInfoSources packageInfoSources) {
        return false;
    }

    @Override
    public boolean isSupport(SourceCodes<? extends SourceCode> sourceCodes) {
        return sourceCodes instanceof KotlinSources;
    }
}
