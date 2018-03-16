package jig.domain.model.report.method;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlType;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

import java.util.stream.Collectors;

public class MethodDetail {

    private Relation methodRelation;
    private RelationRepository relationRepository;
    private SqlRepository sqlRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public MethodDetail(Relation methodRelation,
                        RelationRepository relationRepository,
                        SqlRepository sqlRepository,
                        JapaneseNameRepository japaneseNameRepository) {
        this.methodRelation = methodRelation;
        this.relationRepository = relationRepository;
        this.sqlRepository = sqlRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Name name() {
        return methodRelation.from();
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(name());
    }

    public Name methodName() {
        return methodRelation.to();
    }

    public Name returnTypeName() {
        Relation relation = relationRepository.get(methodName(), RelationType.METHOD_RETURN_TYPE);
        return relation.to();
    }

    public Names instructFields() {
        Relations relations = relationRepository.find(methodName(), RelationType.METHOD_USE_TYPE);
        return relations.list().stream().map(Relation::to).collect(Names.collector());
    }

    public Name datasourceMethod() {
        return relationRepository.findToOne(methodName(), RelationType.IMPLEMENT)
                .map(Relation::from)
                .orElseGet(() -> {
                    // TODO ログ？
                    return new Name("---");
                });
    }

    public Names instructMapperMethodNames() {
        Relations relations = relationRepository.find(datasourceMethod(), RelationType.METHOD_USE_METHOD);
        return relations.list().stream().map(Relation::to).collect(Names.collector());
    }

    public String useTableNames() {
        StringBuilder sb = new StringBuilder();
        for (Name name : instructMapperMethodNames().list()) {
            Sql sql = sqlRepository.get(name);
            sb.append(sql.tableName());
        }
        return sb.toString();
    }

    public String crud() {
        return instructMapperMethodNames()
                .list().stream()
                .map(sqlRepository::get)
                .map(Sql::sqlType)
                .map(SqlType::name)
                .collect(Collectors.joining(","));
    }
}
