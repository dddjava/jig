package jig.domain.model.characteristic;

import java.util.Set;

public class Characteristics {

    Set<Characteristic> set;

    public Characteristics(Set<Characteristic> set) {
        this.set = set;
    }

    public boolean has(Characteristic characteristic) {
        return set.contains(characteristic);
    }
}
