package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.TypeDependencies;

public class EnumAngleSource {
    private final TypeIdentifiers typeIdentifiers;
    private final CharacterizedTypes characterizedTypes;
    private final TypeDependencies allTypeDependencies;
    private final FieldDeclarations allFieldDeclarations;
    private final FieldDeclarations allStaticFieldDeclarations;

    public EnumAngleSource(TypeIdentifiers typeIdentifiers, CharacterizedTypes characterizedTypes, TypeDependencies allTypeDependencies, FieldDeclarations allFieldDeclarations, FieldDeclarations allStaticFieldDeclarations) {
        this.typeIdentifiers = typeIdentifiers;
        this.characterizedTypes = characterizedTypes;
        this.allTypeDependencies = allTypeDependencies;
        this.allFieldDeclarations = allFieldDeclarations;
        this.allStaticFieldDeclarations = allStaticFieldDeclarations;
    }

    public TypeIdentifiers getTypeIdentifiers() {
        return typeIdentifiers;
    }

    public CharacterizedTypes getCharacterizedTypes() {
        return characterizedTypes;
    }

    public TypeDependencies getAllTypeDependencies() {
        return allTypeDependencies;
    }

    public FieldDeclarations getAllFieldDeclarations() {
        return allFieldDeclarations;
    }

    public FieldDeclarations getAllStaticFieldDeclarations() {
        return allStaticFieldDeclarations;
    }
}
