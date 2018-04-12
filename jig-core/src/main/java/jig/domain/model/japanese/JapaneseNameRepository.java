package jig.domain.model.japanese;

import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.type.TypeIdentifier;

public interface JapaneseNameRepository {

    JapaneseName get(TypeIdentifier typeIdentifier);

    void register(TypeIdentifier typeIdentifier, JapaneseName japaneseName);

    boolean exists(PackageIdentifier packageIdentifier);

    JapaneseName get(PackageIdentifier packageIdentifier);

    void register(PackageIdentifier packageIdentifier, JapaneseName japaneseName);
}
