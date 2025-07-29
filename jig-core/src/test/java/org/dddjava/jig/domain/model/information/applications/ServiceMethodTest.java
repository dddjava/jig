package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaIdentifier;
import testing.JigServiceTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JigServiceTest
class ServiceMethodTest {

    @Test
    void name(JigService jigService, JigRepository jigRepository) {
        JigTypes jigTypes = jigService.jigTypes(jigRepository);

        var targetType = jigTypes.resolveJigType(TypeId.from(CanonicalService.class)).orElseThrow();
        ServiceMethod sut = targetType.allJigMethodStream()
                .filter(jigMethod -> jigMethod.name().equals("fuga"))
                .findAny()
                .map(jigMethod -> new ServiceMethod(jigMethod, new CallerMethods(Set.of())))
                .orElseThrow();

        Optional<TypeId> primaryType = sut.primaryType();
        assertEquals(TypeId.from(Fuga.class), primaryType.orElseThrow());

        List<TypeId> requireTypes = sut.requireTypes();
        assertEquals(1, requireTypes.size());
        assertEquals(TypeId.from(FugaIdentifier.class), requireTypes.get(0));

        UsingMethods usingMethods = sut.usingMethods();
        assertEquals(2, usingMethods.invokedMethodStream().count());

        // 使用しているフィールドの型などはカウントされないので0になる。
        // TODO これまではMethodのシグネチャからとっていたので０になっていたが、InternalUsingTypesでInvokedMethodがとれるようになり、
        //   使用しているフィールドの型などもとれるようになった。（現状でも多すぎだが、ここまでくると流石に多くすぎでは？）
        //List<TypeIdentifier> typeIdentifiers = sut.internalUsingTypes();
        //assertEquals(0, typeIdentifiers.size(), () -> typeIdentifiers.toString());

        // カウントするパターンのテストを足す
    }
}