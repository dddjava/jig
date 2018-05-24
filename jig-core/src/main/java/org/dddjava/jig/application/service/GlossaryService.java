package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.sourcecode.*;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.domain.model.japanese.MethodJapaneseName;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;
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

    public JapaneseName japaneseNameFrom(PackageIdentifier packageIdentifier) {
        return repository.get(packageIdentifier);
    }

    public JapaneseName japaneseNameFrom(TypeIdentifier typeIdentifier) {
        return repository.get(typeIdentifier);
    }

    public JapaneseName japaneseNameFrom(MethodDeclaration methodDeclaration) {
        return repository.get(methodDeclaration);
    }

    public void importJapanese(PackageNameSources packageNameSources) {
        PackageNames packageNames = reader.readPackages(packageNameSources);
        packageNames.register(repository);
    }

    public void importJapanese(TypeNameSources typeNameSources) {
        TypeNames typeNames = reader.readTypes(typeNameSources);

        for (TypeJapaneseName typeJapaneseName : typeNames.list()) {
            repository.register(typeJapaneseName);
        }

        for (MethodJapaneseName methodJapaneseName : typeNames.methodList()) {
            repository.register(methodJapaneseName);
        }
    }
}
