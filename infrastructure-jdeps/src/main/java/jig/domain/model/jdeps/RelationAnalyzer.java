package jig.domain.model.jdeps;

import jig.domain.model.relation.Relations;

public interface RelationAnalyzer {

    Relations analyzeRelations(AnalysisCriteria criteria);
}
