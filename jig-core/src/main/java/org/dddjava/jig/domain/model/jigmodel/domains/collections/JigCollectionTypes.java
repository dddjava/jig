package org.dddjava.jig.domain.model.jigmodel.domains.collections;

import org.dddjava.jig.domain.model.jigmodel.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;

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
