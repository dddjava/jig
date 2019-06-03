package org.dddjava.jig.domain.model.architecture;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 機能のレイヤー。
 * 三層＋ドメインモデルの三層部分を表す。
 */
public enum ApplicationLayer {
    PRESENTATION(BuildingBlock.PRESENTATION) {
        @Override
        boolean match(TypeByteCode typeByteCode) {
            return architecture.isController(typeByteCode.typeAnnotations());
        }
    },

    APPLICATION(BuildingBlock.APPLICATION) {
        @Override
        boolean match(TypeByteCode typeByteCode) {
            return architecture.isService(typeByteCode.typeAnnotations());
        }
    },

    INFRASTRUCTURE(BuildingBlock.DATASOURCE) {
        @Override
        boolean match(TypeByteCode typeByteCode) {
            return architecture.isDataSource(typeByteCode);
        }
    };

    Architecture architecture;
    BuildingBlock buildingBlock;

    ApplicationLayer(BuildingBlock buildingBlock) {
        this.architecture = new Architecture();
        this.buildingBlock = buildingBlock;
    }

    public TypeByteCodes filter(TypeByteCodes typeByteCodes) {
        List<TypeByteCode> list = typeByteCodes.list().stream()
                .filter(this::match)
                .collect(toList());
        return new TypeByteCodes(list);
    }

    abstract boolean match(TypeByteCode typeByteCode);
}
