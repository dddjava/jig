package jig.application.service;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.japanese.JapaneseReader;
import jig.domain.model.project.ProjectLocation;
import org.springframework.stereotype.Service;

@Service
public class GlossaryService {

    final JapaneseReader japaneseReader;
    final JapaneseNameRepository japaneseNameRepository;

    public GlossaryService(JapaneseReader japaneseReader, JapaneseNameRepository japaneseNameRepository) {
        this.japaneseReader = japaneseReader;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public JapaneseName japaneseNameFrom(TypeIdentifier typeIdentifier) {
        return japaneseNameRepository.get(typeIdentifier);
    }

    public void importJapanese(ProjectLocation projectLocation) {
        japaneseReader.readFrom(projectLocation);
    }
}
