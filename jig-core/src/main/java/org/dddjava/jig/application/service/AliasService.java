package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasRepository;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.PackageAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
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

    /**
     * メソッド別名を取得する
     */
    public MethodAlias methodAliasOf(MethodIdentifier methodIdentifier) {
        return aliasRepository.get(methodIdentifier);
    }
}
