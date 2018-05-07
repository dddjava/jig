package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.domain.model.angle.EnumAngles;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.presentation.view.AbstractLocalView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

public class EnumUsageToImageView extends AbstractLocalView {

    private final EnumAngles enumAngles;

    public EnumUsageToImageView(EnumAngles enumAngles) {
        super("jig-diagram_enum-usage.png");
        this.enumAngles = enumAngles;
    }

    @Override
    protected void write(OutputStream outputStream) throws IOException {
        TypeIdentifierFormatter doubleQuote = value -> "\"" + value + "\"";

        String labelsText = enumAngles.list().stream()
                .map(enumAngle -> {
                    TypeIdentifier typeIdentifier = enumAngle.typeIdentifier();
                    return typeIdentifier.format(doubleQuote);
                }).collect(joining("\n"));

        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,color=lightgoldenrod];")
                .add(labelsText)
                .toString();
        Graphviz.fromString(graphText)
                .render(Format.PNG)
                .toOutputStream(outputStream);
    }
}
