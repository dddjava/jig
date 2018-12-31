package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.PackageInfoSources;
import org.dddjava.jig.domain.model.implementation.raw.JavaSources;
import org.dddjava.jig.domain.model.japanese.*;
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

    /**
     * パッケージ和名を取得する
     */
    public JapaneseName japaneseNameFrom(PackageIdentifier packageIdentifier) {
        return repository.get(packageIdentifier);
    }

    /**
     * 型和名を取得する
     */
    public JapaneseName japaneseNameFrom(TypeIdentifier typeIdentifier) {
        return repository.get(typeIdentifier);
    }

    /**
     * メソッド和名を取得する
     */
    public JapaneseName japaneseNameFrom(MethodIdentifier methodIdentifier) {
        return repository.get(methodIdentifier);
    }

    /**
     * Javadocから和名を取り込む
     */
    public void importJapanese(PackageInfoSources packageInfoSources) {
        PackageNames packageNames = reader.readPackages(packageInfoSources);
        packageNames.register(repository);
    }

    /**
     * Javadocから和名を取り込む
     */
    public void importJapanese(JavaSources javaSources) {
        TypeNames typeNames = reader.readTypes(javaSources);

        for (TypeJapaneseName typeJapaneseName : typeNames.list()) {
            repository.register(typeJapaneseName);
        }

        for (MethodJapaneseName methodJapaneseName : typeNames.methodList()) {
            repository.register(methodJapaneseName);
        }
    }
}
