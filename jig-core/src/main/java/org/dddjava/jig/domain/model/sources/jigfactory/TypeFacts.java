package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelation;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること一覧
 */
public class TypeFacts {

    private final List<JigTypeBuilder> list;
    private final EnumModels enumModels;

    public TypeFacts(List<JigTypeBuilder> list, EnumModels enumModels) {
        this.list = list;
        this.enumModels = enumModels;
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

    public EnumModels enumModels() {
        return enumModels;
    }

}
