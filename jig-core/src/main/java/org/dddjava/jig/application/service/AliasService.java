package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.parts.alias.AliasRepository;
import org.dddjava.jig.domain.model.parts.alias.PackageAlias;
import org.dddjava.jig.domain.model.parts.alias.TypeAlias;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;
import org.springframework.stereotype.Service;

/**
 * 用語サービス
 */
@Service
public class AliasService {

    final AliasRepository aliasRepository;

    public AliasService(AliasRepository aliasRepository) {
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
}
