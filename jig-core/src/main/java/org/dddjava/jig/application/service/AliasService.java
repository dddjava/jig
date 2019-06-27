package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.*;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.textfile.AliasSource;
import org.dddjava.jig.domain.model.implementation.source.code.javacode.JavaSources;
import org.dddjava.jig.domain.model.implementation.source.code.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.implementation.source.code.kotlincode.KotlinSources;
import org.springframework.stereotype.Service;

/**
 * 用語サービス
 */
@Service
public class AliasService {

    final SourceCodeJapaneseReader reader;
    final AliasRepository repository;

    public AliasService(SourceCodeJapaneseReader reader, AliasRepository repository) {
        this.reader = reader;
        this.repository = repository;
    }

    /**
     * パッケージ別名を取得する
     */
    public PackageAlias packageAliasOf(PackageIdentifier packageIdentifier) {
        return repository.get(packageIdentifier);
    }

    /**
     * 型別名を取得する
     */
    public TypeAlias typeAliasOf(TypeIdentifier typeIdentifier) {
        return repository.get(typeIdentifier);
    }

    /**
     * メソッド別名を取得する
     */
    public MethodAlias methodAliasOf(MethodIdentifier methodIdentifier) {
        return repository.get(methodIdentifier);
    }

    /**
     * Javadocから別名を取り込む
     */
    void loadPackageAliases(PackageInfoSources packageInfoSources) {
        PackageAliases packageAliases = reader.readPackages(packageInfoSources);
        packageAliases.register(repository);
    }

    /**
     * Javadocから別名を取り込む
     */
    void loadAliases(JavaSources javaSources) {
        TypeAliases typeAliases = reader.readJavaSources(javaSources);
        loadAliases(typeAliases);
    }

    /**
     * KtDocから別名を取り込む
     */
    void loadAliases(KotlinSources kotlinSources) {
        TypeAliases typeAliases = reader.readKotlinSources(kotlinSources);
        loadAliases(typeAliases);
    }

    private void loadAliases(TypeAliases typeAliases) {
        for (TypeAlias typeAlias : typeAliases.list()) {
            repository.register(typeAlias);
        }

        for (MethodAlias methodAlias : typeAliases.methodList()) {
            repository.register(methodAlias);
        }
    }

    public void loadAliases(AliasSource aliasSource) {
        loadAliases(aliasSource.javaSources());
        loadAliases(aliasSource.kotlinSources());
        loadPackageAliases(aliasSource.packageInfoSources());
    }
}
