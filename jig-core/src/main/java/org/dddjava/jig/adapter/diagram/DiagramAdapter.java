package org.dddjava.jig.adapter.diagram;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.diagrams.ClassRelationDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;

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
    public DiagramSourceWriter packageRelation(JigRepository jigRepository) {
        return jigService.packageDependencies(jigRepository);
    }

    @HandleDocument(JigDocument.CompositeUsecaseDiagram)
    public DiagramSourceWriter CompositeUsecaseDiagram(JigRepository jigRepository) {
        return new CompositeUsecaseDiagram(jigService.serviceAngles(jigRepository));
    }

    @HandleDocument(JigDocument.ArchitectureDiagram)
    public DiagramSourceWriter architectureDiagram(JigRepository jigRepository) {
        return jigService.architectureDiagram(jigRepository);
    }

    @HandleDocument(JigDocument.BusinessRuleRelationDiagram)
    public DiagramSourceWriter businessRuleRelation(JigRepository jigRepository) {
        return new ClassRelationDiagram(jigService.coreDomainJigTypes(jigRepository));
    }

    @HandleDocument(JigDocument.CategoryDiagram)
    public DiagramSourceWriter categories(JigRepository jigRepository) {
        return jigService.categories(jigRepository);
    }

    @HandleDocument(JigDocument.CategoryUsageDiagram)
    public DiagramSourceWriter categoryUsages(JigRepository jigRepository) {
        return jigService.categoryUsages(jigRepository);
    }

    @HandleDocument(JigDocument.ServiceMethodCallHierarchyDiagram)
    public DiagramSourceWriter serviceMethodCallHierarchy(JigRepository jigRepository) {
        return new ServiceMethodCallHierarchyDiagram(jigService.serviceAngles(jigRepository));
    }

    @Override
    public List<Path> write(DiagramSourceWriter result, JigDocument jigDocument) {
        return graphvizDiagramWriter.write(result, jigDocument);
    }
}
