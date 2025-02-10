package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigTypesRepository;
import org.dddjava.jig.domain.model.data.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.data.classes.method.UsingMethods;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.type.JigTypes;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaIdentifier;
import testing.JigServiceTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JigServiceTest
class ServiceMethodTest {

    @Test
    void name(JigService jigService, JigTypesRepository jigTypesRepository) {
        JigTypes jigTypes = jigService.jigTypes(jigTypesRepository);

        var targetType = jigTypes.resolveJigType(TypeIdentifier.from(CanonicalService.class)).orElseThrow();
        ServiceMethod sut = targetType.allJigMethodStream()
                .filter(jigMethod -> jigMethod.declaration().methodSignature().methodName().equals("fuga"))
                .findAny()
                .map(jigMethod -> new ServiceMethod(jigMethod, new CallerMethods(List.of())))
                .orElseThrow();

        Optional<TypeIdentifier> primaryType = sut.primaryType();
        assertEquals(TypeIdentifier.from(Fuga.class), primaryType.orElseThrow());

        List<TypeIdentifier> requireTypes = sut.requireTypes();
        assertEquals(1, requireTypes.size());
        assertEquals(TypeIdentifier.from(FugaIdentifier.class), requireTypes.get(0));

        UsingMethods usingMethods = sut.usingMethods();
        assertEquals(2, usingMethods.methodDeclarations().list().size());

        // 使用しているフィールドの型などはカウントされないので0になる。
        List<TypeIdentifier> typeIdentifiers = sut.internalUsingTypes();
        assertEquals(0, typeIdentifiers.size());

        // カウントするパターンのテストを足す
    }
}