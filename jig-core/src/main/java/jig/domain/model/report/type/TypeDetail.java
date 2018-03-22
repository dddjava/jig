package jig.domain.model.report.type;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifiers;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.RelationRepository;

public class TypeDetail {

    private final Identifier identifier;
    private final Characteristics characteristics;
    private RelationRepository relationRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public TypeDetail(Identifier identifier, Characteristics characteristics, RelationRepository relationRepository, JapaneseNameRepository japaneseNameRepository) {
        this.identifier = identifier;
        this.characteristics = characteristics;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Identifier name() {
        return identifier;
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(name());
    }

    public Identifiers usage() {
        Identifiers fields = relationRepository.findFieldUsage(name());

        MethodIdentifiers methods = relationRepository.findMethodUsage(name());
        Identifiers ms = methods.typeIdentifiers();

        return fields.merge(ms);
    }

    public boolean is(Characteristic characteristic) {
        return characteristics.has(characteristic);
    }
}
