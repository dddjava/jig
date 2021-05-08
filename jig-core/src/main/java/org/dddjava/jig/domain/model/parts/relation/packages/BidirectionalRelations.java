package org.dddjava.jig.domain.model.parts.relation.packages;

import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 相互依存一覧
 */
public class BidirectionalRelations {

    List<BidirectionalRelation> list;

    private BidirectionalRelations(List<BidirectionalRelation> list) {
        this.list = list;
    }

    public boolean notContains(PackageRelation packageRelation) {
        for (BidirectionalRelation bidirectionalRelation : list) {
            if (bidirectionalRelation.matches(packageRelation)) {
                return false;
            }
        }
        return true;
    }

    public static BidirectionalRelations from(PackageRelations packageRelations) {
        List<BidirectionalRelation> list = new ArrayList<>();
        BidirectionalRelations bidirectionalRelations = new BidirectionalRelations(list);

        for (PackageRelation packageRelation : packageRelations.list()) {
            for (PackageRelation right : packageRelations.list()) {
                if (packageRelation.from().equals(right.to()) && packageRelation.to().equals(right.from())) {
                    if (bidirectionalRelations.notContains(packageRelation)) {
                        list.add(new BidirectionalRelation(packageRelation));
                    }
                }
            }
        }
        return bidirectionalRelations;
    }

    public String dotRelationText() {
        StringJoiner stringJoiner = new StringJoiner("\n")
                .add("edge [color=red,dir=both,style=bold];");
        for (BidirectionalRelation bidirectionalRelation : list) {
            PackageIdentifier from = bidirectionalRelation.packageRelation.from;
            PackageIdentifier to = bidirectionalRelation.packageRelation.to;
            String line = '"' + from.asText() + '"' + " -> " + '"' + to.asText() + '"' + ';';
            stringJoiner.add(line);
        }
        return stringJoiner.toString();
    }

    public boolean none() {
        return list.isEmpty();
    }

    public List<BidirectionalRelation> list() {
        return list;
    }
}
