package jig.domain.model.tag;

import jig.domain.model.thing.Name;

public interface JapaneseNameRepository {

    boolean exists(Name name);

    JapaneseName get(Name name);

    void register(Name fqn, JapaneseName japaneseName);

    JapaneseNames all();
}
