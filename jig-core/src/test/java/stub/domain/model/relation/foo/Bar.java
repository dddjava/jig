package stub.domain.model.relation.foo;

import stub.domain.model.relation.test.FugaException;

public interface Bar {

    Baz toBaz() throws FugaException;
}
