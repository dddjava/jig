package jig.domain.model.japanasename;

import jig.domain.model.identifier.TypeIdentifier;

public interface JapaneseNameRepository {

    boolean exists(TypeIdentifier typeIdentifier);

    JapaneseName get(TypeIdentifier typeIdentifier);

    void register(TypeIdentifier fqn, JapaneseName japaneseName);

    JapaneseNames all();
}
