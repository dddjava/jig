package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigmodel.businessrules.ValueKind;
import org.dddjava.jig.domain.model.jigmodel.collections.CollectionAngle;
import org.dddjava.jig.domain.model.jigmodel.collections.CollectionAngles;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.values.ValueAngle;
import org.dddjava.jig.domain.model.jigmodel.values.ValueAngles;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.junit.jupiter.api.Test;
import stub.domain.model.category.ParameterizedEnum;
import stub.domain.model.category.SimpleEnum;
import stub.domain.model.type.*;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.domain.model.type.fuga.FugaName;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;

@JigServiceTest
public class ValueAngleTest {

    @Test
    void readProjectData(BusinessRuleService service, Sources sources, JigSourceReadService jigSourceReadService) {
        jigSourceReadService.readProjectData(sources);
        ValueAngles identifiers = service.values(ValueKind.IDENTIFIER);
        assertThat(identifiers.list())
                .extracting(ValueAngle::typeIdentifier)
                .contains(
                        new TypeIdentifier(SimpleIdentifier.class),
                        new TypeIdentifier(FugaIdentifier.class),
                        new TypeIdentifier(FugaName.class)
                )
                .doesNotContain(
                        new TypeIdentifier(SimpleEnum.class),
                        new TypeIdentifier(ParameterizedEnum.class)
                );

        ValueAngles numbers = service.values(ValueKind.NUMBER);
        assertThat(numbers.list())
                .extracting(ValueAngle::typeIdentifier)
                .contains(
                        new TypeIdentifier(SimpleNumber.class),
                        new TypeIdentifier(IntegerNumber.class),
                        new TypeIdentifier(LongNumber.class),
                        new TypeIdentifier(PrimitiveIntNumber.class),
                        new TypeIdentifier(PrimitiveLongNumber.class)
                );

        ValueAngles dates = service.values(ValueKind.DATE);
        assertThat(dates.list())
                .extracting(ValueAngle::typeIdentifier)
                .contains(
                        new TypeIdentifier(SimpleDate.class)
                );

        ValueAngles terms = service.values(ValueKind.TERM);
        assertThat(terms.list())
                .extracting(ValueAngle::typeIdentifier)
                .contains(
                        new TypeIdentifier(SimpleTerm.class)
                );

        CollectionAngles collections = service.collections();
        assertThat(collections.list())
                .extracting(CollectionAngle::typeIdentifier)
                .contains(
                        new TypeIdentifier(SimpleCollection.class),
                        new TypeIdentifier(SetCollection.class)
                );
    }
}
