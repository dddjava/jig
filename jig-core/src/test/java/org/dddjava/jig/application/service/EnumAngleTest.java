package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.categories.CategoryAngle;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.RawSource;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class EnumAngleTest {

    @Test
    void readProjectData(ImplementationService implementationService, BusinessRuleService businessRuleService, RawSource source) {
        TypeByteCodes typeByteCodes = implementationService.readProjectData(source);

        CategoryAngles categoryAngles = businessRuleService.categories(typeByteCodes);

        assertThat(categoryAngles.list())
                .extracting(
                        CategoryAngle::typeIdentifier,
                        categoryAngle -> categoryAngle.constantsDeclarationsName(),
                        categoryAngle -> categoryAngle.fieldDeclarations(),
                        categoryAngle -> categoryAngle.userTypeIdentifiers().asSimpleText(),
                        categoryAngle -> categoryAngle.hasParameter(),
                        categoryAngle -> categoryAngle.hasBehaviour(),
                        categoryAngle -> categoryAngle.isPolymorphism()
                ).contains(
                tuple(
                        new TypeIdentifier("stub.domain.model.category.BehaviourEnum"),
                        "[A, B]", "[]", "[AsmByteCodeFactoryTest, RelationEnum]",
                        false, true, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.ParameterizedEnum"),
                        "[A, B]", "[String param]", "[AsmByteCodeFactoryTest, RelationEnum, ValueAngleTest]",
                        true, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.PolymorphismEnum"),
                        "[A, B]", "[]", "[AsmByteCodeFactoryTest, RelationEnum]",
                        false, false, true
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.RelationEnum"),
                        "[A, B, C]", "[RichEnum field]", "[]",
                        true, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.RichEnum"),
                        "[A, B]", "[String param]", "[AsmByteCodeFactoryTest, RelationEnum]",
                        true, true, true
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.SimpleEnum"),
                        "[A, B, C, D]", "[]", "[AsmByteCodeFactoryTest, RelationEnum, ValueAngleTest]",
                        false, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.category.HasStaticFieldEnum"),
                        "[A, B]", "[]", "[]",
                        false, false, false
                )
        );
    }
}
