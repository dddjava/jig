package org.dddjava.jig.infrastructure.kotlin;

import org.dddjava.jig.domain.model.parts.class_.method.Arguments;
import org.dddjava.jig.domain.model.parts.class_.method.MethodComment;
import org.dddjava.jig.domain.model.parts.class_.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.class_.method.MethodSignature;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.comment.Comment;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KotlinSourceVisitor extends KtTreeVisitorVoid {

    List<ClassComment> typeJapaneseAliases = new ArrayList<>();
    List<MethodComment> methodList = new ArrayList<>();

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
        typeJapaneseAliases.add(new ClassComment(identifier, Comment.fromCodeComment(text)));
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
            if (typeReference == null) {
                continue;
            }
            String string = typeString(typeReference);
            args.add(new TypeIdentifier(string));
        }

        MethodIdentifier methodIdentifier = new MethodIdentifier(identifier, new MethodSignature(methodName, new Arguments(args)));
        methodList.add(new MethodComment(methodIdentifier, Comment.fromCodeComment(text)));
    }

    private KtClass findKtClass(KtNamedFunction function) {
        PsiElement parent = function.getParent();
        if (!(parent instanceof KtClassBody)) {
            return null;
        }

        parent = parent.getParent();

        return parent instanceof KtClass ? (KtClass) parent : null;
    }

    private String typeString(KtTypeReference typeReference) {
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
}
