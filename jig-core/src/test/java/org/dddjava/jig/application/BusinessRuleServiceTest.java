package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JigServiceTest
class BusinessRuleServiceTest {

    @Test
    void クラス可視性の判定(JigRepository jigRepository) throws Exception {
        List<JigType> jigTypes = jigRepository.fetchJigTypes().list();

        JigType publicType = jigTypes.stream()
                .filter(jigType -> jigType.id().fullQualifiedName().endsWith("PublicType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(JigTypeVisibility.PUBLIC, publicType.visibility());

        JigType protectedType = jigTypes.stream()
                .filter(jigType -> jigType.id().fullQualifiedName().endsWith("ProtectedType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(JigTypeVisibility.PUBLIC, protectedType.visibility());

        JigType defaultType = jigTypes.stream()
                .filter(jigType -> jigType.id().fullQualifiedName().endsWith("DefaultType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(JigTypeVisibility.NOT_PUBLIC, defaultType.visibility());

        JigType privateType = jigTypes.stream()
                .filter(jigType -> jigType.id().fullQualifiedName().endsWith("PrivateType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(JigTypeVisibility.NOT_PUBLIC, privateType.visibility());
    }

    /**
     * @see stub.domain.model.annotation の package-info.java にはアノテーションをつけている
     */
    @Test
    void アノテーションつきのpackage_infoをドメインとして扱わない(JigService jigService, JigRepository jigRepository) {
        var typeIdentifier = TypeIdentifier.valueOf("stub.domain.model.annotation.package-info");

        var jigTypes = jigService.jigTypes(jigRepository);

        assertTrue(jigTypes.resolveJigType(typeIdentifier).isPresent(), "JigTypeには存在する");

        var coreDomainJigTypes = jigService.coreDomainJigTypes(jigRepository);
        assertFalse(coreDomainJigTypes.contains(typeIdentifier), "domain coreには存在しない");
    }
}
