package jig.domain.model.characteristic;

import java.util.Set;

public class Characteristics {

    Set<Characteristic> characteristics;

    public Characteristics(Set<Characteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public Satisfaction has(Characteristic characteristic) {
        return Satisfaction.of(characteristics.contains(characteristic));
    }
}
