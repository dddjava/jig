package org.dddjava.jig.domain.model.information.jigobject.member;

import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * メソッド
 */
public class JigMethod {

    MethodDeclaration methodDeclaration;

    MethodAnnotations methodAnnotations;
    Visibility visibility;

    MethodDerivation methodDerivation;
    MethodImplementation methodImplementation;
    private final Instructions instructions;
    private final List<TypeIdentifier> throwsTypes;
    private final List<TypeIdentifier> signatureContainedTypes;

    public JigMethod(MethodDeclaration methodDeclaration, MethodAnnotations methodAnnotations, Visibility visibility, MethodDerivation methodDerivation, Instructions instructions, List<TypeIdentifier> throwsTypes, List<TypeIdentifier> signatureContainedTypes, MethodImplementation methodImplementation) {
        this.methodDeclaration = methodDeclaration;
        this.methodAnnotations = methodAnnotations;
        this.visibility = visibility;
        this.methodDerivation = methodDerivation;
        this.methodImplementation = methodImplementation;
        this.instructions = instructions;
        this.throwsTypes = throwsTypes;
        this.signatureContainedTypes = signatureContainedTypes;
    }

    public MethodDeclaration declaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return instructions.decisionNumber();
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
        return new UsingFields(instructions.fieldReferences());
    }

    public UsingMethods usingMethods() {
        return new UsingMethods(instructions.instructMethods());
    }

    public boolean conditionalNull() {
        return instructions.hasNullDecision();
    }

    public boolean referenceNull() {
        return instructions.hasNullReference();
    }

    public boolean useNull() {
        return referenceNull() || conditionalNull();
    }

    public boolean notUseMember() {
        return instructions.hasMemberInstruction();
    }

    public TypeIdentifiers usingTypes() {
        var list = Stream.of(
                        instructions.usingTypes(),
                        methodDeclaration.relateTypes(),
                        methodAnnotations.list().stream().map(MethodAnnotation::annotationType).toList(),
                        throwsTypes,
                        signatureContainedTypes)
                .flatMap(Collection::stream)
                .toList();
        return new TypeIdentifiers(list);
    }

    public String aliasTextOrBlank() {
        return methodImplementation.comment().summaryText();
    }

    public String aliasText() {
        return methodImplementation.comment()
                .asTextOrDefault(declaration().declaringType().asSimpleText() + "\\n"
                        + declaration().methodSignature().methodName());
    }

    public JigMethodDescription description() {
        return JigMethodDescription.from(methodImplementation.comment());
    }

    /**
     * 出力時に使用する名称
     */
    public String labelTextWithSymbol() {
        return visibility.symbol() + ' ' + labelText();
    }

    public String labelText() {
        return methodImplementation.comment().asTextOrDefault(declaration().methodSignature().methodName());
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
        return methodImplementation.comment().exists();
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
        return instructions.instructMethods().list();
    }

    /**
     * メソッド関連のダイアグラム
     */
    public String usecaseMermaidText(JigMethodFinder jigMethodFinder, MethodRelations methodRelations) {
        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");

        // 基点からの呼び出し全部 + 直近の呼び出し元
        var filteredRelations = methodRelations.filterFromRecursive(this.declaration(), methodIdentifier -> false)
                .merge(methodRelations.filterTo(this.declaration()));

        Set<MethodIdentifier> resolved = new HashSet<>();

        // メソッドのスタイル
        filteredRelations.methodIdentifiers().forEach(methodIdentifier -> {
            // 自分は太字にする
            if (methodIdentifier.equals(declaration().identifier())) {
                resolved.add(methodIdentifier);
                mermaidText.add(usecaseMermaidNodeText());
                mermaidText.add("style %s font-weight:bold".formatted(this.htmlIdText()));
            } else {
                jigMethodFinder.find(methodIdentifier)
                        .ifPresent(jigMethod -> {
                            resolved.add(methodIdentifier);
                            if (jigMethod.remarkable()) {
                                // 出力対象のメソッドはusecase型＆クリックできるように
                                mermaidText.add(jigMethod.usecaseMermaidNodeText());
                                mermaidText.add("click %s \"#%s\"".formatted(jigMethod.htmlIdText(), jigMethod.htmlIdText()));
                            } else {
                                // remarkableでないものは普通の。privateメソッドなど該当。　
                                mermaidText.add(jigMethod.normalMermaidNodeText());
                            }
                        });
            }
        });

        Set<TypeIdentifier> others = new HashSet<>();

        Function<MethodDeclaration, Optional<String>> converter = methodDeclaration -> {
            if (resolved.contains(methodDeclaration.identifier())) {
                return Optional.of(methodDeclaration.htmlIdText());
            }
            // 解決できなかったものは関心が薄いとして、メソッドではなくクラスとして解釈し
            var typeIdentifier = methodDeclaration.declaringType();
            if (typeIdentifier.packageIdentifier().equals(declaration().declaringType().packageIdentifier())) {
                // 暫定的に同じパッケージのもののみ出力する
                // Serviceの場合に出力したいのはControllerやRepositoryになるので、気が向いたらなんとかする
                others.add(typeIdentifier);
                return Optional.of(typeIdentifier.htmlIdText());
            } else {
                return Optional.empty();
            }
        };
        mermaidText.add(filteredRelations.mermaidEdgeText(converter));

        // JigMethodにならないものはクラスノードとして出力する
        others.forEach(typeIdentifier ->
                mermaidText.add("%s[%s]:::others".formatted(typeIdentifier.htmlIdText(), typeIdentifier.asSimpleText())));

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
