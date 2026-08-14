package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.information.core.CoreDomainCondition;
import org.dddjava.jig.domain.model.information.core.CoreDomainJigTypes;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainDataAdapterTest {

    @Test
    void 解析対象が0件でもdomainPackageRootsとtypesのキーを持つJSONを書き出す() {
        var coreDomainJigTypes = new CoreDomainJigTypes(new JigTypes(List.of()), new CoreDomainCondition(Optional.empty()));

        String json = DomainDataAdapter.buildDomainJson(coreDomainJigTypes, coreDomainJigTypes.jigTypes(), new EnumModels(List.of()));

        // キーが欠けると domain.js が types を走査できずページ全体が描画されない
        assertTrue(json.contains("\"domainPackageRoots\":[]"), json);
        assertTrue(json.contains("\"types\":[]"), json);
    }
}
