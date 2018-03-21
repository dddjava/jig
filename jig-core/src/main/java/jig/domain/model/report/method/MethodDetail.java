package jig.domain.model.report.method;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.Sqls;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.identifier.MethodIdentifiers;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.RelationRepository;

import java.util.ArrayList;
import java.util.List;

public class MethodDetail {

    private final Identifier identifier;
    private final MethodIdentifier methodIdentifier;
    private RelationRepository relationRepository;
    private CharacteristicRepository characteristicRepository;
    private SqlRepository sqlRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public MethodDetail(Identifier identifier,
                        MethodIdentifier methodIdentifier,
                        RelationRepository relationRepository,
                        CharacteristicRepository characteristicRepository,
                        SqlRepository sqlRepository,
                        JapaneseNameRepository japaneseNameRepository) {
        this.identifier = identifier;
        this.methodIdentifier = methodIdentifier;
        this.relationRepository = relationRepository;
        this.characteristicRepository = characteristicRepository;
        this.sqlRepository = sqlRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Identifier typeIdentifier() {
        return identifier;
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(typeIdentifier());
    }

    public MethodIdentifier methodIdentifier() {
        return methodIdentifier;
    }

    public Identifier returnTypeIdentifier() {
        return relationRepository.getReturnTypeOf(methodIdentifier());
    }

    public Identifiers instructFields() {
        return relationRepository.findUseTypeOf(methodIdentifier());
    }

    public MethodIdentifiers instructMapperMethodIdentifiers() {
        return relationRepository.findConcrete(methodIdentifier())
                .map(relationRepository::findUseMethod)
                .filter(mapperMethod -> characteristicRepository.has(mapperMethod, Characteristic.MAPPER_METHOD));
    }

    public Sqls sqls() {
        List<Sql> sqls = new ArrayList<>();
        for (MethodIdentifier identifier : instructMapperMethodIdentifiers().list()) {
            sqlRepository.find(identifier).ifPresent(sqls::add);
        }
        return new Sqls(sqls);
    }
}
