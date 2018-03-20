package jig.domain.model.japanasename;

import jig.domain.model.thing.Identifier;

public interface JapaneseNameRepository {

    boolean exists(Identifier identifier);

    JapaneseName get(Identifier identifier);

    void register(Identifier fqn, JapaneseName japaneseName);

    JapaneseNames all();
}
