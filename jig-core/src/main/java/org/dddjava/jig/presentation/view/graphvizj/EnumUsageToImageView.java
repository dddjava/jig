package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.categories.EnumAngle;
import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.presentation.view.JigView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class EnumUsageToImageView implements JigView<EnumAngles> {

    private final GlossaryService glossaryService;

    public EnumUsageToImageView(GlossaryService glossaryService) {
        this.glossaryService = glossaryService;
    }

    @Override
    public void render(EnumAngles enumAngles, OutputStream outputStream) throws IOException {
        TypeIdentifierFormatter doubleQuote = value -> "\"" + value + "\"";

        String enumsText = enumAngles.list().stream()
                .map(enumAngle -> {
                    TypeIdentifier typeIdentifier = enumAngle.typeIdentifier();
                    String enumText = typeIdentifier.format(doubleQuote);
                    return enumText + "[color=gold]";
                }).collect(joining(";\n"));

        RelationText relationText = new RelationText();
        for (EnumAngle enumAngle : enumAngles.list()) {
            for (TypeIdentifier userType : enumAngle.userTypeIdentifiers().list()) {
                relationText.add(
                        userType.fullQualifiedName(),
                        enumAngle.typeIdentifier().fullQualifiedName());
            }
        }

        String userLabel = Stream.concat(
                enumAngles.list().stream().map(EnumAngle::typeIdentifier),
                enumAngles.list().stream().flatMap(enumAngle -> enumAngle.userTypeIdentifiers().list().stream()))
                .distinct()
                .map(typeIdentifier -> {
                    String userTypeText = typeIdentifier.format(doubleQuote);
                    return String.format("%s[label=\"%s\"];",
                            userTypeText, appendJapaneseName(typeIdentifier));
                }).collect(joining("\n"));

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
        JapaneseName japaneseName = glossaryService.japaneseNameFrom(typeIdentifier);
        if (japaneseName.value().equals("")) {
            return typeIdentifier.asSimpleText();
        }
        return japaneseName.summarySentence() + "\\n" + typeIdentifier.asSimpleText();
    }
}
