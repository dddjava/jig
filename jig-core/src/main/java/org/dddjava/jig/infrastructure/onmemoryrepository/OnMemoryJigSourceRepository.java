package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasRepository;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.PackageAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AliasRegisterResult;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;

@Repository
public class OnMemoryJigSourceRepository implements JigSourceRepository {
    static Logger logger = LoggerFactory.getLogger(OnMemoryJigSourceRepository.class);

    AliasRepository aliasRepository;
    TypeFacts typeFacts = new TypeFacts(Collections.emptyList());
    Sqls sqls;

    public OnMemoryJigSourceRepository(AliasRepository aliasRepository) {
        this.aliasRepository = aliasRepository;
    }

    @Override
    public void registerSqls(Sqls sqls) {
        this.sqls = sqls;
    }

    @Override
    public void registerTypeFact(TypeFacts typeFacts) {
        this.typeFacts = typeFacts;
    }

    @Override
    public void registerPackageAlias(PackageAlias packageAlias) {
        typeFacts.registerPackageAlias(packageAlias);
        aliasRepository.register(packageAlias);
    }

    @Override
    public AliasRegisterResult registerTypeAlias(TypeAlias typeAlias) {
        AliasRegisterResult aliasRegisterResult = typeFacts.registerTypeAlias(typeAlias);
        // TODO typeFactsに登録したものを使用するようになれば要らなくなるはず
        aliasRepository.register(typeAlias);

        if (aliasRegisterResult != AliasRegisterResult.成功) {
            logger.warn("{} のTypeAlias登録結果が {} です。処理は続行します。",
                    typeAlias.typeIdentifier().fullQualifiedName(), aliasRegisterResult);
        }
        return aliasRegisterResult;
    }

    @Override
    public void registerMethodAlias(MethodAlias methodAlias) {
        AliasRegisterResult aliasRegisterResult = typeFacts.registerMethodAlias(methodAlias);
        // TODO typeFactsに登録したものを使用するようになれば要らなくなるはず
        aliasRepository.register(methodAlias);

        if (aliasRegisterResult != AliasRegisterResult.成功) {
            logger.warn("{} のMethodAlias登録結果が {} です。処理は続行します。",
                    methodAlias.methodIdentifier().asText(), aliasRegisterResult);
        }
    }

    @Override
    public TypeFacts allTypeFacts() {
        return typeFacts;
    }

    @Override
    public Sqls sqls() {
        return sqls;
    }
}
