package org.dddjava.jig.domain.model.jigsource.jigloader.architecture;

import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;

/**
 * 機能のレイヤー。
 * 三層＋ドメインモデルの三層部分を表す。
 */
public enum ApplicationLayer {
    PRESENTATION {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            return BuildingBlock.PRESENTATION_CONTROLLER.satisfy(typeFact, architecture);
        }
    },

    APPLICATION {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            return BuildingBlock.SERVICE.satisfy(typeFact, architecture);
        }
    },

    INFRASTRUCTURE {
        @Override
        public boolean satisfy(TypeFact typeFact, Architecture architecture) {
            return BuildingBlock.DATASOURCE.satisfy(typeFact, architecture);
        }
    };

    public abstract boolean satisfy(TypeFact typeFact, Architecture architecture);
}
