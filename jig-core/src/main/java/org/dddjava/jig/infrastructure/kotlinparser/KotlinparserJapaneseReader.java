package org.dddjava.jig.infrastructure.kotlinparser;

import kastree.ast.psi.Parser;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.*;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.*;
import org.dddjava.jig.infrastructure.codeparser.SourceCodeParser;
import org.jetbrains.kotlin.com.intellij.psi.PsiClass;
import org.jetbrains.kotlin.com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.kotlin.psi.KtFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

            KtFile file = Parser.Companion.parsePsiFile(sourceCode);
            for (PsiClass psiClass : file.getClasses()) {
                PsiDocComment docComment = psiClass.getDocComment();
                String className = psiClass.getQualifiedName();
                if (docComment != null && className != null) {
                    String text = docComment.getText();
                    TypeIdentifier typeIdentifier = new TypeIdentifier(className);
                    typeJapaneseNames.add(new TypeAlias(typeIdentifier, new Alias(text)));
                }
            }
        }

        return new TypeNames(typeJapaneseNames, methodList);
    }

    String parseKotlinFile(KotlinSource source) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source.toInputStream()))) {
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
