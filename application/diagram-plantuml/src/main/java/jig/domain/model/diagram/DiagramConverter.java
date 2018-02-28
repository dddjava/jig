package jig.domain.model.diagram;

import jig.model.relation.Relations;

public interface DiagramConverter {

    DiagramSource toDiagramSource(Relations things);
}
