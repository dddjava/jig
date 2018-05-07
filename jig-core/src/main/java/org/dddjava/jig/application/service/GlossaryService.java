package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.*;
import org.dddjava.jig.domain.model.japanese.PackageNameSources;
import org.dddjava.jig.domain.model.japanese.TypeNameSources;
import org.springframework.stereotype.Service;

/**
 * 用語サービス
 */
@Service
public class GlossaryService {

    final JapaneseReader reader;
    final JapaneseNameRepository repository;

    public GlossaryService(JapaneseReader reader, JapaneseNameRepository repository) {
        this.reader = reader;
        this.repository = repository;
    }

    public JapaneseName japaneseNameFrom(TypeIdentifier typeIdentifier) {
        return repository.get(typeIdentifier);
    }

    public JapaneseName japaneseNameFrom(PackageIdentifier packageIdentifier) {
        return repository.get(packageIdentifier);
    }

    public void importJapanese(PackageNameSources packageNameSources) {
        PackageNames packageNames = reader.readPackages(packageNameSources);
        packageNames.register(repository);
    }

    public void importJapanese(TypeNameSources typeNameSources) {
        TypeNames typeNames = reader.readTypes(typeNameSources);
        typeNames.register(repository);
    }
}
