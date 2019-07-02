package org.dddjava.jig.domain.model.interpret.architecture;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

/**
 * アーキテクチャの構成要素
 */
public enum BuildingBlock {
    PRESENTATION_CONTROLLER {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            return architecture.isController(typeByteCode.typeAnnotations());
        }
    },
    API_CONTROLLER {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            // 現状 PRESENTATION_CONTROLLER と同義
            return PRESENTATION_CONTROLLER.satisfy(typeByteCode, architecture);
        }
    },
    COORDINATOR {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            // 現状 SERVICE と同義
            return SERVICE.satisfy(typeByteCode, architecture);
        }
    },
    SERVICE {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            return architecture.isService(typeByteCode.typeAnnotations());
        }
    },
    DATASOURCE {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            return architecture.isDataSource(typeByteCode);
        }
    },
    BUSINESS_RULE {
        @Override
        public boolean satisfy(TypeByteCode typeByteCode, Architecture architecture) {
            return architecture.isBusinessRule(typeByteCode);
        }
    };

    public String asText() {
        return name();
    }

    public abstract boolean satisfy(TypeByteCode typeByteCode, Architecture architecture);
}
