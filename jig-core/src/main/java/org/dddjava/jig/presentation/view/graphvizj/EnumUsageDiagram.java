package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.categories.CategoryAngle;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;

import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

public class EnumUsageDiagram implements DotTextEditor<CategoryAngles> {

    private final JapaneseNameFinder japaneseNameFinder;

    public EnumUsageDiagram(JapaneseNameFinder japaneseNameFinder) {
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    public String edit(CategoryAngles categoryAngles) {

        TypeIdentifiers enumTypes = categoryAngles.typeIdentifiers();

        String enumsText = enumTypes.list().stream()
                .map(enumType -> Node.of(enumType)
                        .color("gold")
                        .label(appendJapaneseName(enumType))
                        .asText())
                .collect(joining("\n"));

        String enumValuesText = categoryAngles.list().stream()
                .map(categoryAngle -> {
                    String values = categoryAngle.constantsDeclarations().list().stream()
                            .map(StaticFieldDeclaration::nameText)
                            .collect(joining("|"));
                    return new Node(categoryAngle.typeIdentifier().fullQualifiedName() + "_values")
                            .label(values)
                            .asText();
                })
                .collect(joining("\n"));

        RelationText relationText = new RelationText();
        for (CategoryAngle categoryAngle : categoryAngles.list()) {
            for (TypeIdentifier userType : categoryAngle.userTypeIdentifiers().list()) {
                relationText.add(userType, categoryAngle.typeIdentifier());
            }
        }

        RelationText valuesRelationText = new RelationText("edge [arrowhead=none,style=dotted];");
        for (CategoryAngle categoryAngle : categoryAngles.list()) {
            // enumの定数列挙へのリンク
            valuesRelationText.add(categoryAngle.typeIdentifier(), categoryAngle.typeIdentifier().fullQualifiedName() + "_values");
        }

        String userLabel = categoryAngles.list().stream().flatMap(categoryAngle -> categoryAngle.userTypeIdentifiers().list().stream())
                // 重複を除く
                .distinct()
                // enumを除く
                .filter(typeIdentifier -> !enumTypes.contains(typeIdentifier))
                .map(typeIdentifier ->
                        Node.of(typeIdentifier)
                                .label(appendJapaneseName(typeIdentifier))
                                .asText())
                .collect(joining("\n"));

        String legendText = new StringJoiner("\n", "subgraph cluster_legend {", "}")
                .add("label=凡例;")
                .add("enum[color=gold];")
                .add("enum以外[color=lightgoldenrodyellow];")
                .toString();

        return new StringJoiner("\n", "digraph JIG {", "}")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,color=lightgoldenrodyellow];")
                .add(legendText)
                .add(enumsText)
                .add(relationText.asText())
                .add(userLabel)
                .add("node [shape=record,style=bold,color=black,fontsize=10];")
                .add(enumValuesText)
                .add(valuesRelationText.asText())
                .toString();
    }

    private String appendJapaneseName(TypeIdentifier typeIdentifier) {
        TypeJapaneseName typeJapaneseName = japaneseNameFinder.find(typeIdentifier);
        if (typeJapaneseName.exists()) {
            return typeJapaneseName.japaneseName().summarySentence() + "\\n" + typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText();
    }
}
