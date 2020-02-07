package org.dddjava.jig.domain.model.jigmodel.relation;

import org.dddjava.jig.domain.model.jigdocument.RelationText;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelation;

import java.util.List;

public class RoundingPackageRelations {

    List<PackageRelation> list;

    public RoundingPackageRelations(List<PackageRelation> list) {
        this.list = list;
    }

    public RelationText toRelationText() {
        RelationText relationText = new RelationText();

        for (PackageRelation packageRelation : list) {
            relationText.add(packageRelation.from(), packageRelation.to());
        }
        return relationText;
    }

    public boolean worthless() {
        return list.isEmpty();
    }
}
