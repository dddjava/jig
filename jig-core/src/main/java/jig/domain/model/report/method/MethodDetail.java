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
import jig.domain.model.relation.GenericRelation;
import jig.domain.model.relation.RelationRepository;

import java.util.ArrayList;
import java.util.List;

public class MethodDetail {

    private GenericRelation<Identifier, MethodIdentifier> methodRelation;
    private RelationRepository relationRepository;
    private CharacteristicRepository characteristicRepository;
    private SqlRepository sqlRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public MethodDetail(GenericRelation<Identifier, MethodIdentifier> methodRelation,
                        RelationRepository relationRepository,
                        CharacteristicRepository characteristicRepository,
                        SqlRepository sqlRepository,
                        JapaneseNameRepository japaneseNameRepository) {
        this.methodRelation = methodRelation;
        this.relationRepository = relationRepository;
        this.characteristicRepository = characteristicRepository;
        this.sqlRepository = sqlRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Identifier name() {
        return methodRelation.from();
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(name());
    }

    public MethodIdentifier methodName() {
        return methodRelation.to();
    }

    public Identifier returnTypeName() {
        return relationRepository.getReturnTypeOf(methodName());
    }

    public Identifiers instructFields() {
        return relationRepository.findUseTypeOf(methodName());
    }

    public MethodIdentifiers instructMapperMethodNames() {
        return relationRepository.findConcrete(methodName())
                .map(relationRepository::findUseMethod)
                .filter(mapperMethod -> characteristicRepository.has(mapperMethod, Characteristic.MAPPER_METHOD));
    }

    public Sqls sqls() {
        List<Sql> sqls = new ArrayList<>();
        for (MethodIdentifier identifier : instructMapperMethodNames().list()) {
            sqlRepository.find(identifier).ifPresent(sqls::add);
        }
        return new Sqls(sqls);
    }
}
