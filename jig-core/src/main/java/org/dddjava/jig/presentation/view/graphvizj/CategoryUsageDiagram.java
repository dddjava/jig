package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.categories.CategoryAngle;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.implementation.analyzed.japanese.TypeJapaneseName;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.Collections;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

public class CategoryUsageDiagram implements DotTextEditor<CategoryAngles> {

    private final JapaneseNameFinder japaneseNameFinder;
    JigDocumentContext jigDocumentContext;

    public CategoryUsageDiagram(JapaneseNameFinder japaneseNameFinder) {
        this.japaneseNameFinder = japaneseNameFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(CategoryAngles categoryAngles) {
        if (categoryAngles.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
        }

        TypeIdentifiers enumTypes = categoryAngles.typeIdentifiers();

        String enumsText = enumTypes.list().stream()
                .map(enumType -> Node.of(enumType)
                        .color("gold")
                        .label(appendJapaneseName(enumType))
                        .asText())
                .collect(joining("\n"));

        RelationText relationText = new RelationText();
        for (CategoryAngle categoryAngle : categoryAngles.list()) {
            for (TypeIdentifier userType : categoryAngle.userTypeIdentifiers().list()) {
                relationText.add(userType, categoryAngle.typeIdentifier());
            }
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
                .add("label=" + jigDocumentContext.label("legend") + ";")
                .add(jigDocumentContext.label("enum") + "[color=gold];")
                .add(jigDocumentContext.label("not_enum") + "[color=lightgoldenrodyellow];")
                .toString();

        return new DotTexts(new StringJoiner("\n", "digraph JIG {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.CategoryUsageDiagram) + "\";")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,color=lightgoldenrodyellow];")
                .add(legendText)
                .add(enumsText)
                .add(relationText.asText())
                .add(userLabel)
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
