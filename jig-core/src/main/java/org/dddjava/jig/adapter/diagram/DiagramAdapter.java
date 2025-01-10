package org.dddjava.jig.adapter.diagram;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSource;
import org.dddjava.jig.domain.model.documents.diagrams.ClassRelationDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;
import org.dddjava.jig.infrastructure.view.graphviz.dot.GraphvizDiagramWriter;

import java.nio.file.Path;
import java.util.List;

public class DiagramAdapter implements Adapter<DiagramSourceWriter> {

    private final JigService jigService;
    private final GraphvizDiagramWriter graphvizDiagramWriter;

    public DiagramAdapter(JigService jigService, GraphvizDiagramWriter graphvizDiagramWriter) {
        this.jigService = jigService;
        this.graphvizDiagramWriter = graphvizDiagramWriter;
    }

    @HandleDocument(JigDocument.PackageRelationDiagram)
    public DiagramSourceWriter packageRelation(JigSource jigSource) {
        return jigService.packageDependencies(jigSource);
    }

    @HandleDocument(JigDocument.CompositeUsecaseDiagram)
    public DiagramSourceWriter CompositeUsecaseDiagram(JigSource jigSource) {
        return new CompositeUsecaseDiagram(jigService.serviceAngles(jigSource));
    }

    @HandleDocument(JigDocument.ArchitectureDiagram)
    public DiagramSourceWriter architectureDiagram(JigSource jigSource) {
        return jigService.architectureDiagram(jigSource);
    }

    @HandleDocument(JigDocument.BusinessRuleRelationDiagram)
    public DiagramSourceWriter businessRules(JigSource jigSource) {
        return new ClassRelationDiagram(jigService.businessRules(jigSource));
    }

    @HandleDocument(JigDocument.CategoryDiagram)
    public DiagramSourceWriter categories(JigSource jigSource) {
        return jigService.categories(jigSource);
    }

    @HandleDocument(JigDocument.CategoryUsageDiagram)
    public DiagramSourceWriter categoryUsages(JigSource jigSource) {
        return jigService.categoryUsages(jigSource);
    }

    @HandleDocument(JigDocument.ServiceMethodCallHierarchyDiagram)
    public DiagramSourceWriter serviceMethodCallHierarchy(JigSource jigSource) {
        return jigService.serviceMethodCallHierarchy(jigSource);
    }

    @Override
    public List<Path> write(DiagramSourceWriter result, JigDocument jigDocument) {
        return graphvizDiagramWriter.write(result, jigDocument);
    }
}
