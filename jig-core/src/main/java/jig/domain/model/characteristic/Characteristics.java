package jig.domain.model.characteristic;

import java.util.Set;

public class Characteristics {

    Set<Characteristic> set;

    public Characteristics(Set<Characteristic> set) {
        this.set = set;
    }

    public Satisfaction has(Characteristic characteristic) {
        return Satisfaction.getSatisfaction(set.contains(characteristic));
    }
}
