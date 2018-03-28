package jig.domain.model.report.type;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.identifier.MethodIdentifiers;
import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.TypeIdentifiers;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.RelationRepository;

public class TypeDetail {

    private final TypeIdentifier typeIdentifier;
    private final Characteristics characteristics;
    private RelationRepository relationRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public TypeDetail(TypeIdentifier typeIdentifier, Characteristics characteristics, RelationRepository relationRepository, JapaneseNameRepository japaneseNameRepository) {
        this.typeIdentifier = typeIdentifier;
        this.characteristics = characteristics;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public TypeIdentifier name() {
        return typeIdentifier;
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(name());
    }

    public TypeIdentifiers usage() {
        TypeIdentifiers fields = relationRepository.findFieldUsage(name());

        MethodIdentifiers methods = relationRepository.findMethodUsage(name());
        TypeIdentifiers ms = methods.typeIdentifiers();

        return fields.merge(ms);
    }

    public boolean is(Characteristic characteristic) {
        return characteristics.has(characteristic);
    }
}
