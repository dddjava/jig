package jig.domain.model.report.method;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.Sqls;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
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
    private final MethodDeclaration methodDeclaration;
    private final RelationRepository relationRepository;
    private final CharacteristicRepository characteristicRepository;
    private final SqlRepository sqlRepository;
    private final JapaneseNameRepository japaneseNameRepository;
    private final TypeIdentifierFormatter typeIdentifierFormatter;

    // TODO repositoryうけとるのやめたい
    public MethodDetail(TypeIdentifier typeIdentifier,
                        MethodDeclaration methodDeclaration,
                        RelationRepository relationRepository,
                        CharacteristicRepository characteristicRepository,
                        SqlRepository sqlRepository,
                        JapaneseNameRepository japaneseNameRepository,
                        TypeIdentifierFormatter typeIdentifierFormatter) {
        this.typeIdentifier = typeIdentifier;
        this.methodDeclaration = methodDeclaration;
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

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public TypeIdentifier returnType() {
        return relationRepository.getReturnTypeOf(method());
    }

    public TypeIdentifiers usingFieldTypes() {
        return relationRepository.findUseFields(method()).toTypeIdentifies();
    }

    public MethodDeclarations instructMapperMethodIdentifiers() {
        return relationRepository.findConcrete(method())
                .map(relationRepository::findUseMethod)
                .filter(methodIdentifier -> characteristicRepository.has(methodIdentifier.declaringType(), Characteristic.MAPPER));
    }

    public Sqls sqls() {
        List<Sql> sqls = new ArrayList<>();
        for (MethodDeclaration identifier : instructMapperMethodIdentifiers().list()) {
            sqlRepository.find(identifier).ifPresent(sqls::add);
        }
        return new Sqls(sqls);
    }

    public String typeName() {
        return type().format(typeIdentifierFormatter);
    }

    public MethodDeclarations repositoryMethods() {
        return relationRepository.findUseMethod(methodDeclaration)
                .filter(useMethod -> characteristicRepository.has(useMethod.declaringType(), Characteristic.REPOSITORY));
    }
}
