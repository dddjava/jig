package jig.domain.model.diagram;

import jig.domain.model.relation.dependency.PackageDependencies;

public interface DiagramConverter {

    DiagramSource toDiagramSource(PackageDependencies things);
}
