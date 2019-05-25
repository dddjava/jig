package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.*;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.KotlinSources;
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
        PackageAliases packageAliases = reader.readPackages(packageInfoSources);
        packageAliases.register(repository);
    }

    /**
     * Javadocから別名を取り込む
     */
    public void importJapanese(JavaSources javaSources) {
        TypeAliases typeAliases = reader.readTypes(javaSources);
        importJapanese(typeAliases);
    }

    /**
     * KtDocから別名を取り込む
     */
    public void importJapanese(KotlinSources kotlinSources) {
        TypeAliases typeAliases = reader.readTypes(kotlinSources);
        importJapanese(typeAliases);
    }

    private void importJapanese(TypeAliases typeAliases) {
        for (TypeAlias typeAlias : typeAliases.list()) {
            repository.register(typeAlias);
        }

        for (MethodAlias methodAlias : typeAliases.methodList()) {
            repository.register(methodAlias);
        }
    }
}
