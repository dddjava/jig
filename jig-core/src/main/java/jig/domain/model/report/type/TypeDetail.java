package jig.domain.model.report.type;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.Satisfaction;
import jig.domain.model.characteristic.TypeCharacteristics;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.relation.RelationRepository;

public class TypeDetail {

    private final TypeIdentifier typeIdentifier;
    private final TypeCharacteristics typeCharacteristics;
    private final RelationRepository relationRepository;
    private final JapaneseName japaneseName;
    private final TypeIdentifierFormatter typeIdentifierFormatter;

    // TODO repositoryうけとるのやめたい
    public TypeDetail(TypeIdentifier typeIdentifier,
                      TypeCharacteristics typeCharacteristics,
                      RelationRepository relationRepository,
                      JapaneseName japaneseName,
                      TypeIdentifierFormatter typeIdentifierFormatter) {
        this.typeIdentifier = typeIdentifier;
        this.typeCharacteristics = typeCharacteristics;
        this.relationRepository = relationRepository;
        this.japaneseName = japaneseName;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
    }

    public TypeIdentifier type() {
        return typeIdentifier;
    }

    public JapaneseName japaneseName() {
        return japaneseName;
    }

    public TypeIdentifiers userTypes() {
        TypeIdentifiers userTypes = relationRepository.findFieldUsage(type());

        MethodDeclarations userMethods = relationRepository.findMethodUsage(type());
        TypeIdentifiers methodOwners = userMethods.declaringTypes();

        return userTypes.merge(methodOwners);
    }

    public Satisfaction satisfied(Characteristic characteristic) {
        return typeCharacteristics.has(characteristic);
    }

    public String typeName() {
        return type().format(typeIdentifierFormatter);
    }

    public FieldDeclarations constants() {
        return relationRepository.findConstants(type());
    }

    public FieldDeclarations fieldIdentifiers() {
        return relationRepository.findFieldsOf(typeIdentifier);
    }
}
