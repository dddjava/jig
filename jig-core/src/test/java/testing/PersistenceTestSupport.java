package testing;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceTargetOperationTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PersistenceTestSupport {

    public static List<String> tableNames(PersistenceTargetOperationTypes types) {
        return types.persistenceTargets().stream()
                .map(t -> t.persistenceTarget().name())
                .sorted()
                .toList();
    }

    public static PersistenceAccessorOperation persistenceAccessorOf(PersistenceAccessorRepository repository,
                                                                     PersistenceAccessorOperationId id) {
        return persistenceAccessorOptionalOf(repository, id).orElseThrow();
    }

    public static Optional<PersistenceAccessorOperation> persistenceAccessorOptionalOf(PersistenceAccessorRepository repository,
                                                                                       PersistenceAccessorOperationId id) {
        return repository.findByTypeId(id.typeId(), Set.of())
                .stream()
                .flatMap(ops -> ops.persistenceAccessorOperations().stream())
                .filter(operation -> operation.id().equals(id))
                .findFirst();
    }
}
