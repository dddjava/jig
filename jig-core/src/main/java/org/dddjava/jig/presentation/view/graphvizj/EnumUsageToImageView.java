package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.angle.EnumAngle;
import org.dddjava.jig.domain.model.angle.EnumAngles;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.presentation.view.AbstractLocalView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class EnumUsageToImageView extends AbstractLocalView {

    private final EnumAngles enumAngles;
    private final GlossaryService glossaryService;

    public EnumUsageToImageView(EnumAngles enumAngles, GlossaryService glossaryService) {
        super("jig-diagram_enum-usage.png");
        this.enumAngles = enumAngles;
        this.glossaryService = glossaryService;
    }

    @Override
    protected void write(OutputStream outputStream) throws IOException {
        TypeIdentifierFormatter doubleQuote = value -> "\"" + value + "\"";

        String enumsText = enumAngles.list().stream()
                .map(enumAngle -> {
                    TypeIdentifier typeIdentifier = enumAngle.typeIdentifier();
                    String enumText = typeIdentifier.format(doubleQuote);
                    return enumText + "[color=gold]";
                }).collect(joining(";\n"));

        String relationText = enumAngles.list().stream()
                .flatMap(enumAngle -> {
                    TypeIdentifier typeIdentifier = enumAngle.typeIdentifier();
                    String enumTypeText = typeIdentifier.format(doubleQuote);

                    return enumAngle.userTypeIdentifiers().list()
                            .stream()
                            .map(userType -> {
                                String userTypeText = userType.format(doubleQuote);

                                return String.format("%s -> %s;",
                                        userTypeText, enumTypeText);
                            });
                }).collect(joining("\n"));


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
                .add(relationText)
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
