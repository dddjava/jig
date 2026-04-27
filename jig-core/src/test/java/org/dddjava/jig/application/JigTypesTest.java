package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.junit.jupiter.api.Test;
import testing.JigTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@JigTest
class JigTypesTest {

    @Test
    void クラス可視性の判定(JigRepository jigRepository) {
        List<JigType> jigTypes = jigRepository.fetchJigTypes().list();

        assertAll(
                () -> assertEquals(JigTypeVisibility.PUBLIC,
                        findByFqnSuffix(jigTypes, "PublicType").visibility()),
                () -> assertEquals(JigTypeVisibility.PUBLIC,
                        findByFqnSuffix(jigTypes, "ProtectedType").visibility()),
                () -> assertEquals(JigTypeVisibility.NOT_PUBLIC,
                        findByFqnSuffix(jigTypes, "DefaultType").visibility()),
                () -> assertEquals(JigTypeVisibility.NOT_PUBLIC,
                        findByFqnSuffix(jigTypes, "PrivateType").visibility())
        );
    }

    private static JigType findByFqnSuffix(List<JigType> jigTypes, String suffix) {
        return jigTypes.stream()
                .filter(jigType -> jigType.id().fqn().endsWith(suffix))
                .findAny().orElseThrow(AssertionError::new);
    }

    /**
     * {@link stub.domain.model.annotation} の package-info.java にはアノテーションをつけている
     * アノテーションをつけると package-info.class がコンパイル時に生成される模様。
     */
    @Test
    void アノテーションつきのpackage_infoをドメインとして扱わない(JigService jigService, JigRepository jigRepository) {
        var typeIdentifier = TypeId.valueOf("stub.domain.model.annotation.package-info");

        var jigTypes = jigService.jigTypes(jigRepository);

        assertFalse(jigTypes.resolveJigType(typeIdentifier).isPresent(), "JigTypeに存在しない");

        var coreDomainJigTypes = jigService.coreDomainJigTypes(jigRepository);
        assertFalse(coreDomainJigTypes.jigTypes().contains(typeIdentifier), "domain coreには存在しない");
    }
}
