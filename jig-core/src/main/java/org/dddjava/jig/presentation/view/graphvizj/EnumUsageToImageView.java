package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.categories.EnumAngle;
import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;

import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class EnumUsageToImageView extends GraphvizjView<EnumAngles> {

    private final JapaneseNameFinder japaneseNameFinder;

    public EnumUsageToImageView(JapaneseNameFinder japaneseNameFinder) {
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    protected String graphText(EnumAngles enumAngles) {
        String enumsText = enumAngles.list().stream()
                .map(enumAngle ->
                        Node.of(enumAngle.typeIdentifier())
                                .color("gold").asText())
                .collect(joining("\n"));

        RelationText relationText = new RelationText();
        for (EnumAngle enumAngle : enumAngles.list()) {
            for (TypeIdentifier userType : enumAngle.userTypeIdentifiers().list()) {
                relationText.add(userType, enumAngle.typeIdentifier());
            }
        }

        String userLabel = Stream.concat(
                enumAngles.list().stream().map(EnumAngle::typeIdentifier),
                enumAngles.list().stream().flatMap(enumAngle -> enumAngle.userTypeIdentifiers().list().stream()))
                .distinct()
                .map(typeIdentifier ->
                        Node.of(typeIdentifier)
                                .label(appendJapaneseName(typeIdentifier)).asText())
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
