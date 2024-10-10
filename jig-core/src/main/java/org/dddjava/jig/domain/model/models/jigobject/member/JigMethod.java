package org.dddjava.jig.domain.model.models.jigobject.member;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.parts.classes.method.*;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * メソッド
 */
public class JigMethod {

    MethodDeclaration methodDeclaration;
    MethodComment methodComment;

    boolean nullDecision;

    DecisionNumber decisionNumber;
    MethodAnnotations methodAnnotations;
    Visibility visibility;

    MethodDepend methodDepend;
    MethodDerivation methodDerivation;
    MethodImplementation methodImplementation;

    public JigMethod(MethodDeclaration methodDeclaration, MethodComment methodComment, boolean nullDecision, DecisionNumber decisionNumber, MethodAnnotations methodAnnotations, Visibility visibility, MethodDepend methodDepend, MethodDerivation methodDerivation, MethodImplementation methodImplementation) {
        this.methodDeclaration = methodDeclaration;
        this.methodComment = methodComment;
        this.nullDecision = nullDecision;
        this.decisionNumber = decisionNumber;
        this.methodAnnotations = methodAnnotations;
        this.visibility = visibility;
        this.methodDepend = methodDepend;
        this.methodDerivation = methodDerivation;
        this.methodImplementation = methodImplementation;
    }

    public MethodDeclaration declaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return decisionNumber;
    }

    public MethodAnnotations methodAnnotations() {
        return methodAnnotations;
    }

    public Visibility visibility() {
        return visibility;
    }

    public boolean isPublic() {
        return visibility.isPublic();
    }

    public UsingFields usingFields() {
        return methodDepend.usingFields();
    }

    public UsingMethods usingMethods() {
        return methodDepend.usingMethods();
    }

    public MethodWorries methodWorries() {
        return new MethodWorries(this);
    }

    public boolean conditionalNull() {
        return nullDecision;
    }

    public boolean referenceNull() {
        return methodDepend.hasNullReference();
    }

    public boolean notUseMember() {
        return methodDepend.notUseMember();
    }

    public TypeIdentifiers usingTypes() {
        return methodDepend.usingTypes();
    }

    public String aliasTextOrBlank() {
        return methodComment.asText();
    }

    public String aliasText() {
        return methodComment
                .asTextOrDefault(declaration().declaringType().asSimpleText() + "\\n"
                        + declaration().methodSignature().methodName());
    }

    public JigMethodDescription description() {
        return JigMethodDescription.from(methodComment.documentationComment());
    }

    /**
     * 出力時に使用する名称
     */
    public String labelTextWithSymbol() {
        return visibility.symbol() + ' ' + labelText();
    }

    public String labelText() {
        return methodComment.asTextOrDefault(declaration().methodSignature().methodName());
    }

    public String fqn() {
        return declaration().identifier().asText();
    }

    public String htmlIdText() {
        return declaration().htmlIdText();
    }

    public String labelTextOrLambda() {
        if (declaration().isLambda()) {
            return "lambda";
        }
        return labelText();
    }

    public List<TypeIdentifier> listArguments() {
        return declaration().methodSignature().listArgumentTypeIdentifiers();
    }

    public MethodDerivation derivation() {
        return methodDerivation;
    }

    public boolean objectMethod() {
        return declaration().methodSignature().isObjectMethod();
    }

    public boolean documented() {
        return methodComment.exists();
    }

    /**
     * 注目に値するかの判定
     *
     * publicもしくはドキュメントコメントが記述されているものを「注目に値する」と識別する。
     * privateでもドキュメントコメントが書かれているものは注目する。
     */
    public boolean remarkable() {
        return visibility == Visibility.PUBLIC || documented();
    }

    public List<MethodDeclaration> methodInstructions() {
        return methodDepend.methodInstructions();
    }

    /**
     * メソッド関連のダイアグラム
     */
    public String usecaseMermaidText(JigTypes jigTypes, MethodRelations methodRelations) {
        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");
        // 自身のスタイル（太文字）

        // 基点からの呼び出し全部 + 直近の呼び出し元
        var filteredRelations = methodRelations.filterFromRecursive(this.declaration())
                .merge(methodRelations.filterTo(this.declaration()));

        Set<MethodIdentifier> resolved = new HashSet<>();
        Set<TypeIdentifier> others = new HashSet<>();

        // メソッドのスタイル
        filteredRelations.methodIdentifiers().forEach(methodIdentifier -> {
            // 自分は太字にする
            if (methodIdentifier.equals(declaration().identifier())) {
                resolved.add(methodIdentifier);
                mermaidText.add(usecaseMermaidNodeText());
                mermaidText.add("style %s font-weight:bold".formatted(this.htmlIdText()));
            } else {
                jigTypes.resolveJigMethod(methodIdentifier).ifPresentOrElse(
                        jigMethod -> {
                            resolved.add(methodIdentifier);
                            if (jigMethod.remarkable()) {
                                // 出力対象のメソッドはusecase型＆クリックできるように
                                mermaidText.add(jigMethod.usecaseMermaidNodeText());
                                mermaidText.add("click %s \"#%s\"".formatted(jigMethod.htmlIdText(), jigMethod.htmlIdText()));
                            } else {
                                // remarkableでないものは普通の。privateメソッドなど該当。　
                                mermaidText.add(jigMethod.normalMermaidNodeText());
                            }
                        },
                        () -> others.add(methodIdentifier.declaringType()));
            }
        });

        // JigMethodにならないものはクラスノードとして出力する
        others.forEach(typeIdentifier ->
                mermaidText.add("%s[%s]:::others".formatted(typeIdentifier.htmlIdText(), typeIdentifier.asSimpleText())));

        mermaidText.add(filteredRelations.mermaidEdgeText(resolved));

        mermaidText.add("classDef others fill:#AAA,font-size:90%;");
        mermaidText.add("classDef lambda fill:#999,font-size:80%;");

        return mermaidText.toString();
    }

    private String normalMermaidNodeText() {
        if (declaration().isLambda()) {
            return "%s[\"%s\"]:::lambda".formatted(htmlIdText(), "(lambda)");
        }
        return "%s[\"%s\"]".formatted(htmlIdText(), labelText());
    }

    private String usecaseMermaidNodeText() {
        return "%s([\"%s\"])".formatted(htmlIdText(), labelTextOrLambda());
    }
}
