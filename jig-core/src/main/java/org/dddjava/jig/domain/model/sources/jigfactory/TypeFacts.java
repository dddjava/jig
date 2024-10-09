package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelation;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること一覧
 */
public class TypeFacts {
    private static final Logger logger = LoggerFactory.getLogger(TypeFacts.class);

    private final List<JigTypeBuilder> list;
    private EnumModels enumModels = new EnumModels(List.of());

    public TypeFacts(List<JigTypeBuilder> list) {
        this.list = list;
    }

    private ClassRelations classRelations;
    private MethodRelations methodRelations;

    private JigTypes jigTypes;

    public JigTypes jigTypes() {
        if (jigTypes != null) return jigTypes;
        jigTypes = new JigTypes(list.stream().map(JigTypeBuilder::build).collect(toList()));
        return jigTypes;
    }

    public synchronized MethodRelations toMethodRelations() {
        if (methodRelations != null) {
            return methodRelations;
        }
        List<MethodRelation> collector = new ArrayList<>();
        for (JigTypeBuilder jigTypeBuilder : list) {
            for (JigMethodBuilder jigMethodBuilder : jigTypeBuilder.allMethodFacts()) {
                jigMethodBuilder.collectUsingMethodRelations(collector);
            }
        }
        return methodRelations = new MethodRelations(collector);
    }

    public synchronized ClassRelations toClassRelations() {
        if (classRelations != null) {
            return classRelations;
        }

        this.classRelations = new ClassRelations(jigTypes().list().stream()
                .flatMap(jigType ->
                        jigType.usingTypes().list().stream().map(usingType ->
                                new ClassRelation(jigType.identifier(), usingType)))
                .filter(classRelation -> !classRelation.selfRelation())
                .toList());
        return this.classRelations;
    }

    public void applyTextSource(TextSourceModel textSourceModel) {
        for (JigTypeBuilder jigTypeBuilder : list) {
            jigTypeBuilder.applyTextSource(textSourceModel);
        }

        for (ClassComment classComment : textSourceModel.classCommentList()) {
            registerTypeAlias(classComment);
        }
        for (MethodComment methodComment : textSourceModel.methodCommentList()) {
            registerMethodAlias(methodComment);
        }

        this.enumModels = textSourceModel.enumModels();
    }

    public EnumModels enumModels() {
        return enumModels;
    }

    private void registerTypeAlias(ClassComment classComment) {
        for (JigTypeBuilder jigTypeBuilder : list) {
            if (jigTypeBuilder.typeIdentifier().equals(classComment.typeIdentifier())) {
                jigTypeBuilder.registerTypeAlias(classComment);
                return;
            }
        }

        logger.warn("{} のコメント追加に失敗しました。javaファイルに対応するclassファイルが見つかりません。コンパイルが正常に行われていない可能性があります。処理は続行します。",
                classComment.typeIdentifier());
    }

    private void registerMethodAlias(MethodComment methodComment) {
        for (JigTypeBuilder jigTypeBuilder : list) {
            MethodIdentifier methodIdentifier = methodComment.methodIdentifier();
            if (jigTypeBuilder.typeIdentifier().equals(methodIdentifier.declaringType())) {
                if (jigTypeBuilder.registerMethodAlias(methodComment)) {
                    return;
                }
            }
        }

        logger.warn("{} のコメント追加に失敗しました。javaファイルとclassファイルがアンマッチの可能性があります。処理は続行します。",
                methodComment.methodIdentifier().asText());
    }
}
