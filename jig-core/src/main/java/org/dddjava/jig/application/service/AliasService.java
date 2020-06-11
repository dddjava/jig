package org.dddjava.jig.application.service;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.*;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigsource.file.text.AliasSource;
import org.dddjava.jig.domain.model.jigsource.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.jigsource.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.jigsource.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.jigsource.file.text.scalacode.ScalaSources;
import org.dddjava.jig.domain.model.jigsource.jigloader.SourceCodeAliasReader;
import org.springframework.stereotype.Service;

/**
 * 用語サービス
 */
@Service
public class AliasService {

    final SourceCodeAliasReader reader;
    final JigSourceRepository jigSourceRepository;
    final AliasRepository aliasRepository;

    public AliasService(SourceCodeAliasReader reader, JigSourceRepository jigSourceRepository, AliasRepository aliasRepository) {
        this.reader = reader;
        this.jigSourceRepository = jigSourceRepository;
        this.aliasRepository = aliasRepository;
    }

    /**
     * パッケージ別名を取得する
     */
    public PackageAlias packageAliasOf(PackageIdentifier packageIdentifier) {
        return aliasRepository.get(packageIdentifier);
    }

    /**
     * 型別名を取得する
     */
    public TypeAlias typeAliasOf(TypeIdentifier typeIdentifier) {
        return aliasRepository.get(typeIdentifier);
    }

    /**
     * メソッド別名を取得する
     */
    public MethodAlias methodAliasOf(MethodIdentifier methodIdentifier) {
        return aliasRepository.get(methodIdentifier);
    }

    /**
     * Javadocからパッケージ別名を取り込む
     */
    void loadPackageAliases(PackageInfoSources packageInfoSources) {
        PackageAliases packageAliases = reader.readPackages(packageInfoSources);
        for (PackageAlias packageAlias : packageAliases.list()) {
            jigSourceRepository.registerPackageAlias(packageAlias);
        }
    }

    /**
     * Javadocから別名を取り込む
     */
    void loadJavaAlias(JavaSources javaSources) {
        TypeAliases typeAliases = reader.readJavaSources(javaSources);
        loadTypeAlias(typeAliases);
    }

    /**
     * KtDocから別名を取り込む
     */
    void loadKotlinAlias(KotlinSources kotlinSources) {
        TypeAliases typeAliases = reader.readKotlinSources(kotlinSources);
        loadTypeAlias(typeAliases);
    }

    /**
     * ScalaDocから別名を取り込む
     */
    void loadScalaAlias(ScalaSources scalaSources) {
        TypeAliases typeAliases = reader.readScalaSources(scalaSources);
        loadTypeAlias(typeAliases);
    }

    /**
     * 型別名を取り込む
     */
    private void loadTypeAlias(TypeAliases typeAliases) {
        for (TypeAlias typeAlias : typeAliases.list()) {
            jigSourceRepository.registerTypeAlias(typeAlias);
        }

        for (MethodAlias methodAlias : typeAliases.methodList()) {
            jigSourceRepository.registerMethodAlias(methodAlias);
        }
    }

    /**
     * 別名を取り込む
     */
    public void loadAlias(AliasSource aliasSource) {
        loadJavaAlias(aliasSource.javaSources());
        loadKotlinAlias(aliasSource.kotlinSources());
        loadScalaAlias(aliasSource.scalaSources());
        loadPackageAliases(aliasSource.packageInfoSources());
    }
}
