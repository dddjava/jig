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
    private final RelationRepository relationRepository;
    private final JapaneseNameRepository japaneseNameRepository;

    public TypeDetail(TypeIdentifier typeIdentifier, Characteristics characteristics, RelationRepository relationRepository, JapaneseNameRepository japaneseNameRepository) {
        this.typeIdentifier = typeIdentifier;
        this.characteristics = characteristics;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public TypeIdentifier type() {
        return typeIdentifier;
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(type());
    }

    public TypeIdentifiers userTypes() {
        TypeIdentifiers fields = relationRepository.findFieldUsage(type());

        MethodIdentifiers methods = relationRepository.findMethodUsage(type());
        TypeIdentifiers ms = methods.typeIdentifiers();

        return fields.merge(ms);
    }

    public boolean is(Characteristic characteristic) {
        return characteristics.has(characteristic);
    }
}
