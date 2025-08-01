package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 関連
 */
public class RelationText {

    List<String> list;

    public RelationText() {
        this("");
    }

    public RelationText(String attribute) {
        list = new ArrayList<>();
        list.add(attribute);
    }

    public static RelationText fromPackageRelations(PackageRelations packageRelations) {
        RelationText relationText = new RelationText();
        for (PackageRelation packageRelation : packageRelations.listUnique()) {
            relationText.add(packageRelation.from(), packageRelation.to());
        }
        return relationText;
    }

    private void add(String from, String to) {
        // "hoge" -> "fuga";
        String line = '"' + from + '"' + " -> " + '"' + to + '"' + ';';
        list.add(line);
    }

    public String asText() {
        return String.join("\n", list);
    }

    public String asUniqueText() {
        return list.stream().sorted().distinct().collect(Collectors.joining("\n"));
    }

    public void add(PackageId from, PackageId to) {
        add(from.asText(), to.asText());
    }

    public void add(JigMethodId from, TypeId to) {
        add(from.value(), to.fullQualifiedName());
    }

    public void add(JigMethodId from, JigMethodId to) {
        add(from.value(), to.value());
    }
}
