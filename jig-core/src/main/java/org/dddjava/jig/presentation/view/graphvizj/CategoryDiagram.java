package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.interpret.alias.TypeAlias;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.Collections;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

/**
 * システムが持つEnumと列挙値の1枚絵
 */
public class CategoryDiagram implements DotTextEditor<CategoryAngles> {

    private final AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public CategoryDiagram(AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(CategoryAngles categoryAngles) {
        if (categoryAngles.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
        }

        String records = categoryAngles.list().stream()
                .map(categoryAngle -> {
                    String values = categoryAngle.constantsDeclarations().list().stream()
                            .map(StaticFieldDeclaration::nameText)
                            .collect(joining("</td></tr><tr><td border=\"1\">", "<tr><td border=\"1\">", "</td></tr>"));

                    TypeIdentifier typeIdentifier = categoryAngle.typeIdentifier();
                    return new Node(typeIdentifier.fullQualifiedName())
                            .html("<table border=\"0\" cellspacing=\"0\"><tr><td>" + typeNameOf(typeIdentifier) + "</td></tr>" + values + "</table>")
                            .asText();
                })
                .collect(joining("\n"));

        return new DotTexts(new StringJoiner("\n", "digraph {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.CategoryDiagram) + "\";")
                .add("layout=circo;")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(records)
                .toString());
    }

    private String typeNameOf(TypeIdentifier typeIdentifier) {
        TypeAlias typeAlias = aliasFinder.find(typeIdentifier);
        if (typeAlias.exists()) {
            return typeAlias.asText() + "<br/>" + typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText();
    }
}
