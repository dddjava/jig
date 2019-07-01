package org.dddjava.jig.domain.model.architecture;

import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCodes;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 機能のレイヤー。
 * 三層＋ドメインモデルの三層部分を表す。
 */
public enum ApplicationLayer {
    PRESENTATION(BuildingBlock.PRESENTATION) {
        @Override
        boolean match(TypeByteCode typeByteCode, Architecture architecture) {
            return architecture.isController(typeByteCode.typeAnnotations());
        }
    },

    APPLICATION(BuildingBlock.APPLICATION) {
        @Override
        boolean match(TypeByteCode typeByteCode, Architecture architecture) {
            return architecture.isService(typeByteCode.typeAnnotations());
        }
    },

    INFRASTRUCTURE(BuildingBlock.DATASOURCE) {
        @Override
        boolean match(TypeByteCode typeByteCode, Architecture architecture) {
            return architecture.isDataSource(typeByteCode);
        }
    };

    BuildingBlock buildingBlock;

    ApplicationLayer(BuildingBlock buildingBlock) {
        this.buildingBlock = buildingBlock;
    }

    public TypeByteCodes filter(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<TypeByteCode> list = typeByteCodes.list().stream()
                .filter(typeByteCode -> match(typeByteCode, architecture))
                .collect(toList());
        return new TypeByteCodes(list);
    }

    abstract boolean match(TypeByteCode typeByteCode, Architecture architecture);
}
