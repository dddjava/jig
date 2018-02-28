package jig.infrastructure.plantuml;

import jig.domain.model.tag.JapaneseName;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.NameFormatter;

public class PlantumlNameFormatter implements NameFormatter {

    private String nameReplacePattern = "";
    private JapaneseNameDictionary japaneseNameDictionary;

    public PlantumlNameFormatter(JapaneseNameDictionary japaneseNameDictionary) {
        this.japaneseNameDictionary = japaneseNameDictionary;
    }

    @Override
    public String format(Name fullQualifiedName) {
        String value = fullQualifiedName.value()
                .replaceFirst(nameReplacePattern, "")
                .replaceAll("\\.", "/");
        if (japaneseNameDictionary.exists(fullQualifiedName)) {
            JapaneseName japaneseName = japaneseNameDictionary.get(fullQualifiedName);
            String name = japaneseName.value();
            String s = name.replaceAll("\r\n|[\n\r\u2028\u2029\u0085]", "\\\\n");
            return s + "\\n" + value;
        }
        return value;
    }

    public void setNameShortenPattern(String pattern) {
        nameReplacePattern = pattern;
    }
}
