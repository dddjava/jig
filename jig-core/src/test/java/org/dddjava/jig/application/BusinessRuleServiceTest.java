package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.sources.JigRepository;
import org.junit.jupiter.api.Test;
import stub.domain.model.relation.ClassDefinition;
import testing.JigServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JigServiceTest
class BusinessRuleServiceTest {

    @Test
    void クラス可視性の判定(JigRepository jigRepository) throws Exception {
        List<JigType> jigTypes = jigRepository.fetchJigTypes().list();

        JigType publicType = jigTypes.stream()
                .filter(jigType -> jigType.identifier().fullQualifiedName().endsWith("PublicType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(JigTypeVisibility.PUBLIC, publicType.visibility());

        JigType protectedType = jigTypes.stream()
                .filter(jigType -> jigType.identifier().fullQualifiedName().endsWith("ProtectedType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(JigTypeVisibility.PUBLIC, protectedType.visibility());

        JigType defaultType = jigTypes.stream()
                .filter(jigType -> jigType.identifier().fullQualifiedName().endsWith("DefaultType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(JigTypeVisibility.NOT_PUBLIC, defaultType.visibility());

        JigType privateType = jigTypes.stream()
                .filter(jigType -> jigType.identifier().fullQualifiedName().endsWith("PrivateType"))
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

    @Test
    void 関連(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.jigTypes(jigRepository);

        var targetJigType = jigTypes.resolveJigType(TypeIdentifier.from(ClassDefinition.class)).orElseThrow();
        var classRelations = ClassRelations.internalTypeRelationsFrom(jigTypes, targetJigType);

        assertEquals("""
                        "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.ClassAnnotation";
                        "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.GenericsParameter";
                        "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.ImplementA";
                        "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.ImplementB";
                        "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.SuperClass";""",
                classRelations.dotText());
    }
}
