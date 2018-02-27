package jig.analizer.plantuml;

import jig.domain.model.thing.Name;
import jig.domain.model.tag.JapaneseName;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.domain.model.thing.NameFormatter;

public class PlantUmlNameFormatter implements NameFormatter {

    private String nameReplacePattern;
    private JapaneseNameDictionary japaneseNameDictionary;

    public PlantUmlNameFormatter(String nameReplacePattern, JapaneseNameDictionary japaneseNameDictionary) {
        this.nameReplacePattern = nameReplacePattern;
        this.japaneseNameDictionary = japaneseNameDictionary;
    }

    @Override
    public String format(Name fullQualifiedName) {
        String value = fullQualifiedName.value()
                .replaceFirst(nameReplacePattern + "\\.", "")
                .replaceAll("\\.", "/");
        if (japaneseNameDictionary.exists(fullQualifiedName)) {
            JapaneseName japaneseName = japaneseNameDictionary.get(fullQualifiedName);
            String name = japaneseName.value();
            String s = name.replaceAll("\r\n|[\n\r\u2028\u2029\u0085]", "\\\\n");
            return s + "\\n" + value;
        }
        return value;
    }
}
