package org.dddjava.jig.domain.model.sources.jigfactory;

import java.util.List;

/**
 * classファイル由来のソース
 *
 * TODO 一旦JigTypeBuilderを持つが、JigTypeBuilderを廃止していく方向
 */
public record ClassSourceModel(List<JigTypeBuilder> jigTypeBuilders) {

    public static ClassSourceModel from(List<JigTypeBuilder> list) {
        return new ClassSourceModel(list);
    }
}
