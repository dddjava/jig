package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.businessrules.categories.CategoryAngles;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;

import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

/**
 * システムが持つEnumと列挙値の1枚絵
 */
public class CategoryDiagram implements DotTextEditor<CategoryAngles> {

    private final JapaneseNameFinder japaneseNameFinder;

    public CategoryDiagram(JapaneseNameFinder japaneseNameFinder) {
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    public DotTexts edit(CategoryAngles categoryAngles) {
        String records = categoryAngles.list().stream()
                .map(categoryAngle -> {
                    String values = categoryAngle.constantsDeclarations().list().stream()
                            .map(StaticFieldDeclaration::nameText)
                            .collect(joining("|", "{", "}"));

                    TypeIdentifier typeIdentifier = categoryAngle.typeIdentifier();
                    return new Node(typeIdentifier.fullQualifiedName())
                            .label(appendJapaneseName(typeIdentifier) + "|{ |" + values + "}")
                            .asText();
                })
                .collect(joining("\n"));

        return new DotTexts(new StringJoiner("\n", "digraph {", "}")
                .add("layout=circo;")
                .add("rankdir=LR;")
                .add("node [shape=Mrecord;style=filled;fillcolor=lightyellow];")
                .add(records)
                .toString());
    }

    private String appendJapaneseName(TypeIdentifier typeIdentifier) {
        TypeJapaneseName typeJapaneseName = japaneseNameFinder.find(typeIdentifier);
        if (typeJapaneseName.exists()) {
            return typeJapaneseName.japaneseName().summarySentence() + "\\n" + typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText();
    }
}
