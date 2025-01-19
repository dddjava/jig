package org.dddjava.jig.domain.model.information.domains.businessrules;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.package_.PackageJigTypes;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * ビジネスルール一覧
 */
public class BusinessRules {

    JigTypes jigTypes;

    private ClassRelations internalClassRelations;

    public BusinessRules(JigTypes jigTypes, ClassRelations classRelations) {
        this.jigTypes = jigTypes;

        this.internalClassRelations = classRelations.list().stream()
                .filter(classRelation -> jigTypes.contains(classRelation.from()) && jigTypes.contains(classRelation.to()))
                .collect(collectingAndThen(toList(), ClassRelations::new))
                .distinct();
    }

    public List<JigType> list() {
        return jigTypes.list();
    }

    public boolean empty() {
        return jigTypes.empty();
    }

    public TypeIdentifiers identifiers() {
        return jigTypes.typeIdentifiers();
    }

    public List<PackageJigTypes> listPackages() {
        return jigTypes.listPackages();
    }

    public ClassRelations internalClassRelations() {
        return internalClassRelations;
    }

    public JigTypes jigTypes() {
        return jigTypes;
    }
}
