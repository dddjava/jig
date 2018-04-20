package jig.application.service;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.JapaneseNameRepository;
import org.springframework.stereotype.Service;

@Service
public class GlossaryService {

    final JapaneseNameRepository japaneseNameRepository;

    public GlossaryService(JapaneseNameRepository japaneseNameRepository) {
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public JapaneseName japaneseNameFrom(TypeIdentifier typeIdentifier) {
        return japaneseNameRepository.get(typeIdentifier);
    }
}
