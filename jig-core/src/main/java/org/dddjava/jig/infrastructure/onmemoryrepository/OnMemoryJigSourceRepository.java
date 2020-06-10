package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasRepository;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.PackageAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.springframework.stereotype.Repository;

import java.util.Collections;

@Repository
public class OnMemoryJigSourceRepository implements JigSourceRepository {

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
    public void registerTypeAlias(TypeAlias typeAlias) {
        typeFacts.registerTypeAlias(typeAlias);
        aliasRepository.register(typeAlias);
    }

    @Override
    public void registerMethodAlias(MethodAlias methodAlias) {
        typeFacts.registerMethodAlias(methodAlias);
        aliasRepository.register(methodAlias);
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
