package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.*;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.javafile.JavaSources;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSources;
import org.springframework.stereotype.Service;

/**
 * 用語サービス
 */
@Service
public class GlossaryService {

    final AliasReader reader;
    final AliasRepository repository;

    public GlossaryService(AliasReader reader, AliasRepository repository) {
        this.reader = reader;
        this.repository = repository;
    }

    /**
     * パッケージ別名を取得する
     */
    public Alias japaneseNameFrom(PackageIdentifier packageIdentifier) {
        return repository.get(packageIdentifier);
    }

    /**
     * 型別名を取得する
     */
    public Alias japaneseNameFrom(TypeIdentifier typeIdentifier) {
        return repository.get(typeIdentifier);
    }

    /**
     * メソッド別名を取得する
     */
    public Alias japaneseNameFrom(MethodIdentifier methodIdentifier) {
        return repository.get(methodIdentifier);
    }

    /**
     * Javadocから別名を取り込む
     */
    public void importJapanese(PackageInfoSources packageInfoSources) {
        PackageNames packageNames = reader.readPackages(packageInfoSources);
        packageNames.register(repository);
    }

    /**
     * Javadocから別名を取り込む
     */
    public void importJapanese(JavaSources javaSources) {
        TypeNames typeNames = reader.readTypes(javaSources);

        for (TypeAlias typeAlias : typeNames.list()) {
            repository.register(typeAlias);
        }

        for (MethodAlias methodAlias : typeNames.methodList()) {
            repository.register(methodAlias);
        }
    }
}
