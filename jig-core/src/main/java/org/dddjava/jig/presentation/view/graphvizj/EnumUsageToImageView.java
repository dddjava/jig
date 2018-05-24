package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.domain.model.categories.EnumAngle;
import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;
import org.dddjava.jig.presentation.view.JigView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class EnumUsageToImageView implements JigView<EnumAngles> {

    private final JapaneseNameFinder japaneseNameFinder;

    public EnumUsageToImageView(JapaneseNameFinder japaneseNameFinder) {
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    public void render(EnumAngles enumAngles, OutputStream outputStream) throws IOException {
        String enumsText = enumAngles.list().stream()
                .map(enumAngle ->
                        IndividualAttribute.of(enumAngle.typeIdentifier())
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
                        IndividualAttribute.of(typeIdentifier)
                                .label(appendJapaneseName(typeIdentifier)).asText())
                .collect(joining("\n"));

        String legendText = new StringJoiner("\n", "subgraph cluster_legend {", "}")
                .add("label=凡例;")
                .add("enum[color=gold];")
                .add("enum以外[color=lightgoldenrodyellow];")
                .toString();

        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,color=lightgoldenrodyellow];")
                .add(legendText)
                .add(enumsText)
                .add(relationText.asText())
                .add(userLabel)
                .toString();

        Graphviz.fromString(graphText)
                .render(Format.PNG)
                .toOutputStream(outputStream);
    }

    private String appendJapaneseName(TypeIdentifier typeIdentifier) {
        TypeJapaneseName typeJapaneseName = japaneseNameFinder.find(typeIdentifier);
        if (typeJapaneseName.exists()) {
            return typeJapaneseName.japaneseName().summarySentence() + "\\n" + typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText();
    }
}
