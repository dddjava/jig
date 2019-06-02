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
    PRESENTATION(ArchitectureBlock.PRESENTATION) {
        @Override
        public TypeByteCodes filter(TypeByteCodes typeByteCodes) {
            List<TypeByteCode> list = typeByteCodes.list().stream()
                    .filter(typeByteCode -> architecture.isController(typeByteCode.typeAnnotations()))
                    .collect(toList());
            return new TypeByteCodes(list);
        }
    },

    APPLICATION(ArchitectureBlock.APPLICATION) {
        @Override
        public TypeByteCodes filter(TypeByteCodes typeByteCodes) {
            List<TypeByteCode> list = typeByteCodes.list().stream()
                    .filter(typeByteCode -> architecture.isService(typeByteCode.typeAnnotations()))
                    .collect(toList());
            return new TypeByteCodes(list);
        }
    },

    INFRASTRUCTURE(ArchitectureBlock.DATASOURCE) {
        @Override
        public TypeByteCodes filter(TypeByteCodes typeByteCodes) {
            List<TypeByteCode> list = typeByteCodes.list().stream()
                    .filter(typeByteCode -> architecture.isDataSource(typeByteCode.typeAnnotations()))
                    .collect(toList());
            return new TypeByteCodes(list);
        }
    };

    Architecture architecture;
    ArchitectureBlock architectureBlock;

    ApplicationLayer(ArchitectureBlock architectureBlock) {
        this.architecture = new Architecture();
        this.architectureBlock = architectureBlock;
    }

    public abstract TypeByteCodes filter(TypeByteCodes typeByteCodes);
}
