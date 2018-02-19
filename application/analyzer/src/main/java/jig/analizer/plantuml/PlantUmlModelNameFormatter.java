package jig.analizer.plantuml;

import jig.analizer.dependency.FullQualifiedName;
import jig.analizer.dependency.JapaneseName;
import jig.analizer.dependency.JapaneseNameRepository;
import jig.analizer.dependency.ModelNameFormatter;

public class PlantUmlModelNameFormatter implements ModelNameFormatter {

    private String targetPattern;
    private JapaneseNameRepository japaneseNameRepository;

    public PlantUmlModelNameFormatter(String targetPattern, JapaneseNameRepository japaneseNameRepository) {
        this.targetPattern = targetPattern;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    @Override
    public String format(FullQualifiedName fullQualifiedName) {
        String value = fullQualifiedName.value()
                .replaceFirst(targetPattern, "$1")
                .replaceAll("\\.", "/");
        if (japaneseNameRepository.exists(fullQualifiedName)) {
            JapaneseName japaneseName = japaneseNameRepository.get(fullQualifiedName);
            String name = japaneseName.value();
            String s = name.replaceAll("\r\n|[\n\r\u2028\u2029\u0085]", "\\\\n");
            return s + "\\n" + value;
        }
        return value;
    }
}
