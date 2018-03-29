package jig.domain.model.report.method;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.Sqls;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.RelationRepository;

import java.util.ArrayList;
import java.util.List;

public class MethodDetail {

    private final TypeIdentifier typeIdentifier;
    private final MethodIdentifier methodIdentifier;
    private final RelationRepository relationRepository;
    private final CharacteristicRepository characteristicRepository;
    private final SqlRepository sqlRepository;
    private final JapaneseNameRepository japaneseNameRepository;
    private final TypeIdentifierFormatter typeIdentifierFormatter;

    // TODO repositoryうけとるのやめたい
    public MethodDetail(TypeIdentifier typeIdentifier,
                        MethodIdentifier methodIdentifier,
                        RelationRepository relationRepository,
                        CharacteristicRepository characteristicRepository,
                        SqlRepository sqlRepository,
                        JapaneseNameRepository japaneseNameRepository,
                        TypeIdentifierFormatter typeIdentifierFormatter) {
        this.typeIdentifier = typeIdentifier;
        this.methodIdentifier = methodIdentifier;
        this.relationRepository = relationRepository;
        this.characteristicRepository = characteristicRepository;
        this.sqlRepository = sqlRepository;
        this.japaneseNameRepository = japaneseNameRepository;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
    }

    public TypeIdentifier type() {
        return typeIdentifier;
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(type());
    }

    public MethodIdentifier method() {
        return methodIdentifier;
    }

    public TypeIdentifier returnType() {
        return relationRepository.getReturnTypeOf(method());
    }

    public TypeIdentifiers usingFieldTypes() {
        return relationRepository.findUseTypeOf(method());
    }

    public MethodIdentifiers instructMapperMethodIdentifiers() {
        return relationRepository.findConcrete(method())
                .map(relationRepository::findUseMethod)
                .filter(methodIdentifier -> characteristicRepository.has(methodIdentifier.declaringType(), Characteristic.MAPPER));
    }

    public Sqls sqls() {
        List<Sql> sqls = new ArrayList<>();
        for (MethodIdentifier identifier : instructMapperMethodIdentifiers().list()) {
            sqlRepository.find(identifier).ifPresent(sqls::add);
        }
        return new Sqls(sqls);
    }

    public String typeName() {
        return type().format(typeIdentifierFormatter);
    }
}
