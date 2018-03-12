package jig.infrastructure.plantuml;

import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.diagram.DiagramSource;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.Relations;
import jig.domain.model.thing.NameFormatter;

import java.util.stream.Collectors;

public class PlantumlDiagramConverter implements DiagramConverter {

    NameFormatter nameFormatter;

    public PlantumlDiagramConverter(NameFormatter nameFormatter) {
        this.nameFormatter = nameFormatter;
    }

    @Override
    public DiagramSource toDiagramSource(Relations relations) {
        String source = "@startuml\n" +
                "hide members\n" +
                "hide circle\n" +
                relations.list().stream()
                        .map(this::relationToString)
                        .collect(Collectors.joining(System.lineSeparator())) +
                "\n" +
                "footer %date%\n" +
                "@enduml";
        return new DiagramSource(source);
    }

    String relationToString(Relation relation) {
        return String.format("\"%s\" ..> \"%s\"",
                nameFormatter.format(relation.from().name()),
                nameFormatter.format(relation.to().name())
        );
    }
}
