package jig.domain.model.diagram;

import jig.domain.model.relation.Relations;

public interface DiagramConverter {

    DiagramSource toDiagramSource(Relations things);
}
