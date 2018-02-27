package jig.analizer.plantuml;

import jig.domain.model.thing.Thing;
import jig.domain.model.thing.ThingFormatter;

import java.util.stream.Collectors;

public class PlantUmlThingFormatter implements ThingFormatter {

    private final PlantUmlNameFormatter modelNameFormatter;

    public PlantUmlThingFormatter(PlantUmlNameFormatter modelNameFormatter) {
        this.modelNameFormatter = modelNameFormatter;
    }

    @Override
    public String header() {
        return "@startuml\n" +
                "hide members\n" +
                "hide circle\n";
    }

    @Override
    public String format(Thing thing) {
        return thing.dependency().list().stream()
                .map(dependency -> "\"" + thing.format(modelNameFormatter) + "\" ..> \"" + dependency.format(modelNameFormatter) + "\"")
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Override
    public String footer() {
        return "\n@enduml";
    }
}
