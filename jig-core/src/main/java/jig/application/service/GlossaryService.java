package jig.application.service;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.*;
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

    public void importJapanese(PackageNameSources packageNameSources) {
        PackageNames packageNames = japaneseReader.readPackages(packageNameSources);
        packageNames.register(japaneseNameRepository);
    }

    public void importJapanese(TypeNameSources typeNameSources) {
        TypeNames typeNames = japaneseReader.readTypes(typeNameSources);
        typeNames.register(japaneseNameRepository);
    }
}
