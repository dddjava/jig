package jig.analizer.plantuml;

import jig.domain.model.dependency.FullQualifiedName;
import jig.domain.model.dependency.JapaneseName;
import jig.domain.model.dependency.JapaneseNameRepository;
import jig.domain.model.dependency.ModelNameFormatter;

public class PlantUmlModelNameFormatter implements ModelNameFormatter {

    private String nameReplacePattern;
    private JapaneseNameRepository japaneseNameRepository;

    public PlantUmlModelNameFormatter(String nameReplacePattern, JapaneseNameRepository japaneseNameRepository) {
        this.nameReplacePattern = nameReplacePattern;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    @Override
    public String format(FullQualifiedName fullQualifiedName) {
        String value = fullQualifiedName.value()
                .replaceFirst(nameReplacePattern + "\\.", "")
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
