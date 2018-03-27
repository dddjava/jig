package jig.domain.model.jdeps;

import jig.domain.model.relation.dependency.PackageDependencies;

public interface RelationAnalyzer {

    PackageDependencies analyzeRelations(AnalysisCriteria criteria);
}
