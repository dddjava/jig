package org.dddjava.jig.domain.model.jigsource.jigloader.architecture;

import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;

/**
 * アーキテクチャの構成要素
 */
public enum BuildingBlock {
    PRESENTATION_CONTROLLER {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            return architecture.isController(typeFact.typeAnnotations());
        }
    },
    API_CONTROLLER {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            // 現状 PRESENTATION_CONTROLLER と同義
            return PRESENTATION_CONTROLLER.satisfy(typeFact, architecture);
        }
    },
    COORDINATOR {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            // 現状 SERVICE と同義
            return SERVICE.satisfy(typeFact, architecture);
        }
    },
    SERVICE {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            return architecture.isService(typeFact.typeAnnotations());
        }
    },
    DATASOURCE {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            return architecture.isDataSource(typeFact);
        }
    },
    BUSINESS_RULE {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            return architecture.isBusinessRule(typeFact);
        }
    };

    public String asText() {
        return name();
    }

    public abstract boolean satisfy(TypeFact typeFact, Architecture architecture);
}
