package org.dddjava.jig.domain.model.models.domains.collections;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;

import java.util.List;

/**
 * コレクション型一覧
 */
public class JigCollectionTypes {

    JigTypes jigTypes;
    ClassRelations classRelations;

    public JigCollectionTypes(JigTypes jigTypes, ClassRelations classRelations) {
        this.jigTypes = jigTypes;
        this.classRelations = classRelations;
    }

    public List<JigType> listJigType() {
        return jigTypes.listCollectionType();
    }

    public ClassRelations classRelations() {
        return classRelations;
    }
}
