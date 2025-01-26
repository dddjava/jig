package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelation;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
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

    public static RelationText fromClassRelation(ClassRelations relations) {
        RelationText relationText = new RelationText();
        for (ClassRelation classRelation : relations.distinctList()) {
            relationText.add(classRelation.from(), classRelation.to());
        }
        return relationText;
    }

    public static RelationText fromPackageRelations(PackageRelations packageRelations) {
        RelationText relationText = new RelationText();
        for (PackageRelation packageRelation : packageRelations.list()) {
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

    public void add(PackageIdentifier from, PackageIdentifier to) {
        add(from.asText(), to.asText());
    }

    public void add(TypeIdentifier from, TypeIdentifier to) {
        add(from.fullQualifiedName(), to.fullQualifiedName());
    }

    public void add(MethodDeclaration from, MethodDeclaration to) {
        add(from.asFullNameText(), to.asFullNameText());
    }

    public void add(TypeIdentifier from, String to) {
        add(from.fullQualifiedName(), to);
    }

    public void add(MethodDeclaration from, TypeIdentifier to) {
        add(from.asFullNameText(), to.fullQualifiedName());
    }
}
