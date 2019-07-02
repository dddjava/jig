package org.dddjava.jig.domain.model.interpret.architecture;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 機能のレイヤー。
 * 三層＋ドメインモデルの三層部分を表す。
 */
public enum ApplicationLayer {
    PRESENTATION {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            return BuildingBlock.PRESENTATION_CONTROLLER.satisfy(typeByteCode, architecture);
        }
    },

    APPLICATION {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            return BuildingBlock.SERVICE.satisfy(typeByteCode, architecture);
        }
    },

    INFRASTRUCTURE {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            return BuildingBlock.DATASOURCE.satisfy(typeByteCode, architecture);
        }
    };

    public TypeByteCodes filter(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<TypeByteCode> list = typeByteCodes.list().stream()
                .filter(typeByteCode -> satisfy(typeByteCode, architecture))
                .collect(toList());
        return new TypeByteCodes(list);
    }

    public abstract boolean satisfy(TypeByteCode typeByteCode, Architecture architecture);
}
