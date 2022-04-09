package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelation;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.sources.jigreader.TextSourceModel;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること一覧
 */
public class TypeFacts {
    private final List<JigTypeBuilder> list;

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
        List<ClassRelation> collector = new ArrayList<>();
        for (JigTypeBuilder jigTypeBuilder : list) {
            jigTypeBuilder.collectClassRelations(collector);
        }
        return classRelations = new ClassRelations(collector);
    }

    public void registerPackageAlias(PackageComment packageComment) {
        // TODO Packageを取得した際にくっつけて返せるようにする
    }

    public AliasRegisterResult registerTypeAlias(ClassComment classComment) {
        for (JigTypeBuilder jigTypeBuilder : list) {
            if (jigTypeBuilder.typeIdentifier().equals(classComment.typeIdentifier())) {
                jigTypeBuilder.registerTypeAlias(classComment);
                return AliasRegisterResult.成功;
            }
        }

        return AliasRegisterResult.紐付け対象なし;
    }

    public AliasRegisterResult registerMethodAlias(MethodComment methodComment) {
        for (JigTypeBuilder jigTypeBuilder : list) {
            MethodIdentifier methodIdentifier = methodComment.methodIdentifier();
            if (jigTypeBuilder.typeIdentifier().equals(methodIdentifier.declaringType())) {
                return jigTypeBuilder.registerMethodAlias(methodComment);
            }
        }

        return AliasRegisterResult.紐付け対象なし;
    }

    public void applyTextSource(TextSourceModel textSourceModel) {
        for (JigTypeBuilder jigTypeBuilder : list) {
            jigTypeBuilder.applyTextSource(textSourceModel);
        }
    }
}
