package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.categories.CategoryCharacteristic;
import org.dddjava.jig.domain.model.categories.CategoryCharacteristics;
import org.dddjava.jig.domain.model.categories.CategoryType;
import org.dddjava.jig.domain.model.categories.CategoryTypes;
import org.dddjava.jig.domain.model.characteristic.*;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.unit.method.Methods;
import org.dddjava.jig.domain.model.values.PotentiallyValueType;
import org.dddjava.jig.domain.model.values.PotentiallyValueTypes;
import org.dddjava.jig.domain.model.values.ValueTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * プロジェクトから読み取った情報
 */
public class ProjectData {

    private final TypeByteCodes typeByteCodes;

    // データソースアクセス
    private Sqls sqls;

    // 特徴とセットになったもの
    private CharacterizedTypes characterizedTypes;

    public ProjectData(TypeByteCodes typeByteCodes, Sqls sqls, CharacterizedTypeFactory characterizedTypeFactory) {
        this.typeByteCodes = typeByteCodes;

        CharacterizedTypes characterizedTypes = new CharacterizedTypes(typeByteCodes, characterizedTypeFactory);
        this.characterizedTypes = characterizedTypes;

        this.sqls = sqls;
    }

    public Sqls sqls() {
        return sqls;
    }

    public CharacterizedTypes characterizedTypes() {
        return characterizedTypes;
    }

    public ValueTypes valueTypes() {
        ArrayList<PotentiallyValueType> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            list.add(new PotentiallyValueType(typeByteCode.typeIdentifier(), typeByteCode.fieldDeclarations()));
        }
        PotentiallyValueTypes potentiallyValueTypes = new PotentiallyValueTypes(list);

        return potentiallyValueTypes.toValueTypes(categories());
    }

    public Methods methods() {
        return typeByteCodes.instanceMethods();
    }

    public Methods controllerMethods() {
        return methods().controllerMethods(characterizedTypes);
    }

    public CategoryTypes categories() {
        TypeIdentifiers enumTypeIdentifies = characterizedTypes().stream()
                .filter(Characteristic.ENUM)
                .typeIdentifiers();

        ArrayList<CategoryType> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : enumTypeIdentifies.list()) {
            Characteristics characteristics = characterizedTypes.stream()
                    .pickup(typeIdentifier)
                    .characteristics();

            // TODO ifなくしたいけどCharacteristicをなくしてから考える
            List<CategoryCharacteristic> categoryCharacteristicList = new ArrayList<>();
            if (characteristics.has(Characteristic.ENUM_BEHAVIOUR)) {
                categoryCharacteristicList.add(CategoryCharacteristic.BEHAVIOUR);
            }
            if (characteristics.has(Characteristic.ENUM_PARAMETERIZED)) {
                categoryCharacteristicList.add(CategoryCharacteristic.PARAMETERIZED);
            }
            if (characteristics.has(Characteristic.ENUM_POLYMORPHISM)) {
                categoryCharacteristicList.add(CategoryCharacteristic.POLYMORPHISM);
            }

            list.add(new CategoryType(typeIdentifier, new CategoryCharacteristics(categoryCharacteristicList)));
        }

        return new CategoryTypes(list);
    }

    public TypeIdentifiers repositories() {
        return characterizedTypes().stream()
                .filter(Characteristic.REPOSITORY)
                .typeIdentifiers();
    }

    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
    }
}
