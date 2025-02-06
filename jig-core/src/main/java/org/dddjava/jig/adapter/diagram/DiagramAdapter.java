package org.dddjava.jig.adapter.diagram;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.diagrams.ClassRelationDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.information.JigDataProvider;

import java.nio.file.Path;
import java.util.List;

@HandleDocument
public class DiagramAdapter implements Adapter<DiagramSourceWriter> {

    private final JigService jigService;
    private final GraphvizDiagramWriter graphvizDiagramWriter;

    public DiagramAdapter(JigService jigService, GraphvizDiagramWriter graphvizDiagramWriter) {
        this.jigService = jigService;
        this.graphvizDiagramWriter = graphvizDiagramWriter;
    }

    @HandleDocument(JigDocument.PackageRelationDiagram)
    public DiagramSourceWriter packageRelation(JigDataProvider jigDataProvider) {
        return jigService.packageDependencies(jigDataProvider);
    }

    @HandleDocument(JigDocument.CompositeUsecaseDiagram)
    public DiagramSourceWriter CompositeUsecaseDiagram(JigDataProvider jigDataProvider) {
        return new CompositeUsecaseDiagram(jigService.serviceAngles(jigDataProvider));
    }

    @HandleDocument(JigDocument.ArchitectureDiagram)
    public DiagramSourceWriter architectureDiagram(JigDataProvider jigDataProvider) {
        return jigService.architectureDiagram(jigDataProvider);
    }

    @HandleDocument(JigDocument.BusinessRuleRelationDiagram)
    public DiagramSourceWriter businessRuleRelation(JigDataProvider jigDataProvider) {
        return new ClassRelationDiagram(jigService.coreDomainJigTypes(jigDataProvider));
    }

    @HandleDocument(JigDocument.CategoryDiagram)
    public DiagramSourceWriter categories(JigDataProvider jigDataProvider) {
        return jigService.categories(jigDataProvider);
    }

    @HandleDocument(JigDocument.CategoryUsageDiagram)
    public DiagramSourceWriter categoryUsages(JigDataProvider jigDataProvider) {
        return jigService.categoryUsages(jigDataProvider);
    }

    @HandleDocument(JigDocument.ServiceMethodCallHierarchyDiagram)
    public DiagramSourceWriter serviceMethodCallHierarchy(JigDataProvider jigDataProvider) {
        return new ServiceMethodCallHierarchyDiagram(jigService.serviceAngles(jigDataProvider));
    }

    @Override
    public List<Path> write(DiagramSourceWriter result, JigDocument jigDocument) {
        return graphvizDiagramWriter.write(result, jigDocument);
    }
}
