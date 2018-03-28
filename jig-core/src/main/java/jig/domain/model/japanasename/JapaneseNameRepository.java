package jig.domain.model.japanasename;

import jig.domain.model.identifier.PackageIdentifier;
import jig.domain.model.identifier.TypeIdentifier;

public interface JapaneseNameRepository {

    boolean exists(TypeIdentifier typeIdentifier);

    JapaneseName get(TypeIdentifier typeIdentifier);

    void register(TypeIdentifier typeIdentifier, JapaneseName japaneseName);

    boolean exists(PackageIdentifier packageIdentifier);

    JapaneseName get(PackageIdentifier packageIdentifier);

    void register(PackageIdentifier packageIdentifier, JapaneseName japaneseName);
}
