package jig.domain.model.report.method;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.Sqls;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.thing.Identifier;
import jig.domain.model.thing.Identifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MethodDetail {

    private Relation methodRelation;
    private RelationRepository relationRepository;
    private CharacteristicRepository characteristicRepository;
    private SqlRepository sqlRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public MethodDetail(Relation methodRelation,
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

    public Identifier methodName() {
        return methodRelation.to();
    }

    public Identifier returnTypeName() {
        Relation relation = relationRepository.get(methodName(), RelationType.METHOD_RETURN_TYPE);
        return relation.to();
    }

    public Identifiers instructFields() {
        Relations relations = relationRepository.find(methodName(), RelationType.METHOD_USE_TYPE);
        return relations.list().stream().map(Relation::to).collect(Identifiers.collector());
    }

    public Optional<Identifier> datasourceMethod() {
        return relationRepository
                .findToOne(methodName(), RelationType.IMPLEMENT)
                .map(Relation::from);
    }

    public Identifiers instructMapperMethodNames() {
        return datasourceMethod().map(name -> {
            Relations relations = relationRepository.find(name, RelationType.METHOD_USE_METHOD);
            return relations.list().stream()
                    .map(Relation::to)
                    .filter(mapperMethod -> characteristicRepository.has(mapperMethod, Characteristic.MAPPER_METHOD))
                    .collect(Identifiers.collector());
        }).orElse(Identifiers.empty());
    }

    public Sqls sqls() {
        List<Sql> sqls = new ArrayList<>();
        for (Identifier identifier : instructMapperMethodNames().list()) {
            sqlRepository.find(identifier).ifPresent(sqls::add);
        }
        return new Sqls(sqls);
    }
}
