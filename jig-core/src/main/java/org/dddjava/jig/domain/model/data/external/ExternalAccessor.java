package org.dddjava.jig.domain.model.data.external;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;

public record ExternalAccessor(
        TypeId typeId,
        Collection<ExternalAccessorOperation> operations) {
}
