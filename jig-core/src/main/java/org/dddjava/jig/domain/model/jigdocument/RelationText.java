package org.dddjava.jig.domain.model.jigdocument;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;

import java.util.StringJoiner;

/**
 * 関連
 */
public class RelationText {

    StringJoiner stringJoiner;

    public RelationText() {
        this("");
    }

    public RelationText(String attribute) {
        this.stringJoiner = new StringJoiner("\n");
        stringJoiner.add(attribute);
    }

    public static RelationText fromClassRelation(ClassRelations relations) {
        RelationText relationText = new RelationText();
        for (ClassRelation classRelation : relations.distinctList()) {
            relationText.add(classRelation.from(), classRelation.to());
        }
        return relationText;
    }

    private void add(String from, String to) {
        // "hoge" -> "fuga";
        String line = '"' + from + '"' + " -> " + '"' + to + '"' + ';';
        stringJoiner.add(line);
    }

    public String asText() {
        return stringJoiner.toString();
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
