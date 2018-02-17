package jig.analizer.plantuml;

import jig.analizer.dependency.Model;
import jig.analizer.dependency.ModelFormatter;

import java.util.stream.Collectors;

public class PlantUmlModelFormatter implements ModelFormatter {

    private final PlantUmlModelNameFormatter modelNameFormatter;

    public PlantUmlModelFormatter(PlantUmlModelNameFormatter modelNameFormatter) {
        this.modelNameFormatter = modelNameFormatter;
    }

    @Override
    public String format(Model model) {
        return model.dependency().list().stream()
                .map(dependency -> "\"" + model.format(modelNameFormatter) + "\" ..> \"" + dependency.format(modelNameFormatter) + "\"")
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
